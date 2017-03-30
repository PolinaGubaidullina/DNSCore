/*
 DA-NRW Software Suite | ContentBroker
 Copyright (C) 2013 Historisch-Kulturwissenschaftliche Informationsverarbeitung
 Universität zu Köln

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.uzk.hki.da.at;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uzk.hki.da.model.Object;
import de.uzk.hki.da.utils.Path;

/**
 * Relates to AK-T/02 Ingest - Sunny Day Szenario (mit besonderen Bedingungen).
 * @author Daniel M. de Oliveira
 * @author Trebunski Eugen
 */
public class ATUseCaseIngestSpecialCases extends AcceptanceTest{

	@BeforeClass
	public static void setUp() throws IOException {
		ath.putSIPtoIngestArea("ATÜÄÖ","tgz","ATÜÄÖ");
		ath.putSIPtoIngestArea("ATSonderzeichen_in_Dateinamen","tgz","ATSonderzeichen_in_Dateinamen");
		ath.putSIPtoIngestArea("ATUmlaute_in_Dateinamen","tgz","ATUmlaute_in_Dateinamen");
		ath.putSIPtoIngestArea("AT_CON1","tar","AT_CON1");
		ath.putSIPtoIngestArea("AT_CON2","tgz","AT_CON2");
		ath.putSIPtoIngestArea("AT_CON3","zip","AT_CON3");
		ath.putSIPtoIngestArea("AT&Sonderzeichen%in#Paketnamen","tgz","AT&Sonderzeichen%in#Paketnamen");
		ath.putSIPtoIngestArea("ATLeerzeichen_in_Dateinamen","tgz","ATLeerzeichen_in_Dateinamen");
	}
	
	@AfterClass
	public static void tearDown(){
		
//		FolderUtils.deleteQuietlySafe(Path.make(localNode.getWorkAreaRootPath(),"/work/TEST/"+object.getIdentifier()).toFile());

		Path.make(localNode.getIngestAreaRootPath(),"/"+testContractor.getUsername()+"/AT_CON1.tar").toFile().delete();
		Path.make(localNode.getIngestAreaRootPath(),"/"+testContractor.getUsername()+"/AT_CON2.tgz").toFile().delete();
		Path.make(localNode.getIngestAreaRootPath(),"/"+testContractor.getUsername()+"/AT_CON3.zip").toFile().delete();
		
		
	}
	
	@Test
	public void testUmlautsInPackageName() throws Exception{
		ath.awaitObjectState("ATÜÄÖ",Object.ObjectStatus.ArchivedAndValidAndNotInWorkflow);
	}
	
	@Test
	public void testSpecialCharactersInFileNames() throws Exception{
		ath.awaitObjectState("ATSonderzeichen_in_Dateinamen",Object.ObjectStatus.ArchivedAndValidAndNotInWorkflow);
	}
	
	@Test
	public void testUmlautsInFileNames() throws Exception{
		ath.awaitObjectState("ATUmlaute_in_Dateinamen",Object.ObjectStatus.ArchivedAndValidAndNotInWorkflow);
	}
	
	@Test
	public void testTARContainer() throws Exception{
		ath.awaitObjectState("AT_CON1",Object.ObjectStatus.ArchivedAndValidAndNotInWorkflow);
	}
	
	@Test
	public void testTGZContainer() throws Exception{
		ath.awaitObjectState("AT_CON2",Object.ObjectStatus.ArchivedAndValidAndNotInWorkflow);
	}
	
	@Test
	public void testZIPContainer() throws Exception{
		ath.awaitObjectState("AT_CON3",Object.ObjectStatus.ArchivedAndValidAndNotInWorkflow);
	}
	
	@Test
	public void testSpecialCharsInPackageName() throws Exception{
		ath.awaitObjectState("AT&Sonderzeichen%in#Paketnamen",Object.ObjectStatus.ArchivedAndValidAndNotInWorkflow);
	}
	
	@Test
	public void testWhiteSpacesInFileNames() throws Exception{
		ath.awaitObjectState("ATLeerzeichen_in_Dateinamen",Object.ObjectStatus.ArchivedAndValidAndNotInWorkflow);
	}
}
