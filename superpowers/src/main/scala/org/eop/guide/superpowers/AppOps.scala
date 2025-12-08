package org.eop.guide.superpowers

import org.eop.guide.IntOps.seconds
import org.eop.guide.StringOps.withMagentaBackground
import org.eop.guide.superpowers.AppOps.{fail, succeed}
import zio.{Task, UIO, ZIO}

trait AppOps:
  def saveUser(username: String): ZIO[Scenario, String, String] =
    ZIO
      .service[Scenario]
      .flatMap:
        case Scenario.Successful => succeed(username)
        case Scenario.NeverWorks => fail
        case Scenario.Slow       => succeed(username).delay(60.seconds)
        case Scenario.WorksOnTryInner(ref) =>
          ref
            .getAndUpdate(_ + 1)
            .flatMap { numCalls =>
              if numCalls >= 2 then succeed(username) else fail
            }
  end saveUser

  def sendToManualQueue(username: String): Task[String] =
    ZIO.attempt:
      s"Sent $username to manual queue"

  def logUserSignup(username: String): UIO[Unit] =
    ZIO.debug:
      s"Log: Signup initiated for $username".withMagentaBackground
end AppOps

object AppOps:
  private def succeed(username: String) =
    ZIO.succeed:
      s"User $username saved."

  private def fail =
    ZIO
      .fail("**Database crashed!!**")
      .tapError: error =>
        ZIO.debug(s"Log: $error".withMagentaBackground)
end AppOps
