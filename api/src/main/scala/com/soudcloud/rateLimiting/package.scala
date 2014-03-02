package com.soudcloud

import java.net.InetAddress

package object rateLimiting {
  implicit def inetAddressToConsumer(address: InetAddress): Consumer = new Consumer {
    override def identifier: String = if (address != null) address.getHostAddress else throw new IllegalArgumentException("InetAddress cannot be null")
  }
}
