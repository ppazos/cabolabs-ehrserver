package ehr.clinical_documents

import org.springframework.dao.DataIntegrityViolationException

class CompositionIndexController {

    //static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        [compositionIndexInstanceList: CompositionIndex.list(params), compositionIndexInstanceTotal: CompositionIndex.count()]
    }

    /* CompositionIndexes are created automatically on commit
    def create() {
        [compositionIndexInstance: new CompositionIndex(params)]
    }

    def save() {
        def compositionIndexInstance = new CompositionIndex(params)
        if (!compositionIndexInstance.save(flush: true)) {
            render(view: "create", model: [compositionIndexInstance: compositionIndexInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'compositionIndex.label', default: 'CompositionIndex'), compositionIndexInstance.id])
        redirect(action: "show", id: compositionIndexInstance.id)
    }
    */

    def show(Long id) {
        def compositionIndexInstance = CompositionIndex.get(id)
        if (!compositionIndexInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'compositionIndex.label', default: 'CompositionIndex'), id])
            redirect(action: "list")
            return
        }

        [compositionIndexInstance: compositionIndexInstance]
    }

    /* CompositionIndexes are not editable
    def edit(Long id) {
        def compositionIndexInstance = CompositionIndex.get(id)
        if (!compositionIndexInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'compositionIndex.label', default: 'CompositionIndex'), id])
            redirect(action: "list")
            return
        }

        [compositionIndexInstance: compositionIndexInstance]
    }

    def update(Long id, Long version) {
        def compositionIndexInstance = CompositionIndex.get(id)
        if (!compositionIndexInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'compositionIndex.label', default: 'CompositionIndex'), id])
            redirect(action: "list")
            return
        }

        if (version != null) {
            if (compositionIndexInstance.version > version) {
                compositionIndexInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'compositionIndex.label', default: 'CompositionIndex')] as Object[],
                          "Another user has updated this CompositionIndex while you were editing")
                render(view: "edit", model: [compositionIndexInstance: compositionIndexInstance])
                return
            }
        }

        compositionIndexInstance.properties = params

        if (!compositionIndexInstance.save(flush: true)) {
            render(view: "edit", model: [compositionIndexInstance: compositionIndexInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'compositionIndex.label', default: 'CompositionIndex'), compositionIndexInstance.id])
        redirect(action: "show", id: compositionIndexInstance.id)
    }

    def delete(Long id) {
        def compositionIndexInstance = CompositionIndex.get(id)
        if (!compositionIndexInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'compositionIndex.label', default: 'CompositionIndex'), id])
            redirect(action: "list")
            return
        }

        try {
            compositionIndexInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'compositionIndex.label', default: 'CompositionIndex'), id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'compositionIndex.label', default: 'CompositionIndex'), id])
            redirect(action: "show", id: id)
        }
    }
    */
}
