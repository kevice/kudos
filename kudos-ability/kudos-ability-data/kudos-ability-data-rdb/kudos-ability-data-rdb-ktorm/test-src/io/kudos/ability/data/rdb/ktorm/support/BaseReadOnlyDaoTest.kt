package io.kudos.ability.data.rdb.ktorm.support

import io.kudos.ability.data.rdb.jdbc.kit.RdbKit
import io.kudos.ability.data.rdb.ktorm.kit.getDatabase
import io.kudos.ability.data.rdb.ktorm.table.TestTableKtorm
import io.kudos.ability.data.rdb.ktorm.table.TestTableKtormDao
import io.kudos.ability.data.rdb.ktorm.table.TestTableKtorms
import io.kudos.base.query.Criteria
import io.kudos.base.query.Criterion
import io.kudos.base.query.enums.OperatorEnum
import io.kudos.base.query.sort.Order
import io.kudos.base.support.payload.ListSearchPayload
import io.kudos.base.support.payload.SearchPayload
import io.kudos.test.common.init.EnableKudosTest
import org.ktorm.dsl.eq
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * BaseReadOnlyDao测试用例
 *
 * @author K
 * @since 1.0.0
 */
@EnableKudosTest
internal open class BaseReadOnlyDaoTest {

    @Autowired
    private lateinit var testTableDao: TestTableKtormDao


    //region Search
    @Test
    fun getById() {
        val entity = testTableDao.get(-1)
        assertEquals("name1", entity!!.name)

        // 不存在指定主键对应的实体时
        assert(testTableDao.get(1) == null)
    }

    @Test
    fun get() {
        class Record {
            var name: String? = null
            var birthday: LocalDateTime? = null
            var active: Boolean? = null
            var noExistProp: String? = null
        }

        val record = testTableDao.get(-1, Record::class)!!
        assertEquals("name1", record.name)
        assertEquals(true, record.active)
        assertEquals(null, record.noExistProp)

        // 不存在指定主键对应的实体时
        assert(testTableDao.get(1, Record::class) == null)
    }

    @Test
    fun getByIds() {
        assert(testTableDao.getByIds().isEmpty())

        var entities = testTableDao.getByIds(-1, -2, -3)
        assertEquals(3, entities.size)

        entities = testTableDao.getByIds(-1, -2, -3, countOfEachBatch = 2)
        assertEquals(3, entities.size)
    }
    //endregion Search


    //region oneSearch
    @Test
    fun oneSearch() {
        var results = testTableDao.oneSearch(TestTableKtorm::name.name, "name1")
        assertEquals(1, results.size)
        assertEquals(-1, results.first().id)

        // value为null的情况
        results = testTableDao.oneSearch(TestTableKtorm::weight.name, null)
        assertEquals(2, results.size)

        // 单条件升序
        results = testTableDao.oneSearch(TestTableKtorm::active.name, true, Order.asc(TestTableKtorm::name.name))
        assertEquals(8, results.size)
        assertEquals("name1", results.first().name)
        assertEquals("name9", results.last().name)

        // 单条件降序
        results = testTableDao.oneSearch(TestTableKtorm::active.name, true, Order.desc(TestTableKtorm::name.name))
        assertEquals(8, results.size)
        assertEquals("name9", results.first().name)
        assertEquals("name1", results.last().name)

        // 多个排序条件
        val orders = arrayOf(Order.asc(TestTableKtorm::height.name), Order.desc(TestTableKtorm::name.name))
        results = testTableDao.oneSearch(TestTableKtorm::active.name, true, *orders)
        assertEquals(8, results.size)
        assertEquals("name5", results.first().name)
        assertEquals("name4", results[5].name)
    }

    @Test
    fun oneSearchProperty() {
        var results = testTableDao.oneSearchProperty(TestTableKtorm::name.name, "name1", TestTableKtorm::id.name)
        assertEquals(1, results.size)
        assertEquals(-1, results.first())
        results = testTableDao.oneSearchProperty(TestTableKtorm::weight.name, null, TestTableKtorm::id.name)
        assertEquals(2, results.size)
    }

