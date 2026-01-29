package org.eop.guide.initialization

import org.eop.guide.StringOps.{withMagentaBackground, withRedBackground}
import zio.{ZIO, ZLayer}

trait HeatSource

class Oven extends HeatSource

object Oven:
  val live: ZLayer[Any, Nothing, Oven] =
    ZLayer.fromZIO:
      ZIO
        .debug("Creating Oven dependency...".withMagentaBackground)
        .as(new Oven)
end Oven

object OvenSafe:
  val live: ZLayer[Any, Nothing, Oven] =
    ZLayer.scoped[Any]:
      ZIO
        .debug("Creating Oven <OvenSafe> dependency...".withMagentaBackground)
        .as(new Oven)
        .withFinalizer(_ => ZIO.debug("Oven: Turning off".withRedBackground))
end OvenSafe

class Toaster extends HeatSource

object Toaster:
  val live: ZLayer[Any, Nothing, Toaster] =
    ZLayer.fromZIO:
      ZIO
        .debug("Creating Toaster dependency...".withMagentaBackground)
        .as(new Toaster)
end Toaster
