# OWL in HyperGraphDB

The Ontology Web Language (OWL) 2.0 is a semantic web standard based on Description Logics (DL). More information on the [W3C OWL 2.0 home page](http://www.w3.org/TR/owl2-overview/) as well as at the [OWL 2 Wikipedia topic](http://en.wikipedia.org/wiki/Web_Ontology_Language).

The HGDBOWL module offers a **complete, persistent, fully transactional** implementation of the `de facto` standard [OWLAPI](http://owlapi.sourceforge.net/) for working with OWL ontologies. Each ontology is naturally represented as a sub-graph within a HyperGraphDB instance, thanks to the native nested graph support. Therefore:

* Thus, arbitrarily large ontologies that don't necessarily fit in memory can be stored.

* You can program against the standard OWLAPI, including using any reasoner of your choice and remain insulated from HyperGraphDB dependencies. 

* You can also make use of HyperGraphDB querying and graph traversal capabilities to retrieve ontological information.

* Multiple ontologies can be independently stored in the same database.

* You can freely mix ontologies persisted in a database with file-based ontology (one importing the other).

* Of course, you can also open multiple database repository simultaneously, each which its own set of ontologies. This gives you a very fine-grained control of managing your ontology data at a fairly large scale. 

**Where to go next?**

* [Setting Up HGDB-OWL Development](SettingUpHGDBOWLDevelopment)
* [First Steps with the HGDB-OWL API](FirstStepswiththeHGDBOWLAPI)
* [Under the Hood - Some HGDB-OWL Implementation Notes](UndertheHoodSomeHGDBOWLImplementationNotes)
* [Accessing the OWL Representation in HyperGraphDB](AccessingtheOWLRepresentationinHyperGraphDB)
* <a target="_blank" href="http://www.hypergraphdb.org/docs/apps/owl/index.html">HGDB-OWL Module Javadocs</a>

## Versioning

Creating ontologies is like any other kind of software development activity, coding that is. We create a model of the world and it gets complex, we may need to collaborate with others, we will make mistakes and may want to revert back to a previous version of our model. So OWL development at a large scale is best done under a modern distributed version control system. 

The HGDB-OWL module offers such a system with the following features:

* Versioning and diffs performed at the axiom level, not text.
* Standard push, pull, commit, revert etc. operations.
* Branching & merging for collaborative parallel development.
* Truly distributed P2P development (no centralized repository necessary).

**Where to go next?**

* [Versioning Concepts](VersioningConcepts)
* [Maintaining Change History of an Ontology](MaintainingChangeHistoryofanOntology)
* [Communicating with Remote Peers](CommunicatingwithRemotePeers)
* [Branching and Merging](BranchingandMerging)
* [Revision Labels](RevisionLabels)
* [Setting Up an Ontology Repository](SettingUpanOntologyRepository)

## Protege Integration

The HyperGraphDB backed OWL implementation is also the foundation of a <a target="_blank" href="http://protege.stanford.edu/">Protege</a> plugin which exposes all that functionality in a natural way from the Protege UI. The Protege plugin makes it seamless to store very large ontologies in a graph database with the full support of distributed version control needed for large-scale ontology development by teams of any size. 

The plugin's official page is at <a target="_blank" href="http://hypergraphdb.org/?project=protegeowl&page=Home"></a> and the Github repository is at https://github.com/hypergraphdb/protegeowl

**Where to go next?**

* <a target="_blank" href="http://hypergraphdb.org/?project=protegeowl&page=InstallingProtegeHGDB">Install the Protege plugin</a>
* <a target="_blank" href="http://hypergraphdb.org/?project=protegeowl&page=ProtegeHGDBUserGuide">The HGDB Protege User Guide</a>
