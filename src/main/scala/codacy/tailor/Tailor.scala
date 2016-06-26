package codacy.tailor

import java.nio.file.Path

import codacy.dockerApi._
import codacy.dockerApi.utils.{CommandRunner, ToolHelper}
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

  override def apply(path: Path, conf: Option[List[PatternDef]], files: Option[Set[Path]])(implicit spec: Spec): Try[List[Result]] = {
    Try {
      val filesToLint: List[String] = files.fold(List(path.toString)) {
        paths =>
          paths.map(_.toString).toList
      }

      val patternsToLintOpt = ToolHelper.getPatternsToLint(conf)
      val configuration =
        patternsToLintOpt.fold(List.empty[String]) {
          case patternsToLint if patternsToLint.nonEmpty =>
            val patternIds = patternsToLint.map(_.patternId)
            val parameters = patternsToLint.flatMap(_.parameters).flatten.flatMap { parameter =>
              List(s"--${parameter.name}", parameter.value.toString)
            }
            List("--only=" + patternIds.mkString(",")) ++ parameters
          case _ => List.empty[String]
        }

      val command =
        List("/usr/bin/tailor/bin/tailor",
          "-f", "json") ++
          configuration ++
          List("--") ++ filesToLint

      CommandRunner.exec(command) match {
        case Right(resultFromTool) =>
          parseToolResult(path, resultFromTool.stdout) match {
            case s@Success(_) => s
            case Failure(e) =>
              val msg =
                s"""
                   |${this.getClass.getSimpleName} exited with code ${resultFromTool.exitCode}
                   |command: ${command.mkString(" ")}
                   |message: ${e.getMessage}
                   |stdout: ${resultFromTool.stdout.mkString(Properties.lineSeparator)}
                   |stderr: ${resultFromTool.stderr.mkString(Properties.lineSeparator)}
                """.stripMargin
              Failure(new Exception(msg))
          }

        case Left(e) =>
          Failure(e)
      }
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
            Issue(SourcePath(file.path),
              ResultMessage(violation.message),
              PatternId(violation.rule),
              ResultLine(violation.location.line))
          }
        case file =>
          List(
            FileError(SourcePath(file.path), message = None)
          )
      }
    }
  }

}
