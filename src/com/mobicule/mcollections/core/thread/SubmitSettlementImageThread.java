package com.mobicule.mcollections.core.thread;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

import org.apache.commons.io.FileUtils;
import org.json.me.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mobicule.mcollections.core.beans.Settlement;
import com.mobicule.mcollections.core.beans.SettlementCasesDocument;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.service.SettlementService;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SubmitSettlementImageThread implements Runnable
{
	private Logger log = LoggerFactory.getLogger(SubmitSettlementImageThread.class);

	private SettlementService settlementService;
	private String fileName;
	private String token;
	private String imageString;
	private Settlement settlement;
	private SystemUser user;
	private Long settlementId;
	private String url;
	public SubmitSettlementImageThread(Settlement settlement
			, SystemUser user
			, Long settlementId
			, SettlementService settlementService
			, String fileName 
			, String token 
			, String imageString
			, String url) {
		this.settlementService = settlementService;
		this.fileName=fileName;
		this.token=token;
		this.imageString=imageString;
		this.settlement=settlement;
		this.user=user;
		this.settlementId=settlementId;
		this.url=url;
	}


	@Override
	public void run()
	{
		synchronized (this) {
			getDefaultPathFile(fileName, token, imageString);
		}
		
	}
	
	
	public void getDefaultPathFile(String fileName ,String token , String imageString) {
		JSONObject json = new JSONObject();
		
		try {
			File outputFile = Files.createTempFile(null,null).toFile();
	        
	        byte[] decodedBytes = Base64.getDecoder().decode(imageString);
			 FileUtils.writeByteArrayToFile(outputFile, decodedBytes);
			
			OkHttpClient client = new OkHttpClient().newBuilder().build();
			MediaType mediaType = MediaType.parse("text/plain");
			RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
					.addFormDataPart("file", fileName,
							RequestBody.create(MediaType.parse("application/octet-stream"), outputFile))
					//.addFormDataPart("filetype", "FRONT_VIEW")
					.build();
			Request request = new Request.Builder().url(url).method("POST", body)
					.addHeader("Authorization", "Bearer "+token)
					//.addHeader("Cookie", "JSESSIONID=28E4051C8CEF780BCF0EB7A549477565")
					.build();
			Response response = client.newCall(request).execute();
			String responseBody = response.body().string();
			log.info("String json :: "+responseBody);
			outputFile.deleteOnExit();
			json = new JSONObject(responseBody);
			log.info("json :: " + json);
			String getFileRef = json.optString("fileRef") == null ? Constants.EMPTY_STRING : json.optString("fileRef");
			String getDocumentName = json.optString("name") == null ? Constants.EMPTY_STRING : json.optString("name");
			
			if(getFileRef!=null && !getFileRef.equals(Constants.EMPTY_STRING)) {
				SettlementCasesDocument settlementCasesDocument = new SettlementCasesDocument();
				settlementCasesDocument.setDocument(getFileRef);
				settlementCasesDocument.setDocumentName(getDocumentName);
				
				boolean flag = settlementService.InsertSettlementImage(settlement, user, settlementCasesDocument, settlementId);
				log.info("Flag of db update ::" + flag);
			}
			
		}catch (Exception e) {
			log.info("Exception :: " + e);
		}
		
	}

}
