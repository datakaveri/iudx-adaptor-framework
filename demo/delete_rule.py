import json
import requests
import sys


adaptor_id = sys.argv[1]
rule_id = sys.argv[2]
dest = sys.argv[3]


if (dest == "server"):
    url = f"http://adaptor.iudx.io:8080/adaptor/{adaptor_id}/rules/{rule_id}"
if (dest == "local"):
    url = f"http://localhost:8080/adaptor/{adaptor_id}/rules/{rule_id}"


headers = {
  'username': 'testuser',
  'password': 'testuserpassword',
  'Content-Type': 'application/json'
}


response = requests.request("DELETE", url, headers=headers)
print(json.dumps(json.loads(response.text), indent=4))
