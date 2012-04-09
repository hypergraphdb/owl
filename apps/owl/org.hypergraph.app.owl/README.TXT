Version history:
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
