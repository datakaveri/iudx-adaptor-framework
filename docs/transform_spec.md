### Transform spec
Define how to transform the data into the sink compatible format.
Support for [jolt](https://github.com/bazaarvoice/jolt) (jolt) vanilla javascript (js) and [javascript + json path](https://github.com/json-path/JsonPath) (jsPath) is provided currently.

#### Jolt (jolt)
Jolt is a library with a specification for a very general Json to Json Transformation. 
Demos of jolt specification based transformation can be found [here](http://jolt-demo.appspot.com/#inception).

The schema of the transformSpec for **jolt** is as shown below. **Bold** implies that the property is **required**.  

- **type**(String): jolt
- joltSpec(Array[Object]): jolt spec as shown in jolt demos

Example:
``` 
{
        "type": "jolt",
        "joltSpec": [
            { "operation": "shift", "spec": 
                { "time": "observationDateTime", "deviceId": "id", "k1": "k1" } },
            { "operation": "modify-overwrite-beta", "spec": 
                { "id": "=concat('datakaveri.org/123/', id)" } }
        ]
    }
```

#### Vanilla Javascript (JS)
Using this, one can provide an entire javascript method taking a single argument json and return a json. The script should be as shown below. 
With this, any java/script primitive type maybe extracted and manipulated.

The schema of the transformSpec for **js** is as shown below. **Bold** implies that the property is **required**.  

- **type**(String): js
- **script**(String): String (with escaped characters) of a method which transforms the entire json message and having the signature as shown below. 
    ```
    function tx(obj) {
        var out = {}; var inp = JSON.parse(obj);
        // Transform and add keys to out
        return JSON.stringify(out);
    }
    ```



#### Javascript + Json Path (jsPath)
Using this, one can extract json keys using [json path](https://github.com/json-path/JsonPath) and apply an inline javascript function on the extracted value.

The schema of the transformSpec for **jsPath** is as shown below. **Bold** implies that the property is **required**.  

- **type**(String): jsPath
- **template**(String): Template json of the output message
- **jsonPathSpec**(Array[Object]): An array of transformation operations which extract a key from the original json and place it in the path as specified in the template.  
  - **inputKeyPath**(String): Json Path from where the specific value must be extracted. Any java/script primitive type maybe extracted.
  - **outputKeyPath**(String): Json Path on which the extracted value must be placed into
  - valueModifierScript(String): Javascript inline function with the input variable being `value`.

Example
``` 
{
        "type": "jsPath",
        "template": "{ 'observationDateTime': '2021', 'co2': { 'avgOverTime': 100}, 'id': 'abc'}",
        "jsonPathSpec": [
            {
                "outputKeyPath": "$.observationDateTime",
                "inputValuePath": "$.time"
            },
            {
                "outputKeyPath": "$.co2.avgOverTime",
                "inputValuePath": "$.k1"
            },
            {
                "outputKeyPath": "$.id",
                "inputValuePath": "$.deviceId",
                "valueModifierScript": "value.split('-')[0]"
            }
        ]
    }
```
