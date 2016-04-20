package munt;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

public class RefreshSys extends HttpServlet {
    private static final String CONTENT_TYPE = "text/html; charset=windows-1252";

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuilder infoStr = new StringBuilder();
        StringBuilder exceptionStr = new StringBuilder();
        ArrayList <SysDesc>psList = RunCmd.runCmdRefreshProcStat("CPU", infoStr, exceptionStr);
        request.setAttribute("psList", psList);
        request.setAttribute("sysStat", RunCmd.runCmdRefreshSysStat(infoStr, exceptionStr));
        request.setAttribute("infoStr", infoStr.toString());
        request.setAttribute("exceptionStr", (exceptionStr.toString().compareTo("") == 0)?
                             "None." : exceptionStr.toString());
        RequestDispatcher rd = request.getRequestDispatcher("/sysstat.jsp");
        rd.forward(request, response);
   }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuilder infoStr = new StringBuilder();
        StringBuilder exceptionStr = new StringBuilder();
        String sortOpt = request.getParameter("sortOpt");
        String showAll = request.getParameter("showAll");
        if (showAll != null && 
            showAll.compareTo("ALL") == 0)
            request.setAttribute("showAll", "true");    
        
        if (sortOpt == null) {
            infoStr.setLength(0);
            infoStr.append("Internal Error: Sort Options " +
                "are null.\n");
        }
        else if (sortOpt.compareTo("CPU") != 0 && 
                 sortOpt.compareTo("MEM") != 0 &&
                 sortOpt.compareTo("PID") != 0) {
            infoStr.setLength(0);
            infoStr.append("Internal Error: Sort Options " +
                "parameter not set to CPU or MEM or PID.\n");
        }
        ArrayList <SysDesc>psList = RunCmd.runCmdRefreshProcStat(sortOpt, infoStr, exceptionStr);
        
        request.setAttribute("psList", psList);
        request.setAttribute("sysStat", RunCmd.runCmdRefreshSysStat(infoStr, exceptionStr));
        request.setAttribute("infoStr", infoStr.toString());
        request.setAttribute("exceptionStr", (exceptionStr.toString().compareTo("") == 0)?
                             "None." : exceptionStr.toString());
        RequestDispatcher rd = request.getRequestDispatcher("/sysstat.jsp");
        rd.forward(request, response);
    }
}
 