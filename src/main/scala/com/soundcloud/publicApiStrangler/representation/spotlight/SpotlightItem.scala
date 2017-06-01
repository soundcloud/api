package com.soundcloud.publicApiStrangler.representation.spotlight

import com.soundcloud.publicApiStrangler.representation.Self


case class SpotlightItem(self: Self,
                         user: Self,
                         public: Boolean,
                         title: String,
                         lastModified: String)
