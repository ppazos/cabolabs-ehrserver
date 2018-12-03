#!/bin/bash
set -e

if [ "$1" = 'grails' ]; then
        echo "Running grails"
fi

exec "$@"
