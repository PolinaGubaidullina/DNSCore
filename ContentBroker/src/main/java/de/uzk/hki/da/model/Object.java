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

package de.uzk.hki.da.model;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.io.FilenameUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uzk.hki.da.model.PublicationRight.Audience;
import de.uzk.hki.da.utils.C;
import de.uzk.hki.da.utils.SidecarUtils;


/**
 * The Class DNS Object.
 *
 * @author Jens Peters
 * @author Daniel M. de Oliveira
 */
@Entity
@Table(name="objects")
public class Object {
	
	public static class ObjectStatus {
		public static final Integer InitState = 0;
		public static final String  InitStateText = "Im Workflow (Bearbeitung noch nicht gestartet)";
		
		public static final Integer UnderAudit = 60;
		public static final String UnderAuditText = "In Überprüfung";
		
		public static final Integer InWorkflow = 50;
		public static final String InWorkflowText = "Im Workflow (Bearbeitung)";
		
		public static final Integer Error = 51;
		public static final String ErrorText = "AIP im fehlerhaften Zustand";
		
		public static final Integer ArchivedAndValidAndNotInWorkflow = 100;
		public static final String ArchivedAndValidAndNotInWorkflowText = "Objekt archiviert und in keiner Verarbeitung";
		
		public static final String NotKnownText = "Objekt unbekannt";
		
	}
	

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(Package.class);
	
	/** The object_state. */ 
	private int object_state;

	
	
	
	public static final String REPRESENTATION_FILTER = "^.*[+][ab]";
	
	
	
	
	
	/** The data_pk. */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int data_pk;

	/** 
	 * The identifier. In javadoc comments throughout the source code base often refered to as oid.
	 * */
	@Column(unique=true, columnDefinition="varchar(60)")
	private String identifier;
	
	/** The urn. */
	private String urn;
	
	/** The initial_node. */
	@Column(columnDefinition="varchar(150)")
	private String initial_node;
	
	/** The orig_name. */
	private String orig_name;
	
	/** The date_created. */
	@Column(columnDefinition="varchar(100)")
	private String date_created;
	
	/** The date_modified. */
	@Column(columnDefinition="varchar(100)")
	private String date_modified;
	
	@Column(columnDefinition="varchar(50)")
	private String package_type;
	
	@Column(columnDefinition="varchar(120)")
	private String metadata_file;

	@Transient
	private String DIP_PUBLIC_REPNAME = WorkArea.TMP_PIPS+"/public";
	@Transient
	private String DIP_INSTITUTION_REPNAME = WorkArea.TMP_PIPS+"/institution";
	
	/** The published_flag. */
	private int published_flag=C.PUBLISHEDFLAG_UNDEFINED;
	
	
	/** The last_checked. */
	private Date last_checked;
	
	/** The static_nondisclosure_limit. */
	private Date static_nondisclosure_limit;
	
	/** The dynamic_nondisclosure_limit. */
	private String dynamic_nondisclosure_limit;
	
	private Date static_nondisclosure_limit_institution;
	
	private String dynamic_nondisclosure_limit_institution;
	
	/**
	 * Should be a member of rightsstatement. Also in PREMIS we serialize it that way.
	 * But here we wanted to transport the right through the db without adding tables for rightsstatement.
	 */
	@Column(name="ddb_exclusion")
	private Boolean ddbExclusion = false;
	
	
	/** The original_formats. */
	@Column(name="original_formats")
	private String originalFormats;
	
	/** The most_recent_formats. */
	@Column(name="most_recent_formats")
	private String mostRecentFormats;
	
	/** The most_recent_secondary_attributes. */
	@Column(name="most_recent_secondary_attributes", columnDefinition="varchar(2048)")
	private String mostRecentSecondaryAttributes = "";
	
	/** last publication try (time based) */
	@Column(name="last_publication_try")
	private Date lastPublicationTry;

	/** The rights. */
	@Transient
	private RightsStatement rights = new RightsStatement();
	
	/** The agents. */
	@Transient
	private Set<Agent> agents = new HashSet<Agent>();
	
