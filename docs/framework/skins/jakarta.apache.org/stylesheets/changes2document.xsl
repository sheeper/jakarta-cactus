<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

 <xsl:template match="changes">
  <document>
   <header>
    <title><xsl:value-of select="@title"/></title>
   </header>
   <body>
    <xsl:apply-templates/>
   </body>
  </document>
 </xsl:template>

 <xsl:template match="release">
  <s1>
    <xsl:attribute name="title">
      <xsl:value-of select="@name"/><xsl:text> </xsl:text>
      <xsl:value-of select="@version"/><xsl:text> </xsl:text>
      <xsl:if test="@date">
        <xsl:value-of select="@date"/>
      </xsl:if>
    </xsl:attribute>
   <sl>
    <xsl:apply-templates/>
   </sl>
  </s1>
 </xsl:template>

 <xsl:template match="action">
  <li>
   <icon src="images/{@type}.jpg" alt="{@type}"/>
   <xsl:apply-templates/>
   <xsl:text>(</xsl:text><xsl:value-of select="@dev"/><xsl:text>)</xsl:text>

   <xsl:if test="@due-to">
    <xsl:text> Thanks to </xsl:text>
    <link href="mailto:{@due-to-email}"><xsl:value-of select="@due-to"/></link>
    <xsl:text>.</xsl:text>
   </xsl:if>

   <xsl:if test="@fixes-bug">
    <xsl:text> Fixes </xsl:text>
    <link href="http://xml.apache.org/bugs/show_bug.cgi?id={@fixes-bug}">
     <xsl:text>bug </xsl:text><xsl:value-of select="@fixes-bug"/>
    </link>
    <xsl:text>.</xsl:text>
   </xsl:if>
  </li>
 </xsl:template>

 <xsl:template match="devs">
  <!-- remove -->
 </xsl:template>

</xsl:stylesheet>