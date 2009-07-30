/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.proxygenerator.support;

import java.io.File;
import java.io.FileFilter;

/**
 *
 * @author pnw
 */
public class ExtFileFilter implements FileFilter
{

	private String ext;

	public ExtFileFilter(String ext)
	{
		this.ext = ext;
		if (!ext.startsWith("."))
		{
			ext = new String("." + ext);
		}
	}

	public boolean accept(File path)
	{
		return path.getName().endsWith(ext) && path.isFile();
	}
}
