/*
Semantic Assistants -- http://www.semanticsoftware.info/semantic-assistants

This file is part of the Semantic Assistants architecture.

Copyright (C) 2012, 2013 Semantic Software Lab, http://www.semanticsoftware.info
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
package info.semanticsoftware.semassist.client.wiki.wikihelper;

/**
 * This class contains MediaWiki engine modules.
 * @author Bahar Sateli
 * */
public class MediaWiki extends WikiEngine{

	/** MediaWiki engine helper object. */
	private MediaWikiHelper helper = null;

	/** MediaWiki engine ontology keeper object. */
	private MediaWikiOntologyKeeper ontologyKeeper = null;

	/** MediaWiki engine parser object. */
	private MediaWikiParser parser = null;

	/**
	 * MediaWiki class constructor.
	 * */
	public MediaWiki(){
		helper = new MediaWikiHelper();
		ontologyKeeper = new MediaWikiOntologyKeeper();
		parser = new MediaWikiParser();
	}

	/**
	 * Returns the helper object.
	 * @return WikiHelper object
	 * */
	@Override
	public WikiHelper getHelper(){
		return helper;
	}

	/**
	 * Returns the ontologykeeper object.
	 * @return WikiOntologyKeeper object
	 * */
	@Override
	public WikiOntologyKeeper getOntologyKeeper(){
		return ontologyKeeper;
	}

	/**
	 * Returns the parser object.
	 * @return WikiParser object
	 * */
	@Override
	public WikiParser getParser(){
		return parser;
	}
}