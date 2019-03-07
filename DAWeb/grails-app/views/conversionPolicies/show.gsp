
<%@ page import="daweb3.ConversionPolicies" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'conversionPolicies.label', default: 'Konfigurierte Konversionen')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<div class="page-body">
			
			
			<div id="show-conversionPolicies" class="content scaffold-show" role="main">
				<div class="blue-box"></div>
				<h2><g:message code="default.show.label" args="[entityName]" /></h2>
				<a href="#show-conversionPolicies" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
				<g:if test="${admin}">
					<div class="nav" role="navigation">
						<ul>
							<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
						</ul>
					</div>
				</g:if>
				<g:if test="${flash.message}">
				<div class="message" role="status">${flash.message}</div>
				</g:if>
				<ol class="property-list conversionPolicies">
				
					<g:if test="${conversionPoliciesInstance?.id}">
					<li class="fieldcontain">
						<span id="contractor-label" class="property-label"><g:message code="conversionPolicies.contractor.label" default="id" /></span>
						<span class="property-value" aria-labelledby="contractor-label">${conversionPoliciesInstance?.id?.encodeAsHTML()}</span>
					</li>
					</g:if>
				
					<g:if test="${conversionPoliciesInstance?.conversion_routine}">
					<li class="fieldcontain">
						<span id="conversion_routine-label" class="property-label"><g:message code="conversionPolicies.conversion_routine.label" default="Konversionroutine" /></span>
						<span class="property-value" aria-labelledby="conversion_routine-label">${conversionPoliciesInstance?.conversion_routine?.encodeAsHTML()}</span>
					</li>
					</g:if>
				
					<g:if test="${conversionPoliciesInstance?.source_format}">
					<li class="fieldcontain">
						<span id="source_format-label" class="property-label"><g:message code="conversionPolicies.source_format.label" default="Quellformat" /></span>
						<span class="property-value" aria-labelledby="source_format-label">
							<g:if test="${extension}">
								${extension}:  <g:fieldValue bean="${conversionPoliciesInstance}" field="source_format"/>
							</g:if>
							<g:else>
								 <g:fieldValue bean="${conversionPoliciesInstance}" field="source_format"/>
							</g:else>
						</span>
					</li>
					</g:if>
						<g:if test="${conversionPoliciesInstance?.conversion_routine?.target_suffix}">
					<li class="fieldcontain">
						<span id="source_format-label" class="property-label"><g:message code="conversionPolicies.conversion_routine.target_suffix.label" default="Zielformat" /></span>
						<span class="property-value" aria-labelledby="source_format-label">${conversionPoliciesInstance?.conversion_routine?.target_suffix?.encodeAsHTML()}</span>
						</li>
					</g:if>
					
					<li class="fieldcontain">
						<span id="presentation-label" class="property-label"><g:message code="conversionPolicies.presentation.label" default="Presentation Repository Policy" /></span>
						<span class="property-value" aria-labelledby="contractor-label">
						<g:checkBox name="presentation" value="${conversionPoliciesInstance?.presentation}" disabled="true"/>
						</span>
					</li>
				</ol>
				<g:form>
				
					<fieldset class="buttons">
						<g:if test="${adminAllg}">
							<!--  <g:link class="edit" action="edit" resource="${conversionPoliciesInstance}"><g:message code="default.button.edit.label" default="Edit" /></g:link> -->
							<g:actionSubmit class="edit" action="edit" resource="${conversionPoliciesInstance}" value="${message(code: 'default.button.edit.label',default: 'Edit')}"/>
							<g:actionSubmit class="delete" action="delete" 	value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
						</g:if>
						<g:hiddenField name="id" value="${conversionPoliciesInstance?.id}" />
					</fieldset>
				</g:form>
			</div>
		</div>
	</body>
</html>
