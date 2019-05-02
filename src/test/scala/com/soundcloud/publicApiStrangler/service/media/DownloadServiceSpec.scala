package com.soundcloud.publicApiStrangler.service.media

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.authorization.policies.{ContentAuthorization, ContentPolicy, ContentRestriction, MonetizationModel, Reason}
import com.soundcloud.publicApiStrangler.client.media.MediaServiceClient
import com.soundcloud.publicApiStrangler.client.tracks.{TracksClient, VisibleTrack}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.util.{Await, Future}
import org.joda.time.DateTime

class DownloadServiceSpec extends UnitSpecification {

  trait Context extends Scope {
    val tracksClient = mock[TracksClient]
    val mediaServiceClient = mock[MediaServiceClient]
    val session = mock[UserSession]
    val service = new DownloadService(tracksClient, mediaServiceClient)

    val trackUrn = Urn("soundcloud", "tracks", "1701")
    val trackOwnerUrn = Urn("soundcloud", "users", "1031")
    val downloaderUrn = Urn("soundcloud", "users", "1764")
    val secretToken = Some("shhhhhhh")
    val trackUid = "the-uid"
    lazy val disabledAt: Option[DateTime] = None

    lazy val maybeTrack: Option[VisibleTrack] = None
    lazy val maybeUrl: Option[String] = None

    tracksClient.visibleTrack(session, trackUrn, secretToken) returns Future.value(maybeTrack)
    mediaServiceClient.fetchDownloadOriginalUrl(session, trackUid) returns Future.value(maybeUrl)

    def stubSessionUser(userUrn: Urn) = session.getUser returns userUrn

    lazy val result = Await.result(service.download(session, trackUrn, secretToken))
  }

  "#download" >> {
    "when track is missing" >> {
      trait MissingTrackContext extends Context {
        override lazy val maybeTrack = None
      }

      "download should be not found" in new MissingTrackContext {
        result ==== DownloadNotFound
      }
    }

    "when track is disabled" >> {
      trait DisabledTrackContext extends Context {
        override lazy val disabledAt = Some(DateTime.now())

        lazy val track = VisibleTrack(
          trackUrn,
          trackOwnerUrn,
          None,
          None,
          true,
          disabledAt,
          new ContentAuthorization(trackUrn, ContentPolicy.ALLOW, Reason.DEFAULT, Set.empty[ContentRestriction], MonetizationModel.NOT_APPLICABLE))

        override lazy val maybeTrack = Some(track)
      }

      "download should be not found" in new DisabledTrackContext {
        result ==== DownloadNotFound
      }
    }

    "when track is available" >> {
      trait AvailableTrackContext extends Context {
        lazy val downloadable: Boolean = true
        lazy val policy: ContentPolicy = ContentPolicy.ALLOW
        lazy val maybeUid: Option[String] = Some(trackUid)

        lazy val track = VisibleTrack(
          trackUrn,
          trackOwnerUrn,
          maybeUid,
          None,
          downloadable,
          disabledAt,
          new ContentAuthorization(trackUrn, policy, Reason.DEFAULT, Set.empty[ContentRestriction], MonetizationModel.NOT_APPLICABLE))

        override lazy val maybeTrack = Some(track)
      }

      "when uid exists" >> {
        "when policy is BLOCK" >> {
          trait BlockPolicyContext extends AvailableTrackContext {
            override lazy val policy = ContentPolicy.BLOCK
            stubSessionUser(downloaderUrn)
          }

          "download should be not found" in new BlockPolicyContext {
            result ==== DownloadNotFound
          }
        }

        "when policy is SNIP" >> {
          trait SnipPolicyContext extends AvailableTrackContext {
            override lazy val policy = ContentPolicy.SNIP
            stubSessionUser(downloaderUrn)
          }

          "download should be not found" in new SnipPolicyContext {
            result ==== DownloadNotFound
          }
        }

        "when policy is ALLOW" >> {
          trait AllowPolicyContext extends AvailableTrackContext {
            override lazy val policy = ContentPolicy.ALLOW
            stubSessionUser(downloaderUrn)
          }

          "when track is not downloadable" >> {
            trait NotDownloadableContext extends AllowPolicyContext {
              override lazy val downloadable = false
              stubSessionUser(downloaderUrn)
            }

            "download should be not found" in new NotDownloadableContext {
              result ==== DownloadNotFound
            }
          }

          "when track is downloadable" >> {
            trait DownloadableContext extends AllowPolicyContext {
              override lazy val downloadable = true
              stubSessionUser(downloaderUrn)
            }

            "when media-service returns a url" >> {
              trait UrlFoundContext extends DownloadableContext {
                override lazy val maybeUrl = Some("https://download-url")
              }

              "download should be found" in new UrlFoundContext {
                result ==== DownloadOk("https://download-url")
              }
            }

            "when media-service does not return a url" >> {
              trait UrlNotFoundContext extends DownloadableContext {
                override lazy val maybeUrl = None
              }

              "download should be not found" in new UrlNotFoundContext {
                result ==== DownloadNotFound
              }
            }
          }

          "when the track is not downloadable but the downloader is the track's owner" >> {
            trait TrackOwnerContext extends AllowPolicyContext {
              override lazy val downloadable = false
              override lazy val maybeUrl = Some("https://download-url")
              stubSessionUser(trackOwnerUrn)
            }

            "download should be found" in new TrackOwnerContext {
              result ==== DownloadOk("https://download-url")
            }
          }
        }

        "when policy is MONETIZE" >> {
          trait MonetizePolicyContext extends AvailableTrackContext {
            override lazy val policy = ContentPolicy.MONETIZE
            override lazy val downloadable = true
            override lazy val maybeUrl = Some("https://download-url")
            stubSessionUser(downloaderUrn)
          }

          "download should be found" in new MonetizePolicyContext {
            result ==== DownloadOk("https://download-url")
          }
        }
      }

      "when uid does not exist" >> {
        trait NoUidContext extends AvailableTrackContext {
          override lazy val maybeUid = None
          stubSessionUser(downloaderUrn)
        }

        "download should be not found" in new NoUidContext {
          result ==== DownloadNotFound
        }
      }
    }

    "when track is not available" >> {
      trait UnavailableTrackContext extends Context {
        override lazy val maybeTrack = None
        stubSessionUser(downloaderUrn)
      }

      "download should be not found" in new UnavailableTrackContext {
        result ==== DownloadNotFound
      }
    }
  }
}
