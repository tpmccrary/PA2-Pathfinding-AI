package PA2_Pathfinding_AI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class GeneticAlgorithm {

    private SchedulingProblem problem;
    private double deadline;

    private int populationAmmount = 500;
    private double crossoverRate = 0.5;
    private double mutationRate = 0.05;
    private int tourneySize = 8;

    private int improvmentCounter = 0;
    private int counterLimit = 500;

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

        // Create a random population. A list of schedules.
        List<Schedule> population = genRanPopulation(this.populationAmmount);

        // List of objects to hold schedules and their scores.
        List<ScoredSchedule> scoredPopulation = new ArrayList<ScoredSchedule>();

        // Go through the population, giving each sample a score.
        for (Schedule schedule : population) {
            scoredPopulation.add(new ScoredSchedule(checkSampleFitness(schedule), schedule));
        }

        while (improvmentCounter < counterLimit) {

            printBestScore(scoredPopulation);

            List<ScoredSchedule> newScoredPopulation = new ArrayList<ScoredSchedule>();

            while (newScoredPopulation.size() < scoredPopulation.size()) {
                // Make a list to hold the parents.
                List<ScoredSchedule> parents = new ArrayList<ScoredSchedule>();

                // Get the two parents using tourney selection.
                parents.add(tourneySelection(scoredPopulation));
                parents.add(tourneySelection(scoredPopulation));

                List<ScoredSchedule> children = crossover(this.crossoverRate, parents);

                // Compare children to the best of the last population?

                // // check improvment, if no improvment, up the counter.
                // if (isImprovment(children, parents) == false) {
                //     improvmentCounter++;
                // } else {
                //     // System.out.println("IMPROVMENT");
                //     improvmentCounter = 0;
                // }

                improvmentCounter++;

                // Add children to new population.
                for (ScoredSchedule child : children) {
                    newScoredPopulation.add(new ScoredSchedule(checkSampleFitness(child.schedule), child.schedule));
                }

            }

            scoredPopulation = elitisim(newScoredPopulation, scoredPopulation);

            // printPopulation(scoredPopulation);
        }

        // printPopulation(scoredPopulation);
        
        System.out.println("___SCORE: " + scoredPopulation.get(0).score);
        printSchedule(scoredPopulation.get(0).schedule);

        return scoredPopulation.get(0).schedule;

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

        List<Integer> scheduledCourses = new ArrayList<Integer>();

        // Iterate through the ammount of samples we want.
        for (int i = 0; i < ammount; i++) {
            // Creates an empty schedule.
            Schedule sampleSchedule = problem.getEmptySchedule();

            // Populate the schedule randomly in this loop.
            for (int j = 0; j < sampleSchedule.schedule.length; j++) {

                for (int k = 0; k < sampleSchedule.schedule[j].length; k++) {

                    if (scheduledCourses.size() == problem.courses.size()) {
                        break;
                    }

                    // Get a random course number.
                    int ranCourse = random.nextInt(problem.courses.size());

                    // If the course is not used, then schedule it, else, I keep trying to generate
                    // a random course that is not used.
                    while (scheduledCourses.contains(ranCourse)) {
                        ranCourse = random.nextInt(problem.courses.size());
                    }

                    // Schedule random course.
                    sampleSchedule.schedule[j][k] = ranCourse;
                    scheduledCourses.add(ranCourse);

                }
            }

            // Add this new sample to the population and repeat.
            schedulePop.add(sampleSchedule);
            scheduledCourses = new ArrayList<Integer>();
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

        double score = 0.0;
        int course = -1;

        List<Integer> scheduledCourses = new ArrayList<Integer>();

        for (int i = 0; i < sampSchedule.schedule.length; i++) {
            for (int j = 0; j < sampSchedule.schedule[i].length; j++) {
                course = sampSchedule.schedule[i][j];

                if (course == -1) {
                    continue;
                }

                // Check if that course is duplicated somewhere else in the schedule. If so
                // return.
                if (scheduledCourses.contains(course)) {
                    System.out.println("Duplicate coures");
                    return 0.0;
                }

                // Check to see if that course can be scheduled there. If so, return.
                if (problem.courses.get(course).timeSlotValues[j] == 0) {
                    // System.out.println("Course cannot be scheduled in this time slot");
                    // return 0.0;
                    sampSchedule.schedule[i][j] = -1;
                }

                // Check to see if the number of enrolled students is greater than the room
                // number. If so, return.
                if (problem.courses.get(course).enrolledStudents > problem.rooms.get(i).capacity) {
                    // System.out.println("Room capacity is to small.");
                    // return 0.0;
                    sampSchedule.schedule[i][j] = -1;
                }

                int timeslotBonus = problem.courses.get(course).timeSlotValues[j];

                score += timeslotBonus;

                // Here we will check if the course is in the preffered building.
                Building preferredBuild;
                Building actualBuild;

                preferredBuild = problem.courses.get(course).preferredLocation;
                actualBuild = problem.rooms.get(i).b;

                // If the coordinates are not the same, then we have to get the distance and
                // subtract it from thes score.
                if (preferredBuild.xCoord != actualBuild.xCoord || preferredBuild.yCoord != actualBuild.yCoord) {
                    double distance = 0.0;

                    distance = Math.sqrt(Math.abs((preferredBuild.xCoord - actualBuild.xCoord)
                            + (preferredBuild.yCoord - actualBuild.yCoord)));

                    score = score - distance;
                }

            }
        }

        return score;
    }

    private ScoredSchedule tourneySelection(List<ScoredSchedule> scoredPopulation) {
        Random random = new Random();

        ScoredSchedule contender1 = scoredPopulation.get(random.nextInt(scoredPopulation.size()));
        ScoredSchedule contender2 = contender1;

        List<ScoredSchedule> contenders = new ArrayList<ScoredSchedule>();

        for (int i = 0; i < tourneySize; i++) {
            contenders.add(scoredPopulation.get(random.nextInt(scoredPopulation.size())));
        }

        ScoredSchedule bestContender = contenders.get(0);
        for (ScoredSchedule contender : contenders) {
            if (bestContender.score < contender.score)
            {
                bestContender = contender;
            }
        }

        return bestContender;
    }

    private List<ScoredSchedule> crossover(double crossoverRate, List<ScoredSchedule> parents) {
        Random random = new Random();

        ScoredSchedule child1 = new ScoredSchedule(parents.get(0).score, parents.get(0).schedule);
        ScoredSchedule child2 = new ScoredSchedule(parents.get(1).score, parents.get(1).schedule);

        // Get a random value between 0 amd 1.
        double randomValue = 0 + (1 - 0) * random.nextDouble();

        if (randomValue < crossoverRate) {
            ScoredSchedule child1Temp = new ScoredSchedule(parents.get(0).score, parents.get(0).schedule);

            // Start at half way point. Cross over the parents.
            for (int i = child1.schedule.schedule.length / 2; i < child1.schedule.schedule.length; i++) {
                for (int j = 0; j < child1.schedule.schedule[i].length; j++) {
                    child1.schedule.schedule[i][j] = child2.schedule.schedule[i][j];
                    child2.schedule.schedule[i][j] = child1Temp.schedule.schedule[i][j];
                }
            }
        }

        List<ScoredSchedule> newScoredPopulaton = new ArrayList<ScoredSchedule>();

        newScoredPopulaton.add(mutation(this.mutationRate, child1));
        newScoredPopulaton.add(mutation(this.mutationRate, child2));

        return newScoredPopulaton;

    }

    private ScoredSchedule mutation(double mutationRate, ScoredSchedule child) {
        Random random = new Random();

        // Go through every row in the schedule.
        for (int i = 0; i < child.schedule.schedule.length; i++) {
            for (int j = 0; j < child.schedule.schedule[i].length; j++) {
                // Get a random value between 0 amd 1.
                double randomValue = 0 + (1 - 0) * random.nextDouble();

                // If random value is less than mutation rate, mutate the row.
                if (randomValue < mutationRate) {
                    // Randomlly assign a course to that room (row);
                    child.schedule.schedule[i][j] = random.nextInt(problem.courses.size());
                }
            }

            // // Get a random value between 0 amd 1.
            // double randomValue = 0 + (1 - 0) * random.nextDouble();

            // // If random value is less than mutation rate, mutate the row.
            // if (randomValue < mutationRate) {
            // // Set the rows to -1.
            // for (int j = 0; j < child.schedule.schedule[i].length; j++) {
            // child.schedule.schedule[i][j] = -1;
            // }
            // // Randomlly assign a course to that room (row);
            // child.schedule.schedule[i][random.nextInt(child.schedule.schedule[i].length)]
            // = random
            // .nextInt(problem.courses.size());
            // }
        }

        // return the mutated child
        return child;

    }

    private List<ScoredSchedule> elitisim(List<ScoredSchedule> newPopulation, List<ScoredSchedule> oldPopulation) {
        ScoredSchedule oldPopMax = null;
        for (int i = 1; i < oldPopulation.size(); i++) {
            oldPopMax = oldPopulation.get(i - 1);

            if (oldPopMax.score < oldPopulation.get(i).score) {
                oldPopMax = oldPopulation.get(i);
            }
        }

        ScoredSchedule newPopMin;
        int newMinIndex = 0;
        for (int i = 1; i < newPopulation.size(); i++) {
            newPopMin = newPopulation.get(i - 1);

            if (newPopMin.score > newPopulation.get(i).score) {
                newPopMin = newPopulation.get(i);
                newMinIndex = i;
            }
        }

        newPopulation.set(newMinIndex, oldPopMax);

        return newPopulation;

    }

    private boolean isImprovment(List<ScoredSchedule> children, List<ScoredSchedule> parents) {
        for (ScoredSchedule parent : parents) {
            if (children.get(0).score > parent.score || children.get(1).score > parent.score) {
                return true;
            }
        }

        return false;
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

    private void printPopulation(List<ScoredSchedule> population) {
        for (ScoredSchedule scoredSchedule : population) {
            System.out.println("_____SCORE: " + scoredSchedule.score + "_____");
            printSchedule(scoredSchedule.schedule);
        }
    }

    private void printNonScoredPopulation(List<Schedule> population) {
        for (Schedule schedule : population) {
            printSchedule(schedule);
        }
    }
}
