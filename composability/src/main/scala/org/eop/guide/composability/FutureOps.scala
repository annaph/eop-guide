package org.eop.guide.composability

import org.eop.guide.StringOps.withMagentaBackground

import scala.concurrent.Future

trait FutureOps:
  protected def headline(scenario: Scenario): Future[String] =
    println(s"Network - Getting headline...".withMagentaBackground)

    scenario match
      case Scenario.Successful =>
        Future.successful:
          "stock market rising!"

      case Scenario.HeadlineError =>
        Future.failed:
          new Exception("Headline not available!")

      case Scenario.BoringTopic =>
        Future.successful:
          "boring content"

      case Scenario.FileSystemError =>
        Future.successful:
          "new unicode released!"

      case Scenario.WikiSystemError =>
        Future.successful:
          "Fred built a barn."

      case Scenario.AISlow =>
        Future.successful:
          "space is big!"

      case Scenario.DiskFull =>
        Future.successful:
          "human genome sequenced"
  end headline
end FutureOps
