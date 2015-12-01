package org.hypergraphdb.app.owl.versioning;


/**
 * <p>
 * Represents a change to a versioned object. The methods in this interface 
 * support version management operations such as reverting to a previous
 * revision, normalizing a set of changes to remove redundancy and conflict
 * detection.
 * </p>
 * 
 * @author Borislav Iordanov
 *
 * @param <T>
 */
public interface Change<T extends Versioned<T>>
{	
	/**
	 * Enact this change to the versioned object. Note that applying a change
	 * may be a complex operation involving a query and then a database write.
	 * A complex database operations should of course be performed within a 
	 * transaction. However, the <code>apply</code> implementations of this
	 * interface will assume that they are already executing within a transaction.
	 * The versioning API already ensures that transactions are created at the 
	 * appropriate sites. But if you are developing extensions of the framework, 
	 * please call <code>apply</code> only within a transaction. 
	 */
	void apply(T versioned);
	
	/**
	 * Construct and return <code>VChange</code> that will reverse the effect
	 * of this change. Every implementation must be reversible and return
	 * non-null from this method. 
	 */
	Change<T> inverse();
	
	/**
	 * <p>
	 * Create a new change object that would represent an equivalent change 
	 * operation to apply the <code>previous</code> argument and then <code>this</code>
	 * change. This is an optional operation to be implemented whenever it makes
	 * sense for a pair of changes to be combined into a single change object. The
	 * operation is useful during a normalization process where a list of consecutive changes
	 * is reduced to a minimum length list that is equivalent to the original one.
	 * </p>
	 * 
	 * @param previous A change that was (or will be) applied before <code>this</code> one. 
	 * @return A new change object to which the sequence of <code>previous</code> then
	 * <code>this</code> can be reduced, or <code>null</code> if such a merge of the
	 * two operations is not possible.
	 */
	Change<T> reduce(Change<T> previous);
	
	/**
	 * Return <code>true</code> if there is a conflict between
	 * this change and the <code>other</code> argument. Return 
	 * <code>false</code> if there is no conflict and the two
	 * changes can be applied simultaneously, or in arbitrary
	 * order.
	 */
	boolean conflictsWith(Change<T> other);
	
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