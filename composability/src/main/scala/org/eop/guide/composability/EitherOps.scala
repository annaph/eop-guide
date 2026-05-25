package org.eop.guide.composability

import org.eop.guide.StringOps.withMagentaBackground
import org.eop.guide.composability.AppBase.NoWikiArticle

trait EitherOps:
  def wikiArticle(topic: String): Either[NoWikiArticle.type, String] =
    println(s"Wiki - Fetching article for '$topic'...".withMagentaBackground)
    Thread.sleep(150)

    topic match
      case "stock market" | "space" | "genome" =>
        val result = Right(s"detailed history of $topic")
        println(s"Wiki - article: $result".withMagentaBackground)
        result

      case _ =>
        Left(NoWikiArticle)
  end wikiArticle
end EitherOps
