package metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.Test;
import org.threeten.bp.LocalDate;

import de.bund.bfr.metadata.swagger.Contact;
import de.bund.bfr.metadata.swagger.GenericModelGeneralInformation;
import de.bund.bfr.metadata.swagger.ModelCategory;
import de.bund.bfr.metadata.swagger.Parameter;
import de.bund.bfr.metadata.swagger.Reference;
import de.bund.bfr.metadata.swagger.Reference.PublicationTypeEnum;

public class SwaggerUtilTest {

	@SuppressWarnings("deprecation")
	@Test
	public void testConvertGeneralInformation() {

		GenericModelGeneralInformation gi = new GenericModelGeneralInformation();
		{
			metadata.GeneralInformation deprecated = metadata.MetadataFactory.eINSTANCE.createGeneralInformation();
			
			deprecated.setName("name");
			deprecated.setSource("source");
			deprecated.setIdentifier("identifier");
			deprecated.setCreationDate(new Date(2018, 0, 1));
			deprecated.setRights("rights");
			deprecated.setAvailable(true);
			deprecated.setFormat("format");
			deprecated.setLanguage("language");
			deprecated.setSoftware("software");
			deprecated.setLanguageWrittenIn("languageWrittenIn");
			deprecated.setStatus("status");
			deprecated.setObjective("objective");
			deprecated.setDescription("description");
			deprecated.getModelCategory().add(metadata.MetadataFactory.eINSTANCE.createModelCategory());
			
			metadata.ModificationDate md = metadata.MetadataFactory.eINSTANCE.createModificationDate();
			md.setValue(new Date(2018, 0, 1));
			deprecated.getModificationdate().add(md);

			deprecated.setAuthor(metadata.MetadataFactory.eINSTANCE.createContact());
			deprecated.getCreators().add(metadata.MetadataFactory.eINSTANCE.createContact());
			deprecated.getReference().add(metadata.MetadataFactory.eINSTANCE.createReference());
			
			gi = SwaggerUtil.convert(deprecated);
		}
		
		assertEquals("name", gi.getName());
		assertEquals("source", gi.getSource());
		assertEquals("identifier", gi.getIdentifier());
		assertEquals(1, gi.getAuthor().size());
		assertEquals(1, gi.getCreator().size());
		assertEquals(LocalDate.of(2018, 1, 1), gi.getCreationDate());
		assertEquals(LocalDate.of(2018, 1, 1), gi.getModificationDate().get(0));
		assertEquals("rights", gi.getRights());
		assertEquals("true", gi.getAvailability());
		assertNull(gi.getUrl());
		assertEquals("format", gi.getFormat());
		assertEquals(1, gi.getReference().size());
		assertEquals("language", gi.getLanguage());
		assertEquals("software", gi.getSoftware());
		assertEquals("languageWrittenIn", gi.getLanguageWrittenIn());
		assertNotNull(gi.getModelCategory());
		assertEquals("status", gi.getStatus());
		assertEquals("objective", gi.getObjective());
		assertEquals("description", gi.getDescription());
	}