	/** The contractor. */
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	 
	/** The packages. */
	@OneToMany(targetEntity=Package.class, fetch=FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Package> packages = new ArrayList<Package>();

	/** The documents. */
	@OneToMany(orphanRemoval=true, targetEntity=Document.class, fetch=FetchType.EAGER)
	@JoinColumn(name="object_id")
	@Cascade(CascadeType.ALL)
//	@Fetch(value = FetchMode.SUBSELECT)
	private List<Document> documents = new ArrayList<Document>();
	
	/**
	 * Sets the data_pk.
	 *
	 * @param data_pk the new data_pk
	 */
	public void setData_pk(int data_pk) {
		this.data_pk = data_pk;
	}
	
	/**
	 * Gets the data_pk.
	 *
	 * @return the data_pk
	 */
	public int getData_pk() {
		return data_pk;
	}	
	
	/**
	 * Sets the packages.
	 *
	 * @param packages the new packages
	 */
	public void setPackages(List<Package> packages) {
		this.packages = packages;
	}	
	
	/**
	 * Gets the packages.
	 *
	 * @return the packages
	 */
	public List<Package> getPackages() {
		return this.packages;
	}	
	
	/**
	 * Sets the documents.
	 *
	 * @param documents the new documents
	 */
	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}	
	
	/**
	 * Adds the documents.
	 *
	 * @param documents the new documents
	 */
	public void addDocument(Document doc) {
		this.getDocuments().add(doc);
	}	
	
	/**
	 * Gets the documents.
	 *
	 * @return the documents
	 */
	public List<Document> getDocuments() {
		return this.documents;
	}
	
	
	/**
	 * Gets the document.
	 *
	 * @return the document
	 */
	public Document getDocument(String docname) {
		Document document = null;
		if(!this.documents.isEmpty()) {
			for(Document doc : this.documents) {
				if(doc.getName().equals(docname)) {
					document = new Document();
					document = doc;
				}
			}
		} else {
			logger.debug("This object doesn't contain any documents.");
		}
		return document;
	}
	
	/**
	 * Sets the identifier.
	 *
	 * @param identifier the new identifier
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	/**
	 * Gets the identifier.
	 *
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}
	
	/**
	 * Gets the urn.
	 *
	 * @return the urn
	 */
	public String getUrn() {
		return urn;
	}
	
	/**
	 * Sets the urn.
	 *
	 * @param urn the new urn
	 */
	public void setUrn(String urn) {
		this.urn = urn;
		/*ObjectPremisXmlReader premis = new ObjectPremisXmlReader();
		try {
			Object o = premis.deserialize(new File("src/test/resources/metadata/premistest.xml"));
			System.out.println("--- setUrn // deserialisiere xml ---");
			//Thread.sleep(5000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} */
	}
	
	/**
	 * Sets the orig_name.
	 *
	 * @param orig_name the new orig_name
	 */
	public void setOrig_name(String orig_name) {
		this.orig_name = orig_name;
	}
	
	/**
	 * Gets the orig_name.
	 *
	 * @return the orig_name
	 */
	public String getOrig_name() {
		return orig_name;
	}
	
	/**
	 * Sets the initial_node.
	 *
	 * @param initial_node the new initial_node
	 */
	public void setInitial_node(String initial_node) {
		this.initial_node = initial_node;
	}
	
	/**
	 * Gets the initial_node.
	 *
	 * @return the initial_node
	 */
	public String getInitial_node() {
		return initial_node;
	}
	
	/**
	 * Sets the date_created.
	 *
	 * @param date_created the new date_created
	 */
	public void setDate_created(String date_created) {
		this.date_created = date_created;
	}
	
	/**
	 * Gets the date_created.
	 *
	 * @return the date_created
	 */
	public String getDate_created() {
		return date_created;
	}
	
	/**
	 * Sets the date_modified.
	 *
	 * @param date_modified the new date_modified
	 */
	public void setDate_modified(String date_modified) {
		this.date_modified = date_modified;
	}
	
	/**
	 * Gets the date_modified.
	 *
	 * @return the date_modified
	 */
	public String getDate_modified() {
		return date_modified;
	}
	

