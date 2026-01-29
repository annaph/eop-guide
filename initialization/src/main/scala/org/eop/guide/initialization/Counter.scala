package org.eop.guide.initialization

import zio.{Ref, UIO, ZLayer}

trait Counter:
  def incrementAndGet: UIO[Int]
end Counter

object Counter:
  def live: ZLayer[Any, Nothing, Counter] =
    ZLayer.fromZIO:
      Ref.make(0).map(new CounterImpl(_))
end Counter

class CounterImpl(ref: Ref[Int]) extends Counter:
  override def incrementAndGet: UIO[Int] = ref.updateAndGet(_ + 1)
end CounterImpl