	@Test
	public void testConvertModelCategory() {
		ModelCategory modelCategory;
		{
			metadata.ModelCategory deprecated = metadata.MetadataFactory.eINSTANCE.createModelCategory();
			deprecated.setModelClass("modelClass");
			deprecated.getModelSubClass().add(createStringObject("subClass"));
			deprecated.setModelClassComment("classComment");
			deprecated.setBasicProcess("basicProcess");

			modelCategory = SwaggerUtil.convert(deprecated);
		}

		assertEquals("modelClass", modelCategory.getModelClass());
		assertEquals("subClass", modelCategory.getModelSubClass().get(0));
		assertEquals("classComment", modelCategory.getModelClassComment());
		assertEquals("basicProcess", modelCategory.getBasicProcess().get(0));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testConvertReference() {

		Reference reference;
		{
			metadata.Reference deprecated = metadata.MetadataFactory.eINSTANCE.createReference();
			deprecated.setIsReferenceDescription(false);
			deprecated.setPublicationType(metadata.PublicationType.RPRT);
			deprecated.setPublicationDate(new Date(2018, 0, 1));
			deprecated.setPmid("pmid");
			deprecated.setDoi("10.2903/j.efsa.2018.5134");
			deprecated.setAuthorList("Miguel");
			deprecated.setPublicationTitle("Listeria monocytogenes");
			deprecated.setPublicationAbstract("abstract");
			deprecated.setPublicationJournal("journal");
			deprecated.setPublicationVolume(7);
			deprecated.setPublicationIssue(1);
			deprecated.setPublicationStatus("Published");
			deprecated.setPublicationWebsite("www.efsa.europa.eu");

			reference = SwaggerUtil.convert(deprecated);
		}

		assertFalse(reference.isIsReferenceDescription());
		assertEquals(PublicationTypeEnum.RPRT, reference.getPublicationType().get(0));
		assertEquals(LocalDate.of(2018, 1, 1), reference.getDate());
		assertEquals("pmid", reference.getPmid());
		assertEquals("10.2903/j.efsa.2018.5134", reference.getDoi());
		assertEquals("Listeria monocytogenes", reference.getTitle());
		assertEquals("abstract", reference.getAbstract());
		assertEquals("7", reference.getVolume());
		assertEquals("1", reference.getIssue());
		assertEquals("Published", reference.getStatus());
		assertEquals("www.efsa.europa.eu", reference.getWebsite());
	}
	
	@Test
	public void testConvertContact() {
		
		Contact contact = new Contact();
		{
			metadata.Contact deprecated = metadata.MetadataFactory.eINSTANCE.createContact();
			deprecated.setTitle("title");
			deprecated.setFamilyName("familyName");
			deprecated.setGivenName("givenName");
			deprecated.setEmail("john@doe.com");
			deprecated.setTelephone("0123456789");
			deprecated.setStreetAddress("streetAddress");
			deprecated.setCountry("country");
			deprecated.setCity("city");
			deprecated.setZipCode("12345");
			deprecated.setRegion("region");
			deprecated.setTimeZone("timeZone");
			deprecated.setGender("gender");
			deprecated.setNote("note");
			deprecated.setOrganization("organization");
			
			contact = SwaggerUtil.convert(deprecated);
		}

		assertEquals("title", contact.getTitle());
		assertEquals("familyName", contact.getFamilyName());
		assertEquals("givenName", contact.getGivenName());
		assertEquals("john@doe.com", contact.getEmail());
		assertEquals("0123456789", contact.getTelephone());
		assertEquals("streetAddress", contact.getStreetAddress());
		assertEquals("country", contact.getCountry());
		assertEquals("12345", contact.getZipCode());
		assertEquals("region", contact.getRegion());
		assertEquals("timeZone", contact.getTimeZone());
		assertEquals("gender", contact.getGender());
		assertEquals("note", contact.getNote());
		assertEquals("organization", contact.getOrganization());
	}
	
	@Test
	public void testConvertParameter() {
		
		Parameter param = new Parameter();
		{
			metadata.Parameter deprecated = metadata.MetadataFactory.eINSTANCE.createParameter();
			deprecated.setParameterID("id");
			deprecated.setParameterClassification(metadata.ParameterClassification.CONSTANT);
			deprecated.setParameterName("name");
			deprecated.setParameterDescription("description");
			deprecated.setParameterType("type");
			deprecated.setParameterUnit("unit");
			deprecated.setParameterUnitCategory("unitCategory");
			deprecated.setParameterDataType(metadata.ParameterType.BOOLEAN);
			deprecated.setParameterSource("source");
			deprecated.setParameterSubject("subject");
			deprecated.setParameterDistribution("distribution");
			deprecated.setParameterValue("value");
			deprecated.setParameterVariabilitySubject("subject");
			deprecated.setParameterValueMin("false");
			deprecated.setParameterValueMax("true");
			deprecated.setParameterError("2.718");
			deprecated.setReference(metadata.MetadataFactory.eINSTANCE.createReference());
			
			param = SwaggerUtil.convert(deprecated);
		}

		assertEquals("id", param.getId());
		assertEquals(Parameter.ClassificationEnum.CONSTANT, param.getClassification());
		assertEquals("name", param.getName());
		assertEquals("description", param.getDescription());
		assertEquals("unit", param.getUnit());
		assertEquals("unitCategory", param.getUnitCategory());
		assertEquals(Parameter.DataTypeEnum.BOOLEAN, param.getDataType());
		assertEquals("source", param.getSource());
		assertEquals("subject", param.getSubject());
		assertEquals("distribution", param.getDistribution());
		assertEquals("value", param.getValue());
		assertEquals("subject", param.getVariabilitySubject());
		assertEquals("false", param.getMinValue());
		assertEquals("true", param.getMaxValue());
		assertEquals("2.718", param.getError());
		assertNotNull(param.getReference());
	}

	private static StringObject createStringObject(String string) {
		StringObject so = metadata.MetadataFactory.eINSTANCE.createStringObject();
		so.setValue(string);

		return so;
	}
}
