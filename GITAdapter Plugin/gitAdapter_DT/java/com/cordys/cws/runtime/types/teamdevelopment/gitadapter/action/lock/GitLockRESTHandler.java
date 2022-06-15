package com.cordys.cws.runtime.types.teamdevelopment.gitadapter.action.lock;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import com.cordys.cws.internal.localization.LocalizableMessage;
import com.cordys.cws.internal.localization.Messages;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.GITClient;
import com.cordys.cws.runtime.types.teamdevelopment.gitadapter.git.config.GITRemoteRepositoryConfiguration;
import com.cordys.cws.synchronize.state.root.StateRootHelper;
import com.cordys.cws.util.StudioLogger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GitLockRESTHandler
{
	private final GITClient m_gitClient;

	private final String refSpec = "refs/heads/" + GITRemoteRepositoryConfiguration.MASTER;

	public GitLockRESTHandler(GITClient gitClient)
	{
		m_gitClient = gitClient;
	}

	public GitLock createLock(final File lockEntry, String comment)
	{

		String syncfolder = StateRootHelper.getStateRoot(m_gitClient.getSession(), true).getSynchronizeFolder();
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> values = Map.of("path", lockEntry.getAbsolutePath().replace(syncfolder + "\\", "").replace("\\", "/"), "ref", refSpec);

		try
		{
			String requestBody = mapper.writeValueAsString(values);
			HttpRequest request = HttpRequest.newBuilder()
																			 .POST(HttpRequest.BodyPublishers.ofString(requestBody))
																			 .uri(URI.create(m_gitClient.getRepository() + "/info/lfs/locks"))
																			 .header("Content-Type", "application/vnd.git-lfs+json")
																			 .header("Authorization", m_gitClient.getBasicAuthenticationHeader())
																			 .build();

			JsonNode node = formatResponse(getHttpClientBuilder().send(request, HttpResponse.BodyHandlers.ofString()));
			JsonNode lock = node.get("lock");

			return new GitLock(lockEntry.getAbsolutePath(),
												 lock.get("id").asText(),
												 m_gitClient.getGitAdapter().getUsername(),
												 comment,
												 Date.from(Instant.parse(lock.get("locked_at").asText())),
												 new Date());

		}

		catch (IOException | InterruptedException e)
		{
			throw StudioLogger.studioException(this, e);
		}
	}

	public GitLock getLock(final String documentPath)
	{
		String pathNew = documentPath.replace(m_gitClient.getWorkingCopyLocation().toString() + "\\", "").replace("\\", "/");
		GitLock lockNew = null;

		try
		{

			String encodedURL = URLEncoder.encode(pathNew, StandardCharsets.UTF_8.toString());
			HttpRequest request = HttpRequest.newBuilder()
																			 .GET()
																			 .uri(URI.create(m_gitClient.getRepository() + "/info/lfs/locks?path=" + encodedURL))
																			 .header("Content-Type", "application/vnd.git-lfs+json")
																			 .header("Authorization", m_gitClient.getBasicAuthenticationHeader())
																			 .build();

			JsonNode node = formatResponse(getHttpClientBuilder().send(request, HttpResponse.BodyHandlers.ofString()));

			for (JsonNode lock : node.get("locks"))
			{
				String path = lock.get("path").asText();
				lockNew =
								new GitLock(path, lock.get("id").asText(), lock.get("owner").get("name").asText(), "Locked", Date.from(Instant.parse(lock.get("locked_at").asText())), new Date());
			}
		}

		catch (IOException | InterruptedException exception)
		{
			throw StudioLogger.studioException(this, exception);
		}

		return lockNew;
	}

	public void deleteLock(final String lockID)
	{

		ObjectMapper mapper = new ObjectMapper();
		Map<String, Boolean> values = Map.of("force", true);

		try
		{
			String requestBody = mapper.writeValueAsString(values);
			HttpRequest request = HttpRequest.newBuilder()
																			 .POST(HttpRequest.BodyPublishers.ofString(requestBody))
																			 .uri(URI.create(m_gitClient.getRepository() + "/info/lfs/locks/" + lockID + "/unlock"))
																			 .header("Content-Type", "application/vnd.git-lfs+json")
																			 .header("Authorization", m_gitClient.getBasicAuthenticationHeader())
																			 .build();

			formatResponse(getHttpClientBuilder().send(request, HttpResponse.BodyHandlers.ofString()));
		}
		catch (IOException | InterruptedException exception)
		{
			throw StudioLogger.studioException(this, exception, Messages.ERROR_WHILE_RELEASING_lOCKS);
		}

	}

	private HttpClient getHttpClientBuilder()
	{
		return HttpClient.newBuilder().build();
	}

	private JsonNode formatResponse(HttpResponse<String> httpResponse) throws IOException
	{
		ObjectMapper mapper = new ObjectMapper();
		JsonNode processedRes = mapper.readTree(httpResponse.body());
		int statusCode = httpResponse.statusCode();
		if (statusCode == 200 || statusCode == 201)
		{
			return processedRes;
		}
		else
			if (statusCode == 409)
			{
				GitLock lock = getLock(processedRes.get("lock").get("path").asText());
				throw StudioLogger.studioException(this,
																					 Messages.LOCK_ACQUIRATION_FAILED_DOCUMENT_LOCKED_BY_SOMEONE_ELSE,
																					 lock.getPath(),
																					 new LocalizableMessage(Messages.SVN_LOCKED_BY_USING_COMMENT, lock.getOwner(), processedRes.get("message").asText()));
			}
			else
			{

				throw StudioLogger.studioException(this, Messages.INTERNAL_ERROR, processedRes.get("message").asText());
			}

	}

}
