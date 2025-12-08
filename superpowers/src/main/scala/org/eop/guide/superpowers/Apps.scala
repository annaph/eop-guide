package org.eop.guide.superpowers

import org.eop.guide.IntOps.seconds
import org.eop.guide.StringOps.withMagentaBackground
import org.eop.guide.superpowers.Scenario.*
import zio.{Duration, Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

trait AppBase extends AppOps:
  protected lazy val effect0: ZIO[Scenario, String, String] =
    ZIO.debug("Attempting to save user...".withMagentaBackground) *>
      saveUser(username)

  protected lazy val effect1: ZIO[Scenario, String, String] =
    effect0.retryN(n = 2)

  protected lazy val effect2: ZIO[Scenario, String, String] =
    effect1.orElseFail("FAILURE: User not saved!")

  protected lazy val effect3: ZIO[Scenario, String, String] =
    effect2.timeoutFail("** Save timeout **")(5.seconds)

  protected lazy val effect4: ZIO[Scenario, Throwable, String] =
    effect3.orElse:
      sendToManualQueue(username)

  protected lazy val effect5: ZIO[Scenario, Throwable, String] =
    ZIO.scoped:
      effect4.withFinalizer(_ => logUserSignup(username))

  protected lazy val effect6: ZIO[Scenario, Throwable, (Duration, String)] =
    effect5.timed

  protected lazy val effect7: ZIO[
    Scenario,
    Throwable,
    Option[(Duration, String)]
  ] =
    effect6.when(username != "Anna")

  protected lazy val effect8: ZIO[Scenario, String, String] =
    for
      _      <- ZIO.debug("Before save".withMagentaBackground)
      result <- effect1
    yield result

  val username = "Anna"
end AppBase

object App1 extends ZIOAppDefault with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, String, String] =
    Successful.simulate:
      effect0
end App1

object App2 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    Successful.simulate:
      effect0
end App2

object App3 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    WorksOnThirdTry.simulate:
      effect0
end App3

object App4 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    WorksOnThirdTry.simulate:
      effect1
end App4

object App5 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    NeverWorks.simulate:
      effect1
end App5

object App6 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    NeverWorks.simulate:
      effect2
end App6

object App7 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    Slow.simulate:
      effect3
end App7

object App8 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    NeverWorks.simulate:
      effect4
end App8

object App9 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    Successful.simulate:
      effect5
end App9

object App10 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    Successful.simulate:
      effect6
end App10

object App11 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    Successful.simulate:
      effect7
end App11

object App12 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    Successful.simulate:
      ZIO.debug("Before save")
      effect1
end App12

object App13 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    Successful.simulate:
      effect8
end App13

object App14 extends ZIOAppDefault with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    Successful.simulate:
      for
        _ <- ZIO.debug("**Before**".withMagentaBackground)
        _ <- effect8.debug.repeatN(1)
        _ <- ZIO.debug("**After**".withMagentaBackground)
      yield ()
  end run
end App14
