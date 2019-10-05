package com.cabolabs.ehrserver.sync

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification
import org.hibernate.SessionFactory

@Integration
@Rollback
class SyncLogServiceSpec extends Specification {

    SyncLogService syncLogService
    SessionFactory sessionFactory

    private Long setupData() {
        // TODO: Populate valid domain instances and return a valid ID
        //new SyncLog(...).save(flush: true, failOnError: true)
        //new SyncLog(...).save(flush: true, failOnError: true)
        //SyncLog syncLog = new SyncLog(...).save(flush: true, failOnError: true)
        //new SyncLog(...).save(flush: true, failOnError: true)
        //new SyncLog(...).save(flush: true, failOnError: true)
        assert false, "TODO: Provide a setupData() implementation for this generated test suite"
        //syncLog.id
    }

    void "test get"() {
        setupData()

        expect:
        syncLogService.get(1) != null
    }

    void "test list"() {
        setupData()

        when:
        List<SyncLog> syncLogList = syncLogService.list(max: 2, offset: 2)

        then:
        syncLogList.size() == 2
        assert false, "TODO: Verify the correct instances are returned"
    }

    void "test count"() {
        setupData()

        expect:
        syncLogService.count() == 5
    }

    void "test delete"() {
        Long syncLogId = setupData()

        expect:
        syncLogService.count() == 5

        when:
        syncLogService.delete(syncLogId)
        sessionFactory.currentSession.flush()

        then:
        syncLogService.count() == 4
    }

    void "test save"() {
        when:
        assert false, "TODO: Provide a valid instance to save"
        SyncLog syncLog = new SyncLog()
        syncLogService.save(syncLog)

        then:
        syncLog.id != null
    }
}
