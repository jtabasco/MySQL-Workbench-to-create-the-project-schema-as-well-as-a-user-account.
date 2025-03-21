package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

import projects.entity.Project;
import projects.entity.Material;
import projects.entity.Step;
import projects.entity.Category;
import projects.exception.DbException;
import provided.util.DaoBase;

/**
 * Data Access Object (DAO) class for handling project-related database
 * operations.
 * This class extends DaoBase to inherit common database functionality.
 * 
 * Key responsibilities:
 * - Implements CRUD operations for projects
 * - Manages relationships with materials, steps, and categories
 * - Handles database transactions
 * - Manages database connections and resources
 * 
 * The DAO layer ensures that:
 * - All database operations are performed efficiently
 * - Resources are properly managed and released
 * - Transactions are handled correctly
 * - Related entities are loaded when needed
 */
public class ProjectsDao extends DaoBase {

	/**
	 * Constants for table names in the database.
	 * These constants are used to prevent typos and make table name changes easier.
	 */
	private static final String CATEGORY_TABLE = "category";
	private static final String MATERIAL_TABLE = "material";
	private static final String PROJECT_TABLE = "project";
	private static final String PROJECT_CATEGORY_TABLE = "project_category";
	private static final String STEP_TABLE = "step";

	/**
	 * Inserts a new project into the database.
	 * This method handles the complete project creation process:
	 * 1. Prepares the SQL insert statement
	 * 2. Sets all project parameters
	 * 3. Executes the insert
	 * 4. Retrieves the generated ID
	 * 5. Returns the complete project object
	 * 
	 * @param project The Project object containing the project details to be
	 *                inserted
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
				setParameter(stmt, 1, project.getProjectName(), String.class); // Project name
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class); // Estimated hours
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class); // Actual hours
				setParameter(stmt, 4, project.getDifficulty(), Integer.class); // Difficulty level
				setParameter(stmt, 5, project.getNotes(), String.class); // Project notes

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

	/**
	 * Retrieves all projects from the database ordered by project name.
	 * This method provides a basic view of projects without loading related
	 * entities
	 * to improve performance when only basic project information is needed.
	 * 
	 * Implementation details:
	 * 1. Prepares and executes the SELECT query
	 * 2. Maps the result set to Project objects
	 * 3. Returns the list of projects
	 * 
	 * @return List of all projects in the database
	 * @throws DbException If there is an error during the database operation
	 */
	public List<Project> fetchAllProjects() {
		String sql = "SELECT * FROM " + PROJECT_TABLE + " ORDER BY project_name";

		try (Connection conn = DbConnection.getConnection()) {
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				try (ResultSet rs = stmt.executeQuery()) {
					List<Project> projects = new LinkedList<>();

					while (rs.next()) {
						projects.add(extract(rs, Project.class));
					}

					return projects;
				}
			} catch (SQLException e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}

	/**
	 * Retrieves a specific project by its ID including all related data.
	 * This method performs multiple queries to load:
	 * 1. The base project information
	 * 2. Associated materials
	 * 3. Associated steps (ordered by step order)
	 * 4. Associated categories
	 * 
	 * Implementation details:
	 * 1. Uses a transaction to ensure data consistency
	 * 2. Loads all related entities in a specific order
	 * 3. Returns an Optional to handle the case when no project is found
	 * 
	 * @param projectId The ID of the project to retrieve
	 * @return Optional containing the project if found, empty Optional if not found
	 * @throws DbException If there is an error during the database operation
	 */
	public Optional<Project> fetchProjectById(Integer projectId) {
		String sql = "SELECT * FROM " + PROJECT_TABLE + " WHERE project_id = ?";

		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);

			try {
				Project project = null;

				try (PreparedStatement stmt = conn.prepareStatement(sql)) {
					setParameter(stmt, 1, projectId, Integer.class);

					try (ResultSet rs = stmt.executeQuery()) {
						if (rs.next()) {
							project = extract(rs, Project.class);
							System.out.println();
						}
					}
				}

				if (Objects.nonNull(project)) {
					project.getMaterials().addAll(fetchMaterialsForProject(conn, projectId));
					project.getSteps().addAll(fetchStepsForProject(conn, projectId));
					project.getCategories().addAll(fetchCategoriesForProject(conn, projectId));
				}

				commitTransaction(conn);
				/*
				 * Optional.ofNullable() is used because project may be null at this point if
				 * the given
				 * project ID is invalid.
				 */
				return Optional.ofNullable(project);

			} catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}

