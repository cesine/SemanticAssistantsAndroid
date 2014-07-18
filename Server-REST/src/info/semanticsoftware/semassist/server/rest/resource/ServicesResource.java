/*
Semantic Assistants -- http://www.semanticsoftware.info/semantic-assistants

This file is part of the Semantic Assistants architecture.

Copyright (C) 2013, 2014 Semantic Software Lab, http://www.semanticsoftware.info
Rene Witte
Bahar Sateli

The Semantic Assistants architecture is free software: you can
redistribute and/or modify it under the terms of the GNU Affero General
Public License as published by the Free Software Foundation, either
version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package info.semanticsoftware.semassist.server.rest.resource;

import info.semanticsoftware.semassist.server.rest.model.ServiceModel;
import info.semanticsoftware.semassist.server.rest.utils.Constants;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * Router class to execute service inquiry requests.
 * 
 * @author Bahar Sateli
 */
public class ServicesResource extends ServerResource {

	/**
	 * Handles HTTP GET requests. Returns an XML or JSON representation of the
	 * available services.
	 * 
	 * @return XML or JSON representation of available services
	 */
	@Get
	public Representation getRepresentation() {
		Representation representation = null;

		// What MIME types does the client accept?
		Form headers = (Form) getRequestAttributes().get(
				"org.restlet.http.headers");
		String accepts = headers.getFirstValue("Accept");
		// if no MIME provided, fall back to XML (for backward compatibility
		// with other SA clients
		accepts = (accepts == null) ? "xml" : accepts;
		System.out.println("Request Accepts: " + accepts);

		switch (Constants.MIME_TYPES.valueOf(accepts.toUpperCase())) {
		case JSON:
			String json = new ServiceModel().getAllJSON();
			representation = new StringRepresentation(json,
					MediaType.APPLICATION_JSON);
			break;

		case XML:
		case APPXHTML:
		case APPXML:
			String xml = new ServiceModel().getAllXML();
			representation = new StringRepresentation(xml,
					MediaType.APPLICATION_XML);
			break;

		case TEXT:
			//TODO send a plain text representation
			break;
		default:
			break;
		}
		return representation;

	}

}
