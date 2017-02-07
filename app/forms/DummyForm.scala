package forms

import play.api.data.Form
import play.api.data.Forms._

object DummyForm {

  val form = Form(
    mapping(
      "username" → nonEmptyText
    )(Data.apply)(Data.unapply)
  )

  case class Data(
    username: String
  )

}
