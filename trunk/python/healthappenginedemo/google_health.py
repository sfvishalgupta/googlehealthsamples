import math
import random
import logging
import atom.service
import gdata.service
import gdata.urlfetch
import time
import urllib
from google.appengine.api import urlfetch

from django.utils import simplejson
from tlslite.utils.keyfactory import parsePEMKey
from tlslite.utils.cryptomath import bytesToBase64
from private_key import PRIVATE_KEY

class _SigningWrapper:
  def __init__(self, handler):
    self.handler = handler
    self.rsa_key = parsePEMKey(PRIVATE_KEY)

  def HttpRequest(self, service, operation, data, uri, *args, **kw):
    try:
      auth_token = kw['extra_headers']['Authorization']
      timestamp = int(math.floor(time.time()))
      nonce = '%lu' % random.getrandbits(64)
      data = '%s %s %d %s' % (operation, uri, timestamp, nonce)
      sig = bytesToBase64(self.rsa_key.hashAndSign(data))
      auth_token += ' data="%s" sig="%s" sigalg="rsa-sha1"' % (data, sig)
      logging.info('Authorization: %s' % (auth_token,))
      kw = dict(kw)
      kw['extra_headers'] = dict(kw['extra_headers'])
      kw['extra_headers']['Authorization'] = auth_token
    except (AttributeError, KeyError):
      pass
    return self.handler.HttpRequest(service, operation, data, uri, *args, **kw)


GDATA_RESOURCE = "https://www.google.com/health/feeds/profile/default"
ACCESS_TOKEN_SETTINGS_URL = 'https://www.google.com/health/AuthenticationTokenSettings'
class TokenNameHandler:
  def __init__(self, token_name):
    self.token_name = token_name
  def HttpRequest(self, service, operation, data, uri, extra_headers=None,
    url_params=None, escape_params=True, content_type='application/atom+xml'):
    data = urllib.urlencode({"token_desc": self.token_name})
    logging.info("data="+data)
    return gdata.urlfetch.HttpResponse(urlfetch.fetch(url=ACCESS_TOKEN_SETTINGS_URL, payload=data, method=urlfetch.POST,
                                                      headers=extra_headers))

def set_access_token_name(token, token_name):    
  logging.info( 'ACCESS url=%s' % ACCESS_TOKEN_SETTINGS_URL)
  gdata_service = gdata.service.GDataService(handler=_SigningWrapper(TokenNameHandler(token_name)))
  gdata_service.SetAuthSubToken(str(token.key))
  resource = ACCESS_TOKEN_SETTINGS_URL
  converter = lambda x: x
  logging.info("Starting fetch of GData feed at %s", resource)
  response = gdata_service.Post(None, resource, converter=converter)
  logging.info('ACCESS RESPONSE => %s'%str(response))
  return response

def get_healthinfo(token, xml=None, max='1000'):
  """ retrieves the list of contacts for the given email address, token needs
      to be authorised for this user """

  gdata_service = gdata.service.GDataService(handler=_SigningWrapper(gdata.urlfetch))
  gdata_service.SetAuthSubToken(str(token.key))
  resource = GDATA_RESOURCE
  if not xml:
    resource+='?alt=json'
  resource+='&max-results=%s'%max      

  converter = lambda x: x
  logging.info("Starting fetch of GData feed at %s", resource)
  feed = gdata_service.Get(resource, converter=converter)
  logging.info("Receive response %s", feed)
  return feed
