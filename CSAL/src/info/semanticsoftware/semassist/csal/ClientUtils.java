/*
Semantic Assistants -- http://www.semanticsoftware.info/semantic-assistants

This file is part of the Semantic Assistants architecture.

Copyright (C) 2009 Semantic Software Lab, http://www.semanticsoftware.info
Nikolaos Papadakis
Tom Gitzinger

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
package info.semanticsoftware.semassist.csal;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.text.DateFormat;

import info.semanticsoftware.semassist.csal.result.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
//import javax.xml.transform.*;
//import javax.xml.transform.dom.*;
//import javax.xml.transform.stream.*;
import info.semanticsoftware.semassist.server.*;
import java.net.URI;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import javax.xml.namespace.QName;

public class ClientUtils
{

    public static ArrayList mAnnotArray;
    protected static Comparator<Annotation> mByStartCharacter = new CompareByStart();
    protected static byte[] ILLEGAL_XML_1_0_CHARS;

    public static boolean paramHasValue( GateRuntimeParameter p )
    {
        return paramHasValue( p, false );
    }

    public static String defaultServerHost()
    {
        String strHost = "";
        try
        {
            String host = new SemanticServiceBrokerService().getWSDLDocumentLocation().getHost();
            strHost += host;
        }
        catch( Exception ex)
        {
            //ex.printStackTrace();
            
        }
        return strHost;
    }

    public static String defaultServerPort()
    {
        String strPort = "";
        try
        {
            int port = new SemanticServiceBrokerService().getWSDLDocumentLocation().getPort();
            strPort += port;

        }
        catch( Exception ex )
        {
            //ex.printStackTrace();
        }

        return strPort;
    }

    public static boolean paramHasValue( GateRuntimeParameter p, boolean allowEmptyStrings )
    {
        if( p == null )
        {
            return false;
        }
        String type = p.getType();

        if( type.equals( "double" ) )
        {
            return p.getDoubleValue() != null;
        }
        else if( type.equals( "int" ) )
        {
            return p.getIntValue() != null;
        }
        else if( type.equals( "boolean" ) )
        {
            return p.isBooleanValue() != null;
        }
        else if( type.equals( "string" ) )
        {
            if( allowEmptyStrings )
            {
                return p.getStringValue() != null;
            }
            else
            {
                return (p.getStringValue() != null && !p.getStringValue().equals( "" ));
            }
        }
        else if( type.equals( "url" ) )
        {
            return p.getUrlValue() != null;
        }

        return false;
    }

    /**
     * Produce annotation human-readable string
     */
    public static String getHRResult( String xmlResult )
    {
        Document xmlDoc = getXmlDoc( xmlResult );
        if( xmlDoc == null )
        {
            return "XML file could not be parsed. Sorry.";
        }

        Node root = xmlDoc.getDocumentElement();
        Vector<SemanticServiceResult> results = getServiceResults( root );

        // Work with node.getLocalName()

        return xmlDoc.toString();
    }

    public static Vector<SemanticServiceResult> getServiceResults( String xmlResult )
    {
        Document xmlDoc = getXmlDoc( xmlResult );
        if( xmlDoc == null )
        {
            System.out.println( "XML file could not be parsed. Sorry." );
            return null;
        }


        Node root = xmlDoc.getDocumentElement();
        Vector<SemanticServiceResult> results = getServiceResults( root );
        return results;
    }

    public static File writeStringToFile( String s )
    {
        return writeStringToFile( s, ".xml" );
    }

    public static String getFileNameExt( String s )
    {
        if( s == null )
        {
            return null;
        }

        int i = s.lastIndexOf( '.' );
        if( i < 0 )
        {
            return null;
        }

        return s.substring( i );
    }

    public static File writeStringToFile( String s, String ext )
    {
        try
        {
            File f = createTempFile( "input-", ext );
            FileWriter writer = new FileWriter( f );
            BufferedWriter bufWriter = new BufferedWriter( writer );

            bufWriter.write( s );
            bufWriter.flush();
            bufWriter.close();

            return f;
        }
        catch( Throwable e )
        {
            e.printStackTrace();
        }

        return null;
    }

    public static File createTempFile()
    {
        return createTempFile( "serviceResult-", ".xml" );
    }

    public static File createTempFile( String prefix, String ext )
    {
        String fileName = getRandomFileName( prefix );
        File outFile = null;

        try
        {
            outFile = File.createTempFile( fileName, ext );
        }
        catch( Throwable e )
        {
            e.printStackTrace();
        }

        return outFile;
    }

    public static String getRandomFileName( String prefix )
    {
        DateFormat df = DateFormat.getTimeInstance( DateFormat.LONG );
        String ds = df.toString().replace( " ", "" );
        return prefix + ds + ".";
    }

    public static Vector<SemanticServiceResult> getServiceResults( Node root )
    {
        Vector<SemanticServiceResult> result = new Vector<SemanticServiceResult>();
        Node child;

        if( root != null )
        {
            if( root.hasChildNodes() )
            {
                for( child = root.getFirstChild(); child != null; child = child.getNextSibling() )
                {
                    SemanticServiceResult r = getOneResult( child );

                    if( r != null )
                    {
                        result.add( r );
                    }

                    System.out.println( "------------- Node name: " + child.getNodeName() );
                }
            }
        }
        return result;
    }

    protected static SemanticServiceResult getOneResult( Node node )
    {
        String nodeName = node.getNodeName();
        SemanticServiceResult result = new SemanticServiceResult();

        // Annotation case
        if( nodeName.equals( SemanticServiceResult.ANNOTATION ) )
        {
            NamedNodeMap nm = node.getAttributes();
            String annotationType = nm.getNamedItem( "annotationSet" ).getNodeValue();
            System.out.println( "------------- annotationSet = " + annotationType );

            if( !annotationType.equals( "Annotation" ) )
            {
                result.mResultType = SemanticServiceResult.ANNOTATION_IN_WHOLE;
            }
            else
            {
                // for side-notes
                result.mResultType = SemanticServiceResult.ANNOTATION;
            }
            // annotation vector, annotation sorted by type

            // annotvector by start
            HashMap<String, AnnotationVector> map = getAnnotationObjectByStart( node );


            result.mAnnotations = map;

        }
        // File case
        else if( nodeName.equals( SemanticServiceResult.FILE ) )
        {
            result.mResultType = SemanticServiceResult.FILE;
            NamedNodeMap nm = node.getAttributes();

            // Get file URL on server
            Node urlNode = nm.getNamedItem( "url" );
            result.mFileUrl = urlNode.getNodeValue();
        }
        // Document / Corpus
        else if( nodeName.equals( SemanticServiceResult.CORPUS ) )
        {
            result.mResultType = SemanticServiceResult.CORPUS;
            result.mCorpus = getCorpusDocuments( node );
        }
        else if( nodeName.equals( SemanticServiceResult.DOCUMENT ) )
        {
            result.mResultType = SemanticServiceResult.DOCUMENT;
            NamedNodeMap nm = node.getAttributes();

            // Get file URL on server
            Node urlNode = nm.getNamedItem( "url" );
            result.mFileUrl = urlNode.getNodeValue();
        }
        else if( nodeName.equals( "#text" ) )
        {

            System.out.println( "------------- #text case!! " + nodeName );
	    result = null;
        }
        else
        {
            System.out.println( "------------- Unhandled case: " + nodeName );
	    result = null;
        }

        return result;
    }

    protected static Vector<RetrievedDocument> getCorpusDocuments( Node node )
    {
        if( node == null )
        {
            return null;
        }
        Vector<RetrievedDocument> result = new Vector<RetrievedDocument>();

        Node kid;
        for( kid = node.getFirstChild(); kid != null; kid = kid.getNextSibling() )
        {
            // kid should be an outputDocument node now
            if( kid.getNodeName().equals( "outputDocument" ) )
            {
                RetrievedDocument r = new RetrievedDocument();
                NamedNodeMap nm = kid.getAttributes();
                r.url = nm.getNamedItem( "url" ).getNodeValue();
                result.add( r );
            }
        }

        return result;
    }

    /**
     * Input: A node representing one annotation, including documents
     * and annotation instances. Output: HashMap with document ID as
     * key, and AnnotationVector objects as value. These stand for multiple
     * annotation instances.
     */
    protected static HashMap<String, AnnotationVector> getAnnotationObjects( Node node )
    {
        if( node == null )
        {
            return null;
        }

        NamedNodeMap nm = node.getAttributes();
        String annotationType = nm.getNamedItem( "type" ).getNodeValue();



        Node kid;
        // Use document ID as key, AnnotationVector (not yet annotation
        // instances!) as content
        HashMap<String, AnnotationVector> result = new HashMap<String, AnnotationVector>();

        // Traverse the child nodes of the annotation node, which
        // should be <document> nodes
        if( node.hasChildNodes() )
        {
            int documentCount = 0;

            for( kid = node.getFirstChild(); kid != null; kid = kid.getNextSibling() )
            {

                // kid should be annotation document node now
                if( kid.getNodeName().equals( "document" ) )
                {

                    // Get some document ID
                    NamedNodeMap nmKid = kid.getAttributes();
                    String url = nmKid.getNamedItem( "url" ).getNodeValue();
                    if( url == null || url.equals( "" ) )
                    {
                        url = getDocID( documentCount );
                    }

                    // Get current annotation vector for this document
                    Vector<Annotation> va = getAnnotationsForOneDocument( kid );
                    AnnotationVector anns = new AnnotationVector();
                    anns.mAnnotationVector = va;
                    anns.mType = annotationType;

                    // Put the AnnotationVector in the document's space
                    result.put( url, anns );

                }
                else
                {
                    System.out.println( "---------- Strange thing in annotation case: node " +
                                        "name is not \"document\", but " + kid.getNodeName() + "." );
                }

                documentCount++;
            }
        }

        return result;
    }

    protected static HashMap<String, AnnotationVector> getAnnotationObjectByStart( Node node )
    {
        if( node == null )
        {
            return null;
        }

        NamedNodeMap nm = node.getAttributes();
        String annotationType = nm.getNamedItem( "type" ).getNodeValue();

        Node kid;
        // Use document ID as key, AnnotationVector (not yet annotation
        // instances!) as content
        HashMap<String, AnnotationVector> result = new HashMap<String, AnnotationVector>();

        // Traverse the child nodes of the annotation node, which
        // should be <document> nodes
        if( node.hasChildNodes() )
        {
            int documentCount = 0;

            for( kid = node.getFirstChild(); kid != null; kid = kid.getNextSibling() )
            {

                // kid should be annotation document node now
                if( kid.getNodeName().equals( "document" ) )
                {

                    // Get some document ID
                    NamedNodeMap nmKid = kid.getAttributes();
                    String url = nmKid.getNamedItem( "url" ).getNodeValue();

                    if( url == null || url.equals( "" ) )
                    {
                        url = getDocID( documentCount );
                    }

                    // Get current annotation vector for this document
                    Vector<Annotation> va = getAnnotationsForOneDocument( kid );
                    AnnotationVector anns = new AnnotationVector();
                    anns.mAnnotationVector = va;
                    anns.mType = annotationType;
                    // Put the AnnotationVector in the document's space
                    result.put( url, anns );

                }
                else
                {
                    System.out.println( "---------- Strange thing in annotation case: node " +
                                        "name is not \"document\", but " + kid.getNodeName() + "." );
                }

                documentCount++;
            }
        }

        return result;
    }

    // Attention: Giving an ID to annotation document based on their position
    // in the result probably works only if there are no documents
    // that lacks any type of annotation entirely. Content-based ID
    // would be better.
    protected static String getDocID( int num )
    {
        return (new Integer( num )).toString();
    }

    /**
     * Retrieves annotation vector of annotation instances (Annotation objects)
     * from the DOM tree. All these instances are children of the
     * passed node object, which is typically annotation document node.
     */
    protected static Vector<Annotation> getAnnotationsForOneDocument( Node node )
    {

        if( node == null )
        {
            return null;
        }

        Vector<Annotation> result = new Vector<Annotation>();

        Node kid;
        for( kid = node.getFirstChild(); kid != null; kid = kid.getNextSibling() )
        {

            // kid should be an annotationInstance node now
            if( kid.getNodeName().equals( "annotationInstance" ) )
            {
                Annotation annotation = new Annotation();

                // Get content, start, and end
                NamedNodeMap nm = kid.getAttributes();
                String content = nm.getNamedItem( "content" ).getNodeValue();
                annotation.mContent = content;
                annotation.mStart = Long.parseLong( nm.getNamedItem( "start" ).getNodeValue() );
                annotation.mEnd = Long.parseLong( nm.getNamedItem( "end" ).getNodeValue() );


                // Get features
                annotation.mFeatures = getAnnotationFeatures( kid );

                // Add to result
                result.add( annotation );
            }
            else
            {
                System.out.println( "---------- Strange thing in annotation case: node " +
                                    "name is not \"annotationInstance\", but " + kid.getNodeName() + "." );
            }
        }

        return result;
    }

    protected static HashMap<String, String> getAnnotationFeatures( Node node )
    {
        if( node == null )
        {
            return null;
        }
        if( node.getFirstChild() == null )
        {
            return null;
        }

        HashMap<String, String> result = new HashMap<String, String>();
        Node kid;
        for( kid = node.getFirstChild(); kid != null; kid = kid.getNextSibling() )
        {

            // kid should be annotation feature node now
            if( kid.getNodeName().equals( "feature" ) )
            {

                // Get name and value
                NamedNodeMap nm = kid.getAttributes();
                String name = nm.getNamedItem( "name" ).getNodeValue();
                String value = nm.getNamedItem( "value" ).getNodeValue();

                // Add to result
                result.put( name, value );
            }
            else
            {
                System.out.println( "---------- Strange thing in annotation case: node " +
                                    "name is not \"feature\", but " + kid.getNodeName() + "." );
            }
        }

        return result;
    }

    public final static String getElementValue( Node elem )
    {
        Node kid;
        if( elem != null )
        {
            if( elem.hasChildNodes() )
            {
                for( kid = elem.getFirstChild(); kid != null; kid = kid.getNextSibling() )
                {
                    if( kid.getNodeType() == Node.TEXT_NODE )
                    {
                        return kid.getNodeValue();
                    }
                }
            }
        }
        return "";
    }

    /** Parses XML file and returns XML document.
     * @param obj Object string to parse
     * @return XML document or <B>null</B> if error occured
     */
    public static Document getXmlDoc( Object obj )
    {

        DocumentBuilder docBuilder;
        Document doc = null;
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setIgnoringElementContentWhitespace( true );

        try
        {
            docBuilder = docBuilderFactory.newDocumentBuilder();
        }
        catch( ParserConfigurationException e )
        {
            System.out.println( "Wrong parser configuration: " + e.getMessage() );
            return null;
        }

        try
        {
            if( obj instanceof File )
            {
                doc = docBuilder.parse( (File) obj );

            }
            else if( obj instanceof String )
            {
                String sDoc = (String) obj;
                doc = docBuilder.parse( new InputSource( new StringReader( sDoc ) ) );
            }
            else
            {
                System.out.println( "param not File or String " );
                return null;
            }

        }
        catch( SAXException e )
        {
            System.out.println( "Wrong XML file structure: " + e.getMessage() );
            e.printStackTrace();
            return null;
        }
        catch( Throwable e )
        {
            System.out.println( "Could not read source string: " + e.getMessage() );
        }

        System.out.println( "---------------- XML string parsed" );

        return doc;
    }

    /**
     * Escape characters for text appearing as XML data, between tags.
     *
     * <P>The following characters are replaced with corresponding character entities :
     * <table border='1' cellpadding='3' cellspacing='0'>
     * <tr><th> Character </th><th> Encoding </th></tr>
     * <tr><td> < </td><td> &lt; </td></tr>
     * <tr><td> > </td><td> &gt; </td></tr>
     * <tr><td> & </td><td> &amp; </td></tr>
     * <tr><td> " </td><td> &quot;</td></tr>
     * <tr><td> ' </td><td> &#039;</td></tr>
     * </table>
     *
     * <P>Note that JSTL's {@code <c:out>} escapes the exact same set of
     * characters as this method. <span class='highlight'>That is, {@code <c:out>}
     *  is good for escaping to produce valid XML, but not for producing safe
     *  HTML.</span>
     */
    @SuppressWarnings( "unchecked" )
    public static void SortAnnotations( AnnotationVectorArray annotVectorArr )
    {
        mAnnotArray = new ArrayList();

        if( annotVectorArr == null )
        {
            return;
        }

        for( Iterator<AnnotationVector> it = annotVectorArr.mAnnotVectorArray.iterator(); it.hasNext(); )
        {
            AnnotationVector annotVector = it.next();

            //Add Annotations to AnnotationArray in order to sort
            CreateAnnotationsArray( annotVector );
        }

        // sort all mAnnotations by start offset
        Collections.sort( mAnnotArray, mByStartCharacter );

    }

    @SuppressWarnings( "unchecked" )
    protected static void CreateAnnotationsArray( AnnotationVector annotVector )
    {

        for( Iterator<Annotation> it = annotVector.mAnnotationVector.iterator(); it.hasNext(); )
        {
            Annotation annotation = it.next();

            if( annotation.mContent != null && !annotation.mContent.equals( "" ) )
            {
                annotation.mType = annotVector.mType;
                System.out.println( "annotation.mType: " + annotation.mType );

                mAnnotArray.add( annotation );
            }
        }
    }

}

