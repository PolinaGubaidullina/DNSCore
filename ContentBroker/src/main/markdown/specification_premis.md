#  Spezifikationen der im DNS verwendeten PREMIS (premis.xml)


Jede premis.xml die innerhalb des DNS verwendet wird, unterliegt den folgenden Schema-Definitionen:
* http://www.loc.gov/standards/premis/v2/premis-v2-2.xsd
* http://www.danrw.de/schemas/contract/v1/danrw-contract-1.xsd 
* Ergänzung im Namespace V1 zu Lizenzen: [danrw-contract-1.01.xsd](https://github.com/da-nrw/DNSCore/blob/master/ContentBroker/src/main/xsd/v1/danrw-contract-v1-01.xsd) (unter der URL https://www.danrw.de/schemas/contract/v1/danrw-contract-v1-01.xsd)
* Beabsichtigte Ergänzung um Mindest-Quualitätsstufe im Namespace V1: [danrw-contract-v1-02.xsd](https://github.com/da-nrw/DNSCore/blob/master/ContentBroker/src/main/xsd/v1/danrw-contract-v1-02.xsd) (vsstl. Feb. 2019 unter der URL http://www.danrw.de/schemas/contract/v1/danrw-contract-v1-02.xsd)



### Inhalt einer vom SIP-Builder gebauten Premis-Datei für ein einzulieferndes SIP-Paket

* Ein Object-Element für das gebaute Paket
  * objectIdentifierType: PACKAGE_NAME
  * objectIdentifierValue: \[DAN:Name des SIPs, d.h. Name der tgz-Datei ohne Dateiendung\]
* Ein Event-Element für die SIP-Erstellung
  * eventIdentifierType: SIP_CREATION_ID
  * eventIdentifierValue: Sip_Creation_\[DAN:Datum der Premis-Erstellung\]
  * EventType: SIP_CREATION
  * eventDateTime: \[DAN:Datum der Premis-Erstellung\]
  * Link auf den SIP-Builder-Agent
  * Link auf das Paket-Object
* Ein Agent-Element für den SIP-Builder, mit dem das SIP erstellt wurde
  * agentIdentifierType: APPLICATION_NAME
  * agentIdentifierValue: DA NRW SIP-Builder \[DAN:Version\]
  * agentType: APPLICATION
* Ein rights-Block für die festgelegten Contract Rights

    ```xml
    <rights>
        <rightsStatement>
            <rightsStatementIdentifier>
                <rightsStatementIdentifierType>rightsid</rightsStatementIdentifierType>
                <rightsStatementIdentifierValue></rightsStatementIdentifierValue>
            </rightsStatementIdentifier>
            <rightsBasis>license</rightsBasis>
    ```
  * rightsGranted-Block je identischer Aufbau für unterschiedliche Veröffentlichungsszenarien: **PUBLICATION_PUBLIC | PUBLICATION_INSTITUTION | MIGRATION**. Die genauere Spezifikation der Rechte folgt im darauffolgendem rightsExtention-Block. Die **PUBLICATION_PUBLIC | PUBLICATION_INSTITUTION** sind **Optional**, beim fehlen gibt es auch keine dazugehörigen publicationRight-Blöcke innerhalb des rightsExtention-Blocks.

    ```xml
            <rightsGranted>
                <act>PUBLICATION_PUBLIC</act>
                <restriction>see rightsExtension</restriction>
                <termOfGrant>
                    <startDate>2016-03-14T00:00:00.000+01:00</startDate>
                </termOfGrant>
            </rightsGranted>
    ```

    ```xml
        </rightsStatement>
    ```
* Ein Rights-Extension Block für genauere Spezifikation der Publikationsrechte. Darin sind Restriktionen und Verarbeitungsangaben untergebracht, wie die Inhalte zur Veröffentlichung aufzubereiten sind.

    ```xml
        <rightsExtension>
            <rightsGranted xmlns="http://www.danrw.de/contract/v1" xmlns:xsi="http://www.danrw.de/contract/v1 http://www.danrw.de/schemas/contract/v1/danrw-contract-1.xsd">
    ```
  * Migrationsbedingung: **NONE | NOTIFY | CONFIRM**

    ```xml
                <migrationRight>
                    <condition>NONE</condition>
                </migrationRight>
    ```
  * **(Optional)** DDB-Harvesting nicht erlauben (falls das Element fehlt, gilt es als erlaubt)

    ```xml
                <DDBexclusion/>
    ```
  * **(Optional)** Lizensen für den fall einer Publikationsabsicht. Darf nur angegeben werden wenn in den Metadaten noch keine Lizenz angegeben worden ist. Bei Publikationsabsicht muss eine Lizenzangabe entweder in der Premis oder in den Metadaten(z.B. Mets) stehen.
Der Link ist der wichtigste Teil der Lizenzangabe, dieser wird bis ins Portal weitergereicht und ins EDM übernommen.
    ```xml
                <publicationLicense href="https://creativecommons.org/publicdomain/mark/1.0/" displayLabel="Public Domain Mark 1.0">Public Domain Mark 1.0</publicationLicense>
           
    ```
  * **(Optional)** Angabe einer Mindest-Qualitätsstufe. Sollte während der SIP-Verarbeitung eine kleinere Qualitätstufe ermittelt werden, wird ein USER_ERROR ausgelöst. Mögliche Ausprägungen sind 1,2,3,4,5.
    ```xml
               <minimalIngestQualityLevel>3</minimalIngestQualityLevel>    
    ```
  * **(Optional)** publicationRight-Block je für **PUBLIC | INSTITUTION** 

    ```xml 
                <publicationRight>
                    <audience>PUBLIC</audience>
                    <startDate>2016-03-10T00:00:00.000+01:00</startDate>
    ```
  * **(Optional)** Begrenzung der Publikation durch Sperrgesetz: EPFLICHT, URHG_DE

    ```xml
                    <lawID>URHG_DE</lawID>
    ```
  * **(Optional)** Block für Vorschaurestriktionen für die Öffentlichkeit

    ```xml
                    <restrictions>
    ```
    * **(Optional)** Audio-Restriktionen

     ```xml
                        <restrictAudio>
                            <duration>60</duration>
                        </restrictAudio>
     ```
    * **(Optional)** Text-Restriktionen

     ```xml
                        <restrictText>
                            <certainPages>1 2 3 4 5 6 7 8 9 10</certainPages>
                        </restrictText>
     ```
    * **(Optional)** Video-Restriktionen

     ```xml
                        <restrictVideo>
                            <height>720</height>
                            <duration>120</duration>
                        </restrictVideo>
     ```
    * **(Optional)** Bild-Restriktionen

     ```xml
                        <restrictImage>
                            <width>25%</width>
                            <height>25%</height>
                            <watermark>
                                <watermarkString>WasserzeichenText</watermarkString>
                                <pointSize>20</pointSize>
                                <position>south</position>
                                <opacity>25</opacity>
                            </watermark>
                        </restrictImage>
     ```
    ```xml
                    </restrictions>                 
    ```
  
    ```xml
                </publicationRight>            
    ```
    ```xml
            </rightsGranted>
        </rightsExtension>
    ```
   ```xml
    </rights>
   ```

### Zusätzliche Schnittstellendefinitionen einer Premis-Datei für ein einzulieferndes SIP-Paket
Dieser Abschitt enthält weitere Schnittstellenelemente die in der premis.xml eines einzuliefernden SIP-Paketes enthalten werden können. Der SIP-Builder verwendet diese nicht, da sie für primäre Anwendungsfälle irrelevant sind.

* Das Object-Element kann statt dem **PACKAGE_NAME** auch **URN** oder **OBJECT_BUSINESS_KEY** als **objectIdentifierType** enthalten. Das **objectIdentifierValue** hat entsprechend dem Typ entweder eine URN oder einen systemweit eindeutigen technischen-Identifier (genauere Details: [Identifiern](https://github.com/da-nrw/DNSCore/blob/master/ContentBroker/src/main/markdown/feature_identifier_assignment.md#leistungsmerkmal-vergabe-von-identifiern),[URN-Vergabe](https://github.com/da-nrw/DNSCore/blob/master/ContentBroker/src/main/markdown/specification_sip.de.md#urn-vergabe)).



### Vom System erwartete Eigenschaften einer eingehenden PREMIS-Datei

* Die zwingenden Elemente:
	* Object-Element für das gebaute Paket
	* Ein Event der SIP-Erstellung
	* rights-Block

* Die optionalen Elemente dürfen fehlen:
	* restrictions-Block oder einzelne Bestandteile davon sind optional
	* lawID-Element
	* DDBexclusion-Element
	* Lizenzen-Element (publicationLicense)
	* rightsGranted-Block für PUBLICATION_INSTITUTION-act und PUBLICATION_PUBLIC-act (dementsprechend auch der zugehörige publicationRight-Block innerhalb des Rights-Extension Blocks)
	

### Im Falle von Erstanlieferungen werden folgende PREMIS-Elemente in der initialen premis.xml angelegt.

###### einmalig ein PREMIS-Object

* Identifier-Typ URN angelegt.
* Entspricht einer Entität vom Typ model.Object

###### für das Package in jedem Fall ein PREMIS-Object

* Identifier vom Identifier-Typ PACKAGE_NAME angelegt.
* Der Package-Name entspricht dem Namen der am Ende des Ingestvorgangs erzeugten tar-Datei

###### für alle Files, die direkt im SIP mitangeliefert werden jeweils ein Premis-Object

* siehe [File-Object](https://github.com/da-nrw/DNSCore/blob/master/ContentBroker/src/main/markdown/specification_premis.md#fileobject)

###### für jedes konvertierte File jeweils ein PREMIS-Object

* siehe [File-Object](https://github.com/da-nrw/DNSCore/blob/master/ContentBroker/src/main/markdown/specification_premis.md#fileobject)


###### für jedes konvertierte File jeweils ein PREMIS-Event

* siehe [Konversionsevent](https://github.com/da-nrw/DNSCore/blob/master/ContentBroker/src/main/markdown/specification_premis.md#konversionsevent)

###### einmalig ein PREMIS-Event zur Repräsention des Ingest des jeweiligen Paketes

* siehe [IngestEvent](https://github.com/da-nrw/DNSCore/blob/master/ContentBroker/src/main/markdown/specification_premis.md#ingestevent)

###### für jeden an der Verarbeitung des Paketes beteiligten Knoten ein PREMIS-Agent

* Name des Knotens

###### je Paket eine SIP-Erstellungs-Event

* siehe [SIPCreationEvent](https://github.com/da-nrw/DNSCore/blob/master/ContentBroker/src/main/markdown/specification_premis.md#sipcreationevent)

###### für den Contractor ein PREMIS-Agent

* Contractor Short Name

### Im Falle von Deltas enthalten die PREMIS-Dateien folgende Elemente:


###### einmalig ein PREMIS-Object

* Identifier-Typ URN angelegt.
* Entspricht einer Entität vom Typ model.Object

###### für jedes Paket inklusive des neuesten Delta-Paketes ein PREMIS-Object

* Identifier vom Identifier-Typ PACKAGE_NAME angelegt.
* Der Package-Name entspricht dem Namen der am Ende des Ingestvorgangs erzeugten tar-Datei

###### für alle Files, die in allen (Delta)-SIPs mitangeliefert werden jeweils ein PREMIS-Object

* siehe&nbsp;PREMIS-Objekt, das ein File beschreibt

###### für jedes konvertierte File jeden Paketes jeweils ein PREMIS-Objekt

* siehe&nbsp;PREMIS-Objekt, das ein File beschreibt

###### für jedes konvertierte File jeden Paketes jeweils 1 PREMIS-Event&nbsp;

* siehe Konversionsevent

###### je Paket eine SIP-Erstellungs-Event

* siehe SIPCreationEvent

###### für jedes Paket ein PREMIS-Event

* siehe IngestEvent


###### für jeden an einer Konversion beteiligten Knoten ein PREMIS-Agent

* Name des Knotens

###### im Falle der Nutzung des SIP-Builders ein PREMIS-Agent

* Name bzw. Version des SIP-Builders

###### für den Contractor ein PREMIS-Agent

* Contractor Short Name

### Spezifikationen einzelner Elemente

##### FileObject

* Dateipfad relativ zum data-Verzeichnis
* CompositionLevel (momentan immer 0)
* MD5-Checksumme
* Dateigröße
* PRONOM-ID des Dateiformats
* JHOVE-Daten
* Ursprünglicher Dateiname (vor einer evtl. stattgefundenen Konversion)
* Name des Pakets, in dem die Datei enthalten ist
* Linking zu Package

##### KonversionsEvent

* Event-Typ CONVERT
* Identifier vom Typ TARGET_FILE_PATH
* Zeitpunkt der Konversion
* tatsächlicher Kommandozeilenaufruf
* kanonischer Name des Knotens, auf dem die Konversion stattgefunden hat
* Pfad (relativ zum data-Ordner) zur Datei in der a-Repräsentation, die konvertiert wurde
* Pfad (relativ zum data-Ordner) zur Datei in der b-Repräsentation, die das Ergebnis der Konversion ist

##### IngestEvent

* Event-Typ INGEST_EVENT
* Identifier vom Typ INGEST_ID
* Zeitpunkt des Ingests (= Zeitpunkt der Premis-Erstellung)
* Contractor Short Name des Contractors, der das Paket eingeliefert hat
* Name des Pakets (= Name der am Ende des Ingestvorgangs erzeugten tar-Datei)

##### VirusScanEvent

* Event-Typ VIRUS_SCAN
* Identifier vom Typ VIRUS_SCAN_ID&nbsp;
* Zeitpunkt des Virenscans
* Contractor Short Name des Contractors, der das Paket eingeliefert hat
* Name des Pakets (= Name der am Ende des Ingestvorgangs erzeugten tar-Datei)

##### SIPCreationEvent

* Event-Typ SIP_CREATION
* Identifier vom Typ SIP_CREATION_ID&nbsp;
* Zeitpunkt der SIP-Erstellung
* Version des Erstellungsprogrammes (z.B. SIP-Builder), mit dem das SIP erstellt wurde
* Name des Pakets (= Name der am Ende des Ingestvorgangs erzeugten tar-Datei)


Notizen:
Datenmodell Skizze zum besseren Verständnis hier
Zielstellung: Vollständige Objekthistorie nur aus Daten ableiten können. Das bedeutet auch, dass die Datenbank wiederhergestellt werden kann.
