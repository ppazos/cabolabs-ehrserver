package com.cabolabs.ehrserver.identification

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

@Transactional(readOnly = true)
class PersonIdTypeController {

    static allowedMethods = [save: "POST", update: "PUT"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond PersonIdType.list(params), model:[personIdTypeInstanceCount: PersonIdType.count()]
    }

    def show(PersonIdType personIdTypeInstance) {
        respond personIdTypeInstance
    }

    def create() {
        respond new PersonIdType(params)
    }

    @Transactional
    def save(PersonIdType personIdTypeInstance) {
        if (personIdTypeInstance == null) {
            notFound()
            return
        }

        if (personIdTypeInstance.hasErrors()) {
            render model: [personIdTypeInstance: personIdTypeInstance], view: 'create'
            return
        }

        personIdTypeInstance.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'personIdType.label', default: 'PersonIdType'), personIdTypeInstance.id])
                redirect personIdTypeInstance
            }
            '*' { respond personIdTypeInstance, [status: CREATED] }
        }
    }

    def edit(PersonIdType personIdTypeInstance) {
        respond personIdTypeInstance
    }

    @Transactional
    def update(PersonIdType personIdTypeInstance) {
        if (personIdTypeInstance == null) {
            notFound()
            return
        }

        if (personIdTypeInstance.hasErrors()) {
            respond personIdTypeInstance.errors, view:'edit'
            return
        }

        personIdTypeInstance.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'PersonIdType.label', default: 'PersonIdType'), personIdTypeInstance.id])
                redirect personIdTypeInstance
            }
            '*'{ respond personIdTypeInstance, [status: OK] }
        }
    }

    @Transactional
    def delete(PersonIdType personIdTypeInstance) {

        if (personIdTypeInstance == null) {
            notFound()
            return
        }

        personIdTypeInstance.delete flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'PersonIdType.label', default: 'PersonIdType'), personIdTypeInstance.id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'personIdType.label', default: 'PersonIdType'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
