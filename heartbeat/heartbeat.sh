#!/bin/sh
# Runs on primary droplet. If minitwit url returns 200 code locally it obtains the floating IP.
export DO_TOKEN=$DO_TOKEN
IP='144.126.244.138'
ID=$(curl -s http://169.254.169.254/metadata/v1/id)

while true; do
    RESPONSE=$(curl -o /dev/null -s -w "%{http_code}\n" $PRIMARY_URL)
    if [ $RESPONSE -eq 200 ]; then
        echo "REASSIGN FLOATING IP"
        python3 /usr/local/bin/assign-ip $IP $ID
        exit
    fi
    sleep 5
done
