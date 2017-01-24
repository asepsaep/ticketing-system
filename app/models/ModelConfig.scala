package models

case class ModelConfig(
  treshold: Double = 0.0,
  enableAutoUpdateModel: Boolean = false,
  intervalAutoUpdate: Int = 24
)
