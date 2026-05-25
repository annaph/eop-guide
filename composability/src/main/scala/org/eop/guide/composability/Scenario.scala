package org.eop.guide.composability

import zio.{IO, ZIO, ZLayer}

enum Scenario:
  case Successful
  case HeadlineError
  case BoringTopic
  case FileSystemError
  case WikiSystemError
  case AISlow
  case DiskFull

  def simulate[E, A](zio: => ZIO[Scenario, E, A]): IO[E, A] =
    zio.provide:
      ZLayer.succeed(this)
end Scenario
