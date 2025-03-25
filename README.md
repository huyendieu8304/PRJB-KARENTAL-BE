# Rent Car



## Getting started

To make it easy for you to get started with GitLab, here's a list of recommended next steps.

Already a pro? Just edit this README.md and make it your own. Want to make it easy? [Use the template at the bottom](#editing-this-readme)!

## Add your files

- [ ] [Create](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#create-a-file) or [upload](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#upload-a-file) files
- [ ] [Add files using the command line](https://docs.gitlab.com/ee/gitlab-basics/add-file.html#add-a-file-using-the-command-line) or push an existing Git repository with the following command:

```
cd existing_repo
git remote add origin http://git.fa.edu.vn/hn25_cpl_pjb_01/hn25_cpl_pjb_01_g2/rent-car.git
git branch -M main
git push -uf origin main
```

## Integrate with your tools

- [ ] [Set up project integrations](http://git.fa.edu.vn/hn25_cpl_pjb_01/hn25_cpl_pjb_01_g2/rent-car/-/settings/integrations)

## Collaborate with your team

- [ ] [Invite team members and collaborators](https://docs.gitlab.com/ee/user/project/members/)
- [ ] [Create a new merge request](https://docs.gitlab.com/ee/user/project/merge_requests/creating_merge_requests.html)
- [ ] [Automatically close issues from merge requests](https://docs.gitlab.com/ee/user/project/issues/managing_issues.html#closing-issues-automatically)
- [ ] [Enable merge request approvals](https://docs.gitlab.com/ee/user/project/merge_requests/approvals/)
- [ ] [Set auto-merge](https://docs.gitlab.com/ee/user/project/merge_requests/merge_when_pipeline_succeeds.html)

## Test and Deploy

Use the built-in continuous integration in GitLab.

- [ ] [Get started with GitLab CI/CD](https://docs.gitlab.com/ee/ci/quick_start/index.html)
- [ ] [Analyze your code for known vulnerabilities with Static Application Security Testing(SAST)](https://docs.gitlab.com/ee/user/application_security/sast/)
- [ ] [Deploy to Kubernetes, Amazon EC2, or Amazon ECS using Auto Deploy](https://docs.gitlab.com/ee/topics/autodevops/requirements.html)
- [ ] [Use pull-based deployments for improved Kubernetes management](https://docs.gitlab.com/ee/user/clusters/agent/)
- [ ] [Set up protected environments](https://docs.gitlab.com/ee/ci/environments/protected_environments.html)

***
# Karental - Rent a car

## Tech stack
* Build tool: maven >= 3.9.0
* Java: 17
* Framework: Spring boot 3.4.2
* DBMS: MySql

## Prerequisites to run application
* Java JDK 17
* docker engine

## run necessary Docker containers
  `docker compose up -d`

After run docker compose, you should run the following command to check whether the config of redis RDB and AOF is right
```
docker exec -it redis redis-cli
INFO Persistence
```
If you see `aof_enabled:1` and `rdb_changes_since_last_save` then every thing is alright!

## Start application
`mvn spring-boot:run`

## Build application
`mvn clean package`
Or you want to build the project but skip testing
`mvn clean package -DskipTests`

## Build docker image
In root directory of the source code run `docker build -t huyendieu8304/karental:<tagname> .`
then push the image to docker hub `docker push huyendieu8304/karental:<tagname>`
## Run docker compose
To run docker compose you have to :
1. cd deploy
2. add content file ".env" contain environment variables of the application
3. run `docker compose down` then `docker compose up -d`