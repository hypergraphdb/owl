package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.app.owl.newver.Versioned;

/**
 * Represents a change to a versioned object. The methods in this interface 
 * support version management operations such as reverting to a previous
 * revision, normalizing a set of changes to remove redundancy and conflict
 * detection.
 * 
 * @author Borislav Iordanov
 *
 * @param <T>
 */
public interface VChange<T extends Versioned<?>>
{	
	/**
	 * Enact this change to the versioned object.
	 */
	void apply(T versioned);
	
	/**
	 * Construct and return <code>VChange</code> that will reverse the effect
	 * of this change. Every implementation must be reversible and return
	 * non-null from this method. 
	 */
	VChange<T> inverse();
	
	/**
	 * Return <code>true</code> if there is a conflict between
	 * this change and the <code>other</code> argument. Return 
	 * <code>false</code> if there is no conflict and the two
	 * changes can be applied simultaneously, or in arbitrary
	 * order.
	 */
	boolean conflictsWith(VChange<T> other);
	
	/**
	 * Return <code>true</code> if when applied this change will actually
	 * modify the versioned object and <code>false</code> otherwise.
	 */
	boolean isEffective(T versioned);
	
	/**
	 * Return <code>true</code> is this concrete type of change is idempotent 
	 * and <code>false</code> otherwise. Idempotent changes change be applied
	 * multiple times to the same effect. That is, applying an idempotent change
	 * more than once is pointless. The framework will use this attribute of a
	 * change type to decide whether some changes are superfluous and can be removed
	 * from a change set.
	 */
	boolean isIdempotent();
}