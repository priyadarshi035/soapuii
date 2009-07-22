/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.teststeps;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.xml.namespace.QName;

import org.apache.log4j.Logger;

import com.eviware.soapui.config.RestRequestStepConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestRequestConverter;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertedXPathsContainer;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry.AssertableType;
import com.eviware.soapui.impl.wsdl.teststeps.registry.RestRequestStepFactory.ItemDeletedException;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.eviware.soapui.model.support.InterfaceListenerAdapter;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.support.ProjectListenerAdapter;
import com.eviware.soapui.model.support.TestPropertyListenerAdapter;
import com.eviware.soapui.model.support.TestStepBeanProperty;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepProperty;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.support.resolver.ChangeRestMethodResolver;
import com.eviware.soapui.support.resolver.ImportInterfaceResolver;
import com.eviware.soapui.support.resolver.RemoveTestStepResolver;
import com.eviware.soapui.support.resolver.ResolveContext;
import com.eviware.soapui.support.resolver.ResolveContext.PathToResolve;
import com.eviware.soapui.support.types.StringToStringMap;

public class RestTestRequestStep extends WsdlTestStepWithProperties implements RestTestRequestStepInterface
{
	private final static Logger log = Logger.getLogger( RestTestRequestStep.class );
	private RestRequestStepConfig restRequestStepConfig;
	private RestTestRequest testRequest;
	private RestResource restResource;
	private RestMethod restMethod;
	private final InternalProjectListener projectListener = new InternalProjectListener();
	private final InternalInterfaceListener interfaceListener = new InternalInterfaceListener();
	private WsdlSubmit<RestRequest> submit;

	public RestTestRequestStep( WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest )
			throws ItemDeletedException
	{
		super( testCase, config, true, forLoadTest );

		if( getConfig().getConfig() != null )
		{
			restRequestStepConfig = ( RestRequestStepConfig )getConfig().getConfig().changeType(
					RestRequestStepConfig.type );

			testRequest = buildTestRequest( forLoadTest );
			if( testRequest == null )
				throw new ItemDeletedException();

			// testRequest = new RestTestRequest( null,
			// requestStepConfig.getRestRequest(), this, forLoadTest );
			testRequest.addPropertyChangeListener( this );
			testRequest.addTestPropertyListener( new InternalTestPropertyListener() );

			if( config.isSetName() )
				testRequest.setName( config.getName() );
			else
				config.setName( testRequest.getName() );
		}
		else
		{
			restRequestStepConfig = ( RestRequestStepConfig )getConfig().addNewConfig().changeType(
					RestRequestStepConfig.type );
		}

		for( TestProperty property : testRequest.getProperties().values() )
		{
			addProperty( new RestTestStepProperty( ( RestParamProperty )property ) );
		}

		// init default properties
		addProperty( new TestStepBeanProperty( "Endpoint", false, testRequest, "endpoint", this ) );
		addProperty( new TestStepBeanProperty( "Username", false, testRequest, "username", this ) );
		addProperty( new TestStepBeanProperty( "Password", false, testRequest, "password", this ) );
		addProperty( new TestStepBeanProperty( "Domain", false, testRequest, "domain", this ) );

		// init properties
		addProperty( new TestStepBeanProperty( "Request", false, testRequest, "requestContent", this )
		{
			@Override
			public String getDefaultValue()
			{
				return createDefaultRequestContent();
			}
		} );

		addProperty( new TestStepBeanProperty( "ResponseAsXml", true, testRequest, "responseContentAsXml", this )
		{
			@Override
			public String getDefaultValue()
			{
				return createDefaultResponseXmlContent();
			}
		} );

		addProperty( new TestStepBeanProperty( "Response", true, testRequest, "responseContentAsString", this )
		{
			@Override
			public String getDefaultValue()
			{
				return createDefaultRawResponseContent();
			}
		} );

		initRestTestRequest();

		if( !forLoadTest && restResource != null )
		{
			getResource().getInterface().getProject().addProjectListener( projectListener );
			getResource().getInterface().addInterfaceListener( interfaceListener );

			// we need to listen for name changes which happen when interfaces
			// are
			getResource().getInterface().addPropertyChangeListener( this );
			getResource().addPropertyChangeListener( this );
		}

		if( getRestMethod() != null )
		{
			getRestMethod().addPropertyChangeListener( this );
		}
	}

