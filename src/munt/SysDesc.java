package munt;

public class SysDesc {
    private String percCpu;
    private String percMem;
    private String pid;
    private String user;
    private String command;
    
    public SysDesc(String percCpu, String percMem, String pid,
                String user, String command) {
        this.percCpu = percCpu;
        this.percMem = percMem;
        this.pid = pid;
        this.user = user;
        this.command = command;
    }

    public void setPercCpu(String percCpu) {
        this.percCpu = percCpu;
    }

    public String getPercCpu() {
        return percCpu;
    }

    public void setPercMem(String percMem) {
        this.percMem = percMem;
    }

    public String getPercMem() {
        return percMem;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPid() {
        return pid;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
