## Rules Engine Jobs


### Pipeline Specification file

A pipeline maybe specified in Json format and submitted to the framework server
to auto-generate JAR files and run them.
The following is the spec outline to be followed in making a configuration file.

#### Spec Outline

``` 
{
    "name": "<unique name for this adaptor",
    "adaptorType": "RULES",

    "failureRecoverySpec": {
    },
    
    "inputSpec": {
    },

    "ruleSourceSpec": {
    },

    "publishSpec": {
    }
}
``` 

Detailed explanation of the individual specs are given below.

- [Meta spec](meta_spec.md)
- [Failure Recovery Spec](failurerecovery_spec.md)
- [Input Spec](input_spec.md)
- [Rule Source Spec](rule_source_spec.md)
- [Publish Spec](publish_spec.md)

The spec can then be submitted to the adaptor server which will validate it and generate a JAR for
the entire pipeline.
