package edu.ramapo.tweisse.konojava;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

/**
 * Allows the user to save an existing tournament to a given file.
 */
public class SaveTournamentActivity extends AppCompatActivity {

    /**
     * No activity specific setup here.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_tournament);
    }

    /**
     * Called whenever the save button is clicked. Saves the tournament to a file and quits.
     * @param view The Button object reference for the save button.
     */
    public void SaveGame(View view){

        // Hide the keyboard.
        View kb = this.getCurrentFocus();
        if (kb != null) {
            InputMethodManager methodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            methodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        // Retrieve the tournament that was passed to this activity.
        Tournament tourn = (Tournament)getIntent().getSerializableExtra("tournament");

        // Get the sd card directory.
        File filepath = Environment.getExternalStorageDirectory();

        // Get the filename that the user entered.
        EditText outFileInput = findViewById(R.id.filenameOutput);
        if (outFileInput != null){
            filepath = new File(filepath, outFileInput.getText().toString());
        }

        // Save the data to the chosen path.
        boolean saveResult = Serializer.SerializeToFile(tourn, filepath, this);

        // Tell the user the result of the save and exit the application.
        DisplaySaveResultMessage(saveResult, filepath.getAbsolutePath().toString());
    }

    /**
     * Displays the result of saving the file to the user and exits the application.
     * @param result A boolean value. True if saved successfully, false if not.
     * @param filepath The filepath where the serialized file was saved.
     */
    private void DisplaySaveResultMessage(boolean result, String filepath){
        if (result){
            // Tell the user what the outcome of saving the file was. If successful, exit the application.
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("File saved successfully.");
            alert.setMessage(String.format("The file was successfully saved to '%s'. The application will now exit.", filepath));
            alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                    moveTaskToBack(true);
                }
            });
            alert.setIcon(android.R.drawable.ic_dialog_alert);
            alert.show();
        }
        else {
            Toast errDisplay = Toast.makeText(this, "Error saving file.", Toast.LENGTH_SHORT);
            errDisplay.show();
        }
    }
}
