/*
  DA-NRW Software Suite | ContentBroker
  Copyright (C) 2015 LVRInfoKom
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

package de.uzk.hki.da.metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.apache.commons.io.FilenameUtils;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.uzk.hki.da.model.DAFile;
import de.uzk.hki.da.model.PreservationSystem;
import de.uzk.hki.da.utils.C;
import de.uzk.hki.da.utils.Path;
import de.uzk.hki.da.utils.XMLUtils;

/**
 * @author Polina Gubaidullina
 */

public abstract class MetadataStructure {
	
	/** The logger. */
	public Logger logger = LoggerFactory
			.getLogger(MetadataStructure.class);
	
	protected Path workPath=null;
	
	public MetadataStructure(Path workPath,File metadataFile, List<de.uzk.hki.da.model.Document> documents) 
			throws FileNotFoundException, JDOMException, IOException {
		
		this.workPath=workPath;
	}
	
	public abstract boolean isValid();
	
	public abstract File getMetadataFile();
	
	public abstract HashMap<String, HashMap<String, List<String>>> getIndexInfo(String objectId);
	
	protected void printIndexInfo(String objectId) {
		HashMap<String, HashMap<String, List<String>>> indexInfo = getIndexInfo(objectId);
		for(String id : indexInfo.keySet()) {
			logger.info("-----------------------------------------------------");
			logger.info("ID: "+id);
			for(String info : indexInfo.get(id).keySet()) {
				logger.info(info+": "+indexInfo.get(id).get(info));
			}
			logger.info("-----------------------------------------------------");
		}
	}
	
	public void toEDM(HashMap<String, HashMap<String, List<String>>> indexInfo, File file, PreservationSystem preservationSystem, String objectID, String urn) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            
			Document edmDoc = docBuilder.newDocument();
			Element rootElement = edmDoc.createElement("rdf:RDF");
			edmDoc.appendChild(rootElement);
			
			addXmlNsToEDM(edmDoc, rootElement);
			
			for(String id : indexInfo.keySet()) {
				logger.debug("Index information about "+id+": "+indexInfo.get(id));
				Element providedCHO = addEdmProvidedCHOtoEdm(preservationSystem, id, edmDoc, rootElement);
				Element aggregation = addOreAggregationToEdm(preservationSystem, id, edmDoc, rootElement);
				
				if(indexInfo.get(id).get(C.EDM_IS_PART_OF)==null) {
					List<String> root = new ArrayList<String>();
					root.add("is root element");
					indexInfo.get(id).put(C.EDM_HAS_TYPE, root);
				}
				
				if(indexInfo.get(id).get(C.EDM_IDENTIFIER)==null) {
					List<String> IDs = new ArrayList<String>();
					IDs.add(objectID);
					IDs.add(urn);
					indexInfo.get(id).put(C.EDM_IDENTIFIER, IDs);
				} else {
					indexInfo.get(id).get(C.EDM_IDENTIFIER).add(objectID);
					indexInfo.get(id).get(C.EDM_IDENTIFIER).add(urn);
				}
				
				for(String elementName : indexInfo.get(id).keySet()) {
					Element parentNode = null;
					if(elementName.startsWith("dc:") || elementName.startsWith("dcterms:") || elementName.equals(C.EDM_HAS_TYPE)) {
						parentNode = providedCHO;
					} else if(elementName.startsWith("edm:")) {
						parentNode = aggregation;
					}
					if(parentNode!=null) {
						List<String> values = indexInfo.get(id).get(elementName);
						for(String currentValue : values) {
							if(!currentValue.equals("")) {
								addNewElementToParent(preservationSystem, id, elementName, currentValue, parentNode, edmDoc);
							}
						}
					}
				}
			}
			
