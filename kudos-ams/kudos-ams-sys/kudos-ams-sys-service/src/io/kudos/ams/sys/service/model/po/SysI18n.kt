package io.kudos.ams.sys.service.model.po

import io.kudos.ability.data.rdb.ktorm.support.DbEntityFactory
import io.kudos.ability.data.rdb.ktorm.support.IDbEntity
import java.time.LocalDateTime

/**
 * 国际化数据库实体
 *
 * @author K
 * @since 1.0.0
 */
//region your codes 1
interface SysI18n : IDbEntity<String, SysI18n> {
//endregion your codes 1

    companion object : DbEntityFactory<SysI18n>()

    /** 语言_地区 */
    var locale: String

    /** 语言_地区 */
    var moduleCode: String

    /** 国际化类型字典代码 */
    var i18nTypeDictCode: String

    /** 国际化key */
    var key: String

    /** 国际化值 */
    var value: String

    /** 是否启用 */
    var active: Boolean

    /** 是否内置 */
    var builtIn: Boolean

    /** 创建用户 */
    var createUser: String?

    /** 创建时间 */
    var createTime: LocalDateTime

    /** 更新用户 */
    var updateUser: String?

    /** 更新时间 */
    var updateTime: LocalDateTime?


    //region your codes 2

    //endregion your codes 2

}