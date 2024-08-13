package com.mobicule.mcollections.integration.upi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.mobicule.mcollections.core.beans.Collection;
import com.mobicule.mcollections.core.commons.AesUtil;
import com.mobicule.mcollections.core.commons.AllPayUtility;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.service.CollectionService;

public class AllPayCallBackURL extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Logger log = LoggerFactory.getLogger(AllPayCallBackURL.class);

	@Autowired
	private CollectionService collectionService;

	public AllPayCallBackURL() {
		
	}

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		/*SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this,
				config.getServletContext());*/
		
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			AesUtil aes = new AesUtil("7F7E22D057BBD20D0DCBD232FF57F783");
			if (log.isInfoEnabled()) {
				log.info("-----In doPost-----");
			}

			String callBackResponse = readBody(request);

			if (!StringUtils.isEmpty(callBackResponse)) {
				Map<String, String> splitedStringmap = AllPayUtility
						.splitString(callBackResponse);
				log.info("splitedStringdmap " + splitedStringmap);

				HashMap<String, String> map = new HashMap<String, String>();

				log.info("before call back all pay response decryption"
						+ splitedStringmap);
				String responseJson = aes.decrypt(splitedStringmap
						.get(Constants.AllPayCollectionsDao.ENCRESP));
				log.info("After call back all pay response decryption"
						+ responseJson);

				map = (HashMap<String, String>) AllPayUtility
						.splitString(responseJson);

				log.info("map : " + map);

				Map<Object, Object> updateActivityMap = new HashMap<Object, Object>();

				// updateActivityMap.put(Constants.AllPayCollectionsDao.MODIFIED_BY,
				// "");
				updateActivityMap.put(Constants.AllPayCollectionsDao.RESPONSE,
						callBackResponse == null ? "" : callBackResponse);
				updateActivityMap.put(
						Constants.AllPayCollectionsDao.REFERENCE_NO,
						splitedStringmap.get("orderNo") == null ? ""
								: splitedStringmap.get("orderNo"));
				updateActivityMap.put(
						Constants.AllPayCollectionsDao.ORDER_CURRNCY,
						map.get("currency").toString() == null ? "" : map.get(
								"currency").toString());
				updateActivityMap.put(
						Constants.AllPayCollectionsDao.ORDER_AMT,
						map.get("amount").toString() == null ? "" : map.get(
								"amount").toString());
				updateActivityMap.put(
						Constants.AllPayCollectionsDao.ORDER_BILL_NAME, map
								.get("billing_name").toString() == null ? ""
								: map.get("billing_name").toString());
				updateActivityMap.put(
						Constants.AllPayCollectionsDao.ORDER_BILL_ADDRESS, map
								.get("billing_address").toString() == null ? ""
								: map.get("billing_address").toString());
				updateActivityMap.put(
						Constants.AllPayCollectionsDao.ORDER_BILL_ZIP,
						map.get("billing_zip").toString() == null ? "" : map
								.get("billing_zip").toString());
				updateActivityMap.put(
						Constants.AllPayCollectionsDao.ORDER_BILL_TEL,
						map.get("billing_tel").toString() == null ? "" : map
								.get("billing_tel").toString());
				updateActivityMap.put(
						Constants.AllPayCollectionsDao.ORDER_BILL_EMAIL, map
								.get("billing_email").toString() == null ? ""
								: map.get("billing_email").toString());
				updateActivityMap.put(
						Constants.AllPayCollectionsDao.ORDER_BILL_COUNTRY, map
								.get("billing_country").toString() == null ? ""
								: map.get("billing_country").toString());
				updateActivityMap.put(
						Constants.AllPayCollectionsDao.ORDER_SHIP_NAME, map
								.get("delivery_name").toString() == null ? ""
								: map.get("delivery_name").toString());
				updateActivityMap.put(
						Constants.AllPayCollectionsDao.ORDER_SHIP_ADDRESS,
						map.get("delivery_address").toString() == null ? ""
								: map.get("delivery_address").toString());
				updateActivityMap.put(
						Constants.AllPayCollectionsDao.ORDER_SHIP_COUNTRY,
						map.get("delivery_country").toString() == null ? ""
								: map.get("delivery_country").toString());
				updateActivityMap.put(
						Constants.AllPayCollectionsDao.ORDER_SHIP_TEL,
						map.get("delivery_tel").toString() == null ? "" : map
								.get("delivery_tel").toString());
				updateActivityMap.put(
						Constants.AllPayCollectionsDao.ORDER_BILL_CITY, map
								.get("billing_city").toString() == null ? ""
								: map.get("billing_city").toString());
				updateActivityMap.put(
						Constants.AllPayCollectionsDao.ORDER_BILL_STATE, map
								.get("billing_state").toString() == null ? ""
								: map.get("billing_state").toString());
				updateActivityMap.put(
						Constants.AllPayCollectionsDao.ORDER_SHIP_CITY, map
								.get("delivery_city").toString() == null ? ""
								: map.get("delivery_city").toString());
				updateActivityMap.put(
						Constants.AllPayCollectionsDao.ORDER_SHIP_STATE, map
								.get("delivery_state").toString() == null ? ""
								: map.get("delivery_state").toString());
				updateActivityMap.put(
						Constants.AllPayCollectionsDao.ORDER_SHIP_ZIP,
						map.get("delivery_zip").toString() == null ? "" : map
								.get("delivery_zip").toString());
				updateActivityMap.put(
						Constants.AllPayCollectionsDao.ORDER_NOTES,
						map.get("billing_notes").toString() == null ? "" : map
								.get("billing_notes").toString());
				updateActivityMap.put(
						Constants.AllPayCollectionsDao.ORDER_STATUS,
						map.get("order_status").toString() == null ? "" : map
								.get("order_status").toString());
				updateActivityMap.put(
						Constants.AllPayCollectionsDao.ORDER_CARD_NAME, map
								.get("card_name").toString() == null ? "" : map
								.get("card_name").toString());
				updateActivityMap.put(
						Constants.AllPayCollectionsDao.ORDER_DISCOUNT,
						map.get("discount_value").toString() == null ? "" : map
								.get("discount_value").toString());
				updateActivityMap.put(
						Constants.AllPayCollectionsDao.ORDER_BANK_REF_NO, map
								.get("bank_ref_no").toString() == null ? ""
								: map.get("bank_ref_no").toString());
				updateActivityMap.put(Constants.AllPayCollectionsDao.STATUS,
						map.get("status_code").toString() == null ? "" : map
								.get("status_code").toString());
				updateActivityMap.put(Constants.AllPayCollectionsDao.TYPE,
						"callback");
				updateActivityMap.put(
						Constants.AllPayCollectionsDao.MODIFIED_BY,
						splitedStringmap.get("orderNo") == null ? ""
								: splitedStringmap.get("orderNo"));
				updateActivityMap.put(Constants.CALLBACKTYPE, Constants.ALLPAY);
				boolean updateflag = collectionService
						.callBackOnlinePaymentActivityUpdation(updateActivityMap);

				if (updateflag != true) {
					Collection collection = new Collection();

					collection.setOnlinePaymentStatus(map.get("order_status")
							.toString());
					collection.setPaymentStatusDescription(map.get(
							"order_status").toString());
					collection.setInvoiceId(splitedStringmap.get("orderNo"));

					log.info("Call back all pay response not updated in collection succesfully");

					int addCount = collectionService
							.callBackOnlinePaymentActivityAddition(updateActivityMap);

					if (addCount > 0) {
						boolean collectionUpdatedFlag = collectionService
								.updateAllPayCollection(collection);

						if (collectionUpdatedFlag == true)
							log.info("Call back all pay response added in collection succesfully");
						else
							log.info("Call back all pay response added in collection succesfully");

						log.info("Call back all pay response added in online payment history and collection succesfully");

					} else {
						log.info("Error to added in online payment history Call back all pay response");
					}

				} else {
					log.info("Call back all pay response updated in online payment history succesfully");
				}

				// collectionService.submitCallBackFromKotak(map);

				/* sendAllPayStatusSms(collection,user,status);// Deploy */

			} else {
				log.info("Call back all pay response is null");
			}

		} catch (Exception e) {

			log.error("---Exception---", e);
			e.printStackTrace();

		}

	}

	private String readBody(HttpServletRequest request) throws IOException {
		InputStream is = request.getInputStream();

		ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();

		byte[] buf = new byte[1000];

		for (int nChunk = is.read(buf); nChunk != -1; nChunk = is.read(buf)) {
			arrayOutputStream.write(buf, 0, nChunk);
		}

		if (log.isInfoEnabled()) {
			log.info("");
		}

		String data = new String(arrayOutputStream.toByteArray(), "utf-8");

		return data;
	}
}
