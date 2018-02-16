package de.uzk.hki.da.at;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.reader.BagReader;
import gov.loc.repository.bagit.verify.BagVerifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uzk.hki.da.pkg.BagitUtils;
import de.uzk.hki.da.utils.FolderUtils;

/**
 * 
 * @author Gaby Bender
 *
 */
public class ATSipBuilderNoTar {
	
	private static File targetDir = new File("target/atTargetDir/");
	private static File sourceDir = new File("src/test/resources/at/");
	private static Process p;
	
	@Before
	public void setUp() throws IOException{	
		FolderUtils.deleteDirectorySafe(targetDir);
	}
	
	@After
	public void tearDown() throws IOException{
		FolderUtils.deleteDirectorySafe(targetDir);
		p.destroy();
	}
	
	/**
	 * the sourcefolder contains only one subfolder
	 * @throws IOException
	 */
	@Test
	public void testOneFile() throws IOException {

		File source = new File(sourceDir, "ATSipBuilderNoTar/ATSipBuilderNoTarSingle");
		
 		String cmd = "./SipBuilder-Unix.sh -rights=\""+ATWorkingDirectory.CONTRACT_RIGHT_LICENSED.getAbsolutePath()+"\" -source=\""+source.getAbsolutePath()+ "/\" -destination=\""+
 						targetDir.getAbsolutePath()+"/\" -noTar";
 		
 		p=Runtime.getRuntime().exec(cmd, null, new File("target/installation"));

 		
 		BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

		BufferedReader stdError = new BufferedReader(new
        InputStreamReader(p.getErrorStream()));
		
		String s = "";
		// read the output from the command
	    System.out.println("Here is the standard output of the command:\n");
	    while ((s = stdInput.readLine()) != null) {
	         System.out.println(s);
	    }

	    // read any errors from the attempted command
	    System.out.println("Here is the standard error of the command (if any):\n");
	    while ((s = stdError.readLine()) != null) {
	        System.out.println(s);
	    }
	    
	    File[] directorys = new File(targetDir.getAbsoluteFile() + File.separator + "data").listFiles();
	    File noTarFolder = null;
		if ( directorys.length > 1) {
			System.out.println("ERROR: More than one subfolder found!!!");
		} else {
	    	 noTarFolder = new File(targetDir.getAbsolutePath() + File.separator + "data" + File.separator +  directorys[0].getName());
		}
	    
		assertTrue(BagitUtils.isBagItStyle(noTarFolder));
		assertTrue(new File(noTarFolder + File.separator + "data/NoTar.bmp").exists());
		assertTrue(new File(noTarFolder + File.separator + "data/premis.xml").exists());
				
		assertTrue(validateBagIt(noTarFolder));
	}
	
	/**
	 * the sourcefolder contains multiple subfolder
	 * @throws IOException
	 */
	@Test
	public void testMultipleFiles() throws IOException {

		File source = new File(sourceDir, "ATSipBuilderNoTar/ATSipBuilderNoTarMultiple");
		
 		String cmd = "./SipBuilder-Unix.sh -rights=\""+ATWorkingDirectory.CONTRACT_RIGHT_LICENSED.getAbsolutePath()+"\" -source=\""+source.getAbsolutePath()+"/\" -destination=\""+
 						targetDir.getAbsolutePath()+"/\" -noTar";
 		
 		p=Runtime.getRuntime().exec(cmd, null, new File("target/installation"));

 		
 		BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

		BufferedReader stdError = new BufferedReader(new
        InputStreamReader(p.getErrorStream()));
		
		String s = "";
		// read the output from the command
	    System.out.println("Here is the standard output of the command:\n");
	    while ((s = stdInput.readLine()) != null) {
	         System.out.println(s);
	    }

	    // read any errors from the attempted command
	    System.out.println("Here is the standard error of the command (if any):\n");
	    while ((s = stdError.readLine()) != null) {
	        System.out.println(s);
	    }
	    
	    File noTarFolder = null;
	    File[] directorys = new File(targetDir.getAbsoluteFile() + File.separator + "data").listFiles();
	    
	    for (int i = 0; i < directorys.length; i++ ) {
	    	 String dirName = directorys[i].getName();
	    	 noTarFolder = new File(targetDir.getAbsolutePath() + File.separator +  "data" + File.separator + dirName);
	
	    	 assertTrue(BagitUtils.isBagItStyle(noTarFolder));
			assertTrue(new File(noTarFolder + File.separator + "data/premis.xml").exists());
			if (StringUtils.equals(dirName.trim(), "noTar1")) {
				assertTrue(new File(noTarFolder + File.separator + "data/NoTar1.bmp").exists());
			} else {
				assertTrue(new File(noTarFolder + File.separator + "data/NoTar2.bmp").exists());
			}
					
			assertTrue(validateBagIt(noTarFolder));
	    }
	}
	
	@Test
	public void testDestDir() throws IOException {
			File source = new File(sourceDir, "ATSipBuilderNoTar/ATSipBuilderNoTarDestDir");
		
 		String cmd = "./SipBuilder-Unix.sh -rights=\""+ATWorkingDirectory.CONTRACT_RIGHT_LICENSED.getAbsolutePath()+"\" -source=\""+source.getAbsolutePath()+"/\" -destination=\""+
 						targetDir.getAbsolutePath()+"/\" -noTar" + " -destDir=\"test";
 		
 		p=Runtime.getRuntime().exec(cmd, null, new File("target/installation"));

 		BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

		BufferedReader stdError = new BufferedReader(new
        InputStreamReader(p.getErrorStream()));
		
		String s = "";
		// read the output from the command
	    System.out.println("Here is the standard output of the command:\n");
	    while ((s = stdInput.readLine()) != null) {
	         System.out.println(s);
	    }

	    // read any errors from the attempted command
	    System.out.println("Here is the standard error of the command (if any):\n");
	    while ((s = stdError.readLine()) != null) {
	        System.out.println(s);
	    }

	    File[] directorys = new File(targetDir.getAbsoluteFile() + File.separator + "test" + File.separator + "data").listFiles();

	    File noTarFolder = null;
		if ( directorys.length > 1) {
			System.out.println("ERROR: More than one subfolder found!!!");
		} else {
	    	 noTarFolder = new File(targetDir.getAbsolutePath() + File.separator +  
	    			 		"test" + File.separator + "data" + File.separator + directorys[0].getName());
		}
	    
		System.out.println("### noTarFolder: " + noTarFolder);
		
		assertTrue(BagitUtils.isBagItStyle(noTarFolder));
		assertTrue(new File(noTarFolder + File.separator + "data/NoTar.bmp").exists());
		assertTrue(new File(noTarFolder + File.separator + "data/premis.xml").exists());
				
		assertTrue(validateBagIt(noTarFolder));
		
		
	}
	
	/**
	 * @param folder The folder to check
	 * @return true if the BagIt metadata is valid, otherwise false
	 */
	private boolean validateBagIt(File folder) {
		BagReader reader = new BagReader();
		
		Bag bagVer;
		BagVerifier sut = new BagVerifier();
		try {
			bagVer = reader.read(Paths.get(folder.toURI()));
			sut.isValid(bagVer, false);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} 
		return true;
	}
	
}
