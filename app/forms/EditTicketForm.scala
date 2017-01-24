package forms

import play.api.data.Form
import play.api.data.Forms._

object EditTicketForm {

  val form = Form(
    mapping(
      "id" → longNumber,
      "title" → text,
      "description" → text,
      "priority" → text
    )(Data.apply)(Data.unapply)
  )

  case class Data(
    id: Long,
    title: String,
    description: String,
    priority: String
  )

}
