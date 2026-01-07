package org.eop.guide.initialization

import org.eop.guide.StringOps.{withGreenBackground, withMagentaBackground}
import zio.{UIO, ZIO, ZLayer}

class Dough:
  val rise: UIO[Unit] = ZIO.debug("Dough: rising".withGreenBackground)
end Dough

object Dough:
  val live: ZLayer[Any, Nothing, Dough] =
    ZLayer.fromZIO:
      ZIO
        .debug("Creating Dough dependency...".withMagentaBackground)
        .as(new Dough)
end Dough
