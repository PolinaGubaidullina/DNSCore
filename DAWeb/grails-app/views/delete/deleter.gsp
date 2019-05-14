<html>
	<head>
		<title>Hinweise zum Löschen durch externe Systeme</title>
		<meta name="layout" content="main">
	</head>
	<body>
		<div class="page-body">
			<div id="show-object" class="content scaffold-show" role="main">
				<div class="blue-box"></div>
				<h2>Hinweise zum Löschen</h2>
				<ul class="property-list object">
						
					<li><span class="property-value">
					Sie können das Löschen Ihrer fehlerhaften Objekte auch über externe Systeme initiieren.
					</span></li>
					<li><span class="property-value">
					Nutzen Sie dafür eine URL der folgenden Schemata:<br>
					<b>https://Servername/<g:createLink controller="delete" action="index" params="[urn: 'IhreURN']"/></b><br>
					<b>https://Servername/<g:createLink controller="delete" action="index" params="[origName: 'IhrAblieferungsname']"/></b><br> 
					<b>https://Servername/<g:createLink controller="delete" action="index" params="[identifier: 'IhrIdentifier']"/></b> <br>
					<b>https://Servername/<g:createLink controller="delete" action="index" params="[containerName: 'IhrContainername']"/></b> <br>
					
					</span></li>
				<!--	<li><span class="property-value">Als Antwort erhalten Sie ein maschinenlesbares Ergebnis (JSON), <br></span></li>
					<li><span class="property-value">Sie müssen sich zum Abruf authentifizieren.</span></li> -->	
				</ul>
			</div>
		</div>
	</body>
</html>
