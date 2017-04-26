package com.soundcloud.publicApiStrangler.support

import java.util.concurrent.{LinkedBlockingQueue, ThreadPoolExecutor, TimeUnit}

import airbrake.{AirbrakeNotice, Backtrace, AirbrakeNotifier => NativeAirbrakeNotifier}
import com.soundcloud.jvmkit.module.util.ResourceName
import com.soundcloud.jvmkit.module.util.config.{AppConfig, ConfigConvention}
import com.twitter.finagle.http.Request
import com.twitter.util.FuturePool

import scala.collection.JavaConversions._

/**
  * Convenience object for sending notifications to airbrake.
  */
object AirbrakeNotifier {

  private val airbrakeNotifier = new NativeAirbrakeNotifier

  //TODO: this is pretty much the sme as using a global variable, and couples this class with Bazooka(!). This config should have been injected.
  private val config = new AppConfig()
  private val airbrake = ResourceName("AIRBRAKE")
  private val apiKey = config.get(airbrake, ConfigConvention.API_KEY, "")
  private val environment = config.get(airbrake, ConfigConvention.ENV_NAME, "unknown")
  private val appName = config.getApplicationName

  private val executor = new ThreadPoolExecutor(1, 1, 1, TimeUnit.DAYS, new LinkedBlockingQueue[Runnable](100), new ThreadPoolExecutor.DiscardPolicy)
  private val futurePool = FuturePool(executor)

  private def headers(request: Request) =
    request.headerMap.entrySet().map {
      entry =>
        s"-H '${entry.getKey}: ${entry.getValue}'"
    }.mkString(" ")

  private def createNotice(
                            message: String,
                            backtrace: Backtrace,
                            errorClass: String = null,
                            requestData: Map[String, Object] = Map.empty,
                            requestPath: String = null) =
    new AirbrakeNotice(
      apiKey,
      appName,
      environment,
      message,
      errorClass,
      backtrace,
      requestData,
      Map.empty[String, Object],
      Map.empty[String, Object],
      List(),
      true,
      requestPath,
      null
    )

  private def fireNotice(notice: AirbrakeNotice) = futurePool {
    airbrakeNotifier.notify(notice)
  }

  /**
    * Sends notification to airbrake.
    *
    * @param request Used to create a curl that would help replicate the issue
    * @param status  Status code of the response
    * @param message Descriptive message
    * @param ex      Optional exception to get the stack trace
    */
  def notify(request: Request, status: Int, message: String, ex: Option[Throwable] = None): Unit = {
    val curl = s"curl -X ${request.method} 'http://${request.host.get}${request.uri}' ${headers(request)}"
    val backtrace = ex.map(new Backtrace(_)).getOrElse(new Backtrace(List()))
    val notice = createNotice(message, backtrace, status.toString, Map("curl" -> curl), request.uri)
    fireNotice(notice)
  }

  /**
    * Sends a notification to Airbrake.
    *
    * @param message Descriptive message to be sent
    */
  def notify(message: String): Unit = notify(message, None)

  /**
    * Sends a notification with an exception to Airbrake.
    *
    * @param message        Descriptive message to be sent
    * @param maybeThrowable Optional exception to get the stack trace
    */
  def notify(message: String, maybeThrowable: Option[Throwable]): Unit = {
    val (errorMessage, backtrace, errorClass) = maybeThrowable.map { t =>
      (s"$message\n$t", new Backtrace(t), t.getClass.getCanonicalName)
    }.getOrElse((message, new Backtrace(List()), null))
    val notice = createNotice(errorMessage, backtrace, errorClass)
    fireNotice(notice)
  }

  /**
    * Checks if airbrake is configured or not.
    *
    * @return
    */
  def isEnabled = apiKey.nonEmpty
}
