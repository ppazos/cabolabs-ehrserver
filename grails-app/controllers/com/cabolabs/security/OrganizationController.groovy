package com.cabolabs.security



import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

@Transactional(readOnly = true)
class OrganizationController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond Organization.list(params), model:[organizationInstanceCount: Organization.count()]
    }

    def show(Organization organizationInstance) {
        respond organizationInstance
    }

    def create() {
        respond new Organization(params)
    }

    @Transactional
    def save(Organization organizationInstance) {
        if (organizationInstance == null) {
            notFound()
            return
        }

        if (organizationInstance.hasErrors()) {
            respond organizationInstance.errors, view:'create'
            return
        }

        organizationInstance.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'organization.label', default: 'Organization'), organizationInstance.id])
                redirect organizationInstance
            }
            '*' { respond organizationInstance, [status: CREATED] }
        }
    }

    def edit(Organization organizationInstance) {
        respond organizationInstance
    }

    @Transactional
    def update(Organization organizationInstance) {
        if (organizationInstance == null) {
            notFound()
            return
        }

        if (organizationInstance.hasErrors()) {
            respond organizationInstance.errors, view:'edit'
            return
        }

        organizationInstance.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'Organization.label', default: 'Organization'), organizationInstance.id])
                redirect organizationInstance
            }
            '*'{ respond organizationInstance, [status: OK] }
        }
    }

    @Transactional
    def delete(Organization organizationInstance) {

        if (organizationInstance == null) {
            notFound()
            return
        }

        organizationInstance.delete flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'Organization.label', default: 'Organization'), organizationInstance.id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'organization.label', default: 'Organization'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
