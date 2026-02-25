package org.eop.guide.testing

import zio.ZLayer

final case class Nailer(force: Int)

object HandNailer:
  val live: ZLayer[Any, Nothing, Nailer] =
    ZLayer.succeed:
      Nailer(force = 4)
end HandNailer

object RoboticNailer:
  val live: ZLayer[Any, Nothing, Nailer] =
    ZLayer.succeed:
      Nailer(force = 11)
end RoboticNailer
