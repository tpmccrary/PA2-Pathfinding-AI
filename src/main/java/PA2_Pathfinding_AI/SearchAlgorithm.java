package PA2_Pathfinding_AI;

import java.util.ArrayList;
import java.util.List;

public class SearchAlgorithm {

  // Your search algorithm should return a solution in the form of a valid
  // schedule before the deadline given (deadline is given by system time in ms)
  public Schedule solve(SchedulingProblem problem, long deadline) {

    // get an empty solution to start from
    Schedule solution = problem.getEmptySchedule();

    // YOUR CODE HERE

    return solution;
  }

  // This is a very naive baseline scheduling strategy
  // It should be easily beaten by any reasonable strategy
  public Schedule naiveBaseline(SchedulingProblem problem, long deadline) {

    // get an empty solution to start from
    Schedule solution = problem.getEmptySchedule();

    for (int i = 0; i < problem.courses.size(); i++) {
      System.out.println("Getting course: " + i + " in our problem.");
      Course c = problem.courses.get(i);
      boolean scheduled = false;
      System.out.println("Iterating through all the time slots from course: " + i);
      for (int j = 0; j < c.timeSlotValues.length; j++) {
        if (scheduled) break;
        if (c.timeSlotValues[j] > 0) {
          System.out.println("Course: " + i + " time slot at " + j + "(" + c.timeSlotValues[j] + ") is greater than 0. Going to iterate through all the rooms.");
          for (int k = 0; k < problem.rooms.size(); k++) {
            if (solution.schedule[k][j] < 0) {
              System.out.println("Schedule at pos: " + k + "," + j + " is empty. Put course: " + i + " in this position. Course is now scheduled.");
              solution.schedule[k][j] = i;
              scheduled = true;
              break;
            }
          }
        }
      }
    }

    return solution;
  }

  // Find best possible schedule.
  // Set of N room, M courses, and L buildings.
  // Building properties: 
  //    - has a location (x, y).
  // Room properties: 
  //    - has a building, 
  //    - a max capacity.
  // Course properties: 
  //    - number of students enrolled.
  //    - a vlaue for being scheduled.
  //    - a list of values for each of 10 available time slots. Value 0 means it cannot be held at that time. Positive values are added as a bonus for scheduling a course during that time.
  //    - a preferred building.
  // Courses can only be schedlued in rooms with a room capacity larger than the number of enrolled students.
  // Rooms can only have ONE course scheduled in the courses 10 time slots.
  // Courses have a preferred building. Courses not scheduled in their preferred building it recieves a penalty based on the distance from the preferred to the actual building.
  // 
  // A solution is a mapping of rooms and time slots to courses. That is, each room can be assigned to hold on course in each available time slots.
  //  

  // Schedule rows are rooms.
  // Schedule columns are time slots.

  // Your search algorithm should return a solution in the form of a valid
  // schedule before the deadline given (deadline is given by system time in ms)
  public Schedule geneticAlgorithm(SchedulingProblem problem, long deadline) {

    // get an empty solution to start from
    Schedule solution = problem.getEmptySchedule();

    // YOUR CODE HERE

    // initializePopulation
    // evaluatePopulation
    // while TerminationCriteriaNotSatisfied
    // {
    //    select parents for reproduction. (2)
    //    perform crossover, put a mutation in a child.
    //    evaluate population.
    // }
    
    // These will be the two functions added to improve the basic algorithm
    // ADD ELITISM to improve algorithm. Meaning choose the best from the previous population to move to the next.
    // ADD TOURNEMENT STYLE SELECTION. Meaning have a random selection from the population to go against eachother and get the best.
    // Good reference: https://www.youtube.com/watch?v=MacVqujSXWE

    List<Schedule> population = genRanPopulation(problem, 10);
    printSchedule(population.get(4));
    printSchedule(population.get(0));

    return solution;
  }

  // Generates a random population given the scheduling problem. This means that for every room, it randomly assigns a course at a randomly selected time slot.
  // This means it is possible to have a room that cannot be at that time slot.
  private List<Schedule> genRanPopulation(SchedulingProblem problem, int ammount)
  {
    // Create a list of schedules. This will be our population, which is the schedules we will be comparing.
    List<Schedule> schedulePop = new ArrayList<Schedule>();

    // Iterate through the ammount of samples we want.
    for (int i = 0; i < ammount; i++) {
      // Creates an empty schedule.
      Schedule sampleSchedule = problem.getEmptySchedule();

      // Populate the schedule randomly in this loop.
      for (int j = 0; j < sampleSchedule.schedule.length; j++) {
        // Randomly choose a room number (column).
        int ranRoomNum = problem.random.nextInt(sampleSchedule.schedule[0].length);
        // In that room, set a randomly selected course there.
        sampleSchedule.schedule[j][ranRoomNum] = problem.random.nextInt(problem.courses.size());
      }

      // Add this new sample to the population and repeat.
      schedulePop.add(sampleSchedule);
    }

    return schedulePop;
  }

  // Prints the schedlue in human readable format.
  private void printSchedule(Schedule schedule)
  {
    System.out.println("Rows are the rooms.");
    System.out.println("Columns are the timeslots (always 10).");
    for (int i = 0; i < schedule.schedule.length; i++) {
      System.out.print("Room " + i + ": [");
      for (int j = 0; j < schedule.schedule[i].length; j++) {
        System.out.print(schedule.schedule[i][j] + ", ");
      }
      System.out.println("]");
    }
  }
}
