package edu.ramapo.tweisse.konojava;

import android.graphics.Point;
import java.io.Serializable;
import java.util.Vector;

/**
 * Represents a Kono game board. Holds each players pieces on a board.
 */
public class Board implements Serializable {
    /**
     * Structure to hold information about each cell of the game board.
     */
    private final class Cell implements Serializable {
        /** The owner of the cell. A home location. */
        PlayerColor owner = null;

        /** The occupant currently at this cell. */
        Piece occupant = new Piece();

        /** The point value associated with this cell. */
        int pointValue = 0;
    }

    /** Stores the grid of Cells making up this board. */
    private Cell[][] m_boardArray;

    /** Holds the size of an edge of the board. */
    private int m_boardSize;

    /**
     * Default constructor. Creates a board of 0 size.
     */
    public Board(){
        m_boardSize = 0;
        m_boardArray = null;
    }

    /**
     * Construct a board of given size.
     * @param size The size of the board. 5, 7, or 9.
     */
    public Board(int size) {
        // Handle board setup in another function since we use it elsewhere.
        InitializeBoard(size);
    }

    /**
     * Construct a board from serialized data.
     * @param data A Vector of Strings containing the board occupant colors. Can be obtained from
     *             the Serializer class functions.
     */
    public Board(Vector<String> data){
        int numCells = data.size();
        int boardSize = (int)Math.sqrt(numCells);
        if (numCells == 25 || numCells == 49 || numCells == 81){
            InitializeBoard(boardSize);
        }
        else {
            throw new IllegalArgumentException("Invalid data. Cell count invalid.");
        }

        // Iterate through every cell in the array. Map that cell to its corresponding row, col position.
        // Update the occupant at that cell.
        Point curLoc;
        PlayerColor curColor;
        boolean canCapture;

        for (int i = 0; i < numCells; i++){
            curLoc = BoardView.NumberToPoint(i, boardSize);

            // Set this cell's color based on the data.
            switch(data.elementAt(i).charAt(0)){
                case 'O':
                    curColor = null;
                    break;
                case 'W':
                    curColor = PlayerColor.WHITE;
                    break;
                case 'B':
                    curColor = PlayerColor.BLACK;
                    break;
                default:
                    throw new IllegalArgumentException("Bad data. Cell color does not exist.");
            }

            // Set this cell's ability to capture based on the data. A double character represents
            // capture ability in the serialized file.
            switch(data.elementAt(i).length()){
            case 1:
                canCapture = false;
                break;
            case 2:
                canCapture = true;
                break;
            default:
                throw new IllegalArgumentException("Bad data. Cells can be represented by at most 2 characters.");
            }
            // Reassign the occupant based on the attributes we just pulled from the data.
            SetOccupant(curLoc, new Piece(curColor));
            if (canCapture && curColor != null){
                GetOccupant(curLoc).AllowCapture();
            }
        }
    }

    /**
     * Get the size of this board.
     * @return An int holding the board size.
     */
    public int GetSize(){
        return m_boardSize;
    }

    /**
     * Gets the color of the occupant at the given location on the board.
     * @param loc The Point that we want to get the color for.
     * @return A PlayerColor matching the occupant at the given point.
     */
    public PlayerColor GetOccupantColor(Point loc){
        return GetCell(loc).occupant.GetColor();
    }

    /**
     * Get the color of the owner of a particular location on this board.
     * @param loc The Point that we want to get the owner for.
     * @return A PlayerColor matching the owner of the given point.
     */
    public PlayerColor GetOwner(Point loc){
        return GetCell(loc).owner;
    }

    /**
     * Get the point value of a cell.
     * @param loc The Point that we want to get the point value for.
     * @return An int holding the number of points the given cell is worth.
     */
    public int GetValue(Point loc){
        return GetCell(loc).pointValue;
    }

    /**
     * Gets the Piece object at a given location on this board.
     * @param loc The Point that we want to get the occupant Piece for.
     * @return The Piece object at a given Point on the board.
     */
    public Piece GetOccupant(Point loc) {
        return GetCell(loc).occupant;
    }

    /**
     * Calculates the number of points each player should have based on the current board.
     * @param color The PlayerColor of the player that we want to calculate points for.
     * @return An int holding the number of points the given player has based on where they are on the board.
     */
    public int GetPoints(PlayerColor color){

        int numOpponents = 0;
        int pts = 0;

        for (int i = 0; i < m_boardSize * m_boardSize; i++){

            Cell curCell = GetCell(BoardView.NumberToPoint(i, m_boardSize));
            if (curCell.occupant.GetColor() == color){
                if (curCell.owner == PlayerColor.Opponent(color)){
                    pts += curCell.pointValue;
                }
            }
            else if (curCell.occupant.GetColor() == PlayerColor.Opponent(color)){
                numOpponents += 1;
            }
        }
        // Add the necessary points to reflect capturing opponents.
        int numPieces = m_boardSize + 2;
        pts += 5 * (numPieces - numOpponents);

        return pts;
    }

