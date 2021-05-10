### Publish spec
Define where the transformed data should be published to.
Currently only rabbitmq sink is supported.


The schema of the publishSpec is as shown below. **Bold** implies that the property is **required**.  

- type(String): 
  - rmq (RabbitMQ)
- url(String): Fully qualified URL with protocol information but without port and authentication information. For e.g amqps://databroker.iudx.org.in
- port(Integer): Port
- uname(String): Username
- password(String): Password
- sinkName(String): The RMQ exchange name
- tagName(String): The RMQ routing key

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
