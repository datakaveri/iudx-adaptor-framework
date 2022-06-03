import json
import requests


spec = {"inputSpec": {"type": "http", "url": "http://127.0.0.1:8888/simpleA", "requestType": "GET", "pollingInterval": 1000}}

url = "http://localhost:8080/onboard/run-input-spec"

headers = {
  'username': 'user',
  'password': 'user-password',
  'Content-Type': 'application/json'
}

response = requests.request("POST", url, headers=headers, data=json.dumps(spec))
print(response.status_code)
print(response.json())
