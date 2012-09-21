Version history:
1.6.0 Team (Release Date: 2012.09.21)
++ Core:
	+ All changes from versioning operations and p2p operations are now applied through the manager, to notify all listeners (reasoner!)
	+ Conflicts (double additions, double removals) that may occur in the workingset during reverts or updates are now handled properly.
++ Integration Plugin:
	+ Overall UI improvements.
	+ OWL Rendering of Changes in all ChangesetViews
	+ Conflicts in the workingset will be highlighted.
	+ Changes in Threading. Swing thread will not be used to propagate changes coming from P2P activities.  
1.5.0 Team (Release Date: 2012.09.13)
++ Core: 
	+ Client Server Versioning
	+ Import VOWLXML
	+ Changeset: Reapply pending changes after revert (Noops: double additions, double deletions)
	+ OntologyServer configurable on P2P.
	+ New VersionedOntologyComparator 
1.0.0 JE P2P Patch (Release Date: 2012.08.17)
++ BUGIFX: Nested transactions were used in JE Storage Implementation. They are not supported 
           currently supported by BerkelyDB 11g2 Java Edition 5.0.34. 
1.0.0 JE SWRL Patch (Release Date: 2012.08.08)
++BUGFIX: SWRLAtomHGDB's predicate was not properly detected as bean property in subclass instances.
++Added: R20120807RepairSWRLNull will find old SWRLAtomHGDB instances in existing repositoryies and upgrade their type.
1.0.0 JE (Release Date: 2012.07.23)
++ Core
	+ Database is now 11gR2 Berkeley DB Java Edition 5.0.34
	+ Improved performance
	- OS native Berkeley DB removed
1.0.0 (Release Date: 2012.04.06)
++ Core
	+ Extensive Testing and >40 bugfixes in the model
		+ First time all 1442 OWL API tests pass, after testclasses were modified to ensure proper testing.
	+ P2P Functionality based on Smack XMPP
		+ Push 
		+ Pull
		+ Pull allows merge with uncommitted local changes after old head.
	+ Serialization
		+ VOWLXML Format: Parser, Renderer extend OWLXML by VersionedOntology, Revision, Changeset, Changes.		
	+ Berkeley DB 5.3 used, performance seems low.

++ Integration Plugin
	+ History view showing individual changes per revision
	+ P2P Settings
	+ P2P Menu with: Push active
	
0.9.8 (Release Date: 2012.02.07)
++ Core
	+ Local Versioning of Ontologies
		+ add/remove from Version Control
		+ Commit/Rollback/Revert
		+ Commit comments, timestamp stored 
		+ A Revert/Rollback looses information
		+ Exactly one branch (master) supported
	+ Optimization:
		+ Importing a large Ontology with 150K Axioms now 2 mins (old: > 1h)
		(By using Axiom HashCode index)
		+ Improved garbage collector speed
		+ Improved overall storage traversal performance 
		(By using improved thread synchronization in Hypergraph)

++ Integration Plugin:
	+ Versioned Ontologies have a specific symbol
	+ Dialogs and Actions for Local Versioning 
	+ Faster Repository View with Versioning Information. 
	+ Ontology View showing Revisions and Changesets	
	
0.9.6 (Release Date: 2012.01.10)
	+ Berkeley DB 5.2. used, performance restored

0.9.5 (Date 2012.01.09)
++ Core:
   + Performance Improvements
	 + Builting Entity Cache
	 + Builting Entity Cache Statistics
	 + New more efficient Hypergraph revision used
		+ Faster Class loading w/o exceptions
		+ Less String creation/conversion
	 + Berkeley DB 5.3. used, later found to result in decreased performance (10x) 
   + Improved Garbage Collection

++ Integration Plugin:
   + Delete Ontology from Repository
   + Open Ontology from Repository
   + OWL Import Declaration Resolution with HGDB Repository Lookup
   + Overwrite/Delete Ontologies 
   + Improved GUI responsiveness when large ontologies are loaded.

++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
0.9.0 (Release Date: 2011.12.09)
Features:
+ Complete OWL 2.0 implementation based on Hypergraph DB
+ Cleanup of unused entities
+ Hypergraph DB with Berkeley 5.0 used.
