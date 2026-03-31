package org.eop.guide

import org.eop.guide.StringOps.{withGreenBackground, withRedBackground}
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

trait ZIOAppDebug:
  self =>

  def run: ZIO[ZIOAppArgs & Scope, Any, Any]

  def main(args: Array[String]): Unit =
    zioApp.main(args)

  private def zioApp: ZIOAppDefault =
    new ZIOAppDefault:
      override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
        self.run
          .tapSome:
            case result if !result.isInstanceOf[Unit] =>
              ZIO.debug:
                s"~> Result ~> $result".withGreenBackground
          .tapSomeError:
            case error: String =>
              ZIO.debug:
                s"~> Error ~> '$error'".withRedBackground

            case error: Exception =>
              ZIO.debug:
                s"~> Error ~> ${error.getClass.getSimpleName}: '${error.getMessage}'".withRedBackground
          .tapDefect: cause =>
            cause.dieOption
              .map: deffect =>
                ZIO.debug:
                  s"~> Deffect ~> ${deffect.getClass.getSimpleName}: '${deffect.getMessage}'".withRedBackground
              .getOrElse(ZIO.unit)
      end run
    end new
  end zioApp
end ZIOAppDebug
