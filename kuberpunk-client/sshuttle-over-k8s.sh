#!/bin/sh

HOST_PORT=30022
DRYRUN=
if [ "$1" = "-n" ] ; then
  DRYRUN=true
  shift
fi

TARGET=$1
shift

JUMP_HOST=$1
shift

SSH=/usr/bin/ssh

if [ -n "$JUMP_HOST" ] ; then
  SSH="$SSH -J $JUMP_HOST"
fi

if [ -n "$(echo $TARGET | tr -d '0-9.')" ] ; then
  TARGET=$(dig +short $TARGET)
fi

kubectl apply -f - <<EOF
apiVersion: v1
kind: Pod
metadata:
 name: python-sshd
 labels:
  run: ssh
spec:
 containers:
 - name: python-sshd
   image: eugenes1/python-sshd:latest
   ports:
    - containerPort: 22
 restartPolicy: Never
---
apiVersion: v1
kind: Service
metadata:
 name: python-sshd
 labels:
  run: ssh
spec:
 type: NodePort
 externalTrafficPolicy: Cluster
 ports:
 - port: 2022
   targetPort: 22
   nodePort: $HOST_PORT
   protocol: TCP
 selector:
  run: ssh
EOF

printf "Waiting for pod to be Ready.. "

kubectl wait --for=condition=Ready pod python-sshd

HOST_IP=$(kubectl get pod python-sshd -o jsonpath="{.status.hostIP}")

SSH_TARGET=$HOST_IP:$HOST_PORT
if [ -n "$DRYRUN" ] ; then
  printf "Run the following command to open tunnel via $SSH_TARGET\n"
  printf "sshuttle -e \"$SSH\" -r root@$HOST_IP:$HOST_PORT --python /root/venv/bin/python $TARGET/32"
else
  printf "Opening tunnel via $SSH_TARGET\n"
  exec sshuttle -e "$SSH" -r root@$HOST_IP:$HOST_PORT --python /root/venv/bin/python $TARGET/32
fi