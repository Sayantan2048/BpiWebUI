<!DOCTYPE html>
<%@ page contentType="text/html;charset=windows-1252"%>
<%@ page import="munt.FSDesc" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=windows-1252"/>
        <link rel="stylesheet" type="text/css" href="./resources/css/component.css"/>
        <link rel="shortcut icon" type="image/png" href="./resources/images/favicon.png"/>
        <title>File System Stat</title>
        <script>
        function mountFnct() {
            var oform = document.forms["f2"];
            if (oform.elements["uuid"].value == null ||
                oform.elements["uuid"].value == "") {
                alert("Error:UUID field empty.");   
            } else if (oform.elements["mntpoint"].value == null ||
                oform.elements["mntpoint"].value == "") {
                alert("Error:Mount Point field empty.");   
            } else if (oform.elements["user"].value == null ||
                oform.elements["user"].value == "") {
                alert("Error:User field empty.");   
            } else if (oform.elements["group"].value == null ||
                oform.elements["group"].value == "") {
                alert("Error:Group field empty.");   
            } else if (oform.elements["authPass"].value == null ||
                oform.elements["authPass"].value == "") {
                alert("Error:Authentication pass required.");
            } else {
                oform.elements["whichButton"].value = oform.elements["mountButton"].value;
                oform.submit();
            }
        }
        function unMountFnct() {
            var oform = document.forms["f2"];
            if (oform.elements["uuid"].value == null ||
                oform.elements["uuid"].value == "") {
                alert("Error:UUID field empty.");   
            } else if (oform.elements["authPass"].value == null ||
                oform.elements["authPass"].value == "") {
                alert("Error:Authentication pass required.");
            } else {
                oform.elements["whichButton"].value = oform.elements["unMountButton"].value;
                oform.submit();
            }
        }
        </script>
    </head>
    
    <body id="body1">
        <!-- Table for df -h -->
        <table class="CSSTableGenerator">
        <tr>
            <td colspan="6">Mounted File System Details</td>
        </tr>
        <%  java.util.ArrayList <FSDesc>list = (java.util.ArrayList)request.getAttribute("fsList");
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    FSDesc f = list.get(i);
                    if (f.isMounted()) {
        %>
        <tr>
            <td><%=f.getDevName()%></td>
            <td><%=f.getSize()%></td>
            <td><%=f.getUsed()%></td>
            <td><%=f.getAvail()%></td>
            <td><%=f.getPercUsed()%></td>
            <td><%=f.getMntPoint()%></td>
        </tr>
        <%  }}} %>
        </table>
        <br> <br>
        
        <!-- Table for blikid -->
        <table class="CSSTableGenerator">
        <tr>
            <td colspan="3">Available File System Details</td>
        </tr>
        <tr>
            <td> Device Name</td>
            <td> UUId</td>
            <td> Format</td>
        </tr>
        <%  if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    FSDesc f = list.get(i);
                    if (!f.isMounted()) {
        %>
        <tr>
            <td><%=f.getDevName()%></td>
            <td><%=f.getUuid()%></td>
            <td><%=f.getFmtType()%></td>
        </tr>
        <%  }}
        } else {
            String infoStr = (String)request.getAttribute("infoStr");
            if (infoStr == null) {
                infoStr = "Click Refresh.";
            }
            request.setAttribute("infoStr", infoStr);
        }%>
        </table>        
        <br>
        
        <table>
            <tr>
            <td><form name="f1" action="/BPIFS-MUnt-context-root/rfrshfs" method="post">
            <input type="submit" id="cbutton" name="refreshButton" value="Refresh"/></form></td>
            <td><form name="f3" action="/BPIFS-MUnt-context-root/index.html" method="get">
            <input type="submit" id="cbutton" value="Home"/></form></td>
            </tr>
        </table>
        <br><br>
        
        <!-- Table for Mount/UnMount parameters -->
        <form name="f2" action="/BPIFS-MUnt-context-root/munt" method="post">
            <input type="hidden" name="whichButton"/>
        <table class="CSSTableGenerator">
            <tr><td>Enter Mount/UnMount Details</td></tr>
         </table>
         <table>
            <tr>
                <td colspan="1"><label id="ctextbox3">UUId</label></td>
                <td colspan="3"><input id="ctextbox" type="text" name="uuid" size="30"/></td>
            </tr>
            <tr>
                <td colspan="1"><label id="ctextbox3">Mount Point</label></td>
                <td colspan="3"><input id="ctextbox"type="text" name="mntpoint" size="30"/></td>
            </tr>
            <tr>
                <td colspan="1"><label id="ctextbox3">User</label></td>
                <td colspan="3"><input type="text" id="ctextbox" name="user" size="30" value="root"/></td>
            </tr>
            <tr>
                <td colspan="1"><label id="ctextbox3">Group</label></td>
                <td colspan="3"><input type="text" id="ctextbox" name="group" size="30" value="sambashare0"/></td>
            </tr>
            <tr> 
                <td colspan="1"><label id="ctextbox3" size="30">Kill options</label></td>
                <td colspan="3" id="ctextbox">
                <span style="color:Green;"><input type="radio" name="killOpt" value="NOKILL" checked="checked"/>None</span>
                <span style="color:Orange;"><input type="radio" name="killOpt" value="SIGTERM"/>SIGTERM</span>
                <span style="color:Red;font-weight:bold;"><input type="radio" name="killOpt" value="SIGKILL"/>SIGKILL</span>
                </td>
            </tr>
            <tr>
                <td colspan="1"><label id="ctextbox2">Authentication</label></td>
                <td colspan="3"><input type="password" id="ctextbox" name="authPass" size="30"></td>
            </tr>
            </table>
            <br>
            
            <table>
            <tr>
                <td colspan="1"><input type="button" id="rbutton" name="mountButton" onclick="mountFnct()" value="Mount"/></td>
                <td colspan="1"><input type="button" id="rbutton" name="unMountButton" onclick="unMountFnct()" value="UnMount"/></td>
            </tr>
        </table>
        </form>
        
        <br>
        <h5>Status: <%=request.getAttribute("infoStr")==null?"":request.getAttribute("infoStr")%> </h5>
        <h5>Exceptions: <%=request.getAttribute("exceptionStr")==null?"":request.getAttribute("exceptionStr")%> </h5>
        
        <footer id="footer">    
        <p>Copyright &copy; 2016 &middot;<a href="mailto:std2048@gmail.com">Sayantan Datta</a>&nbspAll Rights Reserved &middot;</p>
        </footer>
    </body>
</html>