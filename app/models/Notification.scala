package models

import java.time.OffsetDateTime

case class Notification(
  id: Option[Long],
  belongsTo: String,
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  isViewed: Boolean,
  origin: Option[String],
  content: Option[String]
)
