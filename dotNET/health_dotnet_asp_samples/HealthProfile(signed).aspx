<%@ Page Language="C#" AutoEventWireup="true" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ Import Namespace="System.Net" %>
<%@ Import Namespace="System.Text.RegularExpressions" %>
<%@ Import Namespace="System.Security.Cryptography" %>
<%@ Import Namespace="System.Security.Cryptography.X509Certificates" %>
<%@ Import Namespace="System.IO" %>
<%@ Import Namespace="System.Xml" %>
<%@ Import Namespace="Google.GData.Client" %>
<%@ Import Namespace="Google.GData.Health" %>
<%@ Import Namespace="Google.GData.Extensions" %>

<script runat="server">
    AsymmetricAlgorithm getRsaKey()
    {
      X509Certificate2 cert = new X509Certificate2("/path/to/yourprivatekey.pfx", "pa$$word");
      RSACryptoServiceProvider privateKey = cert.PrivateKey as RSACryptoServiceProvider;
    
      return privateKey;
    }


    void PrintProfile() {

        GAuthSubRequestFactory authFactory = new GAuthSubRequestFactory("weaver", "exampleCo-exampleApp-1");
        authFactory.Token = (String) Session["token"];
        authFactory.PrivateKey = getRsaKey();

        HealthService service = new HealthService(authFactory.ApplicationName);
        service.RequestFactory = authFactory;
        
        HealthQuery profileQuery = new HealthQuery("https://www.google.com/h9/feeds/profile/default");
        profileQuery.Digest = true;
        //profileQuery.ExtraParameters = "max-results=5";
      

        try
        {       
            HealthFeed feed = service.Query(profileQuery);
            
            foreach (HealthEntry entry in feed.Entries )
            {
                XmlNode ccr = entry.CCR;
                if (ccr != null)
                {
                    Response.Write("<pre>");
                    StringWriter sw = new StringWriter();
                    XmlTextWriter xw = new XmlTextWriter(sw);
                    xw.Formatting = Formatting.Indented;
                    ccr.WriteTo(xw);
                    Response.Write(HttpUtility.HtmlEncode(sw.ToString()));
                    Response.Write("</pre>");
                }
            }
        }
        catch (GDataRequestException gdre)
        {
            HttpWebResponse response = (HttpWebResponse)gdre.Response;
            
            //bad auth token, clear session and refresh the page
            if (response.StatusCode == HttpStatusCode.Unauthorized)
            {
                Session.Clear();
                Response.Redirect(Request.Url.AbsolutePath, true);
            }
            else
            {
                Response.Write("Error processing request: " + gdre.ToString());
            }
        }
    }
</script>


<html xmlns="http://www.w3.org/1999/xhtml" >
<head runat="server">
    <title>Google Code Samples: Google Health read profile sample</title>
</head>
<body>
    <form id="form1" runat="server">
    <h1>Google Health: Read Profile</h1>
    <div>
    <%
        GotoAuthSubLink.Visible = false;
        
        //Session.Clear();
        
        if (Session["token"] != null)
        {
            PrintProfile();
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
    <asp:HyperLink ID="GotoAuthSubLink" runat="server"/>
    </div>
    </form>
</body>
</html>