<?xml version="1.0"?>

<!-- ====================================================================== -->
<!-- Transform an xml xdoc into HTML -->
<!-- $Id$ -->
<!-- ====================================================================== -->

<!-- TODOS:
     - remove all color references and expose them as variables
     - try to have a single XSL that calls other XSL for dealing with
       the different page types as defined in book.xml
     - remove stylesheet images (add, update, fix, remove) from
       xdocs/images
     - handle xdocs located in sub-directories (issue is with the 
       relative path to the images)
     - migrate the xdocs to the new <properties> instead of <head>
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- Location of the book.xml file, which describes the web site meta
       data such as the menu items, etc -->
  <xsl:param name="bookfile" select="''"/>

  <!-- Location of the cvslog.xml file which contains the CVS changelog
       items for the last 15 days web site changes -->
  <xsl:param name="cvslogfile" select="''"/>
  
  <!-- Output method -->
  <xsl:output method="html" indent="no"/>

  <!-- Defined variables -->
  <xsl:variable name="body-bg"    select="'#ffffff'"/>
  <xsl:variable name="body-fg"    select="'#000000'"/>
  <xsl:variable name="body-link"  select="'#023264'"/>
  <xsl:variable name="banner-bg"  select="'#023264'"/>
  <xsl:variable name="banner-fg"  select="'#ffffff'"/>

  <!-- Read the menu definitions. It is located in a file named book.xml
       placed at the same level as the xdoc files. The path we specify is
       relative to where this stylesheet is located -->
  <xsl:variable name="book" select="document($bookfile)/book"/>

  <!-- ==================================================================== -->
  <!-- Document section -->
  <!-- ==================================================================== -->

  <xsl:template match="document">
       
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

        <!-- Add the document title -->
        <title><xsl:call-template name="get-title"/></title>

      </head>

      <body bgcolor="{$body-bg}" text="{$body-fg}" link="{$body-link}"
          alink="{$body-link}" vlink="{$body-link}">

        <!-- ============================================================== -->
        <!-- Top level header -->
        <!-- ============================================================== -->

        <table width="100%" cellspacing="0" cellpadding="0" border="0">
          <tr>

            <!-- Display left logo (the Jakarta logo) -->
            <td valign="top" align="left">
              <a href="http://jakarta.apache.org/index.html">
                <img hspace="0" vspace="0" border="0">
                  <xsl:attribute name="src">images/jakarta-logo.gif</xsl:attribute>
                </img>
              </a>
            </td>

            <td width="100%" valign="middle" align="left" bgcolor="#ffffff">
              <img hspace="0" vspace="0" border="0" align="right">
                <xsl:attribute name="alt">
                  <xsl:call-template name="get-title"/>
                </xsl:attribute>
                <xsl:attribute name="src">images/logocactus.gif</xsl:attribute>
              </img>
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
                Last update : <xsl:value-of select="$book/@updated"/>
              </font>
              <br/>
              <font size="-2">
                Doc for : <b>v<xsl:value-of select="$book/@currentversion"/></b>
                | 
                <a>
                  <xsl:attribute name="href">
                    <xsl:choose>
                      <xsl:when test="contains($book/@currentversion,'dev')">
                        ..
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:value-of select="$book/@otherversion"/>
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:attribute>
                  v<xsl:value-of select="$book/@otherversion"/>
                </a>
              </font>

              <!-- ======================================================== -->
              <!-- Menu -->
              <!-- ======================================================== -->

              <br/>
              <font face="arial,helvetica,sanserif">
                <xsl:apply-templates select="$book"/>
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
                Copyright &#169; <xsl:value-of select="$book/@copyright"/>.
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

  <xsl:template match="menu-item">

    <xsl:choose>
      <xsl:when test="@type='external'">
        <li>
          <font size="-1">
            <a href="{@href}" target="{@id}"><xsl:value-of select="@label"/></a>
          </font>
        </li>
      </xsl:when>
      <xsl:when test="not(@type) or @type!='hidden'">
        <li>
          <a>
            <xsl:attribute name="href"><xsl:value-of 
              select="substring(@source,0,string-length(@source)-3)"/>.html</xsl:attribute>
            <font size="-1"><xsl:value-of select="@label"/></font>
         </a>
       </li>
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
    <font size="+1" color="#000000"><xsl:value-of select="@label"/></font>
    <br/>
    <font size="-1"><xsl:apply-templates/></font>
    <br/>
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
         <br/><xsl:apply-templates/>
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
    <p align="justify"><xsl:apply-templates/></p>
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- "source" elements -->
  <!-- ==================================================================== -->

  <xsl:template match="source">
   <div align="center">
    <table border="1" cellspacing="2" cellpadding="2">
    <tr>
       <td><pre><xsl:apply-templates/></pre></td>
    </tr>
    </table>
   </div>
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- "ul/ol/dl/li/sl/dt/dd" elements -->
  <!-- ==================================================================== -->

  <xsl:template match="ul|ol|dl">
    <blockquote>
      <xsl:copy><xsl:apply-templates/></xsl:copy>
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
            <img src="images/note.gif" width="28" height="29" vspace="0" 
              hspace="0" border="0" alt="Note"/>
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
          <img src="{@src}" alt="{@alt}" width="{@width}" height="{@height}" 
            border="0" vspace="4" hspace="4"/>
        </xsl:when>
        <xsl:otherwise>
          <img src="{@src}" alt="{@alt}" border="0" vspace="4" hspace="4"/>
        </xsl:otherwise>
      </xsl:choose>
    </p>
  </xsl:template>
 
  <xsl:template match="img">
    <img src="{@src}" alt="{@alt}" border="0" vspace="4" hspace="4" 
      align="right"/>
  </xsl:template>

  <xsl:template match="icon">
    <img src="{@src}" alt="{@alt}" border="0" align="absmiddle"/>
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- "a/link/jump/fork/anchor" elements -->
  <!-- ==================================================================== -->

  <xsl:template match="a">
    <a href="{@href}"><xsl:apply-templates/></a>
  </xsl:template>

  <xsl:template match="link">
    <a href="{@href}"><xsl:apply-templates/></a>
  </xsl:template>

  <xsl:template match="jump">
    <a href="{@href}#{@anchor}"><xsl:apply-templates/></a>
  </xsl:template>

  <xsl:template match="fork">
    <a href="{@href}" target="_blank"><xsl:apply-templates/></a>
  </xsl:template>

  <xsl:template match="anchor">
    <a name="{@id}"><xsl:comment>anchor</xsl:comment></a>
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
        <xsl:value-of select="$book/@software"/><xsl:text> </xsl:text>
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
      <img src="images/{@type}.jpg" alt="{@type}" border="0" align="absmiddle"/>

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
         <a href="http://xml.apache.org/bugs/show_bug.cgi?id={@fixes-bug}">
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

    <xsl:variable name="cvslog" select="document($cvslogfile)/changelog"/>

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
            <link href="{concat(substring-before(name, '.'),'.html')}">
              <xsl:value-of select="substring-before(name, '.')"/>
            </link>
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
  <!-- Return the document title -->
  <!-- ==================================================================== -->
  <xsl:template name="get-title">
    <xsl:choose>
      <xsl:when test="/document/properties/title">
        <xsl:value-of select="/document/properties/title"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$book/@title"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
