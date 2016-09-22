package com.soundcloud.bff.nextbff.repository

import com.soundcloud.jvmkit.Urn

trait BulkFetchByUrnRepository extends BulkFetchByUrnWithCustomParamRepository[Urn] {

  def extractUrnFromParam(param: Urn) = param
}