	public void beforeSave()
	{
		super.beforeSave();

		if( testRequest != null )
			testRequest.beforeSave();
	}

	public RestRequestStepConfig getRequestStepConfig()
	{
		return restRequestStepConfig;
	}

	protected RestTestRequest buildTestRequest( boolean forLoadTest )
	{
		if( getRestMethod() == null )
			return null;
		return new RestTestRequest( getRestMethod(), getRequestStepConfig().getRestRequest(), this, forLoadTest );
	}

	private void initRestTestRequest()
	{
		if( getRestMethod() == null )
			setDisabled( true );
		else
			getTestRequest().setRestMethod( getRestMethod() );
	}

	public String getService()
	{
		return getRequestStepConfig().getService();
	}

	public String getResourcePath()
	{
		return getRequestStepConfig().getResourcePath();
	}

	protected String createDefaultRawResponseContent()
	{
		return getResource() == null ? null : getResource().createResponse( true );
	}

	protected String createDefaultResponseXmlContent()
	{
		return getResource() == null ? null : getResource().createResponse( true );
	}

	protected String createDefaultRequestContent()
	{
		return getResource() == null ? null : getResource().createRequest( true );
	}

	@Override
	public Collection<Interface> getRequiredInterfaces()
	{
		ArrayList<Interface> result = new ArrayList<Interface>();
		result.add( findRestResource().getInterface() );
		return result;
	}

	private RestResource findRestResource()
	{
		Project project = ModelSupport.getModelItemProject( this );
		RestService restService = ( RestService )project.getInterfaceByName( getRequestStepConfig().getService() );
		if( restService != null )
		{
			return restService.getResourceByFullPath( getRequestStepConfig().getResourcePath() );
		}
		return null;
	}

	private RestMethod findRestMethod()
	{
		if( !restRequestStepConfig.isSetMethodName() )
		{
			RestRequestConverter.updateRestTestRequest( this );
			
			// Must be an old version RestRequest...
			if( getResource() == null )
			{
				restResource = RestRequestConverter.resolveResource( this );
				if( restResource == null )
					return null;
				getRequestStepConfig().setService( restResource.getInterface().getName() );
				getRequestStepConfig().setResourcePath( restResource.getFullPath() );
			}
			RestMethod method = RestRequestConverter.getMethod( getResource(), getRequestStepConfig().getRestRequest()
					.selectAttribute( null, "method" ).newCursor().getTextValue(), getRequestStepConfig().getRestRequest()
					.getName() );
			restRequestStepConfig.setMethodName( method.getName() );
			return method;
		}
		return ( RestMethod )getWsdlModelItemByName( getResource().getRestMethodList(), getRequestStepConfig()
				.getMethodName() );
	}

	public RestMethod getRestMethod()
	{
		if( restMethod == null )
			restMethod = findRestMethod();
		return restMethod;
	}

	public RestResource getResource()
	{
		if( restResource == null )
			restResource = findRestResource();
		return restResource;
	}

	public Operation getOperation()
	{
		return getResource();
	}

	@Override
	public void release()
	{
		super.release();

		if( restResource != null )
		{
			restResource.removePropertyChangeListener( this );
			restResource.getInterface().getProject().removeProjectListener( projectListener );
			restResource.getInterface().removeInterfaceListener( interfaceListener );
			restResource.getInterface().removePropertyChangeListener( this );
		}
	}

