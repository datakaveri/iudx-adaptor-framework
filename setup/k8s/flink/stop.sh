kubectl delete serviceaccount -n adaptor-framework flink-service-account
kubectl delete clusterrolebinding -n adaptor-framework flink-role-binding-flink
kubectl delete deployment flink-cluster -n adaptor-framework
kubectl delete -f flink-metrics-service.yaml
kubectl delete -f taskmanager-metrics-service.yaml