			javax.xml.transform.Source source = new javax.xml.transform.dom.DOMSource(edmDoc) ;
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            Result result = new javax.xml.transform.stream.StreamResult(file);
            transformer.transform(source, result);
            
		} catch (Exception e) {
			logger.error("Unable to create the edm file!");
			throw new RuntimeException(e);
		}
		
	}
	
	private void addNewElementToParent(PreservationSystem preservationSystem, String id, String elementName, String elementValue, Element parent, Document edmDoc) {
		Element eName = edmDoc.createElement(elementName);
		if(elementName.equals(C.EDM_HAS_VIEW) || elementName.equals(C.EDM_IS_PART_OF) || elementName.equals(C.EDM_HAS_PART) 
				|| elementName.equals(C.EDM_IS_SHOWN_BY) || elementName.equals(C.EDM_OBJECT)|| elementName.equals(C.EDM_RIGHTS)) {
			if(elementName.equals(C.EDM_IS_PART_OF) || elementName.equals(C.EDM_HAS_PART)) {
				elementValue = preservationSystem.getUrisCho()+"/"+elementValue;
			}
			Attr rdfResource = edmDoc.createAttribute("rdf:resource");
			rdfResource.setValue(elementValue);
			eName.setAttributeNode(rdfResource);
		} else {
			eName.appendChild(edmDoc.createTextNode(elementValue));
		}
		parent.appendChild(eName);
	}
	
	private Element addEdmProvidedCHOtoEdm(PreservationSystem preservationSystem, String id, Document edmDoc, Element rootElement) {
		String cho_identifier = preservationSystem.getUrisCho()+"/"+id;
		Element providedCHO = edmDoc.createElement(C.EDM_PROVIDED_CHO);
		Attr rdfAbout = edmDoc.createAttribute("rdf:about");
		rdfAbout.setValue(cho_identifier);
		providedCHO.setAttributeNode(rdfAbout);
		rootElement.appendChild(providedCHO);
		
		return providedCHO;
	}
	
	private Element addOreAggregationToEdm(PreservationSystem preservationSystem, String id, Document edmDoc, Element rootElement) {
		String aggr_identifier = preservationSystem.getUrisAggr()+"/"+id;
		Element aggregation = edmDoc.createElement(C.EDM_ORE_AGGREGATION);
		Attr rdfAbout = edmDoc.createAttribute("rdf:about");
		rdfAbout.setValue(aggr_identifier);
		aggregation.setAttributeNode(rdfAbout);
		rootElement.appendChild(aggregation);
		
		Element aggregatedCHO = edmDoc.createElement(C.EDM_AGGREGATED_CHO);
		Attr rdfAboutACho = edmDoc.createAttribute("rdf:resource");
		rdfAboutACho.setValue(preservationSystem.getUrisCho()+"/"+id);
		aggregatedCHO.setAttributeNode(rdfAboutACho);
		aggregation.appendChild(aggregatedCHO);
		
		return aggregation;
	}
	
	private void addXmlNsToEDM(Document edmDoc, Element rootElement) {
		Attr xmlns_dc = edmDoc.createAttribute("xmlns:dc");
		xmlns_dc.setValue(C.DC_NS.getURI());
		rootElement.setAttributeNode(xmlns_dc);
		
		Attr xmlns_edm = edmDoc.createAttribute("xmlns:edm");
		xmlns_edm.setValue(C.EDM_NS.getURI());
		rootElement.setAttributeNode(xmlns_edm);
		
		Attr xmlns_dcterms = edmDoc.createAttribute("xmlns:dcterms");
		xmlns_dcterms.setValue(C.DCTERMS_NS.getURI());
		rootElement.setAttributeNode(xmlns_dcterms);
		
		Attr xmlns_rdf = edmDoc.createAttribute("xmlns:rdf");
		xmlns_rdf.setValue(C.RDF_NS.getURI());
		rootElement.setAttributeNode(xmlns_rdf);
		
		Attr xmlns_ore = edmDoc.createAttribute("xmlns:ore");
		xmlns_ore.setValue(C.ORE_NS.getURI());
		rootElement.setAttributeNode(xmlns_ore);
	}
	
	private DAFile getReferencedDAFileFromDocument(de.uzk.hki.da.model.Document doc, File refFile) {
		logger.debug("Get dafile from document "+doc.getName());
		DAFile lastDAFile = doc.getLasttDAFile();
		logger.debug("Last dafile "+lastDAFile);
		DAFile referencedFile = null;
		if(refFile.getAbsolutePath().endsWith(lastDAFile.getRelative_path())) {
			referencedFile = lastDAFile;
		} else {
			while(lastDAFile.getPreviousDAFile() != null){
				DAFile previousDAFile = lastDAFile.getPreviousDAFile();
				logger.debug("Check dafile "+previousDAFile);
				if(refFile.getAbsolutePath().endsWith(previousDAFile.getRelative_path())) {
					referencedFile = previousDAFile;
					break;
				}
	        	lastDAFile = lastDAFile.getPreviousDAFile(); 
	        }
		}
		return referencedFile;
	}
	
	public DAFile getReferencedDafile(File metadataFile, String ref, List<de.uzk.hki.da.model.Document> documents) {
		logger.debug("Get referenced file with the reference "+ref);
		DAFile dafile = null;
		try {
			File refFile = XMLUtils.getRelativeFileFromReference(ref, Path.makeFile(workPath,metadataFile.getPath()));
			logger.debug("Referenced file "+refFile);
			String fileNameWithoutExt = FilenameUtils.removeExtension(refFile.getAbsolutePath());
			logger.debug("Referenced file without extension "+fileNameWithoutExt);
			for(de.uzk.hki.da.model.Document doc : documents) {
				logger.debug("Check document "+doc.getName());
				if(fileNameWithoutExt.endsWith(File.separator+doc.getName())) {
					logger.debug("Found matching document "+doc.getName());
					dafile = getReferencedDAFileFromDocument(doc, refFile);
					logger.debug("return dafile "+dafile);
					break;
				}
			}
		} catch (IOException e) {
			logger.error("File "+ref+" does not exist.");
			e.printStackTrace();
		}
		return dafile;
	}
	
	protected List<File> getReferencedFiles(File metadataFile, List<String> references, List<de.uzk.hki.da.model.Document> documents) {
		List<File> existingFiles = new ArrayList<File>();
		List<String> missingFiles = new ArrayList<String>();
		for(String ref : references) {
			DAFile dafile = getReferencedDafile(metadataFile, ref, documents);
			if(dafile==null){
				missingFiles.add(ref);
			} else {
				existingFiles.add(dafile.getPath().toFile());
				logger.debug("Found new referenced file "+dafile.getPath().toFile());
			}
		}
		if(!missingFiles.isEmpty()) {
			for(String missingFile : missingFiles) {
				logger.error("Missing referenced file[s] in metadata file "+metadataFile.getName()+": "+missingFile);
			}
		}
		logger.debug("number of existing referenced files "+existingFiles.size());
		return existingFiles;
	}
	
	protected void writeDocumentToFile(org.jdom.Document doc,File file) throws IOException{
		logger.debug("Write Metadata Document : "+file.getAbsolutePath());
		XMLOutputter outputter = new XMLOutputter();
		outputter.setFormat(Format.getPrettyFormat());
		FileWriter fw=new FileWriter(file);
		outputter.output(doc,fw);
		fw.close();
	}
	
}


