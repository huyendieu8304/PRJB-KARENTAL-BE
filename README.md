
***
# Karental - Rent a car

```
cd existing_repo
git remote add origin https://github.com/huyendieu8304/PRJB-KARENTAL-BE.git
git branch -M main
git push -uf origin main
```
## Tech stack
* Build tool: maven >= 3.9.0
* Java: 17
* Framework: Spring boot 3.4.2
* DBMS: MySql 8.0.41, Redis 7.4.2
* Reverse Proxy: Nginx

## Prerequisites to run application
* Java JDK 17
* Docker engine

## Run Docker compose
In the root directory of the project, run
  `docker compose up -d`

After run docker compose, you should run the following command to check whether the config of redis RDB and AOF is right
```
docker exec -it redis redis-cli
INFO Persistence
CONFIG GET notify-keyspace-events
```
If you see `aof_enabled:1`, `rdb_changes_since_last_save`,`1) "notify-keyspace-events"` and
`2) "xE"` then every thing is alright!

## Start application
To run the application, first you have to provide following environment variables:

| Environment Variable   | What is it used for                                                                                                 |
|------------------------|---------------------------------------------------------------------------------------------------------------------|
| AWS_REGION             | The AWS region where you choose to run S3 on                                                                        |
| AWS_S3_BUCKETNAME      | The S3 bucket name that you use to save file                                                                        |
| APPLICATION_EMAIL      | The email address that all the mail in the system is sent from                                                      |
| AWS_ACCESS_KEY_ID      | The key id to access AWS, this is used to access AWS S3                                                             |
| AWS_ACCESS_KEY         | The key to access AWS, this is used to access AWS S3                                                                |
| DB_URL                 | The url of the datasource                                                                                           |
| DB_PASSWORD            | The password used to access database                                                                                |
| DB_USERNAME            | The username used to access database                                                                                |
| DOMAIN_NAME            | The domain of the website where you access it, if you run this application in local, input "localhost"              |
| EMAIL_PASSWORD         | The password of the application's email, this is used in Spring mail                                                |
| EMAIL_USERNAME         | The username of the application's email, eg: karental@gmail.com                                                     |
| FE_BASE_URL            | The base url of the front end, eg: if you have a react app run local it would be http://localhost:3000              |
| JWT_ACCESS_SECRET_KEY  | The key to sign JWT access token                                                                                    |
| JWT_REFRESH_SECRET_KEY | The key to sign JWT refresh token                                                                                   |
| SPRING_DATA_REDIS_HOST | The host where Redis run, eg: localhost for Redis running on local                                                  |
| TMN_CODE=T3GTKJIG      | The VNPay params to verify request                                                                                  |
| VNPAY_SECRET_KEY       | The VNPay params to verify request                                                                                  |
| VNPAY_RETURN_URL       | The address directed after successfully make transaction, eg: http://localhost:3000/#/my-wallet                     |
| MAILTRAP_PASSWORD      | If you not run the app in "prod" profile, please use Mailtrap, put Mailtrap password of your inbox in this variable |
| MAILTRAP_USERNAME      | If you not run the app in "prod" profile, please use Mailtrap, put Mailtrap username of your inbox in this variable |



After having necessary environment variable, you could open terminal and run `mvn spring-boot:run`
or if you want to run the application with a specific profile `-Dspring-boot.run.profiles=prod`

## APIDocument
This application has already implemented Spring doc, Swagger API, to get file .yml of this application, please do following steps:
1. Run the application in active profile is "dev"
2. Open browser and access http://localhost:8080/karental/swagger-ui.html
3. Access http://localhost:8080/karental/v3/api-docs.yaml to download swagger file
4. Save the file to your device

## Build application
`mvn clean package `
Or you want to build the project but skip testing
`mvn clean package -DskipTests `

If your computer doesn't have Maven, you can replace `mvn` with `./mvnw`, which reside in the source root.

## Build docker image
In root directory of the source code run `docker build -t <your-dockerhub-username>/karental:<tagname> .`
then push the image to docker hub `docker push <your-dockerhub-username>/karental:<tagname>`

## Run docker compose
To run docker compose you have to :
1. install docker compose on your server
2. upload all the directory /deploy to your server 
3. cd to deploy , run `docker compose down` then `docker compose up`
