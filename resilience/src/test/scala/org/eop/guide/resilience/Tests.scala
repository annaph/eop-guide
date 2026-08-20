package org.eop.guide.resilience

import zio.test.{
  Spec,
  TestAspect,
  TestEnvironment,
  ZIOSpecDefault,
  assertCompletes,
  assertTrue
}
import zio.{Ref, Scope, ZIO, ZLayer, durationInt}

trait TestBase extends ZIOSpecDefault with AppBase:
  lazy val troublesomeTestCase: Spec[Ref[Int], Nothing] =
    test("flaky test"):
      for
        counterRef <- ZIO.service[Ref[Int]]
        result     <- spottyLogic(counterRef)
      yield assertTrue(result)
  end troublesomeTestCase
end TestBase

object Test1 extends TestBase:
  override def spec: Spec[TestEnvironment & Scope, Any] =
    test("long test"):
      for _ <- ZIO.sleep(1.minute)
      yield assertCompletes
    @@ TestAspect.withLiveClock
      @@ TestAspect.timeout(3.seconds)
end Test1

object Test2 extends TestBase:
  override def spec: Spec[TestEnvironment & Scope, Any] =
    troublesomeTestCase.provide:
      ZLayer.fromZIO(Ref.make(0))
end Test2

object Test3 extends TestBase:
  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("troublesome suite")(
      troublesomeTestCase @@ TestAspect.flaky
    ).provide:
      ZLayer.fromZIO(Ref.make(0))
end Test3
