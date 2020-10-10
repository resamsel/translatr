NAMESPACE=${1:-translatr}

kubectl create namespace $NAMESPACE
kubectl -n $NAMESPACE apply -f config.yaml
kubectl -n $NAMESPACE apply -f manifest.yaml
kubectl -n $NAMESPACE apply -f loadgenerator.yaml
