DOCKER_IP=${DOCKER_IP:-$(docker-ip)}

APP_NAME="publicapistrangler"

APP_BASE_URL="https://api.soundcloud.com"

ENV="development"

JAVA_OPTS="-Xmx1G -Xms1G"
PORT="5000"

TELE_PORT="5001"
ZIPKIN_HOST="zipkin.dev.s-cloud.net"
ZIPKIN_PORT="9410"
ZIPKIN_SAMPLE_RATE="0.001"

PUBLICAPISTRANGLER_MAX_CONNS="60"

MEMCACHED_HOST="$DOCKER_IP"
MEMCACHED_PORT="11211"
MEMCACHED_TEST_HOST="$DOCKER_IP"
MEMCACHED_TEST_PORT="11211"

DEFAULT_REQUEST_LATENCY_BUCKETS="0.001,0.005,0.010,0.020,0.050,0.100,0.200,0.300,0.500,0.750,1,5"

ZOOKEEPER_SERVERS="${DOCKER_IP}:2181"

AUTHENTICATOR_SRV_RECORD="expdnssrv!http.api.prod.authenticator.dd.srv.int.s-cloud.net"
AUTHSY_SRV_RECORD="expdnssrv!http.api.prod.authsy.dd.srv.int.s-cloud.net"
FOLLOWS_SRV_RECORD="expdnssrv!http.api.prod.follows.dd.srv.int.s-cloud.net"

GATEKEEPER_SRV_RECORD="expdnssrv!http.api.prod.gatekeeper.dd.srv.int.s-cloud.net"

GEOIP_SRV_RECORD="expdnssrv!http.geoip2http.prod.geoip.dd.srv.int.s-cloud.net"
MEDIASERVICE_SRV_RECORD="expdnssrv!http.urlgen.prod.media-service.dd.srv.int.s-cloud.net"

RATE_LIMIT_MEMCACHED_SERVERS="$DOCKER_IP"

MOSHIMOSHI_BASE_URL="http://moshimoshi.int.s-cloud.net"
MOTHERSHIP_API_SERVER_SRV_RECORD="expdnssrv!http.haproxy.prod.public-api.db.srv.int.s-cloud.net"

OKIDOKI_SRV_RECORD="expdnssrv!http.okidoki.prod.moshimoshi.dd.srv.int.s-cloud.net"

RATELIMITING_CIRCUITBREAKER_MAX_CONSECUTIVE_FAILURES="5"
RATELIMITING_CIRCUITBREAKER_RESET_TIMEOUT_IN_MILLIS="1000"
RATELIMITING_JSONCLIENT_REQUEST_TIMEOUT_MILLIS="2000"
RATELIMITING_JSONCLIENT_RETRIES="1"
RATELIMITING_MAX_PROMETHEUS_COUNTER_LABELS="100"
RATELIMITING_SRV_RECORD="expdnssrv!http.api.prod.ratelimiting.dd.srv.int.s-cloud.net"

SEARCH_SRV_RECORD="expdnssrv!http.dispatcher.prod.search.dd.srv.int.s-cloud.net"
TIMELINE_SRV_RECORD="expdnssrv!http.api.prod.timeline.dd.srv.int.s-cloud.net"

TRACK_COORDINATOR_SRV_RECORD="expdnssrv!http.coordinator.prod.tracks.dd.srv.int.s-cloud.net"

LIEBLING_SRV_RECORD="expdnssrv!http.web.prod.liebling.dd.srv.int.s-cloud.net"
SIMILAR_SOUNDS_SRV_RECORD="expdnssrv!http.api.prod.similar-sounds.dd.srv.int.s-cloud.net"