    @Test
    fun oneSearchProperties() {
        val returnProperties = listOf(TestTableKtorm::name.name, TestTableKtorm::id.name)
        val results = testTableDao.oneSearchProperties(TestTableKtorm::name.name, "name1", returnProperties)
        assertEquals(1, results.size)
        assertEquals("name1", results.first()[TestTableKtorm::name.name])
    }
    //endregion oneSearch


    //region allSearch
    @Test
    fun allSearch() {
        val result = testTableDao.allSearch()
        assertEquals(11, result.size)
    }

    @Test
    fun allSearchProperty() {
        val results = testTableDao.allSearchProperty(TestTableKtorm::id.name, Order.desc(TestTableKtorm::id.name))
        assertEquals(11, results.size)
        assertEquals(Integer.valueOf(-1), results[0])
        assertEquals(Integer.valueOf(-11), results[10])
    }

    @Test
    fun allSearchProperties() {
        val returnProperties = listOf(TestTableKtorm::name.name, TestTableKtorm::id.name)
        val results = testTableDao.allSearchProperties(returnProperties)
        assertEquals(11, results.size)
    }
    //endregion allSearch


    //region andSearch
    @Test
    fun andSearch() {
        val propertyMap = mapOf(TestTableKtorm::name.name to "name5", TestTableKtorm::weight.name to null)
        var results = testTableDao.andSearch(propertyMap)
        assertEquals(1, results.size)

        // 自定义查询逻辑
        results = testTableDao.andSearch(propertyMap) { column, _ ->
            if (column.name == TestTableKtorms.name.name) {
                column.ilike("%Me5")
            } else null
        }
        assertEquals(1, results.size)
        assertEquals("name5", results.first().name)
    }

    @Test
    fun andSearchProperty() {
        val propertyMap = mapOf(TestTableKtorm::name.name to "name5", TestTableKtorm::weight.name to null)
        val results = testTableDao.andSearchProperty(propertyMap, TestTableKtorm::name.name)
        assertEquals("name5", results.first())
    }

    @Test
    fun andSearchProperties() {
        val propertyMap = mapOf(TestTableKtorm::name.name to "name1", TestTableKtorm::active.name to true)
        val returnProperties = listOf(TestTableKtorm::name.name, TestTableKtorm::id.name)
        val results = testTableDao.andSearchProperties(propertyMap, returnProperties)
        assertEquals(1, results.size)
        assertEquals("name1", results.first()[TestTableKtorm::name.name])
    }
    //endregion andSearch


    //region orSearch
    @Test
    fun orSearch() {
        val propertyMap = mapOf(TestTableKtorm::name.name to "name5", TestTableKtorm::weight.name to null)
        val results = testTableDao.orSearch(propertyMap)
        assertEquals(2, results.size)
    }

    @Test
    fun orSearchProperty() {
        val propertyMap = mapOf(TestTableKtorm::name.name to "name5", TestTableKtorm::weight.name to null)
        val results = testTableDao.orSearchProperty(propertyMap, TestTableKtorm::name.name, Order.desc(TestTableKtorm::id.name))
        assertEquals(2, results.size)
        assertEquals("name5", results.first())
    }

    @Test
    fun orSearchProperties() {
        val propertyMap = mapOf(TestTableKtorm::name.name to "name5", TestTableKtorm::weight.name to null)
        val returnProperties = listOf(TestTableKtorm::name.name, TestTableKtorm::id.name)
        val results = testTableDao.orSearchProperties(propertyMap, returnProperties, Order.desc(TestTableKtorm::id.name))
        assertEquals(2, results.size)
        assertEquals("name5", results.first()[TestTableKtorm::name.name])
    }
    //endregion orSearch


    //region inSearch
    @Test
    fun inSearch() {
        val ids = listOf(-3, -2, -1, null)
        val results = testTableDao.inSearch(TestTableKtorm::id.name, ids, Order.desc(TestTableKtorm::id.name))
        assertEquals(3, results.size)
        assertEquals("name1", results.first().name)
    }

    @Test
    fun inSearchProperty() {
        val ids = listOf(-3, -2, -1)
        val results =
            testTableDao.inSearchProperty(TestTableKtorm::id.name, ids, TestTableKtorm::name.name, Order.desc(TestTableKtorm::id.name))
        assertEquals(3, results.size)
        assertEquals("name1", results.first())
    }

