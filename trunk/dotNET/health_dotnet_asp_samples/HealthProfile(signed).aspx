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
		XmlExtension ccrExt = (XmlExtension) entry.FindExtension("ContinuityOfCareRecord", "urn:astm-org:CCR");

                XmlDocument ccrXMLDoc = new XmlDocument();
                ccrXMLDoc.ImportNode(ccrExt, true);
                XmlNamespaceManager ccrNameManager = new XmlNamespaceManager(ccrXMLDoc.NameTable);
                ccrNameManager.AddNamespace("ccr", "urn:astm-org:CCR");
                
		// use xpath to extract specific CCR elements
		XmlNodeList meds = ccrExt.Node.SelectNodes("//ccr:Body/ccr:Medications/ccr:Medication/ccr:Product/ccr:ProductName/ccr:Text", ccrNameManager);
		XmlNodeList conditions = ccrExt.Node.SelectNodes("//ccr:Body/ccr:Problems/ccr:Problem/ccr:Description/ccr:Text", ccrNameManager);
		XmlNodeList allergies = ccrExt.Node.SelectNodes("//ccr:Body/ccr:Alerts/ccr:Alert/ccr:Description/ccr:Text", ccrNameManager);
		XmlNodeList procedures = ccrExt.Node.SelectNodes("//ccr:Body/ccr:Procedures/ccr:Procedure/ccr:Description/ccr:Text", ccrNameManager);
		XmlNodeList immunizations = ccrExt.Node.SelectNodes("//ccr:Body/ccr:Immunizations/ccr:Immunization/ccr:Product/ccr:ProductName/ccr:Text", ccrNameManager);
                
		PrintElement(meds, "Your Medications");
		PrintElement(conditions, "Your Conditions");
		PrintElement(allergies, "Your Allergies");
		PrintElement(procedures, "Your Procedures");
		PrintElement(immunizations, "Your Immunizations");
		
                XmlNode ccr = ccrExt;
		if (ccr != null)
                {
                    Response.Write("<div style=\"clear:both;\"><h3>Entire profile</h3><pre>");
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
    
    void PrintElement(XmlNodeList nodeList, String title) {
	Response.Write("<span class=\"right\"><b>" + title + "</b><ol>");
	foreach (XmlNode element in nodeList)
	{
		Response.Write("<li>" + HttpUtility.HtmlEncode(element.InnerText) + "</li>");
	}
	Response.Write("</ol></span>");
    }
</script>


<html xmlns="http://www.w3.org/1999/xhtml" >
<head runat="server">
    <title>Google Health API .NET - Profile Read Sample</title>
    <style type="text/css">
      ul,ol {
	margin:0;
	margin-top:10px;
	padding:0;
	list-style:none;
      }
      pre {
        border:1px solid #ccc;
	padding:15px;
      }
      .right {
	float:left;
	width:200px;
      }
    </style>
</head>
<body>
    <form id="form1" runat="server">
    <h1>Google Health API - .NET Profile Read Sample</h1>
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