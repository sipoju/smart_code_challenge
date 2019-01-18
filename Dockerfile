FROM hseeberger/scala-sbt

################################# end

RUN mkdir -p /opt/smart
WORKDIR /opt/smart

EXPOSE 8080

COPY . /opt/smart/

RUN sbt assembly

CMD ["java", "-jar", "/opt/smart/target/scala-2.12/app.jar"]
