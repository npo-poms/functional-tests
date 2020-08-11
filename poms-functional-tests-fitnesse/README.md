Dit is het testautomatiseringsproject van [Specialisterren](https://www.specialisterren.nl/) voor [de testomgeving van NPO POMS](https://poms-test.omroep.nl/). Het project maakt gebruik van [HSAC](https://github.com/fhoeben/hsac-fitnesse-fixtures/) in [FitNesse](http://fitnesse.org/).

## Properties files

Om de testscripts lokaal of in Jenkins te kunnen draaien, moeten deze properties files aanwezig zijn:

  * `poms-fitnesse-accounts.properties`
  * `poms-fitnesse-apikeys.properties`
  
Beide properties files moeten in één van deze mappen zitten:

  * `~/conf` (Linux/macOS) of `%userprofile%\conf` (Windows)
  * [wiki/FitNesseRoot/files/fileFixture](wiki/FitNesseRoot/files/fileFixture)

De properties files worden apart geleverd door Specialisterren.

## Lokaal draaien

Om toegang te krijgen tot de geautomatiseerde scripts in FitNesse, moet `start.bat` (of `start.sh`) uitgevoerd worden, dat in [/poms-functional-tests-fitnesse](/poms-functional-tests-fitnesse) staat. Dan verschijnt er een opdrachtprompt. Wacht tot er staat: `Starting FitNesse on port: 9090`. 

De FitNesse-omgeving kan dan bekeken worden door te browsen naar: [http://localhost:9090/NpoPoms](http://localhost:9090/NpoPoms).

Vanuit deze pagina kunnen de testscripts worden bekeken na het selecteren van de omgeving (`test` en `dev`), het testdoel (`gui`, `api` en `e2e`) en indien van toepassing de browser (`Firefox` en `Chrome`): [http://localhost:9090/NpoPoms.Omgevingen](http://localhost:9090/NpoPoms.Omgevingen).

De testscripts maken gebruik van scenario's in de scenario library. Die staan hier: [http://localhost:9090/NpoPoms.ScenarioLibrary](http://localhost:9090/NpoPoms.ScenarioLibrary).

## Draaien in Jenkins

In Jenkins moeten er aparte jobs zijn voor elke mogelijke combinatie van de omgeving, het testdoel en indien van toepassing de browser.

### Gui

De configuratie van de testomgeving van `gui` met `Firefox` moet als volgt worden ingesteld:

![Npo-poms-api-jenkins-configuration](wiki/FitNesseRoot/files/images/Npo-poms-gui-jenkins-configuration.png)

Als de properties files in `~/conf` (Linux/macOS) of `%userprofile%\conf` (Windows) zitten, dan moet er dit bij `Commando` staan:
```
cd poms-functional-tests-fitnesse
MOZ_HEADLESS=1
mvn clean test-compile failsafe:integration-test -DfitnesseSuiteToRun=NpoPoms.Omgevingen.Test.Firefox.Gui
```

Als dit niet zo is, dan moet de properties file `poms-fitnesse-accounts.properties` gegenereerd worden via `Commando`. Deze file heeft een dergelijke inhoud:
```
standaardGebruikersnaam=gebruikersnaam
standaardWachtwoord=wachtwoord
npoGebruikersnaam=gebruikersnaam
npoWachtwoord=wachtwoord
adminGebruikersnaam=gebruikersnaam
adminWachtwoord=wachtwoord
omroepUploaderGebruikersnaam=gebruikersnaam
omroepUploaderWachtwoord=wachtwoord
```

Dit moet er bij `Commando` staan:
```
cd poms-functional-tests-fitnesse
mvn clean test-compile
mkdir -p target/fitnesse-results/files/fileFixture

(echo standaardGebruikersnaam='gebruikersnaam' & echo standaardWachtwoord='wachtwoord' & echo npoGebruikersnaam='gebruikersnaam' & echo npoWachtwoord='wachtwoord' & echo adminGebruikersnaam='gebruikersnaam' & echo adminWachtwoord='wachtwoord' & echo omroepUploaderGebruikersnaam='gebruikersnaam' & echo omroepUploaderWachtwoord='wachtwoord') > target/fitnesse-results/files/fileFixture/poms-fitnesse-accounts.properties

mvn failsafe:integration-test -DfitnesseSuiteToRun=NpoPoms.Omgevingen.Test.Firefox.Gui "-DseleniumJsonProfile={'args':['headless','disable-gpu']}"
```

waarbij de waarden achter `=` vervangen moeten worden door de werkelijke waarden.

### Api

De configuratie van de testomgeving van `api` moet ingesteld worden zoals die van `gui` met uitzondering van het veld `Commando`.

Als de properties files in `~/conf` (Linux/macOS) of `%userprofile%\conf` (Windows) zitten, dan moet er dit bij `Commando` staan:
```
cd poms-functional-tests-fitnesse
MOZ_HEADLESS=1
mvn clean test-compile failsafe:integration-test -DfitnesseSuiteToRun=NpoPoms.Omgevingen.Test.Api
```

Als dit niet zo is, dan moet de properties file `poms-fitnesse-apikeys.properties` gegenereerd worden via `Commando`. Deze file heeft een dergelijke inhoud:
```
frontEndApiKey=apiKey
frontEndApiSecret=secret
frontEndApiOrigin=https://poms.testomgeving.example.com/
backEndApiKey=apiKey
backEndApiSecret=secret
backEndApiOrigin=https://poms.testomgeving.example.com/
```

Dit moet er bij `Commando` staan:
```
cd poms-functional-tests-fitnesse
mvn clean test-compile
mkdir -p target/fitnesse-results/files/fileFixture

(echo frontEndApiKey='apiKey' & echo frontEndApiSecret='secret' & echo frontEndApiOrigin='https://poms.testomgeving.example.com/' & echo backEndApiKey='apiKey' & echo backEndApiSecret='secret' & echo backEndApiOrigin='https://poms.testomgeving.example.com/') > target/fitnesse-results/files/fileFixture/poms-fitnesse-apikeys.properties

mvn failsafe:integration-test -DfitnesseSuiteToRun=NpoPoms.Omgevingen.Test.Api "-DseleniumJsonProfile={'args':['headless','disable-gpu']}"
```

waarbij de waarden achter `=` vervangen moeten worden door de werkelijke waarden.

### E2E

De configuratie van de testomgeving van `e2e` met `Firefox` moet ingesteld worden zoals die van `gui` met uitzondering van het veld `Commando`.

Als de properties files in `~/conf` (Linux/macOS) of `%userprofile%\conf` (Windows) zitten, dan moet er dit bij `Commando` staan:
```
cd poms-functional-tests-fitnesse
MOZ_HEADLESS=1
mvn clean test-compile failsafe:integration-test -DfitnesseSuiteToRun=NpoPoms.Omgevingen.Test.Firefox.E2E
```

Als dit niet zo is, dan moeten de properties files `poms-fitnesse-accounts.properties` en `poms-fitnesse-apikeys.properties` gegenereerd worden via `Commando`. Deze files hebben een inhoud zoals eerder genoemd.

Dit moet er bij `Commando` staan:
```
cd poms-functional-tests-fitnesse
mvn clean test-compile
mkdir -p target/fitnesse-results/files/fileFixture

(echo standaardGebruikersnaam='gebruikersnaam' & echo standaardWachtwoord='wachtwoord' & echo npoGebruikersnaam='gebruikersnaam' & echo npoWachtwoord='wachtwoord' & echo adminGebruikersnaam='gebruikersnaam' & echo adminWachtwoord='wachtwoord' & echo omroepUploaderGebruikersnaam='gebruikersnaam' & echo omroepUploaderWachtwoord='wachtwoord') > target/fitnesse-results/files/fileFixture/poms-fitnesse-accounts.properties
(echo frontEndApiKey='apiKey' & echo frontEndApiSecret='secret' & echo frontEndApiOrigin='https://poms.testomgeving.example.com/' & echo backEndApiKey='apiKey' & echo backEndApiSecret='secret' & echo backEndApiOrigin='https://poms.testomgeving.example.com/') > target/fitnesse-results/files/fileFixture/poms-fitnesse-apikeys.properties

mvn failsafe:integration-test -DfitnesseSuiteToRun=NpoPoms.Omgevingen.Test.Firefox.E2E "-DseleniumJsonProfile={'args':['headless','disable-gpu']}"
```

waarbij de waarden achter `=` vervangen moeten worden door de werkelijke waarden.
