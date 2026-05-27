# Cursor Agent Skills

Agent skills for integrating with the [SoundCloud API](https://developers.soundcloud.com/docs/api/guide). Copy this `.cursor/skills/` directory into your application repo, or clone this repository and point Cursor at these skills.

## Skills

| Skill | Use when |
|-------|----------|
| [soundcloud-api-integration](soundcloud-api-integration/SKILL.md) | Starting any SoundCloud API integration — auth rules, URNs, OpenAPI, errors |
| [soundcloud-api-auth](soundcloud-api-auth/SKILL.md) | OAuth 2.1 PKCE, Client Credentials, token refresh |
| [soundcloud-api-discovery](soundcloud-api-discovery/SKILL.md) | Search, related tracks, related artists |

## Related documentation

- [Agents.md](../Agents.md) — agent integration overview for this repo
- [Building with AI](https://developers.soundcloud.com/docs/building-with-ai) — prompt templates and examples
- [LLM context](https://developers.soundcloud.com/docs/llm-context) — quick reference for agents
- [API releases](https://github.com/soundcloud/api/releases) — changelog

## Usage in Cursor

Project skills live at `.cursor/skills/<skill-name>/SKILL.md`. Install by copying the skill folders into your project, or reference this repo when configuring workspace rules.

Invoke explicitly (e.g. “use the soundcloud-api-discovery skill”) or add skill paths to your Cursor rules.
