Eclipse Plug-in for Cactus


* Prerequisites

The Cactus plugin requires versions of the Eclipse platform higher than 2.1 M5.


* Installation

Uncompress the zip file and copy the 'org.apache.cactus.integration.eclipse_<version>' folder to your Eclipse 'plugins' folder and restart Eclipse.


* Configuration

Go to Window -> Preferences -> Cactus and set your preferences.


* Use

While the Cactify action is not implemented you must do the following :
- add Cactus client and common libraries to your Java project
- your Java project must have the following directory structure :
<project's root>/web/WEB-INF/lib
(create these as 'folders')

Select any Cactus test class file and select from the toolbar :
Run As -> Cactus Test