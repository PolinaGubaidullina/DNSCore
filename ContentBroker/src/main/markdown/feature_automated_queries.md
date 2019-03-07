# Leistungsmerkmal: Automatisierbare Abfragen (Status & Retrieval)

Für Informationen zu der Verarbeitung von Paketen steht im Normalfall dem Anwender die Webschnittstelle DA-Web zur Verfügung. 

Da es für massenhafte Abfragen (durch Drittsysteme, durch den Anwender zur Überwachung einer eingelieferten Charge) auch möglich sein soll generisch Abfragen an die DNS zu stellen, stehen zwei Wege zur Verfügung: 

a) Es gibt eine technische Webschnittstelle, die Anfragen im JSON Format via HTTP verarbeiten kann. Die Antworten des Systems erfolgen als maschinenlesbarer JSON Code. Dieses Teilfeature eignet sich für Drittsysteme und arbeitet mit einer technischen Anmeldung am System. Direkter Internetzugriff zwischen DNS und dem Drittsystem sind erforderlich.

b) Oftmals gibt es den Wunsch, den Status einer gesamten (vorgefertigten) Liste an Ablieferungsnamen zu untersuchen. Dazu gibt es die Möglichkeit Abfragen mittels einer vorbereiteten Datei durchzuführen. Diese Datei kann als CSV (comma separated values) Datei in die DA-Web Schnittstelle geladen werden. Die Auswertung erfolgt dann durch den ContentBroker und die Datei steht danach im outgoing Ordner der Anwendung zur Verfügung. 

Beide technischen Abfragen arbeiten jeweils im Use Case der reinen Statusabfrage:
Bsp.:

1. Ist mein Objekt fertig archiviert?
2. In welchem Arbeitsschritt ist mein SIP jetzt?
3. Welcher Fehler liegt vor? 

UND auf dem Leistungsmerkmal Retrieval. Es ist also möglich, via dieser Abfragen auch Retrievalanfragen zu erstellen. 
Bsp.:

1. Eine Liste an Einlieferungsnamen liegt vor, alle diese Objekte sollen abgefragt werden. 
2. Eine größere Menge an AIP soll abgefragt werden (massenhaftes Retrieval)

Im folgenden sind die Statusabfrageszenarien als AT-ST-JSON-1 bis AT-ST-JSON-5 gelistet, bzw. AT-R-JSON-1 für das Retrieval. 

Die Statusabfragen mittels CSV Datei haben die Bezeichnung AT-ST-CSV-1 bis AT-ST-CSV-3, bzw. AT-R-CSV-1 für das Retrieval. 

<b>Hinweis zu Enkodierungen bei JSON Abfragen:</b>
Bei der Übergabe von Parametern (z.B. origName urn:nbn: usw.), die Werte enthalten, die gemäß [RFC-2616](http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html) enkodiert werden müssen, sind diese zu enkodieren. 

<b>Hinweis für Admins</b>

- Auf einem bestehendem Build < #1430 ist zunächst folgendes Migration Script gegen die Object-DB (für den DA-Admin) auszuführen:
[updatescript](../../../src/main/sql/migration7.sql) (Abschl. Commit setzen)
- Die HibernateConfig hibernateCentralDB.cfg.xml ist mit einem Mapping für die Table zu versehen:
```
<mapping class="de.uzk.hki.da.model.SystemEvent"/>
```
- Die beans.xml ist zu prüfen, ggfs. zu ändern (sofern Sie einen "Node" betreiben, nicht für "Pres"):
```
<task:scheduled-tasks>
(...)
<task:scheduled ref="systemEventWorker" method="scheduleTask" fixed-delay="20000" />
</task:scheduled-tasks>
```

- Vergleichen Sie Ihre logback.conf mit der logback.conf.template Es gibt mit diesem Feature einen neuen Logger und passen Sie den Loglevel ggfs. an. 

```
	<logger name="de.uzk.hki.da.event" additivity="false" level="DEBUG">
		<appender-ref ref="WORKER" />
	</logger>
```

- Es gibt fortan ein neues Logfile unter logs/events.log , welches ggfs. Fehlermeldungen zu SystemEvents enthält. 

- Für das hier beschriebene Feature AT-CSV-1 bis 3 müssen z.T. Berechtigungen für den Tomcat-User (aber auch für den irods-User), bzw. den ApacheServer
gesetzt, bzw. überprüft werden. 

```
incoming read für irods, read,write für tomcat user
outgoing read, write für irods user, read für tomcat user 
```

## Szenario AT-ST-CSV-1: Statusabfrage mittels vorbereiteter CSV Datei eines fehlerfrei archivierten Pakets

#### Kontext:

* [ATCSVQueries](../../test/java/de/uzk/hki/da/at/ATCSVQueries.java).testCSVStatusReport
 

##### Vorbedingungen:

1. Excel 
2. Login an der DAWEB
3. Eine semikolongetrennte Datei mit den Spalten:


