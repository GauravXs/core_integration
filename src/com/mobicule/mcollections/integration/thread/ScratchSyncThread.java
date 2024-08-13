package com.mobicule.mcollections.integration.thread;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.beans.UserScratchSync;
import com.mobicule.mcollections.core.service.UserScratchSyncService;

public class ScratchSyncThread implements Runnable{
	private Logger log = LoggerFactory.getLogger(getClass());
	
	private SystemUser systemUser;
	private UserScratchSyncService userScratchSyncService;
	
	public ScratchSyncThread(SystemUser systemUser,UserScratchSyncService userScratchSyncService) {
		this.systemUser = systemUser;
		this.userScratchSyncService=userScratchSyncService;
	}

	@Override
	public void run() {
		log.info("systemUser.getUserTableId() ::: " + systemUser.getUserTableId());
		UserScratchSync userScratchSync=new UserScratchSync();
		
		List<Map<String, Object>> supervisorData = userScratchSyncService.fetchSupervisor(systemUser.getUserTableId());
		if(supervisorData != null && supervisorData.size() > 0) {
			Map<String, Object> fetchData =supervisorData.get(0);
			log.info(" supervisor name :: " + fetchData.get("username"));
			userScratchSync.setSupervisorName(fetchData.get("username")==null?"":fetchData.get("username").toString());
			userScratchSync.setSupervisorId(fetchData.get("supervisorId")==null?"":fetchData.get("supervisorId").toString());
		}
		
		userScratchSync.setUserFname(systemUser.getFirstName());
		userScratchSync.setUserLname(systemUser.getLastName());
		userScratchSync.setLoginId(systemUser.getUsername());
		userScratchSync.setUserTableid(systemUser.getUserTableId());
		userScratchSync.setScratchDateTime(new Timestamp(System.currentTimeMillis()));
		
		userScratchSync.setCreatedBy(systemUser.getUserTableId());
		userScratchSync.setModifiedBy(systemUser.getUserTableId());
		userScratchSync.setDoneScratchSyncFromDevice(systemUser.getImeiNo());
		
		//get last transaction details from collection table for that user
		List<Map<String, Object>> lastTransactionDetailsList=userScratchSyncService.getLastTransactionCollectionDetails(systemUser.getUserTableId());
		log.info("lastTransactionDetailsList size :: " + lastTransactionDetailsList.size());
		if(lastTransactionDetailsList!=null && lastTransactionDetailsList.size()>0)
		 {
			 Map<String, Object> lastTransactionDetails=lastTransactionDetailsList.get(0);
			 userScratchSync.setLastTransDateTime(lastTransactionDetails.get("submission_date_time")==null?null:Timestamp.valueOf(lastTransactionDetails.get("submission_date_time").toString()));
			 userScratchSync.setLastTransReceiptNo(lastTransactionDetails.get("receipt_number")==null?"":lastTransactionDetails.get("receipt_number").toString());
			 userScratchSync.setLastTransImeiNo(lastTransactionDetails.get("imei_number")==null?"":lastTransactionDetails.get("imei_number").toString());
			 userScratchSync.setCustomerName(lastTransactionDetails.get("party_name")==null?"":lastTransactionDetails.get("party_name").toString());
			 userScratchSync.setAmount(lastTransactionDetails.get("amount")==null?"":lastTransactionDetails.get("amount").toString());
			 userScratchSync.setTransactionSource(lastTransactionDetails.get("type")==null?"":lastTransactionDetails.get("type").toString());
			 userScratchSync.setApacNumber(lastTransactionDetails.get("business_partner_number")==null?"":lastTransactionDetails.get("business_partner_number").toString());
			 
		 }
		 
		String insertUserScratchSyncFlag= userScratchSyncService.insertUserScratchSyncDetails(userScratchSync);
		log.info("Data got inserted in user Scratch Sync table:: "+insertUserScratchSyncFlag);
		
	}

}
