<?xml version="1.0"?>

<!-- ====================================================================== -->
<!-- Transform an xml xdoc into HTML -->
<!-- $Id$ -->
<!-- ====================================================================== -->

<!-- TODOS:
     - finish removing all style references and put them in the CSS. Only use
       CSS features that are standard across browsers (it is possible?). VMA
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="software" select="''"/>
  <xsl:param name="title" select="''"/>
  <xsl:param name="copyright" select="''"/>
  
  <!-- Location of the xdoc directory relative to where this stylesheet is 
       located. Note: this path MUST be relative as it is used as a relative 
       URI from within this stylesheet -->
  <xsl:param name="xdocdir" select="''"/>

  <!-- Location of the sitemap.xml file, which describes the web site 
       resources -->
  <xsl:param name="sitefile" select="''"/>

  <!-- Location of the cvslog.xml file which contains the CVS changelog
       items for the last 15 days web site changes -->
  <xsl:param name="cvslogfile" select="''"/>
  
  <!-- Date of the last update, to be passed in from the build -->
  <xsl:param name="last.updated.date"/>
  
  <!-- Version of the current documentation (for switching between the 
       documentation for the current development version and the latest stable 
       release), to be passed in from the build -->
  <xsl:param name="project.version"/>
  
  <!-- Version of the "other" documentation (for switching between the 
       documentation for the current development version and the latest stable 
       release), to be passed in from the build -->
  <xsl:param name="project.other.version"/>
  
  <!-- Output method -->
  <xsl:output method="html" indent="no"/>
  
  <!-- ==================================================================== -->
  <!-- Global variables -->
  <!-- ==================================================================== -->

  <!-- Defined variables -->
  <xsl:variable name="body-fg"    select="'#000000'"/>
  <xsl:variable name="body-link"  select="'#023264'"/>
  <xsl:variable name="banner-bg"  select="'#023264'"/>
  <xsl:variable name="banner-fg"  select="'#ffffff'"/>

  <!-- Read the resource definitions. They are located in a file named 
       sitemap.xml placed at the same level as the xdoc files. 
       The path we specify is relative to where this stylesheet is located -->
  <xsl:variable name="sitemap" 
    select="document(concat($xdocdir,'/',$sitefile))/document/body/sitemap"/>

  <!-- The current document being processed. Note: This is needed for the
       "get-base-directory" template as it can be called with another
       document context (the $navigation one) -->
  <xsl:variable name="document" select="/document"/>

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

        <!-- CSS imports -->
        <link rel="stylesheet" type="text/css">
          <xsl:attribute name="href">
            <xsl:value-of select="$basedir"/>
            <xsl:text>css/apache.css</xsl:text>
          </xsl:attribute>
        </link>

        <!-- Add the authors as a meta tag -->
        <meta name="author">
          <xsl:attribute name="content">
            <xsl:for-each select="properties/authors/author">
              <xsl:value-of select="@name"/>
              <xsl:if test="not(position()=last())">, </xsl:if>
            </xsl:for-each>
          </xsl:attribute>
        </meta>

        <!-- Add the document title -->
        <title><xsl:call-template name="get-title"/></title>

      </head>

      <body>

        <!-- ============================================================== -->
        <!-- Top level header -->
        <!-- ============================================================== -->

        <table width="100%" cellspacing="0" cellpadding="0" border="0">
          <tr>

            <!-- Display left logo (the Jakarta logo) -->
            <td valign="top" align="left">
              <a href="http://jakarta.apache.org/">
                <img hspace="0" vspace="0" border="0">
                  <xsl:attribute name="src">
                    <xsl:value-of select="$basedir"/>
                    <xsl:text>images/jakarta-logo.gif</xsl:text>
                  </xsl:attribute>
                </img>
              </a>
            </td>

            <td width="100%" valign="middle" align="left" bgcolor="#ffffff">
              <a href="http://jakarta.apache.org/cactus/">
                <img hspace="0" vspace="0" border="0" align="right">
                  <xsl:attribute name="alt">
                    <xsl:call-template name="get-title"/>
                  </xsl:attribute>
                  <xsl:attribute name="src">
                    <xsl:value-of select="$basedir"/>
                    <xsl:text>images/logocactus.gif</xsl:text>
                  </xsl:attribute>
                </img>
              </a>
            </td>
          </tr>
          
          <tr>
            <td width="100%" height="2" colspan="2"><hr noshade="" size="1"/></td>
          </tr>
        </table>

        <!-- ============================================================== -->
        <!-- Main panel (sidebar and content) -->
        <!-- ============================================================== -->

        <table width="100%" cellspacing="0" cellpadding="0" border="0">
          <tr>

            <!-- ========================================================== -->
            <!-- Side bar -->
            <!-- ========================================================== -->

            <td width="1%" valign="top">
            </td>
            <td width="14%" valign="top" nowrap="1">
              <font size="-2">
                Last update: <xsl:value-of select="$last.updated.date"/>
              </font>
              <br/>
              <font size="-2">
                Docs for: <b>v<xsl:value-of select="$project.version"/></b>
                <xsl:text> | </xsl:text>
                <a>
                  <xsl:attribute name="href">
                    <xsl:value-of select="$basedir"/>
                    <xsl:choose>
                      <xsl:when test="contains($project.version,'dev')">
                        <xsl:text>..</xsl:text>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:value-of select="$project.other.version"/>
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:attribute>
                  <xsl:text>v</xsl:text>
                  <xsl:value-of select="$project.other.version"/>
                </a>
              </font>

              <!-- ======================================================== -->
              <!-- Menu -->
              <!-- ======================================================== -->

              <br/>
              <font face="arial,helvetica,sanserif">
                <xsl:call-template name="apply-navigation"/>
              </font>

            </td>

            <!-- ========================================================== -->
            <!-- Content Panel -->
            <!-- ========================================================== -->

            <td width="*" valign="top" align="left">
              <xsl:apply-templates/>
            </td>

          </tr>
        </table>

        <br/>
        
        <!-- ============================================================== -->
        <!-- Footer -->
        <!-- ============================================================== -->

        <table width="100%" border="0" cellspacing="0" cellpadding="0">
          <tr><td><hr noshade="" size="1"/></td></tr>
          <tr>
            <td align="center">
             <font face="arial,helvetica,sanserif" size="-1" color="{$body-link}">
               <i>
                Copyright &#169; <xsl:value-of select="$copyright"/>.
                All Rights Reserved.
               </i>
             </font>
            </td>
          </tr>
        </table>

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

    <xsl:variable name="curid" select="@id"/>
    <xsl:variable name="cursite" select="$sitemap//resource[@id=$curid]"/>

    <xsl:variable name="level" select="count(ancestor::item)+1"/>

    <xsl:choose>
      <xsl:when test="$cursite/@href">
        <div id="menu">
          <font size="-{$level}">
            <a href="{$cursite/@href}" target="{@id}">
              <xsl:value-of select="@label"/>
            </a>
          </font>
          <xsl:apply-templates/>
        </div>
      </xsl:when>
      <xsl:when test="$cursite/@source">
        <div id="menu">
          <a>
            <xsl:attribute name="href">
              <xsl:call-template name="get-base-directory"/>
              <xsl:value-of select="substring($cursite/@source,0,string-length($cursite/@source)-3)"/>
              <xsl:text>.html</xsl:text>
            </xsl:attribute>
            <font size="-{$level}">
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
            </font>
            <xsl:apply-templates/>
          </a>
        </div>
      </xsl:when>
      <xsl:otherwise><!-- hidden --></xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>

  <xsl:template match="separator">
    <br/>
  </xsl:template>

  <xsl:template match="menu">
    <br/>
    <!-- alternate color #F3510C -->
    <font size="+1" color="#000000">
      <xsl:value-of select="@label"/>
    </font>
    <br/>
    <font size="-1">
      <xsl:apply-templates/>
    </font>
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- Sitemap elements: "sitemap/resource" elements -->
  <!-- ==================================================================== -->
  <xsl:template match="sitemap">

    <!-- s1 -->
    <xsl:call-template name="section">
      <xsl:with-param name="width">100%</xsl:with-param>
      <xsl:with-param name="font-size">+1</xsl:with-param>
      <xsl:with-param name="name"><xsl:text>Site Map</xsl:text></xsl:with-param>
    </xsl:call-template>
    
  </xsl:template>

  <xsl:template match="sitemap/resource">
    <xsl:if test="@source">
      <li id="sitemap">
        <!-- link -->
        <a>
          <xsl:attribute name="href">
            <xsl:value-of select="substring(@source,0,string-length(@source)-3)"/>
            <xsl:text>.html</xsl:text>
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
          <xsl:text>:</xsl:text>
          <xsl:apply-templates/>
        </xsl:if>
      </li>
    </xsl:if>
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- "section/s1/s2/s3/s4" elements -->
  <!-- ==================================================================== -->

  <xsl:template match="section">
  
    <xsl:variable name="level" select="count(ancestor::section)+1"/>

    <xsl:choose>
      <xsl:when test="$level=1">
        <xsl:call-template name="section">
          <xsl:with-param name="width">100%</xsl:with-param>
          <xsl:with-param name="font-size">+1</xsl:with-param>
          <xsl:with-param name="name"><xsl:value-of select="@name"/></xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$level=2">
        <xsl:call-template name="section">
          <xsl:with-param name="width">98%</xsl:with-param>
          <xsl:with-param name="font-size">+0</xsl:with-param>
          <xsl:with-param name="name"><xsl:value-of select="@name"/></xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$level=3">
        <xsl:call-template name="section">
          <xsl:with-param name="width">96%</xsl:with-param>
          <xsl:with-param name="font-size">-1</xsl:with-param>
          <xsl:with-param name="name"><xsl:value-of select="@name"/></xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="section">
          <xsl:with-param name="width">94%</xsl:with-param>
          <xsl:with-param name="font-size">-2</xsl:with-param>
          <xsl:with-param name="name"><xsl:value-of select="@name"/></xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <xsl:template match="s1">
    <xsl:call-template name="section">
      <xsl:with-param name="width">100%</xsl:with-param>
      <xsl:with-param name="font-size">+1</xsl:with-param>
      <xsl:with-param name="name"><xsl:value-of select="@title"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="s2">
    <xsl:call-template name="section">
      <xsl:with-param name="width">98%</xsl:with-param>
      <xsl:with-param name="font-size">+0</xsl:with-param>
      <xsl:with-param name="name"><xsl:value-of select="@title"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="s3">
    <xsl:call-template name="section">
      <xsl:with-param name="width">96%</xsl:with-param>
      <xsl:with-param name="font-size">-1</xsl:with-param>
      <xsl:with-param name="name"><xsl:value-of select="@title"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="s4">
    <xsl:call-template name="section">
      <xsl:with-param name="width">94%</xsl:with-param>
      <xsl:with-param name="font-size">-2</xsl:with-param>
      <xsl:with-param name="name"><xsl:value-of select="@title"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="section">
   <xsl:param name="width" />
   <xsl:param name="font-size" />
   <xsl:param name="name" />

   <div align="right">
    <table border="0" cellspacing="0" cellpadding="2">
     <xsl:attribute name="width"><xsl:value-of select="$width"/></xsl:attribute>
     <tr>
      <td bgcolor="{$body-link}">
       <font face="arial,helvetica,sanserif" color="#ffffff">
        <xsl:attribute name="size"><xsl:value-of select="$font-size"/></xsl:attribute>
        <b><xsl:value-of select="$name"/></b>
       </font>
      </td>
     </tr>
     <tr>
      <td>
       <font face="arial,helvetica,sanserif" color="{$body-fg}">
         <br/>
         <xsl:apply-templates/>
       </font>
      </td>
     </tr>
    </table>
   </div>
   <br/>
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- "p" elements -->
  <!-- ==================================================================== -->

  <xsl:template match="p">
    <p align="justify">
      <xsl:apply-templates/>
    </p>
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- "source" elements -->
  <!-- ==================================================================== -->

  <xsl:template match="source">
    <div id="source">
      <pre>
        <xsl:apply-templates/>
      </pre>
    </div>
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- "ul/ol/dl/li/sl/dt/dd" elements -->
  <!-- ==================================================================== -->

  <xsl:template match="ul|ol|dl">
    <blockquote>
      <xsl:copy>
        <xsl:apply-templates/>
      </xsl:copy>
    </blockquote>
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
    <p>
      <table width="100%" cellspacing="3" cellpadding="0" border="0">
        <tr>
          <td width="28" valign="top">
            <img width="28" height="29" vspace="0" 
              hspace="0" border="0" alt="Note">
              <xsl:attribute name="src">
                <xsl:call-template name="get-base-directory"/>
                <xsl:text>images/note.gif</xsl:text>
              </xsl:attribute>
            </img>
          </td>
          <td valign="top">
            <font size="-1" face="arial,helvetica,sanserif" color="{$body-fg}">
              <i>
                <xsl:apply-templates/>
              </i>
            </font>
          </td>
        </tr>  
      </table>
    </p>
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- "table/tr/th/td/tn/caption" elements -->
  <!-- ==================================================================== -->

  <xsl:template match="table">
    <table width="100%" border="0" cellspacing="2" cellpadding="2">
      <caption><xsl:value-of select="caption"/></caption>
      <xsl:apply-templates/>
    </table>
  </xsl:template>

  <xsl:template match="tr">
    <tr><xsl:apply-templates/></tr>
  </xsl:template>

  <xsl:template match="th">
    <td bgcolor="#039acc" colspan="{@colspan}" rowspan="{@rowspan}" 
      valign="center" align="center">
      <font color="#ffffff" size="-1" face="arial,helvetica,sanserif">
        <b><xsl:apply-templates/></b>&#160;
      </font>
    </td>
  </xsl:template>

  <xsl:template match="td">
    <xsl:choose>
      <xsl:when test="@nowrap">
        <td bgcolor="#a0ddf0" colspan="{@colspan}" rowspan="{@rowspan}" 
          nowrap="true" valign="top" align="left">
          <font color="#000000" size="-1" face="arial,helvetica,sanserif">
            <xsl:apply-templates/>&#160;
          </font>
        </td>
      </xsl:when>
      <xsl:otherwise>
        <td bgcolor="#a0ddf0" colspan="{@colspan}" rowspan="{@rowspan}" valign="top" align="left">
          <font color="#000000" size="-1" face="arial,helvetica,sanserif">
            <xsl:apply-templates/>&#160;
          </font>
        </td>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="tn">
    <td bgcolor="#ffffff" colspan="{@colspan}" rowspan="{@rowspan}">
      &#160;
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
    <code><font face="courier, monospaced"><xsl:apply-templates/></font></code>
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- "figure/img/icon" elements -->
  <!-- ==================================================================== -->

  <xsl:template match="figure">
    <p align="center">  
      <xsl:choose>
        <xsl:when test="@width">
          <img alt="{@alt}" width="{@width}" height="{@height}" 
            border="0" vspace="4" hspace="4">
            <xsl:attribute name="src">
              <xsl:call-template name="get-base-directory"/>
              <xsl:value-of select="@src"/>
            </xsl:attribute>
          </img>
        </xsl:when>
        <xsl:otherwise>
          <img alt="{@alt}" border="0" vspace="4" hspace="4">
            <xsl:attribute name="src">
              <xsl:call-template name="get-base-directory"/>
              <xsl:value-of select="@src"/>
            </xsl:attribute>
          </img>
        </xsl:otherwise>
      </xsl:choose>
    </p>
  </xsl:template>
 
  <xsl:template match="img">
    <img alt="{@alt}" border="0" vspace="4" hspace="4" align="right">
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

  <xsl:template match="link">
    <a>
      <xsl:attribute name="href">
        <xsl:call-template name="compute-link-href">
          <xsl:with-param name="href" select="@href"/>
        </xsl:call-template>
      </xsl:attribute>
      <xsl:apply-templates/>
    </a>
  </xsl:template>

  <xsl:template match="jump">
    <a>
      <xsl:attribute name="href">
        <xsl:if test="@href">
          <xsl:call-template name="compute-link-href">
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
        <xsl:call-template name="compute-link-href">
          <xsl:with-param name="href" select="@href"/>
        </xsl:call-template>
      </xsl:attribute>
      <xsl:apply-templates/>
    </a>
  </xsl:template>

  <xsl:template match="anchor">
    <a name="{@id}"><xsl:comment>anchor</xsl:comment></a>
  </xsl:template>  

  <xsl:template name="compute-link-href">
    <xsl:param name="href"/>     
    <xsl:choose>
      <xsl:when test="starts-with(@href,'site:')">
        <xsl:call-template name="get-html-file">
          <xsl:with-param name="id" select="substring-after(@href,'site:')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="starts-with(@href,'ext:')">
        <xsl:variable name="id" select="substring-after(@href,'ext:')"/>
        <xsl:value-of select="$sitemap//resource[@id=$id]/@href"/>
      </xsl:when>
      <xsl:when test="starts-with(@href,'http:')">
        <xsl:value-of select="@href"/>
      </xsl:when>
      <xsl:when test="starts-with(@href,'mailto:')">
        <xsl:value-of select="@href"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message>
          <xsl:text>Bad site id: [</xsl:text>
          <xsl:value-of select="@href"/>
          <xsl:text>]</xsl:text>
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
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

    <!-- s1 -->
    <xsl:call-template name="section">
      <xsl:with-param name="width">100%</xsl:with-param>
      <xsl:with-param name="font-size">+1</xsl:with-param>
      <xsl:with-param name="name"><xsl:value-of select="@title"/></xsl:with-param>
    </xsl:call-template>

  </xsl:template>

  <xsl:template match="release">

    <!-- s2 -->
    <xsl:call-template name="section">
      <xsl:with-param name="width">98%</xsl:with-param>
      <xsl:with-param name="font-size">+0</xsl:with-param>
      <xsl:with-param name="name">
        <xsl:value-of select="$software"/><xsl:text> </xsl:text>
        <xsl:value-of select="@version"/>
        <xsl:if test="@date">
          <xsl:text> (released on </xsl:text>
          <xsl:value-of select="@date"/>
          <xsl:text>)</xsl:text>
        </xsl:if>
      </xsl:with-param>
    </xsl:call-template>

  </xsl:template>

  <xsl:template match="release/action">
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

  <xsl:template match="cvslogs">

    <!-- s1 -->
    <xsl:call-template name="section">
      <xsl:with-param name="width">100%</xsl:with-param>
      <xsl:with-param name="font-size">+1</xsl:with-param>
      <xsl:with-param name="name"><xsl:value-of select="@title"/></xsl:with-param>
    </xsl:call-template>

    <xsl:variable name="cvslog" 
      select="document(concat($xdocdir,'/',$cvslogfile))/changelog"/>

    <xsl:choose>
      <xsl:when test="$cvslog/entry">
        <!-- table -->
        <table width="100%" border="0" cellspacing="2" cellpadding="2">
          <caption><xsl:value-of select="caption"/></caption>
          <xsl:apply-templates select="$cvslog/entry">
            <xsl:sort select="concat(date,time)" order="descending"/>
          </xsl:apply-templates>
        </table>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>&lt;no changes&gt;</xsl:text><br/>
      </xsl:otherwise>
    </xsl:choose>

    <br/>
        
  </xsl:template>

  <xsl:template match="entry">
    <tr>
      <!-- td (nowrap=true) -->
      <td bgcolor="#a0ddf0" colspan="{@colspan}" rowspan="{@rowspan}" 
        nowrap="true" valign="top" align="left">
        <font color="#000000" size="-1" face="arial,helvetica,sanserif">
          <xsl:value-of select="date"/>&#160;
        </font>
      </td>
      <!-- td -->
      <td bgcolor="#a0ddf0" colspan="{@colspan}" rowspan="{@rowspan}" valign="top" align="left">
        <font color="#000000" size="-1" face="arial,helvetica,sanserif">
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
          </xsl:for-each>&#160;
        </font>
      </td>
      <!-- td -->
      <td bgcolor="#a0ddf0" colspan="{@colspan}" rowspan="{@rowspan}" valign="top" align="left">
        <font color="#000000" size="-1" face="arial,helvetica,sanserif">
          <xsl:value-of select="msg"/>&#160;
        </font>
      </td>
    </tr>
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- Todos: "version/category/action" elements -->
  <!-- ==================================================================== -->

  <xsl:template match="version">
    <!-- s1 -->
    <xsl:call-template name="section">
      <xsl:with-param name="width">100%</xsl:with-param>
      <xsl:with-param name="font-size">+1</xsl:with-param>
      <xsl:with-param name="name"><xsl:value-of select="@title"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="category">
    <!-- s2 section -->
    <div align="right">
      <table border="0" cellspacing="0" cellpadding="2" width="98%">
        <tr>
          <td bgcolor="{$body-link}">
            <font face="arial,helvetica,sanserif" color="#ffffff" size="+0">
              <b><xsl:value-of select="@title"/></b>
            </font>
          </td>
        </tr>
        <tr>
          <td>
            <font face="arial,helvetica,sanserif" color="{$body-fg}">
              <br/>
                <!-- table -->
                <table width="100%" border="0" cellspacing="2" cellpadding="2">
                  <caption><xsl:value-of select="caption"/></caption>
                  <tr>
                    <!-- th -->
                    <td bgcolor="#039acc" colspan="{@colspan}" rowspan="{@rowspan}" 
                      valign="center" align="center" width="85%">
                      <font color="#ffffff" size="-1" face="arial,helvetica,sanserif">
                        <b>Description</b>&#160;
                      </font>
                    </td>
                    <!-- th -->
                    <td bgcolor="#039acc" colspan="{@colspan}" rowspan="{@rowspan}" 
                      valign="center" align="center" width="15%">
                      <font color="#ffffff" size="-1" face="arial,helvetica,sanserif">
                        <b>Volunteers</b>&#160;
                      </font>
                    </td>
                  </tr>
                  <xsl:apply-templates/>
                </table>
            </font>
          </td>
        </tr>
      </table>
    </div>
    <br/>
  </xsl:template>  

  <xsl:template match="category/action">
    <tr>
      <!-- td -->
      <td bgcolor="#a0ddf0" colspan="{@colspan}" rowspan="{@rowspan}" valign="top" align="left">
        <font color="#000000" size="-1" face="arial,helvetica,sanserif">
          <xsl:apply-templates/>&#160;
        </font>
      </td>
      <!-- td -->
      <td bgcolor="#a0ddf0" colspan="{@colspan}" rowspan="{@rowspan}" valign="top" align="left">
        <font color="#000000" size="-1" face="arial,helvetica,sanserif">
          <xsl:if test="@assigned-to">
            <xsl:value-of select="@assigned-to"/>
          </xsl:if>&#160;
        </font>
      </td>
    </tr>
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
          <xsl:call-template name="get-source"/>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="perdirnav"
      select="document(concat($xdocdir,'/',dir,'/navigation.xml'))/navigation"/>

    <xsl:choose>
      <xsl:when test="$perdirnav/menu">
        <xsl:apply-templates select="$perdirnav"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates 
          select="document(concat($xdocdir,'/navigation.xml'))/navigation"/>
      </xsl:otherwise>
    </xsl:choose>

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

  <!-- ==================================================================== -->
  <!-- Extract directory -->
  <!-- ==================================================================== -->
 
  <xsl:template name="get-directory">
    <xsl:param name="file"/>     
    <xsl:choose>
      <xsl:when test="contains( $file, '/' )">
        <xsl:variable name="dir" select="substring-before($file, '/')" />
        <xsl:variable name="remainder" select="substring-after($file, '/')" />
        <xsl:variable name="path">
          <xsl:call-template name="get-directory">
            <xsl:with-param name="file" select="$remainder"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:value-of select="concat($dir,$path)"/>
      </xsl:when>
      <xsl:otherwise/>
    </xsl:choose>
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- Get current processed file source path -->
  <!-- ==================================================================== -->

  <xsl:template name="get-source-from-id">
    <xsl:param name="id"/>     

    <!-- Issue a warning if the id is invalid -->
    <xsl:if test="not($sitemap//resource[@id=$id])">
      <xsl:message>
        <xsl:text>Id [</xsl:text>
        <xsl:value-of select="$id"/>
        <xsl:text>] has no reference in sitemap.xml</xsl:text>
      </xsl:message>   
    </xsl:if>
    
    <xsl:value-of select="$sitemap//resource[@id=$id]/@source"/>
  </xsl:template>

  <xsl:template name="get-source">
    <xsl:call-template name="get-source-from-id">
      <xsl:with-param name="id" select="$document/@id"/>
    </xsl:call-template>
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- Compute html file from xml source file -->
  <!-- ==================================================================== -->

  <xsl:template name="get-html-file">
    <xsl:param name="id"/>     
    <xsl:variable name="source">
      <xsl:call-template name="get-source-from-id">
        <xsl:with-param name="id" select="$id"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:call-template name="get-base-directory"/>
    <xsl:value-of select="substring($source,0,string-length($source)-3)"/>
    <xsl:text>.html</xsl:text>
  </xsl:template>
  
  <!-- ==================================================================== -->
  <!-- Compute base directory -->
  <!-- ==================================================================== -->

  <xsl:template name="get-base-directory">  
    <xsl:call-template name="get-base-directory-internal">
      <xsl:with-param name="file">
        <xsl:call-template name="get-source"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template name="get-base-directory-internal">
    <xsl:param name="file"/>     
    <xsl:choose>
      <xsl:when test="contains( $file, '/' )">
        <xsl:variable name="remainder" select="substring-after($file, '/')" />
        <xsl:variable name="path">
          <xsl:call-template name="get-base-directory-internal">
            <xsl:with-param name="file" select="$remainder"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:value-of select="concat('../',$path)"/>
      </xsl:when>
      <xsl:otherwise>./</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
