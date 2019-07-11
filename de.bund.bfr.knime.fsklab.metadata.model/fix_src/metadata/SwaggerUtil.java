package metadata;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;

import org.threeten.bp.LocalDate;

import de.bund.bfr.metadata.swagger.ConsumptionModel;
import de.bund.bfr.metadata.swagger.DataModel;
import de.bund.bfr.metadata.swagger.DoseResponseModel;
import de.bund.bfr.metadata.swagger.ExposureModel;
import de.bund.bfr.metadata.swagger.GenericModel;
import de.bund.bfr.metadata.swagger.GenericModelGeneralInformation;
import de.bund.bfr.metadata.swagger.Model;
import de.bund.bfr.metadata.swagger.ModelCategory;
import de.bund.bfr.metadata.swagger.OtherModel;
import de.bund.bfr.metadata.swagger.Parameter;
import de.bund.bfr.metadata.swagger.PredictiveModel;
import de.bund.bfr.metadata.swagger.ProcessModel;
import de.bund.bfr.metadata.swagger.QraModel;
import de.bund.bfr.metadata.swagger.Reference.PublicationTypeEnum;
import de.bund.bfr.metadata.swagger.RiskModel;
import de.bund.bfr.metadata.swagger.ToxicologicalModel;

public class SwaggerUtil {

	public static de.bund.bfr.metadata.swagger.GenericModelGeneralInformation convert(
			metadata.GeneralInformation deprecatedGeneralInformation) {
		GenericModelGeneralInformation swaggerGI = new GenericModelGeneralInformation();

		swaggerGI.setName(deprecatedGeneralInformation.getName());
		swaggerGI.setSource(deprecatedGeneralInformation.getSource());
		swaggerGI.setIdentifier(deprecatedGeneralInformation.getIdentifier());

		if (deprecatedGeneralInformation.getAuthor() != null) {
			swaggerGI.addAuthorItem(convert(deprecatedGeneralInformation.getAuthor()));
		}

		deprecatedGeneralInformation.getCreators().stream().map(SwaggerUtil::convert)
				.forEach(swaggerGI::addCreatorItem);

		if (deprecatedGeneralInformation.getCreationDate() != null) {
			swaggerGI.setCreationDate(toLocalDate(deprecatedGeneralInformation.getCreationDate()));
		}

		deprecatedGeneralInformation.getModificationdate().stream().map(ModificationDate::getValue)
				.map(SwaggerUtil::toLocalDate).forEach(swaggerGI::addModificationDateItem);

		swaggerGI.setRights(deprecatedGeneralInformation.getRights());
		swaggerGI.setAvailability(Boolean.toString(deprecatedGeneralInformation.isAvailable()));

		// has no URL

		swaggerGI.setFormat(deprecatedGeneralInformation.getFormat());

		deprecatedGeneralInformation.getReference().stream().map(SwaggerUtil::convert)
				.forEach(swaggerGI::addReferenceItem);
		swaggerGI.setLanguage(deprecatedGeneralInformation.getLanguage());
		swaggerGI.setSoftware(deprecatedGeneralInformation.getSoftware());
		swaggerGI.setLanguageWrittenIn(deprecatedGeneralInformation.getLanguageWrittenIn());

		if (deprecatedGeneralInformation.getModelCategory() != null) {
			swaggerGI.setModelCategory(convert(deprecatedGeneralInformation.getModelCategory().get(0)));
		}

		swaggerGI.setStatus(deprecatedGeneralInformation.getStatus());
		swaggerGI.setObjective(deprecatedGeneralInformation.getObjective());
		swaggerGI.setDescription(deprecatedGeneralInformation.getDescription());

		return swaggerGI;
	}

	/**
	 * TODO: replace {@link #convert(ModelCategory)} Convert deprecated
	 * ModelCategory to 1.0.4
	 */
	public static de.bund.bfr.metadata.swagger.ModelCategory convert(metadata.ModelCategory deprecated) {
		de.bund.bfr.metadata.swagger.ModelCategory modelCategory = new de.bund.bfr.metadata.swagger.ModelCategory();

		modelCategory.setModelClass(deprecated.getModelClass());
		List<String> subs = new ArrayList<String>();
		for (StringObject sub : deprecated.getModelSubClass()) {
			subs.add(sub.getValue());
		}
		modelCategory.setModelSubClass(subs);

		modelCategory.setModelClassComment(deprecated.getModelClassComment());
		modelCategory.setBasicProcess(Collections.singletonList(deprecated.getBasicProcess()));

		return modelCategory;
	}

	public static de.bund.bfr.metadata.swagger.Reference convert(metadata.Reference record) {

		de.bund.bfr.metadata.swagger.Reference reference = new de.bund.bfr.metadata.swagger.Reference();

		reference.setIsReferenceDescription(record.isIsReferenceDescription());

		if (record.getPublicationType() != null) {
			reference.addPublicationTypeItem(PUBLICATION_TYPE.get(record.getPublicationType()));
		}

		if (record.getPublicationDate() != null) {
			reference.setDate(toLocalDate(record.getPublicationDate()));
		}

		reference.setPmid(record.getPmid());
		reference.setDoi(record.getDoi());
		reference.setAuthorList(record.getAuthorList());
		reference.setTitle(record.getPublicationTitle());
		reference.setAbstract(record.getPublicationAbstract());
		reference.setVolume(Integer.toString(record.getPublicationVolume()));
		reference.setIssue(Integer.toString(record.getPublicationIssue()));
		reference.setStatus(record.getPublicationStatus());
		reference.setWebsite(record.getPublicationWebsite());

		return reference;
	}

