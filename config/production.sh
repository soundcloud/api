APP_NAME="publicapistrangler"

FINAGLE_METRICS_ENABLED="true"
JAVA_OPTS="-XX:+UseCompressedOops -XX:+UseConcMarkSweepGC -XX:NewRatio=1 -Xmx1G -Xms1G -XX:ParallelGCThreads=2 -Dcom.twitter.jvm.numProcs=2"

APP_BASE_URL="https://api.soundcloud.com"

ADMIN_APP_TIMEOUT_MILLIS="30000"

ZIPKIN_HOST="zipkin.dev.s-cloud.net"
ZIPKIN_PORT="9410"
ZIPKIN_SAMPLE_RATE="0.001"

PUBLICAPISTRANGLER_MAX_CONNS="60"
PUBLICAPISTRANGLER_MAX_REQUEST_SIZE_MB="500"

MEMCACHED_PORT="11211"
MEMCACHED_TEST_PORT="11211"

DEFAULT_REQUEST_LATENCY_BUCKETS="0.001,0.005,0.010,0.020,0.050,0.100,0.200,0.300,0.500,0.750,1,5"

AUTHENTICATOR_SRV_RECORD="dnssrv!http.api.prod.authenticator.dd.srv.int.s-cloud.net"
AUTHSY_SRV_RECORD="dnssrv!http.api.prod.authsy.dd.srv.int.s-cloud.net"
FOLLOWS_SRV_RECORD="dnssrv!http.api.prod.follows.dd.srv.int.s-cloud.net"

GATEKEEPER_SRV_RECORD="dnssrv!http.api.prod.gatekeeper.dd.srv.int.s-cloud.net"

GEOIP_SRV_RECORD="dnssrv!http.geoip2http.prod.geoip.dd.srv.int.s-cloud.net"
MEDIASERVICE_SRV_RECORD="dnssrv!http.urlgen.prod.media-service.dd.srv.int.s-cloud.net"

RATE_LIMIT_MEMCACHED_SERVERS="$DOCKER_IP"

MOSHIMOSHI_BASE_URL="http://moshimoshi.int.s-cloud.net"
MOTHERSHIP_API_SERVER_SRV_RECORD="dnssrv!http.passenger.prod.public-api.db.srv.int.s-cloud.net"

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

SEARCH_SRV_RECORD="dnssrv!http.dispatcher.prod.search.dd.srv.int.s-cloud.net"
TIMELINE_SRV_RECORD="dnssrv!http.api.prod.timeline.dd.srv.int.s-cloud.net"

LIEBLING_SRV_RECORD="dnssrv!http.web.prod.liebling.dd.srv.int.s-cloud.net"
SIMILAR_SOUNDS_SRV_RECORD="dnssrv!http.api.prod.similar-sounds.dd.srv.int.s-cloud.net"

TRACK_COORDINATOR_SRV_RECORD="dnssrv!http.coordinator.prod.tracks.dd.srv.int.s-cloud.net"

USER_SUBSCRIPTIONS_SRV_RECORD="dnssrv!http.server.production.subscriptions.dd.srv.int.s-cloud.net"

RATELIMIT_MAX_CONN="999999"

MEMCACHED_HOST="ip-10-33-34-33.m03.ams5.s-cloud.net,ip-10-33-33-27.m02.ams5.s-cloud.net,ip-10-33-20-35.n05.ams5.s-cloud.net,ip-10-33-24-62.n10.ams5.s-cloud.net,ip-10-33-41-34.m11.ams5.s-cloud.net"
ZOOKEEPER_SERVERS="10.33.25.61:2181,10.33.18.53:2181,10.33.32.31:2181,10.33.33.54:2181,10.33.37.50:2181"

STITCH4COUNTS_SRV_RECORD="dnssrv!http.web.prod.stitch4counts.dd.srv.int.s-cloud.net"
STITCH4FOLLOWS_SRV_RECORD="dnssrv!http.web-follows.prod.stitch4counts.dd.srv.int.s-cloud.net"
STITCH_BULK_FETCH_MAX_ENTRIES="7"
