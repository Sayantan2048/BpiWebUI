<!DOCTYPE html>
<%@ page contentType="text/html;charset=windows-1252"%>
<html>
    <head>
        <title>GPIO Control</title>
        <meta http-equiv="Content-Type" content="text/html; charset=windows-1252"/>
        <link rel="stylesheet" type="text/css" href="./resources/css/component.css"/>
        <link rel="shortcut icon" type="image/png" href="./resources/images/favicon.png"/>        <script>
        function validate() {
            var oform = document.forms[0];
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
    <form name="f1" action="/BPIFS-MUnt-context-root/gpioact" method="post">
    <table>
        <tr>
            <td><label id="ctextbox2">Authentication:</label></td>
            <td><input type="password" name="authPass" id="ctextbox" required></td>
        </tr>
    </table>
    <br>
    
    <table class="CSSTableGenerator">
    <tr>
        <td>Pin</td><td>Execute</td><td>Status</td>
    </tr>
    <%  java.util.ArrayList<String> gpioList = (java.util.ArrayList) request.getAttribute("gpioList");
        if (gpioList != null) {
        for (int i = 0; i < gpioList.size(); i ++) {
                String buttonName = "GPIO_" + i;
                String gpioState = gpioList.get(i);
                String buttonValue = gpioState.compareTo("HIGH") == 0 ?
                    "LOW" : "HIGH";
    %>
    <tr>
        <td><%=buttonName%>:</td>
        <!-- Note to self: Form isn't submitted if validate() returns false.-->
        <td><input type="submit" id="rbutton" name="<%=buttonName%>" value="<%=buttonValue%>" onclick="return validate()"/></td>
        <td>Pin Status:<%=gpioState%></td>
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
        <td><form name="f2" action="/BPIFS-MUnt-context-root/gpioact" method="get">
        <input type="submit" value="Refresh" id="cbutton"/></form></td>
        <td><form name="f3" action="/BPIFS-MUnt-context-root/index.html" method="get">
        <input type="submit" value="Home" id="cbutton"/></form></td>
    </tr>
    </table>
    
    <h5>Status: <%=request.getAttribute("infoStr")==null?"":request.getAttribute("infoStr")%> </h5>
    <h5>Exceptions: <%=request.getAttribute("exceptionStr")==null?"":request.getAttribute("exceptionStr")%> </h5>
    
    <footer id="footer">    
    <p>Copyright &copy; 2016 &middot;<a href="mailto:std2048@gmail.com">Sayantan Datta</a>&nbspAll Rights Reserved &middot;</p>
    </footer>
    </body>
</html>