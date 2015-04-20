package com.soundcloud.publicApiStrangler.features

import com.soundcloud.bff.{Json, Future}
import com.soundcloud.bff.web.BffInjectionBasedController

class RolloutController(rollout: Rollout) extends BffInjectionBasedController {

  get("/-/features") { _ =>
    val activationsMap = rollout.allFeatures.map {
      case (feature, percentage) => Map[String, Any]("name" -> feature, "percentage" -> percentage)
    }

    Future(render.json(
      Map("features" -> activationsMap)
    ))
  }

  post("/-/features") { request =>
    val feature: String = request.params.getOrElse("name", "")
    val percentage: Int = request.params.getOrElse("percentage", "-1").toInt

    if (feature.isEmpty || percentage == -1) {
      Future(render.status(400))
    }
    else {
      rollout.activate(feature, percentage)
      Future(render.status(200))
    }
  }

  delete("/-/features") { request =>
    request.params.get("name") match {
      case Some(featureName) => {
        rollout.delete(featureName)
        Future(render.status(200))
      }
      case None => Future(render.status(400))
    }
  }
}