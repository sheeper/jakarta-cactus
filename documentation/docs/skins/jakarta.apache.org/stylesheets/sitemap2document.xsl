<?xml version="1.0"?>

<!--
 * ========================================================================
 * 
 * Copyright 2001-2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ========================================================================
-->

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
          <p>
            The following list gives an overview of all documents on the Cactus
            web site.
          </p>
          <xsl:apply-templates select="resource[@name]">
            <xsl:sort select="@name"/>
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
