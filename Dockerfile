##STAGE 1: BUILD PACKAGE
#FROM maven:3.9.8-amazoncorretto-21 AS build
#
## Copy source code and pom.xml file to /app folder
#WORKDIR /app
#COPY pom.xml .
#COPY src ./src
#
## Build source code with maven
#RUN mvn package -DskipTests
#
##STAGE 2: create image
FROM amazoncorretto:17

# Change from ARG to ENV, ENV would be used during run
ENV AWS_REGION=${AWS_REGION}
ENV AWS_S3_BUCKETNAME=${AWS_S3_BUCKETNAME}
ENV AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID}
ENV AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}
ENV DB_URL=${DB_URL}
ENV DB_USERNAME=${DB_USERNAME}
ENV DB_PASSWORD=${DB_PASSWORD}
ENV EMAIL_USERNAME=${EMAIL_USERNAME}
ENV EMAIL_PASSWORD=${EMAIL_PASSWORD}
ENV JWT_ACCESS_SECRET_KEY=${JWT_ACCESS_SECRET_KEY}
ENV JWT_REFRESH_SECRET_KEY=${JWT_REFRESH_SECRET_KEY}
ENV JWT-CSRF-SECRET-KEY=${JWT-CSRF-SECRET-KEY}
ENV TMN_CODE=${TMN_CODE}
ENV VNPAY_SECRET_KEY=${VNPAY_SECRET_KEY}
ENV SPRING_DATA_REDIS_HOST=${SPRING_DATA_REDIS_HOST}
ENV APPLICATION_EMAIL=${APPLICATION_EMAIL}
ENV FE_BASE_URL=${FE_BASE_URL}
ENV VNPAY_RETURN_URL=${VNPAY_RETURN_URL}
ENV DOMAIN_NAME=${DOMAIN_NAME}

#change directory to /app
WORKDIR /app

# cop file jar to the directory of container
COPY target/karental-0.0.1-SNAPSHOT.jar app.jar
#COPY --from=build /app/target/*.jar app.jar

##Execute Redis
#RUN echo "Starting Redis..." && redis-server /etc/redis/redis.conf --daemonize yes

#expose port
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

