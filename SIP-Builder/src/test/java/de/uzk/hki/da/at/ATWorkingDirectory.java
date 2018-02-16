package de.uzk.hki.da.at;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uzk.hki.da.pkg.ArchiveBuilder;
import de.uzk.hki.da.pkg.ArchiveBuilderFactory;
import de.uzk.hki.da.pkg.BagitUtils;
import de.uzk.hki.da.utils.FolderUtils;

public class ATWorkingDirectory {
	public static final File CONTRACT_RIGHT_LICENSED = new File("target/installation/conf" + File.separator + "standardRightsLicense.xml"); //standardRightsLicense.xml   standardRights.xml
	public static final File CONTRACT_RIGHT_NON_LICENSED = new File("target/installation/conf" + File.separator + "standardRightsNonLicense.xml"); //standardRightsLicense.xml   standardRights.xml
	
	private static String sip1 = "ATWorkingDirectory.tgz";
	
	private File s1 = new File("target/atTargetDir/"+sip1);
	private File unpackedSip1 = new File("target/atTargetDir/"+ FilenameUtils.removeExtension(s1.getName()));
	
	private static File targetDir = new File("target/atTargetDir/");
	private static File workDir = new File("target/atTargetDirWorking/");
	
	private static File sourceDir = new File("src/test/resources/at/ATWorkingDirectory");
	
	private static Process p;
	
	@Before
	public void setUp() throws IOException{	
		FolderUtils.deleteDirectorySafe(new File("target/atTargetDir/"));
		FolderUtils.deleteDirectorySafe(new File("target/atTargetDirWorking"));
	}
	
	@After
	public void tearDown() throws IOException{
		FolderUtils.deleteQuietlySafe(new File("target/atTargetDir/"+sip1));;
		FolderUtils.deleteDirectorySafe(s1);
		FolderUtils.deleteDirectorySafe(targetDir);
		p.destroy();
	}
	
	@Test
	public void test() throws IOException {
		
		String cmd = "./SipBuilder-Unix.sh -rights=\""+ATWorkingDirectory.CONTRACT_RIGHT_LICENSED.getAbsolutePath()+"\" -source=\""+sourceDir.getAbsolutePath()+"/\" -destination=\""+targetDir.getAbsolutePath()+"/\" -workspace=\""+workDir.getAbsolutePath()+"/\" -single";
		
		p=Runtime.getRuntime().exec(cmd,
		        null, new File("target/installation"));
		
		BufferedReader stdInput = new BufferedReader(new
        InputStreamReader(p.getInputStream()));

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
	    
	    
	    assertTrue(new File("target/atTargetDir/"+sip1).exists());
	    
	    assertFalse(new File(workDir +sip1).exists());
	    

//	    Tests content of the first SIP
	    ArchiveBuilder builder = ArchiveBuilderFactory.getArchiveBuilderForFile(s1); 
	    
	    try {
			builder.unarchiveFolder(s1, targetDir);
		} catch (Exception e) {
			throw new RuntimeException("couldn't unpack archive", e);
		}

	    assertTrue(BagitUtils.isBagItStyle(unpackedSip1));
		assertTrue(new File(unpackedSip1, "data/export_mets.xml").exists());
		assertTrue(new File(unpackedSip1, "data/image").exists() && new File(unpackedSip1, "data/image").isDirectory());
		assertTrue(new File(unpackedSip1, "data/premis.xml").exists());
	    
		for(File f : new File (unpackedSip1, "data/image").listFiles()) {
			assertTrue(f.getName().endsWith(".bmp"));
		}
		assertTrue(new File (unpackedSip1, "data/image").listFiles().length==29);
	}

	@Test
	public void testNeverOverwrite() throws IOException {
		String cmd = "./SipBuilder-Unix.sh -rights=\""+CONTRACT_RIGHT_LICENSED.getAbsolutePath()+"\" -source=\""+sourceDir.getAbsolutePath()+"/\" -destination=\""+targetDir.getAbsolutePath()+"/\" -workspace=\""+workDir.getAbsolutePath()+"/\" -single";
		new File("target/atTargetDir/").mkdirs();
		File targetSip = new File("target/atTargetDir/"+sip1); 
		targetSip.createNewFile();
		
		p=Runtime.getRuntime().exec(cmd,
		        null, new File("target/installation"));
		
		BufferedReader stdInput = new BufferedReader(new
        InputStreamReader(p.getInputStream()));

		BufferedReader stdError = new BufferedReader(new
        InputStreamReader(p.getErrorStream()));
		 
		String s = "";
		// read the output from the command
	    System.out.println("Here is the standard output of the command:\n");
	    boolean alwaysOverFound = false;
	    while ((s = stdInput.readLine()) != null) {
	         System.out.println(s);
	         if (s.contains(
	        		"Bereits existierende SIPs wurden nicht neu erstellt." + 
	        		" Starten Sie den SIP-Builder mit der Option -alwaysOverwrite")){
	        	 alwaysOverFound = true;
	         }
	    }

	    // read any errors from the attempted command
	    System.out.println("Here is the standard error of the command (if any):\n");
	    while ((s = stdError.readLine()) != null) {
	        System.out.println(s);
	    }
	    		
	    assertTrue(alwaysOverFound);
	    assertTrue(targetSip.length() == 0);
	}

	@Test
	public void testAlwaysOverwrite() throws IOException {
		String cmd = "./SipBuilder-Unix.sh -rights=\""+ATWorkingDirectory.CONTRACT_RIGHT_LICENSED.getAbsolutePath()+"\" -source=\""+sourceDir.getAbsolutePath()+"/\" -destination=\""+targetDir.getAbsolutePath()+"/\" -workspace=\""+workDir.getAbsolutePath()
				+"/\" -single -alwaysOverwrite";
		new File("target/atTargetDir/").mkdirs();
		File targetSip = new File("target/atTargetDir/"+sip1); 
		targetSip.createNewFile();
		
		p=Runtime.getRuntime().exec(cmd,
		        null, new File("target/installation"));
		
		BufferedReader stdInput = new BufferedReader(new
        InputStreamReader(p.getInputStream()));

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
	    		
	    assertTrue(new File("target/atTargetDir/"+sip1).exists());
	    
	    assertFalse(new File(workDir +sip1).exists());
	    
//	    Tests content of the first SIP
	    ArchiveBuilder builder = ArchiveBuilderFactory.getArchiveBuilderForFile(s1); 
	    
	    try {
			builder.unarchiveFolder(s1, targetDir);
		} catch (Exception e) {
			throw new RuntimeException("couldn't unpack archive", e);
		}

	    assertTrue(BagitUtils.isBagItStyle(unpackedSip1));
		assertTrue(new File(unpackedSip1, "data/export_mets.xml").exists());
		assertTrue(new File(unpackedSip1, "data/image").exists() && new File(unpackedSip1, "data/image").isDirectory());
		assertTrue(new File(unpackedSip1, "data/premis.xml").exists());
	    
		for(File f : new File (unpackedSip1, "data/image").listFiles()) {
			assertTrue(f.getName().endsWith(".bmp"));
		}
		assertTrue(new File (unpackedSip1, "data/image").listFiles().length==29);
	}
}
