package io.kudos.base.bean.validation.teminal.convert.converter.impl

/**
 * Remote注解约束->终端约束的转换器
 *
 * @author K
 * @since 1.0.0
 */
class RemoteConstraintConvertor(annotation: Annotation) : DefaultConstraintConvertor(annotation) {

    override fun getRule(constraintAnnotation: Annotation): LinkedHashMap<String, Any> {
        val map = super.getRule(constraintAnnotation)
        map.remove("checkClass")
        return map
    }

}