	/**
	 * Retrieves all materials associated with a specific project.
	 * This is a helper method used by fetchProjectById to load related materials.
	 * 
	 * Implementation details:
	 * 1. Uses the provided connection to maintain transaction integrity
	 * 2. Executes a SELECT query with a JOIN to get all materials
	 * 3. Maps the result set to Material objects
	 * 
	 * @param conn      The database connection to use
	 * @param projectId The ID of the project to fetch materials for
	 * @return List of materials associated with the project
	 * @throws SQLException If there is an error executing the query
	 */
	private List<Material> fetchMaterialsForProject(Connection conn, Integer projectId) throws SQLException {
		String sql = "SELECT * FROM " + MATERIAL_TABLE + " WHERE project_id = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);
			try (ResultSet rs = stmt.executeQuery()) {
				List<Material> materials = new LinkedList<>();
				while (rs.next()) {
					materials.add(extract(rs, Material.class));
				}
				return materials;
			}
		}
	}

	/**
	 * Retrieves all steps associated with a specific project ordered by step order.
	 * This is a helper method used by fetchProjectById to load related steps.
	 * 
	 * Implementation details:
	 * 1. Uses the provided connection to maintain transaction integrity
	 * 2. Executes a SELECT query with ORDER BY for step order
	 * 3. Maps the result set to Step objects
	 * 
	 * @param conn      The database connection to use
	 * @param projectId The ID of the project to fetch steps for
	 * @return List of steps associated with the project in correct order
	 * @throws SQLException If there is an error executing the query
	 */
	private List<Step> fetchStepsForProject(Connection conn, Integer projectId) throws SQLException {
		String sql = "SELECT * FROM " + STEP_TABLE + " WHERE project_id = ? ORDER BY step_order";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);
			try (ResultSet rs = stmt.executeQuery()) {
				List<Step> steps = new LinkedList<>();
				while (rs.next()) {
					steps.add(extract(rs, Step.class));
				}
				return steps;
			}
		}
	}

	/**
	 * Retrieves all categories associated with a specific project through the
	 * project_category join table.
	 * This is a helper method used by fetchProjectById to load related categories.
	 * 
	 * Implementation details:
	 * 1. Uses the provided connection to maintain transaction integrity
	 * 2. Executes a SELECT query with JOIN to get all categories
	 * 3. Maps the result set to Category objects
	 * 
	 * @param conn      The database connection to use
	 * @param projectId The ID of the project to fetch categories for
	 * @return List of categories associated with the project
	 * @throws SQLException If there is an error executing the query
	 */
	private List<Category> fetchCategoriesForProject(Connection conn, Integer projectId) throws SQLException {
		//@formatter:off
		String sql = "SELECT c.* FROM " + CATEGORY_TABLE + " c " 
				  + "JOIN " + PROJECT_CATEGORY_TABLE + " pc USING (category_id) "
				  + "WHERE project_id = ?";
		//@formatter:on
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);
			try (ResultSet rs = stmt.executeQuery()) {
				List<Category> categories = new LinkedList<>();
				while (rs.next()) {
					categories.add(extract(rs, Category.class));
				}
				return categories;
			}
		}
	}

	/**
	 * Updates an existing project in the database.
	 * This method handles the complete project update process:
	 * 1. Prepares the SQL update statement
	 * 2. Sets all project parameters
	 * 3. Executes the update within a transaction
	 * 4. Returns the updated project object
	 * 
	 * @param project The Project object containing the updated details
	 * @return The updated Project object
	 * @throws DbException If there is an error during the update
	 */
	public boolean modifyProject(Project project) {
		String sql = "UPDATE " + PROJECT_TABLE + " SET "
				+ "project_name = ?, "
				+ "estimated_hours = ?, "
				+ "actual_hours = ?, "
				+ "difficulty = ?, "
				+ "notes = ? "
				+ "WHERE project_id = ?";

		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				setParameter(stmt, 1, project.getProjectName(), String.class);
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class);
				setParameter(stmt, 5, project.getNotes(), String.class);
				setParameter(stmt, 6, project.getProjectId(), Integer.class);

				boolean success = stmt.executeUpdate() == 1;
				commitTransaction(conn);
				return success;
			} catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}

	/**
	 * Deletes a project and all its related data from the database.
	 * This method handles the complete project deletion process:
	 * 1. Prepares the SQL delete statement
	 * 2. Executes the delete within a transaction
	 * 3. Ensures all related data is properly cleaned up
	 * 
	 * @param projectId The ID of the project to delete
	 * @throws DbException If there is an error during deletion
	 */
	public boolean deleteProject(Integer projectId) {
		String sql = "DELETE FROM " + PROJECT_TABLE + " WHERE project_id = ?";

		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				setParameter(stmt, 1, projectId, Integer.class);
				boolean success = stmt.executeUpdate() == 1;
				commitTransaction(conn);
				return success;
			} catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}
}
