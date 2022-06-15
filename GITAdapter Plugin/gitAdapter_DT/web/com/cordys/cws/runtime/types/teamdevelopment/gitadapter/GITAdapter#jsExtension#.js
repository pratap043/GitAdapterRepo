//+++++ GITAdapter
//----- Properties
//----- Events
//----- Filters
//----- Instance Methods
//----- Static Methods


GITAdapter.prototype.isRenameAllowed = function(iDocument, iName)
{
	if(GITAdapter_isSolutionOrProject(iDocument) || (iDocument._isStandalone()
		&& GITAdapter_isWorkspaceSynchronizeObject(iDocument, iDocument.getDirectory())))
	{
		var parentID = (iDocument.getDirectory() != null)?iDocument.getDirectory().documentID():"";
		return getCWSHelperClass().isRenameAllowed(this.documentPlant(), parentID, iDocument.documentID(), iDocument.documentTypeID(), iName);
	}
	return null;
}


GITAdapter.prototype.isMoveAllowed = function(iDocument, iNewParent, iRole)
{
	var isComposite = iRole && iRole.getType().getAggregationKind() == "composite";
	if(!isComposite && "" != iDocument.name() && null != iDocument.name())
	{
		if(GITAdapter_isSolutionOrProject(iDocument) || GITAdapter_isWorkspaceSynchronizeObject(iDocument, iNewParent))
		{
			return getCWSHelperClass().isMoveAllowed(this.documentPlant(), iNewParent.documentID(), iDocument.documentID(), iDocument.documentTypeID(), iDocument.name() );
		}
	}
	return null;
}


function GITAdapter_isSolutionOrProject(iDocument)
{
	var objectType = iDocument.getObjectType();
	return objectType == "Solution" || objectType == "Project";
}


function GITAdapter_isWorkspaceSynchronizeObject(iDocument, iParent)
{	
	if(!iDocument.isTransient()) {
		// Check if it is (contained in) the project root folder
		while(true) {
			if( iParent == null && iDocument.instanceOf( StudioUMFDocumentContainer ) )
        	{
            	if( iDocument.designtimeQNRs().size() > 0 )
            	{
    				return true;
    			}
    		}
			iDocument = iParent;
			if(null == iDocument) { break; }
			iParent = iDocument.getDirectory();
		}
	}
	return false;
}

function getCWSHelperClass()
{
	if(!window._CWSHelper)
		window._CWSHelper = getClassForName("com.cordys.cws.internal.cwshelper.CWSHelper");
	return window._CWSHelper;
}

//----- Private Methods