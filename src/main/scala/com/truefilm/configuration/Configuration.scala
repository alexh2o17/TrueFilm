package com.truefilm.configuration

import pureconfig.ConfigSource
import zio._

object Configuration {
  import pureconfig.generic.auto._

  type Configuration = Has[Config]

  val live : Layer[Throwable, Configuration] = Task.effect(ConfigSource.default.loadOrThrow[Config]).map(Has(_)).toLayerMany

  val test : Layer[Throwable, Configuration] = ZLayer.succeed(
    Config(DbConfig("127.0.0.1:5432","test","test"))
  )
}
