package com.cordys.cws.runtime.types.teamdevelopment.gitadapter;

import com.cordys.cws.runtime.types.teamdevelopment.ISCMAdapter;

public interface IGITAdapter_Base_ extends ISCMAdapter
{

  java.lang.String getURL();

  void setURL(java.lang.String uRL);

  java.lang.String getUsername();

  void setUsername(java.lang.String username);

  java.lang.String getPassword();

  void setPassword(java.lang.String password);

  java.lang.String getBranch();

  void setBranch(java.lang.String branch);

  boolean getProxyEnabled();

  void setProxyEnabled(boolean proxyEnabled);

  java.lang.String getProxyHost();

  void setProxyHost(java.lang.String proxyHost);

  int getProxyPort();

  void setProxyPort(int proxyPort);

  java.lang.String getProxyUsername();

  void setProxyUsername(java.lang.String proxyUsername);

  java.lang.String getProxyPassword();

  void setProxyPassword(java.lang.String proxyPassword);

  int getRevision();

  void setRevision(int revision);

  com.cordys.cws.synchronize.state.root.IStateRoot getWorkingCopyStateRoot();

  void setWorkingCopyStateRoot(com.cordys.cws.synchronize.state.root.IStateRoot workingCopyStateRoot);
}
