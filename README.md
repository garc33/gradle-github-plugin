gradle-github-plugin
====================

Upload artifacts in github hosted maven repo.

How to use ?
============

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