	public static de.bund.bfr.metadata.swagger.Contact convert(metadata.Contact deprecated) {

		de.bund.bfr.metadata.swagger.Contact contact = new de.bund.bfr.metadata.swagger.Contact();
		contact.setCountry(deprecated.getCountry());
		contact.setEmail(deprecated.getEmail());
		contact.setFamilyName(deprecated.getFamilyName());
		contact.setGender(deprecated.getGender());
		contact.setGivenName(deprecated.getGivenName());
		contact.setNote(deprecated.getNote());
		contact.setOrganization(deprecated.getOrganization());
		contact.setRegion(deprecated.getRegion());
		contact.setStreetAddress(deprecated.getStreetAddress());
		contact.setTelephone(deprecated.getTelephone());
		contact.setTimeZone(deprecated.getTimeZone());
		contact.setTitle(deprecated.getTitle());
		contact.setZipCode(deprecated.getZipCode());

		return contact;
	}

	public static de.bund.bfr.metadata.swagger.GenericModelModelMath convert(metadata.ModelMath emfMM) {

		de.bund.bfr.metadata.swagger.GenericModelModelMath swaggerMM = new de.bund.bfr.metadata.swagger.GenericModelModelMath();
		swaggerMM.setFittingProcedure(emfMM.getFittingProcedure());
		emfMM.getParameter().stream().map(SwaggerUtil::convert).forEach(swaggerMM::addParameterItem);

		// quality measures
		if (emfMM.getQualityMeasures() != null) {
			for (StringObject item : emfMM.getQualityMeasures()) {
				de.bund.bfr.metadata.swagger.QualityMeasures qm = new de.bund.bfr.metadata.swagger.QualityMeasures();

				JsonObject measures = Json.createReader(new StringReader(item.getValue())).readObject();
				if (measures.containsKey("SSE"))
					qm.setSSE(measures.getJsonNumber("SSE").bigDecimalValue());
				if (measures.containsKey("AIC"))
					qm.setAIC(measures.getJsonNumber("AIC").bigDecimalValue());
				if (measures.containsKey("BIC"))
					qm.setBIC(measures.getJsonNumber("BIC").bigDecimalValue());
				if (measures.containsKey("MSE"))
					qm.setMSE(measures.getJsonNumber("MSE").bigDecimalValue());
				if (measures.containsKey("RMSE"))
					qm.setRMSE(measures.getJsonNumber("RMSE").bigDecimalValue());
				if (measures.containsKey("rsquared"))
					qm.setRsquared(measures.getJsonNumber("rsquared").bigDecimalValue());

				swaggerMM.addQualityMeasuresItem(qm);
			}
		}

		// Exposure
		if (emfMM.getExposure() != null) {
			swaggerMM.addExposureItem(convert(emfMM.getExposure()));
		}

		emfMM.getModelEquation().stream().map(SwaggerUtil::convert).forEach(swaggerMM::addModelEquationItem);
		emfMM.getEvent().stream().map(StringObject::getValue).forEach(swaggerMM::addEventItem);

		return swaggerMM;
	}

	public static de.bund.bfr.metadata.swagger.Parameter convert(metadata.Parameter deprecated) {

		de.bund.bfr.metadata.swagger.Parameter parameter = new de.bund.bfr.metadata.swagger.Parameter();
		parameter.setId(deprecated.getParameterID());
		parameter.setClassification(CLASSIF.get(deprecated.getParameterClassification()));
		parameter.setName(deprecated.getParameterName());
		parameter.setDescription(deprecated.getParameterDescription());
		parameter.setUnit(deprecated.getParameterUnit());
		parameter.setUnitCategory(deprecated.getParameterUnitCategory());
		parameter.setDataType(TYPES.get(deprecated.getParameterDataType()));
		parameter.setSource(deprecated.getParameterSource());
		parameter.setSubject(deprecated.getParameterSubject());
		parameter.setDistribution(deprecated.getParameterDistribution());
		parameter.setValue(deprecated.getParameterValue());
		parameter.setVariabilitySubject(deprecated.getParameterVariabilitySubject());
		parameter.setMinValue(deprecated.getParameterValueMin());
		parameter.setMaxValue(deprecated.getParameterValueMax());
		parameter.setError(deprecated.getParameterError());

		if (deprecated.getReference() != null) {
			parameter.setReference(convert(deprecated.getReference()));
		}

		return parameter;
	}

	public static de.bund.bfr.metadata.swagger.ModelEquation convert(metadata.ModelEquation deprecated) {

		de.bund.bfr.metadata.swagger.ModelEquation modelEq = new de.bund.bfr.metadata.swagger.ModelEquation();
		modelEq.setName(deprecated.getModelEquationName());
		modelEq.setPropertyClass(deprecated.getModelEquationClass());
		modelEq.setModelEquation(deprecated.getModelEquation());
		deprecated.getReference().stream().map(SwaggerUtil::convert).forEach(modelEq::addReferenceItem);
		deprecated.getHypothesisOfTheModel().stream().map(StringObject::getValue)
				.forEach(modelEq::addModelHypothesisItem);

		return modelEq;
	}

