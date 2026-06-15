# OpenAPI specification

Single-file [OpenAPI 3](https://swagger.io/specification/) spec for the SoundCloud public API (`https://api.soundcloud.com`).

Updated on each [release](https://github.com/soundcloud/api/releases) when the API changes.

## Download

Latest from `master`:

```bash
curl -fsSL https://raw.githubusercontent.com/soundcloud/api/master/openapi/api.yaml -o soundcloud-api.yaml
```

Or pin to a [release tag](https://github.com/soundcloud/api/releases):

```bash
curl -fsSL https://raw.githubusercontent.com/soundcloud/api/YYYY-MM-DD/openapi/api.yaml -o soundcloud-api.yaml
```

## Generate a client

Use [OpenAPI Generator](https://openapi-generator.tech/) or any OpenAPI-aware tool with the downloaded spec.

Interactive reference: [developers.soundcloud.com/docs/api/reference](https://developers.soundcloud.com/docs/api/reference)
