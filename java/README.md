## 98Point6 Drop Token Java shim ##
This is provided as a possible starting point for a Java implementation. This code, based on [Dropwizard](http://www.dropwizard.io/1.1.0/docs/), requires maven and Java 1.8.

Feel free to change **everything**.
## Compile ##
`mvn clean verify`
## Run Service ##
`java -jar target/9dt-backend-1.0-SNAPSHOT.jar server src/main/resources/local.yml`
## Test service manually ##
```
 curl --header "Content-type: Application/json" -X POST http://localhost:8080/drop_token -d'{ "players":["p1", "p2"], "rows":4, "columns":4}'
```
