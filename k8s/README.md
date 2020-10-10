# How to Deploy Translatr on Kubernetes

## TL;DR

Setup everything in separate namespace.

```
./setup.sh
```

Remove separate namespace.

```
./teardown.sh
```

## Set Up
### Create Translatr Namespace

```
kubectl create namespace translatr
```

### Create Config Maps

```
kubectl -n translatr apply -f config.yaml
```

### Create Database and Server Deployments and Services

```
kubectl -n translatr apply -f manifest.yaml
```

### Optional: Create Load Generator Deployment

```
kubectl -n translatr apply -f loadgenerator.yaml
```

## Tear Down
### Cleaning Kubernetes Cluster

```
kubectl delete namespace translatr 
```
