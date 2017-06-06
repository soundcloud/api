package com.soundcloud.publicApiStrangler.mapper.spotlight

import com.soundcloud.publicApiStrangler.client.mothership.response.representation.Self


case class SpotlightItem(self: Self,
                         user: Self,
                         public: Boolean,
                         title: String,
                         lastModified: String)
