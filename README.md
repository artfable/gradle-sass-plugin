# Gradle SASS Plugin
(version: 0.0.1)

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
```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath "com.github.artfable.gradle:gradle-sass-plugin:0.0.1"
    }
}

apply plugin: 'artfable.sass'
```

It'll add a task `compileSass`

## Usage
Parameters to configure:
+ sourceDir - path to directory with .scss files
+ outputDir - path to output directory, where .css files will be created
+ ignoreFailures - if it true, task won't failed if some of files can't be compiled *(default: false)*
+ optimisation - switch on importer, that filter duplicates *(default: true)* 

```groovy
sass {
    group {
        sourceDir = "src/main/webapp/sass"
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