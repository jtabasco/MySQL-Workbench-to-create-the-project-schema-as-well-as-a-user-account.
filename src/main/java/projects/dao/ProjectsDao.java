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
 * Implements CRUD operations for projects and manages relationships with
 * materials,
 * steps, and categories.
 */
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
	 * This method does not load related entities (materials, steps, categories).
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
	 * Steps are returned in the correct sequence based on step_order.
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
	 * Uses a JOIN operation to connect the category and project_category tables.
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
}