	public static de.bund.bfr.metadata.swagger.Exposure convert(metadata.Exposure deprecated) {

		de.bund.bfr.metadata.swagger.Exposure exposure = new de.bund.bfr.metadata.swagger.Exposure();
		exposure.setType(deprecated.getTypeOfExposure());
		exposure.setUncertaintyEstimation(deprecated.getUncertaintyEstimation());
		deprecated.getMethodologicalTreatmentOfLeftCensoredData().stream().map(StringObject::getValue)
				.forEach(exposure::addTreatmentItem);
		deprecated.getLevelOfContaminationAfterLeftCensoredDataTreatment().stream().map(StringObject::getValue)
				.forEach(exposure::addContaminationItem);
		deprecated.getScenario().stream().map(StringObject::getValue).forEach(exposure::addScenarioItem);

		return exposure;
	}

	public static de.bund.bfr.metadata.swagger.GenericModelScope convert(metadata.Scope emfScope) {
		de.bund.bfr.metadata.swagger.GenericModelScope swaggerS = new de.bund.bfr.metadata.swagger.GenericModelScope();

		emfScope.getProduct().stream().map(SwaggerUtil::convert).forEach(swaggerS::addProductItem);
		emfScope.getHazard().stream().map(SwaggerUtil::convert).forEach(swaggerS::addHazardItem);
		emfScope.getPopulationGroup().stream().map(SwaggerUtil::convert).forEach(swaggerS::addPopulationGroupItem);
		swaggerS.setGeneralComment(emfScope.getGeneralComment());
		swaggerS.setTemporalInformation(emfScope.getTemporalInformation());
		// FIXME: Wrong SpatialInformation. We need a SpatialInformation class instead
		// of a String.

		return swaggerS;
	}

	public static de.bund.bfr.metadata.swagger.Product convert(metadata.Product deprecated) {

		de.bund.bfr.metadata.swagger.Product product = new de.bund.bfr.metadata.swagger.Product();
		product.setName(deprecated.getProductName());
		product.setDescription(deprecated.getProductDescription());
		product.setUnit(deprecated.getProductUnit());
		product.addMethodItem(deprecated.getProductionMethod());
		product.addPackagingItem(deprecated.getPackaging());
		product.addTreatmentItem(deprecated.getProductTreatment());
		product.setOriginCountry(deprecated.getOriginCountry());
		product.setOriginArea(deprecated.getOriginArea());
		product.setFisheriesArea(deprecated.getFisheriesArea());

		if (deprecated.getProductionDate() != null) {
			product.setProductionDate(toLocalDate(deprecated.getProductionDate()));
		}

		if (deprecated.getExpiryDate() != null) {
			product.setExpiryDate(toLocalDate(deprecated.getExpiryDate()));
		}

		return product;
	}

	public static de.bund.bfr.metadata.swagger.PopulationGroup convert(metadata.PopulationGroup deprecated) {
		de.bund.bfr.metadata.swagger.PopulationGroup pop = new de.bund.bfr.metadata.swagger.PopulationGroup();
		pop.setName(deprecated.getPopulationName());
		pop.setTargetPopulation(deprecated.getTargetPopulation());
		deprecated.getPopulationSpan().stream().map(StringObject::getValue).forEach(pop::addPopulationSpanItem);
		deprecated.getPopulationDescription().stream().map(StringObject::getValue)
				.forEach(pop::addPopulationDescriptionItem);
		deprecated.getBmi().stream().map(StringObject::getValue).forEach(pop::addBmiItem);
		deprecated.getSpecialDietGroups().stream().map(StringObject::getValue).forEach(pop::addSpecialDietGroupsItem);
		deprecated.getRegion().stream().map(StringObject::getValue).forEach(pop::addRegionItem);
		deprecated.getCountry().stream().map(StringObject::getValue).forEach(pop::addCountryItem);
		deprecated.getPopulationRiskFactor().stream().map(StringObject::getValue)
				.forEach(pop::addPopulationRiskFactorItem);
		deprecated.getSeason().stream().map(StringObject::getValue).forEach(pop::addSeasonItem);
		pop.setPopulationGender(deprecated.getPopulationGender());
		deprecated.getPatternConsumption().stream().map(StringObject::getValue).forEach(pop::addPatternConsumptionItem);
		deprecated.getPopulationAge().stream().map(StringObject::getValue).forEach(pop::addPopulationAgeItem);

		return pop;
	}

	public static de.bund.bfr.metadata.swagger.Hazard convert(metadata.Hazard deprecated) {

		de.bund.bfr.metadata.swagger.Hazard hazard = new de.bund.bfr.metadata.swagger.Hazard();
		hazard.setType(deprecated.getHazardType());
		hazard.setName(deprecated.getHazardName());
		hazard.setDescription(deprecated.getHazardDescription());
		hazard.setUnit(deprecated.getHazardUnit());
		hazard.setAdverseEffect(deprecated.getAdverseEffect());
		hazard.setSourceOfContamination(deprecated.getSourceOfContamination());
		hazard.setBenchmarkDose(deprecated.getBenchmarkDose());
		hazard.setMaximumResidueLimit(deprecated.getMaximumResidueLimit());
		hazard.setNoObservedAdverseAffectLevel(deprecated.getNoObservedAdverseAffectLevel());
		hazard.setLowestObservedAdverseAffectLevel(deprecated.getLowestObservedAdverseAffectLevel());
		hazard.setAcceptableDailyIntake(deprecated.getAcceptableDailyIntake());
		hazard.setIndSum(deprecated.getHazardIndSum());
		hazard.setAcceptableOperatorsExposureLevel(deprecated.getAcceptableOperatorExposureLevel());
		hazard.setAcuteReferenceDose(deprecated.getAcuteReferenceDose());

		return hazard;
	}

