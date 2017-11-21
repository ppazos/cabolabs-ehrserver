#!/bin/bash
set -e

if [ "$1" = 'grails' ]; then
        echo "Running grails"

echo "Starting Mysql server"
mysqld_safe &
sleep 5
echo "Create Mysql table"
echo "CREATE DATABASE IF NOT EXISTS ehrserver" | mysql -uroot
echo "CREATE DATABASE IF NOT EXISTS ehrservertest" | mysql -uroot
sleep 1

fi

exec "$@"