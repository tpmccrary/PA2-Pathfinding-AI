package PA2_Pathfinding_AI;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {

  public static void main(String[] args) {

    int nBuildings = 0;
    int nRooms = 0;
    int nCourses = 0;
    int TIME_LIMIT_SECONDS = 0;
    int algorithm = 0;
    long seed = 0;

    if (args.length == 6) {
      try {
        nBuildings = Integer.parseInt(args[0]);
        nRooms = Integer.parseInt(args[1]);
        nCourses = Integer.parseInt(args[2]);
        TIME_LIMIT_SECONDS = Integer.parseInt(args[3]);
        algorithm = Integer.parseInt(args[4]);
        seed = Long.parseLong(args[5]);
      } catch (NumberFormatException e) {
        System.out.println("Number format exception reading arguments");
        System.exit(1);
      }
    } else {
      System.out.println("ERROR: Incorrect number of arguments (should have six).");
      System.exit(1);
    }

    System.out.println("Number of Buildings: " + nBuildings);
    System.out.println("Number of Rooms: " + nRooms);
    System.out.println("Number of Courses: " + nCourses);
    System.out.println("Time limit (s): " + TIME_LIMIT_SECONDS);
    System.out.println("Algorithm number: " + algorithm);
    System.out.println("Random seed: " + seed);


    SchedulingProblem test1 = new SchedulingProblem(seed);
    test1.createRandomInstance(nBuildings, nRooms, nCourses);

    SearchAlgorithm search = new SearchAlgorithm();

    long deadline = System.currentTimeMillis() + (1000 * TIME_LIMIT_SECONDS);

    // Add your seach algorithms here, each with a unique number
    Schedule solution = null;
    if (algorithm == 0) {
      solution = search.naiveBaseline(test1, deadline);
    }
    else if (algorithm == 1)
    {
      // solution = search.geneticAlgorithm(test1, deadline);
      // GeneticAlgorithm gAlgorithm = new GeneticAlgorithm(test1, deadline);

      // solution = gAlgorithm.geneticAlgorithm();

      solution = runGeneticAlgThreaded(test1, deadline);
    } 
    else {
      System.out.println("ERROR: Given algorithm number does not exist!");
      System.exit(1);
    }

    System.out.println("Deadline: " + deadline);
    System.out.println("Current: " + System.currentTimeMillis());
    System.out.println("Time remaining: " + (deadline - System.currentTimeMillis()));
    if (System.currentTimeMillis() > deadline) {
      System.out.println("EXCEEDED DEADLINE");
    }

    double score = test1.evaluateSchedule(solution);
    System.out.println();
    System.out.println("Score: " + score);
    System.out.println();
  }  

  private static Schedule runGeneticAlgThreaded(SchedulingProblem problem, double deadline)
  {
    Random random = new Random();

    List<GeneticAlgorithm> genAlgThreads = new ArrayList<GeneticAlgorithm>();

      int numCores = Runtime.getRuntime().availableProcessors();
      int threadAmount = (numCores * 2) - 1;

      for (int i = 0; i < threadAmount; i++) {
        genAlgThreads.add(new GeneticAlgorithm(problem, deadline));
        genAlgThreads.get(i).populationAmount = random.nextInt((1000 - 250) + 1) + 250;
        genAlgThreads.get(i).counterLimit = random.nextInt((1000 - 500) + 1) + 500;
        genAlgThreads.get(i).crossoverRate = 0.3 + (0.7 - 0.3) * random.nextDouble();
        genAlgThreads.get(i).mutationRate = 0.03 + (0.07 - 0.03) * random.nextDouble();
        genAlgThreads.get(i).start();
      }

      for (GeneticAlgorithm geneticAlgorithm : genAlgThreads) {
        try {
          geneticAlgorithm.join();
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

      return GeneticAlgorithm.getBestSchedule(GeneticAlgorithm.solutions).schedule;
  }

}