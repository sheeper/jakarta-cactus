<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="stylebook.project"/>
  <xsl:param name="copyright"/>
  <xsl:param name="docid"/>
  <xsl:param name="target"/>

  <xsl:output method="html" indent="yes"/>
<!--  <xsl:strip-space elements="*"/> -->

  <!-- voodoo magic to calculate base directory -->
  <xsl:template name="get-base-directory">  
    <xsl:call-template name="get-base-directory-internal">
      <xsl:with-param name="file" select="$docid"/>
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

<!-- ====================================================================== -->
<!-- document section -->
<!-- ====================================================================== -->

 <xsl:template match="document">
    <html>
      <head>
        <title><xsl:value-of select="header/title"/></title>
      </head>
      <body text="#000000" link="#525D76" vlink="#023264" alink="#023264"
            topmargin="4" leftmargin="4" marginwidth="4" marginheight="4"
            bgcolor="#ffffff">

        <!-- THE TOP BAR (HEADER) -->
        <table width="100%" cellspacing="0" cellpadding="0" border="0">
          <tr>
            <td valign="top" align="left">
              <a href="http://jakarta.apache.org/index.html">
                <img hspace="0" vspace="0" border="0">
                  <xsl:attribute name="src"><xsl:call-template 
                    name="get-base-directory"/>images/jakarta-logo.gif</xsl:attribute>
                </img>
              </a>
            </td>

            <td width="100%" valign="middle" align="left" bgcolor="#ffffff">
              <img hspace="0" 
                   vspace="0" 
                   border="0" 
                   alt="{header/title}" 
                   align="right">
                   <xsl:attribute name="src"><xsl:call-template 
                     name="get-base-directory"/>/images/logocactus.gif</xsl:attribute>
              </img>
            </td>
          </tr>
          
          <tr>
            <td width="100%" height="2" colspan="2"><hr noshade="" size="1"/></td>
          </tr>
        </table>
        <!-- THE MAIN PANEL (SIDEBAR AND CONTENT) -->
        <table width="100%" cellspacing="0" cellpadding="0" border="0">
          <tr>
            <!-- THE SIDE BAR -->
            <td width="1%" valign="top">
            </td>
            <td width="14%" valign="top" nowrap="1">

            <font size="-2">
              Last update : <xsl:value-of select="$updated"/>
            </font>

              <br/>
              <font face="arial,helvetica,sanserif">
                <xsl:apply-templates select="document($stylebook.project)"/>
              </font>
            </td>
            <!-- THE CONTENT PANEL -->
            <td width="*" valign="top" align="left">
              <table border="0" cellspacing="0" cellpadding="3">
                <tr><td><br/><xsl:apply-templates/></td></tr>
<!--
                <tr>
                 <td align="right">
                  <xsl:if test="header/authors">
                   <p>by
                    <xsl:for-each select="header/authors/person">
                     <a href="mailto:{@email}"><xsl:value-of select="@name"/></a>
                     <xsl:if test="not(position()=last())">, </xsl:if>
                    </xsl:for-each>
                   </p>
                  </xsl:if>
                 </td>
                </tr>
-->
              </table>
            </td>
          </tr>
        </table>
        
        <br/>
        
        <table width="100%" border="0" cellspacing="0" cellpadding="0">
          <tr><td><hr noshade="" size="1"/></td></tr>
          <tr>
            <td align="center">
             <font face="arial,helvetica,sanserif" size="-1" color="#525D76">
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

  <!-- 
       ======================================================================
       book section
       ====================================================================== 
  -->
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
            <xsl:attribute name="href"><xsl:call-template 
              name="get-base-directory"/><xsl:value-of 
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

  <xsl:template match="project">
    <br/>    
    <a href="{@href}">
      <font size="+1" color="#F3510C"><xsl:value-of select="@label"/></font>
    </a>
    <br/>
  </xsl:template>  

<!-- ====================================================================== -->
<!-- header section -->
<!-- ====================================================================== -->

 <xsl:template match="header">
<!--
  <center>
   <table width="80%">
    <tr>
     <td bgcolor="#F3DD61">
      <br/>
      <center>
       <b>
        <font face="arial,helvetica,sanserif" color="#000000">
         <xsl:value-of select="title"/>
         <xsl:if test="subtitle">
          : <xsl:value-of select="subtitle"/>
         </xsl:if>
        </font>
       </b>
      </center>
      <br/>      
     </td>
    </tr>
   </table>
  </center>
  <br/>
-->
 </xsl:template>

