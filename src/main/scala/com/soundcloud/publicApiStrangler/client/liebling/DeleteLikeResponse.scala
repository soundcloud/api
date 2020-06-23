package com.soundcloud.publicApiStrangler.client.liebling

sealed trait DeleteLikeResponse
case object LikeDeleted extends DeleteLikeResponse
case object LikeNotFound extends DeleteLikeResponse
