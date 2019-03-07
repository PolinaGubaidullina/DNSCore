package daweb3
/* 
 DA-NRW Software Suite | ContentBroker 
 Copyright (C) 2013 Historisch-Kulturwissenschaftliche Informationsverarbeitung 
 Universität zu Köln, 2014 LVRInfoKom 
 
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

//import java.io.Serializable
//import java.util.concurrent.ThreadPoolExecutor.AbortPolicy
//
//import groovy.transform.EqualsAndHashCode
//import groovy.transform.ToString

import groovy.transform.EqualsAndHashCode;
import groovy.transform.ToString;


@EqualsAndHashCode(includes='username')
@ToString(includes='username', includeNames=true, includePackage=false)
class User  implements Serializable {

	private static final long serialVersionUID = 1

	 transient springSecurityService
	 
	 int id
	 String shortName
	 
	 String username 
	 String password 
	 String provider_type
	 
	 boolean enabled = true
	 boolean accountExpired 
	 boolean accountLocked 
	 boolean passwordExpired
	 
	 boolean mailsPooled
	 boolean usePublicMets
	 boolean useVirusScan = true //DANRW-1511: standardmäßig wird gescannt
	 boolean deltaOnUrn
	 
	 String description
	 String friendly_file_exts
	 String email_contact
	 String forbidden_nodes

	User(String username, String password) {
		this.username = username
		this.password = password
	}

	static constraints = {
		email_contact blank: false
		shortName blank: false, unique: true
		username blank: false, unique: true
		password blank: false, password:true
		description(nullable:true)
		forbidden_nodes(nullable:true)
		friendly_file_exts(nullable:true)
	}

	static mapping = {
		table 'users'
		version false
		password column: 'password'
		accountExpired column: 'accountexpired'
		accountLocked column: 'accountlocked'
		passwordExpired column: 'passwordexpired'
		mailsPooled column: 'mails_pooled'
		usePublicMets column: 'use_public_mets'
		useVirusScan column: 'use_virus_scan'//DANRW-1511
		deltaOnUrn column: 'delta_on_urn'
		friendly_file_exts column: 'friendly_file_exts'
		provider_type column:'provider_type' //DANRW-1446
	}
	
	Set<Role> getAuthorities() { 
		 UserRole.findAllByUser(this).collect { it.role } as Set
		
	}

	def beforeInsert() {
		encodePassword()
	}

	def beforeUpdate() {
		if (isDirty('password')) {
			encodePassword()
		}
	}

	protected void encodePassword() {
		password = springSecurityService?.passwordEncoder ? springSecurityService.encodePassword(password) : password
	}

	static transients = ['springSecurityService']

	String toString() {
		return "$shortName"
	}
}
