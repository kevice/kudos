package io.kudos.ams.sys.service.model.table

import io.kudos.ams.sys.service.model.po.SysTenantResource
import org.ktorm.schema.*
import io.kudos.ability.data.rdb.ktorm.support.StringIdTable


/**
 * 租户-资源关系数据库表-实体关联对象
 *
 * @author K
 * @since 1.0.0
 */
//region your codes 1
object SysTenantResources : StringIdTable<SysTenantResource>("sys_tenant_resource") {
//endregion your codes 1

    /** 租户id */
    var tenantId = varchar("tenant_id").bindTo { it.tenantId }

    /** 资源id */
    var resourceId = varchar("resource_id").bindTo { it.resourceId }


    //region your codes 2

    //endregion your codes 2

}