	@Override
	public void resetConfigOnMove( TestStepConfig config )
	{
		super.resetConfigOnMove( config );

		restRequestStepConfig = ( RestRequestStepConfig )config.getConfig().changeType( RestRequestStepConfig.type );
		testRequest.updateConfig( restRequestStepConfig.getRestRequest() );
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		if( evt.getSource() == restResource )
		{
			if( evt.getPropertyName().equals( RestResource.PATH_PROPERTY ) )
			{
				getRequestStepConfig().setResourcePath( restResource.getFullPath() );
			}
			else if( evt.getPropertyName().equals( "childMethods" ) && restMethod == evt.getOldValue() )
			{
				// TODO: Convert to HttpTestRequestStep
				log.debug( "Removing test step due to removed Rest method" );
				getTestCase().removeTestStep( RestTestRequestStep.this );
			}
		}
		else if( restResource != null && evt.getSource() == restResource.getInterface() )
		{
			if( evt.getPropertyName().equals( Interface.NAME_PROPERTY ) )
			{
				getRequestStepConfig().setService( ( String )evt.getNewValue() );
			}
		}
		else if( evt.getSource() == restMethod )
		{
			if( evt.getPropertyName().equals( RestMethod.NAME_PROPERTY ) )
			{
				getRequestStepConfig().setMethodName( ( String )evt.getNewValue() );
			}
		}
		if( evt.getPropertyName().equals( TestAssertion.CONFIGURATION_PROPERTY )
				|| evt.getPropertyName().equals( TestAssertion.DISABLED_PROPERTY ) )
		{
			if( getTestRequest().getResponse() != null )
			{
				getTestRequest().assertResponse( new WsdlTestRunContext( this ) );
			}
		}
		else
		{
			if( evt.getSource() == testRequest && evt.getPropertyName().equals( WsdlTestRequest.NAME_PROPERTY ) )
			{
				if( !super.getName().equals( ( String )evt.getNewValue() ) )
					super.setName( ( String )evt.getNewValue() );
			}

			notifyPropertyChanged( evt.getPropertyName(), evt.getOldValue(), evt.getNewValue() );
		}

		// TODO copy from HttpTestRequestStep super.propertyChange( evt );
	}

	public class InternalProjectListener extends ProjectListenerAdapter
	{
		@Override
		public void interfaceRemoved( Interface iface )
		{
			if( restResource != null && restResource.getInterface().equals( iface ) )
			{
				log.debug( "Removing test step due to removed interface" );
				( getTestCase() ).removeTestStep( RestTestRequestStep.this );
			}
		}
	}

	public class InternalInterfaceListener extends InterfaceListenerAdapter
	{
		@Override
		public void operationRemoved( Operation operation )
		{
			if( operation == restResource )
			{
				log.debug( "Removing test step due to removed operation" );
				( getTestCase() ).removeTestStep( RestTestRequestStep.this );
			}
		}

		@Override
		public void operationUpdated( Operation operation )
		{
			if( operation == restResource )
			{
				// requestStepConfig.setResourcePath( operation.get );
			}
		}
	}

	@Override
	public boolean dependsOn( AbstractWsdlModelItem<?> modelItem )
	{
		if( modelItem instanceof Interface && getTestRequest().getOperation() != null
				&& getTestRequest().getOperation().getInterface() == modelItem )
		{
			return true;
		}
		else if( modelItem instanceof Operation && getTestRequest().getOperation() == modelItem )
		{
			return true;
		}

		return false;
	}

	public void setRestMethod( RestMethod method )
	{
		if( restMethod == method )
			return;

		RestMethod oldMethod = restMethod;
		restMethod = method;
		getRequestStepConfig().setService( method.getInterface().getName() );
		getRequestStepConfig().setResourcePath( method.getResource().getFullPath() );
		getRequestStepConfig().setMethodName( method.getName() );

		if( oldMethod != null )
			oldMethod.removePropertyChangeListener( this );

		restMethod.addPropertyChangeListener( this );
		getTestRequest().setRestMethod( restMethod );
	}

	public RestTestRequest getTestRequest()
	{
		return testRequest;
	}

	public Interface getInterface()
	{
		return getResource() == null ? null : getResource().getInterface();
	}

	@Override
	public ImageIcon getIcon()
	{
		return testRequest == null ? null : testRequest.getIcon();
	}

	public TestStep getTestStep()
	{
		return this;
	}

