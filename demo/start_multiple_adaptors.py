import json
import requests
import sys
import string

idd = sys.argv[1]
dest = sys.argv[2]
no_jobs = int(sys.argv[3])

if (dest == "server"):
    url = "http://adaptor.iudx.io:8080/adaptor/xx/start"
if (dest == "local"):
    url = "http://localhost:8080/adaptor/xx/start"

headers = {
  'username': 'testuser',
  'password': 'testuserpassword',
  'Content-Type': 'application/json'
}

for i in range(0,no_jobs):
    idd_name = idd+string.ascii_uppercase[i]
    req_url = url.replace("xx", idd_name)
    print(idd_name)
    response = requests.request("POST", req_url, headers=headers)
    print(json.dumps(json.loads(response.text), indent=4))
