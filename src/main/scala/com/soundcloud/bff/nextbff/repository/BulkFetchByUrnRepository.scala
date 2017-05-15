package com.soundcloud.bff.nextbff.repository

import com.soundcloud.jvmkit.module.util.Urn

trait BulkFetchByUrnRepository extends BulkFetchByUrnWithCustomParamRepository[Urn] {

  def extractUrnFromParam(param: Urn) = param
}
