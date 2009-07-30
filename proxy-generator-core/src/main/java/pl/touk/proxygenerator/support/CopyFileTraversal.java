/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.proxygenerator.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author pnw
 */
public class CopyFileTraversal extends FileTraversal
{
	private final static Logger log = Logger.getLogger(CopyFileTraversal.class);
	protected URI baseCopyDir;
	protected URI baseDir;
	
	public CopyFileTraversal(File baseStartDir, File baseCopyDir)
	{
		this.baseDir = baseStartDir.toURI();
		this.baseCopyDir = baseCopyDir.toURI();
	}

	@Override
	public void onFile(final File f) throws IOException
	{
		URI absPath = f.toURI();
		URI relPath = baseDir.relativize(absPath);
		URI copyPath = baseCopyDir.resolve(relPath);
		File copyFile = new File(copyPath);
		
		log.info("Coping file: " + f + " to: " + copyFile);
		IOUtils.copy(new FileInputStream(f), new FileOutputStream(copyFile));
	}

}
