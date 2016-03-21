# DNSCore - Dokumentation

Deutsche Version | [English Version](documentation.md)

Willkommen auf der Startseite der Dokumentation des DNSCore Softwarepakets! Dies hier ist die primäre Quelle zu allen Fragen bezüglich der Handhabung des Softwarepakets. 

**Überblick:**
[Leistungsmerkmale (Features) der Software](features.de.md)

Der Großteil der Dokumentation befindet sich unter

[DNSCore/ContentBroker/src/main/markdown/](../markdown),

[Übersicht der verschiedenen Spezifikationen SIP/AIP/DIP](./specifications_DNS.md)

wobei sich auch noch weitere Dokumention unter folgendem Link findet:

[DNSCore/DAWeb/doc](../../../../DAWeb/doc).

**Versionierung** der Dokumentation:
Die Dokumentation ist Teil des Git-Repository der DNSCore und wird gemeinsam mit der Codebasis versioniert. Auf diese Weise wird der Stand der Dokumentation synchron zum Stand der Codebasis gehalten. Die Links sind, soweit es möglich ist, relativ, damit ein Checkout einer beliebigen Version selbstreferentiell und konsistent ist. Im Falle der Grafiken ist diese Vorgehensweise hinsichtlich der Darstellung in GitHub nicht möglich (wegen der Referenzen auf raw-Dateien). Hier finden sich die Quellen der Grafiken aber immer im ContentBroker/src/main/markdown Verzeichnis wieder.

#### Langzeitarchivierung mit DNSCore verstehen<br>[Alle Benutzergruppen]

Hier befinden sich alle Dokus, die Grundkonzepte der Langzeitarchivierung im Kontext des Einsatzes von DNSCore beschreiben.

* Das DNSCore Objektmodell - Referenzdokumentation ([deutsch](object_model.de.md) | [englisch](object_model.md))
* SIP-Spezifikation ([deutsch](specification_sip.de.md) | [englisch](specification_sip.md))
* DIP-Spezifikation ([englisch](specification_dip.md))
* Dokumentation der Metadaten-Verarbeitung ([deutsch](specification_metadata.de.md) | [englisch](specification_publication_metadata.md))
* Delta Feature - Beschreibung ([deutsch](the_delta_feature.de.md) | [english](the_delta_feature.md))

#### DNSCore verwenden<br>[Endnutzer]

Dieser Bereich wendet sich an die Anwender der DNSCore Lösung für Langzeitarchivierung.

* Ingest ([deutsch](usage_ingest.de.md))
* Retrieval ([deutsch](usage_retrieval.de.md))

#### Bereitstellungen von Dienstleistungen an Endnutzer<br>[Systemadministratoren / Knotenadministratoren / Betreiber]

Dieser Bereich umfasst Dokumentationen, die sich damit beschäftigen, wie mithilfe eines Verbundes von Knoten, auf denen DNSCore läuft, Langzeitarchivierungsfunktionalität für Endkunden bereitgestellt werden kann.

* AIP-Spezifikation ([englisch](specification_aip.md))
* PREMIS-Spezifikation([english](specification_premis.md))
* Formatidentifikation mit DNSCore ([deutsch](operations_format_identification.de.md))
* Formatkonversion mit DNSCore ([deutsch](operations_format_conversion.de.md))
* Formatmodul ([englisch](format_module.md))

#### Umgebungen mit DNSCore aufsetzen<br>[Knotenadministratoren]

In diesem Bereich sind Dokumentationen untergebracht, die die Installation der Software DNSCore,
die vorbereitenden Anpassung von Umgebungen für den Einsatz von DNSCore bzw. Anpassung von DNSCore an die Umgebungen, sowie der Anbindung von DNSCore an Fremdkomponenten thematisieren. 

###### Basiskonfiguration

Die hier untergebrachten Dokumentationen gelten generell für den Einsatz von DNSCore.

* config.properties - Referenzdokumentation ([deutsch](administration_config_properties_reference.de.md))
* beans.xml - Referenzdokumentation ([english](administration-beans.md))
* Speicherbereiche - Referenzdokumentation ([english](processing_stages.md))
* Der DNSCore Installer ([deutsch](administration-the-installer.de.md) | [english](administration-the-installer.md))

###### Erweiterte Konfiguration

Die hier untergebrachten Dokumentationen setzen den Einsatz von DNSCore unter bestimmten Bedingungen voraus. Etwa die Anbindung an bestimmte externe Systeme, deren Bedingungen der Anbindung hier genauer beschrieben sind.

* Installation von ElasticSearch für DNSCore ([deutsch](install_elasticsearch.de.md))
* Installation von Fedora für DNSCore ([deutsch](install_fedora.de.md))
* Installation von PrOAI für DNSCore ([deutsch](install_proai.md))
* Installing iRODS for DNSCore ([englisch](installation_irods.md))

#### Fehlerbehebung<br>[Systemadministratoren / Knotenadministratoren]
Die aufgeführten Dokumente geben Hinweise zur Fehlerbehebung bei der Knotenadministration.
* Spezifikation der Fehlerstatus & Hinweise zur Fehlerbehebung ([deutsch](administration-troubleshooting.de.md) )

#### Funktionalitäten der DNSCore erweitern<br>[Entwickler]

Um DNSCore effektiv bauen, testen und ausliefern zu können, sollten die folgenden Dokus gelesen werden:

* Auflistung der Features ([deutsch](features.de.md))
* Bauen und Testen der DNSCore ([englisch](development_deploy.md))
* Bauen und Ausliefern DAWeb ([english](../../../../DAWeb/doc/setup.md))
* Aufsetzen von Continuous Integration für DNSCore builds ([englisch](development_setting_up_ci.md))
* Systemkomponenten im Überblick ([english](components_connectors.md))
* 3rd Party Module ([english](3rdPartyTools.md))
* Metadata Workflow - Übersicht ([english](metadata_workflow.md))

###### Java API Dokumentation

Die Java API Dokumentation wird zum jetzigen Zeitpunkt nicht aktiv automatisch geupdatet, so dass er auf Bedarf manuell erzeugt werden muss.

* Javadoc für DNSCore erzeugen und auf GitHub publizieren ([englisch](javadoc.md))
* Javadoc API Dokumentation. Älterer Stand. ([hier](http://da-nrw.github.io/DNSCore/apidocs/))
* Javadoc Test Dokumentation. Älterer Stand. ([hier](http://da-nrw.github.io/DNSCore/testapidocs/))

