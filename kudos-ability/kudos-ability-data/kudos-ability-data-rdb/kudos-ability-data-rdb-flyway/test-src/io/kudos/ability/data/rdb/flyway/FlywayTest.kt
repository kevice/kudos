package io.kudos.ability.data.rdb.flyway

import io.kudos.ability.data.rdb.flyway.multidatasource.FlywayMultiDatasourceMigrator
import io.kudos.test.common.init.EnableKudosTest
import io.kudos.test.container.H2TestContainer
import junit.framework.TestCase.assertEquals
import org.soul.ability.data.rdb.jdbc.datasource.DsContextProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Testcontainers
import java.sql.Connection
import kotlin.test.Test

/**
 * Flyway test
 *
 * @author K
 * @since 1.0.0
 */
@EnableKudosTest
@Testcontainers(disabledWithoutDocker = true)
class FlywayTest {

    @Autowired
    private lateinit var migrator: FlywayMultiDatasourceMigrator

    @Autowired
    private lateinit var dsContextProcessor: DsContextProcessor


    @Test
    fun migrate() {
        migrator.migrate() // 模拟重复执行数据库更新也没有问题
        val datasource = dsContextProcessor.getDataSource("ds1")
        var connection: Connection? = null
        try {
            connection = datasource.connection
            val sql = "select count(*) from test_table"
            val statement = connection.createStatement()
            val rs = statement.executeQuery(sql)
            assert(rs.next())
            assertEquals(2, rs.getInt(1))
            rs.close()
            statement.close()
        } finally {
            connection?.close()
        }
    }

    companion object Companion {
        @JvmStatic
        @DynamicPropertySource
        private fun changeProperties(registry: DynamicPropertyRegistry) {
            H2TestContainer.start(registry)
        }
    }

}