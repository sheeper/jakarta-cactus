<?xml version="1.0"?>

<!-- ====================================================================== -->
<!-- Transform an xml xdoc into HTML -->
<!-- $Id$ -->
<!-- ====================================================================== -->

<!-- TODOS:
     - modify <figure> to support site: and ext: notations
     - add warnings for external <link> not using ext:
-->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- Import common templates and parameters -->
  <xsl:import href="common.xsl"/>

  <xsl:output method="html"
      doctype-public="-//W3C//DTD HTML 4.0 Transitional//EN"
      doctype-system="http://www.w3.org/TR/html4/loose.dtd"/>

  <!-- ==================================================================== -->
  <!-- Parameters -->
  <!-- ==================================================================== -->

  <!-- Location of the cvslog.xml file which contains the CVS changelog
       items for the last 15 days web site changes -->
  <xsl:param name="cvslogfile" select="''"/>

  <!-- Version of the current documentation (for switching between the 
       documentation for the current development version and the latest stable 
       release), to be passed in from the build -->
  <xsl:param name="project.version"/>

  <!-- Version of the "other" documentation (for switching between the 
       documentation for the current development version and the latest stable 
       release), to be passed in from the build -->
  <xsl:param name="project.version.previous"/>

  <!-- ==================================================================== -->
  <!-- Document section -->
  <!-- ==================================================================== -->

  <xsl:template match="document">

    <!-- Base directory for the current processed document (see the
         "get-base-directory" template for more info) -->
    <xsl:variable name="basedir">
      <xsl:call-template name="get-base-directory"/>
    </xsl:variable>
    
    <html>

      <head>

        <!-- Add the authors as a meta tag -->
        <meta name="author">
          <xsl:attribute name="content">
            <xsl:for-each select="properties/authors/author">
              <xsl:value-of select="@name"/>
              <xsl:if test="not(position()=last())">, </xsl:if>
            </xsl:for-each>
          </xsl:attribute>
        </meta>

        <!-- CSS stylesheet -->
        <link rel="stylesheet" type="text/css">
          <xsl:attribute name="href">
            <xsl:value-of select="$basedir"/>
            <xsl:text>css/apache.css</xsl:text>
          </xsl:attribute>
        </link>

        <!-- Add the document title -->
        <title><xsl:call-template name="get-title"/></title>

      </head>

      <body text="#000000" link="#525D76" vlink="#023264" alink="#023264">

        <!-- ============================================================== -->
        <!-- Header -->
        <!-- ============================================================== -->

        <div id="header">
          <table border="0" width="100%">
            <tr>
              <td width="50%">
                <div id="projectLogo">
                  <a href="http://jakarta.apache.org/">
                    <img border="0">
                      <xsl:attribute name="alt">
                        <xsl:text>The Apache Jakarta Project</xsl:text>
                      </xsl:attribute>
                      <xsl:attribute name="src">
                        <xsl:value-of select="$basedir"/>
                        <xsl:text>images/jakarta-logo.gif</xsl:text>
                      </xsl:attribute>
                    </img>
                  </a>
                </div>
              </td>
              <td width="50%">
                <div id="subprojectLogo">
                  <a href="http://jakarta.apache.org/cactus/">
                    <img>
                      <xsl:attribute name="alt">
                        <xsl:call-template name="get-title"/>
                      </xsl:attribute>
                      <xsl:attribute name="src">
                        <xsl:value-of select="$basedir"/>
                        <xsl:text>images/logocactus.gif</xsl:text>
                      </xsl:attribute>
                    </img>
                  </a>
                </div>
              </td>
            </tr>
          </table>
          <div id="contextBar">
            <table width="100%">
              <tr>
                <td id="breadCrumbs" width="50%">
                  <xsl:call-template name="generate-breadcrumbs"/>
                </td>
                <td id="status" width="50%">
                  Docs for:
                  <strong>v<xsl:value-of select="$project.version"/></strong>
                  <xsl:text> | </xsl:text>
                  <a>
                    <xsl:attribute name="href">
                      <xsl:value-of select="$basedir"/>
                      <xsl:choose>
                        <xsl:when test="contains($project.version,'dev')">
                          <xsl:value-of select="$project.version.previous"/>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:text>..</xsl:text>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:attribute>
                    <xsl:text>v</xsl:text>
                    <xsl:value-of select="$project.version.previous"/>
                  </a>
                  &#160;&#160;
                  Last update: <xsl:value-of select="$last.updated.date"/>
                </td>
              </tr>
            </table>
          </div>
        </div>

        <div id="main">

          <table width="100%" border="0" cellpadding="0" cellspacing="0">
            <tr>

              <!-- ======================================================== -->
              <!-- Sidebar -->
              <!-- ======================================================== -->

              <td valign="top">
                <div id="sidebar">
                  <div id="navigation">
                    <xsl:call-template name="apply-navigation"/>
                  </div>
                </div>
              </td>

              <!-- ======================================================== -->
              <!-- Content -->
              <!-- ======================================================== -->

              <td valign="top">
                <div id="content">
                  <xsl:apply-templates/>
                </div>
              </td>

            </tr>
          </table>

        </div>

        <!-- ============================================================== -->
        <!-- Footer -->
        <!-- ============================================================== -->

        <div id="footer">
          <p>
            Copyright &#169; <xsl:value-of select="$copyright"/>.
            All Rights Reserved.
          </p>
        </div>

      </body>

    </html>
    
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- "properties" elements -->
  <!-- ==================================================================== -->

  <xsl:template match="properties">
    <!-- Ignore "properties" elements -->
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- Menu elements: "menu/menu-item/separator" elements -->
  <!-- ==================================================================== -->

  <xsl:template match="menu//item">
    <li class="menuItem">
      <xsl:call-template name="generate-navigation-entry">
        <xsl:with-param name="node" select="."/>
      </xsl:call-template>
    </li>
  </xsl:template>

  <xsl:template match="menu">
    <li class="menu">
      <xsl:choose>
        <xsl:when test="@id">
          <xsl:call-template name="generate-navigation-entry">
            <xsl:with-param name="node" select="."/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="@label"/>
        </xsl:otherwise>
      </xsl:choose>
      <ul>
        <xsl:apply-templates/>
      </ul>
    </li>
  </xsl:template>

  <xsl:template name="generate-navigation-entry">
    <xsl:param name="node"/>
    <xsl:variable name="curid" select="$node/@id"/>
    <xsl:variable name="cursite" select="$sitemap//*[@id=$curid]"/>
    <xsl:choose>
      <xsl:when test="name($cursite) = 'external'">
        <a href="{$cursite/@url}" target="{$curid}">
          <!-- Use the label from the sitemap if none has been defined
               in the navigation file -->
          <xsl:choose>
            <xsl:when test="@label">
              <xsl:value-of select="@label"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$cursite/@name"/>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:apply-templates/>
        </a>
      </xsl:when>
      <xsl:when test="name($cursite) = 'resource'">
        <a>
          <xsl:attribute name="href">
            <xsl:call-template name="get-base-directory"/>
            <xsl:value-of select="$cursite/@target"/>
          </xsl:attribute>
          <xsl:attribute name="title">
            <xsl:variable name="description">
              <xsl:call-template name="get-resource-description">
                <xsl:with-param name="id" select="$curid"/>
              </xsl:call-template>
            </xsl:variable>
            <xsl:value-of select="normalize-space($description)"/>
          </xsl:attribute>
          <xsl:if test="$curid = $document/@id">
            <xsl:attribute name="class">currentPage</xsl:attribute>
          </xsl:if>
          <!-- Use the label from the sitemap if none has been defined
               in the navigation file -->
          <xsl:choose>
            <xsl:when test="@label">
              <xsl:value-of select="@label"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$cursite/@name"/>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:apply-templates/>
        </a>
      </xsl:when>
      <xsl:otherwise><!-- hidden --></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- "section/s1/s2/s3/s4" elements -->
  <!-- ==================================================================== -->

  <xsl:template match="section">
    <xsl:variable name="level" select="count(ancestor::section)+1"/>
    <xsl:call-template name="section">
      <xsl:with-param name="level"><xsl:value-of select="$level"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="s1">
    <xsl:call-template name="section">
      <xsl:with-param name="level">1</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="s2">
    <xsl:call-template name="section">
      <xsl:with-param name="level">2</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="s3">
    <xsl:call-template name="section">
      <xsl:with-param name="level">3</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="s4">
    <xsl:call-template name="section">
      <xsl:with-param name="level">4</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="section">
    <xsl:param name="level"/>
    <div class="section">
      <xsl:choose>
        <xsl:when test="$level=1">
          <h1><xsl:value-of select="@title"/></h1>
        </xsl:when>
        <xsl:when test="$level=2">
          <h2><xsl:value-of select="@title"/></h2>
        </xsl:when>
        <xsl:when test="$level=3">
          <h3><xsl:value-of select="@title"/></h3>
        </xsl:when>
        <xsl:when test="$level=4">
          <h4><xsl:value-of select="@title"/></h4>
        </xsl:when>
        <xsl:otherwise>
          <h5><xsl:value-of select="@title"/></h5>
        </xsl:otherwise>
      </xsl:choose>
      <blockquote>
        <xsl:apply-templates/>
      </blockquote>
    </div>
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- "p" elements -->
  <!-- ==================================================================== -->

  <xsl:template match="p">
    <p>
      <xsl:apply-templates/>
    </p>
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- "source" elements -->
  <!-- ==================================================================== -->

  <xsl:template match="source">
    <div class="source">
      <pre>
        <xsl:apply-templates/>
      </pre>
    </div>
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- "ul/ol/dl/li/sl/dt/dd" elements -->
  <!-- ==================================================================== -->

  <xsl:template match="ul|ol|dl">
    <xsl:copy>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
 
  <xsl:template match="li">
    <xsl:copy><xsl:apply-templates/></xsl:copy>
  </xsl:template>

  <xsl:template match="sl">
    <ul><xsl:apply-templates/></ul>
  </xsl:template>

  <xsl:template match="dt">
    <li>
      <strong><xsl:value-of select="."/></strong>
      <xsl:text> - </xsl:text>
      <xsl:value-of select="following::dd"/>
    </li>
  </xsl:template>
 
  <xsl:template match="dd">
    <!-- ignore since already used -->
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- "note" elements -->
  <!-- ==================================================================== -->

  <xsl:template match="note">
    <div class="note">
      <xsl:apply-templates/>
    </div>
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- "table/tr/th/td/tn/caption" elements -->
  <!-- ==================================================================== -->

  <xsl:template match="table">
    <div class="tabular">
      <table border="1" cellspacing="2" cellpadding="2">
        <caption><xsl:apply-templates select="caption"/></caption>
        <xsl:apply-templates/>
      </table>
    </div>
  </xsl:template>

  <xsl:template match="tr">
    <tr><xsl:apply-templates/></tr>
  </xsl:template>

  <xsl:template match="th">
    <th>
      <xsl:if test="@colspan">
        <xsl:attribute name="colspan">
          <xsl:value-of select="@colspan"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@rowspan">
        <xsl:attribute name="rowspan">
          <xsl:value-of select="@rowspan"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates/>
    </th>
  </xsl:template>

  <xsl:template match="td">
    <td>
      <xsl:if test="@colspan">
        <xsl:attribute name="colspan">
          <xsl:value-of select="@colspan"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@rowspan">
        <xsl:attribute name="rowspan">
          <xsl:value-of select="@rowspan"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@nowrap">
        <xsl:attribute name="nowrap">true</xsl:attribute>
      </xsl:if>
      <xsl:apply-templates/>
    </td>
  </xsl:template>

  <xsl:template match="caption">
    <!-- ignore since already used -->
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- "strong/em/filename/code" elements -->
  <!-- ==================================================================== -->

  <xsl:template match="strong">
    <b><xsl:apply-templates/></b>
  </xsl:template>

  <xsl:template match="em">
    <i><xsl:apply-templates/></i>
  </xsl:template>

  <xsl:template match="filename">
    <filename><i><xsl:apply-templates/></i></filename>
  </xsl:template>

  <xsl:template match="code">
    <code><xsl:apply-templates/></code>
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- "figure/img/icon" elements -->
  <!-- ==================================================================== -->

  <xsl:template match="figure">
    <div class="figure">
      <xsl:choose>
        <xsl:when test="@width">
          <img alt="{@alt}" border="0" width="{@width}" height="{@height}">
            <xsl:attribute name="src">
              <xsl:call-template name="get-base-directory"/>
              <xsl:value-of select="@src"/>
            </xsl:attribute>
            <xsl:if test="@usemap">
              <xsl:attribute name="usemap">
                <xsl:value-of select="@usemap"/>
              </xsl:attribute>
            </xsl:if>
            <xsl:apply-templates/>
          </img>
        </xsl:when>
        <xsl:otherwise>
          <img alt="{@alt}" border="0">
            <xsl:attribute name="src">
              <xsl:call-template name="get-base-directory"/>
              <xsl:value-of select="@src"/>
            </xsl:attribute>
            <xsl:if test="@usemap">
              <xsl:attribute name="usemap">
                <xsl:value-of select="@usemap"/>
              </xsl:attribute>
            </xsl:if>
            <xsl:apply-templates/>
          </img>
        </xsl:otherwise>
      </xsl:choose>
    </div>
  </xsl:template>
 
  <xsl:template match="figure/map">
    <map name="{@name}">
      <xsl:apply-templates/>
    </map>
  </xsl:template>

  <xsl:template match="figure/map/area">
    <area shape="{@shape}" coords="{@coords}">
      <xsl:attribute name="href">
        <xsl:call-template name="get-link-href">
          <xsl:with-param name="href" select="@href"/>
        </xsl:call-template>
      </xsl:attribute>
    </area>
  </xsl:template>
  
  <xsl:template match="img">
    <img alt="{@alt}" border="0" vspace="0" hspace="0">
      <xsl:attribute name="src">
        <xsl:call-template name="get-base-directory"/>
        <xsl:value-of select="@src"/>
      </xsl:attribute>
    </img>
  </xsl:template>

  <xsl:template match="icon">
    <img alt="{@alt}" border="0" align="absmiddle">
      <xsl:attribute name="src">
        <xsl:call-template name="get-base-directory"/>
        <xsl:value-of select="@src"/>
      </xsl:attribute>
    </img>
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- "a/link/jump/fork/anchor" elements -->
  <!-- ==================================================================== -->

  <!-- VMA: Temporary hack. This is used by the RDF to Document 
  	   transformation to support <a href> tags in the <description> field
  	   of the RSS feed. A better solution is to transform this stylesheet
  	   to let unknown elements go through, but I don't know how to do that! -->
  <xsl:template match="a">
    <a>
      <xsl:attribute name="href">
        <xsl:call-template name="get-link-href">
          <xsl:with-param name="href" select="@href"/>
        </xsl:call-template>
      </xsl:attribute>
      <xsl:attribute name="title">
      	<xsl:variable name="title">
          <xsl:call-template name="get-link-title">
            <xsl:with-param name="href" select="@href"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:value-of select="normalize-space($title)"/>
      </xsl:attribute>
      <xsl:apply-templates/>
    </a>
  </xsl:template>
  
  <xsl:template match="link">
    <a>
      <xsl:attribute name="href">
        <xsl:call-template name="get-link-href">
          <xsl:with-param name="href" select="@href"/>
        </xsl:call-template>
      </xsl:attribute>
      <xsl:attribute name="title">
      	<xsl:variable name="title">
          <xsl:call-template name="get-link-title">
            <xsl:with-param name="href" select="@href"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:value-of select="normalize-space($title)"/>
      </xsl:attribute>
      <xsl:apply-templates/>
    </a>
  </xsl:template>

  <xsl:template match="jump">
    <a>
      <xsl:attribute name="href">
        <xsl:if test="@href">
          <xsl:call-template name="get-link-href">
            <xsl:with-param name="href" select="@href"/>
          </xsl:call-template>
        </xsl:if>
        <xsl:text>#</xsl:text>
        <xsl:value-of select="@anchor"/>
      </xsl:attribute>
      <xsl:apply-templates/>
    </a>
  </xsl:template>

  <xsl:template match="fork">
    <a target="_blank">
      <xsl:attribute name="href">
        <xsl:call-template name="get-link-href">
          <xsl:with-param name="href" select="@href"/>
        </xsl:call-template>
      </xsl:attribute>
      <xsl:apply-templates/>
    </a>
  </xsl:template>

  <xsl:template match="anchor">
    <a name="{@id}"><xsl:comment>anchor</xsl:comment></a>
  </xsl:template>  

  <xsl:template name="get-link-href">
    <xsl:param name="href"/>
    <xsl:choose>
      <xsl:when test="starts-with(@href,'site:')">
        <xsl:call-template name="get-target-file">
          <xsl:with-param name="id" select="substring-after(@href,'site:')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="starts-with(@href,'ext:')">
        <xsl:variable name="id" select="substring-after(@href,'ext:')"/>
        <xsl:value-of select="$sitemap//external[@id=$id]/@url"/>
      </xsl:when>
      <xsl:when test="starts-with(@href,'http:')">
        <xsl:value-of select="@href"/>
      </xsl:when>
      <xsl:when test="starts-with(@href,'ftp:')">
        <xsl:value-of select="@href"/>
      </xsl:when>
      <xsl:when test="starts-with(@href,'mailto:')">
        <xsl:value-of select="@href"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes">
          <xsl:text>Bad site id: [</xsl:text>
          <xsl:value-of select="@href"/>
          <xsl:text>]</xsl:text>
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="get-link-title">
    <xsl:param name="href"/>
    <xsl:if test="starts-with(@href,'site:')">
      <xsl:call-template name="get-resource-description">
        <xsl:with-param name="id" select="substring-after(@href, 'site:')"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- "br" elements -->
  <!-- ==================================================================== -->

  <xsl:template match="br">
    <br/>
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- "footer/fixme" elements -->
  <!-- ==================================================================== -->

  <xsl:template match="footer">
    <!-- ignore on general documents -->
  </xsl:template>
 
  <xsl:template match="fixme">
    <!-- ignore on documentation -->
  </xsl:template>
 
  <!-- ==================================================================== -->
  <!-- Changes: "releases/release/action/devs" elements -->
  <!-- ==================================================================== -->

  <xsl:template match="releases">
    <div class="section">
      <h1><xsl:value-of select="@title"/></h1>
      <xsl:apply-templates select="release"/>
    </div>
  </xsl:template>

  <xsl:template match="release">
    <div class="section">
      <h2>
        <xsl:value-of select="$software"/><xsl:text> </xsl:text>
        <xsl:value-of select="@version"/>
        <xsl:if test="@date">
          <xsl:text> (released on </xsl:text>
          <xsl:value-of select="@date"/>
          <xsl:text>)</xsl:text>
        </xsl:if>
      </h2>
      <ul class="changes">
        <xsl:apply-templates select="action"/>
      </ul>
    </div>
  </xsl:template>

  <xsl:template match="action">
    <li>
      <!-- icon -->
      <img alt="{@type}" border="0" align="absmiddle">
        <xsl:attribute name="src">
          <xsl:call-template name="get-base-directory"/>
          <xsl:text>images/</xsl:text>
          <xsl:value-of select="@type"/>
          <xsl:text>.jpg</xsl:text>
        </xsl:attribute>
      </img>
      <xsl:apply-templates/>
      <xsl:text>(</xsl:text><xsl:value-of select="@dev"/><xsl:text>)</xsl:text>
      <xsl:if test="@due-to">
        <xsl:text> Thanks to </xsl:text>
         <!-- link -->
         <a href="mailto:{@due-to-email}"><xsl:value-of select="@due-to"/></a>
        <xsl:text>.</xsl:text>
      </xsl:if>
      <xsl:if test="@fixes-bug">
        <xsl:text> Fixes </xsl:text>
         <!-- link -->
         <a href="http://nagoya.apache.org/bugzilla/show_bug.cgi?id={@fixes-bug}">
           <xsl:text>bug </xsl:text><xsl:value-of select="@fixes-bug"/>
        </a>
        <xsl:text>.</xsl:text>
      </xsl:if>
    </li>
  </xsl:template>

  <xsl:template match="devs">
    <!-- remove -->
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- Changelog: "cvslogs/changelog/entry" elements -->
  <!-- ==================================================================== -->

  <xsl:template match="cvschangelog">
    <xsl:variable name="changelog" 
      select="document(concat($xdocdir,'/',$cvslogfile))/changelog"/>
    <xsl:choose>
      <xsl:when test="$changelog/entry">
        <div class="tabular">
          <table width="100%" border="1" cellspacing="2" cellpadding="2">
            <caption><xsl:value-of select="caption"/></caption>
            <xsl:apply-templates select="$changelog/entry">
              <xsl:sort select="concat(date,time)" order="descending"/>
            </xsl:apply-templates>
          </table>
        </div>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>&lt;no changes&gt;</xsl:text><br/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="entry">
    <tr>
      <th nowrap="true">
        <xsl:value-of select="date"/>
      </th>
      <td>
        <xsl:for-each select="file">
          <a>
            <xsl:attribute name="href">
              <xsl:call-template name="get-base-directory"/>
              <xsl:value-of select="concat(substring-before(name, '.'),'.html')"/>
            </xsl:attribute>
            <xsl:value-of select="substring-before(name, '.')"/>
          </a>
          <xsl:if test="position()!=last()">
            <xsl:text>, </xsl:text>
          </xsl:if>
        </xsl:for-each>
      </td>
      <td>
        <xsl:value-of select="msg"/>
      </td>
    </tr>
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- Todos: "version/category/action" elements -->
  <!-- ==================================================================== -->

  <xsl:template match="version">
    <xsl:call-template name="section">
      <xsl:with-param name="level">1</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="category">
    <!-- s2 section -->
    <div class="section">
      <h2><xsl:value-of select="@title"/></h2>
      <div class="tabular">
        <table width="100%" border="1" cellspacing="2" cellpadding="2">
          <caption><xsl:value-of select="caption"/></caption>
          <tr>
            <th width="85%">Description</th>
            <th width="15%">Volunteers</th>
          </tr>
          <xsl:apply-templates/>
        </table>
      </div>
    </div>
  </xsl:template>

  <xsl:template match="category/action">
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

  <!-- ==================================================================== -->
  <!-- Generate the breadcrumbs trail -->
  <!-- ==================================================================== -->
  <xsl:template name="generate-breadcrumbs">
    <!-- Per directory navigation file -->
    <xsl:param name="dir">
      <xsl:call-template name="get-directory">
        <xsl:with-param name="file">
          <xsl:call-template name="get-target"/>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:param>
    <xsl:choose>
      <xsl:when
          test="document(concat($xdocdir,'/',$dir,'../navigation.xml'))/navigation">
        <xsl:call-template name="generate-breadcrumbs">
          <xsl:with-param name="dir" select="concat($dir,'../')"/>
        </xsl:call-template>
        <xsl:text> &gt; </xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <a href="http://www.apache.org/">Apache</a>
        <xsl:text> &gt; </xsl:text>
        <a href="http://jakarta.apache.org/">Jakarta</a>
        <xsl:text> &gt; </xsl:text>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:variable name="curnav"
        select="document(concat($xdocdir,'/',$dir,'navigation.xml'))/navigation"/>
    <a>
      <xsl:attribute name="href">
        <xsl:call-template name="get-target-file">
          <xsl:with-param name="id" select="$curnav/@index"/>
        </xsl:call-template>
      </xsl:attribute>
      <xsl:value-of select="$curnav/@title"/>
    </a>
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- Apply the navigation file -->
  <!-- Note: The navigation.xml files must exist but can be left empty, i.e -->
  <!-- containing only <navigation/>. In which case, the top level menu is  -->
  <!-- used. -->
  <!-- ==================================================================== -->
  <xsl:template name="apply-navigation">
    
    <!-- Per directory navigation file -->
    <xsl:variable name="dir">
      <xsl:call-template name="get-directory">
        <xsl:with-param name="file">
          <xsl:call-template name="get-target"/>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="perdirnav"
      select="document(concat($xdocdir,'/',$dir,'/navigation.xml'))/navigation"/>

    <ul>
      <xsl:choose>
        <xsl:when test="$perdirnav/menu">
          <xsl:apply-templates select="$perdirnav"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates 
            select="document(concat($xdocdir,'/navigation.xml'))/navigation"/>
        </xsl:otherwise>
      </xsl:choose>
    </ul>

  </xsl:template>

  <!-- ==================================================================== -->
  <!-- Return the document title -->
  <!-- ==================================================================== -->
  <xsl:template name="get-title">
    <xsl:choose>
      <xsl:when test="/document/properties/title">
        <xsl:value-of select="/document/properties/title"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$title"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
</xsl:stylesheet>
