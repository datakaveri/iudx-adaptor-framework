### Parse Spec for json
Define how the downstream serialized data needs to be parsed.
Currently only support for json is provided.
The parse spec is responsible for extracting the `primaryKey or key` for the packet and the `timestamp` for watermarking, deduplication and late arrival message rejection.


&nbsp; 

The schema of the inputSpec is as shown below. **Bold** implies that the property is **required**.  

- **type**(String): Serialization formats.
  - json
  - xml (currently not supported) 

&nbsp; 

- **messageContainer**(String): Specifies the datastructure containing the messages to be processed.
  - array - A single response from the source contains multiple messages to be processed and the messages are contained in an array.
  - single - The response from the source contains only a single message to be processed. 

&nbsp; 
    
- **keyPath**(String): Path to the **primary key** of the message. This is an essential component of the stream processing pipeline and is used for deduplication, keyed process functions, etc. If the `{ "messageContainer": "array" } ` then treat each object in the array as the root json and define the key json path here. 

&nbsp; 
  
- **timestampPath**(String): A [Json Path](https://github.com/json-path/JsonPath) to the property in the message containing the timestamp for the message. This is an essential component of the stream processing pipeline and is used for deduplication, watermarking, late arrival message rejection, etc. If the `{ "messageContainer": "array" } ` then treat each object as the root json and define the timestamp json path here.

- inputTimeFormat(String): A ISO8601 styled string format specifier for the time format in messages coming from the source 
  
- outputTimeFormat(String): A ISO8601 styled string format specifier for the time format in messages emitted to the sink 

&nbsp; 
  
- trickle(Array[Map]): An array of all the keys to trickle (copy into every element of inner array) in case `{"messageContainer": "array"}`. Each array element is an object containing the following -
    - keyPath(String): Path to the key to trickle into each container element
    - keyName(String): Name of the key to store the value retrieved at keyPath 
  
  Trickle will always be performed first and can be used to copy for e.g **timestamp** and **primary key** into each object inside the message container before defining **timestampPath** and **keyPath**.

Example:
``` 
{
    "type": "json",
    "messageContainer": "array",
    "containerPath": "$.messages",
    "timestampPath": "$.time",
    "inputTimeFormat": "yyyy-MM-dd HH:mm:ss",
    "outputTimeFormat": "yyyy-MM-dd'T'HH:mm:ssXXX",
    "keyPath": "$.deviceId",
    "trickle": [
        {
            "keyPath": "$.someParentKey",
            "keyName": "someNewKeyName"
        },
        {
            "keyPath": "$.time",
            "keyName": "time"
        },
    ]
}
```
