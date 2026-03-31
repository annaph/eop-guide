package org.eop.guide.failure

import zio.{IO, ZIO, ZLayer}

enum Scenario:
  case Successful, TooCold, NetworkFailure, GPSFailure

  def simulate[E, A](zio: => ZIO[Scenario, E, A]): IO[E, A] =
    zio.provide:
      ZLayer.succeed(this)
end Scenario
