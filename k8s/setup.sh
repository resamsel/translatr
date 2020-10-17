#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

NAMESPACE=${1:-translatr}
HELM_REPO_URL="https://charts.bitnami.com/bitnami"

minikube addons enable ingress

kubectl get namespace $NAMESPACE > /dev/null 2>&1 || kubectl create namespace $NAMESPACE

#
# Installing postgresql
#
[ "$(helm repo ls | grep "$HELM_REPO_URL" | wc -l)" -eq "0" ] && helm repo add bitnami $HELM_REPO_URL
helm install -n translatr postgres bitnami/postgresql \
  --values="$DIR/postgresql-values.yaml"

#
# Installing translatr
#
kubectl -n $NAMESPACE apply -f "$DIR/manifest.yaml"
kubectl -n $NAMESPACE apply -f "$DIR/loadgenerator.yaml"
