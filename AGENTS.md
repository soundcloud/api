# Agent guidelines for API Public

This file gives AI agents and contributors consistent instructions for working on the [SoundCloud Public API](https://api-public-doc.soundcloud.org/) codebase.

## Before you're done (checklist)

For any change that touches API behavior, handlers, or docs, ensure:

- **Format**: Run `make format` (runs `scalafmtAll`).
- **Tests**: Add or update unit tests as needed; run `make unit-test` (or `make precheckin` for the full suite).
- **API docs**: If you add or change endpoints or request/response shapes, update the **Swagger/OpenAPI** spec in `doc/` (see [Swagger / OpenAPI](#swagger--openapi)).
- **Release notes**: If the change is user-facing or noteworthy, add an entry to [RELEASE_NOTES.md](RELEASE_NOTES.md). See [README Notes – Release notes](README.md#release-notes).

## Testing

- - Run unit tests: `make test` or `make unit-test`. Full precheck (lint + unit + package + e2e): `make precheckin`.
- Set `USE_CRUN=false` to run SBT directly when possible.
- **Do not mock case classes.** Use **Fixtures** for test data.
- Fixtures live in `src/test/scala/com/soundcloud/apipublic/test/fixtures/Fixtures.scala` and load JSON from `src/test/resources/...` (e.g. `Fixtures.contentsOf("prefix", "name")`, or existing lazy vals like `Fixtures.trackCoordinatorTrack`).

## Swagger / OpenAPI

- API spec is **OpenAPI 3** under **`doc/`**:
  - Main entry: `doc/api.yaml`
  - Components: `doc/components/` (e.g. `components/schemas/`, `components/responses.yaml`).
- Contract tests use the compiled spec: `doc/make resolve_api` produces `doc/compiled_api.yaml` (used by Dredd).
- When you add or change endpoints or schemas, update the relevant YAML under `doc/` so the published [Swagger UI](https://api-public-doc.soundcloud.org/) and contract tests stay in sync.

## Project context (from README / CONTRIBUTING)

- **Service**: SoundCloud Public API. Development and contribution details: [CONTRIBUTING.md](CONTRIBUTING.md).
- **Architecture**: BFF-style; see [BFF Documentation](https://eng-doc.soundcloud.org/guidelines/bff) when changing business logic.
- **Useful make targets**: `make precheckin`, `make test`, `make format`, `make run`, `make interactive` (SBT console).
- **Release notes**: User-facing or notable changes should be described in [RELEASE_NOTES.md](RELEASE_NOTES.md); markdown is preserved. See existing [releases](https://github.com/soundcloud/api/releases) for style.
- **PRs**: Prefer changes under ~300 lines
