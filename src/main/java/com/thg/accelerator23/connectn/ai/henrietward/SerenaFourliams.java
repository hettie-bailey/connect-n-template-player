package com.thg.accelerator23.connectn.ai.henrietward;

import com.thehutgroup.accelerator.connectn.player.*;
import java.util.List;
import java.util.ArrayList;


public class SerenaFourliams extends Player {
  private int maxDepth;

  public SerenaFourliams(Counter counter, String name, int maxDepth ) {
    //TODO: fill in your name here
    super(counter, SerenaFourliams.class.getName());
    this.maxDepth = maxDepth;
  }

  @Override
  public int makeMove(Board board) {
    // Convert the board to bitboard representation
    long playerBoard = convertToBitboard(board, this.getCounter());
    long opponentBoard = convertToBitboard(board, this.getCounter().getOther());

    int blockingMove = findBlockingMove(playerBoard, opponentBoard);
    if (blockingMove != -1) {
      return blockingMove;
    }

    // Delegate to the bitboard-based makeMove method
    return makeMove(board, playerBoard, opponentBoard);
  }

  private long convertToBitboard(Board board, Counter counter) {
    long bitboard = 0L;
    int width = board.getConfig().getWidth();  // 10
    int height = board.getConfig().getHeight(); // 8

    for (int col = 0; col < width; col++) {
      for (int row = 0; row < height; row++) {
        Position position = new Position(col, row);
        Counter currentCounter = board.getCounterAtPosition(position);
        if (currentCounter == counter) {
          bitboard |= 1L << (col * 8 + row); // Shift by 8 rows per column
        }
      }
    }
    return bitboard;
  }

  private int findBlockingMove(long playerBoard, long opponentBoard) {
    for (int col = 0; col < 10; col++) {
      if (isColumnPlayable(playerBoard, opponentBoard, col)) {
        // Simulate the opponent making a move in this column
        long newOpponentBoard = applyMove(opponentBoard, col);

        System.out.println("Simulating opponent move in column: " + col);
        System.out.println("New opponent board: " + Long.toBinaryString(newOpponentBoard));

        // Check if this move results in a win for the opponent
        if (hasWon(newOpponentBoard)) {
          System.out.println("Blocking opponent win at column: " + col);
          return col; // Block this column
        }
      }
    }
    return -1; // No immediate threat found
  }

  public int makeMove(Board board, long playerBoard, long opponentBoard) {
    int bestMove = -1;
    int bestValue = Integer.MIN_VALUE;
    int alpha = Integer.MIN_VALUE;
    int beta = Integer.MAX_VALUE;

    int[] columnOrder = {4, 3, 5, 2, 6, 1, 7, 0, 8, 9};

    for (int col : columnOrder) {
      if (isColumnPlayable(playerBoard, opponentBoard, col)) {
        long newPlayerBoard = applyMove(playerBoard, col);

        // Use minimax with the bitboard representation
        int moveValue = minimax(newPlayerBoard, opponentBoard, maxDepth - 1, alpha, beta, false);

        if (moveValue > bestValue) {
          bestValue = moveValue;
          bestMove = col;
        }

        alpha = Math.max(alpha, bestValue);
        if (beta <= alpha) {
          break; // Beta cut-off
        }
      }
    }
    return bestMove;
  }




  private int minimax(long playerBoard, long opponentBoard, int depth, int alpha, int beta, boolean isMaximizingPlayer) {

    if (depth == 0 || isGameOver(playerBoard, opponentBoard)) {
      return evaluateBoard(playerBoard, opponentBoard);
    }

    int bestValue = isMaximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

    int[] columnOrder = {4, 3, 5, 2, 6, 1, 7, 0, 8, 9}; //changing priority search to emphasize the center position

    for (int col : columnOrder) {
      if (isColumnPlayable(playerBoard, opponentBoard, col)) {
        long newPlayerBoard, newOpponentBoard;

        if (isMaximizingPlayer) {
          newPlayerBoard = applyMove(playerBoard, col);
          newOpponentBoard = opponentBoard;
        } else {
          newPlayerBoard = playerBoard;
          newOpponentBoard = applyMove(opponentBoard, col);
        }

        int value = minimax(
                newPlayerBoard, newOpponentBoard, depth - 1, alpha, beta, !isMaximizingPlayer
        );

        System.out.println("Column: " + col + ", Score: " + value);

        if (isMaximizingPlayer) {
          bestValue = Math.max(bestValue, value);
          alpha = Math.max(alpha, value);
        } else {
          bestValue = Math.min(bestValue, value);
          beta = Math.min(beta, value);
        }
        if (beta <= alpha) {
          break;
        }
      }
    }
    return bestValue;
  }

