cd /mnt/c/Users/ujjawal.maheshwari/Documents/chatbot-platform
wsl

minikube start --driver=docker
//verify 
minikube status
kubectl get nodes


helm version

cd chatbot-platform-chart
ls
ls templates


cat values.yaml
cat values-dev.yaml
cat values-prod.yaml


//helm rendering 
helm template test-release . | grep "name:"


//deploy helm chart 
helm install chatbot-platform .

helm list

kubectl get pods
kubectl get svc
kubectl get deployments


helm upgrade chatbot-platform . -f values-prod.yaml

helm history chatbot-platform



helm rollback chatbot-platform 1

helm history chatbot-platform



helm repo list

helm list

helm status rabbitmq

helm history rabbitmq




helm uninstall chatbot-platform
helm uninstall rabbitmq










helm install rabbitmq bitnami/rabbitmq
helm status rabbitmq
helm upgrade rabbitmq bitnami/rabbitmq
helm rollback rabbitmq 1
helm history rabbitmq