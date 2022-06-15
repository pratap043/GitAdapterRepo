package com.cordys.cws.runtime.types.teamdevelopment.gitadapter;

import com.cordys.cws.runtime.types.teamdevelopment.SCMAdapter;
import com.cordys.cws.metadata.annotations.EStudioPropertyType;
import com.cordys.cws.metadata.annotations.StudioDocumentContainsAssociations;
import com.cordys.cws.metadata.annotations.StudioDocumentAssociationMethod;
import com.cordys.cws.associations.EAggregation;
import com.cordys.cws.metadata.annotations.StudioDocumentTypeDefinition;
import com.cordys.cws.metadata.annotations.StudioDocumentProperty;
import com.cordys.cws.methods.LocalizableMessageDefinition;
import com.cordys.cws.metadata.annotations.ELockingBehavior;
import com.cordys.cws.metadata.annotations.EScopeModifier;
import com.cordys.cws.runtime.types.documenttype.EContentType;

@StudioDocumentContainsAssociations
@StudioDocumentTypeDefinition(
  name="com.cordys.cws.runtime.types.teamdevelopment.gitadapter.GITAdapter", 
  localizableDisplayName=@LocalizableMessageDefinition(messageID="Cordys.cws.gitadapter.DocumentType.Messages.comCordysCwsRuntimeTypesTeamdevelopmentGitadapterGitadapterDisplayName"), 
  localizableDisplayNamePlural=@LocalizableMessageDefinition(messageID="Cordys.cws.gitadapter.DocumentType.Messages.comCordysCwsRuntimeTypesTeamdevelopmentGitadapterGitadapterDisplayNamePlural"), 
  localizableUsageDescription=@LocalizableMessageDefinition(messageID="Cordys.cws.gitadapter.DocumentType.Messages.comCordysCwsRuntimeTypesTeamdevelopmentGitadapterGitadapterUsageDescription"), 
  documentManagerClassName="com.cordys.cws.runtime.types.teamdevelopment.gitadapter._managers_.GITAdapterManager", 
  runtimeDocumentID="FF08B979-3179-41BF-9D5C-0EFF4484FFE8", 
  targetXMLNamespace="http://schemas.cordys.com/cws/runtime/types/teamdevelopment/gitadapter/GITAdapter/1.0", 
  _checkSum="0xD39DAFB3C143FA36FFD445FE5709643E", 
  extensionMask="#.cws", 
  scopeModifier=EScopeModifier.NONE, 
  lockingBehavior=ELockingBehavior.AUTOMATIC, 
  predefinedTagDefinitions={}, 
  contentType=EContentType.CWS
)
public abstract class GITAdapter_Base_ extends SCMAdapter
implements
  com.cordys.cws.runtime.types.teamdevelopment.gitadapter.IGITAdapter,
  com.cordys.cws.internal.metadata.ICWSTypeDefinitionService
{
  protected final static String URL = "URL";
  protected final static String USERNAME = "Username";
  protected final static String PASSWORD = "Password";
  protected final static String BRANCH = "Branch";
  protected final static String PROXY_ENABLED = "ProxyEnabled";
  protected final static String PROXY_HOST = "ProxyHost";
  protected final static String PROXY_PORT = "ProxyPort";
  protected final static String PROXY_USERNAME = "ProxyUsername";
  protected final static String PROXY_PASSWORD = "ProxyPassword";
  protected final static String REVISION = "Revision";
  protected final static String WORKING_COPY_STATE_ROOT = "WorkingCopyStateRoot";

  @Override
  public void _afterCreate()
  {
    super._afterCreate();
    setProxyEnabled(false);
    setProxyPort(8081);
    setRevision(0);
  }

  @StudioDocumentProperty(name=GITAdapter_Base_.URL, type=EStudioPropertyType.String, isDerived=false)
  @Override
  public java.lang.String getURL()
  {
    return   _getProperty(URL, EStudioPropertyType.String, false);
  }

  @Override
  public void setURL(java.lang.String uRL)
  {
    _setProperty(URL, uRL, EStudioPropertyType.String, false);
  }

  @StudioDocumentProperty(name=GITAdapter_Base_.USERNAME, type=EStudioPropertyType.String, isDerived=false)
  @Override
  public java.lang.String getUsername()
  {
    return   _getProperty(USERNAME, EStudioPropertyType.String, false);
  }

  @Override
  public void setUsername(java.lang.String username)
  {
    _setProperty(USERNAME, username, EStudioPropertyType.String, false);
  }

  @StudioDocumentProperty(name=GITAdapter_Base_.PASSWORD, type=EStudioPropertyType.Password, isDerived=false)
  @Override
  public java.lang.String getPassword()
  {
    return   _getProperty(PASSWORD, EStudioPropertyType.Password, false);
  }

  @Override
  public void setPassword(java.lang.String password)
  {
    _setProperty(PASSWORD, password, EStudioPropertyType.Password, false);
  }

  @StudioDocumentProperty(name=GITAdapter_Base_.BRANCH, type=EStudioPropertyType.String, isDerived=false)
  @Override
  public java.lang.String getBranch()
  {
    return   _getProperty(BRANCH, EStudioPropertyType.String, false);
  }

  @Override
  public void setBranch(java.lang.String branch)
  {
    _setProperty(BRANCH, branch, EStudioPropertyType.String, false);
  }

  @StudioDocumentProperty(name=GITAdapter_Base_.PROXY_ENABLED, type=EStudioPropertyType.Boolean, defaultValue="false", isDerived=false)
  @Override
  public boolean getProxyEnabled()
  {
    return this.<Boolean> _getProperty(PROXY_ENABLED, EStudioPropertyType.Boolean, false);
  }

  @Override
  public void setProxyEnabled(boolean proxyEnabled)
  {
    _setProperty(PROXY_ENABLED, proxyEnabled, EStudioPropertyType.Boolean, false);
  }

  @StudioDocumentProperty(name=GITAdapter_Base_.PROXY_HOST, type=EStudioPropertyType.String, isDerived=false)
  @Override
  public java.lang.String getProxyHost()
  {
    return   _getProperty(PROXY_HOST, EStudioPropertyType.String, false);
  }

  @Override
  public void setProxyHost(java.lang.String proxyHost)
  {
    _setProperty(PROXY_HOST, proxyHost, EStudioPropertyType.String, false);
  }

  @StudioDocumentProperty(name=GITAdapter_Base_.PROXY_PORT, type=EStudioPropertyType.Integer, defaultValue="8081", isDerived=false)
  @Override
  public int getProxyPort()
  {
    return this.<Integer> _getProperty(PROXY_PORT, EStudioPropertyType.Integer, false);
  }

  @Override
  public void setProxyPort(int proxyPort)
  {
    _setProperty(PROXY_PORT, proxyPort, EStudioPropertyType.Integer, false);
  }

  @StudioDocumentProperty(name=GITAdapter_Base_.PROXY_USERNAME, type=EStudioPropertyType.String, isDerived=false)
  @Override
  public java.lang.String getProxyUsername()
  {
    return   _getProperty(PROXY_USERNAME, EStudioPropertyType.String, false);
  }

  @Override
  public void setProxyUsername(java.lang.String proxyUsername)
  {
    _setProperty(PROXY_USERNAME, proxyUsername, EStudioPropertyType.String, false);
  }

  @StudioDocumentProperty(name=GITAdapter_Base_.PROXY_PASSWORD, type=EStudioPropertyType.Password, isDerived=false)
  @Override
  public java.lang.String getProxyPassword()
  {
    return   _getProperty(PROXY_PASSWORD, EStudioPropertyType.Password, false);
  }

  @Override
  public void setProxyPassword(java.lang.String proxyPassword)
  {
    _setProperty(PROXY_PASSWORD, proxyPassword, EStudioPropertyType.Password, false);
  }

  @StudioDocumentProperty(name=GITAdapter_Base_.REVISION, type=EStudioPropertyType.Integer, defaultValue="0", isDerived=false)
  @Override
  public int getRevision()
  {
    return this.<Integer> _getProperty(REVISION, EStudioPropertyType.Integer, false);
  }

  @Override
  public void setRevision(int revision)
  {
    _setProperty(REVISION, revision, EStudioPropertyType.Integer, false);
  }

  @StudioDocumentAssociationMethod(role=GITAdapter_Base_.WORKING_COPY_STATE_ROOT, aggregation=EAggregation.none, associatedType=com.cordys.cws.synchronize.state.root.IStateRoot.class, isPlural=false, memberClass="com.cordys.cws.synchronize.state.root.StateRoot", isMergeable=false, isOrdered=false, minOccurs="0", maxOccurs="1", exhibitParticipants=false, name=GITAdapter_Base_.WORKING_COPY_STATE_ROOT)
  @Override
  public com.cordys.cws.synchronize.state.root.IStateRoot getWorkingCopyStateRoot()
  {
    return _getAssociatedObject(WORKING_COPY_STATE_ROOT);
  }

  @Override
  public void setWorkingCopyStateRoot(com.cordys.cws.synchronize.state.root.IStateRoot workingCopyStateRoot)
  {
    _setAssociatedObject(workingCopyStateRoot, WORKING_COPY_STATE_ROOT);
  }
}
