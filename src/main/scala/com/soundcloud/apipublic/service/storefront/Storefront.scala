package com.soundcloud.apipublic.service.storefront

import com.soundcloud.jvmkit.module.util.Urn
import play.api.libs.json._
import proto.soundcloud.fan_monetization.api.BuyModuleType

import java.net.URL
import scala.util.Try

sealed abstract class StorefrontType(val name: String, val proto: BuyModuleType)

object StorefrontType {
  case object Digital extends StorefrontType("digital", BuyModuleType.DIGITAL)
  case object Vinyl extends StorefrontType("vinyl", BuyModuleType.VINYL)
  case object Cd extends StorefrontType("cd", BuyModuleType.CD)
  case object Cassette extends StorefrontType("cassette", BuyModuleType.CASSETTE)
  case object Apparel extends StorefrontType("apparel", BuyModuleType.APPAREL)
  case object SamplePack extends StorefrontType("sample_pack", BuyModuleType.SAMPLE_PACK)
  case object Subscription extends StorefrontType("subscription", BuyModuleType.SUBSCRIPTION)
  case object LiveEvent extends StorefrontType("live_event", BuyModuleType.LIVE_EVENT)
  case object LiveStream extends StorefrontType("live_stream", BuyModuleType.LIVE_STREAM)
  case object Other extends StorefrontType("other", BuyModuleType.OTHER)

  val all: List[StorefrontType] =
    List(Digital, Vinyl, Cd, Cassette, Apparel, SamplePack, Subscription, LiveEvent, LiveStream, Other)

  def fromName(name: String): Option[StorefrontType] = all.find(_.name == name)

  def fromProto(buyModuleType: BuyModuleType): StorefrontType =
    all.find(_.proto == buyModuleType).getOrElse(Other)
}

case class Storefront(
    trackUrn: Urn,
    title: String,
    storefrontType: StorefrontType,
    link: String,
    linkTitle: Option[String],
    description: Option[String],
    imageUrl: Option[String],
    price: Option[String]
)

object Storefront {
  implicit val writes: Writes[Storefront] = Writes[Storefront] { storefront =>
    Json.obj(
      "track_urn" -> storefront.trackUrn.toString,
      "title" -> storefront.title,
      "type" -> storefront.storefrontType.name,
      "link" -> storefront.link,
      "link_title" -> storefront.linkTitle,
      "description" -> storefront.description,
      "image_url" -> storefront.imageUrl,
      "price" -> storefront.price
    )
  }
}

/** Marker for creators whose subscription does not include the storefront (buy module) feature. */
case object StorefrontNotEligible

case class StorefrontUpsert(
    title: String,
    storefrontType: StorefrontType,
    link: String,
    linkTitle: Option[String],
    description: Option[String],
    price: Option[String]
)

object StorefrontUpsert {
  private val MaxTitleLength = 100
  private val MaxLinkLength = 255
  private val MaxLinkTitleLength = 50
  private val MaxDescriptionLength = 500
  private val MaxPriceLength = 20

  def fromJson(json: JsValue): Either[String, StorefrontUpsert] = {
    for {
      title <- requiredString(json, "title", MaxTitleLength)
      typeName <- requiredString(json, "type", MaxTitleLength)
      storefrontType <- parseType(typeName)
      link <- requiredString(json, "link", MaxLinkLength)
      _ <- validateLink(link)
      linkTitle <- optionalString(json, "link_title", MaxLinkTitleLength)
      description <- optionalString(json, "description", MaxDescriptionLength)
      price <- optionalString(json, "price", MaxPriceLength)
    } yield StorefrontUpsert(title, storefrontType, link, linkTitle, description, price)
  }

  private def requiredString(json: JsValue, field: String, maxLength: Int): Either[String, String] =
    (json \ field).validate[String] match {
      case JsSuccess(value, _) =>
        val trimmed = value.trim
        if (trimmed.isEmpty) Left(s"$field must not be empty")
        else if (trimmed.length > maxLength) Left(s"$field must not exceed $maxLength characters")
        else Right(trimmed)
      case _ => Left(s"$field is required and must be a string")
    }

  private def optionalString(json: JsValue, field: String, maxLength: Int): Either[String, Option[String]] =
    (json \ field).toOption match {
      case None | Some(JsNull) => Right(None)
      case Some(JsString(value)) =>
        val trimmed = value.trim
        if (trimmed.isEmpty) Right(None)
        else if (trimmed.length > maxLength) Left(s"$field must not exceed $maxLength characters")
        else Right(Some(trimmed))
      case _ => Left(s"$field must be a string")
    }

  private def parseType(name: String): Either[String, StorefrontType] =
    StorefrontType
      .fromName(name)
      .toRight(
        s"type must be one of: ${StorefrontType.all.map(_.name).mkString(", ")}"
      )

  // java.net.URL instead of URI: URI rejects real-world URLs with unescaped characters like | or [
  private def validateLink(link: String): Either[String, Unit] = {
    val scheme = Try(new URL(link).getProtocol).toOption.map(_.toLowerCase)
    scheme match {
      case Some("http") | Some("https") => Right(())
      case _ => Left("link must be a valid http(s) URL")
    }
  }
}
