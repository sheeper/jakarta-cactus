/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.commons.cactus.ant;

import java.io.*;
import java.util.*;
import java.text.*;
import java.net.*;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.types.*;

/**
 * A CVS log task that extract information from the execution of the
 * '<code>cvs log</code>' command and put them in a generated output XML file.
 *
 * Note: I have rewritten this task based on the ChangeLog cvslog task from the
 *       Jakarta Alexandria project (initially written by Jeff Martin)
 *
 * @version @version@
 * @author Jeff Martin <a href="mailto:jeff.martin@synamic.co.uk">Jeff Martin</a>
 * @author Vincent Massol <a href="mailto:vmassol@users.sourceforge.net">vmassol@users.sourceforge.net</a> 
 */
public class ChangeLogTask extends Task implements ExecuteStreamHandler
{
    /**
     * Name of properties file containing the user list. This list is used to
     * match a user id retrieved from the '<code>cvs log</code>' command with a
     * display name. The format of the file is : 
     * '<code>[user id] = [display name]</code>'.
     */
    private File m_UserConfigFile;

    /**
     * In memory data structure to store the user list of matching pairs of
     * (user id, user display name).
     */
    private Properties m_UserList = new Properties();

    /**
     * CVS working directory where the '<code>cvs log</code>' operation is
     * performed.
     */
    private File m_CVSWorkingDirectory;

    /**
     * XML Output file containing the results.
     */
    private File m_OutputFile;

    /**
     * Date before which the cvs logs are ignored
     */
    private Date m_ThresholdDate;

    /**
     * Input stream read in from CVS log command
     */
    private BufferedReader m_Input;

    /**
     * Output file stream where results will be written to
     */
    private PrintWriter m_Output;
    
    /**
     * Filesets containting list of files against which the cvs log will be
     * performed. If empty then all files will in the working directory will
     * be checked.
     */
    private Vector m_Filesets = new Vector();

    /**
     * The URL that is used to check if internet access is on.
     */
    private String m_TestURL = "http://jakarta.apache.org";

    // state machine states
    private final static int GET_ENTRY = 0;
    private final static int GET_FILE = 1;
    private final static int GET_DATE = 2;
    private final static int GET_COMMENT = 3;
    private final static int GET_REVISION = 4;
    private final static int GET_PREVIOUS_REV = 5;

    /**
     * Input format for dates read in from cvs log
     */
    private static final SimpleDateFormat INPUT_DATE = 
        new SimpleDateFormat("yyyy/MM/dd");

    /**
     * Output format for dates written to the XML file
     */
    private static final SimpleDateFormat OUTPUT_DATE =
        new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Output format for times written to the XML file
     */
    private static final SimpleDateFormat OUTPUT_TIME =
        new SimpleDateFormat("hh:mm");

    /**
     * Set the properties file name containing the matching list of (user id,
     * user display name). This method is automatically called by the Ant
     * runtime engine when the <code>users</code> attribute is encountered
     * in the Ant XML build file. This attribute is optional.
     *
     * @param theUserConfigFileName the properties file name relative to the
     *        Ant project base directory (basedir attribute in build file).
     */
    public void setUsers(File theUserConfigFileName)
    {
        m_UserConfigFile = theUserConfigFileName;
    }

    /**
     * Set the CVS working directory where the cvs log operation will be
     * performed. This method is automatically called by the Ant
     * runtime engine when the <code>work</code> attribute is encountered
     * in the Ant XML build file. This attribute is mandatory.
     *
     * @param theWorkDir the CVS working directory relative to the Ant
     *        project base directory (basedir attribute in build file).
     */
    public void setWork(File theWorkDir)
    {
        m_CVSWorkingDirectory = theWorkDir;
    }

    /**
     * Set the output file for the log. This method is automatically called by 
     * the Ant runtime engine when the <code>output</code> attribute is
     * encountered in the Ant XML build file. This attribute is mandatory.
     *
     * @param theOutputFile the XML output file relative to the Ant project
     *                      base directory (i.e. basedir attrifbute in build
     *                      file).
     */
    public void setOutput(File theOutputFile)
    {
        m_OutputFile = theOutputFile;
    }

