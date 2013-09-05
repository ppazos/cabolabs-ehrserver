package ehr.clinical_documents

import org.springframework.dao.DataIntegrityViolationException
import com.cabolabs.archetype.*

class DataIndexController {

    //static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        [dataIndexInstanceList: DataIndex.list(params), dataIndexInstanceTotal: DataIndex.count()]
    }

    /* DataIndex is not creatable, is generated
    def create() {
        [dataIndexInstance: new DataIndex(params)]
    }

    def save() {
        def dataIndexInstance = new DataIndex(params)
        if (!dataIndexInstance.save(flush: true)) {
            render(view: "create", model: [dataIndexInstance: dataIndexInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'dataIndex.label', default: 'DataIndex'), dataIndexInstance.id])
        redirect(action: "show", id: dataIndexInstance.id)
    }
    */

    def show(Long id) {
        def dataIndexInstance = DataIndex.get(id)
        if (!dataIndexInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'dataIndex.label', default: 'DataIndex'), id])
            redirect(action: "list")
            return
        }

        [dataIndexInstance: dataIndexInstance]
    }

    /* DataIndex is not editable, is generated
    def edit(Long id) {
        def dataIndexInstance = DataIndex.get(id)
        if (!dataIndexInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'dataIndex.label', default: 'DataIndex'), id])
            redirect(action: "list")
            return
        }

        [dataIndexInstance: dataIndexInstance]
    }

    def update(Long id, Long version) {
        def dataIndexInstance = DataIndex.get(id)
        if (!dataIndexInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'dataIndex.label', default: 'DataIndex'), id])
            redirect(action: "list")
            return
        }

        if (version != null) {
            if (dataIndexInstance.version > version) {
                dataIndexInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'dataIndex.label', default: 'DataIndex')] as Object[],
                          "Another user has updated this DataIndex while you were editing")
                render(view: "edit", model: [dataIndexInstance: dataIndexInstance])
                return
            }
        }

        dataIndexInstance.properties = params

        if (!dataIndexInstance.save(flush: true)) {
            render(view: "edit", model: [dataIndexInstance: dataIndexInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'dataIndex.label', default: 'DataIndex'), dataIndexInstance.id])
        redirect(action: "show", id: dataIndexInstance.id)
    }
    
    def delete(Long id) {
        def dataIndexInstance = DataIndex.get(id)
        if (!dataIndexInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'dataIndex.label', default: 'DataIndex'), id])
            redirect(action: "list")
            return
        }

        try {
            dataIndexInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'dataIndex.label', default: 'DataIndex'), id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'dataIndex.label', default: 'DataIndex'), id])
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
       def indexes = DataIndex.list()
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
