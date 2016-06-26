package codacy

import codacy.dockerApi.DockerEngine
import codacy.tailor.Tailor

object Engine extends DockerEngine(Tailor)