package com.livelygig.product.message.impl
import com.lightbend.lagom.scaladsl.playjson.{SerializerRegistry, Serializers}
import com.livelygig.product.message.api.Message

class MessageSerializerRegistry extends SerializerRegistry {
  override def serializers = List(
    Serializers[Message]
  )
}