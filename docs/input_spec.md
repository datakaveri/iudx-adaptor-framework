### Input spec

Define the input downstream datasource with the polling interval.
The framework will periodically poll the api and obtain data from the downstream server.
For long interval jobs, use the schedule api and set `pollingInterval` as `-1` and set `boundedJob`
to `true`. If state of the scheduled job needs to be maintained after
the job has completed, state can be maintained in minio by providing
credentials to it in `minioConfig`.

The schema of the inputSpec is as shown below. **Bold** implies that the property is **required**.

- **type**(String): Protocols supported
    - http
    - rmq
    - mqtt (currently not supported)
    - grpc (currently not supported)

#### http

- **url**(String): Url of the data source. Dynamic parameters maybe indicated with a placeholder
  text. Values for these placeholders maybe generated using javascript and replaced. For
  e.g, `https://api.test.org/myPathParameter?q1=myQ1Parameter`.
- postBody(String): In case `{ "requestType": "POST" }` then this will be String encoded body that
  the source is expecting. Dynamic parameters maybe indicated with a placeholder text. Values for
  these placeholders maybe generated using javascript and replaced.
- requestGenerationScripts(Array[Object]): Javascript placeholder parameter generation scripts .
  This script will be called before the api call is made.
    - **in**(String): Enum `[url, body]`
    - **pattern**(String): Pattern whose value will be replaced by output of script
    - **script**(String): Javascript to generate the parameter. Note: currently this script doesn't
      accept any input parameters.
- **requestType**(String):
    - If `{ "type": "http" }`
        - GET
        - POST
- **pollingInterval**(Long): Interval in *milliseconds*To be used mostly for short interval calls to
  the source. For
  scheduler (Long interval polling), use this field as `-1`.
    - `> 0` : Polls the source with time interval
    - `-1`: Long interval polling, use scheduler api
- headers(Array[Object]): Headers to be supplied with the api call. Authorization credentials may be
  provided here. This
  is a Map between a String key and a String value. Each object of the array contains
    - **key**(String): Key of the header
    - **value**(String): Value of the header
- requestTimeout(Long): Set the request timeout in seconds. Default is set to 10 seconds
- boundedJob(Boolean): Set the value to be true for scheduled jobs (bounded jobs)
- minioConfig(Object): For scheduled bounded jobs state will be maintained in minio
    - **url**(String) : Minio host URL
    - **bucket**(String) : Bucket Name to store objects
    - **stateName**(String) : Unique name which will be used as file name while saving state
    - **accessKey**(String) : Minio access key
    - **secretKey**(String) : Minio secret key

#### RMQ

- **uri** (String): Fully qualified URI with protocol information including port, vhost and
  authentication information. For e.g amqps://user:password@databroker.iudx.org.in:24567/vhostname
- **queueName** (String): RMQ queue name from which rules are streamed
- parseSpec (Object): Optionally it can contain parseSpec Refer [Parse Spec](parse_spec.md)  docs
  for the same

Example (ETL Job):

``` json
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

Example (ETL Scheduled Job):

```json
{
  "type": "http",
  "url": "https://rs.iudx.org.in/ngsi-ld/v1/entity/abc",
  "requestType": "GET",
  "pollingInterval": -1,
  "headers": {
    "content-type": "application/json"
  },
  "boundedJob": true,
  "minioConfig": {
    "url": "http://minio1:9000",
    "bucket": "custom-state",
    "stateName": "test-state-job",
    "accessKey": "minio",
    "secretKey": "minio123"
  }
}
```

Example (Rule Job)

```json
{
  "type": "rmq",
  "uri": "amqp://guest:guest@rmq:5672",
  "queueName": "adaptor-test",
  "parseSpec": {
    "type": "json",
    "messageContainer": "single",
    "timestampPath": "$.observationDateTime",
    "staticKey": "rules"
  }
```