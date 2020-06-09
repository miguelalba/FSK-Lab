package de.bund.bfr.knime.fsklab.vocabularies.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Optional;

import de.bund.bfr.knime.fsklab.vocabularies.domain.ProductMatrix;

public class ProductMatrixRepository implements BasicRepository<ProductMatrix> {

	private final Connection connection;

	public ProductMatrixRepository(Connection connection) {
		this.connection = connection;
	}

	@Override
	public Optional<ProductMatrix> getById(int id) {

		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT * FROM product_matrix WHERE id = " + id);

			if (resultSet.next()) {
				String ssd = resultSet.getString("ssd");
				String name = resultSet.getString("name");
				String comment = resultSet.getString("comment");

				return Optional.of(new ProductMatrix(id, ssd, name, comment));
			}
			return Optional.empty();
		} catch (SQLException err) {
			return Optional.empty();
		}
	}

	@Override
	public ProductMatrix[] getAll() {
		ArrayList<ProductMatrix> matrixList = new ArrayList<>();
		
		try {			
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT * FROM product_matrix");
			
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				String ssd = resultSet.getString("ssd");
				String name = resultSet.getString("name");
				String comment = resultSet.getString("comment");
				
				matrixList.add(new ProductMatrix(id, ssd, name, comment));
			}
		} catch (SQLException err) {}

		return matrixList.toArray(new ProductMatrix[0]);
	}
}