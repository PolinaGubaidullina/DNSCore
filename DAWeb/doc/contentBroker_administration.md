	/*
	  DA-NRW Software Suite | ContentBroker
	  Copyright (C) 2013 Historisch-Kulturwissenschaftliche Informationsverarbeitung
	  Universität zu Köln
	
	  This program is free software: you can redistribute it and/or modify
	  it under the terms of the GNU General Public License as published by
	  the Free Software Foundation, either version 3 of the License, or
	  (at your option) any later version.
	
	  This program is distributed in the hope that it will be useful,
	  but WITHOUT ANY WARRANTY; without even the implied warranty of
	  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	  GNU General Public License for more details.
	
	  You should have received a copy of the GNU General Public License
	  along with this program.  If not, see <http://www.gnu.org/licenses/>.
	*/
	
### Administration of DNSCore (via DA-Web GUI)

Users owner of Role ROLE_PSADMIN, ROLE_NODEADMIN are able to perform several tasks a normal user ROLE_CONTRACTOR can't.
Some of the features being available are listed below.

The features pop up logging in as owner of (ROLE_PSADMIN, ROLE_NODEADMIN) in the home screen or as additional features visible in the objects view or in qeue list view. 

#### View CB Error Messages 

Error Messages of CB are listed in the frontend being available as "Adminstrative Funktionen" 
on Homepage in case you are logged in as admin role. 

The newest message is on top. The reason for listing is to make debugging 
of errors more easily and (in case you've more than one node to administer) to view errors 
of other nodes as well. 

#### Starting / Stopping CB-Factory 

In case of shutting down CB, you should use the Stop Factory button, 
avoiding uncontrolled interruption of work done by CB. After hitting the button "stop factory "
CB will stop working after having performed all running tasks completely. 

#### Show Actions performed by CB 

To view what your CB process actually is performing you may hit the "show actions" button. 

#### Graceful CB shutdown 

Hitting this button will cause CB to stop main execution thread after having completed all 
running actions. Please notice : There might be still processes called 

    java -jar ContenBroker.jar

Please execute after doing graceful shutdown
    ContentBroker_stop.sh 
    
otherwise starting ContentBroker will be denied next time.


#### Edit Users, Roles and UserRole Membership


DNSCore uses SpringSecurity to administer it's users of DA-Web GUI. 
To perfom ingest & retrieval, more configuration even on the storage level and the node storage level is needed.
To help and assist preservation system administrators to create the necessary folders on the iRODS storage level, we provide the script: [CreateContractorScript](../../ContentBroker/src/main/bash/createiRODSContractor.sh)

Users in Role ROLE_PSADMIN are able to adminster Users (CRUD), Roles (CRUD) and membership of Users in certain Roles. 
At present we suppose to use the Roles:

<pre>
ROLE_PSADMIN The Admin of the preservational system
ROLE_CONTRACTOR The standard Role of contractors
ROLE_SYSTEM Agents (TBD), not used yet
ROLE_NODEADMIN The Admin of a node in the preservational system's domain. 
</pre>

#### Edit ConversionRoutines

Users in Role ROLE_PSADMIN are able to adminster ConversionPolicies related to the system.

#### Format Mapping

On this page the user will have the possibility to load a new format mapping file from Pronom.
To start the action there have to be done some preparations. 

<b>First</b> the path /ci/storage/UserArea/rods/incoming must be created if not existing<br>
<b>Second</b> the new Pronom file must be downloaded and put into the folder /ci/storage/UserArea/rods/incoming<br>
<b>Third</b> the downlaoded file must be renamed, it must end with the current date, for example DROID_SignatureFile_20160503.xml<br>   

If this preparations are done, the user can update the mapping information while pressing the button 'Tabelle leeren und neu laden'. If there is no file, there will be an error message, otherwise the information will be refreshed.
 
#### Reload PIP

For convinence Admins can perform a rebuild of PIP (Presentation  Information packages). 
The PIP is being built on basis of the latest version, including all deltas. This feature is accessible on the "list objects" view for admin users only.

#### Reindex Elasticsearch

For convinence Admins can perform a rebuild of elastic search index insertion on basis of latest
PIP. This feature is accessible on the "list objects" view for admin users only.

#### Perform check status of AIP

Although automated service is carrying out integrity checks on AIP stored in the repository all 
the time, administrators can perform checks on demand as well. This feature is accessible on the "list objects" view for admin users only.


#### Trigger Recover and Deletion of workflow entries

Admins can perform adequate recover processes if they could be carried out by the system.  This is being indicated by buttons.  This feature is accessible on the "queue list" view for NODE_ADMIN role users only.

#### Trigger blockwise Actions on several items 

Users owning at least the NODE_ADMIN role are able to perform blockwise actions on several selected items (Retry, Recover, Deletion of SIP). First step is to filter for a dedicated state on which the blockwise operation should be carried out. After setting filter at least for a state, new operations show up at the and of the queue view table. 
