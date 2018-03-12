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

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;
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
import org.openbase.bco.ontology.lib.commun.web.OntModelHttp;
import org.openbase.bco.ontology.lib.commun.web.SparqlHttp;
import org.openbase.bco.ontology.lib.manager.aggregation.Aggregation;
import org.openbase.bco.ontology.lib.manager.aggregation.AggregationImpl;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.utility.sparql.QueryExpression;
import org.openbase.bco.ontology.lib.trigger.Trigger;
import org.openbase.bco.ontology.lib.trigger.TriggerFactory;
import org.openbase.bco.ontology.lib.trigger.sparql.AskQueryExample;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
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
import java.util.concurrent.ExecutionException;

/**
 * @author agatting on 17.04.17.
 */
public class Measurement {

    private static final Logger LOGGER = LoggerFactory.getLogger(Measurement.class);
    private ObservableImpl<Boolean> triggerMeasurementObservable = null;
    private static final String FILE_NAME = "TriggerMeasurementAggDataBasedOnNormalDay.xlsx";
    private final ColorableLightRemote colorableLightRemote;
    private final PowerSwitchRemote powerSwitchRemote;
    public static Stopwatch measurementWatch = new Stopwatch();
    private final static int TRIGGER_MAX_COUNT = 1000; //1000
    private int triggerCount;
    private boolean simpleQueryActive;
    private final static int DAYS_MAX_COUNT = 10; //365
    private int daysCurCount;
    private final DuplicateData duplicateData;
    private boolean isMeasurementFinished;
    private final Aggregation aggregation;
    private final Stopwatch stopwatch;
    private final List<Long> simpleQuMeasuredValues;
    private final List<Long> complexQuMeasuredValues;
    private long numberOfTriple;
    private DataVolume curDataVolume;
    private boolean isPowerOn;

    public static List<Long> unitChange = new ArrayList<>();
    public static List<Long> sendSPARQL = new ArrayList<>();
    public static List<Long> answerOfServerToOM = new ArrayList<>();
    public static List<Long> triggerImplFromRSB = new ArrayList<>();
    public static List<Long> triggerEnd = new ArrayList<>();

    private static final String SIMPLE_QUERY =
            "PREFIX NAMESPACE: <http://www.openbase.org/bco/ontology#> "
                    + "ASK { "
                        + "?obs a NAMESPACE:OntObservation . "
                        + "?obs NAMESPACE:hasUnitId ?unit . "
                        + "?obs NAMESPACE:hasStateValue NAMESPACE:ON . "
                        + "?unit a NAMESPACE:ColorableLight . "
                    + "}";

//    public static final String COMPLEX_QUERY =
//            "PREFIX NAMESPACE: <http://www.openbase.org/bco/ontology#> "
//            + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> "
//                + "ASK { "
//                    + "{ SELECT ?timeA ?unitA WHERE { "
//                        + "?obsA a NAMESPACE:OntObservation . "
//                        + "?obsA NAMESPACE:hasTimeStamp ?timeA . "
//                        + "?obsA NAMESPACE:hasUnitId ?unitA . "
//                        + "?unitA a NAMESPACE:PowerSwitch . "
//                        + "?obsA NAMESPACE:hasProviderService NAMESPACE:PowerStateService . "
//                        + "?obsA NAMESPACE:hasStateValue NAMESPACE:OFF . "
//                    + "} "
//                    + "ORDER BY DESC(?timeA) "
//                    + "LIMIT 10 } . "
//                    + "{ SELECT ?timeB ?unitB WHERE { "
//                        + "?obsB a NAMESPACE:OntObservation . "
//                        + "?obsB NAMESPACE:hasTimeStamp ?timeB . "
//                        + "?obsB NAMESPACE:hasUnitId ?unitB . "
//                        + "?unitB a NAMESPACE:ColorableLight . "
//                        + "?obsB NAMESPACE:hasProviderService NAMESPACE:PowerStateService . "
//                        + "?obsB NAMESPACE:hasStateValue NAMESPACE:ON . "
//                    + "} "
//                    + "ORDER BY DESC(?timeB) "
//                    + "LIMIT 10 } . "
//                    + "?obsA NAMESPACE:hasUnitId ?unitA . "
//                    + "?obsA NAMESPACE:hasTimeStamp ?timeA . "
//                    + "?obsB NAMESPACE:hasUnitId ?unitB . "
//                    + "?obsB NAMESPACE:hasTimeStamp ?timeB . "
//                    + "BIND(minutes(xsd:dateTime(?timeA)) as ?minuteA) . "
//                    + "BIND(minutes(xsd:dateTime(?timeB)) as ?minuteB) . "
//                    + "FILTER (?minuteA = ?minuteB) . "
//                + "}";

