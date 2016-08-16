export APP_NAME="public-api-strangler"

JAVA_OPTS="-XX:MaxMetaspaceSize=2G -XX:+UseCompressedOops -XX:+UseConcMarkSweepGC -XX:NewRatio=1 -Xmx2G -Xms2G -XX:ParallelGCThreads=2 -Dcom.twitter.jvm.numProcs=10"

export APP_BASE_URL="https://api.soundcloud.com"

export ADMIN_APP_TIMEOUT_MILLIS="30000"

export ZIPKIN_HOST="zipkin.dev.s-cloud.net"
export ZIPKIN_PORT="9410"
export ZIPKIN_SAMPLE_RATE="0.001"

export SERVICE_MAX_CONNS="60"
export SERVICE_MAX_REQUEST_SIZE_MB="500"

export MEMCACHED_PORT="11211"
export MEMCACHED_TEST_PORT="11211"

export DEFAULT_REQUEST_LATENCY_BUCKETS="0.001,0.005,0.010,0.020,0.050,0.100,0.200,0.300,0.500,0.750,1,5"

export AUTHENTICATOR_SRV_RECORD="dnssrv!http.api.prod.authenticator.dd.srv.int.s-cloud.net"
export AUTHSY_SRV_RECORD="dnssrv!http.api.prod.authsy.dd.srv.int.s-cloud.net"
export FOLLOWS_SRV_RECORD="dnssrv!http.api.prod.follows.dd.srv.int.s-cloud.net"
export GOBBLY_SRV_RECORD="dnssrv!http.api.prod.gobbly.dd.srv.int.s-cloud.net"
export GATEKEEPER_SRV_RECORD="dnssrv!http.api.prod.gatekeeper.dd.srv.int.s-cloud.net"

export GEOIP_SRV_RECORD="dnssrv!http.geoip2http.prod.geoip.dd.srv.int.s-cloud.net"
export MEDIASERVICE_SRV_RECORD="dnssrv!http.urlgen.prod.media-service.dd.srv.int.s-cloud.net"

export RATE_LIMIT_MEMCACHED_SERVERS="$DOCKER_IP"

export MOSHIMOSHI_BASE_URL="http://moshimoshi.int.s-cloud.net"

export MOTHERSHIP_API_SERVER="dnssrv!http.passenger.prod.public-api.db.srv.int.s-cloud.net"
export MOTHERSHIP_API_STREAMING_ENABLED="false"
export MOTHERSHIP_API_REQUEST_TIMEOUT_MILLIS="30000"

export OKIDOKI_SRV_RECORD="dnssrv!http.okidoki.prod.moshimoshi.dd.srv.int.s-cloud.net"

export RATELIMITING_CIRCUITBREAKER_MAX_CONSECUTIVE_FAILURES="5"
export RATELIMITING_CIRCUITBREAKER_RESET_TIMEOUT_IN_MILLIS="1000"
export RATELIMITING_JSONCLIENT_REQUEST_TIMEOUT_MILLIS="2000"
export RATELIMITING_JSONCLIENT_RETRIES="1"
export RATELIMITING_MAX_PROMETHEUS_COUNTER_LABELS="100"
export RATELIMITING_SRV_RECORD="dnssrv!http.api.prod.ratelimiting.dd.srv.int.s-cloud.net"

export SLO_ERROR_RATE_THRESHOLD=0.0001
export SLO_LATENCY_P99_THRESHOLD_SECONDS=0.500
export SLO_AVAILABILITY_TARGET=0.99999

export TIMELINE_JSONCLIENT_REQUEST_TIMEOUT_MILLIS="2000"

export SEARCH_SRV_RECORD="dnssrv!http.publicapi.prod.search.dd.srv.int.s-cloud.net"
export TIMELINE_SRV_RECORD="dnssrv!http.api.prod.timeline.dd.srv.int.s-cloud.net"

export LIEBLING_SRV_RECORD="dnssrv!http.web.prod.liebling.dd.srv.int.s-cloud.net"
export SIMILAR_SOUNDS_SRV_RECORD="dnssrv!http.api.prod.similar-sounds.dd.srv.int.s-cloud.net"

export TRACK_COORDINATOR_SRV_RECORD="dnssrv!http.coordinator.prod.tracks.dd.srv.int.s-cloud.net"

export USER_SUBSCRIPTIONS_SRV_RECORD="dnssrv!http.server.production.subscriptions.dd.srv.int.s-cloud.net"

export RATELIMIT_MAX_CONN="999999"

export MEMCACHED_HOST="ip-10-33-34-33.m03.ams5.s-cloud.net,ip-10-33-20-35.n05.ams5.s-cloud.net,ip-10-33-24-62.n10.ams5.s-cloud.net,ip-10-33-41-34.m11.ams5.s-cloud.net"
export ZOOKEEPER_SERVERS="10.33.25.61:2181,10.33.18.53:2181,10.33.32.31:2181,10.33.33.54:2181,10.33.37.50:2181"

export STITCH4FOLLOWS_SRV_RECORD="dnssrv!http.web-follows.prod.stitch4counts.dd.srv.int.s-cloud.net"
export STITCH_BULK_FETCH_MAX_ENTRIES="7"

export OKIDOKI_CLIENT_FAILURE_ACCRUAL_REQ_SUCCESS_RATE="0.95"
export OKIDOKI_CLIENT_FAILURE_ACCRUAL_WINDOW="1000"
export OKIDOKI_CLIENT_FAILURE_ACCRUAL_MARK_DEAD_FOR_MILLIS="200"

export AUTHENTICATOR_CLIENT_FAILURE_ACCRUAL_REQ_SUCCESS_RATE="0.99"
export AUTHENTICATOR_CLIENT_FAILURE_ACCRUAL_WINDOW="1000"
export AUTHENTICATOR_CLIENT_FAILURE_ACCRUAL_MARK_DEAD_FOR_MILLIS="100"

export TRACKMETADATA_SRV_RECORD="dnssrv!http.api.prod.trackmetadata.dd.srv.int.s-cloud.net"

export APP_SILOING_BLACKLIST_APPS="soundcloud:applications:124,soundcloud:applications:3152,soundcloud:applications:65097"
