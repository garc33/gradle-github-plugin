gradle-github-plugin
====================

Upload artifacts in github hosted maven repo.

How to use ?
============
Configure your build like this:
```groovy
buildscript {
    repositories{
        mavenCentral()
        maven{ url = 'https://repo.eclipse.org/content/repositories/egit-releases/' }
        maven{ url = 'https://raw.github.com/garc33/m2p2-repository/mvn-repo/maven/releases/' }
    }
    dependencies { classpath "fr.herman.gradle.plugins:gradle-github-plugin:1.0" }
}
apply plugin: 'github'
github {
    username = 'garc33'
    repo = 'm2p2-repository'
    branch = 'mvn-repo'
    token = 'secrettoken'
}
```
 finally: 
 
     gradle uploadGithub
     
Configuration
=============

| Settings     | Description                                           |Default            |
|--------------|-------------------------------------------------------|-------------------|
|username      |Github username of the repo owner                      |                   |
|token         |OAuth token                                            |                   |
|password      |Github password (if token is not provided)             |                   |
|repo          |The name of the project used to host the maven repo    |                   |
|branch        |The branch which contains the repo                     |master             |
|message       |Commit message for upload                              |upload with...     |
