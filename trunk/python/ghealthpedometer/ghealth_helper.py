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

import gdata
import gdata.auth
import gdata.health
import gdata.health.service
import gdata.alt.appengine

# Health constants
H9_SCOPE = 'https://www.google.com/h9/feeds/'
HEALTH_ITEM_NS = '{http://schemas.google.com/health/item}'

# OAuth 'credentials'
CONSUMER_KEY = 'YOUR_CONSUMER_KEY'  # CHANGE ME
PRIV_KEY_PATH = '/path/to/your/rsa_private_key.pem' # CHANGE ME
SIG_METHOD = gdata.auth.OAuthSignatureMethod.RSA_SHA1

APP_NAME = 'google-HealthPedometerOAuthDemo-v1'  # CHANGE ME

class GoogleHealthHelper(object):
  """Provides the pedometer functionality to interact w/ the Health API.

  Attributes:
    WALK_RECORD_NAME: The unique name of a pedometer reading test result
        as stored in Google Health. This name is also used to restrict query
        results to return only data entered by this app.
  """
  WALK_RECORD_NAME = 'Walking Distance'

  def __init__(self, client=None):
    if not client:
      client = gdata.health.service.HealthService(source=APP_NAME,
                                                  use_h9_sandbox=True)
          
      f = open(PRIV_KEY_PATH)
      self.key = f.read()
      f.close()

      client.SetOAuthInputParameters(SIG_METHOD, CONSUMER_KEY, rsa_key=self.key)
    gdata.alt.appengine.run_on_appengine(client)
    self.client = client

  def UnlinkProfile(self):
    """Unlinks the user's proifle from the app by revoking the OAuth token.

    Raises:
      NonOAuthToken if the user's auth token is not an OAuth token.
      RevokingOAuthTokenFailed if request for revoking an OAuth token failed.
    """
    self.client.RevokeOAuthToken()
    self.client.token_store.remove_all_tokens()

  def GetPedometerReadings(self):
    """Pulls a user's pedometer related test results and returns JSON.

    Returns:
      A JSON object containing the user's results data.
    """
    json = dict([('results', []), ('error', {})])
    try:
      params = {'digest': 'true', 'strict': 'true'}
      query = gdata.health.service.HealthProfileQuery(
          service='h9', params=params,
          categories=['LABTEST', HEALTH_ITEM_NS + self.WALK_RECORD_NAME])
      feed = self.client.GetProfileFeed(query)

      if not feed.entry:
        json['error'] = 'No entries yet.'
        return json

      for entry in feed.entry:
        try:
          results = entry.ccr.GetResults()
          for result in results:
            test = result.FindChildren('Test')[0]
            name = test.FindChildren('Description')[0].FindChildren('Text')[0].text
            value = test.FindChildren('TestResult')[0].FindChildren('Value')[0].text
            unit = test.FindChildren('TestResult')[0].FindChildren('Units')[0].FindChildren('Unit')[0].text
            date = test.FindChildren('DateTime')[0].FindChildren('ExactDateTime')[0].text

            json['results'].append({'name': name, 'value': value,
                                    'unit': unit, 'date': date})
        except:
          json['error'] = 'Could not find one or more CCR elements.'
    except gdata.service.RequestError, request_error:
      if request_error[0]['status'] == 401:
        json['error'] = request_error[0]

    return json

  def PostPedometerReading(self, value, unit=None, date=None):
    """Posts a new pedometer reading to a user's GHealth test results.

    Args:
      value: int The pedometer reading.
      unit: (optional) string The unit value was measured in.
      date: (optional) string Date the pedometer reading was taken.
          This value should be a RFC3339 timestamp formatted date string
          (e.g. '2009-01-26T00:00:00-08:00'). The current timestamp is
          used if this paramater is absent.
    """
    if not date:
      date = str(time.strftime("%Y-%m-%dT%H:%M:%S"))

    ccr_str = """
        <ContinuityOfCareRecord xmlns='urn:astm-org:CCR'>
         <Body>
           <Results>
             <Result>
               <Test>
                 <DateTime>
                    <Type><Text>Collection start date</Text></Type>
                    <ExactDateTime>%s</ExactDateTime>
                  </DateTime>
                  <Description>
                    <Text>%s</Text>
                    <Code>
                      <Value>165263003</Value>
                      <CodingSystem>SNOMED</CodingSystem>
                    </Code>
                  </Description>
                  <TestResult>
                    <Value>%s</Value>
                    <Units><Unit>%s</Unit></Units>
                  </TestResult>
                  <Source>
                    <Actor>
                      <ActorID>Google Health Pedometer Demo</ActorID>
                      <ActorRole><Text>Ordering clinician</Text></ActorRole>
                    </Actor>
                  </Source>
               </Test>
             </Result>
           </Results>
         </Body>
       </ContinuityOfCareRecord>""" % (date, self.WALK_RECORD_NAME,
                                       value, unit)

    title = 'New Health App Pedometer Reading'
    body = 'This reading was added to your profile'
    created_entry = self.client.SendNotice(title, body=body,
                                           content_type='html', ccr=ccr_str)
