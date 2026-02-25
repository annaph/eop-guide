package org.eop.guide.testing

import org.eop.guide.StringOps.{withGreenBackground, withRedBackground}
import org.eop.guide.ZIOAppDebug
import zio.{IO, Random, Scope, UIO, ZIO, ZIOAppArgs, durationInt}

trait AppBase:
  protected lazy val flipFive: UIO[Int] =
    ZIO
      .collectAllSuccesses(Seq.fill(5)(coinToss))
      .map(_.length)

  protected lazy val nightlyBatch: UIO[Unit] =
    ZIO
      .sleep(24.hours)
      .debug("Parsing CSV...".withGreenBackground)

  private lazy val coinToss: IO[String, String] =
    Random.nextBoolean.flatMap: isHeads =>
      if isHeads then
        ZIO.debug("Heads".withGreenBackground) *>
          ZIO.succeed("Heads")
      else
        ZIO.debug("Tails".withRedBackground) *>
          ZIO.fail("Tails")
  end coinToss
end AppBase

object App0 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] = flipFive
end App0

object App1 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] = nightlyBatch
end App1
