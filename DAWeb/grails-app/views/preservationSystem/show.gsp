
<%@ page import="daweb3.PreservationSystem" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'preservationSystem.label', default: 'Bestandserhaltungs-System')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<div class=page-body>
			<a href="#show-preservationSystem" class="skip" tabindex="-1">
				<g:message code="default.link.skip.label" default="Skip to content&hellip;"/>
			</a>
			<div id="show-preservationSystem" class="content scaffold-show" role="main">
				<div class="blue-box"></div>
				<h2><g:message code="default.show.label" args="[entityName]" /></h2>
				<div class="nav" role="navigation">
					<ul>
						<li>
							<g:link class="list" action="index"><g:message code="default.list.label" args="[entityName]" /></g:link>
						</li>
					</ul>
				</div>
				<g:if test="${flash.message}">
				<div class="message" role="status">${flash.message}</div>
				</g:if>
				<ol class="property-list preservationSystem">
					<g:if test="${preservationSystemInstance?.minRepls}">
					<li class="fieldcontain">
						<span id="minRepls-label" class="property-label">
							<g:message code="preservationSystem.minRepls.label" default="Min Repls" />
						</span>
						<span class="property-value" aria-labelledby="minRepls-label">
							<g:fieldValue bean="${preservationSystemInstance}" field="minRepls"/>
						</span>
					</li>
					</g:if>
					<g:if test="${preservationSystemInstance?.closedCollectionName}">
					<li class="fieldcontain">
						<span id="closedCollectionName-label" class="property-label">
							<g:message code="preservationSystem.closedCollectionName.label" default="Closed Collection Name" />
						</span>
						<span class="property-value" aria-labelledby="closedCollectionName-label">
							<g:fieldValue bean="${preservationSystemInstance}" field="closedCollectionName"/>
						</span>
					</li>
					</g:if>
					<g:if test="${preservationSystemInstance?.openCollectionName}">
					<li class="fieldcontain">
						<span id="openCollectionName-label" class="property-label">
							<g:message code="preservationSystem.openCollectionName.label" default="Open Collection Name" />
						</span>
							<span class="property-value" aria-labelledby="openCollectionName-label">
								<g:fieldValue bean="${preservationSystemInstance}" field="openCollectionName"/>
							</span>
					</li>
					</g:if>
					<g:if test="${preservationSystemInstance?.presServer}">
					<li class="fieldcontain">
						<span id="presServer-label" class="property-label">
							<g:message code="preservationSystem.presServer.label" default="Pres Server" />
						</span>
						<span class="property-value" aria-labelledby="presServer-label">
							<g:fieldValue bean="${preservationSystemInstance}" field="presServer"/>
						</span>
					</li>
					</g:if>
					<g:if test="${preservationSystemInstance?.urisAggr}">
					<li class="fieldcontain">
						<span id="urisAggr-label" class="property-label">
							<g:message code="preservationSystem.urisAggr.label" default="Uris Aggr" />
						</span>
						<span class="property-value" aria-labelledby="urisAggr-label">
							<g:fieldValue bean="${preservationSystemInstance}" field="urisAggr"/>
						</span>
					</li>
					</g:if>
					<g:if test="${preservationSystemInstance?.urisCho}">
					<li class="fieldcontain">
						<span id="urisCho-label" class="property-label">
							<g:message code="preservationSystem.urisCho.label" default="Uris Cho" />
						</span>
						<span class="property-value" aria-labelledby="urisCho-label">
							<g:fieldValue bean="${preservationSystemInstance}" field="urisCho"/>
						</span>
					</li>
					</g:if>
					<g:if test="${preservationSystemInstance?.urisFile}">
					<li class="fieldcontain">
						<span id="urisFile-label" class="property-label">
							<g:message code="preservationSystem.urisFile.label" default="Uris File" />
						</span>
						<span class="property-value" aria-labelledby="urisFile-label">
							<g:fieldValue bean="${preservationSystemInstance}" field="urisFile"/>
						</span>
					</li>
					</g:if>
					<g:if test="${preservationSystemInstance?.urnNameSpace}">
					<li class="fieldcontain">
						<span id="urnNameSpace-label" class="property-label">
							<g:message code="preservationSystem.urnNameSpace.label" default="Urn Name Space" /></span>
						<span class="property-value" aria-labelledby="urnNameSpace-label"><g:fieldValue bean="${preservationSystemInstance}" field="urnNameSpace"/></span>
					</li>
					</g:if>
				</ol>
				<g:form url="[resource:preservationSystemInstance, action:'delete']" method="DELETE">
					<fieldset class="buttons">
						<g:actionSubmit class="edit" action="edit" resource="${conversionPoliciesInstance}" value="${message(code: 'default.button.edit.label',default: 'Edit')}"/>
					</fieldset>
				</g:form>
			</div>
		</div>
	</body>
</html>
