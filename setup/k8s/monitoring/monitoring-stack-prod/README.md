## Pre-Requisites
Secrets needed 
```
secrets/
├── admin-password (to set grafana admin Password)
├── admin-user    ( To set grafana admin username)
├── grafana-env-secret (Refer below for env vars to be set) 
```
## Deploy
1. To install the mon-stack
``` 
./install.sh 
```
This installs the whole mon-stack - prometheus, grafana, loki and promtail.

## Note
1.  Appropiately define the environment file  secrets/grafana-env-secret. The template is defined as follow:
 Please do not include comments and substitute appropiate correct values in the placeholders ```<placholder>```.
```
GF_SERVER_ROOT_URL=https://<grafana-domain-name>/
GF_SERVER_DOMAIN=<grafana-domain-name>
TELEGRAM_CHAT_ID=<telegram-chat-id>
TELEGRAM_BOT_TOKEN=<telegram-chat-token>
```

