package com.soundcloud.apipublic.handler.muzooka

import com.twitter.io.Buf

import java.io.{ByteArrayOutputStream, InputStream}
import java.net.URL

class ImageDownloader {
  def downloadImageToBuf(imageUrl: String): Buf = {
    val url = new URL(imageUrl)
    val connection = url.openConnection()
    connection.setRequestProperty("User-Agent", "Mozilla/5.0")

    val inputStream: InputStream = connection.getInputStream
    val byteArrayOutputStream = new ByteArrayOutputStream()

    val buffer = new Array[Byte](1024 * 8)
    var bytesRead = inputStream.read(buffer)
    while (bytesRead != -1) {
      byteArrayOutputStream.write(buffer, 0, bytesRead)
      bytesRead = inputStream.read(buffer)
    }

    inputStream.close()
    Buf.ByteArray.Owned(byteArrayOutputStream.toByteArray)
  }
}