	/**
	 * @return the path to the newest b representation.
	 * @throws IllegalStateException if no dafiles present in object.
	 */
	public String getNameOfLatestBRep(){
		if (getReps().isEmpty()) throw new IllegalStateException("no files present. reps could not get determined from dafiles.");
		
		String newestRep = getReps().get(getReps().size()-1);
		return newestRep.replace("+a", "+b");
	}
	
	
	
	
	
	
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override 
	public String toString(){
		
		
		return 
				"Object["
		+ identifier + "," + urn + "," + "," + orig_name + "," +
		user.getShort_name() +
						"]";
	}
	
	
	
	
	/**
	 * Sets the published_flag.
	 *
	 * @param published_flag the new published_flag
	 */
	public void setPublished_flag(int published_flag) {
		this.published_flag = published_flag;
	}
	
	/**
	 * Gets the published_flag.
	 *
	 * @return the published_flag
	 */
	public int getPublished_flag() {
		return published_flag;
	}
	
	/**
	 * Sets the contractor.
	 *
	 * @param contractor the new contractor
	 */
	public void setContractor(User contractor) {
		this.user = contractor;
	}
	
	
	/**
	 * Gets the contractor.
	 *
	 * @return the contractor
	 */
	public User getContractor() {
		return user;
	}
	
	/**
	 * Sets the object_state.
	 *
	 * @param object_state the new object_state
	 */
	public void setObject_state(int object_state) {
		this.object_state = object_state;
	}
	
	/**
	 * Gets the object_state.
	 *
	 * @return the object_state
	 */
	public int getObject_state() {
		return object_state;
	}
	
	/**
	 * Sets the last_checked.
	 *
	 * @param last_checked the new last_checked
	 */
	public void setLast_checked(Date last_checked) {
		this.last_checked = last_checked;
	}
	
	/**
	 * Gets the last_checked.
	 *
	 * @return the last_checked
	 */
	public Date getLast_checked() {
		return last_checked;
	}
	
	/**
	 * Sets the static_nondisclosure_limit.
	 *
	 * @param static_nondisclosure_limit the new static_nondisclosure_limit
	 */
	public void setStatic_nondisclosure_limit(Date static_nondisclosure_limit) {
		this.static_nondisclosure_limit = static_nondisclosure_limit;
	}
	
	/**
	 * Gets the static_nondisclosure_limit.
	 *
	 * @return the static_nondisclosure_limit
	 */
	public Date getStatic_nondisclosure_limit() {
		return static_nondisclosure_limit;
	}
	
	/**
	 * Sets the dynamic_nondisclosure_limit.
	 *
	 * @param dynamic_nondisclosure_limit the new dynamic_nondisclosure_limit
	 */
	public void setDynamic_nondisclosure_limit(String dynamic_nondisclosure_limit) {
		this.dynamic_nondisclosure_limit = dynamic_nondisclosure_limit;
	}
	
	/**
	 * Gets the dynamic_nondisclosure_limit.
	 *
	 * @return the dynamic_nondisclosure_limit
	 */
	public String getDynamic_nondisclosure_limit() {
		return dynamic_nondisclosure_limit;
	}
	
	public Date getStatic_nondisclosure_limit_institution() {
		return static_nondisclosure_limit_institution;
	}

	public void setStatic_nondisclosure_limit_institution(
			Date static_nondisclosure_limit_institution) {
		this.static_nondisclosure_limit_institution = static_nondisclosure_limit_institution;
	}

	public String getDynamic_nondisclosure_limit_institution() {
		return dynamic_nondisclosure_limit_institution;
	}

	public void setDynamic_nondisclosure_limit_institution(
			String dynamic_nondisclosure_limit_institution) {
		this.dynamic_nondisclosure_limit_institution = dynamic_nondisclosure_limit_institution;
	}

	/**
	 * Sets the original_formats.
	 *
	 * @param original_formats the new original_formats
	 */
	public void setOriginal_formats(String original_formats) {
		this.originalFormats = original_formats;
	}
	
	/**
	 * Gets the original_formats.
	 *
	 * @return the original_formats
	 */
	public String getOriginal_formats() {
		return originalFormats;
	}
	
	/**
	 * Sets the most_recent_formats.
	 *
	 * @param most_recent_formats the new most_recent_formats
	 */
	public void setMost_recent_formats(String most_recent_formats) {
		this.mostRecentFormats = most_recent_formats;
	}
	
