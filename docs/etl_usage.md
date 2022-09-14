## ETL Pipeline

In adaptor jobs there are 2 fundamental modes of operation. 
1. Bounded mode - The pipeline runs once based on the specified schedule
2. Unbounded mode - The pipeline is always running

### Pipeline Specification file

A pipeline maybe specified in Json format and submitted to the framework server
to auto-generate JAR files and run them. 
The following is the spec outline to be followed in making a configuration file.

#### Spec Outline
``` 
{
    "name": "<unique name for this adaptor",
    "schedulePattern": "<cron like schedule pattern >",
    "adaptorType": "ETL",

    "failureRecoverySpec": {
    },
    
    "inputSpec": {
    },
    
    "parseSpec": {
    },
    
    "deduplicationSpec": {
    },
    
    "transformSpec": {
    },
    
    "publishSpec": {
    }
}
``` 

Detailed explanation of the individual specs are given below.  
- [Meta spec](meta_spec.md)  
- [Failure Recovery Spec](failurerecovery_spec.md)  
- [Input Spec](input_spec.md)  
- [Parse Spec](parse_spec.md)  
- [Deduplication Spec](parse_spec.md)  
- [Transformation Spec](transform_spec.md)  
- [Publish Spec](publish_spec.md)  

The spec can then be submitted to the adaptor server which will validate it and generate a JAR for the entire pipeline.
