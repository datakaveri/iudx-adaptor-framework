import json
import requests
import sys

adaptor_id = sys.argv[1]
dest = sys.argv[2]



if (dest == "server"):
    url = f"http://adaptor.iudx.io:8080/adaptor/{adaptor_id}/rules"
if (dest == "local"):
    url = f"http://localhost:8080/adaptor/{adaptor_id}/rules"


headers = {
  'username': 'testuser',
  'password': 'testuserpassword',
  'Content-Type': 'application/json'
}


response = requests.request("GET", url, headers=headers)
print("Available rules are")
print(json.dumps(json.loads(response.text), indent=4))

