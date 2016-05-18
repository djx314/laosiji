package indicator.rw.plans

import javax.inject.Singleton

import acyclic.file
import indicator.dao.IndicatorDao
import indicator.models.EconomicProperty
import indicator.rw.base.{MultiQueryInfo, RWComponentWithFuture}
import indicator.utils.{SelectCommon, UbwHelper}
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import modules.DBConfig
import net.scalax.fsn.sjjs._
import net.scalax.ubw.shaper.QueryJsonInfo
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future
import scala.language.postfixOps

/**
 * Created by djx314 on 2015/5/7.
 */
case class 数据源管理Model(
)

@Singleton
class 数据源管理 @javax.inject.Inject() (
  override val dbConfigProvider: DatabaseConfigProvider,
  val ubwHelper: UbwHelper,
  val selectCommon: SelectCommon,
  val indicatorDao: IndicatorDao
) extends RWComponentWithFuture[账号管理Model] with DBConfig {

  import driver.api._
  import ubwHelper._
  import indicatorDao._

  override val key = "数据源管理"
  override val title = "数据源管理"
  override def components = Nil

  override def genDataFuture(data: DataType, 权限信息: 权限信息类): Future[QueryJsonInfo] = {

    val accountQuery = for {
      inSource <- economicSourceDao.tableQuery.in
      source <- economicSourceDao.tableQuery.out
    } yield {
      List(
        source.id.columnOut(Out.order, Out.primaryKey) setTo inSource.id.columnIn(In.primaryKey, In.autoInc) as "ID",
        source.name.columnOut(Out.order, Out.defaultAsc) setTo inSource.name.columnIn(In.primaryKey) as "数据源标识",
        source.sourceName.columnOut(Out.order) setTo inSource.sourceName as "数据源名称",
        source.describe.columnOut(Out.order) setTo inSource.describe as "数据源描述"
      )
    }

    Future successful QueryJsonInfo(accountQuery._1.result, accountQuery._2)

  }

}
