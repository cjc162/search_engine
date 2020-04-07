FROM openjdk:13
VOLUME /tmp
COPY SearchEngine-db2bd8beea67.json SearchEngine-db2bd8beea67.json
ENV GOOGLE_APPLICATION_CREDENTIALS "./SearchEngine-db2bd8beea67.json"
ADD target/docker-spring-boot.jar docker-spring-boot.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/docker-spring-boot.jar"]