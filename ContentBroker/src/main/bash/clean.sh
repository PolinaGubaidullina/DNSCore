#!/bin/bash
# author: Daniel M. de Oliveira
# author: Jens Peters


if [ "$1" = "ci" ]
then
	echo clean iRODS Stuff
	irm -rf /ci/aip/TEST                2>/dev/null
	irm -rf /ci/pips/institution/TEST   2>/dev/null
	irm -rf /ci/pips/public/TEST        2>/dev/null
	
	irm -rf /ci/CN/federated/c-i/aip/TEST/    2>/dev/null
	irm -rf /ci/CN/federated/zoneA/aip/TEST/  2>/dev/null
	
	rm -r /ci/storage/GridCacheArea/aip/TEST 
	mkdir /ci/storage/GridCacheArea/aip/TEST
	rm -r /ci/storage/WorkArea/work
	mkdir /ci/storage/WorkArea/work/TEST
	mkdir /ci/storage/WorkArea/pips/TEST
	rm -r /ci/storage/IngestArea/TEST
	mkdir /ci/storage/IngestArea/TEST
	rm -r /ci/storage/IngestArea/noBagit/TEST
	mkdir -p /ci/storage/IngestArea/noBagit/TEST
	rm -rf /ci/archiveStorage/aip
	mkdir /ci/archiveStorage/aip
	rm -rf /ci/archiveStorage/CN/federated/c-i/aip/TEST
	rm -rf /ci/archiveStorage/CN/federated/zoneA/aip/TEST

	src/main/bash/rebuildIndex.sh
else
	src/main/bash/cleanHSQLDB.sh
fi

echo Stopping ContentBroker
src/main/bash/ContentBroker_stop.sh


