Eclipse Plug-in for Cactus
Developer's readme

How to install the plugin for development

* Check-out the JUnit plugin CVS module

Make an anonymous repository location to Eclipse dev, etc.
Select the jdt.junit module and then 'Check out as project'
Apply the patch provided with the Cactus plugin to the
JUnit project (Compare with -> Patch, and select the appropriate .txt file)

* Make an Eclipse plugin project

Create a blank plugin project

Import files from your file system
OR
Check-out repository location

* Resolve dependencies

Import... -> External plug-ins and fragments
Select all needed plugins from workbench (simplest : select all plugins except jdt.junit)
This creates projects in your workspace corresponding to the plugins that you selected,
hence resolving dependencies for the Cactus and JUnit plugins.

* Add cactussupport.jar

In order to emulate the plugin's installation when in self-hosting, we need to
copy the cactussupport.jar to the plugin project
(i.e. at the root of the folder <your Eclipse directory>/workspace/<the Eclipse plugin project>).