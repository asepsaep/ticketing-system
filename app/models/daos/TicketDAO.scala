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

trait TicketDAO {
  def find(id: Long): Future[Option[Ticket]]
  def findFrom(reporter: String): Future[Seq[Ticket]]
  def findTo(assignedTo: String): Future[Seq[Ticket]]
  def findByStatus(status: String): Future[Seq[Ticket]]
  def findByPriority(priority: String): Future[Seq[Ticket]]
  def all: Future[Seq[Ticket]]
  def allByPage(page: Int, pageSize: Int): Future[Seq[Ticket]]
  def allByPageAndUsername(page: Int, pageSize: Int, username: String): Future[Seq[Ticket]]
  def save(ticket: Ticket): Future[Ticket]
  def count: Future[Int]
  def delete(id: Long): Future[Unit]
}

class TicketDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends TicketDAO with HasDatabaseConfigProvider[JdbcProfile] {

  private val Tickets = TableQuery[TicketTable]

  private def findBy(criterion: (TicketTable ⇒ slick.lifted.Rep[Boolean])) = db.run(
    for {
      ticket ← Tickets.filter(criterion).result
    } yield ticket
  ).recover {
      case e ⇒ Logger.error(e.getMessage, e); List.empty
    }

  override def find(id: Long): Future[Option[Ticket]] = {
    db.run(Tickets.filter(_.id === id).result.headOption)
  }

  override def findByStatus(status: String): Future[Seq[Ticket]] = {
    db.run(Tickets.filter(_.status === status).result)
  }

  override def findByPriority(priority: String): Future[Seq[Ticket]] = {
    db.run(Tickets.filter(_.priority === priority).result)
  }

  override def count: Future[Int] = {
    db.run(Tickets.length.result)
  }

  override def all: Future[Seq[Ticket]] = {
    db.run(Tickets.sortBy(_.createdAt.desc).result)
  }

  override def allByPage(page: Int, pageSize: Int): Future[Seq[Ticket]] = {
    db.run(Tickets.sortBy(_.createdAt.desc).drop(page * pageSize).take(pageSize).result)
  }

  override def allByPageAndUsername(page: Int, pageSize: Int, username: String): Future[Seq[Ticket]] = {
    db.run(Tickets.filter(_.assignee === username).sortBy(_.createdAt.desc).drop(page * pageSize).take(pageSize).result)
  }

  override def delete(id: Long): Future[Unit] = {
    db.run(Tickets.filter(_.id === id).delete).map(_ ⇒ {})
  }

  override def save(ticket: Ticket): Future[Ticket] = {
    ticket.id match {
      case None ⇒ db.run(
        for {
          i ← Tickets.returning(Tickets.map(_.id)).into((item, id) ⇒ item.copy(id = Some(id))) += ticket
        } yield i
      )
      case Some(id) ⇒ find(id).flatMap {
        case None ⇒ db.run(
          for {
            i ← Tickets.returning(Tickets.map(_.id)).into((item, id) ⇒ item.copy(id = Some(id))) += ticket
          } yield i
        )
        case Some(_) ⇒ db.run(
          for {
            u ← Tickets.filter(_.id === id).update(ticket)
          } yield ticket
        )
      }
    }
  }

  override def findTo(assignee: String): Future[Seq[Ticket]] = {
    db.run(Tickets.filter(_.assignee === assignee).sortBy(_.createdAt.desc).result)
  }

  override def findFrom(reporter: String): Future[Seq[Ticket]] = {
    db.run(Tickets.filter(_.reporter === reporter).sortBy(_.createdAt.desc).result)
  }

}
