package munt;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

public class MUnt extends HttpServlet {
    private static final String CONTENT_TYPE = "text/html; charset=windows-1252";

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(CONTENT_TYPE);
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head><title>MUnt</title></head>");
        out.println("<body>");
        out.println("<p>The servlet has received a GET. This is the reply.</p>");
        out.println("</body></html>");
        out.close();
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String selectedButton = request.getParameter("whichButton");
        StringBuilder infoStr = new StringBuilder();
        StringBuilder exceptionStr = new StringBuilder();
        String uuid = request.getParameter("uuid");
        String mntPoint = request.getParameter("mntpoint");
        String user = request.getParameter("user");
        String group = request.getParameter("group");
        String killOpt = request.getParameter("killOpt");
        String authPass = request.getParameter("authPass");
        boolean authPassed = true;
        
        if (authPass == null || authPass.compareTo("") == 0) {
            authPassed = false;
            infoStr.setLength(0); 
            infoStr.append("Error: Password field empty.\n");
        }
        if (authPassed)
            authPassed = RunCmd.runCmdAuthPass(authPass, infoStr, exceptionStr);
        
        if (authPassed && selectedButton != null &&
            selectedButton.compareTo("Mount") == 0) {
            if (uuid == null || uuid.compareTo("") == 0) {
                infoStr.setLength(0); 
                infoStr.append("Error: UUID field empty.\n");
            }
            else if (mntPoint == null || mntPoint.compareTo("") == 0) {
                infoStr.setLength(0); 
                infoStr.append("Error: Mount point field empty.\n");
            }
            else if (user == null || user.compareTo("") == 0) {
                infoStr.setLength(0); 
                infoStr.append("Error: User field empty.\n");
            }
            else if (group == null || group.compareTo("") == 0) {
                infoStr.setLength(0); 
                infoStr.append("Error: Group field empty.\n");
            }
            else {
                infoStr.setLength(0);
                RunCmd.runCmdMountVfyFailed(uuid, mntPoint, user, 
                                            group, infoStr, exceptionStr);
            }
        }
        if (authPassed && selectedButton != null &&
            selectedButton.compareTo("UnMount") == 0) {
            boolean frcKill = false;
            if (uuid == null || uuid.compareTo("") == 0) {
                infoStr.setLength(0);
                infoStr.append("Error: UUID field empty.\n");
            }
            else if (killOpt == null) {
                infoStr.setLength(0);
                infoStr.append("Internal Error: Kill Options " +
                    "are null.\n");
            }
            else if (killOpt.compareTo("NOKILL") != 0 && 
                     killOpt.compareTo("SIGTERM") != 0 &&
                     killOpt.compareTo("SIGKILL") != 0) {
                infoStr.setLength(0);
                infoStr.append("Internal Error: Kill Options " +
                    "parameter not set to NOKILL or SIGTEM or SIGKILL.\n");
            }
            else {  
                frcKill = (killOpt.compareTo("NOKILL") == 0) ? false : true;
                if (frcKill)
                    killOpt = killOpt.compareTo("SIGTERM") == 0 ? 
                        "15" : "9";
                
                infoStr.setLength(0);
                RunCmd.runCmdUMountVfy(uuid, frcKill, killOpt, 
                                       infoStr, exceptionStr);
            }
        }
                
        ArrayList <FSDesc>list = RunCmd.runCmdRefreshFS(infoStr, exceptionStr);
        request.setAttribute("infoStr", infoStr.toString());
        request.setAttribute("exceptionStr", (exceptionStr.toString().compareTo("") == 0)?
                             "None." : exceptionStr.toString());
        request.setAttribute("fsList", list);
        RequestDispatcher rd = request.getRequestDispatcher("/mount.jsp");
        rd.forward(request, response);
    }
}
