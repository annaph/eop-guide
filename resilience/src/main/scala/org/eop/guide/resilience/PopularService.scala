package org.eop.guide.resilience

import zio.cache.{Cache, Lookup}
import zio.{Duration, UIO, ZIO, ZLayer}

trait PopularService:
  val cloudStorage: CloudStorage

  val invoice: UIO[String] =
    cloudStorage.invoice

  def retrieve(name: String): UIO[FileContent] =
    cloudStorage.retrieve(name)
end PopularService

object PopularServiceDefault:
  val live: ZLayer[CloudStorage, Nothing, PopularService] =
    ZLayer.fromZIO:
      for storage <- ZIO.service[CloudStorage]
      yield new PopularService:
        override val cloudStorage = storage
end PopularServiceDefault

class PopularServiceWithCache(
    override val cloudStorage: CloudStorage,
    cache: Cache[String, Nothing, FileContent]
) extends PopularService:
  override def retrieve(name: String): UIO[FileContent] =
    cache.get(name)
end PopularServiceWithCache

object PopularServiceWithCache:
  val live: ZLayer[CloudStorage, Nothing, PopularService] =
    ZLayer.fromZIO:
      for
        cloudStorage <- ZIO.service[CloudStorage]
        cache        <- Cache.make(
          capacity = 1,
          timeToLive = Duration.Infinity,
          lookup = Lookup(cloudStorage.retrieve)
        )
      yield new PopularServiceWithCache(cloudStorage, cache)
end PopularServiceWithCache
