package org.eop.guide.initialization

import org.eop.guide.StringOps.{
  withGreenBackground,
  withMagentaBackground,
  withRedBackground
}
import zio.{IO, UIO, ZIO, ZLayer}

trait Bread:
  val eat: UIO[Unit] = ZIO.debug("Bread: Eating".withGreenBackground)
end Bread

class BreadStoreBought extends Bread

object BreadStoreBought:
  val live: ZLayer[Any, Nothing, Bread] =
    ZLayer.fromZIO:
      ZIO
        .debug:
          "Creating Bread [BreadStoreBought] dependency...".withMagentaBackground
        .as(new BreadStoreBought)
end BreadStoreBought

class BreadHomeMade(dough: Dough, heatSource: Oven) extends Bread

object BreadHomeMade:
  val live: ZLayer[Dough & Oven, Nothing, Bread] =
    ZLayer.fromZIO:
      for
        _ <- ZIO.debug:
          "Creating Bread [BreadHomeMade] dependency...".withMagentaBackground
        dough      <- ZIO.service[Dough]
        heatSource <- ZIO.service[Oven]
      yield new BreadHomeMade(dough, heatSource)
end BreadHomeMade

class BreadFromFriend extends Bread

object BreadFromFriend:
  private val WORKS_ON_ATTEMPT = 3

  def live: ZLayer[RetryConfig & Counter, String, Bread] =
    ZLayer.fromZIO:
      for
        retryConfig <- ZIO.service[RetryConfig]
        counter     <- ZIO.service[Counter]
        bread       <- createBread(counter).retryN(retryConfig.times)
      yield bread
  end live

  private def createBread(counter: Counter): IO[String, BreadFromFriend] =
    counter.incrementAndGet.flatMap: n =>
      if n < WORKS_ON_ATTEMPT then fail(n)
      else succeed(n)

  private def succeed(invocations: Int): UIO[BreadFromFriend] =
    ZIO
      .debug:
        s"Attempt $invocations: Creating Bread [BreadFromFriend] dependency...".withMagentaBackground
      .as(new BreadFromFriend)
  end succeed

  private def fail(invocations: Int): IO[String, Nothing] =
    ZIO.debug:
      s"Attempt $invocations: Friend unreachable".withMagentaBackground
    *> ZIO.fail:
      "Failure(Friend unreachable)".withRedBackground
  end fail
end BreadFromFriend
