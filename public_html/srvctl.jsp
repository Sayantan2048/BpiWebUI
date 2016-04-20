<!DOCTYPE html>
<%@ page contentType="text/html;charset=windows-1252"%>
<html>
    <head>
        <title>Server Control</title>
        <meta http-equiv="Content-Type" content="text/html; charset=windows-1252"/>
        <link rel="stylesheet" type="text/css" href="./resources/css/component.css"/>
        <link rel="shortcut icon" type="image/png" href="./resources/images/favicon.png"/>
        <script>
            function validate() {
                var oform = document.forms["f1"];
                if (oform.elements["authPass"].value == null ||
                    oform.elements["authPass"].value == "") {
                    alert("Error:Authentication pass required.");
                    return false;
                }
                return true;
            }
        </script>
    </head>
    <body id="body1">
    
    <form name="f1" action="/BPIFS-MUnt-context-root/srvctl" method="post">
    
    <%  java.util.ArrayList<munt.ServiceInfo> cmdList = (java.util.ArrayList)request.getAttribute("cmdList");
        if (cmdList != null) {
    %>
    <table>
        <tr>
            <td><label id="ctextbox2">Authentication:</label></td>
            <td><input type="password" id="ctextbox" name="authPass" required></td>
        </tr>
    </table>   
    <table class="CSSTableGenerator">
        <tr>
            <td>Process</td><td>Execute</td><td>Status</td>
        </tr>
    <%      munt.ServiceInfo f;
            for (int i = 0; i < cmdList.size(); i++) {
                f = cmdList.get(i);
                String buttonName = f.getProcessName();
                String cmdState = f.getProcessStatus();
                String buttonValue = cmdState.compareTo("RUNNING") == 0 ?
                    "STOP" : "START";
                cmdState = cmdState.compareTo("RUNNING") == 0 ? 
                    "Service running." : "Service not running";
    %>
    <tr>
        <td><%=buttonName%></td>
        <td>
        <!-- Note to self: Form isn't submitted if validate() returns false.-->
        <input type="submit" id="rbutton" name="<%=buttonName%>" value="<%=buttonValue%>" onclick="return validate()"/>
        </td>
        <td><%=cmdState%></td>
    </tr>
    <%      } 
        } else {
            String infoStr = (String)request.getAttribute("infoStr");
            if (infoStr == null) {
                infoStr = "Click Refresh.";
            }
            request.setAttribute("infoStr", infoStr);
        } 
    %>
    </table>
    </form>
    <br>
    
    <table>
    <tr>
        <td>
        <form name="f2" action="/BPIFS-MUnt-context-root/srvctl" method="get">
            <input type="submit" id="cbutton" value="Refresh"/>
        </form>
        </td>
        <td>
        <form name="f3" action="/BPIFS-MUnt-context-root/index.html" method="get">
            <input type="submit" id="cbutton" value="Home"/>
        </form>
        </td>
    </tr>
    </table>
    
    <h5>Status: <%=request.getAttribute("infoStr")==null?"":request.getAttribute("infoStr")%> </h5>
    <h5>Exceptions: <%=request.getAttribute("exceptionStr")==null?"":request.getAttribute("exceptionStr")%> </h5>
    
    <footer id="footer">    
    <p>Copyright &copy; 2016 &middot;<a href="mailto:std2048@gmail.com">Sayantan Datta</a>&nbspAll Rights Reserved &middot;</p>
    </footer>
    </body>
</html>