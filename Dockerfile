FROM openjdk:8-alpine AS build
RUN apk update && apk add wget unzip git
RUN wget https://github.com/grails/grails-core/releases/download/v3.3.10/grails-3.3.10.zip
RUN unzip grails-3.3.10.zip -d /opt/
ENV GRAILS_HOME=/opt/grails-3.3.10
ENV PATH="${GRAILS_HOME}/bin:${PATH}"
WORKDIR /app
# COPY . .
RUN git clone https://github.com/ppazos/cabolabs-ehrserver.git .
RUN /opt/grails-3.3.10/bin/grails war

FROM tomcat:8-jdk8-openjdk
COPY --from=build /app/opts/base_opts /app/opts/base_opts
COPY --from=build /app/build/libs/app-2.3.war /usr/local/tomcat/webapps/ROOT.war
COPY --from=build /app/grails-app/conf/application.yml /app/config.yml
WORKDIR /app
EXPOSE 8080
CMD ["catalina.sh", "run"]
