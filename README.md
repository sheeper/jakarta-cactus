# CACTUS
###### The throrn in your bug's side

![Java CI with Maven](https://github.com/dgeissl/jakarta-cactus/workflows/Java%20CI%20with%20Maven/badge.svg)

## Introduction

This is a clone of the original Jakarta project Cactus.

The original documentation is available online at [Jakarta Cactus](http://jakarta.apache.org/cactus).
The documentation is also packaged in the distributions (which you can get on  http://archive.apache.org/dist/jakarta/cactus/).

## Project Status

As Cactus has been officialy retired in 2011/08/05 due to lack of development community (For more information, please explore the [Attic](http://attic.apache.org/).) but migrating a large testbase to newer techniques is a long term process - that may never happen - i'll start a fork from the [SVN](http://svn.apache.org/repos/asf/jakarta/cactus/trunk) and make minor improvements here and there.

## Goals & Plans

As written before this fork aims to fix some minor problems for projects that are still using Cactus for server side testing. The focus will be on the framework.

The first steps will be:

- migrating to maven 3 (from maven 2) as buildsystem
- migrating the code base to java 6 (github [build infrastructure](https://docs.travis-ci.com/user/languages/java/#Testing-Against-Multiple-JDKs) ain't supporting older versions and even this has reached it s EOL in November 2012)
- cleanup the maven configurations (encoding, managing versions, update plugins, update dependencies, drop steps that are no longer required on github)

Possible future actions will be:
- check how releases may workout on github (as this is my first os here :-) )
- check github documentation features vs. maven site
- as Cactus supports some very old APIs and even a maven1 itegration those features may be dropped over time (depending on the effort of keeping them alive - at least for the build)

The longer term goal is to check the effort of JUnit4 integration (TestRunner) to provide a easier migration path to newer techniques.

There are currently no plans to revive cactus to a full blown - state of the art - in container - testing framework again.

## Participating

As this project is hosted on github you may use the usual instruments provided (as there are [reporting bugs](https://github.com/dgeissl/jakarta-cactus/issues) providing [pull requests](https://github.com/dgeissl/jakarta-cactus/pulls)).
The [JIRA](https://issues.apache.org/jira/browse/cactus/?selectedTab=com.atlassian.jira.jira-projects-plugin:summary-panel) tickets will not be migrated.

To build Cactus from source, please refer to the [building instructions](BUILDING.txt).

Thank you.
The Cactus Team
