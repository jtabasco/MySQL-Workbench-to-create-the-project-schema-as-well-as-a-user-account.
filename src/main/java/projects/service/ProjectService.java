package projects.service;

import projects.dao.ProjectsDao;
import projects.entity.Project;

/**
 * Service class that handles business logic for Project operations
 */
public class ProjectService {
	
	/** Data Access Object for Project operations */
	private ProjectsDao projectDao = new ProjectsDao();
	
	/**
	 * Adds a new project to the database
	 * 
	 * @param project The Project object containing the project details to be added
	 * @return The Project object with the generated ID after insertion
	 */
	public Project addProject(Project project) {
		return projectDao.insertProject(project);
	}
}
