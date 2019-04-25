# asset-uploads

This is a `public-api-strangler` component that preprocesses, rewrites and
forwards any `multipart/form-data` requests.

Specifically this component handles:

  * Consistent propagation of `Auth` headers
  * Track uploads

## Overview

![System overview](doc/overview.svg)
