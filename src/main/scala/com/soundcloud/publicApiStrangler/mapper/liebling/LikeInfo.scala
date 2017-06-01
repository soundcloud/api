package com.soundcloud.publicApiStrangler.mapper.liebling

import com.soundcloud.publicApiStrangler.support.mapping.ObjectMapping

trait LikeInfo extends ObjectMapping[(Boolean, Long)] {
  def did_user_like = resource._1

  def like_count = resource._2
}