```
identifier;origName;createddate;updateddate;statuscode;erfolg;bemerkung
```
Zeichensatz ist CP1252 (Windows-Standard). Es wird angenommen, dass die Datei mittels EXCEL erstellt wurde. 
[Beispiel, bitte die RAW Filedarstellung sichern](../../../src/test/resources/at/ATCSVQueries.csv) 

Mindestens die Spalte Originalname (origName) muss befüllt sein. 

#### Testpaket(e):

ATUseCaseIngest1.tgz

#### Durchführung:

1. Die Datei ATUseCaseIngest1.tgz wird eingespielt. Es muss die Info über eine positive Archivierung erhalten worden sein.
2. Die [Vorlage wird heruntergeladen, bitte die RAW Filedarstellung sichern und in EXCEL geöffnet](../../../src/test/resources/at/ATCSVQueries.csv)
2. Die EXCEL Testdatei wird mit dem vergebenen Originalnamen des AIP befüllt. 
3. Die Excel wird gespeichert.
4. Der Tester meldet sich an der DAWEB an. 
5. Hochladen der CSV Datei mittels der DA-WEB oder Ablage in den incoming Ordner des Contractors.
6. Start der Berichtserstellung mittels Aktion "Bericht generieren" und Klick auf Start.
7. Entnahme der CSV Datei aus dem outgoing Ordner, bzw. Entnahme über die DA-Web. 

#### Akzeptanzkriterien:

1. Die CSV Datei enthält nun in der Spalte Bemerkung "Objekt archiviert und in keiner Verarbeitung" und in der Spalte "erfolg"  true, ferner ist die Spalte identifier vom System befüllt. 

#### Status und offene Punkte

## Szenario AT-ST-CSV-2: Statusabfrage mittels vorbereiteter CSV Datei eines fehlerhaften Arbeitsschritts

#### Kontext:

* [ATCSVQueries](../../test/java/de/uzk/hki/da/at/ATCSVQueries.java).testCSVReportJobInError

#### Vorbedingungen:

wie vor

#### Testpaket(e):

z.B. ATDetectUncompletedReferencesLido.tgz (Paket führt zu keinem gültigen AIP). D.h. die Verabreitung darf bei diesem SIP NICHT erfolgreich verlaufen. 

#### Durchführung:

1. Notierung des OrigNames aus der Jobansicht 
2. wie vor

#### Akzeptanzkriterien:

Die CSV Datei enthält einen Eintrag in der Spalte erfolg = false und den korrekten Statuscode. 

## Szenario AT-ST-JSON-1: Statusabfrage eines fehlerfrei archivierten Pakets mit Originalname mittels JSON

##### Vorbedingungen:

* Der Nutzer hat einen Account und ist unter der Rolle "Vertragspartner" eingeloggt in der DA-WEB.
* Der User hat mindestens ein beliebiges Paket eingespielt, welches fertig archiviert ist.
* Dem User liegen URN, Identifier und originalName vor.
* Der User hat die Möglichkeit die Links vorher in richtiger Art und Weise zu konstruieren.

#### Durchführung:

