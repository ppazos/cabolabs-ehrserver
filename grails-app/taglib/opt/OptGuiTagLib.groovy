// copied from https://github.com/ppazos/openEHR-OPT-GUI/blob/master/grails-app/taglib/com/cabolabs/openehr/opt/OptGuiTagLib.groovy
package opt

import com.cabolabs.openehr.opt.model.*

class OptGuiTagLib {

    //static defaultEncodeAs = [taglib:'html']
//    static defaultEncodeAs = [taglib:'raw']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]

    // icon for each IM type
    // TODO: more data values
    static typeIcon = [
      'FOLDER':         'folder-open',
      'COMPOSITION':    'file-text-o',
      'EVENT_CONTEXT':  'info',
      'SECTION':        'chevron-left',
      'OBSERVATION':    'eye',
      'EVALUATION':     'refresh',
      'INSTRUCTION':    'paper-plane',
      'ACTION':         'flash',
      'ADMIN_ENTRY':    'gear',
      'HISTORY':        'list-alt',
      'EVENT':          'calendar',
      'POINT_EVENT':    'calendar',
      'INTERVAL_EVENT': 'calendar',
      'ITEM_TREE':     'sitemap',
      'ITEM_TABLE':    'table',
      'ITEM_LIST':     'list',
      'ITEM_SINGLE':   'circle-o',
      'CLUSTER':       'sitemap',
      'ELEMENT':       'pencil',
      'DV_DATE':       'calendar',
      'DV_DATE_TIME':  'calendar',
      'DV_TEXT':       'font',
      'DV_CODED_TEXT': 'list-ul'
    ]

    // attributes of the IM that are not in the OPT
    static typeIMAttributes = [:] // TODO

    // names for the IM attributes (in OPT or not in OPT) that don't have a nodeId
    // so those don't have a term in the ontology.
    // TODO: I18N
    /*
    static typeIMAttributeName = [
      'COMPOSITION': [
        '/category': 'Category'
      ]
    ]
    */

    // User from datatypes to assign names to internal attributes
    // TODO: complete, and I18N
    static typeIMAttributeNameEndsWith = [
      '/defining_code': 'Defining Code',
      '/value': 'Value',
      '/category': 'Category'
    ]

    private def traverse(ObjectNode o, body, definition)
    {
       //println " ".multiply(pad) + o.rmTypeName.padRight(35-pad, '.') + (o.archetypeId ?: o.path)
       out << '<div class="'+ o.rmTypeName +'">'
       //out << (o.archetypeId ?: o.path)


       // changes to the object root to get the terminology term text
       // all terms are on the root nodes
       if (o.archetypeId) definition = o


       def s = ''
       if (typeIcon[o.rmTypeName])
       {
          s = $/<span class="fa-stack">
          <i class="fa fa-circle fa-stack-2x"></i>
          <i class="fa fa-${typeIcon[o.rmTypeName]} fa-stack-1x fa-inverse" aria-hidden="true"></i>
          </span>/$
       }
       s = '<h3>'+ s + o.rmTypeName +'<i class="fa fa-caret-up" aria-hidden="true"></i><i class="fa fa-caret-down" aria-hidden="true"></i></h3>'

       row([], { s })


       if (o.nodeId)
       {
          row([], {
            $/<label class="col-sm-2 control-label">Text</label>
            <div class="col-sm-10">${definition.getText(o.nodeId)} (${o.nodeId})</div>/$
          })
          row([], {
            $/<label class="col-sm-2 control-label">Description</label>
            <div class="col-sm-10">${definition.getDescription(o.nodeId)}</div>/$
          })
       }
       else
       {
          def entry = typeIMAttributeNameEndsWith.find { o.path.endsWith(it.key) }
          if (entry)
          {
             row([], {
               $/<label class="col-sm-2 control-label">Text</label>
               <div class="col-sm-10">${entry.value}</div>/$
             })
          }
          /* TODO: cant get the parent object right now
           https://github.com/ppazos/openEHR-OPT/issues/30
          else
          {
             if (typeIMAttributeName[o.parent.rmTypeName] && typeIMAttributeName[o.parent.rmTypeName][o.path])
             {
                out << '<div>'
                out << typeIMAttributeName[o.parent.rmTypeName][o.path]
                out << '</div>'
             }
          }
          */
       }

       if (o.archetypeId)
       {
          row([], {
             $/<label class="col-sm-2 control-label">Archetype ID</label>
             <div class="col-sm-10">${o.archetypeId}</div>/$
          })
       }

       // paths
       row([], {
          $/<label class="col-sm-2 control-label">Local Path</label>
           <div class="col-sm-10">${o.path}</div>/$
       })
       row([], {
          $/<label class="col-sm-2 control-label">Absolute Path</label>
          <div class="col-sm-10">${o.templatePath}</div>/$
       })

       // is slot?
       if (o.type == 'ARCHETYPE_SLOT')
       {
         row([], {
            $/<label class="col-sm-2 control-label">Unresolved slot to</label>
            <div class="col-sm-10">${o.includes}</div>/$
         })
       }

       o.attributes.each{
          traverse(it, body, definition)
       }

       out << '</div>'
    }

    private def traverse(AttributeNode a, body, definition)
    {
       //println " ".multiply(pad) + a.rmAttributeName

       a.children.each{
          traverse(it, body, definition)
       }
    }

    def row = { attrs, body ->

       out << '<div class="row">'
       out << body()
       out << '</div>'
    }

    def displayOPTTree = { attrs, body ->

        traverse(attrs.opt.definition, body, attrs.opt.definition)
    }

    def displayOPTNodes = { attrs, body ->

        attrs.opt.nodes.sort{it.key}.each { path, node ->

          out << '<div>'
          out << path
          out << '</div>'
        }
    }

    def displayOPT = { attrs, body ->

       def opt = attrs.opt

       row([], {
          $/<label class="col-sm-2 control-label">UID</label>
           <div class="col-sm-10">${opt.uid}</div>/$
       })

       row([], {
          $/<label class="col-sm-2 control-label">Template ID</label>
          <div class="col-sm-10">${opt.templateId}</div>/$
       })

       row([], {
          $/<label class="col-sm-2 control-label">Concept</label>
          <div class="col-sm-10">${opt.concept}</div>/$
       })

       row([], {
          $/<label class="col-sm-2 control-label">Language</label>
          <div class="col-sm-10">${opt.language}</div>/$
       })

       row([], {
          $/<label class="col-sm-2 control-label">Purpose</label>
          <div class="col-sm-10">${opt.purpose}</div>/$
       })
    }
}
