/**
 * ==================================================================
 *
 * This file is part of org.openbase.bco.ontology.lib.
 *
 * org.openbase.bco.ontology.lib is free software: you can redistribute it and modify
 * it under the terms of the GNU General Public License (Version 3)
 * as published by the Free Software Foundation.
 *
 * org.openbase.bco.ontology.lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with org.openbase.bco.ontology.lib. If not, see <http://www.gnu.org/licenses/>.
 * ==================================================================
 */
package org.openbase.bco.ontology.lib.testing;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openbase.bco.dal.remote.unit.ColorableLightRemote;
import org.openbase.bco.dal.remote.unit.PowerSwitchRemote;
import org.openbase.bco.dal.remote.unit.Units;
import org.openbase.bco.ontology.lib.commun.web.OntModelWeb;
import org.openbase.bco.ontology.lib.commun.web.SparqlUpdateWeb;
import org.openbase.bco.ontology.lib.manager.aggregation.Aggregation;
import org.openbase.bco.ontology.lib.manager.aggregation.AggregationImpl;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.system.config.StaticSparqlExpression;
import org.openbase.bco.ontology.lib.trigger.Trigger;
import org.openbase.bco.ontology.lib.trigger.TriggerFactory;
import org.openbase.bco.ontology.lib.trigger.sparql.AskQueryExample;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.ObservableImpl;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.schedule.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.ontology.OntologyChangeType.OntologyChange;
import rst.domotic.ontology.TriggerConfigType.TriggerConfig;
import rst.domotic.state.ActivationStateType.ActivationState;
import rst.domotic.state.PowerStateType.PowerState;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author agatting on 17.04.17.
 */
public class Measurement {

    private static final Logger LOGGER = LoggerFactory.getLogger(Measurement.class);
    private ObservableImpl<Boolean> triggerMeasurementObservable = null;
    private static final String FILE_NAME = "TriggerMeasurement.xlsx";
    private final ColorableLightRemote colorableLightRemote;
    private final PowerSwitchRemote powerSwitchRemote;
    private final Stopwatch measurementWatch;
    private final static int TRIGGER_MAX_COUNT = 5; //1000
    private int simpleTriggerCurCount;
    private boolean simpleQueryActive;
    private final static int DAYS_MAX_COUNT = 365; //365
    private int daysCurCount;
    private final DuplicateData duplicateData;
    private boolean finishedMeasurement;
    private final Aggregation aggregation;
    private final Stopwatch stopwatch;
    private final List<Long> simpleQuMeasuredValues;
    private final List<Long> complexQuMeasuredValues;
    private long numberOfTriple;

//    private static final String SIMPLE_QUERY =
//            "PREFIX NS: <http://www.openbase.org/bco/ontology#> "
//                    + "ASK { "
//                        + "?obs a NS:Observation . "
//                        + "?obs NS:hasUnitId ?unit . "
//                        + "?obs NS:hasStateValue NS:ON . "
//                        + "?unit a NS:ColorableLight . "
//                    + "}";

    public static final String COMPLEX_QUERY =
            "PREFIX NS: <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> "
                + "ASK { "
                    + "{ SELECT ?timeA ?unitA WHERE { "
                        + "?obsA a NS:Observation . "
                        + "?obsA NS:hasTimeStamp ?timeA . "
                        + "?obsA NS:hasUnitId ?unitA . "
                        + "?unitA a NS:PowerSwitch . "
                        + "?obsA NS:hasProviderService NS:PowerStateService . "
                        + "?obsA NS:hasStateValue NS:OFF . "
                    + "} "
                    + "ORDER BY DESC(?timeA) "
                    + "LIMIT 10 } . "
                    + "{ SELECT ?timeB ?unitB WHERE { "
                        + "?obsB a NS:Observation . "
                        + "?obsB NS:hasTimeStamp ?timeB . "
                        + "?obsB NS:hasUnitId ?unitB . "
                        + "?unitB a NS:ColorableLight . "
                        + "?obsB NS:hasProviderService NS:PowerStateService . "
                        + "?obsB NS:hasStateValue NS:ON . "
                    + "} "
                    + "ORDER BY DESC(?timeB) "
                    + "LIMIT 10 } . "
                    + "?obsA NS:hasUnitId ?unitA . "
                    + "?obsA NS:hasTimeStamp ?timeA . "
                    + "?obsB NS:hasUnitId ?unitB . "
                    + "?obsB NS:hasTimeStamp ?timeB . "
                    + "BIND(minutes(xsd:dateTime(?timeA)) as ?minuteA) . "
                    + "BIND(minutes(xsd:dateTime(?timeB)) as ?minuteB) . "
                    + "FILTER (?minuteA = ?minuteB) . "
                + "}";

    private static final String SIMPLE_QUERY = AskQueryExample.QUERY_0;

