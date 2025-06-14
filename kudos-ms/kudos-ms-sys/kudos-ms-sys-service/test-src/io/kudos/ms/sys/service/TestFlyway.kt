package io.kudos.ms.sys.service

import io.kudos.test.common.init.EnableKudosTest
import io.kudos.test.container.H2TestContainer
import org.flywaydb.core.Flyway
import org.soul.ability.data.rdb.flyway.FlywayTool
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
import org.springframework.context.annotation.Import
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.Test

@EnableKudosTest
@Testcontainers(disabledWithoutDocker = true)
//@Import(FlywayAutoConfiguration::class)
class TestFlyway {

    @Test
    fun test(){
        println("test")
//        println("Applied migrations: " + flyway.info().appliedMigrations.map { it.version.toString() })
    }

    companion object Companion {
        @JvmStatic
        @DynamicPropertySource
        private fun changeProperties(registry: DynamicPropertyRegistry) {
            H2TestContainer.start(registry)
        }
    }

}