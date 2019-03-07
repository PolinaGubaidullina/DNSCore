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

/**
 * @author jpeters
 */
import java.util.Map;

import grails.plugin.springsecurity.annotation.Secured

import org.springframework.dao.DataIntegrityViolationException

class ConversionPoliciesController {

	
	def springSecurityService
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
		def admin = 0;
		User user = springSecurityService.currentUser
		if (user.authorities.any { it.authority == "ROLE_NODEADMIN" }) {
			admin = 1;
		}
		
        params.max = Math.min(max ?: 30, 100)
		
		/*
		 * 11.07.2016 extension to access table format_mapping
		 */
		def fm = new FormatMapping()
		def mapping
		List<ConversionPolicies> lcp = ConversionPolicies.list(params)
		Map <String, String> fmMap = [:]
		String ext
		
		for (ConversionPolicies item : lcp) {
			mapping = fm.findAll("from FormatMapping where puid = :puid", [puid : item.source_format])
			 
			String puid = mapping.puid.toString().replace('[', '').replace(']','');
			String extension =  mapping.extension.toString().replace('[', '').replace(']','');
			
			
			fmMap.put(puid, extension);	
		}
		
        [conversionPoliciesInstanceList: ConversionPolicies.list(params), 
			conversionPoliciesInstanceTotal: ConversionPolicies.count(),
			formatMappList : fmMap, user:user, admin: admin]
    }

	@Secured(['ROLE_PSADMIN'])
    def create() {
		def user =  springSecurityService.currentUser
		def admin = 0
		if (user.authorities.any { it.authority == "ROLE_NODEADMIN" }) {
			admin = 1;
		}
        [conversionPoliciesInstance: new ConversionPolicies(params), admin: admin, user: user]
	}

	@Secured(['ROLE_PSADMIN'])
    def save() {
		 def conversionPoliciesInstance = new ConversionPolicies(params)
        if (!conversionPoliciesInstance.save(flush: true)) {
            render(view: "create", model: [conversionPoliciesInstance: conversionPoliciesInstance])
            return
        }
        flash.message = message(code: 'default.created.message', args: [message(code: 'conversionPolicies.label', default: 'ConversionPolicies'), conversionPoliciesInstance.id])
		redirect(action: "create")
	 }

	@Secured(['ROLE_PSADMIN'])
	def delete(Long id) {
		def conversionPoliciesInstance =  ConversionPolicies.get(id)
		conversionPoliciesInstance.delete flush: true 
		flash.message = message(code: 'default.deleted.message', args: [message(code: 'conversionPolicies.label', default: 'ConversionPolicies'), conversionPoliciesInstance.id])
		redirect(action: "list")
	}
	
	@Secured(['ROLE_PSADMIN'])
	def cancel() {
		redirect(action: "show",  id: params.id)  
	}
	
    def show(Long id) {
		def user = springSecurityService.currentUser
		def admin = 0;
		def adminAnzeige = 0;
		if (user.authorities.any { it.authority == "ROLE_PSADMIN"}) {
			admin = 1;
		}
		if (user.authorities.any { it.authority == "ROLE_NODEADMIN" }) {
			adminAnzeige = 1;
		}
        def conversionPoliciesInstance = ConversionPolicies.get(id)
				
        if (!conversionPoliciesInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'conversionPolicies.label', default: 'ConversionPolicies'), id])
            redirect(action: "list", model:[user:user, admin:adminAnzeige])
            return
        } 
		
		/*
		 * 11.07.2016 extension to access table format_mapping
		 */
		def fm = new FormatMapping()
		def mapping
		String ext

		mapping = fm.findAll("from FormatMapping where puid = :puid", [puid : conversionPoliciesInstance.source_format])
		ext = mapping.extension
		
        [conversionPoliciesInstance: conversionPoliciesInstance, adminAllg: admin, admin: adminAnzeige, user: user,
			extension : ext.replace("[", "").replace("]","")]
    }

	
	@Secured(['ROLE_PSADMIN'])
    def edit(Long id) {
		def user =  springSecurityService.currentUser
        def admin = 0
		if (user.authorities.any { it.authority == "ROLE_NODEADMIN" }) {
			admin = 1;
		}
		def conversionPoliciesInstance = ConversionPolicies.get(id)
	    if (!conversionPoliciesInstance) {
	        flash.message = message(code: 'default.not.found.message', args: [message(code: 'conversionPolicies.label', default: 'ConversionPolicies'), id])
	        redirect(action: "list", model:[admin: admin, user: user])
	        return
	    }

        [conversionPoliciesInstance: conversionPoliciesInstance, admin: admin, user: user]
    } 

	@Secured(['ROLE_PSADMIN'])
    def update(Long id, Long version) {
        def conversionPoliciesInstance = ConversionPolicies.get(id)
        if (!conversionPoliciesInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'conversionPolicies.label', default: 'ConversionPolicies'), id])
            redirect(action: "list")
            return
        }

        if (version != null) {
            if (conversionPoliciesInstance.version > version) {
                conversionPoliciesInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'conversionPolicies.label', default: 'ConversionPolicies')] as Object[],
                          "Another user has updated this ConversionPolicies while you were editing")
                render(view: "edit", model: [conversionPoliciesInstance: conversionPoliciesInstance])
                return
            }
        }

        conversionPoliciesInstance.properties = params

        if (!conversionPoliciesInstance.save(flush: true)) {
            render(view: "edit", model: [conversionPoliciesInstance: conversionPoliciesInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'conversionPolicies.label', default: 'ConversionPolicies'), conversionPoliciesInstance.id])
        redirect(action: "show", id: conversionPoliciesInstance.id)
	}
}
