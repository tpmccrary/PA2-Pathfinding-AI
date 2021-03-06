package PA2_Pathfinding_AI;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

public class GeneticAlgorithm extends Thread {

    // Static list that is not tied down to an object. This allows all threads to
    // add to the list where we can find the best one later.
    public static List<ScoredSchedule> solutions = new ArrayList<ScoredSchedule>();

    public List<ScoredSchedule> scoredPopulation = new ArrayList<ScoredSchedule>();

    public static int numThreads = 0;
    public static Hashtable<Integer, GeneticAlgorithm> threads = new Hashtable<Integer, GeneticAlgorithm>();

    private int myThreadNum;

    // The scheduling problem itself.
    private SchedulingProblem problem;
    private double deadline;

    // How may schedules in a populaiton.
    public int populationAmount = 500;
    // The rate at which crossovers are preformed. MUST be between 0 and 1.
    public double crossoverRate = 0.5;
    // The rate at which mutations are preformed. MUST be between 0 and 1.
    public double mutationRate = 0.1;
    // The partipant size for a tourney.
    public int tourneySize = 8;
    // The rate at which different algorithm threads crossover.
    public double threadCrossoverRate = 0.01;

    private int improvmentCounter = 0;
    public int counterLimit = 800;

    // Constructor.
    GeneticAlgorithm(SchedulingProblem problem, double deadline) {
        this.problem = problem;
        this.deadline = deadline;
        threads.put(numThreads, this);
        myThreadNum = numThreads;
        numThreads++;
    }

