package io.kudos.ability.log.audit.commobn.annotation

import io.kudos.ability.log.audit.commobn.enums.OperationTypeEnum
import io.kudos.ability.log.audit.commobn.support.DefaultAuditLogDetailDescriptionFormatter
import io.kudos.ability.log.audit.commobn.support.IAuditLogDetailDescriptionFormatter
import io.kudos.base.enums.impl.YesNotEnum
import kotlin.reflect.KClass

/**
 * Create by (admin) on 1/27/15.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class WebAudit(
    /**
     * 操作类型
     */
    val opType: OperationTypeEnum,
    /**
     * 所属子系统
     */
    val subsysCode: String = "",
    /**
     * 日志所属模块
     */
    val moduleCode: String,
    /**
     * 操作描述
     */
    val desc: String = "",
    /**
     * 是否忽略表单数据
     * 是	: request提交表单数据将不被存储
     * 否	: request提交表单数据将被存储
     */
    val ignoreForm: YesNotEnum = YesNotEnum.YES,
    /**
     * 操作类型(扩展)
     */
    val opTypeExt: String = "",
    /**
     * 详情日志处理器
     *
     * @return
     */
    val descriptionFormatter: KClass<out IAuditLogDetailDescriptionFormatter> = DefaultAuditLogDetailDescriptionFormatter::class
)
