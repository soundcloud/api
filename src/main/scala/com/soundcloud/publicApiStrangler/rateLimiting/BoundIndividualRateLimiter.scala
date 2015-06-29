package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.publicApiStrangler.rateLimiting.reporting.ReportingRateLimitEventListener
import com.soundcloud.publicApiStrangler.support.TimeConversions.RichTwitterTime
import com.soundcloud.ratelimiting.core.{AccessDependentRateLimit, ClientApplication, RateLimitIdentity}
import com.soundcloud.ratelimiting.events.{Event, ReachedEventPayload}
import com.soundcloud.scalakit.cache.Cache
import com.twitter.util.{Future, Time}
import org.joda.time.DateTime

private[rateLimiting] class BoundIndividualRateLimiter(
    cache: Cache,
    listeners: Seq[EventListener[Event[ReachedEventPayload]]],
    accessDependentRateLimit: AccessDependentRateLimit) {

  val mediator = new RateLimiterCacheMediator(cache, accessDependentRateLimit)
  val rateLimitIdentity = RateLimitIdentity.from(
    accessDependentRateLimit.suitableConfiguration,
    accessDependentRateLimit.rateLimit.group,
    accessDependentRateLimit.rateLimit.mode)

  def event(clientApplication: ClientApplication, status: RateLimitStatus) = {
    Event(DateTime.now, ReportingRateLimitEventListener.stranglerUrn,
      ReachedEventPayload(
        clientApplication, rateLimitIdentity, status.resetTime.map(_.toJodaDateTime), status.requestCount))
  }

  val advancing = RateLimitStatus(rateLimitIdentity, _: Int, _: Option[Time])
  val reached = RateLimitStatus.reached(rateLimitIdentity, _: Option[Time])

  def advanceRateLimitStatus: Future[RateLimitStatus] = {
    mediator.alreadyReached.flatMap { alreadyReached =>
      if (alreadyReached) {
        mediator.expiry.map(reached)
      } else {
        mediator.requestsMadeSoFar.flatMap {
          case Some(number) if number > rateLimitIdentity.maximumNrOfRequests =>
            Future.join(mediator.expiry, mediator.markAsReached).map { case (expiry, _) =>
              val status = reached(expiry)
              notifyListeners(event(accessDependentRateLimit.accessMechanism.clientApplication, status))
              status
            }
          case Some(number) =>
            Future.join(mediator.updateRequestCount, mediator.expiry).map { case (updatedRequestCount, expiry) =>
              val status = advancing(number.toInt, expiry)
              notifyListeners(event(accessDependentRateLimit.accessMechanism.clientApplication, status))
              status
            }
          case None =>
            mediator.establish.map { expiry => advancing(0, Some(expiry)) }
        }
      }
    }
  }

  def rateLimitStatus: Future[RateLimitStatus] = {
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

  private def notifyListeners(event: Event[ReachedEventPayload]): Unit = {
    listeners.foreach(_.notify(event))
  }

}
