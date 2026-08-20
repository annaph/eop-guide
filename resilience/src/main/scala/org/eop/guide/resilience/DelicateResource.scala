package org.eop.guide.resilience

import org.eop.guide.StringOps.withMagentaBackground
import zio.{IO, Queue, Ref, UIO, ZIO, ZLayer, durationInt}

trait DelicateResource:
  val requestProcessor: IO[String, Unit]
end DelicateResource

object DelicateResource:
  val live: ZLayer[Any, Nothing, DelicateResource] =
    ZLayer.fromZIO:
      for
        _ <- ZIO.debug:
          "Creating Delicate Resource...".withMagentaBackground
        _ <- ZIO.debug:
          "Do not make more than three concurrent requests!".withMagentaBackground
        pendingRequestsQueue <- Queue.unbounded[Char]
        _                    <- pendingRequestsQueue.offerAll('A' to 'Z')
        runningRequestsRef   <- Ref.Synchronized.make(Seq.empty[Char])
      yield new DelicateResourceImpl(pendingRequestsQueue, runningRequestsRef)
end DelicateResource

class DelicateResourceImpl(
    pendingRequestsQueue: Queue[Char],
    runningRequestsRef: Ref.Synchronized[Seq[Char]]
) extends DelicateResource:
  override val requestProcessor: IO[String, Unit] =
    for
      currentRequest <- runningRequestsRef.modifyZIO(addRequest)
      _              <- executeRequest(currentRequest)
      _ <- runningRequestsRef.update(removeRequest(_, currentRequest))
    yield ()
  end requestProcessor

  private def addRequest(
      runningRequests: Seq[Char]
  ): IO[String, (Char, Seq[Char])] =
    pendingRequestsQueue.take.flatMap: request =>
      if runningRequests.size >= 3 then ZIO.fail("Crashed the server!!")
      else
        ZIO
          .succeed:
            request -> runningRequests.appended(request)
          .tap: result =>
            ZIO.debug(s"Running requests: ${result._2}".withMagentaBackground)
  end addRequest

  private def executeRequest(request: Char): UIO[Unit] =
    ZIO.sleep:
      (request.intValue * 10).milliseconds
  end executeRequest

  private def removeRequest(
      runningRequests: Seq[Char],
      request: Char
  ): Seq[Char] =
    runningRequests.filterNot(_ == request)
end DelicateResourceImpl
