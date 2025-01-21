package com.thg.accelerator23.connectn.ai.henrietward;

import com.thehutgroup.accelerator.connectn.player.Board;
import com.thehutgroup.accelerator.connectn.player.Counter;
import com.thehutgroup.accelerator.connectn.player.Player;
import com.thehutgroup.accelerator.connectn.player.InvalidMoveException;


public class SerenaFourliams extends Player {
  private int maxDepth;

  public SerenaFourliams(Counter counter, String name, int maxDepth ) {
    //TODO: fill in your name here
    super(counter, SerenaFourliams.class.getName());
    this.maxDepth = maxDepth;
  }

  @Override
  public int makeMove(Board board) {
    int bestMove = -1;
    int bestValue = Integer.MIN_VALUE;
    Counter counter = this.getCounter();
    int alpha = Integer.MIN_VALUE;
    int beta = Integer.MAX_VALUE;

    for (int col = 0; col < board.getConfig().getWidth(); col++) {
      if (isColumnPlayable(board, col)) {
        try {
          Board newBoard = new Board(board, col, counter);
          int moveValue = minimax(newBoard, maxDepth - 1, alpha, beta, false, counter.getOther());
          if (moveValue > bestValue) {
            bestValue = moveValue;
            bestMove = col;
          }
          alpha = Math.max(alpha, bestValue);
          if (beta <= alpha) {
            break; // Beta cut-off
          }
        } catch (InvalidMoveException e) {
          // This should not occur since we checked isColumnPlayable
          e.printStackTrace();
        }
      }
    }
    return bestMove;
  }

  private int minimax(Board board, int depth, int alpha, int beta, boolean isMaximizingPlayer, Counter counter) {

    if (depth == 0 || isGameOver(board)) {
      return evaluateBoard(board, counter);
    }

    int bestValue = isMaximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

    for (int col = 0; col < board.getConfig().getWidth(); col++) {
      if (isColumnPlayable(board, col)) {
        try {
          Board newBoard = new Board(board, col, counter);
          int value = minimax(newBoard, depth - 1, alpha, beta, !isMaximizingPlayer, counter.getOther());

          if (isMaximizingPlayer) {
            bestValue = Math.max(bestValue, value);
            alpha = Math.max(alpha, value);
          } else {
            bestValue = Math.min(bestValue, value);
            beta = Math.min(beta, value);
          }

          if (beta <= alpha) {
            break; // Alpha-Beta Pruning
          }
        } catch (InvalidMoveException e) {
          // Shouldn't happen
          e.printStackTrace();
        }
      }
    }
    return bestValue;
  }

  private boolean isColumnPlayable(Board board, int col) {
    // Check if the top cell of the column is vacant
    return board.getCounterPlacements()[col][board.getConfig().getHeight() - 1] == null;
  }


  private int evaluateBoard(Board board, Counter counter) {
    // Implement evaluation logic to score the board for the given counter
    return 0; // Placeholder
  }

  private boolean isGameOver(Board board) {
    // Implement logic to check if the game is over (win/loss/draw)
    return false; // Placeholder
  }

}










