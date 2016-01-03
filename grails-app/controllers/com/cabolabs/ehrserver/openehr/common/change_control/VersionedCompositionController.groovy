package com.cabolabs.ehrserver.openehr.common.change_control

import static org.springframework.http.HttpStatus.*
import com.cabolabs.ehrserver.openehr.common.change_control.VersionedComposition
import grails.transaction.Transactional

@Transactional(readOnly = true)
class VersionedCompositionController {

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond VersionedComposition.list(params), model:[versionedCompositionInstanceCount: VersionedComposition.count()]
    }

    def show(String uid) {
       def versionedCompositionInstance = VersionedComposition.findByUid(uid)
       respond versionedCompositionInstance
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'versionedComposition.label', default: 'VersionedComposition'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
