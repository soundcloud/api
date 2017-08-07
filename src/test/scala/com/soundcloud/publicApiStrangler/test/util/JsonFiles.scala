package com.soundcloud.publicApiStrangler.test.util

import java.io._

import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import play.api.libs.json.Json
import play.api.libs.json.JsValue

import scala.io.Source
import scala.util.control.NonFatal

class JsonFiles {
  lazy val logger = SoundCloudLoggerFactory.getLogger(this.getClass)
  lazy val baseDir = "src/test/resources/fixtures"
  private val encoding = "UTF-8"

  def write(name: String, payload: String): Unit = {
    this.synchronized {
      if (filePath(name).exists()) {
        filePath(name).delete()
      }

      val bw = new OutputStreamWriter(new FileOutputStream(filePath(name)), encoding)
      bw.write(payload)
      bw.close()
    }
  }

  def load(name: String): Option[JsValue] = {
    val path = filePath(name)

    if (path.exists()) {
      val contents = getContentsOfFile(path)
      Some(parseContents(contents))
    } else {
      logger.warn(s"File $path doesnt exist")
      None
    }
  }

  private def filePath(name: String): File = {
    new File(s"$baseDir/$name.json")
  }

  private def getContentsOfFile(path: File): String = {
    val contents = try {
      Source.fromFile(path, encoding).mkString
    } catch {
      case NonFatal(e) => {
        logger.error(s"Error: [$e] while parsing fixture [$path]", e)
        throw e
      }
    }
    contents
  }

  private def parseContents(contents: String): JsValue = {
    try {
      Json.parse(contents)
    } catch {
      case NonFatal(e) => {
        logger.error(s"Error: [$e] while parsing json fixture:\n###\n$contents\n###")
        throw e
      }
    }
  }
}
