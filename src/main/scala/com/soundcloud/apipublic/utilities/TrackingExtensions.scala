package com.soundcloud.apipublic.utilities

import com.soundcloud.jvmkit.module.util.Urn

object TrackingExtensions {

  implicit class OptionExtension(opt: Option[String]) {

    def annotate(application: Urn): Option[String] = {
      opt match {
        case Some(value) => Some(value.annotate(application))
        case None => None
      }
    }
  }

  implicit class StringExtension(str: String) {

    def annotate(application: Option[Urn]): String = {
      application match {
        case Some(value) => annotate(value)
        case None => str
      }
    }

    def annotate(application: Urn): String = {
      str + "?utm_medium=api&utm_campaign=social_sharing&utm_source=id_" + application.identifier
    }
  }
}
