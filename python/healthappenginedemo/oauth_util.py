from oauth.oauth import OAuthError
from oauth.oauth import OAuthConsumer
from oauth.oauth import OAuthClient
from oauth.oauth import OAuthToken
from oauth.oauth import OAuthRequest
from private_key import LocalOAuthSignatureMethod_RSA_SHA1

import urllib
import logging
import cgi
from google.appengine.api import urlfetch



class SimpleOAuthClient(OAuthClient):
  def __init__(self, server, port, request_token_url='', access_token_url='', authorization_url=''):
    self.server = server
    self.port = port
    self.request_token_url = request_token_url
    self.access_token_url = access_token_url
    self.authorization_url = authorization_url

  def fetch_request_token(self, oauth_request, scope):
    # via GET
    # -> OAuthToken
    url = oauth_request.to_url()
    logging.info( 'REQUEST url=%s'%url)
    response = urlfetch.fetch(url)
    logging.info( 'RESPONSE => %s'%response.content)
    return OAuthToken.from_string(response.content)

  def fetch_access_token(self, oauth_request):
    # via GET
    # -> OAuthToken
    url = oauth_request.to_url()
    logging.info( 'ACCESS url=%s'%url)
    response = urlfetch.fetch(url)
    logging.info( 'ACCESS RESPONSE => %s'%response.content)
    return OAuthToken.from_string(response.content)

REQUEST_TOKEN_URL = 'https://www.google.com/accounts/OAuthGetRequestToken'
ACCESS_TOKEN_URL = 'https://www.google.com/accounts/OAuthGetAccessToken'
## the authorisation URL is different for google health from other google APIS:
##AUTHORIZATION_URL = 'https://www.google.com/accounts/OAuthAuthorizeToken'
AUTHORIZATION_URL = 'https://www.google.com/health/oauth'

SCOPE_URL = 'https://www.google.com/health/feeds'

def get_request_token(request, consumer_name, scope=SCOPE_URL):
  request.META=request.environ
  client = SimpleOAuthClient(request.META['SERVER_NAME'], request.META['SERVER_PORT'], REQUEST_TOKEN_URL, ACCESS_TOKEN_URL, AUTHORIZATION_URL)
  consumer = OAuthConsumer(consumer_name, "")
  signature_method = LocalOAuthSignatureMethod_RSA_SHA1()

  # get request token
  logging.info('* Obtain a request token ...')
  oauth_request = OAuthRequest.from_consumer_and_token(consumer, http_url=client.request_token_url, parameters={'scope':SCOPE_URL})
  oauth_request.sign_request(signature_method, consumer, None)
  token = client.fetch_request_token(oauth_request, SCOPE_URL)
  logging.info( 'REQUEST TOKEN: %s' % str(token.key))
  return token

def get_access_token(request, request_token, consumer_name):
  request.META=request.environ
  client = SimpleOAuthClient(request.META['SERVER_NAME'], request.META['SERVER_PORT'], REQUEST_TOKEN_URL, ACCESS_TOKEN_URL, AUTHORIZATION_URL)
  consumer = OAuthConsumer(consumer_name, "")
  signature_method = LocalOAuthSignatureMethod_RSA_SHA1()

  # get request token
  logging.info('* Obtain an access token ... for request_token '+request_token)
  token = OAuthToken(request_token,'')
  oauth_request = OAuthRequest.from_consumer_and_token(consumer, token=token, http_url=client.access_token_url)
  oauth_request.sign_request(signature_method, consumer, token)
  access_token = client.fetch_access_token(oauth_request)
  logging.info("* GET ACESS TOKEN"+access_token.key)
  return access_token

