# Parsing and Transformation Specification


## Spec Outline
``` 
{
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
    "type": "<json|xml>",
    "messageContainer": "<array - multiple messages in same message |
                          single - single message in one message>"
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
    "deduplicateType": "<timeBased | keyBased >"
}
```


## Transform spec
Currently only for json
``` 
{
    "transformType": "<jolt | jsonPath>",
    "joltSpec": "<stringified jolt spec>",
    "jsonPathSpec": "<stringified json path spec>"
}
```
