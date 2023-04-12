kubectl create namespace adaptor-framework
kubectl create serviceaccount flink-service-account -n adaptor-framework
kubectl create clusterrolebinding flink-role-binding-flink --clusterrole=edit --serviceaccount=default:flink-service-account -n adaptor-framework
./bin/kubernetes-session.sh -Dkubernetes.cluster-id=flink-cluster -Dkubernetes.service-account=flink-service-account -Dkubernetes.container.image=datakaveri/flink:1.16.1
kubectl apply -f flink-metrics-service.yaml
kubectl apply -f taskmanager-metrics-service.yaml
