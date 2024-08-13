package com.mobicule.mcollections.integration.commons;

/**
 * Created by Brahmaiah on 22-Mar-16.
 */
public class MerchantCollect implements ChecksumData{
	public String amount;
	public String customerId;
	public String referenceId;
	public String orderId;
	public String payerVpa;
	public String remarks;
	public String txnId;
	public String aggregatorVPA;
	public String submerchantVPA;
	public String submerchantReferenceid;
	public String expiry;
	public String timeStamp;//dd-mm-yyyy hh:mm:ss
	public String merchantReferenceCode;//sub merchant register code
	public DeviceDetails deviceDetails; 
	public String refUrl; 
	public String debitAccount;//it is optional
	public String ifsc;//it is optional
    public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getSubmerchantVPA() {
		return submerchantVPA;
	}

	public void setSubmerchantVPA(String submerchantVPA) {
		this.submerchantVPA = submerchantVPA;
	}

	public String getSubmerchantReferenceid() {
		return submerchantReferenceid;
	}

	public void setSubmerchantReferenceid(String submerchantReferenceid) {
		this.submerchantReferenceid = submerchantReferenceid;
	}


	public String getExpiry() {
		return expiry;
	}

	public void setExpiry(String expiry) {
		this.expiry = expiry;
	}

 

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }


    public String getPayerVpa() {
        return payerVpa;
    }

    public void setPayerVpa(String payerVpa) {
        this.payerVpa = payerVpa;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

	public String getAggregatorVPA() {
		return aggregatorVPA;
	}

	public void setAggregatorVPA(String aggregatorVPA) {
		this.aggregatorVPA = aggregatorVPA;
	}

	public String getMerchantReferenceCode() {
		return merchantReferenceCode;
	}

	public void setMerchantReferenceCode(String merchantReferenceCode) {
		this.merchantReferenceCode = merchantReferenceCode;
	}

	public DeviceDetails getDeviceDetails() {
		return deviceDetails;
	}

	public void setDeviceDetails(DeviceDetails deviceDetails) {
		this.deviceDetails = deviceDetails;
	}

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

 
	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getRefUrl() {
		return refUrl;
	}

	public void setRefUrl(String refUrl) {
		this.refUrl = refUrl;
	}

	 

	public String getDebitAccount() {
		return debitAccount;
	}

	public void setDebitAccount(String debitAccount) {
		this.debitAccount = debitAccount;
	}

	public String getIfsc() {
		return ifsc;
	}

	public void setIfsc(String ifsc) {
		this.ifsc = ifsc;
	}

	
	
	
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MerchantCollect [amount=");
		builder.append(amount);
		builder.append(", customerId=");
		builder.append(customerId);
		builder.append(", referenceId=");
		builder.append(referenceId);
		builder.append(", orderId=");
		builder.append(orderId);
		builder.append(", payerVpa=");
		builder.append(payerVpa);
		builder.append(", remarks=");
		builder.append(remarks);
		builder.append(", txnId=");
		builder.append(txnId);
		builder.append(", aggregatorVPA=");
		builder.append(aggregatorVPA);
		builder.append(", submerchantVPA=");
		builder.append(submerchantVPA);
		builder.append(", submerchantReferenceid=");
		builder.append(submerchantReferenceid);
		builder.append(", expiry=");
		builder.append(expiry);
		builder.append(", timeStamp=");
		builder.append(timeStamp);
		builder.append(", merchantReferenceCode=");
		builder.append(merchantReferenceCode);
		builder.append(", deviceDetails=");
		builder.append(deviceDetails);
		builder.append(", refUrl=");
		builder.append(refUrl);
		builder.append(", debitAccount=");
		builder.append(debitAccount);
		builder.append(", ifsc=");
		builder.append(ifsc);
		builder.append("]");
		return builder.toString();
	}

	public String getInput() {

		return aggregatorVPA + amount + customerId + (deviceDetails != null ? deviceDetails.getInput() : "") + expiry
				+ timeStamp + merchantReferenceCode + payerVpa + orderId + remarks + (refUrl != null ? refUrl : "")
				+ txnId;

	}

}