package org.eop.guide.initialization

import org.eop.guide.ZIOAppDebug
import zio.{Scope, URIO, ZIO, ZIOAppArgs}

trait AppBase:
  protected lazy val eatBread: URIO[Bread, Unit] =
    ZIO.serviceWithZIO[Bread]: bread =>
      bread.eat
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