	@SuppressWarnings( "unchecked" )
	public void resolve( ResolveContext<?> context )
	{
		super.resolve( context );

		if( getRestMethod() == null )
		{
			if( context.hasThisModelItem( this, "Missing REST Method in Project", getRequestStepConfig().getService()
					+ "/" + getRequestStepConfig().getMethodName() ) )
				return;
			context.addPathToResolve( this, "Missing REST Method in Project",
					getRequestStepConfig().getService() + "/" + getRequestStepConfig().getMethodName() ).addResolvers(
					new RemoveTestStepResolver( this ), new ImportInterfaceResolver( this )
					{
						@Override
						protected boolean update()
						{
							RestMethod restMethod = findRestMethod();
							if( restMethod == null )
								return false;

							setRestMethod( restMethod );
							initRestTestRequest();
							setDisabled( false );
							return true;
						}

					}, new ChangeRestMethodResolver( this )
					{
						@Override
						public boolean update()
						{
							RestMethod restMethod = getSelectedRestMethod();
							if( restMethod == null )
								return false;

							setRestMethod( restMethod );
							initRestTestRequest();
							setDisabled( false );
							return true;
						}

						protected Interface[] getInterfaces( WsdlProject project )
						{
							List<RestService> interfaces = ModelSupport.getChildren( project, RestService.class );
							return interfaces.toArray( new Interface[interfaces.size()] );
						}
					} );
		}
		else
		{
			getRestMethod().resolve( context );
			if( context.hasThisModelItem( this, "Missing REST Method in Project", getRequestStepConfig().getService()
					+ "/" + getRequestStepConfig().getMethodName() ) )
			{
				PathToResolve path = context.getPath( this, "Missing REST Method in Project", getRequestStepConfig()
						.getService()
						+ "/" + getRequestStepConfig().getMethodName() );
				path.setSolved( true );
			}
		}
	}
	
	@Override
	public void prepare( TestCaseRunner testRunner, TestCaseRunContext testRunContext ) throws Exception
	{
		super.prepare( testRunner, testRunContext );

		testRequest.setResponse( null, testRunContext );

		for( TestAssertion assertion : testRequest.getAssertionList() )
		{
			assertion.prepare( testRunner, testRunContext );
		}
	}

	/*
	 * @SuppressWarnings("unchecked") public void resolve(ResolveContext<?> context)
	 * { super.resolve(context);
	 * 
	 * if (getResource() == null) { if (context.hasThisModelItem(this,
	 * "Missing REST Resource in Project", getRequestStepConfig() .getService() +
	 * "/" + getRequestStepConfig().getResourcePath())) return;
	 * context.addPathToResolve( this, "Missing REST Resource in Project",
	 * getRequestStepConfig().getService() + "/" +
	 * getRequestStepConfig().getResourcePath()) .addResolvers(new
	 * RemoveTestStepResolver(this), new ImportInterfaceResolver(this) {
	 * 
	 * @Override protected boolean update() { RestResource restResource =
	 * findRestResource(); if (restResource == null) return false;
	 * 
	 * setResource(restResource); initRestTestRequest(); setDisabled(false);
	 * return true; }
	 * 
	 * }, new ChangeOperationResolver(this, "Resource") {
	 * 
	 * @Override public boolean update() { RestResource restResource =
	 * (RestResource) getSelectedOperation(); if (restResource == null) return
	 * false;
	 * 
	 * setResource(restResource); initRestTestRequest(); setDisabled(false);
	 * return true; }
	 * 
	 * protected Interface[] getInterfaces( WsdlProject project) {
	 * List<RestService> interfaces = ModelSupport .getChildren(project,
	 * RestService.class); return interfaces .toArray(new Interface[interfaces
	 * .size()]); } }); } else { getResource().resolve(context); if
	 * (context.hasThisModelItem(this, "Missing REST Resource in Project",
	 * getRequestStepConfig() .getService() + "/" +
	 * getRequestStepConfig().getResourcePath())) { PathToResolve path =
	 * context.getPath(this, "Missing REST Resource in Project",
	 * getRequestStepConfig().getService() + "/" +
	 * getRequestStepConfig().getResourcePath()); path.setSolved(true); } } }
	 */

