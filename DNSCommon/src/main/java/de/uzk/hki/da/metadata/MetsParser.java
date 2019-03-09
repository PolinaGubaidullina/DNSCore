package de.uzk.hki.da.metadata;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.xpath.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uzk.hki.da.utils.C;
import  de.uzk.hki.da.metadata.NullLastComparator;

/**
 * @author Eugen Trebunski
 * @author Polina Gubaidullina
 */

public class MetsParser{
	
	/** The logger. */
	public Logger logger = LoggerFactory
			.getLogger(MetsParser.class);
	
	private Document metsDoc = new Document();
	private String METS_XPATH_EXPRESSION = "//mets:file";
	XPath metsXPath = XPath.newInstance(METS_XPATH_EXPRESSION);
	private final String TITLE_PAGE = "title_page";
	private final String STRUCTMAP_TYPE_LOGICAL = "LOGICAL";
	private final Namespace XLINK_NS = 	Namespace.getNamespace("http://www.w3.org/1999/xlink");
	private final Namespace METS_NS = Namespace.getNamespace("http://www.loc.gov/METS/");
	private final List<Element> fileElements;
	public static final String titleSparator=" : ";
	public static final String dateIssuedCreatedCondition="[Electronic ed.]";

	public MetsParser(Document doc) throws JDOMException {
		this.metsDoc = doc;
		fileElements = getFileElementsFromMetsDoc(metsDoc);
	}
	
	
	public Document getMetsDoc() {
		return metsDoc;
	}

