<?xml version="1.0"?>

<!--
 * ========================================================================
 * 
 * Copyright 2001-2005 The Apache Software Foundation.
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
<!-- Transform the RDF format to an xdocs news Document -->   
<!-- $Id: rdf2document.xsl 239134 2005-01-30 09:58:23Z vmassol $ -->
<!-- ====================================================================== -->

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:dc="http://purl.org/dc/elements/1.1/">

  <xsl:output indent="yes" method="xml"/>

  <!-- ==================================================================== -->
  <!-- Templates -->
  <!-- ==================================================================== -->

  <xsl:template match="rdf:RDF">
    <document id="news">
      <properties>
        <title><xsl:value-of select="channel/title"/></title>
      </properties>
      <body>
        <section title="Archives">
          <ul>
            <li>
              <link href="site:news_archives_2003">2003 news 
                archives</link>
            </li>
            <li>
              <link href="site:news_archives_2002">2002 news 
                archives</link>
            </li>
            <li>
              <link href="site:news_archives_2001">2001 news 
               archives</link>
            </li>
          </ul>
        </section>
        <section title="News and Events">
          <link href="site:news_rdf">
            <img src="images/rss.png"/>
          </link>          	
          <table>
            <xsl:apply-templates select="item"/>
          </table>
        </section>
      </body>
    </document>
  </xsl:template>

  <xsl:template match="item">
    <anchor id="substring-after(link, '#')"/>
    <tr>
      <th><xsl:value-of select="substring-before(dc:date, 'T')"/></th>
      <td><xsl:value-of select="description" disable-output-escaping="yes"/></td>
    </tr>
  </xsl:template>

</xsl:stylesheet>
