package pl.touk.proxygenerator;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.*;

/**
 * Compresses a file or directory into a Zip archive. Users of the
 * class supply the name of the file or directory as an argument.
 */
public class SimpleZip
{

	private static ZipOutputStream zos;
	/**
	 * Creates a Zip archive. If the name of the file passed in is a
	 * directory, the directory's contents will be made into a Zip file.
	 *
	 * basePackingPath - path, to which packed files dir will be relativize
	 */
	public static void makeZip(String fileName, String outputName, String basePackingPath)
			throws IOException, FileNotFoundException, URISyntaxException
	{
		File file = new File(fileName);
		makeZip(file, outputName, basePackingPath);
	}

	public static void makeZip(File file, String outputName, String basePackingPath)
			throws IOException, FileNotFoundException, URISyntaxException
	{
		zos = new ZipOutputStream(new FileOutputStream(outputName));
		//Call recursion.
		recurseFiles(file, basePackingPath);
		//We are done adding entries to the zip archive,
		//so close the Zip output stream.
		zos.close();
	}

	/**
	 * Recurses down a directory and its subdirectories to look for
	 * files to add to the Zip. If the current file being looked at
	 * is not a directory, the method adds it to the Zip file.
	 *
	 * basePackingPath - path, to which packed files dir will be relativize
	 */
	private static void recurseFiles(File file, String basePackingPath)
			throws IOException, FileNotFoundException, URISyntaxException
	{
		if (file.isDirectory())
		{
			//Create an array with all of the files and subdirectories
			//of the current directory.
			String[] fileNames = file.list();
			if (fileNames != null)
			{
				//Recursively add each array entry to make sure that we get
				//subdirectories as well as normal files in the directory.
				for (int i = 0; i < fileNames.length; i++)
				{
					recurseFiles(new File(file, fileNames[i]), basePackingPath);
				}
			}
		} //Otherwise, a file so add it as an entry to the Zip file.
		else
		{
			byte[] buf = new byte[1024];
			int len;
			//Create a new Zip entry with the file's name.
			URI absPath = new URI(file.getPath());
			URI baseDir = new URI(basePackingPath);
			URI relPath = baseDir.relativize(absPath);

			String packingPath = relPath.toString();
			ZipEntry zipEntry = new ZipEntry(packingPath);
			//Create a buffered input stream out of the file
			//we're trying to add into the Zip archive.
			FileInputStream fin = new FileInputStream(file);
			BufferedInputStream in = new BufferedInputStream(fin);
			zos.putNextEntry(zipEntry);
			//Read bytes from the file and write into the Zip archive.
			while ((len = in.read(buf)) >= 0)
			{
				zos.write(buf, 0, len);
			}
			//Close the input stream.
			in.close();
			//Close this entry in the Zip stream.
			zos.closeEntry();
		}
	}
}
