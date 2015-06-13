package org.hypergraphdb.app.owl.versioning;

import java.util.Set;

/**
 * <p>
 * Represents the state of a {@link Versioned} object in a way that it can
 * be compared to the state of other copies of the same object. This is 
 * essentially an abstraction of what one would call the "version" of an
 * object. 
 * </p>
 * 
 * <p>
 * Comparison gives a logical ordering of versions. Because version
 * history is not linear, this ordering is not trivial and can't simply
 * be obtained from a chronological ordering. For purposes of <code>VersionState</code>
 * comparison, we say the a state A is prior to state B (or that B is a 
 * subsequent to A) if B was reached from A as a result of new revisions being
 * applied to A. The two states A and B are divergent if there are revisions applied
 * to A that are not part of B and vice versa (i.e. when the two state evolved in
 * parallel rather than in sequence).   
 * </p>
 * 
 * <p>
 * The intent behind the <code>VersionState</code> concept is not only to compare
 * two versioned objects, but also to compute deltas: given version state A
 * and version state B for a versioned object V, compute all necessary changes to
 * reach B from A, for example. The delta is expressed as a set of {@link Revision}s.
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public interface VersionState<T extends Versioned<T>>
{
	enum Compared
	{
		same,
		prior,
		divergent,
		subsequent
	}
	
	/**
	 * Return the {@link Compared} result from comparing this 
	 * <code>VersionState</code> with some <code>otherState</code>.
	 */
    Compared compare(VersionState<T> otherState);
    
    /**
     * Return the set of revisions that need to be applied to the versioned
     * having <code>this</code> state, in order to reach the state 
     * of <code>other</code>.
     * @param other
     */
    Set<Revision> delta(Versioned<T> other);
}