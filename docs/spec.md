# Parsing and Transformation Specification


### Spec Outline
``` 
{
    "name": "<unique name for this adaptor",
    
    "inputSpec": {
    },
    
    // Below specs apply specifically for the inputSpec type
    
    "parseSpec": {
    },
    
    "deduplicationSpec": {
    },
    
    "transformSpec": {
    },
    
    "publishSpec": {
    }
}
``` 



### Input spec
Define the input downstream datasource with the polling interval.
The framework will periodically poll the api and obtain data from the downstream server.

``` 

{
    "type": "<http|protobuf>",
    "url": "<url with protocol>",
    "requestType": "<GET | POST",
    "pollingInterval": <number (if > threshold, will be batch job)>,
    "headers": {
        ... <map> ...
    },
    "postBody": "<templated body, later support for changing body params will be supported>"
}
```


### Parse Spec for json
Define how the downstream serialized data needs to be parsed.
Currently only support for json is provided.
An api may yield an array of data packets whose json path will have to be provided
in `containerPath` and messageContainer will have to be `array`.
All paths mentioned here are [JSON Path](https://github.com/json-path/JsonPath) paths.
Every packet needs a primary key whose path is defined by `keyPath`.
Every packet needs a primary timestamp whose path is defined by `timestampPath`.
``` 
{
    "type": "<json|xml>",
    "messageContainer": "<array - multiple messages in same message |
                          single - single message in one message>"
    "containerPath": "json path to container (list of objects) of messages in case
                        inputSpec.messageContainer == array.
                      Subsequent to this all paths will apply to individual objects
                      of the container."
    "timestampPath": "json path to time",
    "inputTimeFormat": "yyyy-MM-dd HH:mm:ss",
    "outputTimeFormat": "yyyy-MM-dd'T'HH:mm:ssXXX",
    "keyPath": "json path to key (deviceId/id etc)"
}
```


### Deduplication spec
Define the mechanism by which a duplicate data packet can be identified and rejected.
Currently only time based deduplication is supported.
``` 
{
    "type": "<timeBased | extraKeyBased >"
}
```


### Transform spec

#### Jolt Based
Define how to transform the data into the sink compatible format.
The jolt specification format can be understood from 
[Jolt](https://github.com/bazaarvoice/jolt).
``` 
{
    "type": "jolt",
    "joltSpec": "<stringified jolt spec>"
}
```

#### JS based
```
{
    "type": "js",
    "script": "function anyName(obj) { //do stuff; return newObj }"
}
```


### Publish spec
Define where the transformed data should be published to.
Currently only rabbitmq sink is supported.
`sinkName` specifies the "exchange" to which data needs to be published into
and `tagName` specifies the "routing-key".
``` 
{
    "type": "<rmq>"
    "url": "<url with protocol no port>",
    "port": <port>,
    "uname": "<uname>",
    "password": "<password>",
    "sinkName": "<exchange name>",
    "tagName": "<routing key>"
}
```

### Example
An example of the complete spec is below 
```
{
    "name": "test",

    "inputSpec": {
        "type": "http",
        "url": "http://mockserver:8080/simpleB",
        "requestType": "GET",
        "pollingInterval": 1000
    },

    "parseSpec": {
        "type": "json",
        "messageContainer": "array",
        "containerPath": "$.data",
        "keyPath": "$.deviceId",
        "timestampPath": "$.time",
        "inputTimeFormat": "yyyy-MM-dd HH:mm:ss",
        "outputTimeFormat": "yyyy-MM-dd'T'HH:mm:ssXXX"
    },
    
    "deduplicationSpec": {
        "type": "timeBased"
    },
    
    "transformSpec": {
        "type": "jolt",
        "joltSpec": [
            { "operation": "shift", "spec": 
                { "time": "observationDateTime", "deviceId": "id", "k1": "k1" } },
            { "operation": "modify-overwrite-beta", "spec": 
                { "id": "=concat('datakaveri.org/123/', id)" } }
        ]
    },

    "publishSpec": {
        "type": "rmq",
        "url": "amqp://mockrmq",
        "port": 5672,
        "uname": "guest",
        "password": "guest",
        "sinkName": "adaptor-test",
        "tagName": "test"
    }
}
```
