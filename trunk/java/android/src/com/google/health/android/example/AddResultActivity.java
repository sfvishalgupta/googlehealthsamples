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

import java.util.Calendar;
import java.util.TreeMap;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.health.android.example.gdata.Result;
import com.google.health.android.example.gdata.Test;

public class AddResultActivity extends Activity {

  private static final TreeMap<String, String> RESULTS = new TreeMap<String, String>();

  private static final int DATE_DIALOG = 0;

  private ArrayAdapter<String> typeAdapter;
  private Spinner typeSpinner;
  private EditText valueText;
  private TextView unitsLabel;
  private EditText dateText;

  private DatePickerDialog.OnDateSetListener dateSetListener;

  private int day;
  private int month;
  private int year;

  // TODO Load template results instead of map
  static {
    RESULTS.put("Blood glucose", "mg/dL");
    RESULTS.put("Blood pressure", "mmHg");
    RESULTS.put("Body temperature", "degrees Fahrenheit");
    RESULTS.put("Breathing", "breaths/min");
    RESULTS.put("Calories burned", "calories");
    RESULTS.put("Calories consumed", "calories");
    RESULTS.put("Cycling distance", "miles");
    RESULTS.put("Cycling time", "minutes");
    RESULTS.put("Exercise minutes", "minutes");
    RESULTS.put("Heart rate", "bpm");
    RESULTS.put("Height", "in");
    RESULTS.put("Hours slept", "hours");
    RESULTS.put("Peak flow", "liters/sec");
    RESULTS.put("Running distance", "miles");
    RESULTS.put("Running time", "minutes");
    RESULTS.put("Steps taken", "steps");
    RESULTS.put("Swimming distance", "meters");
    RESULTS.put("Swimming time", "minutes");
    RESULTS.put("Vegetable servings", "servings");
    RESULTS.put("Walking distance", "miles");
    RESULTS.put("Walking time", "minutes");
    RESULTS.put("Weight", "lb");
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.add_result_view);

    typeAdapter = new ArrayAdapter<String>(this, R.layout.add_result_type_spinner);
    typeAdapter.setDropDownViewResource(R.layout.add_result_type_spinner_item);
    for (String key : RESULTS.keySet()) {
      typeAdapter.add(key);
    }

    typeSpinner = (Spinner) findViewById(R.id.add_result_type_spinner);
    typeSpinner.setAdapter(typeAdapter);
    typeSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String resultName = typeAdapter.getItem(position);
        unitsLabel.setText(RESULTS.get(resultName));
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    valueText = (EditText) findViewById(R.id.add_result_value);

    unitsLabel = (TextView) findViewById(R.id.add_result_units);
    unitsLabel.setText(RESULTS.get(RESULTS.firstKey()));

    Calendar cal = Calendar.getInstance();
    year = cal.get(Calendar.YEAR);
    month = cal.get(Calendar.MONTH);
    day = cal.get(Calendar.DAY_OF_MONTH);

    dateText = (EditText) findViewById(R.id.add_result_date);
    dateText.setText(year + "-" + (month + 1) + "-" + day);
    dateText.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        showDialog(DATE_DIALOG);
      }
    });

    dateSetListener = new DatePickerDialog.OnDateSetListener() {
      public void onDateSet(DatePicker view, int pickedYear, int pickedMonth, int pickedDay) {
        year = pickedYear;
        month = pickedMonth;
        day = pickedDay;

        dateText.setText(year + "-" + (month + 1) + "-" + day);
      }
    };

    Button button = (Button) findViewById(R.id.add_result_date_button);
    button.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        showDialog(DATE_DIALOG);
      }
    });

    button = (Button) findViewById(R.id.add_result_save_button);
    button.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        String resultName = typeAdapter.getItem(typeSpinner.getSelectedItemPosition());

        Test test = new Test();
        test.setName(resultName);
        test.setValue(valueText.getText().toString());
        test.setUnits(RESULTS.get(resultName));

        test.setDate(year + "-" + (month + 1) + "-" + day);

        Result result = new Result();
        result.addTest(test);

        Bundle bundle = new Bundle();
        bundle.putSerializable(HealthAndroidExample.RESULT_PROPERTY, result);

        Intent intent = new Intent();
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);

        // Force close the keyboard. Otherwise, is will stay open since the main
        // class opens a progress dialog before onActivityResult finishes.
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(valueText.getWindowToken(), 0);

        finish();
      }
    });

    button = (Button) findViewById(R.id.add_result_cancel_button);
    button.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        setResult(RESULT_CANCELED);
        finish();
      }
    });
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    switch (id) {
    case DATE_DIALOG:
      return new DatePickerDialog(this, dateSetListener, year, month, day);
    }
    return null;
  }
}