	public static de.bund.bfr.metadata.swagger.GenericModelDataBackground convert(metadata.DataBackground emfDBG) {
		de.bund.bfr.metadata.swagger.GenericModelDataBackground swaggerDBG = new de.bund.bfr.metadata.swagger.GenericModelDataBackground();

		if (emfDBG.getStudy() != null)
			swaggerDBG.setStudy(convert(emfDBG.getStudy()));

		emfDBG.getStudySample().stream().map(SwaggerUtil::convert).forEach(swaggerDBG::addStudySampleItem);
		emfDBG.getAssay().stream().map(SwaggerUtil::convert).forEach(swaggerDBG::addAssayItem);
		emfDBG.getDietaryAssessmentMethod().stream().map(SwaggerUtil::convert)
				.forEach(swaggerDBG::addDietaryAssessmentMethodItem);
		emfDBG.getLaboratory().stream().map(SwaggerUtil::convert).forEach(swaggerDBG::addLaboratoryItem);

		return swaggerDBG;
	}

	public static de.bund.bfr.metadata.swagger.Assay convert(metadata.Assay deprecated) {

		de.bund.bfr.metadata.swagger.Assay assay = new de.bund.bfr.metadata.swagger.Assay();
		assay.setName(deprecated.getAssayName());
		assay.setDescription(deprecated.getAssayDescription());
		assay.setMoisturePercentage(deprecated.getPercentageOfMoisture());
		assay.setFatPercentage(deprecated.getPercentageOfFat());
		assay.setDetectionLimit(deprecated.getLimitOfDetection());
		assay.setQuantificationLimit(deprecated.getLimitOfQuantification());
		assay.setLeftCensoredData(deprecated.getLeftCensoredData());
		assay.setContaminationRange(deprecated.getRangeOfContamination());
		assay.setUncertaintyValue(deprecated.getUncertaintyValue());

		return assay;
	}

	public static de.bund.bfr.metadata.swagger.DietaryAssessmentMethod convert(
			metadata.DietaryAssessmentMethod deprecated) {

		de.bund.bfr.metadata.swagger.DietaryAssessmentMethod diet = new de.bund.bfr.metadata.swagger.DietaryAssessmentMethod();
		diet.setCollectionTool(deprecated.getCollectionTool());
		diet.setNumberOfNonConsecutiveOneDay(Integer.toString(deprecated.getNumberOfNonConsecutiveOneDay()));
		diet.setSoftwareTool(deprecated.getSoftwareTool());
		diet.addNumberOfFoodItemsItem(deprecated.getNumberOfFoodItems());
		diet.addRecordTypesItem(deprecated.getRecordTypes());
		diet.addFoodDescriptorsItem(deprecated.getFoodDescriptors());

		return diet;
	}

	public static de.bund.bfr.metadata.swagger.Laboratory convert(metadata.Laboratory deprecated) {

		de.bund.bfr.metadata.swagger.Laboratory lab = new de.bund.bfr.metadata.swagger.Laboratory();
		lab.setName(deprecated.getLaboratoryName());
		lab.setCountry(deprecated.getLaboratoryCountry());
		deprecated.getLaboratoryAccreditation().stream().map(StringObject::getValue).forEach(lab::addAccreditationItem);

		return lab;
	}

	public static de.bund.bfr.metadata.swagger.StudySample convert(metadata.StudySample deprecated) {

		de.bund.bfr.metadata.swagger.StudySample sample = new de.bund.bfr.metadata.swagger.StudySample();
		sample.setSampleName(deprecated.getSampleName());
		sample.setProtocolOfSampleCollection(deprecated.getProtocolOfSampleCollection());
		sample.setSamplingStrategy(deprecated.getSamplingStrategy());
		sample.setTypeOfSamplingProgram(deprecated.getTypeOfSamplingProgram());
		sample.setSamplingMethod(deprecated.getSamplingMethod());
		sample.setSamplingPlan(deprecated.getSamplingPlan());
		sample.setSamplingWeight(deprecated.getSamplingWeight());
		sample.setSamplingSize(deprecated.getSamplingSize());
		sample.setLotSizeUnit(deprecated.getLotSizeUnit());
		sample.setSamplingPoint(deprecated.getSamplingPoint());

		return sample;
	}

	public static de.bund.bfr.metadata.swagger.Study convert(metadata.Study deprecated) {

		de.bund.bfr.metadata.swagger.Study study = new de.bund.bfr.metadata.swagger.Study();
		study.setIdentifier(deprecated.getStudyIdentifier());
		study.setTitle(deprecated.getStudyTitle());
		study.setDescription(deprecated.getStudyDescription());
		study.setDesignType(deprecated.getStudyDesignType());
		study.setAssayMeasurementType(deprecated.getStudyAssayMeasurementType());
		study.setAssayTechnologyType(deprecated.getStudyAssayTechnologyType());
		study.setAssayTechnologyPlatform(deprecated.getStudyAssayTechnologyPlatform());
		study.setAccreditationProcedureForTheAssayTechnology(
				deprecated.getAccreditationProcedureForTheAssayTechnology());
		study.setProtocolName(deprecated.getStudyProtocolName());
		study.setProtocolType(deprecated.getStudyProtocolType());
		study.setProtocolDescription(deprecated.getStudyProtocolDescription());

		if (deprecated.getStudyProtocolURI() != null) {
			study.setProtocolURI(deprecated.getStudyProtocolURI().toString());
		}

		study.setProtocolVersion(deprecated.getStudyProtocolVersion());
		study.setProtocolParametersName(deprecated.getStudyProtocolParametersName());
		study.setProtocolComponentsName(deprecated.getStudyProtocolComponentsName());
		study.setProtocolComponentsType(deprecated.getStudyProtocolComponentsType());

		return study;
	}

