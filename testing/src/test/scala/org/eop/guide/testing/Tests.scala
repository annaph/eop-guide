package org.eop.guide.testing

import org.eop.guide.StringOps.withMagentaBackground
import zio.test.{
  Spec,
  TestAspect,
  TestClock,
  TestEnvironment,
  TestRandom,
  TestResult,
  ZIOSpecDefault,
  assertCompletes,
  assertTrue
}
import zio.{Scope, UIO, URIO, ZIO, durationInt}

trait TestBase extends ZIOSpecDefault:
  def testCase(label: String): Spec[Any, Nothing] =
    test(s"Case $label in a value"):
      showLabel(label)

  def showLabel(label: String): UIO[TestResult] =
    ZIO
      .debug(s"Running $label...".withMagentaBackground)
      .as(assertCompletes)
end TestBase

object Test1 extends TestBase:
  override def spec: Spec[TestEnvironment & Scope, Any] =
    test("Basic"):
      assertTrue(1 == 1)
end Test1

object Test2 extends TestBase:
  override def spec: Spec[TestEnvironment & Scope, Any] =
    test("Only the last assertTrue matters"):
      assertTrue(1 != 1) // Ignored!
      assertTrue(1 == 1)
end Test2

object Test3 extends TestBase:
  override def spec: Spec[TestEnvironment & Scope, Any] =
    test("Multiple Boolean expressions"):
      assertTrue(1 == 1, 2 == 2, 3 == 3)
end Test3

object Test4 extends TestBase:
  override def spec: Spec[TestEnvironment & Scope, Any] =
    test("Combine using operators"):
      assertTrue(1 == 0) || assertTrue(2 == 2) && assertTrue(3 == 3)
end Test4

object Test5 extends TestBase:
  override def spec: Spec[TestEnvironment & Scope, Any] =
    test("negation"):
      !assertTrue(42 == 47)
end Test5

object Test6 extends TestBase:
  override def spec: Spec[TestEnvironment & Scope, Any] =
    test("Effect as test"):
      ZIO
        .debug("Executing logic...".withMagentaBackground)
        .as(assertCompletes)
end Test6

object Test7 extends TestBase:
  private lazy val effectA: UIO[TestResult] = showLabel("A")

  override def spec: Spec[TestEnvironment & Scope, Any] =
    test("Case A"):
      effectA
end Test7

object Test8 extends TestBase:
  private lazy val effectA: UIO[TestResult] = showLabel("A")
  private lazy val effectB: UIO[TestResult] = showLabel("B")

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("Suite of Tests")(
      test("Case A in suite")(effectA),
      test("Case B in suite")(effectB)
    ) @@ TestAspect.sequential
end Test8

object Test9 extends TestBase:
  private lazy val testA: Spec[Any, Nothing] = testCase("A")
  private lazy val testB: Spec[Any, Nothing] = testCase("B")

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("A Suite of Tests")(testA, testB) @@ TestAspect.sequential
end Test9

object Test10 extends TestBase:
  private lazy val testNailerWithMaterial: URIO[Material & Nailer, TestResult] =
    for
      material <- ZIO.service[Material]
      nailer   <- ZIO.service[Nailer]
    yield assertTrue:
      nailer.force < material.brittleness

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("Construction Combinations")(
      test("Wood with hand nailer"):
        testNailerWithMaterial.provide(
          WoodMaterial.live,
          HandNailer.live
        )
      ,
      test("Plastic with hand nailer"):
        testNailerWithMaterial.provide(
          PlasticMaterial.live,
          HandNailer.live
        )
      ,
      test("Metal with robo nailer"):
        testNailerWithMaterial.provide(
          MetalMaterial.live,
          RoboticNailer.live
        )
      ,
      test("Plastic with robo nailer"):
        testNailerWithMaterial.provide(
          PlasticMaterial.live,
          RoboticNailer.live
        )
    ) @@ TestAspect.sequential
end Test10

object Test11 extends TestBase with AppBase:
  override def spec: Spec[TestEnvironment & Scope, Any] =
    test("Flip 5 times"):
      for
        _     <- TestRandom.feedBooleans(true, true, true)
        _     <- TestRandom.feedBooleans(false, false)
        heads <- flipFive.debug("Number of heads".withMagentaBackground)
      yield assertTrue(heads == 3)
end Test11

object Test12 extends TestBase with AppBase:
  override def spec: Spec[TestEnvironment & Scope, Any] =
    test("Batch runs after 24 hours"):
      for
        nightlyBatchF <- nightlyBatch.fork
        _             <- TestClock.adjust(24.hours)
        _             <- nightlyBatchF.join
      yield assertCompletes
end Test12
