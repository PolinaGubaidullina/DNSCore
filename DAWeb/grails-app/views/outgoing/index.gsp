<html>
<head>
	<title>Paket entnehmen</title>
	<meta name="layout" content="main">
</head>
	<g:if test="${msg}">
		<div class="message" role="status">${msg}</div>
	</g:if>
	
	<g:each var="currentFile" in="${filelist}">
    	<g:link controller="outgoing" action="webdav" params="['filename':currentFile.getName()]">${currentFile.getName() }</g:link><br>
	</g:each>
</body>