	/** Internal map used to convert RIS types to 1.0.4 Reference types. */
	public static Map<PublicationType, PublicationTypeEnum> PUBLICATION_TYPE;
	static {
		PUBLICATION_TYPE = new HashMap<PublicationType, PublicationTypeEnum>();

		PUBLICATION_TYPE.put(PublicationType.ABST, PublicationTypeEnum.ABST);
		PUBLICATION_TYPE.put(PublicationType.ADVS, PublicationTypeEnum.ADVS);
		PUBLICATION_TYPE.put(PublicationType.AGGR, PublicationTypeEnum.AGGR);
		PUBLICATION_TYPE.put(PublicationType.ANCIENT, PublicationTypeEnum.ANCIENT);
		PUBLICATION_TYPE.put(PublicationType.ART, PublicationTypeEnum.ART);
		PUBLICATION_TYPE.put(PublicationType.BILL, PublicationTypeEnum.BILL);
		PUBLICATION_TYPE.put(PublicationType.BLOG, PublicationTypeEnum.BLOG);
		PUBLICATION_TYPE.put(PublicationType.BOOK, PublicationTypeEnum.BOOK);
		PUBLICATION_TYPE.put(PublicationType.CASE, PublicationTypeEnum.CASE);
		PUBLICATION_TYPE.put(PublicationType.CHAP, PublicationTypeEnum.CHAP);
		PUBLICATION_TYPE.put(PublicationType.CHART, PublicationTypeEnum.CHART);
		PUBLICATION_TYPE.put(PublicationType.CLSWK, PublicationTypeEnum.CLSWK);
		PUBLICATION_TYPE.put(PublicationType.COMP, PublicationTypeEnum.COMP);
		PUBLICATION_TYPE.put(PublicationType.CONF, PublicationTypeEnum.CONF);
		PUBLICATION_TYPE.put(PublicationType.CPAPER, PublicationTypeEnum.CPAPER);
		PUBLICATION_TYPE.put(PublicationType.CTLG, PublicationTypeEnum.CTLG);
		PUBLICATION_TYPE.put(PublicationType.DATA, PublicationTypeEnum.DATA);
		PUBLICATION_TYPE.put(PublicationType.DBASE, PublicationTypeEnum.DBASE);
		PUBLICATION_TYPE.put(PublicationType.DICT, PublicationTypeEnum.DICT);
		PUBLICATION_TYPE.put(PublicationType.EBOOK, PublicationTypeEnum.EBOOK);
		PUBLICATION_TYPE.put(PublicationType.ECHAP, PublicationTypeEnum.ECHAP);
		PUBLICATION_TYPE.put(PublicationType.EDBOOK, PublicationTypeEnum.EDBOOK);
		PUBLICATION_TYPE.put(PublicationType.EJOUR, PublicationTypeEnum.EJOUR);
		PUBLICATION_TYPE.put(PublicationType.ELECT, PublicationTypeEnum.ELECT);
		PUBLICATION_TYPE.put(PublicationType.ENCYC, PublicationTypeEnum.ENCYC);
		PUBLICATION_TYPE.put(PublicationType.EQUA, PublicationTypeEnum.EQUA);
		PUBLICATION_TYPE.put(PublicationType.FIGURE, PublicationTypeEnum.FIGURE);
		PUBLICATION_TYPE.put(PublicationType.GEN, PublicationTypeEnum.GEN);
		PUBLICATION_TYPE.put(PublicationType.GOVDOC, PublicationTypeEnum.GOVDOC);
		PUBLICATION_TYPE.put(PublicationType.GRANT, PublicationTypeEnum.GRANT);
		PUBLICATION_TYPE.put(PublicationType.HEAR, PublicationTypeEnum.HEAR);
		PUBLICATION_TYPE.put(PublicationType.ICOMM, PublicationTypeEnum.ICOMM);
		PUBLICATION_TYPE.put(PublicationType.INPR, PublicationTypeEnum.INPR);
		PUBLICATION_TYPE.put(PublicationType.JOUR, PublicationTypeEnum.JOUR);
		PUBLICATION_TYPE.put(PublicationType.JFULL, PublicationTypeEnum.JFULL);
		PUBLICATION_TYPE.put(PublicationType.LEGAL, PublicationTypeEnum.LEGAL);
		PUBLICATION_TYPE.put(PublicationType.MANSCPT, PublicationTypeEnum.MANSCPT);
		PUBLICATION_TYPE.put(PublicationType.MAP, PublicationTypeEnum.MAP);
		PUBLICATION_TYPE.put(PublicationType.MGZN, PublicationTypeEnum.MGZN);
		PUBLICATION_TYPE.put(PublicationType.MPCT, PublicationTypeEnum.MPCT);
		PUBLICATION_TYPE.put(PublicationType.MULTI, PublicationTypeEnum.MULTI);
		PUBLICATION_TYPE.put(PublicationType.MUSIC, PublicationTypeEnum.MUSIC);
		// Typo in PublicationTypeEnum. It should be 'NEWS'
		PUBLICATION_TYPE.put(PublicationType.NEWS, PublicationTypeEnum.NEW);
		PUBLICATION_TYPE.put(PublicationType.PAMP, PublicationTypeEnum.PAMP);
		PUBLICATION_TYPE.put(PublicationType.PAT, PublicationTypeEnum.PAT);
		PUBLICATION_TYPE.put(PublicationType.PCOMM, PublicationTypeEnum.PCOMM);
		PUBLICATION_TYPE.put(PublicationType.RPRT, PublicationTypeEnum.RPRT);
		PUBLICATION_TYPE.put(PublicationType.SER, PublicationTypeEnum.SER);
		PUBLICATION_TYPE.put(PublicationType.SLIDE, PublicationTypeEnum.SLIDE);
		PUBLICATION_TYPE.put(PublicationType.SOUND, PublicationTypeEnum.SOUND);
		PUBLICATION_TYPE.put(PublicationType.STAND, PublicationTypeEnum.STAND);
		PUBLICATION_TYPE.put(PublicationType.STAT, PublicationTypeEnum.STAT);
		PUBLICATION_TYPE.put(PublicationType.THES, PublicationTypeEnum.THES);
		PUBLICATION_TYPE.put(PublicationType.UNPB, PublicationTypeEnum.UNPB);
		PUBLICATION_TYPE.put(PublicationType.VIDEO, PublicationTypeEnum.VIDEO);
	}

