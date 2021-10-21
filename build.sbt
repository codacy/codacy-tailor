import com.typesafe.sbt.packager.docker.Cmd
import sjsonnew._
import sjsonnew.BasicJsonProtocol._
import sjsonnew.support.scalajson.unsafe._

lazy val toolVersion = settingKey[String]("The version of the underlying tool retrieved from patterns.json")
toolVersion := {
  case class Patterns(name: String, version: String)
  implicit val patternsIso: IsoLList[Patterns] =
    LList.isoCurried((p: Patterns) => ("name", p.name) :*: ("version", p.version) :*: LNil) {
      case (_, n) :*: (_, v) :*: LNil => Patterns(n, v)
    }

  val jsonFile = (resourceDirectory in Compile).value / "docs" / "patterns.json"
  val json = Parser.parseFromFile(jsonFile)
  val patterns = json.flatMap(Converter.fromJson[Patterns])
  patterns.get.version
}

name := "codacy-tailor"

scalaVersion := "2.13.1"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.7.4",
  "com.codacy" %% "codacy-engine-scala-seed" % "5.0.1" withSources ()
)

enablePlugins(JavaAppPackaging)

enablePlugins(DockerPlugin)

organization := "com.codacy"

def installAll(tailorVersion: String) =
  s"""apk --no-cache add bash curl &&
     |curl -#fLO https://github.com/sleekbyte/tailor/releases/download/v$tailorVersion/tailor-$tailorVersion.tar &&
     |tar -xvf tailor-$tailorVersion.tar &&
     |mv tailor-$tailorVersion /usr/bin/tailor &&
     |rm -rf tailor-$tailorVersion.tar
     |apk del curl &&
     |rm -rf /var/cache/apk/*""".stripMargin
    .replaceAll(System.lineSeparator(), " ")

mappings in Universal ++= {
  (resourceDirectory in Compile) map { resourceDir =>
    val src = resourceDir / "docs"
    val dest = "/docs"

    for {
      path <- src.allPaths.get if !path.isDirectory
    } yield path -> path.toString.replaceFirst(src.toString, dest)
  }
}.value

val dockerUser = "docker"
val dockerGroup = "docker"

daemonUser in Docker := dockerUser

daemonGroup in Docker := dockerGroup

dockerBaseImage := "amazoncorretto:8-alpine3.14-jre"

dockerCommands := dockerCommands.value.flatMap {
  case cmd @ Cmd("ADD", _) =>
    List(
      Cmd("RUN", s"adduser -u 2004 -D $dockerUser"),
      Cmd("RUN", installAll(toolVersion.value)),
      cmd,
      Cmd("RUN", "mv /opt/docker/docs /docs"),
      Cmd("RUN", s"chown -R $dockerUser:$dockerGroup /docs")
    )
  case other => List(other)
}
