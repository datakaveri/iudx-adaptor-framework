# Data flow scenarios

This describes all the flow scenarios which might occur with the downstream app servers.



## Simple

1. simpleA
   - Single api
   - *key* in downstream -forms--> id in iudx
   - Simple uname/passwd based auth
2. simpleB 
   - Just like simpleA
   - Single API single resource containing multiple data
   - Need to decimate data based on key in json-array response but won't be part of id creation
3. simpleC 
   - Api with time duration as params
   - Need to fix time duration intervals
   - Need to de-duplicate based on these intervals
4. simpleD - Non json-response
   - Excel
   - XML



## Advance flows

1. advanceA - 2 api flow
   - 2 apis, api_a, api_b
   - api_a -> Get list of <entities>
   - for each <entities>: api_b -> <main>
2. advanceB - Auth flow 
   - 2 apis, auth_a, api_a, api_b
   - auth_a -> with uname passwd get token
   - api_a -> get list of devices
   - api_b -> get data for each device


## Conversions
- JsonObject to JsonObject
- JsonArray to JsonObject (Decimate array)
- XML to JsonObject
- Excel to JsonObject
