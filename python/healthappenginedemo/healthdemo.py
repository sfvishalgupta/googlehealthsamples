import wsgiref.handlers
import cgi
import os
from google.appengine.ext.webapp import template
import re
import datetime
import time
import string
import urllib2
import logging
import random
import base64

from django.utils import simplejson
from oauth.oauth import *
from oauth.rsa import *
from hrp import index
from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext import db
from google.appengine.api import urlfetch
from model import *
from google_health import *
import oauth_util

local =  os.environ.get('SERVER_SOFTWARE','').startswith('Devel')

if local:
  SERVER, PORT = 'http://localhost', 8080
else:
  SERVER, PORT = 'http://healthdemo.appspot.com', 80


CALLBACK_URL = 'https://www.google.com/health/oauth?oauth_callback='+ \
               '%s:%d'%(urllib.quote(SERVER),PORT)+'/request_token_ready&permission=1&oauth_token='
CONSUMER = 'healthdemo.appspot.com'


class MainPage(webapp.RequestHandler):
  def gallardoReg(self):
    path = os.path.join(os.path.dirname(__file__), 'registration.html')
    self.response.out.write(template.render(path,
                                            {'logout':users.CreateLogoutURL(self.request.uri)}))  

  def get(self):
    user = users.GetCurrentUser()
    self.response.headers['Content-Type'] = 'text/html'
    if not user:
      self.response.out.write("<br><a href=\""+users.CreateLoginURL(self.request.uri)+"\">Sign in</a>")
      return

    reg = getReged(user.email())
    if not reg:
      self.gallardoReg()
      return
    ## decrypt the OAuth token after it was in the datastore, since the admin account could be
    ## compromised and we dont wont to leak tokens. This is a general GAE problem.
    try:
      token = OAuthToken(reg.token,'')
      feed_xml = get_healthinfo(token, xml=False, max=self.request.get('max-results','1000')).replace("\"$t\":","\"T\":")
    except Exception , e:
      logging.info(e)
      self.redirect('/revoke')
      return
    set_access_token_name(token, "TOTO_"+token.key[-8:])    
    profile = simplejson.loads(feed_xml)
    ## some health specific loop to add a link to the HRP page if it exists...
    for en in profile['feed'].get('entry',{}):
        for e  in en.get('ContinuityOfCareRecord',{}).get('Body',{}).get('Problems',{}).get('Problem',{}):
          k = e['Description']['Text']
          if index.find(k['T'])!=-1: k['HRP']=urllib.quote(k['T'])

    path = os.path.join(os.path.dirname(__file__), 'index.html')
    self.response.out.write(template.render(path, {'profile':profile,
                                                     'logout':users.CreateLogoutURL(self.request.uri)}))

class DownloadPage(MainPage):
  def get(self):
    user = users.GetCurrentUser()
    if not user:
      return
    self.response.headers['Content-Type'] = 'application/force-download'   
    self.response.headers['Content-Disposition']="attachment; filename=yourhealth.xml";
    reg = getReged(user.email())
    if not reg:
      self.gallardoReg()
      return

    ## store the OAuth token in the datastore, The admin account could be
    ## compromised but an attacked needs the private key, since all tokens are issued secure.
    token = OAuthToken(reg.token,'')
    feed_xml = get_healthinfo(token, xml=True)
    self.response.out.write(feed_xml)
    return


class TokenPage(webapp.RequestHandler):
   def get(self):
     user = users.GetCurrentUser()
     if user:
       token = cgi.escape(self.request.get('oauth_token'))
       access_token = oauth_util.get_access_token(self.request, token, CONSUMER)
       register(user.email(), token=str(access_token.key))
     self.redirect("/")

class RevokeRegistrationPage(webapp.RequestHandler):
   def get(self):
     user = users.GetCurrentUser()
     if user:
       register(user.email(), status="unregistered")
     self.redirect('/')

class HealthRegistrationPage(webapp.RequestHandler):
   def post(self):
     user = users.GetCurrentUser()
     if user:
       token = oauth_util.get_request_token(self.request, CONSUMER)
       self.redirect(CALLBACK_URL+token.key)
       return
     self.redirect("/")

def main():
  application = webapp.WSGIApplication([('/', MainPage),
                                        ('/revoke', RevokeRegistrationPage),
                                        ('/download', DownloadPage),
                                        ('/registration', HealthRegistrationPage),
                                        ('/request_token_ready', TokenPage)])
  wsgiref.handlers.CGIHandler().run(application)

if __name__ == "__main__":
  main()