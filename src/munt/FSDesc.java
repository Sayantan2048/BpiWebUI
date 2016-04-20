package munt;

public class FSDesc {
    private String devName;
    private String size;
    private String used;
    private String avail;
    private String percUsed;
    private String mntPoint;
    private String uuid;
    private String fmtType;
    private boolean chkMounted;
        
    public FSDesc(String devName, String size, String used, String avail, 
                  String percUsed, String mntPoint, String uuid, String fmtType,
                  boolean chkMounted) {
        this.devName = devName;
        this.size = size;
        this.used = used;
        this.avail = avail;
        this.percUsed = percUsed;
        this.mntPoint = mntPoint;
        this.uuid = uuid;
        this.fmtType = fmtType;
        this.chkMounted = chkMounted;
    }

    public void setDevName(String devName) {
        this.devName = devName;
    }

    public String getDevName() {
        return devName;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getSize() {
        return size;
    }

    public void setUsed(String used) {
        this.used = used;
    }

    public String getUsed() {
        return used;
    }

    public void setAvail(String avail) {
        this.avail = avail;
    }

    public String getAvail() {
        return avail;
    }

    public void setPercUsed(String percUsed) {
        this.percUsed = percUsed;
    }

    public String getPercUsed() {
        return percUsed;
    }

    public void setMntPoint(String mntPoint) {
        this.mntPoint = mntPoint;
    }

    public String getMntPoint() {
        return mntPoint;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setFmtType(String fmtType) {
        this.fmtType = fmtType;
    }

    public String getFmtType() {
        return fmtType;
    }
    
    public boolean isMounted() {
        return chkMounted;
    }
}
