package com.example.luira.dia;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private  EditText input;
    private RecyclerView mMessageList;
    Adapter adapter;
    String text,user,time;
    String passDialog;
    List<ChatMessage> m;
    final Context c = this;
    String encryptedMsg = null;
    EditText userInputDialogEditText;
    private static int SIGN_IN_REQUEST_CODE = 1;
    FloatingActionButton fab;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_sign_out)
        {
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(MainActivity.this, "You have been signed out.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
        if(item.getItemId() == R.id.menu_password){
            LayoutInflater layoutInflaterAndroid = LayoutInflater.from(c);
            View mView = layoutInflaterAndroid.inflate(R.layout.user_input_dialog, null);
            AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(c);
            alertDialogBuilderUserInput.setView(mView);

            userInputDialogEditText = mView.findViewById(R.id.userInputDialog);
            alertDialogBuilderUserInput
                    .setCancelable(false)
                    .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                        public static final String TAG ="" ;

                        public void onClick(DialogInterface dialogBox, int id) {
                            // ToDo get user input here
                            passDialog = userInputDialogEditText.getText().toString();


                        }
                    })

                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogBox, int id) {
                                    dialogBox.cancel();
                                }
                            });
            AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
            alertDialogAndroid.show();
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SIGN_IN_REQUEST_CODE){


        if (resultCode == RESULT_OK){

            Toast.makeText(this, "Successfully signed in. Welcome!", Toast.LENGTH_SHORT).show();
            //displayChatMessage();
        }
        else {
            Toast.makeText(this, "We couldn't signed you in. Please try again later", Toast.LENGTH_SHORT).show();
            finish();
        }
    }}


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_main);
        input = findViewById(R.id.input);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        m = new ArrayList<>();
        adapter = new Adapter(m);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mMessageList = findViewById(R.id.messageRec);
        mMessageList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mMessageList.setLayoutManager(linearLayoutManager);
        adapter.notifyDataSetChanged();
        mMessageList.setAdapter(adapter);



        if (FirebaseAuth.getInstance().getCurrentUser() == null) {

            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_REQUEST_CODE);
        } else {
            Toast.makeText(this, "Welcome " + FirebaseAuth.getInstance().getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
        }


        fab = (FloatingActionButton) findViewById(R.id.fab);
        input.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.toString().trim().length()==0){
                    fab.setEnabled(false);
                } else {
                    fab.setEnabled(true);
                }


            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            public static final String TAG = "" ;

            @Override
            public void onClick(View v) {
                final String messageValue = input.getText().toString().trim();
                Date curDate = new Date();
                SimpleDateFormat format = new SimpleDateFormat("hh:mm aa");
                String DateToStr = format.format(curDate);
//                String password = "password";



                if(passDialog == null){
                    Toast.makeText(MainActivity.this, "Debe introducir una contraseña para poder acceder!", Toast.LENGTH_LONG).show();

                } else {

                try {
                     encryptedMsg = AESCrypt.encrypt(passDialog, messageValue);
                }catch (GeneralSecurityException e){
                    //handle error
                }
                if (!TextUtils.isEmpty(messageValue)) {

                    final DatabaseReference newPost = mDatabase.child("Messages").push();
                    newPost.child("messageText").setValue(encryptedMsg);
                    Log.d(TAG, encryptedMsg);
                    newPost.child("messageUser").setValue(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                    newPost.child("messageTime").setValue(DateToStr);
                }
                m.clear();
                adapter.notifyDataSetChanged();


                input.setText("");
                input.requestFocus();
            }}
        });
        Log.d("prueba", "onStart: ");

        mDatabase.child("Messages").addValueEventListener(new ValueEventListener() {
            public static final String TAG = "" ;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                m.clear();
                String messageAfterDecrypt = null;
                //String password = "password";


                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    

                    try{
                       text = snapshot.child("messageText").getValue().toString();
                       user  = snapshot.child("messageUser").getValue().toString();
                       time = snapshot.child("messageTime").getValue().toString();
                       messageAfterDecrypt = AESCrypt.decrypt(passDialog, text);
                        Log.d("MENSAJE DECRYPTED", "onDataChange: "+ messageAfterDecrypt);
                    }catch (Exception e){
                        Log.d("TAG", e.toString());
                    }


                    Log.d("PRUEBA","Retrieve");
                    m.add(new ChatMessage(messageAfterDecrypt,user,time));
                    adapter.notifyDataSetChanged();

                }}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });


//        Query query = FirebaseDatabase.getInstance()
//                .getReference()
//                .child("Messages");
//
//        FirebaseRecyclerOptions<ChatMessage> options =
//                new FirebaseRecyclerOptions.Builder<ChatMessage>()
//                        .setQuery(query, ChatMessage.class)
//                        .build();
//
//        Adapter adapter = new Adapter(options);
//        mMessageList.setAdapter(adapter);


    }






//    private void displayChatMessage() {
//        Query query=FirebaseDatabase.getInstance().getReference().child("chats");
//
//        ListView listOfMessage = (ListView) findViewById(R.id.list_of_message);
//        FirebaseListOptions<ChatMessage> options =
//                new FirebaseListOptions.Builder<ChatMessage>()
//                        .setQuery(query, ChatMessage.class)
//                        .setLayout(R.layout.list_item)
//                        .build();
//        adapter = new FirebaseListAdapter<ChatMessage>(options){
//        @Override
//            protected void populateView (View v, ChatMessage model, int position) {
//                TextView messageText, messageUser, messageTime;
//                messageText = (TextView) v.findViewById(R.id.message_text);
//                messageUser = (TextView) v.findViewById(R.id.message_user);
//                messageTime = (TextView) v.findViewById(R.id.message_time);
//
//                messageText.setText(model.getMessageText());
//            Log.d("TEXT", "populateView: "+ messageText.getText().toString());
//                messageUser.setText(model.getMessageUser());
//                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getMessageTime()));
//
//            }
//        };
//        listOfMessage.setAdapter(adapter);
//    }

//    public void signup(View v){
//        Intent i = new Intent(getApplicationContext(), Register.class);
//        startActivity(i);
//
//    }
//
//    public void login (View v){
//        Intent i = new Intent(getApplicationContext(),loginActivity.class);
//        startActivity(i);
//    }
}
