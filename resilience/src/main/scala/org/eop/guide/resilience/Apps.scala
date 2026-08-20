package org.eop.guide.resilience

import nl.vroste.rezilience.Bulkhead.BulkheadError
import nl.vroste.rezilience.CircuitBreaker.{
  CircuitBreakerCallError,
  CircuitBreakerOpen
}
import nl.vroste.rezilience.{
  Bulkhead,
  CircuitBreaker,
  RateLimiter,
  Retry,
  TrippingStrategy
}
import org.eop.guide.StringOps.withMagentaBackground
import org.eop.guide.ZIOAppDebug
import zio.{
  Clock,
  Duration,
  IO,
  Random,
  Ref,
  Schedule,
  Scope,
  UIO,
  URIO,
  ZIO,
  ZIOAppArgs,
  durationInt
}

import java.time.{Duration as JavaDuration, Instant}

trait AppBase:
  lazy val thunderingHerds: URIO[PopularService, String] =
    for
      popularService <- ZIO.service[PopularService]
      _              <- ZIO.collectAllParDiscard(Seq.fill(n = 100):
        popularService.retrieve(name = "Awesome Memes"))
      result <- popularService.invoice
    yield result
  end thunderingHerds

  lazy val intermittentSlowResponse: UIO[Unit] =
    Random
      .nextIntBetween(minInclusive = 0, maxExclusive = 1000)
      .flatMap: n =>
        if n != 0 then ZIO.unit
        else ZIO.sleep(3.seconds)
  end intermittentSlowResponse

  lazy val makeRateLimiter: URIO[Scope, RateLimiter] =
    RateLimiter.make(
      max = 1,
      interval = 3.seconds
    )
  end makeRateLimiter

  lazy val makeBulkhead: URIO[Scope, Bulkhead] =
    Bulkhead.make(
      maxInFlightCalls = 3,
      maxQueueing = 64
    )
  end makeBulkhead

  lazy val makeCircuitBreaker: URIO[Scope, CircuitBreaker[Any]] =
    CircuitBreaker.make(
      trippingStrategy = TrippingStrategy.failureCount(maxFailures = 2),
      resetPolicy = Retry.Schedules.common()
    )
  end makeCircuitBreaker

  lazy val rapidlySchedule: Schedule[Any, Any, _] =
    Schedule.recurs(140) && Schedule.spaced(50.milliseconds)

  val scheduledValues = Seq(
    1_100.milliseconds -> true,
    4_100.milliseconds -> false,
    5_100.milliseconds -> true
  )

  def protectExpensiveCall(
      globalStart: Instant,
      user: String,
      rateLimiter: RateLimiter
  ): URIO[Scope, Unit] =
    rateLimiter:
      expensiveCall(globalStart, user)
  end protectExpensiveCall

  def protectDelicateResource(
      bulkhead: Bulkhead,
      delicateResource: DelicateResource
  ): IO[BulkheadError[String], Unit] =
    bulkhead:
      delicateResource.requestProcessor
  end protectDelicateResource

  def protectStrugglingService(
      circuitBreaker: CircuitBreaker[Any],
      strugglingService: StrugglingService
  ): IO[CircuitBreakerCallError[Any], Unit] =
    circuitBreaker:
      strugglingService.makeCall()
  end protectStrugglingService

  def expensiveCall(globalStart: Instant, user: String): UIO[Unit] =
    for
      now <- Clock.instant
      _   <- ZIO.debug:
        s"$user request @ ${durationBetween(globalStart, now)}s".withMagentaBackground
      _ <- ZIO.sleep(30.milliseconds)
    yield ()
  end expensiveCall

  def lotsOfRequests(totalRequests: Int, timeout: Duration)(
      request: => UIO[Unit]
  ): UIO[Int] =
    ZIO
      .collectAllSuccessesPar:
        List.fill(n = totalRequests):
          request.timeoutFail("Took too long!")(timeout)
      .map(_.length)
      .map(totalRequests - _)
  end lotsOfRequests

  def spottyLogic(counterRef: Ref[Int]): UIO[Boolean] =
    counterRef
      .updateAndGet(_ + 1)
      .flatMap: counter =>
        if counter == 3 then
          ZIO
            .debug(s"Success.".withMagentaBackground)
            .as(true)
        else
          ZIO
            .debug(s"Failure!".withMagentaBackground)
            .as(false)
  end spottyLogic

  private def durationBetween(first: Instant, second: Instant): Long =
    JavaDuration.between(first, second).getSeconds
end AppBase

object App0 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    thunderingHerds.provide:
      CloudStorage.live >+> PopularServiceDefault.live
