import json
import requests
import sys

f_name = sys.argv[1]
dest = sys.argv[2]


if (dest == "server"):
    url = "http://adaptor.iudx.io:8080/rule"
if (dest == "local"):
    url = "http://localhost:8080/rule"

config = {}

with open(f_name, "r") as f:
    config = json.load(f)


headers = {
  'username': 'testuser',
  'password': 'testuserpassword',
  'Content-Type': 'application/json'
}

response = requests.request("POST", url, headers=headers, data=json.dumps(config))

if (response.status_code == 202):
    print("Submitted config Successfully")
else:
    print("Unsuccessful")
