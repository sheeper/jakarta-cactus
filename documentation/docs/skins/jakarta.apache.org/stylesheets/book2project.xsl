<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- match the root book element -->
  <xsl:template match="book">

    <project>

      <parameter name="copyright" value="{@copyright}"/>
      <parameter name="updated" value="{@updated}"/>
      <parameter name="currentversion" value="{@currentversion}"/>
      <parameter name="otherversion" value="{@otherversion}"/>
      <parameter name="software" value="{@software}"/>

      <resource source="sbk:/style/resources/logocactus.gif" target="images/logocactus.gif"/>
      <resource source="sbk:/style/resources/jakarta-logo.gif" target="images/jakarta-logo.gif"/>
      <resource source="sbk:/style/resources/update.jpg" target="images/update.jpg"/>
      <resource source="sbk:/style/resources/remove.jpg" target="images/remove.jpg"/>
      <resource source="sbk:/style/resources/add.jpg" target="images/add.jpg"/>
      <resource source="sbk:/style/resources/fix.jpg" target="images/fix.jpg"/>
      <resource source="sbk:/style/resources/note.gif" target="images/note.gif"/>

      <xsl:apply-templates/>
    </project>

  </xsl:template>

  <xsl:template match="menu-item">

    <xsl:if test="not(@type) or ( @type!='external' )">

      <create source="{@source}" 
              target="{substring(@source,0,string-length(@source)-3)}.html" 
              producer="parser" 
              printer="html">

        <xsl:if test="@type and ( @type!='hidden' and @type!='document' )">
          <processor name="xslt">
            <parameter name="stylesheet" value="sbk:/style/stylesheets/{@type}2document.xsl"/>
          </processor>
        </xsl:if>

        <processor name="xslt">
          <parameter name="docid" value="{@source}"/>
          <parameter name="stylesheet" value="sbk:/style/stylesheets/document2html.xsl"/>
        </processor>
      </create>
    </xsl:if>

  </xsl:template>
  
  <xsl:template match="external">
  </xsl:template>

  <xsl:template match="project">
  </xsl:template>

</xsl:stylesheet>
