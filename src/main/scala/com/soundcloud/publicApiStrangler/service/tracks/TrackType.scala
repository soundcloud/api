package com.soundcloud.publicApiStrangler.service.tracks

import proto.soundcloud.tracks.api.{TrackType => ProtoTrackType}

sealed abstract class TrackType(val trackType: String)

object TrackType {
  case object Original extends TrackType("original")
  case object Remix extends TrackType("remix")
  case object Live extends TrackType("live")
  case object Recording extends TrackType("recording")
  case object Spoken extends TrackType("spoken")
  case object Podcast extends TrackType("podcast")
  case object Demo extends TrackType("demo")
  case object InProgress extends TrackType("in progress")
  case object Stem extends TrackType("stem")
  case object Loop extends TrackType("loop")
  case object Sound extends TrackType("sound")
  case object Sample extends TrackType("sample")
  case object DjMix extends TrackType("dj-mix")
  case object Other extends TrackType("other")

  def fromProto(trackType: ProtoTrackType): Option[TrackType] = trackType match {
    case ProtoTrackType.OTHER => Some(Other)
    case ProtoTrackType.ORIGINAL => Some(Original)
    case ProtoTrackType.REMIX => Some(Remix)
    case ProtoTrackType.LIVE => Some(Live)
    case ProtoTrackType.RECORDING => Some(Recording)
    case ProtoTrackType.SPOKEN => Some(Spoken)
    case ProtoTrackType.PODCAST => Some(Podcast)
    case ProtoTrackType.DEMO => Some(Demo)
    case ProtoTrackType.IN_PROGRESS => Some(InProgress)
    case ProtoTrackType.STEM => Some(Stem)
    case ProtoTrackType.LOOP => Some(Loop)
    case ProtoTrackType.SOUND => Some(Sound)
    case ProtoTrackType.SAMPLE => Some(Sample)
    case ProtoTrackType.DJ_MIX => Some(DjMix)
    case ProtoTrackType.UNKNOWN | _ => None
  }

}
