import com.typesafe.sbt.packager.docker.{Cmd, ExecCmd}

name := "codacy-tailor"

version := "1.0.0-SNAPSHOT"

val languageVersion = "2.13.1"

scalaVersion := languageVersion

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.7.4",
  "com.codacy" %% "codacy-engine-scala-seed" % "3.1.0" withSources ()
)

enablePlugins(JavaAppPackaging)

enablePlugins(DockerPlugin)

version in Docker := "1.0.0-SNAPSHOT"

organization := "com.codacy"

val tailorVersion = "0.12.0"

val installAll =
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

dockerBaseImage := "develar/java"

dockerCommands := dockerCommands.value.flatMap {
  case cmd @ Cmd("ADD", _) =>
    List(
      Cmd("RUN", s"adduser -u 2004 -D $dockerUser"),
      cmd,
      Cmd("RUN", "mv /opt/docker/docs /docs"),
      Cmd("RUN", s"chown -R $dockerUser:$dockerGroup /docs")
    )
  case other => List(other)
}
