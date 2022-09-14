### Rule Source Spec

Define the rule source config, currently only RMQ source is supported for rule source.

The schema of the ruleSourceSpec is as shown below. **Bold** implies that the property is
**required**.

- **type** (String): Protocols supported
    - rmq
- **uri** (String): Fully qualified URI with protocol information including port, vhost and
  authentication information. For e.g amqps://user:password@databroker.iudx.org.in:24567/vhostname
- **queueName** (String): RMQ queue name from which rules are streamed
- parseSpec (Object): Optionally it can contain parseSpec Refer [Parse Spec](parse_spec.md)  docs
  for the same

Example:

``` json
{
    "type": "rmq",
    "uri": "amqp://guest:guest@rmq:5672",
    "queueName": "rules-test",
    "parseSpec": {
      ...
    }
}
```
