/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.proxygenerator.support;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Recursivly traverse given directory, and returns filtered files
 *
 * @author pnw
 */
public class FileTraversal
{
	public final List<File> traverse(final File f) throws IOException
	{
		return traverse(f, null);
	}
	
	public final List<File> traverse(final File f, FileFilter filter) throws IOException
	{
		ArrayList<File> files = new ArrayList();

		if (f.isDirectory())
		{
			onDirectory(f);

			File[] childs;
			if (filter != null)
				childs = f.listFiles(filter);
			else
				childs = f.listFiles();

			for (File child : childs)
				if (child.isFile())
					onFile(child);

			files.addAll(Arrays.asList(childs));
			childs = f.listFiles();
			
			for (File child : childs)
				files.addAll(traverse(child, filter));
		}
		
		return files;
	}

	public void onDirectory(final File d) {}

	public void onFile(final File f) throws IOException {}
}