    /**
     * Checks to see if a given row and column are out of range for this board.
     * @param loc The Point that we want to check validity of.
     * @return A boolean value. True if the point is on the board, false if not.
     */
    public boolean IsValidLocation(Point loc){
        if (loc.x < 1 || loc.x > m_boardSize || loc.y < 1 || loc.y > m_boardSize) {
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Scans the board to see if there is a winner based on the positions of the pieces.
     * @return A PlayerColor matching the color of the winner on this board. Null if there is no winner.
     * PlayerColor.BOTH if the game is a tie.
     */
    public PlayerColor GetWinner(){

        // If one players pieces are completely gone, the other wins.
        if (CountPieces(PlayerColor.WHITE ) == 0){
            return PlayerColor.BLACK;
        }
        if (CountPieces(PlayerColor.BLACK) == 0){
            return PlayerColor.WHITE;
        }

        // Variables to represent if a color has all its pieces in the opponent's home locations.
        boolean whiteWin = true;
        boolean blackWin = true;

        // Check every cell in the array. If any piece is not in the opponent's home location, they
        // have no yet won the game.
        for (int row = 0; row < m_boardSize; row++){
            for (int col = 0; col < m_boardSize; col ++){
                PlayerColor thisOccupant = m_boardArray[row][col].occupant.GetColor();
                PlayerColor thisOwner = m_boardArray[row][col].owner;

                // If the occupant is not in the opponent's home location that color has not won.
                if (thisOccupant == PlayerColor.WHITE && thisOwner != PlayerColor.BLACK){
                    whiteWin = false;
                }
                else if (thisOccupant == PlayerColor.BLACK && thisOwner != PlayerColor.WHITE){
                    blackWin = false;
                }
            }
        }
        // Return the winner based on how many points each player has.
        if (whiteWin || blackWin){
            int whitePoints = GetPoints(PlayerColor.WHITE);
            int blackPoints = GetPoints(PlayerColor.BLACK);

            if (whitePoints > blackPoints){
                return PlayerColor.WHITE;
            }
            else if (blackPoints > whitePoints){
                return PlayerColor.BLACK;
            }
            return PlayerColor.BOTH;
        }
        else {
            return null;
        }
    }

    /**
     * Executes a move on this board described by the Move object passed to it.
     * @param move The Move object describing the move that we would like to execute.
     * @param playerColor The PlayerColor matching that of the player who will execute the move.
     * @return A MoveResult object describing the outcome of the move, including any error that occurred.
     */
    public MoveResult MakeMove(Move move, PlayerColor playerColor){
        if (move == null){
            throw new IllegalArgumentException("Null move.");
        }

        // If the player wants to quit.
        if (move.GetAction() == Action.QUIT){
            return new MoveResult(-5, MoveError.QUIT);
        }

        Point movePos = move.GetLocation();

        // Get the Cell at the location that we want to move.
        Cell moveCell = GetCell(movePos);

        // Make sure this location has a piece in it.
        if (moveCell.occupant.GetColor() == null) {
            return new MoveResult(0, MoveError.EMPTY);
        }
        // Make sure the piece is the player's own.
        if (moveCell.occupant.GetColor() != playerColor){
            return new MoveResult(0, MoveError.INVALID_COLOR);
        }
        // Make sure the location we are trying to move is on the board.
        if (!IsValidLocation(movePos)){
            throw new IllegalArgumentException("Invalid row or column.");
        }
        // Figure out where the user would like to move to.
        int targetRow;
        int targetCol;
        switch(move.GetDirection()){
            case NW:
                targetRow = movePos.x - 1;
                targetCol = movePos.y - 1;
                break;
            case NE:
                targetRow = movePos.x - 1;
                targetCol = movePos.y + 1;
                break;
            case SE:
                targetRow = movePos.x + 1;
                targetCol = movePos.y + 1;
                break;
            case SW:
                targetRow = movePos.x + 1;
                targetCol = movePos.y - 1;
                break;
            default:
                throw new IllegalArgumentException("Invalid Move object.");
        }
        Point targetPos = new Point(targetRow, targetCol);

        // Validate that the position the user wants to move to is on the board.
        if (!IsValidLocation(targetPos)){
            return new MoveResult(0, MoveError.INVALID_DIRECTION);
        }
        // Make sure the target is not occupied by one of the player's own pieces.
        Cell targetCell = GetCell(targetPos);
        if (targetCell.occupant.GetColor() == playerColor){
            return new MoveResult(0, MoveError.OCCUPIED);
        }
        // Also make sure that if occupied by an opponent, the player can capture.
        else if (targetCell.occupant.GetColor() != null && !moveCell.occupant.CanCapture()){
            return new MoveResult(0, MoveError.NO_CAPTURE);
        }
        // If we have made it this far, we are allowed to execute the move.
        // First, calculate the points to be added to the player's score after this move.
        int points = 0; // Will track how many points this move yields.
        if ((targetCell.owner == PlayerColor.Opponent(playerColor)) || (moveCell.owner == PlayerColor.Opponent(playerColor))){
            points += (targetCell.pointValue - moveCell.pointValue);
        }
        // Add 5 points for capturing.
        if (!targetCell.occupant.IsEmpty() && targetCell.occupant.GetColor() != playerColor){
            points += 5;
        }
        // Allow the piece to capture if we reach the opponent's home location.
        if (targetCell.owner == PlayerColor.Opponent(playerColor)){
            moveCell.occupant.AllowCapture();
        }
        // Perform the move.
        targetCell.occupant = moveCell.occupant;
        moveCell.occupant = new Piece();

        // Return a successful MoveResult object.
        return new MoveResult(points, null);
    }

    /**
     * Initializes the board array to default conditions.
     * @param boardSize The size of the board that we want to create. Must be 5, 7, or 9.
     * @exception IllegalArgumentException If the board size is not valid.
     */
    private void InitializeBoard(int boardSize){

        // First, validate the size of the board.
        if (boardSize != 5 && boardSize != 7 && boardSize != 9){
            throw new IllegalArgumentException("Invalid board size.");
        }
        m_boardSize = boardSize;

        // Initialize the board array to the proper size.
        m_boardArray = new Cell[m_boardSize][m_boardSize];

        // Loop through each row to assign cell values.
        for (int row = 0; row < m_boardSize; row++){
            // Default values. We'll use these unless the right conditions are met below.
            PlayerColor cellColor = null;
            boolean onEdge = false;

            // If we are on the first 2 rows, the color will be set to white.
            if (row <= 1){
                cellColor = PlayerColor.WHITE;
                if (row == 0){
                    // Indicate that we are on the top edge of the board.
                    onEdge = true;
                }
            }
            // If we are on the last 2 rows, the color will be set to black.
            else if (row >= m_boardSize - 2){
                cellColor = PlayerColor.BLACK;
                if (row == m_boardSize - 1){
                    // Indicate that we are on the bottom edge of the board.
                    onEdge = true;
                }
            }
            // Loop through each column in the current row to assign each cell correctly.
            for (int col = 0; col < m_boardSize; col++){
                m_boardArray[row][col] = new Cell(); // Initialize the cell at each location.
                if (onEdge || col == 0 || col == boardSize - 1){

                    m_boardArray[row][col].occupant = new Piece(cellColor);
                    m_boardArray[row][col].owner = cellColor;

                    // Assign the correct point value to each location.
                    if (!onEdge || col == 1 || col == m_boardSize - 2){
                        m_boardArray[row][col].pointValue = 1;
                    }
                    else if (col == 0 || col == m_boardSize -1 ){
                        m_boardArray[row][col].pointValue = 3;
                    }
                    else {
                        m_boardArray[row][col].pointValue = ((Math.min(col, m_boardSize - 1 - col) + 1) * 2) - 1;
                    }
                }
            }
        }
        // Remove bad values from middle of the board.
        for (int row = 2; row < boardSize - 2; row++){
            m_boardArray[row][0].pointValue = 0;
            m_boardArray[row][boardSize - 1].pointValue = 0;
        }
    }

    /**
     * Returns the number of pieces of a certain color are left on the board.
     * @param color The PlayerColor that we want to count on the board.
     * @return An int holding the number of pieces of the given color.
     */
    private int CountPieces(PlayerColor color){
        int count = 0;

        for (int i = 0; i < m_boardSize; i++){
            for (int j = 0; j < m_boardSize; j++){
                if (m_boardArray[i][j].occupant.GetColor() == color){
                    count += 1;
                }
            }
        }
        return count;
    }

    /**
     * Gets the Cell structure at a given point on the board. Null if off the board.
     * @param loc The Point that we want to get the Cell at.
     * @return The Cell structure located at the given point.
     */
    private Cell GetCell(Point loc){
        if (IsValidLocation(loc)){
            return m_boardArray[loc.x - 1][loc.y - 1];
        }
        else {
            throw new IllegalArgumentException("Bad Point.");
        }
    }

    /**
     * Assign an occupant to a given cell.
     * @param loc The Point at which we want to assign a new occupant to.
     * @param occupant The occupant that we want to assign to the location on the board.
     */
    private void SetOccupant(Point loc, Piece occupant){
        GetCell(loc).occupant = occupant;
    }

    public static void main(String args[]){
    }
}

