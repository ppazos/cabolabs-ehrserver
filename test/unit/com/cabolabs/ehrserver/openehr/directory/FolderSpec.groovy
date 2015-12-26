package com.cabolabs.ehrserver.openehr.directory

import com.cabolabs.ehrserver.openehr.directory.Folder
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Folder)
class FolderSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "create single folder should validate"()
    {
       when: "create single folder"
       def folder = new Folder(name:"folder 1", organizationUid:"1234")
       
       then: "should validate"
       folder.validate()
    }
    
    void "create single folder should save"()
    {
       when: "create single folder"
       def folder = new Folder(name:"folder 1", organizationUid:"1234")
       
       then: "should validate"
       folder.save()
    }
    
    void "create single folder should save and get"()
    {
       when: "create single folder"
       def folder = new Folder(name:"folder 1", organizationUid:"1234")
       folder.save()
       def folder_get = Folder.get(1)
       
       then: "should validate"
       folder_get.name == folder.name
    }
    
    void "create folder with sub-folders"()
    {
       when: "create folder tree"
       def folder = new Folder(name:"root", organizationUid:"1234")
       folder.folders = [
          new Folder(name:"child 1", organizationUid:"1234"),
          new Folder(name:"child 2", organizationUid:"1234"),
          new Folder(name:"child 3", organizationUid:"1234"),
       ]
       
       folder.save()
       
       def folder_get = Folder.get(1)
       
       then: "should get structure saved tree"
       folder_get.folders.size() == 3
       Folder.count() == 4
    }
    
    void "create folder tree, access parent"()
    {
       when: "create folder tree"
       def folder = new Folder(name:"root", organizationUid:"1234")
       def subfolders = [
          new Folder(name:"child 1", organizationUid:"1234"),
          new Folder(name:"child 2", organizationUid:"1234"),
          new Folder(name:"child 3", organizationUid:"1234"),
       ]
       subfolders.each { folder.addToFolders(it) } // addTo sets the backlink to parent
       
       folder.save(flush:true) // without flish doesn't asve for the folders to be available in then
       def folder_child_get = Folder.get(2)
       
       then: "should get the parent"
       Folder.count() == 4
       folder_child_get.parent.id == 1
       folder_child_get.parent.name == "root"
    }
}