    public void run() {
        geneticAlgorithm();
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

    // Genetic algorithm starts here.
    public Schedule geneticAlgorithm() {

        // get an empty solution to start from
        // Schedule solution = problem.getEmptySchedule();

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

        // Create a random population. This is a list of schedules.
        List<Schedule> population = genRanPopulation(this.populationAmount);

        // Initialize a list to hold the population that will be scored.
        // List<ScoredSchedule> scoredPopulation = new ArrayList<ScoredSchedule>();

        // Go through the population, giving each sample a score. This puts the schedule
        // and score into
        // an object, and that object gets put into the prev list.
        for (Schedule schedule : population) {
            scoredPopulation.add(new ScoredSchedule(checkSampleFitness(schedule), schedule));
        }

        population = null;

        boolean passedDeadline = false;
        while (improvmentCounter < counterLimit && passedDeadline == false) {

            // Initialize a list that will be our new population.
            List<ScoredSchedule> newScoredPopulation = new ArrayList<ScoredSchedule>();

            // Keep adding to the new population until its the same size as the old
            // population.
            while (newScoredPopulation.size() < scoredPopulation.size()) {
                // Make a list to hold the parents.
                List<ScoredSchedule> parents = new ArrayList<ScoredSchedule>();

                // Get the two parents using tourney selection.
                parents.add(tourneySelection(scoredPopulation));
                parents.add(tourneySelection(scoredPopulation));

                // Get a list of children (onlt two) from crossing over the two parents.
                List<ScoredSchedule> children = onePointCrossover(this.crossoverRate, parents);
                children = mutation(this.mutationRate, children);

                for (ScoredSchedule child : children) {
                    child.score = checkSampleFitness(child.schedule);
                }

                // check if improvment occurred. If not add to counter.
                if (improvmentOccured(children, parents) == false) {
                    improvmentCounter++;
                } else {
                    improvmentCounter = 0;
                }

                parents = null;

                // Add the children to the new population.
                for (ScoredSchedule child : children) {
                    newScoredPopulation.add(new ScoredSchedule(child.score, child.schedule));
                }

                children = null;

            }

            
            // Use elitism to get the best schedule from the previous population, and put it
            // in the new one.
            scoredPopulation = elitisim(newScoredPopulation, scoredPopulation);
            

            if (System.currentTimeMillis() > deadline  - deadline / 3) {
                // passedDeadline = true;
                break;
            }

            // See if we crossover with another thread.
            threadCrossover();

            

        }

        for (ScoredSchedule scoredSchedule : scoredPopulation) {
            scoredSchedule.score = checkSampleFitness(scoredSchedule.schedule);
        }

        ScoredSchedule bestSchedule = GeneticAlgorithm.getBestSchedule(scoredPopulation);
        solutions.add(bestSchedule);

        System.out.println("____THREAD " + myThreadNum + "----SCORE: " + bestSchedule.score + "----Populationn: " + this.populationAmount + "----Crossover rate: " + this.crossoverRate + "----Mutaion rate: " + this.mutationRate + "----Counter limit: " + this.counterLimit + "----Thread Crossover rate: " + this.threadCrossoverRate + "____");
        // printSchedule(bestSchedule.schedule);

        return bestSchedule.schedule;

    }

    // Generates a random population (aka a list of schedules) given the scheduling
    // problem. This means that for every room, it randomly assigns a course at a
    // randomly selected time slot.
    // This means it is possible to have a room that cannot be at that time slot,
    // this is expected.
    private List<Schedule> genRanPopulation(int ammount) {
        // Create a list of schedules. This will be our population, which is the
        // schedules we will be comparing.
        List<Schedule> schedulePop = new ArrayList<Schedule>();

        Random random = new Random();

        // Loops the ammount we want to be in the population.
        for (int i = 0; i < ammount; i++) {

            // Creates an empty schedule.
            Schedule sampleSchedule = problem.getEmptySchedule();

            int courseStart = random.nextInt(problem.courses.size());
            int course = courseStart;
            for (int j = 0; j < problem.courses.size(); j++) {
                course++;
                // IF we reach the end, go to 0;
                if (course >= problem.courses.size()) {
                    course = 0;
                }

                if (j >= problem.rooms.size() * problem.courses.get(j).timeSlotValues.length)
                {
                    break;
                }

                int ranRoom = random.nextInt(problem.rooms.size());
                int ranTimeslot = random.nextInt(problem.courses.get(course).timeSlotValues.length);

                // ranRoom = random.nextInt(problem.rooms.size());
                // ranTimeslot = random.nextInt(problem.courses.get(course).timeSlotValues.length);

                while (sampleSchedule.schedule[ranRoom][ranTimeslot] != -1) {
                    ranRoom = random.nextInt(problem.rooms.size());
                    ranTimeslot = random.nextInt(problem.courses.get(course).timeSlotValues.length);
            
                }

                sampleSchedule.schedule[ranRoom][ranTimeslot] = course;

            }

            schedulePop.add(sampleSchedule);
        }

        return schedulePop;
    }

    // Given a schedule, returns how fit it is. In other words, returns the value of
    // this sample, how good it is.
    // This will be a calculation if the number is higher.
    private double checkSampleFitness(Schedule sampSchedule) {
        // Check if course can be in that timeslot. If it is, add the timeslot num to
        // the score. If not set it to zero.
        // Check if course fits in that room. if not set it to zero.
        // Check if course is in the preffered building, if not, subtract penalty
        // (distance of actual to preffered).
        // Check there are no duplicate courses.
        // Add these value together and return the score.

        return problem.evaluateSchedule(sampSchedule);
    }

    // Given a population, selects a set ammount for them to "fight" against
    // eachother. One with best score wins and is returned.
    private ScoredSchedule tourneySelection(List<ScoredSchedule> scoredPopulation) {
        Random random = new Random();

        // Create a list that will hold the schedules and their scores.
        List<ScoredSchedule> contenders = new ArrayList<ScoredSchedule>();

        // For the ammount in the tourney, randomly pick contenders.
        for (int i = 0; i < this.tourneySize; i++) {
            contenders.add(scoredPopulation.get(random.nextInt(scoredPopulation.size())));
        }

        // Go through the list of contenders and get the one with the best score.
        ScoredSchedule bestContender = contenders.get(0);
        for (ScoredSchedule contender : contenders) {
            if (bestContender.score < contender.score) {
                bestContender = contender;
            }
        }

        return bestContender;
    }

    // This is the crossover function. This crossover function is based off OX1.
    // Given parents, they crossover to make children, and the children are then
    // returned.
    private List<ScoredSchedule> ox1Crossover(double crossoverRate, List<ScoredSchedule> parents) {
        Random random = new Random();

        // Create two new child objects to hold their score and schedule.
        ScoredSchedule child1 = new ScoredSchedule(0.0, parents.get(0).schedule);
        ScoredSchedule child2 = new ScoredSchedule(0.0, parents.get(1).schedule);

        // Get a random value between 0 amd 1.
        double randomValue = 0 + (1 - 0) * random.nextDouble();
        // Determine if we crossover.
        if (randomValue < crossoverRate) {

            // If we crossover, make the childrens schedules empty.
            child1.schedule = problem.getEmptySchedule();
            child2.schedule = problem.getEmptySchedule();

            // USING OX1 Crossover.

            // Gets two random crossover points.
            int crossoverPoint1 = random.nextInt(this.problem.rooms.size());
            int crossoverPoint2 = random.nextInt(this.problem.rooms.size());

            // A list of courses that have been scheduled.
            List<Integer> scheduledCourses = new ArrayList<Integer>();

            // This ensures crossoverPoint2 is greater than crossoverPoint1.
            while (crossoverPoint2 < crossoverPoint1) {
                crossoverPoint2 = random.nextInt(this.problem.rooms.size());
            }

            // Copy data between these points in parent 1 to child 1.
            for (int i = crossoverPoint1; i <= crossoverPoint2; i++) {
                // child1.schedule.schedule[i] = parents.get(0).schedule.schedule[i];

                for (int j = 0; j < child1.schedule.schedule[i].length; j++) {
                    child1.schedule.schedule[i][j] = parents.get(0).schedule.schedule[i][j];

                    if (child1.schedule.schedule[i][j] != -1) {
                        scheduledCourses.add(child1.schedule.schedule[i][j]);
                    }
                }
            }

            // Now put the other rows from parent 2 into child 1.
            int index = crossoverPoint2;
            for (int i = 0; i < parents.get(1).schedule.schedule.length
                    - ((crossoverPoint2 - crossoverPoint1) + 1); i++) {
                index++;
                if (index >= parents.get(1).schedule.schedule.length) {
                    index = 0;
                }

                for (int j = 0; j < child1.schedule.schedule[i].length; j++) {
                    if (scheduledCourses.contains(parents.get(1).schedule.schedule[index][j]) == false) {
                        child1.schedule.schedule[index][j] = parents.get(1).schedule.schedule[index][j];
                        scheduledCourses.add(child1.schedule.schedule[index][j]);

                    }
                }

            }

            // Create new crossover points for the next child.
            crossoverPoint1 = random.nextInt(this.problem.rooms.size());
            crossoverPoint2 = random.nextInt(this.problem.rooms.size());

            // Re-initializes courses that have been scheduled.
            scheduledCourses = new ArrayList<Integer>();

            // This ensures crossoverPoint2 is greater than 1.
            while (crossoverPoint2 < crossoverPoint1) {
                crossoverPoint2 = random.nextInt(this.problem.rooms.size());
            }

            // Copy data between these points in parent2 to child 2.
            for (int i = crossoverPoint1; i <= crossoverPoint2; i++) {
                // child1.schedule.schedule[i] = parents.get(0).schedule.schedule[i];

                for (int j = 0; j < child2.schedule.schedule[i].length; j++) {
                    child2.schedule.schedule[i][j] = parents.get(1).schedule.schedule[i][j];

                    if (child2.schedule.schedule[i][j] != -1) {
                        scheduledCourses.add(child2.schedule.schedule[i][j]);
                    }
                }
            }

            // Now put the other rows in parent 1 into child 2.
            index = crossoverPoint2;
            for (int i = 0; i < parents.get(0).schedule.schedule.length
                    - ((crossoverPoint2 - crossoverPoint1) + 1); i++) {
                index++;
                if (index >= parents.get(0).schedule.schedule.length) {
                    index = 0;
                }

                for (int j = 0; j < child2.schedule.schedule[i].length; j++) {
                    if (scheduledCourses.contains(parents.get(0).schedule.schedule[index][j]) == false) {
                        child2.schedule.schedule[index][j] = parents.get(0).schedule.schedule[index][j];
                        scheduledCourses.add(child2.schedule.schedule[index][j]);

                    }
                }

            }
        }

        // Create reference to new population created (the new children).
        List<ScoredSchedule> newScoredPopulaton = new ArrayList<ScoredSchedule>();
        newScoredPopulaton.add(new ScoredSchedule(0.0, child1.schedule));
        newScoredPopulaton.add(new ScoredSchedule(0.0, child2.schedule));

        // // Attempt to mutate the children. Then add them to the list while
        // calculating
        // // their score.
        // child1 = mutation(this.mutationRate, child1);
        // newScoredPopulaton.add(new
        // ScoredSchedule(checkSampleFitness(child1.schedule), child1.schedule));

        // child2 = mutation(this.mutationRate, child2);
        // newScoredPopulaton.add(new
        // ScoredSchedule(checkSampleFitness(child2.schedule), child2.schedule));

        return newScoredPopulaton;

    }

    private List<ScoredSchedule> onePointCrossover(double crossoverRate, List<ScoredSchedule> parents) {
        Random random = new Random();

        // Create two new child objects to hold their score and schedule.
        ScoredSchedule child1 = new ScoredSchedule(0.0, parents.get(0).schedule);
        ScoredSchedule child2 = new ScoredSchedule(0.0, parents.get(1).schedule);

        // Get a random value between 0 amd 1.
        double randomValue = 0 + (1 - 0) * random.nextDouble();
        // Determine if we crossover.
        if (randomValue < crossoverRate) {

            List<Integer> scheduledCourses = new ArrayList<Integer>();

            // If we crossover, make the childrens schedules empty.
            // child1.schedule = problem.getEmptySchedule();
            // child2.schedule = problem.getEmptySchedule();

            int crossoverPoint = random.nextInt(this.problem.rooms.size());

            child1.schedule = problem.getEmptySchedule();
            child2.schedule = problem.getEmptySchedule();
            // System.out.println(crossoverPoint);

            // for (int i = 0; i < problem.rooms.size(); i++) {
            //     if (i < crossoverPoint)
            //     {
            //         child1.schedule.schedule[i] = parents.get(0).schedule.schedule[i];
            //     }
            //     else
            //     {
            //         child1.schedule.schedule[i] = parents.get(1).schedule.schedule[i];
            //     }
            // }

            for (int i = 0; i < child1.schedule.schedule.length; i++) {
                for (int j = 0; j < child1.schedule.schedule[i].length; j++) {

                    if (i < crossoverPoint) {
                        child1.schedule.schedule[i][j] = parents.get(0).schedule.schedule[i][j];

                        if (parents.get(0).schedule.schedule[i][j] != -1) {
                            scheduledCourses.add(parents.get(0).schedule.schedule[i][j]);
                        }

                    } else {
                        if (scheduledCourses.contains(parents.get(1).schedule.schedule[i][j]) == false) {
                            child1.schedule.schedule[i][j] = parents.get(1).schedule.schedule[i][j];
                        }
                    }

                }
            }

            // printSchedule(child1.schedule);
            // System.exit(0);

            scheduledCourses = new ArrayList<Integer>();

            for (int i = 0; i < child2.schedule.schedule.length; i++) {
                for (int j = 0; j < child2.schedule.schedule[i].length; j++) {

                    if (i < crossoverPoint) {
                        child2.schedule.schedule[i][j] = parents.get(1).schedule.schedule[i][j];

                        if (parents.get(1).schedule.schedule[i][j] != -1) {
                            scheduledCourses.add(parents.get(1).schedule.schedule[i][j]);
                        }

                    } else {
                        if (scheduledCourses.contains(parents.get(0).schedule.schedule[i][j]) == false) {
                            child2.schedule.schedule[i][j] = parents.get(0).schedule.schedule[i][j];
                        }
                    }

                }
            }

        }

        // Create reference to new population created (the new children).
        List<ScoredSchedule> newScoredPopulaton = new ArrayList<ScoredSchedule>();
        newScoredPopulaton.add(new ScoredSchedule(0.0, child1.schedule));
        newScoredPopulaton.add(new ScoredSchedule(0.0, child2.schedule));

        // Attempt to mutate the children. Then add them to the list while calculating
        // their score.
        // child1 = mutation(this.mutationRate, child1);
        // newScoredPopulaton.add(new
        // ScoredSchedule(checkSampleFitness(child1.schedule), child1.schedule));

        // child2 = mutation(this.mutationRate, child2);
        // newScoredPopulaton.add(new
        // ScoredSchedule(checkSampleFitness(child2.schedule), child2.schedule));

        // printPopulation(newScoredPopulaton, false);

        return newScoredPopulaton;
    }

    // This is the mutation function. Given a child, will mutate them based on a
    // rate. Returns the given child.
    // This mutation is based on swapping two random courses in the schedule.
    private List<ScoredSchedule> mutation(double mutationRate, List<ScoredSchedule> children) {
        Random random = new Random();

        for (ScoredSchedule child : children) {
            // Get a random value between 0 amd 1.
            double randomValue = 0 + (1 - 0) * random.nextDouble();
            if (randomValue < mutationRate) {
                // Get a random room and timeslot.
                int ranRoom1 = random.nextInt(problem.rooms.size());
                int ranTimeslot1 = random.nextInt(problem.courses.get(0).timeSlotValues.length);

                // Get another random room and timeslot.
                int ranRoom2 = random.nextInt(problem.rooms.size());
                int ranTimeslot2 = random.nextInt(problem.courses.get(0).timeSlotValues.length);

                while (child.schedule.schedule[ranRoom1][ranTimeslot1] == -1
                        || child.schedule.schedule[ranRoom2][ranTimeslot2] == -1
                        || (problem.courses
                                .get(child.schedule.schedule[ranRoom1][ranTimeslot1]).timeSlotValues[ranTimeslot2] <= 0
                                || problem.courses.get(
                                        child.schedule.schedule[ranRoom2][ranTimeslot2]).timeSlotValues[ranTimeslot1] <= 0)) {
                    ranRoom1 = random.nextInt(problem.rooms.size());
                    ranTimeslot1 = random.nextInt(problem.courses.get(0).timeSlotValues.length);

                    ranRoom2 = random.nextInt(problem.rooms.size());
                    ranTimeslot2 = random.nextInt(problem.courses.get(0).timeSlotValues.length);
                }

                // Temporarly remember the first random room and timeslot.
                int tempCourseNum = child.schedule.schedule[ranRoom1][ranTimeslot1];

                // Swap the first random room and timeslots course with the other.
                child.schedule.schedule[ranRoom1][ranTimeslot1] = child.schedule.schedule[ranRoom2][ranTimeslot2];
                child.schedule.schedule[ranRoom2][ranTimeslot2] = tempCourseNum;
            }
        }

        // If random value is less than mutation rate, then we mutate.

        // return the mutated child
        return children;

    }

    // This gets the best schedule from the old population and puts it in the worse
    // schedule for the new population.
    private List<ScoredSchedule> elitisim(List<ScoredSchedule> newPopulation, List<ScoredSchedule> oldPopulation) {

        // Get the best schedule in the old population and store it in oldPopMax.
        ScoredSchedule oldPopMax = oldPopulation.get(0);
        for (ScoredSchedule scoredSchedule : oldPopulation) {
            if (oldPopMax.score < scoredSchedule.score) {
                oldPopMax = scoredSchedule;
            }
        }

        // Get the index of the worse population in the new population.
        ScoredSchedule newPopMin = newPopulation.get(0);
        int newMinIndex = 0;
        for (int i = 1; i < newPopulation.size(); i++) {
            if (newPopMin.score > newPopulation.get(i).score) {
                newPopMin = newPopulation.get(i);
                newMinIndex = i;
            }
        }

        // Replacing here.
        newPopulation.set(newMinIndex, oldPopMax);

        return newPopulation;

    }

    // Given parents and children, will return true if the children are better than
    // the parents.
    private boolean improvmentOccured(List<ScoredSchedule> children, List<ScoredSchedule> parents) {
        for (ScoredSchedule parent : parents) {
            if (children.get(0).score > parent.score || children.get(1).score > parent.score) {
                return true;
            }
        }

        return false;
    }

    private boolean threadCrossover()
    {
        Random random = new Random();

        // Get a random value between 0 amd 1.
        double randomValue = 0 + (1 - 0) * random.nextDouble();

        if (randomValue < 0.01) {


            List<ScoredSchedule> parents1 = new ArrayList<ScoredSchedule>();
            List<ScoredSchedule> parents2 = new ArrayList<ScoredSchedule>();

            parents1.add(tourneySelection(this.scoredPopulation));
            parents2.add(tourneySelection(this.scoredPopulation));

            if (numThreads < 2) {
                return false;
            }

            int randomThread = random.nextInt(numThreads);

            while (randomThread == myThreadNum) {
                randomThread = random.nextInt(numThreads);
            }

            if (threads.get(randomThread).scoredPopulation.size() < 2) {
                return false;
            }

            parents1.add(tourneySelection(threads.get(randomThread).scoredPopulation));
            parents2.add(tourneySelection(threads.get(randomThread).scoredPopulation));

            // Get a list of children (onlt two) from crossing over the two parents.
            List<ScoredSchedule> children1 = onePointCrossover(this.crossoverRate, parents1);

            scoredPopulation.set(random.nextInt(scoredPopulation.size()), children1.get(0));
            scoredPopulation.set(random.nextInt(scoredPopulation.size()), children1.get(1));

            List<ScoredSchedule> children2 = onePointCrossover(this.crossoverRate, parents2);
            threads.get(randomThread).scoredPopulation
                    .set(random.nextInt(threads.get(randomThread).scoredPopulation.size()), children2.get(0));
            threads.get(randomThread).scoredPopulation
                    .set(random.nextInt(threads.get(randomThread).scoredPopulation.size()), children2.get(1));
        }

        return true;
    }

    // Given a population, retunrs the best schedule from that population.
    public static ScoredSchedule getBestSchedule(List<ScoredSchedule> population) {

        ScoredSchedule populationMax = population.get(0);

        for (ScoredSchedule scoredSchedule : population) {
            if (populationMax.score < scoredSchedule.score) {
                populationMax = scoredSchedule;
            }
        }

        return populationMax;
    }

    private void printBestScore(List<ScoredSchedule> population) {
        ScoredSchedule populationMax = null;
        for (int i = 1; i < population.size(); i++) {
            populationMax = population.get(i - 1);

            if (populationMax.score < population.get(i).score) {
                populationMax = population.get(i);
            }
        }

        System.out.println("Populations Best: " + populationMax.score);
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

    private void printPopulation(List<ScoredSchedule> population, boolean printSchedule) {
        for (ScoredSchedule scoredSchedule : population) {
            System.out.println("_____SCORE: " + scoredSchedule.score + "_____");
            if (printSchedule) {
                printSchedule(scoredSchedule.schedule);
            }
        }
    }

    private void printNonScoredPopulation(List<Schedule> population) {
        for (Schedule schedule : population) {
            printSchedule(schedule);
        }
    }
}