    /**
     * Set the threshold cvs log date. This method is automatically called by 
     * the Ant runtime engine when the <code>date</code> attribute is
     * encountered in the Ant XML build file. This attribute is optional. The
     * format is "yyyy/MM/dd hh:mm".
     *
     * @param theThresholdDate the threshold date before which cvs log are
     *                         ignored.
     */
    public void setDate(String theThresholdDate)
    {
        try {
            m_ThresholdDate = INPUT_DATE.parse(theThresholdDate);
        } catch(ParseException e) {
            throw new BuildException("Bad date format [" + 
                theThresholdDate + "].");
        }
    }

    /**
     * Set the threshold cvs log date by calculating it : "today - elapsed".
     * This method is automatically called by 
     * the Ant runtime engine when the <code>elapsed</code> attribute is
     * encountered in the Ant XML build file. This attribute is optional. The
     * elasped time must be expressed in days.
     *
     * @param theElapsedDays the elapsed time from now in days. All cvs logs
     *                       that are this old will be shown.
     */
    public void setElapsed(Long theElapsedDays)
    {
        long now = System.currentTimeMillis();
        m_ThresholdDate = new Date(
            now - theElapsedDays.longValue() * 24 * 60 * 60 * 1000);
    }

    /**
     * Adds a set of files (nested fileset attribute).
     * This method is automatically called by 
     * the Ant runtime engine when the <code>fileset</code> nested tag is
     * encountered in the Ant XML build file. This attribute is optional.
     *
     * @param theSet the fileset that contains the list of files for which
     *               cvs logs will be checked.
     */
    public void addFileset(FileSet theSet)
    {
        m_Filesets.addElement(theSet);
    }

    /**
     * Set the test URL to check if internet access is on. This method is
     * automatically called by the Ant runtime engine when the 
     * <code>testURL</code> attribute is encountered in the Ant XML build file.
     * This attribute is optional.
     *
     * @param theURLString the test URL string.
     */
    public void setTestURL(String theURLString)
    {
        m_TestURL = theURLString;
    }

    /**
     * Read the user list from the properties file and store the matches in
     * memory. If no properties file has been set, do not do anything.
     */
    private void readUserList()
    {
        if (m_UserConfigFile != null) {
            if (!m_UserConfigFile.exists()) {
                throw new BuildException("User list configuration file [" + 
                    m_UserConfigFile.getAbsolutePath() + 
                    "] was not found. Please check location.");
            }
            try {
                m_UserList.load(new FileInputStream(m_UserConfigFile));
            } catch(IOException e) {
                throw new BuildException(e);
            }
        }
    }

    /**
     * Check if internet access is on.  If not, then if the output XML file
     * already exist we don't touch it and if it does not exist we create an
     * empty one.
     *
     * @return true if internet access is on of false otherwise
     */
    private boolean testInternetAccess() throws BuildException
    {
        try {
            URL url = new URL(m_TestURL);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.connect();
            connection.disconnect();
        } catch (MalformedURLException e) {
            throw new BuildException("Bad URL [" + m_TestURL + "]");
        } catch (IOException e) {

            // Cannot contact server, we assume internet access is off. In
            // that case, we do nothing, meaning that if the output XML file
            // exist we don't touch it and if it does not exist we create an
            // empty one.
            if (!m_OutputFile.exists()) {

                try {
                    m_Output = new PrintWriter(new OutputStreamWriter(
                        new FileOutputStream(m_OutputFile),"UTF-8"));
                    m_Output.println("<changelog>");
                    m_Output.println("</changelog>");
                    m_Output.flush();
                    m_Output.close();
                } catch (IOException ee) {
                    throw new BuildException(ee);
                }

            }

            return false;
        }

        return true;
    }

