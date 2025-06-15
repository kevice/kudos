package io.kudos.base.bean.validation.teminal.convert.converter.impl

import io.kudos.base.bean.validation.constraint.annotations.Compare
import io.kudos.base.lang.reflect.getMemberProperty
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType


/**
 * Compare注解约束->终端约束的转换器
 *
 * @author K
 * @since 1.0.0
 */
class CompareConstraintConvertor(annotation: Annotation) : DefaultConstraintConvertor(annotation) {

    override fun getRule(constraintAnnotation: Annotation): LinkedHashMap<String, Any> {
        val map = super.getRule(constraintAnnotation)
        val beanClass = context.beanClass
        val returnType = beanClass.getMemberProperty(context.property).returnType
        if (returnType.isSubtypeOf(Number::class.starProjectedType)) {
            map["isNumber"] = "true"
        }
        constraintAnnotation as Compare
        val depends = constraintAnnotation.depends
        if (depends.properties.isNotEmpty()) {
            map["depends"] = super.getRule(depends)
        } else {
            map.remove("depends")
        }
        return map
    }

}
