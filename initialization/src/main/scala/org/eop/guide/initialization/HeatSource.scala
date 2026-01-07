package org.eop.guide.initialization

import org.eop.guide.StringOps.withMagentaBackground
import zio.{ZIO, ZLayer}

trait HeatSource

class Oven extends HeatSource

object Oven:
  val live: ZLayer[Any, Nothing, HeatSource] =
    ZLayer.fromZIO:
      ZIO
        .debug("Creating HeatSource [Oven] dependency...".withMagentaBackground)
        .as(new Oven)
end Oven
