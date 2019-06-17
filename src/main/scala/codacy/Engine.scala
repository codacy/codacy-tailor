package codacy

import codacy.tailor.Tailor
import com.codacy.tools.scala.seed.DockerEngine

object Engine extends DockerEngine(Tailor)()
