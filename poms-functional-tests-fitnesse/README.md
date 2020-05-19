Dit is het testautomatiseringsproject van [Specialisterren](https://www.specialisterren.nl/) voor [de testomgeving van NPO POMS](https://poms-test.omroep.nl/). Het project maakt gebruik van [HSAC](https://github.com/fhoeben/hsac-fitnesse-fixtures/) in [FitNesse](http://fitnesse.org/).

## Lokaal draaien

Om toegang te krijgen tot de geautomatiseerde scripts in FitNesse, moet `start.bat` uitgevoerd worden, dat in [/poms-functional-tests-fitnesse](/poms-functional-tests-fitnesse) staat. Dan verschijnt er een opdrachtprompt. Wacht tot er staat: `Starting FitNesse on port: 9090`. 

De FitNesse-omgeving kan dan bekeken worden door te browsen naar: [http://localhost:9090/NpoPoms](http://localhost:9090/NpoPoms).

De testscripts van de acceptatie-omgeving staan in: [http://localhost:9090/NpoPoms.Omgevingen.Test.TestScripts](http://localhost:9090/NpoPoms.Omgevingen.Test.TestScripts).

De testscripts maken gebruik van scenario's in de scenario library. Die staan hier: [http://localhost:9090/NpoPoms.ScenarioLibrary](http://localhost:9090/NpoPoms.ScenarioLibrary).

Om de testscripts lokaal te kunnen draaien, moet een map `fileFixture` aangemaakt worden in [/poms-functional-tests-fitnesse/wiki/FitNesseRoot/files](/poms-functional-tests-fitnesse/wiki/FitNesseRoot/files). Het tekstbestand `accounts.properties`, dat apart geleverd wordt door Specialisterren, moet dan naar deze map worden gekopieerd.

## Draaien in Jenkins

Om de scripts te kunnen draaien in Jenkins, moet de configuratie als volgt worden ingesteld:

### Broncodebeheer (SCM)

![Npo-poms-jenkins-configuratie1](/poms-functional-tests-fitnesse/wiki/FitNesseRoot/files/images/Npo-poms-jenkins-configuratie1.png)

### Bouwstappen

![Npo-poms-jenkins-configuratie2](/poms-functional-tests-fitnesse/wiki/FitNesseRoot/files/images/Npo-poms-jenkins-configuratie2.png)

`Commando` is gebaseerd op de inhoud van het tekstbestand `accounts.properties`. Stel dat dit de inhoud is:
```
email1=email_of_user1
password1=password_of_user1
email2=email_of_user2
password2=password_of_user2
email3=email_of_user3
password4=password_of_user3
```

Dan moet dit bij `Commando` staan:

```
cd poms-functional-tests-fitnesse
mvn clean test-compile

mkdir -p target/fitnesse-results/files/fileFixture
(echo email1=email_of_user1 & echo password1=password_of_user1 & echo email2=email_of_user2 & echo password2=password_of_user2 & echo email3=email_of_user3 & echo password3=password_of_user3) > target/fitnesse-results/files/fileFixture/accounts.properties

mvn failsafe:integration-test -DfitnesseSuiteToRun=NpoPoms.Omgevingen.Test.TestScripts "-DseleniumJsonProfile={'args':['headless','disable-gpu']}"
```

### Acties die na de bouwpoging uitgevoerd worden

![Npo-poms-jenkins-configuratie3](/poms-functional-tests-fitnesse/wiki/FitNesseRoot/files/images/Npo-poms-jenkins-configuratie3.png)
