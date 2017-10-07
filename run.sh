#!/bin/bash
# This should just download the correct version of Grails as specified in the project and launch the application on port 8090
./grailsw -Dserver.port=8090 -Duser.timezone=UTC run-app