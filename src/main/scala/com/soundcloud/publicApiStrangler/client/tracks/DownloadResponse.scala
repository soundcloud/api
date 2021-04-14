package com.soundcloud.publicApiStrangler.client.tracks

sealed trait DownloadResponse

case class DownloadUrlResponse(url: String) extends DownloadResponse
case object DownloadErrorResponse extends DownloadResponse
