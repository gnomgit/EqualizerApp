package com.example.myapplication.View;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.myapplication.Controller.EventServices;
import com.example.myapplication.Controller.PersonServices;
import com.example.myapplication.Controller.VolleyCallback;
import com.example.myapplication.Model.Event;
import com.example.myapplication.Model.Person;
import com.example.myapplication.R;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;

public class EventAdd extends AppCompatActivity {
    Person person;
    String personJson;
    ArrayList<Person> persons;
    LinkedHashMap<Long, Boolean> selected;
    Event newEvent;

    ListView participantsListView;
    ParticipantsListAdapter participantsListAdapter;
    EditText name;
    EditText description;
    static EditText date;
    static EditText time;

    Intent me;
    ObjectMapper mapper;
    Object mThis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_add);
        Toolbar toolbar = (Toolbar) findViewById(R.id.eventAddToolbar);
        toolbar.setTitle("Add Event");
        setSupportActionBar(toolbar);

        persons = new ArrayList<>();
        selected = new LinkedHashMap<>();
        me = getIntent();
        personJson = me.getStringExtra("person");
        if (personJson == null) finish();
        mapper = new ObjectMapper();
        try {
            person = mapper.readValue(personJson, Person.class);
        }   catch (IOException e) {
            e.printStackTrace();
        }
        if (person == null) finish();

        mThis = this;

        name = findViewById(R.id.addEventNameEditText);
        description = findViewById(R.id.addEventDescriptionEditText);
        date = findViewById(R.id.addEventDateEditText);
        time = findViewById(R.id.addEventTimeEditText);

        setParticipantsList(person.id);
    }

    public void onOkClick (View view) {
        newEvent = new Event();
        newEvent.owner = person.id;
        newEvent.participants.add(person.id);
        newEvent.name = name.getText().toString();
        newEvent.description = description.getText().toString();
        newEvent.date = date.getText().toString() + "T" + time.getText().toString();
        for (Person p : persons) {
            if (selected.get(p.id)) {
                newEvent.participants.add(p.id);
            }
        }
        try {
            Log.i("newEvent", mapper.writeValueAsString(newEvent));
            addEvent();
        }   catch (JsonProcessingException e) {
            e.printStackTrace();
            return;
        }   catch (JSONException e) {
            e.printStackTrace();
        }
        setResult(RESULT_OK, me);
        finish();
    }

    public void onCancelClick (View view) {
        setResult(RESULT_CANCELED, me);
        finish();
    }

    public void setListView () {
        participantsListView = findViewById(R.id.addEventParticipantsListView);
        participantsListAdapter = new ParticipantsListAdapter((Context) mThis, 0, persons);
        participantsListView.setAdapter(participantsListAdapter);
    }

    public void setParticipantsList (final long pId) {
        PersonServices personServices = new PersonServices();

        personServices.contacts(this, pId,
                new VolleyCallback() {
                    @Override
                    public void onSuccessResponse(String response) {
                        try {
                            persons = mapper.readValue(response, mapper.getTypeFactory().constructCollectionType(ArrayList.class, Person.class));
                            if (persons != null) {
                                for (int i = 0; i < persons.size(); i++) {
                                    selected.put(persons.get(i).id, false);
                                }

                                setListView();

                            }
                        }   catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                }
        });
    }

    public void addEvent () throws JsonProcessingException, JSONException {
        EventServices eventServices = new EventServices();

        eventServices.addActivity(this, "EventOut",
                new JSONObject(mapper.writeValueAsString(newEvent)),
                new VolleyCallback() {
                    @Override
                    public void onSuccessResponse(String response) {

                        try {
                            Event createdEvent = mapper.readValue(response, Event.class);

                            if (createdEvent != null) {
                                if (createdEvent.error == null) {
                                    me.putExtra("event", mapper.writeValueAsString(createdEvent));
                                }   else {
                                    Toast.makeText(getApplicationContext(), createdEvent.error.description, Toast.LENGTH_SHORT).show();
                                }
                            }   else {
                                Toast.makeText(getApplicationContext(), "null Event returned", Toast.LENGTH_SHORT).show();
                            }

                        }   catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT);
                    }
                });
    }

    public class ParticipantsListAdapter extends ArrayAdapter<Person> {

        private ArrayList<Person> participants;
        private Context context;
        private LayoutInflater layOutInflater;
        View.OnClickListener listener;

        public ParticipantsListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Person> participants) {
            super(context, resource, participants);
            this.participants = participants;
            selected = new LinkedHashMap<>();
            for (Person p : participants) {
                selected.put(p.id, false);
            }
            this.context = context;
            this.layOutInflater = LayoutInflater.from(EventAdd.this);

            listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CheckBox checkBox = (CheckBox) view;
                    selected.put((long)checkBox.getTag(), checkBox.isChecked());
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            };

        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            Person p = getItem(position);
            if (view == null) {
                view = layOutInflater.inflate(R.layout.participants_layout, null);
                CheckBox checkBox = view.findViewById(R.id.participantCheckBox);
                checkBox.setText(p.lastName);
                checkBox.setTag(p.id);
                checkBox.setOnClickListener(listener);
                checkBox.setChecked(selected.get(persons.get(position).id));
            }   else {
                view.setSelected(selected.get(p.id));
            }

            return view;
        }

    }

    public static String formatFill (int value, int total) {
        String result = String.valueOf(value);
        for (int i = result.length(); i < total; i++) {
            result = "0" + result;
        }
        return result;
    }

    public static class TimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        @Override
        public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {
            Log.i("time", "Selected Time: " + String.valueOf(hourOfDay) + " : " + String.valueOf(minute));
            time.setText(formatFill(hourOfDay, 2) + ":" + formatFill(minute, 2) + ":00");
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
        }
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }
        public void onDateSet(DatePicker view, int year, int month, int day) {
            Log.i("time", "Selected Time: " + String.valueOf(year) + ":" + formatFill(month, 2) + ":" + formatFill(day, 2));
            date.setText(String.valueOf(year) + "-" + formatFill(month + 1, 2) + "-" + formatFill(day, 2));
        }
    }

    public void onTimeClick(View v) {
        TimePicker mTimePicker = new TimePicker();
        mTimePicker.show(getFragmentManager(), "Select time");
    }

    public void onDateClick(View v) {
        DatePickerFragment mDatePicker = new DatePickerFragment();
        mDatePicker.show(getFragmentManager(), "Select date");
    }


}
