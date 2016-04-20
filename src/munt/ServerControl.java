package munt;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

public class ServerControl extends HttpServlet {
    private static final String CONTENT_TYPE = "text/html; charset=windows-1252";
    private static final ArrayList<String> serviceList = new ArrayList<String>();
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        serviceList.add("nmbd");
        serviceList.add("smbd");
        serviceList.add("aria2c");
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuilder infoStr = new StringBuilder();
        StringBuilder exceptionStr = new StringBuilder();
        
        ArrayList <ServiceInfo>cmdList = RunCmd.runCmdRefreshServices(serviceList,
                                                        infoStr, exceptionStr);
        request.setAttribute("cmdList", cmdList);
        request.setAttribute("infoStr", infoStr.toString());
        request.setAttribute("exceptionStr", (exceptionStr.toString().compareTo("") == 0)?
                             "None." : exceptionStr.toString());
        RequestDispatcher rd = request.getRequestDispatcher("/srvctl.jsp");
        rd.forward(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuilder infoStr = new StringBuilder();
        StringBuilder exceptionStr = new StringBuilder();
        
        String authPass = request.getParameter("authPass");
        boolean authPassed = true;
        
        if (authPass == null || authPass.compareTo("") == 0) {
            authPassed = false;
            infoStr.setLength(0); 
            infoStr.append("Error: Password field empty.\n");
        }
        if (authPassed && RunCmd.runCmdAuthPass(authPass, infoStr, exceptionStr)) {
            String cmdVal = "";
            String cmd = "";
            boolean noButtonFound = true;
    
            for (int i = 0; i < serviceList.size(); i++) {
                String buttonName = serviceList.get(i); // button name is same as service name
                cmdVal = request.getParameter(buttonName); //button values are either START or STOP
                if (cmdVal != null) {
                    noButtonFound = false;
                    if (cmdVal.compareTo("START") != 0 &&
                        cmdVal.compareTo("STOP") != 0) {
                            infoStr.setLength(0);
                            infoStr.append("Error:Invalid button values.\n");
                            noButtonFound = true;
                            break;
                        }
                    cmd = buttonName;
                    break;
                }
            }
            if (noButtonFound)
                infoStr.append("Error:No valid cmd buttons were found.\n");
            else 
                RunCmd.runCmdChgProcState(cmd, cmdVal, infoStr, exceptionStr);
        }
        
        ArrayList <ServiceInfo>cmdList = RunCmd.runCmdRefreshServices(serviceList,
                                                        infoStr, exceptionStr);
        request.setAttribute("cmdList", cmdList);
        request.setAttribute("infoStr", infoStr.toString());
        request.setAttribute("exceptionStr", (exceptionStr.toString().compareTo("") == 0)?
                             "None." : exceptionStr.toString());
        RequestDispatcher rd = request.getRequestDispatcher("/srvctl.jsp");
        rd.forward(request, response);
    }
    
}
