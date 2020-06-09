package metadata.swagger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Test;
import org.threeten.bp.LocalDate;

import de.bund.bfr.metadata.swagger.Parameter;
import de.bund.bfr.metadata.swagger.PredictiveModel;
import de.bund.bfr.metadata.swagger.PredictiveModelDataBackground;
import de.bund.bfr.metadata.swagger.PredictiveModelGeneralInformation;
import de.bund.bfr.metadata.swagger.PredictiveModelModelMath;
import de.bund.bfr.metadata.swagger.PredictiveModelScope;

public class PredictiveModelSheetImporterTest {

	@SuppressWarnings("static-method")
	@Test
	public void test() throws Exception {
		Sheet sheet;
		try (InputStream stream = Files.newInputStream(Paths.get("files/annotation_v1.0.4.xlsx"));
				Workbook workbook = WorkbookFactory.create(stream)) {
			sheet = workbook.getSheet("Predictive Model");
		}

		PredictiveModel model = (PredictiveModel) new PredictiveModelSheetImporter().retrieveModel(sheet);
		test(model.getGeneralInformation());
		test(model.getScope());
		test(model.getDataBackground());
		test(model.getModelMath());
	}

	private static void test(PredictiveModelGeneralInformation information) throws Exception {
		assertEquals("Listeria Monocytogenes (DR of gQMRA)", information.getName());
		assertEquals("PUBLISHED SCIENTIFIC STUDIES", information.getSource());
		assertEquals("DR000001", information.getIdentifier());
		assertEquals(6, information.getAuthor().size());
		assertEquals(6, information.getCreator().size());
		assertEquals(LocalDate.of(2018, 3, 30), information.getCreationDate());
		assertNull(information.getModificationDate()); // Not set
		assertEquals("CC0 1.0", information.getRights());
		assertEquals("Open access", information.getAvailability());
		assertEquals("http://onlinelibrary.wiley.com/doi/10.2903/sp.efsa.2017.EN-1252/abstract", information.getUrl());
		assertEquals(".fskx", information.getFormat());
		assertEquals(1, information.getReference().size());
		assertEquals("English", information.getLanguage());
		assertEquals("FSK-Lab", information.getSoftware());
		assertEquals("R 3", information.getLanguageWrittenIn());
		assertNotNull(information.getModelCategory());
		assertEquals("Uncurated", information.getStatus());
		assertEquals("Objective", information.getObjective());
		assertEquals("Description", information.getDescription());
		
		TestUtils.testFirstCreator(information.getCreator().get(0));
		TestUtils.testFirstAuthor(information.getAuthor().get(0));
	}

	private static void test(PredictiveModelScope scope) {
		assertEquals(10, scope.getProduct().size());
		assertEquals(1, scope.getHazard().size());
		assertNull(scope.getGeneralComment());
		assertNull(scope.getTemporalInformation());
		// TODO: spatial information: String*
	}

	private static void test(PredictiveModelDataBackground background) {
		assertNotNull(background.getStudy());
		assertEquals(3, background.getStudySample().size());
		assertEquals(3, background.getLaboratory().size());
		assertEquals(3, background.getAssay().size());
	}

	private static void test(PredictiveModelModelMath math) {
		assertEquals(9, math.getParameter().size());
		assertEquals(1, math.getQualityMeasures().size());
		assertNull(math.getModelEquation());
		assertEquals("Maximum likelihood estimation (MLE)", math.getFittingProcedure());
		assertNull(math.getEvent());
		
		// Check first parameter
		Parameter firstParameter = math.getParameter().get(0);

		assertEquals("DR_Inputs3", firstParameter.getId());
		assertEquals(Parameter.ClassificationEnum.INPUT, firstParameter.getClassification());
		assertEquals("DR_Inputs3.csv", firstParameter.getName());
		assertEquals("[]", firstParameter.getUnit());
		assertEquals("Dimensionless Quantity", firstParameter.getUnitCategory());
		assertEquals(Parameter.DataTypeEnum.FILE, firstParameter.getDataType());
		assertEquals("Boolean", firstParameter.getSource());
		assertEquals("Boolean", firstParameter.getSubject());
		assertEquals("Boolean", firstParameter.getDistribution());
		assertEquals("\"DR_inputs3.csv\"", firstParameter.getValue());
		assertNull(firstParameter.getReference()); // reference is not implemented yet
		assertEquals("a", firstParameter.getVariabilitySubject());
		assertEquals("max0", firstParameter.getMaxValue());
		assertEquals("min0", firstParameter.getMinValue());
		assertEquals("error0", firstParameter.getError());
	}
}