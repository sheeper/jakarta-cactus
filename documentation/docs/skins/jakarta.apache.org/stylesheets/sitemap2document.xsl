<?xml version="1.0"?>

<!-- ====================================================================== -->
<!-- Transform the Sitemap to a Document, so that it later can easily be -->
<!-- transformed into HTML -->
<!-- $Id$ -->
<!-- ====================================================================== -->

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- Import common templates and parameters -->
  <xsl:import href="common.xsl"/>

  <xsl:output method="xml"/>

  <!-- ==================================================================== -->
  <!-- Templates -->
  <!-- ==================================================================== -->

  <xsl:template match="sitemap">
    <document id="sitemap">
      <properties>
        <title>Sitemap</title>
      </properties>
      <body>
        <section title="Sitemap">
          <xsl:apply-templates/>
        </section>
      </body>
    </document>
  </xsl:template>

  <xsl:template match="resource">
    <li class="sitemap">
      <a>
        <xsl:attribute name="href">
          <xsl:value-of select="@target"/>
        </xsl:attribute>
        <xsl:choose>
          <xsl:when test="@name">
            <xsl:value-of select="@name"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@id"/>
          </xsl:otherwise>
        </xsl:choose>
      </a>
      <xsl:if test="text()">
        <p>
          <xsl:apply-templates/>
        </p>
      </xsl:if>
    </li>
  </xsl:template>

  <xsl:template match="external">
  </xsl:template>

</xsl:stylesheet>
