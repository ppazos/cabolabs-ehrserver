package common.change_control

import org.springframework.dao.DataIntegrityViolationException

class ContributionController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max)
	{
        params.max = Math.min(max ?: 10, 100)
		
		def lst = Contribution.list(params)
		def cnt = Contribution.count()
		
        return [contributionInstanceList: lst,
		        contributionInstanceTotal: cnt]
    }

    def create() {
        [contributionInstance: new Contribution(params)]
    }

    def save() {
        def contributionInstance = new Contribution(params)
        if (!contributionInstance.save(flush: true)) {
            render(view: "create", model: [contributionInstance: contributionInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'contribution.label', default: 'Contribution'), contributionInstance.id])
        redirect(action: "show", id: contributionInstance.id)
    }

    def show(Long id) {
        def contributionInstance = Contribution.get(id)
        if (!contributionInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'contribution.label', default: 'Contribution'), id])
            redirect(action: "list")
            return
        }

        [contributionInstance: contributionInstance]
    }

    def edit(Long id) {
        def contributionInstance = Contribution.get(id)
        if (!contributionInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'contribution.label', default: 'Contribution'), id])
            redirect(action: "list")
            return
        }

        [contributionInstance: contributionInstance]
    }

    def update(Long id, Long version) {
        def contributionInstance = Contribution.get(id)
        if (!contributionInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'contribution.label', default: 'Contribution'), id])
            redirect(action: "list")
            return
        }

        if (version != null) {
            if (contributionInstance.version > version) {
                contributionInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'contribution.label', default: 'Contribution')] as Object[],
                          "Another user has updated this Contribution while you were editing")
                render(view: "edit", model: [contributionInstance: contributionInstance])
                return
            }
        }

        contributionInstance.properties = params

        if (!contributionInstance.save(flush: true)) {
            render(view: "edit", model: [contributionInstance: contributionInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'contribution.label', default: 'Contribution'), contributionInstance.id])
        redirect(action: "show", id: contributionInstance.id)
    }

    def delete(Long id) {
        def contributionInstance = Contribution.get(id)
        if (!contributionInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'contribution.label', default: 'Contribution'), id])
            redirect(action: "list")
            return
        }

        try {
            contributionInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'contribution.label', default: 'Contribution'), id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'contribution.label', default: 'Contribution'), id])
            redirect(action: "show", id: id)
        }
    }
}
