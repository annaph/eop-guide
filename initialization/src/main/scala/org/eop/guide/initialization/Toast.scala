package org.eop.guide.initialization

import org.eop.guide.StringOps.{withGreenBackground, withMagentaBackground}
import zio.{UIO, ZIO, ZLayer}

trait Toast:
  val bread: Bread
  val heatSource: HeatSource
  val eat: UIO[Unit] = ZIO.debug(s"Toast: Eating".withGreenBackground)
end Toast

object Toast:
  val live: ZLayer[Bread & Toaster, Nothing, Toast] =
    ZLayer.fromZIO:
      for
        _ <- ZIO.debug:
          s"Creating Toast dependency...".withMagentaBackground
        bread      <- ZIO.service[Bread]
        heatSource <- ZIO.service[Toaster]
      yield new ToastImpl(bread, heatSource)
end Toast

class ToastImpl(val bread: Bread, val heatSource: Toaster) extends Toast
