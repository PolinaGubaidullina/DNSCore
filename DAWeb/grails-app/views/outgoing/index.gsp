<html>
	<head>
		<title>Paket entnehmen</title>
		<meta name="layout" content="main">
	
		
	</head>
	<body>
		<g:if test="${msg}">
			<div class="message" role="status">${msg}</div>
		</g:if>
		
		<div style="margin: 0.8em 0 0.3em">
			<g:each var="currentFile" in="${filelist}">
				<g:link controller="outgoing" action="download" params="['filename':currentFile.getName()]">${currentFile.getName() }</g:link><br>
			</g:each>
		</div>
	</body>
</html>
