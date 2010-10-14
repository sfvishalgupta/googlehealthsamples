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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.health.android.example.gdata.Result;
import com.google.health.android.example.gdata.Test;

public class ResultAddActivity extends Activity {
  
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
  
  static {
    RESULTS.put("Blood pressure", "mmHg");
    RESULTS.put("Height", "inches");
    RESULTS.put("Weight", "lb");
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.result_add_view);
    
    typeAdapter = new ArrayAdapter<String>(this, R.layout.result_type_spinner_item);
    typeAdapter.setDropDownViewResource(R.layout.result_type_dropdown_item);
    for (String key : RESULTS.keySet()) {
      typeAdapter.add(key);
    }
    
    typeSpinner = (Spinner) findViewById(R.id.resultTypeSpinner);
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
    
    valueText = (EditText) findViewById(R.id.resultValueText);
    
    unitsLabel = (TextView) findViewById(R.id.resultUnitsLabel);
    unitsLabel.setText(RESULTS.get(RESULTS.firstKey()));
    
    Calendar cal = Calendar.getInstance();
    year = cal.get(Calendar.YEAR);
    month = cal.get(Calendar.MONTH);
    day = cal.get(Calendar.DAY_OF_MONTH);
    
    dateText = (EditText) findViewById(R.id.resultDateText);
    dateText.setText(year + "-" + month + "-" + day);
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
        
        dateText.setText(year + "-" + month + "-" + day);
      }
    };
    
    Button button = (Button) findViewById(R.id.resultDateButton);
    button.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        showDialog(DATE_DIALOG);
      }
    });
    
    button = (Button) findViewById(R.id.resultSaveButton);
    button.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        String resultName = typeAdapter.getItem(typeSpinner.getSelectedItemPosition());
        
        Test test = new Test();
        test.setName(resultName);
        test.setValue(valueText.getText().toString());
        test.setUnits(RESULTS.get(resultName));
        
        test.setDate(year + "-" + month + "-" + day);
        
        Result result = new Result();
        result.addTest(test);
        
        Bundle bundle = new Bundle();
        bundle.putSerializable(HealthAndroidExample.RESULT_PROPERTY, result);
        
        Intent intent = new Intent();
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
      }
    });
    
    button = (Button) findViewById(R.id.resultCancelButton);
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
