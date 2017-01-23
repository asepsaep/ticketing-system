package models.services

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import models.Account
import models.daos.AccountDAO

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

trait AccountService extends IdentityService[Account] {
  def save(user: Account): Future[Account]
  def retrieveAlongWithEmail(loginInfo: LoginInfo, email: String): Future[Option[Account]]
  def findCredentialsAccount(email: String): Future[Option[Account]]
}

class AccountServiceImpl @Inject() (accountDAO: AccountDAO) extends AccountService {

  override def retrieve(loginInfo: LoginInfo): Future[Option[Account]] = {
    accountDAO.findByLoginInfo(loginInfo)
  }

  override def retrieveAlongWithEmail(loginInfo: LoginInfo, email: String): Future[Option[Account]] = {
    accountDAO.findByLoginInfoAlongWithEmail(loginInfo.providerKey, email)
  }

  override def save(account: Account): Future[Account] = {
    accountDAO.save(account)
  }

  override def findCredentialsAccount(email: String): Future[Option[Account]] = {
    accountDAO.findCredentialsAccount(email)
  }

}
