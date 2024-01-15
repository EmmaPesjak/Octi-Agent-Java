package se.miun.dt175g.octi.client;


import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import se.miun.dt175g.octi.core.*;


/**
 * StudentAgent is an implementation of the Octi Agent interface that uses an iterative deepening minimax
 * search with alpha-beta pruning to determine the best move for the current game state.
 * @author Emma Pesjak
 */
public class StudentAgent extends Agent {
	private long startTime;
	private final int BUFFER_TIME = 30; // Some buffer time needed for the recursions to finish before the time limit exceeds.
	private final int MAX_DEPTH = 5;

	/**
	 * Method for deciding which action the Agent should make next.
	 * @param octiState is the current game state.
	 * @return the next action.
	 */
	@Override
	public OctiAction getNextMove(OctiState octiState) {
		startTime = System.currentTimeMillis();
		Node<OctiState, OctiAction> root = Node.root(octiState);
		OctiAction bestAction = null;

		// Iterative deepening DFS minimax with alpha-beta pruning.
		for (int depth = 1; depth <= MAX_DEPTH; depth++) {
			ScoredNode result = minimax(root, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, true);

			// If time is up, break out of the loop and return the best action so far.
			if (System.currentTimeMillis() - startTime + BUFFER_TIME > this.timeLimit) {
				break;
			}
			bestAction = result.action;
		}
		return bestAction;
	}

	/**
	 * Conducts an iterative deepening minimax search with alpha-beta pruning to find the best move.
	 * @param parentNode is the parent node.
	 * @param depth is the current depth of the search.
	 * @param alpha is the alpha value for alpha-beta pruning.
	 * @param beta is the beta value for alpha-beta pruning.
	 * @param isMaximizingPlayer is a boolean stating whether it is currently min or max.
	 * @return a ScoredNode containing the score and the best OctiAction.
	 */
	private ScoredNode minimax(Node<OctiState, OctiAction> parentNode, int depth, int alpha, int beta, boolean isMaximizingPlayer) {
		OctiAction bestAction = null;
		int bestScore;

		// Check if the recursive search needs to be terminated because of state/depth/time limit.
		if (parentNode.state.isTerminal() || depth == 0 || System.currentTimeMillis() - startTime + BUFFER_TIME > this.timeLimit) {
			return new ScoredNode(evaluate(parentNode, isMaximizingPlayer), null);
		}

		// Check for winning move for maximizing player, if so, return early.
		if (isMaximizingPlayer && Objects.equals(parentNode.state.hasWinner(), player.getColor())) {
			return new ScoredNode(100000000, parentNode.action);
		}

		// Check for winning move for minimizing player, if so, return early.
		if (!isMaximizingPlayer && Objects.equals(parentNode.state.hasWinner(), oppPlayer.getColor())) {
			return new ScoredNode(-100000000, parentNode.action);
		}

		List<Node<OctiState, OctiAction>> childNodes = generateChildNodes(parentNode); // Generate all nodes.
		orderNodes(childNodes, isMaximizingPlayer); // Move ordering, explore better branches first.

		if (isMaximizingPlayer) {
			bestScore = Integer.MIN_VALUE;

			// Iterate over the children, recursively calling the minimax on them.
			for (Node<OctiState, OctiAction> childNode : childNodes) {

				// Check if the time limit is close.
				long elapsedTime = System.currentTimeMillis() - startTime;
				if (elapsedTime + BUFFER_TIME > this.timeLimit) {
					return new ScoredNode(bestScore, bestAction);
				}

				// Return early if we have a winner.
				if (Objects.equals(childNode.state.hasWinner(), player.getColor())) {
					return new ScoredNode(100000000, childNode.action);
				}

				ScoredNode scoredNode = minimax(childNode, depth - 1, alpha, beta, false);
				if (scoredNode.score > bestScore) {
					bestScore = scoredNode.score;
					bestAction = childNode.action;
				}

				// Update the alpha and check if we can break.
				alpha = Math.max(alpha, bestScore);
				if (beta <= alpha) {
					break;
				}
			}
		} else {
			bestScore = Integer.MAX_VALUE;

			// Iterate over the children, recursively calling the minimax on them.
			for (Node<OctiState, OctiAction> childNode : childNodes) {
				long elapsedTime = System.currentTimeMillis() - startTime;

				// Check if the time limit is close.
				if (elapsedTime + BUFFER_TIME > this.timeLimit) {
					return new ScoredNode(bestScore, bestAction);
				}

				// Return early if we have a winner.
				if ( Objects.equals(childNode.state.hasWinner(), oppPlayer.getColor())) {
					return new ScoredNode(-100000000, childNode.action);
				}

				ScoredNode scoredNode = minimax(childNode, depth - 1, alpha, beta, true);
				if (scoredNode.score < bestScore) {
					bestScore = scoredNode.score;
					bestAction = childNode.action;
				}

				// Update the beta and check if we can break.
				beta = Math.min(beta, bestScore);
				if (beta <= alpha) {
					break;
				}
			}
		}
		return new ScoredNode(bestScore, bestAction);
	}

