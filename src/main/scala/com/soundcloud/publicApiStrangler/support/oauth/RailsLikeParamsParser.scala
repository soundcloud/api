package com.soundcloud.publicApiStrangler.support.oauth

import java.io.File

import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.twitter.finagle.http.exp.Multipart.{InMemoryFileUpload, OnDiskFileUpload}
import com.twitter.finagle.http.exp.{Multipart, MultipartDecoder}
import com.twitter.finagle.http.{MediaType, Request}
import com.twitter.io.{Buf, Files}
import play.api.libs.json._

import scala.util.{Success, Try}

class RailsLikeParamsParser {
  def parse(request: HandlerRequest): Option[Map[String, String]] = {
    for {
      requestParams <- Some(request.params)
      bodyParams <- parseRequestBody(request)
    } yield requestParams ++ bodyParams
  }

  def parseFilesFromRequest(request: HandlerRequest, fileName: String): Option[Array[Byte]] = {
    request.mediaType match {
      case Some(MediaType.MultipartForm) => parseMultipartBodyWithFiles(request, fileName)
      case _ => None
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

  private def parseMultipartBodyWithFiles(request: Request, fileName: String): Option[Array[Byte]] = {
    MultipartDecoder.decode(request) match {
      case Some(Multipart(_, files)) =>
        files.get(fileName) match {
          case Some(fileUpload :: _) =>
            fileUpload match {
              case InMemoryFileUpload(content: Buf, _, _, _) =>
                Some(Buf.ByteArray.Owned.extract(content))

              case OnDiskFileUpload(content: File, _, _, _) => {
                val limit = 1024 * 1024 * 12
                Some(Files.readBytes(file = content, limit = limit))
              }
            }
          case _ => None

        }
      case _ => None
    }
  }

  private def parseMultipartBody(request: Request): Option[Map[String, String]] = {
    Try(
      MultipartDecoder
        .decode(request)
        .map(_.attributes)
    ).map(extractMultipartParams) match {
      case Success(params) if !params.isEmpty => Some(params)
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
}
