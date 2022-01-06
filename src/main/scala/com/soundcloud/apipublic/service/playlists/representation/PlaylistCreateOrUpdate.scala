package com.soundcloud.apipublic.service.playlists.representation

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.apipublic.client.mothership.request.representation.{MissingValue, NullValue, NullableValue, Value}
import play.api.libs.json._
import proto.soundcloud.playlists.api.{PlaylistCreateOrUpdate => ProtoPlaylistCreateOrUpdate, Tracks => ProtoTracks}

case class PlaylistCreateOrUpdate(
    description: NullableValue[String] = MissingValue,
    ean: NullableValue[String] = MissingValue,
    genre: NullableValue[String] = MissingValue,
    label_name: NullableValue[String] = MissingValue,
    license: NullableValue[String] = MissingValue,
    permalink: NullableValue[String] = MissingValue,
    permalink_url: NullableValue[String] = MissingValue,
    public: NullableValue[Boolean] = MissingValue,
    purchase_title: NullableValue[String] = MissingValue,
    purchase_url: NullableValue[String] = MissingValue,
    release: NullableValue[String] = MissingValue,
    release_date: NullableValue[String] = MissingValue,
    set_type: NullableValue[String] = MissingValue,
    tag_list: NullableValue[String] = MissingValue,
    title: NullableValue[String] = MissingValue,
    tracks: NullableValue[Seq[Map[String, String]]] = MissingValue
) {

  def toProto: ProtoPlaylistCreateOrUpdate = {
    ProtoPlaylistCreateOrUpdate(
      description = mapNullableStringToOption(this.description),
      ean = mapNullableStringToOption(this.ean),
      genre = mapNullableStringToOption(this.genre),
      labelName = mapNullableStringToOption(this.label_name),
      license = mapNullableStringToOption(this.license),
      permalink = mapNullableStringToOption(this.permalink),
      permalinkUrl = mapNullableStringToOption(this.permalink_url),
      public = mapNullableBooleanToOption(this.public),
      purchaseTitle = mapNullableStringToOption(this.purchase_title),
      purchaseUrl = mapNullableStringToOption(this.purchase_url),
      release = mapNullableStringToOption(this.release),
      releaseDate = mapNullableStringToOption(this.release_date),
      setType = mapNullableStringToOption(this.set_type),
      tagList = mapNullableStringToOption(this.tag_list),
      title = mapNullableStringToOption(this.title),
      tracks = this.tracks match {
        case MissingValue => None
        case NullValue => Some(ProtoTracks(urns = Seq.empty))
        case Value(trackMaps) =>
          Some(
            ProtoTracks(urns = trackMaps.map(trackMap => Urn("soundcloud", "tracks", trackMap("id")).toString))
          )
      }
    )
  }

  def allFieldsMissing: Boolean = {
    productIterator.forall {
      case MissingValue => true
      case _ => false
    }
  }

  private def mapNullableStringToOption(nullableValue: NullableValue[String]): Option[String] = {
    nullableValue match {
      case Value(value) => Some(value)
      case NullValue => Some("")
      case MissingValue => None
    }
  }

  private def mapNullableBooleanToOption(nullableValue: NullableValue[Boolean]): Option[Boolean] = {
    nullableValue match {
      case Value(value) => Some(value)
      case NullValue => Some(false)
      case MissingValue => None
    }
  }
}

object PlaylistCreateOrUpdate {
  implicit def nullvalueReads[T: Reads] = new Reads[NullableValue[T]] {
    override def reads(jsValue: JsValue) = JsSuccess(NullableValue.read(jsValue))
  }

  implicit val reads = Reads[PlaylistCreateOrUpdate] { json: JsValue =>
    def extractNullableString(fieldName: String): NullableValue[String] =
      (json \ fieldName) match {
        case JsDefined(JsNull) => NullValue
        case JsDefined(JsString(str)) => Value(str)
        case _ => MissingValue
      }

    val isPublic = (json \ "sharing").asOpt[String].map {
      case "public" => Value(true)
      case "private" => Value(false)
      case invalid => throw new Exception(s"`sharing` can only be 'private' or 'public', given $invalid")
    }

    val trackIds = (json \ "tracks") match {
      case JsDefined(JsArray(idList)) =>
        Value(idList.map { idJson =>
          idJson.asOpt[Map[String, String]].getOrElse(idJson.asOpt[Map[String, Int]].get.mapValues(_.toString))
        })
      case _ => MissingValue
    }

    JsSuccess(
      PlaylistCreateOrUpdate(
        description = extractNullableString("description"),
        ean = extractNullableString("ean"),
        genre = extractNullableString("genre"),
        label_name = extractNullableString("label_name"),
        license = extractNullableString("license"),
        permalink = extractNullableString("permalink"),
        permalink_url = extractNullableString("permalink_url"),
        public = isPublic.getOrElse(MissingValue),
        purchase_title = extractNullableString("purchase_title"),
        purchase_url = extractNullableString("purchase_url"),
        release = extractNullableString("release"),
        release_date = extractNullableString("release_date"),
        set_type = extractNullableString("set_type"),
        tag_list = extractNullableString("tag_list"),
        title = extractNullableString("title"),
        tracks = trackIds
      )
    )
  }

  def fromForm(
      playlistParams: Map[String, String],
      trackIds: Option[Seq[Map[String, String]]]
  ): PlaylistCreateOrUpdate = {
    PlaylistCreateOrUpdate(
      description = parseNullableStringInput(playlistParams, "description"),
      ean = parseNullableStringInput(playlistParams, "ean"),
      genre = parseNullableStringInput(playlistParams, "genre"),
      label_name = parseNullableStringInput(playlistParams, "label_name"),
      license = parseNullableStringInput(playlistParams, "license"),
      permalink = parseNullableStringInput(playlistParams, "permalink"),
      permalink_url = parseNullableStringInput(playlistParams, "permalink_url"),
      public = isPublic(playlistParams).getOrElse(MissingValue),
      purchase_title = parseNullableStringInput(playlistParams, "purchase_title"),
      purchase_url = parseNullableStringInput(playlistParams, "purchase_url"),
      release = parseNullableStringInput(playlistParams, "release"),
      release_date = parseNullableStringInput(playlistParams, "release_date"),
      set_type = parseNullableStringInput(playlistParams, "set_type"),
      tag_list = parseNullableStringInput(playlistParams, "tag_list"),
      title = parseNullableStringInput(playlistParams, "title"),
      tracks = trackIds.map(Value(_)).getOrElse(MissingValue)
    )
  }

  private def parseNullableStringInput(params: Map[String, String], fieldName: String): NullableValue[String] = {
    params.get(fieldName).map(v => Value[String](v)).getOrElse(MissingValue)
  }

  private def isPublic(params: Map[String, String]) = params.get("sharing").map {
    case "public" => Value(true)
    case "private" => Value(false)
    case invalid => throw new Exception(s"`sharing` can only be 'private' or 'public', given $invalid")
  }
}
