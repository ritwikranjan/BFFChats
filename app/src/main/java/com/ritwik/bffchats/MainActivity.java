package com.ritwik.bffchats;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


public class MainActivity extends AppCompatActivity {

    static final int RC_SIGN_IN = 101;
    private static final String ANONYMOUS = "Anonymous";
    private static final String CHANNEL_ID = "default_channel_id";
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseRdeference;
    FirebaseAuth mFirebaseAuth;
    FirebaseAuth.AuthStateListener mFirebaseAuthStateListener;
    ChildEventListener mChildEventListener;
    MessageAdapter messageAdapter;
    Uri mPhotoUrl;
    String mUsername;
    TextInputLayout messageEditText;
    ListView messageListView;
    Button send;
    PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        {
            send = findViewById(R.id.sendButton);
            messageEditText = findViewById(R.id.textMessageInputLayout);
            mFirebaseDatabase = FirebaseDatabase.getInstance( );
            mDatabaseRdeference = mFirebaseDatabase.getReference( ).child("messages");
            mFirebaseAuth = FirebaseAuth.getInstance( );
            messageListView = findViewById(R.id.messageView);
            List<Messages> messageList = new ArrayList<>( );
            messageAdapter = new MessageAdapter(this, R.id.messageView, messageList);
            messageListView.setAdapter(messageAdapter);
        }
        mFirebaseAuthStateListener = new FirebaseAuth.AuthStateListener( ) {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mFirebaseAuth.getCurrentUser( );
                if (user != null) {
                    Toast.makeText(MainActivity.this, "User is signed in", Toast.LENGTH_SHORT).show( );
                    onSignedInInitialize(user.getDisplayName( ), user.getPhotoUrl( ));
                } else {
                    onSignOutCleanUp( );
                    AuthMethodPickerLayout mAuthPickerLayout = new AuthMethodPickerLayout
                            .Builder(R.layout.login_main)
                            .setGoogleButtonId(R.id.signInGoogle)
                            .build( );
                    startActivityForResult(
                            AuthUI.getInstance( )
                                    .createSignInIntentBuilder( )
                                    .setIsSmartLockEnabled(false)
                                    .setAuthMethodPickerLayout(mAuthPickerLayout)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder( ).build( )))
                                    .build( ),
                            RC_SIGN_IN);

                }
            }
        };
        mFirebaseAuth.addAuthStateListener(mFirebaseAuthStateListener);
        send.setOnClickListener(new View.OnClickListener( ) {
            @Override
            public void onClick(View v) {
                String message = messageEditText.getEditText( ).getText( ).toString( );
                Messages newMessage = new Messages(message, mUsername, mPhotoUrl.toString( ));
                mDatabaseRdeference.push( ).setValue(newMessage);
                messageEditText.getEditText( ).setText("");
            }
        });
        messageEditText.getEditText( ).addTextChangedListener(new TextWatcher( ) {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString( ).trim( ).length( ) > 0) {
                    send.setEnabled(true);
                } else {
                    send.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
    }

    public void onSignedInInitialize(String displayName, Uri photoUrl) {
        mUsername = displayName;
        mPhotoUrl = photoUrl;
        attachDatabaseChangeListener( );
    }

    public void onSignOutCleanUp() {
        mUsername = ANONYMOUS;
        detachDatabaseChangeListener( );
        messageAdapter.clear( );
    }

    public void attachDatabaseChangeListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener( ) {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Messages messages = dataSnapshot.getValue(Messages.class);
                    if (messages.name != mUsername) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID)
                                .setContentTitle(messages.getName( ))
                                .setContentText(messages.getMessage( ))
                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                .setStyle(new NotificationCompat.BigTextStyle( )
                                        .bigText(messages.getMessage( )))
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true);
                        createNotificationChannel( );
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);
                        // notificationId is a unique int for each notification that you must define
                        notificationManager.notify(messageAdapter.getCount( ), builder.build( ));
                    }
                    messageAdapter.add(messages);
                    messageListView.post(new Runnable( ) {
                        @Override
                        public void run() {
                            // Select the last row so it will scroll into view...
                            messageListView.setSelection(messageAdapter.getCount( ) - 1);
                        }
                    });

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            };
            mDatabaseRdeference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseChangeListener() {
        if (mChildEventListener != null) {
            mDatabaseRdeference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater( );
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId( )) {
            case R.id.signOut:
                AuthUI.getInstance( ).signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);


        }

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "BFFChats Notification";
            String description = "new message";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
