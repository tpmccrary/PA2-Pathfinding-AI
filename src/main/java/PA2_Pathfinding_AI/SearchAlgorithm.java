package PA2_Pathfinding_AI;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

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
        if (scheduled)
          break;
        if (c.timeSlotValues[j] > 0) {
          System.out.println("Course: " + i + " time slot at " + j + "(" + c.timeSlotValues[j]
              + ") is greater than 0. Going to iterate through all the rooms.");
          for (int k = 0; k < problem.rooms.size(); k++) {
            if (solution.schedule[k][j] < 0) {
              System.out.println("Schedule at pos: " + k + "," + j + " is empty. Put course: " + i
                  + " in this position. Course is now scheduled.");
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
  // - has a location (x, y).
  // Room properties:
  // - has a building,
  // - a max capacity.
  // Course properties:
  // - number of students enrolled.
  // - a vlaue for being scheduled.
  // - a list of values for each of 10 available time slots. Value 0 means it
  // cannot be held at that time. Positive values are added as a bonus for
  // scheduling a course during that time.
  // - a preferred building.
  // Courses can only be schedlued in rooms with a room capacity larger than the
  // number of enrolled students.
  // Rooms can only have ONE course scheduled in the courses 10 time slots.
  // Courses have a preferred building. Courses not scheduled in their preferred
  // building it recieves a penalty based on the distance from the preferred to
  // the actual building.
  //
  // A solution is a mapping of rooms and time slots to courses. That is, each
  // room can be assigned to hold on course in each available time slots.
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
    // select parents for reproduction. (2)
    // perform crossover, put a mutation in a child.
    // evaluate population.
    // }

    // These will be the two functions added to improve the basic algorithm
    // ADD ELITISM to improve algorithm. Meaning choose the best from the previous
    // population to move to the next.
    // ADD TOURNEMENT STYLE SELECTION. Meaning have a random selection from the
    // population to go against eachother and get the best.
    // Good reference: https://www.youtube.com/watch?v=MacVqujSXWE

    // Create a random population. A list of schedules.
    List<Schedule> population = genRanPopulation(problem, 10);

    // Create a hashtable to record the schedules score.
    Hashtable<Double, Schedule> scoredPopulation = new Hashtable<Double, Schedule>();

    // Go through the population, giving each sample a score.
    for (Schedule schedule : population) {
      scoredPopulation.put(checkSampleFitness(schedule, problem), schedule);
    }

    Hashtable<Double, Schedule> parents = new Hashtable<Double, Schedule>();

    double parent1Key = tourneySelection(scoredPopulation, problem);
    double parent2Key = tourneySelection(scoredPopulation, problem);

    System.out.println(parents);

    crossover(0.7, scoredPopulation, parent1Key, parent2Key, problem);

    return solution;
  }

  // Generates a random population (aka a list of schedules) given the scheduling
  // problem. This means that for every room, it randomly assigns a course at a
  // randomly selected time slot.
  // This means it is possible to have a room that cannot be at that time slot,
  // this is expected.
  private List<Schedule> genRanPopulation(SchedulingProblem problem, int ammount) {
    // Create a list of schedules. This will be our population, which is the
    // schedules we will be comparing.
    List<Schedule> schedulePop = new ArrayList<Schedule>();

    Random random = new Random();

    // Iterate through the ammount of samples we want.
    for (int i = 0; i < ammount; i++) {
      // Creates an empty schedule.
      Schedule sampleSchedule = problem.getEmptySchedule();

      // Populate the schedule randomly in this loop.
      for (int j = 0; j < sampleSchedule.schedule.length; j++) {
        // Randomly choose a room number (column).
        int ranRoomNum = random.nextInt(sampleSchedule.schedule[0].length);
        // In that room, set a randomly selected course there.
        sampleSchedule.schedule[j][ranRoomNum] = random.nextInt(problem.courses.size());
      }

      // Add this new sample to the population and repeat.
      schedulePop.add(sampleSchedule);
    }

    return schedulePop;
  }

  // Given a schedule, returns how fit it is. In other words, returns the value of
  // this sample, how good it is.
  // This will be a calculation if the number is higher.
  private double checkSampleFitness(Schedule sampSchedule, SchedulingProblem problem) {
    // Check if course can be in that timeslot. If it is, add the timeslot num to
    // the score. If not set it to zero.
    // Check if course fits in that room. if not set it to zero.
    // Check if course is in the preffered building, if not, subtract penalty
    // (distance of actual to preffered).
    // Add these value together and return the score.

    double score = 0.0;
    boolean roomScheduled = false;
    int course = -1;

    // Go through each sample. Room and timeslot.
    for (int i = 0; i < sampSchedule.schedule.length; i++) {
      for (int j = 0; j < sampSchedule.schedule[i].length; j++) {

        // Error check for mutliple courses scheduled in one room.
        if (sampSchedule.schedule[i][j] != -1 && roomScheduled == true) {
          System.out.println("ERROR: 2 courses schedueled in a room in the 10 time slots.");
          System.exit(1);
        }

        // If the schedule and that position is -1, that means nothing is scheduled
        // there so move on.
        if (sampSchedule.schedule[i][j] == -1) {
          continue;
        } else {
          // Get what course it is. The course is the number stored at the schedule
          // location.
          course = sampSchedule.schedule[i][j];
          roomScheduled = true;

          // Check to see if course fits in room. If not break and do not do anything to
          // the score..
          if (problem.courses.get(course).enrolledStudents > problem.rooms.get(i).capacity) {
            System.out.println("Room is not large enough for course.");
            roomScheduled = false;
            break;
          }

          // Check if the course can be held at the time slot its in (j). If so, add to
          // the score.
          if (problem.courses.get(course).timeSlotValues[j] != 0) {
            // System.out.println("Course can be held at time: " + j);
            score += problem.courses.get(course).timeSlotValues[j];
          } else {
            roomScheduled = false;
            break;
          }

          // Here we will check if the course is in the preffered building.
          Building preferredBuild;
          Building actualBuild;

          preferredBuild = problem.courses.get(course).preferredLocation;
          actualBuild = problem.rooms.get(i).b;

          // If the coordinates are not the same, then we have to get the distance and
          // subtract it from thes score.
          if (preferredBuild.xCoord != actualBuild.xCoord || preferredBuild.yCoord != actualBuild.yCoord) {
            double distance = 0.0;

            distance = Math.sqrt(
                Math.abs((preferredBuild.xCoord - actualBuild.xCoord) + (preferredBuild.yCoord - actualBuild.yCoord)));

            score = score - distance;
          }

          roomScheduled = false;

          break;
        }
      }
    }

    return score;
  }

  private double tourneySelection(Hashtable<Double, Schedule> scoredPopulation, SchedulingProblem problem) {
    Random random = new Random();

    // These two lines of code are done in order to get a random element for the
    // hashtable. The keys are grabbed from the hashtable and then put into an
    // array.
    // A random index gets a key from this array. That will be our first contender.
    Double[] keys = new Double[scoredPopulation.size()];
    keys = scoredPopulation.keySet().toArray(keys);

    double contender1 = keys[random.nextInt(keys.length)];
    double contender2 = contender1;
    // This is done to make sure we get a different contender from the population.
    while (contender2 == contender1) {
      contender2 = keys[random.nextInt(keys.length)];
    }

    // Returns the best score from the two contenders.
    return Double.max(contender1, contender2);

  }

  private Hashtable<Double, Schedule> crossover(double crossoverRate, Hashtable<Double, Schedule> scoredPopulation,
      double parent1Key, double parent2Key, SchedulingProblem problem) {
    // We are gooing to swap the top half and bottom half of parents.

    Random random = new Random();

    Schedule parent1 = scoredPopulation.get(parent1Key);
    Schedule parent2 = scoredPopulation.get(parent2Key);

    // Get a random value between 0 amd 1.
    double randomValue = 0 + (1 - 0) * random.nextDouble();

    if (randomValue < crossoverRate) {
    
      Schedule parent1Temp = new Schedule(parent1.schedule.length, parent1.schedule[0].length);

      for (int i = 0; i < parent1.schedule.length; i++) {
        for (int j = 0; j < parent1.schedule[i].length; j++) {
          parent1Temp.schedule[i][j] = parent1.schedule[i][j];
        }
      }

      // Start at half way point.
      for (int i = parent1.schedule.length / 2; i < parent1.schedule.length; i++) {
        for (int j = 0; j < parent1.schedule[i].length; j++) {
          parent1.schedule[i][j] = parent2.schedule[i][j];
          parent2.schedule[i][j] = parent1Temp.schedule[i][j];
        }
      }
    }

    Schedule child1 = parent1;
    Schedule child2 = parent2;

    child1 = mutation(0.05, child1, problem);
    child2 = mutation(0.05, child2, problem);

    Hashtable<Double, Schedule> newPopulation = new Hashtable<Double, Schedule>();

    newPopulation.put(checkSampleFitness(child1, problem), child1);
    newPopulation.put(checkSampleFitness(child2, problem), child2);

    return newPopulation;
  }

  private Schedule mutation(double mutationRate, Schedule sampeSchedule, SchedulingProblem problem) {
    Random random = new Random();

    for (int i = 0; i < sampeSchedule.schedule.length; i++) {

      // Get a random value between 0 amd 1.
      double randomValue = 0 + (1 - 0) * random.nextDouble();

      // If random value is less than mutation rate, mutate the line.
      if (randomValue < mutationRate) {
        for (int j = 0; j < sampeSchedule.schedule[i].length; j++) {
          sampeSchedule.schedule[i][j] = -1;
        }
        sampeSchedule.schedule[i][random.nextInt(sampeSchedule.schedule[i].length)] = random
            .nextInt(problem.courses.size());
      }
    }

    return sampeSchedule;

  }

  // Prints the schedlue in human readable format.
  private void printSchedule(Schedule schedule) {
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