    @Test
    fun inSearchProperties() {
        val ids = listOf(-3, -2, -1)
        val returnProperties = listOf(TestTableKtorm::name.name, TestTableKtorm::id.name, TestTableKtorm::active.name)
        val results =
            testTableDao.inSearchProperties(TestTableKtorm::id.name, ids, returnProperties, Order.desc(TestTableKtorm::id.name))
        assertEquals(3, results.size)
        assertEquals("name1", results.first()[TestTableKtorm::name.name])
    }

    @Test
    fun inSearchById() {
        val ids = listOf(-3, -2, -1)
        val results = testTableDao.inSearchById(ids)
        assertEquals(3, results.size)
    }

    @Test
    fun inSearchPropertyById() {
        val ids = listOf(-3, -2, -1)
        val results =
            testTableDao.inSearchPropertyById(ids, TestTableKtorm::name.name, Order.desc(TestTableKtorm::id.name))
        assertEquals(3, results.size)
        assertEquals("name1", results.first())
    }

    @Test
    fun inSearchPropertiesById() {
        val ids = listOf(-3, -2, -1)
        val returnProperties = listOf(TestTableKtorm::name.name, TestTableKtorm::id.name)
        val results = testTableDao.inSearchPropertiesById(ids, returnProperties, Order.desc(TestTableKtorm::id.name))
        assertEquals(3, results.size)
        assertEquals("name1", results.first()[TestTableKtorm::name.name])
    }
    //endregion inSearch


    //region search Criteria
    @Test
    fun search() {
        val inIds = Criterion(TestTableKtorm::id.name, OperatorEnum.IN, listOf(-2, -4, -6, -7))
        val eqActive = Criterion(TestTableKtorm::active.name, OperatorEnum.EQ, true)
        val andCriteria = Criteria.and(inIds, eqActive)
        val likeName = Criterion(TestTableKtorm::name.name, OperatorEnum.LIKE_S, "name1")
        val orCriteria: Criteria = Criteria.or(likeName, andCriteria)
        val noNull = Criterion(TestTableKtorm::weight.name, OperatorEnum.IS_NOT_NULL, null)
        val criteria: Criteria = Criteria.and(orCriteria, noNull)
        val results = testTableDao.search(criteria, Order.desc(TestTableKtorm::weight.name))
        assertEquals(5, results.size)
        assertEquals(-10, results.first().id)
    }

    @Test
    fun searchProperty() {
        // ILIKE_S，IS_NOT_NULL，GT
        var criteria = Criteria.and(
            Criterion(TestTableKtorm::name.name, OperatorEnum.ILIKE_S, "Name1"),
            Criterion(TestTableKtorm::weight.name, OperatorEnum.IS_NOT_NULL, null),
            Criterion(TestTableKtorm::height.name, OperatorEnum.GT, 160)
        )
        var results = testTableDao.searchProperty(criteria, TestTableKtorm::id.name, Order.desc(TestTableKtorm::weight.name))
        assertEquals(2, results.size)
        assertEquals(-10, results.first())

        // IEQ
        criteria = Criteria.of(TestTableKtorm::name.name, OperatorEnum.IEQ, "Name1")
        results = testTableDao.searchProperty(criteria, TestTableKtorm::id.name)
        assertEquals(1, results.size)

        // GT_P
        criteria = Criteria.of(TestTableKtorm::height.name, OperatorEnum.GT_P, TestTableKtorm::weight.name)
        results = testTableDao.searchProperty(criteria, TestTableKtorm::id.name)
        assertEquals(9, results.size)

        // NE_P
        criteria = Criteria.of(TestTableKtorm::height.name, OperatorEnum.NE_P, TestTableKtorm::weight.name)
        results = testTableDao.searchProperty(criteria, TestTableKtorm::id.name)
        assertEquals(9, results.size)
    }

