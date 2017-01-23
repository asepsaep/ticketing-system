package models

import com.mohiva.play.silhouette.api.util.{ PasswordInfo ⇒ SilhouetterPasswordInfo }
import scala.language.implicitConversions

case class PasswordInfo(
  hasher: String,
  password: String,
  salt: Option[String],
  accountUsername: String
)

object PasswordInfo {

  def passwordInfo2db(accountUsername: String, passwordInfo: SilhouetterPasswordInfo) = {
    PasswordInfo(
      hasher = passwordInfo.hasher,
      password = passwordInfo.password,
      salt = passwordInfo.salt,
      accountUsername = accountUsername
    )
  }

  implicit def db2PasswordInfo(passwordInfo: PasswordInfo): SilhouetterPasswordInfo = {
    new SilhouetterPasswordInfo(passwordInfo.hasher, passwordInfo.password, passwordInfo.salt)
  }

  implicit def dbTableElement2PasswordInfo(passwordInfo: PasswordInfoTable#TableElementType): SilhouetterPasswordInfo = {
    new SilhouetterPasswordInfo(passwordInfo.hasher, passwordInfo.password, passwordInfo.salt)
  }

}

