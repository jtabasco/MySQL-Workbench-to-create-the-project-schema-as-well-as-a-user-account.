package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import projects.entity.Project;
import projects.exception.DbException;
import provided.util.DaoBase;

public class ProjectsDao extends DaoBase {

	// Constants for table names in the database
	private static final String CATEGORY_TABLE = "category";
	private static final String MATERIAL_TABLE = "material";
	private static final String PROJECT_TABLE = "project";
	private static final String PROJECT_CATEGORY_TABLE = "project_category";
	private static final String STEP_TABLE = "step";

	/**
	 * Inserts a new project into the database.
	 * 
	 * @param project The Project object containing the project details to be inserted
	 * @return The Project object with the generated project ID
	 * @throws DbException If there is an error during the database operation
	 */
	public Project insertProject(Project project) {
		// SQL query to insert project details into the project table
		// @formatter:off
		String sql = "INSERT INTO " + PROJECT_TABLE + 
					" (project_name, estimated_hours, actual_hours, difficulty, notes) VALUES " +
					"(?, ?, ?, ?, ?) ";
		// @formatter:on

		try (Connection conn = DbConnection.getConnection()) {
			// Start a database transaction
			startTransaction(conn);

			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				// Set parameters for the prepared statement
				setParameter(stmt, 1, project.getProjectName(), String.class);        // Project name
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class); // Estimated hours
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);    // Actual hours
				setParameter(stmt, 4, project.getDifficulty(), Integer.class);        // Difficulty level
				setParameter(stmt, 5, project.getNotes(), String.class);              // Project notes

				// Execute the insert statement
				stmt.executeUpdate();

				// Get the ID of the newly inserted project
				Integer projectId = getLastInsertId(conn, PROJECT_TABLE);
				// Commit the transaction
				commitTransaction(conn);

				// Set the generated ID in the project object and return it
				project.setProjectId(projectId);
				return project;
			} catch (Exception e) {
				// Rollback the transaction if an error occurs
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}
	
}
