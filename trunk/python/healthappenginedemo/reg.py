import wsgiref.handlers
import cgi
import os
from google.appengine.ext.webapp import template
import re
import urllib
import datetime
import string
import logging

from google.appengine.api import users
from google.appengine.ext import webapp

from model import *


  
class RegPage(webapp.RequestHandler):
  def get(self):
    u = urllib.unquote(self.request.path.split("/")[-1])
    self.response.out.write("Registering "+u+"...")
    user = users.GetCurrentUser()
    if not user:
      self.response.out.write("<br><a href=\""+users.CreateLoginURL(self.request.uri)+"\">Sign in (must be over 18)</a>")
      return   
    permit = isReged(user.email())
    if not permit:
      self.response.out.write("Permission required.")
      return
    register(u,"registered")
    self.redirect("/")
def main():
  application = webapp.WSGIApplication([('/.*', RegPage)])
  wsgiref.handlers.CGIHandler().run(application)

if __name__ == "__main__":
  main()