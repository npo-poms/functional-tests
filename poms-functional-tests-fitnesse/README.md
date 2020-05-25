Dit is het testautomatiseringsproject van [Specialisterren](https://www.specialisterren.nl/) voor [de testomgeving van NPO POMS](https://poms-test.omroep.nl/). Het project maakt gebruik van [HSAC](https://github.com/fhoeben/hsac-fitnesse-fixtures/) in [FitNesse](http://fitnesse.org/).

## poms-fitnesse-accounts.properties

Om de testscripts lokaal of in Jenkins te kunnen draaien, moet er een tekstbestand `poms-fitnesse-accounts.properties` staan in `~/conf` (Linux/macOS) of `%userprofile%\conf` (Windows). Dit bestand wordt apart geleverd door Specialisterren. Als de map `conf` nog niet bestaat, moet deze eerst aangemaakt worden.

## Lokaal draaien

Om toegang te krijgen tot de geautomatiseerde scripts in FitNesse, moet `start.bat` (of `start.sh`) uitgevoerd worden, dat in [/poms-functional-tests-fitnesse](/poms-functional-tests-fitnesse) staat. Dan verschijnt er een opdrachtprompt. Wacht tot er staat: `Starting FitNesse on port: 9090`. 

De FitNesse-omgeving kan dan bekeken worden door te browsen naar: [http://localhost:9090/NpoPoms](http://localhost:9090/NpoPoms).

De testscripts van de testomgeving staan in: [http://localhost:9090/NpoPoms.Omgevingen.Test.TestScripts](http://localhost:9090/NpoPoms.Omgevingen.Test.TestScripts).

De testscripts maken gebruik van scenario's in de scenario library. Die staan hier: [http://localhost:9090/NpoPoms.ScenarioLibrary](http://localhost:9090/NpoPoms.ScenarioLibrary).

## Draaien in Jenkins

Om de scripts te kunnen draaien in Jenkins, moet de configuratie als volgt worden ingesteld:

### Broncodebeheer (SCM)

![Npo-poms-jenkins-configuratie1](/poms-functional-tests-fitnesse/wiki/FitNesseRoot/files/images/Npo-poms-jenkins-configuratie1.png)

### Bouwstappen

![Npo-poms-jenkins-configuratie2](/poms-functional-tests-fitnesse/wiki/FitNesseRoot/files/images/Npo-poms-jenkins-configuratie2.png)

Dit moet er bij `Commando` staan:

```
cd poms-functional-tests-fitnesse
MOZ_HEADLESS=1
mvn clean test-compile failsafe:integration-test -DfitnesseSuiteToRun=NpoPoms.Omgevingen.Test.TestScripts
```

### Acties die na de bouwpoging uitgevoerd worden

![Npo-poms-jenkins-configuratie3](/poms-functional-tests-fitnesse/wiki/FitNesseRoot/files/images/Npo-poms-jenkins-configuratie3.png)
