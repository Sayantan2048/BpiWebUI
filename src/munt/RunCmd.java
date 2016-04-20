package munt;

import java.io.BufferedReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import java.io.UnsupportedEncodingException;

import java.security.InvalidKeyException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

class RunCmd {
    
    private static boolean runCmdGetMountedDevFailed(ArrayList<FSDesc> list, 
                                                     StringBuilder infoStr,
                                                     StringBuilder exceptionStr) {
        boolean getMountedDevFailed = false;
        try {
            String s = null;
            String[] parts = null; 
            Process p = Runtime.getRuntime().exec("df -h");
            BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));
                        
            while ((s = stdInput.readLine()) != null) {
                parts = s.split("\\s+");
                list.add(new FSDesc(parts[0], parts[1], parts[2], parts[3],
                                    parts[4], parts[5], "", "", true));
            }
            
            while ((s = stdError.readLine()) != null) {
                infoStr.append(s + "\n");
                getMountedDevFailed = true;        
            }
        }
        catch (IOException e) {
            exceptionStr.append("Exception occured while running command: df -h.\n"
                + e.getMessage() + ".\n");
            getMountedDevFailed = true;
        }
        
        infoStr.append(getMountedDevFailed ? "Failed to fetch list " +
            "of mounted devices.\n" : "");
        return getMountedDevFailed;
    }
    
    private static boolean runCmdGetBlockDevsFailed(ArrayList<FSDesc> list,
                                                    StringBuilder infoStr,
                                                    StringBuilder exceptionStr) {
        boolean getBlockDevsFailed = false;
        try {
            String s = null;
            final Pattern pattern1 = Pattern.compile("UUID=\"(.+?)\"");
            final Pattern pattern2 = Pattern.compile("(\\s+)TYPE=\"(.+?)\"");
            
            Process p = Runtime.getRuntime().exec("blkid");
            BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));
                        
            while ((s = stdInput.readLine()) != null) {
                Matcher matcher1 = pattern1.matcher(s);
                Matcher matcher2 = pattern2.matcher(s);
                list.add(new FSDesc(s.split(":")[0], "", "", "", "", "", 
                        matcher1.find()?matcher1.group(1):"", 
                        matcher2.find()?matcher2.group(2):"", 
                        false));
            }
            
            while ((s = stdError.readLine()) != null) {
                infoStr.append(s + "\n");
                getBlockDevsFailed = true;
            }
        }
        catch (IOException e) {
            exceptionStr.append("Exception occured while running command: blkid.\n"
                + e.getMessage() + ".\n");
            getBlockDevsFailed = true;
        }
        
        infoStr.append(getBlockDevsFailed ? "Failed to fetch list " +
            "of block devices.\n" : "");
        return getBlockDevsFailed;
    }
    
    /* For refresh file system. */
    public static ArrayList<FSDesc> runCmdRefreshFS(StringBuilder infoStr, 
                                                  StringBuilder exceptionStr) {
        ArrayList <FSDesc>list = new ArrayList<FSDesc>();
        boolean refreshFailed = runCmdGetMountedDevFailed(list, infoStr, exceptionStr);
        refreshFailed = runCmdGetBlockDevsFailed(list, infoStr, exceptionStr) || 
            refreshFailed;
        
        infoStr.append(refreshFailed ? "Refresh failed.\n" : "Refresh complete.\n");
        return list;
    }
    
    /* For Mount. */
    /* Return true if provided mount point does not exsist or mount point is not
     * a directory or mount point is not empty. */
    private static boolean runCmdMntPointDNExist(String mntPoint, 
                                                 StringBuilder infoStr) {
        File f = new File(mntPoint);
        if (!f.exists()) {
            infoStr.append("Mount point not found.\n");
            return true;
        }
        else if (!f.isDirectory()) {
            infoStr.append("Mount point is not a directory.\n");
            return true;
        } 
        else if (f.list().length != 0) {
            infoStr.append("Mount point is not empty.\n");
            return true;
        }
        return false;
    }
    
    /* Return true if mount point already in use. */
    private static boolean runCmdMntPointUsed(String mntPoint, 
                                            StringBuilder infoStr,
                                            StringBuilder exceptionStr) {
        ArrayList <FSDesc>list = new ArrayList<FSDesc>();
        boolean refreshFailed = runCmdGetMountedDevFailed(list, infoStr, exceptionStr);
        if (refreshFailed)
            return true;
        FSDesc f;
        for (int i = 0; i < list.size(); i++) {
            f = list.get(i);
            if (f.getMntPoint().matches(mntPoint + "(.*)")) {
                infoStr.append("Mount point already in use.\n");
                return true;
            }
        }
        return false;
    }
    
    /* Return true if uuid is already mounted or uuid is invalid.*/
    private static boolean runCmdUuidMounted(String uuid, StringBuilder infoStr, 
                                             StringBuilder exceptionStr) {
        ArrayList <FSDesc>list = new ArrayList<FSDesc>();
        boolean refreshFailed = runCmdGetMountedDevFailed(list, infoStr, exceptionStr);
        if (refreshFailed)
            return true;
        refreshFailed = runCmdGetBlockDevsFailed(list, infoStr, exceptionStr);
        if (refreshFailed)
            return true;
        
        FSDesc f;
        String devName = null;
        for (int i = 0; i < list.size(); i++) {
            f = list.get(i);
            if (!f.isMounted() && f.getUuid().compareTo(uuid) == 0) {
                devName = f.getDevName();
                if (devName.matches("/dev/mmcblk0(.*)")) {
                    infoStr.append("You are not allowed to mount:" +
                                   devName + ";\n");
                    return true;
                }
                break;
            }
        }
        if (devName == null) {
            infoStr.append("UUId is invalid.\n");
            return true;
        }
        for (int i = 0; i < list.size(); i++) {
            f = list.get(i);
            if (f.isMounted() && f.getDevName().compareTo(devName) == 0) {
                infoStr.append("Device is already mounted at:" +
                               f.getMntPoint() + ";\n");
                return true;
            }
        }
        return false;
    }
    
    /* Return true if user or group is invalid */
    private static boolean runCmdUsrGrpInvalid(String user, String group,
                                               StringBuilder uid,
                                               StringBuilder gid,
                                               StringBuilder infoStr,
                                               StringBuilder exceptionStr) {
        try {
            String s = null;
            final Pattern pattern1 = Pattern.compile("uid=(\\d+)\\((.+?)\\)");
                       
            Process p = Runtime.getRuntime().exec("id " + user);
            BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));
                        
            if ((s = stdInput.readLine()) != null) {
                Matcher matcher1 = pattern1.matcher(s);
                if (matcher1.find()) {
                    uid.append(matcher1.group(1));
                    if (matcher1.group(2).compareTo(user) != 0) {
                        infoStr.append("User mismatch. Used user:" 
                                       + user + ". Found user:" + matcher1.group(2)
                                       + ".\n");
                    }
                }
                else {
                    infoStr.append("Regex couldn't match user id.\n");
                    return true;
                }
            }
            else {
                infoStr.append("User id not found.\n");
                return true;
            }
            
            if ((s = stdError.readLine()) != null) {
                infoStr.append(s + ". User id not found.\n");
                return true;
            }
        }
        catch (IOException e) {
            exceptionStr.append("Exception occured while running command: id "
                                + user + ".\n" + e.getMessage() + ".\n");
            infoStr.append("User id not found.\n");
            return true;
        }
        
        try {
            String s = null;
            final Pattern pattern1 = Pattern.compile("gid=(\\d+)\\((.+?)\\)");
            
            Process p = Runtime.getRuntime().exec("id " + group);
            BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));
                        
            if ((s = stdInput.readLine()) != null) {
                Matcher matcher1 = pattern1.matcher(s);
                if (matcher1.find()) {
                    gid.append(matcher1.group(1));
                }
                else {
                    infoStr.append("Regex couldn't match group id.\n");
                    return true;
                }
            }
            else {
                infoStr.append("Group id not found.\n");
                return true;
            }
            
            if ((s = stdError.readLine()) != null) {
                infoStr.append(s + ". Group id not found.\n");
                return true;
            }
        }
        catch (IOException e) {
            exceptionStr.append("Exception occured while running command: id "
                                + group + ".\n" + e.getMessage() + ".\n");
            infoStr.append("Group id not found.\n");
            return true;
        }
                
        return false;                                               
    }
    
    /* return true if failed to set file permissions.
     * Used to set file/folder permissions after mounting extN 
     * file systems 
     */
    private static boolean runCmdSetFilePermissionFailed(String uid, String gid,
                                                         String mountPoint, 
                                                         StringBuilder infoStr,
                                                         StringBuilder exceptionStr) {
        String cmd = "";
        boolean setFilePermissionFailed = false;
        /* Change ownership */
        try {
            String s = null;
            cmd = "chown -R " + uid + ":" + gid + " " + mountPoint;
            Process p = Runtime.getRuntime().exec(cmd);
            
            BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));
                        
            while ((s = stdInput.readLine()) != null) {
                infoStr.append(s + "\n");
            }
            
            while ((s = stdError.readLine()) != null) {
                infoStr.append(s + "\n");
                setFilePermissionFailed = true;
            }
        }
        catch (IOException e) {
            exceptionStr.append("Exception occured while running command:"
                                + cmd + ".\n" + e.getMessage() + ".\n");
            setFilePermissionFailed = true;
        }
        
        /* Change permissions */
        try {
            String s = null;
            cmd = "chmod 770 -R " + mountPoint;
            Process p = Runtime.getRuntime().exec(cmd);
            
            BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));
                        
            while ((s = stdInput.readLine()) != null) {
                infoStr.append(s + "\n");
            }
            
            while ((s = stdError.readLine()) != null) {
                infoStr.append(s + "\n");
                setFilePermissionFailed = true;
            }
        }
        catch (IOException e) {
            exceptionStr.append("Exception occured while running command:"
                                + cmd + ".\n" + e.getMessage() + ".\n");
            setFilePermissionFailed = true;
        } 
        
        infoStr.append(setFilePermissionFailed ? "Failed to set proper file" +
                       "/folder permissions.\n" : "");
        return setFilePermissionFailed;    
    }
    
    /* Return true if mount command failed. */
    private static boolean runCmdMountFailed(String uuid, String mountPoint,
                                             String uid, String gid,
                                             StringBuilder infoStr, 
                                             StringBuilder exceptionStr) {
        String cmd = "mount";
        boolean mountFailed = false;
        try {
            String s = null;
            ArrayList<FSDesc> list = new ArrayList<FSDesc>();
            boolean blkDevDetFailed = runCmdGetBlockDevsFailed(list, infoStr, 
                                                               exceptionStr);
            
            if (blkDevDetFailed) {
                infoStr.append("Mount Failed.\n");
                return true;
            }
            FSDesc f;
            String fmtType = null;
            for (int i = 0; i < list.size(); i++) {
                f = list.get(i);
                if (f.getUuid().compareTo(uuid) == 0) {
                    fmtType = f.getFmtType();
                    break;
                }
            }
            if (fmtType == null || fmtType.length() < 1) {
                infoStr.append("Unrecoginzed format:" + fmtType +
                               ". Mount failed.\n");
                return true;
            }
            
            boolean setExplicitFilePermission = false;
            if (fmtType.matches("ext\\d+")) {
                cmd = "mount -t " + fmtType + " UUID=\"" + uuid + "\" " + 
                      mountPoint;
                setExplicitFilePermission = true;
            }
            else if (fmtType.compareTo("ntfs") == 0) {
                fmtType = "ntfs-3g";
                cmd = "mount -t " + fmtType + " -o umask=0007,gid=" +
                gid + ",uid=" + uid + " UUID=\""
                + uuid + "\" " + mountPoint;
            }
            else {
                cmd = "mount -t " + fmtType + " -o umask=0007,gid=" +
                gid + ",uid=" + uid + " UUID=\""
                + uuid + "\" " + mountPoint;
            }
            
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));
                        
            while ((s = stdInput.readLine()) != null) {
                infoStr.append(s + "\n");
            }
            
            while ((s = stdError.readLine()) != null) {
                infoStr.append(s + "\n");
                mountFailed = true;
            }
            
            if (setExplicitFilePermission) 
                runCmdSetFilePermissionFailed(uid, gid, mountPoint, infoStr, exceptionStr);
        }
        catch (IOException e) {
            exceptionStr.append("Exception occured while running command:"
                                + cmd + ".\n" + e.getMessage() + ".\n");
            mountFailed = true;
        } catch (InterruptedException e) {
            exceptionStr.append("Exception occured while running command:"
                                + cmd + ".\n" + e.getMessage() + ".\n");
            mountFailed = true;
        }
        infoStr.append(mountFailed ? "Mount Failed.\n" : "");
        return mountFailed;
    }
    
    public static void runCmdMountVfyFailed(String uuid, String mntPoint,
                                            String user, String group,
                                            StringBuilder infoStr, 
                                            StringBuilder exceptionStr) {
        boolean mntPointDNExsist = runCmdMntPointDNExist(mntPoint, infoStr);
        if (mntPointDNExsist) {
            infoStr.append("Mount failed.\n");
            return;
        }
        /* In most cases this check is redundant but it is required in case the 
         * mount point is used by another filesystem and the mounted 
         * file system is empty.
         */
        boolean mntPointUsed = runCmdMntPointUsed(mntPoint, infoStr, exceptionStr);
        if (mntPointUsed) {
            infoStr.append("Mount failed.\n");
            return;
        }
        boolean uuidVerify = runCmdUuidMounted(uuid, infoStr, exceptionStr);
        if (uuidVerify) {
            infoStr.append("Mount failed.\n");
            return;
        }
        StringBuilder uid = new StringBuilder();
        StringBuilder gid = new StringBuilder();
        boolean userGroupInvalid = runCmdUsrGrpInvalid(user, group, uid, gid, infoStr, exceptionStr);
        if (userGroupInvalid) {
            infoStr.append("Mount failed.\n");
            return;
        }
        infoStr.append(runCmdMountFailed(uuid, mntPoint,
                                         uid.toString(),
                                         gid.toString(),
                                         infoStr, exceptionStr) ? 
                       "" : "Mount successful.\n");
    }
    
    /* For Unmount. */
    /* Return true if uuid is not already mounted or uuid is invalid.*/
    private static boolean runCmdUuidNotMounted(String uuid,
                                                StringBuilder mountPoint,
                                                StringBuilder infoStr, 
                                             StringBuilder exceptionStr) {
        ArrayList <FSDesc>list = new ArrayList<FSDesc>();
        boolean refreshFailed = runCmdGetMountedDevFailed(list, infoStr, exceptionStr);
        if (refreshFailed)
            return true;
        refreshFailed = runCmdGetBlockDevsFailed(list, infoStr, exceptionStr);
        if (refreshFailed)
            return true;
        
        FSDesc f;
        String devName = null;
        for (int i = 0; i < list.size(); i++) {
            f = list.get(i);
            if (!f.isMounted() && f.getUuid().compareTo(uuid) == 0) {
                devName = f.getDevName();
                if (devName.matches("/dev/mmcblk0(.*)")) {
                    infoStr.append("You are not allowed to UnMount:" +
                                   devName + ";\n");
                    return true;
                }
                break;
            }
        }
        if (devName == null) {
            infoStr.append("UUId is invalid.\n");
            return true;
        }
        for (int i = 0; i < list.size(); i++) {
            f = list.get(i);
            if (f.isMounted() && f.getDevName().compareTo(devName) == 0) {
                mountPoint.append(f.getMntPoint());
                return false;
            }
        }
        infoStr.append("Device:" + devName + " is not mounted.\n");
        return true;
    }
    /* Return true if dev is in use. */
    private static boolean runCmdVerifyDevInUse(String uuid, String mntPoint,
                                                HashSet<String> pidList,
                                                StringBuilder infoStr,
                                                StringBuilder exceptionStr) {
        try {
            String s = null;
            final Pattern pattern1 = Pattern.compile(mntPoint);

            Process p = Runtime.getRuntime().exec("lsof");
            BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));
            
            boolean devInUse = false;
            StringBuilder devUseDet = new StringBuilder();
            while ((s = stdInput.readLine()) != null) {
                Matcher matcher1 = pattern1.matcher(s);
                if (matcher1.find()) {
                    String[] det = s.split("\\s+");
                    devUseDet.append("(" + det[0] + ", " +
                                     det[1] + ", " + det[8]
                                    + ")\n");
                    pidList.add(det[1]);
                    devInUse = true;
                }
            }
            
            if (devInUse) {
                infoStr.append("The following list of " +
                    "files are open(Process Name, PID, File Location):"
                    + devUseDet.toString() + ".\n");
                return true;
            }
                     
            while ((s = stdError.readLine()) != null) {
                infoStr.append(s + ". lsof failed.\n");
                return true;
            }
        }
        catch (IOException e) {
            exceptionStr.append("Exception occured while running command: lsof. "
                                + e.getMessage() + ".\n");
            infoStr.append("Couldn't process lsof.\n");
            return true;
        }
        return false;
    }
    /* return true if kill process failed. */
    private static boolean runCmdKillProcessFailed(HashSet<String> pidList,
                                                   String killOpt,
                                                   StringBuilder infoStr,
                                                   StringBuilder exceptionStr) {
        Iterator pidIter = pidList.iterator();
        while (pidIter.hasNext()) {
            String pid = (String)pidIter.next();
            try {
                String s = null;
                
                Process p = Runtime.getRuntime().exec("kill -" + 
                                                      killOpt + " "+ pid);
                BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));
                
                while ((s = stdInput.readLine()) != null) {
                    infoStr.append(s + ".\n");
                }
                                                                
                while ((s = stdError.readLine()) != null) {
                    infoStr.append(s + ". kill encountered error " +
                        "killing pid:" + pid +".\n");
                    return true;
                }
            }
            catch (IOException e) {
                exceptionStr.append("Exception occured while running command: " +
                    "kill -" + killOpt + " " + pid + ".\n" + e.getMessage() + ".\n");
                infoStr.append("Couldn't kill -" + killOpt+ " " + pid + ".\n");
                return true;
            }
        }
        infoStr.append("Ran kill -" + killOpt + " command.\n");
        return false;
    }
    /* return true if umount failed. */
    private static boolean runCmdUnMountFailed(String uuid,
                                            StringBuilder infoStr,
                                            StringBuilder exceptionStr) {
        try {
            String s = null;
            
            Process p = Runtime.getRuntime().exec("umount UUID=\"" + uuid
                                                + "\"");
            BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));
            
            while ((s = stdInput.readLine()) != null) {
                infoStr.append(s + ".\n");
            }
            boolean unmountFailed = false;                                                
            while ((s = stdError.readLine()) != null) {
                infoStr.append(s + ".\n");
                unmountFailed = true;
            }
            if (unmountFailed)
                return true;
        }
        catch (IOException e) {
            exceptionStr.append("Exception occured while running command: " +
                "umount UUID=\"" + uuid + "\";" + e.getMessage() + ".\n");
            return true;
        }
        return false;
    }
    public static void runCmdUMountVfy(String uuid, boolean forceKill,
                                       String killOpt,
                                        StringBuilder infoStr, 
                                        StringBuilder exceptionStr) {
        StringBuilder getMntPoint = new StringBuilder();
        
        boolean devNotMounted = runCmdUuidNotMounted(uuid, getMntPoint, infoStr, exceptionStr);
        if (devNotMounted) {
            infoStr.append("UnMount failed.\n");
            return;
        }
        
        HashSet<String> pidList = new HashSet<String>();
        boolean devInUse = runCmdVerifyDevInUse(uuid, getMntPoint.toString(),
                                                pidList, infoStr, 
                                                exceptionStr);
        if (!forceKill && devInUse) { 
            infoStr.append("UnMount failed.\n");
            return;
        }
        
        else if (!devInUse) {
            infoStr.append(runCmdUnMountFailed(uuid, infoStr, exceptionStr) ?
                           "UnMount failed.\n" : "UnMount successful.\n");
            return;
        }

        boolean killProcessFailed = runCmdKillProcessFailed(pidList, killOpt,
                                                            infoStr, exceptionStr);
        if (killProcessFailed) {
            infoStr.append("UnMount failed.\n");
            return;
        }
        infoStr.append(runCmdUnMountFailed(uuid, infoStr, exceptionStr) ? 
                       "UnMount failed.\n" : "UnMount successful.\n");
    }
    
    /* Refresh process stat */
    public static ArrayList<SysDesc> runCmdRefreshProcStat(String sortOpt,
                                                    StringBuilder infoStr, 
                                                     StringBuilder exceptionStr){
        ArrayList<SysDesc> psList = new ArrayList<SysDesc>();
        boolean refreshFailed = false;
        
        try {
            String s = null;
            
            Process p = Runtime.getRuntime().exec("ps -eo pcpu,pmem,pid,user,args");
            
            final Pattern pattern = Pattern.compile(
            "(\\d+\\.\\d+)\\s+(\\d+\\.\\d+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S.+)");
            
            BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));
            
            String[] parts;
            stdInput.readLine();  
            while ((s = stdInput.readLine()) != null) {
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                psList.add(new SysDesc(matcher.group(1), 
                        matcher.group(2), matcher.group(3),
                        matcher.group(4), matcher.group(5)));
                }
            }
            /* List is already sorted as per PID. */
            if (sortOpt.compareTo("PID") != 0)
            Collections.sort(psList, new Comparator<SysDesc>() {
                    @Override
                    public int compare(SysDesc ps2, SysDesc ps1) {
                        if (sortOpt.compareTo("CPU") == 0)
                            return  ps1.getPercCpu().compareTo(
                                ps2.getPercCpu());
                        return  ps1.getPercMem().compareTo(
                                ps2.getPercMem());
                   }
                });
                                                            
            while ((s = stdError.readLine()) != null) {
                infoStr.append(s + ".\n");
                refreshFailed = true;
            }
        }
        catch (IOException e) {
            exceptionStr.append("Exception occured while running command: " +
            "ps -eo pcpu,pmem,pid,user,args. " + e.getMessage() + ".\n");
            refreshFailed = true;
        }
        infoStr.append(refreshFailed ? "Refresh failed.\n" :
                       "Refresh complete.\n");
        return psList;
    }
    
    public static String runCmdRefreshSysStat(StringBuilder infoStr, 
                                            StringBuilder exceptionStr) {
        StringBuilder systemStat = new StringBuilder();
        try {
            String s = null;
            String regex = "(\\d+)\\s+";
            regex += regex;
            regex += regex;
            regex += regex;
            regex += regex;
            final Pattern pattern = Pattern.compile(regex);
            
            Process p1 = Runtime.getRuntime().exec("vmstat");
            BufferedReader stdInput1 = new BufferedReader(new
                InputStreamReader(p1.getInputStream()));
            BufferedReader stdError1 = new BufferedReader(new
                InputStreamReader(p1.getErrorStream()));
            
            Process p2 = Runtime.getRuntime().exec(
                "cat /sys/devices/platform/sunxi-i2c.0/i2c-0/0-0034/temp1_input");
            BufferedReader stdInput2 = new BufferedReader(new
                InputStreamReader(p2.getInputStream()));
            BufferedReader stdError2 = new BufferedReader(new
                InputStreamReader(p2.getErrorStream()));
            
            String sysTemp = "0";
            if ((s = stdInput2.readLine()) != null) {
                sysTemp = Float.toString(Float.parseFloat(s) / 1000.0f);
            }
            while ((s = stdError2.readLine()) != null) {
                infoStr.append(s + ".\n");
            }
            
            systemStat.append("<table>");
            systemStat.append("<tr><td colspan=\"6\">System Stats</td></tr>");
            while ((s = stdInput1.readLine()) != null) {
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    systemStat.append("<tr>");
                    systemStat.append("<td>Waiting for CPU:"+matcher.group(1)+"</td>");
                    systemStat.append("<td>Waiting for IO:"+matcher.group(2)+"</td>");
                    systemStat.append("<td>VM used:"+matcher.group(3)+"</td>");
                    systemStat.append("<td>Mem free:"+matcher.group(4)+"</td>");
                    systemStat.append("<td>Mem buffered:"+matcher.group(5)+"</td>");
                    systemStat.append("<td>Mem Cached:"+matcher.group(6)+"</td>");
                    
                    systemStat.append("</tr><tr>");
                    systemStat.append("<td>Swap to Mem(blocks/s):"+matcher.group(7)+"</td>");
                    systemStat.append("<td>Mem to Swap(blocks/s):"+matcher.group(8)+"</td>");
                    systemStat.append("<td>I/P from disks:"+matcher.group(9)+"</td>");
                    systemStat.append("<td>O/P to disks:"+matcher.group(10)+"</td>");
                    systemStat.append("<td>Interrupt/s:"+matcher.group(11)+"</td>");
                    systemStat.append("<td>Context Switch/s:"+matcher.group(12)+"</td>");
                    systemStat.append("</tr><tr>");
                    
                    systemStat.append("<td>%CPU user:"+matcher.group(13)+"</td>");
                    systemStat.append("<td>%CPU sys:"+matcher.group(14)+"</td>");
                    systemStat.append("<td>%CPU Idle:"+matcher.group(15)+"</td>");
                    systemStat.append("<td>%CPU wait for IO:"+matcher.group(16)+"</td>");
                    systemStat.append("<td>Sys Temp(C):"+sysTemp+"</td>");
                    systemStat.append("</tr>");
                }
            }
            systemStat.append("</table>");
                                                            
            while ((s = stdError1.readLine()) != null) {
                infoStr.append(s + ".\n");
            }
        }
        catch (IOException e) {
            exceptionStr.append("Exception occured while running command: " +
            "vmstat/cat" + e.getMessage() + ".\n");
            
        }
        return systemStat.toString();
    }
    
    public static ArrayList<String> runCmdRefreshGpio( int maxPins,
                                        StringBuilder infoStr,
                                        StringBuilder exceptionStr){
        ArrayList<String> gpioState = new ArrayList<String>();
        boolean refreshFailed = false;
        
        for (int i = 0; i < maxPins; i++) {
        try {
            String s = null;
            
            Process p = Runtime.getRuntime().exec("/root/readpin " + i);
            
            BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));
            
            if ((s = stdInput.readLine()) != null)
                gpioState.add(s.compareTo("1") == 0 ? "HIGH" : "LOW");    
            
            while ((s = stdError.readLine()) != null) {
                infoStr.append(s + ".\n");
                refreshFailed = true;
            }
        }
        catch (IOException e) {
            exceptionStr.append("Exception occured while running command: " +
            "/root/readpin " + i +". "+ e.getMessage() + ".\n");
            refreshFailed = true;
        }
        }
        
        infoStr.append(refreshFailed ? "Refresh failed.\n" :
                       "Refresh complete.\n");
        return gpioState;
    }
    
    public static void runCmdChgPinState(String pin,
                                        String newState,
                                        StringBuilder infoStr,
                                        StringBuilder exceptionStr) {
        boolean setPinFailed = false;
        String cmd = "/root/setpin " + pin + " " + newState;
        try {
            String s = null;
            
            Process p = Runtime.getRuntime().exec(cmd);
            
            BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));
            
            if ((s = stdInput.readLine()) != null)
                infoStr.append(s + ".\n");    
            
            while ((s = stdError.readLine()) != null) {
                infoStr.append(s + ".\n");
                setPinFailed = true;
            }
        }
        catch (IOException e) {
            exceptionStr.append("Exception occured while running command: " +
            cmd + ". " + e.getMessage() + ".\n");
            setPinFailed = true;
        }
                
        infoStr.append(setPinFailed ? "Failed to set pin " + pin + ".\n" :
                       "Successfully set pin " + pin + " to " + newState
                       + ".\n");
    }
    
    public static ArrayList<ServiceInfo> runCmdRefreshServices(
                                        ArrayList<String>serviceList,
                                        StringBuilder infoStr,
                                        StringBuilder exceptionStr) {
        
        ArrayList<ServiceInfo> cmdList = new ArrayList<ServiceInfo>();
        ArrayList<String> serviceListCpy = new ArrayList<String>(serviceList);
        boolean refreshFailed = false;
        
        try {
            Process p = Runtime.getRuntime().exec("ps -eo args");
            String s;
            
            BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));
            
            while ((s = stdInput.readLine()) != null) {
                for (int i = 0; i < serviceListCpy.size(); i++) {
                    if (s.matches("(.*)" + 
                        serviceListCpy.get(i) + "(.*)")) {
                        cmdList.add(new ServiceInfo(serviceListCpy.get(i), "RUNNING"));
                        serviceListCpy.remove(i);
                    }
                }
            }
            
            for (int i = 0; i < serviceListCpy.size(); i++) {
                cmdList.add(new ServiceInfo(serviceListCpy.get(i), "NRUNNING"));
            }
            
            while ((s = stdError.readLine()) != null) {
                infoStr.append(s + ".\n");
                refreshFailed = true;
            }   
        }
        catch (IOException e) {
            exceptionStr.append("Exception occured while running command: " +
            "ps -eo args.\n" + e.getMessage() + ".\n");
            refreshFailed = true;
        }
        
        infoStr.append(refreshFailed ? "Refresh failed.\n" :
                       "Refresh complete.\n");

        return cmdList;
    }
    
    public static void runCmdChgProcState(String service, String val,
                                        StringBuilder infoStr, 
                                        StringBuilder exceptionStr) {
        String cmd = service;
        boolean cmdFailed = false;
        boolean possibleIODeadlock = false;
        try {
            Process p;
            String s;
            
            if (service.compareTo("smbd") == 0) {
                cmd = (val.compareTo("START") == 0) ? "service smbd start" :
                    "service smbd stop";
                p = Runtime.getRuntime().exec(cmd);
            }
            else if (service.compareTo("nmbd") == 0) {
                cmd = (val.compareTo("START") == 0) ? "service nmbd start" :
                    "service nmbd stop";
                p = Runtime.getRuntime().exec(cmd);
            }
            else if (service.compareTo("aria2c") == 0) {
                cmd = (val.compareTo("START") == 0) ? 
                    "aria2c --enable-rpc --rpc-listen-all" :
                    "killall -15 aria2c";
                p = Runtime.getRuntime().exec(cmd);
                possibleIODeadlock = true;
            } else {
                infoStr.append("Start/Stop service for command " + service +
                               "in not configured.\n");
                return;
            }
                
            BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));
            
            while (!possibleIODeadlock && (s = stdInput.readLine()) != null) {
                infoStr.append(s + ".\n");
            } 
            while (!possibleIODeadlock && (s = stdError.readLine()) != null) {
                infoStr.append(s + ".\n");
                cmdFailed = true;
            }
            Thread.sleep(1000);
        } catch (IOException e) {
            exceptionStr.append("Exception occured while running command: " +
            cmd + ".\n" + e.getMessage() + ".\n");
            cmdFailed = true;
        } 
        catch (InterruptedException e) {
             exceptionStr.append(e.getMessage());   
        }
        
        infoStr.append(cmdFailed ? "Failed to run command:" + cmd + ".\n" :
                       "Successfully " + (val.compareTo("START") == 0 ? "started ":
                       "stopped ") + service + ".\n");   
        
    }
    
    private static PassEnc defPass;
    private static PassEnc curPass;
    
    private static String runCmdGenText(int i) {
        String text = "";
        char c;
        for (int j = 0; j < 10; j++) {
            c = (char)(((j + i) >> 1)& 255);
            text += c;
        }
        return text;
    }
    
    static {
        runCmdSetDefPass();
        runCmdSetCurPass();
    }
    public static void runCmdSetDefPass() {
        byte[] f = {-22, 97, 69, -111, -118, -109, 75, -92, 82, 43, -74, 30, 32, -28, 85, 16,};
        defPass = new PassEnc(f, 6, null);
    }
    public static void runCmdSetCurPass() {
        curPass = null;
    }
    
    /* Return true if password is correct. */
    public static boolean runCmdAuthPass(String pass,
                                         StringBuilder infoStr,
                                         StringBuilder exceptionStr) {
        try {
            PassEnc enc = curPass;
            if (curPass == null) {
                enc = defPass;
                infoStr.append("Warning: Authenticating default password.\n");
            }
            
            byte[] key = pass.getBytes("UTF-8");
            MessageDigest md4 = MessageDigest.getInstance("MD5");
            key = md4.digest(key);
            
            Key aesKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] encrypted = cipher.doFinal(runCmdGenText(enc.getF2()).getBytes());
            byte[] f = enc.getF1();
            
            for (int i = 0; i < 16; i++) {
                if (f[i] != encrypted[i]) {
                    infoStr.append("Password Authentication Failed.\n");
                    return false;
                }
            }
        } catch (UnsupportedEncodingException e) {
            exceptionStr.append("Caught unsupported encoding exception.\n");
        } catch (InvalidKeyException e) {
            exceptionStr.append("Caught invalid key exception.\n");
        } catch (NoSuchAlgorithmException e) {
                exceptionStr.append("Caught no such algorithm exception.\n");
                System.out.println(e.getMessage());
        } catch (NoSuchPaddingException e) {
                exceptionStr.append("Caught no such padding exception.\n");    
        } catch (BadPaddingException e) {
                    exceptionStr.append("Caught bad padding exception.\n");  
        } catch (IllegalBlockSizeException e) {
                    exceptionStr.append("Caught bad padding exception.\n");  
        }
                    
        return true;                                     
    }
    
    public static void runCmdSetNewPass(String pass,
                                         StringBuilder infoStr,
                                         StringBuilder exceptionStr) {
        boolean setNewPassFailed = false;
        try {
            PassEnc enc = new PassEnc(null, pass.hashCode(), null);
                        
            byte[] key = pass.getBytes("UTF-8");
            MessageDigest md4 = MessageDigest.getInstance("MD5");
            key = md4.digest(key);
            
            Key aesKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            enc.setF1(cipher.doFinal(runCmdGenText(enc.getF2()).getBytes()));
            curPass = enc;
        } catch (UnsupportedEncodingException e) {
            exceptionStr.append("Caught unsupported encoding exception.\n");
            setNewPassFailed = true;
        } catch (InvalidKeyException e) {
            exceptionStr.append("Caught invalid key exception.\n");
            setNewPassFailed = true;
        } catch (NoSuchAlgorithmException e) {
                exceptionStr.append("Caught no such algorithm exception.\n");
                setNewPassFailed = true;
        } catch (NoSuchPaddingException e) {
                exceptionStr.append("Caught no such padding exception.\n");
                setNewPassFailed = true;
        } catch (BadPaddingException e) {
                exceptionStr.append("Caught bad padding exception.\n");
                setNewPassFailed = true;
        } catch (IllegalBlockSizeException e) {
                exceptionStr.append("Caught bad padding exception.\n");
                setNewPassFailed = true;
        }
        
        infoStr.append(setNewPassFailed ? "Failed to set new password.\n" :
                       "Password is now changed.\n");
    
    }
}