    /**
     * Execute task
     */
    public void execute() throws BuildException
    {
        if (m_CVSWorkingDirectory == null) {
            throw new BuildException("The [workDir] attribute must be set");
        }

        if (!m_CVSWorkingDirectory.exists()) {
            throw new BuildException("Cannot find CVS working directory [" +
                m_CVSWorkingDirectory.getAbsolutePath() + "]");
        }

        if (m_OutputFile == null) {
            throw new BuildException("The [output] attribute must be set");
        }

        readUserList();

        // Verify if the computer has internet access by trying to connect to
        // a URL
        if (!testInternetAccess()) {
            // If no internet access, do nothing.
            return;
        }

        Commandline toExecute = new Commandline();

        toExecute.setExecutable("cvs");
        toExecute.createArgument().setValue("log");

        // Check if a threshold date has been specified
        if (m_ThresholdDate != null) {
            toExecute.createArgument().setValue("-d\">=" + 
                OUTPUT_DATE.format(m_ThresholdDate) + "\"");
        }

        // Check if list of files to check has been specified
        if (!m_Filesets.isEmpty()) {

            Enumeration e = m_Filesets.elements();
            while(e.hasMoreElements()) {
                FileSet fs = (FileSet)e.nextElement();
                DirectoryScanner ds = fs.getDirectoryScanner(project);
                String[] srcFiles = ds.getIncludedFiles();
                for (int i = 0; i < srcFiles.length; i++) {
                    toExecute.createArgument().setValue(srcFiles[i]);
                }                
            }
        }

        Execute exe = new Execute(this);
        exe.setCommandline(toExecute.getCommandline());
        exe.setAntRun(project);
        exe.setWorkingDirectory(m_CVSWorkingDirectory);
        try {
            exe.execute();
        } catch(IOException e) {
            throw new BuildException(e);
        }
    }

    /**
     * Set the input stream for the CVS process. As CVS requires no input, this
     * is not used.
     *
     * @param theOs the output stream to write to the standard input stream of
     *              the subprocess (i.e. the CVS process)
     */
    public void setProcessInputStream(OutputStream theOs) throws IOException
    {
    }

    /**
     * Set the error stream for reading from CVS log. Not used in the current
     * version (should be handled in future versions).
     *
     * @param theIs the input stream to read from the error stream from the
     *              subprocess (i.e. the CVS process)
     */
    public void setProcessErrorStream(InputStream theIs) throws IOException
    {
    }

    /**
     * Set the input stream used to read from CVS log
     *
     * @param theIs the input stream to read from the output stream of the
     *              subprocess (i.e. the CVS process)
     */
    public void setProcessOutputStream(InputStream theIs) throws IOException
    {
        m_Input = new BufferedReader(new InputStreamReader(theIs));
    }

    /**
     * Stop handling of the streams (i.e. the cvs process).
     */
    public void stop()
    {
    }

    /**
     * Start reading from the cvs log stream.
     */
    public void start() throws IOException
    {
        m_Output = new PrintWriter(new OutputStreamWriter(
            new FileOutputStream(m_OutputFile),"UTF-8"));

        String file = null;
        String line = null;
        String date = null;
        String author = null;
        String comment = null;
        String revision = null;
        String previousRev = null;

        // Current state in the state machine used to parse the CVS log stream
        int status = GET_FILE;

        // RCS entries
        Hashtable entries = new Hashtable();

        while ((line = m_Input.readLine()) != null) {

            switch(status){

                case GET_FILE:
                    if (line.startsWith("Working file:")) {
                        file = line.substring(14, line.length());
                        status = GET_REVISION;
                    }
                    break;

                case GET_REVISION:
                    if (line.startsWith("revision")) {
                        revision = line.substring(9);
                        status = GET_DATE;
                    }

                    // If we encounter a "=====" line, it means there was no
                    // description and thus the entry must be forgotten
                    else if (line.startsWith("======")) {
                        status = GET_FILE;
                    }
                    break;

                case GET_DATE:
                    if (line.startsWith("date:")) {
                        date = line.substring(6, 16);
                        line = line.substring(line.indexOf(";") + 1);
                        author = line.substring(10, line.indexOf(";"));

                        if ((m_UserList != null) && m_UserList.containsKey(author)) {
                            author = "<![CDATA[" + m_UserList.getProperty(author) + "]]>";
                        }

                        status = GET_COMMENT;
                    }
                    break;

                case GET_COMMENT:
                    comment = "";
                    while (line != null && !line.startsWith("======") && 
                        !line.startsWith("------")) {

                        comment += line + "\n";
                        line = m_Input.readLine();
                    }
                    comment = "<![CDATA[" + 
                        comment.substring(0,comment.length() - 1) + "]]>";

                    status = GET_PREVIOUS_REV;
                    break;

                case GET_PREVIOUS_REV:
                    if (line.startsWith("revision")) {
                        previousRev = line.substring(9);
                        status = GET_FILE;

                        Entry entry;
                        if (!entries.containsKey(date + author + comment)) {
                            entry = new Entry(date, author, comment);
                            entries.put(date + author + comment, entry);
                        } else {
                            entry = (Entry)entries.get(date + author + comment);
                        }
                        entry.addFile(file, revision, previousRev);
                    }
                    if (line.startsWith("======")) {
                        status = GET_FILE;
                        Entry entry;
                        if (!entries.containsKey(date + author + comment)) {
                            entry = new Entry(date, author, comment);
                            entries.put(date + author + comment, entry);
                        }else {
                            entry = (Entry)entries.get(date + author + comment);
                        }
                        entry.addFile(file, revision);
                    }

            }
            
        }
        m_Output.println("<changelog>");
        Enumeration en = entries.elements();
        while (en.hasMoreElements()) {
            ((Entry)en.nextElement()).print();
        }
        m_Output.println("</changelog>");
        m_Output.flush();
        m_Output.close();
    }

