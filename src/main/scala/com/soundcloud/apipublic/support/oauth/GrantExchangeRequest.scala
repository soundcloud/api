package com.soundcloud.apipublic.support.oauth

case class GrantExchangeRequest(clientCredential: ClientCredential, accessGrant: AccessGrant, context: RequestContext)
