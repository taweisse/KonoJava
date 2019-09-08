package edu.ramapo.tweisse.konojava;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import java.io.File;

/**
 * Allows the user to load and resume an existing serialized file from storage.
 */
public class LoadTournamentActivity extends AppCompatActivity implements View.OnClickListener{

    /**
     * No activity specific setup here.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_tournament);
    }

    /**
     * Disables the back animation when leaving this activity.
     */
    @Override
    public void onPause(){
        super.onPause();
        // Disable back animation.
        overridePendingTransition(0, 0);
    }

    /**
     * Handles the user clicking the open file button. Loads the tournament if the file is valid and switches
     * to PlayGameActivity.
     */
    public void onClick(View view) {
        if (view.getId() == R.id.chooseFileBtn){
            // Hide the keyboard.
            View kb = this.getCurrentFocus();
            if (kb != null) {
                InputMethodManager methodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                methodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            // Get the filename the user wants to open.
            String filename;
            EditText filenameInput = findViewById(R.id.filenameInput);
            if (filenameInput != null){
                filename = filenameInput.getText().toString();
            }
            else {
                throw new IllegalArgumentException("EditText doesn't exist in this view.");
            }

            // Get the sd card directory.
            File sdcard = Environment.getExternalStorageDirectory();
            File file = new File(sdcard, filename);

            if (file.exists()){
                // Attempt to read the file.
                Tournament tourn = Serializer.DeserializeFromFile(file, this);
                if (tourn == null){
                    // Tell the user that there was an error reading the file.
                    Toast errDisplay = Toast.makeText(this, "Error loading from file.", Toast.LENGTH_SHORT);
                    errDisplay.show();
                }
                else {
                    // Launch the tournament.
                    Intent intent = new Intent(this, PlayGameActivity.class);
                    intent.putExtra("tournament", tourn);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }
            else {
                // The file was not found. Tell the user.
                Toast errDisplay = Toast.makeText(this, "File not found.", Toast.LENGTH_SHORT);
                errDisplay.show();
            }
        }
    }
}
