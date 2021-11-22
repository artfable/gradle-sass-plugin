# Gradle SASS Plugin
[ ![artifactory](https://img.shields.io/badge/Artifactory-v0.2.0-green) ](https://artfable.jfrog.io/ui/packages/gav:%2F%2Fcom.artfable.gradle:gradle-sass-plugin)

## Overview
The plugin that was written on [kotlin](https://kotlinlang.org) for working with [SASS](http://sass-lang.com/). It allowed compile .scss (not .sass!) files to .css. 
It has several advantages compared to analogs:
* It used [jsass](https://github.com/bit3/jsass) that used [libsass](http://sass-lang.com/libsass) that based on c/c++. 
Consequently, it works much faster than analogs that use compass, that based on ruby gems.
* You have no dependencies from ruby gem, that could be trouble in closed networks.
* I could find only one working plugin for gradle, that use libsass. However, it can work only with one file per build. (So you need copy tasks or sth like that)
* Jsass make duplicates by default (if file 'a' import file 'b' and 'c'; and file 'b' import 'c' - then you'll get in 'a' code of file 'c' two times). 
This plugin has own importer, that removes such duplicates. (Can be switched off by setting false to flag `optimisation`)

Now, it works only with .scss files. Files that start with '_' used only for import. Plugin parse all subdirectories in `sourceDir`.

## Install
```kotlin
buildscript {
    repositories {
        maven(url = "https://artfable.jfrog.io/artifactory/default-maven-local")
    }
    dependencies {
        classpath("com.artfable.gradle:gradle-sass-plugin:0.2.0")
    }
}

apply(plugin = "artfable.sass")
```

It'll add a task `compileSass`

For use in `plugins {}` see [Gradle resolution strategy](https://docs.gradle.org/current/userguide/custom_plugins.html#note_for_plugins_published_without_java_gradle_plugin)

## Usage
Parameters to configure:
+ sourceDir - path to directory with .scss files
+ outputDir - path to output directory, where .css files will be created
+ ignoreFailures - if it true, task won't failed if some of files can't be compiled *(default: false)*
+ optimisation - switch on importer, that filter duplicates *(default: true)* 

```kotlin
sass {
    group {
        sourceDir = "${projectDir}/src/main/webapp/sass"
        outputDir = "${buildDir}/tmp/webapp/css"
    }
    group {
        //  ...
    }
    //  ignoreFailures = false
    //  optimisation = true
}
```

You can add so many directories as you want by adding more `group` blocks.