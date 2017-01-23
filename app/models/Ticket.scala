package models

import java.time.OffsetDateTime

case class Ticket(
  id: Option[Long],
  reporter: Option[String],
  assignee: Option[String],
  assigneeName: Option[String],
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  closedAt: Option[OffsetDateTime] = None,
  status: Option[String],
  priority: Option[String],
  title: Option[String],
  description: Option[String],
  resolution: Option[String]
)