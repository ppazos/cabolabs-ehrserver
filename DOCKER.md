# Using Docker

## Build Image
docker build -t ehrserver .


## Run Server on port 8090
docker run -p "8090:8090" ehrserver 


## Run Tests
docker run ehrserver grails test-app -integration