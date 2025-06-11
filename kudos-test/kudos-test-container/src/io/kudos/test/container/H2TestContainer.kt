package io.kudos.test.container

import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.FixedHostPortGenericContainer
import org.testcontainers.containers.wait.strategy.Wait

/**
 * h2测试容器
 *
 * @author K
 * @since 1.0.0
 */
object H2TestContainer {

    private const val IMAGE_NAME = "oscarfonts/h2:2.1.210"

    const val DATABASE = "test"

    const val PORT = 1521

    const val USERNAME = "sa"

    const val PASSWORD = ""

    val container = FixedHostPortGenericContainer(IMAGE_NAME)
        // 通过环境变量指定 H2 启动参数：TCP 模式，允许外部访问，不存在时创建数据库
        .withEnv("H2_OPTIONS", "-tcp -tcpAllowOthers -ifNotExists")
        .withFixedExposedPort(PORT, 1521)
        .waitingFor(Wait.forListeningPort())


    fun start(registry: DynamicPropertyRegistry?): FixedHostPortGenericContainer<*> {
        println(">>>>>>>>>>>>>>>>>>>> Starting h2 container...")
        container.start()
        if (registry != null) {
            registerProperties(registry)
        }
        println(">>>>>>>>>>>>>>>>>>>> H2 container started.")
        return container
    }

    private fun registerProperties(registry: DynamicPropertyRegistry) {
        val url = "jdbc:h2:tcp://localhost:$PORT/mem:$DATABASE;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;"
        registry.add("spring.datasource.dynamic.datasource.h2.url") { url }
        registry.add("spring.datasource.dynamic.datasource.h2.username") { USERNAME }
        registry.add("spring.datasource.dynamic.datasource.h2.password") { PASSWORD }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        start(null)
        println("H2 ${container.host} port: $PORT")
        Thread.sleep(Long.Companion.MAX_VALUE)
    }

}
