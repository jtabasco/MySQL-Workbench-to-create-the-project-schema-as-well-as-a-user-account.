package projects;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import projects.entity.Project;
import projects.exception.DbException;
import projects.service.ProjectService;

public class ProjectsApp {
	// Scanner for reading user input
	private Scanner scanner = new Scanner(System.in);
	// Service layer for project operations
	private ProjectService projectService = new ProjectService();
	// List of available operations in the menu
	//@formatter:off
	private List<String> operations = List.of(
			"1- Add a project"
			);
	//@formatter:on

	/**
	 * Main method to start the application
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {
		
		new ProjectsApp().displayMenu();

	}

	/**
	 * Displays and handles the main menu loop
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
				default:
					System.out.println("\n"+operation+ " is not valid. Try Again.");
					break;
					

				}
			} catch (Exception e) {
				System.out.println("\nError: " + e.toString() + " Try Again!!");
			}

		}

	}

	/**
	 * Creates a new project by gathering user input
	 * Collects project name, estimated hours, actual hours, difficulty, and notes
	 */
	private void createProject() {
		String projectName=getStringInput("Enter de project name: ");
		BigDecimal estimatedHours=getDecimalInput("Enter de estimated Hours: ");
		BigDecimal actualHours=getDecimalInput("Enter de actual Hours: ");
		Integer difficulty = getValidDifficultyInput("Enter the project difficulty (1-5): ");
		String notes=getStringInput("Enter the project notes: ");
		
		Project project = new Project();
		
		project.setProjectName(projectName);
		project.setEstimatedHours(estimatedHours);
		project.setActualHours(actualHours);
		project.setDifficulty(difficulty);
		project.setNotes(notes);
		
		Project dbProject =projectService.addProject(project);
		System.out.println("\nYou have successfully created project: "+ dbProject);
		

	}

	/**
	 * Handles the exit menu option
	 * @return true to indicate the menu should exit
	 */
	private boolean exitMenu() {
		System.out.println("\nExiting the menu.");
		return true;
	}

	/**
	 * Gets the user's menu selection
	 * @return the selected operation number or -1 to exit
	 */
	private int getOperation() {
		printOperations();
		Integer op = getIntInput("\nEnter an opertion number (press Enter to Quit): ");
		return Objects.isNull(op) ? -1 : op;

	}
	
	/**
	 * Gets and validates project difficulty input
	 * @param prompt The message to display to the user
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
	 * Converts user input to BigDecimal
	 * @param prompt The message to display to the user
	 * @return BigDecimal value or null if input is empty
	 * @throws DbException if input is not a valid number
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
	 * Converts user input to Integer
	 * @param prompt The message to display to the user
	 * @return Integer value or null if input is empty
	 * @throws DbException if input is not a valid number
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
	 * Gets string input from the user
	 * @param prompt The message to display to the user
	 * @return Trimmed string or null if input is blank
	 */
	private String getStringInput(String prompt) {
		System.out.print(prompt);
		String line = scanner.nextLine();

		return line.isBlank() ? null : line.trim();
	}

	/**
	 * Prints the available menu operations
	 */
	private void printOperations() {
		System.out.println();
		System.out.println("Here's what you can do:");
		operations.forEach(op -> System.out.println("   "+op));

	}

}