1. Das Paket ATUseCaseIngest1.tgz wird eingeliefert
1. *Die URL&nbsp;**[https://|https://**]**<QSERVERNAME>**/daweb3/status/index?origName=<DER ORIGNAME> wird mit dem für* *ATUseCaseIngest1.tgz vergebenen&nbsp;*{*}Originalnamen abgefragt*
1. Es kommt einmalig eine neue Abfrage des Benutzernamens, Passwort des Contractors.

#### Akzeptanzkriterien:

* Im IE muss ggfs. "Datei öffnen mit .. Texteditor" angegeben werden, um die JSON Antwort zu sehen.
* Das fertig archivierte Objekt gibt den Status "archived" an.

```json
https://da-nrw-q.lvr.de/daweb3/status/index?origName=EAD_Testdatensatz_2015-03-16
{"result":[
{"type":"Object","status":"archived","urn":"urn:nbn:de:danrw-2-20150316406969","contractor":"LVR-Test","origName":"EAD_Testdatensatz_2015-03-16","identifier":"2-20150316406969","packages":\["1"\]}
]}
```

## Szenario AT-ST-JSON-2: Statusabfrage eines fehlerfrei archivierten Pakets mit URN mit JSON

#### Vorbedingungen:

wie vor

#### Durchführung:

wie vor, nun aber mit der URL&nbsp;

* [https://|https://**]**<QSERVERNAME>**/daweb3/status/index?urn=<URN> wird mit dem für*&nbsp;*ATUseCaseIngest1.tgz vergebenen&nbsp;*{*}URN abgefragt*

#### *Akzeptanzkriterien:*

wie vor

## Szenario AT-ST-JSON-3: Statusabfrage eines fehlerfrei archivierten Pakets mit Identifier mit JSON

#### Vorbedingungen:

wie vor

#### Durchführung:

wie vor, nun aber mit der URL

* [https://|https://**]**<QSERVERNAME>**/daweb3/status/index?identifier=<IDENTIFIER> wird mit dem für*&nbsp;*ATUseCaseIngest1.tgz vergebenen&nbsp;*{*}Identifier abgefragt*

#### Akzeptanzkriterien:

wie vor

## Szenario AT-ST-JSON-4: Abfrage der URN eines fehlerfrei archivierten Pakets mittels OriginalNamen mit JSON

#### Vorbedingungen:

wie vor

#### Durchführung:

Testszenario ist analog wie AT-ST-JSON-1, hier nur als Szenario "Abfrage URN"

wie vor, nun aber mit der URL: 

* Die URL 
** [https://|https://**]**<QSERVERNAME>**/daweb3/status/index?origName=<DER ORIGNAME> wird mit dem für*&nbsp;*ATUseCaseIngest1.tgz vergebenen&nbsp;*{*}Originalnamen abgefragt*

#### Akzeptanzkriterien:

Die JSON Response enthält den korrekten Wert für URN

## Szenario AT-ST-JSON-5: Statusabfrage eines Pakets im Fehlerstatus mit JSON

#### Kontext:

wie vor

#### Vorbedingungen:

wie vor

#### Durchführung:

1. Eine fehlerhaftes Paket wird eingeliefert (TODO welches)
1. *Die URL&nbsp;**[https://|https://**]**<QSERVERNAME>**/daweb3/status/index?origName=<DER ORIGNAME> wird mit dem für*&nbsp;das fehlerhafte Paket*&nbsp;vergebenen&nbsp;*{*}Originalnamen abgefragt\*

#### Akzeptanzkriterien:

Die Response bezeichnet das Objekt als "transient" und den Queue Entry als "package in progress error", 
das Object als "Object in transient state"


```json
{"result":[{"type":"QueueEntry","urn":null,"contractor":"LVR-Test","origName":"AT-V-001_ATDuplicateDocumentName_2015-03-16","identifier":"2-20150316407197","status":"package in progress error : (114)"},
{"type":"Object","status":"Object is in transient state","urn":null,"contractor":"LVR-Test","origName":"AT-V-001_ATDuplicateDocumentName_2015-03-16","identifier":"2-20150316407197","packages":["1"]}]}
```


## Szenario: AT-R-JSON-1: Retrievalanfragen mittels JSON Request

Automatisierte Drittsysteme haben die Möglichkeit, Retrievalanfragen auch mittels JSON Request zu erstellen. 
Retrieval requests by external systems can be issued by POST requests to an RESTful interface which is available at the URL https://Servername/daweb3/automatedRetrieval/queueForRetrievalJSON

The JSON POST Data must at least contain one of the following fields: URN, IDENTIFIER, ORGINALNAME.

The original name should be the name, the item is listed in your own domain, while the other identifiers (identifier and urn) are build during the ingest process.

Example:

{"urn":"urn:nbn:de:danrw-131614-2013111519609","origName":"testPackage_docx99","identifier":"131614-2013111519609"}

#### Vorbedingungen:

1. Login an der DAWEB
2. Ein fertig archiviertes PAket, welches in keinem Workflow sein darf

#### Durchführung:

1. In der DA-Web auf dem Punkt "Hinweise zur Ansteuerung über externe Systeme" gehen
2. Dort auf den Punkt "Erstellung von Retrievalanfragen" klicken 
3. Eintragen des Identifiers in die Maske. Im Hintergrund wird ein JSON Post ausgelöst. 

#### Akzeptanzkriterien:

1. Das Paket steht zur Entnahme bereit.

## Szenario: AT-R-CSV-1: Retrievalanfrage mittels vorbereiteter CSV-Datei

#### Kontext:

* [ATCSVQueries](../../test/java/de/uzk/hki/da/at/ATCSVQueries.java).testCSVRetrievalRequests
    gilt für alle Szenarien der CSV Operationen.  
    
##### Vorbedingungen:

1. Excel 
2. Login an der DAWEB
3. Eine semikolongetrennte Datei mit den Spalten:

    identifier;origName;createdate;updatedate;statuscode;erfolg;bemerkung

Zeichensatz ist CP1252 (Windows-Standard). Es wird angenommen, dass die Datei mittels EXCEL erstellt wurde. 
[Beispiel](../../../src/test/resources/at/ATCSVQueries.csv)

#### Testpaket(e):

ATUseCaseIngest1.tgz

#### Durchführung:

1. Die Datei ATUseCaseIngest1.tgz wird eingespielt. Es muss die Info über eine positive Archivierung erhalten worden sein.
2. Die [Vorlage wird heruntergeladen und in EXCEL geöffnet](../../../src/test/resources/at/ATCSVQueries.csv)
2. Die EXCEL Testdatei wird mit dem vergebenen Originalnamen des AIP befüllt. 
3. Die Excel wird gespeichert.
4. Der Tester meldet sich an der DAWEB an. 
5. Hochladen der CSV Datei mittels der DA-WEB oder Ablage in den incoming Ordner des Contractors.
6. Start des Massentretrievals mittels "Abfragen verarbeiten".

#### Akzeptanzkriterien:

Die Retrievalanfrage wird korrekt verarbeitet und das DIP steht zur Entnahme bereit.  

#### Status und offene Punkte
