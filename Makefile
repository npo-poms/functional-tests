
.PHONY: run exec

ROOT_DIR:=$(shell dirname $(realpath $(firstword $(MAKEFILE_LIST))))
NAME=fitnesse-tests-runner
DIR=/fitnessetests
DOCKER_ARGS=--mount src="${ROOT_DIR}",target=${DIR},type=bind ${NAME}
DOCKER_ARGS_RUN=--mount src="$$HOME/conf",target=/root/conf,type=bind ${DOCKER_ARGS}
ARTIFACT=poms-functional-tests-fitnesse-1.0.11c
ZIP=target/${ARTIFACT}-standalone.zip
JAR=target/${ARTIFACT}.jar


run-%:  ${JAR} ${NAME}
	docker run --rm -e MOZ_HEADLESS=1 ${DOCKER_ARGS_RUN}  mvn failsafe:integration-test -DfitnesseSuiteToRun=NpoPoms.Omgevingen.$*.TestScripts.Gui  -DseleniumJsonProfile={'args':['headless','disable-gpu']}

run: run-Acceptatie

exec: ${NAME}
	docker run -p 9090:9090 -e MOZ_HEADLESS=1 ${DOCKER_ARGS_RUN} mvn exec:exec

${NAME}: Dockerfile settings.xml
	docker build -t ${NAME} .
	touch $@

${ZIP}: ${NAME}
	docker run ${DOCKER_ARGS} mvn package

${JAR}: ${NAME}
	docker run ${DOCKER_ARGS} mvn jar:jar


it: ${NAME}
	docker run -it  ${DOCKER_ARGS} bash


clean:
	rm -rf target/*
	rm -f ${NAME}
