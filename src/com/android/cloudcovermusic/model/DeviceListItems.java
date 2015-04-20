package com.android.cloudcovermusic.model;


/**
 * 
 * @author Sourabh Karkal
 *
 */

public class DeviceListItems {

	String mUuid;
	String mActivePlayer;
	String mDeviceId;
	String mCustId;
	String mUserId;
	String mName;
	String mDescription;
	String mType;
	String mLastSeen;
	String mIsDeleted;
	
	boolean isSelected;

	public DeviceListItems(String mUuid, String mActivePlayer,String mDeviceId,String mCustId,
			String mUserId,String mName,String mDescription,String mType,String mLastSeen, String mIsDeleted) {
		
		super();
		this.mUuid = mUuid;
		this.mActivePlayer = mActivePlayer;
		this.mDeviceId = mDeviceId;
		this.mCustId = mCustId;
		this.mUserId = mUserId;
		this.mName = mName;
		this.mDescription = mDescription;
		this.mType = mType;
		this.mLastSeen = mLastSeen;
		this.mIsDeleted = mIsDeleted;
	}
	public DeviceListItems() {
		// TODO Auto-generated constructor stub
	}
	public String getUuid() {
		return mUuid;
	}

	public void setUuid(String uuid) {
		this.mUuid = uuid;
	}

	public String getActivePlayer() {
		return mActivePlayer;
	}

	public void setActivePlayer(String mActivePlayer) {
		this.mActivePlayer = mActivePlayer;
	}

	public String getDeviceId() {
		return mDeviceId;
	}

	public void setDeviceId(String mDeviceId) {
		this.mDeviceId = mDeviceId;
	}

	public String getCustId() {
		return mCustId;
	}

	public void setCustId(String mCustId) {
		this.mCustId = mCustId;
	}

	public String getUserId() {
		return mUserId;
	}

	public void setUserId(String mUserId) {
		this.mUserId = mUserId;
	}

	public String getName() {
		return mName;
	}

	public void setName(String mName) {
		this.mName = mName;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String mDescription) {
		this.mDescription = mDescription;
	}

	public String getType() {
		return mType;
	}

	public void setType(String mType) {
		this.mType = mType;
	}

	public String getLastSeen() {
		return mLastSeen;
	}

	public void setLastSeen(String mLastSeen) {
		this.mLastSeen = mLastSeen;
	}

	public String getIsDeleted() {
		return mIsDeleted;
	}

	public void setIsDeleted(String mIsDeleted) {
		this.mIsDeleted = mIsDeleted;
	}
	
	public boolean getisSelected() {
		return isSelected;
	}

	public void setisSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	
}
