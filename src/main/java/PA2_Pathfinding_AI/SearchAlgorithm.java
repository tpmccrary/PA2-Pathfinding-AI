package PA2_Pathfinding_AI;

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

    return solution;
  }
}
