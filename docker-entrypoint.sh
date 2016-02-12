#!/bin/bash
set -eo pipefail
mysqld_safe &
sleep 5
echo "CREATE DATABASE IF NOT EXISTS ehrserver" | mysql -uroot
sleep 5
grails -Dserver.port=8090 run-app