package com.soundcloud.publicApiStrangler.controller

/**
 * Default headers returned by public api.
 */
object DefaultResponseHeaders {

  val defaultHeaders = Map(
    "Access-Control-Allow-Headers" -> "Accept, Authorization, Content-Type, Origin",
    "Access-Control-Allow-Methods" -> "GET, PUT, POST, DELETE",
    "Access-Control-Allow-Origin" -> "*",
    "Access-Control-Expose-Headers" -> "Date",
    "Cache-Control" -> "private, max-age=0, must-revalidate")

}
