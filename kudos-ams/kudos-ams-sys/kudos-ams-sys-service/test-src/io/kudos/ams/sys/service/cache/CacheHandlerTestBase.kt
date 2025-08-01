package io.kudos.ams.sys.service.cache

import io.kudos.test.common.init.EnableKudosTest
import io.kudos.test.container.containers.H2TestContainer
import io.kudos.test.container.containers.RedisTestContainer
import org.junit.jupiter.api.TestInstance
import org.soul.ability.cache.common.enums.CacheStrategy
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable


/**
 * 所有CacheHandlerTest的父类
 *
 * @author K
 * @since 1.0.0
 */
@EnableKudosTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
open class CacheHandlerTestBase {

    companion object {

        @DynamicPropertySource
        @JvmStatic
        private fun registerProperties(registry: DynamicPropertyRegistry) {
            registry.add("kudos.ability.cache.enabled") { "true" }
            registry.add("cache.config.strategy") { CacheStrategy.SINGLE_LOCAL.name }

            val h2Thread = Thread { H2TestContainer.startIfNeeded(registry) }
            val redisThread = Thread { RedisTestContainer.startIfNeeded(registry) }

            h2Thread.start()
            redisThread.start()

            h2Thread.join()
            redisThread.join()
        }

    }

}