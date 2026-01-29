package org.eop.guide.initialization

import org.eop.guide.ZIOAppDebug
import org.eop.guide.initialization.App6.eatBread
import zio.{Scope, URIO, ZIO, ZIOAppArgs, ZLayer}

trait AppBase:
  protected lazy val eatBread: URIO[Bread, Unit] =
    ZIO.serviceWithZIO[Bread]: bread =>
      bread.eat

  protected lazy val eatToast: URIO[Toast, Unit] =
    ZIO.serviceWithZIO[Toast]: toast =>
      toast.eat
end AppBase

object App1 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    eatBread.provide:
      BreadStoreBought.live
end App1

object App2 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    eatBread.provide(
      BreadHomeMade.live,
      Dough.live,
      Oven.live
    )
end App2

object App3 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    eatToast.provide(
      // ZLayer.Debug.tree,
      Toast.live,
      BreadHomeMade.live,
      Dough.live,
      Oven.live,
      Toaster.live
    )
end App3

object App4 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    eatToast.provide(
      Toast.live,
      BreadStoreBought.live,
      Toaster.live
    )
end App4

object App5 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    eatBread.provide(
      BreadHomeMade.live,
      Dough.live,
      OvenSafe.live
    )
end App5

object App6 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    eatBread.provide(
      RetryConfigDefault.live,
      Counter.live,
      BreadFromFriend.live
    )
end App6

object App7 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    eatBread.provide(
      RetryConfigDefault.live,
      Counter.live,
      BreadFromFriend.live.orElse(BreadStoreBought.live)
    )
end App7

object App8 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    eatBread.provide(
      RetryConfig.live(times = 1),
      Counter.live,
      BreadFromFriend.live
    )
end App8

object App9 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    eatBread.provide(
      RetryConfig.live(times = 2),
      Counter.live,
      BreadFromFriend.live
    )
end App9

object App10 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    eatBread.provide(
      RetryConfigHocon.live,
      Counter.live,
      BreadFromFriend.live
    )
end App10
