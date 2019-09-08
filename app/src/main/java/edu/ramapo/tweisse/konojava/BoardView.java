package edu.ramapo.tweisse.konojava;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Displays a Board object as a grid of buttons on a specific activity.
 */
public class BoardView {

    /** Holds the activity that we want the board to be displayed on. */
    private Activity m_activity;

    /**
     * Default constructor. Creates a BoardView linked to no activity.
     */
    BoardView(){
        this(null);
    }

    /**
     * Creates a BoardView object linked to a specific activity.
     * @param activity The activity that we want the board to be displayed on.
     */
    BoardView(Activity activity){
        m_activity = activity;
    }

    /**
     * Creates the UI elements necessary to render a board, given a container id and a board size.
     * @param containerId The ID of the GridLayout which we want to contain the board buttons.
     * @param boardSize The size of the board that we are trying to create.
     */
    public void CreateBoard(int containerId, int boardSize){
        // Set the row count and column count on screen to match the size of our board.
        android.support.v7.widget.GridLayout boardView = m_activity.findViewById(containerId);

        if (boardView != null) {
            // Get the screen width to size the board.
            DisplayMetrics displayMetrics = new DisplayMetrics();
            m_activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenWidth = displayMetrics.widthPixels;

            // Set the width of the board to match the screen.
            ViewGroup.LayoutParams boardParams = boardView.getLayoutParams();
            boardParams.width = screenWidth;
            boardParams.height = boardParams.width;
            boardView.setLayoutParams(boardParams);

            // Set the correct number of rows and columns for this board.
            boardView.setRowCount(boardSize);
            boardView.setColumnCount(boardSize);

            // Fill the grid view with buttons.
            for (int i = 0; i < boardSize * boardSize; i++){
                Button button = new Button(m_activity.getApplicationContext());
                button.setId(i);
                button.setOnClickListener((android.view.View.OnClickListener)m_activity);
                boardView.addView(button);
            }
            for (int i = 0; i < boardSize * boardSize; i++){
                Button button = m_activity.findViewById(i);
                if (button != null){
                    button.getBackground().setColorFilter(0x00000000, PorterDuff.Mode.MULTIPLY);
                    ViewGroup.LayoutParams params = button.getLayoutParams();
                    params.height = screenWidth / boardSize;
                    params.width = screenWidth / boardSize;
                    button.setLayoutParams(params);
                }
            }
        }
    }

    /**
     * Updates the Colors of the board buttons to reflect the state of the given Board object.
     * @param board The Board object we want to display on screen.
     * @param BOARD_COLOR The color user for the blank spaces on the board.
     * @param WHITE_COLOR The color used for white pieces on the board.
     * @param BLACK_COLOR The color used for black pieces on the board.
     * @param SCORE_COLOR The color used for displaying the point value on home pieces.
     */
    public void UpdateBoard(Board board, int BOARD_COLOR, int WHITE_COLOR, int BLACK_COLOR, int SCORE_COLOR){
        // Loop through every position on the board.
        int boardSize = board.GetSize();
        for (int i = 0; i < boardSize * boardSize; i++){
            Button curBtn = (m_activity.findViewById(i));
            if (curBtn != null){
                // Change the button colors to reflect the occupant at each location.
                Piece curPiece = board.GetOccupant(NumberToPoint(i, boardSize));
                if (curPiece.GetColor() == null){
                    int value = board.GetValue(NumberToPoint(i, boardSize));
                    if (value > 0){
                        curBtn.setText(Integer.toString(value));
                    }
                    else {
                        curBtn.setText("");
                    }
                    curBtn.setTextColor(SCORE_COLOR);
                    curBtn.getBackground().setColorFilter(BOARD_COLOR, PorterDuff.Mode.MULTIPLY);
                }
                else if (curPiece.GetColor() == PlayerColor.WHITE && !curPiece.CanCapture()){
                    curBtn.setText("");
                    curBtn.setTextColor(BLACK_COLOR);
                    curBtn.getBackground().setColorFilter(WHITE_COLOR, PorterDuff.Mode.MULTIPLY);
                }
                else if (curPiece.GetColor() == PlayerColor.WHITE && curPiece.CanCapture()){
                    curBtn.setText("\u25CF");
                    curBtn.setTextColor(BLACK_COLOR);
                    curBtn.getBackground().setColorFilter(WHITE_COLOR, PorterDuff.Mode.MULTIPLY);
                }
                else if (curPiece.GetColor() == PlayerColor.BLACK && !curPiece.CanCapture()){
                    curBtn.setText("");
                    curBtn.setTextColor(WHITE_COLOR);
                    curBtn.getBackground().setColorFilter(BLACK_COLOR, PorterDuff.Mode.MULTIPLY);
                }
                else if (curPiece.GetColor() == PlayerColor.BLACK && curPiece.CanCapture()){
                    curBtn.setText("\u25CF");
                    curBtn.setTextColor(WHITE_COLOR);
                    curBtn.getBackground().setColorFilter(BLACK_COLOR, PorterDuff.Mode.MULTIPLY);
                }
                else {
                    curBtn.setText("");
                    curBtn.setTextColor(BLACK_COLOR);
                    curBtn.getBackground().setColorFilter(BOARD_COLOR, PorterDuff.Mode.MULTIPLY);
                }
            }
        }
    }

    /**
     * Converts a 1-D zero indexed array subscript to a point on the board.
     * @param num The 1-D array subscript we want to convert.
     * @param boardSize The size of the board we are converting on.
     * @return A Point representing a location on a board of given size.
     */
    public static Point NumberToPoint(int num, int boardSize){
        int row = num / boardSize + 1; // Relies on integer division.
        int col = num % boardSize + 1;
        return new Point(row, col);
    }
}
