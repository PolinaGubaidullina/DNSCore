<%@ page import="daweb3.CbNode" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'cbNode.label', default: 'CbNode')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<div class="page-body">
			<div class="blue-box"></div>
			<h2><g:message code="default.show.label" args="[entityName]" /></h2>
			<a href="#show-cbNode" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
			<div class="nav" role="navigation">
				<ul>
					<g:if test="${params.sysid}" >
	 					<li><g:link class="show" controller="SystemEvent" action="show" id="${params.sysid}">SystemEvent anzeigen</g:link></li>
					</g:if>
					<li><g:link class="list" action="index"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
					<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
				</ul>
			</div>
			<div id="show-cbNode" class="content scaffold-show" role="main">
				<g:if test="${flash.message}">
				<div class="message" role="status">${flash.message}</div>
				</g:if>
				<ol class="property-list cbNode">
				
					<g:if test="${cbNodeInstance?.contractors}">
					<li class="fieldcontain">
						<span id="contractors-label" class="property-label"><g:message code="cbNode.contractors.label" default="Contractors" /></span>
						
							<g:each in="${cbNodeInstance.contractors}" var="c">
							<span class="property-value" aria-labelledby="contractors-label"><g:link controller="user" action="show" id="${c.id}">${c?.encodeAsHTML()}</g:link></span>
							</g:each>
						
					</li>
					</g:if>
				
					<g:if test="${cbNodeInstance?.name}">
					<li class="fieldcontain">
						<span id="name-label" class="property-label"><g:message code="cbNode.name.label" default="Name" /></span>
						
							<span class="property-value" aria-labelledby="name-label"><g:fieldValue bean="${cbNodeInstance}" field="name"/></span>
						
					</li>
					</g:if>
				
				</ol>
				<g:form url="[resource:cbNodeInstance, action:'delete']" method="DELETE">
					<fieldset class="buttons">
						<g:actionSubmit class="edit" action="edit" resource="${cbNodeInstance}" value="${message(code: 'default.button.edit.label', default: 'Edit')}" />
					</fieldset>
				</g:form>
			</div>
		</div>
	</body>
</html>
