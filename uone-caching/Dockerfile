FROM public.ecr.aws/bitnami/python:3.8 as tester

ENV SRC_DIR=/usr/src/uone_caching
ENV APP_DIR=${SRC_DIR}/uone_caching

WORKDIR ${SRC_DIR}

COPY src/requirements.txt src/setup.py src/setup.cfg .bumpversion.cfg ${SRC_DIR}/

RUN pip install -r requirements.txt \
    && pip install -e . \
    && pip install pytest pytest-cov pylint botocore

COPY src/uone_caching ${APP_DIR}
COPY src/tests/* ${SRC_DIR}/tests/

CMD ["pytest"]

################################################################################
# build image
################################################################################
FROM public.ecr.aws/bitnami/python:3.8 as package

ENV BUILD_DIR=/tmp/build/uone_caching

RUN mkdir -p ${BUILD_DIR}
WORKDIR ${BUILD_DIR}

COPY --from=tester /usr/src/uone_caching/uone_caching ${BUILD_DIR}/uone_caching
COPY --from=tester /usr/src/uone_caching/setup.py /usr/src/uone_caching/setup.cfg ${BUILD_DIR}/
COPY --from=tester /usr/src/uone_caching/.bumpversion.cfg /usr/src/uone_caching/.bumpversion.cfg ${BUILD_DIR}/

RUN python -m pip install --upgrade build \
    && python -m build

CMD ["ls", "/tmp/build/uone_caching/dist"]

################################################################################
# non-zip image
################################################################################
FROM public.ecr.aws/bitnami/python:3.8 as layer

ENV FROM_DIR=/tmp/build/uone_caching/dist
ENV INPUT_DIR=/input_items
ENV BUILD_DIR=/python
ENV OUTPUT_DIR=/asset

RUN mkdir -p ${OUTPUT_DIR}

COPY --from=package ${FROM_DIR} ${INPUT_DIR}/

WORKDIR ${INPUT_DIR}

RUN pip install $(find ${INPUT_DIR} -type f -name '*.tar.gz' | head -n1) -t ${BUILD_DIR} \
    && mv ${BUILD_DIR} ${OUTPUT_DIR}${BUILD_DIR}
