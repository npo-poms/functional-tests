This testing project uses the FitNesse baseline provided by [hsac-fitnesse-fixtures](https://github.com/fhoeben/hsac-fitnesse-fixtures).
It offers a web environment (wiki) to define and run tests.

## Running Locally
To start the wiki locally execute: `mvn clean compile exec:exec` or `start.bat`, it can then be accessed at [http://localhost:9090/](http://localhost:9090/).

More details on the features of hsac-fitnesse-fixtures, and example tests, can then be accessed at 
[http://localhost:9090/HsacExamples](http://localhost:9090/HsacExamples).

## Run from maven

### headless
`mvn test-compile failsafe:integration-test -DseleniumBrowser=chrome "-DseleniumJsonProfile={'args':['headless', 'disable-gpu', 'window-size=1366x768']}" -DfitnesseSuiteToRun=ExampleSuite.Omgevingen.Test.TestScripts.DesktopDevice.ExampleTest`

### Selenium Grid Chrome
`mvn test-compile failsafe:integration-test -DseleniumBrowser=chrome -DseleniumGridUrl=http://<ip>:4444/wb/hub -DfitnesseSuiteToRun=ExampleSuite.Omgevingen.Test.TestScripts.DesktopDevice.ExampleTest`

### Selenium Grid Firefox
`mvn test-compile failsafe:integration-test -DseleniumBrowser=firefox -DseleniumGridUrl=http://<ip>:4444/wb/hub "-DseleniumJsonProfile={'args':['headless', 'disable-gpu', 'window-size=1366x768']}" -DfitnesseSuiteToRun=ExampleSuite.Omgevingen.Test.TestScripts.DesktopDevice.ExampleTest`


## creating allure report

`mvn site -Pallure`


## Upgrading

To upgrade to newer version of the hsac-fitnesse-fixtures project:

* stop FitNesse
* stop all selenium webdrivers that might be running
* upgrade `hsac.fixtures.version` property in `pom.xml`
* run `mvn clean -Pdelete-hsac-fitnesse-fixtures`
* start as normal

Upgrade FitNesse version used:

* stop FitNesse
* upgrade `fitnesse.version` property in `pom.xml`
* start as normal
