# Spring-boot and Jasypt integration with externalized Jasypt properties for Kubernetes Deployment

One of the most important aspect of 12-factor coding standards is externalization of environment specific variables. We will cover how we can easily achieve this using Spring Boot. We will also use JASYPT framework to encrypt these environment variables which are passed from the deployment yaml

##### We will be performing following steps:
- Download this source code 
- Create database, database user and table 
- Run JASYPT command to encrypt your user id and password 
- Run maven command to build the code 
- Run Docker commands to create the image and push the image to docker registry   
- Review the k8s/springjasypt-deployment.yaml and replace the container image and database server ip address 
- Connect to Kubernetes cluster
- Deploy the application to Kubernetes cluster and expose the service 
- Test your application 

### Download this source code 
Clone this github repository 
```
git clone https://github.com/prashantkumashi/spring-jasypt-env-config.git 
```
### Create database, database user and table 
Run following steps using mysql CLI or workbench. 
```
create database testdb;
create user 'appuser'@'%' identified by 'apppasswd123';
grant all privileges on testdb.* to 'appuser'@'%';
flush privileges;

use testdb;

CREATE TABLE `users` (
   `userid` int(11) NOT NULL AUTO_INCREMENT,
   `username` varchar(100) NOT NULL,
   `user_type` varchar(45) DEFAULT NULL,
   PRIMARY KEY (`userid`)
);
```
### Run JASYPT command to encrypt your database user id and password 
##### Run following command for encrypting database user id. 
> Note: If you do not have JASYCPT jar file in your local maven repository, you can choose to download from ```http://www.jasypt.org/download.html```. 

```
java -cp ~/.m2/repository/org/jasypt/jasypt/1.9.3/jasypt-1.9.3.jar  org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI input="appuser" password=Password algorithm=PBEWithMD5AndDES

----ENVIRONMENT-----------------

Runtime: Oracle Corporation OpenJDK 64-Bit Server VM 11.0.1+13



----ARGUMENTS-------------------

input: appuser
password: Password
algorithm: PBEWithMD5AndDES



----OUTPUT----------------------

2QOhTWf7XCnYDK2lI/eIOA==
```

##### Run the tool again for you password encryption 
```
java -cp ~/.m2/repository/org/jasypt/jasypt/1.9.3/jasypt-1.9.3.jar  org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI input="apppasswd123" password=Password algorithm=PBEWithMD5AndDES

----ENVIRONMENT-----------------

Runtime: Oracle Corporation OpenJDK 64-Bit Server VM 11.0.1+13



----ARGUMENTS-------------------

input: apppasswd123
password: Password
algorithm: PBEWithMD5AndDES



----OUTPUT----------------------

PxSPx1RxhGIoFlBbWLla5YlqpI68w4la
``` 

> IMPORTANT: You can also add other command line options for further hardening your encryption, the options chosen for encryptions should also be configured in the JASYPT encryptor configuration in your java bean definition. For more details go to http://www.jasypt.org/index.html 

### Review the application.properties and Jasypt Bean definition 
##### Check ```src/main/resources/application.properties``` file, it should reference the variables defined
```
spring.application.name=spring-jasypt-env-config

spring.datasource.url=jdbc:mysql://${MYSQL_DB_SERVER}:3306/testdb
spring.datasource.username=${MYSQL_DB_USER}
spring.datasource.password=${MYSQL_DB_PASSWD}
spring.datasource.tomcat.max-wait=20000
spring.datasource.tomcat.max-active=15
spring.datasource.tomcat.max-idle=10
spring.datasource.tomcat.min-idle=5

spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.MySQLDialect
spring.jpa.properties.hibernate.id.new_generator_mappings = false
spring.jpa.properties.hibernate.format_sql = false

logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=INFO
logging.level.com.concretepage= INFO

spring.main.allow-bean-definition-overriding=true 

server.port=8088

jasypt.encryptor.algorithm=${JASYPT_ENCRYPTOR_ALGORITHM}
jasypt.encryptor.password=${JASYPT_ENCRYPTOR_PASSWORD}
```

##### Check ```src/main/java/com/samples/pck/springjasypt/SpringJasyptEnvConfigApplication.java``` file
It has bean definition for EnvironmentStringPBEConfig. This object is a Jasypt configuration to read environment variables and set the Jasypt properties for alogorithm and other settings used.   

```
   @Bean
   public EnvironmentStringPBEConfig environmentStringPBEConfig() {
       EnvironmentStringPBEConfig config = new EnvironmentStringPBEConfig();
       config.setAlgorithm("jasypt.encryptor.algorithm");
       config.setPasswordEnvName("jasypt.encryptor.password");
       return config;
   }
```

### Build your code 
Run ```mvn clean install -DskipTests=true``` to build the code and create the distribution, if you are running it before setting the environment variables the unit tests would fail, add ```-DskipTests=true``` to skip tests. 

