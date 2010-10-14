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

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import com.google.health.android.example.gdata.HealthGDataClient;
import com.google.health.android.example.gdata.Result;
import com.google.health.android.example.gdata.Test;
import com.google.health.android.example.gdata.HealthGDataClient.AuthenticationException;
import com.google.health.android.example.gdata.HealthGDataClient.InvalidProfileException;

/**
 * Known issues:
 * - Re-orientation causes profile dialog to display.
 */
public final class HealthAndroidExample extends ListActivity {

  /** Set this to 'weaver' to connect to H9, and 'health' to connect to Health. */
  private static final String SERVICE_NAME = "weaver";

  private static final String LOG_TAG = "AndroidHealthClient";

  private static final int ACTIVITY_AUTHENTICATE = 0;
  private static final int ACTIVITY_ADD_RESULT = 1;

  private static final String PREF = "MyPrefs";

  private static final int DIALOG_ACCOUNTS = 0;
  private static final int DIALOG_PROFILES = 1;
  
  private static final String ACCOUNT_PROPERTY = "account";
  private static final String PROFILE_PROPERTY = "profile";
  
  /** Property key for returning a result from a child activity. */
  public static final String RESULT_PROPERTY = "result";
  
  private String authToken;

  private HealthGDataClient client = new HealthGDataClient(SERVICE_NAME);
  
  private Map<String, String> profiles = new LinkedHashMap<String, String>();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    gotAccount(false);
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    Dialog dialog;
    AlertDialog.Builder builder = new AlertDialog.Builder(this);

