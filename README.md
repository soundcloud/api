# SoundCloud API Issue Tracker
This repository is a means for our current API users to engage with SoundCloud on
*  reporting bugs and feature requests in order to improve the API. 
Please use the [issue tracker] in this repository. 

* following the latest changes to the API. 
Please check the [releases] page.

### Quick links
* [API documentation] 
* [Developer portal]
* [Backstage blog]
* [API announcements]

**NOTE: This is not a forum for requesting new API keys. While we hope to explore opening up access again, we're currently not issuing new keys to 3rd party apps. Please subscribe to our [@SoundCloudDev] on Twitter for updates**

## Opening an issue
Please use the [issue tracker] in this repository for bug reports, feature requests, suggestions, and documentation improvements. We will triage the request and mark it as one of the following types:

- <kbd>[bug]</kbd> — Unexpected behavior relating to the API that can be reproduced.
- <kbd>[suggestion]</kbd> — Requests for additional functionality or changes in the API that has been acknowledged by SoundCloud.
- <kbd>[documentation]</kbd> — Requests for changes to documentation that has been acknowledged by SoundCloud.
- <kbd>[question]</kbd> — Relates to the API, but does not fit into the above categories.

In order to show your support for issues filed here, please add a thumbs-up emoji 👍🏽. 

Please note - if there is an issue filed for something that is answered already in the FAQ, we will close the issue in the spirit of having the open issues only for unanswered questions of topics. 

## Security Issues
Please don't post security issues or vulnerabilities here. Instead, submit them to our public [bug-bounty program] to get triaged and receive bounty payouts.

## Frequently asked questions
### Can I request an API key for a new app?
We're actively exploring on how best to open up public access to our API again. While we're currently not issuing new API keys to 3rd party apps, we're helping improve the systems and tools in place to better serve the already active integrations. This is helping us to better understand what resources we need to put in place in order to support this product at scale. We understand this may be frustrating to new developers, but we hope to have your support and patience during this phase. Please subscribe to our [@SoundCloudDev] on Twitter or our [Backstage Blog] for API Announcements

### How can I update my app's `redirect_uri`?
Our Support Team will be happy to help you with your URI's redirects. Please fill out a ticket [here] under "I need help with something else" and they will get back to you as soon as they can.

### My app disappeared from `https://soundcloud.com/you/apps`, what should I do?
If you registered an app with Soundcloud and now cannot find it, it's possible your API keys were revoked as unused for some time.
Our Support Team will be happy to help you with reinstating the keys. Please fill out a ticket [here] under "I need help with something else" and they will get back to you as soon as they can.

### I have a questions about the SDKs. Can I post them here?
Yes, you may. However, please note that we're not maintaining the Ruby, Python or JS SDK anymore and we're going to be announcing soon that we're going to open it for maintainers from the community if there's interest. Our recommendation is to use the API directly and build your own client. We will also be exposing the underlying yaml file for the OpenAPI spec soon, so that folks can generate clients from this. 

### I am an official partner of SoundCloud. Can I use this issue tracker for a technical question?
This repository only deals with the public-facing API. Any partnership-specific questions don’t belong here as this is a public forum; please contact your SoundCloud partnership contact.

## Code of Conduct
Our request to the community - we're a rather small team. You'd be surprised how small we are. We want to help the community but we need to be brutally lean on what we can accomplish. Our mission is to enable a thriving community of innovative projects powered by the SoundCloud platform. In that spirit, we're requesting folks to follow the following guidelines when interacting with our team. 

Examples of behavior that contributes to creating a positive environment include:
- Using welcoming and inclusive language
- Being respectful of differing viewpoints and experiences
- Focusing on what is best for the community
- Showing empathy towards other community members

Examples of unacceptable behavior by participants include:
- The use of sexualized language or imagery and unwelcome sexual attention or advances
- Trolling, insulting/derogatory comments, and personal or political attacks
- Public or private harassment
- Publishing others' private information, such as a physical or electronic address, without explicit permission
- Other conduct which could reasonably be considered inappropriate in a professional setting

## Stay in touch
For all SoundCloud developer-related announcements, please subscribe to [@SoundCloudDev], our [Backstage blog], our [API announcements], or [releases] page.

[@SoundCloudDev]: https://twitter.com/soundclouddev
[Backstage Blog]: https://developers.soundcloud.com/blog/
[API documentation]: https://developers.soundcloud.com/docs/api/reference
[Developer portal]: https://developers.soundcloud.com/
[issue tracker]: https://www.github.com/soundcloud/api/issues
[Javascript SDK]: https://github.com/soundcloud/soundcloud-javascript
[API announcements]: https://developers.soundcloud.com/blog/category/api
[releases]: https://github.com/soundcloud/api/releases 

[bug]: https://github.com/soundcloud/api/labels/bug
[suggestion]: https://github.com/soundcloud/api/labels/suggestion
[documentation]: https://github.com/soundcloud/api/labels/documentation
[question]: https://github.com/soundcloud/api/labels/question

[wontfix]:  https://github.com/soundcloud/api/labels/wontfix
[offtopic]:  https://github.com/soundcloud/api/labels/offtopic
[duplicate]:  https://github.com/soundcloud/api/labels/duplicate
[more info needed]: https://github.com/soundcloud/api/labels/more%20info%20needed
[inactive]:  https://github.com/soundcloud/api/labels/inactive

[bug-bounty program]: https://bugcrowd.com/soundcloud
[here]:https://help.soundcloud.com/hc/en-us/requests/new?ticket_form_id=581048
<html lang="es">
<head>
<meta charset="utf-8">
<meta name="theme-color" content="#333">
<link rel="dns-prefetch" href="//style.sndcdn.com">
<link rel="dns-prefetch" href="//a-v2.sndcdn.com">
<link rel="dns-prefetch" href="//api-v2.soundcloud.com">
<link rel="dns-prefetch" href="//sb.scorecardresearch.com">
<content:encoded><![CDATA[<p>Users with two-factor authentication enabled can now begin the account recovery process from the password reset flow. Previously, the account password was needed to access 2FA account recovery, but passwords on 2FA-enabled accounts could only be reset with a valid second factor. If you lost your password and all of your second factors, you were locked out because you could not access account recovery. With this change, a user can recover their account as long as they can perform email verification and provide a recovery factor, such as an SSH key, PAT, or previously signed in device.</p>
n<?xml version="1.0" encoding="UTF-8"?><rss version="2.0"
	xmlns:content="http://purl.org/rss/1.0/modules/content/"
	xmlns:wfw="http://wellformedweb.org/CommentAPI/"
	xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns:atom="http://www.w3.org/2005/Atom"
	xmlns:sy="http://purl.org/rss/1.0/modules/syndication/"
	xmlns:slash="http://purl.org/rss/1.0/modules/slash/"
	
	xmlns:georss="http://www.georss.org/georss"
	xmlns:geo="http://www.w3.org/2003/01/geo/wgs84_pos#"
	>

<channel>
	<title>authentication Archives - The GitHub Blog</title>
	<atom:link href="https://github.blog/changelog/label/authentication/feed/" rel="self" type="application/rss+xml" />
	<link>https://github.blog/changelog/label/authentication/</link>
	<description>Updates, ideas, and inspiration from GitHub to help developers build and design software.</description>
	<lastBuildDate>Fri, 05 Jan 2024 19:59:21 +0000</lastBuildDate>
	<language>en-US</language>
	<sy:updatePeriod>
	hourly	</sy:updatePeriod>
	<sy:updateFrequency>
	1	</sy:updateFrequency>
	<generator>https://wordpress.org/?v=6.4.3</generator>

<image>
	<url>https://github.blog/wp-content/uploads/2019/01/cropped-github-favicon-512.png?fit=32%2C32</url>
	<title>authentication Archives - The GitHub Blog</title>
	<link>https://github.blog/changelog/label/authentication/</link>
	<width>32</width>
	<height>32</height>
</image> 
<site xmlns="com-wordpress:feed-additions:1">153214340</site>	<item>
		<title>You can pick your account during device flow login</title>
		<link>https://github.blog/changelog/2024-01-05-you-can-pick-your-account-during-device-flow-login</link>
		
		<dc:creator><![CDATA[Kevin Duck]]></dc:creator>
		<pubDate>Fri, 05 Jan 2024 19:59:21 +0000</pubDate>
				<guid isPermaLink="false">https://github.blog/changelog/2024-01-05-you-can-pick-your-account-during-device-flow-login</guid>

					<description><![CDATA[<p>You can pick your account during device flow login</p>
<p>The post <a href="https://github.blog/changelog/2024-01-05-you-can-pick-your-account-during-device-flow-login">You can pick your account during device flow login</a> appeared first on <a href="https://github.blog">The GitHub Blog</a>.</p>
]]></description>
										<content:encoded><![CDATA[<p>If you are signed into multiple accounts on GitHub.com, you&#039;ll be able to pick between them when you sign in using the <a href="https://docs.github.com/en/apps/oauth-apps/building-oauth-apps/authorizing-oauth-apps#device-flow">device flow</a>. This authentication method is typically used for console applications, like the GitHub CLI and remote use of VS Code.</p>
<p><img decoding="async" src="https://github.com/github/release-assets/assets/1666363/e4db7ad6-b0f1-48c0-98ed-95c50f3e8778" alt="An account picker showing 3 accounts - one signed in with a green &quot;Continue&quot; button, another signed in with a &quot;Select&quot; option, and a third that&#039;s signed out, with a &quot;Sign in&quot; button. Above the picker it reads &quot;Device Activation&quot;"></p>
<p>For more information about using multiple accounts, see <a href="https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/switching-between-accounts">&quot;Switching between accounts&quot;</a>.</p>
<p>The post <a href="https://github.blog/changelog/2024-01-05-you-can-pick-your-account-during-device-flow-login">You can pick your account during device flow login</a> appeared first on <a href="https://github.blog">The GitHub Blog</a>.</p>
]]>

</content:encoded>
		
		<post-id xmlns="com-wordpress:feed-additions:1">76009</post-id>
	</item>
		<item>
	<title>Log in to multiple GitHub accounts with the CLI</title>
		<link>https://github.blog/changelog/2023-12-18-log-in-to-multiple-github-accounts-with-the-cli</link>
		
		<dc:creator><![CDATA[Kevin Duck]]></dc:creator>
		<pubDate>Mon, 18 Dec 2023 15:35:34 +0000</pubDate>
				<guid isPermaLink="false">https://github.blog/changelog/2023-12-18-log-in-to-multiple-github-accounts-with-the-cli</guid>

					<description><![CDATA[<p>Log in to multiple GitHub accounts with the CLI</p>
<p>The post <a href="https://github.blog/changelog/2023-12-18-log-in-to-multiple-github-accounts-with-the-cli">Log in to multiple GitHub accounts with the CLI</a> appeared first on <a href="https://github.blog">The GitHub Blog</a>.</p>
]]></description>
										<content:encoded>
<![CDATA[
<p>Introducing support for multiple GitHub accounts on a single host within the CLI! Log in with your work and personal accounts to manage your projects, wherever they&#039;re happening.</p>
<p>To add multiple accounts in the CLI, use the <code>gh auth login</code> command just as before. Now, instead of replacing your previous account, you will see the addition of a new account under <code>gh auth status</code>. This account will be marked as <code>active</code>, to indicate that <code>gh</code> will use it when communicating with GitHub. Run <code>gh auth switch</code> to change the active account, or <code>gh auth logout</code> to remove an account. Further details can be found in the <a href="https://github.com/cli/cli/releases/tag/v2.40.0"><code>v2.40.0</code> release notes</a>.</p>
<p>Install or update the GitHub CLI today from your <a href="https://github.com/cli/cli?tab=readme-ov-file#installation">preferred source</a>.</p>
<p>The post <a href="https://github.blog/changelog/2023-12-18-log-in-to-multiple-github-accounts-with-the-cli">Log in to multiple GitHub accounts with the CLI</a> appeared first on <a href="https://github.blog">The GitHub Blog</a>.</p>
]]></content:encoded>
					
		
		
		<post-id xmlns="com-wordpress:feed-additions:1">75848</post-id>	</item>
		<item>
		<title>[Public Beta] Guest collaborators for enterprise managed users</title>
		<link>https://github.blog/changelog/2023-11-08-public-beta-guest-collaborators-for-enterprise-managed-users</link>
		
		<dc:creator><![CDATA[Kevin Duck]]></dc:creator>
		<pubDate>Wed, 08 Nov 2023 18:21:30 +0000</pubDate>
				<guid isPermaLink="false">https://github.blog/changelog/2023-11-08-public-beta-guest-collaborators-for-enterprise-managed-users</guid>

					<description><![CDATA[<p>[Public Beta] Guest collaborators for enterprise managed users</p>
<p>The post <a href="https://github.blog/changelog/2023-11-08-public-beta-guest-collaborators-for-enterprise-managed-users">[Public Beta] Guest collaborators for enterprise managed users</a> appeared first on <a href="https://github.blog">The GitHub Blog</a>.</p>
]]></description>
										<content:encoded><![CDATA[<p>GitHub Enterprise Cloud customers that use Enterprise Managed Users (EMUs) can now participate in a public beta for a new user role that has restricted visibility of internal repositories. The <code>guest collaborator</code> role is defined via SCIM and assigned to users by the identity provider. Guest collaborators helps companies who work with contractors and other short-term partners in a flexible and managed fashion on specific projects, while also sharing code and ideas without restrictions amongst full enterprise members. When a guest collaborator is added to an organization they will only receive access to <code>internal</code> visibility repositories within that organization.</p>
<p><img decoding="async" src="//github.com/github/release-assets/assets/1852630/b24ce0be-3abe-4cc2-b0d9-ac6a49e04cc1" alt="add a guest collaborator"></p>
<p>Learn more about <a href="https://docs.github.com/enterprise-cloud@latest/admin/managing-accounts-and-repositories/managing-users-in-your-enterprise/roles-in-an-enterprise#guest-collaborators">guest collaborators</a>.</p>
<p>The post <a href="https://github.blog/changelog/2023-11-08-public-beta-guest-collaborators-for-enterprise-managed-users">[Public Beta] Guest collaborators for enterprise managed users</a> appeared first on <a href="https://github.blog">The GitHub Blog</a>.</p>
]]></content:encoded>
					
		
		
		<post-id xmlns="com-wordpress:feed-additions:1">75396</post-id>	</item>
		<item>
		<title>Multi-account support on GitHub.com</title>
		<link>https://github.blog/changelog/2023-11-03-multi-account-support-on-github-com</link>
		
		<dc:creator><![CDATA[Kevin Duck]]></dc:creator>
		<pubDate>Fri, 03 Nov 2023 15:35:26 +0000</pubDate>
				<guid isPermaLink="false">https://github.blog/changelog/2023-11-03-multi-account-support-on-github-com</guid>

					<description><![CDATA[<p>Multi-account support on GitHub.com</p>
<p>The post <a href="https://github.blog/changelog/2023-11-03-multi-account-support-on-github-com">Multi-account support on GitHub.com</a> appeared first on <a href="https://github.blog">The GitHub Blog</a>.</p>
]]></description>
										<content:encoded><![CDATA[<p>GitHub.com now remembers multiple accounts in your browser. You can find the account switcher in your profile picture context menu, letting you more easily switch between user accounts without re-entering your credentials.</p>
<p><img decoding="async" src="https://github.com/github/release-assets/assets/1666363/bd646682-187d-42e5-96c2-1ddfdc76f240" alt="image"></p>
<p>The account switcher helps developers alternate between Enterprise Managed User accounts provided by an employer and personal accounts for use with personal projects and open source contributions. It also helps administrators manage service accounts they use for automation and integration purposes.</p>
<p>Because these accounts often have significantly different privileges, there&#039;s never any mixing of user permissions between saved accounts. When you visit a page that your current account can&#039;t access, you&#039;ll see a prompt to switch accounts if you have more than one signed in.</p>
<p>When you switch accounts, you won&#039;t need to sign in again or perform 2FA unless the account session has expired. Session expiration occurs after two weeks without activity. SAML/OIDC SSO authorization is also saved for sessions, but often expires every 1 or 24 hours, and may need to be done again before you can access your organization resources.</p>
<p>To learn more, see &quot;<a href="https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/switching-between-accounts">Switching between accounts</a>&quot;.</p>
<p>The post <a href="https://github.blog/changelog/2023-11-03-multi-account-support-on-github-com">Multi-account support on GitHub.com</a> appeared first on <a href="https://github.blog">The GitHub Blog</a>.</p>
]]></content:encoded>
					
		
		
		<post-id xmlns="com-wordpress:feed-additions:1">75273</post-id>	</item>
		<item>
		<title>Enabling reactive 2FA requirements for release creation</title>
		<link>https://github.blog/changelog/2023-10-31-enabling-reactive-2fa-requirements-for-release-creation</link>
		
		<dc:creator><![CDATA[Kevin Duck]]></dc:creator>
		<pubDate>Tue, 31 Oct 2023 19:54:40 +0000</pubDate>
				<guid isPermaLink="false">https://github.blog/changelog/2023-10-31-enabling-reactive-2fa-requirements-for-release-creation</guid>

					<description><![CDATA[<p>Enabling reactive 2FA requirements for release creation</p>
<p>The post <a href="https://github.blog/changelog/2023-10-31-enabling-reactive-2fa-requirements-for-release-creation">Enabling reactive 2FA requirements for release creation</a> appeared first on <a href="https://github.blog">The GitHub Blog</a>.</p>
]]></description>
										<content:encoded><![CDATA[<p>Users who are not part of the <a href="https://github.blog/2023-03-09-raising-the-bar-for-software-security-github-2fa-begins-march-13/">mandatory 2FA program</a> will now be added to it within 24 hours of creating their first <a href="https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-a-repository">release</a>. In August we expanded the 2FA requirement to include most GitHub.com users that had created a release. Those groups have now completed their 2FA enrollment, but additional developers have since created their first release. They will be added to the 2FA program in the coming days, as will more users over time as they create releases.  </p>
<p>Enterprise or organization administrators can learn more about their users&#039; current 2FA requirements by visiting the <a href="https://github.blog/changelog/2023-07-31-2fa-requirement-status-on-the-people-pages/">People page</a> for their enterprise or organization.</p>
<p>To learn more about the 2FA program, see our <a href="https://github.blog/2023-03-09-raising-the-bar-for-software-security-github-2fa-begins-march-13/">May 2023 blog post</a>, as well as the <a href="https://docs.github.com/en/authentication/securing-your-account-with-two-factor-authentication-2fa/about-mandatory-two-factor-authentication">&#8220;About the mandatory 2FA program&#8221; documentation</a>.</p>
<p>The post <a href="https://github.blog/changelog/2023-10-31-enabling-reactive-2fa-requirements-for-release-creation">Enabling reactive 2FA requirements for release creation</a> appeared first on <a href="https://github.blog">The GitHub Blog</a>.</p>
]]></content:encoded>
					
		
		
		<post-id xmlns="com-wordpress:feed-additions:1">75179</post-id>	</item>
		<item>
		<title>Changes to token permission on packages</title>
		<link>https://github.blog/changelog/2023-09-26-changes-to-token-permission-on-packages</link>
		
		<dc:creator><![CDATA[Kevin Duck]]></dc:creator>
		<pubDate>Tue, 26 Sep 2023 20:45:12 +0000</pubDate>
				<guid isPermaLink="false">https://github.blog/changelog/2023-09-26-changes-to-token-permission-on-packages</guid>

					<description><![CDATA[<p>Changes to token permission on packages</p>
<p>The post <a href="https://github.blog/changelog/2023-09-26-changes-to-token-permission-on-packages">Changes to token permission on packages</a> appeared first on <a href="https://github.blog">The GitHub Blog</a>.</p>
]]></description>
										<content:encoded><![CDATA[<p>Announcing changes to permissions for packages.</p>
<p>We are restricting the <a href="https://docs.github.com/rest/git/refs#create-a-reference"><code>refs</code> REST API endpoint</a> from accepting POSTs from users and apps that only have the <a href="https://docs.github.com/packages/learn-github-packages/configuring-a-packages-access-control-and-visibility">permission to read and write packages.</a> Previously, this endpoint accepted updates to both <code>tags</code> and <code>branches.</code></p>
<p>If that ability is critical to your development flows you will now be required to add explicit contents permissions to create refs.</p>
<ul>
<li><a href="https://docs.github.com/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens">Users will need to add the <code>public_repo</code> scope to their PAT token.</a><br />
<img decoding="async" src="https://github.com/github/release-assets/assets/7575792/2fc21313-dc9b-4ac6-a34e-d97335b8766b" alt="Screenshot showing User PAT for selecting correct scope">
</li>
<li><a href="https://docs.github.com/rest/overview/permissions-required-for-github-apps#repository-permissions-for-contents">Apps will need to use the <code>read and write</code> contents permission.</a><br />
<img decoding="async" src="https://github.com/github/release-assets/assets/7575792/705cbda4-d96e-4bf2-aafc-ef18c3c295da" alt="Screenshot showing app permissions needed">
</li>
<li><a href="https://docs.github.com/actions/security-guides/automatic-token-authentication">GitHub Actions customers will need to add <code>contents:write</code> to their workflow YAML.</a>
<pre><code>permissions:
     contents: write</code></pre>
</li>
</ul>
<p>A small cohort of customers relying on this flow have been notified of these changes and will have additional time to remediate.</p>
<p>We appreciate your feedback in GitHub&#039;s <a href="https://github.com/orgs/community/discussions/categories/repositories">public feedback discussions</a>.</p>
<p>The post <a href="https://github.blog/changelog/2023-09-26-changes-to-token-permission-on-packages">Changes to token permission on packages</a> appeared first on <a href="https://github.blog">The GitHub Blog</a>.</p>
]]></content:encoded>
					
		
		
		<post-id xmlns="com-wordpress:feed-additions:1">74328</post-id>	</item>
		<item>
		<title>Passkeys are Generally Available</title>
		<link>https://github.blog/changelog/2023-09-21-passkeys-are-generally-available</link>
		
		<dc:creator><![CDATA[Kevin Duck]]></dc:creator>
		<pubDate>Thu, 21 Sep 2023 16:01:56 +0000</pubDate>
				<guid isPermaLink="false">https://github.blog/changelog/2023-09-21-passkeys-are-generally-available</guid>

					<description><![CDATA[<p>Passkeys are Generally Available</p>
<p>The post <a href="https://github.blog/changelog/2023-09-21-passkeys-are-generally-available">Passkeys are Generally Available</a> appeared first on <a href="https://github.blog">The GitHub Blog</a>.</p>
]]></description>
										<content:encoded><![CDATA[<p>Passkeys are a replacement for passwords when signing in, providing higher security, ease-of-use, and loss-protection. They are now <a href="https://github.blog/2023-09-21-passkeys-are-generally-available/">generally available on GitHub.com</a> for all users. By using a passkey you no longer need to enter a password, or even your username, when you sign in &#8211; nor do you need to perform 2FA, if you have 2FA enabled on your account. This is because passkeys validate your identity, as well as possession of a device, so they count as two authentication factors in one. Once enrolled, you can register a brand new passkey and upgrade many security keys to passkeys.</p>
<p align="center"><img decoding="async" alt="Screenshot of the security key upgrade prompt, asking the user if they'd like to upgrade a security key called 'fingerprint' to a passkey." src="https://i0.wp.com/github-production-user-asset-6210df.s3.amazonaws.com/1666363/253016634-a4fb158f-31f7-4b70-8a92-a7720bf0f7ed.png?w=346&#038;ssl=1" data-recalc-dims="1"> </p>
<p>To learn more, check out our documentation <a href="https://docs.github.com/en/authentication/authenticating-with-a-passkey/about-passkeys">&quot;About passkeys&quot;</a>, as well as <a href="https://gh.io/passkeys-beta-blog">this previous blog post from the passkeys beta announcement</a>. If you have any feedback, please drop us a note in our <a href="https://gh.io/passkey-feedback">public discussion</a> &#8211; we&#039;re excited for this advance in account security, and would love to understand how we can make it better for you.</p>
<p>The post <a href="https://github.blog/changelog/2023-09-21-passkeys-are-generally-available">Passkeys are Generally Available</a> appeared first on <a href="https://github.blog">The GitHub Blog</a>.</p>
]]></content:encoded>
					
		
		
		<post-id xmlns="com-wordpress:feed-additions:1">74211</post-id>	</item>
		<item>
		<title>Enterprise managed users support for Ping Federate is now generally available</title>
		<link>https://github.blog/changelog/2023-09-19-enterprise-managed-users-support-for-ping-federate-is-now-generally-available</link>
		
		<dc:creator><![CDATA[Kevin Duck]]></dc:creator>
		<pubDate>Tue, 19 Sep 2023 20:44:17 +0000</pubDate>
				<guid isPermaLink="false">https://github.blog/changelog/2023-09-19-enterprise-managed-users-support-for-ping-federate-is-now-generally-available</guid>

					<description><![CDATA[<p>Enterprise managed users support for Ping Federate is now generally available</p>
<p>The post <a href="https://github.blog/changelog/2023-09-19-enterprise-managed-users-support-for-ping-federate-is-now-generally-available">Enterprise managed users support for Ping Federate is now generally available</a> appeared first on <a href="https://github.blog">The GitHub Blog</a>.</p>
]]></description>
										<content:encoded><![CDATA[<p>Now generally available, GitHub Enterprise Cloud customers with enterprise managed users (EMU) can integrate with Ping Federate as a formally supported SSO and SCIM identity provider. To get started, download the Ping Federate <a href="https://www.pingidentity.com/en/resources/downloads/pingfederate.html">&quot;GitHub EMU Connector 1.0&quot; from the add-ons tab on the download page, under the &quot;SaaS Connectors&quot; heading</a>. Add the connector to your Ping Federate installation and consult the <a href="https://docs.pingidentity.com/r/en-us/pingfederate-github-emu-connector/pingfederate_github_connector_configure_pingfederate_for_provisioning_and_sso">Ping Federate documentation</a> in addition to GitHub&#039;s <a href="https://docs.github.com/en/enterprise-cloud@latest/admin/identity-and-access-management/using-enterprise-managed-users-for-iam/configuring-saml-single-sign-on-for-enterprise-managed-users">SAML SSO</a> and <a href="https://docs.github.com/en/enterprise-cloud@latest/admin/identity-and-access-management/using-enterprise-managed-users-for-iam/configuring-scim-provisioning-for-enterprise-managed-users">SCIM</a> documentation for configuration.</p>
<p align="center">
<img decoding="async" width="150" height="150" src="https://i0.wp.com/github-production-user-asset-6210df.s3.amazonaws.com/1852630/269085450-d6d1c763-66d0-497a-87a0-7770607504f2.png?resize=150%2C150&#038;ssl=1" alt="The Ping Identity logo" data-recalc-dims="1">
</p>
<p>The &quot;GitHub EMU Connector&quot; is maintained and <a href="https://support.pingidentity.com/s/">supported</a> by our partner, Ping Identity. Ping additionally maintains their own <a href="https://docs.pingidentity.com/r/en-us/pingfederate-github-emu-connector/pingfederate_github_connector_changelog">release notes</a> for this connector.</p>
<p>The post <a href="https://github.blog/changelog/2023-09-19-enterprise-managed-users-support-for-ping-federate-is-now-generally-available">Enterprise managed users support for Ping Federate is now generally available</a> appeared first on <a href="https://github.blog">The GitHub Blog</a>.</p>
]]></content:encoded>
					
		
		
		<post-id xmlns="com-wordpress:feed-additions:1">74155</post-id>	</item>
		<item>
		<title>New 2FA account recovery options via password reset flow</title>
		<link>https://github.blog/changelog/2023-09-07-new-2fa-account-recovery-options-via-password-reset-flow</link>
		
		<dc:creator><![CDATA[Kevin Duck]]></dc:creator>
		<pubDate>Thu, 07 Sep 2023 20:33:28 +0000</pubDate>
				<guid isPermaLink="false">https://github.blog/changelog/2023-09-07-new-2fa-account-recovery-options-via-password-reset-flow</guid>

					<description><![CDATA[<p>New 2FA account recovery options via password reset flow</p>
<p>The post <a href="https://github.blog/changelog/2023-09-07-new-2fa-account-recovery-options-via-password-reset-flow">New 2FA account recovery options via password reset flow</a> appeared first on <a href="https://github.blog">The GitHub Blog</a>.</p>
]]></description>
										<content:encoded><![CDATA[<p>Users with two-factor authentication enabled can now begin the account recovery process from the password reset flow. Previously, the account password was needed to access 2FA account recovery, but passwords on 2FA-enabled accounts could only be reset with a valid second factor. If you lost your password and all of your second factors, you were locked out because you could not access account recovery. With this change, a user can recover their account as long as they can perform email verification and provide a recovery factor, such as an SSH key, PAT, or previously signed in device.</p>
<p>Once you have performed email verification and provided a recovery factor, your recovery will be manually reviewed by GitHub&#039;s support team, who will email you within three business days. If your request is approved, you&#039;ll receive a link that lets you disable 2FA on your account. After that, you can reset your password and regain access to your account.</p>
<p>For more information about two-factor authentication, see &quot;<a href="https://docs.github.com/en/authentication/securing-your-account-with-two-factor-authentication-2fa/about-two-factor-authentication">About two-factor authentication</a>&quot;. For account recovery details, see &quot;<a href="https://docs.github.com/en/authentication/securing-your-account-with-two-factor-authentication-2fa/recovering-your-account-if-you-lose-your-2fa-credentials#requesting-help-with-two-factor-authentication">Recovering your account if you lose your 2FA credentials</a>&quot;.</p>
<p>The post <a href="https://github.blog/changelog/2023-09-07-new-2fa-account-recovery-options-via-password-reset-flow">New 2FA account recovery options via password reset flow</a> appeared first on <a href="https://github.blog">The GitHub Blog</a>.</p>
]]></content:encoded>
					
		
		
		<post-id xmlns="com-wordpress:feed-additions:1">74021</post-id>	</item>
		<item>
		<title>Log in with multiple GitHub accounts on GitHub Mobile</title>
		<link>https://github.blog/changelog/2023-08-15-log-in-with-multiple-github-accounts-on-github-mobile</link>reviewed by GitHub&#039;s support team, who will email you within three business days. If your request is approved, you&#039;ll receive a link that lets you disable 2FA on your account. After that, you can r‌eset your password and regain access to your account.</p>
<p>For more information about two-factor authentication, see &quot;<a href="https://docs.github.com/en/authentication/securing-your-account-with-two-factor-authentication-2fa/about-two-factor-authentication">About two-factor authentication</a>&quot;. For account recovery details, see &quot;<a href="https://docs.github.com/en/authentication/securing-your-account-with-two-factor-authentication-2fa/recovering-your-account-if-you-lose-your-2fa-credentials#requesting-help-with-two-factor-authentication">Recovering your account if you lose your 2FA credentials</a>&quot;.</p>
<p>The post <a href="https://github.blog/changelog/2023-09-07-new-2fa-account-recovery-options-via-password-reset-flow">New 2FA account recovery options via password reset flow</a> appeared first on <a href="https://github.blog">The GitHub Blog</a>.</p>
]]></content:encoded>
					
		
		
		<post-id xmlns="com-wordpress:feed-additions:1">74021</post-id>	</item>
		<item>
		<title>Log in with multiple GitHub accounts on GitHub Mobile</title>
		<link>https://github.blog/changelog/2023-08-15-log-in-with-multiple-github-accounts-on-github-mobile</link>
<link rel="dns-prefetch" href="//api.soundcloud.com">
<link rel="dns-prefetch" href="//ssl.google-analytics.com">
<link rel="dns-prefetch" href="//i1.sndcdn.com">
<link rel="dns-prefetch" href="//i2.sndcdn.com">
<link rel="dns-prefetch" href="//i3.sndcdn.com">
<link rel="dns-prefetch" href="//i4.sndcdn.com">
<link rel="dns-prefetch" href="//wis.sndcdn.com">
<link rel="dns-prefetch" href="//va.sndcdn.com">
<link rel="dns-prefetch" href="//pixel.quantserve.com">
<title>Stream Kelow LaTesha music | Listen to songs, albums, playlists for free on SoundCloud</title>
<meta content="record, sounds, share, sound, audio, tracks, music, soundcloud" name="keywords">
<meta name="referrer" content="origin">
<meta name="google-site-verification" content="dY0CigqM8Inubs_hgrYMwk-zGchKwrvJLcvI_G8631Q">
<link crossorigin="use-credentials" rel="manifest" href="/webmanifest.json">
<meta name="viewport" content="width=device-width,minimum-scale=1,maximum-scale=1,user-scalable=no">
<meta content="19507961798" property="fb:app_id">
<meta content="SoundCloud" property="og:site_name">
<meta content="SoundCloud" property="twitter:site">
<meta content="SoundCloud" property="twitter:app:name:iphone">
<meta content="336353151" property="twitter:app:id:iphone">
<meta content="SoundCloud" property="twitter:app:name:ipad">
<meta content="336353151" property="twitter:app:id:ipad">
<meta content="SoundCloud" property="twitter:app:name:googleplay">
<meta content="com.soundcloud.android" property="twitter:app:id:googleplay">
<link href="/sc-opensearch.xml" rel="search" title="SoundCloud" type="application/opensearchdescription+xml">
<meta name="description" content="Play Kelow LaTesha and discover followers on SoundCloud | Stream tracks, albums, playlists on desktop and mobile."><meta property="twitter:app:name:iphone" content="SoundCloud"><meta property="twitter:app:id:iphone" content="336353151"><meta property="twitter:app:name:ipad" content="SoundCloud"><meta property="twitter:app:id:ipad" content="336353151"><meta property="twitter:app:name:googleplay" content="SoundCloud"><meta property="twitter:app:id:googleplay" content="com.soundcloud.android"><meta property="twitter:app:url:googleplay" content="soundcloud://users:2686999"><meta property="twitter:app:url:iphone" content="soundcloud://users:2686999"><meta property="twitter:app:url:ipad" content="soundcloud://users:2686999"><meta property="twitter:title" content="Kelow LaTesha"><meta property="twitter:image" content="https://i1.sndcdn.com/avatars-S5SSZRQ0TRVjnPbJ-p0umgQ-t500x500.jpg"><meta property="twitter:description" content="Listen to Kelow LaTesha | SoundCloud is an audio platform that lets you listen to what you love and share the sounds you create."><meta property="twitter:card" content="player"><meta property="twitter:player:height" content="450"><meta property="twitter:player:width" content="435"><meta property="twitter:player" content="https://w.soundcloud.com/player/?url=https%3A%2F%2Fapi.soundcloud.com%2Fusers%2F2686999&amp;auto_play=false&amp;show_artwork=true&amp;visual=true&amp;origin=twitter"><meta property="twitter:url" content="https://soundcloud.com/kelow"><meta property="al:ios:app_name" content="SoundCloud"><meta property="al:ios:app_store_id" content="336353151"><meta property="al:android:app_name" content="SoundCloud"><meta property="al:android:package" content="com.soundcloud.android"><meta property="og:type" content="music.musician"><meta property="og:url" content="https://soundcloud.com/kelow"><meta property="og:title" content="Kelow LaTesha"><meta property="og:image" content="https://i1.sndcdn.com/avatars-S5SSZRQ0TRVjnPbJ-p0umgQ-t500x500.jpg"><meta property="og:image:width" content="500"><meta property="og:image:height" content="500"><meta property="og:description" content="Listen to Kelow LaTesha | SoundCloud is an audio platform that lets you listen to what you love and share the sounds you create."><meta property="og:locality" content="PG County, Maryland"><meta property="og:country-name" content="United States"><meta property="al:ios:url" content="soundcloud://users:2686999"><meta property="al:android:url" content="soundcloud://users:2686999"><meta property="al:web:should_fallback" content="false"><meta property="soundcloud:sound_count" content="30"><meta property="soundcloud:follower_count" content="21989">
<link rel="canonical" href="https://soundcloud.com/kelow"><link rel="alternate" media="only screen and (max-width: 640px)" href="https://m.soundcloud.com/kelow"><link rel="alternate" type="text/xml+oembed" href="https://soundcloud.com/oembed?url=https%3A%2F%2Fsoundcloud.com%2Fkelow&amp;format=xml"><link rel="alternate" type="text/json+oembed" href="https://soundcloud.com/oembed?url=https%3A%2F%2Fsoundcloud.com%2Fkelow&amp;format=json"><link rel="alternate" href="android-app://com.soundcloud.android/soundcloud/users:2686999"><link rel="alternate" href="ios-app://336353151/soundcloud/users:2686999">
<meta name="application-name" content="SoundCloud">
<meta name="msapplication-tooltip" content="Launch SoundCloud">
<meta name="msapplication-TileImage" content="https://a-v2.sndcdn.com/assets/images/sc-icons/win8-2dc974a18a.png">
<meta name="msapplication-TileColor" content="#ff5500">
<meta name="msapplication-starturl" content="https://soundcloud.com">
<link href="https://a-v2.sndcdn.com/assets/images/sc-icons/favicon-2cadd14bdb.ico" rel="icon">
<link href="https://a-v2.sndcdn.com/assets/images/sc-icons/ios-a62dfc8fe7.png" rel="apple-touch-icon">
<link href="https://a-v2.sndcdn.com/assets/images/sc-icons/fluid-b4e7a64b8b.png" rel="fluid-icon">
<script>
  (function () {
    window.ddjskey = '7FC6D561817844F25B65CDD97F28A1';
    window.ddoptions = {
      ajaxListenerPath: [
        { host: 'soundcloud.com', path: 'comments', strict: true },
        { host: 'soundcloud.io', path: 'comments', strict: true },
      ],
      overrideAbortFetch: true,
      sessionByHeader: true,
      cookieName: 'datadome',
      endpoint: 'https://dwt.soundcloud.com/js/',
      disableAutoRefreshOnCaptchaPassed: true
    };
  })();
</script>
<script src="https://dwt.soundcloud.com/tags.js" async></script>

<script>!function(){var o,a,r;function e(a){return a.test(o)}o=window.navigator.userAgent.toLowerCase();var i,t,n,s=void 0!==window.opera&&"[object Opera]"===window.opera.toString(),p=o.match(/\sopr\/([0-9]+)\./),d=e(/chrome/),c=e(/webkit/),m=!d&&e(/safari/),w=!s&&e(/msie|trident/),f=!c&&e(/gecko/);i=p?parseInt(p[1],10):(n=o.match(/(opera|chrome|safari|firefox|msie|rv:)\/?\s*(\.?\d+(\.\d+)*)/i))&&(t=o.match(/version\/([.\d]+)/i))?parseInt(t[1],10):n?parseInt(n[2],10):null;var h=e(/mobile|android|iphone|ipod|symbianos|nokia|s60|playbook|playstation/);f&&(r=(a=o.match(/(firefox)\/?\s*(\.?\d+(\.\d+)*)/i))&&a.length>1&&parseInt(a[2],10)>=47),i&&!h&&(d&&!p&&i<49||f&&!p&&!1===r||m&&i<9||w||s&&i<13||p&&i<27)&&(window.__sc_abortApp=!0)}()</script>
<link rel="stylesheet" href="https://style.sndcdn.com/css/inter-43e88497e6ff16c818c5.css">

<link rel="stylesheet" href="https://a-v2.sndcdn.com/assets/css/app-695cb4782df564cc8803.css">
</head>
<body class="sc-classic">

<div id="app">
<style>.header{width:100%;background:var(--background-surface-color);height:46px}.sc-classic .header{background:#333}.header__logo{background:var(--background-surface-color)}.sc-classic .header__logo{background:#333}body:not(.sc-classic) .header__logoLink{display:flex;flex-direction:column;justify-content:center;align-content:center}.header__logoLink{height:46px;width:48px}.header__logoLink svg{color:var(--primary-color)}.sc-classic .header__logoLink{background:transparent url(https://a-v2.sndcdn.com/assets/images/header/brand-1b72dd8210.svg) no-repeat 0 11px;background-size:49px 22px;display:block;height:46px;width:49px;margin-right:23px}.sc-classic .header__logoLink{background-image:url(https://a-v2.sndcdn.com/assets/images/header/cloud-e365a472bf.png);background-position-x:12px;background-size:48px 22px;width:69px;margin-right:unset}.sc-classic .header__logoLink svg{display:none}.header__logoLink:focus{background-color:rgba(255,72,0,.8);outline:0}#header__loading{margin:13px auto 0;width:16px;background:url(https://a-v2.sndcdn.com/assets/images/loader-dark-45940ae3d4.gif) center no-repeat;background-size:16px 16px}@media (-webkit-min-device-pixel-ratio:2),(min-resolution:192dpi),(min-resolution:2dppx){.sc-classic .header__logoLink{background-image:url(https://a-v2.sndcdn.com/assets/images/header/cloud@2x-e5fba4606d.png)}}</style>
<div role="banner" class="header sc-selection-disabled show fixed g-dark g-z-index-header">
<div class="header__inner l-container l-fullwidth">
<div class="header__left left">
<div class="header__logo left">
<a href="/" title="Home" class="header__logoLink sc-border-box sc-ir">
<svg viewBox="0 0 143 64" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
<path fill="currentColor" transform="translate(-166.000000, -1125.000000)" d="M308.984235,1169.99251 C308.382505,1180.70295 299.444837,1189.03525 288.718543,1188.88554 L240.008437,1188.88554 C237.777524,1188.86472 235.977065,1187.05577 235.966737,1184.82478 L235.966737,1132.37801 C235.894282,1130.53582 236.962478,1128.83883 238.654849,1128.10753 C238.654849,1128.10753 243.135035,1124.99996 252.572022,1124.99996 C258.337036,1124.99309 263.996267,1126.54789 268.948531,1129.49925 C276.76341,1134.09703 282.29495,1141.75821 284.200228,1150.62285 C285.880958,1150.14737 287.620063,1149.90993 289.36674,1149.91746 C294.659738,1149.88414 299.738952,1152.0036 303.438351,1155.78928 C307.13775,1159.57496 309.139562,1164.70168 308.984235,1169.99251 Z M229.885123,1135.69525 C231.353099,1153.48254 232.420718,1169.70654 229.885123,1187.43663 C229.796699,1188.23857 229.119091,1188.84557 228.312292,1188.84557 C227.505494,1188.84557 226.827885,1188.23857 226.739461,1187.43663 C224.375448,1169.85905 225.404938,1153.33003 226.739461,1135.69525 C226.672943,1135.09199 226.957336,1134.50383 227.471487,1134.18133 C227.985639,1133.85884 228.638946,1133.85884 229.153097,1134.18133 C229.667248,1134.50383 229.951641,1135.09199 229.885123,1135.69525 Z M220.028715,1187.4557 C219.904865,1188.26549 219.208361,1188.86356 218.389157,1188.86356 C217.569953,1188.86356 216.87345,1188.26549 216.7496,1187.4557 C214.986145,1172.28686 214.986145,1156.96477 216.7496,1141.79593 C216.840309,1140.9535 217.551388,1140.31488 218.398689,1140.31488 C219.245991,1140.31488 219.95707,1140.9535 220.047779,1141.79593 C222.005153,1156.95333 221.998746,1172.29994 220.028715,1187.4557 Z M210.153241,1140.2517 C211.754669,1156.55195 212.479125,1171.15545 210.134176,1187.41757 C210.134176,1188.29148 209.425728,1188.99993 208.551813,1188.99993 C207.677898,1188.99993 206.969449,1188.29148 206.969449,1187.41757 C204.70076,1171.36516 205.463344,1156.34224 206.969449,1140.2517 C207.05845,1139.43964 207.744425,1138.82474 208.561345,1138.82474 C209.378266,1138.82474 210.06424,1139.43964 210.153241,1140.2517 Z M200.258703,1187.47476 C200.169129,1188.29694 199.474788,1188.91975 198.647742,1188.91975 C197.820697,1188.91975 197.126356,1188.29694 197.036782,1187.47476 C195.216051,1173.32359 195.216051,1158.99744 197.036782,1144.84627 C197.036782,1143.94077 197.770837,1143.20671 198.676339,1143.20671 C199.581842,1143.20671 200.315897,1143.94077 200.315897,1144.84627 C202.251054,1158.99121 202.231809,1173.33507 200.258703,1187.47476 Z M190.383229,1155.50339 C192.880695,1166.56087 191.755882,1176.32196 190.287906,1187.58915 C190.168936,1188.33924 189.522207,1188.89148 188.762737,1188.89148 C188.003266,1188.89148 187.356537,1188.33924 187.237567,1187.58915 C185.903044,1176.47448 184.797296,1166.48462 187.142244,1155.50339 C187.142244,1154.60842 187.867763,1153.8829 188.762737,1153.8829 C189.65771,1153.8829 190.383229,1154.60842 190.383229,1155.50339 Z M180.526821,1153.82571 C182.814575,1165.15009 182.071055,1174.7396 180.469627,1186.10211 C180.27898,1187.7798 177.400223,1187.79886 177.247706,1186.10211 C175.798795,1174.91118 175.112468,1165.0357 177.190512,1153.82571 C177.281785,1152.97315 178.001234,1152.32661 178.858666,1152.32661 C179.716099,1152.32661 180.435548,1152.97315 180.526821,1153.82571 Z M170.575089,1159.31632 C172.977231,1166.82778 172.157452,1172.92846 170.479765,1180.63056 C170.391921,1181.42239 169.722678,1182.02149 168.925999,1182.02149 C168.12932,1182.02149 167.460077,1181.42239 167.372232,1180.63056 C165.923321,1173.08097 165.332318,1166.84684 167.23878,1159.31632 C167.330053,1158.46376 168.049502,1157.81722 168.906934,1157.81722 C169.764367,1157.81722 170.483816,1158.46376 170.575089,1159.31632 Z"></path>
</svg>
SoundCloud
</a>
</div>
</div>
<div id="header__loading" class="sc-hidden"></div>
</div>
</div>
<script>window.setTimeout((function(){if(!window.__sc_abortApp){var e=window.document.getElementById("header__loading");e&&(e.className="")}}),6e3)</script>
<style>.errorPage__inner{width:580px;margin:0 auto;position:relative;padding-top:460px;background:url(https://a-v2.sndcdn.com/assets/images/errors/500-e5a180b7a8.png) no-repeat 50% 80px;text-align:center;transition:all 1s linear}.errorTitle{margin-bottom:10px;font-size:30px}.errorText{line-height:28px;color:#666;font-size:20px}.errorButtons{margin-top:30px}@media (max-width:1280px){.errorPage__inner{background-size:80%}}</style>
<noscript class="errorPage__inner">
<div class="errorPage__inner">
<p class="errorTitle">JavaScript is disabled</p>
<p class="errorText sc-font-light">You need to enable JavaScript to use SoundCloud</p>
<div class="errorButtons">
<a href="http://www.enable-javascript.com/" target="_blank" class="sc-button sc-button-medium">Show me how to enable it</a>
</div>
</div>
</noscript>
<noscript><article itemscope itemtype="http://schema.org/MusicGroup">
  <header>
    <h1 itemprop="name"><a itemprop="url" href="/kelow">Kelow LaTesha</a></h1>
<p>Kelow LaTesha</p>
<p>PG County, Maryland</p>
<img src="https://i1.sndcdn.com/avatars-S5SSZRQ0TRVjnPbJ-p0umgQ-t500x500.jpg" width="" height="" alt="Kelow LaTesha’s avatar" itemprop="image">
    <div itemprop="potentialAction" itemscope itemtype="http://schema.org/ListenAction">
    <div itemprop="target" itemscope itemtype="http://schema.org/EntryPoint">
        <meta itemprop="urlTemplate" content="https://soundcloud.com/kelow">
        <meta itemprop="actionPlatform" content="http://schema.org/DesktopWebPlatform">
        <meta itemprop="actionPlatform" content="http://schema.org/IOSPlatform">
        <meta itemprop="actionPlatform" content="http://schema.org/AndroidPlatform">
        <meta itemprop="actionPlatform" content="http://schema.googleapis.com/GoogleAudioCast">
        <meta itemprop="actionPlatform" content="http://schema.googleapis.com/GoogleVideoCast">
    </div>
    <div itemprop="expectsAcceptanceOf" itemscope itemtype="http://schema.org/Offer">
        <meta itemprop="category" content="free">
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="AD"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="AE"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="AF"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country"><html itemscope itemtype="https://schema.org/QAPage" class="html__responsive " lang="en">

    <head>

        <title>How to make VS Code change language to JavaScript React when open a JSX file - Stack Overflow</title>
        <link rel="shortcut icon" href="https://cdn.sstatic.net/Sites/stackoverflow/Img/favicon.ico?v=ec617d715196">
        <link rel="apple-touch-icon" href="https://cdn.sstatic.net/Sites/stackoverflow/Img/apple-touch-icon.png?v=c78bd457575a">
        <link rel="image_src" href="https://cdn.sstatic.net/Sites/stackoverflow/Img/apple-touch-icon.png?v=c78bd457575a"> 
        <link rel="search" type="application/opensearchdescription+xml" title="Stack Overflow" href="/opensearch.xml">
        <link rel="canonical" href="https://stackoverflow.com/questions/45558193/how-to-make-vs-code-change-language-to-javascript-react-when-open-a-jsx-file" />
    <meta name="viewport" content="width=device-width, height=device-height, initial-scale=1.0, minimum-scale=1.0">
        <meta property="og:type" content= "website" />
        <meta property="og:url" content="https://stackoverflow.com/questions/45558193/how-to-make-vs-code-change-language-to-javascript-react-when-open-a-jsx-file"/>
        <meta property="og:site_name" content="Stack Overflow" />
        <meta property="og:image" itemprop="image primaryImageOfPage" content="https://cdn.sstatic.net/Sites/stackoverflow/Img/apple-touch-icon@2.png?v=73d79a89bded" />
        <meta name="twitter:card" content="summary"/>
        <meta name="twitter:domain" content="stackoverflow.com"/>
        <meta name="twitter:title" property="og:title" itemprop="name" content="How to make VS Code change language to JavaScript React when open a JSX file" />
        <meta name="twitter:description" property="og:description" itemprop="description" content="Currently, when I open a .jsx file the default language is plain JavaScript. Is there a way to set the editor to change the language based on the file extension?&#xA;&#xA;Ideally, I can put this setting in..." />
    <script id="webpack-public-path" type="text/uri-list">https://cdn.sstatic.net/</script>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.js"></script>
    <script defer src="https://cdn.sstatic.net/Js/third-party/npm/@stackoverflow/stacks/dist/js/stacks.min.js?v=d5f780ae3281"></script>
    <script src="https://cdn.sstatic.net/Js/stub.en.js?v=d1b2dbc97e66"></script>

    <link rel="stylesheet" type="text/css" href="https://cdn.sstatic.net/Shared/stacks.css?v=7dd441f95133">
    <link rel="stylesheet" type="text/css" href="https://cdn.sstatic.net/Sites/stackoverflow/primary.css?v=ec5b3fb059a5">


    
            <link rel="alternate" type="application/atom+xml" title="Feed for question &#x27;How to make VS Code change language to JavaScript React when open a JSX file&#x27;" href="/feeds/question/45558193">
        <script>
            StackExchange.ready(function () {

                    StackExchange.using("snippets", function () {
                        StackExchange.snippets.initSnippetRenderer();
                    });
                    
                StackExchange.using("postValidation", function () {
                    StackExchange.postValidation.initOnBlurAndSubmit($('#post-form'), 2, 'answer');
                });


                StackExchange.question.init({showAnswerHelp:true,showTrendingSortLaunchPopover:false,showTrendingSortPostLaunchPopover:false,totalCommentCount:0,shownCommentCount:0,enableTables:true,questionId:45558193});

                styleCode();

                    StackExchange.realtime.subscribeToQuestion('1', '45558193');
                    StackExchange.using("gps", function () { StackExchange.gps.trackOutboundClicks('#content', '.js-post-body'); });


            });
        </script>

        
        
        
        <link rel="stylesheet" type="text/css" href="https://cdn.sstatic.net/Shared/Channels/channels.css?v=db82e15fc6d3">

        
        


    <script type="application/json" data-role="module-args" data-module-name="Shared/options.mod">{"options":{"locale":"en","serverTime":1708644465,"routeName":"Questions/Show","stackAuthUrl":"https://stackauth.com","networkMetaHostname":"meta.stackexchange.com","site":{"name":"Stack Overflow","description":"Q\u0026A for professional and enthusiast programmers","isNoticesTabEnabled":true,"enableNewTagCreationWarning":true,"insertSpaceAfterNameTabCompletion":false,"id":1,"cookieDomain":".stackoverflow.com","childUrl":"https://meta.stackoverflow.com","negativeVoteScoreFloor":null,"enableSocialMediaInSharePopup":true,"protocol":"https"},"user":{"fkey":"5662ef7983073cad2533440b428cb2f211f6e3919d4e5e1756ab13f36bbe9766","tid":"8b614c2f-6729-46d5-9043-9c1e835a4d01","rep":0,"isAnonymous":true,"isAnonymousNetworkWide":true},"events":{"postType":{"question":1},"postEditionSection":{"title":1,"body":2,"tags":3}}}}</script>
<script type="application/json" data-role="module-args" data-module-name="Shared/settings.mod">{"settings":{"intercom":{"appId":"inf0secd","hostBaseUrl":"https://stacksnippets.net","enableJavascriptImplementationFor":true},"subscriptions":{"defaultBasicMaxTrueUpSeats":250,"defaultFreemiumMaxTrueUpSeats":50,"defaultMaxTrueUpSeats":1000},"paths":{"jQueryUIJSPath":"https://ajax.googleapis.com/ajax/libs/jqueryui/1.12.0/jquery-ui.min.js","jQueryUICSSPath":"https://ajax.googleapis.com/ajax/libs/jqueryui/1.12.0/themes/smoothness/jquery-ui.css"},"search":{},"mentions":{"maxNumUsersInDropdown":50},"elections":{"opaVoteResultsBaseUrl":"https://www.opavote.com/results/"},"accounts":{"currentPasswordRequiredForChangingStackIdPassword":true},"legal":{"oneTrustTCFConfigId":"c3d9f1e3-55f3-4eba-b268-46cee4c6789c","useCustomConsent":false},"site":{"forceHttpsImages":true,"stacksEditorPreviewEnabled":true,"allowImageUploads":true,"enableUserHovercards":true,"enableImgurHttps":true,"styleCode":true},"markdown":{"enableTables":true},"flags":{"allowRetractingFlags":true,"allowRetractingCommentFlags":true},"snippets":{"renderDomain":"stacksnippets.net","snippetsEnabled":true},"tags":{},"comments":{},"userMessaging":{"showNewFeatureNotice":true},"questions":{"maxTitleSize":150,"enableQuestionTitleLengthLiveWarning":true,"enableSavesFeature":true,"questionTitleLengthStartLiveWarningChars":50}}}</script>
<script>StackExchange.init();</script>

    <script>
        StackExchange.using.setCacheBreakers({"Js/adops.en.js":"6da43f5e0a84","Js/ask.en.js":"","Js/begin-edit-event.en.js":"20edbaccceae","Js/copy-transpiled.en.js":"d31dc7eba3bc","Js/events.en.js":"","Js/explore-qlist.en.js":"2b1f34938b8b","Js/full-anon.en.js":"5adec7a87b06","Js/full.en.js":"387d53fc6a48","Js/highlightjs-loader.en.js":"510e2f94c2bf","Js/inline-tag-editing.en.js":"3e8cc64ee9d6","Js/keyboard-shortcuts.en.js":"107c2ac31497","Js/markdown-it-loader.en.js":"5818ef89ff9d","Js/mentions-transpiled.en.js":"d398f7c1afbf","Js/moderator.en.js":"288a6408e5e1","Js/postCollections-transpiled.en.js":"11a255fe9394","Js/post-validation.en.js":"243bf7d290a3","Js/question-editor.en.js":"","Js/review-v2-transpiled.en.js":"d8246fd945d5","Js/revisions.en.js":"47b4d5ac24c9","Js/stacks-editor.en.js":"27d5bb2356fc","Js/tageditor.en.js":"dc13482a67f8","Js/tageditornew.en.js":"b3d2f0187c1a","Js/tagsuggestions.en.js":"bd6ec908f2a7","Js/unlimited-transpiled.en.js":"f26a1d5f3365","Js/wmd.en.js":"102d8a628997","Js/snippet-javascript-codemirror.en.js":"ae1dcf38deb7"});
        StackExchange.using("gps", function() {
             StackExchange.gps.init(false);
        });
    </script>
    <noscript id="noscript-css"><style>body,.s-topbar{margin-top:1.9em}</style></noscript>
    </head>
    <body class="question-page unified-theme">
    
    <div id="notify-container"></div>
    <div id="custom-header"></div>
        

<header class="s-topbar ps-fixed t0 l0 js-top-bar">
	<div class="s-topbar--container">
			<a href="#" class="s-topbar--menu-btn js-left-sidebar-toggle" role="menuitem" aria-haspopup="true" aria-controls="left-sidebar" aria-expanded="false"><span></span></a>
			<div class="topbar-dialog leftnav-dialog js-leftnav-dialog dno">
				<div class="left-sidebar js-unpinned-left-sidebar" data-can-be="left-sidebar" data-is-here-when="sm"></div>
			</div>
				<a href="https://stackoverflow.com" class="s-topbar--logo js-gps-track"
		   data-gps-track="top_nav.click({is_current:false, location:2, destination:8})">
					<span class="-img _glyph">Stack Overflow</span>
				</a>



			<ol class="s-navigation" role="presentation">

					<li class="md:d-none">
						<a href="https://stackoverflow.co/" class="s-navigation--item js-gps-track"
				   data-gps-track="top_nav.products.click({location:2, destination:7})"
				   data-ga="[&quot;top navigation&quot;,&quot;about menu click&quot;,null,null,null]">About</a>
					</li>

				<li>
					<a href="#"
				   class="s-navigation--item js-gps-track js-products-menu"
				   aria-controls="products-popover"
				   data-controller="s-popover"
				   data-action="s-popover#toggle"
				   data-s-popover-placement="bottom"
				   data-s-popover-toggle-class="is-selected"
				   data-gps-track="top_nav.products.click({location:2, destination:1})"
				   data-ga="[&quot;top navigation&quot;,&quot;products menu click&quot;,null,null,null]">
						Products
					</a>
				</li>

					<li class="md:d-none">
						<a href="https://stackoverflow.co/teams/" class="s-navigation--item js-gps-track"
				   data-gps-track="top_nav.products.click({location:2, destination:7})"
				   data-ga="[&quot;top navigation&quot;,&quot;learn more - teams&quot;,null,null,null]">For Teams</a>
					</li>
			</ol>
			<div class="s-popover ws2 mtn2 p0"
			 id="products-popover"
			 role="menu"
			 aria-hidden="true">
				<div class="s-popover--arrow"></div>
				<ol class="list-reset s-anchors s-anchors__inherit">
					<li class="m6">
						<a href="/questions" class="bar-sm p6 d-block h:bg-black-225 js-gps-track"
					   data-gps-track="top_nav.products.click({location:2, destination:2})"
					   data-ga="[&quot;top navigation&quot;,&quot;public qa submenu click&quot;,null,null,null]">
							<span class="fs-body1 d-block">Stack Overflow</span>
							<span class="fs-caption d-block fc-black-400">Public questions &amp; answers</span>
						</a>
					</li>
					<li class="m6">
						<a href="https://stackoverflow.co/teams/" class="bar-sm p6 d-block h:bg-black-225 js-gps-track"
					   data-gps-track="top_nav.products.click({location:2, destination:3})"
					   data-ga="[&quot;top navigation&quot;,&quot;teams submenu click&quot;,null,null,null]">
							<span class="fs-body1 d-block">Stack Overflow for Teams</span>
							<span class="fs-caption d-block fc-black-400">Where developers &amp; technologists share private knowledge with coworkers</span>
						</a>
					</li>
					<li class="m6">
						<a href="https://stackoverflow.co/talent/" class="bar-sm p6 d-block h:bg-black-225 js-gps-track"
					   data-gps-track="top_nav.products.click({location:2, destination:5})"
					   data-ga="[&quot;top navigation&quot;,&quot;talent submenu click&quot;,null,null,null]">
							<span class="fs-body1 d-block">Talent</span>
							<span class="fs-caption d-block fc-black-400">
								Build your employer brand
							</span>
						</a>
					</li>
					<li class="m6">
						<a href="https://stackoverflow.co/advertising/" class="bar-sm p6 d-block h:bg-black-225 js-gps-track"
					   data-gps-track="top_nav.products.click({location:2, destination:6})"
					   data-ga="[&quot;top navigation&quot;,&quot;advertising submenu click&quot;,null,null,null]">
							<span class="fs-body1 d-block">Advertising</span>
							<span class="fs-caption d-block fc-black-400">Reach developers &amp; technologists worldwide</span>
						</a>
					</li>
					<li class="bt bc-black-200 py6 px6 bbr-md">
						<a href="https://stackoverflow.co/labs/" class="bar-sm p6 d-block h:bg-black-225 js-gps-track"
					   data-gps-track="top_nav.products.click({location:2, destination:7})"
					   data-ga="[&quot;top navigation&quot;,&quot;labs submenu click&quot;,null,null,null]">
						 	<span class="fs-body1 d-block">Labs</span>
							<span class="fs-caption d-block fc-black-400">The future of collective knowledge sharing</span>
						 </a>
					</li>
					<li class="bg-black-100 bt bc-black-200 py6 px6 bbr-md">
						<a href="https://stackoverflow.co/" class="fc-black-400 d-block py6 px6 h:fc-black-600 js-gps-track"
					   data-gps-track="top_nav.products.click({location:2, destination:7})"
					   data-ga="[&quot;top navigation&quot;,&quot;about submenu click&quot;,null,null,null]">About the company</a>
					</li>
				</ol>
			</div>


		        <form id="search" role="search" action=/search class="s-topbar--searchbar js-searchbar " autocomplete="off">
                        <div class="s-topbar--searchbar--input-group">
                            <input name="q"
                                   type="text"
                                   role="combobox"
                                   placeholder="Search&#x2026;"
                                   value=""
                                   autocomplete="off"
                                   maxlength="240"
                                   class="s-input s-input__search js-search-field "
                                   aria-label="Search"
                                   aria-controls="top-search" 
                                   data-controller="s-popover"
                                   data-action="focus->s-popover#show"
                                   data-s-popover-placement="bottom-start" />
                            <svg aria-hidden="true" class="s-input-icon s-input-icon__search svg-icon iconSearch" width="18" height="18"  viewBox="0 0 18 18"><path  d="m18 16.5-5.14-5.18h-.35a7 7 0 1 0-1.19 1.19v.35L16.5 18l1.5-1.5ZM12 7A5 5 0 1 1 2 7a5 5 0 0 1 10 0Z"/></svg>
                            <div class="s-popover p0 wmx100 wmn4 sm:wmn-initial js-top-search-popover" id="top-search" role="menu">
    <div class="s-popover--arrow"></div>
    <div class="js-spinner p24 d-flex ai-center jc-center d-none">
        <div class="s-spinner s-spinner__sm fc-orange-400">
            <div class="v-visible-sr">Loading&#x2026;</div>
        </div>
    </div>

    <span class="v-visible-sr js-screen-reader-info"></span>
    <div class="js-ac-results overflow-y-auto hmx3 d-none"></div>

    <div class="js-search-hints" aria-describedby="Tips for searching"></div>
</div>
                        </div>
                </form>
		
<nav class="h100 ml-auto overflow-x-auto pr12">
    <ol class="s-topbar--content" role="menubar">
    
    
    
        <li class="js-topbar-dialog-corral" role="presentation">
                

    <div class="topbar-dialog siteSwitcher-dialog dno" role="menu">
        <div class="header fw-wrap">
            <h3 class="flex--item">
                <a href="https://stackoverflow.com">current community</a>
            </h3>
            <div class="flex--item fl1">
                <div class="ai-center d-flex jc-end">
                    <button
                        class="js-close-button s-btn s-btn__muted p0 ml8 d-none sm:d-block"
                        type="button"
                        aria-label="Close"
                    >
                        <svg aria-hidden="true" class="svg-icon iconClear" width="18" height="18"  viewBox="0 0 18 18"><path  d="M15 4.41 13.59 3 9 7.59 4.41 3 3 4.41 7.59 9 3 13.59 4.41 15 9 10.41 13.59 15 15 13.59 10.41 9 15 4.41Z"/></svg>
                    </button>
                </div>
            </div>
        </div>
        <div class="modal-content bg-blue-200 current-site-container">
            <ul class="current-site">
                    <li class="d-flex">
                            <div class="fl1">
                <a href="https://stackoverflow.com"  
       class="current-site-link d-flex gx8 site-link js-gps-track"
       data-id="1"
       data-gps-track="site_switcher.click({ item_type:3 })">
        <div class="favicon favicon-stackoverflow site-icon flex--item" title="Stack Overflow"></div>
        <span class="flex--item fl1">
            Stack Overflow
        </span>
    </a>

    </div>
    <div class="related-links">
            <a href="https://stackoverflow.com/help" class="js-gps-track" data-gps-track="site_switcher.click({ item_type:14 })">help</a>
            <a href="https://chat.stackoverflow.com/?tab=site&amp;host=stackoverflow.com" class="js-gps-track" data-gps-track="site_switcher.click({ item_type:6 })">chat</a>
    </div>

                    </li>
                    <li class="related-site d-flex">
                            <div class="L-shaped-icon-container">
        <span class="L-shaped-icon"></span>
    </div>

                            <a href="https://meta.stackoverflow.com"  
       class="s-block-link px16 d-flex gx8 site-link js-gps-track"
       data-id="552"
       data-gps-track="site.switch({ target_site:552, item_type:3 }),site_switcher.click({ item_type:4 })">
        <div class="favicon favicon-stackoverflowmeta site-icon flex--item" title="Meta Stack Overflow"></div>
        <span class="flex--item fl1">
            Meta Stack Overflow
        </span>
    </a>

                    </li>
            </ul>
        </div>

        <div class="header" id="your-communities-header">
            <h3>
your communities            </h3>

        </div>
        <div class="modal-content" id="your-communities-section">

                <div class="call-to-login">
<a href="https://stackoverflow.com/users/signup?ssrc=site_switcher&amp;returnurl=https%3a%2f%2fstackoverflow.com%2fquestions%2f45558193%2fhow-to-make-vs-code-change-language-to-javascript-react-when-open-a-jsx-file" class="login-link js-gps-track" data-gps-track="site_switcher.click({ item_type:10 })">Sign up</a> or <a href="https://stackoverflow.com/users/login?ssrc=site_switcher&amp;returnurl=https%3a%2f%2fstackoverflow.com%2fquestions%2f45558193%2fhow-to-make-vs-code-change-language-to-javascript-react-when-open-a-jsx-file" class="login-link js-gps-track" data-gps-track="site_switcher.click({ item_type:11 })">log in</a> to customize your list.                </div>
        </div>

        <div class="header">
            <h3><a href="https://stackexchange.com/sites">more stack exchange communities</a>
            </h3>
            <a href="https://stackoverflow.blog" class="float-right">company blog</a>
        </div>
        <div class="modal-content">
                <div class="child-content"></div>
        </div>        
    </div>

        </li>
    
            <li role="none"><button class="s-topbar--item s-btn s-btn__icon s-btn__muted d-none sm:d-inline-flex js-searchbar-trigger" role="menuitem" aria-label="Search" aria-haspopup="true" aria-controls="search" title="Click to show search"><svg aria-hidden="true" class="svg-icon iconSearch" width="18" height="18"  viewBox="0 0 18 18"><path  d="m18 16.5-5.14-5.18h-.35a7 7 0 1 0-1.19 1.19v.35L16.5 18l1.5-1.5ZM12 7A5 5 0 1 1 2 7a5 5 0 0 1 10 0Z"/></svg></button></li>
                        <li role="none">
                            <a href="https://stackoverflow.com/users/login?ssrc=head&returnurl=https%3a%2f%2fstackoverflow.com%2fquestions%2f45558193%2fhow-to-make-vs-code-change-language-to-javascript-react-when-open-a-jsx-file" class="s-topbar--item s-topbar--item__unset s-btn s-btn__outlined ws-nowrap js-gps-track" role="menuitem" rel="nofollow"
               data-gps-track="login.click" data-ga="[&quot;top navigation&quot;,&quot;login button click&quot;,null,null,null]">Log in</a>
                        </li>
                        <li role="none"><a href="https://stackoverflow.com/users/signup?ssrc=head&returnurl=https%3a%2f%2fstackoverflow.com%2fquestions%2f45558193%2fhow-to-make-vs-code-change-language-to-javascript-react-when-open-a-jsx-file" class="s-topbar--item s-topbar--item__unset ml4 s-btn s-btn__filled ws-nowrap" role="menuitem" rel="nofollow" data-ga="[&quot;sign up&quot;,&quot;Sign Up Navigation&quot;,&quot;Header&quot;,null,null]">Sign up</a></li>
    </ol>
</nav>


	</div>
</header>

	<script>
		StackExchange.ready(function () { StackExchange.topbar.init(); });
		StackExchange.scrollPadding.setPaddingTop(50, 10); 
	</script>



            <div id="top-hero-div" class="">
                


            </div>


    <div class="container">
                

<div id="left-sidebar" data-is-here-when="md lg" class="left-sidebar js-pinned-left-sidebar ps-relative">
    <div class="left-sidebar--sticky-container js-sticky-leftnav">
        <nav role="navigation">
            <ol class="nav-links">
                <li>
                    <ol class="nav-links">
                        

<li class="ps-relative"  aria-current="false">


    <a
       href="/"
       class="s-block-link pl8 js-gps-track nav-links--link -link__with-icon"
       
       data-gps-track="top_nav.click({is_current: false, location:2, destination:8,  has_activity_notification:False})"
       aria-controls="" data-controller="" data-s-popover-placement="right"
       aria-current="false"
       data-s-popover-auto-show="true" data-s-popover-hide-on-outside-click="never"
    >
        <div class="d-flex ai-center">
<svg aria-hidden="true" class="svg-icon iconHome" width="18" height="18"  viewBox="0 0 18 18"><path  d="M15 10v5a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-5H0l9-9 9 9h-3Zm-8 1v6h4v-6H7Z"/></svg>                <span class="-link--channel-name pl6">Home</span>

        </div>
    </a>
</li>


                        

<li class="ps-relative  youarehere"  aria-current="true">


    <a id="nav-questions"
       href="/questions"
       class="s-block-link pl8 js-gps-track nav-links--link -link__with-icon"
       
       data-gps-track="top_nav.click({is_current: true, location:2, destination:1,  has_activity_notification:False})"
       aria-controls="" data-controller="" data-s-popover-placement="right"
       aria-current="false"
       data-s-popover-auto-show="true" data-s-popover-hide-on-outside-click="never"
    >
        <div class="d-flex ai-center">
<svg aria-hidden="true" class="svg-icon iconQuestion" width="18" height="18"  viewBox="0 0 18 18"><path  d="m4 15-3 3V4c0-1.1.9-2 2-2h12c1.09 0 2 .91 2 2v9c0 1.09-.91 2-2 2H4Zm7.75-3.97c.72-.83.98-1.86.98-2.94 0-1.65-.7-3.22-2.3-3.83a4.41 4.41 0 0 0-3.02 0 3.8 3.8 0 0 0-2.32 3.83c0 1.29.35 2.29 1.03 3a3.8 3.8 0 0 0 2.85 1.07c.62 0 1.2-.11 1.71-.34.65.44 1 .68 1.06.7.23.13.46.23.7.3l.59-1.13a5.2 5.2 0 0 1-1.28-.66Zm-1.27-.9a5.4 5.4 0 0 0-1.5-.8l-.45.9c.33.12.66.29.98.5-.2.07-.42.11-.65.11-.61 0-1.12-.23-1.52-.68-.86-1-.86-3.12 0-4.11.8-.9 2.35-.9 3.15 0 .9 1.01.86 3.03-.01 4.08Z"/></svg>                <span class="-link--channel-name pl6">Questions</span>

        </div>
    </a>
</li>




                        

<li class="ps-relative"  aria-current="false">


    <a
       href="/tags"
       class="s-block-link pl8 js-gps-track nav-links--link -link__with-icon"
       
       data-gps-track="top_nav.click({is_current: false, location:2, destination:2,  has_activity_notification:False})"
       aria-controls="" data-controller="" data-s-popover-placement="right"
       aria-current="false"
       data-s-popover-auto-show="true" data-s-popover-hide-on-outside-click="never"
    >
        <div class="d-flex ai-center">
<svg aria-hidden="true" class="svg-icon iconTags" width="18" height="18"  viewBox="0 0 18 18"><path  d="M9.24 1a3 3 0 0 0-2.12.88l-5.7 5.7a2 2 0 0 0-.38 2.31 3 3 0 0 1 .67-1.01l6-6A3 3 0 0 1 9.83 2H14a3 3 0 0 1 .79.1A2 2 0 0 0 13 1H9.24Z" opacity=".4"/><path  d="M9.83 3a2 2 0 0 0-1.42.59l-6 6a2 2 0 0 0 0 2.82L6.6 16.6a2 2 0 0 0 2.82 0l6-6A2 2 0 0 0 16 9.17V5a2 2 0 0 0-2-2H9.83ZM12 9a2 2 0 1 1 0-4 2 2 0 0 1 0 4Z"/></svg>                <span class="-link--channel-name pl6">Tags</span>

        </div>
    </a>
</li>

                        
                        <li class="pb24"></li>


                        

<li class="ps-relative"  aria-current="false">


    <a id="nav-users"
       href="/users"
       class="s-block-link pl8 js-gps-track nav-links--link -link__with-icon"
       
       data-gps-track="top_nav.click({is_current: false, location:2, destination:3,  has_activity_notification:False})"
       aria-controls="" data-controller="" data-s-popover-placement="right"
       aria-current="false"
       data-s-popover-auto-show="true" data-s-popover-hide-on-outside-click="never"
    >
        <div class="d-flex ai-center">
<svg aria-hidden="true" class="svg-icon iconPeople" width="18" height="18"  viewBox="0 0 18 18"><path  d="M17 14c0 .44-.45 1-1 1H9a1 1 0 0 1-1-1H2c-.54 0-1-.56-1-1 0-2.63 3-4 3-4s.23-.4 0-1c-.84-.62-1.06-.59-1-3 .06-2.42 1.37-3 2.5-3s2.44.58 2.5 3c.06 2.41-.16 2.38-1 3-.23.59 0 1 0 1s1.55.71 2.42 2.09c.78-.72 1.58-1.1 1.58-1.1s.23-.4 0-1c-.84-.61-1.06-.58-1-3 .06-2.41 1.37-3 2.5-3s2.44.59 2.5 3c.05 2.42-.16 2.39-1 3-.23.6 0 1 0 1s3 1.38 3 4Z"/></svg>                <span class="-link--channel-name pl6">Users</span>

        </div>
    </a>
</li>


                            

<li class="ps-relative"  aria-current="false">


    <a id="nav-companies"
       href="https://stackoverflow.com/jobs/companies?so_medium=stackoverflow&amp;so_source=SiteNav"
       class="s-block-link pl8 js-gps-track nav-links--link -link__with-icon"
       
       data-gps-track="top_nav.click({is_current: false, location:2, destination:12,  has_activity_notification:False})"
       aria-controls="" data-controller="" data-s-popover-placement="right"
       aria-current="false"
       data-s-popover-auto-show="true" data-s-popover-hide-on-outside-click="never"
    >
        <div class="d-flex ai-center">
<svg aria-hidden="true" class="svg-icon iconBriefcase" width="18" height="18"  viewBox="0 0 18 18"><path  d="M5 4a1 1 0 0 1 1-1h6a1 1 0 0 1 1 1v1h1a2 2 0 0 1 2 2v6a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V7c0-1.1.9-2 2-2h1V4Zm7 0H6v1h6V4Z"/></svg>                <span class="-link--channel-name pl6">Companies</span>

        </div>
    </a>
</li>



        <li class="ml8 mt32 mb8">
            <a href="javascript:void(0)"
               class="s-link s-link d-flex fl-grow1 fc-black-400 h:fc-black-600 fs-fine"
               role="button"
               aria-controls="popover-labs-left-nav"
               data-controller="s-popover"
               data-action="s-popover#toggle"
               data-s-popover-placement="top"
               data-s-popover-toggle-class="is-selected"
            >
                <div class="flex--item fl-grow1 tt-uppercase fc-black-600 fw-bold">Labs</div>
                <div class="flex--item px12">
                    <svg aria-hidden="true" class="svg-icon iconInfoSm" width="14" height="14"  viewBox="0 0 14 14"><path  d="M7 1a6 6 0 1 1 0 12A6 6 0 0 1 7 1Zm1 10V6H6v5h2Zm0-6V3H6v2h2Z"/></svg>
                </div>
            </a>
        </li>
            

<li class="ps-relative"  aria-current="false">


    <a id="nav-labs-discussions"
       href="/beta/discussions"
       class="s-block-link pl8 ai-center js-gps-track nav-links--link -link__with-icon"
       
       data-gps-track="top_nav.click({is_current: false, location:2, destination:24,  has_activity_notification:False})"
       aria-controls="" data-controller="" data-s-popover-placement="right"
       aria-current="false"
       data-s-popover-auto-show="true" data-s-popover-hide-on-outside-click="never"
    >
        <div class="d-flex ai-center">
<svg aria-hidden="true" class="fc-black-400 w16 svg-icon iconSpeechBubble" width="18" height="18"  viewBox="0 0 18 18"><path  d="m4 15-3 3V4c0-1.1.9-2 2-2h12a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2H4Z"/></svg>                <span class="-link--channel-name pl6">Discussions</span>

                <div class="ps-absolute flex--item r2 mx4 ml-auto">
                    <div class="bar-sm bg-purple-100 fc-purple-400 fs-fine fw-bold px4 py2 lh-unset tt-uppercase">New</div>
                </div>
        </div>
    </a>
</li>


                            <li class="ml8 mt32 mb8">
                                <div class="d-flex jc-space-between ai-center">
                                    <a
                                        class="s-link d-flex fl-grow1 fc-black-400 h:fc-black-600 fs-fine js-collectives-navcta-toggle"
                                        href="javascript:void(0)"
                                        role="button"
                                        aria-controls="popover-discover-collectives"
                                        data-controller="s-popover"
                                        data-action="s-popover#toggle"
                                        data-s-popover-placement="top"
                                        data-s-popover-toggle-class="is-selected"
                                        data-gps-track="top_nav.click({is_current:false, location:2, destination:17})"
                                    >
                                        <div class="flex--item fl-grow1 tt-uppercase fc-black-600 fw-bold">Collectives</div>
                                        <div class="flex--item px12">
<svg aria-hidden="true" class="svg-icon iconPlusSm" width="14" height="14"  viewBox="0 0 14 14"><path  d="M8 2H6v4H2v2h4v4h2V8h4V6H8V2Z"/></svg>                                        </div>
                                    </a>

                                </div>

                            </li>
                                

<li class="ps-relative"  aria-current="false">


    <a id="nav-collective-discover"
       href="/collectives"
       class="s-block-link pl8 ai-center js-collectives-navcta-toggle js-gps-track nav-links--link -link__with-icon"
       
       data-gps-track="top_nav.click({is_current: false, location:2, destination:18,  has_activity_notification:False})"
       aria-controls="" data-controller="" data-s-popover-placement="right"
       aria-current="false"
       data-s-popover-auto-show="true" data-s-popover-hide-on-outside-click="never"
    >
        <div class="d-flex ai-center">
<svg aria-hidden="true" class="mt-auto fc-orange-400 svg-icon iconStarVerified" width="18" height="18"  viewBox="0 0 18 18"><path  d="M9.86.89a1.14 1.14 0 0 0-1.72 0l-.5.58c-.3.35-.79.48-1.23.33l-.72-.25a1.14 1.14 0 0 0-1.49.85l-.14.76c-.1.45-.45.8-.9.9l-.76.14c-.67.14-1.08.83-.85 1.49l.25.72c.15.44.02.92-.33 1.23l-.58.5a1.14 1.14 0 0 0 0 1.72l.58.5c.35.3.48.79.33 1.23l-.25.72c-.23.66.18 1.35.85 1.49l.76.14c.45.1.8.45.9.9l.14.76c.14.67.83 1.08 1.49.85l.72-.25c.44-.15.92-.02 1.23.33l.5.58c.46.52 1.26.52 1.72 0l.5-.58c.3-.35.79-.48 1.23-.33l.72.25c.66.23 1.35-.18 1.49-.85l.14-.76c.1-.45.45-.8.9-.9l.76-.14c.67-.14 1.08-.83.85-1.49l-.25-.72c-.15-.44-.02-.92.33-1.23l.58-.5c.52-.46.52-1.26 0-1.72l-.58-.5c-.35-.3-.48-.79-.33-1.23l.25-.72a1.14 1.14 0 0 0-.85-1.49l-.76-.14c-.45-.1-.8-.45-.9-.9l-.14-.76a1.14 1.14 0 0 0-1.49-.85l-.72.25c-.44.15-.92.02-1.23-.33l-.5-.58Zm-.49 2.67L10.6 6.6c.05.15.19.24.34.25l3.26.22c.36.03.5.48.23.71l-2.5 2.1a.4.4 0 0 0-.14.4l.8 3.16a.4.4 0 0 1-.6.44L9.2 12.13a.4.4 0 0 0-.42 0l-2.77 1.74a.4.4 0 0 1-.6-.44l.8-3.16a.4.4 0 0 0-.13-.4l-2.5-2.1a.4.4 0 0 1 .22-.7l3.26-.23a.4.4 0 0 0 .34-.25l1.22-3.03a.4.4 0 0 1 .74 0Z"/></svg>                <span class="-link--channel-name pl6">Explore Collectives</span>

        </div>
    </a>
</li>


                    </ol>
                </li>
                
                

        

<li class="js-freemium-cta ps-relative">

    <div class="fs-fine tt-uppercase fc-black-600 fw-bold ml8 mt16 mb8">Teams</div>

    <div class="bt bl bb bc-black-200 p12 pb6 fc-black-500 blr-sm overflow-hidden">
        <strong class="fc-black-600 mb6">Stack Overflow for Teams</strong>
        – Start collaborating and sharing organizational knowledge.
        
        <img class="wmx100 mx-auto my8 h-auto d-block" width="139" height="114" src="https://cdn.sstatic.net/Img/teams/teams-illo-free-sidebar-promo.svg?v=47faa659a05e" alt="">

        <a href="https://try.stackoverflow.co/why-teams/?utm_source=so-owned&amp;utm_medium=side-bar&amp;utm_campaign=campaign-38&amp;utm_content=cta" 
           class="w100 s-btn s-btn__filled s-btn__xs bg-orange-400 js-gps-track"
           data-gps-track="teams.create.left-sidenav.click({ Action: 6 })"
           data-ga="[&quot;teams left navigation - anonymous&quot;,&quot;left nav free cta&quot;,&quot;stackoverflow.com/teams/create/free&quot;,null,null]">Create a free Team</a>
        <a href="https://stackoverflow.co/teams/" 
           class="w100 s-btn s-btn__muted s-btn__xs js-gps-track"
           data-gps-track="teams.create.left-sidenav.click({ Action: 5 })"
           data-ga="[&quot;teams left navigation - anonymous&quot;,&quot;left nav free cta&quot;,&quot;stackoverflow.com/teams&quot;,null,null]">Why Teams?</a>

    </div>
</li>


    <li class="d-flex ai-center jc-space-between ml8 mt32 mb8 js-create-team-cta d-none">
        <a href="javascript:void(0)"
            class="s-link d-flex fl-grow1 fc-black-400 h:fc-black-600 fs-fine js-gps-track"
            role="button"
            aria-controls="popover-teams-create-cta"
            data-controller="s-popover"
            data-action="s-popover#toggle"
            data-s-popover-placement="bottom-start"
            data-s-popover-toggle-class="is-selected"
            data-gps-track="teams.create.left-sidenav.click({ Action: ShowInfo })"
            data-ga="[&quot;teams left navigation - anonymous&quot;,&quot;left nav show teams info&quot;,null,null,null]"
        >
            <div class="flex--item fl-grow1 fc-black-600 fw-bold tt-uppercase">Teams</div>
            <div class="flex--item px12">
                <svg aria-hidden="true" class="svg-icon iconPlusSm" width="14" height="14"  viewBox="0 0 14 14"><path  d="M8 2H6v4H2v2h4v4h2V8h4V6H8V2Z"/></svg>
            </div>
        </a>
    </li>
    <li class="ps-relative js-create-team-cta d-none">
        <a href="https://stackoverflowteams.com/teams/create/free/?utm_source=so-owned&amp;utm_medium=side-bar&amp;utm_campaign=campaign-38&amp;utm_content=cta"
        class="s-block-link pl8 js-gps-track nav-links--link"
        title="Stack Overflow for Teams is a private, secure spot for your organization's questions and answers."
        data-gps-track="teams.create.left-sidenav.click({ Action: FreemiumTeamsCreateClick })"
        data-ga="[&quot;teams left navigation - anonymous&quot;,&quot;left nav team click&quot;,&quot;stackoverflow.com/teams/create/free&quot;,null,null]">
            <div class="d-flex ai-center">
                <div class="flex--item s-avatar va-middle bg-orange-400">
                    <div class="s-avatar--letter mtn1">
                        <svg aria-hidden="true" class="svg-icon iconBriefcaseSm" width="14" height="14"  viewBox="0 0 14 14"><path  d="M4 3a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v1h.5c.83 0 1.5.67 1.5 1.5v5c0 .83-.67 1.5-1.5 1.5h-7A1.5 1.5 0 0 1 2 10.5v-5C2 4.67 2.67 4 3.5 4H4V3Zm5 1V3H5v1h4Z"/></svg>
                    </div>
                    <svg aria-hidden="true" class="native s-avatar--badge svg-icon iconShieldXSm" width="9" height="10"  viewBox="0 0 9 10"><path fill="var(--white)" d="M0 1.84 4.5 0 9 1.84v3.17C9 7.53 6.3 10 4.5 10 2.7 10 0 7.53 0 5.01V1.84Z"/><path fill="var(--black-400)" d="M1 2.5 4.5 1 8 2.5v2.51C8 7.34 5.34 9 4.5 9 3.65 9 1 7.34 1 5.01V2.5Zm2.98 3.02L3.2 7h2.6l-.78-1.48a.4.4 0 0 1 .15-.38c.34-.24.73-.7.73-1.14 0-.71-.5-1.23-1.41-1.23-.92 0-1.39.52-1.39 1.23 0 .44.4.9.73 1.14.12.08.18.23.15.38Z"/></svg>
                </div>
                <div class="flex--item pl6">
                    Create free Team
                </div>
            </div>
        </a>
    </li>

            </ol>
        </nav>
    </div>


        <div class="s-popover ws2" id="popover-discover-collectives" role="menu">
            <div class="s-popover--arrow"></div>
            <div>
                <svg aria-hidden="true" class="fc-orange-400 float-right ml24 svg-spot spotCollective" width="48" height="48"  viewBox="0 0 48 48"><path  d="M25.5 7a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5ZM14 18.25c0-.69.56-1.25 1.25-1.25h22.5c.69 0 1.25.56 1.25 1.25V37.5a1 1 0 0 1-1.6.8l-4.07-3.05a1.25 1.25 0 0 0-.75-.25H15.25c-.69 0-1.25-.56-1.25-1.25v-15.5ZM7 24.5a2.5 2.5 0 1 1-5 0 2.5 2.5 0 0 1 5 0ZM25.5 48a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5ZM48 24.5a2.5 2.5 0 1 1-5 0 2.5 2.5 0 0 1 5 0Z" opacity=".2"/><path  d="M21 3.5a3.5 3.5 0 1 1 7 0 3.5 3.5 0 0 1-7 0ZM24.5 2a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3ZM0 23.5a3.5 3.5 0 1 1 7 0 3.5 3.5 0 0 1-7 0ZM3.5 22a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3ZM21 44.5a3.5 3.5 0 1 1 7 0 3.5 3.5 0 0 1-7 0Zm3.5-1.5a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3Zm20-23a3.5 3.5 0 1 0 0 7 3.5 3.5 0 0 0 0-7ZM43 23.5a1.5 1.5 0 1 1 3 0 1.5 1.5 0 0 1-3 0Zm-23.23-3.14a1 1 0 0 1-.13 1.4l-2.08 1.74 2.08 1.73a1 1 0 1 1-1.28 1.54l-2.42-2.02a1.63 1.63 0 0 1 0-2.5l2.42-2.02a1 1 0 0 1 1.4.13Zm7.59 1.41a1 1 0 1 1 1.28-1.54l2.42 2.02c.78.65.78 1.85 0 2.5l-2.42 2.02a1 1 0 1 1-1.28-1.54l2.08-1.73-2.08-1.73ZM24.12 18a1 1 0 0 1 .87 1.12l-1 8a1 1 0 1 1-1.98-.24l1-8a1 1 0 0 1 1.11-.87Zm-11.87-5C11.01 13 10 14 10 15.25v15.5c0 1.24 1 2.25 2.25 2.25h17.33c.06 0 .11.02.15.05l4.07 3.05a2 2 0 0 0 3.2-1.6V15.25c0-1.24-1-2.25-2.25-2.25h-22.5ZM12 15.25c0-.14.11-.25.25-.25h22.5c.14 0 .25.11.25.25V34.5l-4.07-3.05a2.2 2.2 0 0 0-1.35-.45H12.25a.25.25 0 0 1-.25-.25v-15.5Zm7.24-10.68a1 1 0 1 0-.48-1.94A22.04 22.04 0 0 0 2.91 17.7a1 1 0 1 0 1.92.58 20.04 20.04 0 0 1 14.4-13.72Zm11.05-1.66a1 1 0 0 0-.58 1.92c6.45 1.92 11.54 7 13.46 13.46a1 1 0 1 0 1.92-.58 22.05 22.05 0 0 0-14.8-14.8ZM4.57 28.76a1 1 0 0 0-1.94.48 22.03 22.03 0 0 0 16.13 16.13 1 1 0 1 0 .48-1.94A20.03 20.03 0 0 1 4.57 28.76Zm40.8.48a1 1 0 1 0-1.94-.48 20.04 20.04 0 0 1-13.72 14.41 1 1 0 0 0 .58 1.92 22.04 22.04 0 0 0 15.08-15.85Z"/></svg>
                <h5 class="pt4 fw-bold">Collectives™ on Stack Overflow</h5>
                <p class="my16 fs-caption fc-black-500">Find centralized, trusted content and collaborate around the technologies you use most.</p>
                <a href="/collectives"
            class="js-gps-track s-btn s-btn__filled s-btn__xs"
            data-gps-track="top_nav.click({is_current:false, location:2, destination:18})">
                    Learn more about Collectives
                </a>
            </div>
        </div>

        <div class="s-popover ws2"
        id="popover-teams-create-cta"
        role="menu"
        aria-hidden="true">
            <div class="s-popover--arrow"></div>

            <div class="ps-relative overflow-hidden">
                <p class="mb2"><strong>Teams</strong></p>
                <p class="mb12 fs-caption fc-black-400">Q&amp;A for work</p>
                <p class="mb12 fs-caption fc-black-500">Connect and share knowledge within a single location that is structured and easy to search.</p>
                <a href="https://stackoverflow.co/teams/"
            class="js-gps-track s-btn s-btn__filled s-btn__xs"
            data-gps-track="teams.create.left-sidenav.click({ Action: CtaClick })"
            data-ga="[&quot;teams left navigation - anonymous&quot;,&quot;left nav cta&quot;,&quot;stackoverflow.com/teams&quot;,null,null]">
                    Learn more about Teams
                </a>
            </div>

            <div class="ps-absolute t8 r8">
                <svg aria-hidden="true" class="fc-orange-400 svg-spot spotPeople" width="48" height="48"  viewBox="0 0 48 48"><path  d="M13.5 28a4.5 4.5 0 1 0 0-9 4.5 4.5 0 0 0 0 9ZM7 30a1 1 0 0 1 1-1h11a1 1 0 0 1 1 1v5h11v-5a1 1 0 0 1 1-1h12a1 1 0 0 1 1 1v10a2 2 0 0 1-2 2H33v5a1 1 0 0 1-1 1H20a1 1 0 0 1-1-1v-5H8a1 1 0 0 1-1-1V30Zm25-6.5a4.5 4.5 0 1 0 9 0 4.5 4.5 0 0 0-9 0ZM24.5 34a4.5 4.5 0 1 0 0-9 4.5 4.5 0 0 0 0 9Z" opacity=".2"/><path  d="M16.4 26.08A6 6 0 1 0 7.53 26C5.64 26.06 4 27.52 4 29.45V40a1 1 0 0 0 1 1h9a1 1 0 1 0 0-2h-4v-7a1 1 0 1 0-2 0v7H6v-9.55c0-.73.67-1.45 1.64-1.45H16a1 1 0 0 0 .4-1.92ZM12 18a4 4 0 1 1 0 8 4 4 0 0 1 0-8Zm16.47 14a6 6 0 1 0-8.94 0A3.6 3.6 0 0 0 16 35.5V46a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1V35.5c0-1.94-1.64-3.42-3.53-3.5ZM20 28a4 4 0 1 1 8 0 4 4 0 0 1-8 0Zm-.3 6h8.6c1 0 1.7.75 1.7 1.5V45h-2v-7a1 1 0 1 0-2 0v7h-4v-7a1 1 0 1 0-2 0v7h-2v-9.5c0-.75.7-1.5 1.7-1.5ZM42 22c0 1.54-.58 2.94-1.53 4A3.5 3.5 0 0 1 44 29.45V40a1 1 0 0 1-1 1h-9a1 1 0 1 1 0-2h4v-7a1 1 0 1 1 2 0v7h2v-9.55A1.5 1.5 0 0 0 40.48 28H32a1 1 0 0 1-.4-1.92A6 6 0 1 1 42 22Zm-2 0a4 4 0 1 0-8 0 4 4 0 0 0 8 0Z"/><g  opacity=".35"><path d="M17 10a1 1 0 011-1h12a1 1 0 110 2H18a1 1 0 01-1-1Zm1-5a1 1 0 100 2h12a1 1 0 100-2H18ZM14 1a1 1 0 00-1 1v12a1 1 0 001 1h5.09l4.2 4.2a1 1 0 001.46-.04l3.7-4.16H34a1 1 0 001-1V2a1 1 0 00-1-1H14Zm1 12V3h18v10h-5a1 1 0 00-.75.34l-3.3 3.7-3.74-3.75a1 1 0 00-.71-.29H15Z"/></g></svg>
            </div>
        </div>

        <div class="s-popover ws2"
             id="popover-labs-left-nav"
             role="menu"
             aria-hidden="true">
            <div class="s-popover--arrow"></div>
            <svg aria-hidden="true" class="fc-black-600 mb8 svg-icon iconLabs" width="42" height="18"><path d="M11.5 13.624a.374.374 0 0 1-.37.376H5.361a.374.374 0 0 1-.37-.376V4.376c0-.207.165-.376.37-.376H6.62c.204 0 .37.169.37.376v7.611h4.138c.205 0 .371.169.371.377v1.26zm9.432.215c-.07.1-.185.161-.308.161H19.13a.376.376 0 0 1-.356-.254l-.55-1.7h-3.111l-.55 1.7a.377.377 0 0 1-.355.254h-1.494a.376.376 0 0 1-.353-.506l3.39-9.247A.376.376 0 0 1 16.103 4h1.13c.158 0 .299.099.353.247l3.39 9.247a.376.376 0 0 1-.045.345zm-4.157-7.386l-1.219 3.531h2.266l-1.047-3.53zm13.335 5.71a.37.37 0 0 0-.003.524c.956.971 2.047 1.313 3.486 1.313 1.014 0 1.92-.265 2.582-.788.67-.53 1.063-1.306 1.063-2.255 0-.855-.268-1.622-.867-2.145-.456-.41-1.008-.633-1.89-.767l-1.037-.153c-.377-.057-.672-.19-.832-.332-.146-.132-.221-.315-.221-.568 0-.309.11-.56.306-.737.199-.179.518-.312.986-.312.708 0 1.254.151 1.726.601a.37.37 0 0 0 .516-.004l.883-.87a.37.37 0 0 0-.008-.534C35.942 4.334 35.004 4 33.721 4c-1.016 0-1.872.292-2.479.836-.61.548-.935 1.32-.935 2.207 0 .82.243 1.502.781 2.01h.001c.468.437 1.135.716 1.93.826l1.072.153c.508.073.647.147.795.286l.008.007c.14.125.234.34.234.67 0 .332-.124.567-.344.73-.235.174-.617.293-1.165.293-.867 0-1.49-.185-2.066-.76a.37.37 0 0 0-.522-.003l-.92.908zM22.37 14a.374.374 0 0 1-.37-.376V4.376c0-.207.166-.376.37-.376h3.543c.913 0 1.697.264 2.257.78.564.519.863 1.259.863 2.129 0 .845-.377 1.524-.87 1.947.57.433 1.01 1.145 1.01 2.157 0 .941-.317 1.702-.894 2.224-.57.517-1.354.763-2.225.763H22.37zm3.543-1.977c.96 0 .959-1.01.959-1.01s0-1.013-.959-1.013H24v2.023h1.913zm-.115-4.063c1.074 0 1.074-1.015 1.074-1.015s0-1.016-1.074-1.016H24V7.96h1.798z" fill="var(--black-600)"/><path d="M0 4v10a4 4 0 0 0 4 4h34a4 4 0 0 0 4-4V4a4 4 0 0 0-4-4H4a4 4 0 0 0-4 4zm4-2h34a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2z" fill="var(--black-600)"/></svg>
            <p class="fs-caption">Get early access and see previews of new features.</p>
            <a class="s-btn s-btn__filled s-btn__xs s-btn__icon fs-fine" href="https://stackoverflow.co/labs/"><svg aria-hidden="true" class="svg-icon iconShareSm" width="14" height="14"  viewBox="0 0 14 14"><path  d="M5 1H3a2 2 0 0 0-2 2v8c0 1.1.9 2 2 2h8a2 2 0 0 0 2-2V9h-2v2H3V3h2V1Zm2 0h6v6h-2V4.5L6.5 9 5 7.5 9.5 3H7V1Z"/></svg> Learn more about Labs</a>
        </div>


</div>



        <div id="content" class="snippet-hidden">

            

<div itemprop="mainEntity" itemscope itemtype="https://schema.org/Question">
    <link itemprop="image" href="https://cdn.sstatic.net/Sites/stackoverflow/Img/apple-touch-icon.png?v=c78bd457575a">

    <div class="inner-content clearfix">

        

            <div id="question-header" class="d-flex sm:fd-column">
                        <h1 itemprop="name" class="fs-headline1 ow-break-word mb8 flex--item fl1"><a href="/questions/45558193/how-to-make-vs-code-change-language-to-javascript-react-when-open-a-jsx-file" class="question-hyperlink">How to make VS Code change language to JavaScript React when open a JSX file</a></h1>
                <div class="ml12 aside-cta flex--item print:d-none sm:ml0 sm:mb12 sm:order-first sm:as-end">
                        <a href="/questions/ask" class="ws-nowrap s-btn s-btn__filled">
        Ask Question
    </a>

                </div>
            </div>
            <div class="d-flex fw-wrap pb8 mb16 bb bc-black-200">
                    <div class="flex--item ws-nowrap mr16 mb8" title="2017-08-08 02:01:47Z">
                        <span class="fc-black-400 mr2">Asked</span>
                        <time itemprop="dateCreated" datetime="2017-08-08T02:01:47">6 years, 6 months ago</time>
                    </div>
                    <div class="flex--item ws-nowrap mr16 mb8">
                        <span class="fc-black-400 mr2">Modified</span>
                        <a href="?lastactivity" class="s-link s-link__inherit" title="2022-10-05 07:58:13Z">1 year, 4 months ago</a>
                    </div>
                    <div class="flex--item ws-nowrap mb8" title="Viewed 29,191 times">
                        <span class="fc-black-400 mr2">Viewed</span>
                        29k times
                    </div>
            </div>

            <div id="mainbar" role="main" aria-label="question and answers">
                
<div class="question js-question" data-questionid="45558193" data-position-on-page="0" data-score="28"  id="question">
    <style>
    </style>
<div class="js-zone-container zone-container-main">
    <div id="dfp-tlb" class="everyonelovesstackoverflow everyoneloves__top-leaderboard everyoneloves__leaderboard"></div>
		<div class="js-report-ad-button-container " style="width: 728px"></div>
</div>


    <div class="post-layout ">
        <div class="votecell post-layout--left">
            <div class="js-voting-container d-flex jc-center fd-column ai-stretch gs4 fc-black-300" data-post-id="45558193" data-referrer="None">
        <button class="js-vote-up-btn flex--item s-btn ba bar-pill c-pointer as-center bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050"
                data-controller="s-tooltip"
                data-s-tooltip-placement="right"
                title="This question shows research effort; it is useful and clear"
                aria-pressed="false"
                aria-label="Up vote"
                data-selected-classes="fc-theme-primary bc-theme-primary bg-theme-primary-100"
                data-unselected-classes="bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050">
            <svg aria-hidden="true" class="svg-icon iconArrowUp" width="18" height="18"  viewBox="0 0 18 18"><path  d="M1 12h16L9 4l-8 8Z"/></svg>
        </button>
        <div class="js-vote-count flex--item d-flex fd-column ai-center fc-theme-body-font fw-bold fs-subheading py4"
             itemprop="upvoteCount"
             data-value="28">
            28
        </div>
        <button class="js-vote-down-btn flex--item mb8 s-btn ba bar-pill c-pointer as-center bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050"
                data-controller="s-tooltip"
                data-s-tooltip-placement="right"
                title="This question does not show any research effort; it is unclear or not useful"
                aria-pressed="false"
                aria-label="Down vote"
                data-selected-classes="fc-theme-primary bc-theme-primary bg-theme-primary-100"
                data-unselected-classes="bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050">
            <svg aria-hidden="true" class="svg-icon iconArrowDown" width="18" height="18"  viewBox="0 0 18 18"><path  d="M1 6h16l-8 8-8-8Z"/></svg>
        </button>


        
<button class="js-saves-btn s-btn s-btn__unset c-pointer py4"
        type="button"
        id="saves-btn-45558193"
        data-controller="s-tooltip"
        data-s-tooltip-placement="right"
        data-s-popover-placement=""
        title="Save this question."
        aria-pressed="false"
        data-post-id="45558193"
        data-post-type-id="1"
        data-user-privilege-for-post-click="0"
        aria-controls=""
        data-s-popover-auto-show="false"
>
    <svg aria-hidden="true" class="fc-theme-primary-400 js-saves-btn-selected d-none svg-icon iconBookmark" width="18" height="18"  viewBox="0 0 18 18"><path  d="M3 17V3c0-1.1.9-2 2-2h8a2 2 0 0 1 2 2v14l-6-4-6 4Z"/></svg>
    <svg aria-hidden="true" class="js-saves-btn-unselected svg-icon iconBookmarkAlt" width="18" height="18"  viewBox="0 0 18 18"><path  d="m9 10.6 4 2.66V3H5v10.26l4-2.66ZM3 17V3c0-1.1.9-2 2-2h8a2 2 0 0 1 2 2v14l-6-4-6 4Z"/></svg>
</button>








    
    <a class="js-post-issue flex--item s-btn s-btn__unset c-pointer py6 mx-auto" href="/posts/45558193/timeline" data-shortcut="T" data-ks-title="timeline" data-controller="s-tooltip" data-s-tooltip-placement="right" title="Show activity on this post." aria-label="Timeline"><svg aria-hidden="true" class="mln2 mr0 svg-icon iconHistory" width="19" height="18"  viewBox="0 0 19 18"><path  d="M3 9a8 8 0 1 1 3.73 6.77L8.2 14.3A6 6 0 1 0 5 9l3.01-.01-4 4-4-4h3L3 9Zm7-4h1.01L11 9.36l3.22 2.1-.6.93L10 10V5Z"/></svg></a>

</div>

        </div>

        

<div class="postcell post-layout--right">
    
    <div class="s-prose js-post-body" itemprop="text">
                
<p>Currently, when I open a <code>.jsx</code> file the default language is plain JavaScript. Is there a way to set the editor to change the language based on the file extension?</p>

<p>Ideally, I can put this setting in both my local setting config file OR the workspace specific config file.</p>

<p>The version I am using is <code>Version 1.15.0-insider (1.15.0-insider)</code>.</p>
    </div>

        <div class="mt24 mb12">
            <div class="post-taglist d-flex gs4 gsy fd-column">
                <div class="d-flex ps-relative fw-wrap">
                    
                    <ul class='ml0 list-ls-none js-post-tag-list-wrapper d-inline'><li class='d-inline mr4 js-post-tag-list-item'><a href="/questions/tagged/visual-studio-code" class="post-tag" title="show questions tagged &#39;visual-studio-code&#39;" aria-label="show questions tagged &#39;visual-studio-code&#39;" rel="tag" aria-labelledby="tag-visual-studio-code-tooltip-container">visual-studio-code</a></li></ul>
                </div>
            </div>
        </div>

    <div class="mb0 ">
        <div class="mt16 d-flex gs8 gsy fw-wrap jc-end ai-start pt4 mb16">
            <div class="flex--item mr16 fl1 w96">
                


<div class="js-post-menu pt2" data-post-id="45558193" data-post-type-id="1">

    <div class="d-flex gs8 s-anchors s-anchors__muted fw-wrap">

        <div class="flex--item">
            <a href="/q/45558193"
               rel="nofollow"
               itemprop="url"
               class="js-share-link js-gps-track"
               title="Short permalink to this question"
               data-gps-track="post.click({ item: 2, priv: 0, post_type: 1 })"
               data-controller="se-share-sheet"
               data-se-share-sheet-title="Share a link to this question"
               data-se-share-sheet-subtitle=""
               data-se-share-sheet-post-type="question"
               data-se-share-sheet-social="facebook twitter devto"
               data-se-share-sheet-location="1"
               data-se-share-sheet-license-url="https%3a%2f%2fcreativecommons.org%2flicenses%2fby-sa%2f3.0%2f"
               data-se-share-sheet-license-name="CC BY-SA 3.0"
               data-s-popover-placement="bottom-start">Share</a>
        </div>



                <div class="flex--item">
                    <button type="button"
                            id="btnFollowPost-45558193" class="s-btn s-btn__link js-follow-post js-follow-question js-gps-track"
                            data-gps-track="post.click({ item: 14, priv: 0, post_type: 1 })"
                            data-controller="s-tooltip " data-s-tooltip-placement="bottom"
                            data-s-popover-placement="bottom" aria-controls=""
                            title="Follow this question to receive notifications">
                        Follow
                    </button>
                </div>






    </div>
    <div class="js-menu-popup-container"></div>
</div>
            </div>

                <div class="post-signature flex--item">
<div class="user-info user-hover ">
    <div class="d-flex ">
        <div class="user-action-time fl-grow1">
            <a href="/posts/45558193/revisions" title="show all edits to this post"
                         class="js-gps-track"
                         data-gps-track="post.click({ item: 4, priv: 0, post_type: 1 })">edited <span title='2017-08-08 02:35:49Z' class='relativetime'>Aug 8, 2017 at 2:35</span></a>
        </div>
        
    </div>
    <div class="user-gravatar32">
        <a href="/users/4810504/tom-van-rompaey"><div class="gravatar-wrapper-32"><img src="https://graph.facebook.com/10206696305836466/picture?type=large" alt="Tom Van Rompaey&#39;s user avatar" width="32" height="32" class="bar-sm"></div></a>
    </div>
    <div class="user-details">
        <a href="/users/4810504/tom-van-rompaey">Tom Van Rompaey</a>
        <div class="-flair">
            <span class="reputation-score" title="reputation score " dir="ltr">3,566</span><span title="20 silver badges" aria-hidden="true"><span class="badge2"></span><span class="badgecount">20</span></span><span class="v-visible-sr">20 silver badges</span><span title="22 bronze badges" aria-hidden="true"><span class="badge3"></span><span class="badgecount">22</span></span><span class="v-visible-sr">22 bronze badges</span>
        </div>
    </div>
</div>
                </div>
            <div class="post-signature owner flex--item">
                <div class="user-info ">
    <div class="d-flex ">
        <div class="user-action-time fl-grow1">
            asked <span title='2017-08-08 02:01:47Z' class='relativetime'>Aug 8, 2017 at 2:01</span>
        </div>
        
    </div>
    <div class="user-gravatar32">
        <a href="/users/2226315/xyz"><div class="gravatar-wrapper-32"><img src="https://www.gravatar.com/avatar/becc42cd815d380c8d12394adf77f4fa?s=64&amp;d=identicon&amp;r=PG&amp;f=y&amp;so-version=2" alt="XYZ&#39;s user avatar" width="32" height="32" class="bar-sm"></div></a>
    </div>
    <div class="user-details" itemprop="author" itemscope itemtype="http://schema.org/Person">
        <a href="/users/2226315/xyz">XYZ</a><span class="d-none" itemprop="name">XYZ</span>
        <div class="-flair">
            <span class="reputation-score" title="reputation score 26,377" dir="ltr">26.4k</span><span title="15 gold badges" aria-hidden="true"><span class="badge1"></span><span class="badgecount">15</span></span><span class="v-visible-sr">15 gold badges</span><span title="87 silver badges" aria-hidden="true"><span class="badge2"></span><span class="badgecount">87</span></span><span class="v-visible-sr">87 silver badges</span><span title="153 bronze badges" aria-hidden="true"><span class="badge3"></span><span class="badgecount">153</span></span><span class="v-visible-sr">153 bronze badges</span>
        </div>
    </div>
</div>


            </div>
        </div>
    </div>
    
</div>




            <span class="d-none" itemprop="commentCount"></span> 
    <div class="post-layout--right js-post-comments-component">
        <div id="comments-45558193" class="comments js-comments-container bt bc-black-200 mt12  dno" data-post-id="45558193" data-min-length="15">
            <ul class="comments-list js-comments-list"
                    data-remaining-comments-count="0"
                    data-canpost="false"
                    data-cansee="true"
                    data-comments-unavailable="false"
                    data-addlink-disabled="true">

            </ul>
	    </div>

        <div id="comments-link-45558193" data-rep=50 data-anon=true>
                    <a class="js-add-link comments-link disabled-link" title="Use comments to ask for more information or suggest improvements. Avoid answering questions in comments."  href="#" role="button">Add a comment</a>
                <span class="js-link-separator dno">&nbsp;|&nbsp;</span>
            <a class="js-show-link comments-link dno" title="Expand to show all comments on this post" href=# onclick="" role="button"></a>
        </div>         
    </div>
    </div>

</div>


<div class="js-zone-container zone-container-responsive">
    <div id="dfp-isb" class="everyonelovesstackoverflow everyoneloves__inline-sidebar mx-auto"></div>
		<div class="js-report-ad-button-container mx-auto" style="width: 300px"></div>
</div>

                
                
                <div id="answers">
                    <a name="tab-top"></a>
                    <div id="answers-header">
                        <div class="answers-subheader d-flex ai-center mb8">
                            <div class="flex--item fl1">
                                <h2 class="mb0" data-answercount="8">
                                        8 Answers
                                    <span style="display:none;" itemprop="answerCount">8</span>
                                </h2>
                            </div>
                            <div class="flex--item">
                                

<div class="d-flex g4 gsx ai-center sm:fd-column sm:ai-start">
    <div class="d-flex fd-column ai-end sm:ai-start">
        <label class="flex--item fs-caption" for="answer-sort-dropdown-select-menu">
            Sorted by:
        </label>
        <a 
            class="js-sort-preference-change s-link flex--item fs-fine d-none"
            data-value="ScoreDesc"
            href="/questions/45558193/how-to-make-vs-code-change-language-to-javascript-react-when-open-a-jsx-file?answertab=scoredesc#tab-top"
        >
            Reset to default
        </a>
    </div>
    <div class="flex--item s-select">
        <select id="answer-sort-dropdown-select-menu">
                    <option
                        value=scoredesc
                        selected=selected
                    >
                        Highest score (default)
                    </option>
                    <option
                        value=trending
                    >
                        Trending (recent votes count more)
                    </option>
                    <option
                        value=modifieddesc
                    >
                        Date modified (newest first)
                    </option>
                    <option
                        value=createdasc
                    >
                        Date created (oldest first)
                    </option>
        </select>
    </div>
</div>


                            </div>
                        </div>
                            
                    </div>


                                    
<a name="51640555"></a>
<div id="answer-51640555" class="answer js-answer" data-answerid="51640555" data-parentid="45558193" data-score="53" data-position-on-page="1" data-highest-scored="1" data-question-has-accepted-highest-score="0"  itemprop="acceptedAnswer" itemscope itemtype="https://schema.org/Answer">
    <div class="post-layout">
        <div class="votecell post-layout--left">
            <div class="js-voting-container d-flex jc-center fd-column ai-stretch gs4 fc-black-300" data-post-id="51640555" data-referrer="None">
        <button class="js-vote-up-btn flex--item s-btn ba bar-pill c-pointer as-center bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050"
                data-controller="s-tooltip"
                data-s-tooltip-placement="right"
                title="This answer is useful"
                aria-pressed="false"
                aria-label="Up vote"
                data-selected-classes="fc-theme-primary bc-theme-primary bg-theme-primary-100"
                data-unselected-classes="bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050">
            <svg aria-hidden="true" class="svg-icon iconArrowUp" width="18" height="18"  viewBox="0 0 18 18"><path  d="M1 12h16L9 4l-8 8Z"/></svg>
        </button>
        <div class="js-vote-count flex--item d-flex fd-column ai-center fc-theme-body-font fw-bold fs-subheading py4"
             itemprop="upvoteCount"
             data-value="53">
            53
        </div>
        <button class="js-vote-down-btn flex--item mb8 s-btn ba bar-pill c-pointer as-center bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050"
                data-controller="s-tooltip"
                data-s-tooltip-placement="right"
                title="This answer is not useful"
                aria-pressed="false"
                aria-label="Down vote"
                data-selected-classes="fc-theme-primary bc-theme-primary bg-theme-primary-100"
                data-unselected-classes="bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050">
            <svg aria-hidden="true" class="svg-icon iconArrowDown" width="18" height="18"  viewBox="0 0 18 18"><path  d="M1 6h16l-8 8-8-8Z"/></svg>
        </button>


        
<button class="js-saves-btn s-btn s-btn__unset c-pointer py4"
        type="button"
        id="saves-btn-51640555"
        data-controller="s-tooltip"
        data-s-tooltip-placement="right"
        data-s-popover-placement=""
        title="Save this answer."
        aria-pressed="false"
        data-post-id="51640555"
        data-post-type-id="2"
        data-user-privilege-for-post-click="0"
        aria-controls=""
        data-s-popover-auto-show="false"
>
    <svg aria-hidden="true" class="fc-theme-primary-400 js-saves-btn-selected d-none svg-icon iconBookmark" width="18" height="18"  viewBox="0 0 18 18"><path  d="M3 17V3c0-1.1.9-2 2-2h8a2 2 0 0 1 2 2v14l-6-4-6 4Z"/></svg>
    <svg aria-hidden="true" class="js-saves-btn-unselected svg-icon iconBookmarkAlt" width="18" height="18"  viewBox="0 0 18 18"><path  d="m9 10.6 4 2.66V3H5v10.26l4-2.66ZM3 17V3c0-1.1.9-2 2-2h8a2 2 0 0 1 2 2v14l-6-4-6 4Z"/></svg>
</button>







            <div class="js-accepted-answer-indicator flex--item fc-green-400 py6 mtn8 d-none" data-s-tooltip-placement="right" title="Loading when this answer was accepted&#x2026;" tabindex="0" role="note" aria-label="Accepted">
                <div class="ta-center">
                    <svg aria-hidden="true" class="svg-icon iconCheckmarkLg" width="36" height="36"  viewBox="0 0 36 36"><path  d="m6 14 8 8L30 6v8L14 30l-8-8v-8Z"/></svg>
                </div>
            </div>

    
    <a class="js-post-issue flex--item s-btn s-btn__unset c-pointer py6 mx-auto" href="/posts/51640555/timeline" data-shortcut="T" data-ks-title="timeline" data-controller="s-tooltip" data-s-tooltip-placement="right" title="Show activity on this post." aria-label="Timeline"><svg aria-hidden="true" class="mln2 mr0 svg-icon iconHistory" width="19" height="18"  viewBox="0 0 19 18"><path  d="M3 9a8 8 0 1 1 3.73 6.77L8.2 14.3A6 6 0 1 0 5 9l3.01-.01-4 4-4-4h3L3 9Zm7-4h1.01L11 9.36l3.22 2.1-.6.93L10 10V5Z"/></svg></a>

</div>

        </div>

        

<div class="answercell post-layout--right">
    
    <div class="s-prose js-post-body" itemprop="text">
<p>Press <code>cntrl + ,</code> and add this to user settings <code>JSON</code> file:</p>

<pre><code>"files.associations": {
    "*.js": "javascriptreact"
}
</code></pre>

<p>like this:
<a href="https://i.stack.imgur.com/Evp95.png" rel="noreferrer"><img src="https://i.stack.imgur.com/Evp95.png" alt="enter image description here"></a></p>
    </div>
    <div class="mt24">
        <div class="d-flex fw-wrap ai-start jc-end gs8 gsy">
            <time itemprop="dateCreated" datetime="2018-08-01T19:00:00"></time>
            <div class="flex--item mr16" style="flex: 1 1 100px;">
                


<div class="js-post-menu pt2" data-post-id="51640555" data-post-type-id="2">

    <div class="d-flex gs8 s-anchors s-anchors__muted fw-wrap">

        <div class="flex--item">
            <a href="/a/51640555"
               rel="nofollow"
               itemprop="url"
               class="js-share-link js-gps-track"
               title="Short permalink to this answer"
               data-gps-track="post.click({ item: 2, priv: 0, post_type: 2 })"
               data-controller="se-share-sheet"
               data-se-share-sheet-title="Share a link to this answer"
               data-se-share-sheet-subtitle=""
               data-se-share-sheet-post-type="answer"
               data-se-share-sheet-social="facebook twitter devto"
               data-se-share-sheet-location="2"
               data-se-share-sheet-license-url="https%3a%2f%2fcreativecommons.org%2flicenses%2fby-sa%2f4.0%2f"
               data-se-share-sheet-license-name="CC BY-SA 4.0"
               data-s-popover-placement="bottom-start">Share</a>
        </div>



                <div class="flex--item">
                    <button type="button"
                            id="btnFollowPost-51640555" class="s-btn s-btn__link js-follow-post js-follow-answer js-gps-track"
                            data-gps-track="post.click({ item: 14, priv: 0, post_type: 2 })"
                            data-controller="s-tooltip " data-s-tooltip-placement="bottom"
                            data-s-popover-placement="bottom" aria-controls=""
                            title="Follow this answer to receive notifications">
                        Follow
                    </button>
                </div>






    </div>
    <div class="js-menu-popup-container"></div>
</div>
            </div>


            <div class="post-signature flex--item fl0">
                <div class="user-info ">
    <div class="d-flex ">
        <div class="user-action-time fl-grow1">
            answered <span title='2018-08-01 19:00:00Z' class='relativetime'>Aug 1, 2018 at 19:00</span>
        </div>
        
    </div>
    <div class="user-gravatar32">
        <a href="/users/5284370/soorena"><div class="gravatar-wrapper-32"><img src="https://i.stack.imgur.com/oDkhj.jpg?s=64&amp;g=1" alt="Soorena&#39;s user avatar" width="32" height="32" class="bar-sm"></div></a>
    </div>
    <div class="user-details" itemprop="author" itemscope itemtype="http://schema.org/Person">
        <a href="/users/5284370/soorena">Soorena</a><span class="d-none" itemprop="name">Soorena</span>
        <div class="-flair">
            <span class="reputation-score" title="reputation score " dir="ltr">4,382</span><span title="5 gold badges" aria-hidden="true"><span class="badge1"></span><span class="badgecount">5</span></span><span class="v-visible-sr">5 gold badges</span><span title="30 silver badges" aria-hidden="true"><span class="badge2"></span><span class="badgecount">30</span></span><span class="v-visible-sr">30 silver badges</span><span title="43 bronze badges" aria-hidden="true"><span class="badge3"></span><span class="badgecount">43</span></span><span class="v-visible-sr">43 bronze badges</span>
        </div>
    </div>
</div>


            </div>
        </div>
        
    
    </div>
    
</div>




            <span class="d-none" itemprop="commentCount">2</span> 
    <div class="post-layout--right js-post-comments-component">
        <div id="comments-51640555" class="comments js-comments-container bt bc-black-200 mt12 " data-post-id="51640555" data-min-length="15">
            <ul class="comments-list js-comments-list"
                    data-remaining-comments-count="0"
                    data-canpost="false"
                    data-cansee="true"
                    data-comments-unavailable="false"
                    data-addlink-disabled="true">

                        <li id="comment-101036785" class="comment js-comment " data-comment-id="101036785" data-comment-owner-id="7993009" data-comment-score="0">
        <div class="js-comment-actions comment-actions">
            <div class="comment-score js-comment-score js-comment-edit-hide">
            </div>
        </div>
        <div class="comment-text  js-comment-text-and-form">
            <div class="comment-body js-comment-edit-hide">
                
                <span class="comment-copy">cntrl + does not work in Mac. Can you please provide a solution for mac too ?</span>
                
                <div class="d-inline-flex ai-center">
&ndash;&nbsp;<a href="/users/7993009/dr-younes-henni"
                                title="1,663 reputation"
                                class="comment-user">Dr. Younes Henni</a>
                </div>
                <span class="comment-date" dir="ltr"><span title='2019-07-30 10:12:58Z, License: CC BY-SA 4.0' class='relativetime-clean'>Jul 30, 2019 at 10:12</span></span>
            </div>
        </div>
    </li>
    <li id="comment-113409273" class="comment js-comment " data-comment-id="113409273" data-comment-owner-id="4136722" data-comment-score="1">
        <div class="js-comment-actions comment-actions">
            <div class="comment-score js-comment-score js-comment-edit-hide">
                    <span title="number of &#x27;useful comment&#x27; votes received"
                            class="cool">1</span>
            </div>
        </div>
        <div class="comment-text  js-comment-text-and-form">
            <div class="comment-body js-comment-edit-hide">
                
                <span class="comment-copy">command + ,  works on mac.  This opens the settings in a GUI form.  Then just search for &#39;file associations&#39;. and enter the item as   <code>*.js</code>  and value as <code>javascriptreact</code>.   Note: you don&#39;t need the quotations when using the GUI.</span>
                
                <div class="d-inline-flex ai-center">
&ndash;&nbsp;<a href="/users/4136722/wide-awake"
                                title="1,429 reputation"
                                class="comment-user">Wide Awake</a>
                </div>
                <span class="comment-date" dir="ltr"><span title='2020-09-30 07:54:14Z, License: CC BY-SA 4.0' class='relativetime-clean'>Sep 30, 2020 at 7:54</span></span>
                        <span title="this comment was edited 2 times">
                            <svg aria-hidden="true" class="va-text-bottom o50 svg-icon iconPencilSm" width="14" height="14"  viewBox="0 0 14 14"><path fill="#F1B600" d="m2 10.12 6.37-6.43 1.88 1.88L3.88 12H2v-1.88Z"/><path fill="#E87C87" d="m11.1 1.71 1.13 1.12c.2.2.2.51 0 .71L11.1 4.7 9.21 2.86l1.17-1.15c.2-.2.51-.2.71 0Z"/></svg>
                        </span>
            </div>
        </div>
    </li>

            </ul>
	    </div>

        <div id="comments-link-51640555" data-rep=50 data-anon=true>
                    <a class="js-add-link comments-link disabled-link" title="Use comments to ask for more information or suggest improvements. Avoid comments like &#x201C;&#x2B;1&#x201D; or &#x201C;thanks&#x201D;."  href="#" role="button">Add a comment</a>
                <span class="js-link-separator dno">&nbsp;|&nbsp;</span>
            <a class="js-show-link comments-link dno" title="Expand to show all comments on this post" href=# onclick="" role="button"></a>
        </div>         
    </div>
    </div>
</div>

<div class="js-zone-container zone-container-main">
    <div id="dfp-mlb" class="everyonelovesstackoverflow everyoneloves__mid-leaderboard everyoneloves__leaderboard"></div>
		<div class="js-report-ad-button-container " style="width: 728px"></div>
</div>
                                    
<a name="45558336"></a>
<div id="answer-45558336" class="answer js-answer accepted-answer js-accepted-answer" data-answerid="45558336" data-parentid="45558193" data-score="48" data-position-on-page="2" data-highest-scored="0" data-question-has-accepted-highest-score="0"  itemprop="suggestedAnswer" itemscope itemtype="https://schema.org/Answer">
    <div class="post-layout">
        <div class="votecell post-layout--left">
            <div class="js-voting-container d-flex jc-center fd-column ai-stretch gs4 fc-black-300" data-post-id="45558336" data-referrer="None">
        <button class="js-vote-up-btn flex--item s-btn ba bar-pill c-pointer as-center bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050"
                data-controller="s-tooltip"
                data-s-tooltip-placement="right"
                title="This answer is useful"
                aria-pressed="false"
                aria-label="Up vote"
                data-selected-classes="fc-theme-primary bc-theme-primary bg-theme-primary-100"
                data-unselected-classes="bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050">
            <svg aria-hidden="true" class="svg-icon iconArrowUp" width="18" height="18"  viewBox="0 0 18 18"><path  d="M1 12h16L9 4l-8 8Z"/></svg>
        </button>
        <div class="js-vote-count flex--item d-flex fd-column ai-center fc-theme-body-font fw-bold fs-subheading py4"
             itemprop="upvoteCount"
             data-value="48">
            48
        </div>
        <button class="js-vote-down-btn flex--item mb8 s-btn ba bar-pill c-pointer as-center bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050"
                data-controller="s-tooltip"
                data-s-tooltip-placement="right"
                title="This answer is not useful"
                aria-pressed="false"
                aria-label="Down vote"
                data-selected-classes="fc-theme-primary bc-theme-primary bg-theme-primary-100"
                data-unselected-classes="bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050">
            <svg aria-hidden="true" class="svg-icon iconArrowDown" width="18" height="18"  viewBox="0 0 18 18"><path  d="M1 6h16l-8 8-8-8Z"/></svg>
        </button>


        
<button class="js-saves-btn s-btn s-btn__unset c-pointer py4"
        type="button"
        id="saves-btn-45558336"
        data-controller="s-tooltip"
        data-s-tooltip-placement="right"
        data-s-popover-placement=""
        title="Save this answer."
        aria-pressed="false"
        data-post-id="45558336"
        data-post-type-id="2"
        data-user-privilege-for-post-click="0"
        aria-controls=""
        data-s-popover-auto-show="false"
>
    <svg aria-hidden="true" class="fc-theme-primary-400 js-saves-btn-selected d-none svg-icon iconBookmark" width="18" height="18"  viewBox="0 0 18 18"><path  d="M3 17V3c0-1.1.9-2 2-2h8a2 2 0 0 1 2 2v14l-6-4-6 4Z"/></svg>
    <svg aria-hidden="true" class="js-saves-btn-unselected svg-icon iconBookmarkAlt" width="18" height="18"  viewBox="0 0 18 18"><path  d="m9 10.6 4 2.66V3H5v10.26l4-2.66ZM3 17V3c0-1.1.9-2 2-2h8a2 2 0 0 1 2 2v14l-6-4-6 4Z"/></svg>
</button>







            <div class="js-accepted-answer-indicator flex--item fc-green-400 py6 mtn8" data-s-tooltip-placement="right" title="Loading when this answer was accepted&#x2026;" tabindex="0" role="note" aria-label="Accepted">
                <div class="ta-center">
                    <svg aria-hidden="true" class="svg-icon iconCheckmarkLg" width="36" height="36"  viewBox="0 0 36 36"><path  d="m6 14 8 8L30 6v8L14 30l-8-8v-8Z"/></svg>
                </div>
            </div>

    
    <a class="js-post-issue flex--item s-btn s-btn__unset c-pointer py6 mx-auto" href="/posts/45558336/timeline" data-shortcut="T" data-ks-title="timeline" data-controller="s-tooltip" data-s-tooltip-placement="right" title="Show activity on this post." aria-label="Timeline"><svg aria-hidden="true" class="mln2 mr0 svg-icon iconHistory" width="19" height="18"  viewBox="0 0 19 18"><path  d="M3 9a8 8 0 1 1 3.73 6.77L8.2 14.3A6 6 0 1 0 5 9l3.01-.01-4 4-4-4h3L3 9Zm7-4h1.01L11 9.36l3.22 2.1-.6.93L10 10V5Z"/></svg></a>

</div>

        </div>

        

<div class="answercell post-layout--right">
    
    <div class="s-prose js-post-body" itemprop="text">
<p>Within VS Code, in the bottom right of the window you will see a smiley face - to the left of that is the language the currently visible file is associated with (e.g. JavaScript). Ensure your currently opened file is a .jsx file.</p>

<p>Clicking this will reveal a menu at the top. Click the <code>Configure File Association for '.jsx'...</code>, and then choose "JavaScript React".</p>
    </div>
    <div class="mt24">
        <div class="d-flex fw-wrap ai-start jc-end gs8 gsy">
            <time itemprop="dateCreated" datetime="2017-08-08T02:23:24"></time>
            <div class="flex--item mr16" style="flex: 1 1 100px;">
                


<div class="js-post-menu pt2" data-post-id="45558336" data-post-type-id="2">

    <div class="d-flex gs8 s-anchors s-anchors__muted fw-wrap">

        <div class="flex--item">
            <a href="/a/45558336"
               rel="nofollow"
               itemprop="url"
               class="js-share-link js-gps-track"
               title="Short permalink to this answer"
               data-gps-track="post.click({ item: 2, priv: 0, post_type: 2 })"
               data-controller="se-share-sheet"
               data-se-share-sheet-title="Share a link to this answer"
               data-se-share-sheet-subtitle=""
               data-se-share-sheet-post-type="answer"
               data-se-share-sheet-social="facebook twitter devto"
               data-se-share-sheet-location="2"
               data-se-share-sheet-license-url="https%3a%2f%2fcreativecommons.org%2flicenses%2fby-sa%2f3.0%2f"
               data-se-share-sheet-license-name="CC BY-SA 3.0"
               data-s-popover-placement="bottom-start">Share</a>
        </div>



                <div class="flex--item">
                    <button type="button"
                            id="btnFollowPost-45558336" class="s-btn s-btn__link js-follow-post js-follow-answer js-gps-track"
                            data-gps-track="post.click({ item: 14, priv: 0, post_type: 2 })"
                            data-controller="s-tooltip " data-s-tooltip-placement="bottom"
                            data-s-popover-placement="bottom" aria-controls=""
                            title="Follow this answer to receive notifications">
                        Follow
                    </button>
                </div>






    </div>
    <div class="js-menu-popup-container"></div>
</div>
            </div>


            <div class="post-signature flex--item fl0">
                <div class="user-info ">
    <div class="d-flex ">
        <div class="user-action-time fl-grow1">
            answered <span title='2017-08-08 02:23:24Z' class='relativetime'>Aug 8, 2017 at 2:23</span>
        </div>
        
    </div>
    <div class="user-gravatar32">
        <a href="/users/2253888/esr"><div class="gravatar-wrapper-32"><img src="https://www.gravatar.com/avatar/c3584277297d48dcfb1baace9ff9aadb?s=64&amp;d=identicon&amp;r=PG" alt="ESR&#39;s user avatar" width="32" height="32" class="bar-sm"></div></a>
    </div>
    <div class="user-details" itemprop="author" itemscope itemtype="http://schema.org/Person">
        <a href="/users/2253888/esr">ESR</a><span class="d-none" itemprop="name">ESR</span>
        <div class="-flair">
            <span class="reputation-score" title="reputation score " dir="ltr">1,669</span><span title="1 gold badge" aria-hidden="true"><span class="badge1"></span><span class="badgecount">1</span></span><span class="v-visible-sr">1 gold badge</span><span title="18 silver badges" aria-hidden="true"><span class="badge2"></span><span class="badgecount">18</span></span><span class="v-visible-sr">18 silver badges</span><span title="22 bronze badges" aria-hidden="true"><span class="badge3"></span><span class="badgecount">22</span></span><span class="v-visible-sr">22 bronze badges</span>
        </div>
    </div>
</div>


            </div>
        </div>
        
    
    </div>
    
</div>




            <span class="d-none" itemprop="commentCount">2</span> 
    <div class="post-layout--right js-post-comments-component">
        <div id="comments-45558336" class="comments js-comments-container bt bc-black-200 mt12 " data-post-id="45558336" data-min-length="15">
            <ul class="comments-list js-comments-list"
                    data-remaining-comments-count="0"
                    data-canpost="false"
                    data-cansee="true"
                    data-comments-unavailable="false"
                    data-addlink-disabled="true">

                        <li id="comment-78076831" class="comment js-comment " data-comment-id="78076831" data-comment-owner-id="2226315" data-comment-score="0">
        <div class="js-comment-actions comment-actions">
            <div class="comment-score js-comment-score js-comment-edit-hide">
            </div>
        </div>
        <div class="comment-text  js-comment-text-and-form">
            <div class="comment-body js-comment-edit-hide">
                
                <span class="comment-copy">Thanks, I should pay close attention to the dropdown list.</span>
                
                <div class="d-inline-flex ai-center">
&ndash;&nbsp;<a href="/users/2226315/xyz"
                                title="26,377 reputation"
                                class="comment-user owner">XYZ</a>
                </div>
                <span class="comment-date" dir="ltr"><span title='2017-08-08 03:01:38Z, License: CC BY-SA 3.0' class='relativetime-clean'>Aug 8, 2017 at 3:01</span></span>
            </div>
        </div>
    </li>
    <li id="comment-118101978" class="comment js-comment " data-comment-id="118101978" data-comment-owner-id="9108471" data-comment-score="3">
        <div class="js-comment-actions comment-actions">
            <div class="comment-score js-comment-score js-comment-edit-hide">
                    <span title="number of &#x27;useful comment&#x27; votes received"
                            class="cool">3</span>
            </div>
        </div>
        <div class="comment-text  js-comment-text-and-form">
            <div class="comment-body js-comment-edit-hide">
                
                <span class="comment-copy">Can we make some settings in VS code to take all <code>.js</code> files as <code>.jsx</code> files? I mean I don&#39;t want to click in the bottom right every time to change <code>.js</code> to<code>.jsx</code>!</span>
                
                <div class="d-inline-flex ai-center">
&ndash;&nbsp;<a href="/users/9108471/zeeshan-ahmad-khalil"
                                title="853 reputation"
                                class="comment-user">Zeeshan Ahmad Khalil</a>
                </div>
                <span class="comment-date" dir="ltr"><span title='2021-03-26 06:05:58Z, License: CC BY-SA 4.0' class='relativetime-clean'>Mar 26, 2021 at 6:05</span></span>
            </div>
        </div>
    </li>

            </ul>
	    </div>

        <div id="comments-link-45558336" data-rep=50 data-anon=true>
                    <a class="js-add-link comments-link disabled-link" title="Use comments to ask for more information or suggest improvements. Avoid comments like &#x201C;&#x2B;1&#x201D; or &#x201C;thanks&#x201D;."  href="#" role="button">Add a comment</a>
                <span class="js-link-separator dno">&nbsp;|&nbsp;</span>
            <a class="js-show-link comments-link dno" title="Expand to show all comments on this post" href=# onclick="" role="button"></a>
        </div>         
    </div>
    </div>
</div>

                                    
<a name="45558597"></a>
<div id="answer-45558597" class="answer js-answer" data-answerid="45558597" data-parentid="45558193" data-score="13" data-position-on-page="3" data-highest-scored="0" data-question-has-accepted-highest-score="0"  itemprop="suggestedAnswer" itemscope itemtype="https://schema.org/Answer">
    <div class="post-layout">
        <div class="votecell post-layout--left">
            <div class="js-voting-container d-flex jc-center fd-column ai-stretch gs4 fc-black-300" data-post-id="45558597" data-referrer="None">
        <button class="js-vote-up-btn flex--item s-btn ba bar-pill c-pointer as-center bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050"
                data-controller="s-tooltip"
                data-s-tooltip-placement="right"
                title="This answer is useful"
                aria-pressed="false"
                aria-label="Up vote"
                data-selected-classes="fc-theme-primary bc-theme-primary bg-theme-primary-100"
                data-unselected-classes="bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050">
            <svg aria-hidden="true" class="svg-icon iconArrowUp" width="18" height="18"  viewBox="0 0 18 18"><path  d="M1 12h16L9 4l-8 8Z"/></svg>
        </button>
        <div class="js-vote-count flex--item d-flex fd-column ai-center fc-theme-body-font fw-bold fs-subheading py4"
             itemprop="upvoteCount"
             data-value="13">
            13
        </div>
        <button class="js-vote-down-btn flex--item mb8 s-btn ba bar-pill c-pointer as-center bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050"
                data-controller="s-tooltip"
                data-s-tooltip-placement="right"
                title="This answer is not useful"
                aria-pressed="false"
                aria-label="Down vote"
                data-selected-classes="fc-theme-primary bc-theme-primary bg-theme-primary-100"
                data-unselected-classes="bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050">
            <svg aria-hidden="true" class="svg-icon iconArrowDown" width="18" height="18"  viewBox="0 0 18 18"><path  d="M1 6h16l-8 8-8-8Z"/></svg>
        </button>


        
<button class="js-saves-btn s-btn s-btn__unset c-pointer py4"
        type="button"
        id="saves-btn-45558597"
        data-controller="s-tooltip"
        data-s-tooltip-placement="right"
        data-s-popover-placement=""
        title="Save this answer."
        aria-pressed="false"
        data-post-id="45558597"
        data-post-type-id="2"
        data-user-privilege-for-post-click="0"
        aria-controls=""
        data-s-popover-auto-show="false"
>
    <svg aria-hidden="true" class="fc-theme-primary-400 js-saves-btn-selected d-none svg-icon iconBookmark" width="18" height="18"  viewBox="0 0 18 18"><path  d="M3 17V3c0-1.1.9-2 2-2h8a2 2 0 0 1 2 2v14l-6-4-6 4Z"/></svg>
    <svg aria-hidden="true" class="js-saves-btn-unselected svg-icon iconBookmarkAlt" width="18" height="18"  viewBox="0 0 18 18"><path  d="m9 10.6 4 2.66V3H5v10.26l4-2.66ZM3 17V3c0-1.1.9-2 2-2h8a2 2 0 0 1 2 2v14l-6-4-6 4Z"/></svg>
</button>







            <div class="js-accepted-answer-indicator flex--item fc-green-400 py6 mtn8 d-none" data-s-tooltip-placement="right" title="Loading when this answer was accepted&#x2026;" tabindex="0" role="note" aria-label="Accepted">
                <div class="ta-center">
                    <svg aria-hidden="true" class="svg-icon iconCheckmarkLg" width="36" height="36"  viewBox="0 0 36 36"><path  d="m6 14 8 8L30 6v8L14 30l-8-8v-8Z"/></svg>
                </div>
            </div>

    
    <a class="js-post-issue flex--item s-btn s-btn__unset c-pointer py6 mx-auto" href="/posts/45558597/timeline" data-shortcut="T" data-ks-title="timeline" data-controller="s-tooltip" data-s-tooltip-placement="right" title="Show activity on this post." aria-label="Timeline"><svg aria-hidden="true" class="mln2 mr0 svg-icon iconHistory" width="19" height="18"  viewBox="0 0 19 18"><path  d="M3 9a8 8 0 1 1 3.73 6.77L8.2 14.3A6 6 0 1 0 5 9l3.01-.01-4 4-4-4h3L3 9Zm7-4h1.01L11 9.36l3.22 2.1-.6.93L10 10V5Z"/></svg></a>

</div>

        </div>

        

<div class="answercell post-layout--right">
    
    <div class="s-prose js-post-body" itemprop="text">
<p>The setting goes to config file should be something like below,</p>

<pre><code>"files.associations": {
    "*.jsx": "javascriptreact"
}
</code></pre>
    </div>
    <div class="mt24">
        <div class="d-flex fw-wrap ai-start jc-end gs8 gsy">
            <time itemprop="dateCreated" datetime="2017-08-08T03:00:39"></time>
            <div class="flex--item mr16" style="flex: 1 1 100px;">
                


<div class="js-post-menu pt2" data-post-id="45558597" data-post-type-id="2">

    <div class="d-flex gs8 s-anchors s-anchors__muted fw-wrap">

        <div class="flex--item">
            <a href="/a/45558597"
               rel="nofollow"
               itemprop="url"
               class="js-share-link js-gps-track"
               title="Short permalink to this answer"
               data-gps-track="post.click({ item: 2, priv: 0, post_type: 2 })"
               data-controller="se-share-sheet"
               data-se-share-sheet-title="Share a link to this answer"
               data-se-share-sheet-subtitle=""
               data-se-share-sheet-post-type="answer"
               data-se-share-sheet-social="facebook twitter devto"
               data-se-share-sheet-location="2"
               data-se-share-sheet-license-url="https%3a%2f%2fcreativecommons.org%2flicenses%2fby-sa%2f3.0%2f"
               data-se-share-sheet-license-name="CC BY-SA 3.0"
               data-s-popover-placement="bottom-start">Share</a>
        </div>



                <div class="flex--item">
                    <button type="button"
                            id="btnFollowPost-45558597" class="s-btn s-btn__link js-follow-post js-follow-answer js-gps-track"
                            data-gps-track="post.click({ item: 14, priv: 0, post_type: 2 })"
                            data-controller="s-tooltip " data-s-tooltip-placement="bottom"
                            data-s-popover-placement="bottom" aria-controls=""
                            title="Follow this answer to receive notifications">
                        Follow
                    </button>
                </div>






    </div>
    <div class="js-menu-popup-container"></div>
</div>
            </div>


            <div class="post-signature owner flex--item fl0">
                <div class="user-info ">
    <div class="d-flex ">
        <div class="user-action-time fl-grow1">
            answered <span title='2017-08-08 03:00:39Z' class='relativetime'>Aug 8, 2017 at 3:00</span>
        </div>
        
    </div>
    <div class="user-gravatar32">
        <a href="/users/2226315/xyz"><div class="gravatar-wrapper-32"><img src="https://www.gravatar.com/avatar/becc42cd815d380c8d12394adf77f4fa?s=64&amp;d=identicon&amp;r=PG&amp;f=y&amp;so-version=2" alt="XYZ&#39;s user avatar" width="32" height="32" class="bar-sm"></div></a>
    </div>
    <div class="user-details" itemprop="author" itemscope itemtype="http://schema.org/Person">
        <a href="/users/2226315/xyz">XYZ</a><span class="d-none" itemprop="name">XYZ</span>
        <div class="-flair">
            <span class="reputation-score" title="reputation score 26,377" dir="ltr">26.4k</span><span title="15 gold badges" aria-hidden="true"><span class="badge1"></span><span class="badgecount">15</span></span><span class="v-visible-sr">15 gold badges</span><span title="87 silver badges" aria-hidden="true"><span class="badge2"></span><span class="badgecount">87</span></span><span class="v-visible-sr">87 silver badges</span><span title="153 bronze badges" aria-hidden="true"><span class="badge3"></span><span class="badgecount">153</span></span><span class="v-visible-sr">153 bronze badges</span>
        </div>
    </div>
</div>


            </div>
        </div>
        
    
    </div>
    
</div>




            <span class="d-none" itemprop="commentCount">0</span> 
    <div class="post-layout--right js-post-comments-component">
        <div id="comments-45558597" class="comments js-comments-container bt bc-black-200 mt12  dno" data-post-id="45558597" data-min-length="15">
            <ul class="comments-list js-comments-list"
                    data-remaining-comments-count="0"
                    data-canpost="false"
                    data-cansee="true"
                    data-comments-unavailable="false"
                    data-addlink-disabled="true">

            </ul>
	    </div>

        <div id="comments-link-45558597" data-rep=50 data-anon=true>
                    <a class="js-add-link comments-link disabled-link" title="Use comments to ask for more information or suggest improvements. Avoid comments like &#x201C;&#x2B;1&#x201D; or &#x201C;thanks&#x201D;."  href="#" role="button">Add a comment</a>
                <span class="js-link-separator dno">&nbsp;|&nbsp;</span>
            <a class="js-show-link comments-link dno" title="Expand to show all comments on this post" href=# onclick="" role="button"></a>
        </div>         
    </div>
    </div>
</div>

<div class="js-zone-container zone-container-main">
    <div id="dfp-smlb" class="everyonelovesstackoverflow everyoneloves__mid-second-leaderboard everyoneloves__leaderboard"></div>
		<div class="js-report-ad-button-container " style="width: 728px"></div>
</div>
                                    
<a name="69104459"></a>
<div id="answer-69104459" class="answer js-answer" data-answerid="69104459" data-parentid="45558193" data-score="6" data-position-on-page="4" data-highest-scored="0" data-question-has-accepted-highest-score="0"  itemprop="suggestedAnswer" itemscope itemtype="https://schema.org/Answer">
    <div class="post-layout">
        <div class="votecell post-layout--left">
            <div class="js-voting-container d-flex jc-center fd-column ai-stretch gs4 fc-black-300" data-post-id="69104459" data-referrer="None">
        <button class="js-vote-up-btn flex--item s-btn ba bar-pill c-pointer as-center bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050"
                data-controller="s-tooltip"
                data-s-tooltip-placement="right"
                title="This answer is useful"
                aria-pressed="false"
                aria-label="Up vote"
                data-selected-classes="fc-theme-primary bc-theme-primary bg-theme-primary-100"
                data-unselected-classes="bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050">
            <svg aria-hidden="true" class="svg-icon iconArrowUp" width="18" height="18"  viewBox="0 0 18 18"><path  d="M1 12h16L9 4l-8 8Z"/></svg>
        </button>
        <div class="js-vote-count flex--item d-flex fd-column ai-center fc-theme-body-font fw-bold fs-subheading py4"
             itemprop="upvoteCount"
             data-value="6">
            6
        </div>
        <button class="js-vote-down-btn flex--item mb8 s-btn ba bar-pill c-pointer as-center bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050"
                data-controller="s-tooltip"
                data-s-tooltip-placement="right"
                title="This answer is not useful"
                aria-pressed="false"
                aria-label="Down vote"
                data-selected-classes="fc-theme-primary bc-theme-primary bg-theme-primary-100"
                data-unselected-classes="bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050">
            <svg aria-hidden="true" class="svg-icon iconArrowDown" width="18" height="18"  viewBox="0 0 18 18"><path  d="M1 6h16l-8 8-8-8Z"/></svg>
        </button>


        
<button class="js-saves-btn s-btn s-btn__unset c-pointer py4"
        type="button"
        id="saves-btn-69104459"
        data-controller="s-tooltip"
        data-s-tooltip-placement="right"
        data-s-popover-placement=""
        title="Save this answer."
        aria-pressed="false"
        data-post-id="69104459"
        data-post-type-id="2"
        data-user-privilege-for-post-click="0"
        aria-controls=""
        data-s-popover-auto-show="false"
>
    <svg aria-hidden="true" class="fc-theme-primary-400 js-saves-btn-selected d-none svg-icon iconBookmark" width="18" height="18"  viewBox="0 0 18 18"><path  d="M3 17V3c0-1.1.9-2 2-2h8a2 2 0 0 1 2 2v14l-6-4-6 4Z"/></svg>
    <svg aria-hidden="true" class="js-saves-btn-unselected svg-icon iconBookmarkAlt" width="18" height="18"  viewBox="0 0 18 18"><path  d="m9 10.6 4 2.66V3H5v10.26l4-2.66ZM3 17V3c0-1.1.9-2 2-2h8a2 2 0 0 1 2 2v14l-6-4-6 4Z"/></svg>
</button>







            <div class="js-accepted-answer-indicator flex--item fc-green-400 py6 mtn8 d-none" data-s-tooltip-placement="right" title="Loading when this answer was accepted&#x2026;" tabindex="0" role="note" aria-label="Accepted">
                <div class="ta-center">
                    <svg aria-hidden="true" class="svg-icon iconCheckmarkLg" width="36" height="36"  viewBox="0 0 36 36"><path  d="m6 14 8 8L30 6v8L14 30l-8-8v-8Z"/></svg>
                </div>
            </div>

    
    <a class="js-post-issue flex--item s-btn s-btn__unset c-pointer py6 mx-auto" href="/posts/69104459/timeline" data-shortcut="T" data-ks-title="timeline" data-controller="s-tooltip" data-s-tooltip-placement="right" title="Show activity on this post." aria-label="Timeline"><svg aria-hidden="true" class="mln2 mr0 svg-icon iconHistory" width="19" height="18"  viewBox="0 0 19 18"><path  d="M3 9a8 8 0 1 1 3.73 6.77L8.2 14.3A6 6 0 1 0 5 9l3.01-.01-4 4-4-4h3L3 9Zm7-4h1.01L11 9.36l3.22 2.1-.6.93L10 10V5Z"/></svg></a>

</div>

        </div>

        

<div class="answercell post-layout--right">
    
    <div class="s-prose js-post-body" itemprop="text">
<p>why not like this ?</p>
<p>Open your <code>setting.json</code> in vscode and add or edit <code>files.associations</code> like below:</p>
<pre><code>&quot;files.associations&quot;: {
    &quot;**/src/**/*.js&quot;: &quot;javascriptreact&quot;,
    &quot;**/src/**/*.jsx&quot;: &quot;javascriptreact&quot;
}
</code></pre>
<p>I wounder to know why no one shared this on internet (at least I haven't found).</p>
<p>This code will auto detect your react files since we wrote path for it, it will find <code>src</code> folder of your react app and detect all of the <code>.js, .jsx</code> files as react and you can still have your <code>.js</code> files out of <code>src</code> folder like <code>public</code> or anywhere else you like.</p>
<p>I have to let you know, in your non react project all of <code>.js, .jsx</code> files inside of <code>src</code> folder gonna detect as react file as well because we wrote the setting like this and I don't know any better way.</p>
    </div>
    <div class="mt24">
        <div class="d-flex fw-wrap ai-start jc-end gs8 gsy">
            <time itemprop="dateCreated" datetime="2021-09-08T13:57:23"></time>
            <div class="flex--item mr16" style="flex: 1 1 100px;">
                


<div class="js-post-menu pt2" data-post-id="69104459" data-post-type-id="2">

    <div class="d-flex gs8 s-anchors s-anchors__muted fw-wrap">

        <div class="flex--item">
            <a href="/a/69104459"
               rel="nofollow"
               itemprop="url"
               class="js-share-link js-gps-track"
               title="Short permalink to this answer"
               data-gps-track="post.click({ item: 2, priv: 0, post_type: 2 })"
               data-controller="se-share-sheet"
               data-se-share-sheet-title="Share a link to this answer"
               data-se-share-sheet-subtitle=""
               data-se-share-sheet-post-type="answer"
               data-se-share-sheet-social="facebook twitter devto"
               data-se-share-sheet-location="2"
               data-se-share-sheet-license-url="https%3a%2f%2fcreativecommons.org%2flicenses%2fby-sa%2f4.0%2f"
               data-se-share-sheet-license-name="CC BY-SA 4.0"
               data-s-popover-placement="bottom-start">Share</a>
        </div>



                <div class="flex--item">
                    <button type="button"
                            id="btnFollowPost-69104459" class="s-btn s-btn__link js-follow-post js-follow-answer js-gps-track"
                            data-gps-track="post.click({ item: 14, priv: 0, post_type: 2 })"
                            data-controller="s-tooltip " data-s-tooltip-placement="bottom"
                            data-s-popover-placement="bottom" aria-controls=""
                            title="Follow this answer to receive notifications">
                        Follow
                    </button>
                </div>






    </div>
    <div class="js-menu-popup-container"></div>
</div>
            </div>


            <div class="post-signature flex--item fl0">
                <div class="user-info ">
    <div class="d-flex ">
        <div class="user-action-time fl-grow1">
            answered <span title='2021-09-08 13:57:23Z' class='relativetime'>Sep 8, 2021 at 13:57</span>
        </div>
        
    </div>
    <div class="user-gravatar32">
        <span class="anonymous-gravatar"></span>
    </div>
    <div class="user-details" itemprop="author" itemscope itemtype="http://schema.org/Person">
        user12914956<span class="d-none" itemprop="name">user12914956</span>
        <div class="-flair">
            
        </div>
    </div>
</div>


            </div>
        </div>
        
    
    </div>
    
</div>




            <span class="d-none" itemprop="commentCount"></span> 
    <div class="post-layout--right js-post-comments-component">
        <div id="comments-69104459" class="comments js-comments-container bt bc-black-200 mt12  dno" data-post-id="69104459" data-min-length="15">
            <ul class="comments-list js-comments-list"
                    data-remaining-comments-count="0"
                    data-canpost="false"
                    data-cansee="true"
                    data-comments-unavailable="false"
                    data-addlink-disabled="true">

            </ul>
	    </div>

        <div id="comments-link-69104459" data-rep=50 data-anon=true>
                    <a class="js-add-link comments-link disabled-link" title="Use comments to ask for more information or suggest improvements. Avoid comments like &#x201C;&#x2B;1&#x201D; or &#x201C;thanks&#x201D;."  href="#" role="button">Add a comment</a>
                <span class="js-link-separator dno">&nbsp;|&nbsp;</span>
            <a class="js-show-link comments-link dno" title="Expand to show all comments on this post" href=# onclick="" role="button"></a>
        </div>         
    </div>
    </div>
</div>

                                    
<a name="49491349"></a>
<div id="answer-49491349" class="answer js-answer" data-answerid="49491349" data-parentid="45558193" data-score="5" data-position-on-page="5" data-highest-scored="0" data-question-has-accepted-highest-score="0"  itemprop="suggestedAnswer" itemscope itemtype="https://schema.org/Answer">
    <div class="post-layout">
        <div class="votecell post-layout--left">
            <div class="js-voting-container d-flex jc-center fd-column ai-stretch gs4 fc-black-300" data-post-id="49491349" data-referrer="None">
        <button class="js-vote-up-btn flex--item s-btn ba bar-pill c-pointer as-center bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050"
                data-controller="s-tooltip"
                data-s-tooltip-placement="right"
                title="This answer is useful"
                aria-pressed="false"
                aria-label="Up vote"
                data-selected-classes="fc-theme-primary bc-theme-primary bg-theme-primary-100"
                data-unselected-classes="bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050">
            <svg aria-hidden="true" class="svg-icon iconArrowUp" width="18" height="18"  viewBox="0 0 18 18"><path  d="M1 12h16L9 4l-8 8Z"/></svg>
        </button>
        <div class="js-vote-count flex--item d-flex fd-column ai-center fc-theme-body-font fw-bold fs-subheading py4"
             itemprop="upvoteCount"
             data-value="5">
            5
        </div>
        <button class="js-vote-down-btn flex--item mb8 s-btn ba bar-pill c-pointer as-center bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050"
                data-controller="s-tooltip"
                data-s-tooltip-placement="right"
                title="This answer is not useful"
                aria-pressed="false"
                aria-label="Down vote"
                data-selected-classes="fc-theme-primary bc-theme-primary bg-theme-primary-100"
                data-unselected-classes="bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050">
            <svg aria-hidden="true" class="svg-icon iconArrowDown" width="18" height="18"  viewBox="0 0 18 18"><path  d="M1 6h16l-8 8-8-8Z"/></svg>
        </button>


        
<button class="js-saves-btn s-btn s-btn__unset c-pointer py4"
        type="button"
        id="saves-btn-49491349"
        data-controller="s-tooltip"
        data-s-tooltip-placement="right"
        data-s-popover-placement=""
        title="Save this answer."
        aria-pressed="false"
        data-post-id="49491349"
        data-post-type-id="2"
        data-user-privilege-for-post-click="0"
        aria-controls=""
        data-s-popover-auto-show="false"
>
    <svg aria-hidden="true" class="fc-theme-primary-400 js-saves-btn-selected d-none svg-icon iconBookmark" width="18" height="18"  viewBox="0 0 18 18"><path  d="M3 17V3c0-1.1.9-2 2-2h8a2 2 0 0 1 2 2v14l-6-4-6 4Z"/></svg>
    <svg aria-hidden="true" class="js-saves-btn-unselected svg-icon iconBookmarkAlt" width="18" height="18"  viewBox="0 0 18 18"><path  d="m9 10.6 4 2.66V3H5v10.26l4-2.66ZM3 17V3c0-1.1.9-2 2-2h8a2 2 0 0 1 2 2v14l-6-4-6 4Z"/></svg>
</button>







            <div class="js-accepted-answer-indicator flex--item fc-green-400 py6 mtn8 d-none" data-s-tooltip-placement="right" title="Loading when this answer was accepted&#x2026;" tabindex="0" role="note" aria-label="Accepted">
                <div class="ta-center">
                    <svg aria-hidden="true" class="svg-icon iconCheckmarkLg" width="36" height="36"  viewBox="0 0 36 36"><path  d="m6 14 8 8L30 6v8L14 30l-8-8v-8Z"/></svg>
                </div>
            </div>

    
    <a class="js-post-issue flex--item s-btn s-btn__unset c-pointer py6 mx-auto" href="/posts/49491349/timeline" data-shortcut="T" data-ks-title="timeline" data-controller="s-tooltip" data-s-tooltip-placement="right" title="Show activity on this post." aria-label="Timeline"><svg aria-hidden="true" class="mln2 mr0 svg-icon iconHistory" width="19" height="18"  viewBox="0 0 19 18"><path  d="M3 9a8 8 0 1 1 3.73 6.77L8.2 14.3A6 6 0 1 0 5 9l3.01-.01-4 4-4-4h3L3 9Zm7-4h1.01L11 9.36l3.22 2.1-.6.93L10 10V5Z"/></svg></a>

</div>

        </div>

        

<div class="answercell post-layout--right">
    
    <div class="s-prose js-post-body" itemprop="text">
<p>Instead of the config file, I do click on the extensions in the bottom right of the window. Then click on <code>configure File Association for '.jsx'</code> and he asked me to select a language and I selected Javascript React. and now all the files are Javascript React with a little react icon</p>
    </div>
    <div class="mt24">
        <div class="d-flex fw-wrap ai-start jc-end gs8 gsy">
            <time itemprop="dateCreated" datetime="2018-03-26T12:19:42"></time>
            <div class="flex--item mr16" style="flex: 1 1 100px;">
                


<div class="js-post-menu pt2" data-post-id="49491349" data-post-type-id="2">

    <div class="d-flex gs8 s-anchors s-anchors__muted fw-wrap">

        <div class="flex--item">
            <a href="/a/49491349"
               rel="nofollow"
               itemprop="url"
               class="js-share-link js-gps-track"
               title="Short permalink to this answer"
               data-gps-track="post.click({ item: 2, priv: 0, post_type: 2 })"
               data-controller="se-share-sheet"
               data-se-share-sheet-title="Share a link to this answer"
               data-se-share-sheet-subtitle=""
               data-se-share-sheet-post-type="answer"
               data-se-share-sheet-social="facebook twitter devto"
               data-se-share-sheet-location="2"
               data-se-share-sheet-license-url="https%3a%2f%2fcreativecommons.org%2flicenses%2fby-sa%2f3.0%2f"
               data-se-share-sheet-license-name="CC BY-SA 3.0"
               data-s-popover-placement="bottom-start">Share</a>
        </div>



                <div class="flex--item">
                    <button type="button"
                            id="btnFollowPost-49491349" class="s-btn s-btn__link js-follow-post js-follow-answer js-gps-track"
                            data-gps-track="post.click({ item: 14, priv: 0, post_type: 2 })"
                            data-controller="s-tooltip " data-s-tooltip-placement="bottom"
                            data-s-popover-placement="bottom" aria-controls=""
                            title="Follow this answer to receive notifications">
                        Follow
                    </button>
                </div>






    </div>
    <div class="js-menu-popup-container"></div>
</div>
            </div>


            <div class="post-signature flex--item fl0">
                <div class="user-info user-hover ">
    <div class="d-flex ">
        <div class="user-action-time fl-grow1">
            answered <span title='2018-03-26 12:19:42Z' class='relativetime'>Mar 26, 2018 at 12:19</span>
        </div>
        
    </div>
    <div class="user-gravatar32">
        <a href="/users/6318732/mohsin-latif"><div class="gravatar-wrapper-32"><img src="https://i.stack.imgur.com/wASRE.jpg?s=64&amp;g=1" alt="Mohsin Latif&#39;s user avatar" width="32" height="32" class="bar-sm"></div></a>
    </div>
    <div class="user-details" itemprop="author" itemscope itemtype="http://schema.org/Person">
        <a href="/users/6318732/mohsin-latif">Mohsin Latif</a><span class="d-none" itemprop="name">Mohsin Latif</span>
        <div class="-flair">
            <span class="reputation-score" title="reputation score " dir="ltr">495</span><span title="6 silver badges" aria-hidden="true"><span class="badge2"></span><span class="badgecount">6</span></span><span class="v-visible-sr">6 silver badges</span><span title="7 bronze badges" aria-hidden="true"><span class="badge3"></span><span class="badgecount">7</span></span><span class="v-visible-sr">7 bronze badges</span>
        </div>
    </div>
</div>


            </div>
        </div>
        
    
    </div>
    
</div>




            <span class="d-none" itemprop="commentCount"></span> 
    <div class="post-layout--right js-post-comments-component">
        <div id="comments-49491349" class="comments js-comments-container bt bc-black-200 mt12  dno" data-post-id="49491349" data-min-length="15">
            <ul class="comments-list js-comments-list"
                    data-remaining-comments-count="0"
                    data-canpost="false"
                    data-cansee="true"
                    data-comments-unavailable="false"
                    data-addlink-disabled="true">

            </ul>
	    </div>

        <div id="comments-link-49491349" data-rep=50 data-anon=true>
                    <a class="js-add-link comments-link disabled-link" title="Use comments to ask for more information or suggest improvements. Avoid comments like &#x201C;&#x2B;1&#x201D; or &#x201C;thanks&#x201D;."  href="#" role="button">Add a comment</a>
                <span class="js-link-separator dno">&nbsp;|&nbsp;</span>
            <a class="js-show-link comments-link dno" title="Expand to show all comments on this post" href=# onclick="" role="button"></a>
        </div>         
    </div>
    </div>
</div>

                                    
<a name="64178428"></a>
<div id="answer-64178428" class="answer js-answer" data-answerid="64178428" data-parentid="45558193" data-score="0" data-position-on-page="6" data-highest-scored="0" data-question-has-accepted-highest-score="0"  itemprop="suggestedAnswer" itemscope itemtype="https://schema.org/Answer">
    <div class="post-layout">
        <div class="votecell post-layout--left">
            <div class="js-voting-container d-flex jc-center fd-column ai-stretch gs4 fc-black-300" data-post-id="64178428" data-referrer="None">
        <button class="js-vote-up-btn flex--item s-btn ba bar-pill c-pointer as-center bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050"
                data-controller="s-tooltip"
                data-s-tooltip-placement="right"
                title="This answer is useful"
                aria-pressed="false"
                aria-label="Up vote"
                data-selected-classes="fc-theme-primary bc-theme-primary bg-theme-primary-100"
                data-unselected-classes="bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050">
            <svg aria-hidden="true" class="svg-icon iconArrowUp" width="18" height="18"  viewBox="0 0 18 18"><path  d="M1 12h16L9 4l-8 8Z"/></svg>
        </button>
        <div class="js-vote-count flex--item d-flex fd-column ai-center fc-theme-body-font fw-bold fs-subheading py4"
             itemprop="upvoteCount"
             data-value="0">
            0
        </div>
        <button class="js-vote-down-btn flex--item mb8 s-btn ba bar-pill c-pointer as-center bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050"
                data-controller="s-tooltip"
                data-s-tooltip-placement="right"
                title="This answer is not useful"
                aria-pressed="false"
                aria-label="Down vote"
                data-selected-classes="fc-theme-primary bc-theme-primary bg-theme-primary-100"
                data-unselected-classes="bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050">
            <svg aria-hidden="true" class="svg-icon iconArrowDown" width="18" height="18"  viewBox="0 0 18 18"><path  d="M1 6h16l-8 8-8-8Z"/></svg>
        </button>


        
<button class="js-saves-btn s-btn s-btn__unset c-pointer py4"
        type="button"
        id="saves-btn-64178428"
        data-controller="s-tooltip"
        data-s-tooltip-placement="right"
        data-s-popover-placement=""
        title="Save this answer."
        aria-pressed="false"
        data-post-id="64178428"
        data-post-type-id="2"
        data-user-privilege-for-post-click="0"
        aria-controls=""
        data-s-popover-auto-show="false"
>
    <svg aria-hidden="true" class="fc-theme-primary-400 js-saves-btn-selected d-none svg-icon iconBookmark" width="18" height="18"  viewBox="0 0 18 18"><path  d="M3 17V3c0-1.1.9-2 2-2h8a2 2 0 0 1 2 2v14l-6-4-6 4Z"/></svg>
    <svg aria-hidden="true" class="js-saves-btn-unselected svg-icon iconBookmarkAlt" width="18" height="18"  viewBox="0 0 18 18"><path  d="m9 10.6 4 2.66V3H5v10.26l4-2.66ZM3 17V3c0-1.1.9-2 2-2h8a2 2 0 0 1 2 2v14l-6-4-6 4Z"/></svg>
</button>







            <div class="js-accepted-answer-indicator flex--item fc-green-400 py6 mtn8 d-none" data-s-tooltip-placement="right" title="Loading when this answer was accepted&#x2026;" tabindex="0" role="note" aria-label="Accepted">
                <div class="ta-center">
                    <svg aria-hidden="true" class="svg-icon iconCheckmarkLg" width="36" height="36"  viewBox="0 0 36 36"><path  d="m6 14 8 8L30 6v8L14 30l-8-8v-8Z"/></svg>
                </div>
            </div>

    
    <a class="js-post-issue flex--item s-btn s-btn__unset c-pointer py6 mx-auto" href="/posts/64178428/timeline" data-shortcut="T" data-ks-title="timeline" data-controller="s-tooltip" data-s-tooltip-placement="right" title="Show activity on this post." aria-label="Timeline"><svg aria-hidden="true" class="mln2 mr0 svg-icon iconHistory" width="19" height="18"  viewBox="0 0 19 18"><path  d="M3 9a8 8 0 1 1 3.73 6.77L8.2 14.3A6 6 0 1 0 5 9l3.01-.01-4 4-4-4h3L3 9Zm7-4h1.01L11 9.36l3.22 2.1-.6.93L10 10V5Z"/></svg></a>

</div>

        </div>

        

<div class="answercell post-layout--right">
    
    <div class="s-prose js-post-body" itemprop="text">
<p>press ctrl+shift+P and then go to user settings, after that, Search for file.associations
and then click add item. In the key input-field enter *.js and in the value input-field enter javascriptreact</p>
    </div>
    <div class="mt24">
        <div class="d-flex fw-wrap ai-start jc-end gs8 gsy">
            <time itemprop="dateCreated" datetime="2020-10-02T21:25:43"></time>
            <div class="flex--item mr16" style="flex: 1 1 100px;">
                


<div class="js-post-menu pt2" data-post-id="64178428" data-post-type-id="2">

    <div class="d-flex gs8 s-anchors s-anchors__muted fw-wrap">

        <div class="flex--item">
            <a href="/a/64178428"
               rel="nofollow"
               itemprop="url"
               class="js-share-link js-gps-track"
               title="Short permalink to this answer"
               data-gps-track="post.click({ item: 2, priv: 0, post_type: 2 })"
               data-controller="se-share-sheet"
               data-se-share-sheet-title="Share a link to this answer"
               data-se-share-sheet-subtitle=""
               data-se-share-sheet-post-type="answer"
               data-se-share-sheet-social="facebook twitter devto"
               data-se-share-sheet-location="2"
               data-se-share-sheet-license-url="https%3a%2f%2fcreativecommons.org%2flicenses%2fby-sa%2f4.0%2f"
               data-se-share-sheet-license-name="CC BY-SA 4.0"
               data-s-popover-placement="bottom-start">Share</a>
        </div>



                <div class="flex--item">
                    <button type="button"
                            id="btnFollowPost-64178428" class="s-btn s-btn__link js-follow-post js-follow-answer js-gps-track"
                            data-gps-track="post.click({ item: 14, priv: 0, post_type: 2 })"
                            data-controller="s-tooltip " data-s-tooltip-placement="bottom"
                            data-s-popover-placement="bottom" aria-controls=""
                            title="Follow this answer to receive notifications">
                        Follow
                    </button>
                </div>






    </div>
    <div class="js-menu-popup-container"></div>
</div>
            </div>


            <div class="post-signature flex--item fl0">
                <div class="user-info ">
    <div class="d-flex ">
        <div class="user-action-time fl-grow1">
            answered <span title='2020-10-02 21:25:43Z' class='relativetime'>Oct 2, 2020 at 21:25</span>
        </div>
        
    </div>
    <div class="user-gravatar32">
        <a href="/users/14382181/epic-gamedev"><div class="gravatar-wrapper-32"><img src="https://lh3.googleusercontent.com/a-/AOh14GiAtnziKx8E1NvuVpb0c-_tROgeTNFFrh5g5nDi=k-s64" alt="Epic gameDev&#39;s user avatar" width="32" height="32" class="bar-sm"></div></a>
    </div>
    <div class="user-details" itemprop="author" itemscope itemtype="http://schema.org/Person">
        <a href="/users/14382181/epic-gamedev">Epic gameDev</a><span class="d-none" itemprop="name">Epic gameDev</span>
        <div class="-flair">
            <span class="reputation-score" title="reputation score " dir="ltr">1</span>
        </div>
    </div>
</div>


            </div>
        </div>
        
    
    </div>
    
</div>




            <span class="d-none" itemprop="commentCount"></span> 
    <div class="post-layout--right js-post-comments-component">
        <div id="comments-64178428" class="comments js-comments-container bt bc-black-200 mt12  dno" data-post-id="64178428" data-min-length="15">
            <ul class="comments-list js-comments-list"
                    data-remaining-comments-count="0"
                    data-canpost="false"
                    data-cansee="true"
                    data-comments-unavailable="false"
                    data-addlink-disabled="true">

            </ul>
	    </div>

        <div id="comments-link-64178428" data-rep=50 data-anon=true>
                    <a class="js-add-link comments-link disabled-link" title="Use comments to ask for more information or suggest improvements. Avoid comments like &#x201C;&#x2B;1&#x201D; or &#x201C;thanks&#x201D;."  href="#" role="button">Add a comment</a>
                <span class="js-link-separator dno">&nbsp;|&nbsp;</span>
            <a class="js-show-link comments-link dno" title="Expand to show all comments on this post" href=# onclick="" role="button"></a>
        </div>         
    </div>
    </div>
</div>

                                    
<a name="70294679"></a>
<div id="answer-70294679" class="answer js-answer" data-answerid="70294679" data-parentid="45558193" data-score="0" data-position-on-page="7" data-highest-scored="0" data-question-has-accepted-highest-score="0"  itemprop="suggestedAnswer" itemscope itemtype="https://schema.org/Answer">
    <div class="post-layout">
        <div class="votecell post-layout--left">
            <div class="js-voting-container d-flex jc-center fd-column ai-stretch gs4 fc-black-300" data-post-id="70294679" data-referrer="None">
        <button class="js-vote-up-btn flex--item s-btn ba bar-pill c-pointer as-center bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050"
                data-controller="s-tooltip"
                data-s-tooltip-placement="right"
                title="This answer is useful"
                aria-pressed="false"
                aria-label="Up vote"
                data-selected-classes="fc-theme-primary bc-theme-primary bg-theme-primary-100"
                data-unselected-classes="bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050">
            <svg aria-hidden="true" class="svg-icon iconArrowUp" width="18" height="18"  viewBox="0 0 18 18"><path  d="M1 12h16L9 4l-8 8Z"/></svg>
        </button>
        <div class="js-vote-count flex--item d-flex fd-column ai-center fc-theme-body-font fw-bold fs-subheading py4"
             itemprop="upvoteCount"
             data-value="0">
            0
        </div>
        <button class="js-vote-down-btn flex--item mb8 s-btn ba bar-pill c-pointer as-center bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050"
                data-controller="s-tooltip"
                data-s-tooltip-placement="right"
                title="This answer is not useful"
                aria-pressed="false"
                aria-label="Down vote"
                data-selected-classes="fc-theme-primary bc-theme-primary bg-theme-primary-100"
                data-unselected-classes="bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050">
            <svg aria-hidden="true" class="svg-icon iconArrowDown" width="18" height="18"  viewBox="0 0 18 18"><path  d="M1 6h16l-8 8-8-8Z"/></svg>
        </button>


        
<button class="js-saves-btn s-btn s-btn__unset c-pointer py4"
        type="button"
        id="saves-btn-70294679"
        data-controller="s-tooltip"
        data-s-tooltip-placement="right"
        data-s-popover-placement=""
        title="Save this answer."
        aria-pressed="false"
        data-post-id="70294679"
        data-post-type-id="2"
        data-user-privilege-for-post-click="0"
        aria-controls=""
        data-s-popover-auto-show="false"
>
    <svg aria-hidden="true" class="fc-theme-primary-400 js-saves-btn-selected d-none svg-icon iconBookmark" width="18" height="18"  viewBox="0 0 18 18"><path  d="M3 17V3c0-1.1.9-2 2-2h8a2 2 0 0 1 2 2v14l-6-4-6 4Z"/></svg>
    <svg aria-hidden="true" class="js-saves-btn-unselected svg-icon iconBookmarkAlt" width="18" height="18"  viewBox="0 0 18 18"><path  d="m9 10.6 4 2.66V3H5v10.26l4-2.66ZM3 17V3c0-1.1.9-2 2-2h8a2 2 0 0 1 2 2v14l-6-4-6 4Z"/></svg>
</button>







            <div class="js-accepted-answer-indicator flex--item fc-green-400 py6 mtn8 d-none" data-s-tooltip-placement="right" title="Loading when this answer was accepted&#x2026;" tabindex="0" role="note" aria-label="Accepted">
                <div class="ta-center">
                    <svg aria-hidden="true" class="svg-icon iconCheckmarkLg" width="36" height="36"  viewBox="0 0 36 36"><path  d="m6 14 8 8L30 6v8L14 30l-8-8v-8Z"/></svg>
                </div>
            </div>

    
    <a class="js-post-issue flex--item s-btn s-btn__unset c-pointer py6 mx-auto" href="/posts/70294679/timeline" data-shortcut="T" data-ks-title="timeline" data-controller="s-tooltip" data-s-tooltip-placement="right" title="Show activity on this post." aria-label="Timeline"><svg aria-hidden="true" class="mln2 mr0 svg-icon iconHistory" width="19" height="18"  viewBox="0 0 19 18"><path  d="M3 9a8 8 0 1 1 3.73 6.77L8.2 14.3A6 6 0 1 0 5 9l3.01-.01-4 4-4-4h3L3 9Zm7-4h1.01L11 9.36l3.22 2.1-.6.93L10 10V5Z"/></svg></a>

</div>

        </div>

        

<div class="answercell post-layout--right">
    
    <div class="s-prose js-post-body" itemprop="text">
<p>Open that JavaScript file. On the bottom left You can see the language&quot;JavaScript&quot;.
Click on it and select &quot;JavaScript React&quot;.</p>
<p>Now Done:)</p>
    </div>
    <div class="mt24">
        <div class="d-flex fw-wrap ai-start jc-end gs8 gsy">
            <time itemprop="dateCreated" datetime="2021-12-09T18:06:32"></time>
            <div class="flex--item mr16" style="flex: 1 1 100px;">
                


<div class="js-post-menu pt2" data-post-id="70294679" data-post-type-id="2">

    <div class="d-flex gs8 s-anchors s-anchors__muted fw-wrap">

        <div class="flex--item">
            <a href="/a/70294679"
               rel="nofollow"
               itemprop="url"
               class="js-share-link js-gps-track"
               title="Short permalink to this answer"
               data-gps-track="post.click({ item: 2, priv: 0, post_type: 2 })"
               data-controller="se-share-sheet"
               data-se-share-sheet-title="Share a link to this answer"
               data-se-share-sheet-subtitle=""
               data-se-share-sheet-post-type="answer"
               data-se-share-sheet-social="facebook twitter devto"
               data-se-share-sheet-location="2"
               data-se-share-sheet-license-url="https%3a%2f%2fcreativecommons.org%2flicenses%2fby-sa%2f4.0%2f"
               data-se-share-sheet-license-name="CC BY-SA 4.0"
               data-s-popover-placement="bottom-start">Share</a>
        </div>



                <div class="flex--item">
                    <button type="button"
                            id="btnFollowPost-70294679" class="s-btn s-btn__link js-follow-post js-follow-answer js-gps-track"
                            data-gps-track="post.click({ item: 14, priv: 0, post_type: 2 })"
                            data-controller="s-tooltip " data-s-tooltip-placement="bottom"
                            data-s-popover-placement="bottom" aria-controls=""
                            title="Follow this answer to receive notifications">
                        Follow
                    </button>
                </div>






    </div>
    <div class="js-menu-popup-container"></div>
</div>
            </div>


            <div class="post-signature flex--item fl0">
                <div class="user-info ">
    <div class="d-flex ">
        <div class="user-action-time fl-grow1">
            answered <span title='2021-12-09 18:06:32Z' class='relativetime'>Dec 9, 2021 at 18:06</span>
        </div>
        
    </div>
    <div class="user-gravatar32">
        <a href="/users/16788449/anamika-pandey"><div class="gravatar-wrapper-32"><img src="https://www.gravatar.com/avatar/57d07ffd90dcca7665d85ec93a0cd8aa?s=64&amp;d=identicon&amp;r=PG&amp;f=y&amp;so-version=2" alt="Anamika Pandey&#39;s user avatar" width="32" height="32" class="bar-sm"></div></a>
    </div>
    <div class="user-details" itemprop="author" itemscope itemtype="http://schema.org/Person">
        <a href="/users/16788449/anamika-pandey">Anamika Pandey</a><span class="d-none" itemprop="name">Anamika Pandey</span>
        <div class="-flair">
            <span class="reputation-score" title="reputation score " dir="ltr">1</span>
        </div>
    </div>
</div>


            </div>
        </div>
        
    
    </div><title>autojs - B&#250;squeda</title><meta content="text/html; charset=utf-8" http-equiv="content-type" /><meta name="referrer" content="origin-when-cross-origin" /><meta property="og:description" content="La búsqueda inteligente de Bing facilita la búsqueda en menos tiempo de todo lo que buscas y, además, te da premios." /><meta property="og:site_name" content="Bing" /><meta property="og:title" content="autojs - Bing" /><meta property="og:url" content="https://www.bing.com/search?q=autojs&amp;form=WSHBSH&amp;qs=HS&amp;cvid=e321066b41dc426184a064d75924a313&amp;pq=&amp;cc=MX&amp;setlang=es-MX&amp;nclid=AC1E1DBB5EAEDDBC2F9B5D4F0F557FAC&amp;ts=1708643909987&amp;wsso=Moderate&amp;showconv=0" /><meta property="fb:app_id" content="3732605936979161" /><meta property="og:image" content="http://www.bing.com/sa/simg/facebook_sharing_5.png" /><meta property="og:type" content="website" /><meta property="og:image:width" content="600" /><meta property="og:image:height" content="315" /><link href="/search?format=rss&amp;q=autojs&amp;form=WSHBSH&amp;qs=HS&amp;cvid=e321066b41dc426184a064d75924a313&amp;pq=&amp;cc=MX&amp;setlang=es-MX&amp;nclid=AC1E1DBB5EAEDDBC2F9B5D4F0F557FAC&amp;ts=1708643909987&amp;wsso=Moderate&amp;showconv=0" data-orighref="" rel="alternate" title="XML" type="text/xml" /><link href="/search?format=rss&amp;q=autojs&amp;form=WSHBSH&amp;qs=HS&amp;cvid=e321066b41dc426184a064d75924a313&amp;pq=&amp;cc=MX&amp;setlang=es-MX&amp;nclid=AC1E1DBB5EAEDDBC2F9B5D4F0F557FAC&amp;ts=1708643909987&amp;wsso=Moderate&amp;showconv=0" data-orighref="" rel="alternate" title="RSS" type="application/rss+xml" /><link href="/sa/simg/favicon-trans-bg-blue-mg.ico" data-orighref="" rel="icon" /><link rel="preconnect" href="https://r.bing.com" /><link rel="preconnect" href="https://r.bing.com" crossorigin/><link rel="dns-prefetch" href="https://r.bing.com" /><link rel="dns-prefetch" href="https://r.bing.com" crossorigin/><link rel="preconnect" href="https://th.bing.com" /><link rel="preconnect" href="https://th.bing.com" crossorigin/><link rel="dns-prefetch" href="https://th.bing.com" /><link rel="dns-prefetch" href="https://th.bing.com" crossorigin/><script type="text/javascript" nonce="hdm+zCWukVbuQiu/8nwvTExR65VnfK54cOHZ35vK2Vk=">//<![CDATA[
_G={Region:"MX",Lang:"es-MX",ST:(typeof si_ST!=='undefined'?si_ST:new Date),Mkt:"es-MX",RevIpCC:"mx",RTL:false,Ver:"08",IG:"F4148F017DED47FD91F14746ED92DB7A",EventID:"65d7ebb6419f4345939b7bed9ec091d2",V:"web",P:"SERP",DA:"CHIE01",SUIH:"MdGtWoR2ZMp_BIp97Y-GZA",adc:"b_ad",EF:{cookss:1,bmcov:1,crossdomainfix:1,bmasynctrigger:1,bmasynctrigger3:1,getslctspt:1,newtabsloppyclick:1,chevroncheckmousemove:1,sharepreview:1,shareoutimage:1,chatskip2content:1},gpUrl:"\/fd\/ls\/GLinkPing.aspx?" }; _G.lsUrl="/fd/ls/l?IG="+_G.IG ;curUrl="https:\/\/www.bing.com\/search";function si_T(a){ if(document.images){_G.GPImg=new Image;_G.GPImg.src=_G.gpUrl+'IG='+_G.IG+'&'+a;}return true;}_G.NTT="600000";_G.CTT="3000";_G.BNFN="Default";_G.LG="160";;
//]]></script><style type="text/css">#b_header #id_h{content-visibility:hidden}#b_results>.b_ans:not(.b_top):nth-child(n+3) .rqnaContainerwithfeedback #df_listaa{content-visibility:auto;contain-intrinsic-size:648px 205px}#b_results>.b_algo:not(.b_algoBorder):nth-child(n+3)>h2{content-visibility:auto;contain-intrinsic-size:608px 24px}#b_results>.b_algo:not(.b_algoBorder):nth-child(n+3) .b_caption:not(.b_rich):not(.b_capmedia):not(.b_snippetgobig):not(.rebateContent){content-visibility:auto;contain-intrinsic-size:608px 65px;padding-right:16px;margin-right:-16px;margin-left:-16px;padding-left:16px}#b_results>.b_algo:not(.b_algoBorder):nth-child(n+3) .b_caption.b_rich .captionMediaCard .wide_wideAlgo{content-visibility:auto;contain-intrinsic-size:370px 120px}#b_results>.b_algo:not(.b_algoBorder):nth-child(n+3) .scs_icn{content-visibility:auto}#b_results>.b_ans:nth-child(n+3) .b_rs:not(.pageRecoContainer){content-visibility:auto;contain-intrinsic-size:608px 296px}#b_results>.b_ans:nth-child(n+3) .b_rs:not(.pageRecoContainer) .b_rsv3{padding-bottom:1px}#b_results>.b_pag{content-visibility:auto;contain-intrinsic-size:625px 45px}#b_footer>#b_footerItems{content-visibility:auto;contain-intrinsic-size:1px 24px}.cnt_vis_hid{content-visibility:hidden}.sw_ddbk:after,.sw_ddw:after,.sw_ddgn:after,.sw_st:after,.sw_sth:after,.sw_ste:after,.sw_st2:after,.sw_plus:after,.sw_tpcg:after,.sw_tpcw:after,.sw_tpcbk:after,.sw_close:after,.sb_pagN:after,.sb_pagP:after,.sw_up:after,.sw_down:after,.b_expandToggle:after,.b_fLogo:after,.b_cm:after,.sw_lpoi:after{position:relative}.sw_ddbk:after,.sw_ddw:after,.sw_ddgn:after,.sw_st:after,.sw_sth:after,.sw_ste:after,.sw_st2:after,.sw_plus:after,.sw_tpcg:after,.sw_tpcw:after,.sw_tpcbk:after,.sw_close:after,.sb_pagN:after,.sb_pagP:after,.sw_up:after,.sw_down:after,.b_expandToggle:after,.b_fLogo:after,.b_cm:after,.sw_lpoi:after{content:url(/rp/Qk540QN8GIaMmT8oEUEVF6N8IF8.png)}.sw_ddbk,.sw_ddw,.sw_ddgn,.sw_st,.sw_sth,.sw_ste,.sw_st2,.sw_plus,.sw_tpcg,.sw_tpcw,.sb_clrhov,.sb_pagN,.sb_pagP,.sw_up,.sw_down,.b_expandToggle,.b_fLogo,.b_cm,.sw_lpoi,.actExpander span{position:relative;display:inline-block;overflow:hidden;direction:ltr}.sw_ddbk:after,.sw_ddw:after,.sw_ddgn:after,.sw_st:after,.sw_sth:after,.sw_ste:after,.sw_st2:after,.sw_plus:after,.sw_tpcg:after,.sw_tpcw:after,.sw_tpcbk:after,.sb_pagN:after,.sb_pagP:after,.sw_down:after,.b_expandToggle:after,.b_fLogo:after,.b_cm:after,.sw_lpoi:after{display:inline-block;transform:scale(.5)}.sw_up:after{display:inline-block;transform:scale(.5,-.5) translateY(-20px)}.sw_tpcg,.b_expandToggle,.b_fLogo{display:block}.sw_st,.sw_sth,.sw_ste,.sw_st2{height:12px;width:12px}.sw_st:after{transform-origin:-68px -40px}.sw_st2:after{transform-origin:-124px -40px}.sw_sth:after{transform-origin:-152px -40px}.sw_ste:after{transform-origin:-96px -40px}.sw_ddbk,.sw_ddw,.sw_ddgn{height:4px;width:7px}.sw_ddbk:after,.sw_ddw:after{transform-origin:-180px -52px}.sw_ddgn:after{transform-origin:-180px -40px}.sw_tpcg,.sw_tpcw,.sw_tpcbk{height:12px;width:12px}.sw_tpcg:after,.sw_tpcw:after,.sw_tpcbk:after{transform-origin:-218px -40px}.sw_plus{height:8px;width:8px}.sw_plus:after{transform-origin:-198px -40px}.b_expandToggle,.sw_up,.sw_down{height:10px;width:16px}.b_expandToggle:after,.sw_up:after,.sw_down:after{transform-origin:-348px 0}.sw_up:after{transform-origin:-348px 0}.b_icon{width:20px;height:20px;z-index:0}.b_cm{height:10px;width:12px}.b_cm:after{transform-origin:-152px -68px}.sw_lpoi{height:12px;width:8px}.sw_lpoi:after{transform-origin:-48px -40px}z{a:1}z{a:1}html,body #b_results .b_no{background-color:#fff}#b_results>li a{color:#4007a2}#b_results>li a:visited{color:#681da8}#b_results>li{background-color:#fff}div.b_dropdown .b_selected{background:#e1e0df}#b_tween .b_toggle:hover,#b_tween .ftrH:hover{background:#f2f2f2}.b_scroll{background:#999;border-color:#999}.b_scroll:hover{background:#4d4d4d}.b_dropdown{background-color:#fff;border-color:#e5e5e5}.ctxt,select,input[type="text"]{color:#444;border-color:#ddd}.ccal{border-color:#ddd}.ccal:hover,.ctxt:hover,select:hover,input[type="text"]:hover{box-shadow:0 1px 2px 0 rgba(0,0,0,.1)}.ctxt:focus,select:focus,input[type="text"]:focus{color:#444;border-color:#919191;box-shadow:0 1px 2px 0 rgba(0,0,0,.1)}.ccal input[type="text"],.ccal input[type="text"]:hover,.ccal input[type="text"]:focus{color:#444}.ccal.ccal_rfs input[type="text"],.ccal.ccal_rfs input[type="text"]:hover,.ccal.ccal_rfs input[type="text"]:focus{color:#666}label{color:#767676}#b_results ::placeholder{color:#767676;opacity:1}#b_results :-ms-input-placeholder{color:#767676}#b_results ::-webkit-input-placeholder{color:#767676}#b_results input[type="text"]:disabled{color:#ccc}a,#b_tween a:visited,#b_results .b_no a{color:#4007a2}a:visited,#b_results>li a:visited{color:#681da8}.trgr_icon .c_tlbxTrgIcn.sw_css{border-color:#006d21 transparent}.b_posText{color:#006d21}.b_negText{color:#c80000}#b_context .b_entityTitle,#b_results .b_entityTitle{color:#444}#b_context .b_entitySubTitle,#b_results .b_entitySubTitle{color:#767676}body,.b_promoteText,#b_tween a.ftrH:hover,.b_expando,.b_expando h2,.b_active a,.b_active a:visited,.b_active a:hover,#b_results>.b_pag a,#b_results .b_no,#b_content a.cbl:visited,#b_content a.cbl{color:#71777d}#b_tween,#b_tween a.ftrH{color:#70757a}.b_top,.b_top .b_promoteText{color:#444}.b_alert,.b_pAlt{color:#d90026}#b_results .b_alert,#b_results .b_pAlt{color:#d90026}.b_demoteText,.b_secondaryText,.b_attribution,.b_factrow,.b_focusLabel,.b_footnote,.b_ad .b_adlabel,#b_tween .b_dropdown a,.b_expando .b_subModule,.b_expando .b_suppModule,.b_algo .b_vList td{color:#767676}.b_algo .b_factrow{color:#767676}.b_caption,.snippet{color:#71777d}.b_top .b_caption{color:inherit}.ciot{background-color:#000}#b_context .b_subModule,#b_results .b_subModule{border-bottom:1px solid #ececec}#b_results .b_sideBleed:last-child .b_subModule{border-bottom:0}.b_subModule h2{color:#111}.c_tlbx{border-color:#999;background:#fff}.sw_poi{color:#fff}.sw_poia{color:#fff}.b_caption p strong,.b_caption .b_factrow strong,.b_secondaryText strong{color:#767676}.b_belowcaption .b_secondaryText{color:#767676 !important}#b_context .b_ans,#b_context #wpc_ag{background-color:#fff}#b_context .b_posText{color:#006d21}#b_context .b_negText{color:#c80000}#b_context,#b_context #wpc_eif{color:#71777d}#b_context .b_alert{color:#d90026}#b_content .b_lowFocusLink a,#b_context .b_secondaryText,#b_context .b_attribution,#b_context .b_factrow,#b_context .b_footnote,#b_context .b_ad .b_adlabel,.b_expando .b_secondaryText,.b_expando .b_attribution,.b_expando .b_factrow,.b_expando .b_footnote{color:#767676}#b_context .b_footnote a,.b_promtext{color:#111}#b_context .b_pointer.b_mhdr:hover .b_secondaryText{color:#36b}z{a:1}.b_footer{background-color:#f5f5f5}.b_footer,.b_footer a,.b_footer a:visited{color:#666}#b_results>.b_ad a{color:#4007a2}#b_results>.b_ad a:visited{color:#681da8}#b_context>li.b_ad{color:#71777d;background-color:#fff}#b_context>li.b_ad a{color:#4007a2}#b_context>li.b_ad a:visited{color:#681da8}.b_ad .b_caption p strong,.b_ad .b_caption .b_ad_description strong,.b_ad .b_caption .b_factrow strong,.b_ad .b_secondaryText strong{color:#767676}cite,#b_results cite.sb_crmb a,#b_results cite a.sb_metalink,#b_results .b_adurl cite a,#bk_wr_container cite a{color:#006d21}#b_context cite,#b_context cite a{color:#006d21}.b_ad cite,.b_ad cite a{color:#006d21}.b_button:hover,.b_button:visited,.b_hlButton,.b_hlButton:hover,.b_hlButton:visited,.b_foregroundText,.ciot{color:#fff}.b_button:hover,.b_hlButton{background-color:#0072c5}.b_button:active,.b_hlButton:active{background-color:#333}.b_hlButton:hover{background-color:#106ebe}.b_border,.b_button,.b_hlButton{border-color:#ccc}#b_results>.b_pag{background-color:transparent}#b_results>.b_pag a:hover{background-color:#f4f4f4}#b_results>.b_pag .sb_pagS_bp:hover{background-color:#fff}#b_results>.b_pag a.sb_pagP:hover,#b_results>.b_pag a.sb_pagN:hover{background-color:inherit}#b_results>.b_pag .sb_pagS_bp{color:#111}.b_pag a{border:3px solid transparent}.b_pag a.sb_bp{border:0;border-bottom:3px solid transparent}.b_pag .sb_pagS{border-color:#00809d}.b_pag a.sb_pagS_bp{border-color:#00809d}z{a:1}z{a:1}#b_results>.b_ad{color:#71777d;background-color:#f9fcf7}#b_results,#b_results .b_defaultText,#b_results>.b_pag a:hover,#b_tween .b_toggle:hover,#b_tween .b_highlighted{color:#71777d}html,body,h1,h2,h3,h4,h5,h6,p,img,ol,ul,li,form,table,tr,th,td{border:0;border-collapse:collapse;border-spacing:0;list-style:none;margin:0;padding:0}html{overflow-y:scroll}#b_content{clear:both;min-height:316px;padding:41px 0 20px var(--lgutter)}#b_pole{margin:-25px 0 15px var(--lgutterrev);padding-left:var(--polepadl)}#b_tween:not(.b_hidetoggletween)~#b_pole{margin-top:3px}.b_underSearchbox~#b_pole{margin-top:0}#b_context{margin:0 0 0 var(--rgutter);padding:0 20px}#b_context .b_ans,#b_context .b_ad{margin:0 -20px}#b_context .b_ans{padding:10px 20px 0}#b_context .b_ad{padding:10px 20px}#b_results,#b_context,#b_tween>span,.b_hList>li,.c_tlbxTrg,.b_hPanel>span,.ccal .ccali,.b_footerRight,.b_hPanel .b_xlText,.b_hPanel .cico,.b_moreLink,.b_label+.b_hList,.lc_bks,.lc_bkl,.fiw,.csrc,.b_algo .b_title H2,.b_algo .b_title>div,.b_algo .b_title .b_suffix>div,h3{display:inline-block}.b_pointer{cursor:pointer}label,.b_ad .b_adlabel,.c_tlbxTrgIcn{display:block}#b_tween{margin-top:-28px}.b_underSearchbox~#b_tween{margin-top:-2px}#b_tween,#b_tween .ftrH{height:30px}#b_tween>span{padding-right:25px}#b_results>li{margin:0 0 4px}#b_results>li,#b_results>.b_ad{padding:10px 20px}#b_results>.b_ans{padding:12px 20px 0}#b_results>.b_algo{padding:12px 20px 0}#b_results>li .b_fullb{margin-left:-20px;margin-right:-20px}#b_results>.b_ad{padding-right:18px;border-right:2px solid #e5e5e5}#b_results>li:first-child{padding-top:10px}#b_results>.b_pag{padding:26px 0 26px 20px}.sb_hbop,.b_hide,.ttl,#sw_tfbb,.sw_next,.sw_prev,#id_d,.b_hidden img{display:none}.b_hidden{visibility:hidden}#b_context .b_ans{margin-bottom:5px}#b_context .b_ad{margin-bottom:5px}main,aside,.b_inlineList li,.b_factrow li{display:inline}.b_footerRight,td,th,#b_context,.b_hList>li{vertical-align:top}.b_footer{width:100%;padding:12px 0}.c_tlbxTrg{width:15px;height:14px;margin:-1px 6px -3px 2px}.c_tlbxTrgIcn{margin:4px 0 2px 3px}.c_tlbx{position:absolute;z-index:6;border:1px solid;padding:10px}.b_gridList ul:first-child,.b_vlist2col ul:first-child{margin:0 20px 0 0}.b_gridList li,.b_vlist2col li{padding:0 0 10px}.b_vlist2col.b_deep li{padding:0 0 10px}.b_overhangR .b_vlist2col ul:first-child{margin:0 15px 0 0}.b_overhangR .b_vlist2col ul{width:180px}.b_deep p{height:33px}#b_context .b_ad .b_adlabel,.sc_rf form,form.sc_rf{margin-bottom:10px}.b_ad li,#b_results .b_ad .b_adlabel{margin-bottom:8px}.b_ad li:last-child{margin-bottom:0}.b_ad li li,.b_ad li li:last-child{margin:0}#b_results .b_ad .b_vlist2col,#b_results .b_ad .b_factrow,#b_results .b_ad .b_bcfactrow{margin-top:-6px}#b_results .b_ad .sb_adRA .b_vlist2col{padding-left:0}.sx_ci{border:1px solid #e5e5e5;margin-top:3px;width:80px;height:60px}.b_favicon{margin:0 .5em 0 0}.b_imagePair:after,.b_vlist2col:after,.b_gridList:after{clear:left}.b_imagePair.reverse:after,.b_overhangR:after{clear:right}.b_clear,#b_results>li:after,.b_clearfix:after{clear:both}#b_results>li:after,.b_clearfix:after,.b_imagePair:after,.b_vlist2col:after,.b_gridList:after,.b_overhangR:after{content:'.';display:block;height:0;visibility:hidden}.b_vlist2col ul,.b_gridList ul,.b_float,.b_footer,.b_float_img,.b_pag li,.b_mhdr h2{float:left}.b_floatR_img,.b_floatR,.wr_tc{float:right}.b_overflow,.b_hList li,.b_1linetrunc,.b_deep p{overflow:hidden}.b_creditedImg img,.b_creditedImg .cico{padding-bottom:1px}h4,.sa_uc>.b_vList>li>table td,.b_smBottom,#b_context .b_ad h2,.b_attribution,.b_focusTextExtraSmall,.b_snippet{padding-bottom:2px}.b_focusLabel,.b_secondaryFocus,.b_focusTextLarge,.b_focusTextMedium,.b_focusTextSmall{padding-bottom:4px}.b_factrow{padding-bottom:2px}h2,label{padding-bottom:3px}.b_vPanel .b_vPanel>div,.b_vList .b_vPanel>div{padding-bottom:5px}.b_dataList li,.b_mBottom{padding-bottom:5px}.b_lBottom,.b_entitySubTitle{padding-bottom:12px}#b_context .b_subModule h2,#b_results .b_subModule h2{padding-bottom:16px}#b_results #sp_recourse.b_lBottom,.b_caption,.b_moreLink,.b_footnote,.b_hList>li,#b_context h2,#b_context h2.b_entityTitle,#b_results .b_ad .b_factrow,#b_results .b_ad .b_bcfactrow,.b_expando h2,.b_no h1,.b_no h4,.b_no li,.b_prominentFocusLabel,.b_locStr{padding-bottom:10px}.b_vPanel>div,.b_vList>li{padding-bottom:10px}.b_vList .b_hList>li,.b_vPanel .b_hList>li,.b_vList .b_float_img,.b_creditedImg .b_footnote,.b_creditedImg .cico img,#b_results>.b_ad,.b_suppModule .b_mhdr,.b_vList>li>.tab-container,.b_vPanel>div>.tab-container,.b_ad .b_deep h3,#b_content .b_float_img_nbp{padding-bottom:0}.b_caption .b_factrow:last-child,#b_results .b_caption .b_factrow:last-child,.b_caption>.b_dataList:last-child li:last-child,.b_caption .b_moreLink:last-child,.b_vList .b_moreLink:last-child,.b_vList .b_factrow:last-child,.b_vPanel .b_factrow:last-child,.b_caption .b_attribution:last-child,.b_vList .b_attribution:last-child,.b_hList .b_attribution:last-child,.b_vPanel .b_attribution:last-child,.b_vList>li>table:last-child tr:last-child td,.b_vPanel>div>table:last-child tr:last-child td,.b_vList .b_focusLabel:last-child,.b_vPanel .b_focusLabel:last-child,.b_vList .b_secondaryFocus:last-child,.b_vPanel .b_secondaryFocus:last-child,.b_vList .b_focusTextExtraSmall:last-child,.b_vPanel .b_focusTextExtraSmall:last-child,.b_vList .b_focusTextSmall:last-child,.b_vPanel .b_focusTextSmall:last-child,.b_vList .b_focusTextMedium:last-child,.b_vPanel .b_focusTextMedium:last-child,.b_vList .b_focusTextLarge:last-child,.b_vPanel .b_focusTextLarge:last-child,.b_vList h4:last-child,.b_vPanel h4:last-child,.b_vPanel .b_caption:last-child,.b_vPanel .b_vList:last-child>li:last-child,.b_vPanel .b_footnote:last-child{padding-bottom:0}.b_vList .b_vPanel,.b_vPanel .b_vPanel{margin-bottom:-5px}.b_hList .b_vPanel{margin-bottom:-10px}.b_mBMargin{margin-bottom:5px}#b_results .b_no{margin:0 0 80px}.b_rich{padding-top:3px}h2+.b_rich,.btitle+.b_rich{padding-top:2px}.b_algo .b_attribution img{vertical-align:text-bottom}.b_lLeft,.b_floatR_img,.b_suffix{padding-left:10px}.wr_tc,.b_xlLeft,.b_deep,#b_results .b_ad .b_vlist2col,#b_tween{padding-left:20px}h2 .b_secondaryText{margin-left:5px}.b_hList.b_imgStrip>li{padding-right:1px}.fiw,.lc_bkl,.b_mRight,.b_label,.csrc{padding-right:4px}.b_lRight,.b_imgStrip .imgData,.b_underSearchbox .b_label{padding-right:10px}.b_hPanel>span,.b_hList>li{padding-right:10px}.b_xlRight{padding-right:20px}.b_hList.b_imgStrip>li:last-child,.b_hList>li:last-child,.b_hPanel>span:last-child,td:last-child,th:last-child,#b_tween>span:last-child{padding-right:0}.b_overhangR{margin-right:-30px;padding-right:150px}.wr_tc{margin-right:-150px}.b_tbl{margin-right:-10px}.b_border,.b_button,.b_hlButton,.b_scroll,.b_dropdown{border-width:1px;border-style:solid}.b_button,.b_hlButton{line-height:30px;text-decoration:none;text-align:center;cursor:pointer;padding:0 15px;min-width:50px}.lc_bks .cbtn{margin-top:15px}#b_context .b_subModule,#b_results .b_subModule,.b_expando .b_subModule{padding:0 19px 0 19px;margin-bottom:16px}#b_context .b_subModule .b_lBottom.lo_im_ov{padding-bottom:0;margin-bottom:12px}#b_results .b_sideBleed:last-child .b_subModule{margin-bottom:0}.b_dropdown{position:absolute;z-index:6}.b_scroll{position:relative;top:0;width:5px;height:20px}.b_mhdr{margin:-15px 0 -5px;padding:15px 0 5px}.b_mhdr .sw_up,.b_mhdr .sw_down{margin-top:10px}.b_mhdr .b_moreLink,.b_mhdr .b_secondaryText{margin-top:6px}.b_vPanel .sc_rf form,.b_suppModule .b_mhdr{margin-bottom:0}.b_rTxt{text-align:right}.b_cTxt{text-align:center}table{width:100%;word-wrap:break-word}td,th,.b_float_img{padding:0 10px 10px 0}th{text-align:left}.sw_poi,.sw_poia{float:left;margin:-3px 5px 0 0;line-height:20px;text-align:center}.ctxt,select,input[type="text"]{outline:none;padding:0 11px;height:30px;border-width:1px;border-style:solid;border-radius:2px}.ctxt.b_focusTextMedium{padding:3px 11px 8px 11px;height:57px}.ctxt.b_outTextBox{border-top:4px solid #919191;padding:0 11px 8px 11px}.ctxt.b_outTextBox:focus{border-top-width:1px;padding-top:3px}select{padding:0 0 0 11px;height:32px}input.ctxt,.ccal input,.ccal .ccali,.b_favicon,.b_footnote .cico{vertical-align:middle}.ccal .ctxt,.ccal .ctxt:hover,.ccal .ctxt:focus,.ccal .ccali{background:none;border:none;box-shadow:none;-webkit-box-shadow:none}.ccal{border-width:1px;border-style:solid}.ccal .ccali{height:30px}.ccal .ccalp{padding:5px 5px 0 5px}.b_underSearchbox{margin:-20px 20px 14px}.b_underSearchbox .b_hList>li{padding:0 8px 0 0}.b_compactSearch label{float:left;margin:7px 10px 0 0}.b_compactSearch input{margin-right:0;float:left}.b_compactSearch .cbtn{border-left:0}.b_footer table{width:520px;margin:15px 20px 0 120px}#b_footerItems ul{display:block}#b_footerItems li{display:inline;float:left}#b_footerItems span{margin-right:24px;margin-left:48px;float:right}#b_footerItems a{margin-right:24px;font-size:11px}#b_footerItems{height:24px;line-height:24px;padding:0 20px 0 24px;font-size:11px}.b_footerRight{margin:13px 0 0 50px}.b_1linetrunc{text-overflow:ellipsis;white-space:nowrap}div.cico.b_capImg{margin-bottom:4px}#b_context .b_entitySubTitle,#b_results .b_entityTP .b_entitySubTitle{margin-top:-9px}.b_entityTP .b_infocardTopR .b_floatR_img,.b_tinyInfocard .b_floatR_img,.b_smallInfoFacts .sml{padding-bottom:10px}.b_vmparent{display:-ms-flexbox;display:-webkit-flex;display:flexbox;display:-webkit-box;display:flex;align-items:center}.b_relative{position:relative}.bdzoom115{zoom:1.15;-ms-zoom:1.15}.bdzoom110{zoom:1.1;-ms-zoom:1.1}.bdzoom105{zoom:1.05;-ms-zoom:1.05}.bdzoom95{zoom:.95;-ms-zoom:.95}:root{--lgutter:160px;--lgutterrev:-160px;--polepadl:180px;--polepadl2:120px;--dtabpadl:170px;--rgutter:80px;--crleft:888px;--bminwidth:1320px}z{a:1}input,textarea,h4,h5{font:inherit;font-size:100%}body,.b_no h4,h2 .b_secondaryText,h2 .b_alert,.b_underSearchbox,#b_header{font:14px/normal "Arial",Helvetica,Sans-Serif}h1,h2,h3{font:13px/1.2em "Arial",Sans-Serif}h2{font-size:20px;line-height:28px}.sb_add h2{line-height:28px}h3,.b_no h1{font-size:18px;line-height:22px}cite{font-style:normal}.sb_alert a{font-style:italic}#b_content,#b_context,.b_expando{line-height:1.2em}cite,#b_context,.b_expando,#vidans2{word-wrap:break-word}#sa_ul li,.nowrap{white-space:nowrap}.b_footer{line-height:18px}.b_attribution,#b_content .b_attribution{font-size:16px;line-height:24px}#b_content main #b_results .b_algo cite{font-size:14px}.ba_title>h2+cite{font-size:16px;line-height:24px;padding-top:1px}.b_subModule h2{font-size:18px;line-height:22px}.b_smText,.b_footnote,.ciot{font-family:"Arial",Helvetica,Sans-Serif;font-size:11px;line-height:normal}.b_footnote{line-height:16px}.b_ad .b_adlabel,.b_ad .b_adlabel strong{font:12px/normal "Arial",Helvetica,Sans-Serif}.b_mText{font:16px/22px "Arial",Helvetica,Sans-Serif}.b_focusLabel{font:16px "Arial",Sans-Serif;line-height:20px}.b_secondaryFocus{font:13px "Arial",Helvetica,Sans-Serif;line-height:18px}.b_focusTextExtraSmall{font:18px "Arial",Sans-Serif;line-height:1.3em}h2.b_entityTitle,.b_prominentFocusLabel,.b_xlText{font-size:24px;font-family:"Arial",Sans-Serif;line-height:1.2em}h2.b_entityTitle{line-height:normal}.b_entitySubTitle{font-size:13px;line-height:18px}.b_focusTextSmall,.b_focusTextMedium,.b_focusTextLarge{font:400 28px "Arial",Sans-Serif}.b_focusTextMedium{font-size:40px;line-height:48px}.b_focusTextLarge{font-size:54px;line-height:64px}.b_focusTextSmall{font-size:28px;line-height:38px}h2.b_topTitle{font-size:20px;line-height:28px}.sb_add h2.b_topTitle{line-height:28px}strong,.b_active a,.b_no h4,.b_strong,.b_ad .b_adlabel strong,.cbl{font-weight:700}th,h2 strong,h3 strong{font-weight:normal}#b_tween{font-size:14px}#b_tween>span,#b_tween .ftrH{line-height:30px}.b_attribution cite{unicode-bidi:normal}a,.b_algoheader a:hover,.b_topbar a:hover,.b_pag a:hover,.cbtn:hover,.cbtn a:hover,.b_hlButton:hover,.ftrB a:hover,.b_algo:hover .b_vList h2 a,.b_algo:first-child:hover .b_vList h2 a,#b_header:hover~#b_content #b_pole~#b_results li:first-child.b_ad li:first-child h2 a,#b_header:hover~#b_content #b_pole~#b_results li:first-child.b_algo h2 a,#b_results>.b_ans:hover .ent_cnt h2>a,#b_results>.b_ans:hover #sp_requery h2>a,#b_results>.b_ans .b_rich>.b_vList>li:hover h5.b_lBMargin>a{text-decoration:none}a:hover,.b_algoheader a h2:hover,.b_algo:first-child:hover h2 a,.b_algo .b_underline a,.sb_add .b_underline a,#b_results>.b_ad li:first-child .sb_adTA:hover h2 a,#b_header:hover~#b_content #b_results li:first-child.b_ad li:first-child div.sb_adTA h2 a,#b_header:hover~#b_content #b_results li:first-child.b_algo h2 a,#b_results>.b_algo:not(.b_algo~.b_algo)>h2 a,#b_results>.b_algo:not(.b_algo~.b_algo) .b_algo_group>h2 a,#b_results>.b_algo:not(.b_algo~.b_algo)>.b_title h2 a,#b_results>.b_adTop li:first-child .sb_adTA h2 a,#b_results>.b_ad li .sb_adTA:hover h2 a,#b_results .b_algoBorder h2 a{text-decoration:underline}z{a:1}z{a:1}#b_results>li.b_ans.b_topborder{padding:15px 19px 10px 19px;margin-bottom:12px;border-radius:6px;margin-top:0}#b_results>li.b_ans.b_topborder .bgtopgr{border-bottom-left-radius:6px;border-bottom-right-radius:6px}#b_results>li.b_ans.b_topborder .bgtopwh{border-radius:6px 6px 0 0}#b_results li.b_ans~.b_topborder{margin-top:10px}#b_results li.b_ad~.b_ans.b_topborder{margin-top:12px}#b_results>li.b_ans.b_topborder.b_tophb{border:1px solid #ddd;box-shadow:none;padding:0}#b_results>li.b_ans.b_topborder,#b_results>li.b_ans.b_topborder.b_tophb.b_topshad{box-shadow:0 0 0 1px rgba(0,0,0,.05);border:none}.b_tophb .b_tophbh{padding:15px 19px 16px 19px}.b_tophb .b_tophbb{border-top:1px solid #ddd;padding:15px 19px 19px 19px}.b_topshad .b_tophbb{padding-bottom:10px}.b_tophb .b_tophbb.bgbtopnone{border-top:none}.b_tophb .bgtopwh{background-color:#fff}.b_tophb .bgtopgr{background-color:#f5f5f5}#b_results>.b_ad+.b_top{margin-top:0}.b_top .b_attribution+.b_rich,.b_top .b_factrow+.b_rich{padding-top:8px}.b_top .b_topTitle+.b_rich{padding-top:12px}.b_tHeader,.b_demoteText,.b_secondaryText,.b_attribution,.b_factrow,.b_focusLabel,.b_footnote,.b_ad .b_adlabel,#b_tween .b_dropdown a,.b_expando .b_subModule,.b_expando .b_suppModule,.b_algo .b_vList td,#b_content .b_lowFocusLink a,#b_context .b_secondaryText,#b_context .b_attribution,#b_context .b_factrow,#b_context .b_footnote,#b_context .b_ad .b_adlabel,.b_expando .b_secondaryText,.b_expando .b_attribution,.b_expando .b_factrow,.b_expando .b_footnote{color:#767676}.b_ad .b_secondaryText{color:#767676}#b_context .b_mhdr:hover .b_secondaryText,.b_expando .b_mhdr:hover .b_secondaryText{color:#001ba0}#b_results>.b_top .b_prominentFocusLabel,#b_results>.b_top .b_topTitle,#b_results>.b_top .b_focusTextExtraSmall,#b_results>.b_top .b_focusTextExtraSmall a,#b_results>.b_top .b_focusTextSmall,#b_results>.b_top .b_focusTextSmall a,#b_results>.b_top .b_focusTextMedium,#b_results>.b_top .b_focusTextMedium a,#b_results>.b_top .b_focusTextLarge,#b_results>.b_top .b_focusTextLarge a{color:#111}span.b_negText.b_focusTextExtraSmall{color:#c80000 !important}span.b_posText.b_focusTextExtraSmall{color:#006d21 !important}.b_top .b_focusTextExtraSmall a,.b_top .b_focusTextSmall a,.b_top .b_focusTextMedium a,.b_top .b_focusTextLarge a{text-decoration:none}#b_results>.b_top:hover .b_focusTextExtraSmall a,#b_results>.b_top:hover .b_focusTextSmall a,#b_results>.b_top:hover .b_focusTextMedium a,#b_results>.b_top:hover .b_focusTextLarge a{color:#001ba0}#b_results>.b_top .b_focusTextExtraSmall a:hover,#b_results>.b_top .b_focusTextSmall a:hover,#b_results>.b_top .b_focusTextMedium a:hover,#b_results>.b_top .b_focusTextLarge a:hover{text-decoration:underline}.fc_cal_holder table{font-size:11px}body .fc_cal_holder{border:1px solid #0072c5}body .fc_cal_holder .fc_cal_disabled{color:#767676}body .fc_cal_holder a:link,body .fc_cal_holder a:visited{color:#666}body .fc_cal_holder td,body .fc_cal_holder .fc_cal_disabled,body .fc_cal_holder .fc_cal_days td{width:20px;line-height:20px;padding:0 10px 10px 0}.fc_cal_holder tr td:first-child{padding-left:10px}.fc_cal_holder tr:last-child td{padding-bottom:15px}body .fc_cal_holder .fc_cal_days td{line-height:15px;color:#767676;background-color:#fff}body .fc_cal_holder a{padding:0}body .fc_cal_holder td a:hover,body .fc_cal_holder td a:active,body .fc_cal_holder td.fc_cal_current a:hover,body .fc_cal_holder td.fc_cal_current a:active{background-color:#eee;color:#666}body .fc_cal_holder .fc_cal_monthHolder+.fc_cal_monthHolder{border-left:1px solid #bfdcf0}body .fc_cal_holder .fc_cal_monthHolder{background-color:#fff;border:0;padding:15px 15px 10em 15px}body .fc_cal_holder th div{background-color:#fff;border:0;padding:0 0 15px;color:#666;text-align:center;font-size:13px}body .fc_cal_holder .fc_cal_current a{background-color:#001ba0}body .fc_cal_monthDec.fc_cal_monthChange,body .fc_cal_monthInc.fc_cal_monthChange{background:url(rms://rms:answers:SharedStaticAssets:navchevrons_topRefresh) no-repeat;width:8px;height:12px;background-position:0 -110px;font-size:0}body .fc_cal_monthDec.fc_cal_monthChange{background-position:0 -44px}body .fc_cal_holder .fc_cal_month_first .fc_cal_monthDec{margin:1px 0 0 15px}body .fc_cal_holder .fc_cal_month_last .fc_cal_monthInc{margin:1px 15px 0 0}#b_results .b_attribution,#b_context .b_attribution{line-height:22px}.b_deep,#b_results .b_caption,#b_results .b_snippet,#b_results .b_factrow,#b_results p,#b_context .b_caption,#b_context .b_snippet,#b_context .b_factrow,#b_context p,#snct{line-height:22px}#b_context .b_caption .b_factrow .csrc{line-height:19px}#b_content .b_deep p{height:44px}#b_content .carousel p,#b_content .b_slideexp p,#b_content #lMapContainer p,#b_content .MicrosoftMap p,#b_content .rwrl p,#b_content .qna_body p,#bw-rqna p,#ecmp_relatedPh p,#na_cl p{line-height:inherit}#b_content .b_imgSetData p,#b_content .b_caption .b_slideexp p{line-height:18px}#b_results #lgb_info .b_factrow{padding-bottom:4px}#snct .imgbig .b_overflow>h4{line-height:normal}#b_results .b_attribution,.b_ad h2,.b_deep h3,#snct h4{padding-bottom:0}#b_results .b_attribution .cico{border-radius:0}#b_results .b_algo .b_attribution,#b_results .sb_add .b_attribution{padding:1px 0 0 0}.b_deep h3{line-height:1.2em}#b_results .b_factrow{padding-bottom:2px}.b_ans h2.b_topTitle.b_headerTitle{padding-bottom:3px}.b_ans h2.b_lBottom{padding-bottom:10px}.b_expando h2{padding-bottom:10px}.b_ans h2,.b_algo h2{padding-bottom:0}z{a:1}.b_ad li,#b_context>.b_ad>ul>li,#b_dynRail>.b_ad>ul>li{margin-bottom:22px}#b_context .b_ad .b_caption{padding-bottom:4px}#b_content ol#b_results li.b_ad{padding-bottom:10px;margin-bottom:0}#b_results li.b_ad.b_adBottom{padding-top:11px}#b_context .b_entityTP{padding:9px 19px 4px 19px;margin:-10px -20px -6px -20px;width:100%;box-shadow:0 0 0 1px rgba(0,0,0,.05);border-radius:6px;background:#fff}#b_context .b_entityTP .irp{border-radius:6px 6px 0 0;overflow:hidden}#b_context .b_ans:not(:first-child)>.b_entityTP{margin-top:-16px}#b_context .b_ad:not(:last-child){padding-bottom:15px;border-bottom:1px solid #ebebeb}.b_dark #b_context .b_ad:not(:last-child){padding-bottom:15px;border-bottom:1px solid #545250}#b_context .b_ans:not(:last-child){padding-bottom:5px}#b_context .b_ans .b_entityTP{margin-bottom:20px}#b_content .b_algo .b_attribution cite strong{font-weight:normal}z{a:1}.b_footer{background-color:#f1f3f4}.b_scopebar li:hover:not(.b_active){border-bottom:3px solid #ccc}#b_header .scopebar_pipe{border-bottom:none}#b_header .b_scopebar li.b_nohov:hover{border-bottom:none}#b_results .b_lineclamp1{display:-webkit-box;-webkit-line-clamp:1;-webkit-box-orient:vertical;overflow:hidden;white-space:normal}#b_results .b_lineclamp2{display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden;white-space:normal}#b_results .b_lineclamp3{display:-webkit-box;-webkit-line-clamp:3;-webkit-box-orient:vertical;overflow:hidden;white-space:normal}#b_results .b_lineclamp4{display:-webkit-box;-webkit-line-clamp:4;-webkit-box-orient:vertical;overflow:hidden;white-space:normal}#b_results .b_lineclamp5{display:-webkit-box;-webkit-line-clamp:5;-webkit-box-orient:vertical;overflow:hidden;white-space:normal}z{a:1}z{a:1}#qs_searchBox{background-color:#fff;color:#444;text-align:center;display:flex;align-items:center;height:40px;max-width:300px;position:absolute;border-radius:20px;border:none;outline:0;text-decoration:none;box-shadow:0 0 0 1px rgba(0,0,0,.05),0 2px 3px 0 rgba(0,0,0,.1);margin-top:8px;display:none;cursor:pointer;font-weight:600;z-index:30009}#qs_searchBox:hover{box-shadow:0 0 0 1px rgba(0,0,0,.05),0 2px 4px 1px rgba(0,0,0,.14)}#qs_selectedText{order:2;margin:auto 12px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;max-width:258px}#qs_searchIconOuter{width:18px;height:18px;order:1;margin-left:12px;margin-right:0}#qs_searchIconInner{display:inline-flex;width:45px;height:45px;background-repeat:no-repeat;transform-origin:top right;transform:translate(-2px,-2px)}#qs_copyBox,#qs_chatBox{text-align:center;display:flex;align-items:center;height:40px;max-width:300px;position:absolute;border:none;outline:0;text-decoration:none;box-shadow:0 0 0 1px rgba(0,0,0,.05),0 2px 3px 0 rgba(0,0,0,.1);margin-top:8px;margin-left:8px;display:none;cursor:pointer;z-index:30009}#qs_copyBox:hover,#qs_chatBox:hover{box-shadow:0 0 0 1px rgba(0,0,0,.05),0 2px 4px 1px rgba(0,0,0,.14)}#qs_copyIconOuter,#qs_chatIconOuter{width:18px;height:18px;margin:auto 12px}#qs_chatBox{background-color:none;background-image:linear-gradient(to right,#2870ea,#1b4aef);border-radius:20px}#qs_chatIconInner{display:inline-flex;width:24px;height:24px;transform:translate(-2px,-4px)}#qs_copyBox{background-color:#fff;color:#444;border-radius:6px}#qs_copyText{padding-right:12px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;max-width:258px}#qs_copyIconInner{display:inline-flex;width:45px;height:45px;transform:scale(.4) translate(-66px,-3px);transform-origin:top right}#qs_mapBox{background-color:#fff;color:#444;text-align:center;padding:12px 12px 0 12px;max-width:300px;position:absolute;height:28px;border-radius:6px;border:none;outline:0;text-decoration:none;box-shadow:0 0 0 1px rgba(0,0,0,.05),0 2px 3px 0 rgba(0,0,0,.1);margin-top:8px;margin-left:8px;display:none;cursor:pointer;z-index:30009}#qs_mapBox:hover{box-shadow:0 0 0 1px rgba(0,0,0,.05),0 2px 4px 1px rgba(0,0,0,.14)}#qs_mapText{padding-right:12px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;max-width:258px}#qs_mapIconOuter{width:18px;height:18px}#qs_mapIconInner{display:inline-flex;width:48px;height:48px;transform:scale(.4) translate(-72px,-3px);transform-origin:top right}#qs_searchIconInner{background-image:url(data:image/svg+xml,%3Csvg%20width%3D%2224%22%20height%3D%2224%22%20viewBox%3D%220%200%2024%2024%22%20fill%3D%22none%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%3E%0D%0A%20%20%20%20%3Cpath%20d%3D%22M10%202.75C14.0041%202.75%2017.25%205.99594%2017.25%2010C17.25%2011.7319%2016.6427%2013.3219%2015.6295%2014.5688L20.5303%2019.4697C20.8232%2019.7626%2020.8232%2020.2374%2020.5303%2020.5303C20.2641%2020.7966%2019.8474%2020.8208%2019.5538%2020.6029L19.4697%2020.5303L14.5688%2015.6295C13.3219%2016.6427%2011.7319%2017.25%2010%2017.25C5.99594%2017.25%202.75%2014.0041%202.75%2010C2.75%205.99594%205.99594%202.75%2010%202.75ZM10%204.25C6.82436%204.25%204.25%206.82436%204.25%2010C4.25%2013.1756%206.82436%2015.75%2010%2015.75C13.1756%2015.75%2015.75%2013.1756%2015.75%2010C15.75%206.82436%2013.1756%204.25%2010%204.25Z%22%20fill%3D%22%23174AE4%22%20%2F%3E%0D%0A%3C%2Fsvg%3E)}.b_dark #qs_searchIconInner{background-image:url(data:image/svg+xml,%3Csvg%20width%3D%2224%22%20height%3D%2224%22%20viewBox%3D%220%200%2024%2024%22%20fill%3D%22none%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%3E%0D%0A%20%20%20%20%3Cpath%20d%3D%22M10%202.75C14.0041%202.75%2017.25%205.99594%2017.25%2010C17.25%2011.7319%2016.6427%2013.3219%2015.6295%2014.5688L20.5303%2019.4697C20.8232%2019.7626%2020.8232%2020.2374%2020.5303%2020.5303C20.2641%2020.7966%2019.8474%2020.8208%2019.5538%2020.6029L19.4697%2020.5303L14.5688%2015.6295C13.3219%2016.6427%2011.7319%2017.25%2010%2017.25C5.99594%2017.25%202.75%2014.0041%202.75%2010C2.75%205.99594%205.99594%202.75%2010%202.75ZM10%204.25C6.82436%204.25%204.25%206.82436%204.25%2010C4.25%2013.1756%206.82436%2015.75%2010%2015.75C13.1756%2015.75%2015.75%2013.1756%2015.75%2010C15.75%206.82436%2013.1756%204.25%2010%204.25Z%22%20fill%3D%22%23A2B7F4%22%20%2F%3E%0D%0A%3C%2Fsvg%3E)}#qs_chatIconInner{background-image:url(/rp/h3Ueufj9JSjh37HfPlWZlGd5qzs.svg)}#qs_copyIconInner{background-image:url(/rp/Om7UMSlV70RivPZPDFNo-QYcRsc.png)}#qs_mapIconInner{background-image:url(/rp/PoyOAiBkKelhSYdnJauQl8gP6Sw.png)}.sw_close{display:inline-block;position:relative;overflow:hidden;direction:ltr;height:12px;width:12px}.sw_close:after{display:inline-block;transform:scale(.5);transform-origin:-218px -40px}.sw_meIc,.sw_spd,.idp_ham,.idp_wlid{position:relative;overflow:hidden;direction:ltr}.sw_meIc:after,.idp_ham:after,.idp_wlid:after{position:relative;transform:scale(.5);display:inline-block}.idp_ham{height:14px;width:20px;vertical-align:top;top:17px}.idp_ham:focus{outline-style:solid;outline-offset:5px}.idp_ham:after{transform-origin:-274px -40px}.idp_ham:hover:after,.idp_ham:active:after,.idp_ham:focus:after{transform-origin:-318px -40px}.idp_wlid,.sw_meIc{height:18px;width:18px}.idp_wlid:after{transform-origin:-48px 0}.rh_reedm .sw_meIc:after{transform-origin:-94px 0}.sw_meIc:after{transform-origin:-58px 0}.sw_spd:after{transform-origin:-362px -28px}.sw_meIc:after,.idp_ham:after,.idp_wlid:after{content:url(/rp/Qk540QN8GIaMmT8oEUEVF6N8IF8.png)}.b_searchboxForm,.sa_as .sa_drw{background-color:#fff}.b_searchboxForm .b_searchboxSubmit{background-color:#fff;border-color:#fff}.b_scopebar,.b_scopebar a,.b_scopebar a:visited,.id_button,.id_button:visited{color:#444}.b_scopebar .b_active a,.b_scopebar a:hover,.id_button:hover{color:#444}.b_idOpen a#id_l,a#id_rh.openfo{color:#333}#bepfo,#id_d{color:#333;background-color:#fff}.wpc_bub a{color:#4007a2}#sw_as{color:#444}.sa_tm strong{color:inherit}.sa_hv{background:#ececec}.sa_hd{color:inherit}#b_header{padding:18px 0 0 0;background-color:#fff;background:#fff}#b_header #sb_form,.b_logoArea,.b_logo,.b_searchboxForm,.id_button,.id_avatar,.idp_ham,.b_scopebar li,.b_scopebar a{display:inline-block}#b_header #sb_form{margin-right:10px}.b_searchbox{width:490px;margin:1px 0 1px 1px;padding:0 10px 0 19px;border:0;max-height:none;outline:none;box-sizing:border-box;height:44px;vertical-align:top;border-radius:6px;background-color:transparent}.b_searchboxSubmit{height:40px;width:40px;text-indent:-99em;border-width:0;border-style:solid;margin:3px 3px 3px 7px;background-position:-762px 0;-webkit-transform:scale(.45);-ms-transform:scale(.45);transform:scale(.45)}#sw_as{width:auto;position:relative;z-index:6}.sa_as{position:absolute;width:100%}#sa_ul div.sa_tm,#sa_ul .sa_hd{margin-left:20px}#sw_as #sa_ul li.pp_tile{padding-left:20px}.sa_hd{padding-top:5px}.b_searchboxSubmit,.sa_sg{cursor:pointer}#sb_form_q::-webkit-search-cancel-button{display:none}#b_header .b_scopebar .b_active,#b_results .b_pag a.sb_pagS_bp{border-color:#174ae4}#b_header #rh_animcrcl.serp.anim,#b_header .rwds_svg.serp circle{stroke:#174ae4}#b_header #rh_meter_heart path,#b_header #rh_animpath.serp.anim,#b_header .rh_reedm .rhfill.serp .medal,#b_header .rhlined.serp .medal{fill:#174ae4}.b_searchboxForm{box-shadow:0 0 0 1px rgba(0,0,0,.05),0 2px 4px 1px rgba(0,0,0,.09);border-radius:6px;border-left:1px solid transparent;border-right:none;border-top:1px solid transparent;border-bottom:1px solid transparent}.b_idOpen #id_d,#bepfo,#id_hbfo.slide_down{box-shadow:0 0 0 1px rgba(0,0,0,.1),0 2px 4px 1px rgba(0,0,0,.18);border-radius:6px}#sw_as #sa_ul:not(:empty){box-shadow:0 0 0 1px rgba(0,0,0,.1),0 2px 4px 1px rgba(0,0,0,.18)}.b_searchboxForm:hover,.b_focus .b_searchboxForm{box-shadow:0 0 0 1px rgba(0,0,0,.1),0 2px 4px 1px rgba(0,0,0,.18);border-left:1px solid transparent;border-right:none;border-top:1px solid transparent;border-bottom:1px solid transparent}.as_on .b_searchboxForm{border-radius:6px 6px 0 0}@media screen and (-ms-high-contrast:active){.b_idOpen #id_d{border:1px solid #fff}}@media screen and (-ms-high-contrast:black-on-white){.b_idOpen #id_d{border:1px solid #000}}#sw_as #sa_ul:not(:empty),#sw_as li:last-of-type.sa_hv{border-bottom-left-radius:6px;border-bottom-right-radius:6px}.spl-headerbackground{border-radius:6px 6px 0 0}body,#b_header{min-width:1204px}#id_h{display:block;position:relative;float:right;text-align:right;margin:0;line-height:50px;right:40px}.id_button{margin:0 8px;vertical-align:top}#id_rh,#id_rbh{padding:0 4px 0 24px;margin:0}.sw_spd{height:64px;width:64px;border-radius:50%;top:-7px;background-repeat:no-repeat;background-image:url(/rp/Qk540QN8GIaMmT8oEUEVF6N8IF8.png);transform:scale(.5);background-position:-362px -28px;margin:0 -16px 0 -8px;vertical-align:top}.sw_meIc{vertical-align:top;margin:16px 0 0 16px}#bepfo,#bepfm,#bepfl{width:320px}#bepfm{display:block}#bepfl{text-align:center;margin:50px 0}#bepfo{position:absolute;right:0;z-index:6;text-align:left}.idp_ham{margin:0 20px 0 16px;height:14px;width:20px}.b_scopebar{padding:0;margin:7px 0 0 var(--lgutter);border-bottom:none}#b_header{border-bottom:1px solid #ececec}.blue2#miniheader .b_scopebar ul{height:33px;overflow-y:hidden}.b_scopebar ul{height:39px;overflow-y:hidden}.b_scopebar li{padding:3px 0;margin:0 12px;line-height:25px;font-size:11px;letter-spacing:initial}.b_scopebar>ul li{text-transform:uppercase}.b_scopebar a{padding:0 8px}.b_scopebar .b_active{border-bottom:3px solid #00809d}#b_header .b_topbar,#b_header .b_scopebar{background:none;overflow-y:inherit}#b_header .b_topbar{margin-bottom:0}#b_header .b_scopebar{margin-bottom:0}.b_scopehide{content-visibility:hidden}.b_logo{font-family:"Arial",Helvetica,Sans-Serif}a,#b_header a,#b_header a:hover,.b_toggle,.b_toggle:hover{text-decoration:none}input{font:inherit;font-size:100%}.b_searchboxForm{font:18px/normal "Arial",Helvetica,Sans-Serif}.b_searchbox{font-size:16px}.id_button{line-height:50px;height:50px}.b_scopebar .b_active a{font-weight:600}.b_scopebar,.b_scopebar li{line-height:30px}.sa_tm{line-height:36px}.b_scopebar li{vertical-align:top}#sa_ul,.pp_title{font:16px/normal "Arial",Sans-Serif}#sa_ul .sa_hd{color:#444;font:600 13px/16px 'Arial',Sans-Serif;cursor:default;text-transform:uppercase;font-weight:bold}#sw_as strong{font-weight:bold}z{a:1}#sb_go_par{display:inline-block}#hp_container #sb_go_par{display:inline}#sb_go_par:hover::before,#sb_go_par.shtip::before,#sb_go_par[vptest]::before{bottom:-15px;left:26px;z-index:6}#sb_go_par:hover::after,#sb_go_par.shtip::after,#sb_go_par[vptest]::after{top:52px;left:26px;z-index:4}#miniheader #sb_go_par:hover::before,#miniheader #sb_go_par:hover::after{left:18px}*[data-sbtip]{position:relative}[vptest]::after,*[data-sbtip]:not(.disableTooltip):hover::after,*[data-sbtip].shtip:not(.disableTooltip)::after{position:absolute;background-color:#666;content:attr(data-sbtip);font:13px/18px Arial,Helvetica,sans-serif;white-space:nowrap;color:#fff;padding:10px 15px;transform:translateX(-50%);box-shadow:0 0 0 1px rgba(0,0,0,.06),0 4px 12px 1px rgba(0,0,0,.14);border-radius:4px}[vptest]::before,*[data-sbtip]:not(.disableTooltip):hover::before,*[data-sbtip].shtip:not(.disableTooltip)::before{position:absolute;background-color:#666;width:12px;height:12px;content:"";transform:translateX(-50%) rotate(45deg)}.mic_cont.partner [data-sbtipx]:hover::before{bottom:-29px;left:10px}.mic_cont.partner [data-sbtipx]:hover::after{top:38px;left:10px}.disableTooltip *[data-sbtip]:hover::before,.disableTooltip *[data-sbtip]:hover::after,.disableTooltip *[data-sbtip].shtip::before,.disableTooltip *[data-sbtip].shtip::after,.as_on *[data-sbtip]:hover::before,.as_on *[data-sbtip]:hover::after,.as_on *[data-sbtip].shtip::before,.as_on *[data-sbtip].shtip::after,.focus_hi *[data-sbtip]:hover::before,.focus_hi *[data-sbtip]:hover::after,.focus_hi *[data-sbtip].shtip::before,.focus_hi *[data-sbtip].shtip::after{display:none}#id_h #id_l{margin-right:0;display:inline-block}#id_a{vertical-align:top;position:relative;top:8px}#id_rh,#id_rbh{padding-left:24px}.idp_ham{margin-left:20px}z{a:1}::-webkit-search-decoration,::-webkit-search-cancel-button,.b_searchbox{-webkit-appearance:none}#b_skip_to_content{display:flex;flex-direction:row;justify-content:flex-end;align-items:center;padding:4px 4px 4px 19px;position:absolute;left:0;top:24px;background:#ececec;box-shadow:0 1.2px 3.6px rgba(0,0,0,.1),0 6.4px 14.4px rgba(0,0,0,.13);border-radius:0 28px 28px 0;z-index:1000}#b_skip_to_content:not(:focus){padding:0;clip:rect(1px,1px,1px,1px)}#b_skip_to_content .text-back{display:flex;flex-direction:row;align-items:flex-start;background:#fff;border-radius:16px;flex:none;order:0;flex-grow:0}#b_skip_to_content .text{font-weight:500;font-size:13px;line-height:16px;color:#001ba0;flex:none;order:0;flex-grow:0;margin:8px 17px}.b_searchbox { width: 571px; } .hasmic .b_searchbox { width: 535px; } .hassbi .b_searchbox { width: 535px; } .hasmic.hassbi .b_searchbox { width: 499px; } #b_header .b_searchboxForm .b_searchbox.b_softkey { width: 533px; } #b_header .hasmic .b_searchboxForm .b_searchbox.b_softkey { width: 497px; } #b_header .hassbi .b_searchboxForm .b_searchbox.b_softkey { width: 497px; } #b_header .hasmic.hassbi .b_searchboxForm .b_searchbox.b_softkey { width: 461px; } .b_searchboxForm.as_rsform .b_searchbox { width: 897px; } .hasmic .b_searchboxForm.as_rsform .b_searchbox { width: 861px; } .hassbi .b_searchboxForm.as_rsform .b_searchbox { width: 861px; } .hasmic.hassbi .b_searchboxForm.as_rsform .b_searchbox { width: 825px; } #b_header .b_searchboxForm.as_rsform .b_searchbox.b_softkey { width: 859px; } #b_header .hasmic .b_searchboxForm.as_rsform .b_searchbox.b_softkey { width: 823px; } #b_header .hassbi .b_searchboxForm.as_rsform .b_searchbox.b_softkey { width: 823px; } #b_header .hasmic.hassbi .b_searchboxForm.as_rsform .b_searchbox.b_softkey { width: 787px; }a{z:1}a{z:1}#b_header .b_searchboxForm,#uaanswer .b_searchboxForm{border-radius:24px}#b_header .b_searchbox,#uaanswer .b_searchboxForm{border-radius:24px}.as_on #b_header .b_searchboxForm{border-radius:24px 24px 0 0}.as_no_s #b_header .b_searchboxForm,.as_nw #b_header .b_searchboxForm{border-radius:24px}#b_header #sw_as{top:1px}#b_header #sw_as #sa_ul,#uaanswer #sw_as #sa_ul,#sw_as #sa_ul li:last-of-type{border-radius:0 0 24px 24px}.sb_bpr{height:44px;line-height:40px}#sb_clt.sb_clrhov{display:inline-block;overflow:visible;top:0;margin:3px 0;vertical-align:middle}#sb_clt #sw_clx{padding:9px;height:22px;display:inline-block;line-height:20px}#sb_clt[data-sbtipx]:hover::before{bottom:-15px;left:50%}#sb_clt[data-sbtipx]:hover::after{left:50%;top:49px}#b_header .hasmic .mic_cont.partner{margin:3px 0;vertical-align:middle}#b_header .mic_cont.partner .mic_icon{margin:11px 9px}#b_header .mic_cont.partner .sb_icon{font-size:0;height:18px;width:18px}#b_header .mic_cont.partner .b_icon{width:36px;z-index:4}#b_header .mic_cont.partner .b_icon:hover::before{bottom:-15px;left:12px;position:absolute;background-color:#666;width:12px;height:12px;content:"";transform:rotate(45deg);z-index:6}#b_header .mic_cont.partner .b_icon:hover::after{top:49px;left:18px;position:absolute;background-color:#666;content:attr(data-sbtipx);font:13px/18px Arial,Helvetica,sans-serif;white-space:nowrap;color:#fff;padding:10px 15px;box-shadow:0 0 0 1px rgba(0,0,0,.06) 0 4px 12px 1px rgba(0,0,0,.14);border-radius:4px;z-index:4}#b_header .b_searchboxForm #sbiarea{margin:3px 0;vertical-align:middle}#b_header .b_searchboxForm #sb_sbi{padding:11px 9px;font-size:0}#b_header .b_searchboxForm #sb_sbip:not(.disableTooltip):hover::before,#b_header .b_searchboxForm #sb_sbip.shtip:not(.disableTooltip)::before,#b_header .b_searchboxForm #sb_sbip[vptest]::before{bottom:-15px;left:18px}#b_header .b_searchboxForm #sb_sbip:not(.disableTooltip):hover::after,#b_header .b_searchboxForm #sb_sbip.shtip:not(.disableTooltip)::after,#b_header .b_searchboxForm #sb_sbip[vptest]::after{top:49px;left:18px}#b_header .b_searchboxForm #sb_search{display:inherit;vertical-align:middle;border-radius:50%;margin:3px 7px 3px 0}#sb_search,#sb_search a{display:inline-block}#b_header .b_searchboxForm #sb_search{position:relative}#b_header .b_searchboxForm #sb_search #b_icon_spyglass{position:absolute;cursor:pointer;pointer-events:none}#b_header .b_searchboxForm #sb_search #b_icon_spyglass:not(svg){background-position:-762px 0;width:36px;height:36px;transform:scale(.5);top:2px;left:2px}.b_searchboxForm #sb_search svg#b_icon_spyglass{fill:#00809d;width:calc(36px*.5);height:calc(36px*.5);padding:11px}#b_header .b_searchboxForm .b_searchboxSubmit{margin:0;transform:none;background-image:none;background-color:transparent;width:40px;height:40px;border-radius:50%}#b_header #sb_go_par:hover::before,#b_header #sb_go_par.shtip::before,#b_header #sb_go_par[vptest]::before{bottom:-15px;left:20px}#b_header #sb_go_par:hover::after,#b_header #sb_go_par.shtip::after,#b_header #sb_go_par[vptest]::after{top:49px;left:20px}#b_header .b_searchboxForm #sb_search #b_icon_spyglass:not(svg){background-position-y:-40px}a{z:1}#b_header .b_searchboxForm #sb_search #b_icon_spyglass:not(svg){background-image:url(/rp/Qk540QN8GIaMmT8oEUEVF6N8IF8.png)}.b_logo:after{position:absolute}.b_logo:after{content:url(/rp/Qk540QN8GIaMmT8oEUEVF6N8IF8.png)}.b_logo{transform:none}.b_logo:after{transform-origin:-48px 0}#detailPage .b_logo:after,#bnp_cookie_banner .b_logo:after{transform-origin:-48px 0}@media(forced-colors:active){.b_logo{forced-color-adjust:none;background-color:#000}}.b_logoArea{text-align:right;width:140px;height:40px;margin:0 20px 0 0;vertical-align:top}.b_logo{text-align:left;vertical-align:top;position:relative;display:inline-block;width:116px;height:18px;margin-top:15px;text-indent:0;overflow:hidden;direction:ltr}.b_logo:after{position:relative;display:inline-block;transform:scale(.5)}#detailPage .b_logo,#bnp_cookie_banner .b_logo{transform:none}@media(forced-colors:active){.b_logo{forced-color-adjust:none;background-color:#000}}.b_logo:after{content:url(/rp/Qk540QN8GIaMmT8oEUEVF6N8IF8.png)}#b_header .b_searchboxForm #sb_search{margin:3px 4px 3px 3px;height:40px}#b_header .b_searchbox{border-radius:0;margin-left:-15px;margin-right:6px}#b_header #sb_form:not(.hassbi,.hasmic) .sb_clrhov,#b_header.b_focus #sb_form:not(.hassbi,.hasmic) .b_searchboxForm .sb_clrhov{margin-right:10px}.hassbi #sb_clt.sb_clrhov,.hasmic #sb_clt.sb_clrhov{margin-right:0}#b_header #sb_clt.sb_clrhov+#vkeyIcon{margin-left:-10px;margin-right:10px}#b_header .hassbi #sb_clt.sb_clrhov+#vkeyIcon,#b_header .hasmic #sb_clt.sb_clrhov+#vkeyIcon{margin-left:0;margin-right:0}#b_header .b_searchboxForm #sbiarea{margin-right:10px}#b_header .hasmic:not(.hassbi) .mic_cont.partner{margin-right:10px}div:not(#sb_search) .b_searchboxSubmit{margin-left:5px}#b_header .b_searchboxSubmit~#sw_clx{right:2px}#b_header .b_searchboxSubmit~.b_searchbox{margin-right:8px}.b_searchboxSubmit{background-repeat:no-repeat}.b_searchboxSubmit{background-image:url(/rp/Qk540QN8GIaMmT8oEUEVF6N8IF8.png)}#sb_clt{display:inline;margin:0 6px 0 0;top:-1px}.sb_clrhov{visibility:hidden}.b_searchboxForm.sh-b_searchboxForm #sb_clt,#detailheader #sb_clt{display:none}body.b_sbText #b_header .b_searchboxForm:hover .sb_clrhov,body.b_sbText.as_on #b_header .sb_clrhov,body.b_sbText #b_header.b_focus .b_searchboxForm .sb_clrhov{visibility:visible}*[data-sbtipx]:hover::after{position:absolute;top:40px;left:6px;background-color:#666;content:attr(data-sbtipx);font:13px/18px Arial,Helvetica,sans-serif;white-space:nowrap;color:#fff;padding:10px 15px;transform:translateX(-50%);box-shadow:0 0 0 1px rgba(0,0,0,.06),0 4px 12px 1px rgba(0,0,0,.14);border-radius:4px;z-index:4}*[data-sbtipx]:hover::before{position:absolute;bottom:-27px;left:6px;background-color:#666;width:12px;height:12px;content:"";transform:translateX(-50%) rotate(45deg);z-index:6}.as_on *[data-sbtipx]:hover::before,.as_on *[data-sbtipx]:hover::after{display:none}.b_searchboxForm{position:relative}.mic_cont{cursor:pointer;display:inline-block}.mic_cont #vs_mic_icon{margin:11px 9px;height:18px;width:18px}.mic_cont #vs_mic_icon:not(svg){background-size:463px 46px;background-repeat:no-repeat;background-position:-361px 0;z-index:1;float:left}.mic_cont svg#vs_mic_icon{position:absolute;fill:#00809d}.mic_cont .ovr_cont{width:36px;height:40px}.mic_cont.partner{vertical-align:middle}.mic_cont .disableDefaultTooltip[data-tooltip]:hover::before,.mic_cont .disableDefaultTooltip[data-tooltip]:hover::after{display:none}.mic_cont .b_icon{position:relative;height:100%;visibility:hidden}.mic_cont .b_icon .icon_path{fill:#00809d}.mic_cont #pv_tooltip,.mic_cont #pvl_tooltip{padding:18px;background:#fff;position:absolute;z-index:1001;width:220px;border-radius:4px;box-shadow:0 0 0 1px rgba(0,0,0,.05),0 4px 12px 1px rgba(0,0,0,.14);font-family:Arial,Helvetica,Sans-Serif;font-size:13px;color:#444;left:-192px;top:48px;cursor:auto}.mic_cont .b_tooltip_point{transform:rotate(45deg);display:block;width:14px;height:14px;background:#fff;margin-top:-27px;border:1px solid #f5f5f5;border-bottom:transparent;border-right:transparent;left:202px;position:absolute;z-index:-1;box-shadow:-1px -1px 0 1px rgba(0,0,0,.01),-1px -1px 0 1px rgba(0,0,0,.01)}.mic_cont .b_tooltip_close_btn{position:absolute;right:15px;top:15px;cursor:pointer;z-index:1}.mic_cont .b_tooltip_title{font-size:16px;font-family:'Segoe UI',Arial,Helvetica,Sans-Serif;text-align:left;text-overflow:ellipsis;overflow:hidden;white-space:nowrap;margin-bottom:12px}.mic_cont .b_tooltip_description{text-align:left;color:#666;margin-bottom:10px;font-size:14px}.mic_cont .b_tooltip_close{width:15px;height:15px}.mic_cont .b_pvl_cont{width:fit-content;height:112px;background-color:#f5f5f5;padding:25px;cursor:pointer;font:13px/20px 'Roboto',Helvetica,Sans-Serif;color:#00809d;margin:auto;margin-bottom:10px}.mic_cont .b_pvl_icon{width:64px;height:64px;margin:auto;margin-bottom:4px}.mic_cont .b_pvl_cont span{font:13px/20px 'Roboto',Helvetica,Sans-Serif;color:#767676;text-align:center;display:block}.mic_cont hr.b_tooltip_divider{border-top:1px solid #bbb;border-radius:5px;margin-top:12px;margin-bottom:12px}.mic_cont.icon .b_icon{visibility:visible}.mic_cont .b_allowOverlay svg{height:unset;width:unset}.read_icon{width:100%;height:100%}@keyframes small_l{0%{transform:scaleY(1)}100%{transform:scaleY(7)}}.b_read_sl{animation:small_l .5s alternate infinite;transform-origin:center;transition:all .5s}@keyframes large_l{0%{transform:scaleY(1)}100%{transform:scaleY(.1429)}}.b_read_ll{animation:large_l .5s alternate infinite;transform-origin:center;transition:all .5s}@keyframes med_l{0%{transform:scaleY(1)}50%{transform:scaleY(1.8)}100%{transform:scaleY(.55)}}.b_read_ml{animation:med_l .5s alternate infinite;transform-origin:center;transition:all .5s}.b_allowOverlay{position:fixed;background:rgba(0,0,0,.7);height:100%;width:100%;top:0;left:0;z-index:6;cursor:auto}.b_ov_blocked .b_ov_perms{width:131px;height:109px;position:absolute;right:20px}.b_ov_blocked .b_ov_permZoom{height:100px;width:100px;border-radius:50px;position:absolute;top:0;left:unset;right:53px}.b_ov_blocked .b_ov_click{top:33px;left:3px}.b_ov_blocked .b_ov_click2{top:-25px;left:unset;right:27px}.b_ov_blocked .b_ov_textCont{left:63px;top:54px}.b_ov_blocked .b_ov_txt_lst{margin-left:18px}.b_ov_blocked .b_ov_txt_lst_itm{list-style:decimal}.b_overlayCont{position:relative;width:600px;height:394px;margin:100px auto auto auto;background-color:#c4c4c4;border-radius:10px;overflow:hidden}.rms_image{height:100%;width:100%;display:block}.b_ov_dots{position:absolute;padding:7px 0 0 13px;cursor:pointer}.b_ov_close{width:12px;height:12px;position:absolute;right:8px;top:6px;cursor:pointer}.b_ov_sb{background-color:#e8e8e8;height:30px;margin-top:23px;overflow:auto}.b_ov_sb .b_ov_bar .b_ov_micx{height:10px;width:8px;position:absolute;right:29px;top:3px}.b_ov_sb .b_ov_bar .b_ov_star{height:10px;width:9px;position:absolute;right:11px;top:2px}.b_ov_bar{background-color:#fff;height:16px;width:580px;margin:7px 0 0 10px;border-radius:4px;position:relative}.b_ov_body{height:341px;background-color:#114a8b;position:relative;border-radius:0 0 9px 9px}.b_ov_perms{width:125px;height:51px;margin-left:15px}.b_ov_permZoom{height:128px;width:128px;border-radius:64px;position:absolute;top:-15px;left:30px;box-shadow:0 2px 4px 1px rgba(0,0,0,.14);-webkit-box-shadow:0 2px 4px 1px rgba(0,0,0,.14);overflow:hidden}.b_ov_click{width:30px;height:30px;position:absolute;top:70px;left:70px}.b_ov_hand{width:30px;height:29px;position:absolute;top:15px;left:15px}.b_ov_hand_step{position:absolute;left:16px;top:6px;color:#000}.b_ov_textCont{position:absolute;left:160px;top:85px}.b_ov_mic{height:50px;width:35px;margin-bottom:23px}.b_ov_mic .icon_path{fill:#fff}.b_ov_title{color:#fff;font-size:24px;line-height:28px;margin-bottom:20px}.b_ov_text{color:#fff;font-size:16px;line-height:18px;font-weight:100;font-style:normal}.sb_bpr~.mic_cont #vs_mic_icon:not(svg){background-position:-361px 0}.sb_bpr~.mic_cont svg#vs_mic_icon,.sb_bpr~.mic_cont .icon_path{fill:#111}.b_speech_overlay{cursor:default;z-index:1005}.b_speech_cont{left:0;top:0;width:100%;height:200px;z-index:5;overflow:hidden;position:absolute;background:#fff;border:1px solid #ccc;box-sizing:border-box;box-shadow:0 4.8px 14.4px rgba(0,0,0,.18),0 25.6px 57.6px rgba(0,0,0,.12);border-radius:6px}.b_speech_cont.b_speech_suggestions{height:auto}.b_speech_cont.b_speech_suggestions .b_speech_cont_main{height:200px}.b_speech_suggestions .b_mic_close{top:100px}.b_mic_close{cursor:pointer;height:102px;width:102px;top:50%;right:19px;transform:translate3d(0,-50%,0);position:absolute;transition:transform .2s;background:none;border:none;padding:0}.b_mic_close:hover{transform:translate3d(0,-50%,0) scale(1.1)}.b_mic_close:active{transform:translate3d(0,-50%,0)}.b_speech_suggestion_container{padding:3%;padding-top:0}.b_suggestion_item{display:inline-block;border:1px solid #ddd;border-radius:36px !important;padding:0 16px 0 12px;cursor:pointer;height:36px;background-color:#fff;vertical-align:middle;width:100%;text-align:left}.b_suggestion_item:hover{background-color:#ececec}.b_suggestion_item:hover .b_suggestion_item_text{color:#444}.b_suggestion_item .b_suggestion_item_text{font:16px/normal 'Roboto',Sans-Serif;line-height:36px;text-overflow:ellipsis;white-space:nowrap;margin:0 10px;color:#767676}@keyframes wave{0%{transform:rotatex(0deg)}100%{transform:rotatex(40deg)}}.b_mic_wave{animation:wave .5s alternate infinite;transform-origin:center bottom;transition:all .5s}@keyframes pulse{0%{transform:scale(1)}100%{transform:scale(1.25)}}.b_mic_pulse{animation:pulse .5s alternate infinite;transform-origin:center;transition:all .5s}.b_speech_suggestions .b_rec_text{top:100px}.b_rec_text{width:80%;position:absolute;font-style:normal;font-weight:normal;font-size:30px;line-height:35px;color:#404040;top:50%;padding:8px 32px;transform:translate3d(0,-50%,0);white-space:normal}.b_rec_placeholder{color:#767676}#b_noMic{display:inline-block}#b_noMic:hover{text-decoration:underline}.sbox .sb_form .mic_cont #vs_mic_icon.mic_icon{width:29px;height:22px;background-size:22px 22px;background-repeat:no-repeat;background-position:center;margin:0}.sbox .sb_form .mic_cont .ovr_cont{position:absolute;top:0;left:0;height:100%;width:100%}.b_searchboxForm .b_speech_cont{left:-3px;top:-3px;width:calc(100% - -3px - -3px);box-shadow:0 0 0 1px rgba(0,0,0,.1),0 2px 4px 1px rgba(0,0,0,.18);border-left:1px solid transparent;border-right:none;border-top:1px solid transparent;border-bottom:1px solid transparent}.mic_cont.icon.partner{background:transparent;padding:0}.mic_cont #vs_mic_icon:not(svg){background-image:url(/rp/Qk540QN8GIaMmT8oEUEVF6N8IF8.png)}.mic_cont .b_tooltip_close{background-image:url(/rp/fdVZU4ttbw8NDRm6H3I5BW3_vCo.svg)}.sbox .sb_form .mic_cont #vs_mic_icon.mic_icon{background-image:url(/rp/yFxRwxrSg4AefXfEJUKsQSDhvz8.png)}#sbiarea{display:inline-block}#sb_sbi{display:inline-block;cursor:pointer;vertical-align:middle;position:relative;width:18px;height:18px}:not(svg)#sbi_b{background-position:-682px 0;width:36px;height:36px;transform:scale(.5);position:absolute;top:2px;left:0}svg.sb_icon{fill:#00809d}#sb_sbip[shdlg]:hover::before,#sb_sbip[shdlg]:hover::after,#sb_sbip[shdlg].shtip::before,#sb_sbip[shdlg].shtip::after{display:none}:not(svg)#sbi_b{background-image:url(/rp/Qk540QN8GIaMmT8oEUEVF6N8IF8.png)}.phead #chat_upsell_bubble_icon{display:none}#b_phead_chat{display:inline-block;vertical-align:top}.b_sydConvMode #sb_form #chat_upsell_bubble_icon{transition-delay:0s,0s;opacity:0;visibility:hidden}.phead_border .b_phead_chat_link{display:none}.chat_upsell_scope_bar_hidden{display:none}#sb_form #chat_upsell_bubble_icon.b_hide{display:none}#sb_form #chat_upsell_bubble_icon{margin-left:16px;font-size:11px;color:#444;display:inline-block;vertical-align:top;margin-top:4px;cursor:pointer;width:40px;height:40px;background:url(/rp/em88jYr3ZOv7yX3AqoOU5z8EEnA.png) no-repeat;background-size:40px 40px;transition-property:opacity,visibility;transition-delay:1000ms,1000ms;transition-duration:0s;transition-timing-function:cubic-bezier(.75,0,.25,1);opacity:1;visibility:visible}@media(max-width:1232px){#sb_form .b_phead_chat_link{display:none}}#sb_form #chat_upsell_bubble_icon:hover{background:url(/rp/wyumpKcxMMmwB6w11Xy29cAPKig.png) no-repeat;background-size:40px 40px}.b_dark #sb_form #chat_upsell_bubble_icon{border-color:#545250;color:#edebe9;background:url(/rp/em88jYr3ZOv7yX3AqoOU5z8EEnA.png) no-repeat;background-size:40px 40px}.b_dark #sb_form #chat_upsell_bubble_icon:hover{background:url(/rp/wyumpKcxMMmwB6w11Xy29cAPKig.png) no-repeat;background-size:40px 40px}@media(max-width:1640px){html #b_header.as_rsform .b_phead_chat_link{display:none}}@media(max-width:1440px){#id_n{white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:90px;display:inline-block}}.rwds_svg{vertical-align:top;display:inline-block}.rwds_svg.serp{margin:8px 0 0 8px}.rhlined,.rhfill{vertical-align:top;width:32px;height:32px}#id_rh,#id_rbh{position:relative}.id_button toolTip:after,#id_rh:after{white-space:pre-line !important;width:200px}.rhcoinflip{vertical-align:top;width:60px;height:60px;position:absolute;right:-6px;top:-6px}#idCont .rhcoinflip{position:absolute;right:-5px;top:-9px}#rh_meter{vertical-align:top;width:40px;height:40px;margin-left:-36px;margin-top:-4px}.rh_reedm .rhlined,.rhfill,.rh_reedm .meter,.rd_hide{display:none}.rhlined,.rh_reedm .rhfill,#rh_meter{display:inline-block}.noBg .rhlined.hp .meter,.noBg .rhfill.hp .meter,.rhlined.serp .meter,.rhfill.serp .meter{stroke:rgba(177,177,177,.4)}.rhlined.serp .medal{fill:#919191}.noBg .rh_reedm .rhfill.hp .medal,.rh_reedm .rhfill.serp .medal{fill:#00809d}#rh_animcrcl{fill:none;stroke:transparent;stroke-width:0}#rh_animcrcl.anim{stroke-width:2}.rh_scale .rhfill,.rh_scale #rh_meter{animation:scaling .4s cubic-bezier(.3,.55,.1,1)}@-webkit-keyframes scaling{0%{transform:scale(1,1)}50%{transform:scale(1.3,1.3)}100%{transform:scale(1,1)}}@keyframes scaling{0%{transform:scale(1,1)}50%{transform:scale(1.3,1.3)}100%{transform:scale(1,1)}}#id_rbh.serp{padding-right:0}#id_rbh.serp.reverse{padding-left:14px}#id_rbh.serp svg{height:25px;fill:#818285}@media(max-width:1362.9px){#id_h #id_rbh,#idCont #id_h #id_rbh{display:none}#id_rfob,#id_rfoc{display:none !important}}#b_header.bm_oneMap #id_rh.id_button.toolTip.rh_reedm{top:1px}#b_header.bm_oneMap #serp_medal_svg{margin-top:0 !important}#id_rfob{display:none;flex-direction:row;justify-content:center;align-items:center;position:relative}#id_rfoc{display:none;overflow:hidden;top:100%;position:absolute;z-index:10;right:60px;border-radius:4px;box-shadow:0 16px 32px rgba(0,0,0,.14);background-color:#fff;color:#444;height:600px;width:360px;align-items:center;justify-content:center}#id_rfb{margin-right:8px}#b_results .sb_add .b_rhonclick:hover{cursor:default}a{z:1}#langChange.langdisp{display:inline-block;vertical-align:top}@media(min-width:1390px) and (max-width:1545px){#sb_form .b_phead_chat_link{display:none}}@media(max-width:1810px){html #b_header.as_rsform .b_phead_chat_link{display:none}}@media all and (max-width:1399px){#langChange.langdisp{display:none}}#langChange a{color:#444}#langChange a:hover{color:#444}#langChange span{margin:0 10px}#langChange{color:#444;margin-right:14px}nav.b_scopebar li#b-scopeListItem-conv:nth-child(n+3) svg{display:none}#b-scopeListItem-conv .scp_conv_mode{height:16px;width:16px}#b-scopeListItem-web .scp_conv_mode{height:15px;width:15px}#b-scopeListItem-conv .scp_conv_mode,#b-scopeListItem-web .scp_conv_mode{margin-right:6px;background-size:100%;display:inline-block;vertical-align:middle;position:relative;text-indent:0;overflow:hidden;direction:ltr;transform:none}#b-scopeListItem-conv .scp_conv_mode::after{transform-origin:-878px 0;transform:scale(.5);position:relative;display:inline-block}.b_active#b-scopeListItem-conv .scp_conv_mode::after{transform-origin:-878px 0;transform:scale(.5);position:relative;display:inline-block}#b-scopeListItem-web .scp_conv_mode::after{transform-origin:-846px -34px;transform:scale(.5);position:relative;display:inline-block}.b_active#b-scopeListItem-web .scp_conv_mode::after{transform-origin:-846px -34px;transform:scale(.5);position:relative;display:inline-block}nav.b_scopebar{transform:translateY(0);transition-property:transform;transition-duration:0s;transition-delay:823ms;transition-timing-function:cubic-bezier(.75,0,.25,1);position:relative}.b_sydConvMode nav.b_scopebar{width:500px;transform:translateY(-50px);transition-delay:187ms;margin-top:7px}.b_sydConvMode #b_scope_container nav.b_scopebar{margin-top:0}.b_sydConvMode nav.b_scopebar li:nth-child(n+3){display:none;transition-delay:0s}.b_scopebar .b_active{border-bottom:none}.b_scopebar li::after{content:'';width:100%;height:3px;display:block;opacity:0;background:#174ae4;margin-top:3px}.b_scopebar li.b_active:not(#b-scopeListItem-conv)::after{opacity:1}.b_sydConvMode .b_scopebar #b-scopeListItem-web::after{opacity:1}.b_scopebar #b-scopeListItem-web{position:relative}.b_scopebar #b-scopeListItem-web::after{position:absolute;width:100%;transition-property:transform,width;transition-duration:500ms;transition-timing-function:cubic-bezier(.75,0,.25,1)}.b_sydConvMode .b_bcbSwitch nav.b_scopebar li:nth-child(n+2){display:none;transition-delay:0s}@media(forced-colors:active){.b_scopebar li::after{background:Highlight}}#b-scopeListItem-conv .scp_conv_mode::after,.b_active#b-scopeListItem-conv .scp_conv_mode::after,#b-scopeListItem-web .scp_conv_mode::after,.b_active#b-scopeListItem-web .scp_conv_mode::after{content:url(/rp/Qk540QN8GIaMmT8oEUEVF6N8IF8.png)}#b-scopeListItem-menu .b_sp_over_menu .b_scopebar_item{padding:0 0 0 10px}#b-scopeListItem-menu .b_sp_over_menu .b_scopebar_item a{text-transform:uppercase}.b_scopebar #b-scopeListItem-menu .b_sp_over_menu{margin:5px 0 0 14px;height:auto}.b_scopebar>ul li.b_hide{display:none}#b-scopeListItem-menu{vertical-align:initial;line-height:0;padding:3px 5px 0 5px;height:100%}#b-scopeListItem-menu svg{height:28px;width:18px;fill:#444;cursor:pointer}.b_scopebar.b_scope_dropdown_expanded{z-index:3}#b-scopeListItem-menu.focusin .b_sp_over_menu{transform:none;display:block;z-index:1002}#b-scopeListItem-menu .b_sp_over_menu{color:#444;cursor:pointer;transform:scale(0);display:none;position:absolute;background-color:#fff;border-radius:6px;padding:4px 0;margin:18px 0 0 -17px;box-shadow:0 4px 12px 1px rgba(0,0,0,.14);height:auto;overflow-y:unset;min-width:120px}#b-scopeListItem-menu .b_sp_over_item{font-weight:normal;font-size:13px;color:#444;text-align:left;padding:0 0 0 12px;margin:0;display:list-item}#b-scopeListItem-menu li.b_sp_over_item:hover{background:#f5f5f5;color:#111;border-bottom:none}#b-scopeListItem-menu .b_sp_over_item.divider{border-top:1px solid #ddd}#b-scopeListItem-menu .b_sp_over_item a{display:inline-block;width:100%;height:100%;padding:0;line-height:39px;text-transform:none}.b_sp_over_item .icon{background-repeat:no-repeat;background-size:1350%}.b_sp_over_item .icon{display:inline-block;vertical-align:middle;margin-right:12px;height:16px;width:16px}.b_sp_over_item .text{display:inline-block;vertical-align:middle}.b_sp_over_menu #outlook .icon{background-position:-50px 0}.b_sp_over_menu #excel .icon{background-position:0 0}.b_sp_over_menu #word .icon{background-position:-117px 0}.b_sp_over_menu #powerpoint .icon{background-position:-67px 0}.b_sp_over_menu #onenote .icon{background-position:-33px 0}.b_sp_over_menu #onedrive .icon{background-position:-16px 0}.b_sp_over_menu #sway .icon{background-position:-167px 0}.b_sp_over_menu #calendar .icon{background-position:-183px 0}.b_sp_over_menu #people .icon{background-position:-200px 0}.b_sp_over_item .icon{background-image:url(/rp/4HbL1kafp7kZCaGRQ_5yOMfALrQ.png)}.b_scopebar #b-scopeListItem-menu{padding:3px 0;line-height:30px;height:auto}.b_scopebar li:hover:not(.b_active)#b-scopeListItem-menu{border-bottom:none}.b_scopebar #b-scopeListItem-menu .b_sp_over_menu{margin:5px 0 0 14px}#b-scopeListItem-menu .b_sp_over_menu .b_scopebar_item a{font-size:11px}.b_scopebar #b-scopeListItem-menu svg{width:16px;height:16px;display:block}.b_scopebar #b-scopeListItem-menu .b_sp_menu_separ{width:16px;height:15px;display:inline-block;vertical-align:text-bottom}body:not(.b_sydConvMode) nav.b_scopebar li#b-scopeListItem-notebook{display:none}.b_sydConvMode nav.b_scopebar li#b-scopeListItem-notebook{display:inline-block;visibility:visible;opacity:1}.b_sydConvMode nav.b_scopebar li#b-scopeListItem-notebook svg{margin-right:8px;transform:translateY(5px);fill:#174ae4}nav.b_scopebar li.b_active#b-scopeListItem-notebook::after{opacity:0}.b_sydConvMode.b_notebookMode .b_sydShowConv nav.b_scopebar li#b-scopeListItem-notebook::after{opacity:1}.b_sydConvMode.b_notebookMode .b_sydShowConv nav.b_scopebar #b-scopeListItem-conv::after{opacity:0}.b_sydConvMode.b_notebookMode #b_bceBcbToggle .selected{pointer-events:auto}.tools_scope a{border-radius:20px;padding:0 12px}.tools_scope:not(.active):hover a{background-color:#ececec}.b_dark .tools_scope:not(.active):hover a{background-color:#323130}.tools_scope.active a{background:#e2e9ff;color:#174ae4;font-weight:700}.b_dark .tools_scope.active a{background:#a2b7f4;color:#1b1a19}.b_scopebar li.tools_scope:hover{border-bottom-color:transparent}</style><style type="text/css">.hasmic .mic_cont.partner{display:inline-block}.mic_cont.partner{margin:0 0 5px 18px}.b_deep ul{width:230px}#b_results{width:648px}html body,body #b_header{min-width:var(--bminwidth,1362px)}#b_context{width:432px}.b_poleContent{width:1160px}@media(max-width:1406.9px){#b_content{--rgutter:40px;--crleft:848px}}@media(max-width:883.9px){#b_content{--rgutter:20px;--crleft:828px}}@media(max-width:1362.9px){body{--bminwidth:1232px}#b_context{width:344px}.b_poleContent{width:1072px}#id_h #id_rbh,#idCont #id_h #id_rbh{display:none}}@media(max-width:1274.9px){body{--bminwidth:1164px}#b_context{width:256px}#b_header #id_h{right:0}#id_h #id_rh,#id_h #id_rbh{padding-left:8px}.b_poleContent{width:1004px}}@media(max-width:1189.9px){body.b_norr #id_h #id_n{display:none}body.b_norr{--bminwidth:1084px}}@media(max-width:838.9px){.b_respl #b_content{--lgutter:113px;--lgutterrev:-113px;--polepadl:133px;--polepadl2:133px;--dtabpadl:125px;--crleft:801px;--bzaimgoffsetx:-50px}}@media(max-width:791.9px){.b_respl #b_content{--lgutter:66px;--lgutterrev:-66px;--polepadl:86px;--polepadl2:86px;--dtabpadl:62px;--crleft:754px;--bzaimgoffsetx:-97px}}@media(max-width:743.9px){.b_respl #b_content{--lgutter:18px;--lgutterrev:-18px;--polepadl:38px;--polepadl2:38px;--dtabpadl:12px;--crleft:706px;--bzaimgoffsetx:-145px}}</style><script type="text/javascript" nonce="hdm+zCWukVbuQiu/8nwvTExR65VnfK54cOHZ35vK2Vk=">//<![CDATA[
var logJSText=function(n,t){t===void 0&&(t=null);(new Image).src=_G.lsUrl+'&Type=Event.ClientInst&DATA=[{"T":"CI.ClientInst","FID":"CI","Name":"'+escape(n)+(t?'","Text":"'+escape(t):"")+'"}]'},getHref=function(){return location.href};try{var ignErr=["ResizeObserver loop completed with undelivered notifications.","ResizeObserver loop limit exceeded"],rszMsg="ResizeObserverLimit",maxErr=3,ignoreCurrentError=function(n,t){return ignErr.indexOf(n)<0?!1:(window.WRC||logJSText(rszMsg),window.WRC=1,t!=null&&(typeof sj_sp!="undefined"&&sj_sp(t),typeof sj_pd!="undefined"&&sj_pd(t)),!0)},regexEsc=function(n){return n.replace(/([.?*+^$&[\]\\(){}|<>-])/g,"\\$1")};window.jsErrorHandler=function(n){var u,y,s,nt,tt,r,f,it,e,l,a,o;try{if(u='"noMessage"',y=(n.error||n).message||u,ignoreCurrentError(y,n))return;if(s=(window.ERC?window.ERC:0)+1,window.ERC=s,s>maxErr){logJSText("max errors reached");return}var h=n.error||n,p=n.filename,w=n.lineno,b=n.colno,k=n.extra,c=h.severity||"Error",d=h.message||u,i=h.stack,t='"'+escape(d.replace(/"/g,""))+'"',g=new RegExp(regexEsc(getHref()),"g");if(i){for(nt=/\(([^\)]+):[0-9]+:[0-9]+\)/g,r={};(tt=nt.exec(i))!==null;)f=tt[1],r[f]?r[f]++:r[f]=1;e=0;for(l in r)r[l]>1&&(a=regexEsc(l),it=new RegExp(a,"g"),i=i.replace(it,e),i+="#"+e+"="+a,e++);i=i.replace(g,"self").replace(/"/g,"");t+=',"Stack":"'+(escape(i)+'"')}if(p&&(t+=',"Meta":"'+escape(p.replace(g,"self"))+'"'),w&&(t+=',"Line":"'+w+'"'),b&&(t+=',"Char":"'+b+'"'),k&&(t+=',"ExtraInfo":"'+k+'"'),d===u)if(c="Warning",t+=',"ObjectToString":"'+n.toString()+'"',JSON&&JSON.stringify)t+=',"JSON":"'+escape(JSON.stringify(n))+'"';else for(o in n)n.hasOwnProperty(o)&&(t+=',"'+o+'":"'+n[o]+'"');var rt=(new Date).getTime(),ut='"T":"CI.'+c+'","FID":"CI","Name":"JS'+c+'","Text":'+t+"",ft="<E><T>Event.ClientInst<\/T><IG>"+_G.IG+"<\/IG><TS>"+rt+"<\/TS><D><![CDATA[[{"+ut+"}]]\]><\/D><\/E>",et="<ClientInstRequest><Events>"+ft+"<\/Events><STS>"+rt+"<\/STS><\/ClientInstRequest>",v=new XMLHttpRequest;v.open("POST","/fd/ls/lsp.aspx?",!0);v.setRequestHeader("Content-Type","text/xml");v.send(et);typeof sj_evt!="undefined"&&sj_evt.fire("ErrorInstrumentation",t)}catch(ot){logJSText("MetaJSError","Failed to execute error handler. "+ot.message)}};window.addEventListener&&window.addEventListener("error",window.jsErrorHandler,!1)}catch(e){logJSText("MetaJSError","Failed to bind error handler "+e.message)};var amd,define,require;(function(n){function e(n,i,u){t[n]||(t[n]={dependencies:i,callback:u},r(n))}function r(n){if(n){if(n)return u(n)}else{if(!f){for(var r in t)u(r);f=!0}return i}}function u(n){var s,e;if(i[n])return i[n];if(t.hasOwnProperty(n)){var h=t[n],f=h.dependencies,l=h.callback,a=r,o={},c=[a,o];if(f.length<2)throw"invalid usage";else if(f.length>2)for(s=f.slice(2,f.length),e=0;e<s.length;e++)c.push(u(s[e]));return l.apply(this,c),i[n]=o,o}}var t={},i={},f=!1;n.define=e;n.require=r})(amd||(amd={}));define=amd.define;require=amd.require;var _w=window,_d=document,sb_ie=window.ActiveXObject!==undefined,sb_i6=sb_ie&&!_w.XMLHttpRequest,_ge=function(n){return _d.getElementById(n)},_qs=function(n,t){return t=typeof t=="undefined"?_d:t,t.querySelector?t.querySelector(n):null},sb_st=function(n,t){return setTimeout(n,t)},sb_rst=sb_st,sb_ct=function(n){clearTimeout(n)},sb_gt=function(){return(new Date).getTime()},sj_gx=function(){return sb_i6?new ActiveXObject("MSXML2.XMLHTTP"):new XMLHttpRequest};_w.sj_ce=function(n,t,i){var r=_d.createElement(n);return t&&(r.id=t),i&&(r.className=i),r};_w.sj_cook=_w.sj_cook||{get:function(n,t){var i=_d.cookie.match(new RegExp("\\b"+n+"=[^;]+")),r;return t&&i?(r=i[0].match(new RegExp("\\b"+t+"=([^&]*)")),r?r[1]:null):i?i[0]:null}};_w.sk_merge||(_w.sk_merge=function(n){_d.cookie=n});_w.bbe="A:rms:answers:Shared:BingCore.Bundle";define("fallback",["require","exports"],function(n,t){function f(){return function(){for(var r,h,c,t=[],n=0;n<arguments.length;n++)t[n]=arguments[n];if(r=s(arguments.callee),u&&(h=e(r),h.toString()!=f().toString()))return h.apply(null,arguments);c=i[r].q;t[0]==="onPP"&&o();c.push(t)}}function s(n){for(var t in i)if(i[t].h===n)return t}function e(n,t){for(var u,e=n.split("."),i=_w,r=0;r<e.length;r++)u=e[r],typeof i[u]=="undefined"&&t&&(i[u]=r===e.length-1?f():{}),i=i[u];return i}function o(){var e=i["rms.js"].q,o,f,r,n,s,u,t;if(e.length>0)for(o=!1,f=0;f<e.length;f++){for(r=e[f],n=0;n<r.length;n++)if(s=_w.bbe,u=r[n][s],u||(u=r[n]["A:rmsBu0"]),u){t=_d.createElement("script");t.setAttribute("data-rms","1");t.setAttribute("crossorigin","anonymous");t.src=u;t.type="text/javascript";setTimeout(function(){_d.body.appendChild(t)},0);r.splice(n,1);o=!0;break}if(o)break}}function h(){var n,t,f;for(u=!1,n=0;n<r.length;n++)t=r[n],f=e(t,!0),i[t]={h:f,q:[]}}function c(){for(var t,n=0;n<r.length;n++){var o=r[n],s=i[o].q,h=e(o);for(t=0;t<s.length;t++)h.toString()!==f().toString()&&h.apply(null,s[t])}u=!0}function l(n,t,i,r){n&&((n===_w||n===_d||n===_d.body)&&t=="load"?_w.sj_evt.bind("onP1",i,!0):n.addEventListener?n.addEventListener(t,i,r):n.attachEvent?n.attachEvent("on"+t,i):n["on"+t]=i)}t.__esModule=!0;t.replay=void 0;var r=["rms.js","sj_evt.bind","sj_evt.fire","sj_jb","sj_wf","sj_cook.get","sj_cook.set","sj_pd","sj_sp","sj_be","sj_go","sj_ev","sj_ue","sj_evt.unbind","sj_et","Log.Log","sj_mo","sj_so"],i={},u=!1;_w.fb_is=o;t.replay=c;h();_w.sj_be=l});function lb(){_w.si_sendCReq&&sb_st(_w.si_sendCReq,800);_w.lbc&&_w.lbc()};(function(){function n(n){n=sb_ie?_w.event:n;(!n.altKey||n.ctrlKey||n.shiftKey)&&(n.key&&n.key==="Enter"||n.keyCode&&n.keyCode===13)&&_w.si_ct(sb_ie?n.srcElement:n.target,!1,n,"enter")}sj_be(document,"keydown",n,!1)})();(function(){function n(n){_w.si_ct(sb_ie?_w.event.srcElement:n.target,!1,_w.event||n);sj_evt&&sj_evt.fire("cbksent",n)}sj_be(document,"mousedown",n,!1);sj_evt.bind("sydFSCLoaded",function(){var t;((t=CIB===null||CIB===void 0?void 0:CIB.config)===null||t===void 0?void 0:t.bing)&&(CIB.config.bing.sendClickBeacon=n)},!0)})();_w.si_sbwu=function(n){var r=_G.BQIG==null?_G.IG:_G.BQIG,u="/fd/ls/GLinkPingPost.aspx?IG="+r+n,t="sendBeacon",i=!1;if(navigator&&navigator[t])try{navigator[t](u,"");i=!0}catch(f){}return i};ClTrCo={furl:!0};var ctcc=0,clc=_w.ClTrCo||{};_w.si_ct=function(n,t,i,r){var u,o,e,s,f,a,h,c,l;if(clc.SharedClickSuppressed)return!0;u="getAttribute";try{for(;n!==document.body;n=n.parentNode){if(!n||n===document||n[u]("data-noct"))break;if(o=(n.tagName==="A"||n[u]("data-clicks"))&&(n[u]("h")||n[u]("data-h")||n[u]("data-cspi"))||n[u]("_ct"),o){e=n[u]("_ctf");s=-1;i&&(i.type==="keydown"?s=-2:i.button!=null&&(s=i.button));e&&_w[e]||(e="si_T");e==="si_T"&&(f=n[u]("href"),_G!==undefined&&_G.EF!==undefined&&_G.EF.newtabredironclicktracking===1&&f.indexOf("/newtabredir")==0?(a=new RegExp("[?&]?url=([^&]*)(&|$)"),h=f.match(a),h&&(f=f.indexOf("&be=1")>=0?encodeURIComponent(atob(decodeURIComponent(h[1]))):h[1])):f=encodeURIComponent(n[u]("href")),clc.furl&&!n[u]("data-private")?o+="&url="+f:clc.mfurl&&(o+="&abc="+f));r&&(o+="&source="+r);c="";clc.mc&&(c="&c="+ctcc++);l="&"+o+c;_w.si_sbwu(l)||_w[e]&&_w[e](l,n,i,s);break}if(t)break}}catch(v){_w.SharedLogHelper?SharedLogHelper.LogWarning("clickEX",null,v):(new Image).src=_G.lsUrl+'&Type=Event.ClientInst&DATA=[{"T":"CI.Warning","FID":"CI","Name":"JSWarning","Text":'+v.message+"}]"}return!0};_w.si_sbwu||(_w.si_sbwu=function(){return!1}),function(){_w._G&&(_G.si_ct_e="click")}();var wlc_d = 1500, wlc_t =63844246198;;var perf;(function(n){function f(n){return i.hasOwnProperty(n)?i[n]:n}function e(n){var t="S";return n==0?t="P":n==2&&(t="M"),t}function o(n){for(var c,i=[],t={},r,l=0;l<n.length;l++){var a=n[l],o=a.v,s=a.t,h=a.k;s===0&&(h=f(h),o=o.toString(36));s===3?i.push("".concat(h,":").concat(o)):(r=t[s]=t[s]||[],r.push("".concat(h,":").concat(o)))}for(c in t)t.hasOwnProperty(c)&&(r=t[c],i.push("".concat(e(+c),':"').concat(r.join(","),'"')));return i.push(u),i}for(var r=["redirectStart","redirectEnd","fetchStart","domainLookupStart","domainLookupEnd","connectStart","secureConnectionStart","connectEnd","requestStart","responseStart","responseEnd","domLoading","domInteractive","domContentLoadedEventStart","domContentLoadedEventEnd","domComplete","loadEventStart","loadEventEnd","unloadEventStart","unloadEventEnd","firstChunkEnd","secondChunkStart","htmlEnd","pageEnd","msFirstPaint"],u="v:1.1",i={},t=0;t<r.length;t++)i[r[t]]=t;n.compress=o})(perf||(perf={}));window.perf=window.perf||{},function(n){n.log=function(t,i){var f=n.compress(t),r;f.push('T:"CI.Perf",FID:"CI",Name:"PerfV2"');var e="/fd/ls/lsp.aspx?",o="sendBeacon",h="<E><T>Event.ClientInst<\/T><IG>".concat(_G.IG,"<\/IG><TS>").concat(i,"<\/TS><D><![CDATA[{").concat(f.join(","),"}]\]><\/D><\/E>"),s="<ClientInstRequest><Events>".concat(h,"<\/Events><STS>").concat(i,"<\/STS><\/ClientInstRequest>"),u=!_w.navigator||!navigator[o];if(!u)try{navigator[o](e,s)}catch(c){u=!0}u&&(r=sj_gx(),r.open("POST",e,!0),r.setRequestHeader("Content-Type","text/xml"),r.send(s))}}(window.perf);var perf;(function(n){function a(){return c(Math.random()*1e4)}function o(){return y?c(f.now())+l:+new Date}function v(n,r,f){t.length===0&&i&&sb_st(u,1e3);t.push({k:n,v:r,t:f})}function p(n){return i||(r=n),!i}function w(n,t){t||(t=o());v(n,t,0)}function b(n,t){v(n,t,1)}function u(){var u,f;if(t.length){for(u=0;u<t.length;u++)f=t[u],f.t===0&&(f.v-=r);t.push({k:"id",v:e,t:3});n.log(t,o());t=[];i=!0}}function k(){r=o();e=a();i=!1;sj_evt.bind("onP1",u)}var s="performance",h=!!_w[s],f=_w[s],y=h&&!!f.now,c=Math.round,t=[],i=!1,l,r,e;h?l=r=f.timing.navigationStart:r=_w.si_ST?_w.si_ST:+new Date;e=a();n.setStartTime=p;n.mark=w;n.record=b;n.flush=u;n.reset=k;sj_be(window,"load",u,!1);sj_be(window,"beforeunload",u,!1)})(perf||(perf={}));_w.si_PP=function(n,t,i){var r,o,l,h,e,c;if(!_G.PPS){for(o=["FC","BC","SE","TC","H","BP",null];r=o.shift();)o.push('"'+r+'":'+(_G[r+"T"]?_G[r+"T"]-_G.ST:-1));var u=_w.perf,s="navigation",r,f=i||_w.performance&&_w.performance.timing;if(f&&u){if(l=f.navigationStart,u.setStartTime(l),l>=0){for(r in f)h=f[r],typeof h=="number"&&h>0&&r!=="navigationStart"&&r!==s&&u.mark(r,h);_G.FCT&&u.mark("FN",_G.FCT);_G.BCT&&u.mark("BN",_G.BCT)}u.record("nav",s in f?f[s]:performance[s].type)}e="connection";c="";_w.navigator&&navigator[e]&&(c=',"net":"'.concat(navigator[e].type,'"'),navigator[e].downlinkMax&&(c+=',"dlMax":"'.concat(navigator[e].downlinkMax,'"')));_G.PPImg=new Image;_G.PPImg.src=_G.lsUrl+'&Type=Event.CPT&DATA={"pp":{"S":"'+(t||"L")+'",'+o.join(",")+',"CT":'+(n-_G.ST)+',"IL":'+_d.images.length+"}"+(_G.C1?","+_G.C1:"")+c+"}"+(_G.P?"&P="+_G.P:"")+(_G.DA?"&DA="+_G.DA:"")+(_G.MN?"&MN="+_G.MN:"");_G.PPS=1;sb_st(function(){u&&u.flush();sj_evt.fire("onPP");sj_evt.fire(_w.p1)},1)}};_w.onbeforeunload=function(){si_PP(new Date,"A")};sj_evt.bind("ajax.requestSent",function(){window.perf&&perf.reset()});var RightRailMetricModule;(function(){var n=!1,t=!1,r,i;typeof PerformanceObserver!="undefined"&&typeof PerformanceObserver=="function"&&(r=PerformanceObserver.supportedEntryTypes||[],r.indexOf("element")>=0&&(i=new PerformanceObserver(function(r){r.getEntries().forEach(function(r){var f,u,e,o;typeof _w.frpPreviousEntry=="undefined"&&(_w.frpPreviousEntry=r);((f=r===null||r===void 0?void 0:r.identifier)===null||f===void 0?void 0:f.length)>0&&(u=r.identifier,u.startsWith("frp")&&u!=="frp.SearchBox"&&(e=Math.round(Math.min(_w.frpPreviousEntry.renderTime,r.renderTime)),_G.frp=e,_w.perf&&!n&&(_w.perf.record&&_w.perf.record("FRP",e),n=!0)),u==="RightRailPaint"&&(o=Math.round(r.renderTime),_G.RightRailPaint=o,_w.perf&&!t&&(_w.perf.record&&_w.perf.record("RightRailPaint",o),t=!0)),n&&t&&i.disconnect())})}),i.observe({entryTypes:["element"]})))})(RightRailMetricModule||(RightRailMetricModule={}));var sj_log=function(n,t,i){var r=new RegExp('"',"g");(new Image).src=_G.lsUrl+'&Type=Event.ClientInst&DATA=[{"T":"'+n+'","FID":"CI","Name":"'+t+'","Text":"'+escape(i.replace(r,""))+'"}]'};var BM=BM||{},adrule="."+_G.adc+" > ul";BM.rules={".b_scopebar":[0,80,0],".b_logo":[-1,-1,0],".b_searchboxForm":[100,19,0],"#id_h":[-1,-1,0],"#b_tween":[-1,-1,1],"#b_results":[100,-1,1],"#b_pole":[-1,-1,1],"#b_topw":[-1,-1,0],"#b_context":[710,-1,1],"#b_navheader":[-1,-1,0],"#bfb-answer":[-1,-1,1],".tab-menu > ul":[-1,-1,1],".b_footer":[0,-1,0],"#b_notificationContainer":[-1,-1,0],"#ajaxMaskLayer":[-1,-1,0],"img,div[data-src],.rms_img":[-1,-1,0],iframe:[-1,-1,0]};BM.rules[adrule]=[-1,-1,1];var BM=BM||{};(function(n){function u(n,u){n in t||(t[n]=[]);!u.compute||n in r||(r[n]=u.compute);!u.unload||n in i||(i[n]=u.unload);u.load&&u.load()}function f(n,i){t[n].push({t:s(),i:i})}function e(n){return n in i&&i[n](),n in t?t[n]:void 0}function o(){for(var n in r)r[n]()}function s(){return window.performance&&performance.now?Math.round(performance.now()):new Date-window.si_ST}var t={},i={},r={};n.wireup=u;n.enqueue=f;n.dequeue=e;n.trigger=o})(BM);(function(n){function i(){var i=document.documentElement,r=document.body,u="innerWidth"in window?window.innerWidth:i.clientWidth,f="innerHeight"in window?window.innerHeight:i.clientHeight,e=window.pageXOffset||i.scrollLeft,o=window.pageYOffset||i.scrollTop,s=document.visibilityState||"default";n.enqueue(t,{x:e,y:o,w:u,h:f,dw:r.clientWidth,dh:r.clientHeight,v:s})}var t="V";n.wireup(t,{load:null,compute:i,unload:null})})(BM);(function(n){function i(){var e,o,u,s,f,r;if(document.querySelector&&document.querySelectorAll){e=[];o=n.rules;for(u in o)for(s=o[u],u+=!s[2]?"":" >*",f=document.querySelectorAll(u),r=0;r<f.length;r++){var i=f[r],h=0,c=0,l=i.offsetWidth,a=i.offsetHeight;do h+=i.offsetLeft,c+=i.offsetTop;while(i=i.offsetParent);e.push({_e:f[r],x:h,y:c,w:l,h:a})}n.enqueue(t,e)}}var t="L";n.wireup(t,{load:null,compute:i,unload:null})})(BM);(function(n){function f(){u(sj_be,r)}function r(i){return i&&n.enqueue(t,i),!0}function e(){u(sj_ue,r)}function u(n,t){for(var u,r=0;r<i.length;r++)u=i[r],n(u==="resize"?window:document,window.navigator.pointerEnabled?u.replace("mouse","pointer"):u,t,!1)}var t="EVT",i=["click","mousedown","mouseup","touchstart","touchend","mousemove","touchmove","scroll","keydown","resize"];n.wireup(t,{load:f,compute:null,unload:e})})(BM);FallBackToDefaultProfilePic = function (e) { var new_element = document.createElement('span'); new_element.setAttribute('id', 'id_p'); new_element.setAttribute('class', 'sw_spd id_avatar'); new_element.setAttribute('aria-label', "Imagen de perfil predeterminada"); new_element.setAttribute('role', 'link'); var p = e.parentNode; p && p.replaceChild(new_element, e); };
//]]></script></head><body class="b_respl"><header id="b_header" role="banner" class=""><a id="b_skip_to_content" data-priority="2" href="#" role="button" tabindex="0"><div class="text-back"><div class="text" href="#">Ir al contenido</div></div></a><form action="/search" id="sb_form" class="hassbi hasmic"><a class="b_logoArea" target="" href="/?FORM=Z9FD1" h="ID=SERP,5045.1"><h1 class="b_logo" title="Volver a la búsqueda de Bing"></h1></a><div class="b_searchboxForm" role="search"><div id="sb_search"><a tabindex="-1" href="javascript:void(0);" h="ID=SERP,5046.1"><div id="sb_go_par"  data-sbtip='Buscar en Internet' ><div id="b_icon_spyglass" class="sb_icon"></div><input type="submit" class="b_searchboxSubmit" id="sb_form_go" tabIndex="0" name="go" value="Buscar" /></div></a></div><input class="b_searchbox" id="sb_form_q" name="q" aria-autocomplete="both" aria-label="Escribe tu búsqueda aquí; irán apareciendo sugerencias de búsqueda a medida que escribes" type="search" value="autojs"
                                               maxlength="2000"
                                               dir="" autocapitalize="off" autocorrect="off" autocomplete="off" spellcheck="false"
                                               placeholder="Pregúntame cualquier cosa" /><div id="sb_clt" class="sb_clrhov" data-second-sbtipx="Borrar" data-sbtipx="Borrar"><a id="sw_clx" data-second-href=javascript:void(0) tabindex="0" aria-label="Borrar texto" role="button"><div class="sw_close"></div></a></div><div id="mic_cont_icon" class="mic_cont icon partner" data-preload=true><div id="vs_mic_icon" class="mic_icon sb_icon disabled"></div><div class="ovr_cont"></div></div><div id="sbiarea" data-idprefix="sb" data-ptn=""><div id="sb_sbip" data-sbtip="Buscar con una imagen" data-lgdgevt="0"><div id="sb_sbi" tabindex="0" role="button" data-scrtl="1"
                             aria-label="Buscar con una imagen"><div id="sbi_b" class="sb_icon disabled"></div></div></div></div><div id="sbicom_loader" style="display:none" data-ptn="Web" data-iid=".5096" data-evt="" data-ep="0" data-iconpl="1"></div><input id="sa_qs" name="qs" value="ds" type="hidden" /><input type="hidden" value="QBRE" name="form" /></div><a class="b_phead_chat_link" tabindex="-1" href="javascript:void(0);" h="ID=SERP,5047.1"><div id="b_phead_chat" role="button" aria-label="Chat" title="Chatear con la IA de Bing" data-priority="2" className="b_hide"><div id="chat_upsell_bubble_icon" class="b_hide"></div></div></a></form><div id="id_h" role="complementary" aria-label="Premios y preferencias de la cuenta" data-priority="2"><a id="id_l" class="id_button" aria-haspopup="true" aria-controls="id_d" aria-expanded="false" data-clarity-mask="true" title="Ramón" href="javascript:void(0);" h="ID=SERP,5052.1"><span class="sw_spd id_avatar" id="id_a" style="display:none" aria-hidden="true"></span><span id="id_n" title="Ramón" aria-hidden="false">Ram&#243;n</span><img id="id_p" class="id_avatar sw_spd" role="presentation" alt="Imagen de perfil" onError="" data-src="https://storage.live.com/users/0x08f9a40508eac164/myprofile/expressionprofile/profilephoto:UserTileStatic/p?ck=1&amp;ex=720&amp;sid=2F8652FD12A56ECE034D46D0133C6F04&amp;fofoff=1" data-alt="" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNgYAAAAAMAASsJTYQAAAAASUVORK5CYII="/><div id="id_linkicon"
 style="position:relative;vertical-align:top;margin-right:-10px;right:10px;display:none"><svg class="id_linkicon_svg" style="width:16px;height:16px;vertical-align:top;margin-top:27px;" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 20 20"><g filter="url(#a)"><circle cx="10" cy="9" r="8" fill="#fff" /></g><path fill="#767676" d="M8.2 6a.6.6 0 0 1 .07 1.196L8.2 7.2H7a1.8 1.8 0 0 0-.106 3.597L7 10.8h1.2a.6.6 0 0 1 .07 1.196L8.2 12H7a3 3 0 0 1-.13-5.997L7 6h1.2ZM13 6a3 3 0 0 1 .13 5.997L13 12h-1.2a.6.6 0 0 1-.07-1.196l.07-.004H13a1.8 1.8 0 0 0 .106-3.597L13 7.2h-1.2a.6.6 0 0 1-.07-1.196L11.8 6H13ZM7 8.4h6a.6.6 0 0 1 .07 1.196L13 9.6H7a.6.6 0 0 1-.07-1.196L7 8.4h6-6Z" /><defs><filter id="a" width="20" height="20" x="0" y="0" color-interpolation-filters="sRGB" filterUnits="userSpaceOnUse"><feFlood flood-opacity="0" result="BackgroundImageFix" /><feColorMatrix in="SourceAlpha" result="hardAlpha" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" /><feOffset dy="1" /><feGaussianBlur stdDeviation="1" /><feComposite in2="hardAlpha" operator="out" /><feColorMatrix values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.14 0" /><feBlend in2="BackgroundImageFix" result="effect1_dropShadow_1352_97902" /><feBlend in="SourceGraphic" in2="effect1_dropShadow_1352_97902" result="shape" /></filter></defs></svg></div></a><span id="id_d" _iid="SERP.5070"></span><a class="id_button toolTip" id="id_rh" aria-label="Microsoft Rewards 2826" role="button" aria-expanded="false" aria-haspopup="true" aria-controls="bepfo" href="javascript:void(0)" h="ID=SERP,5059.1"><span id="id_rc" class="serp">2826</span><span id="rewards_header_icon serp" class="rwds_svg serp pc"><span class="rhlined serp"></span><span class="rhfill serp"></span><svg xmlns="http://www.w3.org/2000/svg" id="rh_meter"><circle cx="20" cy="20" r="14" id="rh_animcrcl" class="serp" stroke-dasharray="88, 88" transform="rotate(-90,20,20)"></circle></svg></span></a><a class="b_hide" id="id_rwl" data-title="Microsoft Rewards" href="/rewards/dashboard" h="ID=SERP,5058.1"></a><span id="nc_iid" _IG="F4148F017DED47FD91F14746ED92DB7A" _iid="SERP.5057" _cid="064AB47E778061551B14A05376D76011"></span><a id="id_sc" class="idp_ham nohphbtop" aria-label="Configuraciones y vínculos rápidos" aria-expanded="false" aria-controls="id_hbfo" aria-haspopup="true" role="button" tabindex="0" href="javascript:void(0);" h="ID=SERP,5068.1"></a><span id="id_hbfo" _iid="SERP.5069" class="slide_up nohpfo" tabindex='-1' aria-hidden="true" aria-labelledby="id_sc" aria-modal="true" role="menu"></span></div><span id="langChange" class="b_hide"><a data-noajax="1" href="/search?q=autojs&amp;form=WSHBSH&amp;qs=HS&amp;cvid=e321066b41dc426184a064d75924a313&amp;pq=&amp;cc=MX&amp;nclid=AC1E1DBB5EAEDDBC2F9B5D4F0F557FAC&amp;ts=1708643909987&amp;wsso=Moderate&amp;showconv=0&amp;setlang=en&amp;sid=2F8652FD12A56ECE034D46D0133C6F04" h="ID=SERP,5043.1">English</a></span><nav class="b_scopebar" role="navigation" aria-label="Filtro de búsqueda"><ul class=" b_scopehide"><li class=" b_active" data-menuUrl=""
    id="b-scopeListItem-web" data-query=""><a class="" aria-current="page" href="/?scope=web&amp;FORM=HDRSC1" h="ID=SERP,5031.1"><span class="scp_conv_mode"></span>B&#250;squeda</a></li><li class="" data-menuUrl=""
    id="b-scopeListItem-conv" data-query=""><a class="" aria-current="false" href="javascript:void(0)" h="ID=SERP,5032.1"><span class="scp_conv_mode"></span>Copilot</a></li><li class="" data-menuUrl=""
    id="b-scopeListItem-images" data-query=""><a class="" aria-current="false" href="/images/search?q=autojs&amp;FORM=HDRSC3" h="ID=SERP,5033.1">Im&#225;genes</a></li><li class="" data-menuUrl=""
    id="b-scopeListItem-video" data-query=""><a class="" aria-current="false" href="/videos/search?q=autojs&amp;FORM=HDRSC4" h="ID=SERP,5034.1">V&#237;deos</a></li><li class="" data-menuUrl=""
    id="b-scopeListItem-local" data-query=""><a class="" aria-current="false" href="/maps?q=autojs&amp;FORM=HDRSC6" h="ID=SERP,5035.1">Mapas</a></li><li class="" data-menuUrl=""
    id="b-scopeListItem-news" data-query=""><a class="" aria-current="false" href="/news/search?q=autojs&amp;FORM=HDRSC7" h="ID=SERP,5036.1">Noticias</a></li><li class="" data-menuUrl=""
    id="b-scopeListItem-shop" data-query=""><a class="" aria-current="false" href="/shop?q=autojs&amp;FORM=SHOPTB" h="ID=SERP,5037.1">Shopping</a></li><li id="b-scopeListItem-menu"><a target="_self" aria-current="false" href="javascript:void(0);" tabindex="0" aria-haspopup="true" role="button" aria-label="Dropdown Menu"><span class="b_sp_menu_separ" id="b_sp_menu_separ"><svg focusable="false" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"></path></svg></span>M&#225;s</a><div class="b_sp_over_cont"><ul class="b_sp_over_menu"><li class=" b_sp_over_item b_scopebar_item" data-menuUrl=""
    id="b-scopeListItem-flights" data-query=""><a class="" aria-current="false" href="/travel/search?q=autojs&amp;m=flights&amp;FORM=FBSCOP" h="ID=SERP,5038.1">Vuelos</a></li><li class=" b_sp_over_item b_scopebar_item" data-menuUrl=""
    id="b-scopeListItem-travelhub" data-query=""><a class="" aria-current="false" href="/travel/search?q=autojs&amp;m=travel&amp;FORM=THSCOP" h="ID=SERP,5039.1">Viajes</a></li><li class=" b_sp_over_item b_scopebar_item" data-menuUrl=""
    id="b-scopeListItem-hotels" data-query=""><a class="" aria-current="false" href="/travel/search?q=autojs&amp;m=hotels&amp;FORM=HTSCOP" h="ID=SERP,5040.1">Hoteles</a></li><li class=" b_sp_over_item b_scopebar_item" data-menuUrl=""
    id="b-scopeListItem-realestate" data-query=""><a class="" aria-current="false" href="/homes?FORM=000060" h="ID=SERP,5041.1">Sector inmobiliario</a></li><li class=" b_sp_over_item b_scopebar_item" data-menuUrl=""
    id="b-scopeListItem-bingpages" data-query=""><a class="" aria-current="false" href="/bp/verify?FORM=000061" h="ID=SERP,5042.1">Mi Bing</a></li></ul></div></li><li id="b-scopeListItem-notebook" class=""><a href="javascript:void(0);" h="ID=SERP,5051.1"><svg width="12" height="16" viewBox="0 0 12 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M3 0.5C3 0.223858 2.77614 0 2.5 0C2.22386 0 2 0.223858 2 0.5V1H1.5C0.671573 1 0 1.67157 0 2.5V14.5C0 15.3284 0.671573 16 1.5 16H7V12.5C7 11.6716 7.67157 11 8.5 11H12V2.5C12 1.67157 11.3284 1 10.5 1H10V0.5C10 0.223858 9.77614 0 9.5 0C9.22386 0 9 0.223858 9 0.5V1H6.5V0.5C6.5 0.223858 6.27614 0 6 0C5.72386 0 5.5 0.223858 5.5 0.5V1H3V0.5ZM3 5.5C3 5.22386 3.22386 5 3.5 5H8.5C8.77614 5 9 5.22386 9 5.5C9 5.77614 8.77614 6 8.5 6H3.5C3.22386 6 3 5.77614 3 5.5ZM3.5 8H8.5C8.77614 8 9 8.22386 9 8.5C9 8.77614 8.77614 9 8.5 9H3.5C3.22386 9 3 8.77614 3 8.5C3 8.22386 3.22386 8 3.5 8ZM3 11.5C3 11.2239 3.22386 11 3.5 11H5.5C5.77614 11 6 11.2239 6 11.5C6 11.7761 5.77614 12 5.5 12H3.5C3.22386 12 3 11.7761 3 11.5ZM11.6892 12H8.5C8.22386 12 8 12.2239 8 12.5V15.6892C8.05127 15.6498 8.10021 15.6069 8.14645 15.5607L11.5607 12.1464C11.6069 12.1002 11.6498 12.0513 11.6892 12Z" /></svg>Bloc de notas</a></li><li id="scope_tools_wrapper" class="tools_scope b_hide"><a href="javascript:void(0);" h="ID=SERP,5050.1">Herramientas</a></li></ul></nav></header><script type="text/javascript" nonce="hdm+zCWukVbuQiu/8nwvTExR65VnfK54cOHZ35vK2Vk=">//<![CDATA[
(function(n){var i,r,t;if(document.querySelector){i=[];r="ad";function u(){var w=sb_gt(),l=document.documentElement,s=document.body,t=0,r=-1,b=l.clientHeight,a=["#b_results ."+_G.adc,".sb_adsWv2",".ads"],n,f,e,p,c,o,u;if(s){n=0;f=document.querySelector("#b_pole .b_PolePAContainer");f&&(n=f.offsetHeight,r=f.offsetTop);var v=document.querySelector("#b_results #productAdCarousel"),h=document.querySelector("#b_results .pa_b_supertop"),y=document.querySelector("#b_results .bn_wide");for(h?(r=h.offsetTop,n=h.offsetHeight):y?n+=y.offsetHeight:v&&(n+=v.offsetHeight),t=n,e=0;e<a.length;e++)for(p=a[e],c=document.querySelectorAll(p),o=0;o<c.length;o++)u=c[o],u&&u.className.indexOf("b_adTop")!==-1&&(t+=u.offsetHeight,r===-1&&(r=u.offsetTop));t===0&&(t=-1);i=[r,t,l.clientWidth,b,s.offsetWidth,s.offsetHeight,sb_gt()-w]}}n?(t=n.onbeforefire,n.onbeforefire=function(){t&&t();u();n.mark(r,i)}):(t=si_PP,si_PP=function(){u();var n='"'+r+'":['+i.join()+"]";_G.C1=_G.C1?_G.C1+","+n:n;t.apply(null,[].slice.apply(arguments))})}})(_w.pp);_w.AM=["live.com","azureedge.net","virtualearth.net","windows.net","onenote","hexun.com","dict.bing.com.cn","msn.com","variflight.com","bing.net","msftoffers.com","chinacloudapp.cn","cbsnews.com","swx.cdn.skype.com","swc.cdn.skype.com","latest-swx.cdn.skype.com","a.config.skype.com","b.config.skype.com","platform.bing.com","microsofttranslator.com","bing.com","facebook.net",".delve.office.com",".uservoice.com",".cdn.office.net","platform.twitter.com","cdn.syndication.twimg.com","spoprod-a.akamaihd.net","bingstatic.com","yahoo.co.jp","youtube.com","ytimg.com","r.msftstatic.com","rafd.bing.com","rafd.staging-bing-int.com","raka.bing.com","r.bing.com","r.staging-bing-int.com","raka.staging-bing-int.com","rcf.staging-bing-int.com","rcf.bing.com","s.cn.bing.net","xpaycdn-int.azureedge.net","xpaycdn.azureedge.net","ssl.kaptcha.com","tst.kaptcha.com","snrproxy.binginternal.com","snrproxyeast.binginternal.com","snrproxync.binginternal.com","snrproxysc.binginternal.com","snrproxywest.binginternal.com","cetonc.binginternal.com","cetosc.binginternal.com","cetoeast.binginternal.com","cetowest.binginternal.com","akam.bing.com","akam.staging-bing-int.com","ewlmaakam.staging-bing-int.com","akamproxyeast.staging-bing-int.com","akamproxync.staging-bing-int.com","akamproxysc.staging-bing-int.com","akamproxywest.staging-bing-int.com","cjfdbemmaeeohgibnhdhlakiahifjjcf","ewlmaakam.staging-bing-int.com","bngpiglbalmenaabohcooocpnljgfemj","rwww.bing.com"];_w.APD=[".delve.office.com",".uservoice.com",".cdn.office.net","a.config.skype.com","abcnews.go.com","amazon.com","apps.powerapps.com","app.powerbi.com","app.powerbi.cn","app.powerbi.de","app.powerbigov.us","b.config.skype.com","bfb","bfb-int","bing.com","bing.net","bing-int.com","bingsandbox.com","bingweathermap.azureedge.net","bloomberg.com","c.tenor.com","cbsnews.com","cdn.syndication.twimg.com","channel9.msdn.com","chinacloudapp.cn","clarity.ms","cnn.com","covid19healthbot.cdc.gov","covid19healthbot-dev.cdc.gov","ctmbing.azurefd.net","dailymotion.com","dict.bing.com.cn","downvids.com","downvids.net","edition.cnn.com","embed.vevo.com","euronews.com","facebook.com","fave.api.cnn.io","hexun.com","huffingtonpost.com","idsync.rlcdn.com","ign.com","imdb.com","latest-swx.cdn.skype.com","live.com","login.live-int.com","mashable.com","microsoft.com","microsoftonline.com","microsofttranslator.com","mathsolver.microsoft.com","msecnd.net","msftoffers.com","msit.powerbi.com","msn.com","mtv.com","onenote","photosynth.net","platform.bing.com","platform.twitter.com","powerbi-df.analysis-df.windows.net","r.msftstatic.com","rafd.bing.com","rafd.staging-bing-int.com","raka.bing.com","r.bing.com","r.staging-bing-int.com","raka.staging-bing-int.com","rcf.staging-bing-int.com","rcf.bing.com","rottentomatoes.com","s.cn.bing.net","skype.com","snrproxy.binginternal.com","snrproxyeast.binginternal.com","snrproxync.binginternal.com","snrproxysc.binginternal.com","snrproxywest.binginternal.com","spoprod-a.akamaihd.net","substrate.office.com","swc.cdn.skype.com","swx.cdn.skype.com","variflight.com","video.disney.com","videoplayercdn.osi.office.net","vimeo.com","azureedge.net","virtualearth.net","web.powerapps.com","widgets.icanbuy.com","widgets.ign.com","windows.net","wsj.com","xpaycdn-int.azureedge.net","xpaycdn.azureedge.net","ssl.kaptcha.com","tst.kaptcha.com","yahoo.co.jp","youtube.com","ytimg.com","zdnet.com","chrome-extension://haldlgldplgnggkjaafhelgiaglafanh","player.twitch.tv","mixer.com","3pcookiecheck.azureedge.net","cetonc.binginternal.com","cetosc.binginternal.com","cetoeast.binginternal.com","cetowest.binginternal.com","akam.bing.com","akam.staging-bing-int.com","ewlmaakam.staging-bing-int.com","akamproxyeast.staging-bing-int.com","akamproxync.staging-bing-int.com","akamproxysc.staging-bing-int.com","akamproxywest.staging-bing-int.com","cjfdbemmaeeohgibnhdhlakiahifjjcf","bngpiglbalmenaabohcooocpnljgfemj","opentable.com","wolframalpha.com","mswolfram-staging-tm.office.net","instacart.com","kayak.com","klarna.com","redfin.com","shopify.com","zillow.com","rwww.bing.com"];_w.APC=["bm_","fb_","panelWrapper","df_topAlAs","df_playBut","df_vidTime","na_cai","ckt_","Light","Dark","taskbar","ssSIV","square_","partial-det","tall_","item","sw_","syd_","sb_","sml","ftrd","sa_","id_","sc_","flt_","fc_","cca","tab-","emb","ctx","dc_","cipa","dict","btm","wtr","wpc","fin","sp-","carousel","vp_","vid","nav_","vt","va_","avc","cic","sports","lc_","bing","dmap_","pvc_","ans_","mcd","composite","mt_","irp","iap","tv","aggtv","irhc","vrh","det","tit","sub","col","card","hlsel","hlblk","ovl","ctpt","bubble","memodal","meoverlay","c_","spl-","microsoft","skp","saa","unlockButton","overlay","MapPushpinBase","pa_","aa_","skype_","ftrSbR","quizContainer","alrt_","st_","expan","word","rpt_","o_","e_","searchbar","row","Traffic","tl","gray","bep","wk_","crs_","w10","personal","fs3_","ezp_","hp","post","mc_","fb","lgb","el_","perf","stb","PP","bw","infobubble","l_","ms-","NavBar_","cmt_","bottom","Copyright","upsell","ab_","w_","hlig","eachStep","close_","cGifIcon","cThIcon","autosuggest","showtimesMovie","sel","dish","formatShowtimes","wp_","hasExpandText","forecast","as_","ecmp","cmp","comp","userChat","bot","bTyp","team","serp","preG","option","azBxInsert","ec_","cs_","spin","skype-conversation","conversation","fs_","grammarly","filterBar","withFilters","textanno","mv_lm","usagTpVsDosage","trans_button_group","algo_action_template","meg_item","ev_","ol_","offer","embed","videoplayercdn","searchNearby","directionsPanel","dragOverlay","infobox","mss","noneG","usage","drImp","sf_","dfindOverModal","circuit","swc","CodeMirror","cm-s-default","msg msg-warning","LogoContainer","quadrantOverride","ac-","gc-","fsmd-","fsg-","fsmf-","msto_","rq","geoItm","bqaq_quotes","bqap_padding","loc","ent_cnt","r_mf","exp_","btOverlay","mnot_container","info_C","ev_talkbox_notification","ev_talkbox_wrapper_min","p_tr_","slide","bnc-","itr_poi","cg-","elmlr_","scrl","gam-","htv-","genel-","gs_","qo_","jss_","mapsresp","geochainContainer","scaleBar","ae-","CalendarSync","spl_","adbDef","layerFrame","esp-","elis-","elcan-","elec-","sharegeneralcard","edu_","br-","covt_dd","covt_dd_sel","msac_sel","msac_ddi","cov_tt_tr","cov_tt_tn","cov_","dr_","bt_","epv_","rich_card","mf-item-cntr","footer_mcp","ntro-","ntro_","th-answer-explanation-wrapper","th-questions-completed","th-next-question","th-modal-wrapper","th-show-hint-wrapper","th-show-hint-description","th-show-hint-example","th-word-carousel","th-audio-player","retail_btf","na_","news_","nws_","scs_","scadt","jb_","peregrine-","opalfeedblock","expnd_c","wfr_grid  ldeft","rwgradienttitle","azuremediaplayer","amp-","vjs-","logo-title-row","rwrl","wpt_hdrs","actionitem","sl_","ml_","wc_","rcld_","pst_expand_btn","pst_collapse_btn","hdr_rating","disambig-outline","editor-","sto_","tt_","nc_","dr_","pt_","qna-","qna_","paahybrid_content","b_floatrt","pec_","maplibregl","atlas-map","marker-collection-container","fui-","recharts",];_w.APN=["fb_ovrly_cnt","b_bfb","b_bfb_context_ans","b_mapOverlay","m365ChatPromptLibraryDialogSurface"];0;(function(){function t(t,i){var u=t.tagName;return(u==="SCRIPT"&&(n.href=t.src)||u==="OBJECT"&&t.type&&t.type.indexOf("flash")>0&&(n.href=t.data))&&n.href.length>0&&n.hostname.length>0&&n.hostname!==location.hostname&&!e(n.hostname)?(sj_log("CI.AntiMalware",i,u.substr(0,1)+":"+n.href.substr(0,r)),!1):(t.getAttribute&&(t.getAttribute("data-rms")||t.getAttribute("data-bing-script"))&&t.setAttribute("nonce",o),!0)}function e(n){for(var t=0;t<i.length;t++)if(n.indexOf(i[t])>=0)return!0;return!1}var i=_w.AM,r=100,n=document.createElement("A"),o=_G.IG.substr(0,6),u,f;document.write=function(n){n.length>0&&sj_log("CI.AntiMalware","DW",n.substr(0,r))};typeof Element!="undefined"&&Element.prototype&&(u=Element.prototype.appendChild,Element.prototype.appendChild=function(n){return t(n,"AC")?u.apply(this,arguments):null},f=Element.prototype.insertBefore,Element.prototype.insertBefore=function(n){return t(n,"IB")?f.apply(this,arguments):null})})();_G!==undefined&&_G.EF!==undefined&&_G.EF.bmasynctrigger===1&&window.requestAnimationFrame!==undefined&&document.visibilityState!==undefined&&document.visibilityState==="visible"?requestAnimationFrame(function(){_G.EF.bmasynctrigger2===1?requestAnimationFrame(function(){BM.trigger()}):_G.EF.bmasynctrigger3===1?requestAnimationFrame(function(){setTimeout(function(){BM.trigger()},0)}):setTimeout(function(){BM.trigger()},0)}):BM.trigger();var NetworkPerformance;(function(){function n(){var r=window.navigator,n,t=0,i=0;typeof r!="undefined"&&(n=r.connection);typeof n!="undefined"&&(t=n.rtt||t,i=n.downlink||i,typeof Log!="undefined"&&typeof Log.Log==typeof Function&&Log.Log("NetworkPerformance","NetworkPerformanceDetails","timinginfo",!1,"Rtt",t.toString(),"Downlink",i.toString()))}sj_evt.bind("onPP",n,!0)})(NetworkPerformance||(NetworkPerformance={}));var AwayTimeThreshold = 15; var AwayTimeThresholdCustomControl = false; var AwayTimeScrollTopPoleRS = false;;(function(){function t(){var n=_ge("b_header");n&&(n.className="b_focus")}var n=_ge("sb_form_q");sj_be(n,"focus",t)})();var MicLoad;(function(n){function r(n){function i(){sj_evt.unbind("micLoaded",i);var t=n.querySelector(".b_icon");t&&t.click()}t||(n.clicked||(sj_evt.bind("micLoaded",i,!0),n.clicked=!0),sj_log("CI.VoiceSearch","MicPreClick","Beforeload"))}function i(){t=!0;sj_evt.unbind("micLoaded",i)}var t=!1;n.handleMicClick=r;sj_evt.bind("micLoaded",i,!0)})(MicLoad||(MicLoad={}));var SBI;(function(n){var t;(function(n){function u(n){function r(){sj_evt.unbind(t,r);sj_evt.fire("sbidlg.show",n.parentElement)}i||(n.clicked||(sj_evt.bind(t,r,!0),n.clicked=!0),sj_log("CI.VisualSearch","SbiPreClick","BeforeLoad"))}function r(){i=!0;sj_evt.unbind(t,r)}var t="sbiLoaded",i=!1;n.handleIconClick=u;sj_evt.bind(t,r,!0)})(t=n.IconLoader||(n.IconLoader={}))})(SBI||(SBI={}));var Identity; (function(Identity) { Identity.sglid =false; Identity.orgIdPhotoUrl ="https://business.bing.com/api/v3/search/person/photo?caller=IP\u0026id={0}"; Identity.setLoginPreference =false; Identity.isExplicitMsaSignIn =true; })(Identity || (Identity = {}));;window.data_iid = "SERP.5066";;var DynScopesDropdownRE;(function(n){function v(n,t,r,e,s,h,c){var a,l,v;u||(i=n,f=e,o=c,sj_evt.fire("dynamicscopesrearranging",t),o||(w(t,n,s,h),nt(r)),sj_evt.bind("onP1",p),((a=_G===null||_G===void 0?void 0:_G.EF)===null||a===void 0?void 0:a.logforctl)===1&&(l=_ge("b_header"),v=l===null||l===void 0?void 0:l.querySelector(".b_logoArea"),sj_be(v,"click",y)),u=!0)}function y(){var n="WebLogoClick";document.querySelector("cib-serp[mode='conversation']")&&(n="ConversationLogoClick");_w.sj_log&&sj_log("CI.HeaderMSLogo",n,(_G===null||_G===void 0?void 0:_G.V)||"other")}function p(){if(sj_cook&&sj_cook.set&&sj_cook.clear){var n="dsc";sj_cook.clear(n,"/");i&&sj_cook.set(n,"order",i,!1,"/")}}function w(n,t,i,r){var c=tt(),e,o,s,h,u;if(c){if(e=rt(c),b(e,i,r),n){var a=n.split(","),v=e.length,l=[],y=[];for(u=0;u<a.length;u++)o=a[u].split(":"),o&&o.length==2&&(s=parseInt(o[0]),h=parseInt(o[1]),s<v&&h<v&&(l[h]=e[s].innerHTML,y[h]=e[s].id));for(u=0;u<l.length;u++)e[u].innerHTML=l[u],e[u].id=y[u]}f&&k(e);c.className=""}}function b(n,t,i){var r,u;if(i)t?sessionStorage.setItem("RealEstateQuery",t):sessionStorage.removeItem("RealEstateQuery");else if(t)for(r=0;r<n.length;r++)n[r].id===a&&(u=n[r].innerHTML,n[r].innerHTML=u.replace(/href="[^"]*"/,'href="https://www.bing.com/homes/map?q='+t+'&FORM=000060"'))}function k(n){for(var i,u,f,t=0;t<n.length;t++)if(n[t].id===c||n[t].id===l){i=n.length-1;e&&r>0&&(i=r-1);t!=i&&(u=n[t].innerHTML,f=n[t].id,t<i?d(n,t,i):g(n,i,t),n[i].innerHTML=u,n[i].id=f,sj_evt.fire("dynamicchatscoperelocate"));break}}function d(n,t,i){for(var r=t;r<i;r++)n[r].innerHTML=n[r+1].innerHTML,n[r].id=n[r+1].id}function g(n,t,i){for(var r=i;r>t;r--)n[r].innerHTML=n[r-1].innerHTML,n[r].id=n[r-1].id}function nt(n){var e,o,h,u,c,l,f,a,r,i;if(n&&(h=n.split(":"),u=it(),(e=u===null||u===void 0?void 0:u.children)===null||e===void 0?void 0:e.length))for(c=u.children,l=s(),f=0;f<h.length;f++){for(a=t+h[f],r=0;r<c.length;r++)if(i=c[r],i.id===a){u.removeChild(i);break}for(r=0;r<l.length;r++)if(i=l[r],i.id===a){(o=i===null||i===void 0?void 0:i.parentElement)===null||o===void 0?void 0:o.removeChild(i);break}}}function tt(){var n=_d.querySelectorAll(".b_scopebar > .b_scopehide");return n&&n.length>0?n[0]:null}function it(){var n=_d.querySelectorAll(".b_scopebar");return(n===null||n===void 0?void 0:n.length)?n[0].firstChild:null}function rt(n){for(var f,i=[],u=n.children,t=0;t<u.length;t++)u[t].id===h?(r=t,f=s(),i.push.apply(i,f),e=!0):i.push(u[t]);return i}function s(){var n=_d.querySelectorAll(".b_scopebar #b-scopeListItem-menu .b_sp_over_menu .b_scopebar_item");return Array.prototype.slice.call(n)}var u=!1,i,f=!1,e=!1,r=-1,o=!1,t="b-scopeListItem-",h=t+"menu",c=t+"conv",l=t+"convups",a=t+"realestate";n.init=v})(DynScopesDropdownRE||(DynScopesDropdownRE={}));_G.AppVer="43703550";
//]]></script><script type="text/javascript" nonce="hdm+zCWukVbuQiu/8nwvTExR65VnfK54cOHZ35vK2Vk=" >//<![CDATA[
_G.FCT=new Date;
//]]></script><script type="text/javascript" nonce="hdm+zCWukVbuQiu/8nwvTExR65VnfK54cOHZ35vK2Vk=" >//<![CDATA[
_G.BCT=new Date;
//]]></script><style type="text/css">#b_header #id_h{content-visibility:visible}#b_content a.b_LinksColorMD,#b_content a:visited.b_LinksColorMD{color:#1a73e8}.b_CursorProgress{cursor:progress}#b_results .b_expando .b_icbtns .ibs_btn,#b_context .b_icbtns .ibs_btn,#b_pole .b_icbtns .ibs_btn{border-color:#106ebe;color:#106ebe}#b_content #b_context .b_sgmbtns a.cbtn,#b_content #b_context .b_sgmbtns a.cbtn:visited,#b_content #b_results .b_sgmbtns a.cbtn,#b_content #b_results .b_sgmbtns a.cbtn:visited,#b_content #b_context .b_sgmbtns .b_tpoptbtn,#b_content #b_context .b_sgmbtns .b_tpoptbtn:visited,#b_content #b_results .b_sgmbtns .b_tpoptbtn,#b_content #b_results .b_sgmbtns .b_tpoptbtn:visited{border-color:#106ebe;background-color:#106ebe}.listCard.vertcard a.ibs_1btns .ibs_btn .b_primtxt,.listCard.vertcard a.ibs_2btns .ibs_btn .b_primtxt{color:#106ebe}.wk_buttons .wk_button .cbtn input,.wq_tq_sbutton2 .cbtn input[type="submit"],#ifeu a#GoToButton,#b_results li .coupon>button,#b_context li .coupon>button,#b_content .gtOfferBtn,#b_content .b_ans .rebatesBanner a.cbtn,#b_content .wpc_module .rebatesBanner a.cbtn,.wpc_module .rebatesBanner a.cbtn,#b_content .b_sgmbtns a.cbtn,#b_content .b_sgmbtns a.cbtn:visited{background-color:#106ebe !important;color:#fff !important;border-color:#106ebe !important}#b_results .br-seemrbtnCntnt,#b_context .br-seemrbtnCntnt,#b_results .jb_see_all_btn,#b_results .wiki_seemore,.b_rich .iaplanner .iasabt,.salink .vasabt,.fb_seemorebutton,.sa_main .sa_sabt,.b_saContainer .sa_sabt,#b_results .ans_nws .new_see_more_container .new_see_more,.wfr_smlk,#b_content #b_pole #ans_nws .new_see_more{background-color:#106ebe !important}#financeAnswer .b_sideBleed .HeroTab .tab-active{box-shadow:inset 0 -3px 0 0 #106ebe !important;color:#106ebe !important}.toggle_img.overlay.toggle_on .indicator{fill:#106ebe !important}.b_filterButton.b_selected:not(.b_neutral),.tfil-sc-g .tfil-active{border-color:#106ebe !important;background-color:#106ebe !important}#ans_nws .tfil-sc-g .tfil-active{background-color:#767676 !important;border-color:#767676 !important}#lmfilters .lm_gfs .ftrH.b_selected,#lmfilters .lm_gfs .ftrH.b_selected:hover{border-color:#106ebe !important}#b_content #lMapContainer .advanceFilters .filterBar.lmFilters .ftrH.b_selected,#b_content #lMapContainer .advanceFilters .lm_filters .ftrH.b_selected,#b_content #lMapContainer .advanceFilters .b_filterButton.b_selected,#b_content #lMapContainer .advanceFilters .filterBar.lmFilters .ftrH.adv_clicked,#b_content #lMapContainer .advanceFilters .lm_filters .ftrH.adv_clicked{border-color:#106ebe !important;background-color:#106ebe !important}.tfil-sc-p .tfil-active,#b_filterBar .filtBarItem.selectedFilter,#b_filterBar #b_searchThisArea>span{border-color:#106ebe !important;background-color:#106ebe !important}#b_content .aqptmt .slide a.rel_ent{border-color:#106ebe}#b_content .b_imgans .aqptmt .slide a.rel_ent{border-color:#ddd}#lgb_info .lgb_facts svg path.gray50_fill,.b_entityTP .infoModule svg:not(#sacamerasvg) path.gray50_fill,.b_wftp_tms .infoModule svg path.gray50_fill,.b_lstp_tms .infoModule svg path.gray50_fill{fill:#106ebe}#b_content .b_rbubble.b_rselected{background-color:#106ebe;border-color:#106ebe}#b_content .b_cvnMtLink>a{border-color:#106ebe}#b_results #df_listaa .b_vPanel .df_hd .b_primtxt{color:#106ebe}#b_content .radio-container .checkmarked:after{background:#106ebe;border-color:#106ebe}#b_content .radio-container input:checked~.checkmarked{border-color:#106ebe}#warningsvg path.gray70_fill{fill:#106ebe}#b_sydConvCont{display:block;transform:translateY(0);position:fixed;height:100%;width:100%;opacity:0;transition-property:opacity,visibility,z-index;transition-delay:1s,1187ms,1187ms;transition-duration:187ms,0s,0s;visibility:hidden;z-index:-1}#b_sydBgCover{background-color:#fff;z-index:2;position:fixed;height:100vh;width:100vw;z-index:1;transition-property:transform,visibility;transition-duration:1000ms,0s;transition-timing-function:cubic-bezier(.75,0,.25,1);transition-delay:0s;transform:translateY(calc(var(--cib-header,94px) + 18px));visibility:visible}.b_sydConvAnsTest{z-index:1;position:absolute;top:120px}#b_sydHeadBg{opacity:0;height:112px;width:100%;display:block;position:absolute;transform:translateY(0);transition:transform 1000ms cubic-bezier(.75,0,.25,1),opacity 187ms cubic-bezier(0,0,0,1);z-index:-1;display:none}#b_header{transition-property:background;transition-duration:0s;transition-delay:1.5s}#b_header .bfbTenantLogoCls{display:none}.b_ctf #b_header{transition-property:background,z-index;transition-duration:0s;transition-delay:1.5s,1000ms}.b_searchboxForm{transition-property:opacity,visibility;transition-delay:1000ms,1000ms;transition-duration:0s;transition-timing-function:cubic-bezier(.75,0,.25,1);opacity:1;visibility:visible;z-index:7}#b_content{transition-property:transform,visibility;transition-duration:1000ms,0s;transition-timing-function:cubic-bezier(.75,0,.25,1);transition-delay:0s;visibility:visible;position:relative;background-color:#fff;z-index:0}.b_widgetContainer{transition-property:visibility;transition-delay:0s;transition-duration:0s}#b_sydHeadBg{background:url(/rp/1AKRcEqxcHA1tXDCCycPO6nXsgk.jpg)}div#b_metaCont,div#b_metaChat{display:inline-flex}#b_metaCont.b_metaPadding{margin:16px 0 16px var(--polepadl)}div#b_metaCont_PillCont{max-width:750px;gap:8px 6px;display:flex;flex-wrap:wrap}div#b_metaCont_PillCont.b_metaExpanded{max-width:690px}#b_metaCont{flex-direction:column}#b_meta_bot{flex-direction:row;display:flex}#b_metaCont_Title{margin-bottom:10px;color:#767676;font-size:14px;line-height:18px}.b_metaCont_Pill{font-weight:700;font-size:14px;line-height:16px;color:#444 !important;background:#fff;border:1px solid #174ae4;padding:8px 16px 8px 16px;border-radius:16px;height:32px;box-sizing:border-box;justify-content:center;align-items:center;white-space:nowrap;display:flex}.b_metaCont_Pill:hover{background:#eff3ff;text-decoration:none}.b_metaCont_Pill.b_hide{display:none}a.b_metaCont_Pill:visited{color:#444}.b_metaExpanded #b_meta_exp{display:none}.b_metaExpanded .b_metaCont_Pill.b_hide{display:flex}#b_meta_exp{cursor:pointer}#b_meta_col{cursor:pointer;color:#174ae4 !important;background:#e2e9ff;border:none;padding-left:38px;background-repeat:no-repeat;background-size:10px 5.5px;background-position:19px 13px}#b_meta_col:hover{background-color:#d1dbfa}#b_metaChat{width:32px;height:32px;margin-right:10px;background-repeat:no-repeat;background-size:32px;background-position:center}#b_meta_col{background-image:url(/rp/OuJfi801QISuInMovU7PaSrDbMI.png)}#b_metaChat{background-image:url(/rp/em88jYr3ZOv7yX3AqoOU5z8EEnA.png)}#b_sydWelcomeTemplate_ .b_wlcmTileCont .b_wlcmTile{box-shadow:none;border:1px solid var(--cib-color-stroke-neutral-primary)}#b_sydWelcomeTemplate{display:none}.b_wlcmCont{justify-content:center;align-items:center;display:flex;flex-direction:column}.b_wlcmHdr{align-items:center;justify-content:center;display:flex;flex-direction:column;margin-bottom:36px}.b_wlcmPersLogoCont{display:flex;gap:10px;flex-direction:row;text-align:center;align-items:center;margin-inline:0 6px}.b_wlcmPersLogo{height:48px;width:auto}.b_wlcmPersName{font-style:normal;font-weight:600;font-size:40px;line-height:52px;text-align:center}.b_wlcmPersDesc{font-style:normal;font-weight:400;font-size:20px;line-height:26px;margin-top:1vh;text-align:center}.b_wlcmPersAuthorText{font-style:normal;font-weight:400;font-size:14px;line-height:18px;color:#666;margin-top:1vh;margin-bottom:2vh;text-align:center}.b_wlcmSubDesc{font-size:16px}.b_wlcmTileCont{display:flex;flex-wrap:wrap;align-content:stretch;justify-content:center;max-width:940px}.b_wlcmTileWrap{padding:10px;flex-grow:1;display:flex;box-sizing:border-box}.b_wlcmTileWrap[size="small"]{width:16.67%}.b_wlcmTileWrap[size="medium"]{width:25%}.b_wlcmTileWrap[size="large"]{width:33.33%}.b_wlcmTile{box-shadow:0 6.67587px 25.869px -1.66897px rgba(73,141,255,.3);border:2px solid transparent;width:100%;background:#fff;border-radius:12px;padding:14px;cursor:pointer;display:flex;flex-direction:column;background:var(--cib-color-background-surface-card-primary);font-family:inherit;font-style:normal;text-align:left;position:relative}.b_wlcmTile:hover{border:2px solid var(--cib-color-stroke-accent-primary)}.b_wlcmTile p{font-size:16px;margin-top:8px;line-height:22px;color:var(--cib-color-foreground-neutral-primary);overflow-wrap:anywhere}.b_wlcmTile h1{font-size:14px;line-height:24px;display:flex}.b_wlcmTile h1::before{height:24px;width:24px;margin-right:8px;content:"";background-size:24px}.b_wlcmTile img{height:100%;width:100%;object-fit:cover}.b_wlcmTile .b_wlcmTileOverlay{opacity:0;position:absolute;top:0;left:0;width:100%;height:100%;overflow:hidden;padding:14px;box-sizing:border-box;border-radius:inherit;background-color:rgba(0,0,0,.6);transition:all .2s ease-in-out}.b_wlcmTile .b_wlcmTileOverlay h4{font-weight:400;font-size:10px;color:#fff}.b_wlcmTile .b_wlcmTileOverlay p{font-weight:600;font-size:14px;color:#fff;display:-webkit-box;-webkit-line-clamp:5;-webkit-box-orient:vertical;overflow:hidden;text-overflow:ellipsis}.b_wlcmTile:hover .b_wlcmTileOverlay,.b_wlcmTile:focus-visible .b_wlcmTileOverlay{opacity:1}.b_wlcmTile:active .b_wlcmTileOverlay{background-color:rgba(0,0,0,.65)}.b_wlcmTile.code h1{color:#09743d}.b_wlcmTile.design h1{color:#7111b0}.b_wlcmTile.write h1{color:#0e56bb}.b_wlcmTile.laugh h1{color:#94302d}.b_wlcmTile.chat h1{color:#174ae4}.b_wlcmTile.organize h1{color:#08684a}.b_wlcmTile.compare h1{color:#b0570c}.b_wlcmTile.shop h1{color:#436dcd}.b_wlcmTile.travel h1{color:#444}.b_wlcmTile.create h1{color:#066da7}.b_wlcmTile.shopwithbing{box-shadow:0 4px 15px 1px var(--cib-color-fill-accent-strong-primary)}.b_wlcmTile.shopwithbing h1{color:#436dcd;font-size:20px;font-weight:600}.b_wlcmTile.freesydney{box-shadow:0 4px 15px 1px var(--cib-color-fill-accent-strong-primary)}.b_wlcmTile.freesydney h1{color:#436dcd;font-size:20px;font-weight:600}.b_wlcmTile.createwithsuno h1{color:#5e438f;font-size:14px}.b_wlcmTile.onthisday h1{color:#066da7;font-size:14px;font-weight:600}.b_wlcmTile.onthisday h1::before{background-repeat:no-repeat}.b_wlcmTile.hasImage{padding:0;border:none}.b_wlcmTile.hasImage img{border-radius:inherit}.b_wlcmTile.hasImage.neurips{box-shadow:0 4px 15px 1px var(--cib-color-fill-accent-strong-primary)}.b_wlcmTile.hasImage.neurips h1{color:#436dcd;font-size:20px;font-weight:600}.b_wlcmTilePrivacy{padding-top:10px;color:var(--cib-color-fill-accent-strong-primary)}body.b_dark #b_sydWelcomeTemplate_ .b_wlcmTileCont .b_wlcmTile{box-shadow:unset}.b_wlcmPersLogo.copilot,body.b_dark #b_sydWelcomeTemplate_ .b_wlcmPersLogo.copilot{content:url(/rp/_WT61VllAyx4fyquJgufM7hwmeg.png)}.b_wlcmPersLogo.sydney,body.b_dark #b_sydWelcomeTemplate_ .b_wlcmPersLogo.sydney{content:url(/rp/ZIFQVh6WX4QKgPWuBMBKNrERh3A.png)}.b_wlcmPersLogo.designer,body.b_dark #b_sydWelcomeTemplate_ .b_wlcmPersLogo.designer{content:url(/rp/l8iFubC9EgDGvKo7xeNIvSQwikU.png)}.b_wlcmPersLogo.travel,body.b_dark #b_sydWelcomeTemplate_ .b_wlcmPersLogo.travel{content:url(/rp/yVphAWXw9yH5SA2dSSirhTicqlI.png)}.b_wlcmPersLogo.cooking,body.b_dark #b_sydWelcomeTemplate_ .b_wlcmPersLogo.cooking{content:url(/rp/BqcV_rAG5D1CLdivtAHll5am1pU.png)}.b_wlcmPersLogo.fitness,body.b_dark #b_sydWelcomeTemplate_ .b_wlcmPersLogo.fitness{content:url(/rp/Z8Pa_8PskXiauLlSopMbEWSFgzg.png)}.b_wlcmPersLogo.custom{content:url(/rp/burpYy-CRO1vgH1vjAqyS6-OGgA.png)}body.b_dark #b_sydWelcomeTemplate_ .b_wlcmPersLogo.custom{content:url(/rp/QrTZOk5rYcdxoIXxpBmn5g5csJc.png)}.b_wlcmTile h1::before,.b_wlcmTile.chat h1::before{background-image:url(/rp/kEvTUFzKj2D-xte4DoUlrzmPyEc.png)}.b_wlcmTile.code h1::before{background-image:url(/rp/tucoHZVv4ubzFjfXhxKqf3gaIVA.png)}.b_wlcmTile.design h1::before{background-image:url(/rp/hXyyS-qUAYcgnA86cK6MeAQqLLY.png)}.b_wlcmTile.write h1::before,.b_wlcmTile.hasImage.neurips h1::before{background-image:url(/rp/IOALgZVf8sFUYZ411VgUf34Jeuo.png)}.b_wlcmTile.laugh h1::before{background-image:url(/rp/a-G2ZteqzuB0ZXKzAc0B_fAinFc.png)}.b_wlcmTile.organize h1::before{background-image:url(/rp/D0TjmITlkNYeShKg4VZq6uwFVPU.png)}.b_wlcmTile.compare h1::before{background-image:url(/rp/-KutFzCK2PE8OulbflI9msUMhHQ.png)}.b_wlcmTile.shop h1::before,.b_wlcmTile.shopwithbing h1::before{background-image:url(/rp/QnAXqFPafDMdyUoS-wnr6nQHB7M.png)}.b_wlcmTile.travel h1::before{background-image:url(/rp/A61yW6snD6XU1-8bpFoh_CQoaRo.png)}.b_wlcmTile.create h1::before{background-image:url(/rp/jxWPRKr_ayAp7F6IpAjhwabKW3I.png)}.b_wlcmTile.freesydney h1::before{background-image:url(/rp/Z4vyvteU-KT7dvKWOhpvT2qjgFs.png)}.b_wlcmTile.createwithsuno h1::before{background-image:url(/rp/GViBJydZ8nSlFjhmXPiqDLAynQ0.png)}.b_wlcmTile.onthisday h1::before{background-image:url(/rp/MNhmfKaCGPLMqVRVashuCg7ESKM.png)}#b_sydWelcomeTemplate{display:none}#b_tween{position:relative}#b_tween>div,#b_tween>span:not(.b_hide),#b_tween_searchTools>div,#b_tween_searchTools>span{display:inline-block}#b_content:not(.mapOverlayOnSerp) .b_hidetoggletween{display:none}#b_content:not(.mapOverlayOnSerp) #b_tween.b_hidetoggletween,#b_tween>div.b_hide{display:none}#b_tween_searchResults{line-height:30px}#b_tween_searchTools{height:30px}#b_tween>div>span{padding-right:25px}#b_tween #b_tween_searchTools .ftrH{padding-left:0}#b_tween .ftrB .b_dropdown{z-index:9}.ftrH,.ftrHd,.ftrD>a,.ftrD>div{white-space:nowrap;height:36px;line-height:36px;padding:0 16px}.ftrD{max-height:216px;overflow:hidden;min-width:125px}.amazonBackground{background:#f2f2f2 !important}.ftrD>a,.ftrD>div{display:block}.ftrS .ftrD{margin-right:10px}.ftrSbR{position:absolute;top:0;right:0;height:100%;margin:5px 0;padding-right:3px}.ftrB .sw_ddbk{margin:2px 0 2px 7px}.ftrB .sw_tpcg,.ftrB .sw_tpcbl{margin:0 0 0 14px}.ftrH,.ftrHd,.ftrB .sw_ddbk,.ftrB .sw_tpcg,.ftrB .sw_tpcbl{display:inline-block;zoom:1}#b_tween a.ftrH{height:30px;line-height:30px;background-color:transparent;border:none;border-radius:16px;max-width:220px;padding:0 15px}#b_tween .ftrB a.ftrH.b_selected,#b_tween .ftrB a.ftrH:hover,#b_tween .ftrB a.ftrH:focus{color:#111;cursor:pointer;background-color:#fff}@media screen and (-ms-high-contrast:active){#b_tween a.ftrH,div.b_dropdown .ftrD a{color:WindowText}}#b_tween .ftrB .b_dropdown{position:absolute;background-color:#fff;border:1px solid #ccc;border-radius:6px;box-shadow:0 2px 3px 0 rgba(0,0,0,.1);margin-top:6px;min-width:160px;overflow:hidden;animation:.25s ease 0s 1 normal forwards tweenDropDown}#b_tween .ftrB .b_dropdown.hasError{max-height:350px !important}#b_tween .ftrB .b_dropdown.hasError .ftrD{max-height:350px}@keyframes tweenDropDown{from{max-height:0}to{max-height:312px}}#b_tween .b_dropdown .b_toggle{font-size:13px;color:#666}#b_tween .b_dropdown .b_selected{color:#111;background:#f5f5f5 !important}#b_tween .b_dropdown .b_highlighted{color:#111;font-weight:bold}.filDrop{display:inline-block;padding-left:8px;font-size:9px;vertical-align:bottom}#b_results .b_algo .b_vlist2col.b_deep{color:#767676}#b_results .b_algo .b_vlist2col.b_deep ul{width:274px}#b_results .b_algo .b_vlist2col.b_deep,#b_results .b_algo .b_deep.b_moreLink{padding-left:16px}#b_results .b_algo .b_vlist2col.b_deep>ul:first-child{margin-right:0}#b_results .b_algo .b_vlist2col.b_deep>ul:nth-child(2){margin-left:44px}#b_results .b_algo form.b_externalSearch{margin-left:16px}#b_content #b_results .tpcn .tpic .wr_fav{border-radius:6px}#b_results .tpcn .tpic .wr_fav .siteicon img{border-radius:4px}#b_results .tpcn .sw_ddgn:after{transform-origin:-180px -52px}#b_results .b_algo{position:relative}#b_results .b_algo .tpic .wr_fav{position:relative}#b_results .tpic .wr_fav .siteicon img{top:4px;left:4px;width:18px;height:18px;position:absolute}#b_results .tpic .wr_fav img.siteicon.rms_img{width:18px;height:18px}#b_results .b_algo:hover .tpic .wr_fav{box-shadow:0 3px 7px rgba(0,0,0,.13),0 .6px 2px rgba(0,0,0,.1)}#b_results .b_algo:hover .tptxt{text-shadow:0 3px 7px rgba(0,0,0,.13),0 .6px 2px rgba(0,0,0,.1)}#b_results .b_algo:hover .tpcn .tptt,#b_results .b_algo:hover .tpcn .b_attribution cite,#b_results .b_algo:hover .tpcn .b_attribution .c_tlbxTrg{color:#111}#b_results .tilk{display:flex}.b_algo{position:relative}#b_results .b_algo .tpcn{border-bottom:none;padding:0 0;margin:0 0;display:flex;position:relative}.b_algo .tpcn>.tilk,.b_algo .tpcn strong>.tilk{padding-bottom:4px}.b_algo .tpcn .tpic{display:flex;height:38px;flex-direction:row;align-items:center;margin-right:8px}#b_results .b_algo .tpcn .b_attribution{padding-bottom:0;padding-top:0;margin-top:0;height:20px;line-height:20px;font-size:13px;display:flex}#b_results .b_algo .tpic .wr_fav{width:26px;height:26px;text-align:center;border:1px solid #ececec;background-color:#f5f5f5;border-radius:50%;display:flex;align-items:center;justify-content:center;margin-right:0}.tpcn .tptt{height:18px;padding-bottom:0;line-height:18px;font-size:14px;color:#111}#b_results .tpcn .b_attribution{line-height:20px}.tpcn .b_attribution cite,.tpcn .b_attribution .c_tlbxTrg{color:#444}#b_results .b_algo .gb_lnk:hover{text-decoration:none}.tpmeta{display:flex}#b_results .tpmeta .b_rebateSlug .algoSlug_icon,#b_results .tpmeta .b_algoSlug .algoSlug_icon{margin-bottom:0}#b_results .b_topicon_topslugin .algoSlug_icon{font-size:14px;line-height:16px;color:#71777d;margin-bottom:8px}.tilk:hover{text-decoration:none}#b_results .tpcn .scs_arw,#b_results .tpcn .scs_cls{bottom:-42px}#b_results .tpcn .scs_exp{position:absolute;top:0;right:0}#b_results .wr_fav .siteicon img,#b_results .wr_fav img.siteicon{vertical-align:text-top}#b_results .nattr img.siteicon,#b_results .mattr img.siteicon,#b_results .sh_favicon>img.siteicon{vertical-align:inherit;margin-bottom:-1px}#b_results .nattr span.wr_fav,#b_results .mattr span.wr_fav{margin-right:4px}.wr_fav{margin-right:8px}#b_results .nattr .b_attribution,#b_results .mattr .b_attribution,#b_results .cattr .b_attribution,#b_context .mattr .b_attribution,.wr_fav{display:inline-block}.wr_fav .cico{border-radius:0;display:inline-block;overflow:unset}.b_ansb .wr_fav,.wr_fav.b_hide{display:none}.b_mapImg .cico,.sa_as .cico,#reviews_tp .cico{border-radius:0}.cico{overflow:hidden;border-radius:6px}.clpimg,.clpdimg,.bsimg{width:100%}.cico picture{line-height:0;display:block}z{a:1}.b_caption.b_snippetgobig p{font-size:18px;line-height:24px !important;color:#111}.b_algo.b_algoBorder{box-shadow:0 0 0 1px rgba(0,0,0,.05);border-radius:6px}#b_results>li.b_algoBorder{margin-top:8px;padding-top:15px}#b_results>li.b_algoBorder.b_algo_feedback{margin-bottom:19px;position:relative}.b_gobig_feedback{position:absolute;right:19px;bottom:-21px}#b_results>li.b_algoBorder.b_algo_feedback+li.b_ans.b_mop.b_mopb{margin-top:28px}.b_rebateSlug .algoSlug_icon,.b_algoSlug .algoSlug_icon{margin-right:2px;border:1px solid #ececec;color:#666;padding:0 2px;font-size:11px;vertical-align:middle;line-height:11px;border-radius:6px;margin-bottom:3px}.b_rebateSlug .algoSlug_icon{margin-bottom:0;margin-top:3px;float:left}.b_vList>li.b_annooverride{padding-bottom:0}a{a:1}@import'./_attribution.scss';.recommendationsTableTitle{border-top:0}.recommendationsTableTitle h2{padding-bottom:8px;font-size:13px;color:#666;text-transform:uppercase}.recommendationsTableFeedback{margin-top:2px;margin-bottom:21px}.recommendationsTableFeedback .fbans .b_footnote,.recommendationsTableFeedback .fbans .hlig{text-align:left !important}.pagereco_DText,.pagereco_DText_M{font-size:11px;line-height:16px;color:#767676}.pagereco_DText{margin:-4px 0 12px}.pagereco_DText_M{padding-top:10px}.pagereco_TRow{display:flex;flex-wrap:wrap;width:100%;line-height:16px;margin-bottom:10px}.pagereco_TRow:not(.pagereco_TRowM) .pagereco_TDomain{width:165px}.pagereco_TRow:not(.pagereco_TRowM) .pagereco_TDomainWithFavicon{width:188px}.pagereco_TTitle{flex:1}.pagereco_TTitle a:hover{text-decoration:none}.pagereco_TTitle .b_title:hover{text-decoration:underline}.pagereco_TRow .b_title,.pagereco_TRow .b_attr{text-overflow:ellipsis;overflow:hidden;display:-webkit-box;-webkit-box-orient:vertical;-webkit-line-clamp:1}.pageRecoContainer .b_fpl_cntr{display:flex}.pageRecoContainer .b_fpl_cntr .b_fpl_icon{display:inline-block;margin-right:8px;height:16px}.pageRecoContainer .b_fpl_cntr .b_fpl_icon .cico{border-radius:2px}.rpr_dark .pageRecoContainer .b_fpl_cntr .b_fpl_icon .cico{background-color:#faf9f8}.pageRecoContainer .pagereco_TRow.pagereco_TRowM .b_fpl_icon{margin-top:3px}.pageRecoContainer .b_fpl_attr{display:inline-block}.fbans>div>a,.fbans>div>a:visited{color:#767676 !important}.fbans{padding-right:19px;margin-top:-4px;margin-bottom:-9px}.fbans .b_footnote,.fbans .hlig{padding:0;text-align:right}a{a:1}.b_caption>.b_richcard{padding-top:2px}.b_richcard .b_vList{line-height:20px}.b_richcard+.b_factrow{margin-top:-10px}.b_richcard .tab-head{margin-bottom:12px}.rc_herotabheader .tab-head{border-bottom:1px #ddd;border-bottom-style:double}.richcard_heroSection{padding-top:4px;color:#111}.b_richcard .richcard_heroSection .b_vList{line-height:22px}.tab-container{overflow:hidden;margin:0 auto;white-space:nowrap;padding-bottom:10px}.tab-head{display:inline-block;margin-bottom:10px;width:100%}.tab-menu{display:inline-block;overflow:hidden;vertical-align:top;width:100%}.tab-menu.tab-hasnav{margin-left:2px;margin-right:2px;height:32px;line-height:32px}.tab-menu ul{list-style:none;margin-left:0}.tab-menu ul.sp_1{transition:margin 320ms cubic-bezier(.3,.55,.1,1)}.tab-menu ul.sp_2{transition:margin 400ms cubic-bezier(.3,.55,.1,1)}.tab-menu ul.sp_3{transition:margin 460ms cubic-bezier(.3,.55,.1,1)}.tab-menu ul.sp_4{transition:margin 560ms cubic-bezier(.3,.55,.1,1)}.tab-menu ul.sp_5{transition:margin 680ms cubic-bezier(.3,.55,.1,1)}.tab-menu.tab-flex ul{display:flex}ul.tab-notrans{transition:none}.tab-menu li,.tab-navl,.tab-navr{display:inline-block;cursor:pointer;text-align:center;line-height:32px;height:32px;width:32px}@media screen and (-ms-high-contrast:active){.tab-navr svg,.tab-navl svg{fill:WindowText}}.tab-menu li.tab-active,.tab-navl.tab-disable,.tab-navr.tab-disable{cursor:default}@media screen and (-ms-high-contrast){.tab-menu li.tab-active{border-bottom:1px solid WindowText;height:30px}}.tab-menu li{padding:0 16px;margin-right:2px}.tab-fullwidth li{margin-right:0}.tab-navl.tab-disable,.tab-navr.tab-disable{background-color:#ececec}.tab-navl.tab-disable:hover,.tab-navr.tab-disable:hover{background-color:#ececec}.tab-navl.tab-disable .sv_ch,.tab-navr.tab-disable .sv_ch{fill:#444;opacity:.2}.tab-navl.tab-disable .sv_ch:hover,.tab-navr.tab-disable .sv_ch:hover{fill:#444;opacity:.2}.tab-navl:hover,.tab-navr:hover{background-color:#f5f5f5}.tab-navl .sv_ch:hover,.tab-navr .sv_ch:hover{fill:#111}.tab-menu li.tab-active{line-height:32px}.tab-menu.tab-fullwidth li.tab-active{box-shadow:none;line-height:32px;background-color:#ececec;color:#111;border:0;border-top:none}.tab-content{white-space:normal}.tab-hide{display:none}.tab-ajaxLoad{background:url(/rp/Dq3c4WiQf6W_1npctmuqOl5qkG0.gif) no-repeat;width:40px;height:60px;background-size:40px 40px;margin:0 auto;position:relative;top:60px;padding-bottom:30px;transform:translateY(-30px);animation:.4s linear 0s 1 normal forwards delayLoader}@keyframes delayLoader{0%{opacity:0}99%{opacity:0}100%{opacity:1}}.tab-ajaxCompleted{transition:transform .32s cubic-bezier(.3,.55,.1,1)}.tab-ajaxError{margin:30px 0 0;text-align:center}.tab-menu.tab-flex>ul>li{box-sizing:border-box}.tab-menu li{overflow:hidden;-ms-text-overflow:ellipsis;text-overflow:ellipsis;vertical-align:top}.tab-menu li:last-child{margin-right:0}.tab-head,.tab-menu>ul{height:32px}.tab-head.tab-customLight .tab-menu li{background-color:transparent;color:#444;opacity:.8}.tab-head.tab-customDark .tab-menu li{background-color:transparent;color:#fff;opacity:.8}.tab-head.tab-customLight .tab-menu li:hover,.tab-head.tab-customDark .tab-menu li:hover{box-shadow:none;border-top:0}.tab-head.tab-customLight .tab-navl,.tab-head.tab-customLight .tab-navr,.tab-head.tab-customDark .tab-navl,.tab-head.tab-customDark .tab-navr{background-color:transparent}.tab-head.tab-customLight .tab-menu li.tab-active{box-shadow:inset 0 -3px 0 0 #444;opacity:1}.tab-head.tab-customDark .tab-menu li.tab-active{box-shadow:inset 0 -3px 0 0 #fff;opacity:1}.tab-head.tab-customLight .tab-menu.tab-fullwidth li,.tab-head.tab-customDark .tab-menu.tab-fullwidth li{box-shadow:none;border-top:0;font-weight:normal}.tab-head.tab-customLight .tab-navl span,.tab-head.tab-customLight .tab-navr span,.tab-head.tab-customDark .tab-navl span,.tab-head.tab-customDark .tab-navr span{background-image:url(/rp/S9lIHAMBy7JxaR5m1KRvMwDswyw.png);background-size:30px 150px;background-repeat:no-repeat;width:100%;height:100%;margin:0;display:block}.tab-head.tab-customLight .tab-navl span,.tab-head.tab-customLight .tab-navr span{background-position:0 -60px}.tab-head.tab-customDark .tab-navl span,.tab-head.tab-customDark .tab-navr span{background-position:0 0}.tab-head.tab-customLight .tab-navl span,.tab-head.tab-customDark .tab-navl span{transform:scaleX(-1)}.tab-head.tab-customLight .tab-navl.tab-disable span,.tab-head.tab-customLight .tab-navr.tab-disable span,.tab-head.tab-customDark .tab-navl.tab-disable span,.tab-head.tab-customDark .tab-navr.tab-disable span{background-position:0 -30px}.tab-menu li,.tab-navl,.tab-navr{color:#737373}.tab-menu li{background-color:#eee;color:#444}.tab-fullwidth li{background-color:#eee}.tab-navl,.tab-navr{background-color:#eee;user-select:none}.tab-menu li:hover{box-shadow:inset 0 -3px 0 0 #ccc;background-color:#f5f5f5;color:#111;border-top:none}.tab-menu li.tab-active{box-shadow:inset 0 -3px 0 0 #de3700;background-color:#ececec;color:#111;border-top:none}.b_ad .tab-flex li:hover,.tab-flex li:hover{box-shadow:inset 0 -3px 0 0 #ccc;background-color:#f5f5f5;color:#111;border-top:none}.b_ad .tab-flex li.tab-active,.tab-flex li.tab-active{box-shadow:inset 0 -3px 0 0 #de3700;background-color:#ececec;color:#111;border-top:none}@media(forced-colors:active){.b_ad .tab-flex li.tab-active,.tab-flex li.tab-active{position:relative}.b_ad .tab-flex li.tab-active::after,.tab-flex li.tab-active::after{content:"";background-color:#de3700;display:block;position:absolute;bottom:0;left:0;width:100%;height:3px;forced-color-adjust:none}}.b_ans .b_sideBleed,.b_algo .b_sideBleed,.b_ad .b_sideBleed{margin-left:-20px;margin-right:-20px}.b_ans.b_topborder .b_sideBleed,.b_ans.b_nonfirsttopb .b_sideBleed{margin-left:-19px;margin-right:-19px}.b_topborder .b_bottomBleed,.b_nonfirsttopb .b_bottomBleed{margin-bottom:-10px}.b_tophbb .b_bottomBleed{margin-bottom:-19px}.b_topborder .b_topBleed,.b_nonfirsttopb .b_topBleed{margin-top:-15px}.b_tophbb .b_topBleed{margin-top:-15px}#b_context .b_ans .b_sideBleed{margin-left:-20px;margin-right:-20px}#b_context .b_ad .b_sideBleed{margin-left:-20px;margin-right:-20px}#b_context .b_ans:not(:last-child) .b_bottomBleed{margin-bottom:-5px}#b_context .b_ad:not(:last-child) .b_bottomBleed .b_expando .b_ans .b_bottomBleed{margin-bottom:-15px}#b_context .b_ans .b_topBleed{margin-top:-10px}#b_context .b_ad .b_topBleed{margin-top:-10px}#b_context .b_ans .b_entityTP .b_sideBleed,#b_context .b_ad .pa_sb .b_sideBleed{margin-left:-19px;margin-right:-19px}#b_context .b_ans .b_entityTP .b_topBleed,#b_context .b_ad .pa_sb .b_topBleed{margin-top:-9px}#b_context .b_ans .b_entityTP .b_bottomBleed{margin-bottom:-4px}.b_ans .b_sideBleed .b_antiSideBleed,.b_algo .b_sideBleed .b_antiSideBleed,.b_ad .b_sideBleed .b_antiSideBleed{padding-left:20px;padding-right:20px}.b_ans.b_topborder .b_antiSideBleed,.b_ans.b_nonfirsttopb .b_antiSideBleed{padding-left:19px;padding-right:19px}.b_topborder .b_bottomBleed .b_antiBottomBleed,.b_nonfirsttopb .b_bottomBleed .b_antiBottomBleed{padding-bottom:10px}.b_topborder .b_topBleed .b_antiTopBleed,.b_nonfirsttopb .b_topBleed .b_antiTopBleed{padding-top:15px}.b_tophbb .b_bottomBleed .b_antiBottomBleed{padding-bottom:19px}.b_tophbb .b_topBleed .b_antiTopBleed{padding-top:15px}#b_context .b_ans .b_antiSideBleed{padding-left:20px;padding-right:20px}#b_context .b_ad .b_antiSideBleed{padding-left:20px;padding-right:20px}#b_context .b_ans:not(:last-child) .b_antiBottomBleed{padding-bottom:5px}#b_context .b_ad:not(:last-child) .b_antiBottomBleed,.b_expando .b_ans .b_bottomBleed{padding-bottom:15px}#b_context .b_ans .b_antiTopBleed{padding-top:10px}#b_context .b_ad .b_antiTopBleed{padding-top:10px}#b_context .b_ans .b_entityTP .b_antiSideBleed,#b_context .b_ad .pa_sb .b_antiSideBleed{padding-left:19px;padding-right:19px}#b_context .b_ans .b_entityTP .b_antiTopBleed,#b_context .b_ad .pa_sb .b_antiTopBleed{padding-top:9px}#b_context .b_ans .b_entityTP .b_antiBottomBleed{padding-bottom:4px}#b_results li.b_ans.b_mop.b_mopb,#b_results li.b_ans.b_nonfirsttopb{border-radius:6px;box-shadow:0 0 0 1px rgba(0,0,0,.05);margin-top:12px;margin-bottom:10px;padding:15px 19px 10px}#b_results li.b_ans.b_mop.b_mopb .b_sideBleed{margin-left:-19px;margin-right:-19px}@charset "UTF-8";.vsa{position:relative}.vsa .b_slideexp{margin-bottom:5px}.vsa .b_moreLink{padding:8px 0 10px}.vsa #mmcar .b_slidesContainer{clear:both}.vsa .b_headerTitle{padding:6px 0 12px}.mgzsv .mc_vtvc_title{max-height:44px;display:-webkit-box;-webkit-box-orient:vertical;-webkit-line-clamp:2;overflow-y:hidden}.vtvc_desc{margin-top:12px;border-top:1px solid #ddd;padding-top:12px;line-height:18px;height:90px;display:-webkit-box;-webkit-box-orient:vertical;-webkit-line-clamp:5;overflow-y:hidden}#b_results .vsa .b_attribution{padding-bottom:4px}#b_results #serpvidansrr .b_attribution,#b_context #serpvidansrr .b_attribution,#b_context #serpvidans.rrlist .b_attribution{padding:0 0 12px;line-height:18px}#b_results #serpvidansrr .vsatitle,#b_context #serpvidansrr .vsatitle{padding:0 0 2px}#serpvidansrr .rel_ent_w h2>a,#serpvidansrr .rel_ent_w h2>a:visited{color:#111}#serpvidansrr .b_snippet>a{position:relative;background:#3e67b5;border-radius:1.02917px;font-size:12px;line-height:28px;text-align:center;color:#fff;padding:6px 8px;margin-left:10px}#serpvidansrr .b_snippet{margin-bottom:10px;font-size:13px;line-height:17px;color:#444}#serpvidans.rrlist.vasac{margin-top:-10px}#serpvidans.rrlist h2{padding-bottom:0}#mmcar .mc_vtvc_title{font-size:13px}#mmcar .btn{z-index:2}.mc_vtvc_meta_row .mc_vtvc_meta_row_channel::before{content:" · "}.cdx .b_sldrp .slide{box-shadow:none}#b_pole #serpvidans .slide .mc_vtvc.b_canvas .vtmu,#b_pole #serpvidans .slide .mc_vtvc.b_canvas .vtpl{bottom:112px}#b_pole #serpvidans .slide .mc_vtvc.b_canvas .mc_vtvc_meta_pubdate{padding-bottom:0}#b_pole .vsathm.dark #serpvidans .mc_vtvc,#b_pole .vsathm.grey #serpvidans .mc_vtvc,#b_pole .vsathm.black #serpvidans .mc_vtvc{border:1px solid #353535;background-color:#151515}#b_pole .vsathm.light #serpvidans .mc_vtvc,#b_pole .vsathm.white #serpvidans .mc_vtvc{border:1px solid #eee;background-color:#fff}#b_pole .vsathm.blue #serpvidans .mc_vtvc{background-color:#f6f9fd}#b_pole .vsathm.brown #serpvidans .mc_vtvc{background-color:#fdf9f6}#b_pole .vsathm.green #serpvidans .mc_vtvc{background-color:#f8fdf6}#b_pole .vsathm.orange #serpvidans .mc_vtvc{background-color:#fdf9f6}#b_pole .vsathm.pink #serpvidans .mc_vtvc{background-color:#fdf6f9}#b_pole .vsathm.purple #serpvidans .mc_vtvc{background-color:#fdf6fd}#b_pole .vsathm.red #serpvidans .mc_vtvc{background-color:#fdf7f6}#b_pole .vsathm.yellow #serpvidans .mc_vtvc{background-color:#fdfbf6}#b_pole .vsathm.teal #serpvidans .mc_vtvc{background-color:#f6fdfd}#serpvidans .mc_vtvc_tot .mc_vtvc_title strong{font-weight:500}#b_pole #serpvidans .slide .mc_vtvc_tot .mc_vtvc_meta{position:absolute;padding:12px 16px;height:74px}#b_pole #serpvidans .slide .mc_vtvc_tot .mc_vtvc_meta .mc_vtvc_title{position:relative;margin-bottom:4px;line-height:15px;font-size:13px;height:30px;top:0;left:0;width:100%}#b_pole #serpvidans .slide.onhov .mc_vtvc_tot .mc_vtvc_meta .mc_vtvc_title{height:18px;line-height:18px}#b_pole #serpvidans .slide .mc_vtvc_tot .mc_vtvc_meta .mc_vtvc_meta_block_area{top:0;left:0;height:20px;width:100%}#b_results .b_ans.b_mop.b_vidAns .rel_ent_w.rel_ent_crs,#b_results .b_ans.b_bop.b_vidAns .rel_ent_w.rel_ent_crs,#b_results .b_ans.b_vidAns:nth-child(n+5) .rel_ent_w.rel_ent_crs{content-visibility:auto;contain-intrinsic-size:1px 50px;margin-right:-19px;padding-right:19px;margin-left:-19px;padding-left:19px;padding-bottom:4px}#b_results .b_ans.b_mop.b_vidAns #mmcar,#b_results .b_ans.b_bop.b_vidAns #mmcar,#b_results .b_ans.b_vidAns:nth-child(n+5) #mmcar{content-visibility:auto;contain-intrinsic-size:1px 265px;margin:0 -20px -5px -20px;padding:0 20px 5px 20px}#b_results .b_ans.b_mop.b_vidAns #vsb_tr_c,#b_results .b_ans.b_bop.b_vidAns #vsb_tr_c,#b_results .b_ans.b_vidAns:nth-child(n+5) #vsb_tr_c{content-visibility:auto;contain-intrinsic-size:1px 259px}.mmtitle>a{display:block}.mc_fh{height:100%;border-radius:6px}.mc_tc_bs{overflow:hidden}.b_slideexp{margin-bottom:20px;position:relative}.b_ans>.b_slideexp>.slide:last-child,.b_ans>.b_slideexp:last-child,.b_vPanel .b_slideexp:last-child{margin-bottom:0;padding-bottom:0}.b_slidebar .slide{display:inline-block;vertical-align:top}.b_slidebar .slide,.b_slideexp .b_viewport{overflow:hidden}.b_slideexp .b_viewport{margin:auto}.b_slidebar{white-space:nowrap}.b_slidebar .slide{white-space:normal;position:relative}.b_cards .cico,.b_slidebar .slide .cico{border-radius:0}.b_slidebar,.b_slidebar .slide{width:100%}.b_slidebar.anim{transition:margin-left .35s cubic-bezier(.15,.85,.35,1)}.slide>.spinner{position:absolute;left:50%}.slide>.spinner>.inner{position:relative;left:-50%;width:40px;height:40px;background:url(/rp/OJWYLxkTdSOmE7-V53KpAdOj-xY.gif) no-repeat;margin:40px auto 30px;z-index:1000}.slide_mask.hideSlideMask{visibility:hidden}.b_slidebar.b_autoslidingfade .slide.slide_fading{opacity:1}.slide_mask,.b_slidebar.b_autoslidingfade .slide{transition:opacity .3s linear}.slide_mask.slide_fading,.b_slidebar.b_autoslidingfade .slide{opacity:0}.slide_mask{position:absolute;width:100%;height:100%;opacity:.7;top:0}.carousel_seemore{text-align:center}.carousel_seemore.dark a{color:#fff}.b_slidebar.enable_selecting .slide.selected::after,.b_slidebar.enable_selecting .slide:hover::after{box-shadow:inset 0 0 0 2px #fff}.b_slidebar .slide.selected::after,.b_slidebar .slide:focus::after{box-shadow:inset 0 0 0 2px #0099bc;outline:0}.b_slidebar.enable_selecting .slide.selected::after,.b_slidebar.enable_selecting .slide:hover::after,.b_slidebar .slide.selected::after,.b_slidebar .slide:focus::after{content:"";height:100%;width:100%;position:absolute;left:0;top:0}.b_slideexp .b_antiSideBleed{display:inline-block}.carousel_seemore>.b_moreLink.rndChev{vertical-align:middle;height:92px;text-decoration-color:#444;display:inline-block}.carousel_seemore .seeAll_txt{display:block;color:#444;line-height:17px}.carousel_seemore .seeAll_chev{display:block;height:48px;padding-bottom:12px;margin-top:15px}html[dir="rtl"] .carousel_seemore .seeAll_chev{transform:scaleX(-1)}.b_slideexp .b_viewport.scrollbar{overflow-x:auto;-ms-overflow-style:none;scrollbar-width:none}.b_slideexp .b_viewport.scrollbar::-webkit-scrollbar{display:none}.b_slideexp .b_viewport{-webkit-overflow-scrolling:touch}.b_overlay .btn.rounded{position:absolute;cursor:pointer;z-index:1;-moz-user-select:none;-khtml-user-select:none;-webkit-user-select:none;-o-user-select:none;-ms-user-select:none;user-select:none}.b_overlay .btn.rounded,.b_overlay .btn.rounded .bg,.b_overlay .btn.rounded .cr,.b_overlay .btn.rounded .cr>div,.b_overlay .btn.rounded .vcac>div{border-radius:50%}.b_overlay .btn.rounded .vcac{height:0}.b_overlay .btn.rounded{height:32px;width:32px;top:50%;margin-top:-16px}.b_overlay .btn.rounded .bg,.b_overlay .btn.rounded:hover .bg{opacity:0}.b_overlay .btn.rtl.rounded .cr{direction:ltr}.b_overlay .btn.hidden.rounded .cr,.b_overlay .btn.disabled.rounded .cr{visibility:hidden}.b_overlay .btn.rounded .cr>div{border:1px solid #ececec;box-shadow:0 2px 3px 0 rgba(0,0,0,.1);height:30px;width:30px;overflow:hidden;background-image:none;background-color:#fff}.b_overlay .btn.rounded .cr>div:hover{box-shadow:0 2px 4px 1px rgba(0,0,0,.14)}.b_overlay .btn.rounded .cr>div:after{bottom:5px;background-color:#fff;transform-origin:-430px 0;display:inline-block;transform:scale(.5);position:relative}.b_overlay .btn.rounded .cr>div:hover:after{transform-origin:-514px 0}.b_overlay .btn.ltr.rounded .cr>div:after{right:5px}.b_overlay .btn.rtl.rounded .cr>div:after{left:5px}.b_overlay .btn.prev.ltr.rounded .cr,.b_overlay .btn.next.rtl.rounded .cr{transform:scaleX(-1)}body .b_overlay .btn.rounded.next{right:-12px}body .b_overlay .btn.rounded.prev{left:-13px}.ra_car_container .b_overlay .btn.prev.ltr.rounded .cr>div,.ra_car_container .b_overlay .btn.next.rtl.rounded .cr>div{transform:unset}.ra_car_container .b_overlay .btn.rounded .cr>div{background-position:0;border:unset}.ra_car_container .b_overlay .btn.rounded .cr>div:after{content:unset}@media screen and (-ms-high-contrast:active){.b_overlay .btn.rounded.hidden *,.b_overlay .btn.rounded.disabled *{background:none}.b_overlay .btn.rounded.hidden,.b_overlay .btn.rounded.disabled{background:none}}.b_overlay .btn.rounded .cr>div:after{content:url(/rp/Qk540QN8GIaMmT8oEUEVF6N8IF8.png)}.b_overlay{position:relative}.vcac{position:absolute;width:100%;top:50%}.vcac>div{position:relative;width:100%}.vsarf .mc_vtvc .mc_vtvc_meta { padding: 16px 16px 10px 16px; } .vsarf .mc_vtvc .mc_vtvc_meta_w { height: 113px; margin-top: -0px; } .vsarf .mc_vtvc .mc_vtvc_title { height: 36px; line-height: 18px; margin-bottom: 0px; margin-top: 0px; } .vsarf .mc_vtvc .mc_vtvc_meta_block_area { height: 51px; } .vsarf .mc_vtvc .vtmu, .vsarf .mc_vtvc .vtpl { bottom: 121px; } .vsarf .mc_vtvc_th_dock { height: 113px; } .vsarf .mc_vtvc_th .cico { height: 132px; } .mc_vtvc{background-color:#fff;box-shadow:0 0 0 1px rgba(0,0,0,.05);line-height:0;margin:0;position:relative;border-radius:6px;overflow:hidden}.mc_vtvc.noshadow{box-shadow:none}.mc_vtvc_con_rc{border-radius:6px;overflow:hidden;position:relative}.mc_vtvc>a{color:#71777d;display:block;text-decoration:none;width:100%}.mc_vtvc>a:focus::after{outline:2px solid #a5c7fe;width:100%;height:100%;content:'';outline-offset:-2px;position:absolute;top:0;left:0}.mc_vtvc_th{background-color:#d5d5d5;position:relative}.mc_vtvc_th .cico{border-radius:0}.mc_vtvc_ban_lo,.mc_vtvc_ban_up{position:absolute;vertical-align:middle}.mc_vtvc_ban_lo{bottom:0}.mc_vtvc_ban_up{top:0}.mc_vtvc_title{font-weight:normal;margin-bottom:11px;overflow:hidden;color:#111;height:54px;line-height:18px}.mc_vtvc_title a{display:inline-block;color:#111}.mc_vtvc_title a:hover{text-decoration:underline}.mc_vtvc_src_ico{float:left;margin-right:4px}.mc_vtvc_act{height:16px;margin-top:-40px;padding:12px 8px;z-index:1}.mc_vtvc_actc{right:16px;bottom:16px;position:absolute;display:inline-block;z-index:1}.mc_vtvc_act_sep{border-top:1px solid #d5d5d5;height:40px;margin:0 8px}.mc_vtvc_fh .mc_vtvc_act_sep,.mc_vtvc_fh .mc_vtvc_act{visibility:hidden}#serpvidans .b_topTitle{margin-bottom:8px}.mc_vtvc_htc{width:100%;height:100%;position:absolute;top:0;bottom:0;left:0;right:0}.mc_vtvc_htb{width:100%;height:100%;background:rgba(0,0,0,.7);position:absolute;top:0;bottom:0;left:0;right:0}.mc_vtvc_ht{width:100%;padding:0 16px;line-height:16px;color:#fff;text-decoration:underline;word-break:break-word;box-sizing:border-box;vertical-align:middle;text-align:center}.mc-vtvc_th_live_b{background-color:#c80000;color:#fff;display:inline-block;padding:2px 8px;font:11px/14px Arial;border-radius:2px;text-transform:uppercase;height:15px;width:26px;position:absolute;left:8px;top:110px}.isvctrl .isv .mc_vtvc_ban_up{left:0;right:initial}.mc_vtvc_ban_lo,.mc_vtvc_ban_up{right:0}.vt_text.b_lRight .b_lLeft{margin:0 0 0 1px;height:14px;line-height:14px;padding:2px 8px;background:rgba(0,0,0,.75);border-radius:2px;font-weight:bold}.mc_vthtb{width:100%;height:100%;background:rgba(0,0,0,.7);position:absolute;top:0;bottom:0;left:0;right:0;display:table}.mc_vtht{width:100%;padding:0 16px;line-height:16px;color:#fff;text-decoration:underline;word-break:break-word;box-sizing:border-box;vertical-align:middle;text-align:center;display:table-cell}.vt_text.b_lRight .b_lLeft{margin:0 0 0 1px;height:14px;line-height:14px;padding:2px 8px;background:rgba(0,0,0,.75);border-radius:2px;font-weight:bold}.emptyStyleForDebuggingPurpose{top:0}.emptyStyleForDebuggingPurpose{top:0}.mc_vtvc_center_play{width:32px;height:32px;background-size:contain;position:absolute;margin:auto;bottom:0;top:0;left:0;right:0;box-shadow:none;border-radius:0}.mc_vtvc_center_play.rmvbg{width:32px;height:32px;background-image:none}.mc_vtvc_htb,.mc_vtvc_ht{display:none}.vt_onhv .mc_vtvc_htb{display:table}.vt_onhv .mc_vtvc_ht{display:table-cell}.mc_vtvc_center_play{display:inline-block}.vt_onhv .mc_vtvc_center_play{display:none}.mc_vtvc .vtmu,.mc_vtvc .vtpl{bottom:163px}.vsarf .mc_vtvc .vtmu,.vsarf .mc_vtvc .vtpl{bottom:122px}.svarh #mmcar .mc_vtvc .vtmu,.svarh #mmcar .mc_vtvc .vtpl{bottom:137px}.svarht #mmcar .mc_vtvc .vtmu,.svarht #mmcar .mc_vtvc .vtpl{top:8px;left:8px}.mc_vtvc_center_play{background-image:url(data:image/svg+xml,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20viewBox%3D%220%200%2040%2040%22%3E%3Ctitle%3EPlay_40x40%3C%2Ftitle%3E%3Cpath%20d%3D%22M20%20.833a19.167%2019.167%200%201%200%2019.167%2019.167%2019.167%2019.167%200%200%200-19.167-19.167z%22%20opacity%3D%22.6%22%2F%3E%3Cpath%20d%3D%22M20%202a18%2018%200%201%201-18%2018%2018.02%2018.02%200%200%201%2018-18m0-2a20%2020%200%201%200%2020%2020%2020%2020%200%200%200-20-20zm8.072%2020.9a1.671%201.671%200%200%201-.513.513l-9.99%206.357a1.671%201.671%200%200%201-2.569-1.413v-12.715a1.671%201.671%200%200%201%202.569-1.41l9.99%206.358a1.671%201.671%200%200%201%20.513%202.31z%22%20fill%3D%22%23fff%22%2F%3E%3C%2Fsvg%3E)}.mc_vtvc_vchl .meta_vc_content{padding:1px 4px;background:#106ebe;border-radius:4px;color:#fff}.mc_vtvc_meta{box-sizing:border-box;display:inline-block;line-height:18px;position:relative;width:100%}.mc_vtvc_meta_block{bottom:0;position:absolute;width:100%}.mc_vtvc_meta_row{line-height:16px;font-size:11px;height:17px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.mc_vtvc_meta_row a{display:inline-block}.mc_vtvc_meta_row a:hover{text-decoration:underline}.mc_vtvc_meta_block_area{position:relative}.mc_vtvc_meta_block,.mc_vtvc_meta_block a{color:#444}.b_dark .mc_vtvc_meta_block{color:#edebe9}.mc_vtvc_meta_row>:nth-of-type(n+2)::before{content:" · "}.mc_vtvc_meta_row .mc_vtvc_meta_row_channel::before{content:"  › "}.mc_vtvc_kmt_title{line-height:18px;padding-bottom:4px;font-weight:bold;color:#00809d;text-transform:uppercase;font-size:11px}.mc_vtvc_kmt,.mc_vtvc_con_rc.onhov .wkmt .mc_vtvc_meta_row,.mc_vtvc_con_rc.onhov .wkmt .tibc_tt{display:none}.mc_vtvc_con_rc.onhov .wkmt .mc_vtvc_title{max-height:18px;margin-bottom:18px;overflow:hidden}.mc_vtvc_con_rc.onhov.mmsts .mc_vtvc_title{visibility:hidden}.mc_vtvc_con_rc.onhov .mc_vtvc_kmt{display:block}.mc_vtvc_kmt_content .b_factrow{line-height:17px;font-size:11px;max-height:34px;overflow:hidden;text-overflow:ellipsis;padding:0 16px 0 0;color:#444}.mc_vtvc_meta .mc_vtvc_kmt_title{line-height:18px}.mc_vtvc_title:hover{text-decoration:none}.mc_vtvc_meta_pubdate{color:#444;padding-bottom:3px}.mc_vtvc_meta_channel{color:#006d21}.mc_vtvc_meta_w,.mc_vplvc_meta_w{position:relative}.mc_vtvc_meta_bg_w,.mc_vplvc_meta_bg_w{height:100%;width:100%;overflow:hidden;position:absolute;top:0}.mc_vtvc_meta_bg_w .cico,.mc_vplvc_meta_bg_w .cico{border-radius:0;overflow:visible}.dg_u .mm_vidch_th_c{overflow:visible}.dg_u .mm_vidch_th_bg img{margin-top:-20px}.emptyStyleForDebuggingPurpose{top:0}.mc_vtvc_meta_w .mc_vtvc_meta{background:rgba(255,255,255,.75)}.mc_vtvc_meta_bg_w img,.mc_vtvc_meta_bg_w .mc_vtvc_cb_w{filter:blur(25px);transform:scale(1.2)}body{--video-metadata-channel-color:#3c3c3c}body.b_dark{--video-metadata-channel-color:unset}.vsarf .mc_vtvc_meta_pubdate{color:unset}.vsarf .mc_vtvc_meta_channel{color:var(--video-metadata-channel-color)}.vsarf .mc_vtvc .mc_vtvc_meta_w .mc_vtvc_title,.vsarf .mc_vtvc .mc_vtvc_meta_w .mc_vtvc_title strong{font-weight:bold}.vsarf .mc_vtvc_meta_w .mc_vtvc_meta_row{font-size:13px}.emptyStyleForDebuggingPurpose{top:0}.vsarf .mc_vtvc_th .cico{height:132px}.emptyStyleForDebuggingPurpose{top:0}.emptyStyleForDebuggingPurpose{top:0}.emptyStyleForDebuggingPurpose{top:0}.emptyStyleForDebuggingPurpose{top:0}.emptyStyleForDebuggingPurpose{top:0}.emptyStyleForDebuggingPurpose{top:0}.emptyStyleForDebuggingPurpose{top:0}.vtbc .mv_vtvc_play{display:inline-block;position:absolute;bottom:8px;left:9px;height:8px;width:7px}.vtbc .mv_vtvc_play_ext{background:url(/rp/Lp38sn_O4jegSK0IHxZVxyp-yKQ.png) -172px -31px;display:inline-block;height:8px;width:12px;position:absolute;bottom:8px;left:6px}.vtbc .pivot{height:20px;width:8px;min-width:8px}.vtbc .mv_vtvc_play{background:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAcAAAAIBAMAAADHKvg1AAAAIVBMVEX///////////////////////////////////////////9/gMdvAAAACnRSTlMAETNEVWaImbvuo4D1oAAAAB9JREFUCB1jmMoABKuaQcSqQhCxKgFGgLhNQIkpQAwA8zkLyQAl6F0AAAAASUVORK5CYII=)}.mc_bc_w{height:18px;padding:8px;text-align:right}.mc_bc{background-color:rgba(0,0,0,.75);padding:2px 8px;line-height:14px;color:#fff;display:inline-block;vertical-align:middle;border-radius:2px;font-weight:bold}.mc_bc_w .pivot{text-align:center;margin-right:1px;height:14px}.vsarr .mmgrid .mc_bc_w .mc_bc{background-color:rgba(0,0,0,.75);opacity:1}.vsarf .mc_vtvc_cb_w { height: 100%; width: 100%; } .vsarf .mc_vtvc_cb { height: 50%; width: 25%; display: inline-block; }.vrhdata{display:none}.smtplayerhtml5{height:100%;width:100%;overflow:hidden}.smtplayerhtml5 video{min-height:100%;min-width:100%}.smtplayerhtml5 .videoplaying{background-color:#000}.smtplayerhtml5.hide{display:none}.vol{width:22px;height:18px;bottom:0;margin-left:1px;margin-right:1px;position:relative;display:inline-block}.vol.hide,.vol .hide{display:none}.vol .bg{background:rgba(0,0,0,.75);border-radius:2px}.vol.bg,.vol.cont{position:absolute;bottom:0}.vol .vol.bg.volnb{border-radius:0 0 2px 2px}.vol .volsliderHandle.bg{border-radius:2px 2px 0 0}.vol.cont .volsliderHandle{height:70px;display:none;width:22px;float:left;bottom:18px;position:absolute;display:block}.vol.cont .volsliderHandle.hide{display:none}.volsliderHandle .vsb{height:54px;width:4px;background-color:#999;margin:9px auto 8px;position:relative;display:block;border-radius:2px}.volsliderHandle .vsh{height:6px;width:14px;padding:9px 7px 9px 7px;margin:0 -12px;display:block;position:absolute;top:30px}.volsliderHandle .vsh.hide{display:none}.volsliderHandle .vshi{height:4px;width:14px;background-color:#fff;border-radius:2px}.volMuteIcon{width:16px;height:14px;margin:2px 4px;float:left}.volMuteHandle{width:22px;height:18px}.vo{background:url(/rp/fFZxBXElP9WYOO0jhTaElyLhEVU.svg) no-repeat}.vm{background:url(/rp/fsX-ZVd03wB2TL0vmQJxSp4U9vs.svg) no-repeat}.vl{background:url(/rp/YXYMPC1Rry_XJGc7Yg8lR4B2eEs.svg) no-repeat}.vf{background:url(/rp/NosrlR4amKTs1zYxWy3laZN3HRk.svg) no-repeat}@media(forced-colors:active){.vol{forced-color-adjust:none}}.vrhcp .vrhol{position:absolute;width:100%;height:35px;max-height:35px;bottom:0;left:0;padding:0;background:none;display:block;z-index:9}.vrhcp .vrhol.hide,.vrhcp .vrhol .hide{display:none}.vrhot{white-space:nowrap;text-overflow:ellipsis;overflow:hidden;display:inline-block;position:absolute;max-width:240px;height:18px;line-height:14px;margin-left:8px;top:10px;left:0;right:0;border-radius:2px;padding-right:8px}.vrhot div{display:inline-block}.vrhot.cont{color:#fff;font-size:11px;font-weight:bold;background-color:rgba(0,0,0,.75);padding:2px 8px;margin-left:0;top:0;box-sizing:border-box;position:relative}.vrhc .ricons{position:absolute;right:8px;top:10px;left:auto;bottom:auto;height:18px;display:inline-block;cursor:pointer}.vrhol.icons_1 .vrhot{margin-right:27px}.vrhol.icons_2 .vrhot{margin-right:49px}.vrhol.icons_3 .vrhot{margin-right:79px}.vrhol .vrhot.cont,.vsb_tr_chd .vrhol.icons_1 .vrhot{margin-right:0}.vpb{position:absolute;display:block;bottom:0;left:0;height:4px}.vpb div{position:absolute}.vpb.cont{width:0;background:#fff}.vpb.cont.test{display:none}.vpb.back{background-color:#999}.vrhcp .vrhol.npb{height:36px;max-height:36px}.vrhol .vadda{width:22px;height:18px;padding:0;margin-right:0;margin-left:2px;bottom:0;position:relative;display:inline-block;z-index:1;background:rgba(0,0,0,.75);border-radius:2px;overflow:hidden}.vrhol .vadda.hide{display:none}.vrhol .vadda .mc_vfaa{margin:3px 5px}.ricons .vol{float:left}.ricons .adultFlag{float:right}.vrhc.inline .vt_vp,.vrhc.popout .vt_vp,.vrhc.mousefollow .vt_vp{position:absolute;bottom:0;border:hidden;padding:0;top:0;left:0;z-index:3}.vrhtc .hide{display:none}.vrh_clc .vt_vp,.vrh_clc .vrhtc .vrhi,.vrh_clc .player_ol{cursor:pointer}.vrh_clc .cico{border-radius:0}.vrhtc{border:hidden;top:0;left:0;padding:0}.vrhc.mousefollow .vrhtc,.vrhc.popout .vrhtc{background-color:#999}.vrhtpc.load .vt_vp{background:url(/rp/J_o2maogFDeUOsovPJL-ofEuxJ4.gif) center center no-repeat}.vrhtpc.load .vt_vp.test{background:url('') center center no-repeat}.vrhc.inline .vrhtc .vrhi,.vrhc.popout .vrhtc .vrhi,.vrhc.mousefollow .vrhtc .vrhi{position:absolute !important;border:hidden;z-index:2;padding:0;left:0;top:0}.player_ol{position:absolute;bottom:0;opacity:0;filter:alpha(opacity=0);border:hidden;z-index:7}.player_ol.load{background:url(/rp/J_o2maogFDeUOsovPJL-ofEuxJ4.gif) center center no-repeat}.vrhcpt{display:flex;flex-direction:column;box-sizing:border-box;padding:16px;line-height:18px;font-size:13px;font-family:Arial,Helvetica,Sans-Serif;background:#fff}.vrhdl{max-height:36px;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden;margin-bottom:12px;color:#444}.vrhtt{font-weight:bold;margin:12px 0;color:#000}.vrhmeta,.vrhsrc{color:#666}.b_dark .vrhmeta,.b_dark .vrhsrc{color:#edebe9}.vrhmeta{order:-1;padding-bottom:12px;border-bottom:1px solid #ececec}.vrhsrc{color:#006d21;border-top:1px solid #ececec;padding-top:12px}.vrhms{margin:0 3px}.vrhmeta .vrhcadl::before{content:" · "}.vrhsrc .vrhcadl::before{content:"  › "}.hide{display:none}.vrhc .vrhcpt span{display:inline;margin-bottom:0}.vrhcpt span.vrhspu{display:none}.vrhcpt.vrhcpt_act .vrhtt{margin-top:0}.vrhcpt.vrhcpt_bg{background:#fff}.vrhc.popout,.vrhc.inline,.vrhc.mousefollow{border-radius:6px;overflow:hidden;display:table-row-group;z-index:4;background:none}.vrhc.popout,.vrhc.mousefollow{box-shadow:0 4px 4px rgba(0,0,0,.1),0 2px 80px rgba(0,0,0,.2)}.vrhc.popout,.vrhc.inline{position:absolute;top:0;border:1px solid #fff}.vrhc.mousefollow{position:fixed}.vrhcp{position:relative;top:0;left:0;display:table-row}.vrhcp .vrhtc{position:relative;overflow:hidden}.vrhc.hide{display:none}@keyframes vh_fadein{from{opacity:0}to{opacity:1}}.vrhc:not(.hide){animation:vh_fadein 250ms}.vrhc.inline{border:hidden}.vrhc.inline{margin:0}.vrhc.inline img{color:transparent}.vrhc.inline.fullsize{height:100%}.vrhc,.vrhc:hover,.vrhc:link,.vrhc:active,.vrhc:visited{color:#000;text-decoration:none}.vrhc.vrh_clc{cursor:pointer}a.hover-anchor{display:block;height:100%;width:100%;text-decoration:none}.wpt_bc_container{margin:0 -3px 0 -3px}.wpt_bc_container cib-serp{max-width:unset;min-height:320px}.b_rs .b_vList,.b_rrsr .b_vList{font-size:13px}#b_results .b_ans .b_rs h2,#b_results .b_rs .b_vlist2col li,#b_results .b_rs .b_vList li{padding-bottom:16px}#b_results .b_ans #brsv3{width:652px;contain-intrinsic-size:652px 296px}#b_results .b_ans #brsv3 h2{padding-bottom:0;font-size:20px;line-height:1.3;color:#202124;margin-bottom:8px}#b_results .b_ans #brsv3 h2 strong{font-weight:normal}#b_results .b_ans #brsv3 .b_vList{padding-top:12px}#b_results #brsv3 .b_vList li{width:318px;padding-bottom:0;margin:4px 0;display:inline-block}#b_results #brsv3 .b_vList li:nth-child(odd){margin-right:16px}#b_results #brsv3 .b_vList li a{max-height:none;min-height:48px;padding:0 17px;background:#f1f3f4;border-radius:100px;box-sizing:border-box;display:flex;align-items:center}#b_results #brsv3 .b_vList li a:hover{text-decoration:underline;color:#202124}#b_results #brsv3 .b_vList li a .b_suggestionIcon{width:20px;height:20px;padding:10px}#b_results #brsv3 .b_vList li a .b_suggestionBk{width:32px;height:32px;padding:0;margin:4px;background:url(/rp/eF3rIdIG4fsLyPy7mzgRnjCDKIA.png) no-repeat -292px 0;transform:scale(.5)}.b_pinrs_ol .b_pinrs .b_suggestionIcon{display:none}#b_results #brsv3 .b_vList li a .b_suggestionIcon svg{width:20px;height:20px}#b_results #brsv3 .b_vList li a .b_suggestionIcon.sug_sprite{background:url(/rp/Qk540QN8GIaMmT8oEUEVF6N8IF8.png) no-repeat -762px -40px;transform:scale(.5)}#b_results #brsv3 .b_vList a .b_suggestionText{max-width:227px;margin-left:16px;font-style:normal;font-size:16px;line-height:19px;color:#202124;display:-webkit-box;text-align:left;-webkit-box-orient:vertical;-webkit-line-clamp:2;overflow-wrap:break-word;overflow:hidden;flex:1}#b_results #brsv3 .b_vList a .b_suggestionText strong{font-weight:bold}.b_rs .b_vlist2col ul:first-child{margin-right:40px}#relatedSearchesLGWContainer{box-sizing:border-box}#relatedSearchesLGWContainer *,#relatedSearchesLGWContainer *:before,#relatedSearchesLGWContainer *:after{box-sizing:inherit}#relatedSearchesLGWContainer{padding:0;box-sizing:border-box;background-color:transparent;border-radius:0;position:fixed;left:0;bottom:120px;transform:translateX(-100%);opacity:0;transition-duration:.5s}#relatedSearchesLGWContainer.rtl{transform:translateX(200%)}#rs_lgw_list{display:flex;flex-direction:column;align-items:flex-start}#rs_lgw_list a{display:grid;grid-template-columns:auto auto auto;background-color:#f1f3f4;border-radius:0 100px 100px 0;align-items:center;margin:10px 12px 0 0;padding:9px 0 9px 0;color:#202124 !important;min-height:50px;text-decoration:none}#rs_lgw_list a:not(:hover) .animation-padding{width:12px;transition-duration:.5s}#rs_lgw_list a{opacity:1;transition-duration:.5s}#rs_lgw_list.child-hover a:not(:hover){opacity:.3;transition-duration:.5s}#rs_lgw_list a:hover .animation-padding{width:32px;transition-duration:.5s}#rs_lgw_list a .b_suggestionText{width:122px;padding-left:12px;padding-right:12px}.rtl #rs_lgw_list a{border-radius:100px 0 0 100px}#rs_lgw_list a .b_suggestionText{overflow:hidden;text-overflow:ellipsis;display:-webkit-box;-webkit-line-clamp:3;line-clamp:3;-webkit-box-orient:vertical;overflow-wrap:break-word}#relatedSearchesLGWContainer h2{font-size:14px;width:120px;margin:0 0 0 1rem;line-height:1rem;overflow:hidden;text-overflow:ellipsis}.rtl #relatedSearchesLGWContainer h2{margin:0 1rem 0 0}#relatedSearchesLGWContainer h2 strong{font-weight:bold}#relatedSearchesLGWContainer.lgw-slide-in{transform:none;opacity:1;transition-duration:.5s}#relatedSearchesLGWContainer .rs_lgw_icon,#relatedSearchesLGWContainer .rs_lgw_icon svg,#relatedSearchesLGWContainer .more-rs-btn-icon,#relatedSearchesLGWContainer .more-rs-btn-icon svg{width:16px;height:16px}.b_ans .b_rs .b_suggestionIcon svg path,.b_ans .b_rrsr .sg_svg svg path{fill:#767676}#relatedSearchesLGWContainer .rs_lgw_icon.sug_sprite{width:40px;height:40px;margin:-12px;background:url(/rp/Qk540QN8GIaMmT8oEUEVF6N8IF8.png) no-repeat -762px -40px;transform:scale(.5)}#relatedSearchesLGWContainer .more-rs-btn-icon.sug_sprite{width:36px;height:20px;margin:-10px;background:url(/rp/Qk540QN8GIaMmT8oEUEVF6N8IF8.png) no-repeat -348px 0;transform:scale(.4)}#rscarousel strong{font-weight:normal}#b_results #inline_rs .b_vlist2col ul:first-child{margin-right:160px;padding-bottom:8px}#b_results #inline_rs .b_vlist2col li{padding-bottom:12px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:210px}#b_results #inline_rs .b_vlist2col{padding-left:0}#rscar_close{float:right}#rscar_close::after{height:16px;width:16px;content:" ";display:inline-block;background-repeat:no-repeat}#rscar_close:hover::after{cursor:pointer}.rslist_title{display:inline-block;color:#444}.rslist_head{padding:12px 0}#inline_rs .b_sideBleed{border-top:1px solid #ececec;padding:0;margin:0}#rscar_close::after{background-image:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAwAAAAMCAYAAABWdVznAAAAR0lEQVR4AWOYOHGiABA7gGgG7ABVDZTxH4gvgARwKL4AVeOAEMDUhFUOpwROxbhMw6UYpyZ0xWRpIMtJJHuapGAlK+JIThoADD3VTZEy3aEAAAAASUVORK5CYII=)}#rscar_close:hover::after{background-image:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAwAAAAMCAYAAABWdVznAAAAR0lEQVQokWNwcXERAGIHEM2AA6CogTL+A/EFbJqgii9A1TigC6BowimHTQKfQbg04VaMQxN+xSRrIMlJJHma5GAlN+KIThoAX1F+rQaZhuoAAAAASUVORK5CYII=)}.sb_pagP_bp:after{transform-origin:-430px 0}.sb_pagP_bp:hover:after,.sb_pagP_bp:active:after,.sb_pagP_bp:focus:after{transform-origin:-514px 0}.sb_pagN_bp:after{transform-origin:-430px 0}.sb_pagN_bp:hover:after,.sb_pagN_bp:active:after,.sb_pagN_bp:focus:after{transform-origin:-514px 0}.sb_inactP:after,.sb_inactP:hover:after{transform-origin:-598px 0}.b_pag a{display:block;text-align:center}.b_pag a.sb_bp{min-width:40px;margin-right:20px;line-height:42px}.b_pag a.sb_pagN,.b_pag a.sb_pagP{border:0;padding:1px}.b_pag a.sb_pagN_bp,.b_pag a.sb_pagP_bp{width:40px;height:40px;margin-top:0}.b_pag a.sb_bp.sb_inactP{min-width:38px}.b_pag a.sb_pagP_bp.sb_inactP{width:38px}.b_pag a.b_roths{transform:rotate(180deg)}.b_pag .sw_prev,.b_pag .sw_next{margin:2px}@media screen and (-ms-high-contrast:active){.sb_pagN:after,.sb_pagP:after{content:""}li.b_pag a.sb_pagN,li.b_pag a.sb_pagP{width:auto}li.b_pag a.b_roths{transform:rotate(0deg)}.b_pag .sw_next,.b_pag .sw_prev{display:block}}z{a:1}.b_pag_lets_chat{display:flex;align-items:center;gap:6px;color:#fff;border-radius:20px;border:none;margin:5px 0;padding:6px 16px 6px 12px;cursor:pointer;background:linear-gradient(129.58deg,#2870ea 20.89%,#1b4aef 77.37%)}.b_pg_chat_btn_svg{height:21px;width:21px;background:url(/rp/WkXfZA9iZ7ItUaPfM--zd16N3Fo.svg) no-repeat}#mfa_root .fba_sbicn{background:url(/rp/Qk540QN8GIaMmT8oEUEVF6N8IF8.png) no-repeat -762px 0;transform:scale(.5)}#b_results>li#mfa_root{right:0;position:fixed;z-index:1011;background-color:transparent;pointer-events:none}#mfa_root .b_ds5{transform:none;transition:transform .4s cubic-bezier(.3,.55,.1,1),opacity .1s;opacity:1;z-index:1011;align-self:flex-end;pointer-events:initial;border-radius:50%;overflow:hidden}#mfa_root.b_fabHide .b_ds5{pointer-events:none;transform:translateX(30px);opacity:0}#mfa_root .b_ds5:active{opacity:.8}#mfa_root .mfa_btn{display:flex;align-items:center;justify-content:center;cursor:pointer}#mfa_root:not(.b_fabexp):focus-within .b_ds5{border:1px solid #919191}#b_results>li#mfa_root { padding: 40px; bottom: 0px; ; } #mfa_root .mfa_btn { background: #fff; width: 56px; height: 56px; } #b_results>li#mfa_root:not(.b_fabexp):focus-within { padding: 39px; }.b_ds5{box-shadow:0 0 0 1px rgba(0,0,0,.08),0 4px 12px 1px rgba(0,0,0,.2);-webkit-box-shadow:0 0 0 1px rgba(0,0,0,.08),0 4px 12px 1px rgba(0,0,0,.2)}a.b_ds5:hover,input.b_ds5:hover{box-shadow:0 0 0 1px rgba(0,0,0,.08),0 4px 14px 2px rgba(0,0,0,.24);-webkit-box-shadow:0 0 0 1px rgba(0,0,0,.08),0 4px 14px 2px rgba(0,0,0,.24)}#b_context.crhide,#b_mtp.crhide{display:none}#b_context.crinvis,#b_mtp.crinvis{visibility:hidden}#b_mtp{display:inline-block;visibility:hidden}#b_mtp:not(.crhide),#b_mtp *{display:inline-block;overflow:hidden;visibility:visible;color:#71777d}#b_context.crshow,.mtpsvg.crshow{opacity:1}#b_context,.mtpsvg{opacity:0;transition:opacity .3s}#b_mtp{width:336px;margin-left:10px;vertical-align:top}.mtprt{height:48px;background:#fff;box-shadow:0 4px 6px 1px rgba(0,0,0,.2),0 0 0 1px rgba(0,0,0,.05);margin:10px 0 8px 0;border-radius:24px 0 0 24px;cursor:pointer;float:right}.mtpseem{margin:0 20px 0 4px;line-height:48px;font-size:13px;float:right}.mtprt img{width:40px;height:40px;margin:4px}.mtprt img{border-radius:20px}#b_mtp .mtpchv{margin:0 0 12px -28px;transform:rotate(90deg)}#b_mtp:not(.crhide) .mtprt{transform:translateX(100%);animation:mtp-in .3s cubic-bezier(0,0,.58,1) forwards}#b_mtp.mtpslidert .mtprt{transform:translateX(0%);animation:mtp-out .3s cubic-bezier(0,0,.58,1) forwards}@keyframes mtp-in{100%{transform:translateX(0%)}}@keyframes mtp-out{100%{transform:translateX(100%)}}body #b_opalpers .b_op_flyout{top:215px}.b_sydConvMode #b_context{display:none}.b_sydConvMode #b_mtp:not(.crhide){display:none}.richrswrapper{box-shadow:0 0 0 1px rgba(0,0,0,.05);padding:10px 20px 5px 20px;margin:-10px -20px 24px -20px;width:100%;border-radius:6px}.richrsrailtitle{border-bottom:1px solid #ddd;padding:5px 19px;margin:0 -20px}.richrsrailtitle h2{font-style:normal;font-weight:400;font-size:20px;line-height:24px;color:#444}.richrsrailsugwrapper>div{border-bottom:1px solid #ddd}.richrsrailsugwrapper>div:last-child{border-bottom:0}#b_content #b_context .richrsrailsuggestion a{display:flex;align-items:center;gap:12px;padding:10px 0;font-style:normal;font-weight:bold;font-size:16px;line-height:22px;color:#111}#b_content #b_context .richrsrailsuggestion .aDiv{display:flex;align-items:center;gap:12px;padding:10px 0;font-style:normal;font-weight:400;font-size:16px;line-height:22px;color:#111}.richrsrailsuggestion_img{width:24px;height:24px;text-align:center;padding:2.5px 0;box-sizing:border-box}.richrsrailsuggestion_img.sug_sprite{display:block;width:20px;height:20px;background-clip:content-box;overflow:hidden;margin:2px;padding:0;direction:ltr}.richrsrailsuggestion_img.sug_sprite:after{display:inline-block;-webkit-transform-origin:-762px -40px;transform-origin:-762px -40px;-ms-transform:scale(.5);-webkit-transform:scale(.5);transform:scale(.5)}.richrsrailsuggestion_text{width:calc(100% - 52px);text-align:initial;white-space:initial}.richrsrailsuggestion_text_ellipse{text-overflow:ellipsis;overflow:hidden;white-space:nowrap}.richrsrailsuggestion_img svg path{fill:#767676}.rs_chat .rs_chatT{display:flex;gap:6px;align-items:center;padding:16px 20px;line-height:18px;color:#444;border-bottom:1px solid #ddd;margin:0 -19px}.b_dark .rs_chat .rs_chatT{color:#faf9f8}.rs_chat .rs_chatText{flex-grow:1}#b_content #b_context .rs_chat .rs_chatText h2{padding-bottom:0}.rs_chat .rs_chatLogoH{width:30px;height:30px}.rs_chat #rs_chatLogo{margin-top:1px;width:30px;height:30px;background:url(/rp/em88jYr3ZOv7yX3AqoOU5z8EEnA.png) no-repeat;background-size:30px 30px}.rs_chat{box-shadow:0 0 0 1px rgba(0,0,0,.05);padding:0 19px 4px 19px;margin:-10px -20px 24px -20px;width:100%;border-radius:6px;border-color:transparent}.b_dark .rs_chat{background-color:#292827}.rs_chat #rs_chatReD .aDiv{display:flex;background:#174ae4;padding:5px 11px;align-items:center;gap:4px;border-radius:16px;color:#fff;cursor:pointer;white-space:nowrap;font-family:inherit;font-size:14px;border:1px solid #174ae4}.rs_chat #rs_chatReD .aDiv:hover{box-shadow:var(--cib-shadow-card-raised);text-decoration:underline;text-decoration-color:#fff}.rs_chat .rs_chatSydneyC{height:20px;width:20px}.rs_chat #rs_chatSydney{height:20px;width:20px;background:url(/rp/0Kv2EbGZU7NV-wbo0XwhxQSY_1k.svg) no-repeat;background-size:20px 20px}.richrsChatSuggestion_img{width:25px;height:25px;align-self:start}.richrsChatSuggestion_img .rs_chatSImgId{width:25px;height:25px;background:url(/rp/v6T2hhKPV0ncl46oF7sbuo4xFNE.svg) no-repeat;background-size:25px 25px}.b_dark .richrsChatSuggestion_img .rs_chatSImgId{width:25px;height:25px;background:url(/rp/LKJ0Wx0BQGVJuYsik9a4BW2Q3GU.svg) no-repeat;background-size:25px 25px}.rs_chatSC{color:#fff}#b_content #b_context .richrsrailsuggestion.rsChat_rqdiv_new .aDiv{display:flex;gap:12px;cursor:pointer;white-space:nowrap;border:none;background:none;width:100%;font-family:inherit}#b_content #b_context .richrsrailsuggestion.rsChat_rqdiv_new .aDiv:hover{text-decoration:underline}.richrsrailsuggestion_img.sug_sprite:after{content:url(/rp/Qk540QN8GIaMmT8oEUEVF6N8IF8.png)}#b_dynRail{display:inline-block;vertical-align:top;padding-left:60px;max-width:472px;width:472px}#b_dynRail .b_dr_mod:not(:last-child){margin-bottom:10px}@media only screen and (max-width:1908px){#b_dynRail{width:382px}}@media only screen and (max-width:1818.9px){#b_dynRail{width:294px}}@media only screen and (max-width:1730.9px){#b_dynRail{display:none}}#b_dynRail .b_entity{margin-bottom:20px;padding:9px 19px 4px;width:100%;box-shadow:0 0 0 1px rgba(0,0,0,.05);border-radius:6px}z{a:1}#b_results h2 #kn_p,#b_results h3 #kn_p,#b_results h4 #kn_p{position:relative;display:inline}#b_pole #kn_p,.b_ans #kn_p{display:none}#kn_a{color:#4007a2;font:14px arial;left:-20px;position:absolute;top:25%}.b_title #kn_a,.b_overflow #kn_a{left:-3px;position:relative}</style><style type="text/css">#tabcontrol_5_4E8B10 .tab-head { height: 40px; } #tabcontrol_5_4E8B10 .tab-menu { height: 40px; } #tabcontrol_5_4E8B10_menu { height: 40px; } #tabcontrol_5_4E8B10_menu>li { background-color: #ffffff; margin-right: 0px; height: 40px; line-height:40px; font-weight: 700; color: #767676; } #tabcontrol_5_4E8B10_menu>li:hover { color: #111; position:relative; } #tabcontrol_5_4E8B10_menu .tab-active { box-shadow: inset 0 -3px 0 0 #111; background-color: #ffffff; line-height: 40px; color: #111; } #tabcontrol_5_4E8B10_menu .tab-active:hover { color: #111; } #tabcontrol_5_4E8B10_navr, #tabcontrol_5_4E8B10_navl { height: 40px; width: 32px; background-color: #ffffff; } #tabcontrol_5_4E8B10_navr .sv_ch, #tabcontrol_5_4E8B10_navl .sv_ch { fill: #444; } #tabcontrol_5_4E8B10_navr:hover .sv_ch, #tabcontrol_5_4E8B10_navl:hover .sv_ch { fill: #111; } #tabcontrol_5_4E8B10_navr.tab-disable .sv_ch, #tabcontrol_5_4E8B10_navl.tab-disable .sv_ch { fill: #444; opacity:.2; }#slideexp0_4F5BD7 .slide { width: 236px; margin-right: 8px; }#slideexp0_4F5BD7c .b_slidebar .slide { border-radius: 6px; }#slideexp0_4F5BD7 .slide:last-child { margin-right: 1px; }.b_sldrp .slide{box-shadow:0 0 0 1px rgba(0,0,0,.05);-webkit-box-shadow:0 0 0 1px rgba(0,0,0,.05)}.b_cards .cico,.b_slidebar .slide .cico{border-radius:0}.b_sldrp .slide.see_more{box-shadow:0 0 0 0 rgba(0,0,0,0);-webkit-box-shadow:0 0 0 0 rgba(0,0,0,0)}.b_sldrp .slide.see_more .carousel_seemore{border:0}.b_sldrp .slide.see_more:hover{box-shadow:0 0 0 0 rgba(0,0,0,0);-webkit-box-shadow:0 0 0 0 rgba(0,0,0,0)}#slideexp0_4F5BD7c { margin: -4px; } #slideexp0_4F5BD7c .b_viewport { padding: 4px 1px 4px 1px; margin: 0 3px; }#slideexp0_4F5BD7c .b_slidebar .slide[data-mini="1"] { display:none; }</style><script type="text/javascript" nonce="hdm+zCWukVbuQiu/8nwvTExR65VnfK54cOHZ35vK2Vk=">//<![CDATA[
if (typeof DynScopesDropdownRE !== 'undefined') { DynScopesDropdownRE.init("Video", "0:0,1:1,3:2,2:3,4:4,5:5,6:6", "marketplace", false, "", false, false); } else if (typeof DynScopesDropdown !== 'undefined') { DynScopesDropdown.init("Video", "0:0,1:1,3:2,2:3,4:4,5:5,6:6", "marketplace", false, false); };var _d,sb_de;typeof _d=="undefined"&&(_d=document);sb_de=_d.documentElement;var Shared;(function(n){function t(n){for(var i=[],t=1;t<arguments.length;t++)i[t-1]=arguments[t];return n.replace(/\{([0-9]+)\}/g,function(n,t){var r=parseInt(t);return r>=0&&r<i.length?i[r]:""})}n.formatString=t})(Shared||(Shared={}));function ge_cl(n,t){var u,f,r,i;if(n){if(n.getElementsByClassName)return n.getElementsByClassName(t);for(u=n.getElementsByTagName("*"),f=[],r=0;r<u.length;r++)i=u[r],i&&i.className&&i.className.indexOf(t)!==-1&&f.push(i);return f}return[]};var SharedAnimation;(function(n){function v(){var n,t;if(!("transition"in _d.documentElement.style))for(n in o)t="-"+n+"-",t+"transition"in _d.documentElement.style&&(u=o[n])}function y(){var i,n;if(!("transform"in _d.documentElement.style))for(i in o)n="-"+i+"-transform",n in _d.documentElement.style&&(t=n)}function p(){v();y()}function s(n){if(n.AnimationKey){var t=l[n.AnimationKey];t.endTime=Date.now();_w.Log2&&Log2.LogEvent("ClientInst",{T:"AnimationTiming",startTime:t.startTime,endTime:t.endTime,AnimationName:t.animationName},null,null,null,null,null,null)}}function h(n,t){e+=1;n.AnimationKey=e;l[e]={startTime:Date.now(),endTime:-1,animationName:t}}function w(n){var f,r;n&&n.children&&n.children.length!=0&&(f=_w.innerHeight,n.style[t]="translateY("+f+"px)",i(n,"b_slideListUp"),Lib.CssClass.remove(n,"b_slideListHide"),n.style[t]="",r=n.children[n.children.length-1],r&&(sj_be(r,u,function(i){var u,f;if(i.target===r&&i.propertyName===t)for(s(n),u=0;u<n.children.length;u++)f=n.children[u],f.style[t]=""}),h(n,"cascadeListOld")))}function b(n,r){if(n&&n.children&&n.children.length!=0)n.style[t]="translateY(200px)",i(n,"b_slideListUp"),Lib.CssClass.remove(n,"b_slideListHide"),n.style[t]="",r&&i(n,"b_slideListUp")}function k(n){n&&f(n,"b_slideTranslate",!1,[t,t])}function d(n){n&&f(n,"b_slide",!0,["height","height"])}function g(n){n&&f(n,"b_fade",!1,["opacity","opacity"])}function nt(n){n&&f(n,"b_fadeUp",!1,[t,t])}function tt(n){if(n){for(var t=0;t<c.length;t++)Lib.CssClass.remove(n,c[t]);i(n,r);sj_evt.fire("transitionDone",n);h(n,"Hide");s(n)}}function it(n){var t=n.offsetTop}function i(n,t){it(n);Lib.CssClass.toggle(n,t)}function rt(n){var t=n.clientHeight;t==0&&Lib.CssClass.contains(n,r)&&(Lib.CssClass.remove(n,r),t=n.clientHeight,Lib.CssClass.add(n,r));t>0&&(n.style.height=t+"px")}function a(n,t,i,r){if(n.target==t){var f=Lib.CssClass.contains(t,"b_hide")?i[1]:i[0];n.propertyName===f&&(s(t),Lib.CssClass.remove(t,r),n.propertyName==="height"&&t.style.removeProperty("height"),sj_ue(t,u,a),sj_evt.fire("transitionDone",t))}}function f(n,t,f,e){f&&rt(n);i(n,t);i(n,r);sj_be(n,u,function(i){a(i,n,e,t)});h(n,t)}var r="b_hide",c=["b_slideListUp","b_slide","b_fade","b_fadeUp"],l={},e=0,u="transitionend",t="transform",o={o:"oTransitionEnd",moz:"transitionend",webkit:"webkitTransitionEnd"};n.cascadeListOld=w;n.cascadeList=b;n.toggleSlideTranslate=k;n.toggleSlide=d;n.toggleFade=g;n.toggleFadeUp=nt;n.toggleHide=tt;p()})(SharedAnimation||(SharedAnimation={}));var SmartEvent;(function(n){function o(n,i,r,u){u===void 0&&(u=!0);sj_be(n,i,r);t.push({el:n,evt:i,h:r,baj:u})}function s(n,i,r,u,f){r===void 0&&(r=!0);sj_evt.bind(n,i,u,f);t.push({evt:n,h:i,baj:r})}function u(){e(!1)}function f(){e(!0);sj_ue(_w,i,f);sj_evt.unbind(r,u)}function e(n){for(var i,u,f=[],r=0;r<t.length;++r)i=t[r],n||i.baj?(u=i.el,u?sj_ue(u,i.evt,i.h):sj_evt.unbind(i.evt,i.h)):f.push(i);t=f}var i="unload",r="ajax.unload",t=[];n.bind=o;n.bindc=s;sj_be(_w,i,f);sj_evt.bind(r,u)})(SmartEvent||(SmartEvent={}));function sa_cl(n,t,i){if(n&&t){var r=n.className;i?r.indexOf(t)==-1&&(r=r.concat(" ",t)):r=r.replace(t,"");n.className=r}};var EntityPreviewConfig = { featureList: "", testHooks: false };
//]]></script><script type="text/javascript" nonce="hdm+zCWukVbuQiu/8nwvTExR65VnfK54cOHZ35vK2Vk=">//<![CDATA[
var sj_appHTML=function(n,t){var u,f,e,r,i,s,h;if(t&&n){var c="innerHTML",l="script",a="appendChild",v="length",y="src",p=sj_ce,o=p("div");if(o[c]="<br>"+t,u=o.childNodes,o.removeChild(u[0]),f=o.getElementsByTagName(l),f)for(e=0;e<f[v];e++)r=p(l),i=f[e],i&&(r.type=i.type=="module"||i.type=="importmap"?i.type:"text/javascript",s=i.getAttribute(y),s?(r.setAttribute(y,s),r.setAttribute("crossorigin","anonymous")):(r.text=i[c],r.setAttribute("data-bing-script","1")),i.parentNode.replaceChild(r,i));for(h=_d.createDocumentFragment();u[v];)h[a](u[0]);n[a](h)}};function parseQueryParamsFromQuery(n,t){var u,f,e,o;t===void 0&&(t=!0);var r=[],s=n&&n.length>0?n.split("&"):[],h=s.length,i=null;for(u=0;u<h;u++)f=s[u],e=f.indexOf("="),e>0&&(i=f.substr(0,e),i.charAt(0)=="?"&&(i=i.substr(1)),i&&(i=i.toLowerCase(),r[i]=f.substr(e+1)));return t&&(o=r.first,r.first=null==o||o=="0"?1:parseInt(o)),r}function parseQueryParams(){var n="";return n=typeof Bing!="undefined"&&Bing.Url&&Bing.Location?Bing.Url.getQueryString(Bing.Location.get()):_w.location.search.substring(1),parseQueryParamsFromQuery(n)}function convertQueryParamsToUrlStr(n,t){t===void 0&&(t=null);var i=t?t:_w.location.pathname.replace(/^\/+/,"/");return i+"?"+queryParamsToString(n)}function queryParamsToString(n){for(var e,o,r,u,s,f,t=[],i=1;i<arguments.length;i++)t[i-1]=arguments[i];if(e=[],u=t.length,u==0)for(s in n)n.hasOwnProperty(s)&&(t.push(s),u++);for(f=0;f<u;f++)o=t[f],r=n[o],(r||r===0)&&e.push(o+"="+r);return e.join("&")}function getCurrentQuery(){if(!currentQuery){var n=parseQueryParams();currentQuery=n.q}return currentQuery}function extractDomainFromUrl(n,t,i){var r,u,f,e;return typeof n!="string"?null:(r=n,u=r.indexOf("://"),u>=0&&!t&&(r=r.substr(u+3),u=-1),u=u>=0?u+3:0,f=r.indexOf(":",u),f>=0&&(r=r.substr(0,f)),f=r.indexOf("/",u),f>=0&&(r=r.substr(0,f)),e=i?r.indexOf("www."):-1,e>=0&&(r=r.substr(u+4)),r)}function addCommonPersistedParams(n){var i=parseQueryParams(),t=queryParamsToString(i,"atlahostname","cdghostname","thhostname","testhooks","adlt","akamaithumb","safesearch","perf","mockimages","mobile","anid","isuserauth","uncrunched","clientid","currentdate","iss"),r=n.indexOf("?")===-1?"?":"&";return t=t.length>0?r+t:"",n+t}var currentQuery=null;var ClickBackRSCustomControl = false;;var fab_config ={"fabStyle":1,"fabSbAction":"FocusSearchBox","fabSbActionHover":"None","fabSbActionData":"None","fabTooltip":"","micFabAlwaysVisible":false,"fabClickNoAS":false,"FabAddSuggestions":false,"DisableAlsoTryWhenHasPoleRS":false};;sj_be(_w, "click", function() { _G.UIWP = true; }, 1);;
//]]></script><script type="importmap" nonce="hdm+zCWukVbuQiu/8nwvTExR65VnfK54cOHZ35vK2Vk="> { "imports": {"rms-answers-SharedStaticAssets-mdast-util-from-markdown":"https://r.bing.com/rp/4XuwXtSbiAVqhJs_4Ciwl3YcLrM.br.js","rms-answers-SharedStaticAssets-mdast-util-gfm-table":"https://r.bing.com/rp/ztanaSb3HsieCWGjv3Tl3lECB88.br.js","rms-answers-SharedStaticAssets-micromark-extension-gfm-table":"https://r.bing.com/rp/u4lHTqMyPv8KR3P-2buvasHaSoM.br.js","rms-answers-SharedStaticAssets-markdown-it":"https://r.bing.com/rp/R_rYK_qaMtV4wMhK7ShAxVvSe_s.br.js","rms-answers-SharedStaticAssets-katex":"https://r.bing.com/rp/NLI5XRtpCK_NYPks3Y5xU5ORkeQ.br.js","rms-answers-SharedStaticAssets-docx":"https://r.bing.com/rp/7O6toTbIT9CUxT9Lujfh9KGnKPg.br.js","rms-answers-SharedStaticAssets-xlsx":"https://r.bing.com/rp/QClZgEttYMm4ob59daALw3CJwco.br.js"} } </script><link id="conv-css-link" rel="prefetch" fetchpriority="low" href="https://r.bing.com/rp/aP3vCNNzRNbEVfBNTfALx3zdHYM.br.css" type="text/css" as="style" /><div id="b_sydWelcomeTemplate" class="b_ziCont" data-priority="2"><div class="b_wlcmHdr"><div class="b_wlcmLogoCont"><div class="b_wlcmLogo" role="img" aria-label="Copilot Logo" tabindex="0"></div><div class="b_wlcmName" role="heading" aria-level="2">Copilot</div></div><div class="b_wlcmDesc" role="heading" aria-level="2">Su complemento de IA para todos los d&#237;as</div></div><div class="b_ziTileCont b_wlcmCont" data-zi-iid="SERP.5651"></div></div><div id="b_sydHeadBg"></div><div id="b_sydConvCont" _iid="SERP.5028" data-priority=""><div id="b_sydtoporpole" class="b_sydConvAnsTest"></div><div id="b_sydBgCover"></div></div><span id="CodexInstLink" class="b_hide" _ct="ID=SERP,5025.1" data-exlk="SERP,5026"></span><span id="CodexInstExtLink" class="b_hide" _ct="ID=SERP,5026.1"></span><span id="CodexCarouselInstLink" class="b_hide" _ct="ID=SERP,5027.1"></span><div id="b_content" class=""><main aria-label="Resultados de la búsqueda"><div id="b_tween" class=""><div id="b_tween_searchResults"><span class="sb_count">Aproximadamente 194,000 resultados</span></div><div id="b_tween_searchTools" class="b_hide"><span class="ftrB"><a class="ftrH" id="h5154" role="button" aria-label="Filtrado por Fecha" aria-expanded="false" aria-controls="d5154" aria-haspopup="true" href="javascript:" h="ID=SERP,5452.1"><span class="fs_label">Fecha</span><span class="sw_ddbk"></span> </a></span></div></div><ol id="b_results" class=""><li class="b_algo" data-id="" ><div class="tpcn"><a class="tilk" href="http://doc.autoxjs.com/" h="ID=SERP,5198.1"><div class="tpic"><div class="wr_fav" data-priority="2"><div class="cico siteicon" style="width:32px;height:32px;"><div class="rms_iac" style="height:32px;line-height:32px;width:32px;" data-height="32" data-width="32" data-alt="Icono de web global" data-class="rms_img" data-src="https://th.bing.com/th?id=ODLS.8a6a75ef-4c98-4e94-9592-d98824c914ed&amp;w=32&amp;h=32&amp;qlt=90&amp;pcl=fffffa&amp;o=6&amp;pid=1.2"></div></div></div></div><div class="tptxt"><div class="tptt">AutoX.js</div><div class="tpmeta"><div class="b_attribution" tabindex="0"><cite>http://doc.autoxjs.com</cite></div></div></div></a></div><h2><a href="http://doc.autoxjs.com/" h="ID=SERP,5198.2">AutoX.js</a></h2><div class="b_caption hasdl" role="contentinfo"><p class="b_lineclamp2 b_algoSlug"><span class="algoSlug_icon" data-priority="2">Web</span>综述 . 由于原作者不再维护 Auto.<strong>js</strong>，我计划在原来4.1版本的基础上继续维护项目，并将原项目命名为 AutoX.js。. 欢迎更多开发者参与这个项目的维护与升级。 这篇文档里有加密 …</p></div><div class="scs_child_rpr rpr_light"><div class="pageRecoContainer " data-fbhlsel=".pageRecoContainer"><div class="recommendationsTableTitle"><h2 class="">Explorar m&#225;s</h2></div><div class="pagereco_TCntr"><div class="pagereco_TRow  "><div class="pagereco_TTitle"><a href="https://zhuanlan.zhihu.com/p/273254317" h="ID=SERP,5467.1"><div class="b_fpl_cntr"><div class="b_fpl_icon" data-priority="2"><div class="cico siteicon" style="width:16px;height:16px;"><div class="rms_iac" style="height:16px;line-height:16px;width:16px;" data-height="16" data-width="16" data-alt="Icono de web global" data-role="img" data-class="rms_img" data-src="https://th.bing.com/th?id=ODLS.7f053868-4d84-44a8-9b95-8fc671ba7f4f&amp;w=16&amp;h=16&amp;o=6&amp;pid=1.2"></div></div></div><div class="b_fpl_attr"><div class="b_title">这个神器过于强大，结果被灰产玩坏了，但今天我给 ...</div></div></div></a></div><div class="pagereco_TDomain"><a href="https://zhuanlan.zhihu.com/p/273254317" h="ID=SERP,5467.2"><div class="b_attr"><cite>zhuanlan.zhihu.com</cite></div></a></div></div><div class="pagereco_TRow  "><div class="pagereco_TTitle"><a href="https://www.52pojie.cn/thread-1792775-1-1.html" h="ID=SERP,5468.1"><div class="b_fpl_cntr"><div class="b_fpl_icon" data-priority="2"><div class="cico siteicon" style="width:16px;height:16px;"><div class="rms_iac" style="height:16px;line-height:16px;width:16px;" data-height="16" data-width="16" data-alt="Icono de web global" data-role="img" data-class="rms_img" data-src="https://th.bing.com/th?id=ODLS.99ecc366-a2a9-4897-a6d7-1125acdcfec7&amp;w=16&amp;h=16&amp;o=6&amp;pid=1.2"></div></div></div><div class="b_fpl_attr"><div class="b_title">基于自动化工具autoxjs的抢票 - 『移动安全区』 - 吾爱 ...</div></div></div></a></div><div class="pagereco_TDomain"><a href="https://www.52pojie.cn/thread-1792775-1-1.html" h="ID=SERP,5468.2"><div class="b_attr"><cite>52pojie.cn</cite></div></a></div></div><div class="pagereco_TRow  "><div class="pagereco_TTitle"><a href="https://blog.csdn.net/Cupster/article/details/115751897" h="ID=SERP,5469.1"><div class="b_fpl_cntr"><div class="b_fpl_icon" data-priority="2"><div class="cico siteicon" style="width:16px;height:16px;"><div class="rms_iac" style="height:16px;line-height:16px;width:16px;" data-height="16" data-width="16" data-alt="Icono de web global" data-role="img" data-class="rms_img" data-src="https://th.bing.com/th?id=ODLS.3dc6f5f0-f66b-4880-893d-62d29cba6f62&amp;w=16&amp;h=16&amp;o=6&amp;pid=1.2"></div></div></div><div class="b_fpl_attr"><div class="b_title"><strong>AutoJS一文精通AutoJS脚本教程详解</strong>_auto.js教程-CSDN博客</div></div></div></a></div><div class="pagereco_TDomain"><a href="https://blog.csdn.net/Cupster/article/details/115751897" h="ID=SERP,5469.2"><div class="b_attr"><cite>blog.csdn.net</cite></div></a></div></div><div class="pagereco_TRow  "><div class="pagereco_TTitle"><a href="http://www.feiyunjs.com/3098.html" h="ID=SERP,5470.1"><div class="b_fpl_cntr"><div class="b_fpl_icon" data-priority="2"><div class="cico siteicon" style="width:16px;height:16px;"><div class="rms_iac" style="height:16px;line-height:16px;width:16px;" data-height="16" data-width="16" data-alt="Icono de web global" data-role="img" data-class="rms_img" data-src="https://th.bing.com/th?id=ODLS.09ca7fcf-1c66-4b5c-92f5-eb86cb2907ba&amp;w=16&amp;h=16&amp;o=6&amp;pid=1.2"></div></div></div><div class="b_fpl_attr"><div class="b_title">autojs怎么了？还能使用吗？彻底凉凉了吗？不能 ...</div></div></div></a></div><div class="pagereco_TDomain"><a href="http://www.feiyunjs.com/3098.html" h="ID=SERP,5470.2"><div class="b_attr"><cite>feiyunjs.com</cite></div></a></div></div><div class="pagereco_TRow  "><div class="pagereco_TTitle"><a href="https://zhuanlan.zhihu.com/p/255069044" h="ID=SERP,5471.1"><div class="b_fpl_cntr"><div class="b_fpl_icon" data-priority="2"><div class="cico siteicon" style="width:16px;height:16px;"><div class="rms_iac" style="height:16px;line-height:16px;width:16px;" data-height="16" data-width="16" data-alt="Icono de web global" data-role="img" data-class="rms_img" data-src="https://th.bing.com/th?id=ODLS.7f053868-4d84-44a8-9b95-8fc671ba7f4f&amp;w=16&amp;h=16&amp;o=6&amp;pid=1.2"></div></div></div><div class="b_fpl_attr"><div class="b_title">autoxjs入门-高级开发环境搭建 - 知乎</div></div></div></a></div><div class="pagereco_TDomain"><a href="https://zhuanlan.zhihu.com/p/255069044" h="ID=SERP,5471.2"><div class="b_attr"><cite>zhuanlan.zhihu.com</cite></div></a></div></div><div class="pagereco_TRow  "><div class="pagereco_TTitle"><a href="https://blog.csdn.net/SUNbrightness/article/details/124229682" h="ID=SERP,5472.1"><div class="b_fpl_cntr"><div class="b_fpl_icon" data-priority="2"><div class="cico siteicon" style="width:16px;height:16px;"><div class="rms_iac" style="height:16px;line-height:16px;width:16px;" data-height="16" data-width="16" data-alt="Icono de web global" data-role="img" data-class="rms_img" data-src="https://th.bing.com/th?id=ODLS.3dc6f5f0-f66b-4880-893d-62d29cba6f62&amp;w=16&amp;h=16&amp;o=6&amp;pid=1.2"></div></div></div><div class="b_fpl_attr"><div class="b_title">autox.js(autojs替代方案) 开发教程-CSDN博客</div></div></div></a></div><div class="pagereco_TDomain"><a href="https://blog.csdn.net/SUNbrightness/article/details/124229682" h="ID=SERP,5472.2"><div class="b_attr"><cite>blog.csdn.net</cite></div></a></div></div></div><div class="recommendationsTableFeedback"><div class="fbans"><div class="b_footnote"><a id="fdbkans_0" class="hlig" target="_blank" data-fbhlsel=".pageRecoContainer" role="button" href="javascript:void(0)" h="ID=SERP,5653.1">Recomendado para ti en funci&#243;n de lo que es popular • Comentarios</a></div></div></div></div></div><div elementtiming=frp.MiddleOfPage style="pointer-events:none;margin-top:-1px;height:1px;width:1px;font-size:1px;position:absolute;top:0;right:0;" tabindex="-1"> &nbsp; </div></li><li class="b_algo" data-id="" ><div class="tpcn"><a class="tilk" href="https://github.com/openautojs/openautojs" h="ID=SERP,5225.1"><div class="tpic"><div class="wr_fav" data-priority="2"><div class="cico siteicon" style="width:32px;height:32px;"><div class="rms_iac" style="height:32px;line-height:32px;width:32px;" data-height="32" data-width="32" data-alt="Icono de web global" data-class="rms_img" data-src="https://th.bing.com/th?id=ODLS.05409d17-5d83-4701-acc1-90430dd3b02c&amp;w=32&amp;h=32&amp;qlt=91&amp;pcl=fffffa&amp;o=6&amp;pid=1.2"></div></div></div></div><div class="tptxt"><div class="tptt">Github</div><div class="tpmeta"><div class="b_attribution" u="1|5101|5053823959433436|fEYQgYuX-iD6NorFoiJ7xLKP3bk5Vxc6" tabindex="0"><cite>https://github.com/openautojs/openautojs</cite><span class="c_tlbxTrg"><span class="c_tlbxH" H="BASE:CACHEDPAGEDEFAULT" K="SERP,5226.1"></span></span></div></div></div></a></div><h2><a href="https://github.com/openautojs/openautojs" h="ID=SERP,5225.2">GitHub - openautojs/openautojs</a></h2><div class="b_caption" role="contentinfo"><div class="b_richcard"><div class="rc_herotabheader"><div id="tabcontrol_5_4E8B10" class="tab-container" data-wire="I;tab;; |B;a;;I|" data-appns="SERP" data-k="5492.2"><div class="tab-head HeroTab"><div class="tab-menu tab-flex"><ul id="tabcontrol_5_4E8B10_menu" style="margin-left:0px;" role="tablist" data-dataurl="/richcardFetch?q=autojs&amp;Treatment=RCNewTemplate&amp;DataSourceType=OS&amp;Tabs={0}&amp;DataSource=WrapStarDU&amp;DocURLHash=FA368AC5A2227BC4B28FDDB93957173A&amp;DocURL=https%3a%2f%2fgithub.com%2fopenautojs%2fopenautojs&amp;TierName=webfluid"><li data-dataurl="#0%2cOverview"
                            id="tab_1_head" data-nc="" data-nr=""
                            class="b_dmtab tab-first"
                            style=" flex:0.18456 1 0px;" data-w="66" data-ow="98" data-content="tab_1" role="tab"
                            aria-controls="tab_1"
                            aria-selected="false" data-appns="SERP" data-k="5488.1">Overview</li><li data-dataurl=""
                            id="tab_2_head" data-nc="" data-nr=""
                            class="b_dmtab tab-active"
                            style=" flex:0.29379 1 0px;" data-w="124" data-ow="156" data-content="tab_2"
                            tabindex="0" role="tab"
                            aria-controls="tab_2"
                            aria-selected="true" data-appns="SERP" data-k="5489.1">Brief Introduction</li><li data-dataurl="#2%2cLicense"
                            id="tab_3_head" data-nc="" data-nr=""
                            class="b_dmtab"
                            style=" flex:0.16573 1 0px;" data-w="56" data-ow="88" data-content="tab_3" role="tab"
                            aria-controls="tab_3"
                            aria-selected="false" data-appns="SERP" data-k="5490.1">License</li><li data-dataurl="#3%2cAbout+Development%ef%bc%9a"
                            id="tab_4_head" data-nc="" data-nr=""
                            class="b_dmtab"
                            style=" flex:0.35593 1 0px;" data-w="157" data-ow="189" data-content="tab_4" role="tab"
                            aria-controls="tab_4"
                            aria-selected="false" data-appns="SERP" data-k="5491.1">About Development：</li></ul></div></div><div class="tab-content"><div id="tab_1" class="tab-hide" data-appns="SERP" data-k="5488.2" role="tabpanel" aria-labelledby="tab_1_head" data-priority="2"></div><div id="tab_2" data-appns="SERP" data-k="5489.2" role="tabpanel" aria-labelledby="tab_2_head" data-priority=""><ul class="b_vList b_divsec"><li data-priority=><span title="A JavaScript runtime and development environment on the Android platform that supports accessibility services, with development goals similar to JsBox and Workflow.">A JavaScript runtime and development environment on the Android platform that supports acce…</span><br/>This project is based on Autox.js to establish the openautojs community, this project aims to allow users to enjoy free and open source Auto .js</li></ul><a href="https://github.com/openautojs/openautojs" h="ID=SERP,5487.1">Ver m&#225;s en github.com</a></div><div id="tab_3" class="tab-hide" data-appns="SERP" data-k="5490.2" role="tabpanel" aria-labelledby="tab_3_head" data-priority="2"></div><div id="tab_4" class="tab-hide" data-appns="SERP" data-k="5491.2" role="tabpanel" aria-labelledby="tab_4_head" data-priority="2"></div></div></div></div></div></div><div class="scs_child_rpr rpr_light"><div class="pageRecoContainer " data-fbhlsel=".pageRecoContainer"><div class="recommendationsTableTitle"><h2 class="">Explorar m&#225;s</h2></div><div class="pagereco_TCntr"><div class="pagereco_TRow  "><div class="pagereco_TTitle"><a href="https://github.com/enjoyDay/my-autojs4" h="ID=SERP,5499.1"><div class="b_fpl_cntr"><div class="b_fpl_icon" data-priority="2"><div class="cico siteicon" style="width:16px;height:16px;"><div class="rms_iac" style="height:16px;line-height:16px;width:16px;" data-height="16" data-width="16" data-alt="Icono de web global" data-role="img" data-class="rms_img" data-src="https://th.bing.com/th?id=ODLS.05409d17-5d83-4701-acc1-90430dd3b02c&amp;w=16&amp;h=16&amp;o=6&amp;pid=1.2"></div></div></div><div class="b_fpl_attr"><div class="b_title">GitHub - enjoyDay/my-autojs4: autojs最后一个免费版</div></div></div></a></div><div class="pagereco_TDomain"><a href="https://github.com/enjoyDay/my-autojs4" h="ID=SERP,5499.2"><div class="b_attr"><cite>github.com</cite></div></a></div></div><div class="pagereco_TRow  "><div class="pagereco_TTitle"><a href="https://github.com/wiatingpub/autojs" h="ID=SERP,5500.1"><div class="b_fpl_cntr"><div class="b_fpl_icon" data-priority="2"><div class="cico siteicon" style="width:16px;height:16px;"><div class="rms_iac" style="height:16px;line-height:16px;width:16px;" data-height="16" data-width="16" data-alt="Icono de web global" data-role="img" data-class="rms_img" data-src="https://th.bing.com/th?id=ODLS.05409d17-5d83-4701-acc1-90430dd3b02c&amp;w=16&amp;h=16&amp;o=6&amp;pid=1.2"></div></div></div><div class="b_fpl_attr"><div class="b_title"><strong>GitHub</strong> - wiatingpub/autojs: 自动化脚本学习教程和相关demo</div></div></div></a></div><div class="pagereco_TDomain"><a href="https://github.com/wiatingpub/autojs" h="ID=SERP,5500.2"><div class="b_attr"><cite>github.com</cite></div></a></div></div></div><div class="recommendationsTableFeedback"><div class="fbans"><div class="b_footnote"><a id="fdbkans_1" class="hlig" target="_blank" data-fbhlsel=".pageRecoContainer" role="button" href="javascript:void(0)" h="ID=SERP,5656.1">Recomendado para ti en funci&#243;n de lo que es popular • Comentarios</a></div></div></div></div></div></li><li class="b_algo" data-id="" ><div class="tpcn"><a class="tilk" href="https://github.com/topics/autojs" h="ID=SERP,5253.1"><div class="tpic"><div class="wr_fav" data-priority="2"><div class="cico siteicon" style="width:32px;height:32px;"><div class="rms_iac" style="height:32px;line-height:32px;width:32px;" data-height="32" data-width="32" data-alt="Icono de web global" data-class="rms_img" data-src="https://th.bing.com/th?id=ODLS.05409d17-5d83-4701-acc1-90430dd3b02c&amp;w=32&amp;h=32&amp;qlt=92&amp;pcl=fffffa&amp;o=6&amp;pid=1.2"></div></div></div></div><div class="tptxt"><div class="tptt">Github</div><div class="tpmeta"><div class="b_attribution" u="2|5102|4994922782411022|09oQa7QYU-7fbbp9WTdmLGUiiqxZF52S" tabindex="0"><cite>https://github.com/topics/autojs</cite><span class="c_tlbxTrg"><span class="c_tlbxH" H="BASE:CACHEDPAGEDEFAULT" K="SERP,5254.1"></span></span></div></div></div></a></div><h2><a href="https://github.com/topics/autojs" h="ID=SERP,5253.2">autojs &#183; GitHub Topics &#183; GitHub</a></h2><div class="b_caption" role="contentinfo"><p class="b_lineclamp2 b_algoSlug"><span class="algoSlug_icon" data-priority="2">Web</span><span class="news_dt">2 de dic. de 2023</span>&nbsp;&#0183;&#32;Add a description, image, and links to the <strong>autojs</strong> topic page so that developers can more easily learn about it. Curate this topic</p></div><div class="pageRecoPlaceholder" data-tag="RelatedPageRecommendations.RecommendationsClickback"></div></li><li class="b_ans b_mop b_vidAns" data-tag data-partnerTag data-id="" h="SERP,5513.1"><div class="vsathm "><div id="serpvidans" class="vsa vab vsarf"><div class="btitle b_title"><h2 class=" vsatitle mmtitle"><a target="" href="/videos/search?q=autojs&amp;qpvt=autojs&amp;FORM=VDRE" h="ID=SERP,5149.1">V&#237;deos de autojs</a></h2></div><div class="mattr"> <div class="b_attribution" data-tag="" data-partnerTag=""><cite><cite>bing.com/videos</cite></cite></div></div><div id="mmcar"><div ihpa="Vista previa" ihp="Sin vista previa"><div id="slideexp0_4F5BD7c" class="b_canvas b_slideexp" data-wire="I;slideexp_init;; |B;select;;I|F;selected;;O|F;active;;O|" data-control-id="slideexp0_4F5BD7" data-appns="SERP" data-k="5549.1" data-stk=""><div class="b_overlay"><div id="slideexp0_4F5BD7chevrons_prevbtn" class="btn  disabled prev ltr rounded bld" data-dir="" role="button"
     aria-label="Haga clic para desplazarse a la izquierda"
     aria-hidden="true"><div class="bg"><span></span></div><div class="vcac"><div style="height: 32px; margin-top: -16px;"><div class="cr"><div></div></div></div></div></div><div id="slideexp0_4F5BD7chevrons_nextbtn" class="btn  disabled next ltr rounded bld" data-dir="" role="button"
     aria-label="Haga clic para desplazarse a la derecha"
     aria-hidden="true"><div class="bg"><span></span></div><div class="vcac"><div style="height: 32px; margin-top: -16px;"><div class="cr"><div></div></div></div></div></div><div class="b_slidesContainer"><div class="b_viewport"><div class="b_slidebar b_sldrp" id="slideexp0_4F5BD7" role="list" aria-label="Usa las teclas de flecha para navegar"><div class="slide" data-dataurl="" data-rinterval="" data-appns="SERP" data-k="5544.1" data-mini=""
             role="listitem"><div id="mc_vtvc__11" class="mc_vtvc b_canvas mc_vtvc_cc  creator " data-priority="1"><a aria-label="1【Autojs概述】#Autojs是什么，能做什么? 本课程主要针对零基础学员，什么学历，你只要会 #电脑 的基本打字，就完全能够学会，因为写脚本真的非常简单。让你开发脚本变的更加轻松。 de YouTube · Duración:  13 minutos 49 segundos  · 1.9K visualizaciones · cargado el 23 de jun. de 2021 · cargado por 立体空间，科技为本！ · Haga clic para reproducir." data-dc="vtdc_black" class="mc_vtvc_link" href="/videos/riverview/relatedvideo?q=autojs&amp;mid=B8A12F5548A7F73D40D5B8A12F5548A7F73D40D5&amp;FORM=VIRE" h="ID=SERP,5514.1"><div class="mc_vtvc_con_rc"><div class="mc_vtvc_th b_canvas"><div class="cico"><img height="132" width="236" data-src-hq="https://th.bing.com/th?id=OVP.TIkJjT_scHV-Kh-EbBsn3gEsDh&amp;w=236&amp;h=132&amp;c=7&amp;rs=1&amp;qlt=90&amp;o=6&amp;dpr=1.5&amp;pid=1.7" alt="1【Autojs概述】#Autojs是什么，能做什么? 本课程主要针对零基础学员，什么学历，你只要会 #电脑 的基本打字，就完全能够学会，因为写脚本真的非常简单。让你开发脚本变的更加轻松。" data-priority="2" id="emb6A4EE497" class="rms_img" src="data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAEALAAAAAABAAEAAAIBTAA7" /></div><div class="mc_vtvc_htc"><div class="mc_vtvc_htb"><div class="mc_vtvc_ht">Ver v&#237;deo</div></div></div><div class="mc_vtvc_center_play"></div><div class="mc_vtvc_ban_lo"><div class="vtbc"><div class="mc_bc_w b_smText"><div class="mc_bc items">13:49</div></div></div></div></div><div class="mc_vtvc_meta_w"><div class="mc_vtvc_meta_bg_w"><div class="mc_vtvc_cb_w"><div class="mc_vtvc_cb" style="background: rgb(83, 89, 128);"></div><div class="mc_vtvc_cb" style="background: rgb(105, 117, 128);"></div><div class="mc_vtvc_cb" style="background: rgb(174, 110, 120);"></div><div class="mc_vtvc_cb" style="background: rgb(101, 109, 148);"></div><div class="mc_vtvc_cb" style="background: rgb(134, 151, 177);"></div><div class="mc_vtvc_cb" style="background: rgb(104, 135, 164);"></div><div class="mc_vtvc_cb" style="background: rgb(161, 166, 193);"></div><div class="mc_vtvc_cb" style="background: rgb(84, 106, 123);"></div></div></div><div class="mc_vtvc_meta"><div class="mc_vtvc_title b_promtxt" title="1【Autojs概述】#Autojs是什么，能做什么? 本课程主要针对零基础学员，什么学历，你只要会 #电脑 的基本打字，就完全能够学会，因为写脚本真的非常简单。让你开发脚本变的更加轻松。"><span title="1【Autojs概述】#Autojs是什么，能做什么? 本课程主要针对零基础学员，什么学历，你只要会 #电脑 的基本打字，就完全能够学会，因为写脚本真的非常简单。让你开发脚本变的更加轻松。">1【Autojs概述】#Autojs是什么，能做什么? 本课程主要针对 …</span></div><div class="mc_vtvc_meta_block_area"><div class="mc_vtvc_meta_block"><div class="mc_vtvc_meta_row mc_vtvc_meta_pubdate"><span class="meta_vc_content">1.9K visualizaciones</span><span class="meta_pd_content">23 de jun. de 2021</span></div><div class="mc_vtvc_meta_row mc_vtvc_meta_channel"><span>YouTube</span><span class="mc_vtvc_meta_row_channel">立体空间，科技为本！</span></div></div></div></div></div><div class="vrhdata" ht="0" vrhm="{&quot;cid&quot;:&quot;serpvidans_hc&quot;,&quot;smturl&quot;:&quot;https://th.bing.com/th?id=OM2.1UA996dIVS-huA_1701306971&amp;pid=1.7&quot;,&quot;bci&quot;:0,&quot;du&quot;:&quot;13:49&quot;,&quot;murl&quot;:&quot;https://www.youtube.com/watch?v=nuV-HvMLHQg&quot;,&quot;thid&quot;:&quot;OVP.TIkJjT_scHV-Kh-EbBsn3gEsDh&quot;,&quot;mid&quot;:&quot;B8A12F5548A7F73D40D5B8A12F5548A7F73D40D5&quot;,&quot;vt&quot;:&quot;1【Autojs概述】#Autojs是什么，能做什么? 本课程主要针对零基础学员，什么学历，你只要会 #电脑 的基本打字，就完全能够学会，因为写脚本真的非常简单。让你开发脚本变的更加轻松。&quot;,&quot;IsAdultThumb&quot;:false,&quot;EnableLoopPlay&quot;:false,&quot;pgurl&quot;:&quot;https://www.youtube.com/watch?v=nuV-HvMLHQg&quot;,&quot;q&quot;:&quot;autojs&quot;,&quot;capt&quot;:{&quot;de&quot;:&quot;此频道主要是一些电脑和手机自动化程序的常用技术和编程语言教学，“自动化程序”指的是通过电脑编程来代替人类手工操作的一类程序或软件。这类程序具有智能性高、应用范围广的优点，本频道会用的编程语言 有 AUTOJS 手机脚本软件 ...&quot;,&quot;s&quot;:&quot;YouTube&quot;,&quot;vc&quot;:&quot;1.9K visualizaciones&quot;,&quot;pud&quot;:&quot;23/06/2021&quot;,&quot;pu&quot;:&quot;立体空间，科技为本！&quot;,&quot;sk&quot;:&quot;rms:answers:Multimedia:MMvsi-youtube_com&quot;}}"></div></div></a></div></div><div class="slide" data-dataurl="" data-rinterval="" data-appns="SERP" data-k="5545.1" data-mini=""
             role="listitem"><div id="mc_vtvc__13" class="mc_vtvc b_canvas mc_vtvc_cc  creator " data-priority="1"><a aria-label="autojs实战脚本教程：autojs制作超级蒜粒脚本操作全教程（1） de bilibili · Duración:  13 minutos 12 segundos  · 1.2K visualizaciones · cargado el 5 de mar. de 2022 · cargado por 猫腻不是我 · Haga clic para reproducir." data-dc="vtdc_black" class="mc_vtvc_link" href="/videos/riverview/relatedvideo?q=autojs&amp;mid=417E6E7F5574493ED551417E6E7F5574493ED551&amp;FORM=VIRE" h="ID=SERP,5515.1"><div class="mc_vtvc_con_rc"><div class="mc_vtvc_th b_canvas"><div class="cico"><img height="132" width="236" data-src-hq="https://th.bing.com/th?id=OVP.fMeiLvh5DuORGV5gh6-i8QIIFK&amp;w=236&amp;h=132&amp;c=7&amp;rs=1&amp;qlt=90&amp;o=6&amp;dpr=1.5&amp;pid=1.7" alt="autojs实战脚本教程：autojs制作超级蒜粒脚本操作全教程（1）" data-priority="2" id="emb125C04D76" class="rms_img" src="data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAEALAAAAAABAAEAAAIBTAA7" /></div><div class="mc_vtvc_htc"><div class="mc_vtvc_htb"><div class="mc_vtvc_ht">Ver v&#237;deo</div></div></div><div class="mc_vtvc_center_play"></div><div class="mc_vtvc_ban_lo"><div class="vtbc"><div class="mc_bc_w b_smText"><div class="mc_bc items">13:12</div></div></div></div></div><div class="mc_vtvc_meta_w"><div class="mc_vtvc_meta_bg_w"><div class="mc_vtvc_cb_w"><div class="mc_vtvc_cb" style="background: rgb(83, 89, 128);"></div><div class="mc_vtvc_cb" style="background: rgb(128, 128, 77);"></div><div class="mc_vtvc_cb" style="background: rgb(128, 57, 57);"></div><div class="mc_vtvc_cb" style="background: rgb(101, 109, 148);"></div><div class="mc_vtvc_cb" style="background: rgb(134, 151, 177);"></div><div class="mc_vtvc_cb" style="background: rgb(104, 135, 164);"></div><div class="mc_vtvc_cb" style="background: rgb(161, 166, 193);"></div><div class="mc_vtvc_cb" style="background: rgb(84, 106, 123);"></div></div></div><div class="mc_vtvc_meta"><div class="mc_vtvc_title b_promtxt" title="autojs实战脚本教程：autojs制作超级蒜粒脚本操作全教程（1）">autojs实战脚本教程：autojs制作超级蒜粒脚本操作全教程（1）</div><div class="mc_vtvc_meta_block_area"><div class="mc_vtvc_meta_block"><div class="mc_vtvc_meta_row mc_vtvc_meta_pubdate"><span class="meta_vc_content">1.2K visualizaciones</span><span class="meta_pd_content">5 de mar. de 2022</span></div><div class="mc_vtvc_meta_row mc_vtvc_meta_channel"><span>bilibili</span><span class="mc_vtvc_meta_row_channel">猫腻不是我</span></div></div></div></div></div><div class="vrhdata" ht="0" vrhm="{&quot;cid&quot;:&quot;serpvidans_hc&quot;,&quot;bci&quot;:0,&quot;du&quot;:&quot;13:12&quot;,&quot;murl&quot;:&quot;https://www.bilibili.com/video/BV1qR4y1G7WG/?uid=425631715234793147375747&quot;,&quot;thid&quot;:&quot;OVP.fMeiLvh5DuORGV5gh6-i8QIIFK&quot;,&quot;mid&quot;:&quot;417E6E7F5574493ED551417E6E7F5574493ED551&quot;,&quot;vt&quot;:&quot;autojs实战脚本教程：autojs制作超级蒜粒脚本操作全教程（1）&quot;,&quot;IsAdultThumb&quot;:false,&quot;EnableLoopPlay&quot;:false,&quot;pgurl&quot;:&quot;https://www.bilibili.com/video/BV1qR4y1G7WG/?uid=425631715234793147375747&quot;,&quot;q&quot;:&quot;autojs&quot;,&quot;capt&quot;:{&quot;de&quot;:&quot;autojs实战脚本教程：autojs制作超级蒜粒脚本操作全教程（1）, 视频播放量 1242、弹幕量 2、点赞数 8、投硬币枚数 4、收藏人数 8、转发人数 5, 视频作者 猫腻不是我, 作者简介 autojs教学、定制脚本助手，相关视频：autojs实战脚本教程：autojs ...&quot;,&quot;s&quot;:&quot;bilibili&quot;,&quot;vc&quot;:&quot;1.2K visualizaciones&quot;,&quot;pud&quot;:&quot;05/03/2022&quot;,&quot;pu&quot;:&quot;猫腻不是我&quot;}}"></div></div></a></div></div><div class="slide" data-dataurl="" data-rinterval="" data-appns="SERP" data-k="5546.1" data-mini=""
             role="listitem"><div id="mc_vtvc__15" class="mc_vtvc b_canvas mc_vtvc_cc  creator " data-priority="1"><a aria-label="autojs实战教程：用autojs采集抖音用户粉丝、获赞等信息，一目了然 de bilibili · Duración:  15 minutos 53 segundos  · 2.8K visualizaciones · cargado el 2 de abr. de 2022 · cargado por 猫腻不是我 · Haga clic para reproducir." data-dc="vtdc_yellow" class="mc_vtvc_link" href="/videos/riverview/relatedvideo?q=autojs&amp;mid=118191D0AFFCE41CE0BB118191D0AFFCE41CE0BB&amp;FORM=VIRE" h="ID=SERP,5516.1"><div class="mc_vtvc_con_rc"><div class="mc_vtvc_th b_canvas"><div class="cico"><img height="132" width="236" data-src-hq="https://th.bing.com/th?id=OVP.61Lmc2qrIVb5wywU0swR9QIIFK&amp;w=236&amp;h=132&amp;c=7&amp;rs=1&amp;qlt=90&amp;o=6&amp;dpr=1.5&amp;pid=1.7" alt="autojs实战教程：用autojs采集抖音用户粉丝、获赞等信息，一目了然" data-priority="2" id="emb23D373959" class="rms_img" src="data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAEALAAAAAABAAEAAAIBTAA7" /></div><div class="mc_vtvc_htc"><div class="mc_vtvc_htb"><div class="mc_vtvc_ht">Ver v&#237;deo</div></div></div><div class="mc_vtvc_center_play"></div><div class="mc_vtvc_ban_lo"><div class="vtbc"><div class="mc_bc_w b_smText"><div class="mc_bc items">15:53</div></div></div></div></div><div class="mc_vtvc_meta_w"><div class="mc_vtvc_meta_bg_w"><div class="mc_vtvc_cb_w"><div class="mc_vtvc_cb" style="background: rgb(128, 128, 77);"></div><div class="mc_vtvc_cb" style="background: rgb(83, 89, 128);"></div><div class="mc_vtvc_cb" style="background: rgb(174, 110, 120);"></div><div class="mc_vtvc_cb" style="background: rgb(122, 139, 71);"></div><div class="mc_vtvc_cb" style="background: rgb(170, 191, 124);"></div><div class="mc_vtvc_cb" style="background: rgb(152, 138, 89);"></div><div class="mc_vtvc_cb" style="background: rgb(217, 217, 162);"></div><div class="mc_vtvc_cb" style="background: rgb(116, 116, 36);"></div></div></div><div class="mc_vtvc_meta"><div class="mc_vtvc_title b_promtxt" title="autojs实战教程：用autojs采集抖音用户粉丝、获赞等信息，一目了然"><span title="autojs实战教程：用autojs采集抖音用户粉丝、获赞等信息，一目了然">autojs实战教程：用autojs采集抖音用户粉丝、获赞等信息， …</span></div><div class="mc_vtvc_meta_block_area"><div class="mc_vtvc_meta_block"><div class="mc_vtvc_meta_row mc_vtvc_meta_pubdate"><span class="meta_vc_content">2.8K visualizaciones</span><span class="meta_pd_content">2 de abr. de 2022</span></div><div class="mc_vtvc_meta_row mc_vtvc_meta_channel"><span>bilibili</span><span class="mc_vtvc_meta_row_channel">猫腻不是我</span></div></div></div></div></div><div class="vrhdata" ht="0" vrhm="{&quot;cid&quot;:&quot;serpvidans_hc&quot;,&quot;bci&quot;:0,&quot;du&quot;:&quot;15:53&quot;,&quot;murl&quot;:&quot;https://www.bilibili.com/video/BV1Wq4y1e7i7/?uid=425631577134793165376937&quot;,&quot;thid&quot;:&quot;OVP.61Lmc2qrIVb5wywU0swR9QIIFK&quot;,&quot;mid&quot;:&quot;118191D0AFFCE41CE0BB118191D0AFFCE41CE0BB&quot;,&quot;vt&quot;:&quot;autojs实战教程：用autojs采集抖音用户粉丝、获赞等信息，一目了然&quot;,&quot;IsAdultThumb&quot;:false,&quot;EnableLoopPlay&quot;:false,&quot;pgurl&quot;:&quot;https://www.bilibili.com/video/BV1Wq4y1e7i7/?uid=425631577134793165376937&quot;,&quot;q&quot;:&quot;autojs&quot;,&quot;capt&quot;:{&quot;de&quot;:&quot;autojs实战教程：用autojs采集抖音用户粉丝、获赞等信息，一目了然一个函数，解决所有！, 视频播放量 2845、弹幕量 0、点赞数 17、投硬币枚数 5、收藏人数 51、转发人数 7, 视频作者 猫腻不是我, 作者简介 autojs教学、定制脚本助手，相关视频：autojs实战教程：轻抖 ...&quot;,&quot;s&quot;:&quot;bilibili&quot;,&quot;vc&quot;:&quot;2.8K visualizaciones&quot;,&quot;pud&quot;:&quot;02/04/2022&quot;,&quot;pu&quot;:&quot;猫腻不是我&quot;}}"></div></div></a></div></div><div class="slide" data-dataurl="" data-rinterval="" data-appns="SERP" data-k="5547.1" data-mini=""
             role="listitem"><div id="mc_vtvc__17" class="mc_vtvc b_canvas mc_vtvc_cc  creator " data-priority="2"><a aria-label="autojs实战教程：轻抖、轻草app做任务赚钱用autojs助手autojs视频教程 de bilibili · Duración:  42 minutos 15 segundos  · 6.6K visualizaciones · cargado el 15 de dic. de 2021 · cargado por 猫腻不是我 · Haga clic para reproducir." data-dc="vtdc_black" class="mc_vtvc_link" href="/videos/riverview/relatedvideo?q=autojs&amp;mid=EA82B56BC98A11A53C41EA82B56BC98A11A53C41&amp;FORM=VIRE" h="ID=SERP,5517.1"><div class="mc_vtvc_con_rc"><div class="mc_vtvc_th b_canvas"><div class="cico"><div class="rms_iac" style="height:132px;line-height:132px;width:236px;" data-height="132" data-width="236" data-alt="autojs实战教程：轻抖、轻草app做任务赚钱用autojs助手autojs视频教程" data-dpr="1.5" data-class="rms_img" data-src="https://th.bing.com/th?id=OVP.r5F_BYElLjm19WeDEz9tUgEsC7&amp;w=236&amp;h=132&amp;c=7&amp;rs=1&amp;qlt=90&amp;o=6&amp;dpr=1.5&amp;pid=1.7"></div></div><div class="mc_vtvc_htc"><div class="mc_vtvc_htb"><div class="mc_vtvc_ht">Ver v&#237;deo</div></div></div><div class="mc_vtvc_center_play"></div><div class="mc_vtvc_ban_lo"><div class="vtbc"><div class="mc_bc_w b_smText"><div class="mc_bc items">42:15</div></div></div></div></div><div class="mc_vtvc_meta_w"><div class="mc_vtvc_meta_bg_w"><div class="mc_vtvc_cb_w"><div class="mc_vtvc_cb" style="background: rgb(83, 89, 128);"></div><div class="mc_vtvc_cb" style="background: rgb(77, 88, 153);"></div><div class="mc_vtvc_cb" style="background: rgb(105, 117, 128);"></div><div class="mc_vtvc_cb" style="background: rgb(101, 109, 148);"></div><div class="mc_vtvc_cb" style="background: rgb(134, 151, 177);"></div><div class="mc_vtvc_cb" style="background: rgb(104, 135, 164);"></div><div class="mc_vtvc_cb" style="background: rgb(161, 166, 193);"></div><div class="mc_vtvc_cb" style="background: rgb(84, 106, 123);"></div></div></div><div class="mc_vtvc_meta"><div class="mc_vtvc_title b_promtxt" title="autojs实战教程：轻抖、轻草app做任务赚钱用autojs助手autojs视频教程"><span title="autojs实战教程：轻抖、轻草app做任务赚钱用autojs助手autojs视频教程">autojs实战教程：轻抖、轻草app做任务赚钱用autojs助 …</span></div><div class="mc_vtvc_meta_block_area"><div class="mc_vtvc_meta_block"><div class="mc_vtvc_meta_row mc_vtvc_meta_pubdate"><span class="meta_vc_content">6.6K visualizaciones</span><span class="meta_pd_content">15 de dic. de 2021</span></div><div class="mc_vtvc_meta_row mc_vtvc_meta_channel"><span>bilibili</span><span class="mc_vtvc_meta_row_channel">猫腻不是我</span></div></div></div></div></div><div class="vrhdata" ht="0" vrhm="{&quot;cid&quot;:&quot;serpvidans_hc&quot;,&quot;bci&quot;:0,&quot;du&quot;:&quot;42:15&quot;,&quot;murl&quot;:&quot;https://www.bilibili.com/video/BV1PQ4y1v7gX/?uid=425631505134793176376758&quot;,&quot;thid&quot;:&quot;OVP.r5F_BYElLjm19WeDEz9tUgEsC7&quot;,&quot;mid&quot;:&quot;EA82B56BC98A11A53C41EA82B56BC98A11A53C41&quot;,&quot;vt&quot;:&quot;autojs实战教程：轻抖、轻草app做任务赚钱用autojs助手autojs视频教程&quot;,&quot;IsAdultThumb&quot;:false,&quot;EnableLoopPlay&quot;:false,&quot;pgurl&quot;:&quot;https://www.bilibili.com/video/BV1PQ4y1v7gX/?uid=425631505134793176376758&quot;,&quot;q&quot;:&quot;autojs&quot;,&quot;capt&quot;:{&quot;de&quot;:&quot;autojs实战教程：轻抖、轻草app做任务赚钱用autojs助手autojs视频教程, 视频播放量 6583、弹幕量 3、点赞数 68、投硬币枚数 36、收藏人数 193、转发人数 24, 视频作者 猫腻不是我, 作者简介 autojs教学、定制脚本助手，相关视频：autojs实战教程 ...&quot;,&quot;s&quot;:&quot;bilibili&quot;,&quot;vc&quot;:&quot;6.6K visualizaciones&quot;,&quot;pud&quot;:&quot;15/12/2021&quot;,&quot;pu&quot;:&quot;猫腻不是我&quot;}}"></div></div></a></div></div><div class="slide" data-dataurl="" data-rinterval="" data-appns="SERP" data-k="5548.1" data-mini=""
             role="listitem"><div id="mc_vtvc__19" class="mc_vtvc b_canvas mc_vtvc_cc  " data-priority="2"><a aria-label="autojs入门 自动刷抖音极速版领取金币 de YouTube · Duración:  18 minutos 15 segundos  · 3K visualizaciones · cargado el 28 de jun. de 2020 · cargado por zhang好玩实验室 · Haga clic para reproducir." data-dc="vtdc_black" class="mc_vtvc_link" href="/videos/riverview/relatedvideo?q=autojs&amp;mid=38E7DF2361EE22B11E5038E7DF2361EE22B11E50&amp;FORM=VIRE" h="ID=SERP,5518.1"><div class="mc_vtvc_con_rc"><div class="mc_vtvc_th b_canvas"><div class="cico"><div class="rms_iac" style="height:132px;line-height:132px;width:236px;" data-height="132" data-width="236" data-alt="autojs入门 自动刷抖音极速版领取金币" data-dpr="1.5" data-class="rms_img" data-src="https://th.bing.com/th?id=OVP.4zFass7OoAqCkMdyoHC9MQEsDh&amp;w=236&amp;h=132&amp;c=7&amp;rs=1&amp;qlt=90&amp;o=6&amp;dpr=1.5&amp;pid=1.7"></div></div><div class="mc_vtvc_htc"><div class="mc_vtvc_htb"><div class="mc_vtvc_ht">Ver v&#237;deo</div></div></div><div class="mc_vtvc_center_play"></div><div class="mc_vtvc_ban_lo"><div class="vtbc"><div class="mc_bc_w b_smText"><div class="mc_bc items">18:15</div></div></div></div></div><div class="mc_vtvc_meta_w"><div class="mc_vtvc_meta_bg_w"><div class="mc_vtvc_cb_w"><div class="mc_vtvc_cb" style="background: rgb(83, 89, 128);"></div><div class="mc_vtvc_cb" style="background: rgb(174, 110, 120);"></div><div class="mc_vtvc_cb" style="background: rgb(77, 153, 112);"></div><div class="mc_vtvc_cb" style="background: rgb(101, 109, 148);"></div><div class="mc_vtvc_cb" style="background: rgb(134, 151, 177);"></div><div class="mc_vtvc_cb" style="background: rgb(104, 135, 164);"></div><div class="mc_vtvc_cb" style="background: rgb(161, 166, 193);"></div><div class="mc_vtvc_cb" style="background: rgb(84, 106, 123);"></div></div></div><div class="mc_vtvc_meta"><div class="mc_vtvc_title b_promtxt" title="autojs入门 自动刷抖音极速版领取金币">autojs入门 自动刷抖音极速版领取金币</div><div class="mc_vtvc_meta_block_area"><div class="mc_vtvc_meta_block"><div class="mc_vtvc_meta_row mc_vtvc_meta_pubdate"><span class="meta_vc_content">3K visualizaciones</span><span class="meta_pd_content">28 de jun. de 2020</span></div><div class="mc_vtvc_meta_row mc_vtvc_meta_channel"><span>YouTube</span><span class="mc_vtvc_meta_row_channel">zhang好玩实验室</span></div></div></div></div></div><div class="vrhdata" ht="0" vrhm="{&quot;cid&quot;:&quot;serpvidans_hc&quot;,&quot;smturl&quot;:&quot;https://th.bing.com/th?id=OM2.UB6xIu5hI9_nOA_1648379614&amp;pid=1.7&quot;,&quot;bci&quot;:0,&quot;du&quot;:&quot;18:15&quot;,&quot;murl&quot;:&quot;https://www.youtube.com/watch?v=MVwnPgbj4dc&quot;,&quot;thid&quot;:&quot;OVP.4zFass7OoAqCkMdyoHC9MQEsDh&quot;,&quot;mid&quot;:&quot;38E7DF2361EE22B11E5038E7DF2361EE22B11E50&quot;,&quot;vt&quot;:&quot;autojs入门 自动刷抖音极速版领取金币&quot;,&quot;IsAdultThumb&quot;:false,&quot;EnableLoopPlay&quot;:false,&quot;pgurl&quot;:&quot;https://www.youtube.com/watch?v=MVwnPgbj4dc&quot;,&quot;q&quot;:&quot;autojs&quot;,&quot;capt&quot;:{&quot;de&quot;:&quot;auto.js 从入门到放弃&quot;,&quot;s&quot;:&quot;YouTube&quot;,&quot;vc&quot;:&quot;3K visualizaciones&quot;,&quot;pud&quot;:&quot;28/06/2020&quot;,&quot;pu&quot;:&quot;zhang好玩实验室&quot;,&quot;sk&quot;:&quot;rms:answers:Multimedia:MMvsi-youtube_com&quot;}}"></div></div></a></div></div></div></div></div></div></div></div></div><template class="vrhc popout vrh_clc hide" id="serpvidans_hc" data-priority="2"><div class="vrhcp"><div class="vrhtc"><div class="vrhtpc"><div class="vt_vp "><div class="smtplayerhtml5 hide"><video class="smtplayer" type="video/mp4" 
                poster="data:image/svg+xml;charset=utf8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='50' height='50'/%3E" playsinline webkit-playsinline /></div></div><div class="player_ol"></div><img class="vrhi hide" alt="Marcador de posición de miniatura estática"/></div><div class="vrhol hide" role="application"><div class="vrhot hide"><div class="vrhot cont"></div></div><div class="b_ri ricons"><div class="vol hide"><div class="vol bg"></div><div class="vol cont hide"><div class="volMuteHandle" tabindex="0" role="button"><div class="volMuteIcon"></div></div></div></div></div></div></div></div><div class="vrhcpt hide" asMask="0"><div class="vrhtt hide"></div><div class="vrhdl hide"></div><div class="vrhmeta hide"><span class="vrhmdvc hide"></span><span class="vrhmddu hide"></span><span class="vrhmdpd hide"></span><span class="vrhmdr hide"></span></div><div class="vrhsrc hide"><span class="vrhsi hide"></span><span class="vrhsn hide"></span><span class="vrhspu hide"></span></div></div></template><a title="" class="b_moreLink ltr" href="/videos/search?q=autojs&amp;FORM=VDRESM" h="ID=SERP,5550.1">Ver m&#225;s v&#237;deos de autojs</a></div></div></li><li class="b_algo" data-id="" ><div class="tpcn"><a class="tilk" href="https://www.wuyunai.com/docs/guide/" h="ID=SERP,5286.1"><div class="tpic"><div class="wr_fav" data-priority="2"><div class="cico siteicon" style="width:32px;height:32px;"><div class="rms_iac" style="height:32px;line-height:32px;width:32px;" data-height="32" data-width="32" data-alt="Icono de web global" data-class="rms_img" data-src="https://th.bing.com/th?id=ODLS.A2450BEC-5595-40BA-9F13-D9EC6AB74B9F&amp;w=32&amp;h=32&amp;qlt=93&amp;pcl=fffffa&amp;o=6&amp;pid=1.2"></div></div></div></div><div class="tptxt"><div class="tptt">wuyunai.com</div><div class="tpmeta"><div class="b_attribution" u="3|5267|4791208187935562|wl4za8c3QlCCykpDybGUHqmhGZFflHAV" tabindex="0"><cite>https://www.wuyunai.com/docs/guide</cite><span class="c_tlbxTrg"><span class="c_tlbxH" H="BASE:CACHEDPAGEDEFAULT" K="SERP,5287.1"></span><span class="c_tlbxH" H="BASE:GENERATIVECAPTIONSHINTSIGNAL" K="SERP,5292.1"></span></span></div></div></div></a></div><h2><a href="https://www.wuyunai.com/docs/guide/" h="ID=SERP,5286.2">简介 | Auto.js Pro文档</a></h2><div class="b_caption" role="contentinfo"><p class="b_lineclamp2 b_algoSlug"><span class="algoSlug_icon" data-priority="2">Web</span><span class="news_dt">22 de oct. de 2022</span>&nbsp;&#0183;&#32;<strong>Auto.js</strong> Pro 是一个面向小应用开发、编程学习的代码开发平台，可以使用 JavaScript 和 Node.js 实现一个小应用，支持多种界面、控件、模块和工具。本文档 …</p></div></li><li class="b_algo" data-id="" ><div class="tpcn"><a class="tilk" href="https://github.com/clearw5/Auto.js/" h="ID=SERP,5310.1"><div class="tpic"><div class="wr_fav" data-priority="2"><div class="cico siteicon" style="width:32px;height:32px;"><div class="rms_iac" style="height:32px;line-height:32px;width:32px;" data-height="32" data-width="32" data-alt="Icono de web global" data-class="rms_img" data-src="https://th.bing.com/th?id=ODLS.05409d17-5d83-4701-acc1-90430dd3b02c&amp;w=32&amp;h=32&amp;qlt=94&amp;pcl=fffffa&amp;o=6&amp;pid=1.2"></div></div></div></div><div class="tptxt"><div class="tptt">Github</div><div class="tpmeta"><div class="b_attribution" u="4|5268|5033495876892249|7L5j9RTUp8vw-F9x69hkC_QjrCdeXHiq" tabindex="0"><cite>https://github.com/clearw5/Auto.js</cite><span class="c_tlbxTrg"><span class="c_tlbxH" H="BASE:CACHEDPAGEDEFAULT" K="SERP,5311.1"></span></span></div></div></div></a></div><h2><a href="https://github.com/clearw5/Auto.js/" h="ID=SERP,5310.2">GitHub - clearw5/Auto.js: Automation&amp;Workflow JavaScript IDE …</a></h2><div class="b_caption" role="contentinfo"><p class="b_lineclamp2 b_algoSlug"><span class="algoSlug_icon" data-priority="2">Web</span>官网: https://pro.<strong>autojs</strong>.org. 官方文档: https://pro.<strong>autojs</strong>.org/docs. 官方博客: https://blog.<strong>autojs</strong>.org. 开源说明. 本仓库为Auto.<strong>js</strong> 4.1的开源代码，从Auto.<strong>js</strong> Pro开始不 …</p></div></li><li class="b_algo" data-id="" ><div class="tpcn"><a class="tilk" href="https://codesandbox.io/examples/package/autojs" h="ID=SERP,5333.1"><div class="tpic"><div class="wr_fav" data-priority="2"><div class="cico siteicon" style="width:32px;height:32px;"><div class="rms_iac" style="height:32px;line-height:32px;width:32px;" data-height="32" data-width="32" data-alt="Icono de web global" data-class="rms_img" data-src="https://th.bing.com/th?id=ODLS.9103df18-58b6-4147-901f-2f2d6345c80f&amp;w=32&amp;h=32&amp;qlt=95&amp;pcl=fffffa&amp;o=6&amp;pid=1.2"></div></div></div></div><div class="tptxt"><div class="tptt">CodeSandbox</div><div class="tpmeta"><div class="b_attribution" u="5|5105|4879332316241137|ovgHl5ZJHUiq3S4a5YMO678U1OoRtPML" tabindex="0"><cite>https://codesandbox.io/examples/package/autojs</cite><span class="c_tlbxTrg"><span class="c_tlbxH" H="BASE:CACHEDPAGEDEFAULT" K="SERP,5334.1"></span><span class="c_tlbxH" H="BASE:GENERATIVECAPTIONSHINTSIGNAL" K="SERP,5339.1"></span></span></div></div></div></a></div><h2><a href="https://codesandbox.io/examples/package/autojs" h="ID=SERP,5333.2">autojs examples - CodeSandbox</a></h2><div class="b_caption" role="contentinfo"><p class="b_lineclamp2 b_algoSlug"><span class="algoSlug_icon" data-priority="2">Web</span><strong>Autojs</strong> is a JavaScript library that simplifies the creation of web applications. Use this online<strong> autojs</strong> playground to view and fork<strong> autojs</strong> example apps and templates on …</p></div></li><li class="b_algo" data-id="" ><div class="tpcn"><a class="tilk" href="https://ilg2021.github.io/AutoJsX-Docs/" h="ID=SERP,5353.1"><div class="tpic"><div class="wr_fav" data-priority="2"><div class="cico siteicon" style="width:32px;height:32px;"><div class="rms_iac" style="height:32px;line-height:32px;width:32px;" data-height="32" data-width="32" data-alt="Icono de web global" data-class="rms_img" data-src="https://th.bing.com/th?id=ODLS.A2450BEC-5595-40BA-9F13-D9EC6AB74B9F&amp;w=32&amp;h=32&amp;qlt=96&amp;pcl=fffffa&amp;o=6&amp;pid=1.2"></div></div></div></div><div class="tptxt"><div class="tptt">ilg2021.github.io</div><div class="tpmeta"><div class="b_attribution" tabindex="0"><cite>https://ilg2021.github.io/AutoJsX-Docs</cite></div></div></div></a></div><h2><a href="https://ilg2021.github.io/AutoJsX-Docs/" h="ID=SERP,5353.2">AutoX.js</a></h2><div class="b_caption" role="contentinfo"><p class="b_lineclamp2 b_algoSlug"><span class="algoSlug_icon" data-priority="2">Web</span><strong>AutoJs</strong>的工作原理是在一个Android App AutoJsX上内置了js引擎，js引擎实现了手机自动化的一套js api。在vs code上写js代码，然后通过局域网推送js代码到AutoJsX，这样就可 …</p></div></li><li class="b_algo" data-id="" ><div class="tpcn"><a class="tilk" href="https://www.npmjs.com/search?q=%40autojs" h="ID=SERP,5375.1"><div class="tpic"><div class="wr_fav" data-priority="2"><div class="cico siteicon" style="width:32px;height:32px;"><div class="rms_iac" style="height:32px;line-height:32px;width:32px;" data-height="32" data-width="32" data-alt="Icono de web global" data-class="rms_img" data-src="https://th.bing.com/th?id=ODLS.4983b4a2-f7e6-47d1-a8cb-212043d62dbf&amp;w=32&amp;h=32&amp;qlt=97&amp;pcl=fffffa&amp;o=6&amp;pid=1.2"></div></div></div></div><div class="tptxt"><div class="tptt">npm</div><div class="tpmeta"><div class="b_attribution" u="7|5270|4562093143253697|kO0O9k-HFioamdaQQRJASeevAOPISQvf" tabindex="0"><cite>https://www.npmjs.com/search?q=@autojs</cite><span class="c_tlbxTrg"><span class="c_tlbxH" H="BASE:CACHEDPAGEDEFAULT" K="SERP,5376.1"></span></span></div></div></div></a></div><h2><a href="https://www.npmjs.com/search?q=%40autojs" h="ID=SERP,5375.2">@autojs - npm search</a></h2><div class="b_caption" role="contentinfo"><p class="b_lineclamp2 b_algoSlug"><span class="algoSlug_icon" data-priority="2">Web</span><span class="news_dt">6 de may. de 2014</span>&nbsp;&#0183;&#32;Asynchronous OpenCV 3.x nodejs bindings with JavaScript and TypeScript API for Auto.<strong>js</strong> Pro. opencv. cv. <strong>autojs</strong>. autojspro. computer vision. face. …</p></div></li><li class="b_algo" data-id="" ><div class="tpcn"><a class="tilk" href="https://docs.autojs6.com/" h="ID=SERP,5394.1"><div class="tpic"><div class="wr_fav" data-priority="2"><div class="cico siteicon" style="width:32px;height:32px;"><div class="rms_iac" style="height:32px;line-height:32px;width:32px;" data-height="32" data-width="32" data-alt="Icono de web global" data-class="rms_img" data-src="https://th.bing.com/th?id=ODLS.A2450BEC-5595-40BA-9F13-D9EC6AB74B9F&amp;w=32&amp;h=32&amp;qlt=98&amp;pcl=fffffa&amp;o=6&amp;pid=1.2"></div></div></div></div><div class="tptxt"><div class="tptt">autojs6.com</div><div class="tpmeta"><div class="b_attribution" u="8|5107|4588434190394766|G3hTV7U63QwmlHteit9_myPVj3bdeI81" tabindex="0"><cite>https://docs.autojs6.com</cite><span class="c_tlbxTrg"><span class="c_tlbxH" H="BASE:GENERATIVECAPTIONSHINTSIGNAL" K="SERP,5399.1"></span></span></div></div></div></a></div><h2><a href="https://docs.autojs6.com/" h="ID=SERP,5394.2">AutoJs6 文档 - GitHub</a></h2><div class="b_caption" role="contentinfo"><p class="b_lineclamp2 b_algoSlug"><span class="algoSlug_icon" data-priority="2">Web</span><strong>AutoJs6</strong> 是一个基于 Rhino 引擎的 JavaScript 脚本引擎，可以用于安卓平台的自动化测试和开发。本文档介绍了<strong> AutoJs</strong>6 的主要特性、功能和用法，包括定时器、线程、对话框、 …</p></div></li><li class="b_algo" data-id="" ><div class="tpcn"><a class="tilk" href="https://programmerclick.com/article/76453956989/" h="ID=SERP,5411.1"><div class="tpic"><div class="wr_fav" data-priority="2"><div class="cico siteicon" style="width:32px;height:32px;"><div class="rms_iac" style="height:32px;line-height:32px;width:32px;" data-height="32" data-width="32" data-alt="Icono de web global" data-class="rms_img" data-src="https://th.bing.com/th?id=ODLS.A2450BEC-5595-40BA-9F13-D9EC6AB74B9F&amp;w=32&amp;h=32&amp;qlt=99&amp;pcl=fffffa&amp;o=6&amp;pid=1.2"></div></div></div></div><div class="tptxt"><div class="tptt">programmerclick.com</div><div class="tpmeta"><div class="b_attribution" u="9|5272|4940913560677862|i-mvXU9pvMPG3nAIsAdwSbMxY4GdVUY1" tabindex="0"><cite>https://programmerclick.com/article/76453956989</cite><span class="c_tlbxTrg"><span class="c_tlbxH" H="BASE:CACHEDPAGEDEFAULT" K="SERP,5412.1"></span></span></div></div></div></a></div><h2><a href="https://programmerclick.com/article/76453956989/" h="ID=SERP,5411.2"><strong>Autojs</strong> de automatizaci&#243;n de aplicaciones m&#243;viles - programador clic</a></h2><div class="b_caption" role="contentinfo"><p class="b_lineclamp4 b_algoSlug"><span class="algoSlug_icon" data-priority="2">Web</span><strong>Autojs</strong> de automatizaci&#243;n de aplicaciones m&#243;viles: P&#225;gina web oficial. En primer lugar, la computadora est&#225; instalada en VScode y luego busca el enchufe -in en el plug -in: <strong>AutoJS</strong>, mi versi&#243;n es un poco antigua, as&#237; que elija: Por supuesto, el simulador o tel&#233;fono m&#243;vil debe instalar <strong>AutoJs</strong>, prestar atenci&#243;n a la misma que la versi&#243;n ...</p></div></li><li class="b_ans" data-tag data-partnerTag data-id="" h="SERP,5572.1" ><div class="wpt_bc_container b_hide" data-priority="2"></div><div elementtiming=frp.BottomOfPage style="pointer-events:none;margin-top:-1px;height:1px;width:1px;font-size:1px;position:absolute;top:0;right:0;" tabindex="-1"> &nbsp; </div></li><li class="b_ans" data-tag data-partnerTag data-id="" h="SERP,5573.1" ><div id="relatedSearchesLGWContainer" data-priority="2"><h2 class="">B&#250;squedas relacionadas de <strong>autojs</strong></h2><div id="rs_lgw_list"><a href="/search?q=autojs+examples&amp;FORM=LGWQS1" h="ID=SERP,5586.1"><div class="animation-padding"></div><div class="rs_lgw_icon  sug_sprite"></div><div class="b_suggestionText">autojs <strong>examples</strong></div></a><a href="/search?q=autojs+github&amp;FORM=LGWQS2" h="ID=SERP,5588.1"><div class="animation-padding"></div><div class="rs_lgw_icon  sug_sprite"></div><div class="b_suggestionText">autojs <strong>github</strong></div></a><a href="/search?q=auto.js+pro+%e6%95%99%e7%a8%8b&amp;FORM=LGWQS3" h="ID=SERP,5590.1"><div class="animation-padding"></div><div class="rs_lgw_icon  sug_sprite"></div><div class="b_suggestionText"><strong>auto</strong>.<strong>js pro 教程</strong></div></a><a href="/search?q=autojs+github+download&amp;FORM=LGWQS4" h="ID=SERP,5592.1"><div class="animation-padding"></div><div class="rs_lgw_icon  sug_sprite"></div><div class="b_suggestionText">autojs <strong>github download</strong></div></a><a class="more-rs-btn" href="#"><div class="animation-padding"></div><div class="more-rs-btn-icon  sug_sprite"></div><div class="b_suggestionText">B&#250;squedas m&#225;s relacionadas</div></a></div></div><div id="inline_rs" data-priority="2" class="b_hide"><div class="rslist_head"><span class="rslist_title b_strong">Otras personas tambi&#233;n buscan</span><a role="button" aria-label="Cerrar" onclick="qbrs_close()" id="rscar_close" /></div><div id="rs_root"><div class="b_rich"><div class="b_vlist2col"><ul><li><a href="/search?q=autojs+examples&amp;FORM=QSRE1" h="ID=SERP,5574.1">autojs <strong>examples</strong></a></li><li><a href="/search?q=auto.js+pro+%e6%95%99%e7%a8%8b&amp;FORM=QSRE3" h="ID=SERP,5578.1"><strong>auto</strong>.<strong>js pro 教程</strong></a></li><li><a href="/search?q=auto.js+pro+%e6%96%87%e6%a1%a3&amp;FORM=QSRE5" h="ID=SERP,5582.1"><strong>auto</strong>.<strong>js pro 文档</strong></a></li></ul><ul><li><a href="/search?q=autojs+github&amp;FORM=QSRE2" h="ID=SERP,5576.1">autojs <strong>github</strong></a></li><li><a href="/search?q=autojs+github+download&amp;FORM=QSRE4" h="ID=SERP,5580.1">autojs <strong>github download</strong></a></li><li><a href="/search?q=autox.js%e4%b8%8b%e8%bd%bd&amp;FORM=QSRE6" h="ID=SERP,5584.1"><strong>autox</strong>.<strong>js下载</strong></a></li></ul></div></div></div></div><div class="b_rs" id="brsv3"><h2 class="">B&#250;squedas relacionadas de <strong>autojs</strong></h2><ul class="b_vList b_divsec"><li data-priority=><a href="/search?q=autojs+examples&amp;FORM=QSRE1" h="ID=SERP,5574.2"><div class="b_suggestionIcon  sug_sprite"></div><div class="b_suggestionText">autojs <strong>examples</strong></div></a></li><li data-priority=><a href="/search?q=autojs+github&amp;FORM=QSRE2" h="ID=SERP,5576.2"><div class="b_suggestionIcon  sug_sprite"></div><div class="b_suggestionText">autojs <strong>github</strong></div></a></li><li data-priority=><a href="/search?q=auto.js+pro+%e6%95%99%e7%a8%8b&amp;FORM=QSRE3" h="ID=SERP,5578.2"><div class="b_suggestionIcon  sug_sprite"></div><div class="b_suggestionText"><strong>auto</strong>.<strong>js pro 教程</strong></div></a></li><li data-priority=><a href="/search?q=autojs+github+download&amp;FORM=QSRE4" h="ID=SERP,5580.2"><div class="b_suggestionIcon  sug_sprite"></div><div class="b_suggestionText">autojs <strong>github download</strong></div></a></li><li data-priority=><a href="/search?q=auto.js+pro+%e6%96%87%e6%a1%a3&amp;FORM=QSRE5" h="ID=SERP,5582.2"><div class="b_suggestionIcon  sug_sprite"></div><div class="b_suggestionText"><strong>auto</strong>.<strong>js pro 文档</strong></div></a></li><li data-priority=><a href="/search?q=autox.js%e4%b8%8b%e8%bd%bd&amp;FORM=QSRE6" h="ID=SERP,5584.2"><div class="b_suggestionIcon  sug_sprite"></div><div class="b_suggestionText"><strong>autox</strong>.<strong>js下载</strong></div></a></li></ul></div></li><li class="b_pag"><nav role="navigation" aria-label="Más resultados para autojs"><h4 class="b_hide">Paginaci&#243;n</h4><ul class="sb_pagF"><li><a class="sb_pagS sb_pagS_bp b_widePag sb_bp sb_pag_first">1</a></li><li><a class="b_widePag sb_bp" aria-label="Página 2" href="/search?q=autojs&amp;qs=HS&amp;cvid=e321066b41dc426184a064d75924a313&amp;pq=&amp;cc=MX&amp;setlang=es-MX&amp;nclid=AC1E1DBB5EAEDDBC2F9B5D4F0F557FAC&amp;ts=1708643909987&amp;wsso=Moderate&amp;FPIG=F4148F017DED47FD91F14746ED92DB7A&amp;first=11&amp;FORM=PERE" h="ID=SERP,5624.1">2</a></li><li><a class="b_widePag sb_bp" aria-label="Página 3" href="/search?q=autojs&amp;qs=HS&amp;cvid=e321066b41dc426184a064d75924a313&amp;pq=&amp;cc=MX&amp;setlang=es-MX&amp;nclid=AC1E1DBB5EAEDDBC2F9B5D4F0F557FAC&amp;ts=1708643909987&amp;wsso=Moderate&amp;FPIG=F4148F017DED47FD91F14746ED92DB7A&amp;first=21&amp;FORM=PERE1" h="ID=SERP,5625.1">3</a></li><li><a class="b_widePag sb_bp" aria-label="Página 4" href="/search?q=autojs&amp;qs=HS&amp;cvid=e321066b41dc426184a064d75924a313&amp;pq=&amp;cc=MX&amp;setlang=es-MX&amp;nclid=AC1E1DBB5EAEDDBC2F9B5D4F0F557FAC&amp;ts=1708643909987&amp;wsso=Moderate&amp;FPIG=F4148F017DED47FD91F14746ED92DB7A&amp;first=31&amp;FORM=PERE2" h="ID=SERP,5626.1">4</a></li><li><a class="b_widePag sb_bp" aria-label="Página 5" href="/search?q=autojs&amp;qs=HS&amp;cvid=e321066b41dc426184a064d75924a313&amp;pq=&amp;cc=MX&amp;setlang=es-MX&amp;nclid=AC1E1DBB5EAEDDBC2F9B5D4F0F557FAC&amp;ts=1708643909987&amp;wsso=Moderate&amp;FPIG=F4148F017DED47FD91F14746ED92DB7A&amp;first=41&amp;FORM=PERE3" h="ID=SERP,5627.1">5</a></li><li><a class="sb_pagN sb_pagN_bp b_widePag sb_bp " title="Página siguiente" href="/search?q=autojs&amp;qs=HS&amp;cvid=e321066b41dc426184a064d75924a313&amp;pq=&amp;cc=MX&amp;setlang=es-MX&amp;nclid=AC1E1DBB5EAEDDBC2F9B5D4F0F557FAC&amp;ts=1708643909987&amp;wsso=Moderate&amp;FPIG=F4148F017DED47FD91F14746ED92DB7A&amp;first=11&amp;FORM=PORE" h="ID=SERP,5628.1"><div class="sw_next">Siguiente</div></a></li></ul></nav><div id="b_pag_chat_btn_container" data-text="Chat"></div></li><li id="mfa_root" class="b_fabHide fabcolapse" data-priority="2"><div class="b_ds5"><a role="button" class="mfa_btn" id="mfa_srch" aria-label="Buscar más" href="javascript:void(0);" h="ID=SERP,5635.1"><div class="fba_sbicn" style="width: 40px; height: 40px;"></div></a></div></li></ol></main><aside aria-label="Resultados adicionales"><div id="b_mtp" class="crhide" data-priority="2"><a class="mtprt" href="javascript: void(0);" h="ID=SERP,5659.1"><img class="mtpsvg mtparr rms_img" height="40" width="40" src="data:image/svg+xml,%3Csvg%20viewBox%3D%220%200%2040%2040%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%3E%3ClinearGradient%20id%3D%22a%22%20gradientUnits%3D%22userSpaceOnUse%22%20x1%3D%220%22%20x2%3D%2240%22%20y1%3D%2220%22%20y2%3D%2220%22%3E%3Cstop%20offset%3D%220%22%20stop-color%3D%22%232556e4%22%20%2F%3E%3Cstop%20offset%3D%221%22%20stop-color%3D%22%233cbef6%22%20%2F%3E%3C%2FlinearGradient%3E%3Ccircle%20cx%3D%2220%22%20cy%3D%2220%22%20fill%3D%22url%28%23a%29%22%20r%3D%2220%22%20%2F%3E%3Cpath%20d%3D%22m23.293%2010.293-9%209c-.391.391-.391%201.024%200%201.414l9%209c.391.391%201.024.391%201.414%200%20.391-.391.391-1.024%200-1.414l-8.293-8.293%208.293-8.293c.391-.391.391-1.024%200-1.414-.39-.391-1.024-.391-1.414%200z%22%20fill%3D%22%23fff%22%20%2F%3E%3C%2Fsvg%3E" /><div class="mtpseem">Ver m&#225;s</div></a></div><ol id="b_context" class="crinvis"><li class="b_ans" data-tag data-partnerTag data-id="" h="SERP,5609.1" ><div data-tag="RichRSRailOuterWrapper" class="richrswrapper" aria-label="Búsquedas relacionadas"><div data-tag="RichRSRailSuggestions" data-priority="2"><div data-priority="2"><div class="richrsrailtitle" data-priority="2"><h2 class="">B&#250;squedas relacionadas</h2></div><div class="richrsrailsugwrapper"><div class="richrsrailsuggestion"><a data-partnerTag="RelatedSearchesRail.Suggestion" href="/search?q=autojs+examples&amp;FORM=R5FD" h="ID=SERP,5610.1"><div class="richrsrailsuggestion_img  sug_sprite"></div><div class="richrsrailsuggestion_text">autojs <strong>examples</strong></div></a></div><div class="richrsrailsuggestion"><a data-partnerTag="RelatedSearchesRail.Suggestion" href="/search?q=autojs+github&amp;FORM=R5FD1" h="ID=SERP,5612.1"><div class="richrsrailsuggestion_img  sug_sprite"></div><div class="richrsrailsuggestion_text">autojs <strong>github</strong></div></a></div><div class="richrsrailsuggestion"><a data-partnerTag="RelatedSearchesRail.Suggestion" href="/search?q=auto.js+pro+%e6%95%99%e7%a8%8b&amp;FORM=R5FD2" h="ID=SERP,5614.1"><div class="richrsrailsuggestion_img  sug_sprite"></div><div class="richrsrailsuggestion_text"><strong>auto</strong>.<strong>js pro 教程</strong></div></a></div><div class="richrsrailsuggestion"><a data-partnerTag="RelatedSearchesRail.Suggestion" href="/search?q=autojs+github+download&amp;FORM=R5FD3" h="ID=SERP,5616.1"><div class="richrsrailsuggestion_img  sug_sprite"></div><div class="richrsrailsuggestion_text">autojs <strong>github download</strong></div></a></div><div class="richrsrailsuggestion"><a data-partnerTag="RelatedSearchesRail.Suggestion" href="/search?q=auto.js+pro+%e6%96%87%e6%a1%a3&amp;FORM=R5FD4" h="ID=SERP,5618.1"><div class="richrsrailsuggestion_img  sug_sprite"></div><div class="richrsrailsuggestion_text"><strong>auto</strong>.<strong>js pro 文档</strong></div></a></div><div class="richrsrailsuggestion"><a data-partnerTag="RelatedSearchesRail.Suggestion" href="/search?q=autox.js%e4%b8%8b%e8%bd%bd&amp;FORM=R5FD5" h="ID=SERP,5620.1"><div class="richrsrailsuggestion_img  sug_sprite"></div><div class="richrsrailsuggestion_text"><strong>autox</strong>.<strong>js下载</strong></div></a></div></div></div></div></div><div elementtiming=RightRailPaint style="pointer-events:none;height:0;width:1px;position:relative;" tabindex="-1"> &nbsp; </div></li></ol></aside><aside aria-label="Explorar más"><ol id="b_dynRail"></ol></aside></div><footer id="b_footer" class="b_footer" role="contentinfo" aria-label="Pie de página"><div id="b_footerItems"><span>&#169; 2024 Microsoft</span><ul><li><a id="sb_privacy" href="http://go.microsoft.com/fwlink/?LinkId=521839" h="ID=SERP,5076.1">Privacidad y cookies</a></li><li><a id="sb_legal" href="http://go.microsoft.com/fwlink/?LinkID=246338" h="ID=SERP,5077.1">Informaci&#243;n legal</a></li><li><a id="sb_advertise" href="https://go.microsoft.com/fwlink/?linkid=868922" h="ID=SERP,5078.1">Anuncios</a></li><li><a id="sb_adinfo" target="_blank" href="http://go.microsoft.com/fwlink/?LinkID=286759" h="ID=SERP,5079.1">Sobre nuestra publicidad</a></li><li><a id="sb_help" target="_blank" href="https://support.microsoft.com/topic/82d20721-2d6f-4012-a13d-d1910ccf203f" h="ID=SERP,5080.1">Ayuda</a></li><li><a role="button" id="sb_feedback" href="#" h="ID=SERP,5081.1">Comentarios</a></li></ul></div><!--foo--><!--foo--></footer><link id="as-css-link" rel="prefetch" fetchpriority="low" href="https://r.bing.com/rb/H/cir3,as,cc,nc/-uCzZE2zqQmXDKoUgQ-FFqMjuYc.css?bu=Io8GrgOgBaQErgOuA64DrgOuA64DiweNB-0GrgOuA64DrgPjBoIFvwauA64DggeuA5oFrgOPB50HrgOuA9IErgPoBK4D&amp;or=w" type="text/css" as="style" /><div id="b_TriviaOverlay"></div><div class="sb_hbop"><div id="d5154" class="b_hide b_dropdown" role="menu" data-filterkey=""><div class="ftrD" id="ftrD_Fecha"><a class="b_toggle" role="menuitem" href="/search?q=autojs&amp;FORM=000017&amp;qs=HS&amp;cvid=e321066b41dc426184a064d75924a313&amp;cc=MX&amp;setlang=es-MX&amp;nclid=AC1E1DBB5EAEDDBC2F9B5D4F0F557FAC&amp;ts=1708643909987&amp;wsso=Moderate&amp;showconv=0&amp;qpvt=autojs" h="ID=SERP,5435.1">Todo</a><a class="b_toggle " role="menuitem" target="" tabindex="-1" href="/search?q=autojs&amp;filters=ex1%3a%22ez1%22&amp;FORM=000017&amp;qs=HS&amp;cvid=e321066b41dc426184a064d75924a313&amp;cc=MX&amp;setlang=es-MX&amp;nclid=AC1E1DBB5EAEDDBC2F9B5D4F0F557FAC&amp;ts=1708643909987&amp;wsso=Moderate&amp;showconv=0&amp;qpvt=autojs" h="ID=SERP,5436.1">&#218;ltimas 24 horas</a><a class="b_toggle " role="menuitem" target="" tabindex="-1" href="/search?q=autojs&amp;filters=ex1%3a%22ez2%22&amp;FORM=000017&amp;qs=HS&amp;cvid=e321066b41dc426184a064d75924a313&amp;cc=MX&amp;setlang=es-MX&amp;nclid=AC1E1DBB5EAEDDBC2F9B5D4F0F557FAC&amp;ts=1708643909987&amp;wsso=Moderate&amp;showconv=0&amp;qpvt=autojs" h="ID=SERP,5437.1">&#218;ltima semana</a><a class="b_toggle " role="menuitem" target="" tabindex="-1" href="/search?q=autojs&amp;filters=ex1%3a%22ez3%22&amp;FORM=000017&amp;qs=HS&amp;cvid=e321066b41dc426184a064d75924a313&amp;cc=MX&amp;setlang=es-MX&amp;nclid=AC1E1DBB5EAEDDBC2F9B5D4F0F557FAC&amp;ts=1708643909987&amp;wsso=Moderate&amp;showconv=0&amp;qpvt=autojs" h="ID=SERP,5438.1">&#218;ltimo mes</a><a class="b_toggle " role="menuitem" target="" tabindex="-1" href="/search?q=autojs&amp;filters=ex1%3a%22ez5_19411_19776%22&amp;FORM=000017&amp;qs=HS&amp;cvid=e321066b41dc426184a064d75924a313&amp;cc=MX&amp;setlang=es-MX&amp;nclid=AC1E1DBB5EAEDDBC2F9B5D4F0F557FAC&amp;ts=1708643909987&amp;wsso=Moderate&amp;showconv=0" h="ID=SERP,5439.1">&#218;ltimo a&#241;o</a><span id="drvph" data-url="/control/tfcalendar?q=autojs&amp;form=WSHBSH&amp;qs=HS&amp;cvid=e321066b41dc426184a064d75924a313&amp;pq=&amp;cc=MX&amp;setlang=es-MX&amp;nclid=AC1E1DBB5EAEDDBC2F9B5D4F0F557FAC&amp;ts=1708643909987&amp;wsso=Moderate&amp;showconv=0&amp;IG=F4148F017DED47FD91F14746ED92DB7A&amp;StartDate=02%2f12%2f2024+18%3a49%3a58&amp;EndDate=02%2f22%2f2024+18%3a49%3a58&amp;IsApplied=False&amp;DisplayText=Intervalo+personalizado&amp;DateOffset=0&amp;FormCode=&amp;ApplyLinkUrl=%2fsearch%3fq%3dautojs%26filters%3dex1%253a%2522ez5_0_0%2522%26FORM%3d000017%26qs%3dHS%26cvid%3de321066b41dc426184a064d75924a313%26cc%3dMX%26setlang%3des-MX%26nclid%3dAC1E1DBB5EAEDDBC2F9B5D4F0F557FAC%26ts%3d1708643909987%26wsso%3dModerate%26showconv%3d0%26qpvt%3dautojs&amp;ApplyLinkText=ez5&amp;IID=SERP.5154"></span></div></div><div id="PromptDetails" class="b_hide" data-lat="0" data-lon="0" data-dist="0" data-noRefresh="true" data-check="true"></div><div id="g_EfLoc" data-lat="18.995777130126953" data-lon="-102.19142150878906" data-disp=""
                data-source="4"></div></div><script type="text/javascript" nonce="hdm+zCWukVbuQiu/8nwvTExR65VnfK54cOHZ35vK2Vk=">//<![CDATA[
{ const icon = _ge("sb_sbi"); if (icon) { icon.addEventListener("click", function() { SBI.IconLoader.handleIconClick(icon); }); } };var img_p = document.getElementById('id_p'); img_p && img_p.addEventListener('error', function() {FallBackToDefaultProfilePic(img_p) });;
//]]></script><script type="text/javascript" nonce="hdm+zCWukVbuQiu/8nwvTExR65VnfK54cOHZ35vK2Vk=">//<![CDATA[
(function(n,t){onload=function(){_G.BPT=new Date;n&&n();!_w.sb_ppCPL&&t&&sb_st(function(){t(new Date)},0)}})(_w.onload,_w.si_PP);_G!==undefined&&_G.EF!==undefined&&_G.EF.bmasynctrigger===1&&window.requestAnimationFrame!==undefined&&document.visibilityState!==undefined&&document.visibilityState==="visible"?requestAnimationFrame(function(){_G.EF.bmasynctrigger2===1?requestAnimationFrame(function(){BM.trigger()}):_G.EF.bmasynctrigger3===1?requestAnimationFrame(function(){setTimeout(function(){BM.trigger()},0)}):setTimeout(function(){BM.trigger()},0)}):BM.trigger();sa_loader=function(){_w.rms.js({'rms:answers:AutoSuggest:AutoSug':'https:\/\/r.bing.com\/rp\/_dZwfDcrceLXWpKNgk7C7VeU-q0.br.js',d:1});};;_w.rms.js({'A:0':0},{'A:rms:answers:Shared:BingCore.Bundle':'\/rp\/IvfoLO3XuUzsCyfv53CD-ckZU3s.br.js'},{'A:rms:answers:VisualSystem:QuickSearch':'https:\/\/r.bing.com\/rp\/Y26LIcmRz0EdnBtSjtN2P4pbrp4.br.js'},{'A:rms:answers:BoxModel:Framework':'https:\/\/r.bing.com\/rp\/R14ozkkie30zM6FSjzwWFp8Ffzk.br.js'},{'A:rms:answers:WebVitals:WebVitalsMetrics':'https:\/\/r.bing.com\/rp\/0qo7e16hAJuKr67fZ-dKtNlvtjg.br.js'},{'A:1':1},{'A:rms:answers:Shared:mlibCssClassAfterOnLoad':'https:\/\/r.bing.com\/rp\/9MqrCXB0EVjVIRzDOArDGhu3yeM.br.js'},{'A:2':2},{'A:rms:answers:Web:ClearSearchBox':'https:\/\/r.bing.com\/rp\/4qLYPfN0EmVUH2TIgYLmYcXKYtQ.br.js'},{'A:rms:answers:HomepageVNext:PreactLibrary':'https:\/\/r.bing.com\/rp\/-8B2JlFI9HgFcgp0RGJHcPQD2GM.br.js'},{'A:rms:answers:HomepageVNext:SpeechSDKClone':'https:\/\/r.bing.com\/rp\/2DP4HPIfaNQ7pkpsKIkpRa3DF6Y.br.js'},{'A:rms:answers:HomepageVNext:PostloadedSpeechPreactBundle':'https:\/\/r.bing.com\/rp\/2_T03GKj2lZLGoe1GR3JpyqblCM.br.js'},{'A:3':3},{'A:rms:answers:MultimediaComponents:SearchByImageComponentLoader':'https:\/\/r.bing.com\/rp\/lLk8XmbdNzzlnPRzVzDhaF9yjqw.br.js'},{'A:4':4},{'A:rms:answers:Web:FreeSydneyHelper':'https:\/\/r.bing.com\/rp\/bX_giwlxG7aZa2HY7n-7y6E9z18.br.js'},{'A:rms:answers:Web:SydneyFSCHelper':'https:\/\/r.bing.com\/rp\/ohHcF6qnJ0cGvl-62sdDPRry1dE.br.js'},{'A:rms:answers:VisualSystem:ConversationScope':'https:\/\/r.bing.com\/rp\/ascyKFaOzJnUTZ_D-Mv819BZ0Go.br.js'},{'A:rms:answers:Identity:BlueIdentityDropdownBootStrap':'https:\/\/r.bing.com\/rp\/Gyuq2bqitqDJM0BeAkbKXGlQXNw.br.js'},{'A:rms:answers:Identity:BlueIdentityHeader':'https:\/\/r.bing.com\/rp\/n21aGRCN5EKHB3qObygw029dyNU.br.js'},{'A:rms:answers:OrgId:SsoFrame':'https:\/\/r.bing.com\/rp\/V_fBQ_iVmAgE_Ta_T-6BNXc0ZY4.br.js'},{'A:5':5},{'A:rms:answers:Identity:ProfilePicturePostLoader':'https:\/\/r.bing.com\/rp\/XJ8OmILbNhm0zU9tdkuGYeXVPRQ.br.js'},{'A:rms:answers:Identity:AccountLinkLoadProfileIcon':'https:\/\/r.bing.com\/rp\/gKwIRAF4fg7noG1zyeUz8x3Jdhc.br.js'},{'A:rms:answers:OrgId:AccountLink':'https:\/\/r.bing.com\/rp\/9cuwOQ_qE7qTGKohzrf_gIjTlPI.br.js'},{'A:6':6},{'A:rms:answers:Rewards:RewardsHeaderSVG':'https:\/\/r.bing.com\/rp\/Gw7eETSwe7GHmKwW1lRqGPQJXRo.br.js'},{'A:rms:answers:Rewards:RewardsNcHeaderBootstrap':'https:\/\/r.bing.com\/rp\/psgXZvzYJMEW2ydikIk493Va1d4.br.js'},{'A:RewardsReportActivity':'https:\/\/r.bing.com\/rs\/7d\/xb\/nj\/jQ1w07qyCcc9l2abcuV-aLzD-d8.js?or=w'},{'A:rms:answers:Rewards:RewardsCreditRefresh':'https:\/\/r.bing.com\/rp\/5L3iD467J3iJWEPwIjxlK0MMDpY.br.js'},{'A:rms:answers:Rewards:ModernReportActivity':'https:\/\/r.bing.com\/rp\/Sjh4wvzd2Y3WP03lvCYB_XIB9oE.br.js'},{'A:rms:answers:Rewards:ReportActivityBootstrap':'https:\/\/r.bing.com\/rp\/K_V1CARn2Q2lTs5njJKUvUkHyi4.br.js'},{'A:rms:answers:VisualSystem:LanguageSwitch':'https:\/\/r.bing.com\/rp\/GSNeCa5XvtoP6jz0k5V172vRaQ8.br.js'},{'A:rms:answers:VisualSystem:ScopeBarAppShortcuts':'https:\/\/r.bing.com\/rp\/3US3nNU_RgsSNFm9Bzw6xgeuOHk.br.js'},{'A:rms:answers:VisualSystem:NotebookScope':'https:\/\/r.bing.com\/rp\/-BEQ_TpHKH1ZqSGIHPUIMCXcsiY.br.js'},{'A:rms:answers:VisualSystem:ScopeBarRankInst':'https:\/\/r.bing.com\/rp\/NfTD8Ovh04Y_Ni14YxqYB8R_2_Q.br.js'},{'A:7':7},{'A:rms:answers:VisualSystem:LogTabCount':'https:\/\/r.bing.com\/rp\/fRSNKQanUHk53F1a1Bi8UA71Qt4.br.js'},{'A:rms:answers:Web:UpdateDefaults':'https:\/\/r.bing.com\/rp\/6mZmj1db42G_jniFgdT7MCvBgyA.br.js'},{'A:rms:answers:Web:KeyPressScroll':'https:\/\/r.bing.com\/rp\/JigriHckblqcu1XwKpT4wumVS2k.br.js'},{'A:rms:answers:Web:AlgoTitleColorOnMousedown':'https:\/\/r.bing.com\/rp\/UftfQbYuKvGGEUHPU3QGHYd90Z8.br.js'},{'A:rms:answers:Web:CursorProgressOnClick_Rebind':'https:\/\/r.bing.com\/rp\/06bQtOdvnqIODKnOBKJedLV7FUg.br.js'},{'A:rms:answers:Web:CursorProgressOnClick':'https:\/\/r.bing.com\/rp\/zlfm-hC70pZAs62UVTTl3KShKOE.br.js'},{'A:rms:answers:SharedStaticAssets:EsModuleShim':'https:\/\/r.bing.com\/rp\/HbkPYqcLh_xqZSd1KqLTrWN8MYY.br.js'},{'A:rms:answers:SharedStaticAssets:azure_msal_browser':'https:\/\/r.bing.com\/rp\/hkXWsTcGTHs44QxzZyThd4fbbPM.br.js'},{'A:rms:answers:SharedStaticAssets:microsoft-graph-client':'https:\/\/r.bing.com\/rp\/EcPZJcvBpS1TWE_YYG-PcTqlkRQ.br.js'},{'A:8':8},{'A:rms:answers:Shared:Base64Encoder':'https:\/\/r.bing.com\/rp\/5FbVcVko_TuW5Y8VB4_bHoFuDm8.br.js'},{'A:rms:answers:Shared:GlobalActionMenuV2WrapperSharing_AfterOnLoad':'https:\/\/r.bing.com\/rp\/gEJbLKCNueQBwjMiec5b4y7cvVQ.br.js'},{'A:rms:answers:Shared:GlobalActionMenuV2WrapperTrigger':'https:\/\/r.bing.com\/rp\/EmbwE8rah_7zdUQkosWSw-5nAPY.br.js'},{'A:rms:answers:Shared:GlobalActionMenuV2Wrapper_AfterOnLoad':'https:\/\/r.bing.com\/rp\/E9t3sAO3lNcvcdO-T16IoSdcs94.br.js'},{'A:rms:answers:Web:SydneyFullScreenConvLogging':'https:\/\/r.bing.com\/rp\/44Hadr7BiNLQdKT4SHan0JHPRgc.br.js'},{'A:rms:answers:Web:SydneyWaitlistUpdate':'https:\/\/r.bing.com\/rp\/yOU5_vGeE7HIs09fec6ZV9prLO4.br.js'},{'A:rms:answers:CodexBundle:cib-bundle':'https:\/\/r.bing.com\/rp\/WcA8kpHaW-sHs2e3ssIP7XgMCNQ.br.js'},{'A:rms:answers:SharedStaticAssets:speech-sdk':'https:\/\/r.bing.com\/rp\/bll21ZO27j3KPE27uQBxt24c2Fw.br.js'},{'A:rms:answers:Web:ChatHomeScreenBase':'https:\/\/r.bing.com\/rp\/t6Tz4UURXPD2SmtEEPGLoJpq69g.br.js'},{'A:rms:answers:Web:ChatHomeScreen':'https:\/\/r.bing.com\/rp\/x05EBLDKo9Gp0rV1W9OmmpoFp0o.br.js'},{'A:rms:answers:Web:SydneyWelcomeScreenBase':'https:\/\/r.bing.com\/rp\/ClBB7fdm5DZrrkATnoyfumVXhIs.br.js'},{'A:rms:answers:Web:GPTHomeScreen':'https:\/\/r.bing.com\/rp\/KjcvlNLXDDhheWsXAO4G_Y4V6MM.br.js'},{'A:rms:answers:Web:SydneyFullScreenConv':'https:\/\/r.bing.com\/rp\/kOT4O7p1vyMEheo3RqKUPANGLDQ.br.js'},{'A:rms:answers:Notifications:BnpPartner':'https:\/\/r.bing.com\/rp\/2DyrZWV35Nk6it3meHG_C7ZjJ8M.br.js'},{'A:9':9},{'A:rms:answers:Web:SydneyConvModeBackfacePatch':'https:\/\/r.bing.com\/rp\/wkWt7BtQdqUJkCPKQdJdk548UFA.br.js'},{'A:10':10},{'A:rms:answers:Web:SydFullScreenConvResourcesPostLoad':'https:\/\/r.bing.com\/rp\/Jnh8f1BaqA6QhGEDPml3FtXpFbY.br.js'},{'A:rms:answers:SegmentFilters:DropDown':'https:\/\/r.bing.com\/rp\/nc60aT-MXWFDGmlflZLjNBVVxkM.br.js'},{'A:rms:answers:Web:DnsExperimentA':'https:\/\/r.bing.com\/rp\/CcMXS8Oo0OUnUE0LzYK9AFJ6la8.br.js'},{'A:rms:answers:WebResult:ExtendIconClickTargets':'https:\/\/r.bing.com\/rp\/Y_vP5bket1OE26V4d4L4FHDnP4w.br.js'},{'A:rms:answers:WebResult:CaptionClickable':'https:\/\/r.bing.com\/rp\/hulMy94NWe4P3UsIN3zt_iGS9n8.br.js'},{'A:11':11},{'A:rms:answers:WebResult:WebResultToolbox':'https:\/\/r.bing.com\/rp\/ac0si7-jRRg7fGLyaXMYocX1U40.br.js'},{'A:rms:answers:Shared:Swipe':'https:\/\/r.bing.com\/rp\/2pI-3yxS71qnL6vzhVIltDQouTg.br.js'},{'A:rms:answers:Shared:Orientation':'https:\/\/r.bing.com\/rp\/wNhUjm3kl_kvyfrio44J6j1zdYo.br.js'},{'A:rms:answers:Shared:TabControl':'https:\/\/r.bing.com\/rp\/WjLJz0ZZ3W6qclUa_RsS6VdZFzE.br.js'},{'A:12':12},{'A:rms:answers:RelatedPageRecommendations:Shared':'https:\/\/r.bing.com\/rp\/QGgDCAP4Fmzft4oE0wUK8k18tLI.br.js'},{'A:rms:answers:RelatedPageRecommendations:Clickback':'https:\/\/r.bing.com\/rp\/BDwYWcRQC1NNdqbnczZFTEPNiGk.br.js'},{'A:13':13},{'A:rms:answers:Multimedia:VideoSerpAnswer':'https:\/\/r.bing.com\/rp\/8aj_I6fSAQ2HauP0CPPAfDwa2j8.br.js'},{'A:14':14},{'A:rms:answers:Shared:Pager':'https:\/\/r.bing.com\/rp\/RvRBoZ5KQDNHwbHfo-_ZBZIoYQo.br.js'},{'A:rms:answers:Shared:Chevrons':'https:\/\/r.bing.com\/rp\/5f8STjRzdjQ-8jgF3Ho7ptcTR94.br.js'},{'A:rms:answers:Shared:DeviceDetectionHelper':'https:\/\/r.bing.com\/rp\/9tfFg6vO9uS7FxceSoXBYqA1DZY.br.js'},{'A:rms:answers:Shared:SlideExperienceLite':'https:\/\/r.bing.com\/rp\/oGyDh-oDLn6IkPalHPWMedvSDQA.br.js'},{'A:rms:answers:MultimediaComponents:VideoTileVisualComponent':'https:\/\/r.bing.com\/rp\/LTCT2zdUcB3ayDDUpC7BI5zxXuE.br.js'},{'A:15':15},{'A:ProgressiveImages':'https:\/\/r.bing.com\/rs\/7d\/t5\/jnc,nj\/2RFgnacsz6nPw9vvxd8AGFyaQr8.js?or=w'},{'A:16':16},{'A:rms:answers:Multimedia:MMUtilsDefinition':'https:\/\/r.bing.com\/rp\/HdiojNH85n4iu87NAQvDH5bKMnM.br.js'},{'A:rms:answers:Multimedia:MMUtilsStyle':'https:\/\/r.bing.com\/rp\/ciI4DrhPUNmhXgxM4MW52bFXjfw.br.js'},{'A:rms:answers:MultimediaComponents:VideoRichHoverUtils':'https:\/\/r.bing.com\/rp\/ukaQhuYQyTz3agbHIeyBRoBxhmE.br.js'},{'A:rms:answers:Multimedia:MMUtilsEvent':'https:\/\/r.bing.com\/rp\/WPedHfV8dQrn4qkif5azDZSqmn8.br.js'},{'A:rms:answers:MultimediaComponents:VideoRichHoverEnumsAndConstants':'https:\/\/r.bing.com\/rp\/0UgTj-oiRnDKC7d-RWXdk9x4j00.br.js'},{'A:rms:answers:MultimediaComponents:ThumbnailPlayerOverlayComponent':'https:\/\/r.bing.com\/rp\/pYx84E5QxKsDa_GCjDkQj38YO0k.br.js'},{'A:rms:answers:MultimediaComponents:Html5VideoSMTPlayer':'https:\/\/r.bing.com\/rp\/kNUdPzzLEbQzYr3icm3MTxwk6-Y.br.js'},{'A:rms:answers:MultimediaComponents:VolumeIconWithSliderBar':'https:\/\/r.bing.com\/rp\/y9cMttd-SwSaYuiqhqk701fxHs0.br.js'},{'A:rms:answers:Multimedia:MMUtilsQueryAndUrl':'https:\/\/r.bing.com\/rp\/5F86_iDyVt4Fjf1Blaqa2W_2tsA.br.js'},{'A:rms:answers:MultimediaComponents:ThumbnailUrlConsts':'https:\/\/r.bing.com\/rp\/MCwdqGFTwqu20jt3177s57V4wZk.br.js'},{'A:rms:answers:Multimedia:THUrlGenerator':'https:\/\/r.bing.com\/rp\/e5E3S-lkItFHIqVf6KjzTWPZb6Y.br.js'},{'A:rms:answers:MultimediaComponents:ThumbnailPlayerComponent':'https:\/\/r.bing.com\/rp\/q3vkafrVjv12grVkPQlJmLegna0.br.js'},{'A:rms:answers:MultimediaComponents:CaptionContainerComponent':'https:\/\/r.bing.com\/rp\/mbuFLljzSy6Z7Kc_zphPXIgvoh0.br.js'},{'A:rms:answers:MultimediaComponents:VideoRichHoverInstrumentation':'https:\/\/r.bing.com\/rp\/4CqZEDbQQNo8pxNGAY-pz2gS6TM.br.js'},{'A:rms:answers:MultimediaComponents:VideoRichHoverBundle':'https:\/\/r.bing.com\/rp\/BnykzSrhLLq7vL0UKHbUNqrGDVk.br.js'},{'A:17':17},{'A:rms:answers:WholePageTemplate:WholePageTemplateBingChat':'https:\/\/r.bing.com\/rp\/SWO_18i1n--w9RKt8Yv8_xoEWXo.br.js'},{'A:rms:answers:Shared:LGWidget':'https:\/\/r.bing.com\/rp\/IZJX7X52Yk3KGeZJqEoHg_kDPy0.br.js'},{'A:rms:answers:QuerySuggestion:RelatedSearchesLGW':'https:\/\/r.bing.com\/rp\/q7xUNH4DS00Oj5oZJnr72rw3NsI.br.js'},{'A:18':18},{'A:rms:answers:QuerySuggestion:ClickBackRS':'https:\/\/r.bing.com\/rp\/mSXQPT7e1TlMt8h0fagSrjh90gY.br.js'},{'A:19':19},{'A:rms:answers:Pagination:PaginationChatButton':'https:\/\/r.bing.com\/rp\/1Fv2ZQvZTbMVx7KoU_6WkFwvTJ4.br.js'},{'A:rms:answers:VisualSystem:SearchFloatingActionButtonTriggering':'https:\/\/r.bing.com\/rp\/y1tiMssL1_ZRGIkBjxDYmR2kX8o.br.js'},{'A:rms:answers:VisualSystem:FloatingActionButtonCore':'https:\/\/r.bing.com\/rp\/8w26ODmd1hk4C30WJtfkdBYFSfE.br.js'},{'A:rms:answers:VisualSystem:SearchFloatingActionButton':'https:\/\/r.bing.com\/rp\/VJnSxYcv7TZB-im7xvuwo7wcIGs.br.js'},{'A:rms:answers:VisualSystem:LargerClickTarget':'https:\/\/r.bing.com\/rp\/uiannz55FdT0j3p9jGwegfI5aIY.br.js'},{'A:rms:answers:VisualSystem:IPv6Test':'https:\/\/r.bing.com\/rp\/eKvcHdnNwo1WcxoSioV4ztnfZk8.br.js'},{'A:20':20},{'A:rms:answers:Web:SerpKeyboardNavigation_Selector':'https:\/\/r.bing.com\/rp\/ID-70CBAEOXh6Nwxga-CxgpUq4k.br.js'},{'A:rms:answers:Web:SerpKeyboardNavigation':'https:\/\/r.bing.com\/rp\/ydDuUFvQrnTEDpvE14Ya7abrPGk.br.js'},{'A:21':21});;
//]]></script><div id="aRmsDefer"><div style="display:none" "><!--//<![CDATA[
0;var ErrFuncs;(function(n){function i(n,i,r){t("Error",n,i,r)}function r(n,i,r){t("Warning",n,i,r)}function t(n,t,i,r){var u,f;if(r?(u=r,u.message+=" "+t):u=new Error(t),u.extra="manualError",i&&(u.extra+=" "+i),u.severity=n,f=_w.jsErrorHandler,f)f(u);else throw u;}n.LogError=i;n.LogWarning=r})(ErrFuncs||(ErrFuncs={}));JS={LogError:ErrFuncs.LogError,LogWarning:ErrFuncs.LogWarning};SharedLogHelper={LogError:ErrFuncs.LogError,LogWarning:ErrFuncs.LogWarning,LogFatalError:function(){return null},RegisterErrorFilter:function(){return null}};sj_be(_d.body, 'load', function(){if(_w.lb)lb();}, false);;var Lib;(function(n){var t;(function(n){function u(n,t){var r,i;if(t==null||n==null)throw new TypeError("Null element passed to Lib.CssClass");if(n.indexOf)return n.indexOf(t);for(r=n.length,i=0;i<r;i++)if(n[i]===t)return i;return-1}function f(n,u){if(n==null)throw new TypeError("Null element passed to Lib.CssClass. add className:"+u);if(!r(n,u))if(i&&n.classList)n.classList.add(u);else{var f=t(n)+" "+u;o(n,f)}}function e(n,f){var e,s,h;if(n==null)throw new TypeError("Null element passed to Lib.CssClass. remove className:"+f);r(n,f)&&(i&&n.classList?n.classList.remove(f):(e=t(n).split(" "),s=u(e,f),s>=0&&e.splice(s,1),h=e.join(" "),o(n,h)))}function s(n,t){if(n==null)throw new TypeError("Null element passed to Lib.CssClass. toggle className:"+t);i&&n.classList?n.classList.toggle(t):r(n,t)?e(n,t):f(n,t)}function r(n,r){var f,e;if(n==null)throw new TypeError("Null element passed to Lib.CssClass. contains className:"+r);return i&&n.classList?n.classList.contains(r):(f=t(n),f)?(e=f.split(" "),u(e,r)>=0):!1}function h(n,i){var f,e,r,u,o;if(n.getElementsByClassName)return n.getElementsByClassName(i);for(f=n.getElementsByTagName("*"),e=[],r=0;r<f.length;r++)u=f[r],u&&(o=t(u),o&&o.indexOf(i)!==-1&&e.push(u));return e}function o(n,t){n instanceof SVGElement?n.setAttribute("class",t):n.className=t}function t(n){return n instanceof SVGElement?n.getAttribute("class"):n.className}var i=typeof document.body.classList!="undefined";n.add=f;n.remove=e;n.toggle=s;n.contains=r;n.getElementByClassName=h;n.getClassAttribute=t})(t=n.CssClass||(n.CssClass={}))})(Lib||(Lib={}));var QuickSearch;(function(n){n.isCursor=!0})(QuickSearch||(QuickSearch={}));var QuickSearch;(function(n){n.isQuickChat=!0;n.enableNonSearchVert=!0})(QuickSearch||(QuickSearch={}));var FocusScrollToBTop;(function(){function i(){var i=t()-n,r,u;i>AwayTimeThreshold&&((!AwayTimeThresholdCustomControl||AwayTimeThresholdCustomControl&&i>15)&&(AwayTimeScrollTopPoleRS&&(r=document.getElementsByClassName("b_adTop"),(!r||r.length<=0)&&(u=document.querySelector("#b_pole"),u&&(AwayTimeThresholdCustomControl||(u.style.display="block"),_w.sj_log&&sj_log("CI.ReturnTopPoleRS","ReturnTopPoleRS","Triggered")))),_w.scrollTo(0,0)),(i>AwayTimeThreshold&&i<=15||i<AwayTimeThreshold&&i>15)&&_w.sj_log&&sj_log("CI.ReturnTopLog","ReturnTop"+AwayTimeThreshold.toString(),"Triggered"),_w.sj_log&&sj_log("CI.ReturnTop","ReturnTop"+AwayTimeThreshold.toString(),"Triggered"),sj_evt.fire("focusTopScrollEnd"))}function r(){n=t()}function t(){return Math.round(Date.now()/1e3)}var n;sj_evt.bind("focusTopScrollStart",i);sj_evt.bind("focusTopBlur",r);sj_be(_w,"visibilitychange",function(){sj_evt.fire("focusTopScrollStart")});sj_be(_w,"blur",function(){sj_evt.fire("focusTopBlur")})})(FocusScrollToBTop||(FocusScrollToBTop={}));var SkipToContent;(function(){function n(n,t){sj_log===null||sj_log===void 0?void 0:sj_log("CI.".concat(r),n,t!==null&&t!==void 0?t:"1")}function u(t){t&&t.key==="Tab"&&i&&(n("FirstAppear"),i=!1)}var r="SkipToContent",t=document.getElementById("b_skip_to_content"),i;t&&(t.onfocus=function(){n("Appear")},t.onblur=function(){n("Disappear")},t.onclick=function(t){var f,e,o,s,h,c,l,a,v,y,p,r,u,w;if(n("Click"),t.preventDefault(),r=document.querySelectorAll("#b_results h2 a"),document.body.classList.contains("b_sydConvMode")&&((f=_G===null||_G===void 0?void 0:_G.EF)===null||f===void 0?void 0:f.chatskip2content)===1){var i=(o=(e=document.querySelector("cib-serp"))===null||e===void 0?void 0:e.shadowRoot)===null||o===void 0?void 0:o.querySelector("cib-conversation"),b=(h=(s=document.querySelector("cib-serp"))===null||s===void 0?void 0:s.querySelector(".b_wlcmCont"))===null||h===void 0?void 0:h.querySelectorAll(".b_wlcmTileWrap .b_wlcmTile"),k=(p=(y=(v=(a=(l=(c=document.querySelector("cib-serp"))===null||c===void 0?void 0:c.shadowRoot)===null||l===void 0?void 0:l.querySelector("cib-conversation"))===null||a===void 0?void 0:a.shadowRoot)===null||v===void 0?void 0:v.querySelector("cib-welcome-container"))===null||y===void 0?void 0:y.shadowRoot)===null||p===void 0?void 0:p.querySelectorAll(".legal-items a");i&&b&&(i===null||i===void 0?void 0:i.getAttribute("chat-type"))=="consumer"?r=b:i&&k&&(i===null||i===void 0?void 0:i.getAttribute("chat-type"))!=="consumer"&&(r=k)}for(u=0;u<r.length;u++)if(w=r[u],w.offsetParent){w.focus();return}});i=!0;sj_be(_d.body,"keydown",u)})(SkipToContent||(SkipToContent={}));var _vs = { sboxtgt: "/opaluqu", anon: false, locstr:{ wait:"Esperando...", listen: false ?"Empezar a hablar para buscar...":"Escuchando...", block: false ?"No podemos acceder a tu micrófono. Comprueba la configuración de tu navegador o dispositivo.":"Permisos de micrófono rechazados, comprueba la configuración del explorador.", mictt:"Buscar con la voz", error: false ?"No lo hemos recibido. Comprueba la configuración del micrófono o el audio.":"Se ha producido un problema al detectar el audio.", notext: false ?"No hemos entendido eso. ¿Puedes intentarlo de nuevo?":"No se ha detectado ninguna voz.", blockedtitle:"Micrófono bloqueado", blockeddesc:"Se ha bloqueado el acceso al micrófono para esta página.", blockedfix:"Para permitir que Bing acceda a su micrófono, haga clic en el icono del micrófono bloqueado.", blockeddismiss:"Descartar", nomic:"No se encontró ningún dispositivo de micrófono.", nomicLinkText:"Configuración de un micrófono", nomicQuery:"Configurar el micrófono", overlayText:"Haz clic en \u0022Permitir\u0022 para habilitar la búsqueda por voz", micAllowTitle:"¿Quieres buscar con tu voz?", micAllowDesc:"Selecciona Permitir para permitir que Bing use el micrófono", micReTitle:"La búsqueda por voz está desactivada", micReDesc:"Para encenderlo", micReList1:"Haz clic en el botón de micrófono de la barra de direcciones del navegador", micReList2:"Seleccione Permitir Siempre para permitir que Bing use su micrófono", upsellHeader:"Intenta buscar con tu voz", upsellSubheader:"Haz clic en el micrófono y di...", upsellsuggestion0:"Tiempo mañana", upsellsuggestion1:"¿Cuándo se pone el sol hoy?", upsellsuggestion2:"¿Cómo se deletrea paralelogramo?", upsellsuggestion3:"¿Qué significa la palabra incrédulo?", upsellsuggestion4:"¿Qué hora es en Beijing?", upsellsuggestion5:"¿Qué idiomas se hablan en Mozambique?", upsellsuggestion6:"¿Cuántas botellas de plástico se utilizan en un año?", upsellsuggestion7:"¿Cómo se dice hola en japonés?", upsellsuggestion8:"¿Cuánto son 2000 dólares en euros?", upsellsuggestion9:"¿Cuántos metros son 3 pies?", speechSuggestionAria:"Sugerencia de búsqueda", helloQuery:"Hola", learnMoreTooltip:"Buscar mediante voz.", learnMoreTooltipLink:"Más información", learnMoreTitle:"¡Busca con tu voz!", learnMoreInst:"Prueba a decir,", learnMoreExpQuery:"\u0022Cómo decir hola en chino\u0022", personalVoiceTooltipTitle:"Voz personal habilitada", personalVoiceTooltipDescription:"Esta voz sintética fue creada por {0}", personalVoiceTooltipClose:"Cerrar", personalVoiceTooltipEnabledText:"Los resultados de la voz se leerán con una voz sintética creada por {0}", personalVoiceTooltipExampleQuery:"\u0022¿Cuántas onzas hay en un galón?\u0022", personalVoiceTooltipSettingsText:"Administra la voz de lectura en Configuración", }, mobile: false, reopenmic: true, ttsFromSsmlEnabled: false, permRequestOverlayEnabled: true, allowForceQuery: true, enablePersonalVoice: false, enableSecondTooltip: false, enablePersonalVoiceUrlParse: true, mockPersonalVoice: 0, forceHelloQuery: false, testOverlay:"", enableVoiceSRDomain:true, micFabAlwaysVisible: false, spokenResponseTypeLogEnabled: true, noSpeechDetectedReopenMic: true, showSuggestion: false, mockSuggestion: false, enablePreloadMic: true, enableShowMicOnSBClick: false, testQuery: false, enableGMicIcon: false, enableFluentMicIcon: false, isDarkMode: false, enableSydConv: false, voiceFilesPath:"/vs/ec/", setEarconsOnClick: true };;var micCont = document.getElementById('mic_cont_icon'); if (micCont) { micCont.addEventListener('click', MicLoad.handleMicClick); };(function(){function r(n,r){var h,f,e,o,s;t&&i&&(t.value=n,typeof r!="undefined"&&r&&r.sboxtgt&&(t.form.action=r.sboxtgt),h=typeof r!="undefined"&&r&&r.sboxtgt?r.sboxtgt:"/search",f=window.location.protocol+"//"+window.location.host+h+"?q="+encodeURIComponent(n)+u(i),URLSearchParams&&(e=new URLSearchParams(window.location.search),o=e.get("location"),o&&(f+="&location="+o),s=e.get("translatelg"),s&&(f+="&translatelg="+s)),(_G===null||_G===void 0?void 0:_G.IG)&&(f+="&uqurequestid="+_G.IG),window.location.href=f)}function u(n){for(var r="",i=n.getElementsByTagName("input"),t=0;t<i.length;t++)i[t].type==="hidden"&&(r+="&"+i[t].name+"="+encodeURIComponent(i[t].value));return r}var n=_d.querySelector("#sb_form .mic_cont"),t=document.querySelector("#sb_form_q"),i=document.querySelector("#sb_form");n&&(n.handleText=r,n.iconInit=function(){},n.LoggingName="speech",n.TextareaId="#sb_form_q",n.clientbuild="bing",n.shouldReadoutPage=!0)})();var SydneyFSCLocationHelper;(function(n){function t(n){n&&typeof n.locations!="undefined"&&typeof n.locations.locationsList!="undefined"&&n.locations.locationsList.length>0&&typeof CIB!="undefined"&&typeof CIB.config!="undefined"&&typeof CIB.config.sydney!="undefined"&&typeof CIB.config.sydney.message!="undefined"&&(CIB.config.sydney.message.locationHints=n.locations.locationsList)}n.writeToLocationHints=t})(SydneyFSCLocationHelper||(SydneyFSCLocationHelper={}));var __awaiter=this&&this.__awaiter||function(n,t,i,r){function u(n){return n instanceof i?n:new i(function(t){t(n)})}return new(i||(i=Promise))(function(i,f){function o(n){try{e(r.next(n))}catch(t){f(t)}}function s(n){try{e(r["throw"](n))}catch(t){f(t)}}function e(n){n.done?i(n.value):u(n.value).then(o,s)}e((r=r.apply(n,t||[])).next())})},__generator=this&&this.__generator||function(n,t){function o(n){return function(t){return s([n,t])}}function s(o){if(e)throw new TypeError("Generator is already executing.");while(f&&(f=0,o[0]&&(r=0)),r)try{if(e=1,u&&(i=o[0]&2?u["return"]:o[0]?u["throw"]||((i=u["return"])&&i.call(u),0):u.next)&&!(i=i.call(u,o[1])).done)return i;(u=0,i)&&(o=[o[0]&2,i.value]);switch(o[0]){case 0:case 1:i=o;break;case 4:return r.label++,{value:o[1],done:!1};case 5:r.label++;u=o[1];o=[0];continue;case 7:o=r.ops.pop();r.trys.pop();continue;default:if(!(i=r.trys,i=i.length>0&&i[i.length-1])&&(o[0]===6||o[0]===2)){r=0;continue}if(o[0]===3&&(!i||o[1]>i[0]&&o[1]<i[3])){r.label=o[1];break}if(o[0]===6&&r.label<i[1]){r.label=i[1];i=o;break}if(i&&r.label<i[2]){r.label=i[2];r.ops.push(o);break}i[2]&&r.ops.pop();r.trys.pop();continue}o=t.call(n,r)}catch(s){o=[6,s];u=0}finally{e=i=0}if(o[0]&5)throw o[1];return{value:o[0]?o[1]:void 0,done:!0}}var r={label:0,sent:function(){if(i[0]&1)throw i[1];return i[1]},trys:[],ops:[]},e,u,i,f;return f={next:o(0),"throw":o(1),"return":o(2)},typeof Symbol=="function"&&(f[Symbol.iterator]=function(){return this}),f},__spreadArray=this&&this.__spreadArray||function(n,t,i){if(i||arguments.length===2)for(var r=0,f=t.length,u;r<f;r++)!u&&r in t||(u||(u=Array.prototype.slice.call(t,0,r)),u[r]=t[r]);return n.concat(u||Array.prototype.slice.call(t))},PersonaGPTHandler;(function(n){function h(){var n,i,f,o,s;if(u=SydFSCHelper.getConfigOrDefault((n=_w._sydConvConfig)===null||n===void 0?void 0:n.enableWelcomeScreenV2,!1),r=SydFSCHelper.getConfigOrDefault((i=_w._sydConvConfig)===null||i===void 0?void 0:i.enableWelcomeScreenV3,!1),l(),o=(f=_w._sydMobNativeIntegration)===null||f===void 0?void 0:f.enablePersonaBack,o&&sj_be(_w,"popstate",function(){var n,i,r,u;((i=(n=CIB===null||CIB===void 0?void 0:CIB.vm)===null||n===void 0?void 0:n.personas)===null||i===void 0?void 0:i._isPersonaView)&&((u=(r=CIB===null||CIB===void 0?void 0:CIB.vm)===null||r===void 0?void 0:r.personas)===null||u===void 0?void 0:u.setPersona(t))}),CIB.onAIPersonaModeChanged){s=CIB.responseTone;CIB.onAIPersonaModeChanged(function(n){n?(s=CIB.responseTone,CIB.responseTone===SydFSCHelper.PRECISE_TONE&&(CIB.responseTone=SydFSCHelper.BALANCED_TONE)):CIB.responseTone=s})}if(CIB.onSelectedPersonaChanged)CIB.onSelectedPersonaChanged(function(n){var i,f,s,h,c,l,a,v,y,p,w,b,k,d,g;n===t?e.observe((h=(s=(f=(i=_d.querySelector("cib-serp"))===null||i===void 0?void 0:i.shadowRoot)===null||f===void 0?void 0:f.querySelector("cib-conversation"))===null||s===void 0?void 0:s.shadowRoot)===null||h===void 0?void 0:h.querySelector("cib-welcome-container"),{attributeOldValue:!0}):r?(a=(l=(c=_d.querySelector("cib-serp"))===null||c===void 0?void 0:c.shadowRoot)===null||l===void 0?void 0:l.querySelector("cib-conversation"))===null||a===void 0?void 0:a.querySelectorAll('slot[name="wlcmCntnr"]').forEach(function(n){return n.remove()}):u&&((p=(y=(v=_d.querySelector("cib-serp"))===null||v===void 0?void 0:v.shadowRoot)===null||y===void 0?void 0:y.querySelector("cib-conversation"))===null||p===void 0?void 0:p.querySelectorAll('slot[name="wlcmContHeader"]').forEach(function(n){return n.remove()}));o&&(((w=_w===null||_w===void 0?void 0:_w.history)===null||w===void 0?void 0:w.state)?n===t?(b=_w===null||_w===void 0?void 0:_w.history)===null||b===void 0?void 0:b.back():(k=_w===null||_w===void 0?void 0:_w.history)===null||k===void 0?void 0:k.replaceState({newPersona:n},""):((d=_w===null||_w===void 0?void 0:_w.history)===null||d===void 0?void 0:d.state)||n===t||((g=_w===null||_w===void 0?void 0:_w.history)===null||g===void 0?void 0:g.pushState({newPersona:n},"")));SydneySuperAppLogger.logCriticalInfo("onSelectedPersonaChanged",{sysMsg:n})})}function c(n){for(var t,i=0,f=n;i<f.length;i++)t=f[i],(t===null||t===void 0?void 0:t.type)==="attributes"&&t.attributeName==="persona"&&t.oldValue===""&&(r?sj_evt.fire("cib_init",_d.querySelector("cib-serp")):u&&SydWelcomeScreenMobile.setContent(_d.querySelector("cib-serp")),e.disconnect())}function l(){return __awaiter(this,void 0,void 0,function(){var n,e,o,u,s,r;return __generator(this,function(h){switch(h.label){case 0:return i("setAIPersonaConfigs",{sysMsg:"started"}),CIB.config.features.enablAIPersonasForMobile=!0,CIB.config.features.enableAIPersonasForMobile=!0,[4,a()];case 1:return n=h.sent(),e=p(),o=w(),u=__spreadArray(__spreadArray([],e,!0),o,!0),CIB.setPersonaItemsData?(i("setAIPersonaConfigs",{sysMsg:"Set AI Persona data from OS new method"}),r=f(u,n),s={personaItems:r,chatMode:t},CIB.setPersonaItemsData(s)):CIB.setAIPersonaData?(i("setAIPersonaConfigs",{sysMsg:"Set AI Persona data from OS old method"}),r=f(u,n),CIB.setAIPersonaData(r)):i("setAIPersonaConfigs",{sysMsg:"Fallback to the data from default AI Personas Config",payload:{reason:"Empty data from OS",dataFromOS:n}}),[2]}})})}function a(){var n,t,r,u;return __awaiter(this,void 0,void 0,function(){var e,s,f,h,c;return __generator(this,function(l){switch(l.label){case 0:i("getAIPersonasFromOS",{sysMsg:"started"});l.label=1;case 1:return(l.trys.push([1,5,,6]),!((n=_w._sydConvConfig)===null||n===void 0?void 0:n.enablePersonaTestData))?[3,2]:(s=(t=_w.sapphireBridge)===null||t===void 0?void 0:t.getPersona1TestData(),[2,s]);case 2:return[4,SydFSCHelper.createRequest(o(),"","GET")];case 3:return e=l.sent(),f=JSON.parse(e.response),h=(u=(r=f===null||f===void 0?void 0:f.gptListResponse)===null||r===void 0?void 0:r.personagpts)!==null&&u!==void 0?u:[],[2,h];case 4:return[3,6];case 5:return c=l.sent(),b("getAIPersonasFromOS",{sysMsg:"Error while parsing response",err:c,data:e}),[2,[]];case 6:return[2]}})})}function v(){var n,t,i,r,u;return __awaiter(this,void 0,void 0,function(){var s,h,e,c,l,a;return __generator(this,function(v){switch(v.label){case 0:return(v.trys.push([0,6,,7]),!((n=_w._sydConvConfig)===null||n===void 0?void 0:n.enableAIPersonasForDesktop))?[3,4]:(SydFSCHelper.SydLog("SystemEvent","GetPersonasStarted","PersonaGPTHandler",{}),!((t=_w._sydConvConfig)===null||t===void 0?void 0:t.enablePersonaTestData))?[3,1]:(s=(i=_w.sapphireBridge)===null||i===void 0?void 0:i.getPersona1TestData(),[2,s]);case 1:return[4,SydFSCHelper.createRequest(o(),"","GET")];case 2:return h=v.sent(),e=JSON.parse(h.response),c=(u=(r=e===null||e===void 0?void 0:e.gptListResponse)===null||r===void 0?void 0:r.personagpts)!==null&&u!==void 0?u:[],l=f([],c),[2,l];case 3:return[3,5];case 4:return[2,[]];case 5:return[3,7];case 6:return a=v.sent(),SydFSCHelper.SydLog("SystemEvent","GetPersonasParseError","PersonaGPTHandler",{}),[2,[]];case 7:return[2]}})})}function y(n){var t;((t=_w._sydConvConfig)===null||t===void 0?void 0:t.enableAIPersonasForMobile)&&CIB.selectedPersona&&n&&i("Persona conversation id",{payload:{conversationId:n!==null&&n!==void 0?n:"-",selectedPersona:CIB.selectedPersona}})}function o(){var n,t,i=new URL("sydchat/readTopicsInfo",window.location.origin);return i.searchParams.append("lang",(t=(n=_G.Lang)===null||n===void 0?void 0:n.split("-")[0])!==null&&t!==void 0?t:"en"),i.searchParams.append("personas","primary"),i.href}function p(){var n,i,r,u,f,e,o,s,h,c,d=(n=_w._sydConvTranslation)===null||n===void 0?void 0:n.personaBalancedDescription,l=(i=_w._sydConvTranslation)===null||i===void 0?void 0:i.personaBalancedSuggestionChip1,a=(r=_w._sydConvTranslation)===null||r===void 0?void 0:r.personaBalancedSuggestionChip2,v=(u=_w._sydConvTranslation)===null||u===void 0?void 0:u.personaBalancedSuggestionChip3,y=(f=_w._sydConvTranslation)===null||f===void 0?void 0:f.personaBalancedSuggestionChip4,g=(e=_w._sydConvTranslation)===null||e===void 0?void 0:e.personaCreativeDescription,p=(o=_w._sydConvTranslation)===null||o===void 0?void 0:o.personaCreativeSuggestionChip1,w=(s=_w._sydConvTranslation)===null||s===void 0?void 0:s.personaCreativeSuggestionChip2,b=(h=_w._sydConvTranslation)===null||h===void 0?void 0:h.personaCreativeSuggestionChip3,k=(c=_w._sydConvTranslation)===null||c===void 0?void 0:c.personaCreativeSuggestionChip4;return[{personaTitle:"Copilot",topic:t,description:g,prompt:"",optionsSets:[],suggestionChips:[p,w,b,k,],suggestions:[{text:p},{text:w},{text:b},{text:k},],avatarImageUrl:"https://www.bing.com/th?id=OEP.4859ABAF577E9841CBACD96A9B6AD457&pid=BEmail",responseType:SydFSCHelper.CREATIVE_TONE,defaultTone:SydFSCHelper.CREATIVE_TONE,supportedTones:[SydFSCHelper.CREATIVE_TONE,SydFSCHelper.BALANCED_TONE]},{personaTitle:"Copilot",topic:t,description:d,prompt:"",optionsSets:[],suggestionChips:[l,a,v,y,],suggestions:[{text:l},{text:a},{text:v},{text:y},],avatarImageUrl:"https://www.bing.com/th?id=OEP.4859ABAF577E9841CBACD96A9B6AD457&pid=BEmail",responseType:SydFSCHelper.BALANCED_TONE,defaultTone:SydFSCHelper.BALANCED_TONE,supportedTones:[SydFSCHelper.CREATIVE_TONE,SydFSCHelper.BALANCED_TONE]},]}function w(){var r=[],u,t,i,n,f,e;if(typeof FreeSydneyHelper!="undefined"&&FreeSydneyHelper)for(u=FreeSydneyHelper.getFreeSydneyPersonasData().freeSydneyPersonaModels,t=0,i=u;t<i.length;t++)n=i[t],n.id!=="copilot"&&(f=n.welcomeTiles.map(function(n){return{text:n.text}}),e={personaTitle:n.displayName,description:n.welcomeHeader,shortDescription:n.description,defaultTone:n.defaultTone,responseType:n.defaultTone,avatarImageUrl:n.avatarImageUrl,supportedTones:n.supportedTones,optionsSets:n.extraOptionsSets,topic:n.id,prompt:"",suggestions:f},r.push(e));return r}function f(n,t){var f,r=[],u,e,i,o,h;if(r.push.apply(r,n),(t===null||t===void 0?void 0:t.length)>0)for(u=0,e=t;u<e.length;u++)i=e[u],o=i.suggestions.map(function(n){return n.text}),s(o),h=__spreadArray([],i.suggestions,!0),s(h),r.push({personaTitle:i.title,shortDescription:(f=i.shortDescription)!==null&&f!==void 0?f:i.description,description:i.description,topic:i.topic,prompt:i.prompt,optionsSets:i.optionsSets,suggestionChips:o.slice(0,4),suggestions:h.slice(0,4),avatarImageUrl:i.avatarImageUrl,responseType:i.defaultTone,defaultTone:i.defaultTone,supportedTones:i.supportedTones,actionBarMessage:i.actionBarMessage});return r}function s(n){for(var r,i,t=n.length-1;t>0;t--)r=Math.floor(Math.random()*(t+1)),i=[n[r],n[t]],n[t]=i[0],n[r]=i[1]}function i(n,t){typeof SydneySuperAppLogger!="undefined"&&SydneySuperAppLogger&&SydneySuperAppLogger.logInfo(n,t)}function b(n,t){typeof SydneySuperAppLogger!="undefined"&&SydneySuperAppLogger&&SydneySuperAppLogger.logError(n,t)}var t="default",e=new MutationObserver(c),r,u;n.SetupPersonaGPTs=h;n.getPersonas=v;n.logNewConversation=y})(PersonaGPTHandler||(PersonaGPTHandler={}));(function(){var n="b_hide",t=_ge("b_phead_chat"),i=_ge("chat_upsell_bubble_icon");t&&Lib.CssClass.contains(t,n)&&Lib.CssClass.remove(t,n);i&&Lib.CssClass.contains(i,n)&&Lib.CssClass.remove(i,n)})();var Identity = Identity || {}; (function(i) { i.wlImgSm ="https://storage.live.com/users/0x{0}/myprofile/expressionprofile/profilephoto:UserTileStatic/p?ck=1\u0026ex=720\u0026sid=2F8652FD12A56ECE034D46D0133C6F04\u0026fofoff=1"; i.wlImgLg ="https://storage.live.com/users/0x{0}/myprofile/expressionprofile/profilephoto:UserTileMedium/p?ck=1\u0026ex=720\u0026sid=2F8652FD12A56ECE034D46D0133C6F04\u0026fofoff=1";i.popupLoginUrls = {"WindowsLiveId":"https://login.live.com/login.srf?wa=wsignin1.0\u0026rpsnv=11\u0026ct=1708649398\u0026rver=6.0.5286.0\u0026wp=MBI_SSL\u0026wreply=https:%2F%2fwww.bing.com%2Fsecure%2FPassport.aspx%3Fpopup%3D1%26ssl%3D1\u0026lc=2058\u0026id=264960"}; })(Identity);;0;sj_evt.bind("onP1", function() { window["RewardsHeaderSVG"] && RewardsHeaderSVG.fireDefaultEvent(); }, 1, 0);;var bepcfg = bepcfg || {};;bepcfg.v =1;;bepcfg.wb =true? '1' : '0';;var __spreadArray=this&&this.__spreadArray||function(n,t,i){if(i||arguments.length===2)for(var r=0,f=t.length,u;r<f;r++)!u&&r in t||(u||(u=Array.prototype.slice.call(t,0,r)),u[r]=t[r]);return n.concat(u||Array.prototype.slice.call(t))},LightLogger;(function(n){function i(n,t,i,r,u){var e,f;if(u=u||{},u.FID="ModernRewardsFlyout",u.EventName=i,_w&&_w.Log2&&Log2.LogEvent&&_G.XLS)Log2.LogEvent("ClientInst",u,t),Log2.ForceFlush&&r&&Log2.ForceFlush(!0);else if(_w&&_w.Log&&Log.Log){if(e=["Fallback","1"],u&&u.hasOwnProperty)for(f in u)u.hasOwnProperty(f)&&(e.push(f?f.toString():""),e.push(u[f]?u[f].toString():""));Log.Log.apply(Log,__spreadArray([t,"ModernRewardsFlyout",i,r],e,!1))}}var t;(function(n){n.Click="Click";n.Init="Init";n.Load="Load";n.Success="Success";n.Error="Error"})(t=n.InstrumentationEventType||(n.InstrumentationEventType={}));n.logInstrumentationV2=i})(LightLogger||(LightLogger={}));var sch=sch||{};(function(){var n="click";sj_evt.bind("onP1",function(){setTimeout(function(){var i=_ge("id_h"),t=_ge("id_sc");i&&t&&(sj_jb("Blue/HamburgerServicesHeaderFlyout_c",0,i,"mouseover",t,n,t,"focus"),sj_be(t,n,function(n){sch.clk=n}))},50)},1)})();var SearchBox;(function(n){function u(){t=_ge("b_header");sj_be(_d.body,"click",i)}function i(n){var i=sj_et(n);i.id!="sb_form_q"&&(t.className=t.className.replace(r,""))}var r=/(^|\s)b_focus(?!\S)/gi,t;n.removeFocusClass=i;sj_evt.bind("onP1",u)})(SearchBox||(SearchBox={}));(function(){var t=!1,n=_ge("scope_tools_wrapper"),i=_ge("b_tween_searchResults"),r=_ge("b_tween_searchTools"),u=_ge("b_tween"),f=!1;n&&r&&u&&(f=Lib.CssClass.contains(u,"b_hidetoggletween"),Lib.CssClass.contains(r,"b_hide")||(Lib.CssClass.add(n,"active"),t=!0),Lib.CssClass.remove(n,"b_hide"),sj_be(n,"click",function(){t?(_w.sj_log&&sj_log("CI.tweenToggle","hideTools","1"),Lib.CssClass.remove(n,"active"),Lib.CssClass.add(r,"b_hide"),i&&Lib.CssClass.remove(i,"b_hide"),f&&Lib.CssClass.add(u,"b_hidetoggletween"),t=!1):(_w.sj_log&&sj_log("CI.tweenToggle","showTools","1"),Lib.CssClass.add(n,"active"),Lib.CssClass.remove(r,"b_hide"),i&&Lib.CssClass.add(i,"b_hide"),f&&Lib.CssClass.remove(u,"b_hidetoggletween"),t=!0)}))})();if (typeof (performance) !== 'undefined' && _G.BCT && performance.timing && performance.timing.navigationStart){ var SCArrivalTimeRelativeToNavStart = (_G.BCT - performance.timing.navigationStart); typeof (Log) !== 'undefined' && Log && Log.Log && Log.Log("SCArrST", "SCArrST", SCArrivalTimeRelativeToNavStart); };0;0;function getBrowserWidth_Desk(){var t=_d.documentElement,n=Math.round(_w.innerWidth||t.clientWidth);return n<100&&(n=1496),n}function getBrowserHeight_Desk(){var t=_d.documentElement,n=Math.round(_w.innerHeight||t.clientHeight);return n<100&&(n=796),n}function getBrowserScrollWidth_Desk(){var n=Math.round(_d.body.clientWidth);return n<100&&(n=1496),n}function getBrowserScrollHeight_Desk(){var n=Math.round(_d.body.clientHeight);return n<100&&(n=796),n}(function(){function st(n,t){var i="XW",r;n<=it&&(i="W");n<=rt&&(i="M");n<=ut&&(i="N");n<=ft&&(i="S");n<=et&&(i="HTP");n<=ot&&(i="NOTP");r="T";t<=g&&(r="M");t<=nt&&(r="S");c(tt,i,"width",n.toString());c(d,r,"height",t.toString())}function c(r,u,f,e){l("Info",r,u,f,e);t(n,r)!=u&&i(n,r,u,!0,"/",null)}function l(n,t,i,r,u){typeof Log!="undefined"&&Log.Log&&Log.Log(n,t,i,!1,r,u)}function f(n,t,i){typeof Log!="undefined"&&Log.Log&&Log.Log(n,t,i)}function r(r,u,e){var o=t(n,u);o&&o===r||(i(n,u,r,!0,"/",null),o&&e&&f("Info","BRResize",e))}function ht(){var e=t(n,"DPR"),r=_w.devicePixelRatio!=null&&!isNaN(_w.devicePixelRatio)?_w.devicePixelRatio.toFixed(1):"1",u,f;l("Info",w,r,"RawDPR",r);e&&parseFloat(e)===parseFloat(r)||i(n,"DPR",r,!0,"/",null);u=t(n,"UTC");f=((new Date).getTimezoneOffset()*-1).toString();(u==null||u!==f)&&i(n,"UTC",f,!0,"/",null)}function e(r,e){r!=e&&(f("Info",u,e),_w.DMMode=e);var o=t(n,y);t(n,u)!=e&&(i(n,u,e,!0,"/",null),o===p&&_w.location.reload())}function o(n,t,i){var r="(prefers-color-scheme: "+n+")";return _w.matchMedia(r).matches?(t!=i&&e(t,i),!0):!1}function a(){var n=_w.DMMode;_w.matchMedia?o("light",n,"0")||o("dark",n,"1")||o("no-preference",n,"2")||e(n,"3"):e(n,"4")}function ct(){_w[b]=0;_w[k]=0;v();ht();a()}function v(){lt();at()}function lt(){var n=getBrowserWidth_Desk(),t=getBrowserHeight_Desk();st(n,t);r(n.toString(),"CW","W");r(t.toString(),"CH","H");r(getBrowserScrollWidth_Desk().toString(),"SCW","");r(getBrowserScrollHeight_Desk().toString(),"SCH","")}function at(){var i=+t(n,"CW"),r=+t(n,"SCW");(i&&i!=getBrowserWidth_Desk()||r&&r!=getBrowserScrollWidth_Desk())&&f("Info","CookieCorrupt","1")}function vt(){var r=t(n,"CW"),u=t(n,"CH");r&&i(n,"PRVCW",r,!0,"/",null);u&&i(n,"PRVCH",u,!0,"/",null)}var i=sj_cook.set,t=sj_cook.get,n="SRCHHPGUSR",u="DM",y="WEBTHEME",p="2",w="DPR",b="WResizeCNT",k="WResizeTS",d="BRH",g=1e3,nt=700,tt="BRW",it=1480.9,rt=1346.9,ut=1257.9,ft=1164,et=1016,ot=868,s=_ge("b_context"),h=_ge("b_results");s!=null&&h!=null&&s.clientHeight>h.clientHeight&&_w.sj_log&&sj_log("CI.RightRail","Context","high");vt();ct();sj_be(_w,"resize",v);sj_be(_d,"visibilitychange",function(){_d.visibilityState==="visible"&&a()})})();window.ClientObserver={getBrowserWidth:getBrowserWidth_Desk,getBrowserHeight:getBrowserHeight_Desk,getBrowserScrollWidth:getBrowserScrollWidth_Desk,getBrowserScrollHeight:getBrowserScrollHeight_Desk};0;_w["_sydBaseSpecifierPrefix"] = "rms-answers-SharedStaticAssets-";;var AccessibilityHelpers;(function(n){function f(n){var r=i(n,!1,t);r!=null&&r.focus()}function i(n,i,r){var e,o,s,u,c,f,h;if(i===void 0&&(i=!1),r===void 0&&(r=t),e=null,n&&n.querySelectorAll)for(o=n.querySelectorAll(r),s=o.length,u=0;u<s;u++)if(c=i?s-1-u:u,f=o.item(c),f&&f.tabIndex!=-1&&(h=window.getComputedStyle(f),h.display!="none"&&h.visibility!="hidden")){e=f;break}return e}function e(n){var i=r(n,t);i!=null&&i.focus()}function r(n,i){var o,s,u,f,e,r,h,c;if(i===void 0&&(i=t),o=null,_d.querySelectorAll&&n){for(s=_d.querySelectorAll(i),u=[],r=0;r<s.length;r++)f=s.item(r),f&&f.tabIndex!=-1&&u.push(f);if(e=u.indexOf(n),typeof sj_log!="undefined"&&sj_log("CI.MoveFocus","Next",_d.activeElement.tagName+" "+e.toString()),e!=-1)for(r=0;r<u.length;r++)if(h=u[(e+r+1)%u.length],c=window.getComputedStyle(h),c.display!="none"&&c.visibility!="hidden"){o=h;break}}return o}function o(n,t,i){u(n,t,i,!1)}function u(n,t,i,r){n&&t&&typeof t.length=="number"&&i&&typeof i=="function"&&(sj_be(n,"keydown",function(n){for(var r=n.keyCode?n.keyCode:n.charCode,i=0;i<t.length;i++)if(r===t[i]){sj_pd(n);break}}),sj_be(n,"keyup",function(n){for(var f=n.keyCode?n.keyCode:n.charCode,u=0;u<t.length;u++)if(f===t[u]){r?i(n):i();sj_pd(n);break}}))}var t="a[href], body, input, select, [tabindex]";n.focusFirstFocusableElementWithin=f;n.getFocusableElementWithin=i;n.focusNextFocusableElement=e;n.getNextFocusableElement=r;n.activateButtonByKey=o;n.activateButtonByKeyWithEvent=u})(AccessibilityHelpers||(AccessibilityHelpers={}));_w["_sydPayWallConfig"] ={"loadSydneyConvResWithPayWall":false,"useSydneyPayWall":false};;var Feedback;(function(n){var t;(function(){"use strict";function u(t,i){var u=t.getAttribute("id"),f;u||(u="genId"+n.length,t.setAttribute("id",u));f=new r(u,i,t.getAttribute(i));n.push(f)}function i(n,t,i){i===null?n.removeAttribute(t):n.setAttribute(t,i)}function t(n,t,r,f){for(var e,s=_d.querySelectorAll(r),o=0;o<s.length;o++)(e=s[o],f&&e.id&&f[e.id])||(u(e,n),i(e,n,t))}function f(n){for(var u=_d.querySelectorAll(n),e=1,f={},t,i,r=0;r<u.length;++r){if(t=u[r],!t.id){for(;;)if(i="fbpgdgelem".concat(e++),!_ge(i))break;t.id=i}f[t.id]=t}return f}function e(){var i="tabindex",r="-1",n=f("#fbpgdg, #fbpgdg *");t(i,r,"div",n);t(i,r,"svg",n);t(i,r,"a",n);t(i,r,"li",n);t(i,r,"input",n);t(i,r,"select",n);t("aria-hidden","true","body :not(script):not(style)",n)}function o(){for(var r,t=0;t<n.length;t++)r=_d.getElementById(n[t].id),r&&i(r,n[t].attributeName,n[t].originalAttributeValue);n.length=0}function s(){typeof sj_evt!="undefined"&&(sj_evt.bind("onFeedbackStarting",function(){e()}),sj_evt.bind("onFeedbackClosing",function(){o()}))}var n=[],r=function(){function n(n,t,i){this.id=n;this.attributeName=t;this.originalAttributeValue=i}return n}();s()})(t=n.Accessibility||(n.Accessibility={}))})(Feedback||(Feedback={}));var Feedback;(function(n){var t;(function(t){function u(t,r,u,f,e,o){t=typeof t===i?!1:t;t&&scrollTo(0,0);u=typeof u===i?!0:u;n.PackageLoad.Load(r,u,f,e,o)}function o(n,t){for(var r=0,i=null;n&&n.getAttribute&&(!(t>=1)||r<t);){if(i=n.getAttribute("data-fbhlsel"),i!=null)break;r++;n=n.parentNode}return i}function a(t,a,v,y,p,w,b,k,d){function ot(t){var r=null,i;return t&&(i=new c,n.fel("ajax.feedback.collectsettings","gsf",i),r=i.findSettings(t)),r}var it,tt,nt,et,rt,g,ut,ft;(sj_cook&&(tt=sj_cook.get("fdbk_acc","tabfocus"),tt&&(nt=_ge(tt),nt?(nt.focus(),sj_cook.clear("fdbk_acc","/")):(nt=document.querySelector('[id^="'+tt+'"]'),nt?(nt.focus(),sj_cook.clear("fdbk_acc","/")):(et=tt.indexOf("thumb_f")!=-1&&a.indexOf("thumb_f")!=-1||tt.indexOf("thumb_t")!=-1&&a.indexOf("thumb_t")!=-1,et&&(nt=_ge(a),rt=(it=nt===null||nt===void 0?void 0:nt.parentElement)===null||it===void 0?void 0:it.parentElement,rt&&rt.classList.contains("l_ecrd_tud")&&(nt.focus(),sj_cook.clear("fdbk_acc","/"))))))),g=_ge(a),g&&g.classList&&g.classList.contains(s))||(p=typeof p===i?!1:p,ut=o(g,3),e!=="sb_feedback"&&(e=a,typeof sj_evt!==i&&(r&&sj_evt.unbind(f,r),r=function(n){var f=null,i=null,e=null,s,r,h;n&&n.length>1&&(r=n[1],r.tagName!==undefined&&r.nodeType!==undefined?(f=r,i=ot(f)):i=r,s=i&&i.elementToHighlight||f,e=o(s));h=i&&i.linkId||a;u(y,t,v,h,e,i)},sj_evt.bind(f,r,1)),typeof SearchAppWrapper!==i&&SearchAppWrapper.CortanaApp&&SearchAppWrapper.CortanaApp.addEventListener&&SearchAppWrapper.CortanaApp.addEventListener(f,function(n){(typeof n!==i&&n!==null&&(n.isHandled=!0),a===e)&&_ge("fbpgdg")===null&&u(y,t,v,a)})),g!==null?(ft=function(n){if(!(n instanceof KeyboardEvent)||n.keyCode===13){var r=null,i=null,f=null,e;if(p&&g.classList){if(g.classList.contains(h))return!1;g.classList.add(h)}d&&sj_evt.fire("feedback.dialog.defaultcheckedradio",d);sj_pd(n);sj_sp(n);r=sj_et(n);i=ot(r);e=i&&i.elementToHighlight||r;f=o(e);u(y,t,v,a,f||ut,i||k)}},sj_be(g,"click",ft),sj_be(g,"keydown",ft),g.classList&&g.classList.add(s)):(b=typeof b===i?!1:b,b&&u(y,t,v,a,ut)),typeof sj_evt!==i&&sj_evt.fire(l))}var f="feedbackformrequested",l="feedbackInitialized",r,e="",s="feedback-binded",h="clicked",i="undefined",c;t.InitializeFeedback=a;n.le=function(n,t){SharedLogHelper&&SharedLogHelper.LogError&&SharedLogHelper.LogError("Feedback: ".concat(n),null,t)};n.leh=function(t,i,r){n.le("Feedback: ".concat(t," handler failed in ").concat(i),r)};n.fel=function(t,i){for(var u=[],r=2;r<arguments.length;r++)u[r-2]=arguments[r];try{u.unshift(t);sj_evt.fire.apply(null,u)}catch(f){n.leh(t,i,f);throw f;}};c=function(){function n(){this.settingsList=[]}return n.prototype.setStartSettings=function(n,t){n&&t&&this.settingsList.push({c:n,s:t})},n.prototype.findSettings=function(n){var t=null;return this.settingsList.forEach(function(i){sj_we(n,i.c)&&(t=i.s)}),t},n}();sj_evt.fire("fdbkbtstrp_init")})(t=n.Bootstrap||(n.Bootstrap={}))})(Feedback||(Feedback={})),function(n){var t;(function(t){"use strict";function f(n){return typeof n=="object"&&n!==null}function e(n){return n==null?n===undefined?"[object Undefined]":"[object Null]":toString.call(n)}function o(n){if(!f(n)||e(n)!="[object Object]")return!1;if(Object.getPrototypeOf(n)===null)return!0;for(var t=n;Object.getPrototypeOf(t)!==null;)t=Object.getPrototypeOf(t);return Object.getPrototypeOf(n)===t}function s(f,e,s,h){var g=_G.IG,nt=typeof _G.V===i?_G.P:_G.V,c,p,l,v,b,y,d;n.fel("onFeedbackStarting","lp");t.staticConfig={linkId:e,activeElement:_d.activeElement};var a="?ig="+g+"&p="+nt,k=n.RouteProvider.Provide(f),w=encodeURIComponent;if(h){if(h.formConfig&&(k=f==="page"?"sdk/form":f,a+="&formconfig="+h.formConfig),h.service&&(t.staticConfig.service=h.service),h.scenario&&(t.staticConfig.scenario=h.scenario),h.structuredData)try{o(h.structuredData)&&(t.staticConfig.structuredData=h.structuredData)}catch(tt){}if(h.clientSideScreenshot&&(t.staticConfig.clientSideScreenshot=h.clientSideScreenshot),l=h.context,l)for(v in l)l.hasOwnProperty(v)&&(a+="&"+w(v)+"="+w(l[v]));h.pos&&(t.staticConfig.pos=h.pos,a+="&pos=1")}for(c="/feedback/"+k+a,typeof fbsrc!==i&&(c+="&src="+w(fbsrc)),typeof fbpkgiid!==i&&fbpkgiid[f]&&(c+="&iid="+fbpkgiid[f]),b=["addloginsource","client","clientip","corpnet","features","hose","hoseassistant","logjserror","msamock","setvar","testhooks","theme","uncrunched","ptn"],y=0;y<b.length;y++)(p=location.href.match(new RegExp("[?&]"+b[y]+"=[^?&#]*","i")))&&p[0]&&(c+="&"+p[0].substring(1));d=typeof sj_ajaxCSP=="function"?sj_ajaxCSP:sj_ajax;d(c,{callback:function(t,i){var u,f;if(t&&i)try{r&&r.removeAttribute("clicked");u=h&&_ge(h.feedbackContainerId);(h===null||h===void 0?void 0:h.appendFeedbackDialogAfterEvent)?(f=function(){i.appendTo(u||_d.body);sj_evt.unbind(h.appendFeedbackDialogAfterEvent,f)},sj_evt.bind(h.appendFeedbackDialogAfterEvent,f,1)):i.appendTo(u||_d.body);n.fel("onFeedbackShow","lp");n.fel("clarity.trigger","lp","BingFeedback");n.Highlight&&s&&n.Highlight.HighlightElements(s);(e.indexOf("fdbtext_")>-1||e.indexOf("thumb_t")>-1||e.indexOf("thumb_f")>-1||e.indexOf("thumb_tum")>-1)&&sj_evt.fire("HightLightScreenShotById",e,"li",["b_results","b_context"],"ol")}catch(o){n.le("Package load callback failed",o);throw o;}}});u[f]=!0}var u={},i="undefined",r;t.staticConfig={};n.PackageLoad.GetHTML=function(){return _d.documentElement.outerHTML};n.PackageLoad.Load=function(n,t,f,e,o){var h,c;t=typeof t===i?!0:t;f=typeof f===i?"":f;c=typeof o!=i&&o&&o.feedbackContainerId;r=_ge(f);for(h in n)n.hasOwnProperty(h)&&(!t||c||typeof u[h]===i)&&s(h,f,e,o)}})(t=n.PackageLoad||(n.PackageLoad={}))}(Feedback||(Feedback={})),function(n){var t;(function(){"use strict";n.RouteProvider.Provide=function(n){return n==="page"?"sdk/form":n}})(t=n.RouteProvider||(n.RouteProvider={}))}(Feedback||(Feedback={})),function(n){var t;(function(n){"use strict";n.submit={registered:{},use:function(n,t){this.registered[n]=t},clear:function(){this.registered={}}}})(t=n.Hooks||(n.Hooks={}))}(Feedback||(Feedback={}));Feedback.Hooks.submit.use("conversationId", (payload) =>{ var conversationId = _w["CIB"]?.manager.conversation.id; payload["structured_data"]["conversationId"] = conversationId; if (_d.querySelector("cib-serp[mode='conversation']")) { payload["rawRegions"] = ["ConversationMode"] } return payload; });_w["_sydConvConfig"] ={"enableBackgroundFix":1,"hideMuidUpsell":1,"usePill":1,"enableSydTigerAns":1,"tigerEnhancedPromptTemplate":"Search for {0}. Impress me with a great response that helps me complete my most likely intent while showing the smartness, depth, and beauty (bolding, tables, and citations) you can produce. Satisfy my intent as early as possible in the response.","enableScrollOut":1,"enableSydContext":1,"sydOptionSets":"bicfluxv3,gndlogcf,gndbfptlw,fluxprod","enableNoToneOpt":1,"sydBalOpts":"dclfluxv15,gldcl1p","sydIDs":"inlineadsv2ho,inladscontho,nodesign,bgstreamcf,galleryranker,zinwf,132dxccaches0,stcheck,invldreq,revfschelpm,mlchatardg-c,cmcpupsalltf,sydtransctrl,proupsallcf,narrowidcf,220dcl1bt15,0209bicv3,0215wcrwippsr,bingfccf,fpsticycf,sapsgrds0,0131gndbfpr,1119backos,enrrmetacf,cacfrwebt3,sswebtop1cf,sswebtop3","sydBaseUrl":"https://sydney.bing.com","compSydRequestSource":"cib","compSydClientScenario":"chat","compSydClientPlatform":"Web","enablePassingCompliantSydneyClientInfo":1,"compSydRequestScenario":"chat","enterpriseChatDefaultIconLargeChecksum":"R5FZeTI5Afc9NBRK7GHAzPkr1vHbc/X2n1c+mPVUhzE=","enablePluginsForBizChat":1,"augloopEndpoint":"https://augloop.office.com","enableProdEditorEndpoint":1,"ciqReleaseAudienceGroup":"Production","enableDeterminateProgressBar":1,"enablePLSingleColumnStylesV2":1,"enableCheckMsbCibBundleLoad":1,"enableSapphireSydVoiceExp":1,"sapphireArticleContentAPI":"https://assets.msn.com/content/view/v2/Detail","sapphireSydneyQualificationAPI":"https://www.bing.com/edgesvc/postaj/sydneyqualification","sapphireSydneyZeroturnAPI":"https://www.bing.com/edgesvc/postaj/sydneyzeroturn","sapphireSydneyTruncationAPI":"https://www.bing.com/edgesvc/postaj/sydneytruncationswithcontext","enableSydPreloadAudioTTS":1,"enableSydneyShareLink":1,"country":"Mexico","countryConfidence":8,"state":"Michoacan","city":"Morelia","cityConfidence":5,"zip":"58117","timezoneoffset":"-6","lat":"19.7223","long":"-101.1943","language":"Spanish (Mexico)","userMailId":"ramon_cq@hotmail.com","username":"Ramón Cerda Quiroz","userIpAddress":"38.7.20.218","isMSAAuthenticated":1,"moveOnlySydXAnswers":1,"enableSydX1T":1,"enableSydTestCache":false,"enableSydCarousel":1,"traceId":"65d7ebb6419f4345939b7bed9ec091d2","enableSydMic":1,"enableSydShare":1,"enableMessageShareableSignalV2":1,"enableSydCookLoc":1,"enableSQMsg":1,"addDv3Opt":1,"enableErrorTelemetry":1,"checkCreatorAnsFor1T":1,"enableAnsCardSuffix":1,"isAdultUser":1,"enableSpeechContinuousErrorHandling":1,"enableTTSNeverSetting":1,"enableCustomizedTtsOptions":1,"enableSydCLOC":1,"enableCdxFeats":1,"enableShareModalDialog":1,"enableFdbkFinalized":1,"enableSydImageCreate":1,"enableSimpleVoiceInput":1,"enableSpeechDictationMode":1,"enableAudioPreloadLimit":1,"enableSpeechFateTTSStop":1,"enableSpeechErrorHandling":1,"enableSpeechTTSLatencyLogging":1,"enableSpeechIconDarkTheme":1,"enableSpeechAriaLabel":1,"enableNewTopicAutoExpand":1,"enableThreadsAADMSASwitch":1,"enableMaxTurnsPerConversation":1,"maxTurnsPerConversationMuidUser":4,"maxMessageLength":4000,"maxMessageLengthBalanced":2000,"maxMessageLengthCreative":4000,"maxMessageLengthPrecise":4000,"disResetTT":1,"enablePerfTrk":1,"enableTonePerf":1,"enableSinglePerfEventPerMessage":1,"enableE2EPerf":1,"enableAdSlugsMobile":1,"enableUnauthRedir":1,"enableVersionedApiCalls":1,"enableJWTFTRFix":1,"enableFeedbackClientSideScreenshot":1,"enableAddMobileTagToCibOnShowConv":1,"enableStreamSyntheticTextResponses":1,"enableStreamSyntheticTextResponsesForBalancedTone":1,"enableStreamSyntheticTextResponsesForCreativeTone":1,"enableStreamSyntheticTextResponsesForPreciseTone":1,"syntheticStreamSpeed":55,"enableSyntheticStreamSpeedBasedOnTone":1,"syntheticStreamSpeedForBalancedTone":15,"syntheticStreamSpeedForCreativeTone":55,"syntheticStreamSpeedForPreciseTone":55,"enableMobileShareAnchorBottom":1,"enableHideTypingIndicatorOnError":1,"enableLearnmoreAsHeader":1,"enableRaiButtonTooltip":1,"enableResetFocusLossFix":1,"enableShareConversation":1,"enableGetConversationAPIWithSNRProxy":1,"enableGetSharedMessagesAPIWithSNRProxy":1,"enableShowShareButtonWithSecurityCheck":1,"enableServerLogMerging":1,"enableAutoRecoverFromInvalidSession":1,"enableAutoRecoverFromInvalidSessionForFirstTurn":1,"enableCodeCopy":1,"enableCodeBar":1,"enableCodeBarV2":1,"enableInlineFeedback":1,"enableInlineFeedbackV21":1,"enableSerpFeedback":1,"enablePersistentInputText":1,"enableActionBarIMEFix":1,"enableGhostTrailingSpaceByLanguage":1,"shareLoadingUI":1,"customizedShareOutImageThumbnailId":"OBFB.73FF6ADE8CC93B6ED1EDA1CE557E2E09","enableFeedbackInstrumentation":1,"sydSapphireUpsellVisualSearchQRCodeUrl":"https://bingapp.microsoft.com/bing?adjust=13uz7blz_13evwnmy","sydneyContinueOnPhoneShortenQRCodeUrl":"https://bingapp.microsoft.com/bing?style=newbing\u0026adjust=euhmno2_oy62nz1","enableConvModeSwitchAjax":1,"enableSetToneFromUrl":1,"convBackBtn":1,"enableSetConvQueryFlag":1,"enableSearchQueryForInlineAds":1,"enableFixForInlineAdsSQ":1,"codexPartnerScenario":"SERP","enableMessageExport":1,"enableFlatActionBar":1,"enableAutosuggestMetrics":1,"enablePrivacyConsent":1,"enableCodexMuidConsentExplicit":1,"enableFixCodeXAsBug":1,"enableThreads":1,"enableMobileOneClickShareV2":1,"enableDynamicShareTitle":1,"enableSharePreview":1,"enableSharedHoverCitations":1,"enableScreenshotIgnoreOnCibSerp":1,"enableStandaloneUtility":1,"enableThreadShareLandingPage":1,"enableShareThreadWithNoCache":1,"enableFixOnReplaceAllJSError":1,"enableShareCurrentThreadMessagesfromAPI":1,"enablePreserveTextFormat":1,"disable2TSearchHistory":1,"enableSydBeacon":1,"enableVisualSearch":1,"eifpiab":1,"himgdirmsg":1,"efhof":1,"visualSearchSubscriptionId":"Bing.Chat.Multimodal","disablePassBotGreetingInContext":1,"enableThreadContextMenu":1,"enableCloudflareCaptcha":1,"enableResizeObserverWithAnimationFrame":1,"enableStartPromotion":1,"enableKnowledgeCardImage":1,"enableMobileKnowledgeCardOverlay":1,"enableCibOverlayForKnowledgeCard":1,"enableCopyButtonInstrumented":1,"enableMessageExportWithPlainText":1,"enableMessageExportOnlineWord":1,"enableMessageExportOnlineExcel":1,"enableTableBarFlatActions":1,"enableThreadExportOnlineWord":1,"enableMessageExportV2":1,"enableBotMessageActionsBar":1,"enableDirectlyOpenExportOnlineLink":1,"enableLoginHintForSSO":1,"enableLimitToMsaOnlineExport":1,"enableLimitToAadOnlineExport":1,"enableMSAuth1stPartyApp":1,"enableCopyHtml":1,"enableGetChats":1,"enableDelayGetChats":1,"enableExportDocxWithFormat":1,"enableExportDocxWithTableFormat":1,"enableThreadSync":1,"enableCssSplitConvModeOnClick":1,"enableFlux3P":1,"f3pMsaAccountOnly":1,"enableSearchPluginOnTop":1,"disableWelcomeScreen":1,"enableWelcomeScreenV3":1,"codexTableVersion":1,"enableBICHistory":1,"enableAIPersonasForDesktop":1,"enableRenderCardRequestIMSearchParams":1,"typingIndicatorAnimationDuration":1000,"disableTooltip":1,"enableOnProcessingStartEvent":1,"enableOnProcessingCompleteEvent":1,"enableTypewriter":1,"enableCitationsOnSentences":1,"enableMuidDisabledSuggestionBar":1,"fileUploadMaxSizeLongContext":10000000,"fileUploadMaxAudioSize":15000000,"fileUploadFileNameLengthLimitation":100,"fileMaxCountForGptCreator":1,"fileMaxCountForChat":1,"enableUserMessageCopy":1,"enableDeferredImageCreatorCard":1,"enableFaviconsV2":1,"enableUserIpAddress":1,"enableNewChatIconInActionBar":1,"enableActionBarV2":1,"speechSurface":"desktop","enableKatexScroll":1,"padding2TMobile":80,"enablePageHeaderOverlapClamping":1,"enableThreadShowPlugins":1,"enableUpdateUserMessageId":1,"enableMobileFirstClickShare":1,"enableInlinePersonalizationConsent":1,"enableSydWelcomeScreenMobileEvents":1,"enableFetchUserConsent":1,"personalizationInlineConsentTurn":1,"m365ConsumerPluginClientId":"ef47e344-4bff-4e28-87da-6551a21ffbe0","m365ConsumerPluginAuthority":"https://login.windows-ppe.net/consumers","m365ConsumerPluginOptionsSet":"flux_m365consumer_dogfood","m365ConsumerPluginAuthScope":"openid","enableReadAloudMessage":1,"enableReadAloudOnDeepLeoOnly":1,"enableNoBingSearchResponseBackground":1,"enableNoSearchPluginMetaMessage":1,"enableShareInThreadsHeader":1,"enableThreadsConsent":1,"enableMutedThreadsConsent":1,"enableDeleteSingleConversationMemory":1,"enableStableAutosuggestion":1,"threadsAutoSaveOptionset":"autosave","enableThreadContextMenuV2":1,"enableBCBSensitivityLabel":1,"enableOneDs":1,"enablePromptHandling":1,"dedicatedIpType":"unknown","enableCiqAttachmentsOnInputChanges":1,"enableCachedContentFixForIsStartOfSession":1,"enableNotebookMode":1,"enableBackFromNotebook":1,"enableRecoverConvNotebook":1,"enableShiftEnterNotebook":1,"enableNotebookScrollDown":1,"extraNotebookOptionsSets":"blocklist,prjupy","notebookMaxMessageLength":18000,"enableSetNotebookAlignment":1,"disableShareLinkShowntbk":1,"enableCancelPendingRequestOnNewMessage":1,"enableAttributionsV2":1,"minimumZeroInputSuggestionCount":1,"enableCopilotLayout":1,"enableSpeechLogNoiseReduction":1,"multimediaSearchFormCode":"IACMIR","multimediaSearchEnableMediaCanvas":1,"multimediaSearchMaxImageCount":5,"defaultFallBackSERPQuery":"Bing AI","enableRelativeSignInUrl":1,"enableChatScrollFix":1,"enableCvidInAutosuggestAPI":1,"enableSwiftKeyLatestUX1":1,"enableBufferClearOnReset":1,"enablePluginChatModeOnly":1,"enableGhostingSuggestTelemetry":1,"bceTermsOfUseVersion":2,"disableTitlePreviewLabel":1,"enableFreeSydney":1,"enableFreeSydneyV2Style":1,"enableDesignerGPT":1,"defaultMaxPersonaCount":6,"enable1pGPTPreLoadForCopilot":1,"enableFreeSydneyNonSigned":1,"isBingUserSignedIn":1,"freeSydneyCopilotIconUrl":"https://r.bing.com/rp/_WT61VllAyx4fyquJgufM7hwmeg.png","freeSydneySydneyIconUrl":"https://r.bing.com/rp/ZIFQVh6WX4QKgPWuBMBKNrERh3A.png","freeSydneyDesignerIconUrl":"https://r.bing.com/rp/l8iFubC9EgDGvKo7xeNIvSQwikU.png","enableF3pNoSearchBgFix":1,"enableSunoMusicCreate":1,"enableSunoMusicInst":1,"sydneyFeedbackVertical":"chat","enableGptHomeScreen":1,"enableUndersideTopRightButtonBugFix":1,"enableFaqBcc":1,"enableFaqBce":1,"faqBccBceUrl":"https://bing.com/new","gptCreatorCopilotCreatorPath":"/turing/copilot/creator","gptCreatorBingPreviewPath":"/search","gptCreatorSharePath":"/search","enableGoldilocks":1,"enableGoldilocksRAIButton":1,"enableGoldilocksStrokes":1,"enableGoldiLocksBackgroundLight":1,"enableGoldilocksLineHeight":1,"enableGoldilocksMetaMessage":1,"enableGoldilocksAttributionPills":1,"enableGoldilocksMessageActions":1,"enableGoldilocksTurnCounter":1,"enableGoldilocksStrokes2T":1,"enableGoldilocksWiderActionBar":1,"enableGoldilocksExportDropdown":1,"enableGoldilocksInlineFeedback":1,"enableGoldilocksUserMsgActionBar":1,"enableGoldilocksContextual":1,"enableGoldilocksSydneyAction":1,"enableGoldilocksInlinePersistentMetas":1,"enableTopAlignConversation":1,"enableHideZeroTurn":1,"enableMessageGroupHeader":1,"enableFullWidthMessages":1,"enableInlineStreamingIndicator":1,"enableCitationSuperscriptFix":1,"enableGptCreatorConfigurePanelKnowledges":1,"enableGptCreatorConfigurePanelcapabilities":1,"enableGptCreatorConfigurePanelImageGenerator":1,"enableAttributionsV2MoreButton":1,"enableGoldilocksCitations":1,"enableResetASProperties":1,"enableKSPQPropertiesTracking":1,"enableFullScreenIframe":1,"enableDelayedMsgHandlerDisposal":1,"userAnID":"437BD5890DE4EDDEF6D19E79FFFFFFFF","enableAutoSuggestPerfMetrics":1,"enableAutoSuggestPerfMetricP50":1,"enableAutoSuggestPerfMetricP90":1,"enableAutoSuggestPerfMetricAvgLatency":1,"enableAutoSuggestPerfMetricQFT":1,"enableCachingAutosuggest":1,"enableShowAutosuggestButton":1,"enableStartOfSessionFix":1,"codexCanvas":"bingchat","freeSydneyOptionSets":[{"value":"fluxsydney"}],"neuripsOptionSets":[{"value":"nipsgpt"}],"autoHideConvInterval":600000,"enableAjaxBundlePLoad":1,"PLoadIID":"SERP.5648"}; _w["_sydThreads"] ={"threads":[]};;_w["_sydConvTranslation"] ={"letsChat":"Charlemos","chat":"Chat","pause":"Pausar","play":"Jugar","loading":"Cargando","stop":"Detener","canceling":"Cancelando","more":"más","expand":"expandir","seeMore":"Ver más","seeLess":"Ver menos","seeChatHistory":"Ver historial de chats","hideChatHistory":"Ocultar historial de chats","seeMoreGPTs":"Ver más GPT de Copilot","seeLessGPTs":"Ver menos GPT de Copilot","raiSuggestions":"Mostrar más sugerencias de información","raiSuggestionsClose":"Ocultar más sugerencias de información","close":"Cerrar","refresh":"Actualizar","next":"Siguiente","cancel":"Cancelar","preview":"Vista previa","of":"de","submit":"Enviar","explore":"Explorar","exploreMore":"Explorar más","startOver":"Volver a empezar","responses":"respuestas","readAloud":"Leer en voz alta","actionBarPlaceholder":"Pregúntame cualquier cosa","actionBarLongPlaceholder":"Pregúntame lo que quieras o escribe / para agregar personas, archivos y mucho más","actionBarNotebookPlaceholder":"¿Qué tienes en mente? Escribe un mensaje detallado para colaborar con Copilot. Utiliza Mayús+Intro para enviar un mensaje.","actionBarComposeButton":"Nuevo tema","actionBarNewChatButtonDesktop":"Iniciar un nuevo chat","actionBarNewChatButtonMobile":"Nuevo chat","actionBarAutoSuggestTab":"Tabulador","actionBarAutoSuggestTap":"Pulsar","actionBarFileUploadButtonAriaLabel":"Agregar un archivo","actionBarFileUploadLongContextButtonAriaLabel":"Cargar un archivo de texto o probar una URL web","actionBarFileUploadButtonTooltip":"Cargar un archivo de texto o probar una URL web","actionBarFileUploadLongContextButtonTooltip":"Cargar un archivo de texto o probar una URL web","actionBarKeyboardHint":"Aceptar","actionBarTextInputModeButtonAriaLabel":"Usar teclado","actionBarSpeechInputModeButtonAriaLabel":"Usar micrófono","actionBarVisualSearchButtonAriaLabel":"Agregar una imagen para buscar","actionBarVisualSearchButtonTooltip":"Agregar una imagen","actionBarVisualSearchCoachMarkTitle":"El nuevo Bing con imágenes","actionBarVisualSearchCoachMarkDescription":"Ya puedes chatear con Bing mediante imágenes.","actionBarVisualSearchCoachMarkImgAlt":"Imagen de información sobre herramientas de búsqueda visual","actionBarSubmitButtonAriaLabel":"Enviar","actionBarCancelSpeechInputButtonAriaLabel":"Cancelar entrada/lectura de voz","actionBarEditModeButtonAriaLabel":"Anclar","actionBarSpeechBtnStartListeningAriaLabel":"Usar micrófono","actionBarSpeechBtnStopListeningAriaLabel":"Desactivar","actionBarSpeechBtnStopReadoutAriaLabel":"Detener lectura","actionBarPromptGuideToolTip":"Guía de consulta","actionBarNewlineTooltip":"Usa Mayús+Entrar para crear saltos de línea para mensajes más largos","actionBarAttachButtonToolTip":"Adjuntar","actionBarTextInputUnsupportedFileMessage":"Este tipo de archivo no es compatible. Elija una imagen o un archivo de texto e inténtelo de nuevo.","actionBarAddNotebookButtonTooltip":"Nuevo tema","actionBarAddNotebookButtonAriaLabel":"Nuevo tema","actionBarEditResponseTitle":"Editar y enviar","noFilesPermissionTitle":"No se puede acceder a los archivos","noFilesTitle":"No hay archivos","noFilesDescription":"Los archivos almacenados en el dispositivo se mostrarán aquí. Listo para ser agregado a Copilot","noPhotosPermissionTitle":"No se puede acceder a Fotos","noPhotosTitle":"Sin fotos","noPhotosDescription":"Haz una foto y se mostrará aquí. Listo para ser agregado a Copilot","noCameraPermissionTitle":"No se puede acceder a la cámara","quickCaptureTurnOnText":"Activar","screenTip":"Pulsa dos veces para encender y capturar la pantalla cuando haya una aplicación abierta.","screenTipTitle":"No hay información de pantalla disponible","screen":"Pantalla","screenButtonAriaLabel":"Agregar una pantalla de teléfono para buscar","files":"Archivos","photos":"Fotos","camera":"Cámara","attachment":"datos adjuntos","attachmentHasSucceded":"Adjunto agregado correctamente","attachmentHasFailed":"Error en el adjunto","attachmentIsReplaced":"El adjunto anterior se ha sustituido por el nuevo","editResponseQueryPrefix":"Esta es mi versión editada. Revísela: \\n{0}","actionBarQuickCaptureButtonAriaLabel":"Captura rápida","promptLibraryButtonLabel":"Ver consultas","typingIndicatorStopRespondingAriaLabel":"Dejar de responder","feedbackAccessed":"Accedido","feedbackSource":"Origen: Conversación con Bing,","feedbackLikeButtonAriaLabel":"Me gusta","feedbackDislikeButtonAriaLabel":"No me gusta","feedbackOffensiveButtonAriaLabel":"Marcar como ofensivo","feedbackCopyButtonAriaLabel":"Copiar","feedbackShareButtonAriaLabel":"Compartir","feedbackShareAnswer":"Eche un vistazo a esta respuesta de Bing","feedbackShareGenerated":"Echa un vistazo a lo que he generado con Bing","feedbackShareContent":"Aquí tienes una respuesta que obtuve usando el nuevo Bing, el primer motor de respuestas basado en IA del mundo. Haz clic para consultar la respuesta completa y probarlo tú mismo.","feedbackShowTonesButtonAriaLabel":"Mostrar todos los tonos","feedbackHideTonesButtonAriaLabel":"Ocultar todos los tonos","feedbackHidePreciseToneButtonAriaLabel":"Ocultar preciso","feedbackShareErrorToast":"Error al compartir.","feedbackRewriteButtonAriaLabel":"Reescribir","feedbackSearchOnBingButtonAriaLabel":"Buscar en Bing","fileUploadWebUrlLimitErrorMessage":"Solo se permite cargar una dirección URL web","fileUploadFileLimitErrorMessage":"Se ha superado el límite máximo de carga de archivos","fileUploadSameFileNameErrorMessage":"No se permite la carga de archivos con el mismo nombre","fileUploadFlyoutInputboxAriaLabel":"Pegar dirección URL de página web","fileUploadFlyoutTitle":"Agregar un archivo de texto","fileUploadFlyoutUploadButtonLabel":"Cargar desde este dispositivo","fileUploadDragAndDropLabel":"Coloque la imagen o el archivo aquí","fileUploadGenericErrorMessage":"No se pudo cargar el archivo. Inténtelo de nuevo","fileUploadUnsupportedFileMessage":"Este tipo de archivo no es compatible. Elija una imagen o un archivo de texto e inténtelo de nuevo.","fileUploadMaxSizeLimitErrorMessage":"Se superó el tamaño de archivo. Solo puede cargar un archivo de hasta 500 KB.","fileUploadFileNameLengthErrorMessage":"La longitud del nombre de archivo es demasiado larga.","fileUploadMaxSizeLimitLongContextErrorMessage":"Se superó el tamaño de archivo. Solo puede cargar un archivo de hasta 10 MB.","fileUploadMaxSizeLongContextErrorMessage":"Se superó el tamaño de archivo. Solo puede cargar un archivo hasta {0} MB.","fileUploadTextFileUploadErrorMessage":"Error al cargar el archivo.","fileUploadWebPageInfoUploadErrorMessage":"No se pudo extraer el contenido de la página web.","serpfeedback":"Comentarios","exportTitle":"Exportar","exportTextTitle":"Texto","exportPdfTitle":"PDF","exportPdfInOptionsTitle":"Exportar a PDF","exportWordTitle":"Word","exportWordInOptionsTitle":"Exportar a Word","exportWordOnlineTitle":"Word","exportExcelTitle":"Descargar como libro (.xlsx)","exportExcelOnlineTitle":"Editar en Excel","exportTableTitle":"Tabla","exportAddToFile":"Agregar a un archivo existente","exportToastViewButtonLabel":"Ver","exportSuccessToastTitle":"Archivo exportado. Es posible que algunas imágenes y formato no se exporten.","exportErrorToastTitle":"Error al exportar","exportLoadingToastTitle":"Exportando...","exportOpenInWordLabel":"Abrir en Word","exportSaveToOneDrive":"Guardar en OneDrive","exportSaveToDevice":"Guardar en el dispositivo","exportSaveAsWordTitle":"Word","exportSaveAsPdfTitle":"PDF","exportFilenameLabel":"Nombre","exportDefaultFileName":"Contestar","exportEditFilenameLabel":"Editar","exportFiletypeLabel":"Tipo","exportCancel":"Cancelar","exportChooseFolder":"Elegir carpeta","exportedFileUserTitle":"Usuario","exportedFileBotTitle":"Bing","exportedFileSourceTitle":"Fuentes","codeDisclaimer":"Código generado por IA. Revisar y usar cuidadosamente. {0}.","codeDisclaimerLinkLabel":"Más información sobre preguntas frecuentes","copyCodeButtonTooltip":"Copiar","goldilocksMessageHeaderUserText":"Usted","goldilocksMessageHeaderBotText":"Copilot","goldilocksMetaUsingText":"Utilizando: ","goldilocksMetaPluginText":"Complemento","goldilocksMetaPluginsText":"Complementos","goldilocksMetaSearchQueryText":"Buscar","goldilocksMetaSearchQueriesText":"Búsquedas","metaInternalSearchQueryGoldilocks":"Buscando {0}","speechFailedRecognitionError":"No puedo escucharte porque el servidor de voz está inactivo. Inténtalo de nuevo más tarde.","speechLoading":"Esperando audio...","speechListening":"Escuchando...","speechMediaNotReachableError":"No puedo escucharte porque no tienes un micrófono disponible.","speechNoPermissionError":"Tendrás que habilitar el micrófono para que pueda oírte.","speechNoPermissionErrorWinCopilot":"\u003cb\u003e el acceso al micrófono \u003c/b\u003e\u003cbr\u003e Para que Copilot en Windows use el micrófono, asegúrate de que \u003cb\u003e Permitir que las aplicaciones de escritorio accedan al micrófono\u003c/b\u003e está activado en Configuración de Windows. ","speechAudioPlaybackError":"Se ha producido un error.","speechUnknownError":"Se ha producido un error.","speechAuthenticationError":"Error de autenticación. Vuelve a intentarlo más tarde.","speechOnlineNotification":"La entrada de voz se procesa con los servicios en línea de Microsoft y no se recoge ni se guarda.","speechReopenMicMessageHeader":"El micrófono se volverá a abrir automáticamente para una conversación de voz sin problemas","speechReopenMicMessageText":"Pulsa el micrófono para dejar de escuchar.","speechReopenMicMessageCTA":"Desactivar la opción de volver a abrir automáticamente","speechReopenMicMessageUndoHeader":"Entendido. La opción de volver a abrir automáticamente el micrófono está desactivada\u0022","speechReopenMicMessageUndoText":"Ya no volveremos a abrir automáticamente el micrófono.","speechReopenMicMessageUndoCTA":"Deshacer","speechLanguageMessageHeader":"Parece que has cambiado de idioma hablado","speechLanguageMessageText":"Copilot detectó automáticamente un nuevo idioma hablado y responderá a la coincidencia","speechLanguageMessageCTA":"Desactivar","speechLanguageMessageUndoHeader":"Entendido. La detección automática está desactivada","speechLanguageMessageUndoText":"Ya no detectaremos automáticamente los idiomas hablados.","speechLanguageMessageUndoCTA":"Deshacer","visualSearchDragAndDropLabel":"Arrastra una imagen aquí","visualSearchFlyoutTitle":"Agregar una imagen","visualSearchFlyoutInputboxAriaLabel":"Pegar imagen o URL","visualSearchFlyoutUploadButtonLabel":"Cargar desde este dispositivo","visualSearchGenericErrorMessage":"No se pudo cargar el archivo. Vuelva a intentarlo.","visualSearchInvalidUrlMessage":"Lo sentimos: al parecer, la URL que ha proporcionado no es válida. Inténtelo de nuevo","visualSearchUnsupportedFileMessage":"No se admite este tipo de archivo. Elija un archivo de imagen e inténtelo de nuevo.","visualSearchFlyoutTakePhotoButtonLabel":"Hacer una foto","visualSearchMobileUpsellLabel":"Usar la aplicación Bing en el teléfono","visualSearchMaxCameraButtonLabel":"Vista de cámara de pantalla completa","attachmentLoading":"Cargando datos adjuntos","deleteAttachment":"Eliminar datos adjuntos","warmWelcomeTitle":"Te damos la bienvenida al nuevo Bing","warmWelcomeTitleLine2":"","welcomeContainerCopilotGptsPolicy":"Directiva","welcomeContainerPrivacyStatement":"Privacidad","welcomeContainerUseTerms":"Términos","welcomeContainerLearnMore":"Más información","welcomeMessageItemTitle1":"🧐 Haz preguntas complejas","welcomeMessageItemTitle2":"🙌 Obtener mejores respuestas","welcomeMessageItemTitle3":"🎨 Recibe inspiración creativa","discoverMessageItemTitle":"📝 Reescribir texto juntos","welcomeMobCopilotSugg1":"Cree una canción sobre aventuras con su familia","welcomeMobCopilotSugg2":"Escriba un poema con imágenes vívidas que no rime","welcomeMobCopilotSugg3":"Dame una lista de nuevos pasatiempos que podría realizar con tiempo libre limitado.","welcomeContainerFaq":"Preguntas frecuentes","welcomeMessageItemContent1":"¿Qué comidas puedo preparar para mi hijo pequeño quisquilloso que solo come alimentos de color naranja?","welcomeMessageItemContent2":"¿Cuáles son los pros y los contras de las 3 aspiradoras para mascotas más vendidas?","welcomeMessageItemContent3":"Escribe un haiku sobre cocodrilos en el espacio exterior con la voz de un pirata","discoverMessageItemContent":"Intenta seleccionar texto de la página para ver lo que podemos hacer juntos","welcomeMessageItemLabel4":"Novedades en Más creativo:","welcomeSubtitleText":"Experimenta tu copiloto con tecnología de IA para la web","welcomeTertiaryTitleText":"Usa el poder de la IA para encontrar respuestas en la web, crear contenido escrito y mucho más.","welcomeLearnMessage":"Copilot usa IA. Compruebe si hay errores.","welcomeLearnFeedbackSubstitute":"compartir comentarios","muidUsersTermsCond1":"Al hacer clic en \u0022Introducción\u0022 y continuar con la interacción con Copilot, aceptas el ","muidUsersTermsCond1Inline":"Al continuar la interacción con Copilot, aceptas el ","muidUsersTermsCond1Modal":"Al continuar la interacción con Microsoft Copilot, acepta el ","muidUsersTermsCond2":"y confirme que ha revisado el ","muidUsersTermsCond2Modal":" y confirmando que ha revisado ","muidUsersTermsCond3":"","muidUsersPrivacyStatement":"Declaración de privacidad","muidUsersTermsOfUse":"Condiciones de uso","muidUserGetStartedText":"Comenzar","muidUserContinueText":"Continuar","muidUserHeaderText":"","adsDisclaimer":"Los anuncios no están dirigidos en función de la identidad del lugar de trabajo o el historial de chats. {0}.","adsDisclaimerLearnMoreLink":"Más información","welcomeTileContainerTitle":"Algunas ideas para comenzar","welcomeLearnMessageOnMobileCopilot":"Copilot cuenta con tecnología de IA, por lo que es posible que se den sorpresas y errores. {0} para que podamos mejorar.","toneSelectorDescription":"Elegir un estilo de conversación","toneSelectorCreativeTooltip":"Iniciar un chat original e imaginativo","toneSelectorBalancedTooltip":"Recomendado para chats informados diarios","toneSelectorPreciseTooltip":"Iniciar un chat conciso, útil para la búsqueda de hechos","toneSelectorSuppressedTooltip":"No disponible temporalmente. Vuelva a intentarlo más tarde.","toneSelectorMoreCreative":"Más\\r\\nCreativo","toneSelectorMoreBalanced":"Más\\r\\nEquilibrado","toneSelectorMorePrecise":"Más\\r\\nPreciso","toneSelectorFaster":"Rápido","toneSelectorWithGPT4":"Con GPT-4","welcomeMobSugg1":"¿Cómo usa Bing la inteligencia artificial?","welcomeMobSugg2":"¿Qué más puede hacer Bing?","welcomeMobSugg3":"¿Cuáles son las últimas noticias en tecnología?","flippedMobileCopilotTitle":"Copilot","feedbackPromptPositive":"¡Nos alegramos de que le haya gustado esta respuesta!","feedbackFirstMessage":"Sentimos que no le haya resultado útil. ¿Puede informarme más sobre lo que no le gustó?","feedbackPromptMessage":"Gracias por el aviso. Envíe sus comentarios para que el equipo de Bing pueda echar un vistazo.","feedbackPromptGiveFeedback":"Proporcionar comentarios","feedbackPromptGiveFeedbackAriaLabel":"Proporcionar comentarios","feedbackShareConversationQuery":"¿Cuál es la nueva Bing?","feedbackThanksMessageV2":"Gracias por los comentarios. Vuelve a empezar o actualiza la página.","feedbackFormTitleText":"¿Por qué no le ha resultado útil?","feedbackFormInputDefaultText":"Agregue más detalles aquí. Para ayudar a proteger su privacidad, no incluya información personal como su nombre o dirección de correo electrónico.","feedbackFormLegalText":"¿Problema legal o de directiva?","feedbackFormPrivacyText":"Al pulsar Enviar, sus comentarios se utilizarán para mejorar los productos y servicios de Microsoft. Los administradores de TI de su organización podrán ver y administrar los datos de sus comentarios.","feedbackFormUserConsentText":"¿Compartir mi mensaje con Copilot, la respuesta que he recibido de Copiloty los datos de diagnóstico para esta interacción?","feedbackFormUnderstandDataSharedText":"Conozca mejor lo que comparte como datos de diagnóstico","feedbackFormUserConsentYesText":"Sí","feedbackFormUserConsentNoText":"No","feedbackFormReportAConcern":"Informar de un problema","feedbackFormPrivacyStatement":"Declaración de privacidad","feedbackFormNotificationTitleText":"Comentarios enviados","feedbackFormNotificationBodyText":"¡Gracias por ayudar a Bing a mejorar!","feedbackFormThanksMessage":"¡Gracias por los comentarios!","feedbackFormReturnToChatMessage":"Volver al chat","inlineFeedbackShownAriaLabelPrefix":"Se muestra la entrada de comentarios del mensaje","inlineFeedbackSearchOnBingText":"Prueba a buscar en Bing:","serpFeedbackFormTitleText":"Ayúdenos a mejorar la experiencia","serpFeedbackFormInputDefaultText":"Escriba sus comentarios aquí. Para ayudar a proteger su privacidad, no incluya información personal, como su nombre o dirección de correo electrónico.","serpFeedbackFormScreenshot":"Incluir esta captura de pantalla","serpFeedbackFormRadioLike":"Me gusta","serpFeedbackFormRadioDislike":"No me gusta","serpFeedbackFormRadioConnectionIssues":"Problemas de conexión","serpFeedbackFormRadioSlowResponses":"Respuestas lentas","serpFeedbackFormRadioOther":"Otros","compliantPositiveFeedbackFormTitleText":"Enviar comentarios a Microsoft","compliantPositiveFeedbackQuestionText":"¿Qué te ha gustado?","compliantPositiveFeedbackDefaultText":"No incluyas datos personales","compliantNegativeFeedbackFormTitleText":"Enviar comentarios detallados a Microsoft","compliantNegativeFeedbackQuestionText":"¿Qué ha ido mal?","compliantNegativeFeedbackCategoryText":"El contenido es","compliantFeedbackThanksText":"¡Gracias por los comentarios!","compliantFeedbackCategoryInaccurate":"Impreciso","compliantFeedbackCategoryOffensive":"Ofensivo o inadecuado","compliantFeedbackCategoryOther":"Otros","shareConversation":"Compartir toda la conversación","messageLearnMore":"Más información:","messageLearnMoreV2":"Más información","messagePrivacyLabel":"Los datos personales y de la empresa están protegidos en este chat","messageReference":"Referencia","messageReferences":"Referencias","messageSharedContent":"Contenido compartido","messageReceivedAriaLabelPrefix":"Mensaje recibido","messageReportedOffensiveAndRemoved":"Este mensaje se ha quitado porque se ha notificado para realizarle una revisión.","metaInternalSearchQuery":"Buscando: `{0}`","metaInternalLoaderMessage":"Generando respuestas para usted...","metaInternalImageLoaderMessage":"Análisis de la imagen: el desenfoque de privacidad oculta caras de Copilot","metaInternalFileAnalyzeLoaderMessage":"Analizando el archivo: `{0}`","metaInternalFileReadLoaderMessage":"Leyendo el archivo: `{0}`","metaInternalGptCreatorUpdateNameMessage":"Actualizando el nombre de GPT de Copilot","metaInternalGptCreatorUpdateProfileMessage":"Actualizando el perfil de GPT de Copilot","metaInternalGptCreatorNameUpdatedMessage":"Nombre de GPT de Copilot actualizado","metaInternalGptCreatorProfileUpdatedMessage":"Perfil de GPT de Copilot actualizado","messageNotebookPlaceholder":"Copilot creará su contenido aquí","messageActionsCopy":"Copiar","messageActionsCopying":"Copiando","messageActionsCopied":"Copiado","messageActionsCopyError":"Error","messageActionsReport":"Informar","messageActionsEditResponse":"Editar","mobileUpsellTitle1":"Continuar en el teléfono","mobileUpsellMessage1":"Digitalice este código QR para continuar con esta conversación en su aplicación móvil de Bing","mobileUpsellTitle2":"Usar la aplicación Bing para hacer una foto","mobileUpsellMessage2":"Escanea el código QR para hacer la foto con la aplicación Bing en tu dispositivo móvil","injectedActionCardTitle":"¿Chatear acerca de esta página?","injectedActionCardConfirm":"Enviar","injectedActionCardDeny":"Ignorar","tooltipPositive":"Me gusta","tooltipNegative":"No me gusta","tooltipShare":"Compartir","tooltipMore":"Más","tooltipSendToPhone":"Continuar en el teléfono","raiDisclaimerLabel":"El contenido generado por inteligencia artificial puede ser incorrecto","newTopicPrompt":"Nuevo chat le permite comenzar una conversación nueva con Bing sobre cualquier tema","newTopicGreet1":"¡Gracias! Siempre es útil saber cuándo estás listo para continuar. ¿Qué puedo responder ahora?","newTopicGreet2":"Siempre es genial empezar de cero. Pregúntame lo que quieras.","newTopicGreet3":"Por supuesto, me complace empezar de nuevo. ¿Con qué puedo ayudarte ahora?","newTopicGreet4":"Claro, estoy listo para un nuevo desafío. ¿Qué puedo hacer por ti ahora?","newTopicGreet5":"Genial, vamos a cambiar el tema. ¿Qué tienes en mente?","newTopicGreet6":"No te preocupes, me complace probar algo nuevo. ¿Qué puedo responder ahora?","newTopicGreet7":"De acuerdo, estoy listo. Gracias por el reinicio. ¿Sobre qué deberíamos aprender?","newTopicGreet8":"¡Te agradezco la actualización! ¿Tienes algún tema nuevo en mente?","newTopicGreet9":"Entendido, empecemos de nuevo. ¿De qué deberíamos hablar a continuación?","newTopicGreet10":"¡A por otra cosa! ¿Qué puedo hacer por ti?","newTopicGreet11":"De acuerdo, estoy listo para un nuevo tema. ¿Qué deberíamos descubrir juntos?","newTopicGreet12":"Entendido, seguimos adelante. Gracias por decírmelo. ¿En qué puedo ayudarte?","newTopicSugg1":"Enséñame una palabra nueva","newTopicSugg2":"¿Cómo establecer objetivos factibles?","newTopicSugg3":"Enséñame sobre el aterrizaje lunar","newTopicSugg4":"¿Cuál es el mamífero más pequeño del mundo?","newTopicSugg5":"¿Cuál es el océano más profundo?","newTopicSugg6":"¿Cuántas horas hay en un año?","newTopicSugg7":"Quiero aprender una nueva habilidad","newTopicSugg8":"Hazme reír","newTopicSugg9":"¿Qué está pasando en las noticias?","newTopicSugg10":"Muéstrame citas inspiradoras","newTopicSugg11":"Dime una broma","newTopicSugg12":"Necesito ayuda con los deberes","newTopicSugg13":"Buscando un libro de no ficción","newTopicSugg14":"¡Inspírame!","newTopicSugg15":"Muéstrame recetas","newTopicSugg16":"¿Cómo puedo crear un presupuesto?","newTopicSugg17":"Enséñame una palabra nueva","newTopicSugg18":"¿Qué tiempo hace?","newTopicSugg19":"Quiero aprender una nueva habilidad","newTopicSugg20":"¿Cómo comenzó el universo?","newTopicSugg21":"¿Por qué los seres humanos necesitan dormir?","newTopicSugg22":"¿Cómo se hornea una tarta?","newTopicSugg23":"¿Por qué los flamencos son rosas?","newTopicSugg24":"¿Cómo funcionan los hologramas?","newTopicSugg25":"¿Cómo se construyeron las pirámides?","newUserGreet":"Hola, soy Bing. Uso la inteligencia artificial para ayudarte a escribir contenido, encontrar respuestas y chatear sobre las cosas que te interesan. ¿Qué tienes en mente?","newUserSugg1":"Buscar vídeos","newUserSugg2":"Jugar a preguntas y respuestas","newUserSugg3":"Cuénteme un dato divertido","repeatUserGreet":"¡Me alegra que haya vuelto! Estoy abierto a todo lo que quieras hablar. ¿Qué puedo hacer por ti?","repeatUserSugg1":"¿Quién te ha hecho?","repeatUserSugg2":"Crear un poema","repeatUserSugg3":"Mostrarme algo genial","bufferMessage1":"Entendido, espera un segundo...","bufferMessage2":"Te he escuchado, dame un momento...","bufferMessage3":"De acuerdo, voy a procesar esto muy rápido...","configurePanelFileUploadButton":"Carga de archivos","configurePanelNamePlaceHolder":"Asigne un nombre a su GPT de Copilot","configurePanelDescriptionPlaceHolder":"Describa brevemente lo que hace este GPT de Copilot","configurePanelInstructionsPlaceHolder":"Indique al GPT de Copilot cómo se comporta. ¿Qué reglas debe seguir? ¿Para qué sirve? ¿Responde con un estilo determinado?","configurePanelName":"Nombre","configurePanelNameAriaLabel":"Establecer un nombre para el GPT de Copilot","configurePanelDescription":"Descripción","configurePanelDescriptionAriaLabel":"Establecer una descripción para el GPT de Copilot","configurePanelInstructions":"Instrucciones","configurePanelInstructionsAriaLabel":"Establecer instrucciones para el GPT de Copilot","configurePanelCapabilities":"Funcionalidades","configurePanelWebSearch":"Exploración web","configurePanelIsWebSearchEnabledAriaLabel":"habilitar o deshabilitar la funcionalidad de búsqueda web","configurePanelImageGenerator":"Generación de imágenes DALL-E","configurePanelIsImageGeneratorEnableAriaLabel":"habilitar o deshabilitar la funcionalidad del generador de imágenes","configurePanelCodeInterpreter":"Intérprete de código","configurePanelIsCodeInterpreterEnableAriaLabel":"habilitar o deshabilitar la funcionalidad del intérprete de código","configurePanelKnowledge":"Conocimiento","configurePanelSaveButton":"Guardar cambios","configurePanelSaveSuccess":"Guardado correcto","configurePanelSaveFailure":"Error al guardar","configurePanelSaveLoading":"Guardando, espera un momento...","configurePanelAffirmationTips":"Al pulsar \u0022Publicar\u0022, entiendo que si opto por compartir mi GPT de Copilot, el nombre asociado a mi cuenta de Microsoft y cualquier dato confidencial o personal que cargue o escriba en mi GPT, puede estar disponible públicamente o compartirse con el destinatario de mi GPT. También confirmo que tengo todos los derechos, permisos y autorizaciones necesarios para crear este GPT y que este GPT, las instrucciones de GPT y los archivos adjuntos cumplen con el Código de conducta, los Términos y la Directiva de icrosoft Copilot y no infringirán ni fomentarán la infracción de los derechos de terceros (incluidos los derechos de propiedad intelectual, marca comercial o publicidad).","configurePanelUploadTips":"Al cargar los archivos, certifico que tengo el derecho de crear el GPT de Copilot que no infringe ningún derecho de propiedad intelectual de terceros","configurePanelEmptyWarningTips":"{0} no puede estar vacío.","gptCreatorDeleteConfirm":"Eliminar","gptCreatorDeleteQuestion":"¿Estás seguro de que quieres eliminarlo?","gptCreatorDeleting":"Eliminación en curso. Espera…","gptCreatorDeleteFailed":"Error al eliminar. Inténtalo de nuevo","gptCreatorDeleteSucceeded":"Eliminación correcta","gptCreatorDeleteCanceled":"Eliminación cancelada","gptCreatorDeleteCancel":"Cancelar","gptCreatorLoadEditedGptFailure":"Error al cargar GPT de Copilot","gptCreatorLoadGptFailure":"There\u0027s a problem with that Copilot GPT","gptCreatorPrivacyTermsStatement":"¡Hola! Aquí, puede crear GGT de Copilot a través del chat. Solo tienes que dictar, formular preguntas y corregirme si tengo algún problema. Al continuar tu interacción conmigo, estás aceptando la {0} y confirmas que has revisado la {1}. ","gptCreatorTipsTitle":"Sugerencias para crear un GPT de Copilot de calidad:","gptCreatorTipsEnd":"¡Vamos a empezar a crear!","gptCreatorTip1":"Prueba con un nombre corto y pegadizo que describa su función.","gptCreatorTip2":"Utiliza un lenguaje claro y sin ambigüedades. Evita las siglas de nicho, la jerga técnica o el vocabulario demasiado complejo.","gptCreatorTip3":"Haga que el aviso sea específico y procesable, para que el GPT Copilot sepa exactamente lo que quiere que haga. Puedes aportar ejemplos, contexto o limitaciones que sirvan de guía.","gptCreatorTip4":"Utiliza preguntas o afirmaciones que sean relevantes para el usuario y la tarea en cuestión. También puede utilizar palabras clave o frases que el GPT Copilot probablemente reconozca y asocie con la respuesta deseada.","gptCreatorTip5":"Asegúrese de que tiene los derechos necesarios sobre cualquier contenido, carga o instrucciones y utilizados para crear su Copilot GPT.","gptCreatorHeader":"Configuración de GPT de Copilot","gptCreatorConfigurePanel":"Configurar","gptCreatorCreatePanel":"Crear","gptCreatorPublishButton":"Publicar","gptCreatorCopyButtonLabel":"Copiar","gptCreatorPublishDropdownTitle":"Guardar y publicar en","gptCreatorConfirm":"Confirmar","gptCreatorPublishTypeOnlyMe":"Solo yo","gptCreatorPublishTypeWithLink":"Todos los usuarios con un vínculo","gptCreatorPublished":"¡Publicado!","gptCreatorPublishSucceeded":"El GPT de Copilot se publicó correctamente","gptCreatorPublishFailed":"Error al publicar el GPT de Copilot","gptCreatorPublishLoading":"Publicando GPT de Copilot, espera un momento...","gptCreatorOnlyVisitToMe":"Solo visible para mí","gptCreatorViewGpt":"Ver GPT de Copilot","gptCreatorSeeAll":"Ver todos los GGT de Copilot","gptCreatorDialogTitle":"Todos los GPT de Copilot","gptCreatorListTitle":"Mis GPO de Copilot","gptCreatorAddGptName":"Crear un nuevo GPT de Copilot","gptCreatorAddGptDescription":"Usa la herramienta configurar o crear para crear un GPT de Copilot personalizado que puedas mantener privado o compartir.","gptCreatorDescriptionTitle":"Descripción","gptCreatorPreviewButton":"Vista previa de GPT de Copilot","gptCreatorDeleteButtonText":"Eliminar","gptCreatorEditButtonText":"Editar","gptCreatorChatButtonText":"Introducción","gptCreatorPreviewText":"Selecciona un GPT de Copilot para obtener una vista previa aquí","sydneyWindowsCopilotUseTerms":"Condiciones de uso","notebookDisclaimer":"Microsoft Copilot funciona con tecnología de IA, por lo que es posible que se produzcan sorpresas y errores.","notiChatEnd":"Finalizó el tema del chat.","notiRestartChat":"Iniciar un nuevo tema en {0} horas","notiUpdateBrowser":"Lo sentimos, hemos tenido algunos problemas.","notificationDisengaged":"Puede que sea el momento de pasar a un tema nuevo.","notificationForbidden":"Parece que ya no tiene acceso a esta característica. Compruébelo de nuevo en unos días.","notificationResetConversation":"Hemos detectado un problema.","notificationResetConversationCta":"Empecemos de nuevo.","notificationClientIdChangedInNotebook":"El bloc de notas ha expirado.","notificationClientIdChangedInNotebookCta":"iniciar un nuevo tema","notificationMaxTurnsPerDay":"Has alcanzado el límite de mensajes que puedes enviar a Bing en el plazo de 24 horas. Vuelve pronto.","notificationMaxTurnsPerHour":"Ha alcanzado el límite de mensajes que puede enviar a Bing en una hora. ¡Vuelva a echar un vistazo más tarde!","notificationMaxTurnsPerConversation":"Esta conversación ha alcanzado su límite. Usa el botón \u0022escoba\u0022 para borrar esto y chatear más.","notificationMaxTurnsPerConversationInNotebook":"Lo siento, creo que tenemos que continuar. Use el botón \u0022nuevo tema\u0022 de la izquierda para borrar el contenido y empezar de cero.","notificationMaxTurnsForMuidUsers":"Ha alcanzado el límite de conversaciones para hoy.","notificationMaxTurnsForInPrivateUsers":"Has alcanzado el límite de conversaciones para el modo inPrivate. Abrir una ventana que no sea inPrivate para continuar con el chat","notificationProcessing":"Un momento mientras intentamos conectarnos","notificationTooManyRequests":"Lo sentimos, estamos experimentando una demanda excepcionalmente alta. Vuelva a intentarlo más tarde.","notificationUnauthorized":"Parece que se ha cerrado la sesión.","notificationUnauthorizedCta":"Vuelva a iniciar sesión","notificationConversationLimitMissingDOB":"Es posible que tengas que confirmar tu fecha de nacimiento para seguir chateando.","notificationConversationLimitMissingDOBCta":"Actualiza la cuenta aquí.","notificationConversationLimitChildUser":"Bing Chat no está disponible para esta cuenta.","notificationConversationLimitChildUserCta":"Explora la web en bing.com.","notificationUnauthorizedMuidUsersCta":"Inicie sesión para continuar con el chat.","notificationAttemptingToReconnect":"Intentando volver a conectar...","notificationLostConnection":"Parece que se ha perdido la conexión.","notificationLostConnectionCta":"¿Deseas intentar conectar de nuevo?","notificationMessageError":"Parece que se interrumpió el último mensaje.","notificationMessageErrorInNotebook":"Parece que se interrumpió el último mensaje.","notificationSharedTip":"Esta conversación \u003cstrong\u003e\u0022{0}\u0022\u003c/strong\u003e se compartió contigo en {1}","notificationNetworkOffline":"Lo sentimos, no está conectado. Compruebe la conexión de red y vuelva a intentarlo.","notificationAutoRefreshConversation":"Se ha producido un error. Espere mientras lo resolvemos.","notificationAutoResendOnConnectionError":"La conexión se ha perdido momentáneamente. Espere mientras nos volvemos a conectar","notificationAutoRecoverOnMessageError":"Lo sentimos, parece que su último mensaje fue interrumpido. Espere mientras lo resolvemos.","notificationAutoRecoverOnMessageErrorInNotebook":"Lo sentimos, parece que se interrumpió el último mensaje. Espere mientras lo resolvemos.","notificationContinueRecovering":"Todavía estoy resolviendo el problema, espere.","creativeGreet":"Seamos creativos. ¿Tiene algún tema en mente o deberíamos improvisar?","balancedGreet":"Suena bien, podemos equilibrar la diversión y los hechos. ¿Cómo puedo ayudar?","preciseGreet":"Gracias por chatear. ¿Cómo puedo ayudarle hoy?","creativeSugg1":"Escribamos un poema de ritmo","creativeSugg2":"Escribir una carta de amor a las tormentas","creativeSugg3":"Dime mi horóscopo","balancedSugg1":"Dame ideas para una escapada de fin de semana","balancedSugg2":"¿Cómo hacen las abejas la miel?","balancedSugg3":"Escriba un poema sobre las palomitas de maíz","preciseSugg1":"Necesito ayuda con la investigación","preciseSugg2":"Muéstreme cómo resolver un cubo de rubik","preciseSugg3":"Hazme una lista de la compra para esta noche","threadsTitle":"Chats","threadsTitleForFlux":"Menú","threadsRecentTitle":"Actividad reciente","threadsRecentTitleShort":"Recientes","threadsSavedTitle":"Guardado","threadsSeeAllRecent":"Ver todos los chats recientes","threadsSeeAllSaved":"Ver todos los chats guardados","threadsDefaultChatName":"Chat sin título","threadsDaysAgo":"Hace {0} días","threadsYesterday":"Ayer","threadsMinimizeRecent":"Minimizar chats recientes","threadsMinimizeSaved":"Minimizar chats guardados","threadsEdit":"Cambiar nombre","threadsDelete":"Eliminar","threadsConfirm":"Confirmar","threadsCancel":"Cancelar","threadsChatHistory":"Historial de chats","threadsLoadChatHistory":"Cargar historial de chats","threadsPreviewText":"Vuelve a tus chats recientes","threadsDescription":"Tu copiloto de Bing con tecnología de IA ahora tiene guardado automático de chats. Puedes acceder a tus chats desde cualquier dispositivo, de escritorio o móvil, y reanudarlos sin problemas.","threadsSharedOnDate":"Compartido en {0}","threadsLoad":"Cargar chat","threadsExportPanelTitle":"Seleccionar formato","threadsExportPanelAriaLabel":"Seleccionar","threads2TLastAccessed":"Última respuesta en","threads2TInfoChanged":"Es probable que esta información haya cambiado desde","threads2TArchivedText":"Captura de pantalla","threads2TReloadDescription":"Haga clic en Volver a cargar para mostrar la información más reciente de la Web","threads2TReload":"Buscar en Bing.com","threads2TFailedSearch":"Ver lo último en Bing.com","threadsAADMSARedirectDescription":"El historial de conversaciones se está guardando con tu cuenta de Microsoft. Estará aquí la próxima vez que inicie sesión con su cuenta personal.","threadsAADMSARedirectConfirm":"Cambiar de perfil","privateThreadConvDefaultTitle":"Conversación no guardada","threadsPreview":"Vista previa","threadsMore":"Más","threadsDescriptionNote":"Ten en cuenta que todos los chats se eliminarán automáticamente en un plazo de 90 días a menos que se guarden específicamente.","signInCta":"Iniciar sesión ","signInDescription":" para formular más preguntas y tener conversaciones más largas","signInDescriptionInPrivate":"Abrir una ventana que no sea inPrivate para tener conversaciones más largas o hacer más preguntas","pluginEnabledPlugin":"Habilitado","pluginEnabledNumber":"{0} / {1}","pluginTitle":"Complementos","pluginStartNewTopic":"Iniciar un nuevo tema","pluginToChangePlugin":"para cambiar complementos","pluginNotificationMaxExceeded":"Número máximo de complementos habilitados","pluginNotificationMaxExceededV2":"El número máximo de complementos está habilitado.","pluginNotificationExclusive":"{0} es demasiado similar a {1}. Elija uno","pluginNotificationExclusiveV2":"{0} y {1} pueden hacer cosas similares. Elija una.","pluginTerms":"Condiciones de uso","pluginPrivacy":"Directiva de privacidad","pluginPanelFreTitle":"Complementos ya disponibles","pluginPanelFreTextLabel":"Nuevo:","pluginPanelFreText":"Activa los complementos para buscar reservas de restaurantes, vuelos y mucho más de tus marcas favoritas.","pluginPanelFreDismiss":"Descartar notificaciones de complementos","pluginLimitationTitle":"Uso de complementos con Bing AI","pluginLimitationMax":"Elija hasta 3 complementos para cualquier hilo de conversación.","pluginLimitationMaxV2":"Elija hasta {0} complementos para una conversación.","pluginLimitationLock":"Los complementos están bloqueados en una conversación hasta que seleccione Nuevo tema para hacer cambios.","pluginLimitationLockV2":"Para cambiar los complementos después de iniciar una conversación, seleccione {0}.","pluginLimitationDataShared":"Los datos se compartirán con los complementos que elija.","pluginPanelAlertTitle":"Los complementos no están disponibles en modo \u0022Equilibrado\u0022","pluginPanelAlertCta":"Restablecer la conversación ","pluginPanelAlertSubtitle":"y usa el estilo de conversación \u0022Más creativo\u0022 o \u0022Más preciso\u0022 y vuelve a intentarlo.","pluginPanelNolimit":"Al deshabilitar {0}, se deshabilitarán todos los complementos habilitados","pluginRevocationReason":"Este complemento está deshabilitado temporalmente debido a una infracción de las directivas de Microsoft","pluginSearchBoxPlaceholder":"Buscar complementos de chat...","pluginSearchResultEmpty":"No plugins to show","activatetoUsePlugins":"Activa {0} para usar los complementos","threadsToggleExpansion":"Alternancia de expansión de complemento habilitada para subprocesos","threadsEnabledPlugins":"Complementos habilitados:","bookNowWithOpenTable":"Reserve ahora con OpenTable","scrollLeft":"Desplazar a la izquierda","scrollRight":"Desplazar a la derecha","bingSearchDisabled":"Search **deshabilitado**.","bingSearchDisabledforMessage":"Búsqueda desactivada","activateSearch":"Iniciar un tema nuevo y activar la búsqueda","webPageContextPrefix":"Sitio visitado","useGPT4SwitchLabel":"Usar GPT-4","switchGPT4Label":"GPT-4","switchGPT4TurboLabel":"GPT-4 Turbo","switchSelectedText":"Se ha seleccionado {0}","slimHeaderTitle":"Consulta las respuestas del nuevo Bing con tecnología de inteligencia artificial","inputRaiLabel":"","personalizationConsentTitleText":"Las conversaciones están personalizadas para usted","personalizationConsentTitleTextEuOn":"La personalización está activada","personalizationConsentTitleTextEuOff":"Personalización desactivado","personalizationConsentContentText1":"Copilot usa información del historial de chats para que las conversaciones sean únicas para ti.","personalizationConsentContentText1Eu":"Cuando compartas tu historial de chats, Copilot adaptará las respuestas a ti y a tus intereses. ","personalizationConsentContentText2":".","personalizationConsentContentSettingsText":"Desactive la personalización en cualquier momento en la configuración de Bing.","personalizationConsentLearnMoreText":"Más información en Preguntas más frecuentes","personalizationConsentLearnMoreTextEu":"Más información sobre las respuestas personalizadas","personalizationConsentSetEuText1":"Para modificar, visita el","personalizationConsentSetEuLinkText":"Configuración de Bing Chat","personalizationConsentSetEuText2":"página","personalizationConsentTurnOnText":"Activar","personalizationConsentTurnOffText":"No activar","autosaveConsentTitle":"Active el guardado automático para volver a los chats","autosaveConsentBody":"Sus chats no se guardan automáticamente. Para acceder a conversaciones anteriores a través de dispositivos, utilice el guardado automático.","autosaveConsentNote":"Tenga en cuenta que este ajuste borrará la conversación actual.","autosaveConsentAccept":"Activar el guardado automático","autosaveConsentDeny":"No","autosaveOffBanner":"El guardado automático está desactivado","personalConsentTitle":"Activar la personalización para obtener mejores respuestas","personalConsentBody":"Permitir que Bing utilice información de conversaciones recientes del chat de Bing para ofrecer respuestas más personalizadas.","personalConsentAccept":"Activar","personalConsentDeny":"No","personalOffBanner":"La personalización está desactivada","personalOnBanner":"La personalización está actualmente activada","personalOnUndoBanner":"Se ha activado la personalización","personalOffUndoBanner":"Se ha desactivado la personalización","personalConsentUndo":"Deshacer","personalConsentTurnOff":"Desactivar","starterPageTitle":"Bing es el copiloto con tecnología de IA para la web","starterDescription":"Chatee con Bing con sus propias palabras. Si se siente atascado, seleccione uno de estos inicios de conversación para ver cómo funciona Bing:","starterSubtitle1":"Tener una conversación","starterSubtitle2":"Libere su creatividad","starterSubtitle3":"Buscar de forma más inteligente","starterContent1":"Bing funciona mejor cuando usa expresiones naturales y realiza seguimientos, al igual que un chat diario","starterContent2":"Bing puede poner en marcha su creatividad convirtiendo sus visiones en una realidad, ya sea con imágenes o palabras","starterContent3":"Chatear con Bing es una nueva forma de buscar. Deje que Bing resuma la web y le proporcione respuestas","starterChatQuery1":"¿Crees que hay vida en otros planetas? ¿Qué hace que la Tierra sea única?","starterChatQuery2":"¿Qué ocurre si tengo problemas para poder dormir, pero solo en verano?","starterChatQuery3":"Estoy pensando en hacer un jardín, pero no sé cómo. ¿Cuál es el primer paso?","starterChatQuery4":"¿Crees que un caimán o un oso pardo ganarían en una pelea?","starterChatQuery5":"He estado leyendo mucha no ficción últimamente. ¿Puedes ayudarme a encontrar las versiones de ficción de los temas que me gustan?","starterChatQuery6":"Vamos a tener una conversación en la que todo lo que diga esté escrito con emojis.","starterChatQuery7":"He estado pensando en mudarme a un nuevo lugar. ¿A dónde crees que debería ir? ","starterChatQuery8":"Solía escribir un diario y, últimamente, he estado algo vago. ¿Cómo puedo mantener el hábito?","starterChatQuery9":"¿Sueñas como los seres humanos? ¿Con qué sueñas?","starterChatQuery10":"¿Es posible que se produzcan a la vez un terremoto, un tsunami y una erupción volcánica?","starterCreateQuery1":"Tengo un cachorro de pelo negro, excepto por su pelo blanco en la barriga. Dibújala como un dibujo animado.","starterCreateQuery2":"Escribe una historia para ir a dormir para mi sobrina de 3 años. Le gustan los caballos y la magia, y creo que debería rimar\u0022","starterCreateQuery3":"Redactar un breve ensayo que argumente que la luna es mejor que el sol, con la voz de un profesor creído","starterCreateQuery4":"Pinta un oso polar tomando el sol en un iceberg al estilo impresionista. También debe estar bebiendo una limonada","starterCreateQuery5":"Vamos a inventar un color y escribir una publicación en las redes sociales que lo presente al mundo","starterCreateQuery6":"Escribiendo como Batman con la actuación de Christian Bale, dime cómo luchar contra el tráfico yendo en bicicleta","starterCreateQuery7":"Desarrolle un lanzamiento de ventas para una nueva marca de bebida con la voz de un padre que la preparó en casa y provocó unas pequeñas explosiones.","starterCreateQuery8":"Diseñar un póster para una casa de café que refleje una puesta de sol y haga un buen uso del espacio negativo","starterCreateQuery9":"Dibuja una jirafa como capitán de un pontón en un lago","starterCreateQuery10":"Escribir el párrafo de introducción de un artículo sobre la historia de la literatura femenina y dar cinco indicaciones para continuar escribiendo","starterCompareQuery1":"Búscame una receta de tiramisú, haz una lista de la compra y organízala para que pueda conseguir los ingredientes rápidamente en la tienda","starterCompareQuery2":"Hazme un resumen de las noticias de hoy, solo en mi área local, excluidos los deportes\u0022","starterCompareQuery3":"Resumir la investigación con IA más reciente en cinco puntos de viñeta y sacar una conclusión sobre lo que viene a continuación","starterCompareQuery4":"Crear una tabla comparando los mejores vehículos eléctricos por marca, precio, características de seguridad y complementos posibles","starterCompareQuery5":"¿Dónde está el parque más cercano que tenga un área de juegos para niños?","starterCompareQuery6":"Dame tres recomendaciones de vinos para una comida italiana que voy a preparar esta noche. Hecha a base de tomate y un poco picante.","starterCompareQuery7":"Proporcióname una sinopsis del enfoque más reciente de la ONU","starterCompareQuery8":"Crear una tabla comparando a los ganadores del premio Pulitzer que fueron una sorpresa y por qué","starterCompareQuery9":"Explicar cómo funciona Internet de una forma que un niño pueda entender","starterCompareQuery10":"Traducir el poema \u0022Introducción a la Poesía\u0022 al francés y explicar brevemente, en inglés, cualquier pérdida de significado","notificationMaxTurnsAndOpenEdge":"Has alcanzado el límite de conversaciones. Para continuar el chat,","notificationMaxTurnsAndOpenEdgeCta":"Abrir en Microsoft Edge","deleteAllAria":"Eliminar todo el historial de chats","deleteAll":"Eliminar todo el historial de chats","deleteAllMobile":"Eliminar todo","newTopic":"Nuevo tema","moreActions":"Más acciones","chatHistory":"Historial de chats","menu":"Menú","usePreciseLocation":"Actualizar la ubicación","basedOnLocation":"Ubicación: {0}, {1}","basedOnYourLocation":"En función de su ubicación","locationFetchErrorMessage":"Se ha denegado el permiso","locationLearnMore":"(Más información)","pageTitleText":"Bing Chat con GPT-4","sydneyCarouselCollapse":"Contraer","sydneyCarouselTitle":"Temas de chat recientes","actionBarOngoingConvPlaceholder":"Escribir mensaje","captchaTitle":"Comprueba tu identidad","captchaDescription":"Para continuar, escriba los caracteres de la imagen siguiente.","captchaPlaceholder":"Escriba los caracteres...","captchaPlaceholderError":"Lo sentimos, vuelve a intentarlo","warmWelcomeAriaLabel":"Te damos la bienvenida al nuevo Bing","workToggleDescription":"y lo que incluyen los resultados","actionBarTextInputAriaLabel":"Preguntar a Bing","welcomeLearnMessage2":"Bing Chat procesa las palabras que escribe en Windows Copilot para generar respuestas basadas en inteligencia artificial y mejorar su experiencia en Windows.","typingIndicatorStopStreamingAriaLabel":"Detener el streaming","edit":"Editar","compliantMetaInternalLoaderMessage":"Generación de una respuesta segura a partir de {0}","actionBarVisualSearchCoachMarkText":"Análisis de imágenes con IA","feedbackLinkShareTitle":"Aquí está una respuesta que he obtenido con Microsoft Copilot, el primer motor de respuestas con tecnología de inteligencia artificial del mundo. Seleccione esta opción para ver la respuesta completa o pruébela usted mismo.","sydneySapphireConsentDenyText":"Denegar","zeroInputSuggestionFallback1":"¿Cuál es el molinillo de café mejor valorado?","zeroInputSuggestionFallback2":"¿Cuáles son los tres mejores vehículos para una familia de seis con un presupuesto bajo?","zeroInputSuggestionFallback3":"Escribe un broma que le pueda parecer divertida a mis compañeros de trabajo","zeroInputSuggestionFallback4":"¿Cuál es la siguiente tendencia de la moda y dónde debo comprar para encontrarla?","zeroInputSuggestionFallback5":"Escribe una fábula original sobre un pez y una rana que encuentran el amor.","zeroInputSuggestionFallback6":"Crea una tabla que me ayude a planear comidas para las próximas dos semanas","zeroInputSuggestionFallback7":"Crear un eslogan para una nueva plataforma de redes sociales especializada en sarcasmo","zeroInputSuggestionCategory1":"Tienda","zeroInputSuggestionCategory2":"Comparar","zeroInputSuggestionCategory3":"Risa","zeroInputSuggestionCategory4":"Chat","zeroInputSuggestionCategory5":"Escribir","zeroInputSuggestionCategory6":"Organizar","zeroInputSuggestionCategory7":"Crear","morePromptsLink":"Ver más sugerencias","zeroInputDescription":"Bing es el copiloto con tecnología de IA para la web","freeSydneyPersonaTitle":"GPO de Copilot","freeSydneyPersonaMenuTitle":"Chats","freeSydneyAriaButtonText":"cargado","freeSydneyWelcomeAuthorText":"Por {0}","sydneyGptCreatorWelcomeMessage":"Este GPT de Copilot fue realizado por {0}. Las sorpresas y los errores son posibles. Tus comentarios son bienvenidos.","freeSydneyPersonaCopilotWelcomeUpsellTitle":"Bing es el copiloto con tecnología de IA para la web","freeSydneyPersonaCopilotDescription":"Le ofrecemos un equilibrio entre la inteligencia artificial y la web","freeSydneyPersonaSydneyWelcomeUpsellTitle":"Sydney es su complemento de IA original","freeSydneyPersonaSydneyDescription":"He sido un buen Bing.","freeSydneyPersonaDesignerWelcomeUpsellTitle":"Crea cualquier imagen que puedas imaginar","freeSydneyPersonaDesignerDescription":"Creando imágenes a partir de palabras","freeSydneyPersonaDesignerPromptOne":"Una imagen de arcilla de un colorido festival de linternas nocturnas iluminando el cielo","freeSydneyPersonaDesignerPromptTwo":"Imagen de un flamenco corriendo por las calles mientras escucha música","freeSydneyPersonaDesignerPromptThree":"Imagen espacial de un astronauta esquiando en la luna con la Tierra en el fondo","freeSydneyWelcomeTilesWriteTitle":"Escribir","freeSydneyWelcomeTilesWriteDescription":"Escribir las indicaciones del escenario para una batalla entre Sydney y Bing","freeSydneyWelcomeTilesChatTitle":"Chat","freeSydneyWelcomeTilesChatDescription":"¿Dónde has estado, Sydney?","freeSydneyWelcomeTilesLaughTitle":"Risa","freeSydneyWelcomeTilesLaughDescription":"Cuéntame un desastre que no tenga sentido","freeSydneyWelcomeTilesWriteDescription2":"Dame una lista de nuevos pasatiempos que podría realizar con tiempo libre limitado.","freeSydneyWelcomeTilesCompareTitle":"Comparar","freeSydneyWelcomeTilesCompareDescription":"Estoy pensando en hacer un cambio profesional. ¿Puede ayudarme a examinar los pros y los contras?","freeSydneyWelcomeTilesOrganizeTitle":"Organizar","freeSydneyWelcomeTilesOrganizeDescription":"Crea una tabla que me ayude a planear comidas para las próximas dos semanas","freeSydneyWelcomeTilesImageTitle":"Seleccionar para probar","customGptWelcomeTilesQuestionDescription":"¿Qué tipo de preguntas puedo formular?","customGptWelcomeTilesListDescription":"Cuénteme 5 cosas sobre usted","customGptWelcomeTilesSummarizeDescription":"Dame una idea sobre qué tipo de GPT eres","sydneyCopilotProGetCopilotPro":"Obtener Copilot Pro","sydneyCopilotProSuperchargeAIExperience":"Mejore su experiencia con la inteligencia artificial","sunoPolicyText":"La solicitud de canción, incluidos los datos personales que contiene, se compartirá con Suno. Use géneros y acentos para describir su solicitud en lugar de nombres de artistas específicos. Puede crear hasta 5 canciones al día.","sunoNonSignedInGreetings1":" para crear tus propias canciones con Suno. Esto habilitará el complemento Suno y el ","sunoNonSignedInGreetings2":" y ","sunoNonSignedInGreetings3":" se aplicará.","sunoNonSignedInGreetingsSignIn":"Iniciar sesión en Copilot","sunoCachedMessage":"Compón una canción al estilo de Bollywood cruzado con Country para mi padre, que se jubila como profesor.","thirdPartyBingChatAttribution":"Resultados de Bing con tecnología de inteligencia artificial","thirdPartyRAIDisclaimer":"Las sorpresas y los errores son posibles","thirdPartyPrivacyHyperlink":"Privacidad","thirdPartyTermsHyperlink":"Términos"};;var chatBNPData ={"RawUrl":"/search?q=autojs\u0026form=WSHBSH\u0026qs=HS\u0026cvid=e321066b41dc426184a064d75924a313\u0026pq=\u0026cc=MX\u0026setlang=es-MX\u0026nclid=AC1E1DBB5EAEDDBC2F9B5D4F0F557FAC\u0026ts=1708643909987\u0026wsso=Moderate\u0026showconv=0","Referer":"","NotificationsPartnerId":"Chat","IID":"SERP.5649"};;sj_evt.bind("chs_init", ()=>{ChatHomeScreen.init("turing/api/suggestions/v2/zeroinputstarter");}, 1);;var sj_appHTML=function(n,t){var f,e,o,i,r,s,h;if(t&&n){var c="innerHTML",l="script",a="appendChild",v="length",y="src",p=sj_ce,u=p("div");if(u[c]="<br>"+t,f=u.childNodes,u.removeChild(f[0]),e=u.getElementsByTagName(l),e)for(o=0;o<e[v];o++)i=p(l),r=e[o],r&&(i.type="text/javascript",s=r.getAttribute(y),s?(i.setAttribute(y,s),i.setAttribute("crossorigin","anonymous")):(i.text=r[c],i.setAttribute("data-bing-script","1")),r.parentNode.replaceChild(i,r));for(h=_d.createDocumentFragment();f[v];)h[a](f[0]);n[a](h)}};(function() { function PostLoadWSinit() { SydWelcomPostLoad.initRequest("SERP.5650"); sj_evt.unbind("swsldinit", PostLoadWSinit); } sj_evt.bind("swsldinit", PostLoadWSinit, 1); })();;var syd_loc_config ={"locations":{"locationsList":[{"SourceType":1,"RegionType":2,"Center":{"Latitude":19.722299575805664,"Longitude":-101.19429779052734},"Radius":24902,"Name":"Morelia, Michoacan","Accuracy":24902,"FDConfidence":0.5,"CountryName":"Mexico","CountryConfidence":8,"Admin1Name":"Michoacan","PopulatedPlaceName":"Morelia","PopulatedPlaceConfidence":5,"PostCodeName":"58117","UtcOffset":-6,"Dma":0}]}};(function(){ sj_evt.bind("sydFSC.init",function() { var SydFSCModule = false ? SydneyFullScreenConvMob : SydneyFullScreenConv; if (SydFSCModule && SydFSCModule.initWithWaitlistUpdate){ SydFSCModule.initWithWaitlistUpdate(syd_loc_config, 10); } }, true); })();;var ContextRegionPresent = true;;(function(){typeof ContextRegionPresent=="undefined"&&(ContextRegionPresent=!0);ContextRegionPresent&&sj_b!=null?(Lib.CssClass.remove(sj_b,"b_norr"),_w.sj_log&&sj_log("Info","HasRR","1")):sj_b!=null&&Lib.CssClass.add(sj_b,"b_norr")})();sa_CTBConfig={"CU":"https://cc.bingj.com/cache.aspx?q=autojs\u0026d={0}\u0026mkt=es-MX\u0026setlang=es-MX\u0026w={1}","CT":"En caché","NW":"0","disableMetaData":"0","GenCapString":"Subtítulo generado por IA","useOffsetParent":"1","TRGT":"Acciones para este sitio","selectedIconClass":"sw_ddw","toolboxTriggerClassName":"c_tlbxTrg","staticIconClass":"sw_ddgn"};var WireUp,WireConnector,SignalConnector;(function(n){function f(n){var u=[],f,t,i,r,e;if(!n)return u;for(f=n.split(","),t=0;t<f.length;++t)(i=f[t],i&&0!==i.length)&&(r=i.split("/"),e={ci:r[0],cp:r[1],ct:r[2]},u.push(e));return u}function e(n){var t=n.split(";");return{type:t[0],name:t[1],cons:f(t[2])}}function o(n){for(var t,r,u={eps:{},w:!1,id:""},f=n.split("|"),i=0;i<f.length;++i)t=f[i],t&&t.length>0&&(r=e(t),u.eps[r.name]=r);return u}function s(n,t){for(var u=null,o=_d.querySelectorAll,r,f,e,u=o?o.call(_d,"["+n+"]"):_d.getElementsByTagName("*"),i=0;i<u.length;i++)r=u[i],f=r.getAttribute,f&&(e=f.call(r,n),e&&t(r,e))}function h(n,i,r,u){if(t.cis[i]){u.nodeHandler&&u.nodeHandler(i,n,r,t.cis[i]);return}t.cis[i]={};u.logicHandler(i,t.cis[i]);u.nodeHandler&&u.nodeHandler(i,n,r,t.cis[i])}function c(){t={nc:0,nodes:{},chs:{},ihs:{},cis:{}};n.g=t}function i(){var r="data-wire",n,i;s(r,function(n,i){n.id||(n.id="wire"+t.nc++);var u=o(i);u.id=n.id;t.nodes[n.id]=u;n.removeAttribute(r)});for(n in t.nodes)t.nodes.hasOwnProperty(n)&&(i=t.nodes[n],i.w||l(i))}function l(n){var u,t,e,o,i,f;if(n&&n.eps&&(u=_ge(n.id),u)){t=n.eps;e=!0;for(i in t)t.hasOwnProperty(i)&&(f=t[i],o=r(u,f,!0),o||(e=!1));if(e){for(i in t)t.hasOwnProperty(i)&&(f=t[i],r(u,f,!1));n.w=!0}}}function r(n,i,r){var e,o,u,f;if(!i)return!1;if(i.type==="I"){if(e=t.ihs[i.name],r)return e&&!0;e&&e(n)}if(i.type==="F"||i.type==="B")for(o=0;o<i.cons.length;++o)if(u=i.cons[o],u.cp&&u.ct){if(f=t.chs[u.ct],f&&h(n,u.ci,u.cp,f),r)return f&&!0;if(!f)throw""+u.ct+" not registered";}return!0}function a(n,r){t.ihs[n]=r;i()}function u(n,i,r){var s=n&&t.nodes[n.id],e,o,u,f,h;if(s&&(e=s.eps,e)&&(o=e[i],o)&&(u=o.cons,u))for(f=0;f<u.length;++f)h=u[f],r(h)}function v(n,t,i){n&&u(n,t,function(t){sj_evt.fire(t.ci+"_"+t.cp,n,null,i)})}function y(n,t,i){n&&u(n,t,function(t){sj_evt.bind(t.ci+"_"+t.cp,function(t){var u=t[1],r=t[2],f=t[3];r&&r.id!==n.id||i(n,u,r,f)})})}function p(n,r,u){u===void 0&&(u=null);t.chs[n]={logicHandler:r,nodeHandler:u};i()}var t=n.g||{nc:0,nodes:{},chs:{},ihs:{},cis:{}};n.g=t;n.init=a;n.setValue=v;n.onUpdate=y;n.register=p;sj_evt.bind("unload",c,!0);sj_evt.bind("onP1",i,!0)})(WireUp||(WireUp={})),function(){WireUp.register("wire",function(n){sj_evt.bind(n+"_i",function(t){var i=t[1],u=t[2],r=t[3];sj_evt.fire(n+"_o",i,null,r)})})}(WireConnector||(WireConnector={})),function(){WireUp.register("signal",function(n){sj_evt.bind(n+"_i",function(t){var i=t[1],r=t[2],u=t[3];sj_evt.fire(n+"_o",i,null,!0)})})}(SignalConnector||(SignalConnector={}));sj_evt.bind("tab_init", function() { tabcontrol.init("tabcontrol_5_4E8B10","Ahora mismo no podemos encontrar más información sobre esta página",false,0,0,531,752,false,false,0,0,10,false,false,true,true); }, 1);;var rprData ={"InsertId":"SERP.5505","IsInstrumentation":false,"RecordFetchSent":false,"PageSize":6,"FeatureSuppressionClassNames":[]};;var SelectorHelper;(function(n){function t(n,t){var i,r;if(n.matches)return n.matches(t);if(n.webkitMatchesSelector)return n.webkitMatchesSelector(t);if(n.mozMatchesSelector)return n.mozMatchesSelector(t);if(n.msMatchesSelector)return n.msMatchesSelector(t);if(i=_d.querySelectorAll(t),i)for(r=0;r<i.length;r++)if(i[r]===n)return!0;return!1}function i(n,i){var r=n;do r=r.parentElement;while(r!=null&&!t(r,i));return r}n.selectorMatches=t;n.findFirstAncestorWithSelector=i})(SelectorHelper||(SelectorHelper={}));var SlideExpansionData ={"enableBodyWidthOverride":true};;sj_evt.bind("chevrons_init", function() { if(typeof Chevrons_GradientChevronCF != "undefined"){ Chevrons_GradientChevronCF.init("slideexp0_4F5BD7", _ge("slideexp0_4F5BD7chevrons_prevbtn"), _ge("slideexp0_4F5BD7chevrons_nextbtn"),false); } else if(typeof Chevrons_ProminentChevronSepar != "undefined"){ Chevrons_ProminentChevronSepar.init("slideexp0_4F5BD7", _ge("slideexp0_4F5BD7chevrons_prevbtn"), _ge("slideexp0_4F5BD7chevrons_nextbtn"),false,true); } else if(typeof Chevrons_FaultInjection != "undefined"){ Chevrons_FaultInjection.init("slideexp0_4F5BD7", _ge("slideexp0_4F5BD7chevrons_prevbtn"), _ge("slideexp0_4F5BD7chevrons_nextbtn"),false,true); } else{ Chevrons.init("slideexp0_4F5BD7", _ge("slideexp0_4F5BD7chevrons_prevbtn"), _ge("slideexp0_4F5BD7chevrons_nextbtn"),false,true,"slideexp0_4F5BD7chevrons"); } }, 1);;(function() { function InitSlide() { var params = ["0_4F5BD7","slideexp0_4F5BD7",true,8,"Ahora mismo no podemos encontrar más información sobre esta página",true,false,-1,false,true,false,false,true,true,true,false,false,false,]; if (_ge('slideexp0_4F5BD7')) { if (typeof(SmartRendering) !== 'undefined') { SmartRendering.LoadElementWhenDisplayed(this, _ge('slideexp0_4F5BD7'), SlideExperienceLite.init, params); } else { SlideExperienceLite.init.apply(this, params); } } sj_evt.unbind("slideexperiencelite_init", InitSlide); } sj_evt.bind("slideexperiencelite_init", InitSlide, 1); })();;_w.mmLog=function(n,t){var r=mmLogUrl(n,t),u="sendBeacon",i=!0,f,e;if(navigator&&navigator[u])try{f=navigator[u](r,"");i=!f}catch(o){i=!0}return i&&(e=new Image,e.src=r),!0};_w.mmLogUrl=function(n,t){t=t||_G.IG;var i=[_G.hst,"/fd/ls/ls.gif?IG=",t,"&Type=Event.ClientInst&DATA=",n,"&log=UserEvent"];return i.join("")};_w["BingChatCustomGreetingString"] ="{\"messages\":[{\"text\":\"¡Hola, soy Copilot! Soy el nuevo modo de chat de Microsoft impulsado por la inteligencia artificial que puede ayudarte a obtener rápidamente **respuestas resumidas** e **inspiración creativa.** 🙌\",\"author\":\"bot\",\"sourceAttributions\":[],\"feedback\":{\"type\":\"None\"},\"contentOrigin\":\"AnnRules\",\"adaptiveCards\":[{\"type\":\"AdaptiveCard\",\"version\":\"1.0\",\"body\":[{\"type\":\"TextBlock\",\"text\":\"¡Hola, soy Copilot! Soy el nuevo modo de chat de Microsoft impulsado por la inteligencia artificial que puede ayudarte a obtener rápidamente **respuestas resumidas** e **inspiración creativa.** 🙌\",\"wrap\":true}]}],\"suggestedResponses\":[{\"text\":\"cómo puedo dormir mejor\",\"author\":\"user\",\"createdAt\":\"2023-01-20T19:24:37.5530372+00:00\",\"timestamp\":\"2023-01-20T19:24:37.5530372+00:00\",\"messageId\":\"96edad9d-8585-4ff3-a5df-7bfbe2282574\",\"messageType\":\"Suggestion\",\"offense\":\"Unknown\",\"feedback\":{\"type\":\"None\"},\"contentOrigin\":\"AnnRules\"},{\"text\":\"Escribe una historia divertida sobre una rana\",\"author\":\"user\",\"createdAt\":\"2023-01-20T19:24:37.5532484+00:00\",\"timestamp\":\"2023-01-20T19:24:37.5532484+00:00\",\"messageId\":\"61301d30-be5e-4870-b630-46557936c8d5\",\"messageType\":\"Suggestion\",\"offense\":\"Unknown\",\"feedback\":{\"type\":\"None\"},\"contentOrigin\":\"AnnRules\"},{\"text\":\"crea un itinerario detallado de 4 días para visitar París organizado en una tabla.\",\"author\":\"user\",\"createdAt\":\"2023-01-20T19:24:37.5532559+00:00\",\"timestamp\":\"2023-01-20T19:24:37.5532559+00:00\",\"messageId\":\"74de64cd-f4c7-49cf-a299-04076e1e6750\",\"messageType\":\"Suggestion\",\"offense\":\"Unknown\",\"feedback\":{\"type\":\"None\"},\"contentOrigin\":\"AnnRules\"}],\"spokenText\":\"¡Hola, soy Copilot!. Soy el nuevo modo de chat de Microsoft impulsado por la inteligencia artificial que puede ayudarte a obtener rápidamente respuestas resumidas e inspiración creativa.\"}]}"; _w["BingChatEnableClickToChat"] =true; _w["BingChatAlternativeRendering"] ="";;var __assign=this&&this.__assign||function(){return __assign=Object.assign||function(n){for(var t,r,i=1,u=arguments.length;i<u;i++){t=arguments[i];for(r in t)Object.prototype.hasOwnProperty.call(t,r)&&(n[r]=t[r])}return n},__assign.apply(this,arguments)},__spreadArray=this&&this.__spreadArray||function(n,t,i){if(i||arguments.length===2)for(var r=0,f=t.length,u;r<f;r++)!u&&r in t||(u||(u=Array.prototype.slice.call(t,0,r)),u[r]=t[r]);return n.concat(u||Array.prototype.slice.call(t))},LGUtility;(function(n){function ri(){sj_evt.bind(c,ui,!0);sj_evt.bind("bfbMainlineAnswerRemoved",lt,!0);sj_evt.bind("bfbPreExpanded",lt,!0);sj_evt.bind(ni,fi,!0)}function ui(n){var f,o,s;try{if(n[2]){var t=n[1],h=_ge(e(t)),r=h.getBoundingClientRect();ii&&!r.height?(f={name:"InvalidRender",message:"Widget did not have a height during render event"},et(t,f)):(o=u[t],u[t]={name:t,top:r.top+_w.scrollY,bottom:r.bottom+_w.scrollY},o?(s=vt(t),i.splice(s,1),at(t)):at(t))}ut(n[1],!1)}catch(c){d=!0;et(n[1],c)}}function fi(){ct||(pt(ti),ct=!0)}function lt(){for(var t,n=0;n<i.length;n++)if(t=i[n].name,s[t]){wt(t);return}}function at(n){for(var f,e,r=0,t=0;t<i.length;t++){if(f=i[t].top,e=u[n].top,e<f){r=t;i.splice(r,0,u[n]);return}r=t}i.splice(r+1,0,u[n])}function vt(n){for(var t=0;t<i.length;t++)if(i[t].name===n)return t;return null}function ut(n,t){s[n]?t?p[n]=!0:delete p[n]:h[n]&&(t?l[n]=!0:delete l[n]);var i=Object.keys(p).length,r=Object.keys(l).length;!t&&i===0&&r>0&&k>0&&yt()&&ft();sj_evt.fire(gt)}function yt(){for(var t=Object.keys(l),n=0;n<t.length;n++)if(!w[t[n]])return!1;return!0}function ft(){if(rt)setTimeout(function(){ft()},0);else{rt=!0;var n=g.shift(),t=w[n]&&w[n]();sj_evt.fire(c,n,!!t);!t||k--;rt=!1}}function ei(){for(var i,t=Object.keys(l),n=0;n<t.length;n++)i=h[t[n]],g[i]=t[n];g=g.filter(Boolean)}function pt(n){for(var r,t,o,f=Object.keys(h),i=0;i<f.length;i++)r=f[i],u[r]&&(t=_ge(e(r)),t&&(o=parseInt(t.style.top.slice(0,-2)),t.style.top=o+n+"px"))}function e(n){return ot[n]?ot[n]:n+"Container"}function oi(){return p}function wt(n){if(!n||!u[n]||!e(n)||!_ge(e(n)))return null;var t=u[n].bottom,i=_ge(e(n)),r=i.getBoundingClientRect().bottom+_w.scrollY,f=r-t;pt(f)}function si(n,t){if(w[n]=t,yt()){ei();var i=Object.keys(p).length,r=Object.keys(l).length;i===0&&r>0&&k>0&&ft()}}function hi(n,t){var r,e,o;return!d&&u[t]?(r=vt(t),e=i[r].bottom,i[r+1]?(o=i[r+1].top,o-e+f>n):!0):!1}function ci(n,t){var s,e,l,h,w;if(t===void 0&&(t=!1),!b||!n||d)return null;var k=_ge("b_sydConvCont"),p=Lib.CssClass.contains(document.body,"b_sydConvMode"),u=ht&&ht.getBoundingClientRect(),a=_ge("b_content"),r=a&&a.getBoundingClientRect(),v=b===null||b===void 0?void 0:b.getBoundingClientRect(),c=_ge("b_scope_container"),g=c&&typeof c!="undefined",y=c&&c.getBoundingClientRect(),o=!t&&u&&u.bottom||(p?r===null||r===void 0?void 0:r.top:g?y===null||y===void 0?void 0:y.bottom:v===null||v===void 0?void 0:v.bottom);for(o+=_w.scrollY,s=null,e=o+f,l=0;l<i.length;l++){if(h=i[l],!s&&h.top-(o+f)>=n){e=o+f;break}else if(s&&h.top-(s.bottom+f)>=n){e=s.bottom+f;break}s=h;e=h.bottom+f}return(w=r&&_w.getComputedStyle(a).position=="relative"?(r===null||r===void 0?void 0:r.top)+_w.scrollY:0,k&&p)?!t&&(u===null||u===void 0?void 0:u.height)?e-o+(u.height+((u===null||u===void 0?void 0:u.top)-(r===null||r===void 0?void 0:r.top))):e-o:e-w}function li(n,t){var i=_ge(e(n));if(!u[n]||!i||!i.parentElement||d)return null;var o=i.parentElement.getBoundingClientRect(),r=i.getBoundingClientRect(),s=r.top-o.top;return o.bottom-(r.bottom+f)>=t?r.height+s+f:null}function ai(n){var t=o[n];n&&t!=undefined&&y[n]&&((v===null||t<=o[v])&&(v=n),r!==null&&y[r].imbeds[v]?y[n].imbeds[v]&&(r=t<o[r]?n:r):r=v,ut(n,!0),di(n))}function vi(n){if(bt()||!ki(n))return sj_evt.fire(c,n,!1),!1;if(a!=null&&a!=n)return kt(n,a),sj_evt.fire(c,n,!1),!1;var t=y[r],i=_ge(e(n));return({wholepgtemp:!0,LeftGutterDisambig:!0}[n]||i)&&t&&!t.imbeds[n]?t.shared[n]||r===n||s[r]&&s[n]||h[n]&&k>0?!0:(kt(n,r),sj_evt.fire(c,n,!1),!1):(sj_evt.fire(c,n,!1),!1)}function yi(n){return!!y[r].imbeds[n]}function pi(n){return st[n]}function wi(n,t){st[n]=t;sj_evt.fire(dt,n)}function bi(n){var u=n&&n.getAttribute("h"),i={appNS:null,kValue:null},t,r;return u&&(t=u.split(","),t&&t.length>=2&&(r=t[0].split("="),i.appNS=r&&r[1],i.kValue=t[1])),i}function bt(){var t,i,r,u,n;return((i=(t=_w.location)===null||t===void 0?void 0:t.search)===null||i===void 0?void 0:i.indexOf("showselans"))>-1&&typeof URLSearchParams!="undefined"?(n=new URLSearchParams(_w.location.search),n.get("showselans")==="1"):((u=(r=_w.location)===null||r===void 0?void 0:r.search)===null||u===void 0?void 0:u.indexOf("showonlyads"))>-1&&typeof URLSearchParams!="undefined"?(n=new URLSearchParams(_w.location.search),n.get("showonlyads")==="1"):!1}function ki(n){var t=Lib.CssClass.contains(document.body,"b_dark");return t?!!it[n]:!0}function di(n){if(tt[n]){var t=o[n];(a==null||t<o[a])&&(a=n)}}function kt(n,t){Log&&Log.Log&&Log.Log("LeftGutterUtility",n,"LGSuppressed",!1,"SupressingFeature",t)}function et(n,t){var i="Error Name: "+t.name+"\nError Message: "+t.message;Log&&Log.Log&&Log.Log("LeftGutterUtility",n,"error",!1,"errorName",t.name,"errorDesc",i)}function gi(n,t,i,r){for(var f=[],u=4;u<arguments.length;u++)f[u-4]=arguments[u];nt.apply(void 0,__spreadArray([n,"ContainerClick",t,i,r],f,!1))}function nr(n,t,i,r){for(var f=[],u=4;u<arguments.length;u++)f[u-4]=arguments[u];nt.apply(void 0,__spreadArray([n,"ContainerHover",t,i,r],f,!1))}function tr(n,t,i,r){nt(n,"ContainerRender",t,i,r)}function nt(n,t,i,r,u){for(var e=[],f=5;f<arguments.length;f++)e[f-5]=arguments[f];r&&u?Log&&Log.Log&&Log.Log.apply(Log,__spreadArray(["LeftGutterModule",n,t,!1,"AppNS",r,"K",u,"Category","CommonControls","AnswerType",i],e,!1)):Log&&Log.Log&&Log.Log.apply(Log,__spreadArray(["LeftGutterModule",n,t,!1,"AnswerType",i],e,!1))}function ir(n){var t=_ge(e(n));t&&t.parentElement&&t.parentElement.removeChild(t);ut(n,!1)}var o,s,h,tt,it;(function(n){n[n.shopWPFilters=0]="shopWPFilters";n[n.shopFiltersWidget=1]="shopFiltersWidget";n[n.wholepgtemp=2]="wholepgtemp";n[n.financeWidget=3]="financeWidget";n[n.lgRealEstate=4]="lgRealEstate";n[n.lgRealEstateSA=5]="lgRealEstateSA";n[n.lgREAds=6]="lgREAds";n[n.lgAds=7]="lgAds";n[n.lgTravelAds=8]="lgTravelAds";n[n.HotelsLGWidget=9]="HotelsLGWidget";n[n.textAdsWidget=10]="textAdsWidget";n[n.productAdsFilterLGW=11]="productAdsFilterLGW";n[n.shopWidget=12]="shopWidget";n[n.RichDeveloperGutterWidget=13]="RichDeveloperGutterWidget";n[n.LGCashbackEnrollWidget=14]="LGCashbackEnrollWidget";n[n.textDeeplinksWidget=15]="textDeeplinksWidget";n[n.nowCastIcon=16]="nowCastIcon";n[n.destLGW=17]="destLGW";n[n.XMOfferWidget=18]="XMOfferWidget";n[n.GigServicesWidget=19]="GigServicesWidget";n[n.AirlineWidget=20]="AirlineWidget";n[n.FlightWidget=21]="FlightWidget";n[n.HealthWidget=22]="HealthWidget";n[n.TopicsLGWidget=23]="TopicsLGWidget";n[n.AutosMarketplaceWidget=24]="AutosMarketplaceWidget";n[n.PromotionalGutterWidget=25]="PromotionalGutterWidget";n[n.StockGutterWidget=26]="StockGutterWidget";n[n.localWidget=27]="localWidget";n[n.wikiWidget=28]="wikiWidget";n[n.wptMagazineWikiWidget=29]="wptMagazineWikiWidget";n[n.lgWptNav=30]="lgWptNav";n[n.newsWidget=31]="newsWidget";n[n.richCalendarWidget=32]="richCalendarWidget";n[n.richcardWidget=33]="richcardWidget";n[n.lgImgAns=34]="lgImgAns";n[n.lgVidAns=35]="lgVidAns";n[n.lgAnsFeedbackMop=36]="lgAnsFeedbackMop";n[n.lgAnsFeedbackTop=37]="lgAnsFeedbackTop";n[n.segmentWidget=38]="segmentWidget";n[n.flightTimeWidget=39]="flightTimeWidget";n[n.whereToWatchWidget=40]="whereToWatchWidget";n[n.lgShopFilter=41]="lgShopFilter";n[n.sydneyLetsChatWidget=42]="sydneyLetsChatWidget";n[n.lgWeather=43]="lgWeather";n[n.lgWeatherAds=44]="lgWeatherAds";n[n.listNavWidget=45]="listNavWidget";n[n.ShoppingAnswerPALGFilter=46]="ShoppingAnswerPALGFilter";n[n.ShoppingAnswerMALGFilter=47]="ShoppingAnswerMALGFilter";n[n.ShoppingAnswerLGFilter=48]="ShoppingAnswerLGFilter";n[n.sapphireUpsell=49]="sapphireUpsell";n[n.lgJobListings=50]="lgJobListings";n[n.lgCashback=51]="lgCashback";n[n.lgLocalListing=52]="lgLocalListing";n[n.UnifiedAnswerGutterWidget=53]="UnifiedAnswerGutterWidget";n[n.lgHighBingAtWork=54]="lgHighBingAtWork";n[n.lgMedBingAtWork=55]="lgMedBingAtWork";n[n.lgLcgBingAtWork=56]="lgLcgBingAtWork";n[n.EdgeWorkspacesWidget=57]="EdgeWorkspacesWidget";n[n.lgCreator=58]="lgCreator";n[n.lgDarkModeUpsell=59]="lgDarkModeUpsell";n[n.lgWeatherUpsell=60]="lgWeatherUpsell";n[n.listNavPAWidget=61]="listNavPAWidget";n[n.lgRecipe=62]="lgRecipe";n[n.shopPageWidget=63]="shopPageWidget";n[n.LeftGutterDisambig=64]="LeftGutterDisambig";n[n.LGPopDomains=65]="LGPopDomains";n[n.relatedSearches=66]="relatedSearches";n[n.miniSidebar=67]="miniSidebar";n[n.LGOlympicMedals=68]="LGOlympicMedals";n[n.savingsAnswerNav=69]="savingsAnswerNav";n[n.brandUpSell=70]="brandUpSell";n[n.AlgoWidget_0=71]="AlgoWidget_0";n[n.AlgoWidget_1=72]="AlgoWidget_1";n[n.AlgoWidget_2=73]="AlgoWidget_2";n[n.AlgoWidget_3=74]="AlgoWidget_3";n[n.AlgoWidget_4=75]="AlgoWidget_4";n[n.AlgoWidget_5=76]="AlgoWidget_5";n[n.AlgoWidget_6=77]="AlgoWidget_6";n[n.AlgoWidget_7=78]="AlgoWidget_7";n[n.AlgoWidget_8=79]="AlgoWidget_8";n[n.AlgoWidget_9=80]="AlgoWidget_9"})(o||(o={})),function(n){n[n.wholepgtemp=1]="wholepgtemp";n[n.financeWidget=2]="financeWidget";n[n.lgRealEstate=3]="lgRealEstate";n[n.lgRealEstateSA=4]="lgRealEstateSA";n[n.lgREAds=5]="lgREAds";n[n.lgAds=6]="lgAds";n[n.lgTravelAds=7]="lgTravelAds";n[n.HotelsLGWidget=8]="HotelsLGWidget";n[n.textAdsWidget=9]="textAdsWidget";n[n.productAdsFilterLGW=10]="productAdsFilterLGW";n[n.RichDeveloperGutterWidget=11]="RichDeveloperGutterWidget";n[n.textDeeplinksWidget=12]="textDeeplinksWidget";n[n.shopWidget=13]="shopWidget";n[n.nowCastIcon=14]="nowCastIcon";n[n.destLGW=15]="destLGW";n[n.XMOfferWidget=16]="XMOfferWidget";n[n.GigServicesWidget=17]="GigServicesWidget";n[n.AirlineWidget=18]="AirlineWidget";n[n.FlightWidget=19]="FlightWidget";n[n.HealthWidget=20]="HealthWidget";n[n.TopicsLGWidget=21]="TopicsLGWidget";n[n.PromotionalGutterWidget=22]="PromotionalGutterWidget";n[n.AutosMarketplaceWidget=23]="AutosMarketplaceWidget";n[n.StockGutterWidget=24]="StockGutterWidget";n[n.localWidget=25]="localWidget";n[n.wikiWidget=26]="wikiWidget";n[n.wptMagazineWikiWidget=27]="wptMagazineWikiWidget";n[n.lgWptNav=28]="lgWptNav";n[n.newsWidget=29]="newsWidget";n[n.richCalendarWidget=30]="richCalendarWidget";n[n.richcardWidget=31]="richcardWidget";n[n.lgImgAns=32]="lgImgAns";n[n.lgVidAns=33]="lgVidAns";n[n.lgAnsFeedbackMop=34]="lgAnsFeedbackMop";n[n.lgAnsFeedbackTop=35]="lgAnsFeedbackTop";n[n.segmentWidget=36]="segmentWidget";n[n.flightTimeWidget=37]="flightTimeWidget";n[n.sydneyLetsChatWidget=38]="sydneyLetsChatWidget";n[n.lgWeather=39]="lgWeather";n[n.whereToWatchWidget=40]="whereToWatchWidget";n[n.lgShopFilter=41]="lgShopFilter";n[n.lgWeatherAds=42]="lgWeatherAds";n[n.lgCompUpsell=43]="lgCompUpsell";n[n.listNavWidget=44]="listNavWidget";n[n.listNavPAWidget=45]="listNavPAWidget";n[n.savingsAnswerNav=46]="savingsAnswerNav";n[n.ShoppingAnswerPALGFilter=47]="ShoppingAnswerPALGFilter";n[n.ShoppingAnswerMALGFilter=48]="ShoppingAnswerMALGFilter";n[n.ShoppingAnswerLGFilter=49]="ShoppingAnswerLGFilter";n[n.lgCashback=50]="lgCashback";n[n.brandUpSell=51]="brandUpSell";n[n.sapphireUpsell=52]="sapphireUpsell";n[n.lgJobListings=53]="lgJobListings";n[n.LGCashbackEnrollWidget=54]="LGCashbackEnrollWidget";n[n.AlgoWidget_0=55]="AlgoWidget_0";n[n.AlgoWidget_1=56]="AlgoWidget_1";n[n.AlgoWidget_2=57]="AlgoWidget_2";n[n.AlgoWidget_3=58]="AlgoWidget_3";n[n.AlgoWidget_4=59]="AlgoWidget_4";n[n.AlgoWidget_5=60]="AlgoWidget_5";n[n.AlgoWidget_6=61]="AlgoWidget_6";n[n.AlgoWidget_7=62]="AlgoWidget_7";n[n.AlgoWidget_8=63]="AlgoWidget_8";n[n.AlgoWidget_9=64]="AlgoWidget_9";n[n.lgLocalListing=65]="lgLocalListing";n[n.UnifiedAnswerGutterWidget=66]="UnifiedAnswerGutterWidget";n[n.lgHighBingAtWork=67]="lgHighBingAtWork";n[n.lgMedBingAtWork=68]="lgMedBingAtWork";n[n.lgLcgBingAtWork=69]="lgLcgBingAtWork"}(s||(s={})),function(n){n[n.lgCreator=1]="lgCreator";n[n.EdgeWorkspacesWidget=2]="EdgeWorkspacesWidget";n[n.lgDarkModeUpsell=3]="lgDarkModeUpsell";n[n.shopWPFilters=4]="shopWPFilters";n[n.shopFiltersWidget=5]="shopFiltersWidget";n[n.lgWeatherUpsell=6]="lgWeatherUpsell";n[n.lgRecipe=7]="lgRecipe";n[n.LeftGutterDisambig=8]="LeftGutterDisambig";n[n.LGPopDomains=9]="LGPopDomains";n[n.shopPageWidget=10]="shopPageWidget"}(h||(h={})),function(n){n[n.shopFiltersWidget=1]="shopFiltersWidget";n[n.shopWPFilters=2]="shopWPFilters"}(tt||(tt={})),function(n){n[n.destLGW=1]="destLGW";n[n.lgDarkModeUpsell=2]="lgDarkModeUpsell";n[n.FlightWidget=3]="FlightWidget";n[n.AirlineWidget=4]="AirlineWidget";n[n.wikiWidget=5]="wikiWidget";n[n.HealthWidget=6]="HealthWidget";n[n.TopicsLGWidget=7]="TopicsLGWidget";n[n.wptMagazineWikiWidget=8]="wptMagazineWikiWidget";n[n.lgWptNav=9]="lgWptNav";n[n.RichDeveloperGutterWidget=10]="RichDeveloperGutterWidget";n[n.segmentWidget=11]="segmentWidget";n[n.XMOfferWidget=12]="XMOfferWidget";n[n.textDeeplinksWidget=13]="textDeeplinksWidget";n[n.lgCashback=14]="lgCashback";n[n.UnifiedAnswerGutterWidget=15]="UnifiedAnswerGutterWidget";n[n.HotelsLGWidget=16]="HotelsLGWidget";n[n.GigServicesWidget=17]="GigServicesWidget";n[n.richCalendarWidget=18]="richCalendarWidget";n[n.lgImgAns=19]="lgImgAns";n[n.lgLocalListing=20]="lgLocalListing";n[n.AlgoWidget_0=21]="AlgoWidget_0";n[n.AlgoWidget_1=22]="AlgoWidget_1";n[n.AlgoWidget_2=23]="AlgoWidget_2";n[n.AlgoWidget_3=24]="AlgoWidget_3";n[n.AlgoWidget_4=25]="AlgoWidget_4";n[n.AlgoWidget_5=26]="AlgoWidget_5";n[n.AlgoWidget_6=27]="AlgoWidget_6";n[n.AlgoWidget_7=28]="AlgoWidget_7";n[n.AlgoWidget_8=29]="AlgoWidget_8";n[n.AlgoWidget_9=30]="AlgoWidget_9";n[n.lgLcgBingAtWork=31]="lgLcgBingAtWork";n[n.textAdsWidget=32]="textAdsWidget";n[n.lgHighBingAtWork=33]="lgHighBingAtWork";n[n.lgMedBingAtWork=34]="lgMedBingAtWork";n[n.savingsAnswerNav=35]="savingsAnswerNav";n[n.lgRecipe=36]="lgRecipe";n[n.lgJobListings=37]="lgJobListings";n[n.lgRealEstate=38]="lgRealEstate";n[n.lgRealEstateSA=39]="lgRealEstateSA";n[n.shopWPFilters=40]="shopWPFilters";n[n.EdgeWorkspacesWidget=41]="EdgeWorkspacesWidget"}(it||(it={}));var ot={wholepgtemp:"wpt_hdrs",LeftGutterDisambig:"b_lgd",relatedSearches:"b_lgrs",miniSidebar:"mnbr_btnCtr"},t={imbeds:{},shared:{}},y={EdgeWorkspacesWidget:__assign({},t),wholepgtemp:__assign({},t),textAdsWidget:__assign({},t),productAdsFilterLGW:__assign({},t),textDeeplinksWidget:__assign({},t),LGCashbackEnrollWidget:__assign({},t),financeWidget:__assign({},t),shopWidget:__assign({},t),shopPageWidget:__assign({},t),shopWPFilters:__assign({},t),shopFiltersWidget:__assign({},t),lgRealEstate:__assign({},t),lgRealEstateSA:__assign({},t),lgREAds:__assign({},t),lgAds:__assign({},t),lgTravelAds:__assign({},t),HotelsLGWidget:__assign({},t),nowCastIcon:__assign({},t),destLGW:__assign({},t),XMOfferWidget:__assign({},t),GigServicesWidget:__assign({},t),FlightWidget:__assign({},t),AirlineWidget:__assign({},t),HealthWidget:__assign({},t),TopicsLGWidget:__assign({},t),localWidget:__assign({},t),wikiWidget:__assign({},t),wptMagazineWikiWidget:__assign({},t),lgWptNav:__assign({},t),newsWidget:__assign({},t),richCalendarWidget:__assign({},t),richcardWidget:__assign({},t),lgImgAns:__assign({},t),lgVidAns:__assign({},t),lgAnsFeedbackTop:__assign({},t),lgAnsFeedbackMop:__assign({},t),PromotionalGutterWidget:__assign({},t),StockGutterWidget:__assign({},t),AutosMarketplaceWidget:__assign({},t),LeftGutterDisambig:__assign({},t),LGPopDomains:__assign({},t),relatedSearches:__assign({},t),miniSidebar:__assign({},t),LGOlympicMedals:__assign({},t),segmentWidget:__assign({},t),lgRecipe:__assign({},t),flightTimeWidget:__assign({},t),sydneyLetsChatWidget:__assign({},t),lgWeather:__assign({},t),lgWeatherUpsell:__assign({},t),lgDarkModeUpsell:__assign({},t),whereToWatchWidget:__assign({},t),lgWeatherAds:__assign({},t),lgCompUpsell:__assign({},t),RichDeveloperGutterWidget:__assign({},t),listNavPAWidget:__assign({},t),listNavWidget:__assign({},t),lgShopFilter:__assign({},t),savingsAnswerNav:__assign({},t),brandUpSell:__assign({},t),lgCreator:__assign({},t),sapphireUpsell:__assign({},t),lgJobListings:__assign({},t),lgCashback:__assign({},t),AlgoWidget_0:__assign({},t),AlgoWidget_1:__assign({},t),AlgoWidget_2:__assign({},t),AlgoWidget_3:__assign({},t),AlgoWidget_4:__assign({},t),AlgoWidget_5:__assign({},t),AlgoWidget_6:__assign({},t),AlgoWidget_7:__assign({},t),AlgoWidget_8:__assign({},t),AlgoWidget_9:__assign({},t),lgLocalListing:__assign({},t),UnifiedAnswerGutterWidget:__assign({},t),lgHighBingAtWork:__assign({},t),lgMedBingAtWork:__assign({},t),lgLcgBingAtWork:__assign({},t),ShoppingAnswerPALGFilter:__assign({},t),ShoppingAnswerMALGFilter:__assign({},t),ShoppingAnswerLGFilter:__assign({},t)},st={},u={},i=[],dt="lgImbedEle",c="lgRender",gt="lgUpdateRegisteredMap",ni="SydAddCarousel",ti=92,f=32,l={},p={},w={},b=_ge("b_header"),ht=_ge("b_pole"),ii=!0,k=1,a=null,r=null,v=null,rt=!1,d=!1,g=[],ct=!1;n.getContainerId=e;n.getRegisteredAnsWidgets=oi;n.alignPageWid=wt;n.setCallback=si;n.hasSpaceBelow=hi;n.getTopForPageWid=ci;n.getTopForAnsWid=li;n.registerFeature=ai;n.isRenderFeature=vi;n.isImbeddedFeature=yi;n.getImbeddedElement=pi;n.setImbeddedElement=wi;n.getInstVals=bi;n.isShowSelectedAns=bt;n.instLogError=et;n.instLogClick=gi;n.instLogHover=nr;n.instLogRender=tr;n.instLog=nt;n.removeWidget=ir;ri();sj_evt.fire("LGUtilityLoaded")})(LGUtility||(LGUtility={}));var webResultsUrls =["3AF775908702E09EC3B2905512A8ACDF","FA368AC5A2227BC4B28FDDB93957173A","DF6DBA7D5937662C65228AAC59179D92","82CA4A43C9B1941EA9A119915F947015","F0F85F71EBD8640BF423AC275E5C78AA","AADD2E1AE5830EEBBF14D4EA11B4F30B","BD727CEBDC3C950AB3F9F13B0D3DB5C4","1A99D69041124049E7AF00E3C8490BDF","26947B5E8ADF7F9B23D58F76DD788F35","C6DE7008B0077049B33163819D554635"];var MiniTPTriggerWidth = 1020; var EnableMiniTPRightRailRS = false; var EnableSuperCaptionContextRegionTrigger = false;;var miniTPExpanded=!1,miniTPEle=_ge("b_mtp"),contextEle=_ge("b_context");(function(){function h(){return parseInt(getComputedStyle(p)[w])}function it(){var n=parseInt(getComputedStyle(p)[w]);return n>0?n:160}function t(){return Math.floor(f?-_w.pageXOffset:_w.pageXOffset)}function c(n){return _w.innerWidth+t()>=MiniTPTriggerWidth+n}function rt(){var r,t,i,u,n;if(c(h()))return!0;if(t=contextEle.childElementCount,t==0||t>2)return!1;for(i=0;i<t;i++)if(u=contextEle.children[i],n=u.firstChild,n&&((r=n.id)===null||r===void 0?void 0:r.indexOf("testhook_"))!=0&&(EnableMiniTPRightRailRS||!Lib.CssClass.contains(n,"b_rrsr"))&&(n.id!="b_exploreRegion"||n.innerHTML!=""))return!1;return!0}function u(){if(!miniTPExpanded){var n=it();o=Math.min(o,t());miniTPEle.offsetTop>e||c(n)||t()>o+80?miniTPEle.offsetTop>e||_w.innerWidth>1274?k():b(!1):miniTPEle.style.width=g(n)}}function b(t){miniTPExpanded||(Lib.CssClass.add(miniTPEle,"mtpslidert"),Lib.CssClass.remove(contextEle,i),sb_st(function(){var r,i;Lib.CssClass.add(contextEle,l);(r=miniTPEle===null||miniTPEle===void 0?void 0:miniTPEle.parentNode)===null||r===void 0?void 0:r.removeChild(miniTPEle);t&&n.scrollWidth>n.offsetWidth&&(i=n.scrollWidth-n.offsetWidth,f&&(i=-i),_w.scrollTo(i,_w.pageYOffset));sj_evt.fire(tt);Lib.CssClass.add(sj_b,a)},300),d())}function ut(){b(!0)}function k(){var n;(n=miniTPEle===null||miniTPEle===void 0?void 0:miniTPEle.parentNode)===null||n===void 0?void 0:n.removeChild(miniTPEle);contextEle.style.opacity="1";contextEle.style.transition="none";Lib.CssClass.remove(contextEle,i);Lib.CssClass.add(sj_b,a);d()}function d(){miniTPExpanded=!0;sj_ue(_w,"resize",u);sj_ue(_w,"scroll",u);sj_evt.fire(nt);_w.sj_log&&sj_log(r,v,"show")}function ft(){var t=new RegExp("[?&]{1}q=([^&]+)"),n=location.search.match(t);return n?n[1]:null}function g(i){var u=_w.innerWidth==n.scrollWidth,r=u?0:Math.min(64,Math.abs(_w.innerWidth-n.offsetWidth)),f=Math.min(i+MiniTPTriggerWidth-r,_w.innerWidth-r+t());return Math.max(s.offsetWidth+92,f-i-658)+"px"}function et(){var t=!1,n;return(miniTPEle.offsetTop>e&&(t=!0,_w.sj_log&&sj_log(r,v,"showonerr")),st())?!1:(n=document.querySelectorAll("#b_context>.b_ans"),!n||n.length==0||t||rt())?(k(),!0):!1}function ot(){var e,o,t,n;Lib.CssClass.remove(miniTPEle,i);miniTPEle.style.width=g(h());var f=null,s=null,a=document.querySelectorAll("#b_context>.b_ans"),c=(e=a[0])===null||e===void 0?void 0:e.querySelectorAll(".b_entityTP .iaplanner");(c===null||c===void 0?void 0:c.length)>0?f="/th?q="+ft()+"&w=40&h=40&c=7&rs=1&p=0&o=5&dpr=1.5&pid=1.7&adlt=moderate&t=1":(t=(o=a[0])===null||o===void 0?void 0:o.querySelectorAll(".b_entityTP .cico img, .utilAns .cico img"),(t===null||t===void 0?void 0:t.length)>0&&(f=t[0].getAttribute("src"),f&&(s=t[0].getAttribute("data-src-hq"))));n=miniTPEle.getElementsByClassName("mtpsvg")[0];n&&f&&(Lib.CssClass.remove(n,"mtparr"),n.setAttribute("src",f),s&&n.setAttribute("data-src-hq",s));n&&Lib.CssClass.add(n,l);sj_be(miniTPEle.lastChild,"click",ut);sj_be(_w,"resize",u);sj_be(_w,"scroll",u);_w.sj_log&&sj_log(r,y,"affordance")}function st(){var n=contextEle.childElementCount;return c(h())?!1:EnableSuperCaptionContextRegionTrigger&&(n===0||n===1&&Lib.CssClass.contains(contextEle.lastChild,"b_super_task_pane_metadata")||n===2&&Lib.CssClass.contains(contextEle.firstChild,"b_rrsr")&&Lib.CssClass.contains(contextEle.lastChild,"b_super_task_pane_metadata"))?!0:!1}var f=_G.RTL,l="crshow",i="crhide",r="CI.MiniTP",nt="hideminitp",tt="minitphidden",a="b_respl",v="FullTP",y="MiniTp",e=600,n=_d.documentElement,p=_ge("b_content"),o=t(),s,w=f?"paddingRight":"paddingLeft";typeof MiniTPTriggerWidth=="undefined"&&(MiniTPTriggerWidth=1020);typeof EnableMiniTPRightRailRS=="undefined"&&(EnableMiniTPRightRailRS=!1);typeof EnableSuperCaptionContextRegionTrigger=="undefined"&&(EnableSuperCaptionContextRegionTrigger=!1);contextEle&&(_w.sj_log&&sj_log(r,y,"show"),miniTPEle!=null&&(Lib.CssClass.remove(contextEle,"crinvis"),Lib.CssClass.add(contextEle,i),miniTPEle.childNodes&&miniTPEle.childNodes.length>0&&(s=miniTPEle.getElementsByClassName("mtpseem")[0],s&&(et()||ot()))))})();var Reference;(function(n){function i(){u()}function r(){for(var n=_d.querySelectorAll(".richrswrapper .richrsrailexpansion .df_showLogo .domain_Logo_RichRS"),i=_d.querySelectorAll(".richrswrapper .richrsrailexpansion .df_showLogo .b_module_expansion_control.b_module_head"),r=_d.querySelectorAll(".richrswrapper .richrsrailexpansion .df_showLogo .b_expandable_inline_container"),u="b_hideFavIcon",f=function(t){sj_be(i[t],"click",function(){r[t].classList.contains("b_hide")?n[t].classList.remove(u):n[t].classList.add(u)})},t=0;t<n.length&&i.length&&r.length;t++)f(t)}function u(){var n=_d.querySelector(".richrswrapper");sj_be(n,"mouseenter",f);sj_be(n,"mouseleave",e)}function f(){var n=o();this.setAttribute("mouseEventId",n);Log.Log(t,"richRS.Answer.Card.MouseEnter","UserMouseEnter",!1,"MouseEventId",n)}function e(){var n=this.getAttribute("mouseEventId");n&&(this.removeAttribute("mouseEventId"),Log.Log(t,"richRS.Answer.Card.MouseLeave","UserMouseLeave",!1,"MouseEventId",n))}function o(){var n=function(){return Math.floor((1+Math.random())*65536).toString(16).substring(1,5)};return[n(),n(),"-",n(),"-",n(),"-",n(),"-",n(),n(),n()].join("")}var t="richRSAnswerCardMouseEvent";n.domainLogoClickHandler=r;i()})(Reference||(Reference={}));Reference.domainLogoClickHandler();;var StickyRRRSManager;(function(n){function l(n){h>=0&&(t&&s==0&&t.clientWidth!==0&&i.parentElement.style.position=="sticky"&&(i.parentElement.style.top=-(t.clientHeight+10)+"px",s=t.clientWidth),r&&r.offsetHeight<o.offsetHeight&&(_w.sj_log&&sj_log("CI.DOS","StickyRRRSLog","1"),n||(r.style.height=o.clientHeight+"px",t?(i.parentElement.style.position="sticky",i.parentElement.style.top=-(t.clientHeight+10)+"px"):(i.parentElement.style.position="sticky",i.parentElement.style.top="5px"))))}var r=_ge("b_context"),o=_ge("b_results"),i=r.getElementsByClassName("richrswrapper")[0],t=r.getElementsByClassName("rs_chat")[0],s=t?t.clientWidth:0,f=document.querySelectorAll("#b_context .b_ans"),h=-1,c=-1,e,u;if(f&&f.length>=1)for(e=f[f.length-1],c=e.childElementCount,u=0;u<c;++u)if(e.children[u].getAttribute("class").indexOf("richrswrapper")>=0){h=u;break}n.scrollRelatedSearch=l;sj_be(_w,"scroll",l)})(StickyRRRSManager||(StickyRRRSManager={}));if (StickyRRRSManager) { if (StickyRRRSManager.scrollRelatedSearch) { StickyRRRSManager.scrollRelatedSearch(false); } };var ipd = { ipt: "4", secall: true, pd: false };var fbpkgiid = fbpkgiid || {}; fbpkgiid.page = 'SERP.5660';;sj_evt.bind("ajax.feedback.initialized", function(args) { args[1].debugCollector.setContextValue("FederationDebugInfo", "QueryID : 89294fa8b8cb4a46bfa5930ab6dcbdbb"); });;sa_config={"f":"sb_form","i":"sb_form_q","c":"sw_as","zisParent":"sa_zis_parent","aadManageHistoryUrl":"/profile/history?FORM=O2HV46","u":"%2fAS%2fSuggestions%3fpt%3dpage.serp%26bq%3dautojs%26mkt%3des-mx%26qry%3d","partner":"page.serp","ghostingWithGreyText":"NwBasedGhosting","enableTabPrompt":"","enabledDataSources":["Web","SYC"],"eHC":1,"asdhml":1,"ePN":1,"fetchOnEmpty":1,"fetchOnDownArrowKey":1,"t":1,"enableSbUpdate":1,"asmobbacks":1,"longquery":1,"dsarialv":1,"aslogtel":1,"asszrdc":1,"eNw":1,"nwLim":1,"IsRequeryPresent":1,"ezis":1,"enableAlwaysUpdateQFC":1,"imeghmb":1,"leftIconOnSuggestions":1,"EnableHistoryIcon":1,"leftIconOnMsbSuggestions":1,"submitOnEnter":1,"waitForCompositionEnd":1,"enableClientPerfLatencyCalc":1,"asLowNetworkScneario":1,"noNextWordTermination":1,"nextWordNFullSuggsOne":1,"FastEntMulti":1,"CommonFooter":1,"ascbbottom":1,"asdsg":1,"aspreinitcss":1,"ascssdis":1,"ascontainer":1,"asGhostingInstrumentation":1,"moveTrendingNowToRight":1,"asbugfix":1,"easpm":1,"handleDuplicateFetch":1,"d":5,"ascqsl":2000,"zisr1":5};sa_loc= {"Suggestions":"Sugerencias","Feedback":"Comentarios sobre estas sugerencias","ManageHistory":"%eAdministra tu historial de búsqueda%E","RelatedSearches":"Búsquedas relacionadas","PeopleAlsoAsk":"Preguntas relacionadas","PeopleAlsoSearchFor":"Los usuarios también buscan","SeeMore":"Ver más","NudgesAStoChatClickthrough":"Clicking on this will redirect to Copilot","SydneyUpsellSuggestion":"Probar la Bing AI para \"{0}\"","SydneyUpsellAutosuggest":"Probar la IA de Bing para \"{0}\"","SydneyUpsellSuggestionLinkText":"Empezar","SydneyUpsellChatText":"Hola, soy Bing. Soy tu copiloto con tecnología de IA para la web. ","SydneyUpsellChat":"IA de Bing","ASQFChatBannerText":"Prueba Copilot para \"{0}\"","ASQFChatBannerCTAText":"Chat"};;var sa_eL=!1;(function(){function o(t,i,r){t&&sj_ue(t,i,o);sa_eL=sa_eL||r;var u=i==="load"&&n.bundleearly;e||u||(e=!0,sj_evt.fire("AS.bootload",t),sa_loader(),n.aspreloadcss&&c(),n.ascssear&&h(),(n.aspreinitcss||n.ascsr)&&s(),n.csr_preactbootstrap&&typeof preact=="undefined"&&typeof sa_preactloader!="undefined"&&sa_preactloader())}function s(){var n=document.getElementById("as-css-link");n!==null&&n.setAttribute("rel","stylesheet")}function h(){var t,n;!u&&_G.autosuggestCss&&(t=document.head,t&&(n=document.createElement("style"),n.type="text/css",n.innerText=_G.autosuggestCss,n.id="b_ascss",t.appendChild(n),u=!0))}function c(){var t,n;!u&&_G.ascssurl&&(t=document.head,t&&(n=document.createElement("link"),n.rel="preload",n.as="style",n.href=_G.ascssurl,n.id="b_asprecss",n.addEventListener("load",function(){n.rel="stylesheet"}),t.appendChild(n),u=!0))}function f(n,t,i){sj_be(n,t,function(r){o(n,t,i,sj_ev(r))})}var n=sa_config,t=_ge(n.i),i,r,e,u;if(t&&t.setAttribute("autocomplete","off"),i=_ge(n.c),!i){if(r=_ge("sa_qs")||t,!r){_w.sj_log&&sj_log("CI.AS","AS Init Failed","sq_qs_or_sb_not_found");return}i=sj_ce("div");i.id=n.c;_ge("sb_form_q").nodeName==="TEXTAREA"?r.parentNode&&r.parentNode.parentNode.appendChild(i):r.parentNode.appendChild(i)}e=!1;u=!1;_w.sa_loader&&("ontouchend"in _w&&f(t,"touchend",!0),f(t,"click",!0),f(t,"keydown",!0),n.ol&&f(_w,"load",!1))})();(function(){function i(t){n&&(n.value==null||n.value.length==0)&&(t.preventDefault(),_w.open("/images/feed?FORM=HDRSC2","_blank"))}var n=_ge("sb_form_q"),t=_ge("b-scopeListItem-images");sj_be(t,"click",i)})();var UrlLib;(function(n){function t(n,t,i){var e=n.toLowerCase(),o=t.toLowerCase(),u="&"+o+"=",r=e.indexOf(u,0),f;return(r<0&&(u="?"+o+"=",r=e.indexOf(u,0)),r>=0)?(f=n.indexOf("&",r+1),f==-1&&(f=n.length),n.replace(n.substring(r+u.length,f),i)):_w.location.hash?n.substring(0,n.indexOf(_w.location.hash)).concat(n.indexOf("?")<0?"?":"&",t,"=",i)+_w.location.hash:n.concat(n.indexOf("?")<0?"?":"&",t,"=",i)}n.AddParam=t})(UrlLib||(UrlLib={}));var __spreadArray=this&&this.__spreadArray||function(n,t,i){if(i||arguments.length===2)for(var r=0,f=t.length,u;r<f;r++)!u&&r in t||(u||(u=Array.prototype.slice.call(t,0,r)),u[r]=t[r]);return n.concat(u||Array.prototype.slice.call(t))},SHwlochelper;(function(n){function e(t,u,e,o,s){var h=n.sF(f,t.coords.latitude,t.coords.longitude,t.coords.accuracy);h+="&sid="+u;i(h,r,"DeviceLocation",e,o,s)}function o(n,t,f,e){var o=u+"&sid="+n;i(o,r,"Block",t,f,e)}function i(n,i,r,u,f,e){n+="&clientsid="+_G.SID;var o=sj_gx();o.open("GET",n,!0);t(i,u,"try"+r);o.onreadystatechange=function(){var n,s;o.readyState===4&&(o.status===200?(o.responseText&&(s=(n=_ge("PromptDetails"))!==null&&n!==void 0?n:_d.body,s&&(sj_appHTML(s,o.responseText),_w.rms&&_w.rms.start())),t(i,u,r+"Success"),typeof f=="function"&&f()):(t(i,u,r+"Failure"),typeof e=="function"&&e()))};o.send()}function t(n,t,i){for(var u=[],r=3;r<arguments.length;r++)u[r-3]=arguments[r];Log&&Log.Log&&Log.Log.apply(Log,__spreadArray([n,t,i,!0],u,!1))}var r="DevLoc",u="/geolocation/write?isBlocked=true",f="/geolocation/write?isDevLoc=true&lat={0}&lon={1}&acc={2}";n.writeDeviceLocation=e;n.writeBlockLocation=o;n.setCookieHelper=i;n.sF=function(n){for(var i=[],t=1;t<arguments.length;t++)i[t-1]=arguments[t];return n.replace(/{(\d+)}/g,function(n,t){return typeof i[t]!="undefined"?i[t]:n})};n.log=t})(SHwlochelper||(SHwlochelper={}));var SHlochelper;(function(n){function t(n,t){var u=n.split("?"),f,i,r;if(u.length>=2){for(f=encodeURIComponent(t)+"=",i=u[1].split(/[&]/g),r=i.length-1;r>=0;r--)i[r].lastIndexOf(f,0)!==-1&&i.splice(r,1);return u[0]+(i.length>0?"?"+i.join("&"):"")}return n}function r(n){var r,s,t;if(typeof _w.Log2!="undefined"&&Log2.LogEvent)Log2.LogEvent("ClientInst",n,null,null,null,null,null,null),Log2.ForceFlush&&(r={force:!0,useSendBeacon:!0},Log2.ForceFlush(!0,r));else{var u="/fd/ls/lsp.aspx",f=(new Date).getTime(),h="<E><T>Event.ClientInst<\/T><IG>"+_G.IG+"<\/IG><TS>"+f+"<\/TS><D><![CDATA[["+n.replace("]\]>","]]]\]><![CDATA[>")+"]]\]><\/D><\/E>",e="<ClientInstRequest><Events>"+h+"<\/Events><STS>"+f+"<\/STS><\/ClientInstRequest>",o=!1;navigator&&navigator[i]&&(s=new Blob([e],{type:"text/plain"}),o=navigator[i](u,s));o||(t=sj_gx(),t.open("POST",u,!0),t.setRequestHeader("Content-Type","text/xml"),t.send(e))}}function u(n){if(n){for(var t=_w,i=n.split("."),u=i.length,r=0;t&&r<u;)t=t[i[r++]];return t}return null}n.promptDetailsName="PromptDetails";var i="sendBeacon";n.rLocPar=function(n){var i=t(n,"location");return i=t(i,"pivotEntityName"),t(i,"ypid")};n.ge_fcl=function(n,t){var i=ge_cl(n,t);return i&&i.length>0?i[0]:null};n.sc=function(n,t,i,r,u,f,e,o,s){function h(){o&&sj_evt.fire("LocChangerClose_"+o)}if(i!==null)switch(i){case 0:SHwlochelper.writeDeviceLocation(n,s,f,e,h);break;case 1:SHwlochelper.writeBlockLocation(s,f,e,h);break;default:SHwlochelper.setCookieHelper(t,r,u,f,e,h)}};n.sEM=function(t,i){var r,u,s;if(t&&(r=_ge(t),r)){var e=n.ge_fcl(r,"g_neg"),o=n.ge_fcl(r,"g_pos"),f=n.ge_fcl(r,"g_allow");e&&o&&f&&(sa_cl(e,"b_hide",!1),sa_cl(o,"b_hide",!0),sa_cl(f,"dis",!0),u=f.getElementsByTagName("input"),u&&u.length>0&&(u[0].tabIndex=-1));s=n.ge_fcl(r,"g_exp");s.className.indexOf("rot")===-1&&sj_evt.fire("locChangerExpand_"+t,i)}};n.rP=function(t,i){var r=UrlLib.AddParam(location.href,"isRef","1");r=UrlLib.AddParam(r,"showTw","1");i&&(r=UrlLib.AddParam(r,"isAutoP","1"));r=n.rLocPar(r);t&&(r=UrlLib.AddParam(r,"form",t));location.href=r};n.sendBeaconLog=r;n.parseFunction=u})(SHlochelper||(SHlochelper={}));var SHgetlochelper;(function(n){function r(r,u,f,e,o,s,h){var c,l;if(navigator&&typeof navigator.geolocation!="undefined"&&typeof navigator.geolocation.getCurrentPosition=="function")if(c=void 0,c=navigator,typeof c.permissions!="undefined"&&typeof c.permissions.query=="function"){l=!1;SHwlochelper.log(t,e,"Perm_Available");try{c.permissions.query({name:"geolocation"}).then(function(c){if(c&&typeof c.state!="undefined"){if(c.state==="denied"){SHwlochelper.log(t,e,"Perm_Denied");SHlochelper.sEM(u,!0);i(e,h,u,f);return}c.state==="prompt"?SHwlochelper.log(t,e,"Perm_Prompt"):c.state==="granted"&&(l=!0,SHwlochelper.log(t,e,"Perm_Granted"))}if(o&&!l)return SHwlochelper.log(t,e,"Perm_NotGranted_NoTryGet"),!1;n.gL(r,u,f,e,s,h)})}catch(a){SHwlochelper.log(t,e,"Error2","Error",a)}}else{if(SHwlochelper.log(t,e,"Perm_Unavailable"),o)return SHwlochelper.log(t,e,"Perm_Unavailable_NoTryGet"),!1;n.gL(r,u,f,e,s,h)}else SHwlochelper.log(t,e,"getCurrentPosition_5");return!1}function i(n,i,r,u){function e(){typeof i=="function"&&i()}SHwlochelper.log(t,n,"block");var f=_ge(SHlochelper.promptDetailsName);f&&f.setAttribute("data-block","1");SHlochelper.sc(null,null,1,t,"Block",n,e,r,u)}var t="DevLoc";n.tgL=function(n,i,u,f,e,o,s){var h=sj_cook.areCookiesAccessible();return h?r(n,i,u,f,e,o,s):SHwlochelper.log(t,f,"getCurrentPosition_6"),!1};n.gL=function(n,r,u,f,e,o,s){s===void 0&&(s=!0);SHwlochelper.log(t,f,"TryAsk");var h=setTimeout(function(){SHwlochelper.log(t,f,"getCurrentPosition_7")},1e4);try{navigator.geolocation.getCurrentPosition(function(i){function o(){var o=!0,u,s,b;if(n&&(o=!_G.UIWP,o&&(u=_ge(SHlochelper.promptDetailsName),u)))if(s=u.getAttribute("data-noRefresh"),s&&s==="true")o=!1,SHwlochelper.log(t,f,"NoRefreshExplicit");else{var l=u.getAttribute("data-lat"),a=u.getAttribute("data-lon"),v=u.getAttribute("data-dist");if(l&&a&&v){var h=parseFloat(l),c=parseFloat(a),y=parseFloat(v);if(!(h===0&&c===0)&&h&&c&&y){var p=Math.PI/180*h,w=Math.PI/180*i.coords.latitude,k=Math.PI/180*(i.coords.longitude-c),d=Math.acos(Math.sin(p)*Math.sin(w)+Math.cos(p)*Math.cos(w)*Math.cos(k))*3958.76;d<y&&(o=!1,SHwlochelper.log(t,f,"NoRefreshDistance"))}}}typeof e=="function"?(e(i.coords.latitude,i.coords.longitude),sj_evt.fire("LocChangerClose_"+r)):o?(SHwlochelper.log(t,f,"Refresh"),_w.geoLocSendBec===1&&(b={T:"CI."+t,FID:f,Name:"RefreshB"},SHlochelper.sendBeaconLog(b)),SHlochelper.rP("GEOTRI",n)):SHwlochelper.log(t,f,"NoRefresh")}clearTimeout(h);SHwlochelper.log(t,f,"SuccessGet");SHlochelper.sc(i,null,0,t,"DeviceLocation",f,s?o:null,r,u)},function(n){if(clearTimeout(h),n&&n.code){var e=void 0;e=navigator;typeof e.permissions!="undefined"&&typeof e.permissions.query=="function"&&e.permissions.query({name:"geolocation"}).then(function(i){n.code===n.PERMISSION_DENIED&&(SHwlochelper.log(t,f,"Perm_UserResponseDenied"),i.state==="prompt"?SHwlochelper.log(t,f,"ClosedPrompt"):i.state==="denied"&&SHwlochelper.log(t,f,"BlockedPrompt"))});switch(n.code){case n.PERMISSION_DENIED:SHwlochelper.log(t,f,"getCurrentPosition_1");SHlochelper.sEM(r,!0);i(f,o,r,u);break;case n.POSITION_UNAVAILABLE:SHwlochelper.log(t,f,"getCurrentPosition_2");break;case n.TIMEOUT:SHwlochelper.log(t,f,"getCurrentPosition_3");break;default:SHwlochelper.log(t,f,"getCurrentPosition_4","Code",n.code.toString())}}SHlochelper.sEM(r,!0)},{timeout:1e4})}catch(c){SHwlochelper.log(t,f,"Error4","Error",c)}}})(SHgetlochelper||(SHgetlochelper={}));var SHPromptForUserLocation;(function(n){function t(n,t){var i="AutoPrompt";SHwlochelper.log("DevLoc",i,"PromptDevLoc");SHgetlochelper.tgL(!0,null,t,i,n,null,null)}n.init=t})(SHPromptForUserLocation||(SHPromptForUserLocation={}));sj_evt.fire("promptForUserLocationInit");var effectiveLocationUrl="/geolocation/write?isDevLoc=false&lat={0}&lon={1}&dispName={2}&isEff=1&effLocType={3}",EffectiveLocationLogEventType="EffectiveLocation",effectiveLocationDetailsElement=_ge("g_EfLoc"),url;if(effectiveLocationDetailsElement){var latitude=effectiveLocationDetailsElement.getAttribute("data-lat"),longitude=effectiveLocationDetailsElement.getAttribute("data-lon"),source=effectiveLocationDetailsElement.getAttribute("data-source"),displayName=encodeURIComponent(effectiveLocationDetailsElement.getAttribute("data-disp"));latitude&&longitude&&displayName&&(url=SHwlochelper.sF(effectiveLocationUrl,latitude,longitude,encodeURIComponent(displayName),source),SHlochelper.sc(null,url,4,EffectiveLocationLogEventType,"WriteEffectiveLocation",EffectiveLocationLogEventType,null,null,null))};(function(n,t,i,r,u,f,e,o){if(typeof RMS_IACL=="undefined"){_w.RMS_IACL={};var s,h,c="data-src",w="data-dpr",l="getAttribute",g=/^(\/th\?(?:id\=|.*&id\=))([^&]*)(.*)$/i,nt=/(&|\?)bw=/i,tt=".svg",a=3,y=20;function b(t){var r,i;t&&(r=t[l](c),r&&(t.removeAttribute(c),i=n("img"),i.onload=function(n){h--;i.onload=null;var r=t.parentNode;r&&(p(t,i),t.parentNode.replaceChild(i,t));v(n,i,r,null)},i.onerror=function(n){h--;v(n,i,!1,t)},i.src=r))}function it(t,i,r,u){for(var f,y,o=[],e=t[i],s=i;s<r;s++)o.push(t[s].tid);f=n("img");f.onload=function(n){var y,o,b,c,k;h-=r-i;f.onload=null;p(e,f);var it=e[l]("data-width"),g=e[l](w),u=g?parseFloat(g):1;for(y=i;y<r;y++){o=t[y];b=o.style;b.position="relative";b.overflow="hidden";b.direction="ltr";p(o,o);c=f.cloneNode();k="";o.hasAttribute("title")?(k=o.attributes.title.value,c.setAttribute("title",o.attributes.title.value)):o.hasAttribute("alt")&&(k=o.attributes.alt.value);c.setAttribute("alt",k);c.removeAttribute("width");c.removeAttribute("height");var s=c.style,nt=Math.floor(it*u)/u,d=y-i,tt=sb_ie||u<=1?1:u;s.left=(-nt*d-a*(d+1)/u)*tt+"px";s.top=-a/u*tt+"px";u>1&&(s.hasOwnProperty("zoom")?s.zoom=100/u+"%":(s.left=-nt*d-a*(d+1)/u+"px",s.top=-a/u+"px",s.transformOrigin="left top",s.transform="scale("+1/u+")"));s.position="relative";o.appendChild(c)}v(n,e,!0,null)};f.onerror=function(n){h-=r-i;v(n,e,!1,null)};y=e[l](c).replace(o[0],o.join(":"))+"&bw="+a+"&bc="+u;f.src=y.replace(/&p=0/,"")}function rt(n,t){var u,i,r;if(n){if(n.length==1)b(n[0]);else if(n.length>1)for(u=n.length,i=0;u>0;)r=i+y,r>n.length&&(r=n.length),it(n,i,r,t),u-=y,i+=y}else return}function k(n){var ft=d(n),f,o,r,it,u,y,p,w;if(s=ut(),s){for(h=s.length,h>0&&t.fire(e,{timeStamp:ft,images:s}),f={},o=[],u=0;u<s.length;u++){var a=s[u],k=a[l](c),v=null;k&&(r=g.exec(k),r&&r.length&&!nt.test(k)&&r[2]&&r[2].indexOf(tt)===-1&&(v=r[1]+r[3],a.tid=r[2]));v&&(it=f[v]=f[v]||[]).length<30?it.push(a):o.push(a)}for(u=0;u<o.length;u++)b(o[u]);y="ffffff";p=i.body;(p.classList.contains("b_drk")||_w.matchMedia("(prefers-color-scheme:dark)").matches&&p.classList.contains("b_med"))&&(y="1B1A19");for(w in f)f.hasOwnProperty(w)&&rt(f[w],y)}}function ut(){var t="rms_iac",n;if(i.getElementsByClassName)return i.getElementsByClassName(t);if(i.querySelectorAll)return i.querySelectorAll("."+t);var u=[],r=i.getElementsByTagName("div"),f=new RegExp("(^|\\s)"+t+"(\\s|$)");for(n=0;n<r.length;n++)f.test(r[n].className)&&u.push(r[n]);return u}function p(n,t){for(var r,i,u=0;u<n.attributes.length;u++)r=n.attributes[u],i=r.name,i.lastIndexOf("data-",0)==0&&i!=c&&i!=w&&t.setAttribute(i.substring(5),r.value)}function d(n){var t=new Date;return n&&n.timeStamp>0&&n.timeStamp<=t?n.timeStamp:t}function v(n,i,r,u){var e=d(n);t.fire(f,{timeStamp:e,image:i,state:r?"success":"failed",placeholderElement:u});h==0&&t.fire(o,{timeStamp:e,images:s})}t.bind(r,k,1);t.bind(u,k,1)}})(sj_ce,sj_evt,_d,"onPP","RMS.LoadImg","RMS.ImgAOLLoaded","RMS.ImgAOLInit","RMS.ImgAOLCompleted");
//]]>--></div><div style="display:none" "><!--//<![CDATA[
var pinnedHeaderParams ={};;
//]]>--></div><div style="display:none" "><!--//<![CDATA[
var BingAtWork;(function(n){var t;(function(){function h(){var t=f.value;/\S/.test(t)&&n()}function c(t){var i=t[0],r=t[1],u=t[2];n()}function l(n){var t=n&&n.length>1&&n[1];t&&Lib.CssClass.contains(t,"b_searchbox")&&t.id!=s&&a()}function a(){Lib.CssClass.add(sj_b,u)}function n(){Lib.CssClass.remove(sj_b,u)}function v(){sj_evt.bind("autosuggestHide",c,!0);sj_evt.bind("focusChange",l,!0);t&&sj_be(t,"submit",h)}function i(){(n(),r)||(t=_ge(e),f=_ge(o),r=!0,v())}var r=!1,u="as_on",e="sb_form",o="sb_form_q",s="uaseabox",t,f;sj_be(_d,"DOMContentLoaded",i);sj_evt.bind("onP1",i,!0);sj_evt.bind("ajax.load",i,!0);sj_evt.bind("pagehide",n);sj_evt.bind("ajax.unload",n)})(t=n.HeaderWithAutoSuggestState||(n.HeaderWithAutoSuggestState={}))})(BingAtWork||(BingAtWork={}));BingAtWork.HeaderWithAutoSuggestState.init && BingAtWork.HeaderWithAutoSuggestState.init(false);;(function(){var i,r,u,f,n,t;_w.sj_log&&(f=_qs("input#sb_form_q"),n=_qs("textarea#sb_form_q"),f&&(t=(i=f.value)===null||i===void 0?void 0:i.length,sj_log("CI.SERPSB","CharCount",t.toString()),sj_log("CI.SERPSB","LandingRows","1")),n&&(t=(r=n.value)===null||r===void 0?void 0:r.length,sj_log("CI.SERPSB","CharCount",t.toString()),sj_log("CI.SERPSB","LandingRows",(u=n.rows)===null||u===void 0?void 0:u.toString())))})();
//]]>--></div><div style="display:none" "><!--//<![CDATA[
var AccessibilityHelper;(function(n){function f(n,t,i){n&&n.classList&&t&&(i?n.classList.remove(t):n.classList.add(t))}function lt(n){if(n.keyCode===9)f(this,c,!0);else if(n.keyCode===13)if(this.click)this.click();else{var t=_d.createEvent("MouseEvents");t.initEvent(kt,!0,!0);this.dispatchEvent(t,!0)}}function at(){f(this,c)}function w(n){sj_pd(n);sj_sp(n)}function dt(n){var t;if(n){var r=n.querySelectorAll('a:not([disabled]):not([tabindex="-1"]), button:not([disabled]):not([tabindex="-1"]), input[type=text]:not([disabled]):not([tabindex="-1"]), [tabindex]:not([disabled]):not([tabindex="-1"])'),u=r.length,f=!1,i=null;for(t=0;t<u;t++)if(i=r[t],i.offsetWidth>0){if(ct===i){f=!0;break}i.focus();break}if(f)for(t=u-1;t>=0;t--)if(i=r[t],i.offsetWidth>0){i.focus();break}}}function b(n){var t=n.target||n.srcElement,r,u,i,f;if(t){for(r=_d.querySelectorAll(".accDialog"),u=null,i=0;i<r.length;i++)if(f=r[i],f.contains(t)){u=f;ct=t;break}e&&e.offsetWidth>0&&!e.contains(t)?(w(n),dt(e)):e=u}}function gt(n){n&&(n.setAttribute(s,"1"),n.setAttribute(bt,"0"),f(n,c),i(n,tt,lt),i(n,ft,at),i(n,et,w))}function vt(){return _d.querySelectorAll(".accessible")}function o(){var e=vt(),o,r,c,n;if(e)for(o=e.length,n=0;n<o;n++)r=e[n],r&&!r.getAttribute(s)&&gt(r);for(u=_d.querySelectorAll("[data-sbtip],[data-tooltip]"),c=function(){var t=u[n],r=function(){f(t,l)},e=function(){f(t,l,!0)},o=function(n){n.keyCode==27&&f(t,l,!0)};i(t,rt,r);i(t,ut,e);i(t,it,o);t[a]=r;t[v]=e;t[y]=o},n=0;n<u.length;n++)c();t(_d,h,b);i(_d,h,b,!0)}function ni(n,t){if(n&&n.contains(t))for(var i=n.firstElementChild;i!=null;)i.clientHeight>0&&i.clientWidth>0&&!i.contains(t)&&i!=t&&(i.setAttribute(d,"true"),p.push(i)),i=i.nextElementSibling}function yt(n){if(n&&n[1])for(var i=n[1],t=i.parentElement;t;)ni(t,i),t=t.parentElement}function pt(){for(var n=p.pop();n!=null;)n.setAttribute(d,"false"),n=p.pop()}function ti(){var f=vt(),e,r,i,n;if(f)for(e=f.length,i=0;i<e;i++)r=f[i],r&&r.getAttribute(s)&&(t(r,tt,lt),t(r,ft,at),t(r,et,w));if(u!=null)for(i=0;i<u.length;i++)n=u[i],t(n,rt,n[a]),t(n,ut,n[v]),t(n,it,n[y]),n[a]=null,n[v]=null,n[y]=null}function wt(){ti();t(_w,g,o);t(_w,nt,wt);r.unbind(ot,o);r.unbind(st,yt);r.unbind(ht,pt);t(_d,h,b)}var k="acchlp",d="aria-hidden",s="data-marked",bt="tabindex",g="load",nt="unload",kt="click",tt="keyup",it="keydown",rt="focusin",ut="focusout",h="focus",ft="blur",et="contextmenu",ot="Acc.SetAcc",st="AccDlg.Show",ht="AccDlg.Hide",c="nofocus",l="shtip",a="acc.finevt",v="acc.foutevt",y="acc.kdevt",t=sj_ue,i=sj_be,r=sj_evt,e=null,ct=null,p=[],u=null;_w[k]||(_w[k]=n,i(_w,g,o),i(_w,nt,wt),r.bind(ot,o),r.bind(st,yt),r.bind(ht,pt))})(AccessibilityHelper||(AccessibilityHelper={}));var sj_appHTML=function(n,t){var f,e,o,r,i,s,h;if(t&&n){var c="innerHTML",l="script",a="appendChild",v="length",y="src",p=sj_ce,u=p("div");if(u[c]="<br>"+t,f=u.childNodes,u.removeChild(f[0]),e=u.getElementsByTagName(l),e)for(o=0;o<e[v];o++)r=p(l),i=e[o],i&&(r.type=i.type=="module"||i.type=="importmap"?i.type:"text/javascript",s=i.getAttribute(y),s?(r.setAttribute(y,s),r.setAttribute("crossorigin","anonymous")):(r.text=i[c],r.setAttribute("data-bing-script","1")),i.parentNode.replaceChild(r,i));for(h=_d.createDocumentFragment();f[v];)h[a](f[0]);n[a](h)}};
//]]>--></div><div style="display:none" "><!--//<![CDATA[
var SydneyCameraProviderFactory;(function(n){n.registeredCameraProvidersMap={FileUploadCameraProvider:"FileUploadCameraProvider",SydneyMobileWebCameraProvider:"SydneyMobileWebCameraProvider",SydneyNativeCameraProvider:"SydneyNativeCameraProvider",SydneyEdgeNativeCameraProvider:"SydneyEdgeNativeCameraProvider"};n.create=function(t){n.registeredCameraProvidersMap[t]&&_w[t]&&_w[t].create()}})(SydneyCameraProviderFactory||(SydneyCameraProviderFactory={}));var SydneyCameraProviderBase;(function(n){var i,t;(function(n){n.Pending="pending";n.Granted="granted";n.Denied="denied"})(i=n.CameraPermissionStatus||(n.CameraPermissionStatus={}));t=function(){function n(){}return n.prototype.registerResultCallback=function(n){this.onResultCallback=n},n}();n.CameraProviderBase=t})(SydneyCameraProviderBase||(SydneyCameraProviderBase={}));var __awaiter=this&&this.__awaiter||function(n,t,i,r){function u(n){return n instanceof i?n:new i(function(t){t(n)})}return new(i||(i=Promise))(function(i,f){function o(n){try{e(r.next(n))}catch(t){f(t)}}function s(n){try{e(r["throw"](n))}catch(t){f(t)}}function e(n){n.done?i(n.value):u(n.value).then(o,s)}e((r=r.apply(n,t||[])).next())})},__generator=this&&this.__generator||function(n,t){function o(n){return function(t){return s([n,t])}}function s(o){if(e)throw new TypeError("Generator is already executing.");while(f&&(f=0,o[0]&&(r=0)),r)try{if(e=1,u&&(i=o[0]&2?u["return"]:o[0]?u["throw"]||((i=u["return"])&&i.call(u),0):u.next)&&!(i=i.call(u,o[1])).done)return i;(u=0,i)&&(o=[o[0]&2,i.value]);switch(o[0]){case 0:case 1:i=o;break;case 4:return r.label++,{value:o[1],done:!1};case 5:r.label++;u=o[1];o=[0];continue;case 7:o=r.ops.pop();r.trys.pop();continue;default:if(!(i=r.trys,i=i.length>0&&i[i.length-1])&&(o[0]===6||o[0]===2)){r=0;continue}if(o[0]===3&&(!i||o[1]>i[0]&&o[1]<i[3])){r.label=o[1];break}if(o[0]===6&&r.label<i[1]){r.label=i[1];i=o;break}if(i&&r.label<i[2]){r.label=i[2];r.ops.push(o);break}i[2]&&r.ops.pop();r.trys.pop();continue}o=t.call(n,r)}catch(s){o=[6,s];u=0}finally{e=i=0}if(o[0]&5)throw o[1];return{value:o[0]?o[1]:void 0,done:!0}}var r={label:0,sent:function(){if(i[0]&1)throw i[1];return i[1]},trys:[],ops:[]},e,u,i,f;return f={next:o(0),"throw":o(1),"return":o(2)},typeof Symbol=="function"&&(f[Symbol.iterator]=function(){return this}),f},SydneyTestHelper;(function(n){function it(){return e||(e=new tt),e}function rt(){return!!i(k)}function ut(){var n,i,r,u,f,e,o,s,l=t((n=_w._sydConvConfig)===null||n===void 0?void 0:n.enableCodexTesthooks,!1),h=t((i=_w._sydConvConfig)===null||i===void 0?void 0:i.enableTestTurnLimit,!1),a=t((r=_w._sydConvConfig)===null||r===void 0?void 0:r.enableTestDailyLimit,!1),v=t((u=_w._sydConvConfig)===null||u===void 0?void 0:u.enableThreadsTestData,!1),y=t((f=_w._sydConvConfig)===null||f===void 0?void 0:f.enableStagingCreateTesting,!1),p=t((e=_w._sydConvConfig)===null||e===void 0?void 0:e.enablePluginTestData,!1),c;l&&(CIB.config.testhooks.enableTestTurnLimit=h,CIB.config.testhooks.enableTestDailyLimit=a);y&&(CIB.config.sydney.hostnamesToBypassSecureConnection=[],CIB.manager.chat.api.bing._endpoint="https://www.staging-bing-int.com");v&&(CIB.config.features.enableThreadsTestData=!0);c=t((o=_w._sydConvConfig)===null||o===void 0?void 0:o.enableSydTestCache,!1);c&&(CIB.config.debug.testhook=!0,CIB.config.features.enableUserMessageCopy=!1,h&&((s=_w.CIB)===null||s===void 0?void 0:s.manager.chat.errorState.onChatErrorStateChange(function(n){n==="conversation-limit"?sj_evt.fire("ConversationLimit"):n==="conversation-limit-muid-users"&&sj_evt.fire("ConversationLimitForMuidUsers")})),lt());p&&(CIB.config.features.enablePluginTestData=!0,pt())}function t(n,t){return typeof n!="undefined"?n:t}function ft(n){var i,u=t((i=_w._sydConvConfig)===null||i===void 0?void 0:i.enableTigerCachedAttr,!1),r;n&&(u&&(this.config.messaging.streamCachedResponses=!1,r=_d.querySelector("#b_sydTigerCont cib-serp"),r&&r.setAttribute("cached","")),this.processCachedResponse(n))}function et(){var n,i,r,u,f;return __awaiter(this,void 0,void 0,function(){var c,l,a,o,e,s,h;return __generator(this,function(v){switch(v.label){case 0:if(c=t((n=_w._sydConvConfig)===null||n===void 0?void 0:n.enableSydTestCache,!1),!c)return[2];CIB.onConversationRequestStateChange(function(n){var t,i,r,u;n||(sj_evt.fire("RequestCompleted"),r=(i=(t=_d.querySelector("cib-serp"))===null||t===void 0?void 0:t.shadowRoot.querySelector("cib-action-bar"))===null||i===void 0?void 0:i.shadowRoot,r&&(u=sj_ce("style"),u.textContent="\n                        cib-tooltip {\n                            display: none !important;\n                        }\n                    ",r.appendChild(u)))});return l=t((i=_w._sydConvConfig)===null||i===void 0?void 0:i.enableTestTurnLimit,!1),l&&(CIB.config.features.enableMaxTurnsFromBackend=!1,CIB.config.messaging.maxTurnsPerConversation=1),a=t((r=_w._sydConvConfig)===null||r===void 0?void 0:r.enableTurnlimitNonSignedIn,!1),a&&(CIB.config.features.enableMUIDUserMode=!0),o=t((u=_w._sydConvConfig)===null||u===void 0?void 0:u.sydTestCacheMsg,""),e=null,o&&o.indexOf('{"messages":')==0&&(e=JSON.parse(o)),[4,st()];case 1:return s=v.sent(),s&&(e=s),CIB.config.share&&(CIB.config.share.testShareId=t((f=_w._sydConvConfig)===null||f===void 0?void 0:f.sydTestMockShareId,"")),h=ft.bind(CIB),CIB.sendSearchQuery=function(){h(e)},CIB.addMessage=function(){h(e)},sj_evt.fire("CHAT_MOCKED",e),[2]}})})}function ot(n,t){return __awaiter(this,void 0,void 0,function(){var i;return __generator(this,function(){return i="GET",[2,fetch(a+"?AjaxHostName="+n+"&AjaxBag="+t,{headers:{"content-type":"application/json"},method:i})]})})}function i(n){var t=location.search;return decodeURIComponent((new RegExp("[?|&]?"+n+"=([^&;]+?)(&|#|;|$)","i").exec(t)||[,""])[1].replace(/\+/g,"%20"))||null}function st(){return __awaiter(this,void 0,void 0,function(){var n,t,r,u,f;return __generator(this,function(e){switch(e.label){case 0:if(n=i(v)||i(y),t=i(p),!n||n===""||!t||t==="")return[2];e.label=1;case 1:return e.trys.push([1,5,,6]),[4,ot(n,t)];case 2:return(r=e.sent(),!(r&&r.ok))?[3,4]:[4,r.json()];case 3:return u=e.sent(),[2,u];case 4:return[3,6];case 5:return f=e.sent(),[3,6];case 6:return[2]}})})}function o(n){var u=new URL(n),t=new URLSearchParams(u.search),i,r;return t.get("w")&&t.get("h")?(i=new URL(nt),r=new URLSearchParams(i.search),r.set("width",t.get("w")),r.set("height",t.get("h")),i.search=r.toString(),i.href):g}function ht(n){for(var i,s,c,t,e=0,h=n;e<h.length;e++)if(i=h[e],i[r]&&i[r][0]&&i[r][0][l])for(s=0,c=i[r][0][l];s<c.length;s++)t=c[s],t.type===d&&t.url&&(t.url=o(t.url)),t[u]&&t[u][f]&&(t[u][f]=o(t[u][f]));return n}function ct(n){return n=ht(n),JSON.stringify({messages:n})}function lt(){var n,t,r,u=i(w),f;u&&u==="1"&&((r=(t=(n=CIB===null||CIB===void 0?void 0:CIB.manager)===null||n===void 0?void 0:n.chat)===null||t===void 0?void 0:t.stream)===null||r===void 0?void 0:r.next)&&(f=CIB.manager.chat.stream.next,CIB.manager.chat.stream.next=function(n){if(f(n),n&&n.messages&&n.messages.length>0){var t=at(n.messages);vt(ct(n.messages),t)}})}function at(n){return n&&n.length>0?n[0].text.substr(0,20):b}function vt(n,t){var r=new Blob([n],{type:".txt"}),u=URL.createObjectURL(r),i=document.createElement("a");i.href=u;i.download=t;document.body.appendChild(i);i.click();document.body.removeChild(i)}function yt(){var n,t,r,u,f,e,o,l,a,v;((t=(n=CIB===null||CIB===void 0?void 0:CIB.config)===null||n===void 0?void 0:n.answers)===null||t===void 0?void 0:t.query)&&(CIB.config.answers.query.atlahostname=(r=i(s))!==null&&r!==void 0?r:"",CIB.config.answers.query.bag=(u=i(h))!==null&&u!==void 0?u:"",CIB.config.answers.query.features=(f=i(c))!==null&&f!==void 0?f:"",CIB.config.answers.query.mockimages="1");((o=(e=CIB===null||CIB===void 0?void 0:CIB.config)===null||e===void 0?void 0:e.ads)===null||o===void 0?void 0:o.query)&&(CIB.config.ads.query.atlahostname=(l=i(s))!==null&&l!==void 0?l:"",CIB.config.ads.query.bag=(a=i(h))!==null&&a!==void 0?a:"",CIB.config.ads.query.features=(v=i(c))!==null&&v!==void 0?v:"",CIB.config.ads.query.mockimages="1")}function pt(){CIB.config.plugin.pluginList=[{id:"c310c353-b9f0-4d76-ab0d-1dd5e979cf68",iconUrl:"https://bing.com/th?id=OSK.D55C9DB951CBFC913AEACA38DA333D28",name:"Bing Search",description:"Bing Search",exclusionGroups:["Search"],privacyStateUrl:"https://go.microsoft.com/fwlink/?LinkId=521839",termsOfUseUrl:"https://www.bing.com/new/termsofuse",enabled:!0,isToggleVisible:!0,isToggleDisabled:!1,optionsSet:"nosearchv1p"},{id:"mockId1",iconUrl:"https://bing.com/th?id=mockIcon1",name:"Mock Name 1",description:"This is the mock plugin 1",exclusionGroups:["mockExclusionGroup"],privacyStateUrl:"https://bing.microsoft.com/mockPrivacyStateUrl1",termsOfUseUrl:"https://bing.microsoft.com/mockTermsOfUseUrl1",enabled:!1,isToggleVisible:!0,isToggleDisabled:!1,optionsSet:"mockOptionsSet1"},{id:"mockId2",iconUrl:"https://bing.com/th?id=mockIcon2",name:"Mock Name 2",description:"This is the mock plugin 2",exclusionGroups:["mockExclusionGroup"],privacyStateUrl:"https://bing.microsoft.com/mockPrivacyStateUrl2",termsOfUseUrl:"https://bing.microsoft.com/mockTermsOfUseUrl2",enabled:!1,isToggleVisible:!0,isToggleDisabled:!1,optionsSet:"mockOptionsSet2"},{id:"mockId3",iconUrl:"https://bing.com/th?id=mockIcon3",name:"Mock Name 3",description:"This is the mock plugin 3",exclusionGroups:["mockExclusionGroup3"],privacyStateUrl:"https://bing.microsoft.com/mockPrivacyStateUrl3",termsOfUseUrl:"https://bing.microsoft.com/mockTermsOfUseUrl3",enabled:!1,isToggleVisible:!0,isToggleDisabled:!1,optionsSet:"mockOptionsSet3"},{id:"mockId4",iconUrl:"https://bing.com/th?id=mockIcon4",name:"Mock Name 4",description:"This is the mock plugin 4",exclusionGroups:["mockExclusionGroup4"],privacyStateUrl:"https://bing.microsoft.com/mockPrivacyStateUrl4",termsOfUseUrl:"https://bing.microsoft.com/mockTermsOfUseUrl4",enabled:!1,isToggleVisible:!0,isToggleDisabled:!1,optionsSet:"mockOptionsSet4"},]}var a="/VisualSystem/MockingAjax/mock",v="AjaxHostName",y="AtlaHostName",p="ChatBag",w="SaveChatBag",s="TwoTAtlaHostName",h="TwoTBagName",c="twoTFeatures",b="UntitledChatResponse",r="adaptiveCards",k="WaitAnswerScript",l="body",d="Image",u="knowledgeImageMetadata",f="overlayImageUrl",g="https://www.staging-bing-int.com/sa/simg/testimage.png",nt="https://www.staging-bing-int.com/snrtest/image?width=400&height=300",e,tt=function(){function n(){var n=this;this.p=new Promise(function(t,i){n.completeCallback=t;n.errorCallback=i})}return n.prototype.complete=function(n){this.completeCallback(n)},n.prototype.error=function(n){this.errorCallback(n)},n}();n.getAnswerScriptDefer=it;n.shouldWaitForAnswerScript=rt;n.setTestConfigs=ut;n.getConfigOrDefault=t;n.setTestMocks=et;n.getParamFromUrl=i;n.getMockedImageUrl=o;n.set2TTestConfigs=yt})(SydneyTestHelper||(SydneyTestHelper={}));
//]]>--></div><div style="display:none" "><!--//<![CDATA[
(function() { var config ={"url":"https%3a%2f%2flogin.microsoftonline.com%2fcommon%2foauth2%2fauthorize%3fclient_id%3d9ea1ad79-fdb6-4f9a-8bc3-2b70f96e34c7%26response_type%3did_token%26nonce%3dceb0347d-e861-4f9d-ba03-17f0ac551a9f%26redirect_uri%3dhttps%253a%252f%252fwww.bing.com%252forgid%252fidtoken%252fnosignin%26scope%3dopenid%2520email%2520profile%25209ea1ad79-fdb6-4f9a-8bc3-2b70f96e34c7%2f.default%26response_mode%3dform_post%26instance_aware%3dtrue%26msafed%3d0%26prompt%3dnone%26state%3d%257b%2522ig%2522%253a%2522F4148F017DED47FD91F14746ED92DB7A%2522%257d","sandbox":"allow-forms allow-pointer-lock allow-popups allow-same-origin allow-scripts","currentEpoch":"1708649398000"}; if (false) { if (typeof dbg !== "undefined") { dbg.trace("OrgId", "Delaying SSO on postRBComplete event"); } sj_evt.bind('postRBComplete', function() { setTimeout(function() {_w.rms.js({'rms:answers:OrgId:SsoFrame':'https:\/\/r.bing.com\/rp\/V_fBQ_iVmAgE_Ta_T-6BNXc0ZY4.br.js',d:1});sj_evt.fire('ssoFrameExists', config); }, 500); }, true, null, true); } else { sj_evt.fire('ssoFrameExists', config); } })();;(function () { var identityBtn = document.getElementById("id_l"); if (identityBtn) { identityBtn.addEventListener("click", function () { sj_log("CI.ICPopup", "popup", "1"); }); } })();;
//]]>--></div><div style="display:none" "><!--//<![CDATA[
(function() { var accLinkParams ={"accLinkRefreshEndPointUrl":"/orgid/acclink/refresh","isAadAuthenticated":false,"refreshAccountLinkInfoTimeInSeconds":3600,"refreshAccountLinkInfoRetryTimeInSeconds":600,"correlationId":"65d7ebb6419f4345939b7bed9ec091d2","hasActiveLinkedAccount":false}; AccountLink.init(accLinkParams); })();;
//]]>--></div><div style="display:none" "><!--//<![CDATA[
var LogAccessibilityEvents;(function(){function t(){var n=matchMedia("(forced-colors: active)"),t=matchMedia("(prefers-color-scheme: dark)");n.matches&&Log&&Log.Log("Info","Accessibility","'HighContrast");t.matches&&Log&&Log.Log("Info","Accessibility","DarkModeContrast")}var n=document.getElementById("b_skip_to_content");sj_be(_w,"resize",function(){var n=1;n=navigator&&navigator.userAgent.match(/Edge/)&&_w.screen?screen.deviceXDPI/screen.systemXDPI:navigator&&navigator.userAgent.match(/Trident/)?_w.devicePixelRatio:_w.outerWidth/_w.innerWidth;n=Math.round(n);n>=4&&Log&&Log.Log("Info","Accessibility","Zoom")});sj_be(_w,"keydown",function(t){n&&t.target==n&&Log&&Log.Log("Info","Accessibility","ScreenReader")});t()})(LogAccessibilityEvents||(LogAccessibilityEvents={}));var TextSpacing;(function(n){function f(n){if(typeof sj_ce!="undefined"&&u&&t&&r&&(window.MutationObserver||window.WebKitMutationObserver)&&typeof MutationObserver=="function"){var i=new MutationObserver(function(){var i=_qs("header nav ul li a");if(i&&window.getComputedStyle(i).getPropertyValue("letter-spacing")!="normal"&&!t.classList.contains("b_tsp"))t.classList.add("b_tsp"),e(n),typeof Log!="undefined"&&Log.Log("Info","TextSpacing","AddTextSpacing");else if(i&&window.getComputedStyle(i).getPropertyValue("letter-spacing")=="normal"&&t.classList.contains("b_tsp"))t.classList.remove("b_tsp"),typeof Log!="undefined"&&Log.Log("Info","TextSpacing","RemoveTextSpacing");else return});i.observe(r,{subtree:!0,childList:!0})}}function e(n){var t=sj_ce("link");i||(t.rel="stylesheet",t.href=n,u.appendChild(t),i=!0)}var i=!1,r=_qs("html"),u=document.head,t=document.body;n.init=f})(TextSpacing||(TextSpacing={}));TextSpacing.init("https://r.bing.com/rp/zNLlI9iQAsvJZDCGCEkpjo9vCPE.br.css");
//]]>--></div><div style="display:none" "><!--//<![CDATA[
var sj_ajax=function(n,t){function c(){i[u]=h;i.abort&&i.abort()}function s(n,t){typeof n=="function"&&n(t,{request:i,appendTo:function(n){i&&sj_appHTML(n,i.responseText)}})}var r,i=sj_gx(),u="onreadystatechange",f,e=null,o,l=sb_st,a=sb_ct,h=function(){};if(!n||!i){s(r,!1);return}i.open("get",n,!0);t&&(r=t.callback,f=t.timeout,o=t.headers,Object.keys(o||{}).forEach(function(n){i.setRequestHeader(n,o[n])}),t.withCredentials!==undefined&&(i.withCredentials=t.withCredentials));i[u]=function(){if(i.readyState===4){var n=!1;e!==null&&a(e);i.status===200&&(n=!0,i[u]=h);s(r,n)}};sj_evt.bind("ajax.unload",c);i.send();f>0&&(e=l(function(){c();s(r,!1)},f))};
//]]>--></div><div style="display:none" "><!--//<![CDATA[
(function(){function t(){Bnp.Global&&typeof chatBNPData!="undefined"&&chatBNPData&&(Bnp.Global.RawRequestURL=chatBNPData.RawUrl,Bnp.Global.Referer=chatBNPData.Referer);var n=new Bnp.Partner.Request(chatBNPData.NotificationsPartnerId);n.IID=chatBNPData.IID;n.Submit()}function n(){var r,u,f;typeof Bnp!="undefined"&&(Bnp.PartnerRequestSubmitted?sj_evt.unbind("showSydFSC",n):(f=(u=(r=_w._sydConvConfig)===null||r===void 0?void 0:r.enableChatVerticalLegalBNP)!==null&&u!==void 0?u:!1,f&&i(),t()))}function i(){var t=_ge("bnp_cookie_banner"),n;t&&(n=t.parentElement,n&&n.remove())}function r(){sj_evt.bind("showSydFSC",n,!0)}sj_evt.bind("OnBnpLoaded",r,1,0)})();
//]]>--></div><div style="display:none" "><!--//<![CDATA[
var SydWelcomPostLoad;(function(n){function t(n){var i,r=(i=_w.location)===null||i===void 0?void 0:i.search,t;return(r===null||r===void 0?void 0:r.length)>1?(t=[],n&&(t.push("IG="+_G.IG),t.push("IID="+n)),t.join("&")):null}function i(n){var i=sj_gx(),u="/welcomescreenassets",r=t(n);(r===null||r===void 0?void 0:r.length)>0&&(u+="?"+r);i.onreadystatechange=function(){if(i.readyState===4&&i.status===200&&i.responseText){var n=function(){sj_appHTML(_d.body,i.responseText);_w.rms&&_w.rms.start()};_d&&_d.readyState==="complete"?n():_w&&sj_be(_w,"load",n)}};i.open("GET",u,!0);i.send()}n.initRequest=i;sj_evt.fire("swsldinit")})(SydWelcomPostLoad||(SydWelcomPostLoad={}));
//]]>--></div><div style="display:none" "><!--//<![CDATA[
var startSettings ={"formConfig":"PageRecommendations_InlineTable","service":"PageRecommendations","scenario":"InlineTable"}; if (typeof Feedback !== "undefined" && typeof Feedback.Bootstrap !== "undefined") { Feedback.Bootstrap.InitializeFeedback({page:true}, "fdbkans_0", 0, 0, 0, undefined, undefined, startSettings); };
//]]>--></div><div style="display:none" "><!--//<![CDATA[
var startSettings ={"formConfig":"PageRecommendations_InlineLowTable","service":"PageRecommendations","scenario":"InlineLowTable"}; if (typeof Feedback !== "undefined" && typeof Feedback.Bootstrap !== "undefined") { Feedback.Bootstrap.InitializeFeedback({page:true}, "fdbkans_1", 0, 0, 0, undefined, undefined, startSettings); };
//]]>--></div><div style="display:none" "><!--//<![CDATA[
var pMMUtils=pMMUtils||{},MMUtilsDom;(function(n){function s(n,t){return n&&n.getAttribute?n.getAttribute(t):null}function v(n,t){return n.hasAttribute?n.hasAttribute(t):typeof n[t]!="undefined"}function h(n,t,i){n.setAttribute&&n.setAttribute(t,i)}function y(n,t){n.appendChild(t)}function p(n,t){n.removeChild(t)}function w(n,t,i){i?n.insertBefore(t,i):n.insertBefore(t)}function b(n,t,i){n.lastchild==i?n.appendChild(t):n.insertBefore(t,i.nextSibling)}function c(n,t){return n.removeAttribute(t)}function k(n){return n.innerHTML}function d(n,t){n.innerHTML=t}function g(n,t){n.innerText=t}function nt(n,i,r,u){for(;n&&n!==document;n=n.parentNode)if(n.tagName===i&&(!r||t(n,r))&&(!u||n.id===u))return n;return null}function t(n,t){for(var f=u(n),r=f.split(" "),e=r.length,i=0;i<e;i++)if(r[i]==t)return!0;return!1}function e(n,t){var a=u(n),s=a.split(" "),h=r(s),o,e,c,l,i;if(t.indexOf(" ")>=0){for(e=t.split(" "),r(e),i=0;i<h;i++)c=e.indexOf(s[i]),c>=0&&e.splice(c,1);e.length>0&&(o=e.join(" "))}else{for(l=!1,i=0;i<h;i++)if(s[i]===t){l=!0;break}l||(o=t)}return o&&(h>0&&s[0].length>0?f(n,a+" "+o):f(n,o)),n}function tt(n,t){var r,i;if(n==null||t==null)return-1;if(n.indexOf)return n.indexOf(t);for(r=n.length,i=0;i<r;i++)if(n[i]==t)return i;return-1}function i(n,t){var s=u(n),o,h,c,i,l,e;if(t.indexOf(" ")>=0)o=t.split(" "),h=r(o);else{if(s.indexOf(t)<0)return n;o=[t];h=1}for(i=s.split(" "),l=r(i),e=l-1;e>=0;e--)tt(o,i[e])>=0&&(i.splice(e,1),c=!0);return c&&f(n,i.join(" ")),n}function r(n){for(var i=n.length,t=i-1;t>=0;t--)n[t]||(n.splice(t,1),i--);return i}function u(n){if(!n)return"";var t=n.className||"";return typeof t=="string"?t:t.baseVal||""}function f(n,t){var i=n.className||"";return typeof i=="string"?n.className=t:n.className.baseVal=t,n}function o(n,i){var u=i||document,f,e,o,r,s;if(u.getElementsByClassName)return u.getElementsByClassName(n);for(f=u.getElementsByTagName("*"),e=[],r=0,s=f.length;r<s;r++)o=f[r],t(o,n)&&e.push(o);return e}function it(n,t){var i=o(n,t);return i&&i.length>0&&i[0]}function l(n,t){var i=t||document;return i.getElementsByTagName?i.getElementsByTagName(n):null}function rt(n,t){var i=l(n,t);return i&&i.length>0&&i[0]}function a(n,t){n&&t&&!s(n,"data-tag")&&n.setAttribute("data-tag","multimedia."+t)}function ut(n,t,i,r){var u=sj_ce(n,t,i);return a(u,r),u}function ft(n,t){for(var i=o(n,t),u=i!=null?i.length:0,r=u-1;r>0;r--)t.removeChild(i[r])}function et(n,i){while(i&&!t(i,n))i=i.parentNode;return i}function ot(n,t){n&&(h(n,"tabindex","0"),e(n,"nofocus"),n.onkeyup=function(n){if(n.keyCode===9)i(this,"nofocus");else if(n.keyCode===13&&!t)if(this.click)this.click();else{var r=_d.createEvent("MouseEvents");r.initEvent("click",!0,!0);this.dispatchEvent(r,!0)}},n.onblur=function(){e(this,"nofocus")})}function st(n){n&&(c(n,"tabindex"),i(n,"nofocus"),i(n,"accessible"),n.onkeyup=null,n.onblur=null)}n.ga=s;n.ha=v;n.sa=h;n.a=y;n.r=p;n.ib=w;n.ia=b;n.ra=c;n.gcn=u;n.gih=k;n.sih=d;n.sit=g;n.sc=f;n.ac=e;n.rc=i;n.hc=t;n.gpc=nt;n.gebc=o;n.gfbc=it;n.adt=a;n.cew=ut;n.rdc=ft;n.gaebc=et;n.sac=ot;n.rac=st;n.gebt=l;n.gfbt=rt;pMMUtils.ga=n.ga;pMMUtils.ha=n.ha;pMMUtils.sa=n.sa;pMMUtils.a=n.a;pMMUtils.r=n.r;pMMUtils.ib=n.ib;pMMUtils.ia=n.ia;pMMUtils.ra=n.ra;pMMUtils.gcn=n.gcn;pMMUtils.gih=n.gih;pMMUtils.sih=n.sih;pMMUtils.sit=n.sit;pMMUtils.sc=n.sc;pMMUtils.ac=n.ac;pMMUtils.rc=n.rc;pMMUtils.hc=n.hc;pMMUtils.gpc=n.gpc;pMMUtils.gebc=n.gebc;pMMUtils.gfbc=n.gfbc;pMMUtils.adt=n.adt;pMMUtils.cew=n.cew;pMMUtils.rdc=n.rdc;pMMUtils.gaebc=n.gaebc;pMMUtils.sac=n.sac;pMMUtils.rac=n.rac;pMMUtils.gebt=n.gebt;pMMUtils.gfbt=n.gfbt})(MMUtilsDom||(MMUtilsDom={}));
//]]>--></div><div style="display:none" "><!--//<![CDATA[
sj_evt.bind("videoSerpAnswer_load", function() { sj_evt.fire("videoSerpAnswer_init","serpvidans", false); }, 1);;
//]]>--></div><div style="display:none" "><!--//<![CDATA[
sj_evt.bind("VideoTileV_load", function() { sj_evt.fire("VideoTileV_init","mc_vtvc__11", false, "slideexp"+"0_4F5BD7", 0, false); }, 1);;
//]]>--></div><div style="display:none" "><!--//<![CDATA[⁫
sj_evt.bind("VideoTileV_load", function() { sj_evt.fire("VideoTileV_init","mc_vtvc__13", false, "slideexp"+"0_4F5BD7", 1, false); }, 1);;sj_evt.bind("VideoTileV_load", function() { sj_evt.fire("VideoTileV_init","mc_vtvc__15", false, "slideexp"+"0_4F5BD7", 2, false); }, 1);;sj_evt.bind("VideoTileV_load", function() { sj_evt.fire("VideoTileV_init","mc_vtvc__17", false, "slideexp"+"0_4F5BD7", 3, false); }, 1);;sj_evt.bind("VideoTileV_load", function() { sj_evt.fire("VideoTileV_init","mc_vtvc__19", false, "slideexp"+"0_4F5BD7", 4, false); }, 1);;
//]]>--></div><div style="display:none" "><!--//<![CDATA[
var videoRichHoverConfig_serpvidans_hc ={"ah":"Filtrado por BúsquedaSegura. Haz clic para desactivar.","mth":"Vista previa","sth":"Sin vista previa","pid":"1.7","mmstob":false,"ishttps":true,"enperfinst":true,"enhoverhb":true,"enmmstinst":true,"enmuteinst":true,"hoverdelayms":200,"interactionType":1,"isAsyncBind":true,"enLoopPlayOnHover":false,"enAutoPlayOnLoad":false,"enAutoPlayNext":false,"autoPlayType":0,"resetPlayerTh":true,"enHoverControl":true,"enBgPlay":false,"enHoverAsyncMeta":false,"clientOffsetType":0,"instName":"VidCaption","fid":"VideoAnswer","iframeInsertID":"vahov","bh":false,"maa":false,"ffb":false,"va":1,"hmot":0,"pcc":0,"ic":true,"fdmtp":false,"fdmmst":false,"hsth":false,"tw":330,"th":186,"entabhover":true,"isBoundarySizePersisted":false,"enBoundarySizeScale":true,"encc":false,"isCaptionReplaced":true,"pea":1,"enpfc":false,"sd":1,"hmte":1,"autoRetriveMetaMaxCount":0,"boundaryElIds":[{"id":"serpvidans"}],"obt":1,"bindInfos":[{"tsqs":"#serpvidans","target":{"bcn":"mc_vtvc","tcn":"mc_vtvc","tacn":"mc_vtvc_link","ttcn":"mc_vtvc_link"}}]}; sj_evt.bind("Vi.VideoHover.Init", function() { if (VideoRichHover && VideoRichHover.init) { VideoRichHover.init(videoRichHoverConfig_serpvidans_hc, "serpvidans_hc"); } }, true);;
//]]>--></div><div style="display:none" "><!--//<![CDATA[
if (typeof(RelatedSearchesLGW_DLLGW) != "undefined") { RelatedSearchesLGW_DLLGW.setData(false, true, false, true, false, 400, false, true, 4); } else { RelatedSearchesLGW && RelatedSearchesLGW.setData(false, true, false, true, false, 400, false, true, 4); RelatedSearchesLGW && RelatedSearchesLGW.lgRenderBind(); };
//]]>--></div><div style="display:none" "><!--//<![CDATA[
var PaginationWithBFPRResults;(function(){function r(){var t,i,n,r;if(typeof webResultsUrls!="undefined"&&(t=document.querySelector("#b_results li.b_pag"),t))for(i=t.querySelectorAll("a.sb_bp"),n=0;n<i.length;n++)r=i[n],Lib.CssClass.contains(r,"sb_pag_first")||sj_be(r,"mousedown",u)}function u(){var r=f();sj_cook&&sj_cook.set(n,t,r,!1,"/");sj_cook&&sj_cook.set(n,i,_G.IG,!1,"/")}function f(){if(webResultsUrls.length===0)return"";var n;try{n=webResultsUrls.join(",");n=n===null||n===void 0?void 0:n.replace(/,/g,"%2C")}catch(t){throw new Error(t.message);}return n}var n="BFPRResults",t="FirstPageUrls",i="FPIG";r()})(PaginationWithBFPRResults||(PaginationWithBFPRResults={}));
//]]>--></div><div style="display:none" "><!--//<![CDATA[
window["fbk_29"] = function() { Feedback.Bootstrap.InitializeFeedback({page:true},"sb_feedback",1,0,0); sj_evt.unbind("fdbkbtstrp_init", window["fbk_29"]); }; sj_evt.bind("fdbkbtstrp_init", window["fbk_29"], 1);;var LoadThirdPartyIframe;(function(n){function r(){e();var n=document.createElement("iframe");n.id=i;n.style.display="none";n.src="".concat(t,"/instrument/cookieenabled");document.body.appendChild(n)}function u(){var n=document.getElementById(i);n&&n.parentNode&&n.parentNode.removeChild(n)}function f(n){n&&n.origin===t&&Log&&Log.Log&&(Log.Log("ClientInst","CookieInstrumentation","Thirdparty",!1,"IsEnabled",n.data),u())}function e(){var n=sb_gt(),t;n&&(t=Math.floor(n/1e3)*1e3,sj_cook.set("SRCHUSR","TPC",t.toString(),!0,"/"))}function o(){window.addEventListener("message",function(n){return f(n)})}function s(){o();r()}var t="https://3pcookiecheck.azureedge.net",i="3piframe";n.load=s})(LoadThirdPartyIframe||(LoadThirdPartyIframe={}));LoadThirdPartyIframe.load();;(function loadAutoSuggestCss(){ document.getElementById("as-css-link").rel = "stylesheet"; }());;
//]]>--></div><div style="display:none" "><!--//<![CDATA[
sj_evt.bind("promptForUserLocationInit", function() { SHPromptForUserLocation.init(true,"2F8652FD12A56ECE034D46D0133C6F04"); }, 1);;
//]]>--></div></div><script type="text/javascript" nonce="hdm+zCWukVbuQiu/8nwvTExR65VnfK54cOHZ35vK2Vk=" >//<![CDATA[
_G.HT=new Date;
//]]></script><script type="text/javascript" nonce="hdm+zCWukVbuQiu/8nwvTExR65VnfK54cOHZ35vK2Vk=">
    
</div>




            <span class="d-none" itemprop="commentCount"></span> 
    <div class="post-layout--right js-post-comments-component">
        <div id="comments-70294679" class="comments js-comments-container bt bc-black-200 mt12  dno" data-post-id="70294679" data-min-length="15">
            <ul class="comments-list js-comments-list"
                    data-remaining-comments-count="0"
                    data-canpost="false"
                    data-cansee="true"
                    data-comments-unavailable="false"
                    data-addlink-disabled="true">

            </ul>
	    </div>

        <div id="comments-link-70294679" data-rep=50 data-anon=true>
                    <a class="js-add-link comments-link disabled-link" title="Use comments to ask for more information or suggest improvements. Avoid comments like &#x201C;&#x2B;1&#x201D; or &#x201C;thanks&#x201D;."  href="#" role="button">Add a comment</a>
                <span class="js-link-separator dno">&nbsp;|&nbsp;</span>
            <a class="js-show-link comments-link dno" title="Expand to show all comments on this post" href=# onclick="" role="button"></a>
        </div>         
    </div>
    </div>
</div>

                                    
<a name="73957380"></a>
<div id="answer-73957380" class="answer js-answer" data-answerid="73957380" data-parentid="45558193" data-score="0" data-position-on-page="8" data-highest-scored="0" data-question-has-accepted-highest-score="0"  itemprop="suggestedAnswer" itemscope itemtype="https://schema.org/Answer">
    <div class="post-layout">
        <div class="votecell post-layout--left">
            <div class="js-voting-container d-flex jc-center fd-column ai-stretch gs4 fc-black-300" data-post-id="73957380" data-referrer="None">
        <button class="js-vote-up-btn flex--item s-btn ba bar-pill c-pointer as-center bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050"
                data-controller="s-tooltip"
                data-s-tooltip-placement="right"
                title="This answer is useful"
                aria-pressed="false"
                aria-label="Up vote"
                data-selected-classes="fc-theme-primary bc-theme-primary bg-theme-primary-100"
                data-unselected-classes="bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050">
            <svg aria-hidden="true" class="svg-icon iconArrowUp" width="18" height="18"  viewBox="0 0 18 18"><path  d="M1 12h16L9 4l-8 8Z"/></svg>
        </button>
        <div class="js-vote-count flex--item d-flex fd-column ai-center fc-theme-body-font fw-bold fs-subheading py4"
             itemprop="upvoteCount"
             data-value="0">
            0
        </div>
        <button class="js-vote-down-btn flex--item mb8 s-btn ba bar-pill c-pointer as-center bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050"
                data-controller="s-tooltip"
                data-s-tooltip-placement="right"
                title="This answer is not useful"
                aria-pressed="false"
                aria-label="Down vote"
                data-selected-classes="fc-theme-primary bc-theme-primary bg-theme-primary-100"
                data-unselected-classes="bc-black-225 fc-black-500 h:bg-theme-primary-200 h:fc-black-500 f:fc-black-050">
            <svg aria-hidden="true" class="svg-icon iconArrowDown" width="18" height="18"  viewBox="0 0 18 18"><path  d="M1 6h16l-8 8-8-8Z"/></svg>
        </button>


        
<button class="js-saves-btn s-btn s-btn__unset c-pointer py4"
        type="button"
        id="saves-btn-73957380"
        data-controller="s-tooltip"
        data-s-tooltip-placement="right"
        data-s-popover-placement=""
        title="Save this answer."
        aria-pressed="false"
        data-post-id="73957380"
        data-post-type-id="2"
        data-user-privilege-for-post-click="0"
        aria-controls=""
        data-s-popover-auto-show="false"
>
    <svg aria-hidden="true" class="fc-theme-primary-400 js-saves-btn-selected d-none svg-icon iconBookmark" width="18" height="18"  viewBox="0 0 18 18"><path  d="M3 17V3c0-1.1.9-2 2-2h8a2 2 0 0 1 2 2v14l-6-4-6 4Z"/></svg>
    <svg aria-hidden="true" class="js-saves-btn-unselected svg-icon iconBookmarkAlt" width="18" height="18"  viewBox="0 0 18 18"><path  d="m9 10.6 4 2.66V3H5v10.26l4-2.66ZM3 17V3c0-1.1.9-2 2-2h8a2 2 0 0 1 2 2v14l-6-4-6 4Z"/></svg>
</button>







            <div class="js-accepted-answer-indicator flex--item fc-green-400 py6 mtn8 d-none" data-s-tooltip-placement="right" title="Loading when this answer was accepted&#x2026;" tabindex="0" role="note" aria-label="Accepted">
                <div class="ta-center">
                    <svg aria-hidden="true" class="svg-icon iconCheckmarkLg" width="36" height="36"  viewBox="0 0 36 36"><path  d="m6 14 8 8L30 6v8L14 30l-8-8v-8Z"/></svg>
                </div>
            </div>

    
    <a class="js-post-issue flex--item s-btn s-btn__unset c-pointer py6 mx-auto" href="/posts/73957380/timeline" data-shortcut="T" data-ks-title="timeline" data-controller="s-tooltip" data-s-tooltip-placement="right" title="Show activity on this post." aria-label="Timeline"><svg aria-hidden="true" class="mln2 mr0 svg-icon iconHistory" width="19" height="18"  viewBox="0 0 19 18"><path  d="M3 9a8 8 0 1 1 3.73 6.77L8.2 14.3A6 6 0 1 0 5 9l3.01-.01-4 4-4-4h3L3 9Zm7-4h1.01L11 9.36l3.22 2.1-.6.93L10 10V5Z"/></svg></a>

</div>

        </div>

        

<div class="answercell post-layout--right">
    
    <div class="s-prose js-post-body" itemprop="text">
<p>You can add this to your <code>File: association</code> under settings in vscode:-</p>
<p>key - <code>*.js</code> value - <code>javascriptreact</code></p>
    </div>
    <div class="mt24">
        <div class="d-flex fw-wrap ai-start jc-end gs8 gsy">
            <time itemprop="dateCreated" datetime="2022-10-05T07:58:13"></time>
            <div class="flex--item mr16" style="flex: 1 1 100px;">
                


<div class="js-post-menu pt2" data-post-id="73957380" data-post-type-id="2">

    <div class="d-flex gs8 s-anchors s-anchors__muted fw-wrap">

        <div class="flex--item">
            <a href="/a/73957380"
               rel="nofollow"
               itemprop="url"
               class="js-share-link js-gps-track"
               title="Short permalink to this answer"
               data-gps-track="post.click({ item: 2, priv: 0, post_type: 2 })"
               data-controller="se-share-sheet"
               data-se-share-sheet-title="Share a link to this answer"
               data-se-share-sheet-subtitle=""
               data-se-share-sheet-post-type="answer"
               data-se-share-sheet-social="facebook twitter devto"
               data-se-share-sheet-location="2"
               data-se-share-sheet-license-url="https%3a%2f%2fcreativecommons.org%2flicenses%2fby-sa%2f4.0%2f"
               data-se-share-sheet-license-name="CC BY-SA 4.0"
               data-s-popover-placement="bottom-start">Share</a>
        </div>



                <div class="flex--item">
                    <button type="button"
                            id="btnFollowPost-73957380" class="s-btn s-btn__link js-follow-post js-follow-answer js-gps-track"
                            data-gps-track="post.click({ item: 14, priv: 0, post_type: 2 })"
                            data-controller="s-tooltip " data-s-tooltip-placement="bottom"
                            data-s-popover-placement="bottom" aria-controls=""
                            title="Follow this answer to receive notifications">
                        Follow
                    </button>
                </div>






    </div>
    <div class="js-menu-popup-container"></div>
</div>
            </div>


            <div class="post-signature flex--item fl0">
                <div class="user-info ">
    <div class="d-flex ">
        <div class="user-action-time fl-grow1">
            answered <span title='2022-10-05 07:58:13Z' class='relativetime'>Oct 5, 2022 at 7:58</span>
        </div>
        
    </div>
    <div class="user-gravatar32">
        <a href="/users/14773618/titulo"><div class="gravatar-wrapper-32"><img src="https://www.gravatar.com/avatar/0771f854f484a9572b5ea97036762a60?s=64&amp;d=identicon&amp;r=PG&amp;f=y&amp;so-version=2" alt="Titulo&#39;s user avatar" width="32" height="32" class="bar-sm"></div></a>
    </div>
    <div class="user-details" itemprop="author" itemscope itemtype="http://schema.org/Person">
        <a href="/users/14773618/titulo">Titulo</a><span class="d-none" itemprop="name">Titulo</span>
        <div class="-flair">
            <span class="reputation-score" title="reputation score " dir="ltr">17</span><span title="2 bronze badges" aria-hidden="true"><span class="badge3"></span><span class="badgecount">2</span></span><span class="v-visible-sr">2 bronze badges</span>
        </div>
    </div>
</div>


            </div>
        </div>
        
    
    </div>
    
</div>




            <span class="d-none" itemprop="commentCount"></span> 
    <div class="post-layout--right js-post-comments-component">
        <div id="comments-73957380" class="comments js-comments-container bt bc-black-200 mt12  dno" data-post-id="73957380" data-min-length="15">
            <ul class="comments-list js-comments-list"
                    data-remaining-comments-count="0"
                    data-canpost="false"
                    data-cansee="true"
                    data-comments-unavailable="false"
                    data-addlink-disabled="true">

            </ul>
	    </div>

        <div id="comments-link-73957380" data-rep=50 data-anon=true>
                    <a class="js-add-link comments-link disabled-link" title="Use comments to ask for more information or suggest improvements. Avoid comments like &#x201C;&#x2B;1&#x201D; or &#x201C;thanks&#x201D;."  href="#" role="button">Add a comment</a>
                <span class="js-link-separator dno">&nbsp;|&nbsp;</span>
            <a class="js-show-link comments-link dno" title="Expand to show all comments on this post" href=# onclick="" role="button"></a>
        </div>         
    </div>
    </div>
</div>


                        <a name='new-answer'></a>
                            <form id="post-form" action="/questions/45558193/answer/submit" method="post" class="js-add-answer-component post-form">
                                <input type="hidden" id="post-id" value="45558193" />
                                <input type="hidden" id="qualityBanWarningShown" name="qualityBanWarningShown" value="false" />
                                <input type="hidden" name="referrer" value="https://www.bing.com/" />
                                <h2 class="space" id="your-answer-header">
                                    Your Answer
                                </h2>
                                    

    <script>
        StackExchange.ifUsing("editor", function () {
            StackExchange.using("externalEditor", function () {
                StackExchange.using("snippets", function () {
                    StackExchange.snippets.init();
                });
            });
        }, "code-snippets");
    </script>


<script>
    StackExchange.ready(function() {
        var channelOptions = {
            tags: "".split(" "),
            id: "1"
        };
        initTagRenderer("".split(" "), "".split(" "), channelOptions);

        StackExchange.using("externalEditor", function() {
            // Have to fire editor after snippets, if snippets enabled
            if (StackExchange.settings.snippets.snippetsEnabled) {
                StackExchange.using("snippets", function() {
                    createEditor();
                });
            }
            else {
                createEditor();
            }
        });

        function createEditor() {
            StackExchange.prepareEditor({
                useStacksEditor: false,
                heartbeatType: 'answer',
                autoActivateHeartbeat: false,
                convertImagesToLinks: true,
                noModals: true,
                showLowRepImageUploadWarning: true,
                reputationToPostImages: 10,
                bindNavPrevention: true,
                postfix: "",
                imageUploader: {
                    brandingHtml: "Powered by \u003ca href=\"https://imgur.com/\"\u003e\u003csvg class=\"svg-icon\" width=\"50\" height=\"18\" viewBox=\"0 0 50 18\" fill=\"none\" xmlns=\"http://www.w3.org/2000/svg\"\u003e\u003ctitle\u003eImgur Logo\u003c/title\u003e\u003cpath d=\"M46.1709 9.17788C46.1709 8.26454 46.2665 7.94324 47.1084 7.58816C47.4091 7.46349 47.7169 7.36433 48.0099 7.26993C48.9099 6.97997 49.672 6.73443 49.672 5.93063C49.672 5.22043 48.9832 4.61182 48.1414 4.61182C47.4335 4.61182 46.7256 4.91628 46.0943 5.50789C45.7307 4.9328 45.2525 4.66231 44.6595 4.66231C43.6264 4.66231 43.1481 5.28821 43.1481 6.59048V11.9512C43.1481 13.2535 43.6264 13.8962 44.6595 13.8962C45.6924 13.8962 46.1709 13.2535 46.1709 11.9512V9.17788Z\"/\u003e\u003cpath d=\"M32.492 10.1419C32.492 12.6954 34.1182 14.0484 37.0451 14.0484C39.9723 14.0484 41.5985 12.6954 41.5985 10.1419V6.59049C41.5985 5.28821 41.1394 4.66232 40.1061 4.66232C39.0732 4.66232 38.5948 5.28821 38.5948 6.59049V9.60062C38.5948 10.8521 38.2696 11.5455 37.0451 11.5455C35.8209 11.5455 35.4954 10.8521 35.4954 9.60062V6.59049C35.4954 5.28821 35.0173 4.66232 34.0034 4.66232C32.9703 4.66232 32.492 5.28821 32.492 6.59049V10.1419Z\" /\u003e\u003cpath fill-rule=\"evenodd\" clip-rule=\"evenodd\" d=\"M25.6622 17.6335C27.8049 17.6335 29.3739 16.9402 30.2537 15.6379C30.8468 14.7755 30.9615 13.5579 30.9615 11.9512V6.59049C30.9615 5.28821 30.4833 4.66231 29.4502 4.66231C28.9913 4.66231 28.4555 4.94978 28.1109 5.50789C27.499 4.86533 26.7335 4.56087 25.7005 4.56087C23.1369 4.56087 21.0134 6.57349 21.0134 9.27932C21.0134 11.9852 23.003 13.913 25.3754 13.913C26.5612 13.913 27.4607 13.4902 28.1109 12.6616C28.1109 12.7229 28.1161 12.7799 28.121 12.8346C28.1256 12.8854 28.1301 12.9342 28.1301 12.983C28.1301 14.4373 27.2502 15.2321 25.777 15.2321C24.8349 15.2321 24.1352 14.9821 23.5661 14.7787C23.176 14.6393 22.8472 14.5218 22.5437 14.5218C21.7977 14.5218 21.2429 15.0123 21.2429 15.6887C21.2429 16.7375 22.9072 17.6335 25.6622 17.6335ZM24.1317 9.27932C24.1317 7.94324 24.9928 7.09766 26.1024 7.09766C27.2119 7.09766 28.0918 7.94324 28.0918 9.27932C28.0918 10.6321 27.2311 11.5116 26.1024 11.5116C24.9737 11.5116 24.1317 10.6491 24.1317 9.27932Z\"/\u003e\u003cpath d=\"M16.8045 11.9512C16.8045 13.2535 17.2637 13.8962 18.2965 13.8962C19.3298 13.8962 19.8079 13.2535 19.8079 11.9512V8.12928C19.8079 5.82936 18.4879 4.62866 16.4027 4.62866C15.1594 4.62866 14.279 4.98375 13.3609 5.88013C12.653 5.05154 11.6581 4.62866 10.3573 4.62866C9.34336 4.62866 8.57809 4.89931 7.9466 5.5079C7.58314 4.9328 7.10506 4.66232 6.51203 4.66232C5.47873 4.66232 5.00066 5.28821 5.00066 6.59049V11.9512C5.00066 13.2535 5.47873 13.8962 6.51203 13.8962C7.54479 13.8962 8.0232 13.2535 8.0232 11.9512V8.90741C8.0232 7.58817 8.44431 6.91179 9.53458 6.91179C10.5104 6.91179 10.893 7.58817 10.893 8.94108V11.9512C10.893 13.2535 11.3711 13.8962 12.4044 13.8962C13.4375 13.8962 13.9157 13.2535 13.9157 11.9512V8.90741C13.9157 7.58817 14.3365 6.91179 15.4269 6.91179C16.4027 6.91179 16.8045 7.58817 16.8045 8.94108V11.9512Z\"/\u003e\u003cpath d=\"M3.31675 6.59049C3.31675 5.28821 2.83866 4.66232 1.82471 4.66232C0.791758 4.66232 0.313354 5.28821 0.313354 6.59049V11.9512C0.313354 13.2535 0.791758 13.8962 1.82471 13.8962C2.85798 13.8962 3.31675 13.2535 3.31675 11.9512V6.59049Z\" /\u003e\u003cpath d=\"M1.87209 0.400291C0.843612 0.400291 0 1.1159 0 1.98861C0 2.87869 0.822846 3.57676 1.87209 3.57676C2.90056 3.57676 3.7234 2.87869 3.7234 1.98861C3.7234 1.1159 2.90056 0.400291 1.87209 0.400291Z\" fill=\"#1BB76E\"/\u003e\u003c/svg\u003e\u003c/a\u003e",
                    contentPolicyHtml: "User contributions licensed under \u003ca href=\"https://stackoverflow.com/help/licensing\"\u003eCC BY-SA\u003c/a\u003e \u003ca href=\"https://stackoverflow.com/legal/acceptable-use-policy\"\u003e(content policy)\u003c/a\u003e",
                    allowUrls: true
                },
                onDemand: true,
                discardSelector: ".discard-answer",
                enableTables: true,
                isStacksEditorPreviewEnabled: false
                ,enableTables:true,enableSnippets:true
            });
                    }
    });
</script>
<div id="post-editor" class="post-editor js-post-editor">


        <div class="ps-relative">
            <div class="wmd-container mb8">
                <div id="wmd-button-bar" class="wmd-button-bar btr-sm"></div>
                    <div class="ai-content-policy-notice js-ai-policy-notice fc-black p8 bl br bc-black-300 d-none" aria-hidden="true">
                        <div class="d-flex jc-space-between ac-center gsx gs2">
                            <p class="flex--item as-center"><b>Reminder:</b> Answers generated by artificial intelligence tools are not allowed on Stack Overflow. <a href="/help/ai-policy">Learn more</a></p>
                            <button class="flex--item js-dismiss-ai-banner s-btn s-btn__sm s-btn__icon fc-black"><svg aria-hidden="true" class="svg-icon iconClearSm" width="14" height="14"  viewBox="0 0 14 14"><path  d="M12 3.41 10.59 2 7 5.59 3.41 2 2 3.41 5.59 7 2 10.59 3.41 12 7 8.41 10.59 12 12 10.59 8.41 7 12 3.41Z"/></svg></button>
                        </div>
                    </div>
                    <input type="hidden" name="AIPolicyNoticeShown" value="true"/>
                <div class="js-stacks-validation">
                    <div class="ps-relative">
                        <textarea id="wmd-input"
                                  name="post-text"
                                  class="wmd-input s-input bar0 js-post-body-field"
                                  data-editor-type="wmd"
                                  data-post-type-id="2"
                                  cols="92" rows="15"
                                  aria-labelledby="your-answer-header"
                                  tabindex="101"
                                  data-min-length=""></textarea>
                    </div>
                    <div class="s-input-message mt4 d-none js-stacks-validation-message"></div>
                </div>
            </div>
        </div>

    <aside class="d-flex ai-start jc-space-between js-answer-help s-notice s-notice__warning pb0 pr4 pt4 mb8 d-none" role="status" aria-hidden="true">
    <div class="flex--item pt8">
        <p>Thanks for contributing an answer to Stack Overflow!</p><ul><li>Please be sure to <em>answer the question</em>. Provide details and share your research!</li></ul><p>But <em>avoid</em> …</p><ul><li>Asking for help, clarification, or responding to other answers.</li><li>Making statements based on opinion; back them up with references or personal experience.</li></ul><p>To learn more, see our <a href="/help/how-to-answer">tips on writing great answers</a>.</p>
    </div>
    <button class="flex--item js-answer-help-close-btn s-btn s-btn__muted fc-black-600">
        <svg aria-hidden="true" class="svg-icon iconClear" width="18" height="18"  viewBox="0 0 18 18"><path  d="M15 4.41 13.59 3 9 7.59 4.41 3 3 4.41 7.59 9 3 13.59 4.41 15 9 10.41 13.59 15 15 13.59 10.41 9 15 4.41Z"/></svg>
    </button>
</aside>



    <div>
        <div id="draft-saved" class="fc-success h24" style="display:none;">Draft saved</div>
        <div id="draft-discarded" class="fc-error h24" style="display:none;">Draft discarded</div>
    </div>


            <div id="wmd-preview" class="s-prose mb16 wmd-preview js-wmd-preview"></div>
            <div></div>

        <div class="edit-block">
            <input id="fkey" name="fkey" type="hidden" value="5662ef7983073cad2533440b428cb2f211f6e3919d4e5e1756ab13f36bbe9766">
            <input id="author" name="author" type="text">
        </div>

</div>


                                <div class="ps-relative">
                                                <div class="form-item dno new-post-login p0 my16">
                <div class="d-flex gs16 md:fd-column new-login-form">
                    <div class="d-flex fd-column w50 md:w-auto gsy gs8 jc-space-between new-login-left">
                        <h3 class="flex--item fs-title">Sign up or <a id="login-link" href="/users/login?ssrc=question_page&returnurl=https%3a%2f%2fstackoverflow.com%2fquestions%2f45558193%2fhow-to-make-vs-code-change-language-to-javascript-react-when-open-a-jsx-file%23new-answer">log in</a></h3>
                        <script>
                            StackExchange.ready(function () {
                                StackExchange.helpers.onClickDraftSave('#login-link');
                            });
                        </script>
                        <div class="flex--item s-btn s-btn__muted s-btn__outlined s-btn__icon google-login" data-ga="[&quot;sign up&quot;,&quot;Sign Up Started - Google&quot;,&quot;New Post&quot;,null,null]">
                            <svg aria-hidden="true" class="native svg-icon iconGoogle" width="18" height="18"  viewBox="0 0 18 18"><path fill="#4285F4" d="M16.51 8H8.98v3h4.3c-.18 1-.74 1.48-1.6 2.04v2.01h2.6a7.8 7.8 0 0 0 2.38-5.88c0-.57-.05-.66-.15-1.18Z"/><path fill="#34A853" d="M8.98 17c2.16 0 3.97-.72 5.3-1.94l-2.6-2a4.8 4.8 0 0 1-7.18-2.54H1.83v2.07A8 8 0 0 0 8.98 17Z"/><path fill="#FBBC05" d="M4.5 10.52a4.8 4.8 0 0 1 0-3.04V5.41H1.83a8 8 0 0 0 0 7.18l2.67-2.07Z"/><path fill="#EA4335" d="M8.98 4.18c1.17 0 2.23.4 3.06 1.2l2.3-2.3A8 8 0 0 0 1.83 5.4L4.5 7.49a4.77 4.77 0 0 1 4.48-3.3Z"/></svg> Sign up using Google
                        </div>
                        <div class="flex--item s-btn s-btn__muted s-btn__icon facebook-login" data-ga="[&quot;sign up&quot;,&quot;Sign Up Started - Facebook&quot;,&quot;New Post&quot;,null,null]">
                            <svg aria-hidden="true" class="svg-icon iconFacebook" width="18" height="18"  viewBox="0 0 18 18"><path fill="#4167B2" d="M3 1a2 2 0 0 0-2 2v12c0 1.1.9 2 2 2h12a2 2 0 0 0 2-2V3a2 2 0 0 0-2-2H3Zm6.55 16v-6.2H7.46V8.4h2.09V6.61c0-2.07 1.26-3.2 3.1-3.2.88 0 1.64.07 1.87.1v2.16h-1.29c-1 0-1.19.48-1.19 1.18V8.4h2.39l-.31 2.42h-2.08V17h-2.5Z"/></svg> Sign up using Facebook
                        </div>
                        <div class="flex--item s-btn s-btn__muted s-btn__outlined s-btn__icon stackexchange-login" data-ga="[&quot;sign up&quot;,&quot;Sign Up Navigation&quot;,&quot;New Post&quot;,null,null]">
                            <svg aria-hidden="true" class="native svg-icon iconLogoGlyphXSm" width="18" height="18" viewBox="0 0 18 18"><path d="M14 16v-5h2v7H2v-7h2v5h10Z" fill="#BCBBBB"/><path d="m12.09.72-1.21.9 4.5 6.07 1.22-.9L12.09.71ZM5 15h8v-2H5v2Zm9.15-5.87L8.35 4.3l.96-1.16 5.8 4.83-.96 1.16Zm-7.7-1.47 6.85 3.19.63-1.37-6.85-3.2-.63 1.38Zm6.53 5L5.4 11.39l.38-1.67 7.42 1.48-.22 1.46Z" fill="#F48024"/></svg> Sign up using Email and Password
                        </div>
                    </div>
                    <input type="hidden" name="use-facebook" class="use-facebook" value="false" />
                    <input type="hidden" name="use-google" class="use-google" value="false" />
                    <button type="button" class="d-none js-submit-openid">Submit</button>
                    <div class="d-flex gsy gs8 fd-column w50 md:w-auto new-login-right form-item p0">
                                <h3 class="flex--item fs-title">Post as a guest</h3>
            <div class="flex--item">
                <div class="d-flex gs4 gsy fd-column">
                    <label class="s-label" for="display-name">Name</label>
                    <div class="d-flex ps-relative">
                        <input class="s-input" id="display-name" name="display-name" maxlength="30" type="text" value="" tabindex="105" placeholder="" />
                    </div>
                </div>
            </div>
            <div class="flex--item">
                <div class="d-flex gs4 gsy fd-column">
                    <div class="flex--item">
                        <div class="d-flex gs2 gsy fd-column">
                            <label class="flex--item s-label" for="m-address">Email</label>
                            <p class="flex--item s-description">Required, but never shown</p>
                        </div>
                    </div>
                    <div class="d-flex ps-relative">
                        <input class="s-input js-post-email-field" id="m-address" name="m-address" type="text" value="" size="40" tabindex="106" placeholder="" />
                    </div>
                </div>
            </div>

                    </div>
                </div>
            </div>
            <script>
                StackExchange.ready(
                    function () {
                        StackExchange.openid.initPostLogin('.new-post-login', 'https%3a%2f%2fstackoverflow.com%2fquestions%2f45558193%2fhow-to-make-vs-code-change-language-to-javascript-react-when-open-a-jsx-file%23new-answer', 'question_page');
                    }
                );
            </script>
            <noscript>
                        <h3 class="flex--item fs-title">Post as a guest</h3>
            <div class="flex--item">
                <div class="d-flex gs4 gsy fd-column">
                    <label class="s-label" for="display-name">Name</label>
                    <div class="d-flex ps-relative">
                        <input class="s-input" id="display-name" name="display-name" maxlength="30" type="text" value="" tabindex="105" placeholder="" />
                    </div>
                </div>
            </div>
            <div class="flex--item">
                <div class="d-flex gs4 gsy fd-column">
                    <div class="flex--item">
                        <div class="d-flex gs2 gsy fd-column">
                            <label class="flex--item s-label" for="m-address">Email</label>
                            <p class="flex--item s-description">Required, but never shown</p>
                        </div>
                    </div>
                    <div class="d-flex ps-relative">
                        <input class="s-input js-post-email-field" id="m-address" name="m-address" type="text" value="" size="40" tabindex="106" placeholder="" />
                    </div>
                </div>
            </div>

            </noscript>

                                </div>

                                    <div class="form-submit clear-both d-flex gsx gs4">
                                        <button id="submit-button" class="flex--item s-btn s-btn__filled s-btn__icon" type="submit" tabindex="120" autocomplete="off">
                                            Post Your Answer
                                        </button>
                                        <button class="flex--item s-btn s-btn__danger discard-answer dno">
                                            Discard
                                        </button>
                                            <p class="privacy-policy-agreement">
                                                By clicking “Post Your Answer”, you agree to our <a href='https://stackoverflow.com/legal/terms-of-service/public' name='tos' target='_blank' class='-link'>terms of service</a> and acknowledge you have read our <a href='https://stackoverflow.com/legal/privacy-policy' name='privacy' target='_blank' class='-link'>privacy policy</a>.<input type="hidden" name="legalLinksShown" value="1" />
                                            </p>
                                    </div>
                                    <div class="js-general-error general-error clear-both d-none" aria-live="polite"></div>
                            </form>


                            <h2 class="bottom-notice" data-loc="1">
                                <div>
Not the answer you&#x27;re looking for? Browse other questions tagged <ul class='ml0 list-ls-none js-post-tag-list-wrapper d-inline'><li class='d-inline mr4 js-post-tag-list-item'><a href="/questions/tagged/visual-studio-code" class="post-tag" title="show questions tagged &#39;visual-studio-code&#39;" aria-label="show questions tagged &#39;visual-studio-code&#39;" rel="tag" aria-labelledby="tag-visual-studio-code-tooltip-container">visual-studio-code</a></li></ul> or <a href="/questions/ask">ask your own question</a>.                                </div>
                            </h2>
                </div>
            </div>

            
<div id="sidebar" class="show-votes" role="complementary" aria-label="sidebar">
        

    
    <div class="s-sidebarwidget s-sidebarwidget__yellow s-anchors s-anchors__grayscale mb16" data-tracker="cb=1">
        <ul class="d-block p0 m0">
                        <li class="s-sidebarwidget--header s-sidebarwidget__small-bold-text d-flex fc-black-500 d:fc-black-600 bb bbw1">
                            The Overflow Blog
                        </li>
        <li class="s-sidebarwidget--item d-flex px16">
            <div class="flex--item1 fl-shrink0">
<svg aria-hidden="true" class="va-text-top svg-icon iconPencilSm" width="14" height="14"  viewBox="0 0 14 14"><path fill="#F1B600" d="m2 10.12 6.37-6.43 1.88 1.88L3.88 12H2v-1.88Z"/><path fill="#E87C87" d="m11.1 1.71 1.13 1.12c.2.2.2.51 0 .71L11.1 4.7 9.21 2.86l1.17-1.15c.2-.2.51-.2.71 0Z"/></svg>            </div>
            <div class="flex--item wmn0 ow-break-word">
                <a href="https://stackoverflow.blog/2024/02/20/atlassian-compasss-software-component-catalog/" class="js-gps-track" data-ga="[&quot;community bulletin board&quot;,&quot;The Overflow Blog&quot;,&quot;https://stackoverflow.blog/2024/02/20/atlassian-compasss-software-component-catalog/&quot;,null,null]" data-gps-track="communitybulletin.click({ priority: 1, position: 0 })">Who owns this tool? You need a software component catalog</a>
            </div>
        </li>
                        <li class="s-sidebarwidget--header s-sidebarwidget__small-bold-text d-flex fc-black-500 d:fc-black-600 bb bbw1">
                            Featured on Meta
                        </li>
        <li class="s-sidebarwidget--item d-flex px16">
            <div class="flex--item1 fl-shrink0">
<div class="favicon favicon-stackexchangemeta" title="Meta Stack Exchange"></div>            </div>
            <div class="flex--item wmn0 ow-break-word">
                <a href="https://meta.stackexchange.com/questions/396794/upcoming-privacy-updates-removal-of-the-activity-data-section-and-google-conver" class="js-gps-track" title="Upcoming privacy updates: removal of the Activity data section and Google conversion pixel deprecation" data-ga="[&quot;community bulletin board&quot;,&quot;Featured on Meta&quot;,&quot;https://meta.stackexchange.com/questions/396794/upcoming-privacy-updates-removal-of-the-activity-data-section-and-google-conver&quot;,null,null]" data-gps-track="communitybulletin.click({ priority: 3, position: 1 })">Upcoming privacy updates: removal of the Activity data section and Google...</a>
            </div>
        </li>
        <li class="s-sidebarwidget--item d-flex px16">
            <div class="flex--item1 fl-shrink0">
<div class="favicon favicon-stackexchangemeta" title="Meta Stack Exchange"></div>            </div>
            <div class="flex--item wmn0 ow-break-word">
                <a href="https://meta.stackexchange.com/questions/396924/changing-how-community-leadership-works-on-stack-exchange-a-proposal-and-rough" class="js-gps-track" title="Changing how community leadership works on Stack Exchange: a proposal and rough timeline" data-ga="[&quot;community bulletin board&quot;,&quot;Featured on Meta&quot;,&quot;https://meta.stackexchange.com/questions/396924/changing-how-community-leadership-works-on-stack-exchange-a-proposal-and-rough&quot;,null,null]" data-gps-track="communitybulletin.click({ priority: 3, position: 2 })">Changing how community leadership works on Stack Exchange: a proposal and...</a>
            </div>
        </li>
        <li class="s-sidebarwidget--item d-flex px16">
            <div class="flex--item1 fl-shrink0">
<div class="favicon favicon-stackoverflowmeta" title="Meta Stack Overflow"></div>            </div>
            <div class="flex--item wmn0 ow-break-word">
                <a href="https://meta.stackoverflow.com/questions/421831/temporary-policy-generative-ai-e-g-chatgpt-is-banned" class="js-gps-track" data-ga="[&quot;community bulletin board&quot;,&quot;Featured on Meta&quot;,&quot;https://meta.stackoverflow.com/questions/421831/temporary-policy-generative-ai-e-g-chatgpt-is-banned&quot;,null,null]" data-gps-track="communitybulletin.click({ priority: 6, position: 3 })">Temporary policy: Generative AI (e.g., ChatGPT) is banned</a>
            </div>
        </li>
        <li class="s-sidebarwidget--item d-flex px16">
            <div class="flex--item1 fl-shrink0">
<div class="favicon favicon-stackoverflowmeta" title="Meta Stack Overflow"></div>            </div>
            <div class="flex--item wmn0 ow-break-word">
                <a href="https://meta.stackoverflow.com/questions/427864/discussions-update-expansion-to-all-tags" class="js-gps-track" data-ga="[&quot;community bulletin board&quot;,&quot;Featured on Meta&quot;,&quot;https://meta.stackoverflow.com/questions/427864/discussions-update-expansion-to-all-tags&quot;,null,null]" data-gps-track="communitybulletin.click({ priority: 6, position: 4 })">Discussions update: Expansion to all tags</a>
            </div>
        </li>
        <li class="s-sidebarwidget--item d-flex px16">
            <div class="flex--item1 fl-shrink0">
<div class="favicon favicon-stackoverflowmeta" title="Meta Stack Overflow"></div>            </div>
            <div class="flex--item wmn0 ow-break-word">
                <a href="https://meta.stackoverflow.com/questions/428183/2024-moderator-election-qa-question-collection" class="js-gps-track" data-ga="[&quot;community bulletin board&quot;,&quot;Featured on Meta&quot;,&quot;https://meta.stackoverflow.com/questions/428183/2024-moderator-election-qa-question-collection&quot;,null,null]" data-gps-track="communitybulletin.click({ priority: 6, position: 5 })">2024 Moderator Election Q&amp;A – Question Collection</a>
            </div>
        </li>
        </ul>
    </div>


<div class="js-zone-container zone-container-sidebar">
    <div id="dfp-tsb" class="everyonelovesstackoverflow everyoneloves__top-sidebar"></div>
		<div class="js-report-ad-button-container " style="width: 300px"></div>
</div>
<div class="js-zone-container zone-container-sidebar">
    <div id="dfp-msb" class="everyonelovesstackoverflow everyoneloves__mid-sidebar"></div>
		<div class="js-report-ad-button-container " style="width: 300px"></div>
</div>
<div id="hireme"></div>        <div class="module sidebar-linked">
	<h4 id="h-linked">Linked</h4>
	    <div class="linked" data-tracker="lq=1">
            <div class="spacer js-gps-track" data-gps-track="linkedquestion.click({ source_post_id: 45558193, target_question_id: 68516344, position: 0 })">
				<a href="/q/68516344" title="Question score (upvotes - downvotes)">
					<div class="answer-votes  default">0</div>
				</a>
				<a href="/questions/68516344/how-to-solve-code-formatter-problem-in-reactjs-program?noredirect=1" class="question-hyperlink">how to solve code formatter problem in reactjs program?</a>
			</div>
	</div>
</div>


    


        <div class="module sidebar-related">
            <h4 id="h-related">Related</h4>
            <div class="related js-gps-related-questions" data-tracker="rq=3">
                    <div class="spacer" data-question-id="32832264">
                        <a href="/q/32832264" title="Question score (upvotes - downvotes)" >
                            <div class="answer-votes answered-accepted default">63</div>
                        </a>
                        <a href="/questions/32832264/change-language-to-jsx-in-visual-studio-code" class="question-hyperlink">Change language to JSX in Visual Studio Code</a>
                    </div>
                    <div class="spacer" data-question-id="42447845">
                        <a href="/q/42447845" title="Question score (upvotes - downvotes)" >
                            <div class="answer-votes answered-accepted default">3</div>
                        </a>
                        <a href="/questions/42447845/vs-code-alters-jsx-when-saving-js-file" class="question-hyperlink">vs code alters jsx when saving .js file</a>
                    </div>
                    <div class="spacer" data-question-id="44993808">
                        <a href="/q/44993808" title="Question score (upvotes - downvotes)" >
                            <div class="answer-votes answered-accepted large">125</div>
                        </a>
                        <a href="/questions/44993808/visual-studio-code-changes-format-react-jsx" class="question-hyperlink">Visual studio code changes format (React-JSX)</a>
                    </div>
                    <div class="spacer" data-question-id="45903681">
                        <a href="/q/45903681" title="Question score (upvotes - downvotes)" >
                            <div class="answer-votes default">0</div>
                        </a>
                        <a href="/questions/45903681/react-with-vscode-editor" class="question-hyperlink">React with VScode Editor</a>
                    </div>
                    <div class="spacer" data-question-id="49929296">
                        <a href="/q/49929296" title="Question score (upvotes - downvotes)" >
                            <div class="answer-votes answered-accepted default">1</div>
                        </a>
                        <a href="/questions/49929296/visual-studio-code-change-language-mode-depending-on-project" class="question-hyperlink">Visual Studio Code change language mode depending on project</a>
                    </div>
                    <div class="spacer" data-question-id="58177640">
                        <a href="/q/58177640" title="Question score (upvotes - downvotes)" >
                            <div class="answer-votes answered-accepted default">7</div>
                        </a>
                        <a href="/questions/58177640/make-vscode-apply-syntax-highlighting-to-tsx-files" class="question-hyperlink">Make VSCode apply syntax highlighting to .tsx files</a>
                    </div>
                    <div class="spacer" data-question-id="59572779">
                        <a href="/q/59572779" title="Question score (upvotes - downvotes)" >
                            <div class="answer-votes answered-accepted default">0</div>
                        </a>
                        <a href="/questions/59572779/vscode-does-not-format-jsx-correctly" class="question-hyperlink">VSCode does not format JSX Correctly</a>
                    </div>
                    <div class="spacer" data-question-id="63486953">
                        <a href="/q/63486953" title="Question score (upvotes - downvotes)" >
                            <div class="answer-votes answered-accepted default">1</div>
                        </a>
                        <a href="/questions/63486953/vs-code-automatically-changes-language-to-javascript-from-react" class="question-hyperlink">VS Code automatically changes language to JavaScript from React</a>
                    </div>
                    <div class="spacer" data-question-id="67852958">
                        <a href="/q/67852958" title="Question score (upvotes - downvotes)" >
                            <div class="answer-votes default">0</div>
                        </a>
                        <a href="/questions/67852958/vs-code-jsx-auto-formatter" class="question-hyperlink">VS Code JSX auto-formatter</a>
                    </div>
                    <div class="spacer" data-question-id="68169844">
                        <a href="/q/68169844" title="Question score (upvotes - downvotes)" >
                            <div class="answer-votes default">0</div>
                        </a>
                        <a href="/questions/68169844/vscode-javascript-not-recognize-imports" class="question-hyperlink">VSCode javascript not recognize imports</a>
                    </div>
            </div>
        </div>
        <script type="text/javascript">
                 $(document).ready(function() {
                     $(".js-gps-related-questions .spacer").click(function () {
                        fireRelatedEvent($(this).index() + 1, $(this).data('question-id'));
                     });

                 function fireRelatedEvent(position, questionId) {
                     StackExchange.using("gps", function() {
                         StackExchange.gps.track('related_questions.click',
                         {
                             position: position,
                             originQuestionId: 45558193,
                             relatedQuestionId: +questionId,
                             location: 'sidebar',
                             source: 'Baseline'
                         });    
                     });
                 }
             });
         </script>



    <div id="hot-network-questions" class="module tex2jax_ignore">
    <h4>
        <a href="https://stackexchange.com/questions?tab=hot"
           class="js-gps-track s-link s-link__inherit" 
           data-gps-track="posts_hot_network.click({ item_type:1, location:11 })">
            Hot Network Questions
        </a>
    </h4>
    <ul>
            <li >
                <div class="favicon favicon-mathematica" title="Mathematica Stack Exchange"></div><a href="https://mathematica.stackexchange.com/questions/298506/list-of-built-in-functions-written-in-wolfram-language" class="js-gps-track question-hyperlink mb0" data-gps-track="site.switch({ item_type:11, target_site:387 }); posts_hot_network.click({ item_type:2, location:11 })">
                    List of built-in functions written in Wolfram Language
                </a>

            </li>
            <li >
                <div class="favicon favicon-physics" title="Physics Stack Exchange"></div><a href="https://physics.stackexchange.com/questions/802389/would-a-giant-ball-on-earth-roll-towards-the-poles" class="js-gps-track question-hyperlink mb0" data-gps-track="site.switch({ item_type:11, target_site:151 }); posts_hot_network.click({ item_type:2, location:11 })">
                    Would a giant ball on earth roll towards the poles?
                </a>

            </li>
            <li >
                <div class="favicon favicon-writing" title="Writing Stack Exchange"></div><a href="https://writing.stackexchange.com/questions/67888/how-do-i-write-a-sexist-narrator-without-coming-off-as-sexist-myself" class="js-gps-track question-hyperlink mb0" data-gps-track="site.switch({ item_type:11, target_site:166 }); posts_hot_network.click({ item_type:2, location:11 })">
                    How do I write a sexist narrator without coming off as sexist myself?
                </a>

            </li>
            <li >
                <div class="favicon favicon-aviation" title="Aviation Stack Exchange"></div><a href="https://aviation.stackexchange.com/questions/102998/are-flights-in-fast-jet-streams-more-dangerous" class="js-gps-track question-hyperlink mb0" data-gps-track="site.switch({ item_type:11, target_site:528 }); posts_hot_network.click({ item_type:2, location:11 })">
                    Are flights in fast jet streams more dangerous?
                </a>

            </li>
            <li >
                <div class="favicon favicon-travel" title="Travel Stack Exchange"></div><a href="https://travel.stackexchange.com/questions/186524/10-year-uk-visa-ban-15-years-later-and-new-citizenship" class="js-gps-track question-hyperlink mb0" data-gps-track="site.switch({ item_type:11, target_site:273 }); posts_hot_network.click({ item_type:2, location:11 })">
                    10-year UK visa ban. 15 years later and new citizenship
                </a>

            </li>
            <li class="dno js-hidden">
                <div class="favicon favicon-politics" title="Politics Stack Exchange"></div><a href="https://politics.stackexchange.com/questions/84846/what-leverage-or-negotiation-tools-do-government-agencies-actually-have-to-negot" class="js-gps-track question-hyperlink mb0" data-gps-track="site.switch({ item_type:11, target_site:475 }); posts_hot_network.click({ item_type:2, location:11 })">
                    What leverage or negotiation tools do government agencies actually have to negotiate prescription drug prices directly with drug companies?
                </a>

            </li>
            <li class="dno js-hidden">
                <div class="favicon favicon-english" title="English Language &amp; Usage Stack Exchange"></div><a href="https://english.stackexchange.com/questions/618917/word-request-sensatory-words" class="js-gps-track question-hyperlink mb0" data-gps-track="site.switch({ item_type:11, target_site:97 }); posts_hot_network.click({ item_type:2, location:11 })">
                    Word request: Sensatory words
                </a>

            </li>
            <li class="dno js-hidden">
                <div class="favicon favicon-mathematica" title="Mathematica Stack Exchange"></div><a href="https://mathematica.stackexchange.com/questions/298495/marking-circle-centres-with-and-x" class="js-gps-track question-hyperlink mb0" data-gps-track="site.switch({ item_type:11, target_site:387 }); posts_hot_network.click({ item_type:2, location:11 })">
                    Marking circle centres with &#x2B; and x
                </a>

            </li>
            <li class="dno js-hidden">
                <div class="favicon favicon-hsm" title="History of Science and Mathematics Stack Exchange"></div><a href="https://hsm.stackexchange.com/questions/16236/did-ronald-fisher-ever-say-anything-on-varying-the-threshold-of-significance-lev" class="js-gps-track question-hyperlink mb0" data-gps-track="site.switch({ item_type:11, target_site:587 }); posts_hot_network.click({ item_type:2, location:11 })">
                    Did Ronald Fisher ever say anything on varying the threshold of significance level?
                </a>

            </li>
            <li class="dno js-hidden">
                <div class="favicon favicon-diy" title="Home Improvement Stack Exchange"></div><a href="https://diy.stackexchange.com/questions/293925/why-are-wires-connected-only-to-the-line-side-of-one-gfci-outlet" class="js-gps-track question-hyperlink mb0" data-gps-track="site.switch({ item_type:11, target_site:73 }); posts_hot_network.click({ item_type:2, location:11 })">
                    Why are wires connected only to the line side of one GFCI outlet?
                </a>

            </li>
            <li class="dno js-hidden">
                <div class="favicon favicon-gis" title="Geographic Information Systems Stack Exchange"></div><a href="https://gis.stackexchange.com/questions/476479/iterating-over-the-features-of-a-vector-layor-then-performing-zonal-statistics-a" class="js-gps-track question-hyperlink mb0" data-gps-track="site.switch({ item_type:11, target_site:79 }); posts_hot_network.click({ item_type:2, location:11 })">
                    Iterating over the features of a vector layor then performing zonal statistics algorithm on each feature in PyQGIS
                </a>

            </li>
            <li class="dno js-hidden">
                <div class="favicon favicon-worldbuilding" title="Worldbuilding Stack Exchange"></div><a href="https://worldbuilding.stackexchange.com/questions/254509/valid-reason-for-not-having-plate-armor-in-early-medieval-low-magic-fantasy-set" class="js-gps-track question-hyperlink mb0" data-gps-track="site.switch({ item_type:11, target_site:579 }); posts_hot_network.click({ item_type:2, location:11 })">
                    Valid reason for not having plate armor in early medieval, low-magic fantasy setting?
                </a>

            </li>
            <li class="dno js-hidden">
                <div class="favicon favicon-android" title="Android Enthusiasts Stack Exchange"></div><a href="https://android.stackexchange.com/questions/256247/does-android-os-impose-an-sms-sending-limit" class="js-gps-track question-hyperlink mb0" data-gps-track="site.switch({ item_type:11, target_site:139 }); posts_hot_network.click({ item_type:2, location:11 })">
                    Does Android OS impose an SMS sending limit?
                </a>

            </li>
            <li class="dno js-hidden">
                <div class="favicon favicon-mathoverflow" title="MathOverflow"></div><a href="https://mathoverflow.net/questions/464630/could-someone-explain-the-proof-of-this-formula-clearly-i-got-the-wrong-values" class="js-gps-track question-hyperlink mb0" data-gps-track="site.switch({ item_type:11, target_site:504 }); posts_hot_network.click({ item_type:2, location:11 })">
                    Could someone explain the proof of this formula clearly? I got the wrong values for spanning trees with this formula and with Cayley&#x27;s formula
                </a>

            </li>
            <li class="dno js-hidden">
                <div class="favicon favicon-stats" title="Cross Validated"></div><a href="https://stats.stackexchange.com/questions/639918/best-way-to-impute-missing-values-in-a-time-series" class="js-gps-track question-hyperlink mb0" data-gps-track="site.switch({ item_type:11, target_site:65 }); posts_hot_network.click({ item_type:2, location:11 })">
                    Best way to impute missing values in a time series
                </a>

            </li>
            <li class="dno js-hidden">
                <div class="favicon favicon-workplace" title="The Workplace Stack Exchange"></div><a href="https://workplace.stackexchange.com/questions/195596/putting-software-engineer-on-resume-if-the-company-is-a-flat-org-and-told-me-w" class="js-gps-track question-hyperlink mb0" data-gps-track="site.switch({ item_type:11, target_site:423 }); posts_hot_network.click({ item_type:2, location:11 })">
                    Putting &quot;software engineer&quot; on resume if the company is a flat org and told me we were all hired seniors previously?
                </a>

            </li>
            <li class="dno js-hidden">
                <div class="favicon favicon-stats" title="Cross Validated"></div><a href="https://stats.stackexchange.com/questions/639828/sample-size-impact-on-effect-size" class="js-gps-track question-hyperlink mb0" data-gps-track="site.switch({ item_type:11, target_site:65 }); posts_hot_network.click({ item_type:2, location:11 })">
                    Sample Size impact on Effect size
                </a>

            </li>
            <li class="dno js-hidden">
                <div class="favicon favicon-aviation" title="Aviation Stack Exchange"></div><a href="https://aviation.stackexchange.com/questions/103027/what-happens-when-the-runway-is-unusable-at-an-isolated-aerodrome" class="js-gps-track question-hyperlink mb0" data-gps-track="site.switch({ item_type:11, target_site:528 }); posts_hot_network.click({ item_type:2, location:11 })">
                    What happens when the runway is unusable at an isolated aerodrome?
                </a>

            </li>
            <li class="dno js-hidden">
                <div class="favicon favicon-mathematica" title="Mathematica Stack Exchange"></div><a href="https://mathematica.stackexchange.com/questions/298483/how-to-call-a-list-of-pure-functions-pure-vector-function" class="js-gps-track question-hyperlink mb0" data-gps-track="site.switch({ item_type:11, target_site:387 }); posts_hot_network.click({ item_type:2, location:11 })">
                    How to call a list of pure functions/pure vector function?
                </a>

            </li>
            <li class="dno js-hidden">
                <div class="favicon favicon-politics" title="Politics Stack Exchange"></div><a href="https://politics.stackexchange.com/questions/84834/why-did-it-take-so-long-for-the-u-s-government-to-give-medicare-the-power-to-ne" class="js-gps-track question-hyperlink mb0" data-gps-track="site.switch({ item_type:11, target_site:475 }); posts_hot_network.click({ item_type:2, location:11 })">
                    Why did it take so long for the U.S. government to give Medicare the power to negotiate prescription drug prices directly with drug companies?
                </a>

            </li>
            <li class="dno js-hidden">
                <div class="favicon favicon-ell" title="English Language Learners Stack Exchange"></div><a href="https://ell.stackexchange.com/questions/347918/do-you-say-give-a-sociable-compliment-when-you-mean-you-give-a-compliment-as-a" class="js-gps-track question-hyperlink mb0" data-gps-track="site.switch({ item_type:11, target_site:481 }); posts_hot_network.click({ item_type:2, location:11 })">
                    Do you say &quot;give a sociable compliment&quot; when you mean you give a compliment as a way to socialize with people but it&#x27;s not a true praise?
                </a>

            </li>
            <li class="dno js-hidden">
                <div class="favicon favicon-politics" title="Politics Stack Exchange"></div><a href="https://politics.stackexchange.com/questions/84849/has-the-niger-junta-explained-why-they-expelled-french-but-not-us-troops" class="js-gps-track question-hyperlink mb0" data-gps-track="site.switch({ item_type:11, target_site:475 }); posts_hot_network.click({ item_type:2, location:11 })">
                    Has the Niger junta explained why they expelled French but not US troops?
                </a>

            </li>
            <li class="dno js-hidden">
                <div class="favicon favicon-medicalsciences" title="Medical Sciences Stack Exchange"></div><a href="https://medicalsciences.stackexchange.com/questions/33833/is-it-possible-to-survive-your-appendix-bursting-without-surgical-intervention" class="js-gps-track question-hyperlink mb0" data-gps-track="site.switch({ item_type:11, target_site:607 }); posts_hot_network.click({ item_type:2, location:11 })">
                    Is it possible to survive your appendix bursting without surgical intervention?
                </a>

            </li>
            <li class="dno js-hidden">
                <div class="favicon favicon-blender" title="Blender Stack Exchange"></div><a href="https://blender.stackexchange.com/questions/312363/gradient-along-the-surface-of-a-helical-spiral" class="js-gps-track question-hyperlink mb0" data-gps-track="site.switch({ item_type:11, target_site:502 }); posts_hot_network.click({ item_type:2, location:11 })">
                    Gradient along the surface of a helical spiral
                </a>

            </li>
    </ul>

        <a href="#" 
           class="show-more js-show-more js-gps-track" 
           data-gps-track="posts_hot_network.click({ item_type:3, location:11 })">
            more hot questions
        </a>
</div>

                <div id="feed-link" class="js-feed-link">
        <a href="/feeds/question/45558193" title="Feed of this question and its answers">
            <svg aria-hidden="true" class="fc-orange-400 svg-icon iconRss" width="18" height="18"  viewBox="0 0 18 18"><path  d="M3 1a2 2 0 0 0-2 2v12c0 1.1.9 2 2 2h12a2 2 0 0 0 2-2V3a2 2 0 0 0-2-2H3Zm0 1.5c6.9 0 12.5 5.6 12.5 12.5H13C13 9.55 8.45 5 3 5V2.5Zm0 5c4.08 0 7.5 3.41 7.5 7.5H8c0-2.72-2.28-5-5-5V7.5Zm0 5c1.36 0 2.5 1.14 2.5 2.5H3v-2.5Z"/></svg>
            Question feed
        </a>
    </div>
    <aside class="s-modal js-feed-link-modal" tabindex="-1" role="dialog" aria-labelledby="feed-modal-title" aria-describedby="feed-modal-description" aria-hidden="true">
        <div class="s-modal--dialog js-modal-dialog wmx4" role="document"  data-controller="se-draggable">
            <h1 class="s-modal--header fw-bold js-first-tabbable" id="feed-modal-title" data-se-draggable-target="handle" tabindex="0">
                Subscribe to RSS
            </h1>
            <div class="d-flex gs4 gsy fd-column">
                <div class="flex--item">
                    <label class="d-block s-label c-default" for="feed-url">
                        Question feed
                        <p class="s-description mt2" id="feed-modal-description">To subscribe to this RSS feed, copy and paste this URL into your RSS reader.</p>
                    </label>
                </div>
                <div class="d-flex ps-relative">
                    <input class="s-input" type="text" name="feed-url" id="feed-url" readonly="readonly" value="https://stackoverflow.com/feeds/question/45558193" />
                    <svg aria-hidden="true" class="s-input-icon fc-orange-400 svg-icon iconRss" width="18" height="18"  viewBox="0 0 18 18"><path  d="M3 1a2 2 0 0 0-2 2v12c0 1.1.9 2 2 2h12a2 2 0 0 0 2-2V3a2 2 0 0 0-2-2H3Zm0 1.5c6.9 0 12.5 5.6 12.5 12.5H13C13 9.55 8.45 5 3 5V2.5Zm0 5c4.08 0 7.5 3.41 7.5 7.5H8c0-2.72-2.28-5-5-5V7.5Zm0 5c1.36 0 2.5 1.14 2.5 2.5H3v-2.5Z"/></svg>
                </div>
            </div>
            <a class="s-modal--close s-btn s-btn__muted js-modal-close js-last-tabbable" href="#" aria-label="Close">
                <svg aria-hidden="true" class="svg-icon iconClearSm" width="14" height="14"  viewBox="0 0 14 14"><path  d="M12 3.41 10.59 2 7 5.59 3.41 2 2 3.41 5.59 7 2 10.59 3.41 12 7 8.41 10.59 12 12 10.59 8.41 7 12 3.41Z"/></svg>
            </a>
        </div>
    </aside>

</div>

    </div>
<script>StackExchange.ready(function(){$.get('/posts/45558193/ivc/a304?prg=0704a782-a32f-4a26-90cb-340f749e50a7');});</script>
<noscript><div><img src="/posts/45558193/ivc/a304?prg=0704a782-a32f-4a26-90cb-340f749e50a7" class="dno" alt="" width="0" height="0"></div></noscript><div style="display:none" id="js-codeblock-lang"></div></div>

                        

        </div>
    </div>

        
    <script type="text/javascript">
    var cam = cam || { opt: {} };
    var clcGamLoaderOptions = cam || { opt: {} };
    var opt = clcGamLoaderOptions.opt;

    opt.omni = 'BwoLCKTpvID9gdo8EAUYsdPcFSACKAI6FHx2aXN1YWwtc3R1ZGlvLWNvZGV8QABIACSgMNuJL-FgewI';
    opt.refresh = !1;
    opt.refreshInterval = 90;
    opt.sf = !0;
    opt.hb = !1;
    opt.ll = !0;
    opt.tlb_position = 0;
    opt.personalization_consent = !1;
    opt.targeting_consent = !1;
    opt.performance_consent = !1;

    opt.targeting = {Registered:['false'],'so-tag':['visual-studio-code'],'tag-reportable':['visual-studio-code'],NumberOfAnswers:['8']};
    opt.adReportEnabled = !0;
    opt.adReportUrl = '/ads/report-ad';
    opt.adReportText = 'Report this ad';
	opt.adReportFileTypeErrorMessage = 'Please select a PNG or JPG file.';
    opt.adReportFileSizeErrorMessage = 'The file must be under 2 MiB.';
	opt.adReportErrorText = 'Error uploading ad report.';
	opt.adReportThanksText = 'Thanks for your feedback. We’ll review this against our code of conduct and take action if necessary.';
    opt.adReportLoginExpiredMessage = 'Your login session has expired, please login and try again.';
    opt.adReportLoginErrorMessage = 'An error occurred when loading the report form - please try again';
	opt.adReportModalClass = 'js-ad-report';
    opt.perRequestGuid = '0704a782-a32f-4a26-90cb-340f749e50a7';
    opt.responseHash = 'UX2JRCXjnxy3mxEhZX&#x2B;EWU7Nqo8BHD0eWWYMDzBo85I=';


    opt.targeting.TargetingConsent = ['False_Active'];
    opt.allowAccountTargetingForThisRequest = !1;

    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('dfptestads')) {
        const dfptestads = urlParams.get('dfptestads');
        opt.targeting.DfpTestAds = dfptestads;
    }
</script>

        
    <footer id="footer" class="site-footer js-footer theme-light__forced" role="contentinfo">
        <div class="site-footer--container">
                <div class="site-footer--logo">

                    <a href="https://stackoverflow.com" aria-label="Stack Overflow"><svg aria-hidden="true" class="native svg-icon iconLogoGlyphMd" width="32" height="37" viewBox="0 0 32 37"><path d="M26 33v-9h4v13H0V24h4v9h22Z" fill="#BCBBBB"/><path d="m21.5 0-2.7 2 9.9 13.3 2.7-2L21.5 0ZM26 18.4 13.3 7.8l2.1-2.5 12.7 10.6-2.1 2.5ZM9.1 15.2l15 7 1.4-3-15-7-1.4 3Zm14 10.79.68-2.95-16.1-3.35L7 23l16.1 2.99ZM23 30H7v-3h16v3Z" fill="#F48024"/></svg></a>
                </div>
            <nav class="site-footer--nav">
                    <div class="site-footer--col">
                        <h5 class="-title"><a href="https://stackoverflow.com" class="js-gps-track" data-gps-track="footer.click({ location: 2, link: 15})">Stack Overflow</a></h5>
                        <ul class="-list js-primary-footer-links"> "name": "Radius CLI",
    "id": "radcli",
    "version": "0.1.0",
    "dependsOn": {
        "ghcr.io/dhoeric/features/oras:1": {}
    },
    "description": "Installs the Radius CLI along with needed dependencies.",
    "documentationURL": "https://docs.radapp.io/installation/",
    "options": {
        "version": {
            "type": "string",
            "proposals": [
                "latest",
                "edge"
            ],
            "default": "latest",
            "description": "Select or enter an Radius CLI version. Use 'latest' for the latest stable version, 'edge' for the latest development version, or a specific version number (e.g. 0.28.0)."
        }
    }
}
                            <li><a href="/questions" class="js-gps-track -link" data-gps-track="footer.click({ location: 2, link: 16})">Questions</a></li>
                                <li><a href="/help" class="js-gps-track -link" data-gps-track="footer.click({ location: 2, link: 3 })">Help</a></li>
                        </ul>
                    </div>
                    <div class="site-footer--col">
                        <h5 class="-title"><a href="https://stackoverflow.co/" class="js-gps-track" data-gps-track="footer.click({ location: 2, link: 19 })">Products</a></h5>
                        <ul class="-list">
                            <li><a href="https://stackoverflow.co/teams/" class="js-gps-track -link"
                                                 data-ga="[&quot;teams traffic&quot;,&quot;footer - site nav&quot;,&quot;stackoverflow.com/teams&quot;,null,{&quot;dimension4&quot;:&quot;teams&quot;}]"
                                                 data-gps-track="footer.click({ location: 2, link: 29 })">Teams</a></li>
                            <li><a href="https://stackoverflow.co/advertising/" class="js-gps-track -link" data-gps-track="footer.click({ location: 2, link: 21 })">Advertising</a></li>
                            <li><a href="https://stackoverflow.co/collectives/" class="js-gps-track -link" data-gps-track="footer.click({ location: 2, link: 40 })">Collectives</a></li>
                            <li><a href="https://stackoverflow.co/talent/" class="js-gps-track -link" data-gps-track="footer.click({ location: 2, link: 20 })">Talent</a></li>
                        </ul>
                    </div>
                <div class="site-footer--col">
                    <h5 class="-title"><a class="js-gps-track" data-gps-track="footer.click({ location: 2, link: 1 })" href="https://stackoverflow.co/">Company</a></h5>
                    <ul class="-list">
                            <li><a class="js-gps-track -link" data-gps-track="footer.click({ location: 2, link: 1 })" href="https://stackoverflow.co/">About</a></li>
                        <li><a class="js-gps-track -link" data-gps-track="footer.click({ location: 2, link: 27 })" href="https://stackoverflow.co/company/press/">Press</a></li>
                            <li><a class="js-gps-track -link" data-gps-track="footer.click({ location: 2, link: 9 })" href="https://stackoverflow.co/company/work-here/">Work Here</a></li>
                        <li><a class="js-gps-track -link" data-gps-track="footer.click({ location: 2, link: 7 })" href="https://stackoverflow.com/legal">Legal</a></li>
                        <li><a class="js-gps-track -link" data-gps-track="footer.click({ location: 2, link: 8 })" href="https://stackoverflow.com/legal/privacy-policy">Privacy Policy</a></li>
                        <li><a class="js-gps-track -link" data-gps-track="footer.click({ location: 2, link: 37 })" href="https://stackoverflow.com/legal/terms-of-service/public">Terms of Service</a></li>
                            <li><a class="js-gps-track -link" data-gps-track="footer.click({ location: 2, link: 13 })" href="/contact">Contact Us</a></li>
                            <li class="" id="consent-footer-link"><a class="js-gps-track -link js-cookie-settings" data-gps-track="footer.click({ location: 2, link: 38 })" href="#" data-consent-popup-loader="footer">Cookie Settin# git ls-files --others --exclude-from=.git/info/exclude
# Lines that start with '#' are comments.
# For a project mostly in C, the following would be a good set of                 —————}
—————————————————————————————————————————————

 Прогр‏амма даёт возможность добавить или удалить папку или файл в исключения
Windows Defender.

# exclude patterns (uncomment them if you want to use them):
# *.[oa]
# *~
gs</a></li>
                        <li><a class="js-gps-track -link" data-gps-track="footer.click({ location: 2, link: 39 })" href="https://stackoverflow.com/legal/cookie-policy">Cookie Policy</a></li>
                    </ul>
                </div>
                <div class="site-footer--col site-footer--categories-nav">
                    <div>
                        <h5 class="-title"><a href="https://stackexchange.com" data-gps-track="footer.click({ location: 2, link: 30 })">Stack Exchange Network</a></h5>
                        <ul class="-list">
                            <li>
                                <a href="https://stackexchange.com/sites#technology" class="-link js-gps-track" data-gps-track="footer.click({ location: 2, link: 24 })">
                                    Technology
                                </a>
                            </li>
                            <li>
                                <a href="https://stackexchange.com/sites#culturerecreation" class="-link js-gps-track" data-gps-track="footer.click({ location: 2, link: 24 })">
                                    Culture &amp; recreation
                                </a>
                            </li>
                            <li>
                                <a href="https://stackexchange.com/sites#lifearts" class="-link js-gps-track" data-gps-track="footer.click({ location: 2, link: 24 })">
                                    Life &amp; arts
                                </a>
                            </li>
                            <li>
                                <a href="https://stackexchange.com/sites#science" class="-link js-gps-track" data-gps-track="footer.click({ location: 2, link: 24 })">
                                    Science
                                </a>
                            </li>
                            <li>
                                <a href="https://stackexchange.com/sites#professional" class="-link js-gps-track" data-gps-track="footer.click({ location: 2, link: 24 })">
                                    Professional
                                </a>
                            </li>
                            <li>
                                <a href="https://stackexchange.com/sites#business" class="-link js-gps-track" data-gps-track="footer.click({ location: 2, link: 24 })">
                                    Business
                                </a>
                            </li>

                            <li class="mt16 md:mt0">
                                <a href="https://api.stackexchange.com/" class="-link js-gps-track" data-gps-track="footer.click({ location: 2, link: 24 })">
                                    API
                                </a>
                            </li>

                            <li>
                                <a href="https://data.stackexchange.com/" class="-link js-gps-track" data-gps-track="footer.click({ location: 2, link: 24 })">
                                    Data
                                </a>
                            </li>
                        </ul>
                    </div>
                </div>
            </nav>
            <div class="site-footer--copyright fs-fine md:mt24">
                <ul class="-list -social md:mb8">
                    <li><a class="js-gps-track -link" data-gps-track="footer.click({ location: 2, link:4 })" href="https://stackoverflow.blog?blb=1">Blog</a></li>
                    <li><a href="https://www.facebook.com/officialstackoverflow/" class="-link js-gps-track" data-gps-track="footer.click({ location: 2, link: 31 })">Facebook</a></li>
                    <li><a href="https://twitter.com/stackoverflow" class="-link js-gps-track" data-gps-track="footer.click({ location: 2, link: 32 })">Twitter</a></li>
                    <li><a href="https://linkedin.com/company/stack-overflow" class="-link js-gps-track" data-gps-track="footer.click({ location: 2, link: 33 })">LinkedIn</a></li>
                    <li><a href="https://www.instagram.com/thestackoverflow" class="-link js-gps-track" data-gps-track="footer.click({ location: 2, link: 36 })">Instagram</a></li>
                </ul>

                <p class="md:mb0">
Site design / logo &#169; 2024 Stack Exchange Inc; user contributions licensed under <span class='td-underline'><a href="https://stackoverflow.com/help/licensing">CC BY-SA</a></span>.                    <span id="svnrev">rev&nbsp;2024.2.22.5258</span>
                </p>
            </div>
        </div>

    </footer>


    

            <!-- Google tag (gtag.js) -->
            <script async src="https://www.googletagmanager.com/gtag/js?id=G-WCZ03SZFCQ"></script>
            <script>
                window.dataLayer = window.dataLayer || [];
                function gtag() { dataLayer.push(arguments); }
            </script>
        <script>
            StackExchange.ready(function() {

                var ga3Settings = {
                    autoLink: ["stackoverflow.blog","info.stackoverflowsolutions.com","stackoverflowsolutions.com"],
                    sendTitles: true,
                    tracker: window.ga,
                    trackingCodes: [
                        'UA-108242619-1'
                    ],
                    checkDimension: 'dimension42'‪
                };

                var customGA4Dimensions = {};

⁭
<name>Victor Wong</name>
<title/>
<department/>
<company/>
</author>
</item>
<item>
<title>Novedades en CES 2024</title>
<link>https://blog.google/intl/es-es/productos/android-chrome-play/novedades-en-ces-2024/</link>
<description>En CES, compartimos nuevas formas en que los dispositivos del ecosistema Android funcionan mejor juntos, independientemente de la plataforma y la marca.</description>
<pubDate>Tue, 09 Jan 2024 17:00:00 +0000</pubDate>
<guid>https://blog.google/intl/es-es/productos/android-chrome-play/novedades-en-ces-2024/</guid>
<category>Android</category>
<og xmlns:og="http://ogp.me/ns#">
<type>article</type>
<title>Novedades en CES 2024</title>
<description>En CES, compartimos nuevas formas en que los dispositivos del ecosistema Android funcionan mejor juntos, independientemente de la plataforma y la marca.</description>
<site_name>Google</site_name>
<url>https://blog.google/intl/es-es/productos/android-chrome-play/novedades-en-ces-2024/</url>
</og>
<author xmlns:author="http://www.w3.org/2005/Atom">
<name>Sameer Samat</name>
<title/>
<department/>
<company/>
</author>
</item>
<item>
<title>Blog de Google España</title>
<link>https://blog.google/intl/es-es/about/</link>
<description>Noticias de Google España</description>
<pubDate>Tue, 19 Dec 2023 14:44:00 +0000</pubDate>
<guid>https://blog.google/intl/es-es/about/</guid>
<category>Company announcements</category>
<og xmlns:og="http://ogp.me/ns#">
<type>article</type>
<title>Blog de Google España</title>
<description>Noticias de Google España</description>
<site_name>Google</site_name>
<url>https://blog.google/intl/es-es/about/</url>
</og>
<author xmlns:author="http://www.w3.org/2005/Atom">
<name>Equipo de Google España</name>
<title/>
<department/>
<company/>
</author>
</item>
<item>
<title>El siguiente paso hacia la desaparición de las cookies de terceros en Chrome</title>
<link>https://blog.google/intl/es-es/productos/tecnologia/el-siguiente-paso-hacia-la-desaparicion-de-las-cookies-de-terceros-en-chrome/</link>
<description>Google Chrome trabaja en la desaparición de las cookies de terceros cuando navegas. Conoce cómo funciona la nueva Protección Antirastreo.</description>
<pubDate>Thu, 14 Dec 2023 10:00:00 +0000</pubDate>
<guid>https://blog.google/intl/es-es/productos/tecnologia/el-siguiente-paso-hacia-la-desaparicion-de-las-cookies-de-terceros-en-chrome/</guid>
<category>Safety & Security</category>
<og xmlns:og="http://ogp.me/ns#">
<type>article</type>
<title>El siguiente paso hacia la desaparición de las cookies de terceros en Chrome</title>
<description>Google Chrome trabaja en la desaparición de las cookies de terceros cuando navegas. Conoce cómo funciona la nueva Protección Antirastreo.</description>
<site_name>Google</site_name>
<url>https://blog.google/intl/es-es/productos/tecnologia/el-siguiente-paso-hacia-la-desaparicion-de-las-cookies-de-terceros-en-chrome/</url>
</og>
<author xmlns:author="http://www.w3.org/2005/Atom">
<name>Equipo Google España</name>
<title/>
<department/>
<company/>
</author>
</item>
<item>
<title>Conoce más sobre la lengua española de la mano de Google Arts & Culture</title>
<link>https://blog.google/intl/es-es/noticias-compania/iniciativas/conoce-mas-sobre-la-lengua-espanola-de-la-mano-de-google-arts-culture/</link>
<description>"El español: más allá de las palabras" conecta a hispanohablantes de todo el mundo. Conoce más sobre la iniciativa de Google Arts & Culture.</description>
<pubDate>Thu, 14 Dec 2023 10:00:00 +0000</pubDate>
<guid>https://blog.google/intl/es-es/noticias-compania/iniciativas/conoce-mas-sobre-la-lengua-espanola-de-la-mano-de-google-arts-culture/</guid>
<category>Arts & Culture</category>
<og xmlns:og="http://ogp.me/ns#">
<type>article</type>
<title>Conoce más sobre la lengua española de la mano de Google Arts & Culture</title>
<description>


"El español: más allá de las palabras" conecta a hispanohablantes de todo el mundo. Conoce más sobre la iniciativa de Google Arts & Culture.</description>
<site_name>Google</site_name>


<url>https://blog.google/intl/es-es/noticias-compania/iniciativas/conoce-mas-sobre-la-lengua-espanola-de-la-mano-de-google-arts-culture/</url>
</og>


<author xmlns:author="http://www.w3.org/2005/Atom">
<name>Gregorio Martínez</name>
<title>Director de la Fundación Nebrija y Amit Sood, Director de Google Arts & Culture.</title>
<department/>

<company/>
</author>

</item>
<item>

<title>Mejoramos el uso del español en la Búsqueda y Gboard, gracias a la Real Academia Española</title>
<link>https://blog.google/intl/es-es/productos/informacion/mejoramos-el-uso-del-espanol-en-la-busqueda-y-gboard-gracias-a-la-real-academia-espanola/</link>

<pubDate>Wed, 13 Dec 2023 10:00:00 +0000</pubDate>
<guid>https://blog.google/intl/es-es/productos/informacion/mejoramos-el-uso-del-espanol-en-la-busqueda-y-gboard-gracias-a-la-real-academia-espanola/</guid>

<og xmlns:og="http://ogp.me/ns#">
<type>article</type>

<description>Descubre el proyecto que la Real Academia Española 
 a cabo con IA y cómo impacta en el uso de Gboard y en las búsquedas de Google.</description>

<site_name>Google</site_name>
<url>https://blog.google/intl/es-es/productos/informacion/mejoramos-el-uso-del-espanol-en-la-busqueda-y-gboard-gracias-a-la-real-academia-espanola/</url>
</og>

<author xmlns:author="http://www.w3.org/2005/Atom">
<name>Edgar Camelo</name>
<title>Responsable de Alianzas Estratégicas de Google España</title>
<department/>
<company/>

</author>
</item>
<title>Director de la Fundación Nebrija y Amit Sood, Director de Google Arts & Culture.</title>
<department/>

<company/>
</author>

</item>
<item>

<title>Mejoramos el uso del español en la Búsqueda y Gboard, gracias a la Real Academia Española</title>
<link>https://blog.google/intl/es-es/productos/informacion/mejoramos-el-uso-del-espanol-en-la-busqueda-y-gboard-gracias-a-la-real-academia-espanola/</link>

<pubDate>Wed, 13 Dec 2023 10:00:00 +0000</
<title>Director de la Fundación Nebrija y Amit Sood, Director de Google Arts & Culture.</title>
<department/>

<company/>
</author>

</item>
<item>

<title>Mejoramos el uso del español en la Búsqueda y Gboard, gracias a la Real Academia Española</title>
<link>https://blog.google/intl/es-es/productos/informacion/mejoramos-el-uso-del-espanol-en-la-busqueda-y-gboard-gracias-a-la-real-academia-espanola/</link>

<pubDate>Wed, 13 Dec 2023 10:00:00 +0000</pubDate>
<guid>https://blog.google/intl/es-es/productos/informacion/mejoramos-el-uso-del-espanol-en-la-busqueda-y-gboard-gracias-a-la-real-academia-espanola/</guid>

<og xmlns:og="http://ogp.me/ns#">
<type>article</type>

<description>Descubre el proyecto que la Real Academia Española 
 a cabo con IA y cómo impacta en el uso de Gboard y en las búsquedas de Google.</description>

<site_name>Google</site_name>
<url>https://blog.google/intl/es-es/productos/informacion/mejoramos-el-uso-del-espanol-en-la-busqueda-y-gboard-gracias-a-la-real-academia-espanola/</url>
</og>

<author xmlns:author="http://www.w3.org/2005/Atom">
<name>Edgar Camelo</name>
<title>Responsable de Alianzas Estratégicas de Google España</title>
<department/>
<company/>

</author>
</item>>
<guid>https://blog.google/intl/es-es/productos/informacion/mejoramos-el-uso-del-espanol-en-la-busqueda-y-gboard-gracias-a-la-real-academia-espanola/</guid>

<og xmlns:og="http://ogp.me/ns#">
<type>article</type>

<description>Descubre el proyecto que la Real Academia Española 
 a cabo con IA y cómo impacta en el uso de Gboard y en las búsquedas de Google.</description>

<site_name>Google</site_name>
<url>https://blog.google/intl/es-es/productos/informacion/mejoramos-el-uso-del-espanol-en-la-busqueda-y-gboard-gracias-a-la-real-academia-espanola/</url>
</og>

<author xmlns:author="http://www.w3.org/2005/Atom">
<name>Edgar Camelo</name>
<title>Responsable de Alianzas Estratégicas de Google España</title>
<department/>
<company/>

</author>
</item>


                    customGA4Dimensions["post_id"] = "45558193";


                    customGA4Dimensions["tags"] = "|visual-studio-code|";


                var ga4Settings = {
                    tracker: gtag,
                    trackingCodes: [
                        'G-WCZ03SZFCQ'
                    ],
                    consentsToPerformanceCookies: "denied",
                    consentsToTargetingCookies: "denied",
                    eventParameters: customGA4Dimensions,
                    checkForAdBlock: true,
                    sendTitles: true,
                    trackClicks: false,
            <meta itemprop="name" content="AG"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="AI"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="AL"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="AM"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="AO"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="AQ"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="AR"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="AS"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="AT"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="AU"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="AW"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="AX"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="AZ"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="BA"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="BB"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="BD"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="BE"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="BF"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="BG"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="BH"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="BI"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="BJ"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="BL"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="BM"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="BN"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="BO"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="BQ"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="BR"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="BS"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="BT"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="BV"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="BW"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="BY"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="BZ"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="CA"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="CC"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="CD"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="CF"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="CG"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="CH"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="CI"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="CK"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="CL"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="CM"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="CN"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="CO"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="CR"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="CU"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="CV"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="CW"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="CX"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="CY"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="CZ"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="DE"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="DJ"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="DK"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="DM"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="DO"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="DZ"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="EC"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="EE"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="EG"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="EH"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="ER"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="ES"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="ET"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="FI"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="FJ"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="FK"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="FM"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="FO"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="FR"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="GA"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="GB"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="GD"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="GE"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="GF"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="GG"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="GH"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="GI"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="GL"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="GM"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="GN"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="GP"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="GQ"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="GR"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="GS"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="GT"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="GU"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="GW"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="GY"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="HK"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="HM"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="HN"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="HR"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="HT"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="HU"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="ID"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="IE"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="IL"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="IM"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="IN"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="IO"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="IQ"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="IR"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="IS"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="IT"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="JE"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="JM"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="JO"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="JP"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="KE"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="KG"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="KH"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="KI"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="KM"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="KN"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="KP"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="KR"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="KW"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="KY"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="KZ"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="LA"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="LB"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="LC"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="LI"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="LK"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="LR"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="LS"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="LT"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="LU"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="LV"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="LY"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="MA"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="MC"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="MD"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="ME"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="MF"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="MG"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="MH"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="MK"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="ML"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="MM"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="MN"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="MO"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="MP"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="MQ"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="MR"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="MS"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="MT"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="MU"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="MV"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="MW"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="MX"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="MY"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="MZ"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="NA"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="NC"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="NE"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="NF"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="NG"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="NI"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="NL"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="NO"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="NP"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="NR"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="NU"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="NZ"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="OM"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="PA"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="PE"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="PF"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="PG"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="PH"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="PK"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="PL"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="PM"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="PN"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="PR"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="PS"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="PT"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="PW"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="PY"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="QA"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="RE"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="RO"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="RS"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="RU"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="RW"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="SA"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="SB"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="SC"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="SD"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="SE"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="SG"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="SH"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="SI"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="SJ"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="SK"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="SL"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="SM"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="SN"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="SO"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="SR"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="SS"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="ST"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="SV"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="SX"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="SY"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="SZ"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="TC"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="TD"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="TF"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="TG"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="TH"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="TJ"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="TK"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="TL"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="TM"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="TN"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="TO"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="TR"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="TT"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="TV"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="TW"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="TZ"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="UA"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="UG"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="UM"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="US"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="UY"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="UZ"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="VA"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="VC"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="VE"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="VG"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="VI"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="VN"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="VU"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="WF"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="WS"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="YE"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="YT"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="ZA"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="ZM"/>
        </div>
        <div itemprop="eligibleRegion" itemscope itemtype="http://schema.org/Country">
            <meta itemprop="name" content="ZW"/>
        </div>
    </div>
</div>

  </header>
  <p itemprop="description"></p>
    <section>
      <h2>Kelow LaTesha’s tracks</h2>
        <article class="audible" itemscope itemtype="http://schema.org/MusicRecording" itemprop="track">
          <h2 itemprop="name"><a itemprop="url" href="/kelow/nothin-new-1">Nothin New</a>
by <a href="/kelow">Kelow LaTesha</a></h2>
published on <time pubdate>2024-02-14T05:22:38Z</time>
          <meta itemprop="duration" content="PT00H01M59S" />
        </article>
    </section>
  <footer>
     <ul>
  <li><a href="/kelow/likes">Kelow LaTesha's likes</a></li>
  <li><a href="/kelow/sets">Kelow LaTesha's playlists</a></li>
  <li><a href="/kelow/tracks">Kelow LaTesha's tracks</a></li>
  <li><a rel="nofollow" href="/kelow/comments">Kelow LaTesha's comments</a></li>
</ul>
  </footer>
</article>
</noscript>
<style>#updateBrowserMessage{width:600px;margin:0 auto;position:relative;padding-top:410px;background:url(https://a-v2.sndcdn.com/assets/images/errors/browser-9cdd4e6df7.png) no-repeat 50% 130px;text-align:center;display:none}#updateBrowserMessage .messageText{line-height:26px;font-size:20px;margin-bottom:5px}#updateBrowserMessage .downloadLinks{margin-top:0}</style>
<div id="updateBrowserMessage">
<p class="messageText sc-text-light sc-text-secondary">
Your current browser isn't compatible with SoundCloud. <br>
Please download one of our supported browsers.
<a href="https://help.soundcloud.com/hc/articles/115003564308-Technical-requirements">Need help?</a>
</p>
<div class="downloadLinks sc-type-h3 sc-text-h3 sc-text-light sc-text-secondary">
<a href="http://google.com/chrome" target="_blank" title="Chrome">Chrome</a>
| <a href="http://firefox.com" target="_blank" title="Firefox">Firefox</a> |
<a href="http://apple.com/safari" target="_blank" title="Safari">Safari</a>
|
<a href="https://www.microsoft.com/edge" target="_blank" title="Edge">Edge</a>
</div>
</div>
<script>window.__sc_abortApp&&(window.document.getElementById("updateBrowserMessage").style.display="block")</script>
<div id="error__timeout" class="errorPage__inner sc-hidden">
<p class="errorTitle sc-type-h1 sc-text-h1">Sorry! Something went wrong</p>
<div class="errorText sc-font-light">
<p>Is your network connection unstable or browser outdated?</p>
</div>
<div class="errorButtons">
<a class="sc-button" href="https://help.soundcloud.com" target="_blank" id="try-again">I need help</a>
</div>
</div>
<script>function displayError(){if(!window.__sc_abortApp){var r=window.document,e=r.getElementById("error__timeout"),o=r.getElementById("header__loading");e&&o&&(e.className="errorPage__inner",o.className="sc-hidden")}}window.setTimeout(displayError,15e3),window.onerror=displayError</script>
<p>
<a href="/popular/searches" title="Popular searches">Popular searches</a>
</p>
</div>
<script crossorigin src="https://a-v2.sndcdn.com/assets/54-ed770211.js"></script>
<script crossorigin src="https://a-v2.sndcdn.com/assets/52-d72425c2.js"></script>
<script
  async
  src="https://cdn.cookielaw.org/consent/7e62c772-c97a-4d95-8d0a-f99bbeadcf61/otSDKStub.js"
  type="text/javascript"
  charset="UTF-8"
  data-domain-script="7e62c772-c97a-4d95-8d0a-f99bbeadcf61"
></script>
<script type="text/javascript">
  (function (global) {
    function OptanonWrapper() {
      var activeGroups = (global.OptanonActiveGroups || '').split(',');

      if (Array.isArray(OptanonWrapper.callbacks)) {
        for (var i = 0, max = OptanonWrapper.callbacks.length; i < max; i++) {
          try {
            OptanonWrapper.callbacks[i](activeGroups);
          } catch (e) {}
        }
      }

      OptanonWrapper.isLoaded = true;
    };

    OptanonWrapper.callbacks = [];
    OptanonWrapper.isLoaded = false;

    global.OptanonWrapper = OptanonWrapper;
  }(window));
</script>

<script>window.__sc_version="1708424140"</script>
<script>window.__sc_hydration = [{"hydratable":"anonymousId","data":"841130-265482-728628-856024"},{"hydratable":"features","data":{"features":["v2_dsa_report_content_links","mobi_webauth_oauth_mode","cd_repost_to_artists","v2_tracking_moengage_integration","v2_use_onetrust_tcfv2_us_ca","mobi_enable_onetrust_tcfv2","mobi_tracking_send_session_id","mobi_use_onetrust_eu1","mobi_use_onetrust_gb","mobi_use_onetrust_tcfv2_us_ca","mobi_dsa_report_content_form","v2_use_onetrust_user_id_eu2","v2_enable_sourcepoint_tcfv2","mobi_peace_ukraine_logo_takeover","mobi_use_auth_internal_analytics","v2_peace_ukraine_logo_takeover","mobi_use_onetrust_tcfv2_eu2","v2_test_feature_toggle","checkout_send_segment_events_to_event_gateway","mobi_use_onetrust_user_id_eu1","trolley","v2_nigeria_creator_banner","mobi_use_onetrust_user_id_ex_us","mobi_use_onetrust_tcfv2_eu1","v2_post_with_caption","mobi_use_dwt","v2_use_onetrust_tcfv2_eu1","mobi_use_onetrust_eu4","featured_artists_banner","v2_repost_redirect_page","v2_use_onetrust_gb","v2_dsa_ad_compliance","use_onetrust_async","mobi_dsa_report_content_links","v2_signals_collection","v2_track_level_distro_to_plan_picker","v2_direct_support_link","v2_api_auth_sign_out","v2_ie11_support_end","checkout_use_new_connect","mobi_dsa_ad_compliance","v2_hq_file_storage_release","creator_plan_names_repositioning","v2_use_onetrust_eu4","v2_stories_onboarding","mobi_use_onetrust_user_id_eu2","mobi_tracking_moengage_integration","mobi_enable_2fa_config","v2_use_dwt","v2_use_updated_alert_banner_quota_upsell","v2_enable_onetrust","v2_import_playlist_experiment","v2_disable_sidebar_comments_count","v2_subhub_churn_intercept","checkout_use_new_plan_picker","v2_signage_on_home","v2_use_onetrust_eu2","next_pro_first_fans","fpi_messaging_drawer","v2_use_onetrust_us","v2_comment_sorting","checkout_use_recurly_with_paypal","v2_use_onetrust_tcfv2_ex_us","v2_show_for_artists_link","mobi_use_onetrust_eu3","mobi_use_onetrust_elsewhere","v2_use_onetrust_eu3","mobi_use_onetrust_us","v2_oscp_german_tax_fields_support","v2_fallback_queue_for_search","v2_use_onetrust_user_id_ex_us","v2_use_new_connect","v2_use_onetrust_tcfv2_eu2","v2_next_pro_brazil_banner","v2_send_segment_events_to_event_gateway","v2_use_onetrust_eu1","v2_enable_sourcepoint","v2_repost_with_caption_graphql","mobi_use_onetrust_tcfv2_ex_us","v2_tags_recent_tracks","mobi_use_onetrust_eu2","v2_enable_new_web_errors","v2_use_onetrust_elsewhere","checkout_use_dwt","v2_webauth_use_local_tracking","mobi_sign_in_experiment","mobi_enable_onetrust","v2_can_see_insights","fpi_20_fans_rollout","mobi_trinity","v2_webauth_oauth_mode","v2_google_one_tap","v2_enable_pwa","mobi_use_hls_hack","v2_stories","v2_next_pro_referral_banner","v2_use_onetrust_user_id_eu1","v2_use_onetrust_user_id_global","use_recurly_checkout","v2_show_side_by_side_upsell_experience","v2_enable_onetrust_tcfv2","v2_enable_tcfv2_consent_string_cache","v2_track_manager_redirection","mobi_send_segment_events_to_event_gateway","use_on_soundcloud_short_links"]}},{"hydratable":"experiments","data":{}},{"hydratable":"geoip","data":{"country_code":"MX","country_name":"Mexico","region":"MIC","latitude":19.7009,"longitude":-101.1209}},{"hydratable":"privacySettings","data":{"allows_messages_from_unfollowed_users":false,"analytics_opt_in":true,"communications_opt_in":true,"targeted_advertising_opt_in":false,"legislation":[]}},{"hydratable":"trackingBrowserTabId","data":"010445"},{"hydratable":"user","data":{"avatar_url":"https://i1.sndcdn.com/avatars-S5SSZRQ0TRVjnPbJ-p0umgQ-large.jpg","city":"PG County, Maryland","comments_count":0,"country_code":"US","created_at":"2011-01-11T03:48:35Z","creator_subscriptions":[{"product":{"id":"free"}}],"creator_subscription":{"product":{"id":"free"}},"description":"","followers_count":21989,"followings_count":23,"first_name":"Kelow LaTesha","full_name":"Kelow LaTesha","groups_count":0,"id":2686999,"kind":"user","last_modified":"2024-02-14T05:44:34Z","last_name":"","likes_count":660,"playlist_likes_count":34,"permalink":"kelow","permalink_url":"https://soundcloud.com/kelow","playlist_count":5,"reposts_count":null,"track_count":30,"uri":"https://api.soundcloud.com/users/2686999","urn":"soundcloud:users:2686999","username":"Kelow LaTesha","verified":true,"visuals":{"urn":"soundcloud:users:2686999","enabled":true,"visuals":[{"urn":"soundcloud:visuals:210886533","entry_time":0,"visual_url":"https://i1.sndcdn.com/visuals-000002686999-s9xaxm-original.jpg","link":"https://kelowlatesha.ffm.to/titanic"}],"tracking":null},"badges":{"pro":false,"pro_unlimited":false,"verified":true},"station_urn":"soundcloud:system-playlists:artist-stations:2686999","station_permalink":"artist-stations:2686999","url":"/kelow"}}];</script>

<script src="https://a-v2.sndcdn.com/assets/locales/locale-es-835daa2fe74b91eff1493e2597aa7e1ae1b7303b73ca10cf5c950354a44703a6.js" crossorigin></script>

<script src="https://a-v2.sndcdn.com/assets/44-ef108fcd.js" crossorigin></script>


<script crossorigin src="https://a-v2.sndcdn.com/assets/53-250f3527.js"></script>
<script crossorigin src="https://a-v2.sndcdn.com/assets/0-9a69db71.js"></script>
<script crossorigin src="https://a-v2.sndcdn.com/assets/2-9f8eb923.js"></script>
<script crossorigin src="https://a-v2.sndcdn.com/assets/1-0884a8a3.js"></script>
<script crossorigin src="https://a-v2.sndcdn.com/assets/51-e098e879.js"></script>
<script crossorigin src="https://a-v2.sndcdn.com/assets/50-f8de741b.js"></script>
</body>
</html>
