package models

import java.time.OffsetDateTime

import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }

case class Account(
  id: Option[Long],
  username: String,
  name: Option[String],
  email: Option[String],
  userType: Option[String],
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  avatarUrl: Option[String],
  providerId: String,
  providerKey: String
) extends Identity {
  def loginInfo = LoginInfo(providerId, providerKey)
  override def toString = {
    s"""
       |Id = $id
       |Username = $username
       |Name = $name
       |Email = $email
       |User Type = $userType
       |Created at = $createdAt
       |Avatar URL = $avatarUrl
       |Provider ID = $providerId
       |Provider Key = $providerKey
     """.stripMargin
  }
}