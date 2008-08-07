from google.appengine.ext import db
import logging

class Registration(db.Model):
  email = db.StringProperty(multiline=False)
  status =db.StringProperty(multiline=False, choices=set(["registered", "pending", "unregistered"]))
  token = db.StringProperty(multiline=False)


def getReged(email, status="registered"):
  q = Registration.gql("WHERE email = :email AND status = :status", email=email, status=status)
  return q.get()

def register(email, status="registered", token=None):
  logging.info("register email=%s status=%s token=%s"%(email,status,token or 'NO TOKEN'))
  r = Registration.gql("WHERE email = :email", email=email).get()
  if r:
    r.status = status
    r.token=token
    r.put()
  else:
    Registration(email=email,status=status,token=token).put()

def getPending():
  """ get all pending emails"""
  return map(lambda x:x.email,Registration.gql("WHERE status = :status", status="pending").fetch(5))
  