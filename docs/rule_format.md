### Rule Format

- type (String): Supports 2 values based on the usage
    - RULE
    - DELETE

#### Rule Type
- windowMinutes (Number): Data window required for this query. It will be used to expire data in the table
- sqlQuery (String): SQL format query whuch needs to be executed to receive alerts
- sinkExchangeKey (String): RMQ exchange key to which alert will be published
- sinkRoutingKey (String): RMQ routing key for the corresponding exchange 

#### DELETE Type
- ruleId (String): Rule Id which needs to be deleted


Example Rule:

```json
{
    "type": "RULE",
    "sqlQuery": "select * from TABLE",
    "windowMinutes": 1,
    "sinkExchangeKey": "rule-result-test",
    "sinkRoutingKey": "rule-result-test"
}
```

Example Delete RUle

```json
{
    "ruleId": "<rule-id>",
    "type": "DELETE"
}
```
