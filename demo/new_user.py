import json
import requests
import sys

dest = sys.argv[1]


if (dest == "server"):
    url = "http://adaptor.iudx.io:8080/user"
if (dest == "local"):
    url = "http://localhost:8080/user"

config = { "username":"testuser", "password":"testuserpassword" }


headers = {
  'username': 'admin',
  'password': 'admin',
  'Content-Type': 'application/json'
}

response = requests.request("POST", url, headers=headers, data=json.dumps(config))


print(response.text)
