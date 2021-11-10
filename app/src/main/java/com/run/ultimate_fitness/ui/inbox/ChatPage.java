package com.run.ultimate_fitness.ui.inbox;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.CustomMessage;
import com.cometchat.pro.models.MediaMessage;
import com.cometchat.pro.models.TextMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.run.ultimate_fitness.R;
import com.run.ultimate_fitness.utils.Constants;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import co.intentservice.chatui.ChatView;
import co.intentservice.chatui.models.ChatMessage;

public class ChatPage extends AppCompatActivity {

    public String messageText;
    private String listenerID = "CHAT_PAGE";
    public String picture;
    public String newMessage;
    private ChatView chatView;
    public String fullName, firstName, lastName;
    private ImageView profilePicImage;

    public static final String USER_PREFS ="userPrefs";
    public static final String FIRST_NAME ="firstName";
    public static final String LAST_NAME ="lastName";


    public static final String CREDENTIALS_PREFS = "credentials";
    public static final String USER_UID = "uid";
    public String userUID;
    private String temp_key;
    TextView txtUsername;
    public String temp_clientUID;

    public String uid ="";

    public  DatabaseReference root, root2;
    public Iterator i;
    public boolean x = false;
    public String clientUID;


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_page);
        getSupportActionBar().hide();

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(CREDENTIALS_PREFS,MODE_PRIVATE);
        userUID = sharedPreferences.getString(USER_UID, "uid");

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Bundle mbundle = getIntent().getExtras();
        if (!userUID.equals(Constants.MASTER_UID)){
            clientUID = uid;
        }
          else{
            temp_clientUID = mbundle.getString("client_uid");
            clientUID = temp_clientUID;

        }




        profilePicImage = findViewById(R.id.icon_user);






        chatView = (ChatView) findViewById(R.id.chat_view);
        txtUsername = (TextView) findViewById(R.id.txtUsername);


        chatView.setOnSentMessageListener(new ChatView.OnSentMessageListener(){
            @Override
            public boolean sendMessage(ChatMessage chatMessage){
                // perform actual message sending
                messageText = chatView.getTypedMessage();


                x = true;

                sendFirebaseMessage();

                root.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //String image = snapshot.child("image").getValue().toString();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


                return true;
            }
        });

        loadChat();

        loadImage();


    }

    private void loadChat() {
        root = FirebaseDatabase.getInstance("https://ultimate-storm-default-rtdb.europe-west1.firebasedatabase.app").getReference().getRoot().child("users").child(clientUID).child("chat");

        root.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                picture = (String) snapshot.child("image").getValue();

                updateChatConversation(snapshot);


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                updateChatConversation(snapshot);

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public String chatMessage, userName, temp_uid;

    private void updateChatConversation(DataSnapshot snapshot) {


        i = snapshot.getChildren().iterator();
        while (i.hasNext()){



            chatMessage = (String) ((DataSnapshot)i.next()).getValue();
            userName = (String) ((DataSnapshot)i.next()).getValue();
           temp_uid = (String) ((DataSnapshot)i.next()).getValue();

            if (temp_uid.equals(uid)){
                if (x == false) {
                    chatView.addMessage(new ChatMessage(chatMessage, System.currentTimeMillis(), ChatMessage.Type.SENT));
                }
            }

            if (!temp_uid.equals(uid)) {
                chatView.addMessage(new ChatMessage(chatMessage, System.currentTimeMillis(), ChatMessage.Type.RECEIVED));
            }


        }


    }
    private void receiveChatConversation(DataSnapshot snapshot) {

        i = snapshot.getChildren().iterator();
        while (i.hasNext()){



            chatMessage = (String) ((DataSnapshot)i.next()).getValue();
            userName = (String) ((DataSnapshot)i.next()).getValue();
            temp_uid = (String) ((DataSnapshot)i.next()).getValue();


            if (!temp_uid.equals(uid)) {
                chatView.addMessage(new ChatMessage(chatMessage, System.currentTimeMillis(), ChatMessage.Type.RECEIVED));
            }


        }
    }


    private void sendFirebaseMessage() {
        if (uid.equals(Constants.MASTER_UID)){
            uid = clientUID;
        }

        root = FirebaseDatabase.getInstance("https://ultimate-storm-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference()
                .child("users")
                .child(uid)
                .child("chat");

        Map<String,Object> map1 = new HashMap<String, Object>();
        temp_key = root.push().getKey();
        root.updateChildren(map1);

        DatabaseReference message_root = root.child(temp_key);
        Map<String,Object> map2 = new HashMap<String,Object>();
        map2.put("name", fullName);
        map2.put("message", messageText);
        map2.put("uid", uid);

        message_root.updateChildren(map2);

        addLastMessage();

    }

    private void addLastMessage() {
        root2 = FirebaseDatabase.getInstance("https://ultimate-storm-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference()
                .child("users")
                .child(uid);

        DatabaseReference message_root = root2;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("message", messageText);
        message_root.updateChildren(map);


    }

    public  void loadImage() {

        root = FirebaseDatabase.getInstance("https://ultimate-storm-default-rtdb.europe-west1.firebasedatabase.app").getReference().getRoot().child("users").child(clientUID);

        SharedPreferences sharedPreferences = this.getApplicationContext().getSharedPreferences(USER_PREFS, MODE_PRIVATE);
        firstName = sharedPreferences.getString(FIRST_NAME, "");
        lastName = sharedPreferences.getString(LAST_NAME, "");
        fullName = firstName + " " + lastName;


        if (uid.equals("69wADqnIpqYUnmidwcZwaO5F8RL2")) {
            profilePicImage.setImageBitmap(StringToBitMap(picture));

            root.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    picture = (String) snapshot.child("image").getValue();
                    profilePicImage.setImageBitmap(StringToBitMap(picture));
                    String temp_name =(String) snapshot.child("name").getValue();
                    txtUsername.setText(temp_name);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
        else{
         profilePicImage.setImageResource(R.drawable.ultimate_fitness);
            txtUsername.setText("Ultimate Fitness");

        }

    }

    public Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte= Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }


}