	/** Internal map used to convert parameter classification to 1.0.4. */
	public static Map<metadata.ParameterClassification, de.bund.bfr.metadata.swagger.Parameter.ClassificationEnum> CLASSIF;
	static {
		CLASSIF = new HashMap<>();

		CLASSIF.put(metadata.ParameterClassification.INPUT,
				de.bund.bfr.metadata.swagger.Parameter.ClassificationEnum.INPUT);
		CLASSIF.put(metadata.ParameterClassification.OUTPUT,
				de.bund.bfr.metadata.swagger.Parameter.ClassificationEnum.OUTPUT);
		CLASSIF.put(metadata.ParameterClassification.CONSTANT,
				de.bund.bfr.metadata.swagger.Parameter.ClassificationEnum.CONSTANT);
	}

	/** Internal map used to convert parameter types to 1.0.4. */
	public static Map<metadata.ParameterType, de.bund.bfr.metadata.swagger.Parameter.DataTypeEnum> TYPES;
	static {
		TYPES = new HashMap<>();

		TYPES.put(metadata.ParameterType.NULL, de.bund.bfr.metadata.swagger.Parameter.DataTypeEnum.OBJECT);
		TYPES.put(metadata.ParameterType.INTEGER, de.bund.bfr.metadata.swagger.Parameter.DataTypeEnum.INTEGER);
		TYPES.put(metadata.ParameterType.DOUBLE, de.bund.bfr.metadata.swagger.Parameter.DataTypeEnum.DOUBLE);
		TYPES.put(metadata.ParameterType.NUMBER, de.bund.bfr.metadata.swagger.Parameter.DataTypeEnum.NUMBER);
		TYPES.put(metadata.ParameterType.DATE, de.bund.bfr.metadata.swagger.Parameter.DataTypeEnum.DATE);
		TYPES.put(metadata.ParameterType.FILE, de.bund.bfr.metadata.swagger.Parameter.DataTypeEnum.FILE);
		TYPES.put(metadata.ParameterType.BOOLEAN, de.bund.bfr.metadata.swagger.Parameter.DataTypeEnum.BOOLEAN);
		TYPES.put(metadata.ParameterType.VECTOR_OF_NUMBERS,
				de.bund.bfr.metadata.swagger.Parameter.DataTypeEnum.VECTOROFNUMBERS);
		TYPES.put(metadata.ParameterType.VECTOR_OF_STRINGS,
				de.bund.bfr.metadata.swagger.Parameter.DataTypeEnum.VECTOROFSTRINGS);
		TYPES.put(metadata.ParameterType.MATRIX_OF_NUMBERS,
				de.bund.bfr.metadata.swagger.Parameter.DataTypeEnum.MATRIXOFNUMBERS);
		TYPES.put(metadata.ParameterType.VECTOR_OF_NUMBERS,
				de.bund.bfr.metadata.swagger.Parameter.DataTypeEnum.MATRIXOFSTRINGS);
		TYPES.put(metadata.ParameterType.OBJECT, de.bund.bfr.metadata.swagger.Parameter.DataTypeEnum.OBJECT);
		TYPES.put(metadata.ParameterType.OTHER, de.bund.bfr.metadata.swagger.Parameter.DataTypeEnum.OBJECT);
		TYPES.put(metadata.ParameterType.STRING, de.bund.bfr.metadata.swagger.Parameter.DataTypeEnum.STRING);
	}

	@SuppressWarnings("deprecation")
	private static LocalDate toLocalDate(Date date) {
		return LocalDate.of(date.getYear(), date.getMonth() + 1, date.getDate());
	}

	public static List<Parameter> getParameter(Model model) {
		List<Parameter> parameters;

		final String modelType = model.getModelType();
		if (modelType.equals("genericModel")) {
			parameters = ((GenericModel) model).getModelMath().getParameter();
		} else if (modelType.equals("dataModel")) {
			parameters = ((DataModel) model).getModelMath().getParameter();
		} else if (modelType.equals("predictiveModel")) {
			parameters = ((PredictiveModel) model).getModelMath().getParameter();
		} else if (modelType.equals("otherModel")) {
			parameters = ((OtherModel) model).getModelMath().getParameter();
		} else if (modelType.equals("exposureModel")) {
			parameters = ((ExposureModel) model).getModelMath().getParameter();
		} else if (modelType.equals("toxicologicalModel")) {
			parameters = ((ToxicologicalModel) model).getModelMath().getParameter();
		} else if (modelType.equals("doseResponseModel")) {
			parameters = ((DoseResponseModel) model).getModelMath().getParameter();
		} else if (modelType.equals("processModel")) {
			parameters = ((ProcessModel) model).getModelMath().getParameter();
		} else if (modelType.equals("consumptionModel")) {
			parameters = ((ConsumptionModel) model).getModelMath().getParameter();
		} else if (modelType.equals("riskModel")) {
			parameters = ((RiskModel) model).getModelMath().getParameter();
		} else if (modelType.equals("qraModel")) {
			parameters = ((QraModel) model).getModelMath().getParameter();
		} else {
			parameters = null;
		}

		return parameters;
	}

