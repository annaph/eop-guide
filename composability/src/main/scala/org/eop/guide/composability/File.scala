package org.eop.guide.composability

import org.eop.guide.StringOps.withMagentaBackground

import scala.util.{Failure, Success, Try}

trait File extends AutoCloseable:
  def content: String
  def head: String
  def contains(term: String): Boolean
  def summary(term: String): String
  def prepend(entry: String): Try[File]
end File

object File:
  def openFile(path: String): File =
    println(s"File - OPEN: $path".withMagentaBackground)

    path match
      case "file1" | "file2" | "file3" =>
        new DummyFile(
          path,
          lines = List(
            "stacey is a nice girl!",
            "unicode latest version released",
            "anna is lovely person :)"
          )
        )

      case _ =>
        new DummyFile(path)
  end openFile

  def areSame(files: Seq[File]): Boolean =
    println("side-effect print: comparing content".withMagentaBackground)

    files match
      case Seq() =>
        true

      case last +: Seq() =>
        true

      case head +: tail =>
        tail.forall(_.content == head.content)
  end areSame
end File

class DummyFile(
    private val path: String,
    private val lines: List[String] = List.empty[String]
) extends File:
  override def content: String =
    lines.mkString("\n")

  override def head: String =
    lines.headOption.getOrElse(default = "")

  override def contains(term: String): Boolean =
    val result = lines.exists(_.contains(term))

    println(s"File - CONTAINS $term => $result".withMagentaBackground)
    result
  end contains

  override def summary(term: String): String =
    println(s"File - SUMMARY for: $term".withMagentaBackground)

    lines
      .filterNot(_.contains("unicode"))
      .find(_.contains(term)) match
      case Some(line) =>
        line

      case None =>
        println("File - * Threw Exception *".withMagentaBackground)
        throw new Exception("FileSystem error!")
  end summary

  override def prepend(entry: String): Try[File] =
    if entry.contains("genome")
    then
      println("File - disk full!".withMagentaBackground)
      Failure:
        new Exception("Disk is full!")
    else
      println(s"File - WRITE: $entry".withMagentaBackground)
      Success:
        new DummyFile(path, entry :: lines)
  end prepend

  override def close(): Unit =
    println(s"File - CLOSE: $path".withMagentaBackground)
end DummyFile
