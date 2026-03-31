package org.eop.guide.failure

import org.eop.guide.StringOps.{withGreenBackground, withMagentaBackground}
import org.eop.guide.ZIOAppDebug
import org.eop.guide.ZIOOps.{debugAsGreen, debugAsRed}
import org.eop.guide.failure.AppBase.{AppFailure, FailException, FailObject}
import org.eop.guide.failure.Scenario.{
  GPSFailure,
  NetworkFailure,
  Successful,
  TooCold
}
import zio.{IO, Scope, Task, URIO, ZIO, ZIOAppArgs}

trait AppBase:
  protected def failureTypes(
      n: Int
  ): IO[AppFailure, Nothing] =
    n match
      case 0 => ZIO.fail("String fail")
      case 1 => ZIO.fail(FailObject)
      case _ => ZIO.fail(new FailException)
  end failureTypes

  protected def shortCircuit(limit: Int): IO[String, String] =
    for
      _      <- limitFail(step = 0, limit)
      _      <- limitFail(step = 1, limit)
      result <- limitFail(step = 2, limit)
    yield result
  end shortCircuit

  protected def readTemperature: ZIO[Scenario, SensorException, Temperature] =
    ZIO
      .debug(s"Reading temperature...".withMagentaBackground)
      .flatMap: _ =>
        ZIO
          .service[Scenario]
          .flatMap:
            case Successful =>
              ZIO.succeed:
                Temperature(degrees = 23)

            case Scenario.TooCold =>
              ZIO.succeed:
                Temperature(degrees = -37)

            case Scenario.NetworkFailure =>
              ZIO.fail:
                new NetworkException

            case Scenario.GPSFailure =>
              ZIO.fail:
                new GpsException
  end readTemperature

  protected def checkTemperature(
      temperature: Temperature
  ): IO[ClimateFailure, String] =
    ZIO
      .debug(s"Checking temperature...".withMagentaBackground)
      .flatMap: _ =>
        if temperature.degrees > 0 then
          ZIO.succeed:
            "Comfortable temperature"
        else
          ZIO.fail:
            ClimateFailure(message = "***Too Cold***")
  end checkTemperature

  protected def readTemperatureOrThrow(scenario: Scenario): Temperature =
    scenario match
      case Scenario.Successful =>
        Temperature(degrees = 23)

      case Scenario.TooCold =>
        Temperature(degrees = -37)

      case Scenario.NetworkFailure =>
        throw new NetworkException

      case Scenario.GPSFailure =>
        throw new GpsException
  end readTemperatureOrThrow

  private def limitFail(step: Int, limit: Int): IO[String, String] =
    for
      _      <- ZIO.succeed(s"Executing step $step....").debugAsGreen
      result <-
        if step < limit then ZIO.succeed(s"Completed step $step.")
        else ZIO.fail(s"Failied at step $step")
    yield result
  end limitFail
end AppBase

object AppBase:
  type AppFailure = String | FailException | FailObject.type

  class FailException extends Exception:
    override def toString: String = "FailException"
  end FailException

  case object FailObject
end AppBase

object App0 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    for
      _ <- ZIO.debug(s"Begin failing".withGreenBackground)
      _ <- failureTypes(0).flip.debugAsRed
      _ <- failureTypes(1).flip.debugAsRed
      _ <- failureTypes(2).flip.debugAsRed
      _ <- ZIO.debug(s"Done failing".withGreenBackground)
    yield ()
end App0

object App1 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    shortCircuit(limit = 0).flip
end App1

object App2 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    shortCircuit(limit = 1).flip
end App2

object App3 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    shortCircuit(limit = 2).flip
end App3

object App4 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    shortCircuit(limit = 3)
end App4

object App5 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    Successful.simulate:
      readTemperature
end App5

object App6 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    NetworkFailure.simulate:
      readTemperature
end App6

object App7 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    NetworkFailure.simulate:
      readTemperature <* ZIO.debug("should not print!")
end App7

object App8 extends ZIOAppDebug with AppBase:
  private val displayTemperature: URIO[Scenario, Temperature | String] =
    readTemperature.catchAll: _ =>
      ZIO.succeed("readTemperature failed!")

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    NetworkFailure.simulate:
      for
        result <- displayTemperature
        _      <- ZIO.debug(s"$result".withMagentaBackground)
      yield ()
end App8

object App9 extends ZIOAppDebug with AppBase:
  private val displayTemperature: URIO[Scenario, Temperature | String] =
    readTemperature.catchAll:
      case _: NetworkException =>
        ZIO.succeed("Network unavailable!")
      case _: GpsException =>
        ZIO.succeed("GPS Hardware Failure!")
  end displayTemperature

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    GPSFailure.simulate:
      for
        result <- displayTemperature
        _      <- ZIO.debug(s"$result".withMagentaBackground)
      yield ()
end App9

object App10 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    checkTemperature:
      Temperature(degrees = -17)
end App10

object App11 extends ZIOAppDebug with AppBase:
  private val weatherReport: URIO[Scenario, Unit] =
    readTemperature
      .flatMap(checkTemperature)
      .tap: result =>
        ZIO.debug(s"Weather report: '$result'".withMagentaBackground)
      .as(())
      .catchAll:
        case ex: SensorException =>
          ZIO.debug(s"Weather report: '${ex.getMessage}'!".withGreenBackground)

        case ex: ClimateFailure =>
          ZIO.debug(s"Weather report: ${ex.getMessage}!".withMagentaBackground)
  end weatherReport

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    TooCold.simulate:
      weatherReport
end App11

object App12 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    NetworkFailure.simulate:
      ZIO
        .service[Scenario]
        .flatMap: scenario =>
          ZIO.succeed:
            readTemperatureOrThrow(scenario)
end App12

object App13 extends ZIOAppDebug with AppBase:
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    NetworkFailure.simulate:
      ZIO
        .service[Scenario]
        .flatMap(readTemperatureSafely)
        .map(_.toString)
        .orElse:
          ZIO.succeed:
            "Could not read temperature"

  private def readTemperatureSafely(scenario: Scenario): Task[Temperature] =
    ZIO.attempt:
      readTemperatureOrThrow(scenario)
  end readTemperatureSafely
end App13
