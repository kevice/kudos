package io.kudos.base.bean.validation.support

import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Validator
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl

/**
 * Bean校验的上下文
 *
 * @author K
 * @since 1.0.0
 */
object ValidationContext {

    /** 用于传递Bean给ConstraintValidator，因为hibernate validation的ConstraintValidatorContext取不到Bean */
    private val beanMap = mutableMapOf<Int, Any>() // Map<ConstraintDescriptor对象的hashcode，Bean对象>

    /** 是否快速失败模式 */
    private val failFastThreadLocal = InheritableThreadLocal<Boolean>()

    /** 验证器 */
    var validator: Validator? = null

    /**
     * 存放ConstraintDescriptor对象hashcode关联的Bean
     *
     * @param validator 验证器
     * @param bean 待校验的Bean
     * @author K
     * @since 1.0.0
     */
    fun set(validator: Validator, bean: Any) {
        val beanDescriptor = validator.getConstraintsForClass(bean.javaClass)
        beanDescriptor.constrainedProperties.forEach {
            it.constraintDescriptors.forEach { des ->
                val annoClassName = des.annotation.annotationClass.qualifiedName
                if (!annoClassName!!.startsWith("jakarta.validation") && !annoClassName.startsWith("org.hibernate")) {
                    beanMap[des.hashCode()] = bean
                }
            }
        }
    }

    /**
     * 获取ConstraintDescriptor对象hashcode关联的Bean，并从上下文中移除
     *
     * @param constraintValidatorContext 约束验证器上下文
     * @return 待校验的Bean
     * @author K
     * @since 1.0.0
     */
    fun get(constraintValidatorContext: ConstraintValidatorContext): Any? {
        val descriptor = (constraintValidatorContext as ConstraintValidatorContextImpl).constraintDescriptor
        return beanMap.remove(descriptor.hashCode())
    }

    /**
     * 设置快速失败模式
     *
     * @param failFast true：快速失败模式, false: 非快速失败模式
     * @author K
     * @since 1.0.0
     */
    fun setFailFast(failFast: Boolean) = failFastThreadLocal.set(failFast)

    /**
     * 返回快速失败模式
     *
     * @return true：快速失败模式, false: 非快速失败模式
     * @author K
     * @since 1.0.0
     */
    fun isFailFast(): Boolean = failFastThreadLocal.get()

}
