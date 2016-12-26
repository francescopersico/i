package com.livelygig.product.message.api

import java.util.UUID

import play.api.libs.json.{Format, Json}

/**
  * Created by shubham.k on 21-12-2016.
  */
case class Message(id: UUID, userId: UUID, content: String)

object Message {
  implicit val format: Format[Message] = Json.format

}