
package info.semanticsoftware.semassist.server;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the info.semanticsoftware.semassist.server package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: info.semanticsoftware.semassist.server
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GateRuntimeParameter }
     * 
     */
    public GateRuntimeParameter createGateRuntimeParameter() {
        return new GateRuntimeParameter();
    }

    /**
     * Create an instance of {@link UriList }
     * 
     */
    public UriList createUriList() {
        return new UriList();
    }

    /**
     * Create an instance of {@link ServiceInfoForClient }
     * 
     */
    public ServiceInfoForClient createServiceInfoForClient() {
        return new ServiceInfoForClient();
    }

    /**
     * Create an instance of {@link ServiceInfoForClientArray }
     * 
     */
    public ServiceInfoForClientArray createServiceInfoForClientArray() {
        return new ServiceInfoForClientArray();
    }

    /**
     * Create an instance of {@link GateRuntimeParameterArray }
     * 
     */
    public GateRuntimeParameterArray createGateRuntimeParameterArray() {
        return new GateRuntimeParameterArray();
    }

    /**
     * Create an instance of {@link UserContext }
     * 
     */
    public UserContext createUserContext() {
        return new UserContext();
    }

}