	/**
	 * Gets the most_recent_formats.
	 *
	 * @return the most_recent_formats
	 */
	public String getMost_recent_formats() {
		return mostRecentFormats;
	}
	
	/**
	 * Gets the rights.
	 * @return the rights
	 */
	public RightsStatement getRights(){
		
		return rights;
	}
	
	/**
	 * Sets the rights.
	 * @param rights the new rights
	 */
	public void setRights(RightsStatement rights){
		
		this.rights = rights;
	}
	
	/**
	 * Gets the agents.
	 *
	 * @return the agents
	 */
	public Set<Agent> getAgents() {
		
		return agents;
	}
	
	/**
	 * Sets the agents.
	 *
	 * @param agents the new agents
	 */
	public void setAgents(Set<Agent> agents) {
		
		this.agents = agents;
	}
	
	
	
	
	
	/**
	 * Grants right.
	 *
	 * @param eventType EventType specifies the type of action to be checked
	 * @return boolean true if right is granted, false if not
	 * @author Sebastian Cuy
	 * Checks if metadata contains a RightsStatement which grants
	 * the right to perform a given type of action.
	 */
	public boolean grantsRight(String eventType) {
			
		if (eventType.equals("PUBLICATION")
				&& rights.getPublicationRights() != null
				&& !rights.getPublicationRights().isEmpty()) {
			return true;
		} else if (eventType.equals("MIGRATION") && rights.getMigrationRight() != null) {
			if (!rights.getMigrationRight().getCondition().equals(MigrationRight.Condition.CONFIRM)) return true;
		}
		return false;
	}
	
	
	/**
	 * 
	 * @author Thomas Kleinke
	 * @return true if the object has deltas, otherwise false
	 */
	public boolean isDelta() {
		return (packages.size() > 1);
	}
	
	
	/**
	 * Grants publication right.
	 *
	 * @param audience Audience specifies the type of audience to be checked
	 * @return boolean true if right is granted, false if not
	 * @author Sebastian Cuy
	 * Checks if metadata contains a RightsStatement which grants
	 * the right to publish the package for the given type of audience.
	 */
	public boolean grantsPublicationRight(Audience audience) {
			
		for (PublicationRight publRight : rights.getPublicationRights()) {
			if (! publRight.getAudience().equals(audience)) continue;

			logger.debug("comparing rights for audience "+audience);
			logger.debug("date now: "+new Date());
			if (publRight.getStartDate()!=null) 
			{ 
				logger.debug("date from rights: "+publRight.getStartDate());
				logger.debug("publright date before now "+publRight.getStartDate().before(new Date()));
			}
			
			if (publRight.getLawID() == null
				&& (publRight.getStartDate() == null ||
				publRight.getStartDate().before(new Date()))
				) {
				
				return true;
			}
		}
		return false;
	}
	
	
	
	/**
	 * @author Daniel M. de Oliveira 
	 * @return
	 */
	private Collection<DAFile> getFilesFromRepresentation(String rep){

		ArrayList<DAFile> list = new ArrayList<DAFile>();
		
		for (Package pkg:getPackages()){
			for (DAFile f:pkg.getFiles()){
				if (f.getRep_name().equals(rep)) list.add(f);
			}
		}
		
		return list;
	}
 	
	
	/**
	 * Gets the newest files from all representations.
	 *
	 * @param sidecarExtensions Files with the given extensions are considered sidecar files. Sidecar files are treated differently from other files
	 *  (see <a href="https://github.com/da-nrw/DNSCore/blob/master/ContentBroker/src/main/markdown/dip_specification.md#sidecar-files">documentation</a> for details)
	 * @return newest DAFile of each Document.  
	 * @author Thomas Kleinke
	 * @author Daniel M. de Oliveira
	 * @throws RuntimeException if it finds a file on the file system to which it cannot find a corresponding attached instance of
	 * DAFile in this object.
	 */
	
