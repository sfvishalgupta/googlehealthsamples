/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.health.android.example.auth;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.health.android.example.HealthAndroidExample;

/**
 * AuthManager keeps track of the current auth token for a user. The advantage
 * over just passing around a String is that this class can renew the auth token
 * if necessary, and it will change for all classes using this AuthManager.
 */
public class AuthManager {
  /** The activity that will handle auth result callbacks. */
  private final Activity activity;

  /** The name of the service to authorize for. */
  private final String service;

  /** The most recently fetched auth token or null if none is available. */
  private String authToken;

  private final AccountManager accountManager;

  private Runnable whenFinished;

  /**
   * AuthManager requires many of the same parameters as
   * {@link com.google.android.googlelogindist.GoogleLoginServiceHelper
   * #getCredentials(Activity, int, Bundle, boolean, String, boolean)}.
   * The activity must have a handler in {@link Activity#onActivityResult}
   * that calls {@link #authResult(int, Intent)} if the request code is the code
   * given here.
   *
   * @param activity
   *          An activity with a handler in {@link Activity#onActivityResult}
   *          that calls {@link #authResult(int, Intent)} when {@literal code}
   *          is the request code
   * @param service
   *          The name of the service to authenticate as
   */
  public AuthManager(Activity activity, String service) {
    this.activity = activity;
    this.service = service;
    this.accountManager = AccountManager.get(activity);
  }

  /**
   * Call this to do the initial login. The user will be asked to login if they
   * haven't already. The {@link Runnable} provided will be executed when the
   * auth token is successfully fetched.
   *
   * @param runnable
   *          A {@link Runnable} to execute when the auth token has been
   *          successfully fetched and is available via {@link #getAuthToken()}
   */
  public void doLogin(final Runnable runnable, Object o) {
    this.whenFinished = runnable;
    if (!(o instanceof Account)) {
      throw new IllegalArgumentException("AuthManager requires an account.");
    }
    Account account = (Account) o;
    accountManager.getAuthToken(account, service, true, new AccountManagerCallback<Bundle>() {
      public void run(AccountManagerFuture<Bundle> future) {
        try {
          Bundle result = future.getResult();

          // AccountManager needs user to grant permission
          if (result.containsKey(AccountManager.KEY_INTENT)) {
            Intent intent = (Intent) result.get(AccountManager.KEY_INTENT);
            clearNewTaskFlag(intent);
            // TODO Shouldn't need access to HealthAndroidExample... pass activity as parameter.
            activity.startActivityForResult(intent, HealthAndroidExample.ACTIVITY_GET_LOGIN);
            return;
          }

          authToken = result.getString(AccountManager.KEY_AUTHTOKEN);
          // TODO Shouldn't need access to HealthAndroidExample... pass log_tag in constructor.
          Log.d(HealthAndroidExample.LOG_TAG, "Retrieved auth token.");
          runWhenFinished();
        } catch (OperationCanceledException e) {
          Log.e(HealthAndroidExample.LOG_TAG, "Operation Canceled", e);
        } catch (IOException e) {
          Log.e(HealthAndroidExample.LOG_TAG, "IOException", e);
        } catch (AuthenticatorException e) {
          Log.e(HealthAndroidExample.LOG_TAG, "Authentication Failed", e);
        }
      }
    }, null /* handler */);
  }

  private static void clearNewTaskFlag(Intent intent) {
    int flags = intent.getFlags();
    flags &= ~Intent.FLAG_ACTIVITY_NEW_TASK;
    intent.setFlags(flags);
  }

  /**
   * The {@link Activity} passed into the constructor should call this function
   * when it gets {@link Activity#onActivityResult} with the request code passed
   * into the constructor. The resultCode and results should come directly from
   * the {@link Activity#onActivityResult} function. This function will return
   * true if an auth token was successfully fetched or the process is not
   * finished.
   *
   * @param resultCode
   *          The result code passed in to the {@link Activity}'s
   *          {@link Activity#onActivityResult} function
   * @param results
   *          The data passed in to the {@link Activity}'s
   *          {@link Activity#onActivityResult} function
   * @return True if the auth token was fetched or we aren't done fetching the
   *         auth token, or False if there was an error or the request was
   *         canceled
   */
  public boolean authResult(int resultCode, Intent results) {
    if (results != null) {
      authToken = results.getStringExtra(AccountManager.KEY_AUTHTOKEN);
      Log.d(HealthAndroidExample.LOG_TAG, "authResult: " + authToken);
    } else {
      Log.e(HealthAndroidExample.LOG_TAG, "No auth result results!!");
    }
    runWhenFinished();
    return authToken != null;
  }

  /**
   * Returns the current auth token. Response may be null if no valid auth token
   * has been fetched.
   *
   * @return The current auth token or null if no auth token has been fetched
   */
  public String getAuthToken() {
    return authToken;
  }

  public void invalidateAuthToken(final Runnable runnable) {
    this.whenFinished = runnable;

    activity.runOnUiThread(new Runnable() {
      public void run() {
        accountManager.invalidateAuthToken(HealthAndroidExample.ACCOUNT_TYPE, authToken);
      }
    });
  }

  private void runWhenFinished() {
    // Modified from original code, which was creating its own thread and caused a Looper problem.
    if (whenFinished != null) {
      activity.runOnUiThread(whenFinished);
    }
  }
}
