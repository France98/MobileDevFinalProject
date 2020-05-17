package com.example.afinal;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

public class SignIn extends AppCompatActivity {

    private CalendarView calendarView;
    private Button signout, addButton;
    private FirebaseAuth mAuth;
    private ListView list;
    private String selectedDate = "";
    private TextView title;
    ArrayAdapter<String> adapter;

    FirebaseDatabase database;
    private EditText textEdit;
    ArrayList<String> arraylist = new ArrayList<>();

    public void hideKeyBoard(){
        View view1 = this.getCurrentFocus();
        if(view1!= null){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        signout = (Button) findViewById(R.id.signout);
        addButton = (Button) findViewById(R.id.save);
        calendarView = (CalendarView) findViewById(R.id.calendarView);
        list = (ListView) findViewById(R.id.list);
        textEdit = (EditText) findViewById(R.id.savetext);
        title = (TextView) findViewById(R.id.date);

        findViewById(R.id.page2).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                return true;
            }
        });


        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arraylist);
        list.setAdapter(adapter);


        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();


        final FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            // Name, email address, and profile photo Url
            String email = user.getEmail();

        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference().child("User").child(user.getUid()).child("message");

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                hideKeyBoard();
                selectedDate = dayOfMonth + "/" + (month+1) + "/" + year;
                Log.d("TAG", "onSelectedDayChange: dd/mm/yyyy:" + selectedDate);
                title.setText(selectedDate);
            }

        });

        textEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyBoard();
                }
            }
        });

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
//                Day data = dataSnapshot.getValue(Day.class);
                Iterable<DataSnapshot> snapshotIterator = dataSnapshot.getChildren();
                Iterator<DataSnapshot> iterator = snapshotIterator.iterator();

                arraylist.clear();
                while (iterator.hasNext()) {
                    DataSnapshot next = (DataSnapshot) iterator.next();
                    String data =  next.child("message").getValue(String.class);
                    Log.i("TAG", "Value = " + next.child("name").getValue());
                    arraylist.add(data);
                }
                Collections.sort(arraylist, new Comparator<String>() {
                    DateFormat f = new SimpleDateFormat("dd/MM/yyyy");
                    @Override
                    public int compare(String o1, String o2) {
                        try {
                            return f.parse(o1).compareTo(f.parse(o2));
                        } catch (ParseException e) {
                            throw new IllegalArgumentException(e);
                        }
                    }
                });
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException());
            }
        });


        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Write a message to the database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference ref = database.getReference("User");

                String text = textEdit.getText().toString();
                String note = selectedDate + ": " + text;

                Day data = new Day();
                data.setMessage(note);
                ref.child(user.getUid()).child("message").push().setValue(data);
                adapter.notifyDataSetChanged();
                textEdit.getText().clear();
                hideKeyBoard();
            }
        });

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent i = new Intent(SignIn.this, MainActivity.class);
                finish();
                startActivity(i);
            }
        });


    }
}
