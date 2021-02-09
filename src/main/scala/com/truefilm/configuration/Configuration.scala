package com.truefilm.configuration

import pureconfig.ConfigSource
import zio._

object Configuration {
  import pureconfig.generic.auto._

  type Configuration = Has[Config]

  /**
   * Load configuration from resources file
   *
   */
  val live : Layer[Throwable, Configuration] = Task.effect(ConfigSource.default.loadOrThrow[Config]).map(Has(_)).toLayerMany

  val test : Layer[Throwable, Configuration] = ZLayer.succeed(
    Config(DbConfig("127.0.0.1","test","test"))
  )

  def custom(dbConfig: DbConfig) : Layer[Throwable, Configuration] = ZLayer.succeed(
    Config(dbConfig)
  )
}
