def compo = '''<data xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" archetype_node_id="openEHR-EHR-COMPOSITION.signos.v1" xmlns="http://schemas.openehr.org/v1" xsi:type="COMPOSITION">
    <name>
      <value>Signos vitales</value>
    </name>
    <archetype_details>
      <archetype_id>
        <value>openEHR-EHR-COMPOSITION.signos.v1</value>
      </archetype_id>
      <template_id>
        <value>Signos</value>
      </template_id>
      <rm_version>1.0.2</rm_version>
    </archetype_details>
</data>'''

def root = new XmlParser().parseText(compo)

println root.getClass()

root.name + {uid(1234)}

new XmlNodePrinter().print(root) // GPathResult(GPathResult parent, String name, String namespacePrefix, Map<String,String> namespaceTagHints)