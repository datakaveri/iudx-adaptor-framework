import json
import requests


data = { "outerkey": "outerkeyval",
        "data": [ { "time": "20220602 14:29:10Z", "k1": "701", "deviceId": "abc-123" },
                    { "time": "2022-06-02'T'14:29:10Z", "k1": "701", "deviceId": "abc-123" }]}

parsespec = { "timestampPath": "$.time", "messageContainer": "array",
                "keyPath": "$.deviceId", "containerPath": "$.data",
                "inputTimeFormat":"yyyy-MM-dd'T'HH:mm:ssXXX",
                "outputTimeFormat": "yyyy-MM-dd'T'HH:mm:ssXXX"}

req = {"parseSpec": parsespec, "inputData": json.dumps(data)}

url = "http://localhost:8080/onboard/run-parse-spec"

headers = {
  'username': 'testuser',
  'password': 'testuserpassword',
  'Content-Type': 'application/json'
}

response = requests.request("POST", url, headers=headers, data=json.dumps(req))
print(response.status_code)
print(response.json())