end App0

object App1 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    thunderingHerds.provide:
      CloudStorage.live >+> PopularServiceWithCache.live
end App1

object App2 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    for
      startTime <- Clock.instant
      _         <- expensiveCall(startTime, user = "Anna").repeatN(2)
    yield ()
end App2

object App3 extends ZIOAppDebug with AppBase:
  private val user = "Anna"

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    ZIO.scoped:
      for
        startTime   <- Clock.instant
        rateLimiter <- makeRateLimiter
        _ <- protectExpensiveCall(startTime, user, rateLimiter).repeatN(2)
      yield ()
end App3

object App4 extends ZIOAppDebug with AppBase:
  private val users = Seq("Anna", "Stacey", "Nicole")

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    ZIO.scoped:
      for
        startTime   <- Clock.instant
        rateLimiter <- makeRateLimiter
        _           <- ZIO
          .foreachParDiscard(users): user =>
            protectExpensiveCall(startTime, user, rateLimiter).repeatN(2)
      yield "All requests succeeded"
end App4

object App5 extends ZIOAppDebug with AppBase:
  private val resourceCrasher: ZIO[DelicateResource, String, String] =
    for
      delicateResource <- ZIO.service[DelicateResource]
      _                <- ZIO.foreachParDiscard(1 to 10): _ =>
        delicateResource.requestProcessor
    yield "All requests succeeded"
  end resourceCrasher

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    resourceCrasher.provide:
      DelicateResource.live
end App5

object App6 extends ZIOAppDebug with AppBase:
  private val resourceProtector: ZIO[
    Scope & DelicateResource,
    BulkheadError[String],
    String
  ] =
    for
      delicateResource <- ZIO.service[DelicateResource]
      bulkhead         <- makeBulkhead
      _                <- ZIO.foreachParDiscard(1 to 10): _ =>
        protectDelicateResource(bulkhead, delicateResource)
    yield "All requests succeeded"
  end resourceProtector

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    ZIO.scoped:
      resourceProtector.provideSome[Scope]:
        DelicateResource.live
end App6

object App7 extends ZIOAppDebug with AppBase:
  private val resourceCrasher: URIO[StrugglingService, String] =
    for
      strugglingService <- ZIO.service[StrugglingService]
      _           <- strugglingService.makeCall().ignore.repeat(rapidlySchedule)
      callsMade   <- strugglingService.callsMade
      callsFailed <- strugglingService.callsFailed
    yield s"""
             |Total Submitted: $callsMade
             |Failed: $callsFailed
             |
      """.stripMargin
  end resourceCrasher

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    resourceCrasher.provide:
      StrugglingService.live(scheduledValues)
end App7

object App8 extends ZIOAppDebug with AppBase:
  private val resourceProtector: ZIO[
    Scope & StrugglingService,
    CircuitBreakerCallError[Any],
    String
  ] =
    for
      strugglingService <- ZIO.service[StrugglingService]
      callsPreventedRef <- Ref.make(0)
      circuitBreaker    <- makeCircuitBreaker
      _ <- protectStrugglingService(circuitBreaker, strugglingService)
        .catchSome:
          case CircuitBreakerOpen => callsPreventedRef.update(_ + 1)
        .ignore
        .repeat(rapidlySchedule)
      callsMade      <- strugglingService.callsMade
      callsFailed    <- strugglingService.callsFailed
      callsPrevented <- callsPreventedRef.get
    yield s"""
             |Total Submitted: $callsMade
             |Failed: $callsFailed
             |Prevented: $callsPrevented
        """.stripMargin
  end resourceProtector
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    ZIO.scoped:
      resourceProtector.provideSome[Scope]:
        StrugglingService.live(scheduledValues)
end App8

object App9 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    for callsFailed <- lotsOfRequests(
        totalRequests = 50_000,
        timeout = 1.second
      )(request = intermittentSlowResponse)
    yield s"$callsFailed requests timed out!"
end App9

object App10 extends ZIOAppDebug with AppBase:
  private def hedged(callsRacedRef: Ref[Int]): UIO[Unit] =
    intermittentSlowResponse.race:
      intermittentSlowResponse
        .delay(20.milliseconds)
        .tap: _ =>
          callsRacedRef.update(_ + 1)
  end hedged

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    for
      callsRacedRef <- Ref.make(0)
      callsFailed   <- lotsOfRequests(
        totalRequests = 50_000,
        timeout = 1.second
      )(request = hedged(callsRacedRef))
      callsRaced <- callsRacedRef.get
    yield s"\n$callsFailed requests timed out!\n$callsRaced requests did a race!"
end App10
