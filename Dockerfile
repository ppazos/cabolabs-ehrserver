FROM java
ENV DEBIAN_FRONTEND noninteractive
RUN apt-get update
RUN apt-get install -y mysql-server

# install grails
RUN curl -L https://github.com/grails/grails-core/releases/download/v2.5.3/grails-2.5.3.zip  -o /grails.zip
RUN unzip /grails.zip -d /opt
ADD . /app

WORKDIR /app

ENV GRAILS_HOME /opt/grails-2.5.3
ENV PATH $GRAILS_HOME/bin:$PATH

EXPOSE 8090
RUN grails dependency-report
RUN chmod +x /app/docker-entrypoint.sh
# Define default command.
ENTRYPOINT ["/app/docker-entrypoint.sh"]
CMD ["grails", "-Dserver.port=8090", "run-app"]

