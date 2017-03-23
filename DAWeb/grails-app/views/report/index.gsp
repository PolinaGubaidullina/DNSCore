<html>
	<head>
		<title>Bericht verarbeiten</title>
		<meta name="layout" content="main">
		 <r:require modules="periodicalupdater, jqueryui"/>
			<r:script>
				$(function() {
					$("#legend").accordion({ collapsible: true, active: false, autoHeight: false });
				});
				$(function() {
					$("#filter").accordion({ collapsible: true, active: false });
				});
				
				$.PeriodicalUpdater("./snippetIncoming",
					{
						method: "get",
						minTimeout: 3000,
						maxTimeout: 3000,
						success: function(data) {
							$("#entry-list1").html(data);
						}
					}
				);
				$.PeriodicalUpdater("./snippetOutgoing",
					{
						method: "get",
						minTimeout: 1000,
						maxTimeout: 1000,
						success: function(data) {
							$("#entry-list2").html(data);
						}
					}
				);
			</r:script>
	</head>
	<body>
		<g:if test="${msg}">
			<div class="message" role="status">${msg}</div>
		</g:if>
		
		<div id="items">	
			<h2>Bericht hochladen</h2>
			<g:form controller="report" method="POST" action="save" enctype="multipart/form-data">
				<input type="file" name="file"/>
				<input type="submit" value="Hochladen" />
			</g:form><br>
			(Spaltenkopf: identifier;origName;status;bemerkung; semikolongetrennt, EXCEL)	
			<p>
			
			<script language="JavaScript">
			function toggle(source) {
				  checkboxes = document.getElementsByName('currentFiles');
				  for(var i in checkboxes)
				    checkboxes[i].checked = source.checked;
			}
			</script>
			
			<h2>Wartend auf Aktion</h2>	
			<form id="form2" action="decider" >
				<!-- This div is updated through the periodical updater -->
				<div class="list" id="entry-list1">
					<g:include action="snippetIncoming" />
				</div>
				<g:select name="answer" from="${['start': 'Bericht generieren', 'retrieval': 'Retrieval']}" optionKey="key" optionValue="value"/>
				<g:actionSubmit value="Starten" action="decider"/>
			</form>
			<h2>Bereits erstellte Berichte:</h2>
			<!-- This div is updated through the periodical updater -->
			<div class="list" id="entry-list2">
				<g:include action="snippetOutgoing" />
			</div>
		</div>
	</body>
</html>