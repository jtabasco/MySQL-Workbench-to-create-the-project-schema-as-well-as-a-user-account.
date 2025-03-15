package projects;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import projects.entity.Project;
import projects.exception.DbException;
import projects.service.ProjectService;

/**
 * Main application class for the Projects Management System.
 * This class provides a console-based user interface for managing projects.
 * Features include:
 * - Creating new projects
 * - Listing existing projects
 * - Selecting and viewing project details
 */
public class ProjectsApp {
	/** Scanner instance for reading user input from console */
	private Scanner scanner = new Scanner(System.in);

	/** Service layer instance for handling project operations */
	private ProjectService projectService = new ProjectService();

	/** Tracks the currently selected project in the application */
	private Project curProject;

	/** List of available menu operations with their descriptions */
	//@formatter:off
	private List<String> operations = List.of(
			"1- Add a project",
			"2- List projects",
			"3- Select a project"
	);
	//@formatter:on

	/**
	 * Application entry point. Creates a new instance of ProjectsApp
	 * and starts the menu loop.
	 * 
	 * @param args Command line arguments (not used)
	 */
	public static void main(String[] args) {
		new ProjectsApp().displayMenu();
	}

	/**
	 * Main menu loop of the application.
	 * Continuously displays menu options and processes user input until exit.
	 * Handles all exceptions gracefully and displays user-friendly error messages.
	 */
	private void displayMenu() {
		boolean done = false;

		while (!done) {

			try {
				int operation = getOperation();
				switch (operation) {
					case -1:
						done = exitMenu();
						break;
					case 1:
						createProject();
						break;
					case 2:
						listProjects();
						break;
					case 3:
						selectProject();
						break;
					default:
						System.out.println("\n" + operation + " It is not a valid option number. Try Again.");
						break;
				}
			} catch (Exception e) {
				System.out.println("\nError: " + e.toString() + " Try Again!!");
			}

		}

	}

	/**
	 * Creates a new project by gathering user input for all required fields.
	 * Prompts user for:
	 * - Project name (string)
	 * - Estimated hours (decimal)
	 * - Actual hours (decimal)
	 * - Difficulty level (integer 1-5)
	 * - Project notes (string)
	 * After gathering input, creates and saves project to database.
	 */
	private void createProject() {
		String projectName = getStringInput("Enter de project name: ");
		BigDecimal estimatedHours = getDecimalInput("Enter de estimated Hours: ");
		BigDecimal actualHours = getDecimalInput("Enter de actual Hours: ");
		Integer difficulty = getValidDifficultyInput("Enter the project difficulty (1-5): ");
		String notes = getStringInput("Enter the project notes: ");

		Project project = new Project();

		project.setProjectName(projectName);
		project.setEstimatedHours(estimatedHours);
		project.setActualHours(actualHours);
		project.setDifficulty(difficulty);
		project.setNotes(notes);

		Project dbProject = projectService.addProject(project);
		System.out.println("\nYou have successfully created project: " + dbProject);

	}

	/**
	 * Retrieves and displays all projects from the database.
	 * Shows each project's ID and name in a formatted list.
	 * Used both for viewing projects and as part of project selection.
	 */
	private void listProjects() {
		List<Project> projects = projectService.fetchAllProjects();
		System.out.println("\nProjects:");
		projects.forEach(
				project -> System.out.println("   " + project.getProjectId() + ": " + project.getProjectName()));
	}

	/**
	 * Handles the project selection process.
	 * First displays list of available projects, then prompts for project ID.
	 * Updates curProject with selected project if valid ID is provided.
	 * Provides appropriate feedback messages for successful or failed selection.
	 */
	private void selectProject() {
		listProjects();
		Integer projectId = getIntInput("Enter a project ID to select a project: ");

		curProject = null;

		if (projectId != null) {
			curProject = projectService.fetchProjectById(projectId);
			if (Objects.isNull(curProject)) {
				System.out.println("\nInvalid project ID selected.");
			} else {
				System.out.println("\nYou have selected project: " + curProject);
			}
		} else {
			System.out.println("\nInvalid project ID selected.");
		}
	}

	/**
	 * Validates and processes project difficulty input.
	 * Continuously prompts until a valid value (1-5) is entered.
	 * 
	 * @param prompt The message to display to user
	 * @return A valid difficulty value between 1 and 5
	 */
	private Integer getValidDifficultyInput(String prompt) {
		while (true) {
			Integer difficulty = getIntInput(prompt);

			if (difficulty != null && difficulty >= 1 && difficulty <= 5) {
				return difficulty;
			}

			System.out.println("\nDifficulty must be between 1 and 5. Please try again.");
		}
	}

	/**
	 * Processes decimal number input with validation.
	 * Converts input to BigDecimal with scale of 2.
	 * 
	 * @param prompt The message to display to user
	 * @return BigDecimal value or null if input is empty
	 * @throws DbException if input is not a valid decimal number
	 */
	private BigDecimal getDecimalInput(String prompt) {
		String input = getStringInput(prompt);
		if (Objects.isNull(input)) {
			return null;
		}
		try {
			return new BigDecimal(input).setScale(2);
		} catch (NumberFormatException e) {
			throw new DbException(input + " is not a valid number.");
		}
	}

	/**
	 * Processes integer input with validation.
	 * 
	 * @param prompt The message to display to user
	 * @return Integer value or null if input is empty
	 * @throws DbException if input is not a valid integer
	 */
	private Integer getIntInput(String prompt) {
		String input = getStringInput(prompt);
		if (Objects.isNull(input)) {
			return null;
		}
		try {
			return Integer.parseInt(input);
		} catch (NumberFormatException e) {
			throw new DbException(input + " is not a valid number.");
		}
	}

	/**
	 * Gets and processes raw string input from user.
	 * Handles whitespace trimming and blank input conversion.
	 * 
	 * @param prompt The message to display to user
	 * @return Trimmed string or null if input is blank
	 */
	private String getStringInput(String prompt) {
		System.out.print(prompt);
		String line = scanner.nextLine();

		return line.isBlank() ? null : line.trim();
	}

	/**
	 * Displays formatted menu of available operations.
	 * Shows numbered list of possible actions user can take.
	 */
	private void printOperations() {
		System.out.println();
		System.out.println("Here's what you can do:");
		operations.forEach(op -> System.out.println("   " + op));

	}

	/**
	 * Handles program termination.
	 * Displays exit message and signals menu loop to end.
	 * 
	 * @return true to indicate menu should exit
	 */
	private boolean exitMenu() {
		System.out.println("\nExiting the menu.");
		return true;
	}

	/**
	 * Gets the user's menu selection
	 * 
	 * @return the selected operation number or -1 to exit
	 */
	private int getOperation() {
		printOperations();
		Integer op = getIntInput("\nEnter an opertion number (press Enter to Quit): ");
		return Objects.isNull(op) ? -1 : op;

	}

}
