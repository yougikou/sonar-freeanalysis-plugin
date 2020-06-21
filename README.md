# Free Analysis

**FreeAnalysis** is a plugin for static analysis tool - SonarQube, enabling simple / small / quick code analysis requirement in projects.

## Idea, Purpose
A lot of tools for static code analysis are getting better and better.  
Bulk rules of common code style and code practices are created and free to use, but in project, some special checks just for project needs still required sometimes.  

Learn a new tool and build from base really takes time & money.
Building rules based on language grammar is correct approach, but in some case, implementing 1 custom plugin for 1 language may take more time.
Some special languages are not supported by these tools.

Adding checks with **simple utility function and regex** is basic thinking / approach of free analysis plugin. 

The **Idea** is from CodeNarc - a simple code analysis tool which adding 1 rule is very simple with groovy script.
Developers just need to focus on how to write rule itself - use regex to achieve very simple check.

Why **SonarQuble**? Because it provides a server control(analysis configuration) and distribution mechanism.
With sonarLint, developer only need to code once, rules can use in IDE(tested in intellij) for flying check as well.  
It has good frontend for any size project to manage/track issues and be able to integrated with SCM for productive development. 

## Base implementation
1. With base implementation, you can implement checks quickly with regex approach to identify bad text range and report issues.
2. With custom example implementation, you can scan intellij's inspection result files and create issue in SonarQube.

## What needs to do if use this plugin
Just for reference - you may need to do more...
1. Need to decide target file types, file suffixes can be setting in SonarQube project setting.
2. You may need to create some common utility functions which to analysis lines in code files.
3. You can filter which file type - which rule in your rules. 
4. You can directly use FreeAnaylsisSensor to avoid pre-process(import intellij inspection result) in custom example, or you can implement your pre-process.
5. Other things may take your more time - setup SonarQube server, change gradle script to your project ...

## Example of gradle script to add plugin
Copy repository folder to your project root which also include build.gradle.
Add buildscript block before plugins block.
```
// START – add dependency to SonarQube plugin
buildscript {
    repositories {
        maven { url project.file('repository') }
    }
    dependencies {
        classpath 'org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:2.7.1'
    }
}
// END
```

Add below setting and task to somewhere...  
You need to generate token in SonarQube server first (Create a project first).
```
// START – apply SonarQube plugin to project 
apply plugin: "org.sonarqube"

sonarqube {
    properties {
        property 'sonar.host.url', 'http://localhost:9000/'
        property 'sonar.login', 'token'
        property 'sonar.projectKey', 'CodeNarc'
        property 'sonar.scm.disabled', 'True'
        property 'sonar.log.level', 'DEBUG'
        property 'sonar.verbose', 'True'
        property 'sonar.sources', './src/main'
    }
}

task freeAnalysisBySonar {
  dependsOn 'sonarqube'
}
// END
```

## Tool Development
TODO:
1. Enable check testing within a standard way and format.
2. Create a script to generate a new rule/test files set like CodeNarc.

