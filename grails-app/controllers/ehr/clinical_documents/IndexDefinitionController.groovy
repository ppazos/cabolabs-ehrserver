package ehr.clinical_documents

import org.springframework.dao.DataIntegrityViolationException
import com.cabolabs.archetype.*

class IndexDefinitionController {

    //static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        [indexDefinitionInstanceList: IndexDefinition.list(params), indexDefinitionInstanceTotal: IndexDefinition.count()]
    }

    /* IndexDefinition is not creatable, is generated
    def create() {
        [indexDefinitionInstance: new IndexDefinition(params)]
    }

    def save() {
        def indexDefinitionInstance = new IndexDefinition(params)
        if (!indexDefinitionInstance.save(flush: true)) {
            render(view: "create", model: [indexDefinitionInstance: indexDefinitionInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'indexDefinition.label', default: 'IndexDefinition'), indexDefinitionInstance.id])
        redirect(action: "show", id: indexDefinitionInstance.id)
    }
    */

    def show(Long id) {
        def indexDefinitionInstance = IndexDefinition.get(id)
        if (!indexDefinitionInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'indexDefinition.label', default: 'IndexDefinition'), id])
            redirect(action: "list")
            return
        }

        [indexDefinitionInstance: indexDefinitionInstance]
    }

    /* IndexDefinition is not editable, is generated
    def edit(Long id) {
        def indexDefinitionInstance = IndexDefinition.get(id)
        if (!indexDefinitionInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'indexDefinition.label', default: 'IndexDefinition'), id])
            redirect(action: "list")
            return
        }

        [indexDefinitionInstance: indexDefinitionInstance]
    }

    def update(Long id, Long version) {
        def indexDefinitionInstance = IndexDefinition.get(id)
        if (!indexDefinitionInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'indexDefinition.label', default: 'IndexDefinition'), id])
            redirect(action: "list")
            return
        }

        if (version != null) {
            if (indexDefinitionInstance.version > version) {
                indexDefinitionInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'indexDefinition.label', default: 'IndexDefinition')] as Object[],
                          "Another user has updated this IndexDefinition while you were editing")
                render(view: "edit", model: [indexDefinitionInstance: indexDefinitionInstance])
                return
            }
        }

        indexDefinitionInstance.properties = params

        if (!indexDefinitionInstance.save(flush: true)) {
            render(view: "edit", model: [indexDefinitionInstance: indexDefinitionInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'indexDefinition.label', default: 'IndexDefinition'), indexDefinitionInstance.id])
        redirect(action: "show", id: indexDefinitionInstance.id)
    }
    
    def delete(Long id) {
        def indexDefinitionInstance = IndexDefinition.get(id)
        if (!indexDefinitionInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'indexDefinition.label', default: 'IndexDefinition'), id])
            redirect(action: "list")
            return
        }

        try {
            indexDefinitionInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'indexDefinition.label', default: 'IndexDefinition'), id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'indexDefinition.label', default: 'IndexDefinition'), id])
            redirect(action: "show", id: id)
        }
    }
    */
    
    /**
     * (re)generates indexes for all archetypes in the repo.
     * This is usefull to add archetypes to the repo and index them to generate new queries.    
     */
    def generate() {
    
       def manager = ArchetypeManager.getInstance()
       def archetypes = manager.getArchetypes('composition', '.*')
       def ai = new ArchetypeIndexer()
       
       // FIXME: just reindex if there are no indexes defined for the archetype
       
       // delete current
       def indexes = IndexDefinition.list()
       indexes.each {
         it.delete()
       }
       
       // reindex the whole repo
       archetypes.each { archetype ->
          ai.index(archetype)
       }
       
       redirect(action: "list")
    }
}
