<?xml version="1.0" encoding="UTF-8"?>
<!-- SEE LICENSE INFORMATION AT END OF FILE -->

<!DOCTYPE xsl:stylesheet[<!ENTITY nbsp "&#160;" >]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:oe="http://schemas.openehr.org/v1" xmlns:msxsl="urn:schemas-microsoft-com:xslt" >


  <!-- Specify output, create matches for unwanted top-level elements -->
  <xsl:output encoding="UTF-8" doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
    doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" indent="yes"/>

  <!-- templates to deal with rendering versions (ignored for locatable rendering) -->
  <xsl:template match="oe:contribution"/>
  <xsl:template match="oe:commit_audit"/>
  <xsl:template match="oe:uid"/>
  <xsl:template match="oe:preceding_version_uid"/>
  <xsl:template match="oe:lifecycle_state"/>
  <xsl:template match="oe:is_merged"/>
  <xsl:template match="oe:version">
    <!-- top-level match for version -->
    <xsl:apply-templates select="oe:data"/>
  </xsl:template>


  <!-- .............. Start helper templates .................
    These are for element names commonly in
    XML instances with consistent semantic meaning
    or simply special-purpose templates for common
    operations on particular types of nodes.  -->

  <!-- Generic name
        Uses generic-dv-text -->
  <xsl:template name="generic-name">
    <xsl:param name="namenode"/>
    <xsl:param name="namecat"/>
    <xsl:choose>
      <xsl:when test="$namenode/oe:value != ''">
        <xsl:call-template name="generic-DV_TEXT">
          <xsl:with-param name="textnode" select="$namenode"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise> Unnamed <xsl:value-of select="$namecat"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Generic date
        (in format is '2006-05-05T10:44:46'; out format is '05/05/2006') -->
  <xsl:template name="generic-date">
    <xsl:param name="datetimestring"/>
    <xsl:choose>
      <!-- datetime format 1 -->
      <xsl:when test="not(substring($datetimestring, 5,1) = '-')">
        <xsl:if test="not(substring($datetimestring, 5,2) = '')">
          <xsl:if test="not(substring($datetimestring, 7,2) = '')">
            <xsl:value-of select="substring($datetimestring, 7,2)"/>/</xsl:if>
          <xsl:value-of select="substring($datetimestring, 5,2)"/>/</xsl:if>
        <xsl:value-of select="substring($datetimestring, 1,4)"/>
      </xsl:when>
      
      <!-- datetime format 2 -->
      <xsl:otherwise>
        <xsl:if test="not(substring($datetimestring, 6,2) = '')">
          <xsl:if test="not(substring($datetimestring, 9,2) = '')">
            <xsl:value-of select="substring($datetimestring, 9,2)"/>/</xsl:if>
          <xsl:value-of select="substring($datetimestring, 6,2)"/>/</xsl:if>
        <xsl:value-of select="substring($datetimestring, 1,4)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Generic time
        (in format is '2006-05-05T10:44:46'; out format is '10:44 AM +9:30') -->
  <xsl:template name="generic-time">
    <xsl:param name="datetimestring"/>
    <xsl:param name="display-as-24-hour"/>
    <xsl:param name="show-seconds"/>
    <xsl:param name="show-timezone"/>

    <xsl:choose>
      
      <!-- datetime format 1 -->
      <xsl:when test="not(substring($datetimestring, 5,1) = '-')">
        <xsl:choose>
          <xsl:when test="substring($datetimestring, 9,1) = 'T'">
            <xsl:if test="not($display-as-24-hour)">
              <xsl:choose>
                <xsl:when test="substring($datetimestring, 10,2) &lt; '01'">12</xsl:when>
                <xsl:when test="substring($datetimestring, 10,2) &gt; '12'"><xsl:value-of select="number(substring($datetimestring, 10,2))-12"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="substring($datetimestring, 10,2)"/></xsl:otherwise>
              </xsl:choose>
            </xsl:if>
            <xsl:if test="substring($datetimestring, 12,2)">:<xsl:value-of select="substring($datetimestring, 12,2)"/>
              <xsl:if test="substring($datetimestring, 14,2) and $show-seconds='yes'">:<xsl:value-of select="substring($datetimestring, 14,2)"/></xsl:if>                
            </xsl:if>
          </xsl:when>
          <xsl:otherwise>
            <xsl:choose>
              <xsl:when test="not($display-as-24-hour)">
                <xsl:choose>
                  <xsl:when test="substring($datetimestring, 9,2) = '00'">12</xsl:when>
                  <xsl:when test="substring($datetimestring, 9,2) &gt; '12' or substring($datetimestring, 9,2) &lt; '01'"><xsl:value-of select="number(substring($datetimestring, 9,2))-12"/></xsl:when>
                  <xsl:otherwise><xsl:value-of select="substring($datetimestring, 9,2)"/></xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              <xsl:otherwise><xsl:value-of select="substring($datetimestring, 9,2)"/></xsl:otherwise>
            </xsl:choose>
            <xsl:if test="substring($datetimestring, 11,2)">:<xsl:value-of select="substring($datetimestring, 11,2)"/>
              <xsl:if test="substring($datetimestring, 13,2) and $show-seconds='yes'">:<xsl:value-of select="substring($datetimestring, 13,2)"/>
              </xsl:if>                            
            </xsl:if>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="not($display-as-24-hour)">
          <xsl:choose>
            <xsl:when test="substring($datetimestring, 10,2) &gt;= '12'">&nbsp;PM</xsl:when>
            <xsl:otherwise>&nbsp;AM</xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </xsl:when>
      
      <!-- datetime format 2 -->
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="not($display-as-24-hour)">
            <xsl:choose>
              <xsl:when test="substring($datetimestring, 12,2) = '00'">12</xsl:when>
              <xsl:when test="substring($datetimestring, 12,2) &gt; '12' "><xsl:value-of select="number(substring($datetimestring, 12,2))-12"/></xsl:when>
              <xsl:otherwise><xsl:value-of select="substring($datetimestring, 12,2)"/></xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise><xsl:value-of select="substring($datetimestring, 12,2)"/></xsl:otherwise>
        </xsl:choose><xsl:if test="substring($datetimestring, 15,2)">:<xsl:value-of select="substring($datetimestring, 15,2)"/>
          <xsl:if test="substring($datetimestring, 18,2) and $show-seconds='yes'">:<xsl:value-of select="substring($datetimestring, 18,2)"/></xsl:if>
        </xsl:if>
        <xsl:if test="not($display-as-24-hour)">
          <xsl:choose>
            <xsl:when test="substring($datetimestring, 12,2) &gt;= '12'">&nbsp;PM</xsl:when>
            <xsl:otherwise>&nbsp;AM</xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
    
    <!-- display timezone -->
    <xsl:if test="$show-timezone='yes'">
      <xsl:variable select="substring($datetimestring, 11)" name="tz-substring"/>
      <xsl:if test="(contains($tz-substring, '+') or contains($tz-substring, '-')) and not(substring($tz-substring, string-length($tz-substring)-4) = '00:00') and not(substring($tz-substring, string-length($tz-substring)-2) = '+00') and not(substring($tz-substring, string-length($tz-substring)-2) = '-00')">
        <xsl:choose>
          <xsl:when test="contains($tz-substring, '+')">&nbsp;+<xsl:variable select="substring-after($tz-substring, '+')" name="tz-string"/>
            <xsl:value-of select="substring($tz-string, 1, 2)"/>
            <xsl:if test="string-length($tz-string) > 3">:<xsl:choose>                      
              <xsl:when test="contains($tz-string, ':')">
                <xsl:value-of select="substring($tz-string, 4, 2)"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="substring($tz-string, 3, 2)"/>
              </xsl:otherwise>
            </xsl:choose>
            </xsl:if>
          </xsl:when>
          <xsl:otherwise>&nbsp;-<xsl:variable select="substring-after($tz-substring, '-')" name="tz-string"/>
            <xsl:value-of select="substring($tz-string, 1, 2)"/>
            <xsl:if test="string-length($tz-string) > 3">:<xsl:choose>                      
              <xsl:when test="contains($tz-string, ':')">
                <xsl:value-of select="substring($tz-string, 4, 2)"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="substring($tz-string, 3, 2)"/>
              </xsl:otherwise>
            </xsl:choose>
            </xsl:if>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <!-- Generic COMPOSITION header -->
  <xsl:template name="composition-header">
    <xsl:param name="compositionnode"/>

    <!-- Output the composition heading -->
    <div class="composition-header">
      <table width="100%" cellpadding="1" cellspacing="0">
        <tr>
          <td align="left" valign="middle" rowspan="2">
            <h1 class="composition-title">
              <xsl:call-template name="generic-name">
                <xsl:with-param name="namenode" select="$compositionnode/oe:name"/>
                <xsl:with-param name="namecat">clinical record</xsl:with-param>
              </xsl:call-template>
            </h1>
          </td>
          <td align="right" valign="bottom">
            <!-- healthcare facility -->
            <xsl:if test="oe:context/oe:health_care_facility"> Facility: <xsl:value-of
                select="oe:context/oe:health_care_facility/oe:name"/>
            </xsl:if>
          </td>
        </tr>
        <tr>
          <td align="right" valign="top">
            <xsl:choose>
              <xsl:when test="$compositionnode/oe:composer[@xsi:type='PARTY_IDENTIFIED']"> Reported
                by: <xsl:value-of select="$compositionnode/oe:composer/oe:name"/>
              </xsl:when>
              <xsl:when test="$compositionnode/oe:composer[@xsi:type='PARTY_RELATED']"> Reported by:
                  <xsl:value-of select="$compositionnode/oe:composer/oe:name"/>
              </xsl:when>
              <xsl:when test="$compositionnode/oe:composer[@xsi:type='PARTY_SELF']"> Reported by:
                subject </xsl:when>
            </xsl:choose>
          </td>
        </tr>
      </table>

      <!-- add context if present -->
      <xsl:if test="$compositionnode/oe:context">
        <xsl:call-template name="composition-header-context">
          <xsl:with-param name="contextnode" select="$compositionnode/oe:context"/>
        </xsl:call-template>
      </xsl:if>
    </div>
  </xsl:template>

  <xsl:template name="composition-header-context">
    <xsl:param name="contextnode"/>
    <table width="100%" cellpadding="1" cellspacing="0">
      <tr>
        <!-- report identifier -->
        <td align="left" valign="top" class="composition-context">
          <xsl:choose>
            <xsl:when
              test="$contextnode/oe:other_context/oe:items/oe:items[starts-with(@archetype_node_id,'at0007')]">
              <xsl:choose>
                <xsl:when
                  test="$contextnode/oe:other_context/oe:items/oe:items[starts-with(@archetype_node_id,'at0007')]/oe:name">
                  <xsl:call-template name="generic-DV_TEXT">
                    <xsl:with-param name="textnode"
                      select="$contextnode/oe:other_context/oe:items/oe:items[starts-with(@archetype_node_id,'at0007')]/oe:name"
                    />
                  </xsl:call-template>: </xsl:when>
                <xsl:otherwise>Report ID:</xsl:otherwise>
              </xsl:choose>
              <xsl:call-template name="generic-DV_TEXT">
                <xsl:with-param name="textnode"
                  select="$contextnode/oe:other_context/oe:items/oe:items[starts-with(@archetype_node_id,'at0007')]/oe:value"
                />
              </xsl:call-template>
            </xsl:when>
            <xsl:otherwise> &nbsp; </xsl:otherwise>
          </xsl:choose>
        </td>

        <!-- report start time -->
        <td align="right" valign="top"> On: <xsl:call-template name="generic-date">
            <xsl:with-param name="datetimestring" select="$contextnode/oe:start_time/oe:value"/>
          </xsl:call-template>&nbsp; <xsl:call-template name="generic-time">
            <xsl:with-param name="datetimestring" select="$contextnode/oe:start_time/oe:value"/>
          </xsl:call-template>
        </td>
      </tr>
    </table>
    <!-- ignore location and setting-->
    <!-- * participations ignored -->
  </xsl:template>

  <!-- Generic ENTRY header -->
  <xsl:template name="generic-entry-header">
    <xsl:param name="entrynode"/>
    <xsl:param name="entrytype"/>
    <xsl:param name="nameoverride"/>
    <!-- name -->
    <xsl:choose>
      <xsl:when test="$nameoverride and $nameoverride='yes'">
        <b>
          <xsl:value-of select="$entrytype"/>
        </b>
      </xsl:when>
      <xsl:otherwise>
        <b>
          <xsl:call-template name="generic-name">
            <xsl:with-param name="namenode" select="$entrynode/oe:name"/>
            <xsl:with-param name="namecat" select="$entrytype"/>
          </xsl:call-template>
        </b>
      </xsl:otherwise>
    </xsl:choose>
    <!-- protocol -->
    <!--<xsl:if test="$entrynode/protocol"><br/>Protocol<xsl:call-template
                name="generic-ITEM_STRUCTURE">
                <xsl:with-param name="itemsnode" select="$entrynode/protocol"/>
            </xsl:call-template></xsl:if>-->
  </xsl:template>

  <!-- Replace new line template -->
  <xsl:template name="replaceNL">
    <xsl:param name="string"/>
    <xsl:choose>
      <xsl:when test="contains($string,'&#10;')">
        <xsl:value-of select="substring-before($string,'&#10;')"/>
        <br/>
        <xsl:call-template name="replaceNL">
          <xsl:with-param name="string" select="substring-after($string,'&#10;')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$string"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- .............. End helper templates ................. -->


  <!-- .............. Start openEHR class templates .............
        Every one of these templates corresponds to a
        class in openEHR reference model (openEHR v1.1 used). -->

  <!--Generic EVENT_CONTEXT-->
  <xsl:template name="generic-EVENT_CONTEXT">
    <xsl:param name="targetnode"/>

    <p style="margin:0px;">

      <!-- start and end times -->
      <xsl:choose>
        <xsl:when test="$targetnode/oe:end_time/oe:value"> Start time: <xsl:call-template
            name="generic-DV_DATE_TIME">
            <xsl:with-param name="datenode" select="$targetnode/oe:start_time"/>
          </xsl:call-template>
          <br/>End time: <xsl:call-template name="generic-DV_DATE_TIME">
            <xsl:with-param name="datenode" select="$targetnode/oe:end_time"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise> Date: <xsl:call-template name="generic-date">
          <xsl:with-param name="datetimestring" select="$targetnode/oe:start_time/oe:value"/>
          </xsl:call-template>
          <br/>Time: <xsl:call-template name="generic-time">
            <xsl:with-param name="datetimestring" select="$targetnode/oe:start_time/oe:value"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>

      <!-- healthcare facility -->
      <xsl:if test="$targetnode/oe:health_care_facility">
        <br/>Facility: <xsl:value-of select="$targetnode/oe:health_care_facility/oe:name"/>
      </xsl:if>

      <!-- location and setting-->
      <xsl:if test="$targetnode/oe:location">
        <br/>Location: <xsl:value-of select="$targetnode/oe:value"/>
      </xsl:if>
      <xsl:if test="$targetnode/oe:setting">
        <br/>Setting: <xsl:call-template name="generic-DV_CODED_TEXT">
          <xsl:with-param name="targetnode" select="$targetnode/oe:setting"/>
        </xsl:call-template>
      </xsl:if>


      <!-- * participations ignored -->

      <!-- other context -->
      <xsl:if test="$targetnode/oe:other_context">
        <xsl:call-template name="generic-ITEM_STRUCTURE">
          <xsl:with-param name="itemsnode" select="$targetnode/oe:other_context"/>
        </xsl:call-template>
      </xsl:if>
    </p>
  </xsl:template>

  <!-- Generic HISTORY -->
  <xsl:template name="generic-HISTORY">
    <xsl:param name="targetnode"/>

    <br/>Origin: <xsl:call-template name="generic-DV_DATE_TIME">
        <xsl:with-param name="datenode" select="$targetnode/oe:origin"/>
    </xsl:call-template>
    <xsl:if test="$targetnode/oe:period">
      <br/><xsl:value-of select="$targetnode/oe:period/name/value"/>:
      <xsl:call-template name="generic-DV_DURATION">
        <xsl:with-param name="durationnode" select="$targetnode/oe:period"/>
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="$targetnode/oe:duration">
      <br/><xsl:value-of select="$targetnode/oe:duration/name/value"/>:
      <xsl:call-template name="generic-DV_DURATION">
        <xsl:with-param name="durationnode" select="$targetnode/oe:duration"/>
      </xsl:call-template>
    </xsl:if>
    <xsl:for-each select="$targetnode/oe:summary">
      <xsl:call-template name="generic-ITEM_STRUCTURE">
        <xsl:with-param name="itemsnode" select="."/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="$targetnode/oe:events">
      <br/><xsl:call-template name="generic-EVENT">
        <xsl:with-param name="eventnode" select="."/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <!-- Generic ITEM_STRUCTURE -->
  <xsl:template name="generic-ITEM_STRUCTURE" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <xsl:param name="itemsnode"/>
    <xsl:choose>
      <!-- known item tree -->
      <xsl:when test="$itemsnode[@xsi:type = 'ITEM_TREE']">
        <xsl:if test="$itemsnode/oe:items">
          <xsl:call-template name="generic-ITEM_TREE">
            <xsl:with-param name="itemsnode" select="$itemsnode"/>
          </xsl:call-template>
        </xsl:if>
      </xsl:when>
      <!-- known item list -->
      <xsl:when test="$itemsnode[@xsi:type = 'ITEM_LIST']">
        <xsl:if test="$itemsnode/oe:items">
          <xsl:call-template name="generic-ITEM_LIST">
            <xsl:with-param name="itemsnode" select="$itemsnode"/>
          </xsl:call-template>
        </xsl:if>
      </xsl:when>
      <!-- known item table -->
      <xsl:when test="$itemsnode[@xsi:type = 'ITEM_TABLE']">
        <xsl:if test="$itemsnode/oe:items">
          <xsl:call-template name="generic-ITEM_TABLE">
            <xsl:with-param name="itemsnode" select="$itemsnode"/>
          </xsl:call-template>
        </xsl:if>
      </xsl:when>
      <!-- else we just assume single item -->
      <xsl:when test="$itemsnode[@xsi:type = 'ITEM_SINGLE']">
        <xsl:call-template name="generic-ITEM_SINGLE">
          <xsl:with-param name="itemsnode" select="$itemsnode"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise><span color="#f00;">Error displaying item structure: Could not detect concrete type.</span></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Generic ITEM_SINGLE -->
  <xsl:template name="generic-ITEM_SINGLE">
    <xsl:param name="itemsnode"/>
    <br/><xsl:call-template name="generic-ELEMENT">
      <xsl:with-param name="elementnode" select="$itemsnode/oe:item"/>
    </xsl:call-template>
  </xsl:template>
  
  <!-- Generic ITEM_TREE -->
  <xsl:template name="generic-ITEM_TREE">
    <xsl:param name="itemsnode"/>
    <xsl:for-each select="$itemsnode/oe:items">
      <xsl:call-template name="generic-ITEM">
        <xsl:with-param name="itemnode" select="."/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <!-- Generic ITEM_LIST -->
  <xsl:template name="generic-ITEM_LIST">
    <xsl:param name="itemsnode"/>
    <xsl:for-each select="$itemsnode/oe:items">
      <!-- in theory should be able to call generic-ELEMENT directly
                but not 100% sure this will work, depending on XML instance -->
      <xsl:call-template name="generic-ELEMENT">
        <xsl:with-param name="elementnode" select="."/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <!-- Generic ITEM_TABLE -->
  <xsl:template name="generic-ITEM_TABLE">
    <xsl:param name="itemsnode"/>
    <xsl:for-each select="$itemsnode/oe:columns">
      <xsl:call-template name="generic-CLUSTER">
        <xsl:with-param name="clusternode" select="."/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <!-- Generic ITEM
        If we have child "items" not null of xsi:type equal to cluster then calls
        generic cluster template, otherwise calls generic element template -->
  <xsl:template name="generic-ITEM">
    <xsl:param name="itemnode"/>
    <xsl:choose>
      <xsl:when test="$itemnode/oe:items or $itemnode[@xsi:type = 'CLUSTER']">
        <xsl:call-template name="generic-CLUSTER">
          <xsl:with-param name="clusternode" select="$itemnode"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="generic-ELEMENT">
          <xsl:with-param name="elementnode" select="$itemnode"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Generic CLUSTER -->
  <xsl:template name="generic-CLUSTER">
    <xsl:param name="clusternode"/>
    <blockquote style="margin-top:0px;margin-bottom:0px;">
      <xsl:call-template name="generic-name">
        <xsl:with-param name="namenode" select="$clusternode/oe:name"/>
        <xsl:with-param name="namecat">data cluster</xsl:with-param>
      </xsl:call-template>
      <xsl:for-each select="$clusternode/oe:items">
        <xsl:call-template name="generic-ITEM">
          <xsl:with-param name="itemnode" select="."/>
        </xsl:call-template>
      </xsl:for-each>
    </blockquote>
  </xsl:template>

  <!-- Generic ELEMENT -->
  <xsl:template name="generic-ELEMENT">
    <xsl:param name="elementnode"/>
    <blockquote style="margin-top:0px;margin-bottom:0px;">
      <xsl:call-template name="generic-name">
        <xsl:with-param name="namenode" select="$elementnode/oe:name"/>
        <xsl:with-param name="namecat">data item</xsl:with-param>
      </xsl:call-template>: <xsl:choose>
        <xsl:when test="$elementnode/oe:value">
          <xsl:call-template name="generic-DATA_VALUE">
            <xsl:with-param name="dvnode" select="$elementnode/oe:value"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <br/> ? </xsl:otherwise>
      </xsl:choose>
    </blockquote>
  </xsl:template>

  <!-- Generic DATA_VALUE -->
  <xsl:template name="generic-DATA_VALUE" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <xsl:param name="dvnode"/>
    <xsl:choose>
      <xsl:when test="$dvnode/attribute::xsi:type = 'DV_BOOLEAN'">
        <xsl:call-template name="generic-DV_BOOLEAN">
          <xsl:with-param name="boolnode" select="$dvnode"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$dvnode/attribute::xsi:type='DV_CODED_TEXT'">
        <xsl:call-template name="generic-DV_CODED_TEXT">
          <xsl:with-param name="targetnode" select="$dvnode"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$dvnode/attribute::xsi:type='DV_COUNT'">
        <xsl:call-template name="generic-DV_COUNT">
          <xsl:with-param name="targetnode" select="$dvnode"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$dvnode/attribute::xsi:type='DV_DATE_TIME'">
        <xsl:call-template name="generic-DV_DATE_TIME">
          <xsl:with-param name="datenode" select="$dvnode"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$dvnode/attribute::xsi:type='DV_DATE'">
        <xsl:call-template name="generic-DV_DATE">
          <xsl:with-param name="datenode" select="$dvnode"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$dvnode/attribute::xsi:type='DV_DURATION'">
        <xsl:call-template name="generic-DV_DURATION">
          <xsl:with-param name="durationnode" select="$dvnode"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$dvnode/attribute::xsi:type='DV_INTERVAL'">
        <xsl:call-template name="generic-DV_INTERVAL">
          <xsl:with-param name="targetnode" select="$dvnode"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$dvnode/attribute::xsi:type='DV_MULTIMEDIA'">
        <xsl:call-template name="generic-DV_MULTIMEDIA">
          <xsl:with-param name="targetnode" select="$dvnode"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$dvnode/attribute::xsi:type='DV_MULTI_MEDIA'">
        <xsl:call-template name="generic-DV_MULTIMEDIA">
          <xsl:with-param name="targetnode" select="$dvnode"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$dvnode/attribute::xsi:type='DV_ORDERED'">
        <xsl:call-template name="generic-DV_ORDERED">
          <xsl:with-param name="orderednode" select="$dvnode"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$dvnode/attribute::xsi:type='DV_ORDINAL'">
        <xsl:call-template name="generic-DV_ORDINAL">
          <xsl:with-param name="targetnode" select="$dvnode"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$dvnode/attribute::xsi:type = 'DV_PARAGRAPH'">
        <xsl:call-template name="generic-DV_PARAGRAPH">
          <xsl:with-param name="paragraphnode" select="$dvnode"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$dvnode/attribute::xsi:type = 'DV_PROPORTION'">
        <xsl:call-template name="generic-DV_PROPORTION">
          <xsl:with-param name="targetnode" select="$dvnode"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$dvnode/attribute::xsi:type='DV_QUANTITY'">
        <xsl:call-template name="generic-DV_QUANTITY">
          <xsl:with-param name="targetnode" select="$dvnode"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$dvnode/attribute::xsi:type='DV_QUANTITY_RATIO'">
        <xsl:call-template name="generic-DV_QUANTITY_RATIO">
          <xsl:with-param name="targetnode" select="$dvnode"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$dvnode/attribute::xsi:type = 'DV_STATE'">
        <xsl:call-template name="generic-DV_STATE">
          <xsl:with-param name="statenode" select="$dvnode"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$dvnode/attribute::xsi:type = 'DV_TEXT'">
        <xsl:call-template name="generic-DV_TEXT">
          <xsl:with-param name="textnode" select="$dvnode"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$dvnode/attribute::xsi:type='DV_URI'">
        <xsl:call-template name="generic-DV_URI">
          <xsl:with-param name="targetnode" select="$dvnode"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$dvnode/attribute::xsi:type='REFERENCE_RANGE'">
        <xsl:call-template name="generic-REFERENCE_RANGE">
          <xsl:with-param name="targetnode" select="$dvnode"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$dvnode/attribute::xsi:type='DV_IDENTIFIER'">
        <xsl:call-template name="generic-DV_IDENTIFIER">
          <xsl:with-param name="targetnode" select="$dvnode"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <span style="font-size:80%">UNSUPPORTED DATA TYPE ENCOUNTERED: <xsl:value-of select="$dvnode/attribute::xsi:type"/></span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Generic DV_BOOLEAN -->
  <xsl:template name="generic-DV_BOOLEAN">
    <xsl:param name="boolnode"/>
    <xsl:choose>
      <xsl:when test="$boolnode/oe:value='true'"> Yes </xsl:when>
      <xsl:otherwise> No </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Generic DV_CODED_TEXT -->
  <xsl:template name="generic-DV_CODED_TEXT">
    <xsl:param name="targetnode"/>
    <xsl:call-template name="generic-DV_TEXT">
      <xsl:with-param name="textnode" select="$targetnode"/>
    </xsl:call-template>
  </xsl:template>

  <!-- Generic DV_COUNT -->
  <xsl:template name="generic-DV_COUNT">
    <xsl:param name="targetnode"/>
    <xsl:value-of select="$targetnode/oe:magnitude"/>
  </xsl:template>

  
  <!--
  <xsl:variable name="datetimeregex" v>^(?<format1>(?<year1>\d{4})(?:(?<month1>0[1-9]|1[0-2])(?:(?<day1>0[1-9]|[12]\d|3[01]))(?<timestring1>T?(?<hour1>[01]\d|2[0-3])(?:(?<minute1>[0-5]\d)(?:(?<second1>[0-5]\d)(?:[,.](?<secondfraction1>\d+))?)?)?(?<timezone1>Z|(?:(?<eastwest1>[+\-])(?:(?<tzhours1>0\d)|(1[0-2]))(?<tzminutes1>00|30)?))?)?)?)?|(?<format2>(?<year2>\d{4})(?:\-(?<month2>0[1-9]|1[0-2])(?:\-(?<day2>0[1-9]|[12]\d|3[01])(?<timestring2>T(?<hour2>[01]\d|2[0-3])(?:\:(?<minute2>[0-5]\d)(?:\:(?<second2>[0-5]\d)([,.](?<secondfraction2>\d+))?)?)?(?<timezone2>Z|(?:(?<eastwest2>)[+\-](?:(?<tzhours2>0\d)|(?<tzminutes2>1[0-2]))(:(00|30))?))?)?)?)?)$</xsl:variable>
  -->
  <!-- Generic DV_DATE_TIME
            (in format is '2006-05-05T10:44:46'; out format is '10:44')
            uses generic-date; assumes XML instance will use standard date "value" element
            uses generic-time; assumes XML instance will use standard date "value" element  -->
  <xsl:template name="generic-DV_DATE_TIME">
    <xsl:param name="datenode"/>
    <xsl:param name="show-seconds"/>
    <xsl:param name="show-timezone"></xsl:param>
    <xsl:call-template name="generic-date">
      <xsl:with-param name="datetimestring" select="$datenode/oe:value"/>
    </xsl:call-template>&nbsp;<xsl:call-template name="generic-time">
      <xsl:with-param name="datetimestring" select="$datenode/oe:value"/>
      <xsl:with-param name="show-seconds" select="$show-seconds"/>
      <xsl:with-param name="show-timezone" select="$show-timezone"/>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template name="generic-DV_DATE">
    <xsl:param name="datenode"/>
    <xsl:call-template name="generic-date">
      <xsl:with-param name="datetimestring" select="$datenode/oe:value"/>
    </xsl:call-template>
  </xsl:template>

  <!-- Generic DV_DURATION
        at present just prints the value -->
  <xsl:template name="generic-DV_DURATION">
    <xsl:param name="durationnode"/>
    <xsl:value-of select="$durationnode/oe:value"/>
  </xsl:template>

  <!-- Generic DV_INTERVAL -->
  <xsl:template name="generic-DV_INTERVAL">
    <xsl:param name="targetnode"/>
    <xsl:call-template name="generic-DV_ORDERED">
      <xsl:with-param name="orderednode" select="$targetnode/oe:lower"/>
    </xsl:call-template> - <xsl:call-template name="generic-DV_ORDERED">
      <xsl:with-param name="orderednode" select="$targetnode/oe:upper"/>
    </xsl:call-template>
  </xsl:template>

  <!-- Generic DV_MULTIMEDIA -->
  <xsl:template name="generic-DV_MULTIMEDIA">
    <xsl:param name="targetnode"/>
    <xsl:choose>
      <!-- if there is no data, just link the URI value -->
      <xsl:when test="not($targetnode/oe:data)">
        <xsl:call-template name="generic-DV_URI">
          <xsl:with-param name="targetnode" select="$targetnode/oe:uri"/>
          <xsl:with-param name="linktext" select="parent::node()/oe:name"/>
        </xsl:call-template>
      </xsl:when>

      <!-- otherwise we do somethign more fancy -->
      <xsl:otherwise>
        <span style="font-size:80%">CAN'T DISPLAY MULTIMEDIA CONTAINING DATA YET</span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Generic DV_ORDERED -->
  <xsl:template name="generic-DV_ORDERED">
    <xsl:param name="orderednode"/>
    <xsl:choose>
      <xsl:when test="$orderednode[@xsi:type='DV_COUNT']">
        <xsl:call-template name="generic-DV_COUNT">
          <xsl:with-param name="targetnode" select="$orderednode"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$orderednode[@xsi:type='DV_ORDINAL']">
        <xsl:call-template name="generic-DV_ORDINAL">
          <xsl:with-param name="targetnode" select="$orderednode"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$orderednode[@xsi:type='DV_PROPORTION']">
        <xsl:call-template name="generic-DV_PROPORTION">
          <xsl:with-param name="targetnode" select="$orderednode"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$orderednode[@xsi:type='DV_QUANTITY']">
        <xsl:call-template name="generic-DV_QUANTITY">
          <xsl:with-param name="targetnode" select="$orderednode"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <span style="font-size:80%">UNSUPPORTED DV_ORDERED CHILD TYPE ENCOUNTERED: <xsl:value-of
            select="$orderednode/attribute::xsi:type"/></span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Generic DV_ORDINAL -->
  <xsl:template name="generic-DV_ORDINAL">
    <xsl:param name="targetnode"/>
    <xsl:param name="show-value"/>
    <xsl:if test="$show-value='yes'"><span class="numeric-value">
      <xsl:value-of select="$targetnode/oe:value"/>
    </span>:</xsl:if>
    <xsl:value-of select="$targetnode/oe:symbol/oe:value"/>
  </xsl:template>

  <!-- Generic DV_PARAGRAPH -->
  <xsl:template name="generic-DV_PARAGRAPH">
    <xsl:param name="paragraphnode"/>
    <xsl:for-each select="$paragraphnode/oe:items">
      <xsl:call-template name="generic-DV_TEXT">
        <xsl:with-param name="textnode" select="."/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <!-- Generic DV_PROPORTION -->
  <xsl:template name="generic-DV_PROPORTION">
    <xsl:param name="targetnode"/>
    <xsl:value-of select="$targetnode/oe:numerator"/>
    <xsl:choose>
      <!-- ratio -->
      <xsl:when test="$targetnode/oe:type = '0'"> :: <xsl:value-of select="$targetnode/oe:denominator"/>
      </xsl:when>

      <!-- unitary -->
      <xsl:when test="$targetnode/oe:type='1'"> per <xsl:value-of select="$targetnode/oe:denominator"/>
      </xsl:when>

      <!-- percent -->
      <xsl:when test="$targetnode/oe:type='2'">%
        <xsl:if test="not($targetnode/oe:denominator = '100')">
          of <xsl:value-of select="$targetnode/oe:denominator"/>
        </xsl:if>
      </xsl:when>

      <!-- fraction or integer fraction -->
      <xsl:when test="$targetnode/oe:type='3' or $targetnode/oe:type='4'"> / <xsl:value-of
          select="$targetnode/oe:denominator"/>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <!-- Generic DV_QUANTITY -->
  <xsl:template name="generic-DV_QUANTITY">
    <xsl:param name="targetnode"/>

    <xsl:variable name="val" select="number($targetnode/oe:magnitude)"/>
    
    <!-- get all reference ranges into printable string -->
    <xsl:variable name="ranges-text">
      <xsl:if test="$targetnode/oe:normal_range">
        <xsl:choose>
          <xsl:when
            test="$val &gt;= number($targetnode/oe:normal_range/oe:lower/oe:magnitude) and $val &lt;= number($targetnode/oe:normal_range/oe:upper/oe:magnitude)"
            >Value is within normal range</xsl:when>
          <xsl:otherwise>Value is outside normal range</xsl:otherwise>
        </xsl:choose>(<xsl:call-template name="generic-DV_INTERVAL">
          <xsl:with-param name="targetnode" select="oe:range"/>
        </xsl:call-template>).</xsl:if>
      <xsl:if test="$targetnode/oe:other_reference_ranges">
        <xsl:for-each select="$targetnode/oe:other_reference_ranges"><xsl:choose>
            <xsl:when
              test="$val &gt;= number(oe:range/oe:lower/oe:magnitude) and $val &lt;= number(oe:range/oe:upper/oe:magnitude)"
              >Within range</xsl:when>
            <xsl:otherwise>Outside range</xsl:otherwise>
          </xsl:choose>'<xsl:value-of select="oe:meaning/oe:value"/>' (<xsl:call-template
            name="generic-DV_INTERVAL">
            <xsl:with-param name="targetnode" select="oe:range"/>
          </xsl:call-template>).</xsl:for-each>
      </xsl:if>
    </xsl:variable>

    <!-- display quantity -->
    <xsl:choose>

      <!-- when has normal range -->
      <xsl:when test="$targetnode/oe:normal_range">
        <xsl:choose>
          <xsl:when
            test="$val &gt;= number($targetnode/oe:normal_range/oe:lower/oe:magnitude) and $val &lt;= number($targetnode/oe:normal_range/oe:upper/oe:magnitude)">
            <span style="color:#0a0;" title="{$ranges-text}">
              <xsl:value-of select="$val"/>&nbsp;<xsl:value-of
                select="$targetnode/oe:units"/>
            </span>
          </xsl:when>
          <xsl:otherwise>
            <span style="color:#c00;" title="{$ranges-text}">
              <xsl:value-of select="$val"/>&nbsp;<xsl:value-of
                select="$targetnode/oe:units"/>
            </span>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>

      <!-- when does not have normal range -->
      <xsl:otherwise>
        <span title="{$ranges-text}">
          <xsl:value-of select="$val"/>&nbsp;<xsl:value-of
            select="$targetnode/oe:units"/>
        </span>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <!-- Generic DV_QUANTITY_RATIO -->
  <xsl:template name="generic-DV_QUANTITY_RATIO">
    <xsl:param name="targetnode"/>
    <span style="font-size:80%">CAN'T DISPLAY RATIOS YET</span>
  </xsl:template>

  <!-- Generic DV_STATE-->
  <xsl:template name="generic-DV_STATE">
    <xsl:param name="statenode"/>
    <xsl:call-template name="generic-DV_CODED_TEXT">
      <xsl:with-param name="targetnode" select="$statenode/oe:value"/>
    </xsl:call-template>
  </xsl:template>

  <!-- Generic DV_TEXT
                hyperlinks the text if hyperlink is present
                ignores formatiing, mapping, language, encoding -->
  <xsl:template name="generic-DV_TEXT">
    <xsl:param name="textnode"/>
    <xsl:choose>
      <xsl:when test="$textnode/oe:hyperlink">
        <xsl:call-template name="generic-DV_URI">
          <xsl:with-param name="targetnode" select="$textnode/oe:hyperlink"/>
          <xsl:with-param name="linktext" select="$textnode/oe:value"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="replaceNL">
          <xsl:with-param name="string" select="$textnode/oe:value"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="generic-DV_IDENTIFIER">
    <xsl:param name="targetnode"/>
    
    <xsl:value-of select="$targetnode/oe:type"/>::<xsl:value-of select="$targetnode/oe:id"/>
    
    (Issued by: <xsl:value-of select="$targetnode/oe:issuer"/>
    /
    Assigner by: <xsl:value-of select="$targetnode/oe:assigner"/>
    )

  </xsl:template>

  <!-- Generic DV_URI -->
  <xsl:template name="generic-DV_URI">
    <xsl:param name="targetnode"/>
    <xsl:param name="linktext"/>
    <xsl:variable name="linkhref" select="$targetnode/oe:value"/>
    <xsl:choose>
      <xsl:when test="$linktext">
        <a href="{$linkhref}">
          <xsl:value-of select="$linktext"/>
        </a>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$linkhref"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Generic REFERENCE_RANGE -->
  <xsl:template name="generic-REFERENCE_RANGE">
    <xsl:param name="targetnode"/>
    <xsl:call-template name="generic-DV_TEXT">
      <xsl:with-param name="textnode" select="$targetnode/oe:meaning"/>
    </xsl:call-template>: <xsl:call-template name="generic-DV_INTERVAL">
      <xsl:with-param name="targetnode" select="$targetnode/oe:range"/>
    </xsl:call-template>
  </xsl:template>

  <!-- Generic EVENT -->
  <xsl:template name="generic-EVENT">
    <xsl:param name="eventnode"/>
    <xsl:value-of select="$eventnode/oe:name/oe:value"/>&nbsp;
    <xsl:call-template name="generic-time">
      <xsl:with-param name="datetimestring" select="$eventnode/oe:time/oe:value"/>
    </xsl:call-template>
    <xsl:if test="$eventnode/oe:state">
      <br/>State: <xsl:call-template name="generic-ITEM_STRUCTURE">
        <xsl:with-param name="itemsnode" select="$eventnode/oe:state"/>
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="$eventnode/oe:offset">
      <br/>Offset: <xsl:call-template name="generic-DV_DURATION">
        <xsl:with-param name="durationnode" select="$eventnode/oe:offset"/>
      </xsl:call-template>
    </xsl:if>
    <xsl:call-template name="generic-ITEM_STRUCTURE">
      <xsl:with-param name="itemsnode" select="$eventnode/oe:data"/>
    </xsl:call-template>
  </xsl:template>

  <!-- .............. end openEHR class templates .............. -->


  <!-- .............. summary openEHR class templates ....... -->
  <!-- Summary Histology HISTORY -->
  <xsl:template name="summary-histology-HISTORY">
    <xsl:param name="targetnode"/>
    <xsl:if test="$targetnode/oe:summary">
      <xsl:call-template name="generic-ITEM_STRUCTURE">
        <xsl:with-param name="itemsnode" select="$targetnode/oe:summary"/>
      </xsl:call-template>
    </xsl:if>
    <xsl:for-each select="$targetnode/oe:events/oe:data/descendant-or-self::*/oe:items">
      <xsl:if test="@xsi:type='ELEMENT'">
        <xsl:call-template name="generic-ELEMENT">
          <xsl:with-param name="elementnode" select="."/>
        </xsl:call-template>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <!-- Summary Problem Diagnosis Histological ITEM_TREE -->
  <xsl:template name="summary-histological-ITEM_TREE">
    <xsl:param name="itemsnode"/>
    <xsl:for-each select="descendant-or-self::*/oe:items">
      <xsl:if test="@xsi:type='ELEMENT'">
        <xsl:call-template name="generic-ELEMENT">
          <xsl:with-param name="elementnode" select="."/>
        </xsl:call-template>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <!-- Summary ELEMENT -->
  <xsl:template name="summary-ELEMENT">
    <xsl:param name="elementnode"/>
    <xsl:choose>
      <xsl:when test="$elementnode/oe:value">
        <xsl:call-template name="generic-DATA_VALUE">
          <xsl:with-param name="dvnode" select="$elementnode/oe:value"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise> ? </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- ........... end summary openEHR class templates .... -->


  <!-- ........... Alternate openEHR class templates .... -->
  <!-- Alternate ITEM_TREE -->
  <xsl:template name="alt-ITEM_TREE">
    <xsl:param name="itemsnode"/>
    <xsl:for-each select="$itemsnode/oe:items">
      <xsl:call-template name="alt-ITEM">
        <xsl:with-param name="itemnode" select="."/>
      </xsl:call-template>
      <br/>
    </xsl:for-each>
  </xsl:template>

  <!-- Alternate ITEM_LIST -->
  <xsl:template name="alt-ITEM_LIST">
    <xsl:param name="itemsnode"/>
    <xsl:for-each select="$itemsnode/oe:items">
      <xsl:call-template name="alt-ELEMENT">
        <xsl:with-param name="elementnode" select="$itemsnode/oe:items"/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>


  <!-- Alternate ITEM -->
  <xsl:template name="alt-ITEM">
    <xsl:param name="itemnode"/>
    <xsl:param name="displayboldname"/>
    <xsl:choose>
      <xsl:when test="$itemnode/oe:items or $itemnode[@xsi:type = 'CLUSTER']">
        <xsl:call-template name="alt-CLUSTER">
          <xsl:with-param name="clusternode" select="$itemnode"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="alt-ELEMENT">
          <xsl:with-param name="elementnode" select="$itemnode"/>
          <xsl:with-param name="displayboldname" select="$displayboldname"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Alternate CLUSTER -->
  <xsl:template name="alt-CLUSTER">
    <xsl:param name="clusternode"/><b>
      <xsl:call-template name="generic-name">
        <xsl:with-param name="namenode" select="$clusternode/oe:name"/>
        <xsl:with-param name="namecat">data cluster</xsl:with-param>
      </xsl:call-template>
    </b>: <xsl:for-each select="$clusternode/oe:items">
      <br/>
      <xsl:call-template name="alt-ITEM">
        <xsl:with-param name="itemnode" select="."/>
        <xsl:with-param name="displayboldname">no</xsl:with-param>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <!-- Alternate ELEMENT -->
  <xsl:template name="alt-ELEMENT">
    <xsl:param name="elementnode"/>
    <xsl:param name="displayboldname"/>
    <xsl:choose>
      <xsl:when test="$displayboldname='yes'">
        <b>
          <xsl:call-template name="generic-name">
            <xsl:with-param name="namenode" select="$elementnode/oe:name"/>
            <xsl:with-param name="namecat">data item</xsl:with-param>
          </xsl:call-template>
        </b>: </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="generic-name">
          <xsl:with-param name="namenode" select="$elementnode/oe:name"/>
          <xsl:with-param name="namecat">data item</xsl:with-param>
        </xsl:call-template>: </xsl:otherwise>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="$elementnode/oe:value">
        <xsl:call-template name="generic-DATA_VALUE">
          <xsl:with-param name="dvnode" select="$elementnode/oe:value"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <br/> ? </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- ........... end Alternate openEHR class templates .... -->

  <!-- ........... ocean openEHR class templates ............ -->
  <!--Ocean observation HISTORY -->
  <xsl:template name="ocean-observation-HISTORY" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:param name="targetnode"/>
    <!-- this IF is only to stop instance error since RM says it MUST exist -->
    <br/>
    <xsl:if test="$targetnode/oe:duration">
      <br/>Temporal duration: <xsl:call-template name="generic-DV_DURATION">
        <xsl:with-param name="durationnode" select="$targetnode/oe:duration"/>
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="$targetnode/oe:period">
      <br/>Temporal period: <xsl:call-template name="generic-DV_DURATION">
        <xsl:with-param name="durationnode" select="$targetnode/oe:period"/>
      </xsl:call-template>
    </xsl:if>
    <xsl:for-each select="$targetnode/oe:summary">
      <xsl:call-template name="generic-ITEM_STRUCTURE">
        <xsl:with-param name="itemsnode" select="."/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="$targetnode/oe:events">
      <xsl:choose>
        <xsl:when
          test="not($targetnode/oe:events/oe:data/oe:item or $targetnode/oe:events/oe:data/oe:item[xsi:type='ITEM_SINGLE'])">
          <xsl:if test="count(./parent::node()/events) > 1"><br/>Specimen collected: <xsl:call-template name="generic-DV_DATE_TIME">
            <xsl:with-param name="datenode" select="./oe:time"/>
          </xsl:call-template></xsl:if>
          <xsl:call-template name="ocean-observation-event-ITEM_STRUCTURE">
            <xsl:with-param name="itemsnode" select="oe:data"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <br/>
          <xsl:choose>
            <xsl:when test="oe:data/oe:item/oe:value[@xsi:type='DV_QUANTITY']">
              <xsl:value-of select="oe:data/oe:item/oe:value/oe:magnitude"/>&nbsp;<xsl:value-of
                select="oe:data/oe:item/oe:value/oe:units"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="generic-DATA_VALUE">
                <xsl:with-param name="dvnode" select="oe:data/oe:item/oe:value"/>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="ocean-observation-event-ITEM_STRUCTURE"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:param name="itemsnode"/>
    <xsl:choose>
      <xsl:when test="$itemsnode/oe:item or $itemsnode/oe:item[@xsi:type='ITEM_SINGLE']">&nbsp;
        <b><xsl:call-template name="generic-DATA_VALUE">
            <xsl:with-param name="dvnode" select="$itemsnode/oe:item/oe:value"/>
          </xsl:call-template></b>
      </xsl:when>
      <xsl:when test="$itemsnode/oe:item or $itemsnode/oe:item[@xsi:type='ITEM_TABLE']">
        <table cellpadding="1" cellspacing="0" style="font-size:12px;border-top:solid 1px#ddd;">
          <xsl:for-each select="$itemsnode/oe:items">
            <xsl:call-template name="ocean-observation-ITEM">
              <xsl:with-param name="itemnode" select="."/>
            </xsl:call-template>
          </xsl:for-each>
        </table>
      </xsl:when>
      <xsl:otherwise>
        <xsl:for-each select="$itemsnode/oe:items">
          <xsl:call-template name="generic-ITEM">
            <xsl:with-param name="itemnode" select="."/>
          </xsl:call-template>
        </xsl:for-each>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="ocean-observation-ITEM" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:param name="itemnode"/>
    <xsl:param name="show-name"/>
    <xsl:choose>
      <xsl:when test="$itemnode/oe:items or $itemnode[@xsi:type = 'CLUSTER']">
        <xsl:if test="$show-name='true'">
          <tr ><td colspan="4" align="left" valign="top" style="border-bottom:solid 1px #ddd;">
            <xsl:value-of select="$itemnode/oe:name/oe:value"/>             
          </td></tr>
        </xsl:if>
        <xsl:for-each select="$itemnode/oe:items">
          <xsl:call-template name="ocean-observation-ITEM">
            <xsl:with-param name="itemnode" select="."/>
            <xsl:with-param name="show-name" select="$show-name"/>
          </xsl:call-template>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="ocean-observation-ELEMENT">
          <xsl:with-param name="elementnode" select="$itemnode"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="ocean-observation-ELEMENT" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:param name="elementnode"/>
    <tr>
      <xsl:choose>
        <!-- for data values that are text -->
        <xsl:when
          test="$elementnode/oe:value[@xsi:type='DV_TEXT' or 
                    @xsi:type='DV_CODED_TEXT']">
          <td colspan="4" align="left" valign="top" style="border-bottom:solid 1px #ddd;">
            <xsl:call-template name="generic-name">
              <xsl:with-param name="namenode" select="$elementnode/oe:name"/>
              <xsl:with-param name="namecat">laboratory item</xsl:with-param>
            </xsl:call-template>: <xsl:choose>
              <xsl:when test="$elementnode/oe:value">
                <xsl:call-template name="generic-DATA_VALUE">
                  <xsl:with-param name="dvnode" select="$elementnode/oe:value"/>
                </xsl:call-template>
              </xsl:when>
              <xsl:otherwise>&nbsp;?&nbsp;</xsl:otherwise>
            </xsl:choose>
          </td>
        </xsl:when>

        <!-- for data values that are quantities -->
        <xsl:when test="$elementnode/oe:value[@xsi:type='DV_QUANTITY']">
          <xsl:variable name="val" select="number($elementnode/oe:value/oe:magnitude)"/>
          <xsl:variable name="ranges-text">
            <xsl:if test="$elementnode/oe:value/oe:normal_range">
              <xsl:variable name="norm-val-low" select="number($elementnode/oe:value/oe:normal_range/oe:lower/oe:magnitude)"/>
              <xsl:variable name="norm-val-high" select="number($elementnode/oe:value/oe:normal_range/oe:upper/oe:magnitude)"/>
              <xsl:choose>
                <xsl:when test="$val &gt;= $norm-val-low and $val &lt;= $norm-val-high">
                  Value is within normal range
                </xsl:when>
                <xsl:otherwise>
                  Value is outside normal range
                </xsl:otherwise>
              </xsl:choose>
              (<xsl:call-template name="generic-DV_INTERVAL">
                <xsl:with-param name="targetnode" select="oe:range"/>
              </xsl:call-template>).
            </xsl:if>
            <xsl:if test="$elementnode/oe:value/oe:other_reference_ranges">
              <xsl:for-each select="$elementnode/oe:value/oe:other_reference_ranges">
                <xsl:choose>
                  <xsl:when test="$val &gt;= number(oe:range/oe:lower/oe:magnitude) and $val &lt;= number(oe:range/oe:upper/oe:magnitude)">
                    Within range
                  </xsl:when>
                  <xsl:otherwise>Outside range</xsl:otherwise>
                </xsl:choose>
                '<xsl:value-of select="oe:meaning/oe:value"/>' 
                (<xsl:call-template
                  name="generic-DV_INTERVAL">
                  <xsl:with-param name="targetnode" select="oe:range"/>
                </xsl:call-template>).
              </xsl:for-each>
            </xsl:if>
          </xsl:variable>
          <td align="left" valign="top" style="border-bottom:solid 1px #ddd;">
            <xsl:call-template name="generic-name">
              <xsl:with-param name="namenode" select="$elementnode/oe:name"/>
            </xsl:call-template>: </td>
          <td align="right" valign="top" style="border-bottom:solid 1px #ddd;">
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <!-- get all reference ranges into printable string -->
            <xsl:choose>
              <xsl:when test="$elementnode/oe:value/oe:normal_range">
                <xsl:choose>
                  <xsl:when
                    test="$val &gt;= number($elementnode/oe:value/oe:normal_range/oe:lower/oe:magnitude) and $val &lt;= number($elementnode/oe:value/oe:normal_range/oe:upper/oe:magnitude)">
                    <span style="color:#0a0;" title="{$ranges-text}">
                      <xsl:call-template name="ocean-DV_QUANTITY-magnitude-normal_range">
                        <xsl:with-param name="targetnode" select="$elementnode/oe:value"/>
                      </xsl:call-template>
                    </span>
                  </xsl:when>
                  <xsl:otherwise>
                    <span style="color:#c00;" title="{$ranges-text}">
                      <xsl:call-template name="ocean-DV_QUANTITY-magnitude-normal_range">
                        <xsl:with-param name="targetnode" select="$elementnode/oe:value"/>
                      </xsl:call-template>
                    </span>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              <xsl:otherwise>
                <span title="{$ranges-text}">
                  <xsl:call-template name="ocean-DV_QUANTITY-magnitude-normal_range">
                    <xsl:with-param name="targetnode" select="$elementnode/oe:value"/>
                  </xsl:call-template>
                </span>
              </xsl:otherwise>
            </xsl:choose>
          </td>
          <td align="left" valign="top" style="border-bottom:solid 1px #ddd;">
            <xsl:choose>
              <xsl:when
                test="$val &gt;= number($elementnode/oe:value/oe:normal_range/oe:lower/oe:magnitude) and $val &lt;= number($elementnode/oe:value/oe:normal_range/oe:upper/oe:magnitude)">
                <span style="color:#0a0;" title="{$ranges-text}">
                  <xsl:choose>
                    <xsl:when test="contains($elementnode/oe:value/oe:magnitude, '.')"> .<xsl:value-of
                      select="substring-after($elementnode/oe:value/oe:magnitude, '.')"/>
                    </xsl:when>
                    <xsl:otherwise>&nbsp;</xsl:otherwise>
                  </xsl:choose>
                </span>
              </xsl:when>
              <xsl:otherwise>
                <span style="color:#c00;" title="{$ranges-text}">
                  <xsl:choose>
                    <xsl:when test="contains($elementnode/oe:value/oe:magnitude, '.')"> .<xsl:value-of
                      select="substring-after($elementnode/oe:value/oe:magnitude, '.')"/>
                    </xsl:when>
                    <xsl:otherwise>&nbsp;</xsl:otherwise>
                  </xsl:choose>
                </span>
              </xsl:otherwise>
            </xsl:choose>
          </td>
          <td align="left" valign="top" style="border-bottom:solid 1px #ddd;">
              &nbsp;&nbsp;<xsl:value-of select="$elementnode/oe:value/oe:units"/>
          </td>
          <td align="left" valign="top" style="border-bottom:solid 1px #ddd;"/>
        </xsl:when>

        <!-- for all other data values (treated as simple name value pairs) -->
        <xsl:otherwise>
          <td align="left" valign="top" style="border-bottom:solid 1px #ddd;">
            <xsl:call-template name="generic-name">
              <xsl:with-param name="namenode" select="$elementnode/oe:name"/>
              <xsl:with-param name="namecat">laboratory item</xsl:with-param>
            </xsl:call-template>: </td>
          <td align="left" valign="top" style="border-bottom:solid 1px #ddd;" colspan="3">
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <xsl:choose>
              <xsl:when test="$elementnode/oe:value">
                <xsl:call-template name="generic-DATA_VALUE">
                  <xsl:with-param name="dvnode" select="$elementnode/oe:value"/>
                </xsl:call-template>
              </xsl:when>
              <xsl:otherwise>
                <br/>? </xsl:otherwise>
            </xsl:choose>
          </td>
        </xsl:otherwise>
      </xsl:choose>
    </tr>
  </xsl:template>

  <xsl:template name="ocean-DV_QUANTITY-magnitude-normal_range">
    <xsl:param name="targetnode"/>
    <xsl:choose>
      <xsl:when test="contains($targetnode/oe:magnitude, '.')">
        <xsl:value-of select="substring-before($targetnode/oe:magnitude, '.')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$targetnode/oe:magnitude"/>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>
  <!-- ........... end ocean openEHR class templates ........ -->


  <!-- match templates for commonly-archetyped openEHR RM clases -->
  <!-- DEFAULT COMPOSITION TEMPLATE -->
  <xsl:template match="*[starts-with(@archetype_node_id, 'openEHR-EHR-COMPOSITION')]"
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <div class="composition" id="composition-wrapper">
      <div class="openEHR-EHR-COMPOSITION">

        <!-- do generic composition header -->
        <xsl:call-template name="composition-header">
          <xsl:with-param name="compositionnode" select="."/>
        </xsl:call-template>

        <xsl:apply-templates select="oe:content"/>
      </div>
    </div>
  </xsl:template>
  
  <!-- DEFAULT SECTION -->
  <xsl:template match="*[@xsi:type='SECTION']" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <div class="openEHR-EHR-SECTION">
      <h2 style="font-weight:bold;">
        <xsl:call-template name="generic-name">
          <xsl:with-param name="namenode" select="oe:name"/>
          <xsl:with-param name="namecat">section</xsl:with-param>
        </xsl:call-template>
      </h2>
      <xsl:apply-templates select="oe:items"/>
    </div>
  </xsl:template>

  <!-- DEFAULT OBSERVATION TEMPLATE -->
  <xsl:template match="*[starts-with(@archetype_node_id, 'openEHR-EHR-OBSERVATION')]" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:if test="oe:data/oe:events/oe:data/oe:items or oe:data/oe:events/oe:data/oe:item or oe:data/oe:events/oe:data/oe:columns">
      <div class="openEHR-EHR-OBSERVATION">
        <p class="entry-header">
          <!-- header -->
          <xsl:call-template name="generic-entry-header">
            <xsl:with-param name="entrynode" select="."/>
            <xsl:with-param name="entrytype">clinical observation</xsl:with-param>
          </xsl:call-template>

          <!-- data -->
          <br/>
          <xsl:call-template name="generic-HISTORY">
            <xsl:with-param name="targetnode" select="oe:data"/>
          </xsl:call-template>

          <!-- state of subject -->
          <xsl:if test="oe:state">
            <b>
              <xsl:value-of select="oe:state/oe:name/oe:value"/>
            </b>:
            <xsl:call-template name="generic-HISTORY">
              <xsl:with-param name="targetnode" select="oe:state"/>
            </xsl:call-template>
          </xsl:if>
        </p>

        <!-- protocol -->
        <xsl:if test="oe:protocol">
          <br/>
          <div style="color:#aaa;margin-left:-30px;">
            <xsl:call-template name="generic-ITEM_STRUCTURE">
              <xsl:with-param name="itemsnode" select="oe:protocol"/>
            </xsl:call-template>
          </div>
        </xsl:if>
      </div>
    </xsl:if>
  </xsl:template>

  <!-- DEFAULT EVALUATION -->
  <xsl:template match="*[starts-with(@archetype_node_id,'openEHR-EHR-EVALUATION')]"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <div class="openEHR-EHR-EVALUATION">

      <!-- header -->
      <p class="entry-header">
        <xsl:call-template name="generic-entry-header">
          <xsl:with-param name="entrynode" select="."/>
          <xsl:with-param name="entrytype">clinical evaluation</xsl:with-param>
        </xsl:call-template>

        <!-- data -->
        <br/>
        <xsl:call-template name="generic-ITEM_STRUCTURE">
          <xsl:with-param name="itemsnode" select="oe:data"/>
        </xsl:call-template>
      </p>
    </div>
  </xsl:template>

  <!-- DEFAULT INSTRUCTION -->
  <xsl:template match="*[starts-with(@archetype_node_id, 'openEHR-EHR-INSTRUCTION')]"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <div class="openEHR-EHR-INSTRUCTION">

      <!-- header -->
      <p class="entry-header">
        <xsl:call-template name="generic-entry-header">
          <xsl:with-param name="entrynode" select="."/>
          <xsl:with-param name="entrytype">clinical instruction</xsl:with-param>
        </xsl:call-template>
      </p>

      <br/>
      <!-- supports misspelt schema element for an INSTRUCTION -->
      <xsl:call-template name="generic-DV_TEXT">
        <xsl:with-param name="textnode" select="oe:narrative"/>
      </xsl:call-template>
      <xsl:if test="oe:expiry_time">
        <br/>Instruction expires:
        <xsl:call-template name="generic-DV_DATE_TIME">
          <xsl:with-param name="datenode" select="oe:expiry_time"/>
        </xsl:call-template>
      </xsl:if>

      <br/><b>Activities</b>
      <xsl:for-each select="oe:activities">
        <blockquote style="border-bottom:solid 1px #ddd;padding:4px;">
          <xsl:apply-templates select="oe:description"/>
        </blockquote>
      </xsl:for-each>
      <br/>&nbsp;
    </div>
  </xsl:template>

  <!-- default ACTION TEMPLATE -->
  <xsl:template match="*[starts-with(@archetype_node_id, 'openEHR-EHR-ACTION')]"
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <div class="openEHR-EHR-ACTION">

      <!-- header -->
      <p class="entry-header">
        <xsl:call-template name="generic-entry-header">
          <xsl:with-param name="entrynode" select="."/>
          <xsl:with-param name="entrytype">action</xsl:with-param>
        </xsl:call-template>
      </p>

      <table cellspacing="0" cellpadding="2" width="100%">
        <tr>
          <td valign="top" align="left">
            <xsl:call-template name="generic-time">
              <xsl:with-param name="datetimestring" select="oe:time/oe:value"/>
            </xsl:call-template>
          </td>
        </tr>
        <tr>
          <td>
            <xsl:call-template name="action-ITEM_STRUCTURE">
              <xsl:with-param name="itemsnode" select="oe:description"/>
            </xsl:call-template>
          </td>
        </tr>
      </table>
    </div>
  </xsl:template>
  <xsl:template name="action-ITEM_STRUCTURE" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:param name="itemsnode"/>
    <xsl:choose>
      <!-- known item tree -->
      <xsl:when test="$itemsnode[@xsi:type = 'ITEM_TREE']">
        <xsl:if test="$itemsnode/oe:items">
          <xsl:call-template name="alt-ITEM_TREE">
            <xsl:with-param name="itemsnode" select="$itemsnode"/>
          </xsl:call-template>
        </xsl:if>
      </xsl:when>
      <!-- known item list -->
      <xsl:when test="$itemsnode[@xsi:type = 'ITEM_LIST']">
        <xsl:if test="$itemsnode/oe:items">
          <xsl:call-template name="alt-ITEM_LIST">
            <xsl:with-param name="itemsnode" select="$itemsnode"/>
          </xsl:call-template>
        </xsl:if>
      </xsl:when>
      <!-- known item table -->
      <xsl:when test="$itemsnode[@xsi:type = 'ITEM_TABLE']">
        <xsl:if test="$itemsnode/oe:items">
          <xsl:call-template name="generic-ITEM_TABLE">
            <xsl:with-param name="itemsnode" select="$itemsnode"/>
          </xsl:call-template>
        </xsl:if>
      </xsl:when>
      <!-- else we just assume single item or item tree -->
      <xsl:otherwise>
        <xsl:if test="$itemsnode/oe:item">
          <!-- meaning it's single -->
          <br/>
          <xsl:call-template name="alt-ELEMENT">
            <xsl:with-param name="elementnode" select="$itemsnode/oe:item"/>
          </xsl:call-template>
        </xsl:if>
        <xsl:if test="$itemsnode/oe:items">
          <!-- assuming it's a tree (includes list)-->
          <xsl:for-each select="$itemsnode/oe:items">
            <xsl:call-template name="alt-ITEM">
              <xsl:with-param name="itemnode" select="$itemsnode/oe:items"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- DEFAULT ITEM_TREE -->
  <xsl:template match="*[starts-with(@archetype_node_id, 'openEHR-EHR-ITEM_TREE')]"
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:call-template name="generic-ITEM_TREE">
      <xsl:with-param name="itemsnode" select="."/>
    </xsl:call-template>
  </xsl:template>

  <!-- DEFAULT ITEM_TABLE -->
  <xsl:template match="*[starts-with(@archetype_node_id, 'openEHR-EHR-ITEM_TABLE')]"
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:call-template name="generic-ITEM_TABLE">
      <xsl:with-param name="itemsnode" select="."/>
    </xsl:call-template>
  </xsl:template>

  <!-- DEFAULT ITEM_SINGLE -->
  <xsl:template match="*[starts-with(@archetype_node_id, 'openEHR-EHR-ITEM_SINGLE')]"
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:call-template name="generic-ITEM_SINGLE">
      <xsl:with-param name="itemsnode" select="."/>
    </xsl:call-template>
  </xsl:template>

  <!-- DEFAULT ITEM_LIST -->
  <xsl:template match="*[starts-with(@archetype_node_id, 'openEHR-EHR-ITEM_LIST')]"
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:call-template name="generic-ITEM_LIST">
      <xsl:with-param name="itemsnode" select="."/>
    </xsl:call-template>
  </xsl:template>

  <!-- DEFAULT ADMIN_ENTRY TEMPLATE -->
  <xsl:template match="*[starts-with(@archetype_node_id, 'openEHR-EHR-ADMIN_ENTRY')]"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <div class="openEHR-EHR-ADMIN_ENTRY">

      <!-- header -->
      <p class="entry-header">
        <xsl:call-template name="generic-entry-header">
          <xsl:with-param name="entrynode" select="."/>
          <xsl:with-param name="entrytype">administrative entry</xsl:with-param>
        </xsl:call-template>
      </p>

      <xsl:call-template name="generic-ITEM_STRUCTURE">
        <xsl:with-param name="itemsnode" select="oe:data"/>
      </xsl:call-template>
    </div>
  </xsl:template>
  
