package models

object ModelConfigObj {
  var treshold: Double = 0.00
  var enableAutoUpdateModel: Boolean = false
  var intervalAutoUpdate: Int = 24
}

case class ModelConfig(
  treshold: Double = 0.00,
  enableAutoUpdateModel: Boolean = false,
  intervalAutoUpdate: Int = 24
)