package net.lecousin.dataorganizer.core.database.refresh;

public class RefreshOptions {

	public enum GetDataContent {
		IF_NOT_YET_DONE,
		ALL,
	}
	public GetDataContent getDataContent = null;
	
	public boolean tryToRelocateDataSourceIfNecessary = false;

	public enum RetrieveInfoFromInternet {
		MISSING,
		ALL
	}
	public RetrieveInfoFromInternet retrieveInfoFromInternet = null;
	
}
