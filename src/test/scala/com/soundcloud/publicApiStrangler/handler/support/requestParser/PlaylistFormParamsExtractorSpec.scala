package com.soundcloud.publicApiStrangler.handler.support.requestParser

import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.publicApiStrangler.client.mothership.request.representation.{MissingValue, Value}
import com.soundcloud.publicApiStrangler.service.playlists.UpdatePlaylistArtworkRequest
import com.soundcloud.publicApiStrangler.service.playlists.representation.PlaylistCreateOrUpdate
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.{FileElement, FormElement, Request, RequestBuilder}
import com.twitter.io.{BufReader, Reader}
import com.twitter.util.Await

class PlaylistFormParamsExtractorSpec extends UnitSpecification {
  trait Context extends Scope {
    val trackid1 = "3311"
    val trackid2 = "723290971"
    val trackid3 = "60521501"
    val description = "description"
    val ean = "ean"
    val genre = "genre"
    val label_name = "label_name"
    val license = "license"
    val permalink = "permalink"
    val permalink_url = "permalink_url"
    val sharing = "public"
    val purchase_title = "purchase_title"
    val purchase_url = "http://purchase_url"
    val release = "release"
    val release_date = "release_date"
    val set_type = "set_type"
    val tag_list = "tag_list"
    val title = "title"
    val tracks = Value(Seq(Map("id" -> trackid1), Map("id" -> trackid2), Map("id" -> trackid3)))

    def fields = Seq[(String, String)](
      ("playlist[tracks][][id]", trackid1),
      ("playlist[tracks][][id]", trackid2),
      ("playlist[tracks][][id]", trackid3),
      ("playlist[description]", description),
      ("playlist[ean]", ean),
      ("playlist[genre]", genre),
      ("playlist[label_name]", label_name),
      ("playlist[license]", license),
      ("playlist[permalink]", permalink),
      ("playlist[permalink_url]", permalink_url),
      ("playlist[sharing]", sharing),
      ("playlist[purchase_title]", purchase_title),
      ("playlist[purchase_url]", purchase_url),
      ("playlist[release]", release),
      ("playlist[release_date]", release_date),
      ("playlist[set_type]", set_type),
      ("playlist[tag_list]", tag_list),
      ("playlist[title]", title)
    )

    def isMultiPart = false

    lazy val request = HandlerRequest(
      RequestBuilder()
        .url(Request.queryString("http://api.test", Map("a" -> "b")))
        .addFormElement(fields: _*)
        .buildFormPost(isMultiPart)
    )
    val playlistFormParamsExtractor = new PlaylistFormParamsExtractor()
  }

  trait ArtworkContext extends Context {
    val testImage = "test-image.jpg"

    val buf = Await.result(
      BufReader.readAll(Reader.fromStream(this.getClass.getClassLoader.getResourceAsStream(testImage)))
    )
    val file: FormElement = FileElement("playlist[artwork_data]", buf, Some("image/jpeg"), Some(testImage))

    override lazy val request = HandlerRequest(
      RequestBuilder()
        .url(Request.queryString("http://api.test", Map("a" -> "b")))
        .add(file)
        .buildFormPost(isMultiPart)
    )
  }

  "application/x-www-form-urlencoded" >> {
    "extracts all playlist params" in new Context {
      val expectedPlaylist = PlaylistCreateOrUpdate(
        Value(description),
        Value(ean),
        Value(genre),
        Value(label_name),
        Value(license),
        Value(permalink),
        Value(permalink_url),
        Value(true),
        Value(purchase_title),
        Value(purchase_url),
        Value(release),
        Value(release_date),
        Value(set_type),
        Value(tag_list),
        Value(title),
        tracks
      )

      val playlistParams = playlistFormParamsExtractor.playlistFromFormRequest(request)
      playlistParams match {
        case Good(params) => params ==== expectedPlaylist
        case _ => ko
      }
    }

    "does not extract artwork" in new ArtworkContext {
      Await.result(playlistFormParamsExtractor.artworkDataFromRequest(request)) ==== None
    }

    "when NO track params are passed" >> {
      "tracks is missing" in new Context {
        override def fields = Seq[(String, String)](
          ("playlist[title]", "title")
        )

        val expectedTracks = MissingValue

        val playlistParams = playlistFormParamsExtractor.playlistFromFormRequest(request)
        playlistParams match {
          case Good(params) => params.tracks ==== expectedTracks
          case _ => ko
        }
      }
    }
  }

  "multipart/form-data" >> {
    trait MultiPartContext extends Context {
      override def isMultiPart = true
    }

    "extracts all playlist params" in new MultiPartContext {
      val expectedPlaylist = PlaylistCreateOrUpdate(
        Value(description),
        Value(ean),
        Value(genre),
        Value(label_name),
        Value(license),
        Value(permalink),
        Value(permalink_url),
        Value(true),
        Value(purchase_title),
        Value(purchase_url),
        Value(release),
        Value(release_date),
        Value(set_type),
        Value(tag_list),
        Value(title),
        tracks
      )

      val playlistParams = playlistFormParamsExtractor.playlistFromFormRequest(request)
      playlistParams match {
        case Good(params) => params ==== expectedPlaylist
        case _ => ko
      }
    }

    "extracts artwork" in new ArtworkContext with MultiPartContext {
      Await.result(playlistFormParamsExtractor.artworkDataFromRequest(request)) ==== Some(
        UpdatePlaylistArtworkRequest(buf)
      )
    }

    "when NO track params are passed" >> {
      "tracks is missing" in new Context {
        override def fields = Seq[(String, String)](
          ("playlist[title]", "title")
        )

        val expectedTracks = MissingValue

        val playlistParams = playlistFormParamsExtractor.playlistFromFormRequest(request)
        playlistParams match {
          case Good(params) => params.tracks ==== expectedTracks
          case _ => ko
        }
      }
    }
  }

  "Unsupported requests throw a RuntimeException" in new Context {
    override lazy val request = HandlerRequest(
      RequestBuilder()
        .url(Request.queryString("http://api.test", Map("a" -> "b")))
        .buildGet
    )

    playlistFormParamsExtractor.playlistFromFormRequest(request) must throwA[RuntimeException]
  }
}
