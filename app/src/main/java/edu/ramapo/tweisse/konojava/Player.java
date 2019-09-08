package edu.ramapo.tweisse.konojava;

import android.graphics.Point;
import java.io.Serializable;
import java.util.Random;
import java.util.Vector;

/**
 * Holds information about a player in a game, like color and number of points accumulated.
 * Also contains the computer AI system that is capable of finding a good move on a board.
 */
public class Player implements Serializable{

    /** The number of points this player has accumulated. */
    protected int m_points;

    /** This player's color. */
    protected PlayerColor m_color;

    /**
     * A structure to hold a point and the direction that resulted in that point.
     */
    private class SurroundingPoint{
        Point m_endPoint;    /** The possible point to move to. */
        MoveDirection m_dir; /** The direction that would result in a move to m_endPoint. */

        /**
         * Create a SurroundingPoint from an endpoint and move direction.
         */
        SurroundingPoint(Point pt, MoveDirection dir){
            m_endPoint = pt;
            m_dir = dir;
        }
    }

    /**
     * Default constructor. Creates a player with no color and no points.
     */
    Player(){
        this(null, 0);
    }

    /**
     * Initialize a player with a color and points.
     * @param color A PlayerColor holding the desired color of this player.
     * @param points An integer holding the number of points this player should start with.
     */
    Player(PlayerColor color, int points){
        m_points = points;
        m_color = color;
    }

    /**
     * Get the type of this player.
     * @return Null for the base player. Overridden in child classes.
     */
    public PlayerType GetType(){
        return null;
    }

    /**
     * Return the color of this player.
     * @return A PlayerColor object holding this player's color.
     */
    public PlayerColor GetColor(){
        return m_color;
    }

    /**
     * Return the number of points that this player has.
     * @return An int holding the number of points that this player has.
     */
    public int GetPoints(){
        return m_points;
    }

    /**
     * Called before the Play() function. Used by the computer AI to decide on a move.
     * @param move A Move object describing the move we may play.
     * @param board A Board object holding the board we will use to pick a move.
     * @return A Move object holding the move that we have decided to play.
     */
    public Move PrePlay(Move move, Board board) {
        // Find the best move to play on this board.
        Move pickedMove = FindBestMove(board);

        // If the user wants to quit but the computer has found a good move, they will refuse to quit.
        if (move != null && move.GetAction() == Action.QUIT && pickedMove.GetAction() != Action.QUIT){
            return new Move(Action.NO_QUIT);
        }
        else{
            return pickedMove;
        }
    }

    /**
     * Execute a move given the move and the Board on which to make the move.
     * @param move The Move object we describing the move we want to make.
     * @param board The Board object holding the current board on which to make the move.
     * @return A MoveError enum describing the error that occurred while trying to make the move.
     */
    public MoveError Play(Move move, Board board){
        // The computer doesn't want to quit, tell the user to keep playing.
        if (move != null && move.GetAction() == Action.NO_QUIT){
            return MoveError.NO_QUIT;
        }

        // Execute the move.
        MoveResult result = board.MakeMove(move, m_color);
        m_points += result.GetPoints();
        return result.GetError();
    }

