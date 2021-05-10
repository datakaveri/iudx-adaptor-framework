### Input spec
Define the input downstream datasource with the polling interval.
The framework will periodically poll the api and obtain data from the downstream server.
For long interval jobs, use the schedule api and set `pollingInterval` as `-1`.

The schema of the inputSpec is as shown below. **Bold** implies that the property is **required**.  

- **type**(String): Protocols supported 
  - http
  - mqtt (currently not supported)
  - amqp (currently not supported)
  - grpc (currently not supported)
- **url**(String): Url of the data source
- **requestType**(String): 
  - If `{ "type": "http" }`
    - GET
    - POST
- **pollingInterval**(Long): Interval in *milliseconds*To be used mostly for short interval calls to the source. For scheduler (Long interval polling), use this field as `-1`.
  - `> 0` : Polls the source with time interval
  - `-1`: Long interval polling, use scheduler api
- headers(Map<<String:String>>): Headers to be supplied with the api call. Authorization credentials may be provided here. This is a Map between a String key and a String value.
- postBody(String): In case `{ "requestType": "POST" }` then this will be String encoded body that the source is expecting

Example:
``` 
{
    "type": "http",
    "url": "https://rs.iudx.org.in/ngsi-ld/v1/entity/abc",
    "requestType": "GET",
    "pollingInterval": 10000,
    "headers": {
        "content-type": "application/json"
    }
}
```
