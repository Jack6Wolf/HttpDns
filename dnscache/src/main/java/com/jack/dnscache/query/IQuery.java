package com.jack.dnscache.query;

import com.jack.dnscache.model.DomainModel;

/**
 * 查询模块 对外接口
 *
 * @version 1.0
 */
public interface IQuery {

    public DomainModel queryDomainIp(String sp, String host);


    public DomainModel getCacheDomainIp(String sp, String host);
}
