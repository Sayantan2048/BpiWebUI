<!DOCTYPE html>
<%@ page contentType="text/html;charset=windows-1252"%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=windows-1252"/>
        <link rel="stylesheet" type="text/css" href="./resources/css/component.css"/>
        <link rel="shortcut icon" type="image/png" href="./resources/images/favicon.png"/>
        <script>
            function goHome() {
                var oform = document.forms["f3"];
                oform.submit();
            }
            function validate() {
                var oform = document.forms["f1"];
                if (oform.elements["oldPass"].value == null ||
                    oform.elements["oldPass"].value == "") {
                    alert("Error:Old password required.");
                    return false;
                }
                if (oform.elements["newPass"].value == null ||
                    oform.elements["newPass"].value == "") {
                    alert("Error:New password required.");
                    return false;
                }
                return true;
            }
        </script>
    </head>
    <body id="body1">
    <form name="f3" action="/BPIFS-MUnt-context-root/index.html" method="get"></form>
    <form name="f1" action="/BPIFS-MUnt-context-root/chgpwd" method="post">
    <table>
        <tr>
            <td><label id="ctextbox2">Old Password:</label></td>
            <td><input type="password" name="oldPass" id="ctextbox" required></td>
        </tr>
        <tr>
            <td><label id="ctextbox2">New Password:</label></td>
            <td><input type="password" name="newPass" id="ctextbox" required></td>
        </tr>
    </table>
    <br>
    <table>
        <tr>
        <td><input type="submit" value="Change" name="chgPwd" onclick="return validate()" id="cbutton"/></td>
        <td><input type="button" value="Home" onclick="goHome()" id="cbutton"/></td>
        </tr>
    </table>
    </form>
    
    <h5>Status: <%=request.getAttribute("infoStr")==null?"":request.getAttribute("infoStr")%> </h5>
    <h5>Exceptions: <%=request.getAttribute("exceptionStr")==null?"":request.getAttribute("exceptionStr")%> </h5>
    
    <footer id="footer">    
    <p>Copyright &copy; 2016 &middot;<a href="mailto:std2048@gmail.com">Sayantan Datta</a>&nbspAll Rights Reserved &middot;</p>
    </footer>
    </body>
</html>