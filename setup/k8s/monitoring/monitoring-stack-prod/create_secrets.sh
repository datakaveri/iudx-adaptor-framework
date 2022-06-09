#!/bin/bash

set -e 
echo -n  $(cat /dev/urandom |  tr -dc 'a-zA-Z0-9-!@#$%^&*()+{}|:<>?=' | head -c 30) > secrets/admin-user
echo -n  $(cat /dev/urandom |  tr -dc 'a-zA-Z0-9-!@#$%^&*()+{}|:<>?=' | head -c 30) > secrets/admin-password