</xsl:stylesheet>

<!-- ***** BEGIN LICENSE BLOCK *****
   - Version: MPL 1.1/GPL 2.0/LGPL 2.1
   -
   - The contents of this file are subject to the Mozilla Public License Version
   - 1.1 (the "License"); you may not use this file except in compliance with
   - the License. You may obtain a copy of the License at
   - http://www.mozilla.org/MPL/
   -
   - Software distributed under the License is distributed on an "AS IS" basis,
   - WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
   - for the specific language governing rights and limitations under the
   - License.
   -
   - The Original Code is 
   -   EhrView openEHR RM generic html transform (base.xslt).
   -
   - The Initial Developer of the Original Code is
   -   Ocean Informatics Pty Ltd.
   - Portions created by the Initial Developer are Copyright (C) 2006-2008
   - the Initial Developer. All Rights Reserved.
   -
   - Contributor(s):
   -   Lisa Thurston 
   -   Heath Frankel
   -
   - Alternatively, the contents of this file may be used under the terms of
   - either the GNU General Public License Version 2 or later (the "GPL"), or
   - the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
   - in which case the provisions of the GPL or the LGPL are applicable instead
   - of those above. If you wish to allow use of your version of this file only
   - under the terms of either the GPL or the LGPL, and not to allow others to
   - use your version of this file under the terms of the MPL, indicate your
   - decision by deleting the provisions above and replace them with the notice
   - and other provisions required by the LGPL or the GPL. If you do not delete
   - the provisions above, a recipient may use your version of this file under
   - the terms of any one of the MPL, the GPL or the LGPL.
   -
   - ***** END LICENSE BLOCK ***** -->
