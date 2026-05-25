package org.eop.guide.composability

import org.eop.guide.StringOps.withMagentaBackground

trait LLMOps:
  def summarize(article: String): String =
    println("AI - summarize - start".withMagentaBackground)

    if article.contains("space")
    then
      println("AI - taking a long time...".withMagentaBackground)
      Thread.sleep(7_000)

    val summary = s"This is a short summary on '$article'."

    println("AI - summarize - end".withMagentaBackground)
    summary
  end summarize
end LLMOps
