package org.eop.guide.superpowers

import zio.{IO, Ref, UIO, ZIO, ZLayer}

enum Scenario:
  case Successful
  case NeverWorks
  case Slow
  case WorksOnTryInner(ref: Ref[Int])

  def simulate[E, A](effect: => ZIO[Scenario, E, A]): IO[E, A] =
    effect.provide(ZLayer.succeed(this))
end Scenario

object Scenario:
  def WorksOnThirdTry: UIO[WorksOnTryInner] =
    Ref.make(0).map(WorksOnTryInner(_))

  extension (uio: UIO[WorksOnTryInner])
    def simulate[E, A](effect: => ZIO[Scenario, E, A]): IO[E, A] =
      uio.flatMap { scenario => effect.provide(ZLayer.succeed(scenario)) }
  end extension
end Scenario
