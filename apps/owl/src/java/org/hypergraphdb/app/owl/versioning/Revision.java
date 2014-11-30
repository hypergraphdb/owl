package org.hypergraphdb.app.owl.versioning;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hypergraphdb.peer.serializer.HGPeerJsonFactory;

/**
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 18, 2012
 */
public class Revision extends RevisionID implements VersioningObject
{
	public static final long TIMESTAMP_UNDEFINED = Long.MIN_VALUE;
	public static final String USER_ANONYMOUS = null;

	public static DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

	private Date timeStamp;
	private String user;
	private String revisionComment;

	public Revision()
	{
	}

	public Date getTimeStamp()
	{
		return timeStamp;
	}

	/**
	 * @param timeStamp
	 *            the timeStamp to set
	 */
	public void setTimeStamp(Date timeStamp)
	{
		this.timeStamp = timeStamp;
	}

	public String getUser()
	{
		return user;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	public void setUser(String user)
	{
		this.user = user;
	}

	public String getRevisionComment()
	{
		return revisionComment;
	}

	/**
	 * @param comment
	 *            the comment to set.
	 */
	public void setRevisionComment(String comment)
	{
		this.revisionComment = comment;
	}

	public String toString()
	{
		return super.toString() + " by " + user + " at " + (timeStamp == null ? "NA" : dateFormat.format(timeStamp));
	}

	@Override
	public void accept(VOWLObjectVisitor visitor)
	{
		visitor.visit(this);
	}

	@Override
	public int hashCode()
	{
		return super.hashCode()
				+ 7
				* ((user != null ? user.hashCode() : 0) + (timeStamp != null ? timeStamp.hashCode() : 0) + (revisionComment != null ? revisionComment
						.hashCode() : 0));
	}

	@Override
	public boolean equals(Object anObject)
	{
		if (this == anObject)
		{
			return true;
		}
		if (anObject instanceof Revision)
		{
			Revision other = (Revision) anObject;
			return super.equals(other) && equalsInclNull(user, other.user) && equalsInclNull(timeStamp, other.timeStamp)
					&& equalsInclNull(revisionComment, other.revisionComment);
		}
		else
		{
			return false;
		}
	}

	protected boolean equalsInclNull(Object thiso, Object other)
	{
		return (thiso == null && other == null) || (thiso != null && thiso.equals(other));
	}

	public static void main(String[] argv)
	{
		HGPeerJsonFactory fac = HGPeerJsonFactory.getInstance();
		Revision rev = new Revision();
		rev.setTimeStamp(null);
		rev.setRevision(43);
		rev.setUser("asdfasd");
		rev.setRevisionComment("as asd ada fasdfasd");
		System.out.println(fac.make(rev));
		List<Revision> L = new ArrayList<Revision>();
		L.add(rev);
		L.add(new Revision());
		System.out.println(fac.make(L));
	}
}