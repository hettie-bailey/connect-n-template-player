package com.thg.accelerator23.connectn.ai.henrietward;
import java.math.BigInteger;
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
    BigInteger playerBoard = convertToBitboard(board, this.getCounter());
    BigInteger opponentBoard = convertToBitboard(board, this.getCounter().getOther());

    int blockingMove = findBlockingMove(playerBoard, opponentBoard);
    if (blockingMove != -1) {
      return blockingMove;
    }

    return makeMoveWithMinimax(playerBoard, opponentBoard);
  }

  private BigInteger convertToBitboard(Board board, Counter counter) {
    BigInteger bitboard = BigInteger.ZERO;
    int width = board.getConfig().getWidth();
    int height = board.getConfig().getHeight();

    for (int col = 0; col < width; col++) {
      for (int row = 0; row < height; row++) {
        Position position = new Position(col, row);
        Counter currentCounter = board.getCounterAtPosition(position);
        if (currentCounter == counter) {
          bitboard = bitboard.setBit(col * height + (height - 1 - row));
        }
      }
    }
    return bitboard;
  }

  private int findBlockingMove(BigInteger playerBoard, BigInteger opponentBoard) {
    for (int col = 0; col < 10; col++) {
      if (isColumnPlayable(playerBoard, opponentBoard, col)) {
        BigInteger newOpponentBoard = applyMove(opponentBoard, col);
        if (hasWon(newOpponentBoard)) {
          return col;
        }
      }
    }
    return -1;
  }

  private int makeMoveWithMinimax(BigInteger playerBoard, BigInteger opponentBoard) {
    int bestMove = -1;
    int bestValue = Integer.MIN_VALUE;
    int alpha = Integer.MIN_VALUE;
    int beta = Integer.MAX_VALUE;

    int[] columnOrder = {4, 3, 5, 2, 6, 1, 7, 0, 8, 9};

    for (int col : columnOrder) {
      if (isColumnPlayable(playerBoard, opponentBoard, col)) {
        BigInteger newPlayerBoard = applyMove(playerBoard, col);
        int moveValue = minimax(newPlayerBoard, opponentBoard, maxDepth - 1, alpha, beta, false);

        if (moveValue > bestValue) {
          bestValue = moveValue;
          bestMove = col;
        }

        alpha = Math.max(alpha, bestValue);
        if (beta <= alpha) {
          break; // Prune
        }
      }
    }
    return bestMove;
  }

  private int minimax(BigInteger playerBoard, BigInteger opponentBoard, int depth, int alpha, int beta, boolean isMaximizing) {
    if (depth == 0 || hasWon(playerBoard) || hasWon(opponentBoard)) {
      return evaluateBoard(playerBoard, opponentBoard);
    }

    int bestValue = isMaximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;

    for (int col = 0; col < 10; col++) {
      if (isColumnPlayable(playerBoard, opponentBoard, col)) {
        BigInteger newPlayerBoard = isMaximizing ? applyMove(playerBoard, col) : playerBoard;
        BigInteger newOpponentBoard = isMaximizing ? opponentBoard : applyMove(opponentBoard, col);

        int value = minimax(newPlayerBoard, newOpponentBoard, depth - 1, alpha, beta, !isMaximizing);

        if (isMaximizing) {
          bestValue = Math.max(bestValue, value);
          alpha = Math.max(alpha, value);
        } else {
          bestValue = Math.min(bestValue, value);
          beta = Math.min(beta, value);
        }

        if (beta <= alpha) {
          break; // Prune
        }
      }
    }

    return bestValue;
  }

  private boolean isColumnPlayable(BigInteger playerBoard, BigInteger opponentBoard, int col) {
    BigInteger topmostCellMask = BigInteger.ONE.shiftLeft(col * 8 + 7);
    return playerBoard.and(topmostCellMask).equals(BigInteger.ZERO) &&
            opponentBoard.and(topmostCellMask).equals(BigInteger.ZERO);
  }

  private BigInteger applyMove(BigInteger board, int col) {
    BigInteger columnMask = getColumnMask(col);
    BigInteger lowestEmptyRow = columnMask.andNot(board);
    BigInteger movePosition = lowestEmptyRow.and(lowestEmptyRow.negate());
    return board.or(movePosition);
  }

  private BigInteger getColumnMask(int col) {
    int height = 8;
    return BigInteger.ONE.shiftLeft(height).subtract(BigInteger.ONE).shiftLeft(col * height);
  }

  private int evaluateBoard(BigInteger playerBoard, BigInteger opponentBoard) {
    int score = 0;

    // Central control: prioritize central columns
    score += centralControl(playerBoard);

    // Evaluate player and opponent positions
    score += evaluatePosition(playerBoard);
    score -= evaluatePosition(opponentBoard); // Opponent's positions lower the score

    return score;
  }

  private int evaluatePosition(BigInteger board) {
    int score = 0;

    List<BigInteger> winningPatterns = generateWinningPatterns(10, 8); // assuming 10x8 board size
    for (BigInteger pattern : winningPatterns) {
      BigInteger matchingPieces = board.and(pattern); // intersection of board and the winning pattern

      int count = matchingPieces.bitCount();  // Count how many bits are set
      // A 4-in-a-row is a winning condition
      if (count == 4) {
        score += 100000;  // A winning move is worth a very high score
      }
      // A 3-in-a-row with an empty spot (open 3)
      else if (count == 3 && (matchingPieces.and(pattern.not())).signum() > 0) {
        score += 1000;   // Reward open 3-in-a-row patterns
      }
      // A 2-in-a-row with an empty spot (open 2)
      else if (count == 2 && (matchingPieces.and(pattern.not())).signum() > 0) {
        score += 100;    // Reward open 2-in-a-row patterns
      }
    }

    return score;
  }

  // Central column control adds a bonus
  private int centralControl(BigInteger board) {
    int score = 0;
    int width = 10; // Number of columns
    int height = 8; // Number of rows

    // Central columns in a 10x8 grid are columns 4 and 5
    for (int row = 0; row < height; row++) {
      BigInteger rowMask = BigInteger.ZERO;

      // Set bits for columns 4 and 5 in the current row
      rowMask = rowMask.setBit(row * width + 4);  // Column 4
      rowMask = rowMask.setBit(row * width + 5);  // Column 5

      // Count how many bits are set in the intersection of rowMask and board (central control)
      score += Long.bitCount(board.and(rowMask).longValue()); // Use Long.bitCount on the intersection
    }

    return score * 20;  // Arbitrary value to scale the score for central control
  }

  // Generate all winning patterns (4 in a row horizontally, vertically, diagonally)
  private List<BigInteger> generateWinningPatterns(int width, int height) {
    List<BigInteger> patterns = new ArrayList<>();

    // Horizontal patterns
    for (int row = 0; row < height; row++) {
      for (int col = 0; col <= width - 4; col++) {
        BigInteger pattern = BigInteger.ZERO;
        for (int offset = 0; offset < 4; offset++) {
          pattern = pattern.setBit(row * width + col + offset);
        }
        patterns.add(pattern);
      }
    }

    // Vertical patterns
    for (int col = 0; col < width; col++) {
      for (int row = 0; row <= height - 4; row++) {
        BigInteger pattern = BigInteger.ZERO;
        for (int offset = 0; offset < 4; offset++) {
          pattern = pattern.setBit((row + offset) * width + col);
        }
        patterns.add(pattern);
      }
    }

    // Diagonal patterns (bottom-left to top-right)
    for (int row = 0; row <= height - 4; row++) {
      for (int col = 0; col <= width - 4; col++) {
        BigInteger pattern = BigInteger.ZERO;
        for (int offset = 0; offset < 4; offset++) {
          pattern = pattern.setBit((row + offset) * width + col + offset);
        }
        patterns.add(pattern);
      }
    }

    // Diagonal patterns (top-left to bottom-right)
    for (int row = 0; row <= height - 4; row++) {
      for (int col = 3; col < width; col++) {
        BigInteger pattern = BigInteger.ZERO;
        for (int offset = 0; offset < 4; offset++) {
          pattern = pattern.setBit((row + offset) * width + col - offset);
        }
        patterns.add(pattern);
      }
    }

    return patterns;
  }


  private boolean isGameOver(BigInteger playerBoard, BigInteger opponentBoard) {
    // Check if either player has won or the board is full
    return hasWon(playerBoard) || hasWon(opponentBoard) || isBoardFull(playerBoard, opponentBoard);
  }

  private boolean isBoardFull(BigInteger playerBoard, BigInteger opponentBoard) {
    // Check if all positions are occupied
    BigInteger allPositions = playerBoard.or(opponentBoard);
    BigInteger fullBoard = BigInteger.valueOf(0b1111111111111111111111111111111111111111111111111L);
    return allPositions.equals(fullBoard);
  }

  private boolean hasWon(BigInteger board) {
    BigInteger horizontal = board.and(board.shiftRight(1)).and(board.shiftRight(2)).and(board.shiftRight(3));
    BigInteger vertical = board.and(board.shiftRight(7)).and(board.shiftRight(14)).and(board.shiftRight(21));
    BigInteger diagonal1 = board.and(board.shiftRight(6)).and(board.shiftRight(12)).and(board.shiftRight(18));
    BigInteger diagonal2 = board.and(board.shiftRight(8)).and(board.shiftRight(16)).and(board.shiftRight(24));

    return !horizontal.equals(BigInteger.ZERO) ||
            !vertical.equals(BigInteger.ZERO) ||
            !diagonal1.equals(BigInteger.ZERO) ||
            !diagonal2.equals(BigInteger.ZERO);
  }

}











