# How to deploy Translatr to Kubernetes

Better use this tutorial: https://levelup.gitconnected.com/deploying-dockerized-golang-api-on-kubernetes-with-postgresql-mysql-d190e27ac09f

## Create PostgreSQL config map

```
kubectl create -f postgres-configmap.yml
```

## Create PostgreSQL Storage

```
kubectl create -f postgres-storage.yml
```

## Create PostgreSQL Deployment

```
kubectl create -f postgres-deployment.yml
```

## Create PostgreSQL Service

```
kubectl create -f postgres-service.yml
```

## Cleaning PostgreSQL from Kubernetes Cluster

```
kubectl delete service postgres 
kubectl delete deployment postgres
kubectl delete configmap postgres-config
kubectl delete persistentvolumeclaim postgres-pv-claim
kubectl delete persistentvolume postgres-pv-volume
```
