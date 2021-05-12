package com.soundcloud.publicApiStrangler.client.vk2

sealed trait Vk2Response

case class Human(correlationId: String) extends Vk2Response

case object SuspectedBot extends Vk2Response

case object Bot extends Vk2Response
