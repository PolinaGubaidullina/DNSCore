/*
  DA-NRW Software Suite | SIP-Builder
  Copyright (C) 2014 Historisch-Kulturwissenschaftliche Informationsverarbeitung
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

package de.uzk.hki.da.main;

import java.awt.Color;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.UIManager;

import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.TTCCLayout;

import de.uzk.hki.da.cli.Cli;
import de.uzk.hki.da.gui.Gui;
import de.uzk.hki.da.sb.Feedback;

/**
 * Main class; starts GUI or CLI mode
 * 
 * @author Thomas Kleinke
 * @author Polina Gubaidullina
 */
public class SIPBuilder {
	
	private static Logger logger = Logger.getRootLogger();
	
	private static Properties properties;

	public static void main(String[] args) {
    	
		TTCCLayout layout = new TTCCLayout();
		layout.setDateFormat("yyyy'-'MM'-'dd' 'HH':'mm':'ss");
		layout.setThreadPrinting(false);
	    ConsoleAppender consoleAppender = new ConsoleAppender(layout);
	    logger.addAppender( consoleAppender );
        logger.setLevel(Level.DEBUG);
        
        properties = new Properties();
		try {
			properties.load(new InputStreamReader((ClassLoader.getSystemResourceAsStream("configuration/config.properties"))));
		} catch (FileNotFoundException e1) {
			System.exit(Feedback.GUI_ERROR.toInt());
		} catch (IOException e2) {
			System.exit(Feedback.GUI_ERROR.toInt());
		}
        
        try {
        	if (SystemUtils.IS_OS_WINDOWS)
        		System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, "CP850"));
        	else
        		System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			return;
		}
    
	    String mainFolderPath = SIPBuilder.class.getProtectionDomain().getCodeSource().getLocation().getPath();
	    String confFolderPath, dataFolderPath, fidoFolderPath;
	    try {
	    	mainFolderPath = URLDecoder.decode(mainFolderPath, "UTF-8");
			confFolderPath = new File(mainFolderPath).getParent() + File.separator + "conf";
			dataFolderPath = new File(mainFolderPath).getParent() + File.separator + "data";
			fidoFolderPath = new File(mainFolderPath).getParent() + File.separator + "fido";
		} catch (UnsupportedEncodingException e) {
			confFolderPath = "conf";
			dataFolderPath = "data";
			fidoFolderPath = "fido";
		}
	    System.out.println("ConfFolderPath:"+confFolderPath);
	    if (args.length == 0)
	    	startGUIMode(confFolderPath, dataFolderPath, fidoFolderPath);
	    else
		    startCLIMode(confFolderPath, dataFolderPath, args);
    }
    
    /**
     * Starts the SIP-Builder in GUI mode
     * 
     * @param confFolderPath Path to conf folder
     * @param dataFolderPath Path to data folder
     */
    private static void startGUIMode(String confFolderPath, String dataFolderPath, String fidoFolderPath) {
    	
    	try {
    		UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    	} catch (Exception e) {
    		return;
    	}

    	UIManager.put("Label.disabledForeground", Color.LIGHT_GRAY);
    	UIManager.put("ComboBox.disabledForeground", Color.LIGHT_GRAY);
    	UIManager.put("CheckBox.disabledText", Color.LIGHT_GRAY);

    	Gui gui = new Gui(confFolderPath, dataFolderPath, fidoFolderPath);
    	gui.setBounds(100, 100, 750, 520);
    	gui.setResizable(false);
    	gui.setVisible(true);
    	gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	gui.setTitle(getProperties().getProperty("ARCHIVE_NAME") + " SIP-Builder");
    }
    
    /**
     * Starts the SIP-Builder in CLI mode
     * 
     * @param confFolderPath Path to conf folder
     * @param dataFolderPath Path to data folder
     * @param args The CLI arguments passed over by the user
     */
    private static void startCLIMode(String confFolderPath, String dataFolderPath, String[] args) {
    
    	Cli cli = new Cli(confFolderPath, dataFolderPath, args);
    	int exitCode = 0;
    	/**
    	 * Hidden Developer Feature: Ergänzung älterer SIPs um eine default Lizenzangabe in Premis.
    	 */
    	if(false && Arrays.asList(args).contains("-onlyAddLicense")){
    		//Only to add a default license to existing SIP(e.g. old TestSIPs)
    		//StartSipBuilder.sh -onlyAddLicense -source=".../OLDSIPFILE.tgz" -destination=".../DIRECTORYForLicensedSIPs/"
    		exitCode = cli.startAppendLicense();
    	}else
    		exitCode = cli.start();
    	
    	   	
    	if (exitCode < 0)
    		exitCode = 0;
    	
    	System.exit(exitCode);
    }

	

	public static Properties getProperties() {
		return properties;
	}
	
	public static void setProperties(Properties properties) {
		SIPBuilder.properties = properties;
	}
}