  private boolean isColumnPlayable(long playerBoard, long opponentBoard, int col) {
    int height = 8;
    long columnMask = getColumnMask(col); // Full column mask
    long topmostCellMask = 1L << (col * height); // Mask for the topmost cell in the column

    // Check if the topmost cell in the column is empty (both player and opponent should not occupy it)
    return (playerBoard & topmostCellMask) == 0 && (opponentBoard & topmostCellMask) == 0;
  }


  private long applyMove(long board, int col) {
    long columnMask = getColumnMask(col);
    long lowestEmptyRow = (~board) & columnMask; // Find empty spots in the column
    if (lowestEmptyRow == 0) {
      throw new IllegalStateException("Column is full: " + col);
    }
    long movePosition = Long.lowestOneBit(lowestEmptyRow); // Get the lowest empty bit
    return board | movePosition;
  }

  private long getColumnMask(int col) {
    int height = 8; // Correct height
    return (1L << height) - 1 << (col * height);
  }

  private int evaluateBoard(long playerBoard, long opponentBoard) {
    int score = 0;

    // Central control: prioritize central columns
    score += centralControl(playerBoard);

    // Evaluate player and opponent positions
    score += evaluatePosition(playerBoard);
    score -= evaluatePosition(opponentBoard); // Opponent's positions lower the score

    return score;
  }

  private int evaluatePosition(long board) {
    int score = 0;
    int width = 10;
    int height = 8;

    System.out.println("Evaluating board: " + Long.toBinaryString(board));

    // Winning patterns for Connect 4 (all possible 4-in-a-row patterns)
    long[] patterns = generateWinningPatterns(width, height);

    for (long pattern : patterns) {
      long matchingPieces = board & pattern;

      // Count how many bits are set in matching pieces
      int count = Long.bitCount(matchingPieces);

      if (count == 4) {
        score += 100000; // Winning line
      } else if (count == 3) {
        if ((matchingPieces & ~board) != 0) {
          score += 1000; // Open 3
        }
      } else if (count == 2) {
        if ((matchingPieces & ~board) != 0) {
          score += 10;
        }
      }
    }

    System.out.println("Score for position: " + score);
    return score;
  }

  // Central column control adds a bonus
  private int centralControl(long board) {
    long centerMask = (0b10000L | 0b100000L) << (4 * 8); // Center two columns
    return Long.bitCount(board & centerMask) * 20;
  }

  // Generate all winning patterns (4 in a row horizontally, vertically, diagonally)
  private long[] generateWinningPatterns(int width, int height) {
    List<Long> patterns = new ArrayList<>();

    // Horizontal
    for (int row = 0; row < height; row++) {
      for (int col = 0; col <= width - 4; col++) { // Allow patterns to fully fit
        long pattern = 0b1111L << (row * width + col);
        patterns.add(pattern);
      }
    }

    // Vertical
    for (int col = 0; col < width ; col++) {
      for (int row = 0; row < height - 4; row++) {
        long pattern = 0b1L | (0b1L << 10) | (0b1L << 20) | (0b1L << 30);
        pattern <<= (row * 10 + col);
        patterns.add(pattern);
      }
    }

    // Diagonal /
    for (int row = 0; row <= height - 4; row++) {
      for (int col = 0; col <= width - 4; col++) {
        long pattern = 0b1L | (0b1L << 11) | (0b1L << 22) | (0b1L << 33);
        pattern <<= (row * 10 + col);
        patterns.add(pattern);
      }
    }

    // Diagonal \
    for (int row = 3; row < height; row++) {
      for (int col = 0; col <= width - 4; col++) {
        long pattern = 0b1L | (0b1L << 9) | (0b1L << 18) | (0b1L << 27); // Diagonal spacing
        pattern <<= ((row * width) + col );
        patterns.add(pattern);
      }
    }

    long[] patternArray = new long[patterns.size()];
    for (int i = 0; i < patterns.size(); i++) {
      patternArray[i] = patterns.get(i);
    }

    return patternArray;
  }


  private boolean isGameOver(long playerBoard, long opponentBoard) {
    // Check if either player has a winning position
    return hasWon(playerBoard) || hasWon(opponentBoard);
  }

  private boolean hasWon(long board) {
    int width = 10;
    int height = 8;
    long[] patterns = generateWinningPatterns(width, height);
    for (long pattern : patterns) {
      if ((board & pattern) == pattern) {
        return true; // Found a winning line
      }
    }
    return false;
  }

}











