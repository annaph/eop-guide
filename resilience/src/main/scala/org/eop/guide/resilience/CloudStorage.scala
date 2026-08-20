package org.eop.guide.resilience

import zio.{Ref, UIO, ZIO, ZLayer, durationInt}

trait CloudStorage:
  def retrieve(name: String): UIO[FileContent]

  def invoice: UIO[String]
end CloudStorage

object CloudStorage:
  val live: ZLayer[Any, Nothing, CloudStorage] =
    ZLayer.fromZIO:
      for requestCounter <- Ref.make(0)
      yield new CloudStorageImpl(requestCounter)
end CloudStorage

class CloudStorageImpl(requestCounter: Ref[Int]) extends CloudStorage:
  override def retrieve(name: String): UIO[FileContent] =
    for
      _ <- requestCounter.update(_ + 1)
      _ <- ZIO.sleep(3.seconds)
    yield FileContent.hardcoded(name)
  end retrieve

  override def invoice: UIO[String] =
    requestCounter.get.map: count =>
      "Amount owned: $" + count
  end invoice
end CloudStorageImpl
