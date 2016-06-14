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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uzk.hki.da.format.FileWithFileFormat;
import de.uzk.hki.da.utils.Path;
import de.uzk.hki.da.utils.RelativePath;


/**
 * contains the information about one file inside an aip.
 * 
 * for example 
 * repName="representation+a"
 * relativePath="subfolder/file1.txt"
 * 
 * would result in a complete path like
 * "representation+a/subfolder/file1.txt".
 * 
 * also contains information about the file formats of the file.
 * 
 * @author Daniel M. de Oliveira
 *
 */
@Entity
@Table(name="dafiles")
public class DAFile implements FileWithFileFormat{
	
	/** The id. */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	
	/** The Constant logger. */
	static final Logger logger = LoggerFactory.getLogger(DAFile.class);
	
	/** The conversion_instruction_id. */
	private int conversion_instruction_id;
	
	/** The relative_path. */
	@Column(columnDefinition="varchar(600)")
	private String relative_path;
	
	/** The rep_name. */
	@Column(columnDefinition="varchar(100)")
	private String rep_name = "";
	
	/** The format puid. */
	@Column(name="`format_puid`", columnDefinition="varchar(64)")
	private String formatPUID; // encoded as PRONOM-PUID
	
	/** The format secondary attribute. */
	@Column(name="subformat_identifier", columnDefinition="varchar(50)")
	private String subformatIdentifier = ""; // used to store compression or codec information
	
	/** The chksum. */
	private String chksum;
	
	/** The mimetype. */
	@Column(name="`mimetype`")
	private String mimeType;
	
	/** The size. */
	private String size;
	
	/** The previous dafile. */
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "`previousdafile_id`")
	@Cascade(CascadeType.ALL)
	private DAFile previousDAFile;

    @ManyToMany(cascade={javax.persistence.CascadeType.DETACH,
    			javax.persistence.CascadeType.MERGE,
    			javax.persistence.CascadeType.REFRESH,
    			javax.persistence.CascadeType.PERSIST},
    			fetch= FetchType.EAGER)  
    @JoinTable(name="dafile_knownerror",  
    joinColumns={@JoinColumn(name="dafile_id", referencedColumnName="id")},  
    inverseJoinColumns={@JoinColumn(name="knownerror_id", referencedColumnName="id")})
	private List<KnownError> knownErrors = new ArrayList<KnownError>();

	/**
	 * Instantiates a new dA file.
	 */
	public DAFile(){}
	
	/**
	 * Instantiates a new dA file.
	 *
	 * @param pkg the pkg
	 * @param repName the rep name
	 * @param relPath rel path beneath representation folder.
	 */
	public DAFile(String repName,String relPath) {
		setRelative_path(relPath);
		this.setRep_name(repName);
		this.previousDAFile = null;
	}
		
	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	
	
	
	public DAFile getPreviousDAFile() {
		return previousDAFile;
	}

	public void setPreviousDAFile(DAFile previousDAFile) {
		this.previousDAFile = previousDAFile;
	}
	
		
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override 
	public String toString(){
		return "["+getRep_name()+"]/["+relative_path+"]";
	}
	
	
	/**
	 * Gets the relative_path.
	 *
	 * @return the relative_path
	 */
	public String getRelative_path() {
		return relative_path;
	}
	
	
	
	@Transient
	@Override
	public Path getPath() {
		return new RelativePath(rep_name,relative_path);
	}
	
	

	/**
	 * Sets the relative_path.
	 *
	 * @param relative_path the new relative_path
	 */
	public void setRelative_path(String relative_path) {
		if (relative_path.startsWith("/"))
			relative_path = relative_path.substring(1);
		
		this.relative_path = relative_path;
	}

	/**
	 * Gets the rep_name.
	 *
	 * @return the rep_name
	 */
	public String getRep_name() {
		return rep_name;
	}

	/**
	 * Sets the rep_name.
	 *
	 * @param repName the new rep_name
	 */
	public void setRep_name(String repName) {
		this.rep_name = repName;
	}
	
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	public String getMimeType() {
		return mimeType;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override 
	public boolean equals(java.lang.Object o){
		if (!(o instanceof DAFile)) return false;
		DAFile other = (DAFile) o;
		if (this.rep_name.equals(other.rep_name)&&
			this.relative_path.equals(other.relative_path)) 
			return true;
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode(){
		return (this.rep_name+"/"+this.relative_path).hashCode();
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
	 * Gets the conversion_instruction_id.
	 *
	 * @return the conversion_instruction_id
	 */
	public int getConversion_instruction_id() {
		return conversion_instruction_id;
	}

	/**
	 * Sets the conversion_instruction_id.
	 *
	 * @param conversion_instruction_id the new conversion_instruction_id
	 */
	public void setConversion_instruction_id(int conversion_instruction_id) {
		this.conversion_instruction_id = conversion_instruction_id;
	}

	/**
	 * Gets the chksum.
	 * @return the chksum
	 */
	public String getChksum() {
		return chksum;
	}

	/**
	 * Sets the chksum.
	 *
	 * @param chksum the new chksum
	 */
	public void setChksum(String chksum) {
		this.chksum = chksum;
	}

	/**
	 * Gets the size.
	 *
	 * @return the size
	 */
	@Transient
	public String getSize() {
		return size;
	}

	/**
	 * Sets the size.
	 *
	 * @param size the new size
	 */
	public void setSize(String size) {
		this.size = size;
	}

	/**
	 * @return the format puid
	 */
	@Column(name="format_puid")
	public String getFormatPUID() {
		return formatPUID;
	}

	/**
	 * @param formatPUID the new format puid
	 */
	public void setFormatPUID(String formatPUID) {
		this.formatPUID = formatPUID;
	}

	/**
	 */
	@Column(name="subformat_identifier")
	public String getSubformatIdentifier() {
		return subformatIdentifier;
	}

	/**
	 */
	public void setSubformatIdentifier(String subformatIdentifier) {
		this.subformatIdentifier = subformatIdentifier;
	}

	public List<KnownError> getKnownErrors() {
		return knownErrors;
	}
	
	public void setKnownErrors(List<KnownError> knownErrors) {
		this.knownErrors = knownErrors;
		
	}

}
