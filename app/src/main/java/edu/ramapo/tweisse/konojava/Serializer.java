package edu.ramapo.tweisse.konojava;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.support.v4.app.ActivityCompat;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * Holds functions used to serialize a tournament to a text file, as well as read a previously serialized
 * file back into a Tournament object.
 */
public class Serializer {

    /** Holds the permissions needed to read and write external files. */
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Obtains the necessary permissions to read and write files.
     * From https://stackoverflow.com/questions/34040355/how-to-check-the-multiple-permission-at-single-request-in-android-m/48456135#48456135
     * @param activity The Activity that needs the permissions.
     */
    public static void VerifyStoragePermissions(Activity activity){
        // Check if we have write permission.
        int permission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // If we don't have permission we need to prompt the user for it.
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    /**
     * Serializes a Tournament object to a given File.
     * @param tourn The Tournament object we want to serialize.
     * @param file The File we want to write the tournament data to.
     * @param act The Activity which is serializing the file. Needed for permission requests.
     * @return A boolean value. True if the file was successfully serialized. False if not.
     */
    public static boolean SerializeToFile(Tournament tourn, File file, Activity act){
        // Make sure we still have read permissions.
        VerifyStoragePermissions(act);

        try {
            file.createNewFile();
        }
        catch(IOException e){
            return false;
        }

        try {
            PrintWriter stream = new PrintWriter(file);
            stream.println(String.format("Round: %d", tourn.GetRoundNum()));
            stream.println();

            // Check the type of player 2.
            String player2String;
            if (tourn.GetPlayerType(2) == PlayerType.HUMAN){
                player2String = "Human 2";
            }
            else {
                player2String = "Computer";
            }
            stream.println(String.format("%s:", player2String));

            // Print player 2's score so far in this tournament.
            stream.println(String.format("   Score: %d", tourn.GetPlayerScore(2)));

            // Print player 2's color for the current game.
            String curColor;
            if (tourn.GetGame().GetPlayer(2).GetColor() == PlayerColor.WHITE){
                curColor = "White";
            }
            else {
                curColor = "Black";
            }
            stream.println(String.format("   Color: %s", curColor));
            stream.println();

            // Print player 1's score and color.
            stream.println("Human:");
            stream.println(String.format("   Score: %d", tourn.GetPlayerScore(1)));
            if (curColor.equals("White")){
                curColor = "Black";
            }
            else {
                curColor = "White";
            }
            stream.println(String.format("   Color: %s", curColor));
            stream.println();

            // Loop through each cell to print board data.
            stream.print("Board:");
            for (int r = 0; r < tourn.GetGame().GetBoard().GetSize(); r++){
                // New line before each row.
                stream.print("\n   ");
                for (int c = 0; c < tourn.GetGame().GetBoard().GetSize(); c++){
                    // Get occupant information for the current cell.
                    Piece occupant = tourn.GetGame().GetBoard().GetOccupant(new Point(r + 1, c + 1));
                    boolean canCapture = occupant.CanCapture();
                    PlayerColor color = occupant.GetColor();

                    // Determine what to print for each cell depending upon the occupant.
                    String thisCell;
                    if (color == PlayerColor.WHITE && !canCapture){
                        thisCell = "W  ";
                    }
                    else if (color == PlayerColor.WHITE && canCapture){
                        thisCell = "WW ";
                    }
                    else if (color == PlayerColor.BLACK && !canCapture){
                        thisCell = "B  ";
                    }
                    else if (color == PlayerColor.BLACK && canCapture) {
                        thisCell = "BB ";
                    }
                    else {
                        thisCell = "O  ";
                    }
                    stream.print(thisCell);
                }
            }
            stream.println("\n");
            // Print the next player to move.
            stream.print("Next Player: ");
            if (tourn.GetGame().GetNextPlayer() == 1){
                stream.print("Human");
            }
            else {
                stream.print(player2String);
            }
            // Flush buffer to file and close file.
            stream.close();
        }
        catch (FileNotFoundException e){
            // If the file could not be created, return failure.
            return false;
        }
        // Return success if no exception occurred.
        return true;
    }

    /**
     * Creates a Tournament object from serialized data contained in a File.
     * @param file The File we want to read the tournament data from.
     * @param act The Activity which is de-serializing the file. Needed for permission requests.
     * @return A Tournament object if the file was successfully de-serialized. Null if not.
     */
    public static Tournament DeserializeFromFile(File file, Activity act){
        // Make sure we still have read permissions.
        VerifyStoragePermissions(act);

        // The variables that we will need to extract from the serialized file.
        // Initializing them to invalid values will allow us to check for errors with the read.
        Player p1 = null;
        Player p2 = null;

        Board board = null;
        Game game = null;

        int round = -1;
        int nextPlayer = -1;

        // Try to open our input file.
        try {
            FileReader stream = new FileReader(file);
            BufferedReader buff = new BufferedReader(stream);

            // Read every line from the file.
            String line;
            while ((line = buff.readLine()) != null){
                // Split our line into an array of individual words.
                String words[] = line.trim().split("\\s+");

                // Parse the round number.
                if (words[0].equals("Round:")){
                    round = Integer.parseInt(words[1]);
                }
                // The human will always be player 1.
                else if (words[0].equals("Human:") ){
                    p1 = ReadPlayerData(buff, PlayerType.HUMAN);
                }
                // Human 2 or the computer will always be player 2.
                else if (words[0].equals("Computer:")){
                    p2 = ReadPlayerData(buff, PlayerType.COMPUTER);
                }
                else if (words.length > 1 && words[0].equals("Human") && words[1].equals("2:")){
                    p2 = ReadPlayerData(buff, PlayerType.HUMAN);
                }
                // Read in the board data.
                else if (words[0].equals("Board:")){
                    board = new Board(ReadBoardData(buff));
                }
                // Read which player is up next.
                else if (words.length > 1 && words[0].equals("Next") && words[1].equals("Player:")){
                    if (words[2].equals("Human") && words.length == 3){
                        nextPlayer = 1;
                    }
                    else {
                        nextPlayer = 2;
                    }
                }
            }
        }
        // If there was an error reading the file, return a null Tournament.
        catch(FileNotFoundException e){
            return null;
        }
        catch(IOException e){
            return null;
        }
        // Catch any other exception.
        catch(Exception e){
            return null;
        }

        // Create a game.
        game = new Game(p1, p2, nextPlayer, board);

        // Make sure that all pieces of the file were read correctly, and create a tournament.
        if (p1 == null || p2 == null || board == null || game == null || round == -1 || nextPlayer == -1){
            return null;
        }
        else {
            return new Tournament(p1.GetType(), p1.GetPoints(), p2.GetType(), p2.GetPoints(), game, round, nextPlayer);
        }
    }

    /**
     * Reads player data from a file. Assumes that the type of player has already been read.
     * @param buff The BufferedReader object reading the current file.
     * @param type The PlayerType of the player we want to read data for.
     * @return A Player object as a container for the data read from the file.
     */
    private static Player ReadPlayerData(BufferedReader buff, PlayerType type){

        // To hold the data that we will read from the file.
        int score = -1;
        PlayerColor color = null;

        // Attempt to read a single player's data from the reader.
        try{
            String[] words = buff.readLine().trim().split("\\s+");
            score = Integer.parseInt(words[1]);
            words = buff.readLine().trim().split("\\s+");
            if (words[1].equals("White")){
                color = PlayerColor.WHITE;
            }
            else {
                color = PlayerColor.BLACK;
            }

            // If all data was read successfully, return the player.
            if (score > -1 && color != null){
                if (type == PlayerType.HUMAN){
                    return new Human(color, score);
                }
                else {
                    return new Computer(color, score);
                }
            }
            else {
                return null;
            }
        }
        catch(IOException e){
            return null;
        }
        catch(Exception e){
            return null;
        }
    }

    /**
     * Reads the board data from a file and loads it into a vector of strings.
     * @param buff The BufferedReader object which is reading the file.
     * @return A Vector of Strings containing the data from the file describing a Board object.
     */
    private static Vector<String> ReadBoardData(BufferedReader buff){
        Vector<String> boardData = new Vector<>();
        // Read until we hit a blank link. This means that we are at the end of the board.
        try {
            String line;
            while ((line = buff.readLine()) != null){
                String words[] = line.trim().split("\\s+");

                // Break out if we have reached the end of the board.
                if (words[0].equals("")){
                    break;
                }
                else {
                    for (String word : words){
                        boardData.add(word);
                    }
                }
            }
        }
        catch(IOException e){
            return null;
        }
        catch(Exception e){
            return null;
        }
        return boardData;
    }
}