    /**
     * CVS entry class
     */
    private class Entry
    {
        /**
         * The entry date
         */
        private Date m_Date;

        /**
         * The entry author id
         */
        private final String m_Author;

        /**
         * The comment entry
         */
        private final String m_Comment;

        /**
         * The list of files that were CVS committed at the same time
         */
        private final Vector m_Files = new Vector();

        /**
         * Create an entry.
         *
         * @param theDate the entry's date
         * @param theAuthor the entry's author
         * @param theComment the entry's comment
         */
        public Entry(String theDate, String theAuthor, String theComment)
        {
            try {
                m_Date = INPUT_DATE.parse(theDate);
            } catch(ParseException e) {
                log("Bad date format [" + theDate + "].");
            }
            m_Author = theAuthor;
            m_Comment = theComment;
        }

        public void addFile(String theFile, String theRevision)
        {
            m_Files.addElement(new RCSFile(theFile, theRevision));
        }

        public void addFile(String theFile, String theRevision, String thePreviousRev)
        {
            m_Files.addElement(new RCSFile(theFile, theRevision, thePreviousRev));
        }

        public String toString()
        {
            return m_Author + "\n" + m_Date + "\n" + m_Files + "\n" + m_Comment;
        }

        public void print()
        {
            m_Output.println("\t<entry>");
            m_Output.println("\t\t<date>" + OUTPUT_DATE.format(m_Date) + "</date>");
            m_Output.println("\t\t<time>" +OUTPUT_TIME.format(m_Date) + "</time>");
            m_Output.println("\t\t<author>" + m_Author + "</author>");

            Enumeration e = m_Files.elements();
            while (e.hasMoreElements()) {
                RCSFile file = (RCSFile)e.nextElement();
                m_Output.println("\t\t<file>");
                m_Output.println("\t\t\t<name>" + file.getName() + "</name>");
                m_Output.println("\t\t\t<revision>" + file.getRevision() + "</revision>");
                if (file.getPreviousRev() != null) {
                    m_Output.println("\t\t\t<prevrevision>" + file.getPreviousRev()
                        + "</prevrevision>");
                }
                m_Output.println("\t\t</file>");
            }
            m_Output.println("\t\t<msg>" + m_Comment + "</msg>");
            m_Output.println("\t</entry>");
        }

        private class RCSFile
        {
            private String m_Name;
            private String m_Rev;
            private String m_PreviousRev;

            private RCSFile(String theName, String theRev)
            {
                this(theName, theRev, null);
            }

            private RCSFile(String theName, String theRev, String thePreviousRev)
            {
                m_Name = theName;
                m_Rev = theRev;
                if (!m_Rev.equals(m_PreviousRev)) {
                    m_PreviousRev = thePreviousRev;
                }
            }

            public String getName()
            {
                return m_Name;
            }

            public String getRevision()
            {
                return m_Rev;
            }
            public String getPreviousRev()
            {
                return m_PreviousRev;
            }
        }
    }

}
