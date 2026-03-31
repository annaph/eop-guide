package org.eop.guide.failure

trait SensorException extends Exception

class NetworkException extends SensorException:
  override def getMessage = "Network Failure"
end NetworkException

class GpsException extends SensorException:
  override def getMessage = "GPS Failure"
end GpsException
