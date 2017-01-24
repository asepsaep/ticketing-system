package forms

import play.api.data.Form
import play.api.data.Forms._

object DispatchTicketForm {

  val form = Form(
    mapping(
      "id" → longNumber,
      "assignee" → nonEmptyText
    )(Data.apply)(Data.unapply)
  )

  case class Data(
    id: Long,
    assignee: String
  )

}
