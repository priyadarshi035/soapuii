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

/**
 * Goal which touches a timestamp file.
 *
 * @goal generate
 * 
 * @phase process-sources
 */
public class Generate
    extends AbstractProxyGenerateGoal
{
	final protected boolean ziponly = false;
	final protected boolean nozip = false;

    public void execute()
        throws MojoExecutionException
    {
		getLog().info("out: " + output);
		getLog().info("listen: " + listenuri);
		getLog().info("output uri: " + outputuri);
		getLog().info("properties: " + propertiesfile);
		getLog().info("nozip: " + nozip);
		getLog().info("ziponly: " + ziponly);
		getLog().info("sources dir: " + sources);
		getLog().info("target dir: " + target);
    }
}
