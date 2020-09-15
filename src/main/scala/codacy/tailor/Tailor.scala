package codacy.tailor

import java.nio.file.{Path, Paths}

import better.files.File
import com.codacy.plugins.api._
import com.codacy.plugins.api.results.Result.{FileError, Issue}
import com.codacy.plugins.api.results.Tool.Specification
import com.codacy.plugins.api.results.{Pattern, Result, Tool}
import com.codacy.plugins.api.{Options, Source}
import com.codacy.tools.scala.seed.utils.{CommandResult, CommandRunner}
import com.codacy.tools.scala.seed.utils.ToolHelper._
import play.api.libs.json._

import scala.util.{Failure, Properties, Success, Try}

case class TailorViolationLocation(line: Int, column: Option[Int])

case class TailorViolation(severity: String, rule: String, location: TailorViolationLocation, message: String)

case class TailorFile(path: String, violations: List[TailorViolation], parsed: Boolean)

object TailorFile {
  implicit val tailorViolationLocationFmt = Json.format[TailorViolationLocation]
  implicit val tailorViolationFmt = Json.format[TailorViolation]
  implicit val tailorFileFmt = Json.format[TailorFile]
}

object Tailor extends Tool {

  private lazy val configFileNames = Set(".tailor.yml")

  private def nativeConfigurationFromSource(source: Source.Directory): Option[List[String]] = {
    configFileNames
      .map(name => Try(File(source.path) / name))
      .collectFirst {
        case Success(file) if file.isRegularFile =>
          List("-c", file.toJava.getAbsolutePath)
      }
  }

  private def patternsToUseFromCodacyConfig(
      configuration: Option[List[Pattern.Definition]]
  )(implicit specification: Specification): Option[List[String]] = {
    val patternsToLintOpt = configuration.withDefaultParameters

    patternsToLintOpt.map {
      case patternsToLint if patternsToLint.nonEmpty =>
        val patternIds = patternsToLint.map(_.patternId)
        val parameters =
          patternsToLint.flatMap(_.parameters).flatMap { parameter =>
            List(s"--${parameter.name}", paramValueToJsValue(parameter.value).toString)
          }
        List("--only=" + patternIds.mkString(",")) ++ parameters
      case _ => List.empty[String]
    }
  }

  private def lintingConfiguration(source: Source.Directory, configuration: Option[List[Pattern.Definition]])(
      implicit specification: Specification
  ): List[String] = {
    lazy val nativeConfig = nativeConfigurationFromSource(source)

    patternsToUseFromCodacyConfig(configuration)
      .orElse(nativeConfig)
      .getOrElse(List.empty)
  }

  private def failureMessage(command: List[String], resultFromTool: CommandResult, e: Throwable): String = {
    s"""
       |${this.getClass.getSimpleName} exited with code ${resultFromTool.exitCode}
       |command: ${command.mkString(" ")}
       |message: ${e.getMessage}
       |stdout: ${resultFromTool.stdout.mkString(Properties.lineSeparator)}
       |stderr: ${resultFromTool.stderr.mkString(Properties.lineSeparator)}
                """.stripMargin
  }

  def listOfFilesToLint(source: Source.Directory, files: Option[Set[Source.File]]): List[String] = {
    files.fold(List(source.path)) { paths =>
      paths.map(_.toString).toList
    }
  }

  def commandToRun(patternsConfig: List[String], filesToLint: List[String]): List[String] = {
    List("/usr/bin/tailor/bin/tailor", "-f", "json") ++ patternsConfig ++ List("--") ++ filesToLint
  }

  def runCommandOnSourceDir(command: List[String], source: Source.Directory): Try[List[Result]] = {
    CommandRunner.exec(command) match {
      case Right(resultFromTool) =>
        parseToolResult(Paths.get(source.path), resultFromTool.stdout) match {
          case s @ Success(_) => s
          case Failure(e) =>
            val msg = failureMessage(command, resultFromTool, e)
            Failure(new Exception(msg))
        }

      case Left(e) =>
        Failure(e)
    }
  }

  override def apply(
      source: Source.Directory,
      configuration: Option[List[Pattern.Definition]],
      files: Option[Set[Source.File]],
      options: Map[Options.Key, Options.Value]
  )(implicit specification: Specification): Try[List[Result]] = {
    Try {
      val filesToLint: List[String] = listOfFilesToLint(source, files)

      val patternsConfig = lintingConfiguration(source, configuration)

      val command = commandToRun(patternsConfig, filesToLint)

      runCommandOnSourceDir(command, source)
    }.flatten
  }

  private def parseToolResult(path: Path, output: List[String]): Try[List[Result]] = {
    Try(Json.parse(output.mkString)).flatMap(parseToolResult)
  }

  private def parseToolResult(outputJson: JsValue): Try[List[Result]] = {
    /* Example:
     * "files": [
     *   {
     *     "path": "/home/Potatso-iOS/Library/Aspects/Aspects/Source/Aspects.swift",
     *     "violations": [
     *       {
     *         "severity": "warning",
     *         "rule": "trailing-whitespace",
     *         "location": {
     *           "line": 12,
     *           "column": 4
     *         },
     *         "message": "Line should not have any trailing whitespace"
     *       },
     *     ],
     *     "parsed": true
     * ...
     */

    Try((outputJson \ "files").as[List[TailorFile]]).map { files =>
      files.flatMap {
        case file if file.parsed =>
          file.violations.map { violation =>
            Issue(
              Source.File(file.path),
              Result.Message(violation.message),
              Pattern.Id(violation.rule),
              Source.Line(violation.location.line)
            )
          }
        case file =>
          List(FileError(Source.File(file.path), message = None))
      }
    }
  }
}
