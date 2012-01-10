Version history:

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
