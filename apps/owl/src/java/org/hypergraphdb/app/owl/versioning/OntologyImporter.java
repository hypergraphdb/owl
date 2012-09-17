package org.hypergraphdb.app.owl.versioning;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Date;

import org.hypergraphdb.app.owl.HGDBOWLManager;
import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository;


/**
 * OntologyExporter imports all versioned ontologies interactively one by one from VOWLXML Format.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Sep 14, 2012
 */
public class OntologyImporter {
	public static String LOCALDB_DIR = "C:\\OntologyServer\\hg";
	public static String IMPORT_DIR_VOWLXML = "C:\\_CiRM\\workspaceMD\\RepoMigration\\exported";
	
	/**
	 * @param argv ignored.
	 */
	public static void main(String[] argv) {
		importAll();
		System.out.println("Bye bye!");
	}
	
	public static void importAll() {
		File dbdir = new File(LOCALDB_DIR);
		System.out.println("**************************************************");
		System.out.println("* VERSIONED ONTOLOGY IMPORTER STARTED AT "+ new Date());
		System.out.println("  Repository  location: " + dbdir);
		System.out.println("  Import file location: " + IMPORT_DIR_VOWLXML);
		System.out.print("  Continue?[y/n] ");
		if (!userInput().equals("y")) {
			System.out.println("  Import cancelled ");
			return;
		}
		if (!dbdir.exists()) {
			System.out.println("DB Directory does not exist: " + dbdir);
			return;
		}
		System.out.print("  Starting repository... ");
		VDHGDBOntologyRepository dr = null;
		VDHGDBOntologyRepository.setHypergraphDBLocation(dbdir.getAbsolutePath());
		HGDBOntologyManager manager = HGDBOWLManager.createOWLOntologyManager();
		dr = (VDHGDBOntologyRepository) manager.getOntologyRepository();
		System.out.println("done. ");
		System.out.print("  Scanning directory... ");
		File dir = new File(IMPORT_DIR_VOWLXML);
		if (!(dir.exists() && dir.isDirectory())) {
			System.out.println("DB Directory does not exist or is not a dir: " + dir.getAbsolutePath());
			return;
		}
		System.out.println("done. ");
		String[] importFiles = dir.list();
		for (String importFileStr : importFiles) {
			File importFile = new File(IMPORT_DIR_VOWLXML + "\\" + importFileStr);
			System.out.print("Start Import of " + importFile + "?[y/n]");
			if (!userInput().equals("y")) {
				System.out.println("Skipping: " + importFileStr);
				continue;
			}
			try {
				manager.importVersionedOntology(importFile);
			} catch (Exception e) {
				System.out.print("Import of " + importFile + "failed with: " + e + ".\r\n Continue?[y/n]");
				if (!userInput().equals("y")) {
					System.out.println("Throwing import failed exception:");
					throw new RuntimeException(e);
				} else {
					continue;
				}
			}
		}		
		System.out.println("No more files in import directory.");
		dr.printStatistics();
	}
	
	private static String userInput() {
		String retVal;
		try {
			BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in));
			retVal = userInputReader.readLine().trim();
			// not sys in userInputReader.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return retVal;
	}
}
