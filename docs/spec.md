# Parsing and Transformation Specification


## Spec Outline
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



## Input spec

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


## Parse Spec for json
Currently only for json
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
    "keyPath": "json path to key (deviceId/id etc)",
    
}
```


## Deduplication spec
``` 
{
    "type": "<timeBased | keyBased >"
}
```


## Transform spec
Currently only for json
``` 
{
    "type": "<jolt | jsonPath>",
    "joltSpec": "<stringified jolt spec>",
    "jsonPathSpec": "<stringified json path spec>"
}
```


## Publish spec
Currently on for RMQ
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
