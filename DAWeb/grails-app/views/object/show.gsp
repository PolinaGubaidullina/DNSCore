<%@ page import="daweb3.Object" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'object.label', default: 'Object')}" />
		<title>DA-NRW Objekt</title>
	</head>
	<body>
		<div class="page-body">
			<script type="text/javascript" >
				function toggle(source) {
					checkboxes = document.getElementsByName('currentPackages');
					for(var i in checkboxes) {
					  checkboxes[i].checked = source.checked;
					}
				}
				function deselect(source) {
					if (document.getElementById('waehlen').checked) {
						if (source.checked ) {
						} else {
							document.getElementById('waehlen').checked = false;
						}
					} else {
						checkboxes = document.getElementsByName('currentPackages');
						var i= 0;
	 					while ( i < checkboxes.length) {  
						 	if( checkboxes[i].checked) {
							 	check = true;
							 	i++;
							 } else {
							 	check = false;
							 	i++;
							 	break;
							}
						}
						if (check) {
							document.getElementById('waehlen').checked = true;
						} else {
							document.getElementById('waehlen').checked = false;
						}
					}
				}
			</script>
		
			<div id="show-object" class="content scaffold-show" role="main">
				<div class="blue-box"></div>
				<h2>Objektdetail</h2>
				<g:if test="${flash.message}">
				<div class="message" role="status">${flash.message}</div>
				</g:if>
				<ol class="property-list object">
				
					<g:if test="${objectInstance?.user}">
					<li class="fieldcontain">
						<span id="contractor-label" class="property-label"><g:message code="object.user.label" default="Contractor" /></span>
						
							<span class="property-value" aria-labelledby="contractor-label">${objectInstance?.user?.encodeAsHTML()}</span>
						
					</li>
					</g:if>
				
					<g:if test="${objectInstance?.origName}">
					<li class="fieldcontain">
						<span id="origName-label" class="property-label"><g:message code="object.origName.label" default="Orig Name" /></span>
						
							<span class="property-value" aria-labelledby="origName-label"><g:fieldValue bean="${objectInstance}" field="origName"/></span>
						
					</li>
					</g:if><li class="fieldcontain">
					<span id="packages-label" class="property-label"><g:message code="object.packages.label" default="Packages" /></span>
					<g:form controller="package" action="retrievePackages">
					<g:hiddenField name="oid" value="${objectInstance?.id}" />			
					
					<span class="property-value" ><input type="checkbox" name="waehlen" value="" id="waehlen" onClick="toggle(this)"/> Alle an-/abwählen</span><br>
					<g:if test="${sortedPackages}">
						<g:each in="${sortedPackages}" var="p" status="i">
								<span class="property-value" >
									<g:if test="${!objectInstance.isInWorkflowButton()}">
										<g:checkBox name="currentPackages" value="${p.getId()}" checked="false" onClick="deselect(this)"/>
									</g:if>${p?.encodeAsHTML()}
								</span>
						</g:each>	<br>
						<span class="property-value" >
							<g:if test="${!objectInstance.isInWorkflowButton()}">
								<g:actionSubmit class="style-btn-2 " value="Versioniertes Retrieval starten" controller="package" action="retrievePackages"/>
							</g:if>
						</span>
					</g:if>
					</g:form>
					</li>
					<g:if test="${objectInstance?.objectState}">
					<g:set var="statusCode" value="${objectInstance.getStatusCode()}" />
					<li class="fieldcontain">
						<span id="packages-label" class="property-label">Status:</span>
						<span class="property-value" aria-labelledby="packages-label">
						<g:if test="${statusCode == 1}">
							<asset:image width="16px" height="16px" src="/icons/warning32.png"/>
						</g:if>
						<g:elseif test="${statusCode == 2}">
							<asset:image  width="16px" height="16px" src="/icons/clock32.png"/>
						</g:elseif>
						<g:elseif test="${statusCode == 0}">
							<asset:image  width="16px" height="16px" src="/icons/check32.png"/>
						</g:elseif>
					</span>
					</li>
					</g:if>			
					<g:if test="${objectInstance?.urn}">
					<li class="fieldcontain">
						<span id="urn-label" class="property-label"><g:message code="object.urn.label" default="Urn" /></span>
						<span class="property-value" aria-labelledby="urn-label"> ${objectInstance?.urn}</span>
						<g:if test="${objectInstance.published_flag==1}">
							<g:link url="${objectInstance.getPublicPresLink() }" target="_blank"><span class="property-value" aria-labelledby="urn-label">Öffentliche Derivate (PIP)</span></g:link>
						</g:if>
						<g:if test="${objectInstance.published_flag==2}">
							<g:link url="${objectInstance.getInstPresLink() }" target="_blank"><span class="property-value" aria-labelledby="urn-label">Institutionelle Derivate (PIP)</span></g:link>
						</g:if>
						<g:if test="${objectInstance.published_flag==3}">
							<g:link url="${objectInstance.getPublicPresLink() }" target="_blank"><span class="property-value" aria-labelledby="urn-label">Öffentliche Derivate (PIP)</span></g:link>
							<g:link url="${objectInstance.getInstPresLink() }" target="_blank"><span class="property-value" aria-labelledby="urn-label">Institutionelle Derivate (PIP)</span></g:link>
						</g:if>
						</li>
						<li class="fieldcontain">
							<span id="urn-label" class="property-label">Technischer Name:</span>
							<span class="property-value" aria-labelledby="urn-label">${objectInstance?.identifier}</span>
						</li>
						<li class="fieldcontain">
							<span id="urn-label" class="property-label">Größe in Byte:</span>
							<span class="property-value" aria-labelledby="urn-label">
								<g:if test="${objectInstance?.aipSize<= 0}">
									0
								</g:if>
								<g:else>
									${objectInstance?.aipSize}
								</g:else>
							</span>
						</li>
					</g:if>
					
					
					<g:if test="${objectInstance?.createdAt}">
						<li class="fieldcontain">
						<span id="origName-label" class="property-label"><g:message code="object.created.label" default="Datum erstellt" /></span>
						
							<span class="property-value" aria-labelledby="origName-label">${objectInstance.getFormattedCreatedDate()}</span>
					</li>
				
					</g:if>
						<g:if test="${objectInstance?.modifiedAt}">
						<li class="fieldcontain">
						<span id="origName-label" class="property-label"><g:message code="object.modified.label" default="Datum geändert" /></span>
						
							<span class="property-value" aria-labelledby="origName-label">${objectInstance.getFormattedModifiedDate()}</span>
					</li>
					</g:if>
						<g:if test="${objectInstance?.static_nondisclosure_limit}">
						<li class="fieldcontain">
						<span id="origName-label" class="property-label">Startdatum einer Veröffentlichung</span>
						
							<span class="property-value" aria-labelledby="origName-label">${objectInstance.static_nondisclosure_limit}</span>
					</li>
					</g:if>
						<g:if test="${objectInstance?.dynamic_nondisclosure_limit}">
						<li class="fieldcontain">
						<span id="origName-label" class="property-label">Startdatum nach Gesetz"</span>
						
							<span class="property-value" aria-labelledby="origName-label">${objectInstance.static_nondisclosure_limit}</span>
					</li>
					</g:if>
						<g:if test="${objectInstance?.last_checked}">
						<li class="fieldcontain">
						<span id="origName-label" class="property-label">Letzte Überprüfung (Audit)</span>
						
							<span class="property-value" aria-labelledby="origName-label">${objectInstance.last_checked}</span>
					</li>
					</g:if>
					<g:if test="${objectInstance?.original_formats}">
						<li class="fieldcontain">
							<span id="origName-label" class="property-label">Enthaltene Formate aller zu diesem Objekt eingelieferten SIP</span>
							<g:each in="${objectInstance.original_formats?.split(",")}"> 
								<span class="property-value" aria-labelledby="urn-label">
							  		<g:each in="${extensionSip.keySet().toString().replace('[', '').replace(']','').split(",")}" var="keySIP">
										<g:if test="${keySIP.trim() == it.trim()}">
											${extensionSip.getAt(keySIP.trim()).toString().replace('[', '').replace(']','')} : 
										</g:if> 
									</g:each>
							  	 	<g:if test="${!it.startsWith("danrw")}">
							  			<g:link url="http://www.nationalarchives.gov.uk/PRONOM/${it}" target="_blank">
									   		${it}
								  		</g:link>
							   		</g:if>
							   		<g:else>
							   			<span class="property-value" aria-labelledby="urn-label">
								   			<g:each in="${extensionSip.keySet().toString().replace('[', '').replace(']','').split(",")}" var="keySIP">
												<g:if test="${keySIP.trim() == it.trim()}">
													${extensionSip.getAt(keySIP.trim()).toString().replace('[', '').replace(']','')} : 
												</g:if> 
											</g:each>	
							   				${it}
							   			</span>
							   		</g:else>
							   	</span>
							</g:each>
						</li>
					</g:if>
					<g:if test="${objectInstance?.most_recent_formats}">
						<li class="fieldcontain">
							<span id="origName-label" class="property-label">Formate der aktuellsten Repräsentation (DIP)</span>
							<g:each in="${objectInstance.most_recent_formats?.split(",")}">
								<span class="property-value" aria-labelledby="urn-label">
							  		<g:each in="${extensionDip.keySet().toString().replace('[', '').replace(']','').split(",")}" var="keyDIP">
										<g:if test="${keyDIP.trim() == it.trim()}">
											${extensionDip.getAt(keyDIP.trim()).toString().replace('[', '').replace(']','')} : 
										</g:if> 
									</g:each>
									<g:if test="${!it.startsWith("danrw")}">
								  		<g:link url="http://www.nationalarchives.gov.uk/PRONOM/${it}" target="pronom">
								  			${it}
								  		</g:link>
								    </g:if>
								    <g:else>
								      <span class="property-value" aria-labelledby="urn-label">
									 	 <g:each in="${extensionDip.keySet().toString().replace('[', '').replace(']','').split(",")}" var="keyDIP">
									        <g:if test="${keyDIP.trim() == it.trim()}">
											  ${extensionDip.getAt(keyDIP.trim()).toString().replace('[', '').replace(']','')} : 
										    </g:if> 
									  	</g:each>	
								   		${it}
								   	  </span>
							   		</g:else>
							  	 </span>
							</g:each>
						</li>
					</g:if>
					<g:if test="${objectInstance?.most_recent_secondary_attributes}">
					   <li class="fieldcontain">
						<span id="origName-label" class="property-label">Codecs der Oberflächenansicht</span>
							  <g:each in="${objectInstance.most_recent_secondary_attributes?.split(",")}">
							  	 <span class="property-value" aria-labelledby="origName-label">${it}</span>
							  </g:each>
					   </li>
					</g:if>
					<g:if test="${objectInstance?.ddb_exclusion!=null}">
						<li class="fieldcontain">
						<span id="origName-label" class="property-label">Beschränkung Harvesting DDB</span>
							  	 <span class="property-value" aria-labelledby="origName-label">${objectInstance.ddb_exclusion}</span>
						</li>
					</g:if>
					
				</ol>
				<g:form>
					<fieldset class="buttons">
						<g:hiddenField name="id" value="${objectInstance?.id}" />
						<g:actionSubmit class="cancel" action="cancel" value="${message(code: 'default.button.cancel.label', default: 'Cancel')}" />
					</fieldset>
				</g:form>
			</div>
		</div>
	</body>
</html>
