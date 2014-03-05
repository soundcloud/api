package com.soudcloud

package object rateLimiting {
  implicit def ipToConsumer(ip: Ip): Consumer = new Consumer {
    override def identifier: String = ip.address
  }
}
