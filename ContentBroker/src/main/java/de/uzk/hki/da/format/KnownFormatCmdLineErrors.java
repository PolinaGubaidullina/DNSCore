package de.uzk.hki.da.format;
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
/**
 * @author jens Peters
 * Known IPTC Tag Error which is being found in BigTiff Pictures by some customers.
 */
import java.util.List;

import org.hibernate.Session;

import de.uzk.hki.da.model.KnownError;
import de.uzk.hki.da.service.HibernateUtil;


public class KnownFormatCmdLineErrors{
	
	List<KnownError> formatCmdLineErrors = null;
	
	public KnownFormatCmdLineErrors() {
	}
	
	@SuppressWarnings("unchecked")
	public void init(){
		formatCmdLineErrors = null;
		Session session = HibernateUtil.openSession();
		session.beginTransaction();
		List<KnownError>l = session.createQuery("from KnownError")
				.setReadOnly(true).list();
		formatCmdLineErrors = l;
		session.getTransaction().commit();
		session.close();
	}
	
	public List<KnownError> getFormatCmdLineErrors() {
		return formatCmdLineErrors;
	}
	public void setFormatCmdLineErrors(List<KnownError> ke) {
		this.formatCmdLineErrors = ke;
	}
}
