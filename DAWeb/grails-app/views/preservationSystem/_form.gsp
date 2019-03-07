<%@ page import="daweb3.PreservationSystem" %>

<div class="fieldcontain ${hasErrors(bean: preservationSystemInstance, field: 'minRepls', 'error')} required">
	<label for="minRepls">
		<g:message code="preservationSystem.minRepls.label" default="Min Repls" />
		<span class="required-indicator">*</span>
	</label>
	<g:field name="minRepls" type="number" value="${preservationSystemInstance.minRepls}" required=""/>
</div>

<div class="fieldcontain ${hasErrors(bean: preservationSystemInstance, field: 'closedCollectionName', 'error')} ">
	<label for="closedCollectionName">
		<g:message code="preservationSystem.closedCollectionName.label" default="Closed Collection Name" />
		
	</label>
	<g:textField name="closedCollectionName" value="${preservationSystemInstance?.closedCollectionName}" class="input-hoehe"/>
</div>

<div class="fieldcontain ${hasErrors(bean: preservationSystemInstance, field: 'openCollectionName', 'error')} ">
	<label for="openCollectionName">
		<g:message code="preservationSystem.openCollectionName.label" default="Open Collection Name" />
	</label>
	<g:textField name="openCollectionName" value="${preservationSystemInstance?.openCollectionName}" class="input-hoehe"/>
</div>

<div class="fieldcontain ${hasErrors(bean: preservationSystemInstance, field: 'presServer', 'error')} ">
	<label for="presServer">
		<g:message code="preservationSystem.presServer.label" default="Pres Server" />
	</label>
	<g:textField name="presServer" value="${preservationSystemInstance?.presServer}" class="input-hoehe"/>
</div>

<div class="fieldcontain ${hasErrors(bean: preservationSystemInstance, field: 'urisAggr', 'error')} ">
	<label for="urisAggr">
		<g:message code="preservationSystem.urisAggr.label" default="Uris Aggr" />
	</label>
	<g:textField name="urisAggr" value="${preservationSystemInstance?.urisAggr}" class="input-hoehe"/>
</div>

<div class="fieldcontain ${hasErrors(bean: preservationSystemInstance, field: 'urisCho', 'error')} ">
	<label for="urisCho">
		<g:message code="preservationSystem.urisCho.label" default="Uris Cho" />
	</label>
	<g:textField name="urisCho" value="${preservationSystemInstance?.urisCho}" class="input-hoehe"/>
</div>

<div class="fieldcontain ${hasErrors(bean: preservationSystemInstance, field: 'urisFile', 'error')} ">
	<label for="urisFile">
		<g:message code="preservationSystem.urisFile.label" default="Uris File" />
		
	</label>
	<g:textField name="urisFile" value="${preservationSystemInstance?.urisFile}" class="input-hoehe"/>
</div>

<div class="fieldcontain ${hasErrors(bean: preservationSystemInstance, field: 'urnNameSpace', 'error')} ">
	<label for="urnNameSpace">
		<g:message code="preservationSystem.urnNameSpace.label" default="Urn Name Space" />
	</label>
	<g:textField name="urnNameSpace" value="${preservationSystemInstance?.urnNameSpace}" class="input-hoehe"/>
</div>
