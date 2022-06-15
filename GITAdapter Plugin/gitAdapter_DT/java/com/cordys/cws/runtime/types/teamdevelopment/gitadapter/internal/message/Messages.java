package com.cordys.cws.runtime.types.teamdevelopment.gitadapter.internal.message;

import com.cordys.cws.internal.localization.MessageBundle;
import com.cordys.cws.internal.localization.MessageGenerator;
import com.cordys.cws.internal.localization.MessageText;
import com.eibus.localization.IStringResource;

@SuppressWarnings("PMD.LongVariable")
@MessageBundle("Cordys.CWS.GITAdapter.Messages")
public class Messages
{
	@MessageText("Upgrade the GIT working copy.")
	public static final IStringResource UPGRADE_GIT_WORKING_COPY = getMessage();

	@MessageText("Upgrade the GIT working copy.")
	public static final IStringResource CMD_UPGRADE_GIT_WORKING_COPY_SUMMARY = getMessage();

	@MessageText("Upgrade the GIT working copy for one or more workspaces. Upgrade either a specific workspace or all the workspaces in a single organization, or all the workspaces in all the organizations.")
	public static final IStringResource CMD_UPGRADE_GIT_WORKING_COPY_USAGE = getMessage();

	@MessageText("Specify the name of the organization.")
	public static final IStringResource CMD_UPGRADE_GIT_WORKING_COPY_ORGANIZATION_USAGE = getMessage();

	@MessageText("Specify the name or ID of the workspace.")
	public static final IStringResource CMD_UPGRADE_GIT_WORKING_COPY_WORKSPACE_USAGE = getMessage();

	@MessageText("Upgrade the available workspaces in all the organizations.")
	public static final IStringResource CMD_UPGRADE_GIT_WORKING_COPY_WORKSPACE_ALL_ORGANIZATIONS_USAGE = getMessage();

	@MessageText("Workspace ''{0}'' is based on the GITversion {1} working copy format, whereas GITversion {1} is no longer supported. Contact your system administrator to upgrade the working copy of this workspace to a supported version.")
	public static final IStringResource UNSUPPORTED_GIT_WORKING_COPY_VERSION_OPEN = getMessage();

	@MessageText("Workspace ''{0}'' cannot be created. It is configured to use GIT for team development and has {1} as its GIT working copy version. Version {1} is not a supported version. Your system administrator must upgrade the GIT working copy version.")
	public static final IStringResource UNSUPPORTED_GIT_WORKING_COPY_VERSION_CREATE = getMessage();

	@MessageText("You cannot modify document ''{0}'' because it cannot be found in the GIT repository. Incorporate the latest changes and try again.")
	public static final IStringResource CANNOT_LOCK_DOCUMENT_SINCE_NOT_FOUND_FIRST_INCORPORATE = getMessage();

	private static IStringResource getMessage()
	{
		return MessageGenerator.getInstance(Messages.class).nextMessage();
	}

}
