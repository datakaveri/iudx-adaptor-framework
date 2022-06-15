minikube start --driver=docker --addons=ingress --cpus=2 --cni=flannel --install-addons=true --kubernetes-version=stable --memory=6g

minikube tunnel

helm repo add grafana https://grafana.github.io/helm-charts

helm repo add prometheus-community https://prometheus-community.github.io/helm-charts

helm repo update


helm install loki-grafana grafana/grafana --namespace=binder --create-namespace -f config.yaml


kubectl get secret --namespace adaptor-framework loki-grafana -o jsonpath="{.data.admin-password}" | base64 --decode ; echo



kubectl create namespace adaptor-framework

helm install grafana grafana/grafana -n adaptor-framework -f grafana/grafana-values.yaml

helm install loki grafana/loki -n adaptor-framework -f loki/loki-values.yaml

helm install promtail grafana/promtail -n adaptor-framework -f promtail/promtail-values.yaml

helm install prometheus prometheus-community/prometheus -n adaptor-framework -f prometheus/prometheus-values.yaml



Grafana

kubectl get secret --namespace adaptor-framework grafana -o jsonpath="{.data.admin-password}" | base64 --decode ; echo

export POD_NAME=$(kubectl get pods --namespace adaptor-framework -l "app.kubernetes.io/name=grafana,app.kubernetes.io/instance=grafana" -o jsonpath="{.items[0].metadata.name}")

kubectl --namespace adaptor-framework port-forward $POD_NAME 3000


Loki - verifying

kubectl --namespace adaptor-framework port-forward service/loki 3100
curl http://127.0.0.1:3100/api/prom/label


Promtail - verifying

kubectl --namespace adaptor-framework port-forward daemonset/promtail 3101
curl http://127.0.0.1:3101/metrics


Prometheus - 

prometheus-server.mon-stack.svc.cluster.local

export POD_NAME=$(kubectl get pods --namespace mon-stack -l "app=prometheus,component=server" -o jsonpath="{.items[0].metadata.name}")
kubectl --namespace mon-stack port-forward $POD_NAME 9090


helm upgrade promtail grafana/promtail -n mon-stack -f promtail/promtail-values.yaml