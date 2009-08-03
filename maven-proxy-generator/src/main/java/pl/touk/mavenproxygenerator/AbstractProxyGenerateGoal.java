package pl.touk.mavenproxygenerator;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;

import java.io.File;
import org.apache.log4j.Logger;
import org.apache.maven.plugin.MojoFailureException;
import pl.touk.proxygenerator.*;

abstract public class AbstractProxyGenerateGoal
    extends AbstractMojo
{
	private final static Logger log = Logger.getLogger(AbstractProxyGenerateGoal.class);
    /**
     * Location of the file.
     * @parameter expression="${project.build.directory}"
     * @required
     */
    protected File target;

    /**
     * Location of the file.
     * @parameter expression="${project.build.sourceDirectory}"
     * @required
     */
    protected File sources;

    /**
     * Output's name.
     * @parameter expression="${project.build.outputDirectory}"
	 * @required
     */
	protected String output;

	/**
     * Proxy default listen uri
     * @parameter expression="${generate.listenuri}"
     * @required
     */
	protected String listenuri;

    /**
     * Location of the file.
     * @parameter expression="${generate.outputuri}"
	 * @required
     */
	protected String outputuri;

    /**
     * Location of the file.
     * @parameter expression="${generate.propertiesfile}" default-value="http.uri.properties"
     */
	protected String propertiesfile;

	abstract public boolean getNoZip();
	abstract public boolean getNoPackage();

    public void execute()
        throws MojoFailureException
    {
		try
		{
			Config config = new Config(output, target.getPath(), listenuri, outputuri, propertiesfile, getNoPackage(), getNoZip(), sources.getPath());
			Core core = new Core(config);
			core.run();
		} catch (ProxyGeneratorException ex)
		{
			log.error(ex.toString(), ex);
			throw new MojoFailureException(ex.getMessage());
		}
    }
}
