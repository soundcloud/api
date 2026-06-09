package com.soundcloud.apipublic.client.applications

import com.soundcloud.jvmkit.module.json.play.UrnFormat._
import com.soundcloud.jvmkit.module.util.Urn
import play.api.libs.json.{Json, Writes}

case class ClientApplicationDetail(
    urn: Option[Urn],
    owner: Option[Urn],
    name: String,
    description: Option[String],
    url: Option[String],
    redirect_uri: Option[String],
    client_id: Option[String],
    client_secret: Option[String],
    credentials: Option[String]
)

object ClientApplicationDetail {
  implicit val writesClientApplicationDetail: Writes[ClientApplicationDetail] =
    Writes { detail =>
      Json.obj(
        "urn" -> detail.urn,
        "owner" -> detail.owner,
        "name" -> detail.name,
        "description" -> detail.description,
        "url" -> detail.url,
        "redirect_uri" -> detail.redirect_uri,
        "client_id" -> detail.client_id,
        "client_secret" -> detail.client_secret,
        "credentials" -> detail.credentials
      )
    }

  def fromCredentialsExpansion(app: ClientApplication, creds: Seq[ClientCredential]): Seq[ClientApplicationDetail] =
    creds.filter(_.revoked_at.isEmpty).map { cred =>
      ClientApplicationDetail(
        urn = Some(app.urn),
        owner = Some(app.owner),
        name = app.name,
        description = app.description,
        url = app.url,
        redirect_uri = cred.redirect_uri,
        client_id = cred.client_id,
        client_secret = cred.client_secret,
        credentials = cred.urn
      )
    }
}
