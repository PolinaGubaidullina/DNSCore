<%@ page import="daweb3.Object" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'object.label', default: 'Object')}" />
		<title>DA-NRW Objekte</title>
		
		<r:require module="messagebox"/>
		<r:script>
			$(function() {
				$("#filter").accordion({ collapsible: true, active: false });
			});
			function queuedFor(result) {
				var type = "error";
				if (result.success) type = "info";
				var messageBox = $("<div class='message-box'></div>");
				$("#page-body").prepend(messageBox);
				messageBox.message({
					type: type, message: result.msg
				});
			}
		</r:script>
	</head>
	<body>
		<a href="#list-object" class="skip" tabindex="-1">
			<g:message code="default.link.skip.label" default="Skip to content&hellip;"/>
		</a>
		<div id="filter" style="margin: 0.8em 0 0.3em">
			<h1><a href="#">Filter
				<g:if test="${params.search}"><br>
		    		<g:if test="${!params.search?.origName.isEmpty()}">
		    			<span style="margin-right: 25px"><i>Originalname: </i>${params.search?.origName}</span>
		    		</g:if> 
		    		<g:if test="${!params.search?.urn.isEmpty()}">
		    			 <span style="margin-right: 25px"><i>URN: </i>${params.search?.urn}</span>
		    		</g:if> 
		    		<g:if test="${!params.search?.identifier.isEmpty()}">
		    			<span style="margin-right: 25px"><i>Identifier: </i>${params.search?.identifier}</span>
		    		</g:if> 
		    		<g:if test="${params.searchContractorName != null}">
		    			<span style="margin-right: 25px"><i>Contractor: </i>${params.searchContractorName}</span>
		    		</g:if> 
					<div>
						<g:if test="${params.searchDateType != null } ">
	    					<g:if test="${params.searchDateType == 'createdAt'}">Datumsbereich erstellt</g:if>
	    					<g:if test="${params.searchDateType == 'modifiedAt'}">Datumsbereich geändert</g:if>
			    		</g:if>    
			    		<g:if test="${!params.searchDateStart.isEmpty()}">
			    			<span style="margin-right: 25px"><i>Von Datum: </i>${params.searchDateStart}</span>
			    		</g:if> 	
			    		<g:if test="${!params.searchDateEnd.isEmpty()}">
			    			<span style="margin-right: 25px"><i>Bis Datum: </i>${params.searchDateEnd}</span>
			    		</g:if> 
			    	</div>
		    	</g:if> 
			</a></h1> 
            <g:form name="searchForm" action="list">
            <div class="table-responsive">
            	<table class="table">
            		<tr>
            			<td>Original Name:</td>
            			<td>
            				<g:textField name="search.origName" value="${params.search?.origName}" size="50"/>
            			</td>
            		</tr>
            		<tr>
            			<td>URN:</td>
            			<td>
            				<g:textField name="search.urn" value="${params.search?.urn}" size="50"/>
            			</td>
            		</tr>
            			<tr>
            			<td>Identifier:</td>
            			<td>
            				<g:textField name="search.identifier" value="${params.search?.identifier}" size="50"/>
            			</td>
            		</tr>
            		<tr>
            			<td>Datumsbereich:	</td>	
	            		<td>
	            			<g:select id="datetype" name="searchDateType" from="${['Datum erstellt','Datum geändert']}" keys="${['createdAt','modifiedAt']}" value="${params.searchDateType}" noSelection="[null:'Bitte auswählen']"/>
	            		</td>
					</tr>
            		<tr>
            			<td>Von Datum: </td>
            			<td><jqueryPicker:time name="searchDateStart" value=""/>(TT.MM.JJJJ HH:mm:ss)</td>
            		</tr>
            		<tr>
            			<td>Bis Datum: </td>
            			<td><jqueryPicker:time name="searchDateEnd" value=""/>(TT.MM.JJJJ HH:mm:ss) <% // fix for https://github.com/zoran119/grails-jquery-date-time-picker/issues/12 %>
            			
            			<script type="text/javascript">
            			 $(document).ready(function(){
            			$("#searchDateStart").val("${params.searchDateStart}")
            			$("#searchDateEnd").val("${params.searchDateEnd}")
            			 })
            			</script>
            			</td>
            		</tr>
            		<g:if test="${admin}">
            			<tr>
	            			<td>Contractor:</td>
	            			<td>
	            				<g:if test="${params.searchContractorName  == null}" >
	            					<g:select id="user" name="searchContractorName" from="${contractorList}" optionKey="shortName" noSelection="[null:'Bitte auswählen']" required="" value="${objectInstance?.contractorList?.shortName}" class="many-to-one"/>
	            				</g:if>
	            				<g:if test="${params.searchContractorName  != null && !params.searchContractorName.isEmpty()}" >
	            					<g:select id="user" name="searchContractorName" from="${contractorList}" optionKey="shortName" noSelection="[null:'Bitte auswählen']" required="" value="${params.searchContractorName}" class="many-to-one"/>
	            				</g:if>
	            			</td>
            			</tr>
            		</g:if>
            		<tr>
            			<td></td>
            			<td>
            				<g:submitButton name="submit" value="Filter anwenden"/>
           					<g:submitButton name="loeschen" type="submit" value="Filter löschen"/>
	           			</td>
	           			<script type="text/javascript">
		           			$(document).ready(function(){
		           				 	$("#loeschen").click(function() {                				 
				            			$('#searchForm').find(':input').each(function() {
				            	            switch(this.type) {
				                            case 'text':
				                            	$(this).val('');
				                                break;                      
				                            case 'textarea':
				                                $(this).val('');
				                                break;
				            			 	case 'hidden':
				                                $(this).val('0');
				                                break;	
				            			 	case 'select-one':
					                            $(this).val(null);
					                            break;
				                            }
				            			});
		           				    });
		           			});
		           		</script>
            		</tr>
            	</table> 
            </div>    
            </g:form>
        </div>
		<div id="list-object" class="content scaffold-list" role="main">
			<h1 style="text-align: center;">Ihre DA-NRW Objekte</h1>
			<g:if test="${flash.message}">
				<div class="message" role="status">${flash.message}</div>
			</g:if>
			<g:formRemote name="myForm" on404="alert('not found!')" 
              url="[controller: 'object', action:'queueAllForRetrieval']" 
              onLoaded="queuedFor(data)">
              <div style="overflow:auto; height: 600px">
				 <table>
					<thead>
						<tr>
							<th><g:message code="object.identifier.label" default="Ident" /></th>
							<g:sortableColumn property="urn" title="${message(code: 'object.urn.label', default: 'Urn')}" />
	     					<g:sortableColumn property="user" title="${message(code: 'object.user.label', default: 'Contractor')}" />
							<g:sortableColumn property="origName" title="${message(code: 'object.origName.label', default: 'Orig Name')}" />
							<g:sortableColumn property="createdAt" title="${message(code: 'object.created.label', default: 'Erstellt')}" />
							<g:sortableColumn property="modifiedAt" title="${message(code: 'object.modified.label', default: 'Geändert')}" />
							
							<g:if test="${admin}">
								<g:sortableColumn style="text-align: center" property="object_state" title="${message(code: 'object.object_state.label', default: 'Objekt Status')}" />
								<th style="text-align: center">Überprüfen</th>
								<th style="text-align: center">Pres. Derivate</th>
								<th style="text-align: center">Index</th>
							</g:if>
							<th style="text-align: center">Publ.</th>
							<th style="text-align: center">Anfordern				
								<g:if test="${!paginate}">
										<g:actionSubmitImage value="submit" action="submit"
                     						src="${resource(dir: 'images/icons', file: 'boxdownload32.png')}" />
								</g:if>
							</th>
							<th style="text-align: center">Entnahme	</th>			
						</tr>
					</thead>
					<tbody>
					<g:each in="${objectInstanceList}" status="i" var="objectInstance">
											
						<g:set var="statusCode" value="100" />
						<g:if test="${admin}">				
						<g:set var="statusCode" value="${objectInstance.getStatusCode()}" />
						</g:if>
						<tr class="${ ((i % 2) == 0 ? 'odd' : 'even') + ' status-type-' + statusCode}">
								<td>${fieldValue(bean: objectInstance, field: "identifier")}</td>
						
							<td><g:link action="show" id="${objectInstance.id}">${objectInstance.getFormattedUrn()}</g:link></td>
						
							<td>${fieldValue(bean: objectInstance, field: "user")}</td>
						
							<td>${fieldValue(bean: objectInstance, field: "origName")}</td>
							<td>${objectInstance.getFormattedCreatedDate()}</td>
							<td>${objectInstance.getFormattedModifiedDate()}</td>
							
							<td style="text-align: center">
								<g:if test="${statusCode == 1}">
									<g:img style="width:16px; height:16px" uri="/images/icons/warning32.png"/>
								</g:if>
									<th style="text-align: center">Publ.</th>
								<th style="text-align: center">Anfordern				
									<g:if test="${!paginate}">
										<g:actionSubmitImage value="submit" action="submit" src="${resource(dir: 'images/icons', file: 'boxdownload32.png')}" />
									</g:if>
								</th>
								<th style="text-align: center">Entnahme</th>			
							</tr>
						</thead>
						<tbody>
						<g:each in="${objectInstanceList}" status="i" var="objectInstance">					
							<g:set var="statusCode" value="100" />
							<g:if test="${admin}">				
								<g:set var="statusCode" value="${objectInstance.getStatusCode()}" />
							</g:if>
							<tr class="${ ((i % 2) == 0 ? 'odd' : 'even') + ' status-type-' + statusCode}">
								<td>${fieldValue(bean: objectInstance, field: "identifier")}</td>
								<td>
									<g:link action="show" id="${objectInstance.id}">${objectInstance.getFormattedUrn()}</g:link>
								</td>
								<td>${fieldValue(bean: objectInstance, field: "user")}</td>
								<td>${fieldValue(bean: objectInstance, field: "origName")}</td>
								<td>${objectInstance.getFormattedCreatedDate()}</td>
								<td>${objectInstance.getFormattedModifiedDate()}</td>
								<td style="text-align: center">
									<g:if test="${statusCode == 1}">
										<g:img style="width:16px; height:16px" uri="/images/icons/warning32.png"/>
									</g:if>
									<g:elseif test="${statusCode == 2}">
										<g:img style="width:16px; height:16px" uri="/images/icons/clock32.png"/>
									</g:elseif>
									<g:elseif test="${statusCode == 0}">
										<g:img style="width:16px; height:16px" uri="/images/icons/check32.png"/>
									</g:elseif>
								</td>
								<g:if test="${!objectInstance.isInWorkflowButton()}">
									<td style="text-align: center">
										<g:remoteLink action="queueForInspect" onLoaded="queuedFor(data)" id="${objectInstance.id}">
											<g:img style="width:16px; height:16px" uri="/images/icons/search32.png" title="${message(code: 'default.ltp.icon.queueForInspect', default: 'Objekt zum manuellen Überprüfen anfordern')}" alt="${message(code: 'default.ltp.icon.queueForInspect', default: 'Objekt zum manuellen Überprüfen anfordern')}"/>
										</g:remoteLink>
									</td>
									<td style="text-align: center">
										<g:remoteLink action="queueForRebuildPresentation" onLoaded="queuedFor(data)" id="${objectInstance.id}">
											<g:img style="width:16px; height:16px" uri="/images/icons/exchange32.png" title="${message(code: 'default.ltp.icon.rebuildPR', default: 'Objekt für Präsentation neu erzeugen')}" alt="${message(code: 'default.ltp.icon.rebuildPR', default: 'Objekt für Präsentation neu erzeugen')}"/>
										</g:remoteLink>
									</td>
									<td style="text-align: center">
										<g:remoteLink action="queueForIndex" onLoaded="queuedFor(data)" id="${objectInstance.id}">
											<g:img style="width:16px; height:16px" uri="/images/icons/exchange32.png" title="${message(code: 'default.ltp.icon.rebuildIndex', default: 'Objekt neu indexieren')}" alt="${message(code: 'default.ltp.icon.rebuildIndex', default: 'Objekt neu indexieren')}"/>
										</g:remoteLink>
									</td>
								</g:if>
							</td>
						</tr>
					</g:each>
					</tbody>
				</table>(AdministartorView)
			  </div>
			</g:formRemote>
			<g:if test="${paginate}" >
				<!-- workaround weil paginate die search map zerhackstückelt -->
				<g:set var="searchParams" value="${paramsList}"/>
				<div class="pagination">
					<g:paginate total="${objectInstanceTotal}" params="${searchParams}" />
				</div>
			</g:if>
		</div>
	</body>
</html>
