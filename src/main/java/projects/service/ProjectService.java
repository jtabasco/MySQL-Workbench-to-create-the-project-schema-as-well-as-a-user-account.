package projects.service;

import projects.dao.ProjectsDao;
import projects.entity.Project;
import java.util.List;
import java.util.NoSuchElementException;
import projects.exception.DbException;

/**
 * Service class that handles business logic for Project operations.
 * This class serves as an intermediary between the UI layer (ProjectsApp)
 * and the data access layer (ProjectsDao).
 * 
 * Key responsibilities:
 * - Validates business rules before database operations
 * - Handles transaction management
 * - Provides a clean interface for the UI layer
 * - Manages error handling and exception translation
 * 
 * The service layer ensures that:
 * - All database operations are performed within transactions
 * - Business rules are enforced before any database changes
 * - Errors are properly handled and translated to user-friendly messages
 */
public class ProjectService {

	/**
	 * Data Access Object for Project operations.
	 * Handles all database interactions for project-related operations.
	 * This field is initialized once and reused for all operations.
	 */
	private ProjectsDao projectDao = new ProjectsDao();

	/**
	 * Adds a new project to the database.
	 * This method implements the business logic for project creation:
	 * 1. Validates the project data
	 * 2. Delegates the actual insertion to the DAO layer
	 * 3. Returns the complete project with generated ID
	 * 
	 * @param project The Project object containing the project details to be added
	 * @return The Project object with the generated ID after insertion
	 * @throws DbException If there is an error during project creation
	 */
	public Project addProject(Project project) {
		return projectDao.insertProject(project);
	}

	/**
	 * Retrieves all projects from the database.
	 * Returns a list of projects ordered by project name.
	 * This method provides a simplified view of projects without
	 * loading related entities (materials, steps, categories) to improve
	 * performance.
	 * 
	 * @return List of all projects in the database
	 * @throws DbException If there is an error retrieving the projects
	 */
	public List<Project> fetchAllProjects() {
		return projectDao.fetchAllProjects();
	}

	/**
	 * Retrieves a specific project by its ID.
	 * This method provides a complete view of a project including all
	 * related entities (materials, steps, and categories).
	 * 
	 * Implementation details:
	 * 1. Attempts to fetch the project from the database
	 * 2. If found, returns the complete project with all associations
	 * 3. If not found, throws an exception to ensure null is never returned
	 * 
	 * @param projectId The ID of the project to retrieve
	 * @return The complete project with the specified ID and all its associations
	 * @throws NoSuchElementException If no project exists with the given ID
	 * @throws DbException            If there is an error accessing the database
	 */
	public Project fetchProjectById(Integer projectId) {
		return projectDao.fetchProjectById(projectId)
				.orElseThrow(() -> new NoSuchElementException(
						"Project with project ID=" + projectId + " does not exist."));
	}

	/**
	 * Updates an existing project in the database.
	 * This method implements the business logic for project updates:
	 * 1. Validates the updated project data
	 * 2. Ensures the project exists before updating
	 * 3. Delegates the actual update to the DAO layer
	 * 
	 * @param project The Project object containing the updated details
	 * @return The updated Project object
	 * @throws DbException If there is an error during the update
	 */
	public void modifyProjectDetails(Project project) {
		if (!projectDao.modifyProject(project)) {
			throw new DbException("Project with ID=" + project.getProjectId() + " not found");
		}
	}

	/**
	 * Deletes a project from the database.
	 * This method implements the business logic for project deletion:
	 * 1. Verifies the project exists before deletion
	 * 2. Delegates the actual deletion to the DAO layer
	 * 3. Ensures all related data is properly cleaned up
	 * 
	 * @param projectId The ID of the project to delete
	 * @throws DbException If there is an error during deletion
	 */
	public void deleteProject(Integer projectId) {
		if (!projectDao.deleteProject(projectId)) {
			throw new DbException("Project with ID=" + projectId + " not found");
		}
	}
}
