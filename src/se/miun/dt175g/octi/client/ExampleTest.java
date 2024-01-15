package se.miun.dt175g.octi.client;

import se.miun.dt175g.octi.core.PerformanceTest;

/**
 * The ExampleTest class serves as a guide on how to run the PerformanceTest.
 * 
 * This test will evaluate the performance of your custom-built StudentAgent in the Octi game.
 * The purpose is to determine if the agent can achieve a specified win rate or better against
 * its opponents over a series of matches.
 * 
 * Modify and use this example as a reference to run your own performance tests.
 */
public class ExampleTest {
	public static void main(String[] args) {

		// Create an instance of your custom agent (StudentAgent).
		StudentAgent myAgent = new StudentAgent();
		
		/**
		 * This line initializes a performance test for the `myAgent` instance and immediately runs it with default settings.
		 * 
		 * Breakdown:
		 * 1. `PerformanceTest.test(myAgent)`: This part of the code creates a new performance test for the `myAgent` instance. 
		 * 
		 * 2. `.run()`: This method executes the performance test that was set up in the previous step. By not specifying 
		 *    any other configuration methods (like `.random()`, `.color()`, etc.), you are relying on the default values 
		 *    predefined in the PerformanceTest class. 
		 * 
		 * - The agent will play 1 match (default matches value).
		 * - It will have a timeout of 500ms for each move (default timeout value).
		 * - The agent's chosen color will be "red" (default color value).
		 * - The game will be set at difficulty level 0 (default difficulty).
		 */
		//PerformanceTest.test(myAgent).run();

		// Initialize and configure a performance test for the agent:
        // 1. The agent being tested is `myAgent`.
        // 2. A random seed of 55 is set to make game scenarios reproducible.
        // 3. The color chosen for the agent is "red".
        // 4. The agent will play 10 matches.
        // 5. Each move of the agent has a timeout of 500 milliseconds.
        // 6. The difficulty level of the game is set to 1.
        // 7. The performance test will evaluate if the agent can win at least 50% (0.5) of its games.
		PerformanceTest.test(myAgent).random(55).color("red")
				.matches(10).timeout(500).level(2).run(0.5);
		

	}
}
