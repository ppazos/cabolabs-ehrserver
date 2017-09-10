FROM java
ENV DEBIAN_FRONTEND noninteractive

# install grails
RUN curl -L https://github.com/grails/grails-core/releases/download/v2.5.5/grails-2.5.5.zip  -o /grails.zip
RUN unzip /grails.zip -d /opt
ADD . /app

WORKDIR /app

ENV GRAILS_HOME /opt/grails-2.5.5
ENV PATH $GRAILS_HOME/bin:$PATH

EXPOSE 8090
RUN grails dependency-report
# Define default command.
CMD ["grails", "-Dserver.port=8090", "run-app"]

