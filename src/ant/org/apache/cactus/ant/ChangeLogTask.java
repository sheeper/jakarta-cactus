/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Cactus", and "Apache Software
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
package org.apache.cactus.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.ExecuteStreamHandler;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.FileSet;

/**
 * A CVS log task that extract information from the execution of the
 * '<code>cvs log</code>' command and put them in a generated output XML file.
 *
 * Note: I have rewritten this task based on the ChangeLog cvslog task from the
 *       Jakarta Alexandria project (initially written by Jeff Martin)
 *
 * @author <a href="mailto:jeff.martin@synamic.co.uk">Jeff Martin</a>
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class ChangeLogTask extends Task implements ExecuteStreamHandler
{
    /**
     * Name of properties file containing the user list. This list is used to
     * match a user id retrieved from the '<code>cvs log</code>' command with a
     * display name. The format of the file is :
     * '<code>[user id] = [display name]</code>'.
     */
    private File userConfigFile;

    /**
     * In memory data structure to store the user list of matching pairs of
     * (user id, user display name).
     */
    private Properties userList = new Properties();

    /**
     * CVS working directory where the '<code>cvs log</code>' operation is
     * performed.
     */
    private File cvsWorkingDirectory;

    /**
     * XML Output file containing the results.
     */
    private File outputFile;

    /**
     * Date before which the cvs logs are ignored
     */
    private Date thresholdDate;

    /**
     * Input stream read in from CVS log command
     */
    private BufferedReader input;

    /**
     * Error Input stream read in from CVS log command
     */
    private InputStreamReader errorInput;

    /**
     * Output file stream where results will be written to
     */
    private PrintWriter output;

    /**
     * Filesets containting list of files against which the cvs log will be
     * performed. If empty then all files will in the working directory will
     * be checked.
     */
    private Vector filesets = new Vector();

    /**
     * The URL that is used to check if internet access is on.
     */
    private String testURL = "http://jakarta.apache.org";

    /**
     * Debug writer to print debug information when in debug mode
     */
    private PrintWriter debug;

    // state machine states
    private final static int GET_ENTRY = 0;

    private final static int GET_FILE = 1;

    private final static int GET_DATE = 2;

    private final static int GET_COMMENT = 3;

    private final static int GET_REVISION = 4;

    /**
     * Input format for dates read in from cvs log
     */
    private static final SimpleDateFormat INPUT_DATE =
        new SimpleDateFormat("yyyy/MM/dd");

    /**
     * Input format for dates read in from cvs log
     */
    private static final SimpleDateFormat FULL_INPUT_DATE =
        new SimpleDateFormat("yyyy/MM/dd HH:mm");

    /**
     * Output format for dates written to the XML file
     */
    private static final SimpleDateFormat OUTPUT_DATE =
        new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Output format for times written to the XML file
     */
    private static final SimpleDateFormat OUTPUT_TIME =
        new SimpleDateFormat("HH:mm");

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
        this.userConfigFile = theUserConfigFileName;
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
        this.cvsWorkingDirectory = theWorkDir;
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
        this.outputFile = theOutputFile;
    }

    /**
     * Set the threshold cvs log date. This method is automatically called by
     * the Ant runtime engine when the <code>date</code> attribute is
     * encountered in the Ant XML build file. This attribute is optional. The
     * format is "yyyy/MM/dd".
     *
     * @param theThresholdDate the threshold date before which cvs log are
     *                         ignored.
     */
    public void setDate(String theThresholdDate)
    {
        try {
            this.thresholdDate = INPUT_DATE.parse(theThresholdDate);
        } catch (ParseException e) {
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
        this.thresholdDate = new Date(
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
        this.filesets.addElement(theSet);
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
        this.testURL = theURLString;
    }

    /**
     * Set the debug file. This is optional and debug will be turned on only
     * when this attribute is set. All output from the cvs log command will be
     * dumped to this debug file.
     *
     * @param theDebugFile the name of the debug file to use.
     *
     * @exception IOException for backward compatibility with JDK 1.2.2 (not
     *            needed for JDK 1.3+)
     */
    public void setDebug(File theDebugFile)
        throws FileNotFoundException, UnsupportedEncodingException, IOException
    {
        this.debug = new PrintWriter(new OutputStreamWriter(
            new FileOutputStream(theDebugFile), "UTF-8"), true);
    }

    /**
     * Read the user list from the properties file and store the matches in
     * memory. If no properties file has been set, do not do anything.
     */
    private void readUserList()
    {
        if (this.userConfigFile != null) {
            if (!this.userConfigFile.exists()) {
                throw new BuildException("User list configuration file [" +
                    this.userConfigFile.getAbsolutePath() +
                    "] was not found. Please check location.");
            }
            try {
                this.userList.load(new FileInputStream(this.userConfigFile));
            } catch (IOException e) {
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
            URL url = new URL(this.testURL);
            HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();
            connection.connect();
            connection.disconnect();
        } catch (MalformedURLException e) {
            throw new BuildException("Bad URL [" + this.testURL + "]");
        } catch (IOException e) {

            // Cannot contact server, we assume internet access is off. In
            // that case, we do nothing, meaning that if the output XML file
            // exist we don't touch it and if it does not exist we create an
            // empty one.
            if (!this.outputFile.exists()) {

                try {
                    this.output = new PrintWriter(new OutputStreamWriter(
                        new FileOutputStream(this.outputFile), "UTF-8"));
                    this.output.println("<changelog>");
                    this.output.println("</changelog>");
                    this.output.flush();
                    this.output.close();
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
        if (this.cvsWorkingDirectory == null) {
            throw new BuildException("The [workDir] attribute must be set");
        }

        if (!this.cvsWorkingDirectory.exists()) {
            throw new BuildException("Cannot find CVS working directory [" +
                this.cvsWorkingDirectory.getAbsolutePath() + "]");
        }

        if (this.outputFile == null) {
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
        if (this.thresholdDate != null) {
            toExecute.createArgument().setValue("-d\">=" +
                OUTPUT_DATE.format(this.thresholdDate) + "\"");
        }

        // Check if list of files to check has been specified
        if (!this.filesets.isEmpty()) {

            Enumeration e = this.filesets.elements();
            while (e.hasMoreElements()) {
                FileSet fs = (FileSet) e.nextElement();
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
        exe.setWorkingDirectory(this.cvsWorkingDirectory);
        try {
            exe.execute();
        } catch (IOException e) {
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
        this.errorInput = new InputStreamReader(theIs);
    }

    /**
     * Set the input stream used to read from CVS log
     *
     * @param theIs the input stream to read from the output stream of the
     *              subprocess (i.e. the CVS process)
     */
    public void setProcessOutputStream(InputStream theIs) throws IOException
    {
        this.input = new BufferedReader(new InputStreamReader(theIs));
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
        this.output = new PrintWriter(new OutputStreamWriter(
            new FileOutputStream(this.outputFile), "UTF-8"));

        String file = null;
        String line = null;
        String date = null;
        String author = null;
        String comment = null;
        String revision = null;

        // Current state in the state machine used to parse the CVS log stream
        int status = GET_FILE;
        debug("State = GET_FILE");

        // RCS entries
        Hashtable entries = new Hashtable();

        while ((line = this.input.readLine()) != null) {

            // Log to debug file if debug mode is on
            debug("Text: [" + line);

            switch (status) {

                case GET_FILE:
                    if (line.startsWith("Working file:")) {
                        file = line.substring(14, line.length());
                        status = GET_REVISION;
                        debug("Next state = GET_REVISION");
                    }
                    break;

                case GET_REVISION:
                    if (line.startsWith("revision")) {
                        revision = line.substring(9);
                        status = GET_DATE;
                        debug("Next state = GET_DATE");
                    }

                    // If we encounter a "=====" line, it means there is no
                    // more entries for the current file.
                    else if (line.startsWith("======")) {
                        status = GET_FILE;
                        debug("Next state = GET_FILE");
                    }
                    break;

                case GET_DATE:
                    if (line.startsWith("date:")) {
                        date = line.substring(6, 22);
                        line = line.substring(line.indexOf(";") + 1);
                        author = line.substring(10, line.indexOf(";"));

                        if ((this.userList != null) &&
                            this.userList.containsKey(author)) {

                            author = "<![CDATA[" +
                                this.userList.getProperty(author) + "]]>";
                        }

                        status = GET_COMMENT;
                        debug("Next state = GET_COMMENT");
                    }
                    break;

                case GET_COMMENT:
                    comment = "";
                    while (line != null && !line.startsWith("======") &&
                        !line.startsWith("------")) {

                        comment += line + "\n";
                        line = this.input.readLine();

                        debug("Text: [" + line);
                    }
                    comment = "<![CDATA[" +
                        comment.substring(0, comment.length() - 1) + "]]>";

                    // Add the entry to the list of entries
                    Entry entry;
                    if (!entries.containsKey(date + author + comment)) {
                        entry = new Entry(date, author, comment);
                        entries.put(date + author + comment, entry);
                    } else {
                        entry = (Entry) entries.get(date + author + comment);
                    }
                    entry.addFile(file, revision);

                    // Continue reading the other revisions or skip to next file
                    if (line.startsWith("======")) {
                        status = GET_FILE;
                        debug("Next state = GET_FILE");
                    } else {
                        status = GET_REVISION;
                        debug("Next state = GET_REVISION");
                    }
                    break;

            }

            // Read the error stream so that it does not block !
            // We cannot use a BufferedReader as the ready() method is bugged!
            // (see Bug 4329985, which is supposed to be fixed in JDK 1.4 :
            // http://developer.java.sun.com/developer/bugParade/bugs/4329985.html)
            while (this.errorInput.ready()) {
                this.errorInput.read();
            }
        }

        debug("Preparing to write changelog file");

        this.output.println("<changelog>");
        Enumeration en = entries.elements();
        while (en.hasMoreElements()) {
            ((Entry) en.nextElement()).print();
        }
        this.output.println("</changelog>");
        this.output.flush();
        this.output.close();
    }

    /**
     * Print debug information (if in debug mode).
     *
     * @param theMessage the message to print
     */
    private void debug(String theMessage)
    {
        if (this.debug != null) {
            this.debug.println(theMessage);
        }
    }

    /**
     * CVS entry class
     */
    private class Entry
    {
        /**
         * The entry date
         */
        private Date date;

        /**
         * The entry author id
         */
        private final String author;

        /**
         * The comment entry
         */
        private final String comment;

        /**
         * The list of files that were CVS committed at the same time
         */
        private final Vector files = new Vector();

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
                this.date = FULL_INPUT_DATE.parse(theDate);
            } catch (ParseException e) {
                log("Bad date format [" + theDate + "].");
            }
            this.author = theAuthor;
            this.comment = theComment;
        }

        public void addFile(String theFile, String theRevision)
        {
            this.files.addElement(new RCSFile(theFile, theRevision));
        }

        public String toString()
        {
            return this.author + "\n" + this.date + "\n" + this.files + "\n" +
                this.comment;
        }

        public void print()
        {
            output.println("\t<entry>");
            output.println("\t\t<date>" + OUTPUT_DATE.format(this.date) +
                "</date>");
            output.println("\t\t<time>" + OUTPUT_TIME.format(this.date) +
                "</time>");
            output.println("\t\t<author>" + this.author + "</author>");

            Enumeration e = this.files.elements();
            while (e.hasMoreElements()) {
                RCSFile file = (RCSFile) e.nextElement();
                output.println("\t\t<file>");
                output.println("\t\t\t<name>" + file.getName() + "</name>");
                output.println("\t\t\t<revision>" + file.getRevision() +
                    "</revision>");
                output.println("\t\t</file>");
            }
            output.println("\t\t<msg>" + this.comment + "</msg>");
            output.println("\t</entry>");
        }

        private class RCSFile
        {
            private String name;

            private String revision;

            private RCSFile(String theName, String theRevision)
            {
                this.name = theName;
                this.revision = theRevision;
            }

            public String getName()
            {
                return this.name;
            }

            public String getRevision()
            {
                return this.revision;
            }
        }
    }

}
