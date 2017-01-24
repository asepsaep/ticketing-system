package forms

import play.api.data.Form
import play.api.data.Forms._

object UpdateTicketStatusForm {

  val form = Form(
    mapping(
      "id" → longNumber,
      "status" → nonEmptyText,
      "resolution" → text
    )(Data.apply)(Data.unapply)
  )

  case class Data(
    id: Long,
    status: String,
    resolution: String
  )

}
