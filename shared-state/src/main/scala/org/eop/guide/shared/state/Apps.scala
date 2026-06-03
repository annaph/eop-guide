package org.eop.guide.shared.state

import org.eop.guide.ZIOAppDebug
import zio.{Ref, Scope, UIO, ZIO, ZIOAppArgs}

import java.util.concurrent.atomic.AtomicInteger

trait AppBase:
  def parallel[R, E, A](num: Int)(effect: => ZIO[R, E, A]): ZIO[R, E, Unit] =
    ZIO.foreachParDiscard(Range(start = 0, end = num)): _ =>
      effect
end AppBase

object App0 extends ZIOAppDebug with AppBase:
  private val num     = 30_000
  private var counter = 0

  private val increment: UIO[Unit] =
    ZIO.succeed:
      counter = counter + 1
  end increment

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    parallel(num):
      increment
    .as:
      s"Lost updated: ${num - counter}"
end App0

object App1 extends ZIOAppDebug with AppBase:
  private val num = 30_000

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    for
      counter <- Ref.make(0)
      _       <- parallel(num):
        increment(counter)
      result <- counter.get
    yield s"""
             | counter == num => ${(result == num).toString.toUpperCase}
    """.stripMargin
  end run

  private def increment(counter: Ref[Int]): UIO[Unit] =
    counter.update(_ + 1)
end App1

object App2 extends ZIOAppDebug with AppBase:
  private val num      = 30_000
  private val attempts = new AtomicInteger(0)

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    for
      counter <- Ref.make(0)
      _       <- parallel(num):
        increment(counter)
      result <- counter.get
    yield s"""
             | counter: $result
             | attempts: ${attempts.get()}
      """.stripMargin
  end run

  private def increment(counter: Ref[Int]): UIO[Unit] =
    counter.update: value =>
      attempts.incrementAndGet()
      value + 1
  end increment
end App2

object App3 extends ZIOAppDebug with AppBase:
  private val num                                     = 30_000
  private val attempts                                = new AtomicInteger()
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    for
      counter <- Ref.Synchronized.make(0)
      _       <- parallel(num):
        increment(counter)
      result <- counter.get
    yield s"""
             | counter: $result
             | attempts: ${attempts.get()}
    """.stripMargin
  end run

  private def increment(counter: Ref.Synchronized[Int]): UIO[Unit] =
    counter.updateZIO: value =>
      ZIO.succeed:
        attempts.incrementAndGet()
        value + 1
  end increment
end App3
