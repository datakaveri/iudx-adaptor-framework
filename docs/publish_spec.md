### Publish spec
Define where the transformed data should be published to.
Currently only rabbitmq sink is supported.


The schema of the publishSpec is as shown below. **Bold** implies that the property is **required**.  

- type(String): 
  - rmq (RabbitMQ)
  - postgres
- #### RMQ
  - uri(String): Fully qualified URI with protocol information including port, vhost and authentication information. For e.g amqps://user:password@databroker.iudx.org.in:24567/vhostname
  - sinkName(String): The RMQ exchange name
    - Note: For Rule engine jobs this config is not required.
  - tagName(String): The RMQ routing key
    - Note: For Rule engine jobs this config is not required

```json
{
    "type": "<rmq>"
    "uri": "<uri with protocol, uname, password, vhostinfo, etc>",
    "sinkName": "<exchange name>",
    "tagName": "<routing key>"
}
```

- #### Postgres
  - url (string) - Database URL in JDBC url format. Eg: jdbc:postgresql://postgres:5432/iudx-adaptor
  - username (string) - Database username
  - password (string) - Database user's password
  - tableName (string) - Table name where data needs to be published
  - schema (object) - It is an object where key indicates column name and value indicates column's type
    - Supported types of columns are 
      - timestamp (timestamp column with time zone)
      - string (text type columns)
      - int (integer)
      - float (floating point numbers)
      - time (time string eg 22:10:44)
      - point (location coordinates array)

Example (Postgres):
```json
{
    "type": "postgres",
    "url": "jdbc:postgresql://postgres:5432/iudx-adaptor",
    "username": "root",
    "password": "adaptor@db",
    "tableName": "test",
    "schema": {
      "observationDateTime": "timestamp",
      "id": "string",
      "co2AvgOverTime": "int"
    }
}
```