#!/bin/sh

mvn clean -Preporting site site:stage $@
mvn scm-publish:publish-scm -DskipTests $@