	/**
	 * A very basic move ordering method for sorting the nodes according to the amount of pods.
	 * @param childNodes is a list of the nodes to be sorted.
	 * @param isMaximizingPlayer is a boolean stating whether the list should be sorted for min or max.
	 */
	private void orderNodes(List<Node<OctiState, OctiAction>> childNodes, boolean isMaximizingPlayer) {
		childNodes.sort(Comparator.comparingInt(node -> opponentPods(node, isMaximizingPlayer)));
	}

	/**
	 * Returns the number of pods belonging to the player/opponent player based on the game state
	 * and whether it is for min or max.
	 * @param node is the node representing the game state.
	 * @param isMaximizingPlayer is a boolean stating whether it is for min or max.
	 * @return the number of pods.
	 */
	private int opponentPods(Node<OctiState, OctiAction> node, boolean isMaximizingPlayer) {
		return isMaximizingPlayer
				? node.state.getBoard().getPodsForPlayer(oppPlayer.getColor()).size()
				: node.state.getBoard().getPodsForPlayer(player.getColor()).size();
	}

	/**
	 * Evaluation function used by the minimax search. The evaluation includes considerations for winning
	 * or losing states, the number of player's pods, distance to the goal, and potential jump bonuses.
	 * @param node is the node representing the game state.
	 * @param isMaximizingPlayer is a boolean stating whether it is for min or max.
	 * @return the evaluation score for the given game state.
	 */
	private int evaluate(Node<OctiState, OctiAction> node, boolean isMaximizingPlayer) {
		int score = 0;

		// Evaluate based on winning or losing states. Return directly to save time.
		if (Objects.equals(node.state.hasWinner(), player.getColor())) {
			return  100000000;
		} else if (Objects.equals(node.state.hasWinner(), oppPlayer.getColor())) {
			return -100000000;
		}

		// Evaluate based on the number of player's pods.
		score += node.state.getBoard().getPodsForPlayer(player.getColor()).size() * 500;
		score -= node.state.getBoard().getPodsForPlayer(oppPlayer.getColor()).size() * 500;

		// Evaluate based on the distance to the goal.
		score += isMaximizingPlayer
				? evaluateDistanceToGoal(node.state, player.getColor())
				: -evaluateDistanceToGoal(node.state, oppPlayer.getColor());

		// Add a bonus if the action is a jumping action.
		if (node.action instanceof JumpAction) {
			if (isMaximizingPlayer) {
				score += 10000;
			} else {
				score -= 10000;
			}
		}
		return score;
	}

	/**
	 * Evaluates the distance to the goal for pods of the specified color on the given OctiState.
	 * The distance is calculated based on the Manhattan distance from each pod's position to the goal positions.
	 * @param state is the OctiState representing the game state.
	 * @param playerColor is the color of the pods for which the distance to the goal is being evaluated.
	 * @return the distance score based on the Manhattan distance to the goal for the specified color.
	 */
	private int evaluateDistanceToGoal(OctiState state, String playerColor) {
		int distanceScore = 0;
		Point[] goalBase;

		// Get goal positions based on the playerColor.
		goalBase = playerColor.equals("red") ? state.getBlackBase() : state.getRedBase();

		// Iterate through each pod on the board.
		for (Pod pod : state.getBoard().getPodsForPlayer(playerColor)) {
			Point podPosition = state.getBoard().getPositionFromPod(pod);

			// Calculate the Manhattan distance to the goal.
			int distance = calculateDistanceToGoal(podPosition, goalBase);

			// Calculate a score.
			distanceScore += (10 - distance) * 10000;
		}
		return distanceScore;
	}

	/**
	 * Calculates the minimum Manhattan distance from the given pod position to the goal positions.
	 * @param podPosition is the position of the pod for which the distance to the goal is being calculated.
	 * @param goalBase is the array of goal positions.
	 * @return the minimum Manhattan distance from the pod position to the goal positions.
	 */
	private int calculateDistanceToGoal(Point podPosition, Point[] goalBase) {
		int minDistance = Integer.MAX_VALUE;

		// Iterate through goal positions and find the minimum distance.
		for (Point goalPosition : goalBase) {
			int distance = Math.abs(podPosition.x() - goalPosition.x()) + Math.abs(podPosition.y() - goalPosition.y());
			minDistance = Math.min(minDistance, distance);
		}
		return minDistance;
	}

	/**
	 * Represents a scored action, including the score and the associated OctiAction.
	 */
	private static class ScoredNode {
		int score;
		OctiAction action;

		/**
		 * Constructs a ScoredAction with the given score and OctiAction.
		 * @param score  The score of the action.
		 * @param action The OctiAction associated with the score.
		 */
		ScoredNode(int score, OctiAction action) {
			this.score = score;
			this.action = action;
		}
	}
}
