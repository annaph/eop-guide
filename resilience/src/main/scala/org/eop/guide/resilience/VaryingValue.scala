package org.eop.guide.resilience

import org.eop.guide.resilience.VaryingValue.Window
import zio.{Clock, Duration, IO, UIO, ZIO, duration2DurationOps}

import java.time.Instant
import java.util.concurrent.TimeoutException

class VaryingValue[T] private (windows: Seq[Window[T]]):
  def get(atZIO: => UIO[Instant] = Clock.instant): IO[TimeoutException, T] =
    atZIO.flatMap: at =>
      ZIO.getOrFailWith(new TimeoutException("Too soon or too late!")):
        windows
          .find: window =>
            (window.start.equals(at) || window.start.isBefore(at)) &&
              window.end.isAfter(at)
          .map(_.value)
  end get
end VaryingValue

object VaryingValue:
  def apply[T](
      scheduledValues: Seq[(Duration, T)],
      startTime: => Instant
  ): VaryingValue[T] =
    scheduledValues match
      case Seq() =>
        new VaryingValue[T](windows = Seq.empty[Window[T]])

      case Seq(head, tail @ _*) =>
        new VaryingValue(windows = toWindows(head, tail, startTime))
  end apply

  private def toWindows[T](
      headScheduledValue: (Duration, T),
      otherScheduledValues: Seq[(Duration, T)],
      startTime: => Instant
  ): Seq[Window[T]] =
    val (headDuration, headValue) = headScheduledValue

    val firstWindow = Window(
      start = startTime,
      end = startTime.plus(headDuration.asJava),
      value = headValue
    )

    otherScheduledValues.scanLeft(firstWindow):
      case (prevWindow, (duration, value)) =>
        Window(
          start = prevWindow.end,
          end = prevWindow.end.plus(duration.asJava),
          value
        )
  end toWindows

  private case class Window[T](start: Instant, end: Instant, value: T)
end VaryingValue
