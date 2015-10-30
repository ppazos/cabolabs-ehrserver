package directory

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

import com.cabolabs.security.Organization
import grails.plugin.springsecurity.SpringSecurityUtils

@Transactional(readOnly = true)
class FolderController {

   def springSecurityService
   
    static allowedMethods = [save: "POST", update: "PUT"]

    def index(Integer max)
    {
        params.max = Math.min(max ?: 10, 100)
        
        def list, count
        
        // All folders for admins, filtered by org uid for other roles
        if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
        {
           list = Folder.list(params)
           count = Folder.count()
        }
        else
        {
           // auth token used to login
           def auth = springSecurityService.authentication
           def org = Organization.findByNumber(auth.organization)
           
           list = Folder.findAllByOrganizationUid(org.uid, params)
           count = Folder.countByOrganizationUid(org.uid)
        }
        
        
        respond list, model:[folderInstanceCount: count]
    }

    def show(Folder folderInstance)
    {
        respond folderInstance
    }

    def create()
    {
        respond new Folder(params)
    }

    @Transactional
    def save(Folder folderInstance)
    {
        if (folderInstance == null)
        {
            notFound()
            return
        }

        // admins can select the org uid, for other roles is the org used to login
        if (!SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
        {
           // auth token used to login
           def auth = springSecurityService.authentication
           def org = Organization.findByNumber(auth.organization)
           //println "org "+ org
           //println "org uid "+ org.uid
           
           folderInstance.organizationUid = org.uid
        }
        
        if (folderInstance.ehrId)
        {
           def ehr = ehr.Ehr.get(folderInstance.ehrId)
           ehr.directory = folderInstance
           ehr.save() 
        }
        
        folderInstance.save flush:true

        
        
        
        if (folderInstance.hasErrors())
        {
            respond folderInstance, view:'create'
            return
        }
        
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'folder.label', default: 'Folder'), folderInstance.id])
                redirect folderInstance
            }
            '*' { respond folderInstance, [status: CREATED] }
        }
    }

    def edit(Folder folderInstance)
    {
        respond folderInstance
    }

    @Transactional
    def update(Folder folderInstance)
    {
        if (folderInstance == null) {
            notFound()
            return
        }

        if (folderInstance.hasErrors()) {
            respond folderInstance.errors, view:'edit'
            return
        }

        folderInstance.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'Folder.label', default: 'Folder'), folderInstance.id])
                redirect folderInstance
            }
            '*'{ respond folderInstance, [status: OK] }
        }
    }

    @Transactional
    def delete(Folder folderInstance)
    {

        if (folderInstance == null)
        {
            notFound()
            return
        }

        folderInstance.delete flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'Folder.label', default: 'Folder'), folderInstance.id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound()
    {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'folder.label', default: 'Folder'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
    
    @Transactional
    def addItems(Long id)
    {
       println params
       
       List vouids = params.list('versioned_object_uids')
       
       println vouids
       
       def folder = Folder.get(id)
       
       vouids.each {
          if (!folder.items.contains(it)) // avoid adding the same item twice
          {
             folder.items.add(it) // addToItems dont work over simple type hasMany
          }
       }
       
       if (!folder.save(flush:true)) println folder.errors
       
       // FIXME
       render "ok"
    }
}