    private static final String COMPLEX_QUERY = AskQueryExample.QUERY_0;

    public Measurement() throws InterruptedException, CouldNotPerformException, ExecutionException {
//        this.measurementWatch = new Stopwatch();
        this.triggerMeasurementObservable = new ObservableImpl<>(false, this);
//        this.colorableLightRemote = (ColorableLightRemote) Units.getUnit("a0f2c9d8-41a6-45c6-9609-5686b6733d4e", true); //old db
        this.colorableLightRemote = (ColorableLightRemote) Units.getUnit("0529ab14-ea69-4c54-9e7a-2288f7b0a5ec", true);
//        this.powerSwitchRemote = (PowerSwitchRemote) Units.getUnit("d2b8b0e7-dd37-4d89-822c-d66d38dfb6e0", true); //old db
        this.powerSwitchRemote = (PowerSwitchRemote) Units.getUnit("ad275d31-f2ed-4917-9c7f-29e7a83b3c67", true);
        this.triggerCount = 0;
        this.simpleQueryActive = true;
        this.daysCurCount = 0;
        this.duplicateData = new DuplicateData();
//        this.measuredValues = new Long[DAYS_MAX_COUNT][TRIGGER_MAX_COUNT + 1];
        this.isMeasurementFinished = false;
        this.aggregation = new AggregationImpl(null, null, null); //TODO Test values...
        this.stopwatch = new Stopwatch();
        this.simpleQuMeasuredValues = new ArrayList<>();
        this.complexQuMeasuredValues = new ArrayList<>();
        this.numberOfTriple = 0L;
        this.curDataVolume = DataVolume.CONFIG;
        this.isPowerOn = false;

//        init();
//        addNormalDataSetToServer();
//        startAggregatedDataMeasurement();
        initSimplePerformanceMeasurement();
    }

    private void initSimplePerformanceMeasurement() throws InterruptedException, CouldNotPerformException {
        addNormalDataSetToServer();

        simpleTrigger();
        final Observer<Boolean> activationObserver = (source, data) -> startSimplePerformanceMeasurement();
        triggerMeasurementObservable.addObserver(activationObserver);
        startSimplePerformanceMeasurement();
    }

    private void startSimplePerformanceMeasurement() throws InterruptedException, CouldNotPerformException {
        if (triggerCount < TRIGGER_MAX_COUNT) {
            measurementWatch.restart();

            if (isPowerOn) {
                isPowerOn = false;
                colorableLightRemote.setPowerState(PowerState.State.OFF);
            } else {
                isPowerOn = true;
                colorableLightRemote.setPowerState(PowerState.State.ON);
            }
        } else {
            isMeasurementFinished = true;
//            triggerCount = 0;

            final double varianceUnitChange = StatUtils.variance(convertToArray(unitChange));
            final double variancesendSPARQL = StatUtils.variance(convertToArray(sendSPARQL));
            final double varianceanswerOfServerToOM = StatUtils.variance(convertToArray(answerOfServerToOM));
            final double variancetriggerImplFromRSB = StatUtils.variance(convertToArray(triggerImplFromRSB));
            final double variancetriggerEnd = StatUtils.variance(convertToArray(triggerEnd));

            final double meanUnitChange = StatUtils.mean(convertToArray(unitChange));
            final double meansendSPARQL = StatUtils.mean(convertToArray(sendSPARQL));
            final double meananswerOfServerToOM = StatUtils.mean(convertToArray(answerOfServerToOM));
            final double meantriggerImplFromRSB = StatUtils.mean(convertToArray(triggerImplFromRSB));
            final double meantriggerEnd = StatUtils.mean(convertToArray(triggerEnd));

            final double standardDeviationUnitChange = FastMath.sqrt(StatUtils.variance(convertToArray(unitChange)));
            final double standardDeviationsendSPARQL = FastMath.sqrt(StatUtils.variance(convertToArray(sendSPARQL)));
            final double standardDeviationanswerOfServerToOM = FastMath.sqrt(StatUtils.variance(convertToArray(answerOfServerToOM)));
            final double standardDeviationtriggerImplFromRSB = FastMath.sqrt(StatUtils.variance(convertToArray(triggerImplFromRSB)));
            final double standardDeviationtriggerEnd = FastMath.sqrt(StatUtils.variance(convertToArray(triggerEnd)));

            final double medianUnitChange = StatUtils.percentile(convertToArray(unitChange), 50);
            final double mediansendSPARQL = StatUtils.percentile(convertToArray(sendSPARQL), 50);
            final double mediananswerOfServerToOM = StatUtils.percentile(convertToArray(answerOfServerToOM), 50);
            final double mediantriggerImplFromRSB = StatUtils.percentile(convertToArray(triggerImplFromRSB), 50);
            final double mediantriggerEnd = StatUtils.percentile(convertToArray(triggerEnd), 50);

            System.out.println("Variance (bco, om, server-update, trigger, query: " + varianceUnitChange + ", " + variancesendSPARQL + ", " + varianceanswerOfServerToOM + ", " + variancetriggerImplFromRSB + ", " + variancetriggerEnd);
            System.out.println("Mean (bco, om, server-update, trigger, query: " + meanUnitChange + ", " + meansendSPARQL + ", " + meananswerOfServerToOM + ", " + meantriggerImplFromRSB + ", " + meantriggerEnd);
            System.out.println("StandardDeviation (bco, om, server-update, trigger, query: " + standardDeviationUnitChange + ", " + standardDeviationsendSPARQL + ", " + standardDeviationanswerOfServerToOM + ", " + standardDeviationtriggerImplFromRSB + ", " + standardDeviationtriggerEnd);
            System.out.println("Median (bco, om, server-update, trigger, query: " + medianUnitChange + ", " + mediansendSPARQL + ", " + mediananswerOfServerToOM + ", " + mediantriggerImplFromRSB + ", " + mediantriggerEnd);

//            unitChange.clear();
//            sendSPARQL.clear();
//            answerOfServerToOM.clear();
//            triggerImplFromRSB.clear();
//            triggerEnd.clear();
        }
    }

