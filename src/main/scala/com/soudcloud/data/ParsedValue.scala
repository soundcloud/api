package com.soudcloud.data

import com.soundcloud.bff.authorization.Policies

trait ParsedValue {
  def stringify: String
  def raw: Any

  def value(fieldName: String): Option[String]
  def + (fields: Policies): ParsedValue
  def children: Seq[ParsedValue]
  def withChildren(fields: Seq[ParsedValue]): ParsedValue
  def isArray: Boolean
  def isObject : Boolean

}
