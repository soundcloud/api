package com.soundcloud.apipublic.client

import com.soundcloud.apipublic.client.TokenDispenserClient.AccessToken
import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.twitter.util.{Future, Return, Throw, Try}
import play.api.libs.json.{JsValue, Json}

class TokenDispenserClient(jsonClient: JsonClient) {
  import TokenDispenserClient.tokenScope

  /**
    * Dispenses an access token which can be used to access our internal services on behalf of a user.
    * There is currently nothing special about these tokens - they're just
    * regular access tokens assigned using a credential which is exclusively
    * used by third party applications e.g Muzooka
    *
    * @param resourceOwner the user to whom the access token will be associated.
    * @return - `Some(token)` if the token could be issued, `None` otherwise.
    */
  def dispenseAccessToken(
      resourceOwner: Urn,
      connectCredentialUrn: Urn
  ): Future[AccessToken] = {

    jsonClient
      .post(
        Path("/token-request"),
        Params.empty,
        Headers.empty(),
        Some(requestToJson(resourceOwner, connectCredentialUrn, tokenScope))
      )
      .flatMap { r =>
        Try(Json.parse(r.contentString) \ "data").map { x =>
          tokenFromJson(x(0))
        } match {
          case Return(token) => Future.value(AccessToken(token))
          case Throw(e) => Future.exception(new Exception(s"Failed to dispense token: ${e.getMessage}"))
        }
      }
  }

  private def requestToJson(resourceOwner: Urn, clientCredential: Urn, scope: String) = {
    Json.stringify(
      Json.obj(
        "token_request" -> Json.obj(
          "resource_owner" -> resourceOwner.toString,
          "client_credential" -> clientCredential.toString,
          "scope" -> scope
        )
      )
    )
  }

  private def tokenFromJson(tokenResponse: JsValue): String = {
    (tokenResponse \ "attributes" \ "access_token").as[String]
  }

}

object TokenDispenserClient {
  private val tokenScope = ""

  case class AccessToken(
      accessToken: String
  )
}
