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
 * - Updating project information
 * - Deleting projects
 */
public class ProjectsApp {
	/** Scanner instance for reading user input from console */
	private Scanner scanner = new Scanner(System.in);

	/** Service layer instance for handling project operations */
	private ProjectService projectService = new ProjectService();

	/** Tracks the currently selected project in the application */
	private Project curProject;

	/**
	 * List of available menu operations with their descriptions.
	 * Each string represents a menu option with its number and description.
	 * Used in printOperations() to display the menu to the user.
	 */
	//@formatter:off
	private List<String> operations = List.of(
			"1- Add a project",      // Option to create a new project
			"2- List projects",      // Option to view all projects
			"3- Select a project",   // Option to choose a project to work with
			"4- Update project details", // Option to modify existing project
			"5- Delete a project"   // Option to remove a project
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
	 * 
	 * The loop continues until the user chooses to exit (operation -1).
	 * Each operation is handled in a switch statement, with appropriate
	 * error handling for invalid inputs and database operations.
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
					case 4:
						updateProjectDetails();
						break;
					case 5:
						deleteProject();
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
	 * The method prompts the user for each field and validates the input:
	 * - Project name: String input, cannot be empty
	 * - Estimated hours: Decimal number with 2 decimal places
	 * - Actual hours: Decimal number with 2 decimal places
	 * - Difficulty level: Integer between 1 and 5
	 * - Project notes: Optional text input
	 * 
	 * After gathering all input, creates a new Project object and saves it
	 * to the database through the service layer.
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
	 * Uses the service layer to fetch the list of projects and then
	 * formats and displays each project's ID and name in a clean,
	 * indented list format.
	 * 
	 * Example output:
	 * Projects:
	 * 1: Project Alpha
	 * 2: Project Beta
	 * 3: Project Gamma
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

		} else {
			System.out.println("\nInvalid project ID selected.");
		}
	}

	/**
	 * Updates the details of the currently selected project.
	 * Requires a project to be selected first.
	 * 
	 * For each field, the method:
	 * 1. Shows the current value in square brackets
	 * 2. Prompts for a new value
	 * 3. If no new value is provided (user just presses Enter),
	 * keeps the existing value
	 * 4. Validates the input before updating
	 * 
	 * After all fields are processed, updates the project in the database
	 * and refreshes the current project reference.
	 */
	private void updateProjectDetails() {
		if (Objects.isNull(curProject)) {
			System.out.println("\nPlease select a project first.");
			return;
		}

		String projectName = getStringInput(
				"Enter the project name [" + curProject.getProjectName() + "]: ");
		BigDecimal estimatedHours = getDecimalInput(
				"Enter the estimated hours [" + curProject.getEstimatedHours() + "]: ");
		BigDecimal actualHours = getDecimalInput(
				"Enter the actual hours [" + curProject.getActualHours() + "]: ");
		Integer difficulty = getIntInput(
				"Enter the project difficulty (1-5) [" + curProject.getDifficulty() + "]: ");
		String notes = getStringInput(
				"Enter the project notes [" + curProject.getNotes() + "]: ");

		Project project = new Project();
		project.setProjectId(curProject.getProjectId());
		project.setProjectName(
				Objects.isNull(projectName) ? curProject.getProjectName() : projectName);
		project.setEstimatedHours(
				Objects.isNull(estimatedHours) ? curProject.getEstimatedHours() : estimatedHours);
		project.setActualHours(
				Objects.isNull(actualHours) ? curProject.getActualHours() : actualHours);
		project.setDifficulty(
				Objects.isNull(difficulty) ? curProject.getDifficulty() : difficulty);
		project.setNotes(
				Objects.isNull(notes) ? curProject.getNotes() : notes);

		projectService.modifyProjectDetails(project);
		curProject = projectService.fetchProjectById(curProject.getProjectId());
		System.out.println("\nProject updated successfully!");
	}

	/**
	 * Deletes the currently selected project.
	 * Requires a project to be selected first.
	 * 
	 * The deletion process:
	 * 1. Shows the list of all projects
	 * 2. Prompts for the project ID to delete
	 * 3. Verifies the project exists
	 * 4. Asks for confirmation before deletion
	 * 5. If confirmed, removes the project and clears the current project reference
	 * 
	 * Safety features:
	 * - Requires explicit project ID entry
	 * - Requires confirmation before deletion
	 * - Provides feedback on success or cancellation
	 */
	private void deleteProject() {
		listProjects();
		Integer projectId = getIntInput("Enter a project ID to delete a project: ");

		projectService.deleteProject(projectId);
		System.out.println("\nProject " + projectId + " deleted successfully!");

		if (Objects.nonNull(curProject) && curProject.getProjectId().equals(projectId)) {
			curProject = null;
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
	 * Shows a numbered list of possible actions the user can take.
	 * Also displays the currently selected project (if any).
	 * 
	 * The menu is formatted with:
	 * - A header "Here's what you can do:"
	 * - Each operation indented with 4 spaces
	 * - Current project status at the bottom
	 * 
	 * Example output:
	 * Here's what you can do:
	 * 1- Add a project
	 * 2- List projects
	 * 3- Select a project
	 * 4- Update project details
	 * 5- Delete a project
	 * 
	 * You have selected project: Project Alpha
	 */
	private void printOperations() {
		System.out.println();
		System.out.println("Here's what you can do:");
		// Iterate through each operation and print it with indentation
		operations.forEach(op -> System.out.println("   " + op));

		// Display current project status
		if (Objects.isNull(curProject)) {
			System.out.println("\nYou are not working with a project.");
		} else {
			System.out.println("\nYou have selected project: " + curProject);
		}
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
		Integer op = getIntInput("\nEnter an operation number (press Enter to Quit): ");
		return Objects.isNull(op) ? -1 : op;
	}
}