    public Measurement() throws InterruptedException, CouldNotPerformException, JPServiceException {
        this.measurementWatch = new Stopwatch();
        this.triggerMeasurementObservable = new ObservableImpl<>(false, this);
        this.colorableLightRemote = (ColorableLightRemote) Units.getUnit("a0f2c9d8-41a6-45c6-9609-5686b6733d4e", true);
        this.powerSwitchRemote = (PowerSwitchRemote) Units.getUnit("d2b8b0e7-dd37-4d89-822c-d66d38dfb6e0", true);
        this.simpleTriggerCurCount = 0;
        this.simpleQueryActive = true;
        this.daysCurCount = 0;
        this.duplicateData = new DuplicateData();
//        this.measuredValues = new Long[DAYS_MAX_COUNT][TRIGGER_MAX_COUNT + 1];
        this.finishedMeasurement = false;
        this.aggregation = new AggregationImpl();
        this.stopwatch = new Stopwatch();
        this.simpleQuMeasuredValues = new ArrayList<>();
        this.complexQuMeasuredValues = new ArrayList<>();
        this.numberOfTriple = 0L;

        init();
        startAggregatedDataMeasurement();
    }

    private void startMeasurementData() throws InterruptedException, CouldNotPerformException, JPServiceException {

        if (simpleTriggerCurCount < TRIGGER_MAX_COUNT) {
            measurementWatch.restart();

            if (simpleQueryActive) {
                colorableLightRemote.setPowerState(PowerState.State.OFF);
            } else {
                powerSwitchRemote.setPowerState(PowerState.State.OFF);
            }
        } else {
            putIntoExcelFile("TriggerSimpleMeasureData", simpleQuMeasuredValues, complexQuMeasuredValues, daysCurCount);
            daysCurCount++;
            simpleQuMeasuredValues.clear();
            complexQuMeasuredValues.clear();

            if (daysCurCount < DAYS_MAX_COUNT) {
                System.out.println("Duplicate data...Day: " + (daysCurCount + 1));

                duplicateData.duplicateDataOfOneDay(daysCurCount);

                askNumberOfTriple();
                simpleTriggerCurCount = 0;
                startMeasurementData();
            } else {
                finishedMeasurement = true;
            }
        }
    }

    private void startMeasurementAggData() throws InterruptedException, CouldNotPerformException, JPServiceException {

        if (simpleTriggerCurCount < TRIGGER_MAX_COUNT) {
            measurementWatch.restart();

            if (simpleQueryActive) {
                colorableLightRemote.setPowerState(PowerState.State.OFF);
            } else {
                powerSwitchRemote.setPowerState(PowerState.State.OFF);
            }
        } else {
            putIntoExcelFile("TriggerSimpleMeasureData", simpleQuMeasuredValues, complexQuMeasuredValues, daysCurCount);
            daysCurCount++;
            simpleQuMeasuredValues.clear();
            complexQuMeasuredValues.clear();

            if (daysCurCount < DAYS_MAX_COUNT) {
                SparqlUpdateWeb.sparqlUpdateToMainOntologyViaRetry(StaticSparqlExpression.deleteAllObservations, OntConfig.ServerServiceForm.UPDATE);
                aggregation.startAggregation(daysCurCount);
                stopwatch.waitForStart(2000);

                System.out.println("Duplicate data...Day: " + (daysCurCount + 1));
                duplicateData.duplicateDataOfAggObs(daysCurCount);

                askNumberOfTriple();
                simpleTriggerCurCount = 0;
                startMeasurementAggData();
            } else {
                finishedMeasurement = true;
            }
        }
    }

    private void init() throws InterruptedException, JPServiceException {
        InputStream input = Measurement.class.getResourceAsStream("/apartmentDataSimpleWithoutObs.owl");
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        ontModel.read(input, null);

//        final OntModel baseOntModel = OntologyToolkit.loadOntModelFromFile(null, "src/apartmentDataSimpleWithoutObs.owl");
        OntModelWeb.addOntModelViaRetry(ontModel);
    }

    private void startAggregatedDataMeasurement() throws InterruptedException, JPServiceException, CouldNotPerformException {
        System.out.println("Duplicate data...Day: 1");
        duplicateData.duplicateDataOfAggObs(daysCurCount);
        askNumberOfTriple();

        Trigger();
        final Observer<Boolean> activationObserver = (source, data) -> startMeasurementAggData();
        triggerMeasurementObservable.addObserver(activationObserver);
        startMeasurementAggData();
    }

    private void startNotAggregatedDataMeasurement() throws InterruptedException, JPServiceException, CouldNotPerformException {
        System.out.println("Duplicate data...Day: 1");
        duplicateData.duplicateDataOfOneDay(daysCurCount);
        askNumberOfTriple();

        Trigger();
        final Observer<Boolean> activationObserver = (source, data) -> startMeasurementData();
        triggerMeasurementObservable.addObserver(activationObserver);
        startMeasurementData();
    }

    private void askNumberOfTriple() throws InterruptedException, JPServiceException {

        final ResultSet resultSet = SparqlUpdateWeb.sparqlQuerySelectViaRetry(StaticSparqlExpression.countAllTriples);
        Long numTriples = 0L;

        if (resultSet.hasNext()) {
            final QuerySolution querySolution = resultSet.next();
            final String count = querySolution.get("count").asLiteral().getLexicalForm();
            numTriples = Long.parseLong(count);
        } else {
            LOGGER.error("There is no resultSet to identify the number of triples! Set to 0.");
        }
        numberOfTriple = numTriples;
//        measuredValues[daysCurCount][0] = numTriples;
    }