    private double[] convertToArray(final List<Long> stateValues) {
        final double stateValuesArray[] = new double[stateValues.size()];

        for (int i = 0; i < stateValues.size(); i++) {
            stateValuesArray[i] = stateValues.get(i).doubleValue();
        }

        return stateValuesArray;
    }

    private void simpleTrigger() throws InterruptedException, CouldNotPerformException {
        final TriggerConfig triggerConfig = TriggerConfig.newBuilder().setLabel("trigger0").setQuery(COMPLEX_QUERY)
                .setDependingOntologyChange(OntologyChange.newBuilder().addUnitType(UnitType.COLORABLE_LIGHT).build()).build();

        final TriggerFactory triggerFactory = new TriggerFactory();
        final Trigger trigger = triggerFactory.newInstance(triggerConfig);

        trigger.addObserver((Observable<ActivationState.State> source, ActivationState.State data) -> {
            if (measurementWatch.isRunning() && !isMeasurementFinished) {

                measurementWatch.stop();
                Measurement.triggerEnd.add(Measurement.measurementWatch.getTime());
                triggerCount++;

                triggerMeasurementObservable.notifyObservers(true);
            } else if (!isMeasurementFinished) {
                LOGGER.error("Stopwatch is not running!");
            }
        });
    }

    private void startMeasurementData() throws InterruptedException, CouldNotPerformException {

        if (triggerCount < TRIGGER_MAX_COUNT) {
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
                stopwatch.waitForStart(2000);

                duplicateData.duplicateDataOfOneDay(daysCurCount);

                askNumberOfTriple();
                triggerCount = 0;
                startMeasurementData();
            } else {
                isMeasurementFinished = true;
            }
        }
    }

    private void startMeasurementAggData() throws InterruptedException, CouldNotPerformException, ExecutionException {

        if (triggerCount < TRIGGER_MAX_COUNT) {
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
                SparqlHttp.uploadSparqlRequest(QueryExpression.DELETE_ALL_OBSERVATIONS_WITH_FILTER, OntConfig.getOntologyDbUrl(), 0);
                aggregation.startAggregation(daysCurCount);
                stopwatch.waitForStart(2000);

                System.out.println("Duplicate data...Day: " + (daysCurCount + 1));
                duplicateData.duplicateDataOfAggObs(daysCurCount);

                askNumberOfTriple();
                triggerCount = 0;
                startMeasurementAggData();
            } else {
                isMeasurementFinished = true;
            }
        }
    }

    private void initMemoryTest() throws InterruptedException, CouldNotPerformException {
        init();
        System.out.println("TripleCount for configData only: " + askNumberOfTriple());

        Trigger();
        final Observer<Boolean> activationObserver = (source, data) -> startMeasurementMemoryTest();
        triggerMeasurementObservable.addObserver(activationObserver);
        startMeasurementMemoryTest();
    }

    private void startMeasurementMemoryTest() throws InterruptedException, CouldNotPerformException {

        if (triggerCount < TRIGGER_MAX_COUNT) {
            measurementWatch.restart();

            if (simpleQueryActive) {
                colorableLightRemote.setPowerState(PowerState.State.OFF);
            } else {
                powerSwitchRemote.setPowerState(PowerState.State.OFF);
            }
        } else {
            if (!curDataVolume.equals(DataVolume.UNKNOWN)) {
                saveMemoryTestValues("MeasureData", simpleQuMeasuredValues, complexQuMeasuredValues, curDataVolume);
                simpleQuMeasuredValues.clear();
                complexQuMeasuredValues.clear();

                switch (curDataVolume) {
                    case CONFIG:
                        curDataVolume = DataVolume.CONFIG_DAY;
                        System.out.println("Duplicate data...Day: 1");
                        duplicateData.duplicateDataOfOneDay(0);
                        stopwatch.waitForStart(2000);
                        System.out.println("TripleCount for configData and dayData: " + askNumberOfTriple());
                        triggerCount = 0;
                        break;
                    case CONFIG_DAY:
                        curDataVolume = DataVolume.CONFIG_WEEK;
                        for (int i = 1; i < 4; i++) { // start by 1, because there is a dayData already
                            stopwatch.waitForStart(2000);
                            System.out.println("Duplicate data...Day: " + (i + 1));
                            duplicateData.duplicateDataOfOneDay(i);
                        }

                        System.out.println("TripleCount for configData and 7x dayData: " + askNumberOfTriple());
                        triggerCount = 0;
                        break;
                    default:
                        curDataVolume = DataVolume.UNKNOWN;
                        break;
                }
                startMeasurementMemoryTest();
            } else {
                System.out.println("Finished!");
            }
        }
    }

    private void init() throws InterruptedException, NotAvailableException {
        InputStream input = Measurement.class.getResourceAsStream("/apartmentDataSimpleWithoutObs.owl");
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        ontModel.read(input, null);

//        final OntModel baseOntModel = StringModifier.loadOntModelFromFile(null, "src/apartmentDataSimpleWithoutObs.owl");
        OntModelHttp.addModelToServer(ontModel, OntConfig.getOntologyDbUrl(), 0);
    }

    private void addNormalDataSetToServer() throws InterruptedException, NotAvailableException {
        InputStream input = Measurement.class.getResourceAsStream("/apartmentDataSimple.owl");
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        ontModel.read(input, null);

        OntModelHttp.addModelToServer(ontModel, OntConfig.getOntologyDbUrl(), 0);
    }

    private void startAggregatedDataMeasurement() throws InterruptedException, CouldNotPerformException, ExecutionException {
        System.out.println("Duplicate data...Day: 1");
        duplicateData.duplicateDataOfAggObs(daysCurCount);
        askNumberOfTriple();

        Trigger();
        final Observer<Boolean> activationObserver = (source, data) -> startMeasurementAggData();
        triggerMeasurementObservable.addObserver(activationObserver);
        startMeasurementAggData();
    }

    private void startDataMeasurement() throws InterruptedException, CouldNotPerformException {
        System.out.println("Duplicate data...Day: 1");
        duplicateData.duplicateDataOfOneDay(daysCurCount);
        askNumberOfTriple();

        Trigger();
        final Observer<Boolean> activationObserver = (source, data) -> startMeasurementData();
        triggerMeasurementObservable.addObserver(activationObserver);
        startMeasurementData();
    }

    private long askNumberOfTriple() throws InterruptedException {

        try {
            final ResultSet resultSet = SparqlHttp.sparqlQuery(QueryExpression.COUNT_ALL_TRIPLES, OntConfig.getOntologyDbUrl(), 0);
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
            return numTriples;
        } catch (ExecutionException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
        }
        return 0L;
    }

    private void Trigger() throws InterruptedException {
        try {
            final OntologyChange ontologyChange = OntologyChange.newBuilder().addUnitType(UnitType.COLORABLE_LIGHT).build();
            final TriggerConfig triggerConfig = TriggerConfig.newBuilder().setLabel("trigger0").setQuery(SIMPLE_QUERY)
                    .setDependingOntologyChange(ontologyChange).build();

            final TriggerFactory triggerFactory = new TriggerFactory();
            final Trigger trigger = triggerFactory.newInstance(triggerConfig);

            trigger.addObserver((Observable<ActivationState.State> source, ActivationState.State data) -> {
                if (measurementWatch.isRunning() && !isMeasurementFinished && simpleQueryActive) {

                    measurementWatch.stop();
                    simpleQuMeasuredValues.add(measurementWatch.getTime());

//                    measuredValues[daysCurCount][triggerCount + 1] = measurementWatch.getTime();
//                    triggerCount++;
                    simpleQueryActive = false;

                    System.out.println(trigger.getTriggerConfig().getLabel() + " is " + data);
                    System.out.println("measured time of simple query: " + measurementWatch.getTime());
//                    System.out.println("measured time of complex query: " + duration);

                    triggerMeasurementObservable.notifyObservers(true);
                } else if (!isMeasurementFinished) {
                    LOGGER.error("Stopwatch is not running!");
                }
            });
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
        }

        try {
            final OntologyChange ontologyChange = OntologyChange.newBuilder().addUnitType(UnitType.POWER_SWITCH).build();
            final TriggerConfig triggerConfig = TriggerConfig.newBuilder().setLabel("trigger1").setQuery(COMPLEX_QUERY)
                    .setDependingOntologyChange(ontologyChange).build();

            final TriggerFactory triggerFactory = new TriggerFactory();
            final Trigger trigger = triggerFactory.newInstance(triggerConfig);

            trigger.addObserver((Observable<ActivationState.State> source, ActivationState.State data) -> {
                if (measurementWatch.isRunning() && !isMeasurementFinished && !simpleQueryActive) {

                    measurementWatch.stop();
                    complexQuMeasuredValues.add(measurementWatch.getTime());
//                    measuredValues[daysCurCount][triggerCount + 1] = measurementWatch.getTime();

                    triggerCount++;
                    simpleQueryActive = true;

                    System.out.println(trigger.getTriggerConfig().getLabel() + " is " + data);
                    System.out.println("measured time of complex query: " + measurementWatch.getTime());
//                    System.out.println("measured time of complex query: " + duration);

                    triggerMeasurementObservable.notifyObservers(true);
                } else if (!isMeasurementFinished) {
                    LOGGER.error("Stopwatch is not running!");
                }
            });
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER);
        }
    }

    private enum DataVolume {
        CONFIG,
        CONFIG_DAY,
        CONFIG_WEEK,
        UNKNOWN
    }

    private void saveMemoryTestValues(final String sheetName, final List<Long> simpleQuMeasuredValues, final List<Long> complexQuMeasuredValues, final DataVolume dataVolume) {
        XSSFWorkbook workbook;
        XSSFSheet sheet;
        Row rowSimple;
        Row rowComplex;

        try {
            FileInputStream excelFile = new FileInputStream(new File(FILE_NAME));
            workbook = new XSSFWorkbook(excelFile);
            sheet = workbook.getSheet(sheetName);
            rowSimple = sheet.getRow(1);
            rowComplex = sheet.getRow(2);
        } catch (IOException ex) {
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet(sheetName);
            final Row row = sheet.createRow(0);
            rowSimple = sheet.createRow(1);
            rowComplex = sheet.createRow(2);

            row.createCell(1).setCellValue("ConfigData only");
            row.createCell(2).setCellValue("ConfigData and dayData");
            row.createCell(3).setCellValue("ConfigData and 4x dayData");
        }


        long sumSimple = 0L;
        long sumComplex = 0L;

        for (final long valueSimple : simpleQuMeasuredValues) {
            sumSimple += valueSimple;
        }
        for (final long valueComplex : complexQuMeasuredValues) {
            sumComplex += valueComplex;
        }

        int column = 0;

        switch (dataVolume) {
            case CONFIG:
                column = 1;
                break;
            case CONFIG_DAY:
                column = 2;
                break;
            case CONFIG_WEEK:
                column = 3;
                break;
            default:
                break;
        }

        System.out.println("Save date in column: " + column);

        // mean of simple trigger time
        final Cell cellMeanSimple = rowSimple.createCell(column);
        cellMeanSimple.setCellValue(sumSimple / simpleQuMeasuredValues.size());

        // mean of complex trigger time
        final Cell cellMeanComplex = rowComplex.createCell(column);
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
        } catch (IOException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER);
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
        } catch (IOException ex) {
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet(sheetName);
            row = sheet.createRow(daysCurCount);

            row.createCell(0).setCellValue("Days");
            row.createCell(1).setCellValue("Triple");
            row.createCell(2).setCellValue("Mean of simple trigger");
            row.createCell(3).setCellValue("Mean of complex trigger");
        }

        row = sheet.createRow(daysCurCount + 1);

        System.out.println("simple: " + simpleQuMeasuredValues);
        System.out.println("simple count: " + simpleQuMeasuredValues.size());
        System.out.println("complex: " + complexQuMeasuredValues);
        System.out.println("complex count: " + complexQuMeasuredValues.size());

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
        } catch (IOException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER);
        }
    }

}
