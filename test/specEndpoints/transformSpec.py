import json
import requests

data = {
			"deviceId": "abc-123",
			"k1": "a",
			"time": "2021-04-01T12:00:01+05:30"
		}

transformSpec = {
        "type": "jsPath",
		"template": "{ 'co2': { 'avgOverTime': 100}, 'id': 'abc'}",
		"jsonPathSpec": [
            {
                "outputKeyPath": "$.observationDateTime",
                "inputValuePath": "$.time"
            },
			{
				"outputKeyPath": "$.co2.avgOverTime",
				"inputValuePath": "$.k1"
			},
			{
				"outputKeyPath": "$.id",
				"inputValuePath": "$.deviceId",
				"valueModifierScript": "value.split('-')[0]"
			}
		]    
    }

req = {"transformSpec": transformSpec, "inputData": json.dumps(data)}

url = "http://localhost:8080/onboard/run-transform-spec"

headers = {
  'username': 'user',
  'password': 'user-password',
  'Content-Type': 'application/json'
}

response = requests.request("POST", url, headers=headers, data=json.dumps(req))
print(response.status_code)
print(response.json())