    /**
     * Finds the best move available for this play based on the computer AI system.
     * @param board The Board object holding the current board on which to find the best move.
     * @return A Move object describing the best move the AI could find for the given board.
     */
    public Move FindBestMove(Board board){
        // A vector which will store the best move for each piece on the board.
        Vector<Move> moves = new Vector<>();

        // Loop through each board location.
        for (int i = 1; i <= board.GetSize(); i++){
            for (int j = 1; j <= board.GetSize(); j++){
                Point curPt = new Point(i, j);
                // If we have a piece at this location, find the best move it can make.
                if (board.GetOccupant(curPt).GetColor() == m_color){
                    // If the piece is going to be captured, avoid as the first priority.
                    if (CanBeCaptured(board, curPt)){
                        moves.add(EscapeCapture(board, curPt));
                        continue;
                    }
                    // Try to block the opponent from reaching a home location. If this is possible,
                    // a valid move will be returned. If not, blocking isn't an option for this move.
                    Move tmp = BlockOpponent(board, curPt);
                    if (tmp != null){
                        moves.add(tmp);
                        continue;
                    }
                    // Capture a neighboring opponent if possible.
                    tmp = CaptureOpponent(board, curPt);
                    if (tmp != null){
                        moves.add(tmp);
                        continue;
                    }
                    // If nothing above works, we will advance towards a home location, assuming we
                    // can leave our home location without it being captured.
                    if (!ShouldStayBlocking(board, curPt)){
                        moves.add(MoveTowardsHomeLocation(board, curPt));
                    }
                }
            }
        }
        // Remove any null moves from the list that may have ended up there.
        for (int i = 0; i < moves.size(); i++){
            if (moves.elementAt(i) == null){
                moves.remove(i);
                i -= 1;
            }
        }
        // We will rank the moves based on their importance.
        int highestWeight = -1;
        for (Move curMove : moves){
            if (curMove.GetReason().GetWeight() > highestWeight){
                highestWeight = curMove.GetReason().GetWeight();
            }
        }
        // Remove all but the highest ranked moves.
        for (int i = 0; i < moves.size(); i++) {
            if (moves.elementAt(i).GetReason().GetWeight() < highestWeight) {
                moves.remove(i);
                i -= 1;
            }
        }
        // If there were no good moves left to play, return a random move.
        if (moves.isEmpty()){
            // Loop through each board location, and pick a random move for each.
            for (int i = 1; i <= board.GetSize(); i++) {
                for (int j = 1; j <= board.GetSize(); j++) {
                    Point curPt = new Point(i, j);
                    // If we have a piece at this location, find the best move it can make.
                    if (board.GetOccupant(curPt).GetColor() == m_color) {
                        moves.add(MoveToEmptySpace(board, curPt));
                    }
                }
            }
            // Remove any null moves from the list that may have ended up there.
            for (int i = 0; i < moves.size(); i++){
                if (moves.elementAt(i) == null){
                    moves.remove(i);
                    i -= 1;
                }
            }
            // If there still isn't anything left to play, return a move to quit.
            if (moves.isEmpty()){
                return new Move(Action.QUIT);
            }
        }
        // Return a random move from the remaining moves list.
        Random rand = new Random();
        int num = rand.nextInt(moves.size());
        return moves.elementAt(num);
    }

