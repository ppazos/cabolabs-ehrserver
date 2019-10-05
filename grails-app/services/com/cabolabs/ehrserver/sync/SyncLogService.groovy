package com.cabolabs.ehrserver.sync

import grails.gorm.services.Service

@Service(SyncLog)
interface SyncLogService {

    SyncLog get(Serializable id)

    List<SyncLog> list(Map args)

    Long count()

    void delete(Serializable id)

    SyncLog save(SyncLog syncLog)

}