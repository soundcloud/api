module github.com/soundcoud/api-public/asset-uploads

go 1.23.0

toolchain go1.24.4

require (
	github.com/aws/aws-sdk-go v1.55.7
	github.com/google/uuid v1.6.0
	github.com/gorilla/mux v1.8.1 // TODO: the project was archived
	github.com/prometheus/client_golang v1.22.0
	github.com/soundcloud/gokit v1.27.0
	github.com/streadway/handy v0.0.0-20200128134331-0f66f006fb2e
)

require (
	github.com/beorn7/perks v1.0.1 // indirect
	github.com/cespare/xxhash/v2 v2.3.0 // indirect
	github.com/felixge/httpsnoop v1.0.4 // indirect
	github.com/go-logr/logr v1.4.2 // indirect
	github.com/go-logr/stdr v1.2.2 // indirect
	github.com/golang/protobuf v1.5.4 // indirect
	github.com/jmespath/go-jmespath v0.4.0 // indirect
	github.com/kelseyhightower/envconfig v1.4.0 // indirect
	github.com/mattn/go-colorable v0.1.13 // indirect
	github.com/mattn/go-isatty v0.0.20 // indirect
	github.com/munnerz/goautoneg v0.0.0-20191010083416-a7dc8b61c822 // indirect
	github.com/newrelic/go-agent/v3 v3.33.1 // indirect
	github.com/newrelic/go-agent/v3/integrations/logcontext-v2/nrwriter v1.0.0 // indirect
	github.com/newrelic/go-agent/v3/integrations/logcontext-v2/zerologWriter v1.0.2 // indirect
	github.com/pkg/errors v0.9.1 // indirect
	github.com/prometheus/client_model v0.6.2 // indirect
	github.com/prometheus/common v0.65.0 // indirect
	github.com/prometheus/procfs v0.17.0 // indirect
	github.com/rs/xid v1.5.0 // indirect
	github.com/rs/zerolog v1.33.0 // indirect
	go.opentelemetry.io/contrib/instrumentation/net/http/otelhttp v0.49.0 // indirect
	go.opentelemetry.io/otel v1.30.0 // indirect
	go.opentelemetry.io/otel/metric v1.30.0 // indirect
	go.opentelemetry.io/otel/trace v1.30.0 // indirect
	golang.org/x/net v0.40.0 // indirect
	golang.org/x/sync v0.16.0 // indirect
	golang.org/x/sys v0.34.0 // indirect
	golang.org/x/text v0.25.0 // indirect
	google.golang.org/genproto/googleapis/rpc v0.0.0-20240903143218-8af14fe29dc1 // indirect
	google.golang.org/grpc v1.66.1 // indirect
	google.golang.org/protobuf v1.36.6 // indirect
)

replace (
	golang.org/x/text v0.3.0 => golang.org/x/text v0.3.8
	golang.org/x/text v0.3.1 => golang.org/x/text v0.3.8
	golang.org/x/text v0.3.2 => golang.org/x/text v0.3.8
	golang.org/x/text v0.3.3 => golang.org/x/text v0.3.8
	golang.org/x/text v0.3.4 => golang.org/x/text v0.3.8
	golang.org/x/text v0.3.5 => golang.org/x/text v0.3.8
	golang.org/x/text v0.3.6 => golang.org/x/text v0.3.8
	golang.org/x/text v0.3.7 => golang.org/x/text v0.3.8
)