    switch (id) {
    case DIALOG_ACCOUNTS:
      builder.setTitle("Select a Google account");
      
      final AccountManager manager = AccountManager.get(this);
      final Account[] accounts = manager.getAccountsByType("com.google");
      final int size = accounts.length;
      String[] names = new String[size];

      for (int i = 0; i < size; i++) {
        names[i] = accounts[i].name;
      }

      builder.setItems(names, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int i) {
          gotAccount(manager, accounts[i]);
        }
      });

      dialog = builder.create();
      break;

    case DIALOG_PROFILES:
      builder.setTitle("Select a Health profile");
      
      String[] profileNames = profiles.values().toArray(new String[profiles.size()]);

      builder.setItems(profileNames, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int i) {
          String pid = profiles.keySet().toArray(new String[profiles.size()])[i];
          client.setProfileId(pid);
          
          // Store the pid so that we don't have to re-ask for it.
          SharedPreferences settings = getSharedPreferences(PREF, 0);
          SharedPreferences.Editor editor = settings.edit();
          editor.putString(PROFILE_PROPERTY, pid);
          editor.commit();
          
          displayResults();
        }
      });

      dialog = builder.create();
      break;

    default:
      dialog = null;
    }

    return dialog;
  }
  
  private void gotAccount(boolean tokenExpired) {
    SharedPreferences settings = getSharedPreferences(PREF, 0);
    String accountName = settings.getString(ACCOUNT_PROPERTY, null);

    if (accountName != null) {
      AccountManager manager = AccountManager.get(this);
      Account[] accounts = manager.getAccountsByType("com.google");
      int size = accounts.length;
      for (int i = 0; i < size; i++) {
        Account account = accounts[i];
        if (accountName.equals(account.name)) {
          if (tokenExpired) {
            manager.invalidateAuthToken("com.google", authToken);
          }
          gotAccount(manager, account);
          return;
        }
      }
    }
    
    showDialog(DIALOG_ACCOUNTS);
  }

  private void gotAccount(AccountManager manager, Account account) {
    // Store the selected account so we don't have to ask for it again.
    SharedPreferences settings = getSharedPreferences(PREF, 0);
    SharedPreferences.Editor editor = settings.edit();
    editor.putString(ACCOUNT_PROPERTY, account.name);
    editor.commit();
    
    // Retrieve the auth token for the account.
    try {
      Bundle bundle = manager.getAuthToken(account, SERVICE_NAME, true, null, null).getResult();
      if (bundle.containsKey(AccountManager.KEY_INTENT)) {
        Intent intent = bundle.getParcelable(AccountManager.KEY_INTENT);
        int flags = intent.getFlags();
        flags &= ~Intent.FLAG_ACTIVITY_NEW_TASK;
        intent.setFlags(flags);
        startActivityForResult(intent, ACTIVITY_AUTHENTICATE);
      } else if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
        authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
        client.setAuthToken(authToken);
        displayProfiles();
      }
    } catch (Exception e) {
      handleException(e);
      return;
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    switch (requestCode) {
    case ACTIVITY_AUTHENTICATE:
      if (resultCode == RESULT_OK) {
        gotAccount(false);
      } else {
        showDialog(DIALOG_ACCOUNTS);
      }
      break;

    case ACTIVITY_ADD_RESULT:
      if (resultCode == RESULT_OK) {
        Bundle bundle = data.getExtras();
        Result result = (Result) bundle.get(RESULT_PROPERTY);

        // TODO Gracefully handle auth failure when result created.
        try {
          client.createResult(result);
        } catch (AuthenticationException e) {
          gotAccount(true);
          return;
        } catch (InvalidProfileException e) {
          gotAccount(true);
          return;
        }

        displayResults();
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

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.new_result:
      Intent i = new Intent(this, ResultAddActivity.class);
      startActivityForResult(i, ACTIVITY_ADD_RESULT);
      return true;
      
    case R.id.refresh_results:
      displayResults();
      return true;
      
    case R.id.choose_profile:
      gotAccount(false);
      return true;
      
    case R.id.choose_account:
      // Clear the stored account so the user is forced to select a new one.
      SharedPreferences settings = getSharedPreferences(PREF, 0);
      SharedPreferences.Editor editor = settings.edit();
      editor.remove(ACCOUNT_PROPERTY);
      editor.commit();
      
      gotAccount(false);
      return true;
      
    default:
      return super.onOptionsItemSelected(item);
    }
  }

  /**
   * Retrieve a list of profiles from Health and display a dialog allowing the
   * user to select one.
   */
  private void displayProfiles() {
    try {
      profiles = client.retrieveProfiles();
    } catch (AuthenticationException e) {
      gotAccount(true);
      return;
    } catch (InvalidProfileException e) {
      gotAccount(true);
      return;
    }
    
    showDialog(DIALOG_PROFILES);
  }

  /**
   * Retrieve a list of test results from Health and display them in a list.
   */
  private void displayResults() {
    List<Result> results;
    try {
      results = client.retrieveResults();
    } catch (AuthenticationException e) {
      gotAccount(true);
      return;
    } catch (InvalidProfileException e) {
      gotAccount(true);
      return;
    }

    // Collect the Tests from the Results and order them chronologically.
    Set<Test> tests = new TreeSet<Test>(new Comparator<Test>() {
      @Override
      public int compare(Test t1, Test t2) {
        // TODO Check for null dates and names
        int x = t1.getDate().compareTo(t2.getDate()); 
        
        if (x != 0) {
          return x; 
        }
        
        x = t1.getName().compareTo(t2.getName());
        
        if (x != 0) {
          return x;
        }
        
        return -1;
      }
    });
    
    for (Result result : results) {
      tests.addAll(result.getTests());

      // If the Test is missing it's date, then assign the Result date.
      for (Test test : result.getTests()) {
        if (test.getDate() == null) {
          test.setDate(result.getDate());
        }
      }
    }

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
  }

  private void handleException(Exception e) {
    e.printStackTrace();

    SharedPreferences settings = getSharedPreferences(PREF, 0);
    if (settings.getBoolean("logging", false)) {
      Log.e(LOG_TAG, e.getMessage(), e);
    }
  }
}