    @Test
    fun searchProperties() {
        val inIds = Criterion(TestTableKtorm::id.name, OperatorEnum.IN, listOf(-2, -4, -6, -7))
        val eqActive = Criterion(TestTableKtorm::active.name, OperatorEnum.EQ, true)
        val andCriteria = Criteria.and(inIds, eqActive)
        val likeName = Criterion(TestTableKtorm::name.name, OperatorEnum.LIKE_S, "name1")
        val orCriteria: Criteria = Criteria.or(likeName, andCriteria)
        val noNull = Criterion(TestTableKtorm::weight.name, OperatorEnum.IS_NOT_NULL, null)
        val criteria: Criteria = Criteria.and(orCriteria, noNull)
        val returnProperties = listOf(TestTableKtorm::active.name, TestTableKtorm::name.name)
        val results = testTableDao.searchProperties(criteria, returnProperties, Order.desc(TestTableKtorm::weight.name))
        assertEquals(5, results.size)
        assertEquals("name10", results.first()[TestTableKtorm::name.name])
        assertEquals(false, results.first()[TestTableKtorm::active.name])
    }
    //endregion search Criteria


    //region pagingSearch
    @Test
    fun pagingSearch() {
        if (isSupportPaging()) {
            val criteria = Criteria.of(TestTableKtorm::active.name, OperatorEnum.EQ, true)
            val entities = testTableDao.pagingSearch(criteria, 1, 4, Order.asc(TestTableKtorm::id.name))
            assertEquals(4, entities.size)
            assertEquals(-11, entities.first().id)
        }
    }

    @Test
    fun pagingReturnProperty() {
        if (isSupportPaging()) {
            val criteria = Criteria.of(TestTableKtorm::active.name, OperatorEnum.EQ, true)
            val results =
                testTableDao.pagingReturnProperty(criteria, TestTableKtorm::id.name, 1, 4, Order.asc(TestTableKtorm::id.name))
            assertEquals(4, results.size)
            assertEquals(-11, results.first())
        }
    }

    @Test
    fun pagingReturnProperties() {
        if (isSupportPaging()) {
            val criteria = Criteria.of(TestTableKtorm::active.name, OperatorEnum.EQ, true)
            val returnProperties = listOf(TestTableKtorm::id.name, TestTableKtorm::name.name)
            val results =
                testTableDao.pagingReturnProperties(criteria, returnProperties, 1, 4, Order.asc(TestTableKtorm::id.name))
            assertEquals(4, results.size)
            assertEquals(-11, results.first()[TestTableKtorm::id.name])
        }
    }

    private fun isSupportPaging(): Boolean { // h2可以用PostgreSqlDialect来实现分页
        val dialect = RdbKit.getDatabase().dialect
        return !dialect::class.java.name.contains("SqlDialectKt\$detectDialectImplementation\$1")
    }
    //endregion pagingSearch


    //region payload search

