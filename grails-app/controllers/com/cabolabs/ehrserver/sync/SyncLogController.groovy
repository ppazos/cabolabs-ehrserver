package com.cabolabs.ehrserver.sync

import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.*

class SyncLogController {

    SyncLogService syncLogService

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond syncLogService.list(params), model:[syncLogCount: syncLogService.count()]
    }

    def show(Long id) {
        respond syncLogService.get(id)
    }

    def create() {
        respond new SyncLog(params)
    }

    def save(SyncLog syncLog) {
        if (syncLog == null) {
            notFound()
            return
        }

        try {
            syncLogService.save(syncLog)
        } catch (ValidationException e) {
            respond syncLog.errors, view:'create'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'syncLog.label', default: 'SyncLog'), syncLog.id])
                redirect syncLog
            }
            '*' { respond syncLog, [status: CREATED] }
        }
    }

    def edit(Long id) {
        respond syncLogService.get(id)
    }

    def update(SyncLog syncLog) {
        if (syncLog == null) {
            notFound()
            return
        }

        try {
            syncLogService.save(syncLog)
        } catch (ValidationException e) {
            respond syncLog.errors, view:'edit'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'syncLog.label', default: 'SyncLog'), syncLog.id])
                redirect syncLog
            }
            '*'{ respond syncLog, [status: OK] }
        }
    }

    def delete(Long id) {
        if (id == null) {
            notFound()
            return
        }

        syncLogService.delete(id)

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'syncLog.label', default: 'SyncLog'), id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'syncLog.label', default: 'SyncLog'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
