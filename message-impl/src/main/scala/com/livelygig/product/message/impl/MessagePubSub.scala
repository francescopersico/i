package com.livelygig.product.message.impl

import akka.actor.ActorSystem
import com.lightbend.lagom.scaladsl.pubsub.{PubSubComponents, TopicId}
import com.livelygig.product.message.api.Message

/**
  * Created by shubham.k on 29-12-2016.
  */
class MessagePubSub extends PubSubComponents {
  private final val MAX_TOPICS = 1024
  override def actorSystem = ActorSystem("MessageActor")

  def refFor (userId: String) = {
    pubSubRegistry.refFor(TopicId[Message](topicQualifier(userId)))
  }

  def topicQualifier (userId: String) = {
//    val MAX_TOPICS = 1024
    String.valueOf(Math.abs(userId.hashCode()) % MAX_TOPICS)
  }
}
