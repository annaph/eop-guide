package org.eop.guide.resilience

case class FileContent(content: Seq[String])

object FileContent:
  def empty: FileContent =
    FileContent(content = Seq.empty[String])

  def hardcoded(name: String): FileContent =
    name match
      case "Awesome Memes" =>
        FileContent(
          content = Seq(
            "viralImage1",
            "viralImage2",
            "viralImage3"
          )
        )

      case _ =>
        FileContent.empty
  end hardcoded
end FileContent
