# Security Policy

## Reporting a Vulnerability

SoundCloud is committed to the security of our platform and our users. If you believe you have found a security vulnerability in the SoundCloud API, **please do not report it through public GitHub issues.**

Instead, please report all security vulnerabilities through our official Responsible Disclosure program:

* **Reporting Portal:** [SoundCloud on Bugcrowd](https://bugcrowd.com/soundcloud)

Using this portal ensures that your report is handled privately and securely by our security team and remains eligible for our bug bounty program.

## Security Best Practices for Developers

When building applications with the SoundCloud API, please follow the security standards outlined in the [Official API Guide](https://developers.soundcloud.com/docs/api/guide):

1. **Protect Your Credentials:** Never hardcode your `client_id` or `client_secret` in client-side code or commit them to public repositories. Use environment variables for all secrets.
2. **Secure Authentication:** * Use **PKCE** (Proof Key for Code Exchange) for securely exchanging authorization codes.
   * Always use the `state` parameter in OAuth flows to prevent Cross-Site Request Forgery (CSRF).
3. **Token Management:** Treat access tokens as sensitive data. Use the `refresh_token` flow to renew access rather than storing long-lived credentials.

## Official Resources & Updates

To stay informed about security updates or changes to the API, please monitor the channels listed in our README:

* **API Announcements:** [@SoundCloudDev on X](https://x.com/soundclouddev) or [Bluesky](https://bsky.app/profile/soundclouddev.bsky.social)
* **Technical Blog:** [SoundCloud Backstage Blog](https://developers.soundcloud.com/blog/)
* **Support:** For non-security related issues, visit the [SoundCloud Help Center](https://help.soundcloud.com).
