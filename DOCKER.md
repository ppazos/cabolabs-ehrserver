# Docker usage

## Build Image

    docker build . -t ehr-server

## Run MySQL Container

    docker run \
      --name ehr-db \
      -e MYSQL_ROOT_PASSWORD=thisistherootpassword \
      -e MYSQL_DATABASE=ehr \
      -e MYSQL_USER=ehr \
      -e MYSQL_PASSWORD=ehr \
      -d \
      mysql:5

## Run EHRServer Container on port 8090

    docker run \
    --name ehr-server \
    --link ehr-db:mysql \
    -p "8090:8090" \
    -e EHRSERVER_REST_SECRET=ehr \
    -e EHRSERVER_MYSQL_DB_HOST=ehr-db \
    -e EHRSERVER_DB_NAME=ehr \
    -e EHRSERVER_MYSQL_DB_USERNAME=ehr \
    -e EHRSERVER_MYSQL_DB_PASSWORD=ehr \
    -d \
    ehr-server

## Run Tests

    docker run \
    --name ehr-server-test \
    --link ehr-db:mysql \
    -e EHRSERVER_REST_SECRET=ehr \
    -e EHRSERVER_MYSQL_DB_HOST=ehr-db \
    ehr-server grails test-app -integration


## Run EHRServer with grails local_prod environment

    docker run --name \
    ehr-server \
    --link ehr-db:mysql \
    -p "8090:8090" \
    -e EHRSERVER_REST_SECRET=ehr \
    -e EHRSERVER_MYSQL_DB_HOST=ehr-db \
    -e EHRSERVER_MYSQL_DB_PORT=3306 \
    -e EHRSERVER_DB_NAME=ehr \
    -e EHRSERVER_MYSQL_DB_USERNAME=ehr \
    -e EHRSERVER_MYSQL_DB_PASSWORD=ehr \
    -d \
    ehr-server grails -Dgrails.env=local_prod -Dserver.port=8090 -Duser.timezone=UTC run-app
