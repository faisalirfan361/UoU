################################################################################
# Staging image
################################################################################
FROM public.ecr.aws/bitnami/python:3.8 as staging


ENV SRC_DIR=/usr/src
ENV OUTPUT_DIR=/asset

COPY src/uone_data_scraping ${SRC_DIR}/uone_data_scraping/
COPY src/requirements.txt  src/setup.* ${SRC_DIR}/

WORKDIR ${SRC_DIR}
COPY local/pip_install /bin/pip_install
RUN chmod 777 /bin/pip_install \
    && pip_install -r requirements.txt -t .

CMD ["ls", "/asset"]


################################################################################
# Testing image
################################################################################
ARG CODEARTIFACT_AUTH_TOKEN
ARG USERPROFILE=default
FROM public.ecr.aws/bitnami/python:3.8 as tester


ARG CODEARTIFACT_AUTH_TOKEN
ARG USERPROFILE=default

ENV CODEARTIFACT_AUTH_TOKEN=$CODEARTIFACT_AUTH_TOKEN
ENV SRC_DIR=/usr/src
ENV TMPUSERPROFILE=$USERPROFILE

WORKDIR ${SRC_DIR}

COPY --from=staging /bin/pip_install /bin/pip_install
COPY --from=staging ${SRC_DIR} ${SRC_DIR}/
COPY local/local.requirements.txt ${SRC_DIR}
COPY src/tests/* ${SRC_DIR}/tests/

RUN pip_install -r requirements.txt \
    && pip install pytest pytest-cov pylint \
    && pip install -e .

CMD ["pytest"]

################################################################################
# build image
################################################################################
FROM public.ecr.aws/bitnami/python:3.8 as package


ENV SRC_DIR=/usr/src
ENV OUTPUT_DIR=/package

WORKDIR ${SRC_DIR}

COPY --from=staging /bin/pip_install /bin/pip_install
COPY --from=staging ${SRC_DIR}/ ${SRC_DIR}

RUN mkdir -p ${OUTPUT_DIR} \
    && python -m pip install --upgrade build \
    && python -m build \
    && mv $(find . -type f -name '*.tar.gz' | head -n1) ${OUTPUT_DIR}/

CMD ["ls", "-la", "/package"]

################################################################################
# layer image
################################################################################
FROM public.ecr.aws/bitnami/python:3.8 as layer

ENV FROM_DIR=/package
ENV INPUT_DIR=/input_items
ENV BUILD_DIR=/python
ENV OUTPUT_DIR=/asset

RUN mkdir -p ${OUTPUT_DIR}

COPY --from=package ${FROM_DIR} ${INPUT_DIR}/

WORKDIR ${INPUT_DIR}

RUN pip install $(find ${INPUT_DIR} -type f -name '*.tar.gz' | head -n1) -t ${BUILD_DIR} \
    && mv ${BUILD_DIR} ${OUTPUT_DIR}/${BUILD_DIR}
