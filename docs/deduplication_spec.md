### Deduplication spec
Define the mechanism by which a duplicate data packet can be identified and rejected.
Currently only time based deduplication is supported.

The schema of the deduplicationSpec is as shown below. **Bold** implies that the property is **required**.  

- **type**(String): Type of deduplication 
  - timeBased: Deduplicate based on the timestamp and key (as defined in [parse spec](./parse_spec.md))
  - extraKeyBased (Currently not supported): Deduplicate based on an additional key which is not the primary key as mentioned in [parse spec](./parse_spec.md)

Example:
``` 
{
    "type": "timeBased"
}
```
