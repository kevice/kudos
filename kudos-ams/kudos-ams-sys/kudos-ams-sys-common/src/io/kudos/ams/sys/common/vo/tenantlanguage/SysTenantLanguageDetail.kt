package io.kudos.ams.sys.common.vo.tenantlanguage

import io.kudos.base.support.result.IdJsonResult
import java.time.LocalDateTime


/**
 * 租户-语言关系查询记录
 *
 * @author K
 * @since 1.0.0
 */
data class SysTenantLanguageDetail (

    /** 主键 */
    override var id: String? = null,

    //region your codes 1

    /** 租户id */
    var tenantId: String? = null,

    /** 语言代码 */
    var languageCode: String? = null,

    /** 创建用户 */
    var createUser: String? = null,

    /** 创建时间 */
    var createTime: LocalDateTime? = null,

    /** 更新用户 */
    var updateUser: String? = null,

    /** 更新时间 */
    var updateTime: LocalDateTime? = null,

    //endregion your codes 1
//region your codes 2
) : IdJsonResult<String>() {
//endregion your codes 2

    //region your codes 3

    constructor() : this(null)

    //endregion your codes 3

}