	// TODO make it a set
	public List<DAFile> getNewestFilesFromAllRepresentations(String sidecarExts)
	{
		// document name to newest file instance
		Map<String, DAFile> documentMap = new HashMap<String, DAFile>();
		
		for (String rep : getReps())
			for (DAFile f: getFilesFromRepresentation(rep)){
				if (SidecarUtils.hasSidecarExtension(f.getRelative_path(),sidecarExts))
					documentMap.put(f.getRelative_path(), f);
				else
					documentMap.put(FilenameUtils.removeExtension(f.getRelative_path()), f);
			}
			
		return new ArrayList<DAFile>(documentMap.values());
	}
	
	
	/**
	 * Gets the representations based on the existing folders in the objects folder on the file system.
	 * @author Daniel M. de Oliveira
	 * @return representations as sorted array
	 */
	private List<String> getReps() {
		List<String> representations = new ArrayList<String>();
		for (Package p:this.getPackages()) {
			for (DAFile f:p.getFiles()) {
				String repName = f.getRep_name();
				if(!repName.equals(DIP_PUBLIC_REPNAME) && !repName.equals(DIP_INSTITUTION_REPNAME)) {
					representations.add(f.getRep_name());
				}
			}
		}
		Collections.sort(representations);
		return representations;
	}
	

	
	
	
	/**
	 * Gets the latest version of a file in a package.
	 * Currently works only for files directly beneath the rep folders.
	 *
	 * @param filename the filename
	 * @return the latest
	 * @author Daniel M. de Oliveira
	 * @throws IllegalStateException if it finds a file without an associated dafile instance.
	 */
	public DAFile getLatest(String filename) {
		
		DAFile result = null;
		for (String rep : getReps()) {
			
			Collection<DAFile> filesFromRep = getFilesOfRepresentation(rep);
			for (DAFile f:filesFromRep) {
				if (f.getRelative_path().endsWith(filename)) result = f;
				
			}
		}
		
		return result;
	}
	
	
	/**
	 * Gets the latest package.
	 *
	 * @return the newest package which is attached to the object.
	 * @throws IllegalStateException if there are no packages associated to the object.
	 * 
	 * @author Daniel M. de Oliveira
	 */
	public Package getLatestPackage(){
		if (getPackages().size()==0) throw new IllegalStateException("no packages associated");

		Package max = null;
		int maxnumber=0;
		
		for (Package p:getPackages()){
			if (Integer.parseInt(p.getName())>maxnumber){
				maxnumber = Integer.parseInt(p.getName());
				max = p;
			}
		}
		return max;
	}
	
	
	
	
	/**
	 * Gets the most_recent_secondary_attributes.
	 *
	 * @return the most_recent_secondary_attributes
	 */
	public String getMostRecentSecondaryAttributes() {
		return mostRecentSecondaryAttributes;
	}
	
	/**
	 * Sets the most_recent_secondary_attributes.
	 *
	 * @param most_recent_secondary_attributes the new most_recent_secondary_attributes
	 */
	public void setMostRecentSecondaryAttributes(
			String most_recent_secondary_attributes) {
		this.mostRecentSecondaryAttributes = most_recent_secondary_attributes;
	}


	public boolean ddbExcluded() {
		return ddbExclusion;
	}

	public void setDdbExclusion(boolean ddbExclusion) {
		this.ddbExclusion = ddbExclusion;
	}

	public String getPackage_type() {
		return package_type;
	}

	public void setPackage_type(String package_type) {
		this.package_type = package_type;
	}

	public String getMetadata_file() {
		return metadata_file;
	}

	public void setMetadata_file(String metadata_file) {
		this.metadata_file = metadata_file;
	}
	
	public Date getLastPublicationTry() {
		return lastPublicationTry;
	}

	public void setLastPublicationTry(Date lastPublicationTry) {
		this.lastPublicationTry = lastPublicationTry;
	}

	/**
	 * Gets the files of a representation based on the information stored
	 * in the object tree. 
	 * 
	 * @author Daniel M. de Oliveira
	 * @param repName
	 * @return
	 */
	private Collection<DAFile> getFilesOfRepresentation(String repName){
		
		Collection<DAFile> files = new ArrayList<DAFile>();
		
		for (Package pkg:this.getPackages()){
			for (DAFile f:pkg.getFiles()){
				if (f.getRep_name().equals(repName))
					files.add(f);
			}
		}
		
		return files;
	}
	
	
	
	
	
}
