<%@ Page Language="C#" AutoEventWireup="true" ValidateRequest="false" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ Import Namespace="System.Net" %>
<%@ Import Namespace="System.Text.RegularExpressions" %>
<%@ Import Namespace="System.Security.Cryptography" %>
<%@ Import Namespace="System.Security.Cryptography.X509Certificates" %>
<%@ Import Namespace="System.IO" %>
<%@ Import Namespace="System.Xml" %>
<%@ Import Namespace="Google.GData.Client" %>
<%@ Import Namespace="Google.GData.Extensions" %>



<script runat="server">

    AsymmetricAlgorithm getRsaKey()
    {
      X509Certificate2 cert = new X509Certificate2("/path/to/yourprivatekey.pfx", "pa$$word");
      RSACryptoServiceProvider privateKey = cert.PrivateKey as RSACryptoServiceProvider;
    
      return privateKey;
    }
    
    void PostNotice()
    {
        GAuthSubRequestFactory authFactory = new GAuthSubRequestFactory("weaver", "exampleCo-exampleApp-1");
        authFactory.Token = (String)Session["token"];
        authFactory.PrivateKey = getRsaKey();

        Service service = new Service(authFactory.Service, authFactory.ApplicationName);
        service.RequestFactory = authFactory;
       
        AtomEntry newNotice = new AtomEntry();
        newNotice.Title.Text = Request.Form["subject"];
        newNotice.Content.Content = Request.Form["message"];
        //newNotice.Content.Type = "html";

        if (Request.Form["ccr"] != "")
        {
            XmlDocument ccrDoc = new XmlDocument();
            ccrDoc.LoadXml(Request.Form["ccr"]);
            newNotice.ExtensionElements.Add(ccrDoc.DocumentElement);
        }
        
        service.Insert(new Uri("https://www.google.com/h9/feeds/register/default"), newNotice);

        //blank the form elements since .NET loves to save state
        subject.Text = message.Text = ccr.Text = "";
        
    }
    
</script>


<html xmlns="http://www.w3.org/1999/xhtml" >
<head runat="server">
    <title>Google Code Samples: Google Health read profile sample</title>
</head>
<body>
    <form id="form1" runat="server">
    <h1>Google Health: Post Notices</h1>
    <div>
    <%
        GotoAuthSubLink.Visible = false;
        noticeForm.Visible = false;
        
        if (Session["token"] != null)
        {
            noticeForm.Visible = true;
            if (Request.Form["submit"] != null)
            {
                PostNotice();
            }
        }
        else if (Request.QueryString["token"] != null)
        {
            String token = Request.QueryString["token"];
            Session["token"] = AuthSubUtil.exchangeForSessionToken(token, getRsaKey()).ToString();
            Response.Redirect(Request.Url.AbsolutePath, true);
        }
        else //no auth data, print link
        {
            GotoAuthSubLink.Text = "Login to your Google Account";
            GotoAuthSubLink.Visible = true;
            String authSubLink = AuthSubUtil.getRequestUrl("http", "www.google.com",
                "/h9/authsub", Request.Url.ToString(), "https://www.google.com/h9/feeds/", true, true);
            authSubLink += "&permission=1";
            GotoAuthSubLink.NavigateUrl = authSubLink;
        }
        
     %>
    </div><div>
    <asp:HyperLink ID="GotoAuthSubLink" runat="server"/>
    </div><br /><div id="noticeForm" runat="server">
    <h2>Add a New Notice</h2>
    <table>
    <tr><td><asp:Label runat="server" Text="Subject:" /></td>
    <td><asp:TextBox ID="subject" columns="75" runat="server"/></td>
    </tr>
    <tr><td><asp:Label runat="server" Text="Message Body:"/></td>
    <td><asp:TextBox ID="message" rows="10" columns="60" TextMode="MultiLine" runat="server"/></td>
    </tr>
    <tr><td><asp:Label runat="server" Text="CCR Fragment:"/></td>
    <td><asp:TextBox ID="ccr" rows="10" columns="60" TextMode="MultiLine" runat="server"/></td>
    </tr><tr><td>
        <asp:Button ID="submit" runat="server" Text="Post Notice" /></td></tr>
    </table>
    </div>
    </form>
</body>
</html>