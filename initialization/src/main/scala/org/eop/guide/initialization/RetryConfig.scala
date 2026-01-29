package org.eop.guide.initialization

import zio.config.magnolia.deriveConfig
import zio.config.typesafe.FromConfigSourceTypesafe
import zio.{Config, ConfigProvider, ZLayer}

final case class RetryConfig(times: Int)

object RetryConfig:
  def live(times: Int): ZLayer[Any, Nothing, RetryConfig] =
    ZLayer.succeed:
      RetryConfig(times)
end RetryConfig

object RetryConfigDefault:
  private val DEFAULT_VALUE = 0

  def live: ZLayer[Any, Nothing, RetryConfig] =
    ZLayer.succeed:
      RetryConfig(times = DEFAULT_VALUE)
end RetryConfigDefault

object RetryConfigHocon:
  def live: ZLayer[Any, Config.Error, RetryConfig] =
    ZLayer.fromZIO:
      ConfigProvider
        .fromHoconString("{ times: 2 }")
        .load(deriveConfig[RetryConfig])
end RetryConfigHocon
