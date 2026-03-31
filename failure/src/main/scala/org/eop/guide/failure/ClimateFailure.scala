package org.eop.guide.failure

case class ClimateFailure(message: String) extends Exception:
  override def getMessage(): String = message
end ClimateFailure
