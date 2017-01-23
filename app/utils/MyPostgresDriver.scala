package utils

import com.github.tminglei.slickpg._
import slick.driver.JdbcProfile
import slick.profile.Capability

trait MyPostgresDriver extends ExPostgresDriver
  with PgArraySupport
  with PgEnumSupport
  with PgDate2Support
  with PgHStoreSupport {

  def pgjson = "jsonb"

  override protected def computeCapabilities: Set[Capability] = {
    super.computeCapabilities + JdbcProfile.capabilities.insertOrUpdate
  }

  override val api = MyAPI

  object MyAPI extends API
    with ArrayImplicits
    with DateTimeImplicits
    with HStoreImplicits {

    implicit val strListTypeMapper = new SimpleArrayJdbcType[String]("text").to(_.toList)

  }

}

object MyPostgresDriver extends MyPostgresDriver