<!--- Remove everything below and start over --->

Added rate limiting response for client credential grant exchange against `POST /oauth2/token`. In case of
rate limiting, the endpoint will return `429 - Too Many Requests`. 

The client credential grant exchange serves server-side client applications, and currently the tokens are valid for
24 hours. We expect reasonable usage for the client credential exchange per client application per 24 hours.  
