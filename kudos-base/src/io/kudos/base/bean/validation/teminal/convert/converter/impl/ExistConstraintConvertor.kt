package io.kudos.base.bean.validation.teminal.convert.converter.impl

import io.kudos.base.bean.validation.constraint.annotations.Exist
import io.kudos.base.bean.validation.constraint.validator.ConstraintsValidator


/**
 * Exist注解约束->终端约束的转换器
 *
 * @author K
 * @since 1.0.0
 */
class ExistConstraintConvertor(annotation: Annotation) : DefaultConstraintConvertor(annotation) {

    override fun getRule(constraintAnnotation: Annotation): LinkedHashMap<String, Any> {
        val map = linkedMapOf<String, Any>()
        constraintAnnotation as Exist
        val annotations = ConstraintsValidator.getAnnotations(constraintAnnotation.value)
        annotations.forEach {
            val constraintName = it.annotationClass.simpleName!!
            val ruleMap = super.getRule(it)
            ruleMap.remove("message") // 对于Exist约束来说，子约束的message无意义
            map[constraintName] = ruleMap
        }
        map["message"] = constraintAnnotation.message
        return map
    }

}