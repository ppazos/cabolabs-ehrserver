# Create base docker container
FROM java as grails-base

## install grails
RUN set -x \
      && curl -L https://github.com/grails/grails-core/releases/download/v2.5.6/grails-2.5.6.zip  -o /grails.zip \
      && unzip /grails.zip -d /opt \
      && rm grails.zip

ENV GRAILS_HOME /opt/grails-2.5.6
ENV PATH $GRAILS_HOME/bin:$PATH

# Create app container
FROM grails-base

WORKDIR /app
COPY . .

RUN grails dependency-report

CMD ["grails", "-Dserver.port=8090", "run-app"]

EXPOSE 8090