```
$ mvn clean install -DskipTests=true
[INFO] Scanning for projects...
[INFO]
[INFO] --< com.samples.pck.spring-jasypt-env-config:spring-jasypt-env-config >--
[INFO] Building spring-jasypt-env-config 0.0.1-SNAPSHOT
...
...
...
...
[INFO] --- maven-install-plugin:2.5.2:install (default-install) @ spring-jasypt-env-config ---
[INFO] Installing D:\ExamplesFromNet\mygit\spring-jasypt-env-config\target\spring-jasypt-env-config-0.0.1-SNAPSHOT.jar to C:\Users\pr20019686\.m2\repository\com\samples\pck\spring-jasypt-env-config\spring-jasypt-env-config\0.0.1-SNAPSHOT\spring-jasypt-env-config-0.0.1-SNAPSHOT.jar
[INFO] Installing D:\ExamplesFromNet\mygit\spring-jasypt-env-config\pom.xml to C:\Users\pr20019686\.m2\repository\com\samples\pck\spring-jasypt-env-config\spring-jasypt-env-config\0.0.1-SNAPSHOT\spring-jasypt-env-config-0.0.1-SNAPSHOT.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 01:32 min
[INFO] Finished at: 2019-10-18T20:07:19+05:30
[INFO] ------------------------------------------------------------------------
```

### Run Docker commands to create the image and push the image to docker registry   
Build your image and push to docker hub or any other container registry. We will be using docker hub, to connect with docker hub run the login command. 
```
sudo docker login docker.io
```

I have created a public repository on hub.docker.com with the name spring-jasypt-on-kubernetes which we will use for pushing the image
```
sudo docker build -t pr20180701/spring-jasypt-on-kubernetes .
sudo docker push pr20180701/spring-jasypt-on-kubernetes
```

### Review the k8s directory  
##### Namespace yaml - springjasypt-namespace.yaml
```
apiVersion: v1
kind: Namespace
metadata:
  name: samples
```

##### Deployment yaml - springjasypt-deployment.yaml
```
apiVersion: extensions/v1beta1
kind: Deployment
metadata:
  name: springjasypt
  namespace: samples
spec:
  replicas: 1
  template:
    metadata:
      labels:
        app: springjasypt
        version: v1
    spec:
      containers:
      - name: springjasypt
        image: <containerrepourlforimage>
        env:
        - name: JAVA_OPTS
          value: -Xms128m -Xmx512m -XX:PermSize=128m -XX:MaxPermSize=128m -XX:+UseG1GC -Djava.security.egd=file:/dev/urandom
        - name: MYSQL_DB_USER
          value: "ENC(1cWxyUbkZeqw+LYA/LyRhg==)"
        - name: MYSQL_DB_SERVER
          value: "<databasehostname or ip>"
        - name: MYSQL_DB_PASSWD
          value: "ENC(wUVs1lmbkgB7OqSPhKeFKVTY8siHCoag)"
        - name: JASYPT_ENCRYPTOR_ALGORITHM
          value: "PBEWithMD5AndDES"
        - name: JASYPT_ENCRYPTOR_PASSWORD
          value: "Password"
        ports:
        - containerPort: 8088
        securityContext:
          runAsNonRoot: true
          runAsUser: 10001
          capabilities:
            drop:
              - all
            add:
              - NET_BIND_SERVICE
          readOnlyRootFilesystem: true
        volumeMounts:
        - mountPath: /tmp
          name: tmp-volume
      volumes:
        - name: tmp-volume
          emptyDir:
            medium: Memory
      nodeSelector:
        beta.kubernetes.io/os: linux
```

##### Replace the container image and database server ip address based on your environment values
In the example demonstration we will replace the value for ```<containerrepourlforimage>``` to pr20180701/spring-jasypt-on-kubernetes 
```
      - name: springjasypt
        image: <containerrepourlforimage>
```

Replace the value ```<databasehostname or ip>``` with the database server ip address
```
        - name: MYSQL_DB_SERVER
          value: "<databasehostname or ip>"
```

##### Review springjasypt-service.yaml file
```
apiVersion: v1
kind: Service
metadata:
  name: springjasypt
  labels:
    app: springjasypt
  namespace: samples
spec:
  ports:
  - name: http
    port: 80
    targetPort: 8088
  selector:
    app: springjasypt
  type: NodePort
```

### Connect to Kubernetes cluster
### Deploy the application to Kubernetes cluster and expose the service 


### Test your application 

##### Run the ```curl``` command to add sample data
```
$ curl -d '{"username":"ironman","userType":"enduser"}' 'http://localhost:8088/user' --header "Content-Type: application/json"

{"userId":2,"username":"ironman","userType":"enduser"}
```

```
$ curl -d '{"username":"hulk","userType":"enduser"}' 'http://localhost:8088/user' --header "Content-Type: application/json"

{"userId":3,"username":"hulk","userType":"enduser"}
```

##### Check the results by fetching all results
```
$ curl 'http://localhost:8088/user/1'
{"userId":1,"username":"ironman","userType":"enduser"}
```


## Part 2 - We will be covering setting up environment variables using Docker and deploy on a Kubernetes cluster
