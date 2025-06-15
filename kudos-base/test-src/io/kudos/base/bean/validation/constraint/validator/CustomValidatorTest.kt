package io.kudos.base.bean.validation.constraint.validator

import io.kudos.base.bean.validation.constraint.annotations.Custom
import io.kudos.base.bean.validation.kit.ValidationKit
import io.kudos.base.bean.validation.support.IBeanValidator
import org.junit.jupiter.api.Assertions.assertFalse
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * CustomValidator测试用例
 *
 * @author K
 * @since 1.0.0
 */
internal class CustomValidatorTest {

    @Test
    fun validate() {
        val bean1 = TestRemoteBean("user1", false, null)
        assert(ValidationKit.validateBean(bean1).isEmpty())

        val bean2 = TestRemoteBean("user2", true, null)
        assertFalse(ValidationKit.validateBean(bean2).isEmpty())

        val bean3 = TestRemoteBean(null, true, "address")
        assertEquals(1, ValidationKit.validateBean(bean3, failFast = false).size)
    }

    internal data class TestRemoteBean(

        @get:Custom(checkClass = ExistValidator::class, message = "用户名已存在")
        val username: String?,

        val mockExist: Boolean = false, // 模拟用户名是否存在

        @get:Custom.List(
            Custom(checkClass = Rule1Validator::class, message = "不满足规则1"),
            Custom(checkClass = Rule2Validator::class, message = "不满足规则2")
        )
        val address: String?
    )

    internal class ExistValidator : IBeanValidator<TestRemoteBean> {

        override fun validate(bean: TestRemoteBean): Boolean {
            return !bean.mockExist
        }

    }

    internal class Rule1Validator : IBeanValidator<TestRemoteBean> {

        override fun validate(bean: TestRemoteBean): Boolean {
            return true
        }

    }

    internal class Rule2Validator : IBeanValidator<TestRemoteBean> {

        override fun validate(bean: TestRemoteBean): Boolean {
            return false
        }

    }

}