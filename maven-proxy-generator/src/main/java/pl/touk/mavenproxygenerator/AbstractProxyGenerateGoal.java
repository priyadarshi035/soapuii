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
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;

abstract public class AbstractProxyGenerateGoal
    extends AbstractMojo
{
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
     * @parameter expression="${generate.output}"
	 * @required
     */
	protected	String output;

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

    abstract public void execute()
        throws MojoExecutionException;

}
