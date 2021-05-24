### Parse Spec for json
Define how the downstream serialized data needs to be parsed.
Currently only support for json is provided.
The parse spec is responsible for extracting the `primaryKey or key` for the packet and the `timestamp` for watermarking, deduplication and late arrival message rejection.


The schema of the inputSpec is as shown below. **Bold** implies that the property is **required**.  

- **type**(String): Serialization formats. 
  - json
  - xml (currently not supported)
- **messageContainer**(String): Specifies the datastructure containing the messages to be processed. 
  - array - A single response from the source contains multiple messages to be processed and the messages are contained in an array.
  - single - The response from the source contains only a single message to be processed.
- **timestampPath**(String): A [Json Path](https://github.com/json-path/JsonPath) to the property in the message containing the timestamp for the message. If the `{ "messageContainer": "array" } ` then treat each object as the root json and define the timestamp json path here.
- inputTimeFormat(String): A ISO8601 styled string format specifier for the time format in messages coming from the source
- outputTimeFormat(String): A ISO8601 styled string format specifier for the time format in messages emitted to the sink
- trickle(Array[Map]): An array of all the keys to trickle in case `messageContainer == array`. Each array element is an object containing the following - 
    - keyPath(String): Path to the key to trickle into each container element
    - keyName(String): Name of the key to store the value retrieved at keyPath

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
        }
    ]
}
```
