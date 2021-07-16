##Collection of some usefull tasks to run tests (in docker or directly)
##default runs on Chrome
##But for examle: 'make TARGET=Firefox run' would run gui test on firefox in docker

.PHONY: run exec

ROOT_DIR:=$(shell dirname $(realpath $(firstword $(MAKEFILE_LIST))))
NAME=fitnesse-tests-runner
DIR=/fitnessetests
DOCKER_ARGS=--mount src="${ROOT_DIR}",target=${DIR},type=bind ${NAME}
DOCKER_ARGS_RUN=--mount src="$$HOME/conf",target=/root/conf,type=bind ${DOCKER_ARGS}
ARTIFACT=poms-functional-tests-fitnesse-1.0.11c
ZIP=target/${ARTIFACT}-standalone.zip
JAR=target/${ARTIFACT}.jar

#TARGET=TestScripts
TARGET=Chrome


help:     ## Show this help.
	@sed -n 's/^##//p' $(MAKEFILE_LIST)
	@grep -E '^[%a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'


run-%: 	${JAR} ${NAME}  ## run tests on given environemnt (requires docker)
	docker run --rm -e MOZ_HEADLESS=1 ${DOCKER_ARGS_RUN}  mvn failsafe:integration-test -DfitnesseSuiteToRun=NpoPoms.Omgevingen.$*.${TARGET}.Gui  -DseleniumJsonProfile={'args':['headless','disable-gpu']}

run: run-Acceptatie    ## run it on acceptatie environment (requires docker)

exec: ${NAME}          ## run wiki in docker
	docker run -p 9090:9090 -e MOZ_HEADLESS=1 ${DOCKER_ARGS_RUN} mvn exec:exec   -DseleniumJsonProfile={'args':['headless','disable-gpu']}

local-wiki:           ## start up wiki locally (requires maven, java 11)
	mvn compile dependency:copy-dependencies exec:exec


${NAME}: Dockerfile settings.xml
	docker build -t ${NAME} .
	touch $@

${ZIP}: ${NAME}
	docker run ${DOCKER_ARGS} mvn package

${JAR}: ${NAME}
	docker run ${DOCKER_ARGS} mvn jar:jar


it: ${NAME}            ## runs bash in docker image interactively (for debugging)
	docker run -it  ${DOCKER_ARGS} bash


clean:                ## clean artifacts
	rm -rf target/*
	rm -f ${NAME}
