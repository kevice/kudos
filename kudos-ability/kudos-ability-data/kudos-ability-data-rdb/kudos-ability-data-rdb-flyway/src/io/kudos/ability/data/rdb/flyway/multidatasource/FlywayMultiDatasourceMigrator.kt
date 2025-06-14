package io.kudos.ability.data.rdb.flyway.multidatasource

import io.kudos.ability.data.rdb.jdbc.kit.RdbKit
import io.kudos.base.io.FileKit
import io.kudos.base.io.scanner.classpath.ClassPathScanner
import io.kudos.base.logger.LogFactory
import org.flywaydb.core.Flyway
import org.soul.ability.data.rdb.jdbc.datasource.DsContextProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.flyway.FlywayProperties
import java.io.File
import java.sql.Connection


/**
 * Flyway多数据源脚本升级器
 *
 * @author K
 * @since 1.0.0
 */
open class FlywayMultiDatasourceMigrator {

    @Autowired
    private lateinit var flywayMultiDatasourceProperties: FlywayMultiDatasourceProperties

    @Autowired
    private lateinit var flywayProperties: FlywayProperties

    @Autowired
    private lateinit var dsContextProcessor: DsContextProcessor

    private val log = LogFactory.getLog(this)

    private val sqlRootPath = "sql"

    fun migrate() {
        val locationUrls = ClassPathScanner.getLocationUrlsForPath(sqlRootPath)

        // 检测所有合法的模块
        val moduleNames = mutableListOf<String>()
        locationUrls.forEach { url ->
            val path = url.path
            val childFolders = if (url.protocol == "jar") {
                val pathes = FileKit.listFilesOrDirsInJar(url.toString().removeSuffix(sqlRootPath), sqlRootPath)
                pathes.map { it.removePrefix("$sqlRootPath/").removeSuffix("/") }
            } else {
                File(path).listFiles().map { it.name }
            }
            childFolders.forEach { moduleName ->
                if (!moduleNames.contains(moduleName)) {
                    val datasourceKey = flywayMultiDatasourceProperties.getDataSourceKey(moduleName)
                    if (!datasourceKey.isNullOrBlank()) {
                        moduleNames.add(moduleName)
                    } else {
                        log.warn("未配置模块【$moduleName】的数据源！忽略之。模块位置：${url.path}/$moduleName")
                    }
                } else {
                    error("存在名字为【${moduleName}】的多个模块！")
                }
            }
        }

        // 根据优先级排序模块名
        //TODO

        // 升级各模块的sql脚本
        moduleNames.forEach {
            migrateByModule(it)
        }

    }

    protected fun migrateByModule(moduleName: String) {
        val datasourceKey = flywayMultiDatasourceProperties.getDataSourceKey(moduleName)!!
        val datasource = dsContextProcessor.getDataSource(datasourceKey)
        var connection: Connection? = null
        try {
            connection = datasource.connection
            val dbType = RdbKit.determinRdbTypeByUrl(connection.metaData.url).name.lowercase()
            val flyway = Flyway.configure()
                .dataSource(datasource)
                .table("flyway_history_$moduleName")
                .locations("classpath:$sqlRootPath/$moduleName/$dbType")
                .baselineOnMigrate(flywayProperties.isBaselineOnMigrate)
                .baselineVersion(flywayProperties.baselineVersion)
                .encoding(flywayProperties.encoding)
                .outOfOrder(flywayProperties.isOutOfOrder)
                .validateOnMigrate(flywayProperties.isValidateOnMigrate)
                .placeholderReplacement(flywayProperties.isPlaceholderReplacement)
                .load()
            log.info(">>>>>>>>>>>>>  开始升级模块【${moduleName}】的数据库...")
            val result = flyway.migrate()
            if (result.success) {
                val migrationCount = result.migrationsExecuted
                if (migrationCount == 0) {
                    log.info("<<<<<<<<<<<<<  模块【$moduleName】数据库已为最新，无更新任何sql文件。")
                } else {
                    log.info("<<<<<<<<<<<<<  模块【$moduleName】数据库升级完成，共执行了${migrationCount}个sql文件，最新版本为：${result.targetSchemaVersion}")
                }
            } else {
                log.error("flyway升级模块【${moduleName}】的数据库失败！")
            }
        } catch (e: Exception) {
            log.error(e, "flyway升级模块【${moduleName}】数据库出错！")
        } finally {
            connection?.close()
        }
    }

}