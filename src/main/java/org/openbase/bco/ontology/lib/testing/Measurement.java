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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openbase.bco.dal.remote.unit.ColorableLightRemote;
import org.openbase.bco.dal.remote.unit.Units;
import org.openbase.bco.ontology.lib.trigger.Trigger;
import org.openbase.bco.ontology.lib.trigger.TriggerFactory;
import org.openbase.bco.ontology.lib.trigger.sparql.AskQueryExample;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.schedule.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.ontology.OntologyChangeType.OntologyChange;
import rst.domotic.ontology.TriggerConfigType.TriggerConfig;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.domotic.state.ActivationStateType.ActivationState;
import rst.domotic.state.PowerStateType.PowerState;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author agatting on 17.04.17.
 */
public class Measurement {

    private static final Logger LOGGER = LoggerFactory.getLogger(Measurement.class);
    private static final String FILE_NAME = "src/TriggerMeasurement.xlsx";

    private static final String QUERY_0 =
            "PREFIX NS: <http://www.openbase.org/bco/ontology#> "
                    + "ASK { "
                        + "?obs a NS:Observation . "
                        + "?obs NS:hasUnitId ?unit . "
                        + "?obs NS:hasStateValue NS:ON . "
                        + "?unit a NS:ColorableLight . "
                    + "}";

    public Measurement() throws InterruptedException {

        measureTriggerTime();
    }

    public void measureTriggerTime() throws InterruptedException {
        final Stopwatch stopwatchMeasure = new Stopwatch();
        final Stopwatch stopwatchBlock = new Stopwatch();

        final List<Long> measuredData = new ArrayList<>();

        try {
            final OntologyChange ontologyChange = OntologyChange.newBuilder().addCategory(OntologyChange.Category.UNKNOWN)
                    .addUnitType(UnitType.COLORABLE_LIGHT).addServiceType(ServiceType.POWER_STATE_SERVICE).build();
            final TriggerConfig triggerConfig = TriggerConfig.newBuilder().setLabel("trigger0").setQuery(AskQueryExample.QUERY_0)
                    .setDependingOntologyChange(ontologyChange).build();


            final TriggerFactory triggerFactory = new TriggerFactory();
            final Trigger trigger = triggerFactory.newInstance(triggerConfig);

            trigger.addObserver((Observable<ActivationState.State> source, ActivationState.State data) -> {

                if (stopwatchMeasure.isRunning()) {
                    stopwatchMeasure.stop();

                    measuredData.add(stopwatchMeasure.getTime());

                    System.out.println("stopwatch time: " + stopwatchMeasure.getTime());
                }

                System.out.println(trigger.getTriggerConfig().getLabel() + " is " + data);
            });
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER);
        }

        final ColorableLightRemote colorableLightRemote;
        try {
            colorableLightRemote = (ColorableLightRemote) Units.getUnit("a0f2c9d8-41a6-45c6-9609-5686b6733d4e", true);

            for (int i = 0; i < 100; i++) {
                stopwatchBlock.waitForStart(3000);

                stopwatchMeasure.restart();

                if (colorableLightRemote.getPowerState().getValue().equals(PowerState.State.ON)) {
                    colorableLightRemote.setPowerState(PowerState.State.OFF);
                } else {
                    colorableLightRemote.setPowerState(PowerState.State.ON);
                }
            }

            stopwatchBlock.waitForStart(5000);
            System.out.println("set states finished...");

            System.out.println(measuredData.size());
            System.out.println(measuredData);
            createExcelFile("TriggerSimpleMeasure", measuredData);


        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER);
        }
    }

    private void createExcelFile(final String sheetName, final List<Long> measuredData) {

        // https://www.mkyong.com/java/apache-poi-reading-and-writing-excel-file-in-java/
        final XSSFWorkbook workbook = new XSSFWorkbook();
        final XSSFSheet sheet = workbook.createSheet(sheetName);

        long sum = 0;
        for (final Long value : measuredData) {
            sum += value;
        }
        final long mean = sum / measuredData.size();

        int rowNum = 0;
        System.out.println("Creating excel");

        for (final Long value : measuredData) {
            Row row = sheet.createRow(rowNum++);

            final Cell cellTime = row.createCell(0);
            cellTime.setCellValue("Milliseconds");
            final Cell cellValue = row.createCell(1);
            cellValue.setCellValue(value);
            final Cell cellMean = row.createCell(2);
            cellMean.setCellValue(mean);
        }

        try {
            final FileOutputStream outputStream = new FileOutputStream(FILE_NAME);
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            ExceptionPrinter.printHistory(e, LOGGER);
        }

        System.out.println("Done");
    }

}
