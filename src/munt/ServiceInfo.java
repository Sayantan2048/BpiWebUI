package munt;

public class ServiceInfo {
    String processName;
    String processStatus; //button value is opposite of Status e.g 
    // when status is Running then button value is Stop.
    
    public ServiceInfo(String processName, String processStatus) {
        this.processName = processName;
        this.processStatus = processStatus;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessStatus(String processStatus) {
        this.processStatus = processStatus;
    }

    public String getProcessStatus() {
        return processStatus;
    }
}
