<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

 <xsl:import href="copyover.xsl"/>

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

 <xsl:template match="releases">
   <s1>
    <xsl:attribute name="title">
      <xsl:value-of select="@title"/>
    </xsl:attribute>
    <xsl:apply-templates/>
   </s1>
 </xsl:template>

 <xsl:template match="release">
  <s2>
    <xsl:attribute name="title">
      <xsl:value-of select="$software"/><xsl:text> </xsl:text>
      <xsl:value-of select="@version"/>
      <xsl:if test="@date">
        <xsl:text> (released on </xsl:text>
        <xsl:value-of select="@date"/>
        <xsl:text>)</xsl:text>
      </xsl:if>
    </xsl:attribute>
    <xsl:apply-templates/>
  </s2>
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

<!-- ====================================================================== -->
<!-- changelog section -->
<!-- ====================================================================== -->

  <xsl:template match="cvslogs">
    <s1>
      <xsl:attribute name="title">
        <xsl:value-of select="@title"/>
      </xsl:attribute>
      <xsl:apply-templates/>
     </s1>
  </xsl:template>

  <xsl:template match="changelog">
    <xsl:choose>
      <xsl:when test="entry">
        <table>
          <xsl:apply-templates select="entry">
            <xsl:sort select="date" order="descending"/>
          </xsl:apply-templates>
        </table>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>&lt;no changes&gt;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="entry">
    <tr>
      <td>
        <xsl:value-of select="date"/>
      </td>
      <td>
        <link href="{concat(substring-before(file/name, '.'),'.html')}">
          <xsl:value-of select="substring-before(file/name, '.')"/>
        </link>
      </td>
      <td>
        <xsl:value-of select="msg"/>
      </td>
    </tr>
  </xsl:template>

</xsl:stylesheet>