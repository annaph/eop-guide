package org.eop.guide.resilience

import zio.{Clock, Duration, Ref, Task, UIO, ZIO, ZLayer}

trait StrugglingService:
  val callsMade: UIO[Int]

  val callsFailed: UIO[Int]

  def makeCall(): Task[Unit]
end StrugglingService

object StrugglingService:
  def live(
      scheduledValues: Seq[(Duration, Boolean)]
  ): ZLayer[Any, Nothing, StrugglingService] =
    ZLayer.fromZIO:
      for
        now            <- Clock.instant
        callsMadeRef   <- Ref.make(0)
        callsFailedRef <- Ref.make(0)
      yield new StrugglingServiceImpl(
        callsMadeRef,
        callsFailedRef,
        varyingValue = VaryingValue(scheduledValues, startTime = now)
      )
end StrugglingService

class StrugglingServiceImpl(
    callsMadeRef: Ref[Int],
    callsFailedRef: Ref[Int],
    varyingValue: VaryingValue[Boolean]
) extends StrugglingService:
  override val callsMade: UIO[Int] = callsMadeRef.get

  override val callsFailed: UIO[Int] = callsFailedRef.get

  override def makeCall(): Task[Unit] =
    for
      _  <- callsMadeRef.update(_ + 1)
      ok <- varyingValue.get()
      _  <-
        if ok then ZIO.unit
        else
          callsFailedRef.update(_ + 1) *> ZIO.fail:
            new Exception("Failed to make call!")
    yield ()
  end makeCall
end StrugglingServiceImpl