	public static void setParameter(Model model, List<Parameter> pList) {

		final String modelType = model.getModelType();
		if (modelType.equals("genericModel")) {
			((GenericModel) model).getModelMath().setParameter(pList);
		} else if (modelType.equals("dataModel")) {
			((DataModel) model).getModelMath().setParameter(pList);
		} else if (modelType.equals("predictiveModel")) {
			((PredictiveModel) model).getModelMath().setParameter(pList);
		} else if (modelType.equals("otherModel")) {
			((OtherModel) model).getModelMath().setParameter(pList);
		} else if (modelType.equals("exposureModel")) {
			((ExposureModel) model).getModelMath().setParameter(pList);
		} else if (modelType.equals("toxicologicalModel")) {
			((ToxicologicalModel) model).getModelMath().setParameter(pList);
		} else if (modelType.equals("doseResponseModel")) {
			((DoseResponseModel) model).getModelMath().setParameter(pList);
		} else if (modelType.equals("processModel")) {
			((ProcessModel) model).getModelMath().setParameter(pList);
		} else if (modelType.equals("consumptionModel")) {
			((ConsumptionModel) model).getModelMath().setParameter(pList);
		} else if (modelType.equals("riskModel")) {
			((RiskModel) model).getModelMath().setParameter(pList);
		} else if (modelType.equals("qraModel")) {
			((QraModel) model).getModelMath().setParameter(pList);
		}

	}

	/** @return language written in. Null if missing. */
	public static String getLanguageWrittenIn(Model model) {
		String language;

		final String modelType = model.getModelType();
		if (modelType.equals("genericModel")) {
			language = ((GenericModel) model).getGeneralInformation().getLanguageWrittenIn();
		} else if (modelType.equals("dataModel")) {
			language = null;
		} else if (modelType.equals("predictiveModel")) {
			language = ((PredictiveModel) model).getGeneralInformation().getLanguageWrittenIn();
		} else if (modelType.equals("otherModel")) {
			language = ((OtherModel) model).getGeneralInformation().getLanguageWrittenIn();
		} else if (modelType.equals("exposureModel")) {
			language = ((ExposureModel) model).getGeneralInformation().getLanguageWrittenIn();
		} else if (modelType.equals("toxicologicalModel")) {
			language = ((ToxicologicalModel) model).getGeneralInformation().getLanguageWrittenIn();
		} else if (modelType.equals("doseResponseModel")) {
			language = ((DoseResponseModel) model).getGeneralInformation().getLanguageWrittenIn();
		} else if (modelType.equals("processModel")) {
			language = ((ProcessModel) model).getGeneralInformation().getLanguageWrittenIn();
		} else if (modelType.equals("consumptionModel")) {
			language = ((ConsumptionModel) model).getGeneralInformation().getLanguageWrittenIn();
		} else if (modelType.equals("riskModel")) {
			language = ((RiskModel) model).getGeneralInformation().getLanguageWrittenIn();
		} else if (modelType.equals("qraModel")) {
			language = ((QraModel) model).getGeneralInformation().getLanguageWrittenIn();
		} else {
			language = null;
		}

		return language;
	}

	/** @return language written in. Null if missing. */
	public static String getModelName(Model model) {
		String language;

		final String modelType = model.getModelType();
		if (modelType.equals("genericModel")) {
			language = ((GenericModel) model).getGeneralInformation().getName();
		} else if (modelType.equals("dataModel")) {
			language = ((DataModel) model).getGeneralInformation().getName();
		} else if (modelType.equals("predictiveModel")) {
			language = ((PredictiveModel) model).getGeneralInformation().getName();
		} else if (modelType.equals("otherModel")) {
			language = ((OtherModel) model).getGeneralInformation().getName();
		} else if (modelType.equals("exposureModel")) {
			language = ((ExposureModel) model).getGeneralInformation().getName();
		} else if (modelType.equals("toxicologicalModel")) {
			language = ((ToxicologicalModel) model).getGeneralInformation().getName();
		} else if (modelType.equals("doseResponseModel")) {
			language = ((DoseResponseModel) model).getGeneralInformation().getModelName();
		} else if (modelType.equals("processModel")) {
			language = ((ProcessModel) model).getGeneralInformation().getName();
		} else if (modelType.equals("consumptionModel")) {
			language = ((ConsumptionModel) model).getGeneralInformation().getName();
		} else if (modelType.equals("riskModel")) {
			language = ((RiskModel) model).getGeneralInformation().getName();
		} else if (modelType.equals("qraModel")) {
			language = ((QraModel) model).getGeneralInformation().getName();
		} else {
			language = null;
		}

		return language;
	}

