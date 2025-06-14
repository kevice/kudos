package io.kudos.ability.data.rdb.flyway.multidatasource

import io.kudos.base.io.scanner.classpath.ClassPathScanner
import org.soul.ability.data.rdb.jdbc.datasource.DsContextProcessor
import org.springframework.beans.factory.annotation.Autowired
import java.net.URL


/**
 * Flyway多数据源处理器
 *
 * @author K
 * @since 1.0.0
 */
open class FlywayMultiDatasourceHandler {

    @Autowired
    private lateinit var flywayMultiDatasourceProperties: FlywayMultiDatasourceProperties

    @Autowired
    private lateinit var dsContextProcessor: DsContextProcessor

    protected fun findAllModules() : List<String> {
        val locationUrls = ClassPathScanner.getLocationUrlsForPath("sql")
        return listOf()
    }

    fun migrateAll() {
        val modules = findAllModules()
        println("######################### module: $modules")
    }

}