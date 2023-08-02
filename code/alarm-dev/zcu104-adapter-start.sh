#!/bin/bash

CONFIG_FILE="$(pwd)"/examples/config.yaml

docker run \
--rm \
-p 127.0.0.1:4343:4343 \
--mount type=bind,source="${CONFIG_FILE}",target=/config/config.yaml \
zcu104-adapter:latest \
bash

