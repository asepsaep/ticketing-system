package models.daos

import javax.inject.Inject

import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.driver.JdbcProfile
import slick.lifted.TableQuery
import utils.MyPostgresDriver.api._

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import models._
import play.api.Logger

trait NotificationDAO {
  def find(id: Long): Future[Option[Notification]]
  def findFrom(belongsTo: String): Future[Seq[Notification]]
  def findNewFrom(belongsTo: String): Future[Seq[Notification]]
  def save(notification: Notification): Future[Notification]
  def count: Future[Int]
  def delete(id: Long): Future[Unit]
  def all: Future[Seq[Notification]]
  def allByPage(page: Int, pageSize: Int): Future[Seq[Notification]]
  def allByPageAndUsername(page: Int, pageSize: Int, belongsTo: String): Future[Seq[Notification]]
}

class NotificationDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends NotificationDAO with HasDatabaseConfigProvider[JdbcProfile] {

  private val Notifications = TableQuery[NotificationTable]

  override def find(id: Long): Future[Option[Notification]] = {
    db.run(Notifications.filter(_.id === id).result.headOption)
  }

  override def findFrom(belongsTo: String): Future[Seq[Notification]] = {
    db.run(Notifications.filter(_.belongsTo === belongsTo).sortBy(_.createdAt.desc).result)
  }

  override def findNewFrom(belongsTo: String): Future[Seq[Notification]] = {
    db.run(Notifications.filter(_.belongsTo === belongsTo).filter(notif ⇒ !notif.isViewed).sortBy(_.createdAt.desc).result)
  }

  override def save(notification: Notification): Future[Notification] = {
    notification.id match {
      case None ⇒ db.run(
        for {
          i ← Notifications.returning(Notifications.map(_.id)).into((item, id) ⇒ item.copy(id = Some(id))) += notification
        } yield i
      )
      case Some(id) ⇒ find(id).flatMap {
        case None ⇒ db.run(
          for {
            i ← Notifications.returning(Notifications.map(_.id)).into((item, id) ⇒ item.copy(id = Some(id))) += notification
          } yield i
        )
        case Some(_) ⇒ db.run(
          for {
            u ← Notifications.filter(_.id === id).update(notification)
          } yield notification
        )
      }
    }
  }

  override def count: Future[Int] = {
    db.run(Notifications.length.result)
  }

  override def delete(id: Long): Future[Unit] = {
    db.run(Notifications.filter(_.id === id).delete).map(_ ⇒ {})
  }

  override def all: Future[Seq[Notification]] = {
    db.run(Notifications.sortBy(_.createdAt.desc).result)
  }

  override def allByPage(page: Int, pageSize: Int): Future[Seq[Notification]] = {
    db.run(Notifications.sortBy(_.createdAt.desc).drop(page * pageSize).take(pageSize).result)
  }

  override def allByPageAndUsername(page: Int, pageSize: Int, belongsTo: String): Future[Seq[Notification]] = {
    db.run(Notifications.filter(_.belongsTo === belongsTo).sortBy(_.createdAt.desc).drop(page * pageSize).take(pageSize).result)
  }

}