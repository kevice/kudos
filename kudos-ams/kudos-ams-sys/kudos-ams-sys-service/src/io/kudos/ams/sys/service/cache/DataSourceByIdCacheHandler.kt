package io.kudos.ams.sys.service.cache

import io.kudos.ability.cache.common.support.AbstractByIdCacheHandler
import io.kudos.ams.sys.common.vo.datasource.SysDataSourceCacheItem
import io.kudos.ams.sys.service.dao.SysDataSourceDao
import org.soul.ability.cache.common.batch.BatchCacheable
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component


/**
 * 数据源缓存（by id）处理器
 *
 * 1. 缓存所有数据源，包括active=false的
 * 2. 缓存key为：id
 * 3. 缓存value为：SysDataSourceCacheItem对象
 *
 * @author K
 * @since 1.0.0
 */
@Component
open class DataSourceByIdCacheHandler: AbstractByIdCacheHandler<String, SysDataSourceCacheItem, SysDataSourceDao>() {

    companion object {
        private const val CACHE_NAME = "SYS_DATA_SOURCE_BY_ID"
    }

    override fun itemDesc() = "数据源"

    override fun cacheName() = CACHE_NAME

    override fun doReload(key: String): SysDataSourceCacheItem? {
        return getSelf<DataSourceByIdCacheHandler>().getDataSourceById(key)
    }

    /**
     * 根据id从缓存获取数据源，如果缓存中不存在，则从数据库中加载，并存入缓存
     *
     * @param id 数据源id
     * @return SysDataSourceCacheItem，找不到则返回null
     */
    @Cacheable(
        cacheNames = [CACHE_NAME],
        key = "#id",
        unless = "#result == null"
    )
    open fun getDataSourceById(id: String): SysDataSourceCacheItem? {
        return getById(id)
    }

    /**
     * 根据多个id批量从缓存获取数据源，缓存中不存在的，则从数据库中加载，并存入缓存
     *
     * @param ids 数据源id集合
     * @return Map<数据源id, SysDataSourceCacheItem>，不存在的id不会放入map。
     */
    @BatchCacheable(
        cacheNames = [CACHE_NAME],
        valueClass = SysDataSourceCacheItem::class
    )
    open fun getDataSourcesByIds(ids: Collection<String>): Map<String, SysDataSourceCacheItem> {
        return getByIds(ids)
    }

}