# JPackageScriptFX #

This project demonstrates how projects can use a script to build executables of their JavaFX applications via the
jdeps, jlink, and jpackage tools. Two scripts are included for running builds on Mac and Windows. The jpackage
tool is currently only available as an early access release based on the upcoming Java 14 (March 2020).

### Requirements

* Java 13 Installation ([download from AdoptOpenJDK](https://adoptopenjdk.net)) 
* JPackage 14 EA Installation ([download from java.net](https://jdk.java.net/jpackage/))

### Environment

Both platform-specific build scripts need to know where they can find the java installation and where they can
find the jpackage installation. Currently these will be in two different places as projects will normally use a production
release of Java and the early access release of jpackage. Once Java 14 is available the jpackage tool will be part it.

For the time being you have to set the environment variables JAVA_HOME and JPACKAGE_HOME. How you set them depends
on your operating system. On Mac we can set them inside the .bash_profiles file in our user home directory. On Windows
you would set them in the "environment variables" dialog. In your IDE you can normally also set them as part of a
Maven run configuration. If you are the only one working on the project then you can even add them to the pom.xml file of
the main module. 

