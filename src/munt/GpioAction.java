package munt;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

public class GpioAction extends HttpServlet {
    private static final String CONTENT_TYPE = "text/html; charset=windows-1252";
    private static int maxPins = 6;
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuilder infoStr = new StringBuilder();
        StringBuilder exceptionStr = new StringBuilder();
        ArrayList <String>gpioList = RunCmd.runCmdRefreshGpio(maxPins, infoStr, exceptionStr);
        request.setAttribute("gpioList", gpioList);
        request.setAttribute("infoStr", infoStr.toString());
        request.setAttribute("exceptionStr", (exceptionStr.toString().compareTo("") == 0)?
                             "None." : exceptionStr.toString());
        RequestDispatcher rd = request.getRequestDispatcher("/gpio.jsp");
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
            String pin = "";
            String pinVal = "";
            boolean noButtonFound = true;
            
            for (int i = 0; i < maxPins; i++) {
                String buttonName = "GPIO_" + i;
                pinVal = request.getParameter(buttonName);
                if ( pinVal != null) {
                    pin = Integer.toString(i);
                    noButtonFound = false;
                    if (pinVal.compareTo("HIGH") != 0 &&
                        pinVal.compareTo("LOW") != 0) {
                            infoStr.setLength(0);
                            infoStr.append("Error:Invalid button values.\n");
                            noButtonFound = true;
                            break;
                        }
                    pinVal = pinVal.compareTo("HIGH") == 0 ? "H" : "L";
                    break;
                }
            }
            if (noButtonFound)
                infoStr.append("Error:No valid GPIO buttons were found.\n");
            else 
                RunCmd.runCmdChgPinState(pin, pinVal, infoStr, exceptionStr);
        }
        
        ArrayList <String>gpioList = RunCmd.runCmdRefreshGpio(maxPins, infoStr, exceptionStr);
        request.setAttribute("gpioList", gpioList);
        request.setAttribute("infoStr", infoStr.toString());
        request.setAttribute("exceptionStr", (exceptionStr.toString().compareTo("") == 0)?
                             "None." : exceptionStr.toString());
        RequestDispatcher rd = request.getRequestDispatcher("/gpio.jsp");
        rd.forward(request, response);
    }
}
