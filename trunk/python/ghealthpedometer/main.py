#!/usr/bin/python
#
# Copyright (C) 2009 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


__author__ = 'e.bidelman (Eric Bidelman)'

import os
import time
from django.utils import simplejson
from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp import template
from google.appengine.ext.webapp.util import run_wsgi_app

import gdata
import gdata.auth
import gdata.service
import ghealth_helper

health = ghealth_helper.GoogleHealthHelper()

class HealthGetOAuthToken(webapp.RequestHandler):
  def get(self):
    oauth_token = gdata.auth.OAuthTokenFromUrl(self.request.uri)
    if oauth_token:
      oauth_input_params = gdata.auth.OAuthInputParams(
          ghealth_helper.SIG_METHOD, ghealth_helper.CONSUMER_KEY,
          rsa_key=health.key)
      
      oauth_token.oauth_input_params = oauth_input_params

      health.client.SetOAuthToken(oauth_token)
      health.client.UpgradeToOAuthAccessToken()

    access_token = health.client.token_store.find_token(ghealth_helper.H9_SCOPE)
    self.response.out.write(access_token)
    if access_token and users.get_current_user():
      health.client.token_store.add_token(access_token)
    elif access_token:
      health.client.current_token = access_token
      health.client.SetOAuthToken(access_token)

    self.redirect('/')
    
  def post(self):
    user = users.get_current_user()
    if user:
      req_token = health.client.FetchOAuthRequestToken()
      health.client.SetOAuthToken(req_token)

      approval_page_url = health.client.GenerateOAuthAuthorizationURL(
          callback_url=self.request.uri, extra_params={'permission': 1})
      self.redirect(approval_page_url)


class HealthFetchData(webapp.RequestHandler):
  def get(self):
    data = health.GetPedometerReadings()
    html = []
    if data['error']:
      html.append(data['error'])
    return data['results']

  def post(self):
    reading = self.request.get('reading')
    unit = self.request.get('unit')
    if reading and unit:
      health.PostPedometerReading(
          reading, date=time.strftime("%Y-%m-%dT%H:%M:%SZ"), unit=unit)
    self.redirect('/')

class MainPage(HealthFetchData):
  def get(self):
    if not users.get_current_user():
      self.redirect(users.create_login_url(self.request.uri))

    access_token = health.client.token_store.find_token(ghealth_helper.H9_SCOPE)
    if isinstance(access_token, gdata.auth.OAuthToken):
      data = HealthFetchData.get(self)
      revoke_token_link = '/revoke_token'
    else:
      data = None
      access_token = ''
      revoke_token_link = ''

    template_values = {
      'access_token': access_token,
      'data': data,
      'revoke_token_link': revoke_token_link
      }

    path = os.path.join(os.path.dirname(__file__), 'index.html')
    self.response.out.write(template.render(path, template_values))

  def post(self):
    uri = self.request.uri
    self.redirect(uri)


class RevokeToken(webapp.RequestHandler):

  def get(self):
    """Revokes the current user's OAuth access token."""

    try:
      health.UnlinkProfile()
    except gdata.service.RevokingOAuthTokenFailed:
      pass

    self.redirect('/')


def main():
  application = webapp.WSGIApplication([('/', MainPage),
                                        ('/get_oauth_token', HealthGetOAuthToken),
                                        ('/fetch_data', HealthFetchData),
                                        ('/revoke_token', RevokeToken)],
                                         debug=False)
  run_wsgi_app(application)
  

if __name__ == '__main__':
  main()
