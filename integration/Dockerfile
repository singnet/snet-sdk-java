ARG TAG
FROM singularitynet/snet-local-env:${TAG}

ENV ROOT /opt/singnet
ENV BIN ${ROOT}/bin

COPY configure_free_calls.sh ${BIN}/configure_free_calls.sh
RUN configure_free_calls.sh

COPY start_example_service.sh ${BIN}/start_example_service.sh
CMD start_example_service.sh
