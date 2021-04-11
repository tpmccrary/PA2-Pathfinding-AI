package PA2_Pathfinding_AI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class GeneticAlgorithm {

    // The scheduling problem itself.
    private SchedulingProblem problem;
    private double deadline;

    // How may schedules in a populaiton.
    private int populationAmount = 100;
    // The rate at which crossovers are preformed. MUST be between 0 and 1.
    private double crossoverRate = 0.5;
    // The rate at which mutations are preformed. MUST be between 0 and 1.
    private double mutationRate = 0.05;
    // The partipant size for a tourney.
    private int tourneySize = 8;

    private int improvmentCounter = 0;
    private int counterLimit = 800;

    // Constructor.
    GeneticAlgorithm(SchedulingProblem problem, double deadline) {
        this.problem = problem;
        this.deadline = deadline;
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
        Schedule solution = problem.getEmptySchedule();

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
        List<ScoredSchedule> scoredPopulation = new ArrayList<ScoredSchedule>();

        // Go through the population, giving each sample a score. This puts the schedule and score into
        // an object, and that object gets put into the prev list.
        for (Schedule schedule : population) {
            scoredPopulation.add(new ScoredSchedule(checkSampleFitness(schedule), schedule));
        }

        boolean passedDeadline = false;
        while (improvmentCounter < counterLimit && passedDeadline == false) {

            if (System.currentTimeMillis() > deadline)
            {
                passedDeadline = true;
            }
      
            // Initialize a list that will be our new population.
            List<ScoredSchedule> newScoredPopulation = new ArrayList<ScoredSchedule>();

            // Keep adding to the new population until its the same size as the old population.
            while (newScoredPopulation.size() < scoredPopulation.size()) {
                // Make a list to hold the parents.
                List<ScoredSchedule> parents = new ArrayList<ScoredSchedule>();

                // Get the two parents using tourney selection.
                parents.add(tourneySelection(scoredPopulation));
                parents.add(tourneySelection(scoredPopulation));

                // while (parents.get(0).score == parents.get(1).score)
                // {
                //     parents = new ArrayList<ScoredSchedule>();
                //     parents.add(tourneySelection(scoredPopulation));
                //     parents.add(tourneySelection(scoredPopulation));
                // }

                // Get a list of children (onlt two) from crossing over the two parents.
                List<ScoredSchedule> children = crossover(this.crossoverRate, parents);

                if (improvmentOccured(children, parents) == false)
                {
                    improvmentCounter++;
                }
                else
                {
                    improvmentCounter = 0;
                }


                // Add the children to the new population.
                for (ScoredSchedule child : children) {
                    newScoredPopulation.add(new ScoredSchedule(checkSampleFitness(child.schedule), child.schedule));
                }

            }

            // Use elitism to get the best schedule from the previous population, and put it in the new one.
            scoredPopulation = elitisim(newScoredPopulation, scoredPopulation);

            // printBestScore(scoredPopulation);
            // improvmentCounter++;


        }

        // printPopulation(scoredPopulation);   
        
        ScoredSchedule bestSchedule = getBestSchedule(scoredPopulation);

        // printSchedule(scoredPopulation.get(0).schedule);

        System.out.println("BEST END___SCORE: " + bestSchedule.score);
        printSchedule(bestSchedule.schedule);

        printPopulation(scoredPopulation, false);

        return bestSchedule.schedule;

        // return solution;
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

            for (int j = 0; j < problem.courses.size(); j++) {
                int course = j;

                if (course >= problem.rooms.size() * problem.courses.get(course).timeSlotValues.length)
                {
                    break;
                }
    
                int ranRoom = random.nextInt(problem.rooms.size());
                int ranTimeslot = random.nextInt(problem.courses.get(course).timeSlotValues.length);

                while (sampleSchedule.schedule[ranRoom][ranTimeslot] != -1)
                {
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

    // Given a population, selects a set ammount for them to "fight" against eachother. One with best score wins and is returned.
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
            if (bestContender.score < contender.score)
            {
                bestContender = contender;
            }
        }

        return bestContender;
    }

    // This is the crossover function. This crossover function is based off OX1. 
    // Given parents, they are crossover to make children, and the children are then returned.
    private List<ScoredSchedule> crossover(double crossoverRate, List<ScoredSchedule> parents) {
        Random random = new Random();

        // Create two new child objects to hold their score and schedule.
        ScoredSchedule child1 = new ScoredSchedule(0.0, parents.get(0).schedule);

        ScoredSchedule child2 = new ScoredSchedule(0.0, parents.get(0).schedule);

        // Get a random value between 0 amd 1.
        double randomValue = 0 + (1 - 0) * random.nextDouble();
        // Determine if we crossover.
        if (randomValue < crossoverRate) {

            // If we crossover, make the childrens schedules empty.
            child1.schedule = problem.getEmptySchedule();
            child2.schedule = problem.getEmptySchedule();

            // USING OX1 Crossover.

            // Gets two crossover points.
            int crossoverPoint1 = random.nextInt(this.problem.rooms.size());
            int crossoverPoint2 = random.nextInt(this.problem.rooms.size());

            // A list of courses that have been scheduled.
            List<Integer> scheduledCourses = new ArrayList<Integer>();

            // This ensures crossoverPoint2 is greater than crossoverPoint1.
            while (crossoverPoint2 < crossoverPoint1)
            {
                crossoverPoint2 = random.nextInt(this.problem.rooms.size());
            }

            // Copy data between these points in parent 1 to child 1.
            for (int i = crossoverPoint1; i <= crossoverPoint2; i++) {
                // child1.schedule.schedule[i] = parents.get(0).schedule.schedule[i];

                for (int j = 0; j < child1.schedule.schedule[i].length; j++) {
                    child1.schedule.schedule[i][j] = parents.get(0).schedule.schedule[i][j];

                    if (child1.schedule.schedule[i][j] != -1)
                    {
                        scheduledCourses.add(child1.schedule.schedule[i][j]);
                    }
                }
            }

            // Now put the other rows from parent 2 into child 1.
            int index = crossoverPoint2;
            for (int i = 0; i < parents.get(1).schedule.schedule.length - ((crossoverPoint2 - crossoverPoint1) + 1); i++) {
                index++;
                if (index >= parents.get(1).schedule.schedule.length)
                {
                    index = 0;
                }

                for (int j = 0; j < child1.schedule.schedule[i].length; j++) {
                    if (scheduledCourses.contains(parents.get(1).schedule.schedule[index][j]) == false)
                    {
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
            while (crossoverPoint2 < crossoverPoint1)
            {
                crossoverPoint2 = random.nextInt(this.problem.rooms.size());
            }

            // Copy data between these points in parent2 to child 2.
            for (int i = crossoverPoint1; i <= crossoverPoint2; i++) {
                // child1.schedule.schedule[i] = parents.get(0).schedule.schedule[i];

                for (int j = 0; j < child2.schedule.schedule[i].length; j++) {
                    child2.schedule.schedule[i][j] = parents.get(1).schedule.schedule[i][j];

                    if (child2.schedule.schedule[i][j] != -1)
                    {
                        scheduledCourses.add(child2.schedule.schedule[i][j]);
                    }
                }
            }


            // Now put the other rows in parent 1 into child 2.
            index = crossoverPoint2;
            for (int i = 0; i < parents.get(0).schedule.schedule.length - ((crossoverPoint2 - crossoverPoint1) + 1); i++) {
                index++;
                if (index >= parents.get(0).schedule.schedule.length)
                {
                    index = 0;
                }

                for (int j = 0; j < child2.schedule.schedule[i].length; j++) {
                    if (scheduledCourses.contains(parents.get(0).schedule.schedule[index][j]) == false)
                    {
                        child2.schedule.schedule[index][j] = parents.get(0).schedule.schedule[index][j];
                        scheduledCourses.add(child2.schedule.schedule[index][j]);

                    }
                }

            }
        }
        
        // Create reference to new population created (the new children).
        List<ScoredSchedule> newScoredPopulaton = new ArrayList<ScoredSchedule>();

        // Attempt to mutate the children. Then add them to the list while calculating their score.
        child1 = mutation(this.mutationRate, child1);
        newScoredPopulaton.add(new ScoredSchedule(checkSampleFitness(child1.schedule), child1.schedule));

        child2 = mutation(this.mutationRate, child2);
        newScoredPopulaton.add(new ScoredSchedule(checkSampleFitness(child2.schedule), child2.schedule));


        return newScoredPopulaton;

    }

    // This is the mutation function. Given a child, will mutate them based on a rate. Returns the given child.
    // This mutation is based on swapping two random courses in the schedule.
    private ScoredSchedule mutation(double mutationRate, ScoredSchedule child) {
        Random random = new Random();


        // Get a random value between 0 amd 1.
        double randomValue = 0 + (1 - 0) * random.nextDouble();

        // If random value is less than mutation rate, then we mutate.
        if (randomValue < mutationRate) {
            // Get a random room and timeslot.
            int ranRoom1 = random.nextInt(problem.rooms.size());
            int ranTimeslot1 = random.nextInt(problem.courses.get(0).timeSlotValues.length);

            // Get another random room and timeslote.
            int ranRoom2 = random.nextInt(problem.rooms.size());
            int ranTimeslot2 = random.nextInt(problem.courses.get(0).timeSlotValues.length);

            // Temporarly remember the first random room and timeslot.
            int tempCourseNum = child.schedule.schedule[ranRoom1][ranTimeslot1];

            // Swap the first random room and timeslots course with the other.
            child.schedule.schedule[ranRoom1][ranTimeslot1] = child.schedule.schedule[ranRoom2][ranTimeslot2];
            child.schedule.schedule[ranRoom2][ranTimeslot2] = tempCourseNum;
        }

        // return the mutated child
        return child;

    }

    // This gets the best schedule from the old population and puts it in the worse schedule for the new population.
    private List<ScoredSchedule> elitisim(List<ScoredSchedule> newPopulation, List<ScoredSchedule> oldPopulation) {

        // Get the best schedule in the old population and store it in oldPopMax.
        ScoredSchedule oldPopMax = null;
        for (int i = 1; i < oldPopulation.size(); i++) {
            oldPopMax = oldPopulation.get(i - 1);

            if (oldPopMax.score < oldPopulation.get(i).score) {
                oldPopMax = oldPopulation.get(i);
            }
        }

        // Get the index of the worse population in the new population.
        ScoredSchedule newPopMin;
        int newMinIndex = 0;
        for (int i = 1; i < newPopulation.size(); i++) {
            newPopMin = newPopulation.get(i - 1);

            if (newPopMin.score > newPopulation.get(i).score) {
                newPopMin = newPopulation.get(i);
                newMinIndex = i;
            }
        }

        // Replacing here.
        newPopulation.set(newMinIndex, oldPopMax);

        return newPopulation;

    }

    private boolean improvmentOccured(List<ScoredSchedule> children, List<ScoredSchedule> parents) {
        for (ScoredSchedule parent : parents) {
            if (children.get(0).score > parent.score || children.get(1).score > parent.score) {
                return true;
            }
        }

        return false;
    }

    private ScoredSchedule getBestSchedule(List<ScoredSchedule> population)
    {
        ScoredSchedule populationMax = null;
        for (int i = 1; i < population.size(); i++) {
            populationMax = population.get(i - 1);

            if (populationMax.score < population.get(i).score) {
                populationMax = population.get(i);
            }
        }

        return populationMax;
    }

    private void printBestScore(List<ScoredSchedule> population)
    {
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
            if (printSchedule)
            {
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
