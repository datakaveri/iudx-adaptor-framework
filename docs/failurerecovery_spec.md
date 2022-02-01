### Failure Recovery spec
Define job failure recovery settings.
The framework will ensure jobs restart on failure at fixed periods
or with an exponential backoff.

This spec is **optional**.


The schema of the failureRecoverySpec is as shown below.
**Bold** impolies that the property is **required**.


For fixed delays

- **type**(String): "fixed-delay"
- **attempts**(Number): Number of retry attempts
- **delay**(Number): milliseconds before retrying


For exponential backoff
- **type**(String): "exponential-delay"
- **initial-backoff**(Number): Initial delay in milliseconds
- **max-backoff**(Number): Maximum delay in milliseconds
- **backoff-multiplier**(Number): Multiply current delay value by this
- **jitter-factor**(Number): Fraction of the backoff duration to introduce skewness acrosss failed jobs
- **reset-backoff-threshold**(Number): Time in milliseconds specifying how long the job must be running without failure to reset the exponentially increasing backoff to its initial value


Example:
``` json
{
  "type": "exponential-delay",
    "initial-backoff": 1000,
    "max-backoff": 300000,
    "backoff-multiplier": 2.0,
    "reset-backoff-threshold": 3600000,
    "jitter-factor": 0.1
}


{
  "type": "fixed-delay",
    "attempts": 50,
    "delay": 60000
}


