package ehr

class OperationalTemplateController {

    def index()
    {   
       render "hola mundo!"
    }
    
    def list()
    {
       return [opts: ehr.clinical_documents.OperationalTemplateIndex.list(),
               total: ehr.clinical_documents.OperationalTemplateIndex.count()]
    }
}
