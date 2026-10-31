package com.invento.domain.salessite.repository;

import com.invento.domain.salessite.entity.SalesSite;
import com.invento.global.common.BaseRepository;

public interface SalesSiteRepository extends BaseRepository<SalesSite> {

    boolean existsBySalesSiteName(String salesSiteName);

    boolean existsBySalesSiteNameAndSalesSiteIdNot(String salesSiteName, Long salesSiteId);
}
