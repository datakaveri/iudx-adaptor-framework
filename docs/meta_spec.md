### Meta spec
Meta properties of the entire job. The metaspec are root level key values
and aren't contained in a json object.



The schema of the metaSpec is as shown below. **Bold** implies that the property is **required**.  

- **name**(String): Unique name (per user) of the adaptor. No spaces.
- 
- schedulerPattern(String): Cron like pattern for long-running jobs. More information on constructing the pattern can be found from [here](http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html)
- adaptorType (ENUM) (ETL, RULES): Defines the type of adaptor defaults to ETL. in case if its a rule job the value will be RULES
- enableCheckpointing (Boolean): Checkpointing can be disabled using this field. By default, it is enabled

Example:
``` 
{
    "name": "myAdaptor",
    "schedulePattern": "0 * * * * ?"
    "adaptorType": "ETL",
    "enableCheckpointing": true,
    
    "inputSpec": { },
    "parseSpec": { }
}
```