	public static Object getGeneralInformation(Model model) {
		Object information;

		final String modelType = model.getModelType();
		if (modelType.equals("genericModel")) {
			information = ((GenericModel) model).getGeneralInformation();
		} else if (modelType.equals("dataModel")) {
			information = ((DataModel) model).getGeneralInformation();
		} else if (modelType.equals("predictiveModel")) {
			information = ((PredictiveModel) model).getGeneralInformation();
		} else if (modelType.equals("otherModel")) {
			information = ((OtherModel) model).getGeneralInformation();
		} else if (modelType.equals("exposureModel")) {
			information = ((ExposureModel) model).getGeneralInformation();
		} else if (modelType.equals("toxicologicalModel")) {
			information = ((ToxicologicalModel) model).getGeneralInformation();
		} else if (modelType.equals("doseResponseModel")) {
			information = ((DoseResponseModel) model).getGeneralInformation();
		} else if (modelType.equals("processModel")) {
			information = ((ProcessModel) model).getGeneralInformation();
		} else if (modelType.equals("consumptionModel")) {
			information = ((ConsumptionModel) model).getGeneralInformation();
		} else if (modelType.equals("riskModel")) {
			information = ((RiskModel) model).getGeneralInformation();
		} else if (modelType.equals("qraModel")) {
			information = ((QraModel) model).getGeneralInformation();
		} else {
			information = null;
		}

		return information;
	}

	public static Object getScope(Model model) {
		Object scope;

		final String modelType = model.getModelType();
		if (modelType.equals("genericModel")) {
			scope = ((GenericModel) model).getScope();
		} else if (modelType.equals("dataModel")) {
			scope = ((DataModel) model).getScope();
		} else if (modelType.equals("predictiveModel")) {
			scope = ((PredictiveModel) model).getScope();
		} else if (modelType.equals("otherModel")) {
			scope = ((OtherModel) model).getScope();
		} else if (modelType.equals("exposureModel")) {
			scope = ((ExposureModel) model).getScope();
		} else if (modelType.equals("toxicologicalModel")) {
			scope = ((ToxicologicalModel) model).getScope();
		} else if (modelType.equals("doseResponseModel")) {
			scope = ((DoseResponseModel) model).getScope();
		} else if (modelType.equals("processModel")) {
			scope = ((ProcessModel) model).getScope();
		} else if (modelType.equals("consumptionModel")) {
			scope = ((ConsumptionModel) model).getScope();
		} else if (modelType.equals("riskModel")) {
			scope = ((RiskModel) model).getScope();
		} else if (modelType.equals("qraModel")) {
			scope = ((QraModel) model).getScope();
		} else {
			scope = null;
		}

		return scope;
	}

	public static Object getDataBackground(Model model) {
		Object background;

		final String modelType = model.getModelType();
		if (modelType.equals("genericModel")) {
			background = ((GenericModel) model).getDataBackground();
		} else if (modelType.equals("dataModel")) {
			background = ((DataModel) model).getDataBackground();
		} else if (modelType.equals("predictiveModel")) {
			background = ((PredictiveModel) model).getDataBackground();
		} else if (modelType.equals("otherModel")) {
			background = ((OtherModel) model).getDataBackground();
		} else if (modelType.equals("exposureModel")) {
			background = ((ExposureModel) model).getDataBackground();
		} else if (modelType.equals("toxicologicalModel")) {
			background = ((ToxicologicalModel) model).getDataBackground();
		} else if (modelType.equals("doseResponseModel")) {
			background = ((DoseResponseModel) model).getDataBackground();
		} else if (modelType.equals("processModel")) {
			background = ((ProcessModel) model).getDataBackground();
		} else if (modelType.equals("consumptionModel")) {
			background = ((ConsumptionModel) model).getDataBackground();
		} else if (modelType.equals("riskModel")) {
			background = ((RiskModel) model).getDataBackground();
		} else if (modelType.equals("qraModel")) {
			background = ((QraModel) model).getDataBackground();
		} else {
			background = null;
		}

		return background;
	}

	public static Object getModelMath(Model model) {
		Object math;

		final String modelType = model.getModelType();
		if (modelType.equals("genericModel")) {
			math = ((GenericModel) model).getModelMath();
		} else if (modelType.equals("dataModel")) {
			math = ((DataModel) model).getModelMath();
		} else if (modelType.equals("predictiveModel")) {
			math = ((PredictiveModel) model).getModelMath();
		} else if (modelType.equals("otherModel")) {
			math = ((OtherModel) model).getModelMath();
		} else if (modelType.equals("exposureModel")) {
			math = ((ExposureModel) model).getModelMath();
		} else if (modelType.equals("toxicologicalModel")) {
			math = ((ToxicologicalModel) model).getModelMath();
		} else if (modelType.equals("doseResponseModel")) {
			math = ((DoseResponseModel) model).getModelMath();
		} else if (modelType.equals("processModel")) {
			math = ((ProcessModel) model).getModelMath();
		} else if (modelType.equals("consumptionModel")) {
			math = ((ConsumptionModel) model).getModelMath();
		} else if (modelType.equals("riskModel")) {
			math = ((RiskModel) model).getModelMath();
		} else if (modelType.equals("qraModel")) {
			math = ((QraModel) model).getModelMath();
		} else {
			math = null;
		}

		return math;
	}

	public static Parameter cloneParameter(Parameter old) {
		Parameter param = new Parameter();
		param.setClassification(old.getClassification());
		param.setDataType(old.getDataType());
		param.setDescription(old.getDescription());
		param.setError(old.getError());
		param.setId(old.getId());
		param.setMaxValue(old.getMaxValue());
		param.setMinValue(old.getMinValue());
		param.setName(old.getName());
		param.setReference(old.getReference());
		param.setSource(old.getSource());
		param.setSubject(old.getSubject());
		param.setUnit(old.getUnit());
		param.setValue(old.getValue());
		param.setVariabilitySubject(old.getVariabilitySubject());
		return param;
	}

}