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
          <xsl:apply-templates select="resource">
            <xsl:sort select="@name|@id"/>
          </xsl:apply-templates>
        </section>
      </body>
    </document>
  </xsl:template>

  <xsl:template match="resource">
    <li class="sitemap">
      <link>
        <xsl:attribute name="href">
          <xsl:value-of select="concat('site:', @id)"/>
        </xsl:attribute>
        <strong>
          <xsl:choose>
            <xsl:when test="@name">
              <xsl:value-of select="@name"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="@id"/>
            </xsl:otherwise>
          </xsl:choose>
        </strong>
      </link>
      <xsl:text>: </xsl:text>
      <xsl:if test="text()">
        <xsl:apply-templates/>
      </xsl:if>
    </li>
  </xsl:template>

  <xsl:template match="external">
  </xsl:template>

</xsl:stylesheet>
