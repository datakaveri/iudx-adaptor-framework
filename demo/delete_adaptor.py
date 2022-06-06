import json
import requests
import sys


idd = sys.argv[1]
dest = sys.argv[2]


if (dest == "server"):
    url = "http://adaptor.iudx.io:8080/adaptor/xx"
if (dest == "local"):
    url = "http://localhost:8080/adaptor/xx"


url = url.replace("xx", idd)

headers = {
  'username': 'testuser',
  'password': 'testuserpassword',
  'Content-Type': 'application/json'
}


response = requests.request("DELETE", url, headers=headers)
print(json.dumps(json.loads(response.text), indent=4))
