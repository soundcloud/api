APP_NAME="publicapistrangler"
APP_BASE_URL="https://api.soundcloud.com"

ADMIN_APP_TIMEOUT_MILLIS="30000"

ENV="development"

JAVA_OPTS="-Xmx1G -Xms1G"
PORT="5000"

TELE_PORT="5001"
ZIPKIN_HOST="zipkin.dev.s-cloud.net"
ZIPKIN_PORT="9410"
ZIPKIN_SAMPLE_RATE="0.001"

SERVICE_MAX_CONNS="60"
SERVICE_MAX_REQUEST_SIZE_MB="500"
SERVICE_STREAMING="true"
SERVICE_REQUEST_TIMEOUT_MILLIS="300000"

MEMCACHED_HOST="memcached"
MEMCACHED_PORT="11211"
MEMCACHED_TEST_HOST="memcached"
MEMCACHED_TEST_PORT="11211"

DEFAULT_REQUEST_LATENCY_BUCKETS="0.001,0.005,0.010,0.020,0.050,0.100,0.200,0.300,0.500,0.750,1,5"

ZOOKEEPER_SERVERS=zookeeper:2181

AUTHENTICATOR_SRV_RECORD="dnssrv!http.api.prod.authenticator.dd.srv.int.s-cloud.net"
AUTHENTICATOR_JSONCLIENT_REQUEST_TIMEOUT_MILLIS=10000
AUTHSY_SRV_RECORD="dnssrv!http.api.prod.authsy.dd.srv.int.s-cloud.net"
FOLLOWS_SRV_RECORD="dnssrv!http.api.prod.follows.dd.srv.int.s-cloud.net"
GOBBLY_SRV_RECORD="dnssrv!http.api.prod.gobbly.dd.srv.int.s-cloud.net"
GATEKEEPER_SRV_RECORD="dnssrv!http.api.prod.gatekeeper.dd.srv.int.s-cloud.net"

GEOIP_SRV_RECORD="dnssrv!http.geoip2http.prod.geoip.dd.srv.int.s-cloud.net"
MEDIASERVICE_SRV_RECORD="dnssrv!http.urlgen.prod.media-service.dd.srv.int.s-cloud.net"

RATE_LIMIT_MEMCACHED_SERVERS="memcached"

MOSHIMOSHI_BASE_URL="http://moshimoshi.int.s-cloud.net"

MOTHERSHIP_API_SERVER="publicapistub:4567"
MOTHERSHIP_API_STREAMING_ENABLED="false"
MOTHERSHIP_API_REQUEST_TIMEOUT_MILLIS="300000"

OKIDOKI_SRV_RECORD="dnssrv!http.okidoki.prod.moshimoshi.dd.srv.int.s-cloud.net"

RATELIMITING_CIRCUITBREAKER_MAX_CONSECUTIVE_FAILURES="5"
RATELIMITING_CIRCUITBREAKER_RESET_TIMEOUT_IN_MILLIS="1000"
RATELIMITING_JSONCLIENT_REQUEST_TIMEOUT_MILLIS="2000"
RATELIMITING_JSONCLIENT_RETRIES="1"
RATELIMITING_MAX_PROMETHEUS_COUNTER_LABELS="100"
RATELIMITING_SRV_RECORD="dnssrv!http.api.prod.ratelimiting.dd.srv.int.s-cloud.net"

SLO_ERROR_RATE_THRESHOLD=0.0001
SLO_LATENCY_P99_THRESHOLD_SECONDS=0.500
SLO_AVAILABILITY_TARGET=0.99999

TIMELINE_JSONCLIENT_REQUEST_TIMEOUT_MILLIS="2000"

SEARCH_SRV_RECORD="dnssrv!http.publicapi.prod.search.dd.srv.int.s-cloud.net"
TIMELINE_SRV_RECORD="dnssrv!http.api.prod.timeline.dd.srv.int.s-cloud.net"

TRACK_COORDINATOR_SRV_RECORD="dnssrv!http.coordinator.prod.tracks.dd.srv.int.s-cloud.net"

LIEBLING_SRV_RECORD="dnssrv!http.web.prod.liebling.dd.srv.int.s-cloud.net"
SIMILAR_SOUNDS_SRV_RECORD="dnssrv!http.api.prod.similar-sounds.dd.srv.int.s-cloud.net"

USER_SUBSCRIPTIONS_SRV_RECORD="dnssrv!http.server.sandbox.subscriptions.dd.srv.int.s-cloud.net"

STITCH4FOLLOWS_SRV_RECORD="dnssrv!http.web-follows.prod.stitch4counts.dd.srv.int.s-cloud.net"
STITCH_BULK_FETCH_MAX_ENTRIES="7"
