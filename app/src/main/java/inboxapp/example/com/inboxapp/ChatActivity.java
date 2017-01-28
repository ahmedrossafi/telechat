package inboxapp.example.com.inboxapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.AsyncTask;
import android.widget.EditText;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.login.widget.LoginButton;
import com.facebook.messenger.MessengerUtils;
import com.facebook.messenger.MessengerThreadParams;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.facebook.login.LoginManager;
import android.util.Log;
import android.text.TextWatcher;
import android.text.Editable;
import java.io.InputStream;

public class ChatActivity extends Activity {

    //Ici le code de la requette qui utilise startActivityForResult pour envoyer un message
    private static final int REQUEST_CODE_SHARE_TO_MESSENGER = 1;
    private EditText writeamessage;
    private Toolbar mToolbar;
    private View mMessengerButton;
    private MessengerThreadParams mThreadParams;
    private boolean mPicking;
    CallbackManager callbackManager;

    //  Le TextWatcher permet de verifier si quelque chose est saisie dans mon Edi Text
    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // Appel de la fonction pour verifier si quelque chose est saisie apres saisie du texte
            checkFieldsForEmptyValues();
        }
    };

    //Fonction pour verifier si quelque chose est saisie dans Edit Text si oui activer le bouton
    // sinon y desactiver le click


    void checkFieldsForEmptyValues(){
        FrameLayout sendbutton = (FrameLayout) findViewById(R.id.messenger_send_button);

        String writemessage = writeamessage.getText().toString();

        if(writemessage.equals("")){
            sendbutton.setClickable(false);
        } else {
            sendbutton.setClickable(true);
        }
    }

    // Intialisation du sdk facebook + declaration de variables
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        //Callback manager permet d'executer les callbacks au sein du SDK facebook
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_chat);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        //declaration du bouton d'envoi de messages
        mMessengerButton = findViewById(R.id.messenger_send_button);
        writeamessage = (EditText) findViewById(R.id.message);
        mToolbar.setTitle(R.string.app_name);

        // Creer un nouvel intent et verifier si ce qui est passé à l'Intent = à l'action faite
        Intent intent = getIntent();
        if (Intent.ACTION_PICK.equals(intent.getAction())) {
            mThreadParams = MessengerUtils.getMessengerThreadParamsForIntent(intent);
            mPicking = true;
        }

        //Listener qui ecoute sur la fonction d'envoi de message
        mMessengerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMessengerButtonClicked();
            }
        });

        //Declaration des valeurs recues sur l'activite + fonction de deconnexion
        Bundle inBundle = getIntent().getExtras();
        String name = inBundle.get("name").toString();
        String surname = inBundle.get("surname").toString();
        String imageUrl = inBundle.get("imageUrl").toString();
        writeamessage.addTextChangedListener(mTextWatcher);
        checkFieldsForEmptyValues();
        TextView nameView = (TextView) findViewById(R.id.user_name);
        nameView.setText(" " + name + " " + surname);
        LoginButton logout = (LoginButton) findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logOut();
                Intent login = new Intent(ChatActivity.this, LoginActivity.class);
                startActivity(login);
                finish();
            }
        });
        new ChatActivity.DownloadImage((ImageView) findViewById(R.id.profileImage)).execute(imageUrl);
    }


    //Fonction pour l'envoi de message
    private void onMessengerButtonClicked() {
        //Code pour le texte à saisir et ouverture de fenêtre d'envoi avec possibilité de choix d'utilisateurs
        EditText sendit = (EditText) findViewById(R.id.message);
        String mymsg = sendit.getText().toString();

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent
                .putExtra(Intent.EXTRA_TEXT,
                        mymsg);
        sendIntent.setType("text/plain");
        sendIntent.setPackage("com.facebook.orca");
        try {
            startActivity(sendIntent);
        }
        catch (android.content.ActivityNotFoundException ex) {
        }
    }

    //Code pour telecharger l'image de profil et l'avoir affichée après connexion

    public class DownloadImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImage(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }


    //code pour garder la valeur dans l'Edit Text
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Ici on enregistre l etat de lactivite ou on l'a laissé
        // Pour recevoir la valeur sur l edit text apres avoir redemarrer l'activité
            outState.putString("text", writeamessage.getText().toString());
    }


}