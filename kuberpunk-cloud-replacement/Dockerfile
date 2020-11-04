FROM openjdk:11-jdk-slim
ENV APP_FILE kuberpunk-cloud-replacement-0.0.2.jar
ENV APP_HOME /usr/apps
EXPOSE 8080
COPY target/$APP_FILE $APP_HOME/
WORKDIR $APP_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar $APP_FILE"]

