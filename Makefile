
.PHONY: run exec
.DEFAULT_GOAL: exec

ROOT_DIR:=$(shell dirname $(realpath $(firstword $(MAKEFILE_LIST))))

NAME=fitnesse-tests-runner
DOCKER_ARGS=--mount src="$$HOME/conf",target=/root/conf,type=bind --mount src="${ROOT_DIR}",target=/functionaltests,type=bind ${NAME}


exec: ${NAME}
	docker run -p 9090:9090 ${DOCKER_ARGS} mvn exec:exec

${NAME}: Dockerfile
	docker build -t ${NAME} .
	touch $@


target/poms-functional-tests-fitnesse-1.0.11c-standalone.zip:
	docker run --mount src="${ROOT_DIR}",target=/functionaltests,type=bind functional-tests-runner mvn package

run:  target/poms-functional-tests-fitnesse-1.0.11c.jar ${NAME}
	docker run --rm -e MOZ_HEADLESS=1 ${DOCKER_ARGS}  mvn failsafe:integration-test -DfitnesseSuiteToRun=NpoPoms.Omgevingen.Test.TestScripts.Gui -DseleniumJsonProfile={'args':['headless','disable-gpu']}

clean:
	rm -rf target/*
	rm ${NAME}
