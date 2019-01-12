package com.sl.mta_employee.Java.edm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

public class EmployeeEDMProvider extends CsdlAbstractEdmProvider {

	// Service Namespace
	public static final String NAMESPACE = "sl.odata.sample";

	// EDM Container
	public static final String CONTAINER_NAME = "Container";
	public static final FullQualifiedName CONTAINER_FQN = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);

	// Entity Types Names
	public static final String ET_EMPLOYEE_NAME = "Employee";
	public static final FullQualifiedName ET_EMPLOYEE_FQN = new FullQualifiedName(NAMESPACE, ET_EMPLOYEE_NAME);

	// Entity Set Names
	public static final String ES_EMPLOYEES_NAME = "Employees";

	@Override

	public List<CsdlSchema> getSchemas() {

		// create Schema
		CsdlSchema schema = new CsdlSchema();
		schema.setNamespace(NAMESPACE);

		// add EntityTypes
		List<CsdlEntityType> entityTypes = new ArrayList<CsdlEntityType>();
		entityTypes.add(getEntityType(ET_EMPLOYEE_FQN));
		schema.setEntityTypes(entityTypes);

		// add EntityContainer
		schema.setEntityContainer(getEntityContainer());

		// finally
		List<CsdlSchema> schemas = new ArrayList<CsdlSchema>();
		schemas.add(schema);

		return schemas;
	}

	@Override
	public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) {

		// this method is called for one of the EntityTypes that are configured in the
		// Schema
		if (entityTypeName.equals(ET_EMPLOYEE_FQN)) {

			// create EntityType properties
			CsdlProperty empId = new CsdlProperty().setName("EmpId")
					.setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
			CsdlProperty name = new CsdlProperty().setName("Name")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

			// create CsdlPropertyRef for Key element
			CsdlPropertyRef propertyRef = new CsdlPropertyRef();
			propertyRef.setName("EmpId");

			// configure EntityType
			CsdlEntityType entityType = new CsdlEntityType();
			entityType.setName(ET_EMPLOYEE_NAME);
			entityType.setProperties(Arrays.asList(empId, name));
			entityType.setKey(Collections.singletonList(propertyRef));

			return entityType;
		}

		return null;
	}

	@Override

	public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) {

		if (entityContainer.equals(CONTAINER_FQN)) {
			if (entitySetName.equals(ES_EMPLOYEES_NAME)) {
				CsdlEntitySet entitySet = new CsdlEntitySet();
				entitySet.setName(ES_EMPLOYEES_NAME);
				entitySet.setType(ET_EMPLOYEE_FQN);

				return entitySet;
			}
		}

		return null;
	}

	@Override
	public CsdlEntityContainer getEntityContainer() {

		// create EntitySets
		List<CsdlEntitySet> entitySets = new ArrayList<CsdlEntitySet>();
		entitySets.add(getEntitySet(CONTAINER_FQN, ES_EMPLOYEES_NAME));

		// create EntityContainer
		CsdlEntityContainer entityContainer = new CsdlEntityContainer();
		entityContainer.setName(CONTAINER_NAME);
		entityContainer.setEntitySets(entitySets);

		return entityContainer;
	}

	@Override

	public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) {

		// This method is invoked when displaying the Service Document at e.g.
		// http://localhost:8080/DemoService/DemoService.svc
		if (entityContainerName == null || entityContainerName.equals(CONTAINER_FQN)) {
			CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
			entityContainerInfo.setContainerName(CONTAINER_FQN);
			return entityContainerInfo;
		}

		return null;
	}
}