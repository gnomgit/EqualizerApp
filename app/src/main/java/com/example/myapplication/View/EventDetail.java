package com.example.myapplication.View;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.myapplication.Controller.EventServices;
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
import java.util.LinkedHashMap;

public class EventDetail extends AppCompatActivity {

    Person person;
    ArrayList<Person> contacts;
    LinkedHashMap<Long, Boolean> selected;
    Event event;
    boolean notOwner;

    EditText name;
    EditText description;
    EditText date;

    Intent me;
    ObjectMapper mapper;
    Object mThis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        init ();
    }

    private void init() {

        mThis = this;
        me = getIntent();
        String personJson = me.getStringExtra("person");
        String eventJson = me.getStringExtra("event");
        selected = new LinkedHashMap<>();

        mapper = new ObjectMapper();
        try {
            person = mapper.readValue(personJson, Person.class);
            event = mapper.readValue(eventJson, Event.class);
        }   catch (IOException e) {
            e.printStackTrace();
        }

        if (person.id != event.owner) {
            notOwner = true;
        }

        name = findViewById(R.id.detailedEventNameEditText);
        description = findViewById(R.id.detailedEventDescriptionEditText);
        date = findViewById(R.id.detailedEventDateEditText);

        name.setText(event.name);
        description.setText(event.description);
        date.setText(event.date);

        if (notOwner) {
            name.setFocusable(false);
            description.setFocusable(false);
            date.setFocusable(false);
        }
    }

    public void updateEvent () throws JsonProcessingException, JSONException {
        EventServices eventServices = new EventServices();

        eventServices.modifyActivity(this, "PersonOut",
                new JSONObject(mapper.writeValueAsString(event)),
                new VolleyCallback() {
                    @Override
                    public void onSuccessResponse(String response) {


                        try {
                            Event updatedEvent = mapper.readValue(response, Event.class);

                            if (updatedEvent != null) {
                                if (updatedEvent.error == null) {
                                    event = updatedEvent;
                                    me.putExtra("event", mapper.writeValueAsString(event));
                                }   else {
                                    Toast.makeText(getApplicationContext(), updatedEvent.error.description, Toast.LENGTH_SHORT).show();
                                }
                            }   else {
                                Toast.makeText(getApplicationContext(), "null Event returned", Toast.LENGTH_SHORT).show();
                            }

                        }   catch (IOException e) {
                            e.printStackTrace();
                        }

                        finishWithOk();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        });
    }

    public void onOkClick (View view) {
        //event = new Event();
        if (!notOwner) {
            event.name = name.getText().toString();
            event.description = description.getText().toString();
            event.date = date.getText().toString();
            try {
                Log.i("Event", mapper.writeValueAsString(event));
                updateEvent();
            }   catch (JsonProcessingException e) {
                e.printStackTrace();
                return;
            }   catch (JSONException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public void finishWithOk () {
        setResult(RESULT_OK, me);
        finish();
    }

    public void onCancelClick (View view) {
        setResult(RESULT_CANCELED, me);
        finish();
    }

}
