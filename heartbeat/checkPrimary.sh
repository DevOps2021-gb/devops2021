#!/bin/sh
# Runs on backup droplet. If primary droplet does not return 200 codes, backup obtains the floating ip.
export DO_TOKEN=$DO_TOKEN
IP='144.126.244.138'
ID=$(curl -s http://169.254.169.254/metadata/v1/id)

while true; do
    RESPONSE=$(curl -o /dev/null -s -w "%{http_code}\n" $PRIMARY_URL)
    if [ $(echo $RESPONSE | cut -c1-1) != "2" ]; then
        echo $RESPONSE
        python3 /usr/local/bin/assign-ip $IP $ID
        sleep 5m
    fi
    sleep 5
done
