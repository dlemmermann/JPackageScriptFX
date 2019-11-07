# JPackageScriptFX #

This project demonstrates how projects can use scripts to build executables of their JavaFX applications via the
`jdeps`, `jlink`, and `jpackage` tools. Two scripts are included for running builds on Mac and Windows. The jpackage
tool is currently only available as an early access release based on the upcoming Java 14 (March 2020).

Important: the scripts do not try to create a fully modularized solution but instead tries to enable existing
projects / applications, which often use non-modularized 3rd party dependencies, to be packaged again after the
previous packaging tool stopped working for Java 11.

### Prerequisites

* Java 13 Installation ([download from AdoptOpenJDK](https://adoptopenjdk.net)) 
* JPackage 14 EA Installation ([download from java.net](https://jdk.java.net/jpackage/))

### Environment

Both platform-specific build scripts need to know where they can find the java installation and where they can
find the jpackage installation. Currently these will be in two different places as projects will normally use a production
release of Java and the early access release of `jpackage`. Once Java 14 is available the `jpackage` tool will be part it.

For the time being you have to set the environment variables `JAVA_HOME` and `JPACKAGE_HOME`. How you set them depends
on your operating system. On Mac we can set them inside the .bash_profiles file in our user home directory. On Windows
you would set them in the "environment variables" dialog. In your IDE you can normally also set them as part of a
Maven run configuration. If you are the only one working on the project then you can even add them to the pom.xml file of
the main module. 

### Building the Project

Once your environment is set up you can simply call the `mvn clean install` on the root / parent module. It will do
a standard build of the application and in addition it will analyze all the dependencies and copy the resulting set of
JAR files into the folder target/libs. This work is done via the Maven dependency plugin. Once the standard build is 
completed Maven will invoke the shell script (on Mac) or the batch script (on Windows). The build script uses two 
different profiles, both of them being activated via the OS that they are running on.

The scripts both have the same structure:
* Environment
* Dependency Analysis
* Runtime Image Generation
* Packaging

### Environment

The required environment for the scripts consists of environment variables and a directory structure. The following
variables are used:

* JAVA_HOME - the location of the JDK matching the Java version
* JPACKAGE_HOME - the location of the jpackage early access distribution
* JAVA_VERSION - defines the runtime environment that the final application is targeting
* PROJECT_VERSION - the version of the project as defined in the pom.xml file (e.g. 1.0-SNAPSHOT)
* APP_VERSION - the version for the executable (careful: do not re-use project version. The supported Windows version 
  number format has to be major.minor.build, e.g. 1.0.2412. Mac seems to be more flexible.)
* MAIN_JAR - the name of the jar file that contains the main class
  
The directory structure required by the build:

* `target/java-runtime` - contains the runtime environment generated by jlink as part of the build
* `target/installer` - contains the executables generated by jpackage as part of the build
* `target/installer/input/libs` - contains the jars required by the application to run
 
### Dependency Analysis

The scripts use the `jdeps` tool to analyze the dependencies of the application to determine which modules need to
be included in the final package. These modules are stored in the list `detected_modules`. 

```
detected_modules=$JAVA_HOME/bin/jdeps \
  --multi-release ${JAVA_VERSION} \
  --ignore-missing-deps \
  --print-module-deps \
  --class-path "target/installer/input/libs/*" \
    target/classes/com/dlsc/jpackagefx/App.class
```
    
However, the tool can not always find all modules and sometimes manual intervention is required. For this you can add modules 
to the list called `manual_modules`.

```
manual_modules=jdk.crypto.ec
```

### Runtime Image Generation

The next tool to call is called `jlink`. It generates a java runtime environment for our application and places its content
inside the folder target/java-runtime. We could have relied on `jpackage` to perform the linking for us but unfortunately
it does not behave very well with automatic modules, yet. So in order to have full control over the image generation we
are letting the script do it via `jlink`.

```
$JAVA_HOME/bin/jlink \
  --no-header-files \
  --no-man-pages  \
  --compress=2  \
  --strip-debug \
  --add-modules "${detected_modules},${manual_modules}" \
  --output target/java-runtime
```
    
### Packaging

Finally we are invoking the `jpackage` tool in a loop so that it generates all available executables for the platform
that the build is running on. Please be aware that `jpackage` can not build cross-platform installers. The build has to
run separately on all platforms that you want to support. When the build is done you will find the installers inside
the directory `target/installer`. On Mac you will find a DMG, a PKG, and an APP. On Windows you will find an application
directory, an EXE, and an MSI.

```
for type in "app-image" "dmg" "pkg"
do
  $JPACKAGE_HOME/bin/jpackage \
  --package-type $type \
  --dest target/installer \
  --input target/installer/input/libs \
  --name JPackageScriptFX \
  --main-class com.dlsc.jpackagefx.AppLauncher \
  --main-jar ${MAIN_JAR} \
  --java-options -Xmx2048m \
  --runtime-image target/java-runtime \
  --icon src/main/logo/macosx/duke.icns \
  --app-version ${APP_VERSION} \
  --vendor "ACME Inc." \
  --copyright "Copyright © 2019 ACME Inc." \
  --mac-package-identifier com.acme.app \
  --mac-package-name ACME
done
```


