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
import org.openbase.bco.dal.remote.unit.Units;
import org.openbase.bco.ontology.lib.commun.web.OntModelWeb;
import org.openbase.bco.ontology.lib.commun.web.SparqlUpdateWeb;
import org.openbase.bco.ontology.lib.manager.OntologyToolkit;
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
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.domotic.state.ActivationStateType.ActivationState;
import rst.domotic.state.PowerStateType.PowerState;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * @author agatting on 17.04.17.
 */
public class Measurement {

    private static final Logger LOGGER = LoggerFactory.getLogger(Measurement.class);
    private ObservableImpl<Boolean> triggerMeasurementObservable = null;
private static final String FILE_NAME = "TriggerMeasurement.xlsx";
    private final ColorableLightRemote colorableLightRemote;
    private final Stopwatch measurementWatch;
    private final static int TRIGGER_MAX_COUNT = 10; //1000
    private int triggerCurrentCount;
    private final static int DAYS_MAX_COUNT = 1; //365
    private int daysCurrentCount;
    private final ManipulateData manipulateData;
    private final Long[][] measuredValues;
    private boolean finishedMeasurement;

    private static final String SIMPLE_QUERY =
            "PREFIX NS: <http://www.openbase.org/bco/ontology#> "
                    + "ASK { "
                        + "?obs a NS:Observation . "
                        + "?obs NS:hasUnitId ?unit . "
                        + "?obs NS:hasStateValue NS:ON . "
                        + "?unit a NS:ColorableLight . "
                    + "}";

    private static final String COMPLEX_QUERY = AskQueryExample.QUERY_0;

    public Measurement() throws InterruptedException, CouldNotPerformException, JPServiceException {
        this.measurementWatch = new Stopwatch();
        this.triggerMeasurementObservable = new ObservableImpl<>(false, this);
        this.colorableLightRemote = (ColorableLightRemote) Units.getUnit("a0f2c9d8-41a6-45c6-9609-5686b6733d4e", true);
        this.triggerCurrentCount = 0;
        this.daysCurrentCount = 0;
        this.manipulateData = new ManipulateData();
        this.measuredValues = new Long[DAYS_MAX_COUNT][TRIGGER_MAX_COUNT + 1];
        this.finishedMeasurement = false;

        initOntologyMeasurement();
        Trigger();
        final Observer<Boolean> activationObserver = (source, data) -> startMeasurement();
        triggerMeasurementObservable.addObserver(activationObserver);

        startMeasurement();
    }

    private void startMeasurement() throws InterruptedException, CouldNotPerformException, JPServiceException {

        if (triggerCurrentCount < TRIGGER_MAX_COUNT) {
            System.out.println("start measure...");
            measurementWatch.restart();

            colorableLightRemote.setPowerState(PowerState.State.OFF);
//            if (colorableLightRemote.getPowerState().getValue().equals(PowerState.State.ON)) {
//            } else {
//                colorableLightRemote.setPowerState(PowerState.State.ON);
//            }
        } else {
            daysCurrentCount++;

            if (daysCurrentCount < DAYS_MAX_COUNT) {
                System.out.println("duplicate data...Day: " + daysCurrentCount);

                manipulateData.duplicateDataOfOneDay(daysCurrentCount);
                System.out.println("Uploaded ontModel with day data...");

                askNumberOfTriple();
                triggerCurrentCount = 0;
                startMeasurement();
            } else {
                finishedMeasurement = true;
                createExcelFile("TriggerSimpleMeasure", measuredValues);
            }
        }
    }

    private void initOntologyMeasurement() throws InterruptedException, JPServiceException {
        InputStream input = Measurement.class.getResourceAsStream("/apartmentDataSimpleWithoutObs.owl");
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        ontModel.read(input, null);

//        final OntModel baseOntModel = OntologyToolkit.loadOntModelFromFile(null, "src/apartmentDataSimpleWithoutObs.owl");
        OntModelWeb.addOntModelViaRetry(ontModel);

        System.out.println("Duplicate data...Day: 0");
        manipulateData.duplicateDataOfOneDay(daysCurrentCount);
        System.out.println("Uploaded ontModel...");
        askNumberOfTriple();
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
        measuredValues[daysCurrentCount][0] = numTriples;
    }

    private void Trigger() throws InterruptedException {
        try {
            final OntologyChange ontologyChange = OntologyChange.newBuilder().addCategory(OntologyChange.Category.UNKNOWN)
                    .addUnitType(UnitType.COLORABLE_LIGHT).addServiceType(ServiceType.POWER_STATE_SERVICE).build();
            final TriggerConfig triggerConfig = TriggerConfig.newBuilder().setLabel("trigger0").setQuery(SIMPLE_QUERY)
                    .setDependingOntologyChange(ontologyChange).build();

            final TriggerFactory triggerFactory = new TriggerFactory();
            final Trigger trigger = triggerFactory.newInstance(triggerConfig);

            trigger.addObserver((Observable<ActivationState.State> source, ActivationState.State data) -> {
                if (measurementWatch.isRunning() && !finishedMeasurement) {

                    measurementWatch.stop();
                    measuredValues[daysCurrentCount][triggerCurrentCount + 1] = measurementWatch.getTime();

                    triggerCurrentCount++;

                    System.out.println(trigger.getTriggerConfig().getLabel() + " is " + data);
                    System.out.println("measurementWatch time: " + measurementWatch.getTime());

                    triggerMeasurementObservable.notifyObservers(true);
                } else {
                    LOGGER.error("Stopwatch is not running!");
                }
            });
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER);
        }
    }

    private void createExcelFile(final String sheetName, final Long[][] measuredData) {
        // https://www.mkyong.com/java/apache-poi-reading-and-writing-excel-file-in-java/
        System.out.println("Creating excel");
        System.out.println(Arrays.deepToString(measuredData));

        final XSSFWorkbook workbook = new XSSFWorkbook();
        final XSSFSheet sheet = workbook.createSheet(sheetName);
        int rowNum = 0;
        Row row = sheet.createRow(rowNum++);

        row.createCell(0).setCellValue("Days");
        row.createCell(1).setCellValue("Triple");
        row.createCell(2).setCellValue("Mean of trigger time");
        row.createCell(3).setCellValue("Trigger times ...");

        for (Long[] days : measuredData) {
            row = sheet.createRow(rowNum++);
            int colNum = 0;
            long sum = 0L;

            // number of days
            final Cell cellDay = row.createCell(colNum++);
            cellDay.setCellValue(rowNum - 1);

            // number of triple
            final Cell cellTriple = row.createCell(colNum++);
            cellTriple.setCellValue(days[0]);

            // mean of trigger time
            final Cell cellMean = row.createCell(colNum++);

            for (int i = 1; i < days.length; i++) {
                sum += days[i];

                final Cell cellValue = row.createCell(colNum++);
                cellValue.setCellValue(days[i]);
            }

            final long mean = sum / days.length;
            cellMean.setCellValue(mean);
        }

        try {
            final File file = new File(FILE_NAME);
            file.setReadable(true, false);
            file.setExecutable(true, false);
            file.setWritable(true, false);
            final FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            ExceptionPrinter.printHistory(e, LOGGER);
        }
        System.out.println("Done");
    }

}
