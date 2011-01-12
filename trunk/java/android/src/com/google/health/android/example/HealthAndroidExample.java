/*
 * Copyright (c) 2010 Google Inc.
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

package com.google.health.android.example;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import android.accounts.Account;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.google.health.android.example.HealthClient.AuthenticationException;
import com.google.health.android.example.HealthClient.InvalidProfileException;
import com.google.health.android.example.HealthClient.ServiceException;
import com.google.health.android.example.auth.AccountChooser;
import com.google.health.android.example.auth.AuthManager;
import com.google.health.android.example.gdata.HealthGDataClient;
import com.google.health.android.example.gdata.Result;
import com.google.health.android.example.gdata.Test;

public final class HealthAndroidExample extends ListActivity {
  private static final String SERVICE_NAME = HealthClient.H9_SERVICE;
  public static final String LOG_TAG = "HealthAndroidExample";

  private static final int ACTIVITY_AUTHENTICATE = 0;
  // Public so that the AuthManager can start a new get_login activity after the
  // user has authorized the app to access their data.
  public static final int ACTIVITY_GET_LOGIN = 1;
  private static final int ACTIVITY_ADD_RESULT = 2;

  private static final int DIALOG_PROFILES = 0;
  private static final int DIALOG_PROGRESS = 1;
  private static final int DIALOG_ERROR = 2;

  /** Property key for returning a result from a child activity. */
  public static final String RESULT_PROPERTY = "result";

  public static final String ACCOUNT_TYPE = "com.google";

  /** Handler for posting results from worker threads to UI thread. */
  private final Handler handler = new Handler();

  /** Service client for send to and retrieving information from Google Health. */
  private final HealthClient client = new HealthGDataClient(SERVICE_NAME);

  private Map<String, String> profiles = new LinkedHashMap<String, String>();

  private List<Result> results;

  private AuthManager auth;
  private Account account;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    auth = new AuthManager(this, SERVICE_NAME);

    chooseAccount();
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    Dialog dialog;

    switch (id) {
    case DIALOG_PROGRESS:
      dialog = ProgressDialog.show(HealthAndroidExample.this, "", "Loading. Please wait...", true);
      break;

    case DIALOG_PROFILES:
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle("Select a Health profile");

      String[] profileNames = profiles.values().toArray(new String[profiles.size()]);

      builder.setItems(profileNames, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int i) {
          // Remove the dialog so that it's refreshed with new list items the
          // next time it's displayed. onPrepareDialog cannot change the dialog's
          // list items.
          removeDialog(DIALOG_PROFILES);
          String pid = profiles.keySet().toArray(new String[profiles.size()])[i];
          client.setProfileId(pid);
          retrieveResults();
        }
      });

      dialog = builder.create();
      break;

    case DIALOG_ERROR:
      AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
      builder2.setMessage("Error").setCancelable(false).setPositiveButton("Close",
          new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              dialog.cancel();
            }
          });

    default:
      dialog = null;
    }

    return dialog;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    switch (requestCode) {
    case ACTIVITY_AUTHENTICATE:
      if (resultCode == RESULT_OK) {
        if (auth.getAuthToken() == null) {
          Log.w(LOG_TAG, "User authenticated, but auth token not found.");
          authenticate(account);
        } else {
          Log.d(LOG_TAG, "User authenticated, proceeding with profile selection.");
          client.setAuthToken(auth.getAuthToken());
          chooseProfile();
        }
      }
      break;
    // Called after the user has authorized application access to the service.
    case ACTIVITY_GET_LOGIN:
      if (resultCode == RESULT_OK) {
        if (!auth.authResult(resultCode, data)) {
          // Auth token could not be retrieved.
        }
      }
      break;
    case ACTIVITY_ADD_RESULT:
      if (resultCode == RESULT_OK) {
        Bundle bundle = data.getExtras();
        Result result = (Result) bundle.get(RESULT_PROPERTY);

        showDialog(DIALOG_PROGRESS);
        new CreateResultThread(result).start();
      }
      break;
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.results_menu, menu);
    return true;
  }

  /**
   * Called when a menu option is selected on main activity, which includes
   * creating new results, refreshing the list of results from Google Health
   * (i.e. retrieving results entered in Health directly while the app is
   * running), choose a profile, and choose an account.
   *
   * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.new_result:
      Intent i = new Intent(this, ResultAddActivity.class);
      startActivityForResult(i, ACTIVITY_ADD_RESULT);
      return true;

    case R.id.refresh_results:
      retrieveResults();
      return true;

    case R.id.choose_profile:
      chooseProfile();
      return true;

    case R.id.choose_account:
      chooseAccount();
      return true;

    default:
      return super.onOptionsItemSelected(item);
    }
  }

  /**
   * Retrieve a list of accounts stored in the phone and display a dialog
   * allowing the user to choose one.
   */
  protected void chooseAccount() {
    Log.d(LOG_TAG, "Selecting account.");
    AccountChooser accountChooser = new AccountChooser();
    accountChooser.chooseAccount(HealthAndroidExample.this, new AccountChooser.AccountHandler() {
      @Override
      public void handleAccountSelected(Account account) {
        Log.d(LOG_TAG, "Account selected.");
        // The user hit cancel
        if (account == null) {
          return;
        }

        authenticate(account);
      }
    });
  }

  /**
   * Once an account has been selected, use account credentials to get an
   * authorization token. If the account has already been authenticated, then
   * the existing token will be invalidated prior to re-authenticating.
   *
   * @param account
   *          The {@code Account} to authenticate with.
   */
  protected void authenticate(Account account) {
    Log.d(LOG_TAG, "Authenticating account.");
    if (this.account == account) {
      Log.d(LOG_TAG, "Invalidating token.");
      // If we're re-authenticating the same account, invalidate the old token
      // before proceeding.
      auth.invalidateAuthToken(new Runnable() {
        public void run() {
          Log.d(LOG_TAG, "Token invalidated.");
        }
      });
    }

    this.account = account;

    auth.doLogin(new Runnable() {
      public void run() {
        Log.d(LOG_TAG, "User authenticated.");
        onActivityResult(ACTIVITY_AUTHENTICATE, RESULT_OK, null);
      }
    }, account);
  }

  /**
   * Retrieve a list of profiles from Health and display a dialog allowing the
   * user to select one.
   */
  protected void chooseProfile() {
    // If the user hasn't selected an account (i.e. they canceled the initial
    // account dialog), have them do so.
    if (account == null) {
      chooseAccount();
      return;
    }

    showDialog(DIALOG_PROGRESS);

    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          Log.d(LOG_TAG, "Retreiving profiles.");
          profiles = client.retrieveProfiles();
          handler.post(new Runnable() {
            public void run() {
              Log.d(LOG_TAG, "Profiles retrieved.");
              dismissDialog(DIALOG_PROGRESS);
              showDialog(DIALOG_PROFILES);
            }
          });
        } catch (AuthenticationException e) {
          Log.w(LOG_TAG, "User authentication failed. Re-authenticating.");
          handler.post(new AuthenticateRunnable());
        } catch (InvalidProfileException e) {
          Log.w(LOG_TAG, "Profile invalid. Re-retreiving profiles.");
          handler.post(new ChooseProfileRunnable());
          return;
        } catch (ServiceException e) {
          Log.e(LOG_TAG, "Error connecting to Health service: code=" + e.getCode() + ", message="
              + e.getMessage() + ", content=" + e.getContent());
          return;
        }
      }
    }).start();
  }

  /**
   * Retrieve a list of test results from Health.
   */
  protected void retrieveResults() {
    showDialog(DIALOG_PROGRESS);

    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          Log.d(LOG_TAG, "Retreiving results.");
          results = client.retrieveResults();
          handler.post(new Runnable() {
            public void run() {
              Log.d(LOG_TAG, "Results retrieved.");
              dismissDialog(DIALOG_PROGRESS);
              displayResults();
            }
          });
        } catch (AuthenticationException e) {
          Log.w(LOG_TAG, "User authentication failed. Re-authenticating.");
          handler.post(new AuthenticateRunnable());
        } catch (InvalidProfileException e) {
          Log.w(LOG_TAG, "Profile invalid. Re-retreiving profiles.");
          handler.post(new ChooseProfileRunnable());
          return;
        } catch (ServiceException e) {
          Log.e(LOG_TAG, "Error connecting to Health service: code=" + e.getCode() + ", message="
              + e.getMessage() + ", content=" + e.getContent());
          return;
        }
      }}).start();
  }

  /**
   * Display results in the ListView.
   */
  protected void displayResults() {
    Log.d(LOG_TAG, "Displaying results.");
    // Collect the Tests from the Results and order them chronologically.
    Set<Test> tests = new TreeSet<Test>();
    for (Result result : results) {
      tests.addAll(result.getTests());

      // If the Test is missing it's date, then assign the Result date.
      for (Test test : result.getTests()) {
        if (test.getDate() == null) {
          test.setDate(result.getDate());
        }
      }
    }

    // Update the text view of the main activity with the list of test results.
    Test[] items = tests.toArray(new Test[tests.size()]);
    setListAdapter(new ArrayAdapter<Test>(this, R.layout.list_item, items));

    ListView lv = getListView();
    lv.setTextFilterEnabled(true);

    lv.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getApplicationContext(), ((TextView) view).getText(), Toast.LENGTH_SHORT)
            .show();
      }
    });

    openOptionsMenu();
  }

  /**
   * Runnable for (re)authenticating a user on the UI thread using a Handler.
   */
  protected class AuthenticateRunnable implements Runnable {
    @Override
    public void run() {
      authenticate(account);
    }
  }

  /**
   * Runnable for choosing Health profiles on the UI thread using a Handler.
   */
  protected class ChooseProfileRunnable implements Runnable {
    @Override
    public void run() {
      chooseProfile();
    }
  }

  /**
   * Threads for creating results, required since the result is a method
   * variable, and not a class viariable that can be accessed in an anonymous
   * inner class.
   */
  protected class CreateResultThread extends Thread {
    Result result;

    public CreateResultThread(Result result) {
      this.result = result;
    }

    @Override
    public void run() {
      try {
        Log.d(LOG_TAG, "Creating result.");
        client.createResult(result);
        handler.post(new Runnable() {
          public void run() {
            Log.d(LOG_TAG, "Result created.");
            retrieveResults();
          }
        });
      } catch (AuthenticationException e) {
        Log.w(LOG_TAG, "User authentication failed. Re-authenticating.");
        handler.post(new AuthenticateRunnable());
      } catch (InvalidProfileException e) {
        Log.w(LOG_TAG, "Profile invalid. Re-retreiving profiles.");
        handler.post(new ChooseProfileRunnable());
        return;
      } catch (ServiceException e) {
        Log.e(LOG_TAG, "Error connecting to Health service: code=" + e.getCode() + ", message="
            + e.getMessage() + ", content=" + e.getContent());
        return;
      }
    }
  }
}
