package projects.service;

import projects.dao.ProjectsDao;
import projects.entity.Project;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Service class that handles business logic for Project operations.
 * This class serves as an intermediary between the UI layer (ProjectsApp)
 * and the data access layer (ProjectsDao).
 * Provides methods for creating, retrieving, and managing projects.
 */
public class ProjectService {

	/**
	 * Data Access Object for Project operations.
	 * Handles all database interactions for project-related operations.
	 */
	private ProjectsDao projectDao = new ProjectsDao();

	/**
	 * Adds a new project to the database.
	 * Delegates the actual insertion to the DAO layer while providing
	 * a simpler interface to the UI layer.
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
	 * Does not include associated materials, steps, or categories.
	 * 
	 * @return List of all projects in the database
	 * @throws DbException If there is an error retrieving the projects
	 */
	public List<Project> fetchAllProjects() {
		return projectDao.fetchAllProjects();
	}

	/**
	 * Retrieves a specific project by its ID.
	 * Includes all associated data (materials, steps, and categories).
	 * Throws an exception if the project is not found, ensuring that
	 * the returned project is never null.
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
}
