Eclipse Plug-in for Cactus

Installation

Uncompress the zip file and copy the 'org.apache.cactus.eclipse_0.1.0' folder to your Eclipse 'plugins' folder and restart Eclipse.
Download this file from the Cactus web site :
http://jakarta.apache.org/builds/jakarta-cactus/release/v1.5/jakarta-cactus-eclipse-junitsupport.jar
Rename 'junitsupport.jar' in the JUnit plugin folder (org.eclipse.jdt.junit_2.1.0) to 'junitsupport.jar.bak'.
Copy the downloaded file to 'junitsupport.jar' in the JUnit plugin folder (org.eclipse.jdt.junit_2.1.0).
The jakarta-cactus-eclipse-junitsupport.jar jar is a patched jar of the Eclipse JUnit plugin for the Cactus Eclipse plugin.
The Cactus plugin uses the JUnit plugin and there was a need for opening some JUnit plugin API, hence this patch.
In the near future, the Eclipse project will incorporate our patch.
For the time being it is put here for your convenience.


Configuration

Go to Window -> Preferences -> Cactus and set your preferences.


Use

While the Cactify action is not implemented you must do the following :
- add Cactus client and common libraries to your Java project
- your Java project must have the following directory structure :
<project's root>/web/WEB-INF/lib
(create these as 'folders')

Select any Cactus test class file and select from the toolbar :
Run As -> Cactus Test