    @Test
    fun searchBySearchPayload() {
        // 指定returnProperties, 多个属性
        class SearchPayload1 : ListSearchPayload() {
            var name: String? = null
            var weight: Double? = null
            var noExistProp: String? = "noExistProp"
            override var returnProperties: List<String>? = listOf("id", "name", "height")
        }

        // 仅指定SearchPayload
        val searchPayload1 = SearchPayload1().apply {
            name = "name1"
            weight = 56.5
        }
        var result = testTableDao.search(searchPayload1)
        assertEquals(1, result.size)
        assert(result.first() is Map<*, *>)
        assertEquals(3, (result.first() as Map<*, *>).size)
        assertEquals(-1, (result.first() as Map<*, *>)["id"])

        // 指定returnProperties, 单个属性
        searchPayload1.returnProperties = listOf("id")
        result = testTableDao.search(searchPayload1)
        assertEquals(1, result.size)
        assertEquals(-1, result.first())

        // returnProperties为null
        searchPayload1.returnProperties = null
        result = testTableDao.search(searchPayload1)
        assertEquals(1, result.size)
        assert(result.first() is TestTableKtorm)
        assertEquals(-1, (result.first() as TestTableKtorm).id)

        // 分页 & 排序
        searchPayload1.name = null
        searchPayload1.weight = null
        searchPayload1.pageNo = 1
        searchPayload1.pageSize = 3
        searchPayload1.orders = listOf(Order.asc(TestTableKtorm::name.name))
        result = testTableDao.search(searchPayload1)
        assertEquals(3, result.size)
        assert(result.first() is TestTableKtorm)
        assertEquals(-1, (result.first() as TestTableKtorm).id)

        // 指定结果封装类
        class Result {
            var id: Int? = null
            var name: String? = null
            var weight: Double? = null
            var noExistProp: String? = "noExistProp"
        }
        searchPayload1.returnEntityClass = Result::class
        result = testTableDao.search(searchPayload1)
        assertEquals(3, result.size)
        assert(result.first() is Result)
        assertEquals(-1, (result.first() as Result).id)

        // 自定义查询逻辑（通过工厂）
        searchPayload1.name = "nAme1"
        searchPayload1.pageNo = null
        result = testTableDao.search(searchPayload1) { column, value ->
            if (column.name == TestTableKtorms::name.name) {
                testTableDao.whereExpr(column, OperatorEnum.ILIKE_S, value)
            } else {
                null
            }
        }
        assertEquals(3, result.size)

        // 自定义查询逻辑（通过工厂+通过SearchPayload，工厂方式优先）
        class SearchPayload2 : ListSearchPayload() {
            var name: String? = null
            var weight: Double? = null
            var noExistProp: String? = "noExistProp"
            override var returnProperties: List<String>? = listOf("id", "name", "height")
            override var operators: Map<String, OperatorEnum>? = mapOf(SearchPayload2::name.name to OperatorEnum.ILIKE)
        }

        val searchPayload2 = SearchPayload2().apply {
            name = "nAme1"
            weight = 56.5
            pageNo = null
        }
        result = testTableDao.search(searchPayload2) { column, value ->
            if (column.name == TestTableKtorms::name.name) {
                testTableDao.whereExpr(column, OperatorEnum.ILIKE_S, value)
            } else {
                null
            }
        }
        assertEquals(1, result.size)

        // 仅指定whereConditionFactory
        result = testTableDao.search { column, _ ->
            when (column.name) {
                TestTableKtorms::name.name -> {
                    column.ilike("nAme1%")
                }
                TestTableKtorms::active.name -> {
                    column.eq(true)
                }
                else -> null
            }
        }
        assertEquals(2, result.size)

        // 不指定任何条件，相当于allSearch()
        result = testTableDao.search()
        assertEquals(11, result.size)
    }

    //endregion payload search


    //region aggregate
    @Test
    fun count() {
        assertEquals(11, testTableDao.count(null as Criteria?))
        val criteria = Criteria.of(TestTableKtorm::name.name, OperatorEnum.LIKE_S, "name1")
        assertEquals(3, testTableDao.count(criteria))
    }

    @Test
    fun countByPayload() {
        class SearchPayload1 : SearchPayload() {
            var name: String? = null
            var weight: Double? = null
            var noExistProp: String? = "noExistProp"
        }

        val searchPayload1 = SearchPayload1().apply {
            name = "nAme1"
        }
        val result = testTableDao.count(searchPayload1) { column, value ->
            if (column.name == TestTableKtorms::name.name) {
                testTableDao.whereExpr(column, OperatorEnum.ILIKE_S, value)
            } else {
                null
            }
        }
        assertEquals(3, result)
    }

    @Test
    fun sum() {
        assertEquals(1382, testTableDao.sum(TestTableKtorm::height.name))
        val criteria = Criteria.of(TestTableKtorm::name.name, OperatorEnum.LIKE_S, "name1")
        assertEquals(122.5, testTableDao.sum(TestTableKtorm::weight.name, criteria))
        assertEquals(445.74, testTableDao.sum(TestTableKtorm::weight.name))
    }

    @Test
    fun avg() {
        val criteria = Criteria.of(TestTableKtorm::name.name, OperatorEnum.LIKE_S, "name1")
        assertEquals(61.25, testTableDao.avg(TestTableKtorm::weight.name, criteria))
    }

    @Test
    fun max() {
        val criteria = Criteria.of(TestTableKtorm::name.name, OperatorEnum.LIKE_S, "name1")
        assertEquals(66.0, testTableDao.max(TestTableKtorm::weight.name, criteria))
    }

    @Test
    fun min() {
        val criteria = Criteria.of(TestTableKtorm::name.name, OperatorEnum.LIKE_S, "name1")
        assertEquals(56.5, testTableDao.min(TestTableKtorm::weight.name, criteria))
    }
    //endregion aggregate

}