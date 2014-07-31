package com.soudcloud.bff.test.fixtures

import java.io.{File, FileWriter, BufferedWriter}

import com.soundcloud.bff._
import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory

import scala.io.Source
import scala.xml.{Elem, XML}

class XmlFiles {

  lazy val logger = SoundCloudLoggerFactory.getLogger(this.getClass)
  val baseDir = "src/test/resources/fixtures"

  def write(name: String, payload: String): Unit = {
    this.synchronized {
      if (filePath(name).exists()) {
        filePath(name).delete()
      }

      val bw = new BufferedWriter(new FileWriter(filePath(name)))
      bw.write(payload)
      bw.close()
    }
  }

  def load(name: String): Option[Elem] = {
    val path = filePath(name)

    if (path.exists()) {
      val contents = getContentsOfFile(path)
      Some(parseContents(contents))
    } else {
      logger.warn(s"File $path doesn't exist")
      None
    }
  }

  private def filePath(name: String): File = {
    new File(s"$baseDir/$name.xml")
  }

  private def getContentsOfFile(path: File): String = {
    val contents = try {
      Source.fromFile(path, "UTF-8").mkString
    } catch {
      case NonFatal(e) => logger.error(s"Error: [$e] while parsing fixture [$path]", e); throw e
    }
    contents
  }

  private def parseContents(contents: String): Elem = {
    try {
      XML.loadString(contents)
    } catch {
      case NonFatal(e) => println(s"Error: [$e] while parsing json fixture:\n###\n$contents\n###"); throw e
    }
  }
}