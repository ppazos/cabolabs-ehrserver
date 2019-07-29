FROM java

RUN curl -L https://github.com/grails/grails-core/releases/download/v2.5.6/grails-2.5.6.zip -o /grails.zip
RUN unzip /grails.zip -d /opt
ENV GRAILS_HOME /opt/grails-2.5.6
ENV PATH $GRAILS_HOME/bin:$PATH

ADD . /app

WORKDIR /app

EXPOSE 8090
CMD ["grails", "prod", "-Dserver.port=8090", "run-app"]