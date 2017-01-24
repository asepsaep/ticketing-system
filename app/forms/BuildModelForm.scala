package forms

import play.api.data.Form
import play.api.data.Forms._

object BuildModelForm {

  val form = Form(
    mapping(
      "treshold" → text,
      "enableAutoUpdate" → boolean,
      "intervalAutoUpdate" → number
    )(Data.apply)(Data.unapply)
  )

  case class Data(
    treshold: String,
    enableAutoUpdate: Boolean,
    intervalAutoUpdate: Int
  )

}
