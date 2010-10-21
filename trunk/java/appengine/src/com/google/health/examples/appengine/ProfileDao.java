package com.google.health.examples.appengine;

import java.util.List;

import javax.jdo.PersistenceManager;

public class ProfileDao {
  @SuppressWarnings("unchecked")
  public Profile retrieveProfileByEmail(String email) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    String query = "select from " + Profile.class.getName() + " where email == '" + email + "'";
    List<Profile> profiles = (List<Profile>) pm.newQuery(query).execute();
    Profile profile = null;

    if (profiles.size() == 1) {
      profile = profiles.get(0);
    } else if (profiles.size() > 1) {
      throw new IllegalStateException("Multiple profiles with same email address.");
    }

    pm.close();

    return profile;
  }

  public Profile createOrUpdateProfile(Profile profile) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    profile = pm.makePersistent(profile);
    pm.close();

    return profile;
  }
}
