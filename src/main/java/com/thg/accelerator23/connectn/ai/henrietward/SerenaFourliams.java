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
    int blockingCol = blockingMove(board);

    if (blockingCol != -1) {
      return blockingCol;
    }

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

  private int blockingMove(Board board) {
    Counter opponent = this.getCounter().getOther();
    Counter[][] placements = board.getCounterPlacements();

    for (int row = 0; row < 8; row++) {
      for (int col = 0; col <= 6; col++) {
        Counter[] window = {
                placements[col][row],
                placements[col+1][row],
                placements[col+2][row],
                placements[col+3][row]
        };
        if (isThreeInARow(window, opponent)) {
          return findBlockingColumn(board, col, row, "horizontal", placements);
        }
      }
    }

    for (int col = 0; col < 10; col++) {
      for (int row = 0; row <= 4; row++) {
        Counter[] window = {
                placements[col][row],
                placements[col][row+1],
                placements[col][row+2],
                placements[col][row+3]
        };
        if (isThreeInARow(window, opponent)) {
          return findBlockingColumn(board, col, row, "vertical", placements);
        }
      }
    }

    for (int row = 0; row <= 4 ; row++) {
      for (int col = 0; col <= 6; col++) {
        Counter[] window = {
                placements[col][row],
                placements[col + 1][row + 1],
                placements[col + 2][row + 2],
                placements[col + 3][row + 3],
        };

        if (isThreeInARow(window, opponent)) {
          return findBlockingColumn(board, col, row, "diagonal", placements);
        }
      }
    }

    for (int row = 3; row < 8; row++) {
      for (int col = 0; col <= 6; col++) {
        Counter[] window = {
                placements[col][row],
                placements[col + 1][row - 1],
                placements[col + 2][row - 2],
                placements[col + 3][row - 3],
        };

        if (isThreeInARow(window, opponent)) {
          return findBlockingColumn(board, col, row, "diagonal", placements);
        }
      }
    }
    return -1;
  }

  private boolean isThreeInARow(Counter[] window, Counter opponent) {
    int count = 0;
    int empty = 0;
    for (Counter c : window) {
      if (c == opponent) count++;
      if (c == null) empty++;
    }
    return count == 3 && empty == 1;
  }

  private int findBlockingColumn(Board board, int startCol, int startRow, String direction, Counter[][] placements) {
    // Find the empty position that needs blocking
    // Return the column number if it's a valid move
    switch(direction) {
      case "horizontal":
        for (int i = 0; i < 4; i++) {
          if (placements[startCol + i][startRow] == null) {
            if (isValidMove(board,startCol + i)) {
              return startCol + i;
            }
          }
        }
        break;
      case "vertical":
        if (placements[startCol][startRow + 3] == null) {
          if (isValidMove(board, startCol)) {
            return startCol;
          }
        }
        break;
      case "diagonal":
        for (int i = 0; i < 4; i++) {
          if (placements[startCol + i][startRow+i] == null) {
            if (isValidMove(board,startCol + i)) {
              return startCol + i;
            }
          }
        }
        break;
      case "neg_diagonal":
        for (int i = 0; i < 4; i++) {
          if (placements[startCol + i][startRow-i] == null) {
            if (isValidMove(board,startCol + i)) {
              return startCol + i;
            }
          }
        }
        break;
    }
    return -1;
  }

  private boolean isValidMove(Board board, int col) {
    // Check if the column is within bounds and not full
    return col >= 0 && col < board.getConfig().getWidth() &&
            isColumnPlayable(board, col);
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

    int score = 0;

    Counter[][] placements = board.getCounterPlacements();

    score += evaluateRows(placements, counter);
    score += evaluateColumns(placements, counter);
    score += evaluateDiagonals(placements, counter);

    return score;
  }

  private int evaluateRows(Counter[][] placements, Counter counter) {
    int score = 0;

    for (int row = 0; row < 8; row++) {
      for (int col = 0; col <= 6 ; col++)
      {Counter[] window = {
              placements[col][row],
              placements[col+1][row],
              placements[col+2][row],
              placements[col+3][row],
      };

        score += evaluateWindow(window, counter);
      }
    }
    return score;
  }

  private int evaluateColumns(Counter[][] placements, Counter counter) {
    int score = 0;

    for (int col = 0; col < 10; col++) {
      for (int row = 0; row <= 4 ; row++)
      {Counter[] window = {
              placements[col][row],
              placements[col][row+1],
              placements[col][row+2],
              placements[col][row+3],
      };

        score += evaluateWindow(window, counter);
      }
    }
    return score;
  }

  private int evaluateDiagonals(Counter[][] placements, Counter counter) {
    int score = 0;

    for (int row = 0; row <= 4 ; row++) {
      for (int col = 0; col <= 6 ; col++)
      {Counter[] window = {
              placements[col][row],
              placements[col+1][row+1],
              placements[col+2][row+2],
              placements[col+3][row+3],
      };

        score += evaluateWindow(window, counter);
      }
    }
    return score;
  }

  private int evaluateNegDiagonals(Counter[][] placements, Counter counter) {
    int score = 0;

    for (int row = 3; row < 8; row++) {
      for (int col = 0; col <= 6; col++) {
        Counter[] window = {
                placements[col][row],
                placements[col + 1][row - 1],
                placements[col + 2][row - 2],
                placements[col + 3][row - 3],
        };
        score += evaluateWindow(window, counter);
      }
    }

    return score;
  }

  private int evaluateWindow(Counter [] window, Counter counter) {
    int score = 0;
    int countSelf = 0;
    int countOpponent = 0;

    for (Counter c : window) {
      if (c == counter) countSelf++;
      else if (c == counter.getOther()) countOpponent++;
    }

    if (countSelf == 4) score += 100;
    else if (countSelf == 3 && countOpponent == 0) score += 50;
    else if (countSelf == 2 && countOpponent == 0) score += 10;
    else if (countOpponent == 3 && countSelf == 0) score -= 70;
    else if (countOpponent == 4) score -= 100;

    return score;
  }



  private boolean isGameOver(Board board) {
    // Implement logic to check if the game is over (win/loss/draw)
    return false; // Placeholder
  }

}










