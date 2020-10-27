package com.soundcloud.publicApiStrangler.support.oauth

case class TokenExchangeRequest(clientCredential: ClientCredential, accessGrant: AccessGrant, context: RequestContext)
