package org.eop.guide

import org.eop.guide.StringOps.{withGreenBackground, withRedBackground}
import zio.{Console, Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

trait ZIOAppDebug:
  self =>

  def run: ZIO[ZIOAppArgs & Scope, Any, Any]

  def main(args: Array[String]): Unit =
    val app = new ZIOAppDefault {
      override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
        self.run
          .tapSome {
            case result if !result.isInstanceOf[Unit] =>
              Console
                .printLine(line = s"~> Result: $result".withGreenBackground)
          }
          .tapSomeError {
            case error if error.isInstanceOf[String] =>
              Console.printLine(line = s"~> Error: '$error'".withRedBackground)
          }
    }
    app.main(args)
  end main
end ZIOAppDebug
