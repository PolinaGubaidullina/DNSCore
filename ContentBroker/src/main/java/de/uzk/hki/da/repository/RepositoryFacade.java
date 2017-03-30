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

package de.uzk.hki.da.repository;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import com.yourmediashelf.fedora.client.FedoraClientException;

/**
 * Decouples the repository logic used for publishing PIPs from specific implementations.
 * 
 * Different repository applications can be connected to the ContentBroker by implementing
 * this interface and changing the corresponding class in the spring configuration.
 * 
 * @author Sebastian Cuy
 * @author Daniel M. de Oliveira
 * @author Josef Hammer
 */
public interface RepositoryFacade {
	
	/**
	 * Purge an object if it exists.
	 * @param objectId the id of the package
	 * @param collection the collection name
	 * @return boolean, true if package existed and was purged
	 * @throws RepositoryException
	 */
	boolean purgeObjectIfExists(String objectId, String collection) throws RepositoryException;
	
	/**
	 * Create a new object in the repository.
	 * @param objectId the id for the package
	 * @param collection the collection name
	 * @param ownerId a user id
	 * @throws RepositoryException
	 */
	void createObject(String objectId, String collection, String ownerId) throws RepositoryException;
	
	/**
	 * Attach a file to a package in the repository.
	 * @param objectId the object id of object the file is attached to
	 * @param collection a named collection
	 * @param fileId the name of the file to be created
	 * @param file the file to be ingested
	 * @param label the label of the file
	 * @param mimeType the MIME type
	 * @throws IOException 
	 * @throws RepositoryEcxeption
	 */
	void ingestFile(String objectId, String collection, String fileId,
			File file, String label, String mimeType) throws RepositoryException, IOException;
	
	/**
	 * Get the file contents from the repository.
	 * @param objectId the object id of object the file is attached to
	 * @param collection a named collection
	 * @param fileId the id for the file in the repository
	 * @return the contents of the file as an input stream, null if file does not exist
	 * @throws RepositoryException
	 */
	void retrieveTo(OutputStream outputStream, String objectId, String collection, String fileId) throws RepositoryException;

	/**
	 * Check if an object exists in the repository.
	 * @param objectId the id of the object in the repository
	 * @param collection a named collection
	 * @return true if object is present, false if not
	 * @throws RepositoryException
	 */
	boolean fileExists(String objectId, String collection, String fileId) throws RepositoryException;
	/**
	 * Check if an object exists in the repository.
	 * @param objectId the id of the object in the repository
	 * @param collection a named collection
	 * @return true if object is present, false if not
	 * @throws RepositoryException
	 */
	boolean objectExists(String objectId, String collection) throws RepositoryException;

	/**
	 * Adds an RDF triple about an object to the repository.
	 * @param objectId the id of the object, i.e. the RDF subject
	 * @param collection a named collection
	 * @param predicate the predicate as absolute URL
	 * @param object the object as absolute URL
	 * @throws FedoraClientException
	 * @throws RepositoryException
	 */
	void addRelationship(String objectId, String collection, String predicate, String object)
			throws RepositoryException;
	
	/**
	 * Generate a file id from a file path.
	 * @param path the path to the file
	 * @return the generated file id
	 */
	String generateFileId(String path);

}
