package com.soundcloud.publicApiStrangler.mapper.purchaselink

import com.soundcloud.bff.Future
import com.soundcloud.bff.nextbff.mapper.Mapper
import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.publicApiStrangler.mapping.purchaselink.PurchaseLink
import com.soundcloud.publicApiStrangler.support.mapping.{InputValidation, ObjectMapping}
import com.soundcloud.scalakit._
import com.soundcloud.service.client.OkidokiClient

class TrackPurchaseLinkMapper(okidokiClient: OkidokiClient) extends Mapper[Urn, PurchaseLink]
with InputValidation[Urn, PurchaseLink]{
  override def mapNonEmptyInputs(session: UserSession, inputs: Set[Urn])(implicit context: MappingContext): Future[Map[Urn, PurchaseLink]] =
    okidokiClient.trackPurchaseLinks(session, inputs).map { trackPurchaseLinks =>
      trackPurchaseLinks.map(tpl => tpl.track_urn -> new ObjectMapping(tpl) with PurchaseLink).toMap
    }
}
