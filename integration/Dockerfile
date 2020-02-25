ARG TAG
FROM singularitynet/snet-local-env:${TAG}

ENV ROOT /opt/singnet
ENV BIN ${ROOT}/bin

COPY start_example_service.sh ${BIN}/start_example_service.sh

# temporary update to daemon 3.0.0
WORKDIR /tmp
RUN wget -O daemon.tar.gz https://github.com/singnet/snet-daemon/releases/download/v3.0.0/snet-daemon-v3.0.0-linux-amd64.tar.gz
RUN tar xzf daemon.tar.gz
RUN cp -f ./snet-daemon-v3.0.0-linux-amd64/snetd /usr/bin/snetd
WORKDIR ${ROOT}

CMD start_example_service.sh
