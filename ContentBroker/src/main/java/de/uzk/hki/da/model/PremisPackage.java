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

package de.uzk.hki.da.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uzk.hki.da.utils.Path;

@Entity
@Table(name="premis_packages")
public class PremisPackage {
	
	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(PremisPackage.class);
	
	/** The id. */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;

	/** The name. */
	@Column(columnDefinition="varchar(200)")
	private String name;
	
	/** The pkgName. */
	@Column(name="`pkg_name`", columnDefinition="varchar(200)")
	private String pkgName;
	
	
	/** The status. */
	@Column(columnDefinition="varchar(100)")
	private String status;
	
	/** The last_checked. */
	private Date last_checked;
	
	/** The node. */
	@Transient
	private Node node; 
	
	/** The files. */
	@OneToMany(orphanRemoval=true)
	@JoinColumn(name="pkg_id")
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.DELETE})
	private List<PremisDAFile> files = new ArrayList<PremisDAFile>();
	
	/** The events. */
	@OneToMany(orphanRemoval=true)
	@JoinColumn(name="pkg_id")
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.DELETE})
	private List<PremisEvent> events = new ArrayList<PremisEvent>();
	
	private Boolean pruneExceptions = false;
	
	private String container_name; 

	/**
	 * The checksum of that package
	 */
	private String checksum;
	
	
	/** The object. */
	@Transient
	private PremisObject object;
	
	/**
	 * Instantiates a new package.
	 */
	public PremisPackage() {
		
	}
	
	
	
	
	/**
	 * Instantiates a new package.
	 *
	 * @param name the name
	 */
	public PremisPackage(String name) {
		this.name = name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the pkgName.
	 *
	 * @param pkgName the new pkgName
	 */
	public void setPkgName(String pkgName) {
		this.pkgName = pkgName;
	}

	/**
	 * Gets the pkgName.
	 *
	 * @return the pkgName
	 */
	public String getPkgName() {
		return pkgName;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Sets the status.
	 *
	 * @param status the new status
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	
	
	
	/**
	 * Scans folder recursively and generates dafiles for
	 * each file. sets the path of the file to baseName.
	 *
	 * @param repName the rep name
	 * @return the list
	 */
	public List<PremisDAFile> scanRepRecursively(
			Path dataPath,
			String repName) {
		
		String repFolderPath = Path.make( dataPath, repName).toString();
		if (!new File(repFolderPath).exists()) throw new IllegalArgumentException(repFolderPath+" does not exist");

		
		logger.debug("scanning "+repFolderPath);
		
		Collection<File> found = FileUtils.listFiles(new File(repFolderPath),
		        TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		List<PremisDAFile> result = new ArrayList<PremisDAFile>();
		int offset = repFolderPath.length();
		for (File f:found){
			PremisDAFile newFile = new PremisDAFile(repName,f.getPath().
					substring(offset+1, f.getPath().length()));
			logger.debug("found: "+newFile.toString());
			result.add(newFile);
			this.getFiles().add(newFile);
		}
		
		return result;
	}
	
	
	

	
	
	
	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public String getStatus() {
		return status;
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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "["+getId()+","+getName()+"]";
	}


	/**
	 * Gets the container name.
	 *
	 * @return the container name
	 */
	public String getContainerName() {
		return container_name;
	}

	/**
	 * Sets the container name.
	 *
	 * @param cName the new container name
	 */
	public void setContainerName(String cName) {
		this.container_name = cName;
	}

	
	/**
	 * Gets the files.
	 *
	 * @return files
	 * @author Daniel M. de Oliveira
	 * The files collection is used for transfer files from and to db.
	 * The client is responsible for populating this table since it gets not automatically populated.
	 */
	public List<PremisDAFile> getFiles() {
		return files;
	}

	/**
	 * The files collection is used for transfer files from and to db.
	 * The client is responsible for populating this table since it gets not automatically populated.
	 *
	 * @param files the new files
	 * @author Daniel M. de Oliveira
	 */
	public void setFiles(List<PremisDAFile> files) {
		this.files = files;
	}
	

	/**
	 * Gets the events.
	 *
	 * @return the events
	 */
	public List<PremisEvent> getEvents() {
		return events;
	}

	/**
	 * Sets the events.
	 *
	 * @param events the new events
	 */
	public void setEvents(List<PremisEvent> events) {
		this.events = events;
	}




	public String getChecksum() {
		return checksum;
	}




	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}



	public boolean isPruneExceptions() {
		return pruneExceptions;
	}




	public void setPruneExceptions(boolean pruneExceptions) {
		this.pruneExceptions = pruneExceptions;
	}


}
