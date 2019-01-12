package com.sl.mta_employee.Java.processor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.sl.mta_employee.Java.edm.EmployeeEDMProvider;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.commons.api.data.Entity;

public class EmployeeProcessor implements EntityCollectionProcessor {

	private OData odata;
	private ServiceMetadata serviceMetadata;

	public void init(OData odata, ServiceMetadata serviceMetadata) {
		this.odata = odata;
		this.serviceMetadata = serviceMetadata;
	}

	public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo,
			ContentType responseFormat) throws ODataApplicationException, SerializerException {

		// 1st we have retrieve the requested EntitySet from the uriInfo object
		// (representation of the parsed service URI)
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0); // in our example, the
																									// first segment is
																									// the EntitySet
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
		EntityCollection entitySet = null;
		// 2nd: fetch the data from backend for this requested EntitySetName
		// it has to be delivered as EntitySet object
		try {
			entitySet = getData(edmEntitySet);
		} catch (SQLException e) {

		}

		// 3rd: create a serializer based on the requested format (json)
		ODataSerializer serializer = odata.createSerializer(responseFormat);

		// 4th: Now serialize the content: transform from the EntitySet object to
		// InputStream
		EdmEntityType edmEntityType = edmEntitySet.getEntityType();
		ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();

		final String id = request.getRawBaseUri() + "/" + edmEntitySet.getName();
		EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.with().id(id).contextURL(contextUrl)
				.build();
		SerializerResult serializerResult = serializer.entityCollection(serviceMetadata, edmEntityType, entitySet,
				opts);
		InputStream serializedContent = serializerResult.getContent();

		// Finally: configure the response object: set the body, headers and status code
		response.setContent(serializedContent);
		response.setStatusCode(HttpStatusCode.OK.getStatusCode());
		response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
	}

	private EntityCollection getData(EdmEntitySet edmEntitySet) throws SQLException {

		EntityCollection employeeCollection = new EntityCollection();
		// check for which EdmEntitySet the data is requested
		if (EmployeeEDMProvider.ES_EMPLOYEES_NAME.equals(edmEntitySet.getName())) {
			List<Entity> employeeList = employeeCollection.getEntities();

			// Get the DB connection
			Connection conn = null;
			try {
				conn = getConnection();
			} catch (SQLException e) {
				throw new SQLException(e.getMessage(), e);
			}
			// Get the Data from the DB and add into the Entity collection
			try {

				String sql = "SELECT * FROM " + getCurrentSchema(conn) + ".\"MTA_Employee.DB::GetEmployees\";";

				PreparedStatement prepareStatement = conn.prepareStatement(sql);
				ResultSet resultSet = prepareStatement.executeQuery();
				int column1 = resultSet.findColumn("empId");
				int column2 = resultSet.findColumn("name");
				while (resultSet.next()) {

					final Entity e1 = new Entity()
							.addProperty(new Property(null, "EmpId", ValueType.PRIMITIVE,
									Integer.parseInt(resultSet.getString(column1))))
							.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, resultSet.getString(column2)));
					e1.setId(createId("Employees", Integer.parseInt(resultSet.getString(column1))));
					employeeList.add(e1);

				}
			} catch (SQLException e) {
				throw new SQLException(e.getMessage(), e);
			}

		}

		return employeeCollection;
	}

	private URI createId(String entitySetName, Object id) {
		try {
			return new URI(entitySetName + "(" + String.valueOf(id) + ")");
		} catch (URISyntaxException e) {
			throw new ODataRuntimeException("Unable to create id for entity: " + entitySetName, e);
		}
	}

	private Connection getConnection() throws SQLException {
		try {
			Context ctx = new InitialContext();
			Context xmlContext = (Context) ctx.lookup("java:comp/env");
			DataSource ds = (DataSource) xmlContext.lookup("jdbc/DefaultDB");
			Connection conn = ds.getConnection();
			System.out.println("Connected to database");
			return conn;
		} catch (NamingException ignorred) {
			// could happen if HDB support is not enabled
			return null;
		}
	}

	private String getCurrentSchema(Connection conn) throws SQLException {
		String currentSchema = "";
		PreparedStatement prepareStatement = conn
				.prepareStatement("SELECT CURRENT_SCHEMA \"current_schema\" FROM DUMMY;");
		ResultSet resultSet = prepareStatement.executeQuery();
		int column = resultSet.findColumn("current_schema");
		while (resultSet.next()) {
			currentSchema = resultSet.getString(column);
		}
		return currentSchema;
	}

}
