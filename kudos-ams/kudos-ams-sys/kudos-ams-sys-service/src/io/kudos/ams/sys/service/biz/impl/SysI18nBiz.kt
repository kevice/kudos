package io.kudos.ams.sys.service.biz.impl

import io.kudos.ams.sys.service.biz.ibiz.ISysI18nBiz
import io.kudos.ams.sys.service.model.po.SysI18n
import io.kudos.ams.sys.service.dao.SysI18nDao
import io.kudos.ability.data.rdb.ktorm.biz.BaseCrudBiz
import org.springframework.stereotype.Service


/**
 * 国际化业务
 *
 * @author K
 * @since 1.0.0
 */
@Service
//region your codes 1
open class SysI18nBiz : BaseCrudBiz<String, SysI18n, SysI18nDao>(), ISysI18nBiz {
//endregion your codes 1

    //region your codes 2

    //endregion your codes 2

}