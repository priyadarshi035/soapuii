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

public class FilenameFilterClass implements FileFilter
{

	private String filename;

	public FilenameFilterClass(String filename)
	{
		this.filename = filename;
	}

	public boolean accept(File path)
	{
		return path.getName().equals(filename) && path.isFile();
	}
}
