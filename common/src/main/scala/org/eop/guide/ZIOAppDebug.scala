package org.eop.guide

import org.eop.guide.StringOps.{withGreenBackground, withRedBackground}
import org.eop.guide.ZIOAppDebug.{debugDefect, debugError, debugSuccess}
import zio.{Cause, Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

trait ZIOAppDebug:
  self =>

  def run: ZIO[ZIOAppArgs & Scope, Any, Any]

  def main(args: Array[String]): Unit =
    zioApp.main(args)

  private def zioApp: ZIOAppDefault =
    new ZIOAppDefault:
      override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
        self.run
          .tapSome(debugSuccess)
          .tapError(debugError)
          .tapDefect(debugDefect)
  end zioApp
end ZIOAppDebug

object ZIOAppDebug:
  private def debugSuccess[R, E, A]: PartialFunction[A, ZIO[R, E, Any]] =
    (success: A) =>
      success match
        case result if !result.isInstanceOf[Unit] =>
          ZIO.debug:
            s"~> Result: $result".withGreenBackground
  end debugSuccess

  private def debugError[R, E, A]: Function[E, ZIO[R, E, Any]] =
    (error: E) =>
      error match
        case exception: Exception =>
          ZIO.debug:
            s"~> Error: ${exception.getClass.getSimpleName} ~> '${exception.getMessage}'".withRedBackground

        case other =>
          ZIO.debug:
            s"~> Error: $other".withRedBackground
  end debugError

  private def debugDefect[R, E, A]: Function[Cause[Nothing], ZIO[R, E, Any]] =
    (cause: Cause[Nothing]) =>
      cause.dieOption
        .map: deffect =>
          ZIO.debug:
            s"~> Deffect: ${deffect.getClass.getSimpleName} ~> '${deffect.getMessage}'".withRedBackground
        .getOrElse(ZIO.unit)
  end debugDefect
end ZIOAppDebug