    /**
     * Decides if the given point is in danger of being captured by an opponent.
     * @param board The Board object holding the current board.
     * @param start The Point in question on the board.
     * @return A boolean value. True if the piece at the given point is in danger of being captured. False if not.
     */
    private boolean CanBeCaptured(Board board, Point start){
        // If we can capture ourselves, we don't need to worry about being captured.
        if (board.GetOccupant(start).CanCapture()){
            return false;
        }

        Vector<SurroundingPoint> points = GetSurroundingPoints(board, start);
        for (SurroundingPoint point : points){
            // If any surrounding points can capture this piece, return true;
            if (board.GetOccupant(point.m_endPoint).CanCapture() && board.GetOccupantColor(point.m_endPoint) != m_color){
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a move to a location which is safe from capture, or a move to the nearest home location if this isn't possible.
     * @param board The Board object we are moving on.
     * @param start The Point we want to move on the board.
     * @return A Move object describing a move to a safe location, or to the nearest home point if this isn't possible.
     */
    private Move EscapeCapture(Board board, Point start){
        Vector<SurroundingPoint> points = GetSurroundingPoints(board, start);
        for (SurroundingPoint point : points){
            // If a neighboring cell is empty and out of capture risk, move there.
            if (board.GetOccupantColor(point.m_endPoint) == null && !CanBeCaptured(board, point.m_endPoint)){
                return new Move(start, point.m_dir, Action.PLAY, ActionReason.ESCAPE, point.m_endPoint);
            }
        }
        return MoveTowardsHomeLocation(board, start);
    }

    /**
     * Decides if the piece should stay put to continue blocking a home location.
     * @param board The Board object we are moving on.
     * @param start The Point in question on the board.
     * @return A boolean value. True if we should stay put. False if it is safe to move from our current location.
     */
    private boolean ShouldStayBlocking(Board board, Point start){
        // Make sure we are even in a home location.
        if (board.GetOwner(start) != m_color){
            return false;
        }
        // If there is an opponent neighboring us and we are in our home location, stay put.
        Vector<SurroundingPoint> points = GetSurroundingPoints(board, start);
        for (SurroundingPoint point : points){
            if (board.GetOccupantColor(point.m_endPoint) != null && board.GetOccupantColor(point.m_endPoint) != m_color){
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a valid move to block an opponent if possible.
     * @param board The Board object we are playing on.
     * @param start The Point we want to move on the board.
     * @return A valid Move object to block if possible. Null otherwise.
     */
    private Move BlockOpponent(Board board, Point start){
        // Get a list of all surrounding points. If any are our home locations, and empty, check to see
        // if there are any opponents around it that will try to capture.
        Vector<SurroundingPoint> points = GetSurroundingPoints(board, start);
        for (SurroundingPoint point : points){
            Piece curOcc = board.GetOccupant(point.m_endPoint);
            // If we have an empty home location around us, make sure nobody is going to try and capture it.
            if (curOcc.GetColor() == null && board.GetOwner(point.m_endPoint) == m_color){
                // Find any potential opponents around the empty home location.
                Vector<SurroundingPoint> aroundHome = GetSurroundingPoints(board, point.m_endPoint);
                for (SurroundingPoint possibleEnemy : aroundHome){
                    // If there is an opponent that can be blocked, move to the home location.
                    Piece enemy = board.GetOccupant(possibleEnemy.m_endPoint);
                    if (enemy.GetColor() != null && enemy.GetColor() != m_color && !enemy.CanCapture()){
                        return new Move(start, point.m_dir, Action.PLAY, ActionReason.BLOCK, possibleEnemy.m_endPoint);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Finds a valid move to capture an opponent if possible.
     * @param board The Board object we are playing on.
     * @param start The Point we want to move on the board.
     * @return A valid Move to capture an opponent if possible. Null otherwise.
     */
    private Move CaptureOpponent(Board board, Point start){
        // Make sure we can even capture in the first place.
        if (!board.GetOccupant(start).CanCapture()){
            return null;
        }
        Vector<SurroundingPoint> points = GetSurroundingPoints(board, start);
        for (SurroundingPoint point : points){
            Piece curOcc = board.GetOccupant(point.m_endPoint);
            // See if there are any opponent pieces around that we can capture.
            if (curOcc.GetColor() != null && curOcc.GetColor() != m_color){
                // Don't capture if we are going to be captured next move.
                if (!CanBeCaptured(board, point.m_endPoint)){
                    return new Move(start, point.m_dir, Action.PLAY, ActionReason.CAPTURE, point.m_endPoint);
                }
            }
        }
        return null;
    }

    /**
     * Attempts to move the piece at the given start location towards an opponent's home location.
     * @param board The Board object we are playing on.
     * @param start The Point we want to move on the board.
     * @return A valid Move towards an opponent's home location if possible. A move to an empty space if not.
     */
    private Move MoveTowardsHomeLocation(Board board, Point start){
        // Make sure we aren't already at a home location.
        PlayerColor startColor = board.GetOccupantColor(start);
        PlayerColor ownerColor = board.GetOwner(start);
        if (startColor != null && ownerColor != null && startColor != ownerColor){
            return null;
        }
        // Loop through every cell on the board to find the home locations.
        for (int i = 1; i <= board.GetSize(); i++){
            for (int j = 1; j <= board.GetSize(); j++){
                Point curPt = new Point(i, j);

                // If this point is owned by the opponent, and we aren't already there, lets try to move to it.
                if (board.GetOwner(curPt) == PlayerColor.Opponent(m_color) && board.GetOccupantColor(curPt) != m_color){
                    // First, see if it is possible to reach the point.
                    if (CanReach(start, curPt)){
                        // If we can reach the point, return a move towards it.
                        Move move = MoveTowardsPoint(board, start, curPt);
                        if (move != null){
                            return new Move(move.GetLocation(), move.GetDirection(), Action.PLAY, ActionReason.ADVANCE, curPt);
                        }
                    }
                }
            }
        }
        // If we can't get to a home location, return a move to a random board location.
        return MoveToEmptySpace(board, start);
    }

    /**
     * Moves the piece to any available free space. Used as a last resort.
     * @param board The Board object we are playing on.
     * @param start The Point we want to move on the board.
     * @return A valid Move to an empty space around the given point. Null if this is not possible.
     */
    private Move MoveToEmptySpace(Board board, Point start){
        Vector<SurroundingPoint> points = GetSurroundingPoints(board, start);
        for (SurroundingPoint point : points){
            if (board.GetOccupant(point.m_endPoint).GetColor() == null){
                return new Move(start, point.m_dir, Action.PLAY, ActionReason.RANDOM);
            }
        }
        return null;
    }

    /**
     *  Finds a move advancing the start point towards the end point.
     * @param board The Board object we are playing on.
     * @param start The Point we want to move on the board.
     * @param end The Point on the board that we want to reach.
     * @return A valid Move towards the end point from the start point. Null if this is not possible.
     */
    private Move MoveTowardsPoint(Board board, Point start, Point end){
        int initHorDist = Math.abs(start.y - end.y);
        int initVertDist = Math.abs(start.x - end.x);

        Vector<SurroundingPoint> points = GetSurroundingPoints(board, start);
        for (SurroundingPoint point : points){
            // Make sure we wont get captured by moving here.
            if (CanBeCaptured(board, point.m_endPoint)){
                continue;
            }

            int curHorDist = Math.abs(point.m_endPoint.y - end.y);
            int curVertDist = Math.abs(point.m_endPoint.x - end.x);

            // Figure out how to move towards the point we want to. This differs depending on if
            // we need to move horizontally or vertically.
            Move curMove = null;
            if (initHorDist <= initVertDist){
                if (curVertDist < initVertDist && curHorDist <= curVertDist){
                    curMove = new Move(start, point.m_dir, Action.PLAY, null);
                }
            }
            else {
                if (curHorDist < initHorDist && curVertDist <= curHorDist){
                    curMove = new Move(start, point.m_dir, Action.PLAY, null);
                }
            }

            // If nothing is in our way, return this move.
            if (curMove != null){
                Piece startOcc = board.GetOccupant(start);
                Piece endOcc = board.GetOccupant(point.m_endPoint);

                if (endOcc.GetColor() == null){
                    return curMove;
                }
                else if (endOcc.GetColor() != startOcc.GetColor() && startOcc.CanCapture()){
                    return curMove;
                }
                else {
                    continue;
                }
            }
        }
        return null;
    }

    /**
     * Figures out if the start point can reach the end point, since players can only move diagonally.
     * @param start The starting Point object in question.
     * @param end The ending Point object in question.
     * @return A boolean value. True if the start point can reach the end point. False if not.
     */
    private boolean CanReach(Point start, Point end){
        int pt1 = start.x + start.y;
        int pt2 = end.x + end.y;

        // If the x and y sum of each point are both even or both odd, the end point can be reached
        // moving diagonally.
        if (pt1 % 2 == 0 && pt2 % 2 == 0){
            return true;
        }
        else if (pt1 % 2 != 0 && pt2 % 2 != 0){
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Gets a vector of the surrounding Points to a given starting point, with their respective directions.
     * @param board The Board object we are playing on.
     * @param start The Point on the board that we are finding neighbors for.
     * @return A Vector of SurroundingPoint objects describing the neighbors around the given point.
     */
    private Vector<SurroundingPoint> GetSurroundingPoints(Board board, Point start){
        Vector<SurroundingPoint> points = new Vector<>();

        // Loop through the points around the given point.
        for (int i = -1; i <= 1; i++){
            for (int j = -1; j <= 1; j++){
                if (i == 0 || j == 0){
                    // Skip non-diagonal moves.
                    continue;
                }
                // Decide which direction each move is.
                MoveDirection dir;
                if (i == -1 && j == -1){
                    dir = MoveDirection.NW;
                }
                else if (i == -1 && j == 1){
                    dir = MoveDirection.NE;
                }
                else if (i == 1 && j == 1){
                    dir = MoveDirection.SE;
                }
                else {
                    dir = MoveDirection.SW;
                }

                int newRow = start.x + i;
                int newCol = start.y + j;
                Point mvPt = new Point(newRow, newCol);
                if (board.IsValidLocation(mvPt)){
                    points.add(new SurroundingPoint(mvPt, dir));
                }
            }
        }
        return points;
    }
}
