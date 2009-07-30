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
	private boolean zipOnly;
	private boolean noZip;
	private String sources;

	public Config(String output, String listenUri, String outputUri, String propertiesFile, boolean zipOnly, boolean noZip, String sources)
			throws ProxyGeneratorConfigException
	{
		if (zipOnly && noZip)
			throw new ProxyGeneratorConfigException("zipOnly and noZip cannot be set simultaneously");

		this.output = output;
		this.listenUri = listenUri;
		this.outputUri = outputUri;
		this.propertiesFile = propertiesFile;
		this.zipOnly = noZip;
		this.output = output;
		this.sources = sources;
	}

	public String getSUMetaInf() { return getSUDir() + "/META-INF"; }
	public String getSAMetaInf() { return getSADir() + "/META-INF"; }
	public String getSADir() { return output + "-sa"; }
	public String getSUDir() { return getSADir() + "/" + output + "http-su"; }
	public String getOutput() { return output; }
	public String getListenUri() { return listenUri; }
	public String getOutputUri() { return outputUri; }
	public String getPropertiesFile() { return propertiesFile; }
	public boolean getZipOnly() { return zipOnly; }
	public boolean getNoZip() { return noZip; }
	public String getSources() { return sources; }
}