	public PropertyExpansion[] getPropertyExpansions()
	{
		PropertyExpansionsResult result = new PropertyExpansionsResult( this, testRequest );

		result.extractAndAddAll( "requestContent" );
		result.extractAndAddAll( "endpoint" );
		result.extractAndAddAll( "username" );
		result.extractAndAddAll( "password" );
		result.extractAndAddAll( "domain" );

		StringToStringMap requestHeaders = testRequest.getRequestHeaders();
		for( String key : requestHeaders.keySet() )
		{
			result.extractAndAddAll( new RequestHeaderHolder( requestHeaders, key ), "value" );
		}

		// result.addAll( testRequest.getWssContainer().getPropertyExpansions()
		// );

		return result.toArray( new PropertyExpansion[result.size()] );
	}

	public class RequestHeaderHolder
	{
		private final StringToStringMap valueMap;
		private final String key;

		public RequestHeaderHolder( StringToStringMap valueMap, String key )
		{
			this.valueMap = valueMap;
			this.key = key;
		}

		public String getValue()
		{
			return valueMap.get( key );
		}

		public void setValue( String value )
		{
			valueMap.put( key, value );
			testRequest.setRequestHeaders( valueMap );
		}
	}

	public AbstractHttpRequest<?> getHttpRequest()
	{
		return testRequest;
	}

	public TestAssertion addAssertion( String type )
	{
		WsdlMessageAssertion result = testRequest.addAssertion( type );
		return result;
	}

	public void addAssertionsListener( AssertionsListener listener )
	{
		testRequest.addAssertionsListener( listener );
	}

	public TestAssertion cloneAssertion( TestAssertion source, String name )
	{
		return testRequest.cloneAssertion( source, name );
	}

	public String getAssertableContent()
	{
		return testRequest.getAssertableContent();
	}

	public AssertableType getAssertableType()
	{
		return testRequest.getAssertableType();
	}

	public TestAssertion getAssertionByName( String name )
	{
		return testRequest.getAssertionByName( name );
	}

	public List<TestAssertion> getAssertionList()
	{
		return testRequest.getAssertionList();
	}

	public AssertionStatus getAssertionStatus()
	{
		return testRequest.getAssertionStatus();
	}

	public void removeAssertion( TestAssertion assertion )
	{
		testRequest.removeAssertion( assertion );
	}

	public void removeAssertionsListener( AssertionsListener listener )
	{
		testRequest.removeAssertionsListener( listener );
	}

	public TestAssertion moveAssertion( int ix, int offset )
	{
		return testRequest.moveAssertion( ix, offset );
	}

	public Map<String, TestAssertion> getAssertions()
	{
		return testRequest.getAssertions();
	}

	public WsdlMessageAssertion getAssertionAt( int index )
	{
		return testRequest.getAssertionAt( index );
	}

	public int getAssertionCount()
	{
		return testRequest == null ? 0 : testRequest.getAssertionCount();
	}

	public String getDefaultAssertableContent()
	{
		return testRequest.getDefaultAssertableContent();
	}

