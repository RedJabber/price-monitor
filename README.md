# Price aggregator
In memory solution for one minute price statistics aggregation.

# Run from maven
```
mvn spring-boot:run
```
# Build docker image
```
docker build -t price-monitor:0.0.1 .
```                                   
or build and run
``` 
docker build -t price-monitor:0.0.1 . && docker run -p 80:8080 price-monitor:0.0.1
```
# API
## Swagger location
``` 
http://localhost:8080/swagger-ui.html
``` 
## Register tick
```
POST /tick
```

```
curl -X POST "http://localhost:8080/ticks" -H  "accept: */*" -H  "Content-Type: application/json" -d "{\"instrument\":\"instrumentName
\",\"price
\":100.1,\"timestamp\":0}"
```    
## Get statistics
Contains one minute data.

### By instrument
```
GET /statistics/{instrument}
```
```
curl -X GET "http://localhost:8080/statistics/instrumentName" -H  "accept: */*"
```
### General statistics 
```
GET /statistics
```
```
curl -X GET "http://localhost:8080/statistics" -H  "accept: */*"
```

## TODO
 * more description in API.
 * more tests.