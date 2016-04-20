package munt;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

public class ChgPwd extends HttpServlet {
    private static final String CONTENT_TYPE = "text/html; charset=windows-1252";

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuilder infoStr = new StringBuilder();
        StringBuilder exceptionStr = new StringBuilder();
        infoStr.append("None");
        exceptionStr.append("None");
        request.setAttribute("infoStr", infoStr.toString());
        request.setAttribute("exceptionStr", exceptionStr.toString());
        RequestDispatcher rd = request.getRequestDispatcher("/chgpwd.jsp");
        rd.forward(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuilder infoStr = new StringBuilder();
        StringBuilder exceptionStr = new StringBuilder();
        boolean authPassed = true;
        
        String oldPass = request.getParameter("oldPass");
        if (oldPass == null || oldPass.compareTo("") == 0) {
            authPassed = false;
            infoStr.setLength(0);
            infoStr.append("Error: Old password field empty.\n");
        }
        if (authPassed) {
            authPassed = RunCmd.runCmdAuthPass(oldPass, infoStr, exceptionStr);
            if (!authPassed) {
               infoStr.append("Error: Incorrect old password.\n");   
            }
        }
            
        if (authPassed) {
            String newPass = request.getParameter("newPass");
            if (newPass == null || newPass.compareTo("") == 0) {
                authPassed = false;
                infoStr.setLength(0);
                infoStr.append("Error: New password field empty. Password remains unchanged.\n");
            }
            else
                RunCmd.runCmdSetNewPass(newPass, infoStr, exceptionStr);
        }
        
        request.setAttribute("infoStr", infoStr.toString());
        request.setAttribute("exceptionStr", (exceptionStr.toString().compareTo("") == 0)?
                             "None." : exceptionStr.toString());
        RequestDispatcher rd = request.getRequestDispatcher("/chgpwd.jsp");
        rd.forward(request, response);
    }
}
