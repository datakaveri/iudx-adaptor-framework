helm repo add grafana https://grafana.github.io/helm-charts

helm repo add prometheus-community https://prometheus-community.github.io/helm-charts

helm repo update

kubectl create namespace adaptor-framework

helm install grafana grafana/grafana -n adaptor-framework -f grafana/grafana-values.yaml --set "type=LoadBalancer"

helm install loki grafana/loki -n adaptor-framework -f loki/loki-values.yaml

helm install promtail grafana/promtail -n adaptor-framework -f promtail/promtail-values.yaml

helm install prometheus prometheus-community/prometheus -n adaptor-framework -f prometheus/prometheus-values.yaml

kubectl get secret --namespace adaptor-framework grafana -o jsonpath="{.data.admin-password}" | base64 --decode ; echo

export POD_NAME=$(kubectl get pods --namespace adaptor-framework -l "app.kubernetes.io/name=grafana,app.kubernetes.io/instance=grafana" -o jsonpath="{.items[0].metadata.name}")