	public TestStepResult run( TestCaseRunner runner, TestCaseRunContext runContext )
	{
		RestRequestStepResult testStepResult = new RestRequestStepResult( this );

		try
		{
			submit = testRequest.submit( runContext, false );
			HttpResponse response = ( HttpResponse )submit.getResponse();

			if( submit.getStatus() != Submit.Status.CANCELED )
			{
				if( submit.getStatus() == Submit.Status.ERROR )
				{
					testStepResult.setStatus( TestStepStatus.FAILED );
					testStepResult.addMessage( submit.getError().toString() );

					testRequest.setResponse( null, runContext );
				}
				else if( response == null )
				{
					testStepResult.setStatus( TestStepStatus.FAILED );
					testStepResult.addMessage( "Request is missing response" );

					testRequest.setResponse( null, runContext );
				}
				else
				{
					runContext.setProperty( AssertedXPathsContainer.ASSERTEDXPATHSCONTAINER_PROPERTY, testStepResult );
					testRequest.setResponse( response, runContext );

					testStepResult.setTimeTaken( response.getTimeTaken() );
					testStepResult.setSize( response.getContentLength() );
					testStepResult.setResponse( response );

					switch( testRequest.getAssertionStatus() )
					{
					case FAILED :
						testStepResult.setStatus( TestStepStatus.FAILED );
						break;
					case VALID :
						testStepResult.setStatus( TestStepStatus.OK );
						break;
					case UNKNOWN :
						testStepResult.setStatus( TestStepStatus.UNKNOWN );
						break;
					}
				}
			}
			else
			{
				testStepResult.setStatus( TestStepStatus.CANCELED );
				testStepResult.addMessage( "Request was canceled" );
			}

			if( response != null )
			{
				testStepResult.setRequestContent( response.getRequestContent() );
				testStepResult.addProperty( "URL", response.getURL() == null ? "<missing>" : response.getURL().toString() );
				testStepResult.addProperty( "Method", String.valueOf( response.getMethod() ) );
				testStepResult.addProperty( "StatusCode", String.valueOf( response.getStatusCode() ) );
				testStepResult.addProperty( "HTTP Version", response.getHttpVersion() );
			}
			else
				testStepResult.setRequestContent( testRequest.getRequestContent() );
		}
		catch( SubmitException e )
		{
			testStepResult.setStatus( TestStepStatus.FAILED );
			testStepResult.addMessage( "SubmitException: " + e );
		}
		finally
		{
			submit = null;
		}

		testStepResult.setDomain( PropertyExpander.expandProperties( runContext, testRequest.getDomain() ) );
		testStepResult.setUsername( PropertyExpander.expandProperties( runContext, testRequest.getUsername() ) );
		testStepResult.setEndpoint( PropertyExpander.expandProperties( runContext, testRequest.getEndpoint() ) );
		testStepResult.setPassword( PropertyExpander.expandProperties( runContext, testRequest.getPassword() ) );
		testStepResult.setEncoding( PropertyExpander.expandProperties( runContext, testRequest.getEncoding() ) );

		if( testStepResult.getStatus() != TestStepStatus.CANCELED )
		{
			AssertionStatus assertionStatus = testRequest.getAssertionStatus();
			switch( assertionStatus )
			{
			case FAILED :
			{
				testStepResult.setStatus( TestStepStatus.FAILED );
				if( getAssertionCount() == 0 )
				{
					testStepResult.addMessage( "Invalid/empty response" );
				}
				else
					for( int c = 0; c < getAssertionCount(); c++ )
					{
						WsdlMessageAssertion assertion = getAssertionAt( c );
						AssertionError[] errors = assertion.getErrors();
						if( errors != null )
						{
							for( AssertionError error : errors )
							{
								testStepResult.addMessage( "[" + assertion.getName() + "] " +  error.getMessage() );
							}
						}
					}

				break;
			}
			}
		}

		return testStepResult;
	}

	private class InternalTestPropertyListener extends TestPropertyListenerAdapter
	{
		@Override
		public void propertyAdded( String name )
		{
			RestTestRequestStep.this.addProperty( new RestTestStepProperty( getTestRequest().getProperty( name ) ), true );
		}

		@Override
		public void propertyRemoved( String name )
		{
			RestTestRequestStep.this.deleteProperty( name, true );
		}

		@Override
		public void propertyRenamed( String oldName, String newName )
		{
			RestTestRequestStep.this.propertyRenamed( oldName );
		}

		@Override
		public void propertyValueChanged( String name, String oldValue, String newValue )
		{
			RestTestRequestStep.this.firePropertyValueChanged( name, oldValue, newValue );
		}

		@Override
		public void propertyMoved( String name, int oldIndex, int newIndex )
		{
			RestTestRequestStep.this.firePropertyMoved( name, oldIndex, newIndex );
		}
	}

	private class RestTestStepProperty implements TestStepProperty
	{
		private RestParamProperty property;

		public RestTestStepProperty( RestParamProperty property )
		{
			this.property = property;
		}

		public TestStep getTestStep()
		{
			return RestTestRequestStep.this;
		}

		public String getName()
		{
			return property.getName();
		}

		public String getDescription()
		{
			return property.getDescription();
		}

		public String getValue()
		{
			return property.getValue();
		}

		public String getDefaultValue()
		{
			return property.getDefaultValue();
		}

		public void setValue( String value )
		{
			property.setValue( value );
		}

		public boolean isReadOnly()
		{
			return false;
		}

		public QName getType()
		{
			return property.getType();
		}

		public ModelItem getModelItem()
		{
			return getTestRequest();
		}
	}
}
