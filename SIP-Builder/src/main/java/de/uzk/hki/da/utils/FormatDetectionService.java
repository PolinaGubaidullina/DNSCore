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

package de.uzk.hki.da.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.tika.Tika;

/**
 * @author Polina Gubaidullina
 */

public class FormatDetectionService {
	
	private Logger logger = Logger.getLogger(FormatDetectionService.class);
	
	String eadPattern = ".*(?s)\\A.{0,1000}\\x3cead[^\\x3c]{0,1000}\\x3ceadheader.*";
	String metsPattern = ".*(?s)\\A.{0,1000}\\x3c([^: ]+:)?mets[^\\xce]{0,100}xmlns:?[^=]{0,10}=\"http://www.loc.gov/METS.*";
	String lidoPattern = ".*(?s)\\A.{0,1000}\\x3c([^: ]+:)?lidoWrap[^\\xce]{0,100}xmlns:?[^=]{0,10}=\"http://www.lido-schema.org.*";
	
	public File file;
	
	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public FormatDetectionService(File f) {
		this.file = f;
	}
	
	public String detectMimeType(File f) throws IOException {
		String mimeType;
		
		Tika tika = new Tika();
	    try {
	        mimeType = tika.detect(f);
	    }  catch (IOException e) {
	        throw new IOException("Unable to open file for mime type detection: " + file.getAbsolutePath(), e);
	    }
		return mimeType;	
	}
	
	public boolean isXml(File f) throws IOException {
		if(!f.isDirectory() && this.detectMimeType(f).equals("application/xml")) {
			return true;
		} else {
			return false;
		}
	}
	
	public String getMetadataTypeXml(File xmlFile) throws IOException {
		String metadataType = "";
		String beginningOfFile = convertFirst10LinesOfFileToString(xmlFile);
		if(beginningOfFile.matches(metsPattern)) {
			metadataType = C.CB_PACKAGETYPE_METS;
		} else if(beginningOfFile.matches(eadPattern)) {
			metadataType = C.CB_PACKAGETYPE_EAD;
		} else if(beginningOfFile.matches(lidoPattern)) {
			metadataType = C.CB_PACKAGETYPE_LIDO;
		} 
		return metadataType;
	}
	
	private String convertFirst10LinesOfFileToString(File f) throws IOException {
		String result = "";
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line;
		int lineCount=0;
		while ((line = br.readLine()) != null) {
		   // process the line.
			result+=line;
			lineCount++;
			if (lineCount==10) break;
		}
		br.close();
		return result;
	}
	
	public TreeMap<File, String> getMetadataFileWithType() throws Exception {
		File folder = this.file;
		logger.info("Get metadata type of package "+folder);
		TreeMap<File, String> fileWithType = new TreeMap<File, String>();
		List<File> eadFiles = new ArrayList<File>();
		List<File> metsFiles = new ArrayList<File>();
		List<File> lidoFiles = new ArrayList<File>();
		for(File f: folder.listFiles()) {
			logger.info("Check file "+f);
			if(isXml(f)) {
				logger.info(f+" is a xml file");
				String mt = getMetadataTypeXml(f);
				if(mt.equals(C.CB_PACKAGETYPE_METS)) {
					logger.info("of type METS");
					metsFiles.add(f);
				} else if(mt.equals(C.CB_PACKAGETYPE_EAD)) {
					logger.info("of type EAD");
					eadFiles.add(f);
				} else if(mt.equals(C.CB_PACKAGETYPE_LIDO)) {
					logger.info("of type LIDO");
					lidoFiles.add(f);
				}
			} 
		}
		if(eadFiles.size()+metsFiles.size()+lidoFiles.size()==1) {
			if(eadFiles.size()==1) fileWithType.put(eadFiles.get(0), C.CB_PACKAGETYPE_EAD);
			if(metsFiles.size()==1) fileWithType.put(metsFiles.get(0), C.CB_PACKAGETYPE_METS);
			if(lidoFiles.size()==1) fileWithType.put(lidoFiles.get(0), C.CB_PACKAGETYPE_LIDO);
		} else if (eadFiles.size()==1 && metsFiles.size()>=0){
			fileWithType.put(eadFiles.get(0), C.CB_PACKAGETYPE_EAD);
		} else if(eadFiles.size()+metsFiles.size()+lidoFiles.size()>1) {
			/*if(lidoFiles.size()==0&&eadFiles.size()==0&&metsFiles.size()==2){ //To create Public-Mets Test-Sip for CB
				fileWithType.put(metsFiles.get(0), C.CB_PACKAGETYPE_METS);
				fileWithType.put(metsFiles.get(1), C.CB_PACKAGETYPE_METS);
			}else*/
			throw new Exception("Im Verzeichnis "+folder.getName()+" wurde mehr als eine Metadatendatei gefunden. " +
					"\nEAD: \n" + eadFiles +
					"\nMETS: \n" + metsFiles + 
					"\nLIDO: \n"+lidoFiles);
		} else if(eadFiles.size()+metsFiles.size()+lidoFiles.size()==0) {
			logger.error("Im Verzeichnis "+folder.getName()+" wurde keine Metadatendatei gefunden. \nBekannte Formate sind: EAD, METS, LIDO.");
		}
		logger.info("Identified metadata file "+fileWithType);
		return fileWithType;
	}
}
