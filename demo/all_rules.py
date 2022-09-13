import json
import requests
import sys



dest = sys.argv[1]


if (dest == "server"):
    url = "http://adaptor.iudx.io:8080/rule"
if (dest == "local"):
    url = "http://localhost:8080/rule"


headers = {
  'username': 'testuser',
  'password': 'testuserpassword',
  'Content-Type': 'application/json'
}


response = requests.request("GET", url, headers=headers)
print("Available adaptors are")
print(json.dumps(json.loads(response.text), indent=4))

