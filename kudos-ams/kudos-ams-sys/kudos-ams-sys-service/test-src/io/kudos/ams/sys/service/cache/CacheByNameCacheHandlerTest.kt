package io.kudos.ams.sys.service.cache

import io.kudos.ability.cache.common.kit.CacheKit
import io.kudos.ams.sys.common.vo.cache.SysCacheCacheItem
import io.kudos.ams.sys.service.dao.SysCacheDao
import io.kudos.ams.sys.service.model.po.SysCache
import org.soul.ability.cache.common.enums.CacheStrategy
import org.springframework.beans.factory.annotation.Autowired
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * junit test for CacheByNameCacheHandler
 *
 * @author K
 * @since 1.0.0
 */
@EnabledIfDockerAvailable
class CacheByNameCacheHandlerTest : CacheHandlerTestBase() {

    @Autowired
    private lateinit var cacheByNameCacheHandler: CacheByNameCacheHandler

    @Autowired
    private lateinit var sysCacheDao: SysCacheDao

    private val newCacheName = "a_new_test_cache"

    @Test
    fun reloadAll() {
        // 清除并重载缓存，保证与数据库中的数据一致
        cacheByNameCacheHandler.reloadAll(true)

        // 获取当前缓存中的记录
        val cacheName = "TEST_CACHE_1"
        val cacheItem = cacheByNameCacheHandler.getCache(cacheName)

        // 插入新的记录到数据库
        val sysCache = insertNewRecordToDb()

        // 更新数据库的记录
        val cacheIdUpdate = "e5340806-97b4-43a4-84c6-222222222222"
        val cacheNameUpdate = "TEST_CACHE_2"
        val newTtl = 666666
        sysCacheDao.updateProperties(cacheIdUpdate, mapOf(SysCache::ttl.name to newTtl))

        // 从数据库中删除记录
        val idDelete = "e5340806-97b4-43a4-84c6-333333333333"
        val cacheNameDelete = "TEST_CACHE_3"
        sysCacheDao.deleteById(idDelete)

        // 重载缓存，但不清除旧缓存
        cacheByNameCacheHandler.reloadAll(false)

        // 原来缓存中的记录内存地址会变
        val cacheItem1 = cacheByNameCacheHandler.getCache(cacheName)
        assert(cacheItem !== cacheItem1)

        // 数据库中新增的记录在缓存应该要存在
        val cacheItemNew = cacheByNameCacheHandler.getCache(sysCache.name)
        assertNotNull(cacheItemNew)

        // 数据库中更新的记录在缓存中应该也更新了
        val cacheItemUpdate = cacheByNameCacheHandler.getCache(cacheNameUpdate)
        assertEquals(newTtl, cacheItemUpdate!!.ttl)

        // 数据库中删除的记录在缓存中应该还在
        var cacheItemDelete = cacheByNameCacheHandler.getCache(cacheNameDelete)
        assertNotNull(cacheItemDelete)


        // 清除并重载缓存
        cacheByNameCacheHandler.reloadAll(true)

        // 数据库中删除的记录在缓存中应该不存在了
        cacheItemDelete = cacheByNameCacheHandler.getCache(cacheNameDelete)
        assertNull(cacheItemDelete)
    }

    @Test
    fun getCache() {
        var cacheName = "TEST_CACHE_1"
        cacheByNameCacheHandler.getCache(cacheName) // 第一次当放入远程缓存后，会发送清除本地缓存，所以最终取到的是远程缓存反序列化后的对象
        val cacheItem2 = cacheByNameCacheHandler.getCache(cacheName)
        val cacheItem3 = cacheByNameCacheHandler.getCache(cacheName)
        assert(cacheItem2 === cacheItem3)

        // active为false的在缓存中应该不存在
        cacheName = "TEST_CACHE_6"
        assertNull(cacheByNameCacheHandler.getCache(cacheName))
    }

    @Test
    fun syncOnInsert() {
        // 插入新的记录到数据库
        val sysCache = insertNewRecordToDb()

        // 同步缓存
        cacheByNameCacheHandler.syncOnInsert(sysCache, sysCache.id!!)

        // 验证新记录是否在缓存中
        val cacheItem1 = CacheKit.getValue(cacheByNameCacheHandler.cacheName(), newCacheName)
        assertNotNull(cacheItem1)
        val cacheItem2 = cacheByNameCacheHandler.getCache(newCacheName)
        assert(cacheItem1 === cacheItem2)
    }

    @Test
    fun syncOnUpdate() {
        // 更新数据库中已存在的记录
        val cacheId = "e5340806-97b4-43a4-84c6-222222222222"
        val cacheName = "TEST_CACHE_2"
        val newTtl = 666666
        val success = sysCacheDao.updateProperties(cacheId, mapOf(SysCache::ttl.name to newTtl))
        assert(success)

        // 同步缓存
        val sysCache = SysCache().apply { name = cacheName }
        cacheByNameCacheHandler.syncOnUpdate(sysCache, cacheId)

        // 验证缓存中的记录
        val cacheItem1 = CacheKit.getValue(cacheByNameCacheHandler.cacheName(), cacheName)
        assertEquals(newTtl, (cacheItem1 as SysCacheCacheItem).ttl)
        val cacheItem2 = cacheByNameCacheHandler.getCache(cacheName)
        assertEquals(newTtl, (cacheItem2 as SysCacheCacheItem).ttl)
    }

    @Test
    fun syncOnDelete() {
        // 删除数据库中的记录
        val id = "e5340806-97b4-43a4-84c6-333333333333"
        val name = "TEST_CACHE_3"
        val deleteSuccess = sysCacheDao.deleteById(id)
        assert(deleteSuccess)

        // 同步缓存
        cacheByNameCacheHandler.syncOnDelete(id, name)

        // 验证缓存中有没有
        val cacheItem1 = CacheKit.getValue(cacheByNameCacheHandler.cacheName(), name)
        assertNull(cacheItem1)
        val cacheItem2 = cacheByNameCacheHandler.getCache(name)
        assertNull(cacheItem2)
    }

    @Test
    fun syncOnBatchDelete() {
        // 批量删除数据库中的记录
        val id1 = "2da8e352-6e6f-4cd4-93e0-444444444444"
        val name1 = "TEST_CACHE_4"
        val id2 = "2da8e352-6e6f-4cd4-93e0-555555555555"
        val name2 = "TEST_CACHE_5"
        val ids = listOf(id1, id2)
        val count = sysCacheDao.batchDelete(ids)
        assert(count == 2)

        // 同步缓存
        cacheByNameCacheHandler.syncOnBatchDelete(ids, listOf(name1, name2))

        // 验证缓存中有没有
        val cacheItem1 = CacheKit.getValue(cacheByNameCacheHandler.cacheName(), name1)
        assertNull(cacheItem1)
        val cacheItem2 = cacheByNameCacheHandler.getCache(name1)
        assertNull(cacheItem2)
        val cacheItem3 = CacheKit.getValue(cacheByNameCacheHandler.cacheName(), name2)
        assertNull(cacheItem3)
        val cacheItem4 = cacheByNameCacheHandler.getCache(name2)
        assertNull(cacheItem4)
    }

    private fun insertNewRecordToDb() : SysCache {
        val sysCache = SysCache().apply {
            name = newCacheName
            atomicServiceCode = "ams-sys"
            strategyDictCode = CacheStrategy.SINGLE_LOCAL.name
            writeOnBoot = true
            writeInTime = true
            ttl = 666666
        }
        sysCacheDao.insert(sysCache)
        return sysCache
    }

}