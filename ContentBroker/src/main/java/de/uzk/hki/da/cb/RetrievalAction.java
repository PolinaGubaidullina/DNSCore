/*
  DA-NRW Software Suite | ContentBroker
  Copyright (C) 2013 Historisch-Kulturwissenschaftliche Informationsverarbeitung
  Universität zu Köln
  Copyright (C) 2014 LVRInfoKom
  Landschaftsverband Rheinland

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

package de.uzk.hki.da.cb;

import static de.uzk.hki.da.utils.C.FILE_EXTENSION_TAR;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import de.uzk.hki.da.action.AbstractAction;
import de.uzk.hki.da.core.MailContents;
import de.uzk.hki.da.model.DAFile;
import de.uzk.hki.da.model.Package;
import de.uzk.hki.da.model.WorkArea;
import de.uzk.hki.da.pkg.ArchiveBuilder;
import de.uzk.hki.da.pkg.ArchiveBuilderFactory;
import de.uzk.hki.da.pkg.BagitUtils;
import de.uzk.hki.da.utils.C;
import de.uzk.hki.da.utils.FolderUtils;
import de.uzk.hki.da.utils.Path;



/**
 * Generates a DIP based on the objects content and copies it to the user's outgoing folder.
 * 
 * There are two retrieval modes.
 * <ol>
 * <li>Normally the DIP will consist of just the surface representation of the package.
 * <li>In the second mode the DIP will consist of the contents of selected packages.
 * job.question has contain a string like RETRIEVE:1,2 to retrieve the packages with 
 * package.name==1 and package.name==2.
 * </ol>
 * @author Jens Peters
 * @author Daniel M. de Oliveira
 * @author Thomas Kleinke
 */

public class RetrievalAction extends AbstractAction {

	private Path newTar;

	@Override
	public void checkConfiguration() {
	}
	

	@Override
	public void checkPreconditions() {
		if (!wa.objectPath().toFile().exists()) throw new IllegalStateException("object data path on fs doesn't exist on fs");
		if (!wa.toFile(o.getLatest(C.PREMIS_XML)).exists()) throw new RuntimeException("CRITICAL ERROR: premis file could has not been found");
	}
	
	@Override
	public boolean implementation() throws IOException {
		
		newTar = Path.make(n.getUserAreaRootPath(),o.getContractor().getShort_name(),"outgoing",o.getIdentifier() + FILE_EXTENSION_TAR);
		Path tempFolder = createTmpFolder();
		

		if (j.getQuestion()==null||j.getQuestion().isEmpty()){
			stdRetrieval(tempFolder);
		}
		else if (j.getQuestion().startsWith("RETRIEVE:")){
			specialRetrieval(tempFolder);
		}
		
		
		bagitAndTarit(tempFolder);

		cleanupFS();
		o.setObject_state(100);
		new MailContents(preservationSystem,n).retrievalReport(o);
		clearNonpersistentObjectProperties(o);
		return true;
	}

	
	
	
	@Override
	public void rollback() {
		
		newTar.toFile().delete();
	}
	
	
	
	
	private void stdRetrieval(Path tempFolder) throws IOException{
		moveNewestPremisToDIP(tempFolder);
		copySurfaceRepresentation(tempFolder);
	}




	private void specialRetrieval(Path tempFolder) throws IOException {

		for (Package p:packagesToRetrieve()){

			for (DAFile f:p.getFiles()){
			
				File destDir = Path.makeFile(tempFolder,WorkArea.DATA,f.getRep_name(),FilenameUtils.getPath(f.getRelative_path()));
				destDir.mkdirs();
				FileUtils.copyFileToDirectory(wa.toFile(f), destDir);
			}
		}
	}

	
	
	
	private Set<Package> packagesToRetrieve(){
		
		String pp[] = j.getQuestion().replace("RETRIEVE:","").split(",");
		Set<Package> packagesToRetrieve = new HashSet<Package>(); 
		
		for (int i=0;i<pp.length;i++){
			
			for (Package p_:o.getPackages()){
				if (p_.getDelta().toString().equals(pp[i]))
					packagesToRetrieve.add(p_);
			}
		}
		
		return packagesToRetrieve;
	}
	




	private void moveNewestPremisToDIP(Path tempFolder) throws IOException {
		File dest = Path.makeFile(tempFolder,WorkArea.DATA,C.PREMIS_XML);
		FileUtils.copyFile(wa.toFile(o.getLatest(C.PREMIS_XML)), dest);
	}




	private Path createTmpFolder(){
		Path tmpFolder = Path.make(n.getWorkAreaRootPath(),WorkArea.WORK,
				o.getContractor().getShort_name(), o.getIdentifier(), o.getIdentifier()); 
		tmpFolder.toFile().mkdir();
		return tmpFolder;
	}




	private void bagitAndTarit(Path tempFolder){

		BagitUtils.buildBagit(tempFolder.toString());
		
		logger.debug("Building tar at " + newTar);
		try {
			ArchiveBuilder builder = ArchiveBuilderFactory.getArchiveBuilderForFile(new File(FILE_EXTENSION_TAR));
			builder.archiveFolder(tempFolder.toFile(),
							  newTar.toFile(), true);
		} catch (Exception e) {
			throw new RuntimeException("Tar couldn't be packed", e);
		} 
	}
	
	
	private void cleanupFS() throws IOException{
		
		// cleanup
		
		FolderUtils.deleteDirectorySafe(wa.objectPath().toFile());
	}
	
	
	
	/**
	 * @param tempFolder
	 * @throws RuntimeException
	 * @throws IOException 
	 */
	private void copySurfaceRepresentation(Path tempFolder)
			throws RuntimeException, IOException {
		
		String sce = "";
		if (o.getFriendlyFileExtensions()!=null) 
			sce = o.getFriendlyFileExtensions();
		
		List<DAFile> files = o.getNewestFilesFromAllRepresentations(sce);
		for (DAFile f : files)
		{
			if (wa.toFile(f).getName().equals(C.PREMIS_XML)) continue;
				
			File dest = Path.makeFile(tempFolder,WorkArea.DATA,f.getRelative_path());
			logger.info("file will be part of pip: "+dest.getAbsolutePath());
			String destFolder = dest.getAbsolutePath().substring(0, dest.getAbsolutePath().lastIndexOf("/"));

			new File(destFolder).mkdirs();
			FileUtils.copyFile(wa.toFile(f), dest);
		}
	}
}
