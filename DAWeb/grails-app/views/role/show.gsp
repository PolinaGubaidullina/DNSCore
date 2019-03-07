
<%@ page import="daweb3.Role" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'role.label', default: 'Role')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<div class="page-body">
			<div class="blue-box"></div>
			<h2><g:message code="default.show.label" args="[entityName]" /></h2>
			<a href="#show-role" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
			<div class="nav" role="navigation">
				<ul>
					<li><g:link class="list" action="index"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
					<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
				</ul>
			</div>
			<div id="show-role" class="content scaffold-show" role="main">
				<g:if test="${flash.message}">
				<div class="message" role="status">${flash.message}</div>
				</g:if>
				<ol class="property-list role">
					<g:if test="${roleInstance?.authority}">
					<li class="fieldcontain">
						<span id="authority-label" class="property-label"><g:message code="role.authority.label" default="Authority" /></span>
						<span class="property-value" aria-labelledby="authority-label"><g:fieldValue bean="${roleInstance}" field="authority"/></span>
					</li>
					</g:if>
				</ol>
				<g:form url="[resource:roleInstance, action:'delete']" method="DELETE">
					<fieldset class="buttons">
<!-- 						<g:link class="edit" action="edit" resource="${roleInstance}"><g:message code="default.button.edit.label" default="Edit" /></g:link> -->
						<g:actionSubmit  class="edit" action="edit" resource="${roleInstance}" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /> 
						<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
					</fieldset>
				</g:form>
			</div>
		</div>
	</body>
</html>
