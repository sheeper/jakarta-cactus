<?xml version="1.0"?>

<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0">

 <xsl:import href="copyover.xsl"/>
 
 <xsl:template match="roadmap">
  <document>
   <header>
    <title><xsl:value-of select="@title"/></title>
   </header>
   <body>
    <xsl:apply-templates/>
   </body>
  </document>
 </xsl:template>

 <xsl:template match="version">
  <s1 title="{@title}">
   <xsl:apply-templates/>
  </s1>
 </xsl:template>

 <xsl:template match="category">
  <s2 title="{@title}">
   <table>
    <tr><th width="75%">Description</th><th width="25%">Volunteers</th></tr>
    <xsl:apply-templates/>
   </table>
  </s2>
 </xsl:template>  

 <xsl:template match="action">
  <tr>
   <td>
     <xsl:apply-templates/>
   </td>
   <td>
    <xsl:if test="@assigned-to">
     <xsl:value-of select="@assigned-to"/>
    </xsl:if>
   </td>
  </tr>
 </xsl:template>
 
</xsl:stylesheet>
