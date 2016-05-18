package indicator.rw.plans

import javax.inject.Singleton

import acyclic.file
import indicator.dao.IndicatorDao
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
case class 账号管理Model(
  /*streetMultiply: List[Long],
  tradeMultiply: List[Long],
  yearSingle: Option[Int],
  monthMultiply: List[Int]*/
)

@Singleton
class 账号管理 @javax.inject.Inject() (
  override val dbConfigProvider: DatabaseConfigProvider,
  val ubwHelper: UbwHelper,
  val selectCommon: SelectCommon,
  val indicatorDao: IndicatorDao
) extends RWComponentWithFuture[账号管理Model] with DBConfig {

  import driver.api._
  import ubwHelper._
  import indicatorDao._

  override val key = "账号管理"
  override val title = "账号管理"
  override def components = Nil

  override def genDataFuture(data: DataType, 权限信息: 权限信息类): Future[QueryJsonInfo] = {

    val accountQuery = for {
      inAccount <- securityDao.accountDao.tableQuery.in
      account <- securityDao.accountDao.tableQuery.out
    } yield {
      List(
        account.id.columnOut(Out.order, Out.notInRetrieve, Out.primaryKey) setTo inAccount.id.columnIn(In.primaryKey, In.autoInc) as "ID",
        account.name.columnOut(Out.order, Out.defaultAsc, Out.primaryKey) setTo inAccount.name.columnIn(In.primaryKey) as "账号",
        account.password setTo inAccount.password as "密码",
        account.enabled.columnOut(Out.order) setTo inAccount.enabled as "是否启用"
      )
    }

    Future successful QueryJsonInfo(accountQuery._1.result, accountQuery._2)

  }

  override def genStaticMany(data: DataType, auth: 权限信息类): Future[List[MultiQueryInfo[_]]] = {
    import scala.async.Async.{async, await}

    async {
      val authTypes = await(db.run(securityDao.permissionTypeDao.tableQuery.result))
      val authForeigns = for {
        authType <- authTypes
      } yield {
        MultiQueryInfo(s"${authType.typeName} 用户权限", "ID", "ID", (id: Long) => {
          val iQuery = for {
            accountPermissionIn <- securityDao.accountPermissionDao.tableQuery.in
            permission <- securityDao.securityPermissionDao.tableQuery.out if permission.typeId === authType.id
            accountPermission <- securityDao.accountPermissionDao.tableQuery.out if accountPermission.permission === permission.id
            account <- securityDao.accountDao.tableQuery.out if (account.id === accountPermission.account) && (account.id === id)
          } yield {
            List(
              accountPermission.id setTo accountPermissionIn.id.columnIn(In.primaryKey, In.autoInc) as "ID",
              accountPermission.account setTo accountPermissionIn.account as masterForeign,
              accountPermission.permission setTo accountPermissionIn.permission as slaveryForeign
            )
          }
          QueryJsonInfo(iQuery._1.result, iQuery._2)
        }, {
          import net.scalax.ubw.shaper._
          import poiOperation._
          (for {
            permission <- securityDao.securityPermissionDao.tableQuery.ubw if permission.typeId === authType.id
          } yield {
            List(
              permission.id as "ID" order true,
              permission.name as "权限标识" order true,
              permission.permissionName as "权限名称" order true,
              permission.describe as "权限描述"
            )
          }).result
        })
      }

      val roleForeign = MultiQueryInfo("用户角色", "ID", "ID", (id: Long) => {
        val iQuery = for {
          accountRoleIn <- securityDao.accountRoleDao.tableQuery.in
          accountRole <- securityDao.accountRoleDao.tableQuery.out
          account <- securityDao.accountDao.tableQuery.out if (account.id === accountRole.account) && (account.id === id)
        } yield {
          List(
            accountRole.id setTo accountRoleIn.id.columnIn(In.primaryKey, In.autoInc) as "ID",
            accountRole.account setTo accountRoleIn.account as masterForeign,
            accountRole.role setTo accountRoleIn.role as slaveryForeign
          )
        }
        QueryJsonInfo(iQuery._1.result, iQuery._2)
      }, {
        import net.scalax.ubw.shaper._
        import poiOperation._
        (for {
          role <- securityDao.securityRoleDao.tableQuery.ubw
        } yield {
          List(
            role.id as "ID" order true,
            role.name as "角色标识" order true,
            role.roleName as "角色名称" order true,
            role.describe as "角色描述"
          )
        }).result
      })

      val sourceForeign = MultiQueryInfo("用户所属数据源", "ID", "ID", (id: Long) => {
        val iQuery = for {
          economicSourceAccountIn <- economicSourceAccountDao.tableQuery.in
          economicSourceAccount <- economicSourceAccountDao.tableQuery.out
          account <- securityDao.accountDao.tableQuery.out if (account.id === economicSourceAccount.account) && (account.id === id)
        } yield {
          List(
            economicSourceAccount.id setTo economicSourceAccountIn.id.columnIn(In.autoInc, In.primaryKey) as "ID",
            economicSourceAccount.account setTo economicSourceAccountIn.account as masterForeign,
            economicSourceAccount.source setTo economicSourceAccountIn.source as slaveryForeign
          )
        }
        QueryJsonInfo(iQuery._1.result, iQuery._2)
      }, {
        import net.scalax.ubw.shaper._
        import poiOperation._
        (for {
          source <- economicSourceDao.tableQuery.ubw
        } yield {
          List(
            source.id as "ID" order true,
            source.name as "数据源标识" order true,
            source.sourceName as "数据源名称" order true,
            source.describe as "数据源描述"
          )
        }).result
      })

      sourceForeign :: roleForeign :: authForeigns.toList
    }
  }

}
