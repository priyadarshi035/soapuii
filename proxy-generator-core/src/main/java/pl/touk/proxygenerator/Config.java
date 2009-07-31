/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.proxygenerator;

/**
 *
 * @author pnw
 */
public class Config
{
	private String output;
	private String listenUri;
	private String outputUri;
	private String propertiesFile;
	private boolean noPackage;
	private boolean noZip;
	private String sources;

	public Config(String output, String listenUri, String outputUri, String propertiesFile, boolean noPackage, boolean noZip, String sources)
	{
		this.output = output;
		this.listenUri = listenUri;
		this.outputUri = outputUri;
		this.propertiesFile = propertiesFile;
		this.noPackage = noPackage;
		this.noZip = noZip;
		this.output = output;
		this.sources = sources;
	}

	public String getSUMetaInfPath() { return getSUPath() + "/META-INF"; }
	public String getSAMetaInfPath() { return getSAPath() + "/META-INF"; }
	public String getSAPath() { return output + "-sa"; }
	public String getSUPath() { return getSAPath() + "/" + getSUName(); }
	public String getSUName() { return output + "-http-su"; }
	public String getOutput() { return output; }
	public String getListenUri() { return listenUri; }
	public String getOutputUri() { return outputUri; }
	public String getPropertiesFile() { return propertiesFile; }
	public boolean getNoPackage() { return noPackage; }
	public boolean getNoZip() { return noZip; }
	public String getSources() { return sources; }
}
