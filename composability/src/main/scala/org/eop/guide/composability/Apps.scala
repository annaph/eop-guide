package org.eop.guide.composability

import org.eop.guide.StringOps.withMagentaBackground
import org.eop.guide.ZIOAppDebug
import org.eop.guide.composability.AppBase.{
  FileReadFailure,
  FileWriteFailure,
  HeadlineNotAvailable,
  NoInterestingTopic,
  NoWikiArticle,
  ResearchHeadlineError
}
import org.eop.guide.composability.Scenario.{
  AISlow,
  BoringTopic,
  DiskFull,
  FileSystemError,
  HeadlineError,
  Successful,
  WikiSystemError
}
import zio.{
  Chunk,
  IO,
  Schedule,
  Scope,
  Task,
  URIO,
  ZIO,
  ZIOAppArgs,
  durationInt
}

import java.util.concurrent.TimeoutException
import scala.util.{Try, Using}

trait AppBase extends FutureOps, OptionOps, EitherOps, LLMOps:
  val researchHeadline: ZIO[Scenario, ResearchHeadlineError, String] =
    ZIO.scoped:
      for
        headline <- headlineZ
        topic    <- topicOfInterestZ(headline)
        file     <- openFileZ(path = "file1")
        summary  <-
          if file.contains(topic) then summaryForFileZ(file, topic)
          else
            wikiArticleZ(topic)
              .flatMap(summarizeZ)
              .flatMap(prependToFileZ(file, _))
      yield summary
  end researchHeadline

  def headlineZ: ZIO[Scenario, HeadlineNotAvailable.type, String] =
    ZIO
      .service[Scenario]
      .flatMap: scenario =>
        ZIO
          .from:
            headline(scenario)
          .orElseFail:
            HeadlineNotAvailable
  end headlineZ

  def topicOfInterestZ(headline: String): IO[NoInterestingTopic, String] =
    ZIO
      .from:
        topicOfInterest(headline)
      .orElseFail:
        NoInterestingTopic(headline)
  end topicOfInterestZ

  def wikiArticleZ(topic: String): IO[NoWikiArticle.type, String] =
    ZIO.from:
      wikiArticle(topic)
  end wikiArticleZ

  def openFileZ(path: String): URIO[Scope, File] =
    ZIO.fromAutoCloseable:
      ZIO.succeed:
        File.openFile(path)
  end openFileZ

  def prependToFileZ(
      file: File,
      content: String
  ): IO[FileWriteFailure.type, String] =
    ZIO
      .from:
        file.prepend(content)
      .map(_.head)
      .orElseFail:
        FileWriteFailure
  end prependToFileZ

  def summaryForFileZ(
      file: File,
      topic: String
  ): IO[FileReadFailure, String] =
    ZIO
      .attempt:
        file.summary(term = topic)
      .orElseFail:
        FileReadFailure(topic)
  end summaryForFileZ

  def summarizeZ(article: String): Task[String] =
    ZIO
      .attemptBlockingInterrupt:
        summarize(article)
      .onInterrupt:
        ZIO.debug("AI **INTERRUPTED**".withMagentaBackground)
      .timeoutFail(new TimeoutException("LLM takong too long!")):
        3.seconds
  end summarizeZ
end AppBase

object AppBase:
  type ResearchHeadlineError = NoInterestingTopic | FileReadFailure |
    HeadlineNotAvailable.type | NoWikiArticle.type | FileWriteFailure.type |
    Throwable

  case class NoInterestingTopic(headline: String)

  case class FileReadFailure(topic: String)

  case object HeadlineNotAvailable

  case object NoWikiArticle

  case object FileWriteFailure
end AppBase

object App0 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    Successful.simulate:
      headlineZ
end App0

object App1 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    HeadlineError.simulate:
      headlineZ
end App1

object App2 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    topicOfInterestZ:
      "stock market rising!"
end App2

object App3 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    topicOfInterestZ:
      "boring and inane"
end App3

object App4 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    wikiArticleZ:
      "stock market"
end App4

object App5 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    wikiArticleZ:
      "barn"
end App5

object App6 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    ZIO.scoped:
      for file <- openFileZ(path = "file1")
      yield file.contains("topicOfInterest")
end App6

object App7 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    ZIO.succeed:
      areSame.flatten.flatten.get

  private def areSame: Try[Try[Try[Boolean]]] =
    Using(File.openFile(path = "file1")): file1 =>
      Using(File.openFile(path = "file2")): file2 =>
        Using(File.openFile(path = "file3")): file3 =>
          File.areSame(Seq(file1, file2, file3))
  end areSame
end App7

object App8 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    ZIO.scoped:
      for
        file1 <- openFileZ(path = "file1")
        file2 <- openFileZ(path = "file2")
        file3 <- openFileZ(path = "file3")
      yield File.areSame:
        Seq(file1, file2, file3)
end App8

object App9 extends ZIOAppDebug with AppBase:
  private val fileNames = Chunk("file1", "file2", "file3")

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    ZIO.scoped:
      ZIO
        .foreach(fileNames)(openFileZ)
        .map(File.areSame)
end App9

object App10 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    ZIO.scoped:
      for
        file   <- openFileZ(path = "file1")
        result <- prependToFileZ(file, content = "New data")
      yield result
end App10

object App11 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    ZIO.succeed:
      File
        .openFile(path = "file1")
        .summary(term = "space")
end App11

object App12 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    ZIO.succeed:
      File
        .openFile(path = "file1")
        .summary(term = "unicode")
end App12

object App13 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    ZIO.scoped:
      for
        file    <- openFileZ(path = "file1")
        summary <- summaryForFileZ(file, topic = "space")
      yield summary
end App13

object App14 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    ZIO.scoped:
      for
        file    <- openFileZ(path = "file1")
        summary <- summaryForFileZ(file, topic = "unicode")
      yield summary
end App14

object App15 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    ZIO.succeed:
      summarize(article = "Space is huge")
end App15

object App16 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    summarizeZ(article = "Space is huge")
end App16

object App17 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    summarizeZ(article = "genome")
end App17

object App18 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    HeadlineError.simulate:
      researchHeadline
end App18

object App19 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    BoringTopic.simulate:
      researchHeadline
end App19

object App20 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    FileSystemError.simulate:
      researchHeadline
end App20

object App21 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    WikiSystemError.simulate:
      researchHeadline
end App21

object App22 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    AISlow.simulate:
      researchHeadline
end App22

object App23 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    DiskFull.simulate:
      researchHeadline
end App23

object App24 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    Successful.simulate:
      researchHeadline
end App24

object App25 extends ZIOAppDebug with AppBase:
  private val quickHeadlineResearch: ZIO[
    Scenario,
    ResearchHeadlineError | String,
    String
  ] =
    researchHeadline.timeoutFail("strict timeout"):
      100.milliseconds

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    Successful.simulate:
      quickHeadlineResearch
end App25

object App26 extends ZIOAppDebug with AppBase:
  private val schedular: Schedule[Any, Any, Long] =
    Schedule.recurs(3) *> Schedule.spaced(7.seconds)

  private val repeatHeadlineResearch
      : ZIO[Scenario, ResearchHeadlineError, String] =
    researchHeadline
      .repeat:
        schedular.tapOutput: _ =>
          ZIO.debug(Seq.fill(97)('-').mkString(""))
      .map: result =>
        s"Number of repetiton: $result"

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    Successful.simulate:
      repeatHeadlineResearch
end App26
