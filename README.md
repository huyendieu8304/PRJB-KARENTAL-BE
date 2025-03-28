
***
# Karental - Rent a car

```
cd existing_repo
git remote add origin http://git.fa.edu.vn/hn25_cpl_pjb_01/hn25_cpl_pjb_01_g2/rent-car.git
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

## run necessary Docker containers
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
`mvn spring-boot:run`
or if you want to run the application with a specific profile `-Dspring-boot.run.profiles=prod`

## Build application
`mvn clean package`
Or you want to build the project but skip testing
`mvn clean package -DskipTests`

## Build docker image
In root directory of the source code run `docker build -t <your-dockerhub-username>/karental:<tagname> .`
then push the image to docker hub `docker push huyendieu8304/karental:<tagname>`

## Run docker compose
To run docker compose you have to :
1. install docker compose on your server
2. upload all the directory /deploy to your server 
3. cd to deploy , run `docker compose down` then `docker compose up`