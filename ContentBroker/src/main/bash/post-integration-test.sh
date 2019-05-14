#!/bin/bash

# author: Daniel M. de Oliveira
src/main/bash/ContentBroker_stop.sh

rm cbTalk.sh
rm README.txt
rm ContentBroker_start.sh.template
rm ContentBroker_stop.sh.template
rm -r conf
rm target/installation/config.properties
rm target/installation/hibernateCentralDB.cfg.xml

irm -rf /ci/aip/TEST
rm -rf /ci/archiveStorage/aip/TEST

irm -rf /ci/CN/federated/c-i/aip/TEST
rm -rf /ci/archiveStorage/CN/federated/ci/aip/TEST

irm -rf /ci/CN/federated/zoneA/aip/TEST
rm -rf /ci/archiveStorage/CN/federated/zoneA/aip/TEST

sqls=(
	"DELETE FROM events;"
	"DELETE FROM copyjob;"
	"DELETE FROM conversion_queue;"
	"DELETE FROM documents;"
	"DELETE FROM dafiles;"
	"DELETE FROM queue;"
	"DELETE FROM objects_packages;"
	"DELETE FROM copies;"
	"DELETE FROM packages;"
	"DELETE FROM objects;"
	"DELETE FROM pending_mail;"
)

for i in "${sqls[@]}"
do
	echo "$i"

	if [ "$1" = "ci" ]
	then
	    psql -U cb_usr -d CB -c "$i"
	fi
done

