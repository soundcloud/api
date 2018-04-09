package com.soundcloud.publicApiStrangler.client

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.client.support.{FetchClient, JsonResponse}
import com.twitter.finagle.http.Status.Successful
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json._

class TimelineJsonClient(service: JsonClient) extends FetchClient {

  /*
   * Fetches the stream as items
   *
   * The reverseCursor parameter allows fetching items *before* a given cursor, if set to true
   */
  def itemStream(session: UserSession, cursor: Option[String], pageSize: Int = 50, reverseCursor: Boolean = false, cursorEncoding: Option[String] = None): Future[JsObject] =
    fetch(
      service,
      session,
      Path() / "item_stream",
      paramsFor(cursor, pageSize, reverseCursor, cursorEncoding),
      Headers.empty
    ).map(SingleItem(_))

  /*
   * Fetches the stream as events
   *
   * The reverseCursor parameter allows fetching items *before* a given cursor, if set to true
   */
  def stream(session: UserSession, cursor: Option[String], pageSize: Int = 50, reverseCursor: Boolean = false, cursorEncoding: Option[String] = None): Future[JsObject] =
    fetch(
      service,
      session,
      Path() / "stream",
      paramsFor(cursor, pageSize, reverseCursor, cursorEncoding),
      Headers.empty
    ).map(SingleItem(_))

  /*
   * Fetches activities
   *
   * The reverseCursor parameter allows fetching items *before* a given cursor, if set to true
   */
  def activities(session: UserSession, cursor: Option[String], pageSize: Int = 50, reverseCursor: Boolean = false, cursorEncoding: Option[String] = None): Future[JsObject] =
    fetch(
      service,
      session,
      Path() / "activities",
      paramsFor(cursor, pageSize, reverseCursor, cursorEncoding),
      Headers.empty
    ).map(SingleItem(_))

  def profile(session: UserSession, user: Urn, cursor: Option[String], pageSize: Int = 50, reverseCursor: Boolean = false, cursorEncoding: Option[String] = None): Future[JsObject] =
    fetch(
      service,
      session,
      Path() / "profiles" / user.toString,
      paramsFor(cursor, pageSize, reverseCursor, cursorEncoding),
      Headers.empty
    ).map(SingleItem(_))

  def postedAndRepostedTracks(session: UserSession, user: Urn, cursor: Option[String], pageSize: Int = 50, reverseCursor: Boolean = false, cursorEncoding: Option[String] = None): Future[JsObject] =
    fetch(
      service,
      session,
      Path() / "profiles" / user.toString / "tracks" / "posted_and_reposted",
      paramsFor(cursor, pageSize, reverseCursor, cursorEncoding),
      Headers.empty
    ).map(SingleItem(_))

  def postedAndRepostedPlaylists(session: UserSession, user: Urn, cursor: Option[String], pageSize: Int = 50, reverseCursor: Boolean = false, cursorEncoding: Option[String] = None): Future[JsObject] =
    fetch(
      service,
      session,
      Path() / "profiles" / user.toString / "playlists" / "posted_and_reposted",
      paramsFor(cursor, pageSize, reverseCursor, cursorEncoding),
      Headers.empty
    ).map(SingleItem(_))

  def postedAndLikedPlaylists(session: UserSession, user: Urn, cursor: Option[String], pageSize: Int = 50, reverseCursor: Boolean = false, cursorEncoding: Option[String] = None): Future[JsObject] =
    fetch(
      service,
      session,
      Path() / "profiles" / user.toString / "playlists" / "posted_and_liked",
      paramsFor(cursor, pageSize, reverseCursor, cursorEncoding),
      Headers.empty
    ).map(SingleItem(_))

  def reposts(session: UserSession, user: Urn, cursor: Option[String], pageSize: Int = 50, reverseCursor: Boolean = false, cursorEncoding: Option[String] = None): Future[JsObject] =
    fetch(
      service,
      session,
      Path() / "profiles" / user.toString / "reposts",
      paramsFor(cursor, pageSize, reverseCursor, cursorEncoding),
      Headers.empty
    ).map(SingleItem(_))

  def likes(session: UserSession, user: Urn, cursor: Option[String], pageSize: Int = 50, reverseCursor: Boolean = false, cursorEncoding: Option[String] = None): Future[JsObject] =
    fetch(
      service,
      session,
      Path() / "profiles" / user.toString / "likes",
      paramsFor(cursor, pageSize, reverseCursor, cursorEncoding),
      Headers.empty
    ).map(SingleItem(_))

  /*
   * Fetches the followings' tracks as events. This is solely used by the IFTTT integration.
   *
   * The reverseCursor parameter allows fetching items *before* a given cursor, if set to true
   */
  def followingsTracks(session: UserSession, cursor: Option[String], pageSize: Int = 50, reverseCursor: Boolean = false, cursorEncoding: Option[String] = None): Future[JsObject] = {
    val noPaging = JsObject(Seq())

    def tracksOnly(stream: JsObject) = {
      val allEvents = (stream \ "events").as[JsArray].value
      val trackEvents = allEvents.filter(event => (event \ "type").toOption.contains(JsString("track")))
      trackEvents
    }

    stream(session, cursor, pageSize, reverseCursor, cursorEncoding).map { stream =>
      Json.obj(
        "events" -> tracksOnly(stream),
        "meta" -> noPaging
      )
    }
  }

  private def paramsFor(cursor: Option[String], pageSize: Int, reverseCursor: Boolean, cursorEncoding: Option[String]): Params = {
    val params = Map("page_size" -> pageSize.toString) ++ cursorEncoding.map("cursor_encoding" -> _)
    cursor match {
      case Some(cursor: String) =>
        params ++
          Map(
            "cursor" -> cursor,
            "direction" -> (if (reverseCursor) "before" else "after")
          )
      case _ => params
    }
  }

  private def invalidResponse(response: Response): Nothing = {
    throw new IllegalStateException(s"Invalid response: status=${response.statusCode},body=${response.contentString},headers=${response.headerMap}")
  }

  object OptionalSingleItem {
    def apply(response: Response): Option[JsObject] =
      JsonResponse.from(response) match {
        case JsonResponse(Status.NotFound, _, _) => None
        case JsonResponse(Successful(_), Right(JsNull), _) => None
        case JsonResponse(Successful(_), Right(json: JsObject), _) => Some(json)
        case _ => invalidResponse(response)
      }
  }

  object SingleItem {
    def apply(response: Response): JsObject = OptionalSingleItem(response).getOrElse(invalidResponse(response))
  }

}