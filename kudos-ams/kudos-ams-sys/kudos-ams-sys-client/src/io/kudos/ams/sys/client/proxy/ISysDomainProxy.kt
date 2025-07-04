package io.kudos.ams.sys.client.proxy

import io.kudos.ams.sys.common.api.ISysDomainApi
import io.kudos.ams.sys.client.fallback.SysDomainFallback
import org.springframework.cloud.openfeign.FeignClient


/**
 * 域名客户端代理接口
 *
 * @author K
 * @since 1.0.0
 */
//region your codes 1
@FeignClient(name = "sys-domain", fallback = SysDomainFallback::class)
interface ISysDomainProxy : ISysDomainApi {
//endregion your codes 1

    //region your codes 2

    //endregion your codes 2

}