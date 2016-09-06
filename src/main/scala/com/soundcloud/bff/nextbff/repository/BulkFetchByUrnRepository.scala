package com.soundcloud.bff.nextbff.repository

import com.soundcloud.scalakit.Urn

trait BulkFetchByUrnRepository extends BulkFetchByUrnWithCustomParamRepository[Urn] {

  def extractUrnFromParam(param: Urn) = param
}
