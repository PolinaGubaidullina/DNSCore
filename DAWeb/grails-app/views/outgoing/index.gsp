<html>
<head>
	<title>Paket entnehmen</title>
	<meta name="layout" content="main">

	
</head>
<g:if test="${msg}">
			<div class="message" role="status">${msg}</div>
			</g:if>
				<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
			</ul>
		</div>
<g:each var="currentFile" in="${filelist}">
     <g:link controller="outgoing" action="download" params="['filename':currentFile.getName()]">${currentFile.getName() }</g:link><br>
</g:each>
</body>