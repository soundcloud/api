package com.soundcloud.publicApiStrangler.request.representation

import play.api.libs.json._

case class PlaylistUpdate(
                           title: NullableValue[String] = MissingValue,
                           public: NullableValue[Boolean] = MissingValue,
                           description: NullableValue[String] = MissingValue,
                           genre: NullableValue[String] = MissingValue,
                           tag_list: NullableValue[String] = MissingValue,
                           license: NullableValue[String] = MissingValue,
                           label_name: NullableValue[String] = MissingValue,
                           release: NullableValue[String] = MissingValue,
                           release_date: NullableValue[String] = MissingValue,
                           purchase_url: NullableValue[String] = MissingValue,
                           purchase_title: NullableValue[String] = MissingValue,
                           ean: NullableValue[String] = MissingValue,
                           permalink: NullableValue[String] = MissingValue
                         ) {
  def toJsValueList: Seq[(String, JsValue)] =
    this.title.toOptionalJsValue.map("title" -> _).toSeq ++
      this.public.toOptionalJsValue.map("public" -> _).toSeq ++
      this.license.toOptionalJsValue.map("license" -> _).toSeq ++
      this.permalink.toOptionalJsValue.map("permalink" -> _).toSeq ++
      this.description.toOptionalJsValue.map("description" -> _).toSeq ++
      this.genre.toOptionalJsValue.map("genre" -> _).toSeq ++
      this.tag_list.toOptionalJsValue.map("tag_list" -> _).toSeq ++
      this.label_name.toOptionalJsValue.map("label_name" -> _).toSeq ++
      this.release.toOptionalJsValue.map("release" -> _).toSeq ++
      this.release_date.toOptionalJsValue.map("release_date" -> _).toSeq ++
      this.purchase_url.toOptionalJsValue.map("purchase_url" -> _).toSeq ++
      this.purchase_title.toOptionalJsValue.map("purchase_title" -> _).toSeq ++
      this.ean.toOptionalJsValue.map("ean" -> _).toSeq

}

object PlaylistUpdate {

  implicit val writes = Writes[PlaylistUpdate] { o => JsObject(o.toJsValueList) }
}

