
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

/**
 * Lists some information in the view ... 
 * @Author Jens Peters
 */
package daweb3

class InfoController {
	
	def springSecurityService
	
    def index() {
		def admin = 0
		User user = springSecurityService.currentUser
		if (user.authorities.any { it.authority == "ROLE_NODEADMIN" }) {
			admin = 1;
		}
		[ user:user.username, admin: admin]
	}
}
