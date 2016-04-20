<!DOCTYPE html>
<%@ page contentType="text/html;charset=windows-1252"%>
<html>
    <head>
        <title>System Stat</title>
        <meta http-equiv="Content-Type" content="text/html; charset=windows-1252"/>
        <link rel="stylesheet" type="text/css" href="./resources/css/component.css"/>
        <link rel="shortcut icon" type="image/png" href="./resources/images/favicon.png"/>
        <script>
        function goHome() {
            var oform = document.forms["f3"];
            oform.submit();
        }
        </script>
    </head>
    <body id="body1">
    <form name="f3" action="/BPIFS-MUnt-context-root/index.html" method="get"></form>
    <form name="f1" action="/BPIFS-MUnt-context-root/rfrshsys" method="post">
    <table>
        <tr>
            <td colspan="1"><label id="ctextbox4">Sort options</label></td>
            <td colspan="3" id="ctextbox">
                <span>
                <input type="radio" name="sortOpt" value="CPU" checked="checked"/>
                Sort by CPU</span>
                <span>
                <input type="radio" name="sortOpt" value="MEM"/>
                Sort by Memory</span>
                <span>
                <input type="radio" name="sortOpt" value="PID"/>
                Sort by PID</span>
            </td>
        </tr>
        <tr>
            <td colspan="1"><label id="ctextbox4">Show all process</label></td>
            <td colspan="1" id="ctextbox"><input type="checkbox" name="showAll" value="ALL"/></td>
            <td>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp</td>
        </tr>
    </table>
    <br>
    
    <table>
        <tr>
            <td colspan="1"><input type="submit" name="refrshButton" value="Refresh" id="cbutton"/></td>
            <td><input type="button" onclick="goHome()" value="Home" id="cbutton"/></td>
        </tr> 
    </table>    
    </form>
    <br><br>
    
    <div class="CSSTableGenerator">   
    <!-- Get HTML Table formatted sys stats --> 
    <%=request.getAttribute("sysStat")%>
    </div>
    <br><br>
    
    <table class="CSSTableGenerator">
    <%  java.util.ArrayList<munt.SysDesc> psList = (java.util.ArrayList)request.getAttribute("psList");
        if (psList != null) {
    %>
        <tr>
            <td colspan="5">Process Stats</td>
        </tr>   
        <tr>
            <td>CPU%</td>
            <td>Memory%</td>
            <td>PID</td>
            <td>User</td>
            <td>Command</td>
        </tr>
    <%      munt.SysDesc f;
            boolean showAll = request.getAttribute("showAll")!=null?true:false;
            for (int i = 0; i < psList.size() && (
                (i < 25) || showAll); i++) {
                f = psList.get(i);
    %>
                <tr>
                <td><%=f.getPercCpu()%></td>
                <td><%=f.getPercMem()%></td>
                <td><%=f.getPid()%></td>
                <td><%=f.getUser()%></td>
                <td><%=f.getCommand()%></td>
                </tr>
    <%       }
        } else {
            String infoStr = (String)request.getAttribute("infoStr");
            if (infoStr == null) {
                infoStr = "Click Refresh.";
            }
            request.setAttribute("infoStr", infoStr);
        }
    %>
    </table>
    
    <h5>Status: <%=request.getAttribute("infoStr")==null?"":request.getAttribute("infoStr")%> </h5>
    <h5>Exceptions: <%=request.getAttribute("exceptionStr")==null?"":request.getAttribute("exceptionStr")%> </h5>
    
    <footer id="footer">    
    <p>Copyright &copy; 2016 &middot;<a href="mailto:std2048@gmail.com">Sayantan Datta</a>&nbspAll Rights Reserved &middot;</p>
    </footer>
    </body>
</html>