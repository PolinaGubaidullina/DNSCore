package daweb3

import org.springframework.web.multipart.MultipartHttpServletRequest;

class ReportController {
	def springSecurityService
	
	def decider = {
		def user = springSecurityService.currentUser
		if (params.get("answer").equals("start")) {
			redirect(action: 'start');
			return
		}  
		if (params.get("answer").equals("delete")) {
			log.debug("deletion")
			redirect(action: 'delete', params: [currentFiles: params.list("currentFiles")]);
			return
		}  
		if (params.get("answer").equals("retrieval")) {
			log.debug("retrieve")
			redirect(action: 'retrieval');
			return
		}
		redirect(action: 'index');
		return
	}
	
	def index = {
		def user = springSecurityService.currentUser
		
		def relativeDir = user.getContractorShortName()
		def baseFolder = grailsApplication.config.getProperty('localNode.userAreaRootPath') + "/" + relativeDir
		def httpurl = grailsApplication.config.getProperty('transferNode.downloadLinkPrefix') +"/"+   user.getContractorShortName()  + "/outgoing"
		
		def msgN = ""
		def baseDir;		
		def admin = 0;
		if (user.authorities.any { it.authority == "ROLE_NODEADMIN" }) {
			admin = 1;
		}
		
		def msg = null;
		msg = params.get("msg");
		params.remove("msg");
		if (msg!=null) {
		msg = msg + " " + msgN
		} else msg = msgN
			[msg:msg,httpurl:httpurl, user:user.username, admin: admin]
	}
	
	def save() {
		
		if(request instanceof MultipartHttpServletRequest) {
		def user = springSecurityService.currentUser
		
		def relativeDir = user.getContractorShortName() + "/incoming"
		def baseFolder = grailsApplication.config.getProperty('localNode.userAreaRootPath') + "/" + relativeDir
		
		def uploadedfile = request.getFile("file");
		if (!uploadedfile.isEmpty()) {
			uploadedfile.transferTo(new File (baseFolder + "/"+ uploadedfile.getOriginalFilename()))
			return redirect(action: "index", params: [msg :uploadedfile.getOriginalFilename() + " wurde hochgeladen" ]);
			} 
		} 
		return redirect(action: "index", params: [msg: "keine Datei hochgeladen!"])
		
	}
	
	def start(){
			def user = springSecurityService.currentUser
			CbNode node = CbNode.findById(grailsApplication.config.getProperty('localNode.id'));
			SystemEvent se = new SystemEvent();
			se.setType("CreateStatusReportEvent");
			se.setUser(user);
			se.setNode(node);
			se.save flush:true
			return redirect(action: "index", params: [msg: "Auftrag zur Erstellung eines Statusreports erfolgreich erstellt!"])
	}
	
	def retrieval(){
		def user = springSecurityService.currentUser
		CbNode node = CbNode.findById(grailsApplication.config.getProperty('localNode.id'));
		SystemEvent se = new SystemEvent();
		se.setType("CreateRetrievalRequestsEvent");
		se.setUser(user);
		se.setNode(node);
		se.save flush:true
		return redirect(action: "index", params: [msg: "Auftrag zum massenhaften Retrieval erfolgreich erstellt!"])
		
	} 
	
	def delete = {
		def user = springSecurityService.currentUser
		def relativeDir = user.getContractorShortName() + "/incoming"
		def baseFolder = grailsApplication.config.getProperty('localNode.userAreaRootPath') + "/" + relativeDir
		
		def files = params.list("currentFiles")

		def msg = ""
		files.each {
			 log.info "Datei ${it}"
			 try {
				 if (new File (baseFolder+ "/" + it).exists()) new File (baseFolder+ "/" + it).delete();
			 } catch (Exception e) {
			 }
		}
		[msg:msg]
		redirect(action:"index",params: [msg: files.size() + " Dateien gelöscht!"])
	}
	
	def snippetOutgoing = {
		def user = springSecurityService.currentUser
		def baseDir;
		def msgN;
		def currentFileOutgoing = []
		def relativeDir = user.getContractorShortName()
		def baseFolder = grailsApplication.config.getProperty('localNode.userAreaRootPath') + "/" + relativeDir
		def httpurl = grailsApplication.config.getProperty('transferNode.downloadLinkPrefix') +"/"+   user.getContractorShortName()  + "/outgoing"
		
		try {
			baseDir = new File(baseFolder + "/outgoing")
			if (!baseDir.exists()) {
				msgN = "Benutzerordner nicht gefunden"
				log.error(msgN);
		}
		CsvFileFilter filter = new CsvFileFilter();
		currentFileOutgoing = baseDir.listFiles(filter)?.sort{a, b -> a.lastModified() <=> b.lastModified() - (a.name <=> b.name)}.reverse()
			
		if (currentFileOutgoing.length==0) msgN ="Keine Dateien im Ausgangsordner gefunden";
		} catch (e) {
		msgN = "Benutzerordner " + baseDir + " existiert nicht!"
		log.error(msgN);
	
		}
		[currentFileOutgoing:currentFileOutgoing]
		
	}
	
	
	def snippetIncoming = {
		def user = springSecurityService.currentUser
		def baseDir;
		def msgN;
		def currentFileIncoming = []
		def relativeDir = user.getContractorShortName()
		def baseFolder = grailsApplication.config.localNode.getProperty('userAreaRootPath') + "/" + relativeDir
		try {
			baseDir = new File(baseFolder + "/incoming")
			if (!baseDir.exists()) {
				msgN = "Benutzerordner nicht gefunden"
				log.error(msgN);
		}
		CsvFileFilter filter = new CsvFileFilter();
		currentFileIncoming = baseDir.listFiles(filter)?.sort{a, b -> a.lastModified() <=> b.lastModified() - (a.name <=> b.name)}.reverse()
		
		if (currentFileIncoming.length==0) msgN ="Keine Dateien im Eingangsordner gefunden";
		} catch (e) {
		msgN = "Benutzerordner " + baseDir + " existiert nicht!"
		log.error(msgN, e);
		}
		[currentFileIncoming:currentFileIncoming]
		
	}
}
