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

**NOTE: This is not a forum for requesting new API keys. Please send us a request via our support channels, e.g. the chat bot. Also please subscribe to our [@SoundCloudDev] on X for updates**

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
