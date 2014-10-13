//package com.soudcloud.publicApiStrangler.controller
//
//import com.soundcloud.bff.finagle.Request
//import com.soundcloud.bff.test.{UnitSpecification, ControllerSpecification}
//import com.soundcloud.bff.{BazookaConfigComponent, BffApp}
//import com.soundcloud.jvmkit.config.Config
//import com.soundcloud.scalakit.finagle.http.HttpServer
//
//class TimelineControllerSpec extends UnitSpecification with ControllerSpecification {
//  val controller = new TimelineController {
//    override def createRoutes(server: HttpServer) = ???
//    override def config: Config = ???
//  }
//
//  trait Context extends Scope {
//    val request = mock[Request]
//
//  }
//
//  "/e1/me/stream" >> {
//    "renders the stream" in new Context {
//      val res = controller.get("/e1/me/stream")
//      println(controller)
//    }
//  }
//
//}
