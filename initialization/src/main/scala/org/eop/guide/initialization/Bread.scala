package org.eop.guide.initialization

import org.eop.guide.StringOps.{withGreenBackground, withMagentaBackground}
import zio.{UIO, ZIO, ZLayer}

trait Bread:
  val eat: UIO[Unit] = ZIO.debug("Bread: Eating".withGreenBackground)
end Bread

class BreadStoreBought extends Bread

object BreadStoreBought:
  val live: ZLayer[Any, Nothing, Bread] =
    ZLayer.fromZIO:
      ZIO
        .debug(
          "Creating Bread [BreadStoreBought] dependency...".withMagentaBackground
        )
        .as(new BreadStoreBought)
end BreadStoreBought

class BreadHomeMade(dough: Dough, heatSource: HeatSource) extends Bread

object BreadHomeMade:
  val live: ZLayer[Dough & HeatSource, Nothing, Bread] =
    ZLayer.fromZIO:
      for
        _ <- ZIO.debug(
          "Creating Bread [BreadHomeMade] dependency...".withMagentaBackground
        )
        dough      <- ZIO.service[Dough]
        heatSource <- ZIO.service[HeatSource]
      yield new BreadHomeMade(dough, heatSource)
end BreadHomeMade
