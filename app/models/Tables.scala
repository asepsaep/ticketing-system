package models

import java.time.OffsetDateTime

import slick.lifted.ProvenShape
import utils.MyPostgresDriver.api._

class AccountTable(tag: Tag) extends Table[Account](tag, "account") {

  def id = column[Long]("id", O.AutoInc)
  def username = column[String]("username", O.PrimaryKey)
  def name = column[String]("name")
  def email = column[String]("email")
  def userType = column[String]("user_type")
  def createdAt = column[OffsetDateTime]("created_at")
  def avatarUrl = column[String]("avatar_url")
  def providerId = column[String]("provider_id")
  def providerKey = column[String]("provider_key")

  override def * = (
    id.?,
    username,
    name.?,
    email.?,
    userType.?,
    createdAt,
    avatarUrl.?,
    providerId,
    providerKey
  ) <> (Account.tupled, Account.unapply _)

}

class PasswordInfoTable(tag: Tag) extends Table[PasswordInfo](tag, "password_info") {

  def hasher = column[String]("hasher")
  def password = column[String]("password")
  def salt = column[String]("salt")
  def accountUsername = column[String]("account_username")

  override def * = (hasher, password, salt.?, accountUsername) <> ((PasswordInfo.apply _).tupled, PasswordInfo.unapply _)

}

class TicketTable(tag: Tag) extends Table[Ticket](tag, "ticket") {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def reporter = column[String]("reporter")
  def assignee = column[String]("assignee")
  def assigneeName = column[String]("assignee_name")
  def createdAt = column[OffsetDateTime]("created_at")
  def closedAt = column[OffsetDateTime]("closed_at")
  def status = column[String]("status")
  def priority = column[String]("priority")
  def title = column[String]("title")
  def description = column[String]("description")
  def resolution = column[String]("resolution")

  override def * = (
    id.?,
    reporter.?,
    assignee.?,
    assigneeName.?,
    createdAt,
    closedAt.?,
    status.?,
    priority.?,
    title.?,
    description.?,
    resolution.?
  ) <> ((Ticket.apply _).tupled, Ticket.unapply _)

}

class NotificationTable(tag: Tag) extends Table[Notification](tag, "notification") {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def belongsTo = column[String]("belongs_to")
  def createdAt = column[OffsetDateTime]("created_at")
  def isViewed = column[Boolean]("is_viewed")
  def origin = column[String]("origin")
  def content = column[String]("content")

  override def * = (
    id.?,
    belongsTo,
    createdAt,
    isViewed,
    origin.?,
    content.?
  ) <> (Notification.tupled, Notification.unapply _)

}