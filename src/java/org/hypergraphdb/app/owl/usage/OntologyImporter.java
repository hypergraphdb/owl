package org.hypergraphdb.app.owl.usage;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Date;

import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.app.owl.HGOntologyManagerFactory;
import org.hypergraphdb.app.owl.versioning.distributed.OntologyDatabasePeer;

/**
 * OntologyImporter imports all versioned ontologies from a directory
 * interactively one by one from VOWLXML Format.
 * 
 * TODO - this should be moved into a "tools" package alongside
 * possibly other command line programs, such running a standalone server
 * etc.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Sep 14, 2012
 */
public class OntologyImporter
{

	/**
	 * @param argv
	 *            vowlxmlDirectory localDBDir
	 */
	public static void main(String[] argv)
	{
		if (argv.length != 2)
		{
			help();
			System.exit(0);
		}
		File source = new File(argv[0]);
		File target = new File(argv[1]);
		validateDir(source);
		validateDir(target);
		importAll(source, target);
		System.out.println("Thanks for using OntologyImporter.");
	}

	public static void validateDir(File dir)
	{
		if (!dir.isDirectory() || !dir.exists() || !dir.canRead())
		{
			System.err.println("Parameter" + dir + " not acceptable, because (!dir or !exists or !canRead) ");
			System.exit(-1);
		}
	}

	public static void help()
	{
		System.out.println("Interactively imports all versioned OWL/XML files from a directory into a Hypergraph HGOWL repository");
		System.out.println("java OntologyImporter vowlxmlDirectory hgowlRepository");
		System.out.println();
		System.out.println("  vowlxmlSourceDirectory");
		System.out
				.println("      Specifies a source directory that contains one or more ontology files in Versioned OWL/XML format.");
		System.out.println();
		System.out.println("  hgowlRepositoryTargetDirectory");
		System.out.println("      Specifies a target directory that contains a hgowl repository.");
		System.out.println("      If hgowlRepositoryDirectory is an empty directory a hgowl repository will be created.");
		System.out.println();
		System.out.println("Both directories must exist.");
	}

	public static void importAll(File sourceDir, File targetRepo)
	{
		System.out.println("**************************************************");
		System.out.println("* VERSIONED ONTOLOGY IMPORTER STARTED AT " + new Date());
		System.out.println("  Repository  location: " + targetRepo);
		System.out.println("  Import file location: " + sourceDir);
		System.out.print("  Continue?[y/n] ");
		if (!userInput().equals("y"))
		{
			System.out.println("  Import cancelled ");
			return;
		}
		if (!targetRepo.exists())
		{
			System.out.println("DB Directory does not exist: " + targetRepo);
			return;
		}
		System.out.print("  Starting repository... ");
		OntologyDatabasePeer dr = null;
		HGDBOntologyManager manager = HGOntologyManagerFactory.getOntologyManager(targetRepo.getAbsolutePath());
		dr = (OntologyDatabasePeer) manager.getOntologyRepository();
		System.out.println("done. ");
		System.out.print("  Scanning directory... ");
		if (!(sourceDir.exists() && sourceDir.isDirectory()))
		{
			System.out.println("Source Directory does not exist or is not a dir: " + sourceDir.getAbsolutePath());
			return;
		}
		System.out.println("done. ");
		String[] importFiles = sourceDir.list();
		for (String importFileStr : importFiles)
		{
			File importFile = new File(sourceDir + "\\" + importFileStr);
			System.out.print("Start Import of " + importFile + "?[y/n]");
			if (!userInput().equals("y"))
			{
				System.out.println("Skipping: " + importFileStr);
				continue;
			}
			try
			{
				manager.importVersionedOntology(importFile);
			}
			catch (Exception e)
			{
				System.out.print("Import of " + importFile + "failed with: " + e + ".\r\n Continue?[y/n]");
				if (!userInput().equals("y"))
				{
					System.out.println("Throwing import failed exception:");
					throw new RuntimeException(e);
				}
				else
				{
					continue;
				}
			}
		}
		System.out.println("No more files in import directory.");
		dr.printStatistics();
	}

	private static String userInput()
	{
		String retVal;
		try
		{
			BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in));
			retVal = userInputReader.readLine().trim();
			// not sys in userInputReader.close();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		return retVal;
	}
}