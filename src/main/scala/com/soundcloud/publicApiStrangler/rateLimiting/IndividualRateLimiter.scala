package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.publicApiStrangler.rateLimiting.reporting.ReportingRateLimitEventListener
import com.soundcloud.publicApiStrangler.support.TimeConversions.RichTwitterTime
import com.soundcloud.ratelimiting.core.{RateLimitIdentity, ClientApplication, RateLimit}
import com.soundcloud.ratelimiting.events.{Event, ReachedEventPayload}
import com.soundcloud.scalakit.ResourceName
import com.soundcloud.scalakit.cache.Cache
import com.twitter.finagle.http.Request
import com.twitter.util.{Future, Time}
import org.joda.time.DateTime

class IndividualRateLimiter(cache: Cache,
                            val rateLimit: RateLimit,
                            applicationName: ResourceName,
                            listeners: Seq[EventListener[Event[ReachedEventPayload]]]) {

  val rateLimitIdentity = RateLimitIdentity.forRateLimit(rateLimit)

  def event(clientApplication: ClientApplication, status: RateLimitStatus) = {
    Event(DateTime.now, ReportingRateLimitEventListener.stranglerUrn,
      ReachedEventPayload(
        clientApplication, rateLimitIdentity,
        status.resetTime.map(_.toJodaDateTime), status.requestCount, rateLimit.mode))
  }

  val advancing = RateLimitStatus(rateLimit, _: Int, _: Option[Time])
  val reached = RateLimitStatus.reached(rateLimit, _: Option[Time])

  def appliesTo(request: Request) = {
    rateLimit.appliesTo(request.path)
  }

  def advanceRateLimitStatus(clientApplication: ClientApplication): Future[RateLimitStatus] = {
    val mediator = new RateLimiterCacheMediator(cache, rateLimit, clientApplication, applicationName)
    mediator.alreadyReached.flatMap { alreadyReached =>
      if (alreadyReached) {
        mediator.expiry.map(reached)
      } else {
        mediator.requestsMadeSoFar.flatMap {
          case Some(number) if number > rateLimit.maximumNrOfRequests =>
            Future.join(mediator.expiry, mediator.markAsReached).map { case (expiry, _) =>
              val status = reached(expiry)
              notifyListeners(event(clientApplication, status))
              status
            }
          case Some(number) =>
            Future.join(mediator.updateRequestCount, mediator.expiry).map { case (updatedRequestCount, expiry) =>
              val status = advancing(number.toInt, expiry)
              notifyListeners(event(clientApplication, status))
              status
            }
          case None =>
            mediator.establish.map { expiry => advancing(0, Some(expiry)) }
        }
      }
    }
  }

  def rateLimitStatus(clientApplication: ClientApplication): Future[RateLimitStatus] = {
    val mediator = new RateLimiterCacheMediator(cache, rateLimit, clientApplication, applicationName)
    Future.join(mediator.expiry, mediator.alreadyReached) flatMap { case (expiry, alreadyReached) =>
      if (alreadyReached)
        Future(reached(expiry))
      else
        mediator.requestsMadeSoFar map { requestsOpt =>
          val requests = requestsOpt.map(_.toInt).getOrElse(0)
          advancing(requests, expiry)
        }
    }
  }

  def notifyListeners(event: Event[ReachedEventPayload]): Unit = {
    listeners.foreach(_.notify(event))
  }
}
