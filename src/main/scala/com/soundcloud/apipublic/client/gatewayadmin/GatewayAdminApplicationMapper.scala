package com.soundcloud.apipublic.client.gatewayadmin

import play.api.libs.json.JsValue

object GatewayAdminApplicationMapper {
  def fromJson(json: JsValue): GatewayAdminApplication =
    GatewayAdminApplication(
      accessLabel = (json \ "access_label").asOpt[String]
    )
}
