package com.mobicule.mcollections.integration.commons;

/**
 * Created by Brahmaiah on 20-Feb-16.
 */
public class DeviceDetails implements ChecksumData
{

    String app; // (string, optional),
    String capability; // (string, optional),
    String gcmid;
    String geocode; // (string, optional),
    String id; // (string, optional),
    String ip; // (string, optional),
    String location; // (string, optional),
    String mobile; // (string, optional),
    String os; //(string, optional),
    String type; // (string, optional)

    public String getGcmid() {
        return gcmid;
    }

    public void setGcmid(String gcmId) {
        this.gcmid = gcmId;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getCapability() {
        return capability;
    }

    public void setCapability(String capability) {
        this.capability = capability;
    }

    public String getGeocode() {
        return geocode;
    }

    public void setGeocode(String geocode) {
        this.geocode = geocode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

	public String getInput() {
		return (app != null ? app : "") + (capability != null ? capability : "") + (gcmid != null ? gcmid : "")
				+ (geocode != null ? geocode : "") + (id != null ? id : "") + (ip != null ? ip : "")
				+ (location != null ? location : "") + (mobile != null ? mobile : "") + (os != null ? os : "")
				+ (type != null ? type : "");
	}
}
