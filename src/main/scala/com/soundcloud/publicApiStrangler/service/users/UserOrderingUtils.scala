package com.soundcloud.publicApiStrangler.service.users

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.UserRepresentation

object UserOrderingUtils {

  def sortByProvidedUrns(users: Seq[UserRepresentation], urns: Seq[Urn]): Seq[UserRepresentation] = {
    val orderedByUrn = Ordering.by(urns.zipWithIndex.toMap compose {
      (_: UserRepresentation).urn
    })
    users.sorted(orderedByUrn)
  }
}
