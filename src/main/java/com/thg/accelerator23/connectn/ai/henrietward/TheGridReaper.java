package com.thg.accelerator23.connectn.ai.henrietward;

import java.util.List;
import java.util.Random;

import com.thehutgroup.accelerator.connectn.player.*;


public class TheGridReaper extends Player {
  private boolean isFirstMove = true;

  public TheGridReaper(Counter counter, String name) {
    //TODO: fill in your name here
    super(counter, TheGridReaper.class.getName());

  }

  @Override
  public int makeMove(Board board) {
    long startTime= System.currentTimeMillis();
    List<String> messages = List.of(
            "YOU MIGHT BE THE WORST PLAYER I'VE EVER SEEN",
            "THIS IS EMBARRASSING....",
            "IF I WERE YOU, I'D END IT ALL",
            "THAT MOVE WAS GENUINELY DREADFUL",
            "ENGAGING SUPERCHARGED ENGINEERING POWERS",
            "EVEN WHEN I'M NOT IN THE OFFICE, I'M IN IT",
            "HAVE I WON YET?",
            "IF I WIN, IT'S BECAUSE OF ED; IF I LOSE, IT'S BECAUSE OF HETTIE",
            "YOUR NAME IS NOT AS GOOD AS MINE",
            "WERE THESE PRINT STATEMENTS WORTH THE LOST TIME? ABSOLUTELY."
    );

    Random random=new Random();
    String randomMessage= messages.get(random.nextInt(messages.size()));

    String boldRed="\u001B[1;91m";
    String reset="\u001B[0m";
    System.out.println(boldRed + ">>> *** " + randomMessage + " *** <<<" + reset);

    int bestMove = -1;
    int bestValue = Integer.MIN_VALUE;
    Counter counter = this.getCounter();
    Counter opponentCounter =this.getCounter().getOther();
    int alpha = Integer.MIN_VALUE;
    int beta = Integer.MAX_VALUE;
    int blockingCol = blockingMove(board);
    int winningCol = winningMove(board);
    int depth = 3;


    if (isFirstMove) {
      isFirstMove = false;
      return 4;
    }

    if (winningCol != -1) {
      return winningCol;
    }

    if (blockingCol != -1) {
      return blockingCol;
    }


    while (System.currentTimeMillis() - startTime < 8500) {

      for (int col = 0; col < board.getConfig().getWidth(); col++) {
        long currentTime = System.currentTimeMillis();

        if (isColumnPlayable(board, col)) {
          try {
            Board newBoard = new Board(board, col, opponentCounter);
            int moveValue = minimax(newBoard, depth ,alpha, beta, false, opponentCounter.getOther());
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
      depth++;
      if (System.currentTimeMillis() - startTime < 8500) {
        break;
      }
    }
    return bestMove;
  }

  private int winningMove(Board board) {

    Counter counter = this.getCounter();
    Counter[][] placements = board.getCounterPlacements();

    for (int row = 0; row < 8; row++) {
      for (int col = 0; col <= 6; col++) {
        Counter[] window = {
                placements[col][row],
                placements[col + 1][row],
                placements[col + 2][row],
                placements[col + 3][row]
        };
        if (isThreeInARow(window, counter)) {
          return findBlockingColumn(board, col, row, "horizontal", placements);
        }
      }
    }

    for (int col = 0; col < 10; col++) {
      for (int row = 0; row <= 4; row++) {
        Counter[] window = {
                placements[col][row],
                placements[col][row + 1],
                placements[col][row + 2],
                placements[col][row + 3]
        };
        if (isThreeInARow(window, counter)) {
          return findBlockingColumn(board, col, row, "vertical", placements);
        }
      }
    }

    for (int row = 0; row <= 4; row++) {
      for (int col = 0; col <= 6; col++) {
        Counter[] window = {
                placements[col][row],
                placements[col + 1][row + 1],
                placements[col + 2][row + 2],
                placements[col + 3][row + 3],
        };

        if (isThreeInARow(window, counter)) {
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

        if (isThreeInARow(window, counter)) {
          return findBlockingColumn(board, col, row, "neg_diagonal", placements);
        }
      }
    }
    return -1;
  }


  private int blockingMove(Board board) {
    Counter opponent = this.getCounter().getOther();
    Counter[][] placements = board.getCounterPlacements();

    for (int row = 0; row < 8; row++) {
      for (int col = 0; col <= 6; col++) {
        Counter[] window = {
                placements[col][row],
                placements[col + 1][row],
                placements[col + 2][row],
                placements[col + 3][row]
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
                placements[col][row + 1],
                placements[col][row + 2],
                placements[col][row + 3]
        };
        if (isThreeInARow(window, opponent)) {
          return findBlockingColumn(board, col, row, "vertical", placements);
        }
      }
    }

    for (int row = 0; row <= 4; row++) {
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
          return findBlockingColumn(board, col, row, "neg_diagonal", placements);
        }
      }
    }
    return -1;
  }

//  private boolean canOpponentWinNext(Board board, Counter opponent) {
//    // Check each column to see if the opponent can win by placing a counter in that column
//    for (int col = 0; col < board.getConfig().getWidth(); col++) {
//      if (isColumnPlayable(board, col)) {
//        // Simulate the opponent's move
//        Board tempBoard = new Board();
//        // Check if this move leads to a win for the opponent
//        if (hasWon(tempBoard, opponent)) {
//          return true;  // The opponent can win by playing in this column
//        }
//      }
//    }
//    return false;  // The opponent cannot win on the next move
//  }

  private boolean isThreeInARow(Counter[] window, Counter opponent) {
    int count = 0;
    int empty = 0;
    for (Counter c : window) {
      if (c == opponent) count++;
      if (c == null) empty++;
    }
    return count == 3 && empty == 1;
  }

  private boolean isFourInARow(Counter[] window, Counter opponent) {
    int count = 0;
    int empty = 0;
    for (Counter c : window) {
      if (c == opponent) count++;
      if (c == null) empty++;
    }
    return count == 4 && empty == 0;
  }

  private int findBlockingColumn(Board board, int startCol, int startRow, String direction, Counter[][] placements) {
    // Find the empty position that needs blocking
    // Return the column number if it's a valid move
    switch (direction) {
      case "horizontal":
        for (int i = 0; i < 4; i++) {
          if (placements[startCol + i][startRow] == null) {
            if (isValidMove(board, startCol + i)) {
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
          if (placements[startCol + i][startRow + i] == null) {
            if (isValidMove(board, startCol + i)) {
              return startCol + i;
            }
          }
        }
        break;
      case "neg_diagonal":
        for (int i = 0; i < 4; i++) {
          if (placements[startCol + i][startRow - i] == null) {
            if (isValidMove(board, startCol + i)) {
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

    if (depth == 0 || isGameOver(board, counter)) {
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

    for (int col = 0; col < board.getConfig().getWidth(); col++) {
      int centerScore = board.getConfig().getWidth() / 2;
      score += placements[col].length * (centerScore - Math.abs(centerScore - col));
    }

    score += evaluateRows(placements, counter);
    score += evaluateColumns(placements, counter);
    score += evaluateDiagonals(placements, counter);
    score += evaluateNegDiagonals(placements, counter);
    score += calculateClusterProximity(placements, counter, board);

//    System.out.println("Score: " + score);

    return score;
  }

  private int evaluateRows(Counter[][] placements, Counter counter) {
    int score = 0;

    for (int row = 0; row < 8; row++) {
      for (int col = 0; col <= 6; col++) {
        Counter[] window = {
                placements[col][row],
                placements[col + 1][row],
                placements[col + 2][row],
                placements[col + 3][row],
        };

        score += evaluateWindow(window, counter);
      }
    }
    return score;
  }

  private int evaluateColumns(Counter[][] placements, Counter counter) {
    int score = 0;

    for (int col = 0; col < 10; col++) {
      for (int row = 0; row <= 4; row++) {
        Counter[] window = {
                placements[col][row],
                placements[col][row + 1],
                placements[col][row + 2],
                placements[col][row + 3],
        };

        score += evaluateWindow(window, counter);
      }
    }
    return score;
  }

  private int evaluateDiagonals(Counter[][] placements, Counter counter) {
    int score = 0;

    for (int row = 0; row <= 4; row++) {
      for (int col = 0; col <= 6; col++) {
        Counter[] window = {
                placements[col][row],
                placements[col + 1][row + 1],
                placements[col + 2][row + 2],
                placements[col + 3][row + 3],
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

  private int evaluateWindow(Counter[] window, Counter counter) {
    int score = 0;
    int countSelf = 0;
    int countOpponent = 0;
    boolean hasEmptySpace = false;

    for (Counter c : window) {
      if (c == counter) countSelf++;
      else if (c == counter.getOther())
      { countOpponent++;}
      if (c == null) {
        hasEmptySpace =true;
      }
    }

    if (!hasEmptySpace) {
      return 0;
    }


    if (countSelf == 4) score += 10000;
    else if (countSelf == 3 && countOpponent == 0) score += 100;
    else if (countSelf == 2 && countOpponent == 0) score += 10;
    else if (countSelf == 1 && countOpponent == 0) score += 1;
    else if (countOpponent == 3 && countSelf == 0) score -= 100;
    else if (countOpponent == 4) score -= 10000;

    if (countSelf == 3 && countOpponent == 0 && !hasEmptySpace) {
      score -= 1000;
    }
    return score;
  }
//
  private int calculateClusterProximity(Counter[][] placements, Counter counter, Board board) {
    int clusterScore = 0;
    for (int row = 0; row < 8; row++) {
      for (int col = 0; col < 10; col++) {
        if (placements[col][row] == counter) {
          clusterScore += countNearbyCounter(placements, col, row, counter, board);
        }
      }
    }
    return clusterScore;
  }

  private int countNearbyCounter(Counter[][] placements, int col, int row, Counter counter, Board board) {
    int nearbyBonus = 0;

    int[][] directions = {{-1, 0}, {1, 0}, {0, 1}, {0, -1}, {-1, 1}, {1, 1}, {-1, -1}, {1, -1}};

    for (int[] dir : directions) {
      int newCol = col + dir[0];
      int newRow = row + dir[1];
      if (isValidMove(board, newCol) && (newRow >= 0 && newRow < 8) && (newCol >= 0 && newCol < 10) &&
              placements[newCol][newRow] == counter) {
        nearbyBonus += 20;  // Bonus for each nearby counter of same color
      }
    }
    return nearbyBonus;
  }


  private boolean isGameOver(Board board, Counter counter) {
    // Implement logic to check if the game is over (win/loss/draw)

    if (hasWon(board, counter) || boardFull(board)) {
      return true;
    }
    return false; // Placeholder
  }

  private boolean hasWon(Board board, Counter counter) {
    // Loop through each row and column to check all possible windows
    for (int row = 0; row < 8; row++) {
      for (int col = 0; col < 10; col++) {
        // Check horizontal window
        if (col <= 10 - 4) {
          Counter[] horizontalWindow = new Counter[4];
          for (int i = 0; i < 4; i++) {
            Position position = new Position(row, col + i);
            horizontalWindow[i] = board.getCounterAtPosition(position);
          }
          if (isFourInARow(horizontalWindow, counter)) {
            return true;
          }
        }

        // Check vertical window
          Counter[] verticalWindow = new Counter[4];
          for (int i = 0; i < 4; i++) {
            Position position = new Position(row+i, col );
            verticalWindow[i] = board.getCounterAtPosition(position);
          }
          if (isFourInARow(verticalWindow, counter)) {
            return true;
          }


        // Check diagonal window (top-left to bottom-right)
        if (row <= 4 && col <= 6) {
          Counter[] diagonalWindow = new Counter[4];
          for (int i = 0; i < 4; i++) {
            Position position = new Position(row+i, col + i);
            diagonalWindow[i] = board.getCounterAtPosition(position);
          }
          if (isFourInARow(diagonalWindow, counter)) {
            return true;
          }
        }

        // Check diagonal window (bottom-left to top-right)
        if (row >= 3 && col <= 6) {
          Counter[] diagonalWindow = new Counter[4];
          for (int i = 0; i < 4; i++) {
            Position position = new Position(row-i, col + i);
            diagonalWindow[i] = board.getCounterAtPosition(position);
          }
          if (isFourInARow(diagonalWindow, counter)) {
            return true;
          }
        }
      }
    }

    return false;
  }


  private boolean boardFull(Board board) {
    for (int row = 0; row < 8; row++) {
      for (int col = 0; col < 10; col++) {
        Position position = new Position(row, col);
        if (board.getCounterAtPosition(position) == null) {
          return false;  // Found an empty spot, so the board is not full
        }
      }
    }
    return true;  // No empty spots, the board is full
  }


}