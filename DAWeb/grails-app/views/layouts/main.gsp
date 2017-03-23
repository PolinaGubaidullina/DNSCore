<!doctype html>
<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>    <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>    <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>    <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!-->
<html lang="en" class="no-js">
<!--<![endif]-->
	<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
	<title>
		<g:layoutTitle default="Grails" />
	</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	
		<link rel="shortcut icon" href="${resource(dir: 'images', file: 'favicon.ico')}" type="image/x-icon">
		
		<link rel="apple-touch-icon" href="${resource(dir: 'images', file: 'apple-touch-icon.png')}">
		
		<link rel="apple-touch-icon" sizes="114x114" href="${resource(dir: 'images', file: 'apple-touch-icon-retina.png')}">
		
		<link rel="stylesheet" href="${resource(dir: 'css', file: grailsApplication.config.main.css)}" type="text/css">
		
		<link rel="stylesheet" href="${resource(dir: 'css', file: grailsApplication.config.mobile.css)}" type="text/css">
		
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'bootstrap.css')}" type="text/css">
		
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'bootstrap.min.css')}" type="text/css">
		
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'bootstrap-theme.min.css')}" type="text/css">
		
		<script src="${resource(dir: 'js', file: 'jquery-3.1.1.min.js')}" type="text/javascript"></script>
		
		<script src="${resource(dir: 'js', file: 'bootstrap.min.js')}" type="text/javascript"></script>
		
		
	<g:layoutHead />
	<r:require modules="jquery, application" />
	<r:layoutResources />
	
	</head>
	<body>
	
		<div id="header" role="banner">
			<div class="container-fluid">
				<div class="row">
					<div class="col-xs-12 col-sm-6 col-md-8">
						<g:link controller="home">
							<img class="img-logo" src="${resource(dir: 'images', file: grailsApplication.config.daweb3.logo)}" alt="Grails" />
						</g:link>
						<h3>Web Konsole</h3>
					</div>
					<div class="col-xs-6 col-sm-4 hidden-xs">
						<img class="pull-right img-right" src="${resource(dir: 'images', file: 'DANRW_P_OBEN.png')}" width="150" height="45" />
					</div>
				</div>
			</div>
			<div id="header-menu">
				<form name="submitForm" method="POST" action="${createLink(controller: 'logout')}">
					<input type="hidden" name="" value=""> 
						<a HREF="javascript:document.submitForm.submit()">Logout</a>
				</form>
			</div>
		</div>
		<div id="page-body">
			<g:layoutBody />
			
		</div>
		<div align="center">
			<div class="footer" id="page-footer" role="contentinfo">
				<g:meta name="app.name" />
				Build:
				<g:meta name="app.version.buildNumber" />
				, LVR-InfoKom (ab 2014). HKI, Universität zu Köln 2011-2014.
				<g:if test="${grailsApplication.config.provider.logo}">
					<img src="${resource(dir: 'images', file: grailsApplication.config.provider.logo)}" alt="Provider-Logo" />
				</g:if>
			</div>
		</div>
		<div id="spinner" class="spinner" style="display: none;">
			<g:message code="spinner.alt" default="Loading&hellip;" />
		</div>
		<r:layoutResources />
	</body>
</html>
