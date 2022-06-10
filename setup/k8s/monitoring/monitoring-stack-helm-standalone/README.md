minikube start --driver=docker --addons=ingress --cpus=2 --cni=flannel --install-addons=true --kubernetes-version=stable --memory=6g

minikube tunnel

helm repo add grafana https://grafana.github.io/helm-charts

helm repo update


helm install loki-grafana grafana/grafana --namespace=binder --create-namespace -f config.yaml


kubectl get secret --namespace monitoring loki-grafana -o jsonpath="{.data.admin-password}" | base64 --decode ; echo



kubectl create namespace monitoring

helm install grafana grafana/grafana -n monitoring -f grafana/grafana-values.yaml

helm install loki grafana/loki -n monitoring -f loki/loki-values.yaml

helm install promtail grafana/promtail -n monitoring -f promtail/promtail-values.yaml


Grafana

kubectl get secret --namespace monitoring grafana -o jsonpath="{.data.admin-password}" | base64 --decode ; echo

export POD_NAME=$(kubectl get pods --namespace monitoring -l "app.kubernetes.io/name=grafana,app.kubernetes.io/instance=grafana" -o jsonpath="{.items[0].metadata.name}")

kubectl --namespace monitoring port-forward $POD_NAME 3000


Loki - verifying

kubectl --namespace monitoring port-forward service/loki 3100
curl http://127.0.0.1:3100/api/prom/label


Promtail - verifying

kubectl --namespace monitoring port-forward daemonset/promtail 3101
curl http://127.0.0.1:3101/metrics


helm upgrade promtail grafana/promtail -n monitoring -f promtail/promtail-values.yaml