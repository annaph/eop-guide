package org.eop.guide.composability

import org.eop.guide.StringOps.withMagentaBackground
import org.eop.guide.composability.OptionOps.TOPICS

trait OptionOps:
  def topicOfInterest(content: String): Option[String] =
    println("Analytics - Scaning for topic...".withMagentaBackground)
    val result = TOPICS.find(content.contains)

    println(s"Analytics  - topic: $result".withMagentaBackground)
    result
  end topicOfInterest
end OptionOps

object OptionOps:
  private val TOPICS =
    Seq(
      "stock market",
      "space",
      "barn",
      "unicode",
      "genome"
    )
end OptionOps
