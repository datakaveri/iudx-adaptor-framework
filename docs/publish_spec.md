### Publish spec
Define where the transformed data should be published to.
Currently only rabbitmq sink is supported.


The schema of the publishSpec is as shown below. **Bold** implies that the property is **required**.  

- type(String): 
  - rmq (RabbitMQ)
- uri(String): Fully qualified URI with protocol information including port, vhost and authentication information. For e.g amqps://user:password@databroker.iudx.org.in:24567/vhostname
- sinkName(String): The RMQ exchange name
- tagName(String): The RMQ routing key

``` 
{
    "type": "<rmq>"
    "uri": "<uri with protocol, uname, password, vhostinfo, etc>",
    "sinkName": "<exchange name>",
    "tagName": "<routing key>"
}
```
