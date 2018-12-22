# Docker

## Build Image

    docker build . -t ehr-server

## Run EHRServer in local_prod mode

### Run MySQL container

    docker run \
      --name ehr-db \
      -p "3306:3306" \
      -e MYSQL_ROOT_PASSWORD=root \
      -e MYSQL_DATABASE=ehrserver \
      -e MYSQL_USER=ehr \
      -e MYSQL_PASSWORD=ehr \
      -d \
      mysql:5

### Run EHRServer container on port 8090 with grails local_prod environment

    docker run --name \
    ehr-server \
    --link ehr-db:mysql \
    -p "8090:8090" \
    -e EHRSERVER_MYSQL_DB_HOST=ehr-db \
    -e EHRSERVER_MYSQL_DB_PORT=3306 \
    -e EHRSERVER_MYSQL_DB_USERNAME=ehr \
    -e EHRSERVER_MYSQL_DB_PASSWORD=ehr \
    -e EHRSERVER_DB_NAME=ehrserver \
    -e EHRSERVER_APP_NAME=ehrserver \
    -e EHRSERVER_REST_SECRET=ehr \
    -d \
    ehr-server grails -Dgrails.env=local_prod -Dserver.port=8090 -Duser.timezone=UTC run-app

## Run EHRServer in local development mode

### Run MySQL container

    docker run \
      --name ehr-db \
      -p "3306:3306" \
      -e MYSQL_ROOT_PASSWORD=root \
      -e MYSQL_DATABASE=ehrserver \
      -e MYSQL_USER=ehr \
      -e MYSQL_PASSWORD=ehr \
      -d \
      mysql:5

### Run EHRServer container on port 8090 with grails development environment
    docker run --name ehr-server \
    --link ehr-db:mysql \
    -p "8090:8090" \
    -e EHRSERVER_APP_NAME=ehrserver \
    -e EHRSERVER_REST_SECRET=ehrsecret \
    -v $PWD:/app
    -d ehr-server

## Run Tests

    docker run \
      --name ehr-db \
      -p "3306:3306" \
      -e MYSQL_ROOT_PASSWORD=toor \
      -e MYSQL_DATABASE=ehrserver \
      -e MYSQL_USER=ehr \
      -e MYSQL_PASSWORD=ehr \
      -d \
      mysql:5

    docker run \
    --name ehr-server-test \
    --link ehr-db:mysql \
    -e EHRSERVER_REST_SECRET=ehr \
    -e EHRSERVER_MYSQL_DB_HOST=ehr-db \
    ehr-server grails test-app -integration
