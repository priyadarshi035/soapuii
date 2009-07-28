/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.proxygenerator.deployparser;

/**
 *
 * @author azl
 */
public class Endpoint
{
	private String endpoint;
	private String locationURI;
	private String role;
	private String soap;
	private String soapVersion;
	private String service;
	private String wsdlResource;

	public Endpoint()
	{
	}

	public Endpoint(String endpoint, String locationURI, String role, String soap, String service, String soapVersion, String wsdlResource)
	{
		super();

		this.endpoint = endpoint;
		this.locationURI = locationURI;
		this.role = role;
		this.soap = soap;
		this.service = service;
		this.soapVersion = soapVersion;
		this.wsdlResource = wsdlResource;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getLocationURI() {
		return locationURI;
	}

	public void setLocationURI(String locationURI) {
		this.locationURI = locationURI;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getSoap() {
		return soap;
	}

	public void setSoap(String soap) {
		this.soap = soap;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getWsdlResource() {
		return wsdlResource;
	}

	public void setWsdlResource(String wsdlResource) {
		this.wsdlResource = wsdlResource;
	}

	public String getSoapVersion() {
		return soapVersion;
	}

	public void setSoapVersion(String soapVersion) {
		this.soapVersion = soapVersion;
	}


	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(" { Endpoint Atributes --");
		sb.append("endpoint:" + getEndpoint());
		sb.append(", ");
		sb.append("locationURI:" + getLocationURI());
		sb.append(", ");
		sb.append("role:" + getRole());
		sb.append(", ");
		sb.append("soap:" + getSoap());
		sb.append(", ");
		sb.append("service:" + getService());
		sb.append(", ");
		sb.append("wsdlResource:" + getWsdlResource());
		sb.append(". } \n ");

		return sb.toString();
	}

}
