package com.soundcloud.publicApiStrangler.support.oauth

import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.twitter.finagle.http.exp.Multipart.{InMemoryFileUpload, OnDiskFileUpload}
import com.twitter.finagle.http.exp.{Multipart, MultipartDecoder}
import com.twitter.finagle.http.{MediaType, Method, Request}
import com.twitter.io.{Buf, BufReader, Reader}
import com.twitter.util.{Base64StringEncoder, Future}
import play.api.libs.json._

import java.io.File
import java.net.URLDecoder
import scala.util.{Success, Try}

class RailsLikeParamsParser {
  def parse(request: HandlerRequest): Option[Map[String, Seq[String]]] = {
    for {
      requestParams <- Some(request.params.map(tuple => tuple._1 -> Seq(tuple._2)))
      headerParams <- parseAuthHeaders(request)
      bodyParams <- parseRequestBody(request)
    } yield requestParams ++ headerParams ++ bodyParams
  }

  def parseFilesFromRequest(request: HandlerRequest, fileName: String): Future[Option[Buf]] = {
    request.mediaType match {
      case Some(MediaType.MultipartForm) => parseMultipartBodyWithFiles(request, fileName)
      case _ => Future.value(None)
    }
  }

  private def parseRequestBody(request: Request): Option[Map[String, Seq[String]]] = {
    request.mediaType match {
      case Some(MediaType.Json) => parseJsonRequest(request)
      case Some(MediaType.MultipartForm) => parseMultipartBody(request)
      case Some(MediaType.WwwForm) => Some(parseWwwFormBody(request))
      case _ =>
        // It would be possible to restrict to known Content-Types only here.
        // Since we're trying to be permissive, this isn't an error.
        Some(Map.empty)
    }
  }

  private def parseJsonRequest(request: Request): Option[Map[String, Seq[String]]] = {
    val bytes = Buf.ByteArray.Owned.extract(request.content)

    Try(Json.parse(bytes).as[JsObject])
      .map(extractJsonParams)
      .toOption
  }

  private def extractJsonParams(json: JsObject): Map[String, Seq[String]] = {
    json.fieldSet.foldLeft(Map[String, Seq[String]]()) {
      case (acc, field) =>
        field match {
          case (k, v @ (_: JsBoolean | _: JsNumber)) => acc + (k -> Seq(v.toString))
          case (k, JsString(v)) => acc + (k -> Seq(v))
          case _ => acc
        }
    }
  }

  private def parseMultipartBodyWithFiles(request: Request, fileName: String): Future[Option[Buf]] = {
    SCMultipartDecoder.decode(request) match {
      case Some(Multipart(_, files)) =>
        files.get(fileName) match {
          case Some(fileUpload :: _) =>
            fileUpload match {
              case InMemoryFileUpload(content: Buf, _, _, _) =>
                Future.value(Some(content))

              case OnDiskFileUpload(content: File, _, _, _) =>
                val limit = 1024 * 1024 * 12
                BufReader.readAll(Reader.fromFile(content, limit)).map(Some(_))
            }
          case _ => Future.value(None)

        }
      case _ => Future.value(None)
    }
  }

  private def parseWwwFormBody(request: Request): Map[String, Seq[String]] = {
    URLDecoder
      .decode(request.contentString, "UTF-8")
      .split('&')
      .map(_.split('='))
      .map(arr => (arr.headOption.getOrElse(""), if (arr.length > 1) arr(1) else ""))
      .foldLeft(Map[String, Seq[String]]()) {
        case (acc, attribute) =>
          attribute match {
            case (k, vs) => {
              val merged = acc.getOrElse(k, Seq.empty) ++ Seq(vs)
              acc + (k -> merged)
            }
          }
      }
  }

  private def parseMultipartBody(request: Request): Option[Map[String, Seq[String]]] = {
    Try(
      SCMultipartDecoder
        .decode(request)
        .map(_.attributes)
    ).map(extractMultipartParams) match {
      case Success(params) if params.nonEmpty => Some(params)
      case _ => None
    }
  }

  private def extractMultipartParams(attributes: Option[Map[String, Seq[String]]]): Map[String, Seq[String]] = {
    attributes.getOrElse(Map.empty).foldLeft(Map[String, Seq[String]]()) {
      case (acc, attribute) =>
        attribute match {
          case (k, vs) if vs.nonEmpty => acc + (k -> vs)
        }
    }
  }

  private def parseAuthHeaders(request: HandlerRequest): Option[Map[String, Seq[String]]] = {
    request.authorization match {
      case Some(header) =>
        if (header.startsWith("Basic")) {
          BasicAuthCredentials.unapply(header.substring("Basic".length()).trim()) match {
            case Some(params) => Some(Map("client_id" -> Seq(params._1), "client_secret" -> Seq(params._2)))
            case _ => None
          }
        } else Some(Map.empty)
      case _ => Some(Map.empty)
    }
  }
}

object BasicAuthCredentials {
  // TODO create a dedicated case class
  private val ClientAndSecret = """(?s)(.+):(.+)""".r

  def unapply(s: String): Option[(String, String)] = {
    Try(
      Base64StringEncoder.decode(s)
    ).fold(
      _ => None,
      arr =>
        new String(arr, "UTF-8") match {
          case ClientAndSecret(id, secret) => Some((id, secret))
          case _ => None
        }
    )
  }

}

private object SCMultipartDecoder {
  def decode(request: Request): Option[Multipart] = {

    /**
      * Ugly, but we have to manually change the request method from PUT to POST, otherwise MultiPart.Decode will return None
      * We change it back to Put after the parse call to avoid any potential side effects
      * https://twitter.github.io/finagle/docs/com/twitter/finagle/http/exp/MultipartDecoder.html
      * https://softwareengineering.stackexchange.com/a/319429
      */
    val originalMethodIsPut = request.method == Method.Put
    if (originalMethodIsPut) {
      request.method = Method.Post
    }
    val result = MultipartDecoder.decode(request)
    if (originalMethodIsPut) {
      request.method = Method.Put
    }
    result
  }
}
