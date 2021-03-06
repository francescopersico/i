package com.livelygig.product.keeper.impl

import com.lightbend.lagom.scaladsl.playjson.Jsonable
import com.livelygig.product.keeper.api.models.UserAuth
import play.api.libs.json.{Format, Json}
import com.livelygig.product.utils.JsonFormats._
/**
  * Created by shubham.k on 10-01-2017.
  */
case class KeeperState(state: Option[UserAuth], userStatus:UserStatus.Status) extends Jsonable {
  def withStatus (status: UserStatus.Status) = copy(userStatus = status)
}

object KeeperState {
  implicit val format: Format[KeeperState] = Json.format
  val initialState = KeeperState(None, UserStatus.DoesNotExist)

}

object UserStatus extends Enumeration {
  type Status = Value
  val Activated, NotActivated, Deleted, Disabled, DoesNotExist = Value
  implicit val format:Format[Status] = enumFormat(UserStatus)
}