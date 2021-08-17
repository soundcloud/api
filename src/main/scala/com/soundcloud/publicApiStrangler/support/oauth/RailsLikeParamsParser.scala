package com.soundcloud.publicApiStrangler.support.oauth

import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.twitter.finagle.http.exp.Multipart.{InMemoryFileUpload, OnDiskFileUpload}
import com.twitter.finagle.http.exp.{Multipart, MultipartDecoder}
import com.twitter.finagle.http.{MediaType, Request}
import com.twitter.io.{Buf, BufReader, Reader}
import com.twitter.util.{Base64StringEncoder, Future}
import play.api.libs.json._

import java.io.File
import scala.util.{Success, Try}

class RailsLikeParamsParser {
  def parse(request: HandlerRequest): Option[Map[String, String]] = {
    for {
      requestParams <- Some(request.params)
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

  private def parseRequestBody(request: Request): Option[Map[String, String]] = {
    request.mediaType match {
      case Some(MediaType.Json) => parseJsonRequest(request)
      case Some(MediaType.MultipartForm) => parseMultipartBody(request)
      case Some(MediaType.WwwForm) =>
        // In this case, finagle will parse and return request.params accordingly.
        Some(Map.empty)
      case _ =>
        // It would be possible to restrict to known Content-Types only here.
        // Since we're trying to be permissive, this isn't an error.
        Some(Map.empty)
    }
  }

  private def parseJsonRequest(request: Request): Option[Map[String, String]] = {
    val bytes = Buf.ByteArray.Owned.extract(request.content)

    Try(Json.parse(bytes).as[JsObject])
      .map(extractJsonParams)
      .toOption
  }

  private def extractJsonParams(json: JsObject): Map[String, String] = {
    json.fieldSet.foldLeft(Map[String, String]()) {
      case (acc, field) =>
        field match {
          case (k, v @ (_: JsBoolean | _: JsNumber)) => acc + (k -> v.toString)
          case (k, JsString(v)) => acc + (k -> v)
          case _ => acc
        }
    }
  }

  private def parseMultipartBodyWithFiles(request: Request, fileName: String): Future[Option[Buf]] = {
    MultipartDecoder.decode(request) match {
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

  private def parseMultipartBody(request: Request): Option[Map[String, String]] = {
    Try(
      MultipartDecoder
        .decode(request)
        .map(_.attributes)
    ).map(extractMultipartParams) match {
      case Success(params) if params.nonEmpty => Some(params)
      case _ => None
    }
  }

  private def extractMultipartParams(attributes: Option[Map[String, Seq[String]]]): Map[String, String] = {
    attributes.getOrElse(Map.empty).foldLeft(Map[String, String]()) {
      case (acc, attribute) =>
        attribute match {
          case (k, vs) if vs.nonEmpty => acc + (k -> vs.last)
        }
    }
  }

  private def parseAuthHeaders(request: HandlerRequest): Option[Map[String, String]] = {
    request.authorization match {
      case Some(header) =>
        if (header.startsWith("Basic")) {
          BasicAuthCredentials.unapply(header.substring("Basic".length()).trim()) match {
            case Some(params) =>
              Some(Map("client_id" -> params._1, "client_secret" -> params._2))
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
