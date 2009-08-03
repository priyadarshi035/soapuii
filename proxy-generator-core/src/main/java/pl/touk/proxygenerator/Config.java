/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.proxygenerator;

import java.io.File;

/**
 *
 * @author pnw
 */
public class Config
{
	private String target;
	private String output;
	private String listenUri;
	private String outputUri;
	private String propertiesFile;
	private boolean noPackage;
	private boolean noZip;
	private String sources;

	public Config(String output, String target, String listenUri, String outputUri, String propertiesFile, boolean noPackage, boolean noZip, String sources)
	{
		this.output = output;
		this.target = target;
		this.listenUri = listenUri;
		this.outputUri = outputUri;
		this.propertiesFile = propertiesFile;
		this.noPackage = noPackage;
		this.noZip = noZip;
		this.output = output;
		this.sources = sources;
	}

	public String getSUMetaInfPath() { return getSUPath() + File.separatorChar + "META-INF"; }
	public String getSAMetaInfPath() { return getSAPath() + File.separatorChar + "META-INF"; }
	public String getTmpPath() { return target + File.separatorChar + "proxy-generator-build-tmp"; }
	public String getSAPath() { return target + File.separatorChar + getSAName(); }
	public String getSUPath() { return getSAPath() + File.separatorChar + getSUName(); }
	public String getSUName() { return output + "-http-su"; }
	public String getSAName() { return output + "-sa"; }
	public String getOutput() { return output; }
	public String getListenUri() { return listenUri; }
	public String getOutputUri() { return outputUri; }
	public String getPropertiesFile() { return propertiesFile; }
	public boolean getNoPackage() { return noPackage; }
	public boolean getNoZip() { return noZip; }
	public String getSources() { return sources; }
	public String getTargetPath() { return target; }
}
