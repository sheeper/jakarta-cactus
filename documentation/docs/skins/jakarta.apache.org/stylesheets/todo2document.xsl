<?xml version="1.0"?>

<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0">

 <xsl:import href="copyover.xsl"/>
 
 <xsl:template match="todo">
  <document>
   <header>
    <title><xsl:value-of select="@title"/></title>
   </header>
   <body>
    <xsl:apply-templates/>
   </body>
  </document>
 </xsl:template>

 <xsl:template match="devs">
   <s1 title="Developers assigned to tasks">
     <sl>
       <xsl:for-each select="person">
         <li>
           <strong><xsl:value-of select="@id"/></strong>
           <xsl:text> = </xsl:text>
           <link href="mailto:{@email}"><xsl:value-of select="@name"/></link>
         </li>
       </xsl:for-each>  
     </sl>
   </s1>
 </xsl:template>

 <xsl:template match="actions">
  <s1 title="{@priority} severity tasks">
   <sl>
    <xsl:for-each select="action">
     <li>
      <strong>
        <xsl:text>[</xsl:text><xsl:value-of select="@context"/><xsl:text>]</xsl:text>
      </strong>
      <xsl:text> </xsl:text>
      <xsl:apply-templates/>
      <xsl:if test="@assigned-to">
        <em>(assigned to <xsl:value-of select="@assigned-to"/>)</em>
      </xsl:if>
     </li>
    </xsl:for-each>
   </sl>
  </s1>
 </xsl:template>
 
</xsl:stylesheet>
