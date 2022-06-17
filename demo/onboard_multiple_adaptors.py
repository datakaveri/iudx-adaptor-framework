import json
import requests
import sys
import string
import time

f_name = sys.argv[1]
dest = sys.argv[2]
no_jobs = int(sys.argv[3])

if(no_jobs>26):
    exit()

if (dest == "server"):
    url = "http://adaptor.iudx.io:8080/adaptor"
if (dest == "local"):
    url = "http://localhost:8080/adaptor"

config = {}

with open(f_name, "r") as f:
    config = json.load(f)

headers = {
  'username': 'testuser',
  'password': 'testuserpassword',
  'Content-Type': 'application/json'
}
job_name = config["name"]
for i in range(0,no_jobs):
    config["name"] = job_name+string.ascii_uppercase[i]
    response = requests.request("POST", url, headers=headers, data=json.dumps(config))

    if (response.status_code == 202):
        print("Submitted config Successfully: "+ config["name"])
    else:
        print("Unsuccessful")