    private void Trigger() throws InterruptedException {
        try {
            final OntologyChange ontologyChange = OntologyChange.newBuilder().addUnitType(UnitType.COLORABLE_LIGHT).build();
            final TriggerConfig triggerConfig = TriggerConfig.newBuilder().setLabel("trigger0").setQuery(SIMPLE_QUERY)
                    .setDependingOntologyChange(ontologyChange).build();

            final TriggerFactory triggerFactory = new TriggerFactory();
            final Trigger trigger = triggerFactory.newInstance(triggerConfig);

            trigger.addObserver((Observable<ActivationState.State> source, ActivationState.State data) -> {
                if (measurementWatch.isRunning() && !finishedMeasurement && simpleQueryActive) {

                    measurementWatch.stop();
                    simpleQuMeasuredValues.add(measurementWatch.getTime());

//                    measuredValues[daysCurCount][simpleTriggerCurCount + 1] = measurementWatch.getTime();
//                    simpleTriggerCurCount++;
                    simpleQueryActive = false;

                    System.out.println(trigger.getTriggerConfig().getLabel() + " is " + data);
                    System.out.println("measured time of simple query: " + measurementWatch.getTime());

                    triggerMeasurementObservable.notifyObservers(true);
                } else if (!finishedMeasurement) {
                    LOGGER.error("Stopwatch is not running!");
                }
            });
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER);
        }

        try {
            final OntologyChange ontologyChange = OntologyChange.newBuilder().addUnitType(UnitType.POWER_SWITCH).build();
            final TriggerConfig triggerConfig = TriggerConfig.newBuilder().setLabel("trigger1").setQuery(COMPLEX_QUERY)
                    .setDependingOntologyChange(ontologyChange).build();

            final TriggerFactory triggerFactory = new TriggerFactory();
            final Trigger trigger = triggerFactory.newInstance(triggerConfig);

            trigger.addObserver((Observable<ActivationState.State> source, ActivationState.State data) -> {
                if (measurementWatch.isRunning() && !finishedMeasurement && !simpleQueryActive) {

                    measurementWatch.stop();
                    complexQuMeasuredValues.add(measurementWatch.getTime());
//                    measuredValues[daysCurCount][simpleTriggerCurCount + 1] = measurementWatch.getTime();

                    simpleTriggerCurCount++;
                    simpleQueryActive = true;

                    System.out.println(trigger.getTriggerConfig().getLabel() + " is " + data);
                    System.out.println("measured time of complex query: " + measurementWatch.getTime());

                    triggerMeasurementObservable.notifyObservers(true);
                } else if (!finishedMeasurement) {
                    LOGGER.error("Stopwatch is not running!");
                }
            });
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER);
        }
    }

    private void putIntoExcelFile(final String sheetName, final List<Long> simpleQuMeasuredValues, final List<Long> complexQuMeasuredValues, int daysCurCount) {
        // https://www.mkyong.com/java/apache-poi-reading-and-writing-excel-file-in-java/
        XSSFWorkbook workbook;
        XSSFSheet sheet;
        Row row;

        try {
            FileInputStream excelFile = new FileInputStream(new File(FILE_NAME));
            workbook = new XSSFWorkbook(excelFile);
            sheet = workbook.getSheet(sheetName);
        } catch (IOException e) {
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet(sheetName);
            row = sheet.createRow(daysCurCount);

            row.createCell(0).setCellValue("Days");
            row.createCell(1).setCellValue("Triple");
            row.createCell(2).setCellValue("Mean of simple trigger");
            row.createCell(3).setCellValue("Mean of complex trigger");
        }

        row = sheet.createRow(daysCurCount + 1);

//        System.out.println("Creating excel");
        System.out.println("simple: " + simpleQuMeasuredValues);
        System.out.println("complex: " + complexQuMeasuredValues);

        long sumSimple = 0L;
        long sumComplex = 0L;

        for (final long valueSimple : simpleQuMeasuredValues) {
            sumSimple += valueSimple;
        }
        for (final long valueComplex : complexQuMeasuredValues) {
            sumComplex += valueComplex;
        }

        // number of days
        final Cell cellDay = row.createCell(0);
        cellDay.setCellValue(daysCurCount + 1);

        // number of triple
        final Cell cellTriple = row.createCell(1);
        cellTriple.setCellValue(numberOfTriple);

        // mean of simple trigger time
        final Cell cellMeanSimple = row.createCell(2);
        cellMeanSimple.setCellValue(sumSimple / simpleQuMeasuredValues.size());

        // mean of complex trigger time
        final Cell cellMeanComplex = row.createCell(3);
        cellMeanComplex.setCellValue(sumComplex/ complexQuMeasuredValues.size());

        try {
            final File file = new File(FILE_NAME);
            file.setReadable(true, false);
            file.setExecutable(true, false);
            file.setWritable(true, false);
            System.out.println("Save data row ...");
            final FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            ExceptionPrinter.printHistory(e, LOGGER);
        }
//        System.out.println("Done");
    }

}
