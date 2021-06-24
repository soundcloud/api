package com.soundcloud.publicApiStrangler.support.oauth

case class GrantExchangeRequest(clientCredential: ClientCredential, accessGrant: AccessGrant, context: RequestContext)