	public void setMetsDoc(Document metsDoc) {
		this.metsDoc = metsDoc;
	}
	
	
	public String getUrn() {
		String urn = null;
		try {
			String rootDmdSecId = getUniqueRootElementInLogicalStructMap().getAttributeValue("DMDID");
			@SuppressWarnings("unchecked")
			List<Element> dmdSecs = metsDoc.getRootElement().getChildren("dmdSec", METS_NS);
			for(Element dmdSec : dmdSecs) {
				if(dmdSec.getAttributeValue("ID").equals(rootDmdSecId)) {
					Element rootDmdSec = dmdSec;
					@SuppressWarnings("unchecked")
					List<Element> elements = getModsXmlData(rootDmdSec).getChildren();
					for (Element e : elements) {
						if(e.getName().equals("identifier") && e.getAttributeValue("type").equals("urn")) {
							urn = e.getValue();
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Unable to find urn.");
		}
		return urn;
	}
	
	
	/**
	 * 
	 * Method search in each dmdSec for license and return one license instance, only if each dmdSec contains same license, otherwise method causes exceptions.
	 * @return
	 */
	public MetsLicense getLicenseForWholeMets() {
		return getLicenseForWholeMets(false);
	}
	
	/**
	 * 
	 * Method search in each dmdSec for license and return one license instance, only if each dmdSec contains same license, otherwise method causes exceptions.
	 * @return
	 */
	protected MetsLicense getLicenseForWholeMets(boolean quiet) {
		ArrayList<MetsLicense> licenseAl=new ArrayList<MetsLicense>();
		@SuppressWarnings("unchecked")
		List<Element> dmdSecs = metsDoc.getRootElement().getChildren("dmdSec", METS_NS);
		for(Element dmdSec : dmdSecs) {
			licenseAl.add(getLicense(dmdSec,quiet));
		}
		if(licenseAl.size()==0)
			return null;
		//check all licenses, all have to be the same
		Collections.sort(licenseAl,new NullLastComparator<MetsLicense>());
		if(licenseAl.get(0)==null) //all licenses are null
			return null;
		if(!licenseAl.get(0).equals(licenseAl.get(licenseAl.size()-1))) //first and last element have to be same in sorted array
			if(!quiet)
				throw new RuntimeException("METS contains different licenses("+licenseAl.size()+") e.g.:"+licenseAl.get(licenseAl.size()-1)+" "+licenseAl.get(0));
		logger.debug("Recognized License in METS("+licenseAl.size()+") "+licenseAl.get(0));
		return licenseAl.get(0);
	}
	
	/**
	 * Method search in given dmdSec for license. It returns MetsLicense object or null;
	 * 
	 * @param dmdSec
	 * @return metsLicense
	 */
	protected MetsLicense getLicense(Element dmdSec, boolean quiet) {
		MetsLicense metsLicenseReturn = null;
		List<MetsLicense> metsLicenseList = new ArrayList<MetsLicense>();
		List<Element> accessConditionList = new ArrayList<Element>();
		try {
			accessConditionList = getModsXmlData(dmdSec).getChildren("accessCondition", C.MODS_NS);
		} catch (Exception e) {
			logger.debug("No accessCondition element found! "+e.getMessage());
		}

		for(Element accessCondition:accessConditionList){
			try {
				MetsLicense metsLicense=new MetsLicense();
				metsLicense.setHref(accessCondition.getAttributeValue("href", XLINK_NS));
				metsLicense.setType(accessCondition.getAttributeValue("type"));
				metsLicense.setText(accessCondition.getValue());
				metsLicense.setDisplayLabel(accessCondition.getAttributeValue("displayLabel"));
				
				if(metsLicense.getType()!=null && metsLicense.getType().equals(metsLicense.USE_AND_REP_TYPE))
					metsLicenseList.add(metsLicense);
			} catch (Exception e) {
				logger.debug("No valid accessCondition element found! "+e.getMessage());
			}
		}

		Collections.sort(metsLicenseList, new NullLastComparator<MetsLicense>());

		if(metsLicenseList.size()>=1){
			metsLicenseReturn=metsLicenseList.get(0);
			if(quiet!=true && metsLicenseList.size()>1)
				throw new RuntimeException("dmdSec contains multiple licenses (accessCondition-Elements), unsuported");
		}

		return metsLicenseReturn;
	}
	
	private List<String> getPhysicalDescriptionFromDmdId(String dmdID,String objectId) {
		List<String> extent = new ArrayList<String>();
		String logicalId = dmdID.replace(objectId+"-", "");
		try {
			@SuppressWarnings("unchecked")
			List<Element> dmdSecs = metsDoc.getRootElement().getChildren("dmdSec", METS_NS);
			for(Element dmdSec : dmdSecs) {
				if(dmdSec.getAttributeValue("ID").equals(logicalId)) {
					Element rootDmdSec = dmdSec;
					@SuppressWarnings("unchecked")
					List<Element> elements = getModsXmlData(rootDmdSec).getChildren("physicalDescription",C.MODS_NS);
					for (Element e : elements) {
						if(e.getName().equals("physicalDescription") && !e.getChildren("extent", C.MODS_NS).isEmpty()) {
							List<Element> childElements=e.getChildren("extent", C.MODS_NS);
							for(Element eChild:childElements){
								if(!eChild.getValue().trim().isEmpty())
									extent.add(eChild.getValue());
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Unable to process xml element: "+e.getMessage());
		}
		return extent;
		}

	
	@SuppressWarnings("unchecked")
	private List<String> getTitlePageReferencesFromFrontimage() {
		List<String> ret = new ArrayList<String>();
		
		List<Element> fileSecs = metsDoc.getRootElement().getChildren("fileSec", METS_NS);
		if (fileSecs == null){
			return ret;
		}
		List<Element> fileGrps = null;
		for (Element fileSec : fileSecs){
			fileGrps = fileSec.getChildren("fileGrp", METS_NS);
			if (fileGrps != null){
				break;
			}
		}
		if (fileGrps == null){
			return ret;
		}
		List<Element> files = null;
		for (Element fileGrp : fileGrps) {
			String usi = fileGrp.getAttributeValue("USE");
			if ("FRONTIMAGE".equals(usi)) {
				files = fileGrp.getChildren("file", METS_NS);
				if (files != null) {
					break;
				}
			}
		}
		if (files == null){
			return ret;
		}
		
		for (Element file : files){
			List<Element> fLocs = file.getChildren("FLocat", METS_NS);
			if (fLocs == null){
				continue;
			}
			
			for (Element fLoc : fLocs){
				String href = fLoc.getAttributeValue("href", XLINK_NS);
				if (href != null && href.length() > 0){
					ret.add(href);
				}
			}
		}
		
		return ret;
	}
	
	private List<String> getTitlePageReferencesFromDmdId(String dmdID, String objectId) {
		List<String> titlePageRefs = new ArrayList<String>();
		String titlePageLogicalId = getIdFromLogicalStructMap(dmdID.replace(objectId+"-", ""), TITLE_PAGE, "ID");
		List<String> physIds = getPhysicalIdsFromStructLink(metsDoc, titlePageLogicalId);
		for(String physId : physIds) {
			String titlePageFileId = getFileIdFromPhysicalId(physId);
			if(!titlePageFileId.equals("")) {
				String titlePageRef = getReferenceFromFileId(titlePageFileId);
				if(!titlePageRef.equals("")) {
					titlePageRefs.add(titlePageRef);
				}
			}
		}
		return titlePageRefs;
	}
	
	private List<String> getReferencesFromDmdId(String dmdID, String objectId) {	
		List<String> references = new ArrayList<String>();
		String logicalId = getIdFromLogicalStructMap(dmdID.replace(objectId+"-", ""), "", "ID");
		List<String> physicalIds = getPhysicalIdsFromStructLink(metsDoc, logicalId);
		for(String physicalId : physicalIds) {
			if(!getFileIdFromPhysicalId(physicalId).equals("")) {
				String fileId = getFileIdFromPhysicalId(physicalId);
				String ref = getReferenceFromFileId(fileId);
				if(!ref.equals("")) {
					references.add(ref);
				}
			}
		}
		return references;
	}
	
	@SuppressWarnings("unchecked")
	private String getFileIdFromPhysicalId(String physicalId) {
		String fileId = "";
		try {
			List<Element> structMaps = getStructMaps(metsDoc);
			for(Element s : structMaps) {
				if(s.getAttributeValue("TYPE").equals("PHYSICAL")) {
					List<Element> divList =  s.getChildren("div", METS_NS);
					for(Element div : divList) {
						if(div.getAttributeValue("TYPE").equals("physSequence") && div.getAttributeValue("ID").equals("physroot")) {
							List<Element> divs =  div.getChildren("div", METS_NS);
							for(Element d : divs) {
								if(d.getAttributeValue("ID").equals(physicalId)) {
									fileId = d.getChild("fptr", C.METS_NS).getAttributeValue("FILEID");
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.debug("Unable to file the file id.");
		}
		return fileId;
	}
	
	@SuppressWarnings("unchecked")
	private List<Element> getStructMaps(Document doc) {
		List<Element> structMap = new ArrayList<Element>();
		try {
			structMap = doc.getRootElement().getChildren("structMap", METS_NS);
		} catch (Exception e) {
			logger.debug("Unable to find the structMap elements.");
		}
		return structMap;
	} 
	
	public Element getUniqueRootElementInLogicalStructMap() {
		Element logicalRootElement = null;
		try {
			List<Element> structMap = getStructMaps(metsDoc);
			for(Element s : structMap) {
				if(s.getAttributeValue("TYPE").equals(STRUCTMAP_TYPE_LOGICAL)) {
					@SuppressWarnings("unchecked")
					List<Element> metsDivElements = s.getChildren("div", METS_NS);
					if(metsDivElements.size()==1) {
						logicalRootElement = metsDivElements.get(0);
					} else if(metsDivElements.size()==0) {
						logger.error("No unique root element found in the logical structMap!");
					} else {
						logger.error("Found multiple root elements in the logical structMap!");
					}
				}
			}
		} catch (Exception e) {
			logger.error("Unable to find the unique root element in the logical structMap!");
		}
		return logicalRootElement;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private HashMap<String, ArrayList<String>> getParentChildInfoOfDmdIds(String objectId) {
		HashMap parentChildDmdId = new HashMap<String, ArrayList<String>>();
		String parentDmdId = "";
		try {
			Element rootDivElement = getUniqueRootElementInLogicalStructMap();
			ArrayList<String> children = new ArrayList<String>();
			List<Element> divChildren =  rootDivElement.getChildren("div", METS_NS);
			if(divChildren!=null && !divChildren.isEmpty()) {
				for(Element divChild : divChildren) {
					if(divChild.getAttribute("DMDID")!=null && !divChild.getAttributeValue("DMDID").equals(parentDmdId)) {
						children.add(objectId+"-"+divChild.getAttributeValue("DMDID"));
					}
				}	
			}
			if(children!=null && !children.isEmpty()) {
				parentChildDmdId.put(rootDivElement.getAttributeValue("DMDID"), children);
			}	 
		} catch (Exception e) {
			logger.debug("No parent child relationship found.");
		}
		return parentChildDmdId;
	}
	
	private ArrayList<String> getParentDmdIds(String childDmdId, String ObjectId) {
		ArrayList<String> parentDmdIds = new ArrayList<String>();
		HashMap<String, ArrayList<String>> parentChildDmdIdRel = getParentChildInfoOfDmdIds(ObjectId);
		for(String parentId : parentChildDmdIdRel.keySet()) {
			for(String child : parentChildDmdIdRel.get(parentId)) {
				if(child.equals(childDmdId)) {
					parentDmdIds.add(ObjectId+"-"+parentId);
				}
			}
		}
		return parentDmdIds;
	}
	
	private ArrayList<String> getChildrenDmdIds(String parentDmdId, String ObjectId) {
		ArrayList<String> childrenDmdIds = new ArrayList<String>();
		HashMap<String, ArrayList<String>> parentChildDmdIdRel = getParentChildInfoOfDmdIds(ObjectId);
		for(String parentId : parentChildDmdIdRel.keySet()) {
			if(parentId.equals(parentDmdId.replace(ObjectId+"-", ""))) {
				childrenDmdIds = parentChildDmdIdRel.get(parentId);
			}
		}
		return childrenDmdIds;
	}
	
	@SuppressWarnings({ "unchecked" })
	private String getIdFromLogicalStructMap(String dmdID, String type, String idType) {
		String id = "";
		try {
			List<Element> structMap = getStructMaps(metsDoc);
			for(Element s : structMap) {
				if(s.getAttributeValue("TYPE").equals(STRUCTMAP_TYPE_LOGICAL)) {
					List<Element> metsDivElements = s.getChildren("div", C.METS_NS);
					for (Element d : metsDivElements){
						if(d.getAttributeValue("DMDID").equals(dmdID)) {
							if(type.equals(TITLE_PAGE)) {
								List<Element> divs =  d.getChildren("div", C.METS_NS);
								for(Element div : divs) {
									if(div.getAttributeValue("TYPE").equals(type)) {
										id = div.getAttributeValue(idType);
									}
								}
							} else {
								id = d.getAttributeValue(idType);
							}
						} else if(d.getChildren("div", C.METS_NS)!=null) {
							List<Element> divChildren =  d.getChildren("div", C.METS_NS);
							for(Element divChild : divChildren) {
								if(divChild.getAttributeValue("DMDID").equals(dmdID)) {
									id = divChild.getAttributeValue(idType);
								}
							}	
						}
					}
				}
			}
		} catch (Exception e) {
			logger.debug("Unable to find the "+idType+" id from dmdID "+dmdID);
		}
		return id;
	}
	
	@SuppressWarnings("unchecked")
	private List<String> getPhysicalIdsFromStructLink(Document doc, String logicalId) {
		List<String> physIds = new ArrayList<String>();
		List<Element> structLink = new ArrayList<Element>();
		try {
			structLink = doc.getRootElement().getChild("structLink", C.METS_NS).getChildren("smLink", C.METS_NS);
			for(Element link : structLink) {
				if(link.getAttributeValue("from", XLINK_NS).equals(logicalId)) {
					physIds.add(link.getAttributeValue("to", XLINK_NS));
				}
			}
		} catch (Exception e) {
			logger.debug("Unable to find the physical id from logical id "+logicalId);
		}
		return physIds;
	}
		
	private List<String> getDataProvider() {
		List<String> dataProvider = new ArrayList<String>();
		try {
			@SuppressWarnings("unchecked")
			List<Element> amdSections = metsDoc.getRootElement().getChildren("amdSec", C.METS_NS);
			for(Element amdSec : amdSections) {
				if(amdSec.getChild("rightsMD", C.METS_NS).getChild("mdWrap", C.METS_NS).getAttribute("OTHERMDTYPE").getValue().equals("DVRIGHTS")) {
					dataProvider.add(amdSec
							.getChild("rightsMD", C.METS_NS)
							.getChild("mdWrap", C.METS_NS)
							.getChild("xmlData", C.METS_NS)
							.getChild("rights", C.DV)
							.getChild("owner", C.DV)
							.getValue());
				}
			}
		} catch (Exception e) {
			logger.debug("No amd section found!");
		}
		return dataProvider;
	}
	
	private String getPublisher(Element origInfo) {
		String publisher = "";
		try {
			String type = origInfo.getChild("place", C.MODS_NS).getChild("placeTerm", C.MODS_NS).getAttributeValue("type");
			if(type.equals("text")) {
				publisher = origInfo.getChild("place", C.MODS_NS).getChild("placeTerm", C.MODS_NS).getValue();
			}
			if(origInfo.getChild("publisher", C.MODS_NS)!=null) {
				publisher = origInfo.getChild("publisher", C.MODS_NS).getValue()+" ("+publisher+")";
			}
		} catch (Exception e) {
			logger.debug("Element placeTerm does not exist!");
		}
		return publisher;
	}
	
	@SuppressWarnings("unchecked")
	private List<Element> getOrigInfoElements(Element dmdSec) {
		List<Element> origInfoElements = new ArrayList<Element>();
		try {
			origInfoElements = getModsXmlData(dmdSec).getChildren("originInfo", C.MODS_NS);
		} catch (Exception e) {
			logger.debug("No origInfo element found!");
		}
		return origInfoElements;
	}
	
	private String getDateIssued(Element originInfo) {
		String date = "";
			try {
				Element edition=originInfo.getChild("edition", C.MODS_NS); 
				if(edition==null||!edition.getValue().equals(dateIssuedCreatedCondition))
					date = originInfo.getChild("dateIssued", C.MODS_NS).getValue(); 
			} catch (Exception e) {
				logger.debug("Element dateIssued does not exist! : "+e.toString());
			}
		return date;
	}
	
	private String getDateCreated(Element originInfo) {
		String date = "";
			try {
				Element edition=originInfo.getChild("edition", C.MODS_NS); 
				if(edition!=null&&edition.getValue().equals(dateIssuedCreatedCondition))
					date = originInfo.getChild("dateIssued", C.MODS_NS).getValue(); 
			} catch (Exception e) {
				logger.debug("Element dateIssued does not exist! : "+e.toString());
			}
		return date;
	}
	
	@SuppressWarnings("unchecked")
	private List<Element> getNameElements(Element dmdSec) {
		List<Element> nameElements = new ArrayList<Element>();
		Element modsXmlData = getModsXmlData(dmdSec);
		try {
			nameElements = modsXmlData.getChildren("name", C.MODS_NS);
		} catch (Exception e) {
			logger.debug("No name element found!");
		}
		return nameElements;
	}
	
	private String getContributor(Element name) {
		String namePartValue = "";
		try {
			String roleCode="",roleText="";
			Element roleElem=name.getChild("role", C.MODS_NS);
			List<Element> roleTermElem=roleElem.getChildren("roleTerm", C.MODS_NS);
			for(Element e:roleTermElem){
				String type=e.getAttribute("type").getValue();
				if(type.equals("code")){
					roleCode=e.getValue();
				}else if(type.equals("text")){
					roleText=e.getValue();
				}
			}
			if(!roleCode.equals("aut")&&!roleCode.equals("cre")) {
				namePartValue = roleText+(roleText.trim().isEmpty()?"":": ")+getName(name);
			}
		} catch (Exception e) {
			logger.debug("No creator found!");
		}
		return namePartValue;
	}
	
	private String getCreator(Element name) {
		String namePartValue = "";
		try {
			String roleCode="",roleText="";
			Element roleElem=name.getChild("role", C.MODS_NS);
			List<Element> roleTermElem=roleElem.getChildren("roleTerm", C.MODS_NS);
			for(Element e:roleTermElem){
				String type=e.getAttribute("type").getValue();
				if(type.equals("code")){
					roleCode=e.getValue();
				}else if(type.equals("text")){
					roleText=e.getValue();
				}
			}
			
			/*if(roleCode.equals("aut"))
				namePartValue = "Verfasser: "+getName(name);
			else if(roleCode.equals("cre"))
				namePartValue = "Autor: "+getName(name);*/
			
			if(roleCode.equals("aut")||roleCode.equals("cre")) {
				namePartValue = roleText+(roleText.trim().isEmpty()?"":": ")+getName(name);
			}
			
		} catch (Exception e) {
			logger.debug("No contributor found!");
		}
		return namePartValue;
	}
	
	private String getName(Element name) {
		String namePartValue = "";
		if(name.getAttribute("type", C.MODS_NS)==null || name.getAttribute("type", C.MODS_NS).equals("personal")) {
			try {
				@SuppressWarnings("unchecked")
				List<Element> nameParts = name.getChildren("namePart", C.MODS_NS);
				
				String given = "";
				String family = "";
		
				for(Element element :  nameParts) {
					if(element.getAttributes()==null) {
						namePartValue = element.getValue();
					} else {
						if(element.getAttribute("given", C.MODS_NS)!=null) {
							given = element.getAttributeValue("given", C.MODS_NS);
						} 
						if(element.getAttribute("family", C.MODS_NS)!=null) {
							family = element.getAttributeValue("family", C.MODS_NS);
						}
						
						if(given.equals("")&&family.equals("")) {
							namePartValue = element.getValue();
						} else if(!given.equals("")) {
							namePartValue = given + " " + family;	
						} else namePartValue = family;
					}
				}
			} catch (Exception e) {
				logger.debug("Element namePart does not exist!");
			}
			
			if(namePartValue.isEmpty()) {
				try {
					namePartValue = name.getChild("displayForm", C.MODS_NS).getValue();
				} catch (Exception e) {
					logger.error("No name found");
				} 
			}
		}
		return namePartValue;
	}
	
	private List<String> getIdentifier(Element dmdSec) {
		List<String> identifier = new ArrayList<String>();
		Element modsXmlData = getModsXmlData(dmdSec);
		@SuppressWarnings("unchecked")
		List<Element>  elements = modsXmlData.getChildren();
		for(Element e : elements) {
			if(e.getName().equals("identifier")) {
				identifier.add(e.getValue());
			}
		}
		return identifier;
	}
	
	private List<String> getTitle(Element dmdSec) {
		List<String> title = new ArrayList<String>();
		Element modsXmlData = getModsXmlData(dmdSec);
		
		String titleValue = "";
		String displayLabelValue = "";
		String nonSortValue = "";
		String subTitleValue = "";
		String MainTitleValue = "";
		
		try {
			Element titleInfo = modsXmlData.getChild("titleInfo", C.MODS_NS);
			
			try {
				nonSortValue = titleInfo.getChild("nonSort", C.MODS_NS).getText();
			} catch (Exception e) {
				logger.debug("Element nonSort does not exist!");
			}
			
			try {
				titleValue = titleInfo.getChild("title", C.MODS_NS).getText();
			} catch (Exception e) {
				logger.debug("Element title does not exist!");
			}
			
			try {
				displayLabelValue = titleInfo.getChild("displayLabel", C.MODS_NS).getText();
			} catch (Exception e) {
				logger.debug("Element displayLabel does not exist!");
			}
			
			try {
				subTitleValue = titleInfo.getChild("subTitle", C.MODS_NS).getText();
			} catch (Exception e) {
				logger.debug("Element subTitle does not exist!");
			}
			
			if(!titleValue.equals("")) {
				if(!nonSortValue.equals("")) {
					titleValue = nonSortValue + " " + titleValue;
				}
				MainTitleValue = titleValue;
			}
			else if(!nonSortValue.equals("")) {
				MainTitleValue = nonSortValue;
			}
			else {
				MainTitleValue = displayLabelValue;
			}
		} catch(Exception e) {
			logger.error("Element titleInfo does not exist!!!");
		}
		
		if(!subTitleValue.equals("")) {
			title.add(MainTitleValue+titleSparator+ subTitleValue);
		}else{
			title.add(MainTitleValue);
		}
		return title;
	}
	
	public List<Element> getDMDSections() {
		return metsDoc.getRootElement().getChildren("dmdSec", C.METS_NS);
	}
	
	public HashMap<String, Element> getSections(String objectId) {
		HashMap<String, Element> IDtoSecElement = new HashMap<String, Element>();
		@SuppressWarnings("unchecked")
		List<Element> dmdSections =getDMDSections();
		for(Element e : dmdSections) {
			String id = "";
			id = objectId+"-"+e.getAttribute("ID").getValue();
			IDtoSecElement.put(id, e);
		}
		return IDtoSecElement;
	}
	
	public static Element getModsXmlData(Element dmdSec) {
		return dmdSec
				.getChild("mdWrap", C.METS_NS)
				.getChild("xmlData", C.METS_NS)
				.getChild("mods", C.MODS_NS);
	}
	
	public String getHref(Element fileElement) {
		return fileElement.getChild("FLocat", C.METS_NS).getAttribute("href", XLINK_NS).getValue();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Element> getFileElementsFromMetsDoc(Document doc) throws JDOMException {
		List currentFileElements = new ArrayList<Element>();
		List allNodes = metsXPath.selectNodes(doc);
		for (java.lang.Object node : allNodes) {
			Element fileElement = (Element) node;
			currentFileElements.add(fileElement);
		}
		return currentFileElements;
	} 
	
	public List<Element> getFileElements() {
		return fileElements;
	}


	public List<String> getReferences() {
		List<String> references = new ArrayList<String>();
		for(Element fileElement : fileElements) {
			String ref = getHref(fileElement);
			references.add(ref);
			logger.debug("Found reference "+ref);
		}
		logger.debug("number of existing references "+references.size());
		return references;
	}
	
	private String getReferenceFromFileId(String fileId) {
		String ref = "";
		try {
			for(Element fileElement : fileElements) {
				if(fileElement.getAttributeValue("ID").equals(fileId)) {
					ref = getHref(fileElement);
				}
			}
		} catch (Exception e) {
			logger.error("Unable to find the reference from file "+fileId);
		}
		return ref;
	}
	
	public HashMap<String, HashMap<String, List<String>>> getIndexInfo(String ObjectId) {
		HashMap<String, HashMap<String, List<String>>> indexInfo = new HashMap<String, HashMap<String,List<String>>>();
		HashMap<String, Element> dmdSections = getSections(ObjectId);
		
		for(String id : dmdSections.keySet()) {
			Element e = dmdSections.get(id);
			HashMap<String, List<String>> dmdSecInfo = new HashMap<String, List<String>>();
			
//			Title
			dmdSecInfo.put(C.EDM_TITLE, getTitle(e));
			List<String> accessConditions= getAccessConditions(e);
			dmdSecInfo.put(C.DC_RIGHTS,accessConditions);
			dmdSecInfo.put(C.EDM_RIGHTS,accessConditions);
//			identifier
			dmdSecInfo.put(C.EDM_IDENTIFIER, getIdentifier(e));
			
//			Names
			List<String> creators = new ArrayList<String>();
			List<String> contributors = new ArrayList<String>();
			for(Element name : getNameElements(e)) {
				String creator = getCreator(name);
				String contributor = getContributor(name);
				if(!creator.equals("")) {
					creators.add(creator);
				}
				if(!contributor.equals("")) {
					contributors.add(contributor);
				}
			}
			dmdSecInfo.put(C.EDM_CREATOR, creators);
			dmdSecInfo.put(C.EDM_CONTRIBUTOR, contributors);
			
//			Date && Place
			List<String> datesIssued = new ArrayList<String>();
			List<String> datesCreated = new ArrayList<String>();
			List<String> publishers = new ArrayList<String>();
			for(Element origInfo : getOrigInfoElements(e)) {
				String dateIssued = getDateIssued(origInfo);
				String dateCreated = getDateCreated(origInfo);
				String publisher = getPublisher(origInfo);
				if(!dateIssued.equals("")) {
					datesIssued.add(dateIssued);
				}
				if(!dateCreated.equals("")) {
					datesCreated.add(dateCreated);
				}
				if(!publisher.equals("")) {
					publishers.add(publisher);
				}
			}
			dmdSecInfo.put(C.EDM_DATE_ISSUED, datesIssued);
			dmdSecInfo.put(C.EDM_DATE_CREATED, datesCreated);
			dmdSecInfo.put(C.EDM_PUBLISHER, publishers);
			
			
			List<String> allPhysicalDescr = getPhysicalDescriptionFromDmdId(id, ObjectId);
			if (!allPhysicalDescr.isEmpty()) {
				dmdSecInfo.put(C.EDM_EXTENT, allPhysicalDescr);
			}
			
//			TitlePage
			List<String> titlePageRefs = getTitlePageReferencesFromFrontimage();
			if (titlePageRefs.isEmpty()) {
				titlePageRefs = getTitlePageReferencesFromDmdId(id, ObjectId);
			}
			
			List<String> allReferences = getReferencesFromDmdId(id, ObjectId);
			
//			LAV
			if(allReferences==null || allReferences.isEmpty()) {
				allReferences = getReferences();
			}
			
			if(titlePageRefs!=null & !titlePageRefs.isEmpty()) {
				List<String> references = new ArrayList<String>();
				references.add(titlePageRefs.get(0));
				dmdSecInfo.put(C.EDM_IS_SHOWN_BY, references);
				dmdSecInfo.put(C.EDM_OBJECT, references);
				if(titlePageRefs.size()>1) {
					dmdSecInfo.put(C.EDM_HAS_VIEW, titlePageRefs);
				}
			} else if(allReferences!=null && !allReferences.isEmpty()){
				List<String> firstReference = new ArrayList<String>();
				firstReference.add(allReferences.get(0));
				dmdSecInfo.put(C.EDM_IS_SHOWN_BY, firstReference);
				dmdSecInfo.put(C.EDM_OBJECT, firstReference);
			}
//			hasView
			if(allReferences.size()>1) {
				dmdSecInfo.put(C.EDM_HAS_VIEW, allReferences);
			} 
			
//			dataProvider
			dmdSecInfo.put(C.EDM_DATA_PROVIDER, getDataProvider());
			
//			hasPart
			ArrayList<String> childrenDmdIds = getChildrenDmdIds(id, ObjectId);
			if(childrenDmdIds!=null && !childrenDmdIds.isEmpty()) {
				dmdSecInfo.put(C.EDM_HAS_PART, childrenDmdIds);
			}
			
//			isPartOf
			ArrayList<String> parentsDmdIds = getParentDmdIds(id, ObjectId);
			if(parentsDmdIds!=null && !parentsDmdIds.isEmpty()) {
				dmdSecInfo.put(C.EDM_IS_PART_OF, parentsDmdIds);
			}
			
			indexInfo.put(id, dmdSecInfo);
		}
		return indexInfo;
	}

	
	public  List<String> getAccessConditions(Element dmdSec) {
		List<String> retList = new ArrayList<String>();
		try {
			MetsLicense mLic=getLicense(dmdSec,false);
			if(mLic==null || mLic.getHref().trim().isEmpty()){			
				logger.error("Attribute accessCondition.href does not exist!!!");
			}else{
				retList.add(mLic.getHref());
			}
		} catch(Exception e) {
			logger.error("Element accessCondition does not exist!!!");
		}
		return retList;
	}
	
	

//	::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::  SETTER  ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
	
	
	public void setMimetype(Element fileElement, String mimetype) {
		if(fileElement.getAttribute("MIMETYPE")!=null) {
			fileElement.getAttribute("MIMETYPE").setValue(mimetype);
		} else {
			fileElement.setAttribute("MIMETYPE", mimetype);
		}
	}
	
	public void setLoctype(Element fileElement, String loctype) {
		if(loctype!=null) {
			if(fileElement.getChild("FLocat", C.METS_NS).getAttribute("LOCTYPE")!=null) {
				fileElement.getChild("FLocat", C.METS_NS).getAttribute("LOCTYPE").setValue(loctype);
			} else {
				fileElement.getChild("FLocat", C.METS_NS).setAttribute("LOCTYPE", loctype);
			}
		}
	}
	
	public void setHref(Element fileElement, String newHref) {
		fileElement.getChild("FLocat", C.METS_NS).getAttribute("href", XLINK_NS).setValue(newHref);
	}

}
