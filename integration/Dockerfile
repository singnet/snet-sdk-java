ARG TAG
FROM singularitynet/snet-local-env:${TAG}

ENV ROOT /opt/singnet
ENV BIN ${ROOT}/bin

COPY start_example_service.sh ${BIN}/start_example_service.sh

CMD start_example_service.sh
