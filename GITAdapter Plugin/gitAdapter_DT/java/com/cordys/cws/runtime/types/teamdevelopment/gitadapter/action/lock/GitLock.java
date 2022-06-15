package com.cordys.cws.runtime.types.teamdevelopment.gitadapter.action.lock;

import java.util.Date;

public class GitLock
{

	private final String myPath;

	private final String myID;

	private final String myOwner;

	private final String myComment;

	private final Date myCreationDate;

	private final Date myExpirationDate;

	/**
	 * <p>
	 * Constructs an <b>GitLock</b> object.
	 * 
	 * 
	 * @param path
	 *          a file path, relative to the repository root directory
	 * @param id
	 *          a string token identifying the lock
	 * @param owner
	 *          the owner of the lock
	 * @param comment
	 *          a comment message for the lock (optional)
	 * @param created
	 *          a datestamp when the lock was created
	 * @param expires
	 *          a datestamp when the lock expires, i.e. the file is unlocked (optional)
	 */
	public GitLock(String path, String id, String owner, String comment, Date created, Date expires)
	{
		myPath = path;
		myID = id;
		myOwner = owner;
		myComment = comment;
		myCreationDate = created;
		myExpirationDate = expires;
	}

	/**
	 * Gets the lock comment.
	 * 
	 * @return a lock comment message
	 */
	public String getComment()
	{
		return myComment;
	}

	/**
	 * Gets the creation datestamp of this lock.
	 * 
	 * @return a datestamp representing the moment in time when this lock was created
	 */
	public Date getCreationDate()
	{
		return myCreationDate;
	}

	/**
	 * Gets the expiration datestamp of this lock.
	 * 
	 * @return a datestamp representing the moment in time when the this lock expires
	 */
	public Date getExpirationDate()
	{
		return myExpirationDate;
	}

	/**
	 * Gets the lock token.
	 * 
	 * @return a unique string identifying this lock
	 */
	public String getID()
	{
		return myID;
	}

	/**
	 * Gets the lock owner.
	 * 
	 * @return the owner of this lock
	 */
	public String getOwner()
	{
		return myOwner;
	}

	/**
	 * Gets the path of the file for which this lock was created. The path is relative to the repository root directory.
	 * 
	 * @return the path of the locked file
	 */
	public String getPath()
	{
		return myPath;
	}

	/**
	 * Returns a string representation of this object.
	 * 
	 * @return a string representation of this lock object
	 */
	public String toString()
	{
		StringBuilder result = new StringBuilder(64);
		result.append("path=").append(myPath).append(", token=").append(myID).append(", owner=").append(myOwner);
		if (myComment != null)
		{
			result.append(", comment=").append(myComment);
		}
		result.append(", created=").append(myCreationDate);
		if (myExpirationDate != null)
		{
			result.append(", expires=").append(myExpirationDate);
		}
		return result.toString();
	}
}
