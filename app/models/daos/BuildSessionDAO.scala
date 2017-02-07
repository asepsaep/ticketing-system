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

trait BuildSessionDAO {
  def find(id: Long): Future[Option[BuildSession]]
  def findFrom(caller: String): Future[Seq[BuildSession]]
  def save(buildSession: BuildSession): Future[BuildSession]
  def count: Future[Int]
  def delete(id: Long): Future[Unit]
  def all: Future[Seq[BuildSession]]
  def allByPage(page: Int, pageSize: Int): Future[Seq[BuildSession]]
  def allByPageAndUsername(page: Int, pageSize: Int, caller: String): Future[Seq[BuildSession]]
}

class BuildSessionDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends BuildSessionDAO with HasDatabaseConfigProvider[JdbcProfile] {

  private val BuildSessions = TableQuery[BuildSessionTable]

  override def find(id: Long): Future[Option[BuildSession]] = {
    db.run(BuildSessions.filter(_.id === id).result.headOption)
  }

  override def findFrom(caller: String): Future[Seq[BuildSession]] = {
    db.run(BuildSessions.filter(_.caller === caller).sortBy(_.time.desc).result)
  }

  override def save(buildSession: BuildSession): Future[BuildSession] = {
    buildSession.id match {
      case None ⇒ db.run(
        for {
          i ← BuildSessions.returning(BuildSessions.map(_.id)).into((item, id) ⇒ item.copy(id = Some(id))) += buildSession
        } yield i
      )
      case Some(id) ⇒ find(id).flatMap {
        case None ⇒ db.run(
          for {
            i ← BuildSessions.returning(BuildSessions.map(_.id)).into((item, id) ⇒ item.copy(id = Some(id))) += buildSession
          } yield i
        )
        case Some(_) ⇒ db.run(
          for {
            u ← BuildSessions.filter(_.id === id).update(buildSession)
          } yield buildSession
        )
      }
    }
  }

  override def count: Future[Int] = {
    db.run(BuildSessions.length.result)
  }

  override def delete(id: Long): Future[Unit] = {
    db.run(BuildSessions.filter(_.id === id).delete).map(_ ⇒ {})
  }

  override def all: Future[Seq[BuildSession]] = {
    db.run(BuildSessions.sortBy(_.time.desc).result)
  }

  override def allByPage(page: Int, pageSize: Int): Future[Seq[BuildSession]] = {
    db.run(BuildSessions.sortBy(_.time.desc).drop(page * pageSize).take(pageSize).result)
  }

  override def allByPageAndUsername(page: Int, pageSize: Int, caller: String): Future[Seq[BuildSession]] = {
    db.run(BuildSessions.filter(_.caller === caller).sortBy(_.time.desc).drop(page * pageSize).take(pageSize).result)
  }

}
