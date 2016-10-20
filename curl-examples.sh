#!/bin/bash
baseUrl="http://127.0.0.1:8090/ehr/rest"  #Assumes instance is running locally on port 8090

# Verify that jq is installed!
if ! type "jq" > /dev/null; then
  # Notify user
  echo "This script requires Jq but it's not installed. Please install from https://stedolan.github.io/jq/download/"
fi

# This is just a call that shows you the verbose output of authenticating against the endpoint - see below for storing token
# @Todo: I personally prefer posting 'body' since we then don't have to worry about stringifying\escaping special characters in passwords
curl -v -H -XPOST "$baseUrl/login" --data "username=admin&password=admin&organization=1234"

# This is an example of a POST body auth that I use in Termlex
# curl -v -H "Content-Type: application/json" -XPOST "$baseUrl/authenticate" -d'
# {
#     "username": "myusername",
#     "password": "mypassword",
#     "organization": "org"
# }'

# Authenticate with username and password. Read returned header and store token
token=$(curl -H -XPOST "$baseUrl/login" --data "username=admin&password=admin&organization=1234" | jq -r ".token")

printf "\nReceived token : $token \n"

# This query shows how to get profile for user
# @Todo: Looks like grails is not handling content negotiation properly... It only returns XML if I don't have the 'format=json' parameter
printf "\nGetting profile for user\n"
curl -H "Content-Type: application/json" -H "Authorization: Bearer $token" -XGET "$baseUrl/profile/admin?format=json"

# This query shows how to get profile for user
# @Todo: Looks like grails is not handling content negotiation properly... It only returns XML if I don't have the 'format=json' parameter
printf "\nGetting patients associated user\n"
curl -H "Content-Type: application/json" -H "Authorization: Bearer $token" -XGET "$baseUrl/patients/admin?format=json"