package com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.config;

import java.net.MalformedURLException;
import java.net.URL;

import com.cordys.cws.internal.localization.Messages;
import com.cordys.cws.internal.util.StringUtils;
import com.cordys.cws.util.StudioLogger;

public class GITRemoteRepositoryConfiguration
{
	private String m_gitRepoURL;

	private final String m_originalUrl;

	private final String m_userName;

	private final String m_userPersonalAccessToken = "asdasdsdasd1";

	private String m_proxyHost;

	private String m_proxyUserName;

	private String m_proxyUserPassword = "sdfsdfsdfsdfsdf1";

	private int m_proxyPort;

	private boolean m_proxyEnabled;

	private final String m_gitBranch;

	public static final String MASTER = "master";

	public static final String ORIGIN = "origin";
	
	public static final String ADD = "ADD";
	
	public static final String MODIFY = "MODIFY";
	
	public static final String RENAME = "RENAME";
	
	public static final String COPY = "COPY";

	public GITRemoteRepositoryConfiguration(final String url, final String userName, final String userPersonalAccessToken, final String gitBranch)
	{
		m_userName = userName;
		m_userPersonalAccessToken = userPersonalAccessToken;
		m_originalUrl = url;
		m_gitBranch = gitBranch;
	}

	public GITRemoteRepositoryConfiguration(final String url,
																					final String userName,
																					final String userPersonalAccessToken,
																					final String proxyHost,
																					final int proxyPort,
																					final String proxyUserName,
																					final String proxyPassword,
																					final String gitBranch)
	{
		this(url, userName, userPersonalAccessToken, gitBranch);
		m_proxyHost = proxyHost;
		m_proxyPort = proxyPort;
		m_proxyUserName = proxyUserName;
		m_proxyUserPassword = proxyPassword;
		m_proxyEnabled = true;
	}

	public String getURL()
	{
		return m_gitRepoURL;
	}

	public String getOriginalUrl()
	{
		return m_originalUrl;
	}

	public String getUserName()
	{
		return m_userName;
	}

	public String getUserPersonalAccessToken()
	{
		return m_userPersonalAccessToken;
	}

	public String getProxyHost()
	{
		return m_proxyHost;
	}

	public String getProxyUserName()
	{
		return m_proxyUserName;
	}

	public String getProxyUserPassword()
	{
		return m_proxyUserPassword;
	}

	public int getProxyPort()
	{
		return m_proxyPort;
	}

	public String getBranch()
	{
		return m_gitBranch == null ? MASTER : m_gitBranch;
	}

	private static String normalizeURL(final String url)
	{
		// Normalize url by trimming whitespace and never end with '/'
		String normalizedUrl = url.trim();
		while (StringUtils.endsWith(normalizedUrl, '/'))
		{
			normalizedUrl = normalizedUrl.substring(0, normalizedUrl.length() - 1);
		}

		return normalizedUrl;
	}

	public void constructGitURL()
	{
		StringBuffer sbURL = new StringBuffer();

		if (m_proxyEnabled)
		{
			//http://username:password@proxyHost:proxyPort/
			sbURL.append("https://")
					 .append(m_proxyUserName)
					 .append(':')
					 .append(m_proxyUserPassword)
					 .append('@')
					 .append(m_proxyHost)
					 .append(m_proxyPort < 0 ? "" : ":" + m_proxyPort)
					 .append('/');

			m_gitRepoURL = sbURL.toString();
		}
		else
		{
			URL gitURL;
			try
			{
				gitURL = new URL(normalizeURL(m_originalUrl));
				sbURL.append(gitURL.getProtocol())
						 .append("://token:")
						 .append(m_userPersonalAccessToken)
						 .append('@')
						 .append(gitURL.getHost())
						 .append(gitURL.getPort() == -1 ? "" : ":" + gitURL.getPort())
						 .append(gitURL.getPath());

				m_gitRepoURL = sbURL.toString();
			}
			catch (MalformedURLException exception)
			{
				throw StudioLogger.studioException(this, exception, Messages.ERROR, exception.getMessage());
			}
		}
	}

}
