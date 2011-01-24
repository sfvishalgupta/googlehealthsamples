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
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.google.health.android.example.auth.AccountChooser;
import com.google.health.android.example.auth.AuthManager;
import com.google.health.android.example.gdata.HealthClient;
import com.google.health.android.example.gdata.GDataHealthClient;
import com.google.health.android.example.gdata.Result;
import com.google.health.android.example.gdata.Test;
import com.google.health.android.example.gdata.HealthClient.AuthenticationException;
import com.google.health.android.example.gdata.HealthClient.InvalidProfileException;
import com.google.health.android.example.gdata.HealthClient.ServiceException;

public final class HealthAndroidExample extends Activity {
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
  private static final int DIALOG_TERMS = 3;

  private static final String PREF_HEALTH_NOTE = "read_note";

  /** Property key for returning a result from a child activity. */
  public static final String RESULT_PROPERTY = "result";

  public static final String ACCOUNT_TYPE = "com.google";

  /** Service client for send to and retrieving information from Google Health. */
  private final HealthClient client = new GDataHealthClient(SERVICE_NAME);

  private Map<String, String> profiles = new LinkedHashMap<String, String>();

  private List<Result> results;

  private AuthManager auth;
  private Account account;

  @SuppressWarnings("unchecked")
  private AsyncTask currentTask;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_view);

    // Configure the buttons
    Button button = (Button) findViewById(R.id.main_accounts);
    button.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        chooseAccount();
      }
    });

    button = (Button) findViewById(R.id.main_profiles);
    button.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        chooseProfile();
      }
    });

    button = (Button) findViewById(R.id.main_new_result);
    button.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        Intent i = new Intent(HealthAndroidExample.this, AddResultActivity.class);
        startActivityForResult(i, ACTIVITY_ADD_RESULT);
      }
    });

    button = (Button) findViewById(R.id.main_refresh);
    button.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        retrieveResults();
      }
    });

    auth = new AuthManager(this, SERVICE_NAME);

    // If this is the first use, display the requisite Health notice.
    if (!getPreferences(Context.MODE_PRIVATE).getBoolean(PREF_HEALTH_NOTE, false)) {
      showDialog(DIALOG_TERMS);
    } else {
      chooseAccount();
    }
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    Dialog dialog;
    AlertDialog.Builder builder;

    switch (id) {
    case DIALOG_TERMS:
      final SpannableString msg = new SpannableString(this.getString(R.string.health_notice));
      Linkify.addLinks(msg, Linkify.WEB_URLS);
      // TODO Make links click-able

      builder = new AlertDialog.Builder(this);
      builder.setTitle("Please note:");
      builder.setMessage(msg);
      builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          // Store that the user has read the note.
          Editor e = getPreferences(Context.MODE_PRIVATE).edit();
          e.putBoolean(PREF_HEALTH_NOTE, true);
          e.commit();

          chooseAccount();
        }
      });

      dialog = builder.create();
      break;

    case DIALOG_PROGRESS:
      dialog = ProgressDialog.show(HealthAndroidExample.this, "", this.getString(R.string.loading),
          true);
      dialog.setCancelable(true);
      dialog.setOnCancelListener(new OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
          currentTask.cancel(true);
        }
      });
      break;

    case DIALOG_PROFILES:
      String[] profileNames = profiles.values().toArray(new String[profiles.size()]);

      builder = new AlertDialog.Builder(this);
      builder.setTitle(this.getText(R.string.choose_profile));
      builder.setItems(profileNames, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int i) {
          // Remove the dialog so that it's refreshed with new list items the
          // next time it's displayed since onPrepareDialog cannot change the dialog's
          // list items.
          removeDialog(DIALOG_PROFILES);

          String pid = profiles.keySet().toArray(new String[profiles.size()])[i];
          client.setProfileId(pid);

          Button button = (Button) findViewById(R.id.main_profiles);
          button.setText(profiles.get(pid));

          retrieveResults();
        }
      });

      dialog = builder.create();
      break;

    case DIALOG_ERROR:
      builder = new AlertDialog.Builder(this);
      builder.setTitle(this.getText(R.string.connection_error_title));
      builder.setMessage(R.string.connection_error_message);
      builder.setCancelable(true);
      builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
        }
      });

      dialog = builder.create();
      break;

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

          Button button = (Button) findViewById(R.id.main_accounts);
          button.setText(account.name);

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
        currentTask = new CreateResultTask().execute(result);
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
    case R.id.menu_new_result:
      Intent i = new Intent(this, AddResultActivity.class);
      startActivityForResult(i, ACTIVITY_ADD_RESULT);
      return true;

    case R.id.menu_refresh_results:
      retrieveResults();
      return true;

    case R.id.menu_choose_profile:
      chooseProfile();
      return true;

    case R.id.menu_choose_account:
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
    currentTask = new RetrieveProfilesTask().execute();
  }

  /**
   * Retrieve a list of test results from Health.
   */
  protected void retrieveResults() {
    showDialog(DIALOG_PROGRESS);
    currentTask = new RetrieveResultsTask().execute();
  }

  /**
   * Display results in the main activity's test result list.
   */
  protected void displayResults() {
    Log.d(LOG_TAG, "Displaying results.");
    // Collect the Tests from the Results and order them chronologically.
    Set<Test> tests = new TreeSet<Test>();
    for (Result result : results) {
      // If the Test is missing it's date, then assign the Result date.
      for (Test test : result.getTests()) {
        if (test.getDate() == null) {
          test.setDate(result.getDate());
        }
      }
      tests.addAll(result.getTests());
    }
    Test[] items = tests.toArray(new Test[tests.size()]);

    // Update the text view of the main activity with the list of test results.
    ListView lv = (ListView) findViewById(R.id.main_results);
    lv.setAdapter(new ArrayAdapter<Test>(this, R.layout.results_list_item, items));

    lv.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getApplicationContext(), ((TextView) view).getText(), Toast.LENGTH_SHORT)
            .show();
      }
    });

    // Display a notice if not results found.
    if (items.length == 0) {
      Toast.makeText(getApplicationContext(), "No test results in profile.", Toast.LENGTH_LONG).show();
    }
  }

  /**
   * Method processes network connectivity exceptions, which will
   * re-authenticate the user, re-request a Health profile, or request that the
   * user check the network connection.
   *
   * @param e
   *          The network connectivity exception to process, which can be a
   *          AuthenticationException, InvalidProfileException, or
   *          ServiceException.
   */
  protected void handleException(Exception e) {
    if (e instanceof AuthenticationException) {
      Log.w(LOG_TAG, "User authentication failed. Re-authenticating.");
      authenticate(account);
    } else if (e instanceof InvalidProfileException) {
      Log.w(LOG_TAG, "Profile invalid. Re-retrieving profiles.");
      chooseProfile();
    } else if (e instanceof ServiceException) {
      if (e.getCause() != null) {
        // Likely no network connectivity.
        Log.e(LOG_TAG, "Error connecting to Health service.", e);
      } else {
        ServiceException se = (ServiceException) e;
        Log.e(LOG_TAG, "Error connecting to Health service: code=" + se.getCode() + ", message="
            + e.getMessage() + ", content=" + se.getContent());
      }

      // Remove the progress dialog and display the error.
      dismissDialog(DIALOG_PROGRESS);
      showDialog(DIALOG_ERROR);
    }
  }

  protected class CreateResultTask extends AsyncTask<Result, Void, Void> {
    private Exception exception;

    @Override
    protected Void doInBackground(Result... results) {
      Log.d(LOG_TAG, "Creating result.");
      try {
        client.createResult(results[0]);
      } catch (Exception e) {
        exception = e;
      }
      return null;
    }

    protected void onPostExecute(Void result) {
      if (exception != null) {
        handleException(exception);
        return;
      }

      Log.d(LOG_TAG, "Result created.");
      retrieveResults();
    }
  }

  protected class RetrieveProfilesTask extends AsyncTask<Void, Void, Void> {
    private Exception exception;

    @Override
    protected Void doInBackground(Void... params) {
      Log.d(LOG_TAG, "Retreiving profiles.");
      try {
        profiles = client.retrieveProfiles();
      } catch (Exception e) {
        exception = e;
      }
      return null;
    }

    protected void onPostExecute(Void result) {
      if (exception != null) {
        handleException(exception);
        return;
      }

      Log.d(LOG_TAG, "Profiles retrieved.");
      dismissDialog(DIALOG_PROGRESS);
      showDialog(DIALOG_PROFILES);
    }
  }

  protected class RetrieveResultsTask extends AsyncTask<Void, Void, Void> {
    private Exception exception;

    @Override
    protected Void doInBackground(Void... params) {
      try {
        Log.d(LOG_TAG, "Retreiving results.");
        results = client.retrieveResults();
      } catch (Exception e) {
        exception = e;
      }
      return null;
    }

    protected void onPostExecute(Void results) {
      if (exception != null) {
        handleException(exception);
        return;
      }

      Log.d(LOG_TAG, "Results retrieved.");
      dismissDialog(DIALOG_PROGRESS);
      displayResults();
    }
  }
}
