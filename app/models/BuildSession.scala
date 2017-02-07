package models

import java.time.OffsetDateTime

case class BuildSession(
  id: Option[Long],
  caller: String,
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  log: String = ""
)