<!-- ====================================================================== -->
<!-- body section -->
<!-- ====================================================================== -->

  <xsl:template match="s1">
    <xsl:call-template name="section">
      <xsl:with-param name="width">100%</xsl:with-param>
      <xsl:with-param name="font-size">+1</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="s2">
    <xsl:call-template name="section">
      <xsl:with-param name="width">95%</xsl:with-param>
      <xsl:with-param name="font-size">+0</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="s3">
    <xsl:call-template name="section">
      <xsl:with-param name="width">90%</xsl:with-param>
      <xsl:with-param name="font-size">-1</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="s4">
    <xsl:call-template name="section">
      <xsl:with-param name="width">85%</xsl:with-param>
      <xsl:with-param name="font-size">-2</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="section">
   <xsl:param name="width" />
   <xsl:param name="font-size" />

   <div align="right">
    <table border="0" cellspacing="0" cellpadding="2">
     <xsl:attribute name="width"><xsl:value-of select="$width"/></xsl:attribute>
     <tr>
      <td bgcolor="#525D76">
       <font face="arial,helvetica,sanserif" color="#ffffff">
        <xsl:attribute name="size"><xsl:value-of select="$font-size"/></xsl:attribute>
        <b><xsl:value-of select="@title"/></b>
       </font>
      </td>
     </tr>
     <tr>
      <td>
       <font face="arial,helvetica,sanserif" color="#000000"><br/><xsl:apply-templates/></font>
      </td>
     </tr>
    </table>
   </div>
   <br/>
  </xsl:template>
    
<!-- ====================================================================== -->
<!-- footer section -->
<!-- ====================================================================== -->

 <xsl:template match="footer">
  <!-- ignore on general documents -->
 </xsl:template>

<!-- ====================================================================== -->
<!-- paragraph section -->
<!-- ====================================================================== -->

  <xsl:template match="p">
    <p align="justify"><xsl:apply-templates/></p>
  </xsl:template>

  <xsl:template match="source">
   <div align="center">
    <table border="1" cellspacing="2" cellpadding="2">
    <tr>
       <td><pre><xsl:apply-templates/></pre></td>
    </tr>
    </table>
   </div>
  </xsl:template>
  
  <xsl:template match="fixme">
   <!-- ignore on documentation -->
  </xsl:template>

<!-- ====================================================================== -->
<!-- list section -->
<!-- ====================================================================== -->

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

<!-- ====================================================================== -->
<!-- note section -->
<!-- ====================================================================== -->

  <xsl:template match="note">
   <p>
    <table width="100%" cellspacing="3" cellpadding="0" border="0">
      <tr>
        <td width="28" valign="top">
          <img src="images/note.gif" width="28" height="29" vspace="0" hspace="0" border="0" alt="Note"/>
        </td>
        <td valign="top">
          <font size="-1" face="arial,helvetica,sanserif" color="#000000">
            <i>
              <xsl:apply-templates/>
            </i>
          </font>
        </td>
      </tr>  
    </table>
   </p>
  </xsl:template>

<!-- ====================================================================== -->
<!-- table section -->
<!-- ====================================================================== -->

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
    <td bgcolor="#039acc" 
        colspan="{@colspan}" 
        rowspan="{@rowspan}" 
        valign="center" 
        align="center">
      <font color="#ffffff" size="-1" face="arial,helvetica,sanserif">
        <b><xsl:apply-templates/></b>&#160;
      </font>
    </td>
  </xsl:template>

  <xsl:template match="td">
    <xsl:choose>
      <xsl:when test="@nowrap">
        <td bgcolor="#a0ddf0" colspan="{@colspan}" rowspan="{@rowspan}" nowrap="true" valign="top" align="left">
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

<!-- ====================================================================== -->
<!-- markup section -->
<!-- ====================================================================== -->

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

<!-- ====================================================================== -->
<!-- images section -->
<!-- ====================================================================== -->

 <xsl:template match="figure">
  <p align="center"><img src="{@src}" alt="{@alt}" border="0" vspace="4" hspace="4"/></p>
 </xsl:template>
 
 <xsl:template match="img">
   <img src="{@src}" alt="{@alt}" border="0" vspace="4" hspace="4" align="right"/>
 </xsl:template>

 <xsl:template match="icon">
   <img src="{@src}" alt="{@alt}" border="0" align="absmiddle"/>
 </xsl:template>

<!-- ====================================================================== -->
<!-- links section -->
<!-- ====================================================================== -->

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

<!-- ====================================================================== -->
<!-- specials section -->
<!-- ====================================================================== -->

 <xsl:template match="br">
  <br/>
 </xsl:template>

</xsl:stylesheet>
