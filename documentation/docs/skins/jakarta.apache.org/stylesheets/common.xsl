<?xml version="1.0"?>

<!-- ====================================================================== -->
<!-- Templates common to various stylesheets -->
<!-- $Id$ -->
<!-- ====================================================================== -->

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- ==================================================================== -->
  <!-- Parameters -->
  <!-- ==================================================================== -->

  <xsl:param name="software" select="''"/>
  <xsl:param name="title" select="''"/>
  <xsl:param name="copyright" select="''"/>

  <!-- Location of the xdoc directory relative to where this stylesheet is 
       located. Note: this path MUST be relative as it is used as a relative 
       URI from within the stylesheets -->
  <xsl:param name="xdocdir" select="''"/>

  <!-- Location of the sitemap.xml file, which describes the documentation
       resources -->
  <xsl:param name="sitefile" select="''"/>

  <!-- ==================================================================== -->
  <!-- Global variables -->
  <!-- ==================================================================== -->

  <!-- Date of the last update, to be passed in from the build -->
  <xsl:param name="last.updated.date"/>
  
  <!-- Read the resource definitions. They are located in a file named 
       sitemap.xml placed at the same level as the xdoc files. 
       The path we specify is relative to where this stylesheet is located -->
  <xsl:variable name="sitemap" 
    select="document(concat($xdocdir,'/',$sitefile))/sitemap"/>

  <!-- The current document being processed. Note: This is needed for the
       "get-base-directory" template as it can be called with another
       document context (the $navigation one) -->
  <xsl:variable name="document" select="/document"/>

  <!-- ==================================================================== -->
  <!-- Get current processed file source path -->
  <!-- ==================================================================== -->

  <xsl:template name="get-source-from-id">
    <xsl:param name="id"/>
    <!-- Issue a warning if the id is invalid -->
    <xsl:if test="not($sitemap/resource[@id=$id])">
      <xsl:message terminate="yes">
        <xsl:text>Id [</xsl:text>
        <xsl:text>] has no reference in sitemap.xml</xsl:text>
        (<xsl:value-of select="$sitemap"/>)
      </xsl:message>
    </xsl:if>
    <xsl:value-of select="$sitemap//resource[@id=$id]/@target"/>
  </xsl:template>

  <xsl:template name="get-source">
    <xsl:call-template name="get-source-from-id">
      <xsl:with-param name="id" select="$document/@id"/>
    </xsl:call-template>
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
        <xsl:value-of select="concat($dir,'/',$path)"/>
      </xsl:when>
      <xsl:otherwise/>
    </xsl:choose>
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

  <!-- ==================================================================== -->
  <!-- Compute target file name from xml source file name -->
  <!-- ==================================================================== -->

  <xsl:template name="get-target-file">
    <xsl:param name="id"/>
    <xsl:variable name="source">
      <xsl:call-template name="get-source-from-id">
        <xsl:with-param name="id" select="$id"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:call-template name="get-base-directory"/>
    <xsl:value-of select="$source"/>
  </xsl:template>

</xsl:stylesheet>
