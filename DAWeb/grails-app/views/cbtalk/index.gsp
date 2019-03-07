<%@ page contentType="text/html; charset=UTF-8" %>
<html>
  <head>
    <meta name="layout" content="main" />
    <title>Administrative Funktionen</title>         
  </head>
  <r:require modules="periodicalupdater, jqueryui"/>
		<g:javascript>
			$(function() {
				$("#legend").accordion({ collapsible: true, active: false, autoHeight: false });
			});
			$(function() {
				$("#filter").accordion({ collapsible: true, active: false });
			});
			
			$.PeriodicalUpdater("./messageSnippet",
				{
					method: "get",
					minTimeout: 10000,
					maxTimeout: 10000,
					success: function(data) {
						$("#entry-list").html(data);
					}
				}
			);
		</g:javascript>
  <body>
	<div class="page-body">
		<div class="blue-box"></div>
		<h2>Adminfunktionen</h2>
  		<div class="nav" role="navigation">
			<ul>
				<li><a class="list" href="${createLink(controller: 'conversionPolicies', action: 'index')}">Konfigurierte Konversionen</a></li>
				<li><a class="list" href="${createLink(controller: 'user', action: 'index')}">Benutzer</a></li>
				<li><a class="list" href="${createLink(controller: 'role', action: 'index')}">Rollen</a></li>
				<li><a class="list" href="${createLink(controller: 'userRole', action: 'index')}">Benutzer-Rollen</a></li>
				<li><a class="list" href="${createLink(controller: 'PreservationSystem', action: 'index')}">Bestandserhaltungs-System</a></li>
				<li><a class="list" href="${createLink(controller: 'formatMapping', action: 'map')}">Format-Mapping</a></li>
			</ul>
		</div>
   
	   	<h3>CbTalk</h3>
	   	<g:if test="${flash.message}">
	    	<div class="message">${flash.message}</div>
	   	</g:if>
	   	<g:form action="save" method="post" class="abstand-unten admin-fkt-form">
			<g:submitButton name="stopFactory" value="stop Factory" />
			<g:submitButton name="startFactory" value="start Factory" /> 
			<g:submitButton name="showActions" value="show Actions" /> 
			<g:submitButton name="gracefulShutdown" value="ContentBroker graceful shutdown" />  
			<g:submitButton name="showVersion" value="Show version of ContentBroker" />   
			<g:submitButton name="stopDelayed" value="stop delayed Rules" />  
			<g:submitButton name="startDelayed" value="start delayed Rules" />   
		</g:form>
		Rückmeldungen des ContentBroker (können verzögert eintreffen)
		<!-- This div is updated through the periodical updater -->
		<div id="entry-list">
			<g:include action="messageSnippet" />
		</div>
	</div>
  </body>
</html>