package ru.council.GhostBuster.jasper.generator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignSection;
import net.sf.jasperreports.engine.design.JRDesignStyle;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.query.JsonQueryExecuterFactory;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.engine.xml.JRXmlWriter;
import net.sf.jasperreports.export.*;
import ru.council.GhostBuster.jasper.FillerData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.*;

@Slf4j
public class JasperReportsBaseDocumentGenerator {

    protected final Exporter<ExporterInput, PdfReportConfiguration, PdfExporterConfiguration, OutputStreamExporterOutput> pdfExporter = new JRPdfExporter();
    protected final Exporter<ExporterInput, XlsxReportConfiguration, XlsxExporterConfiguration, OutputStreamExporterOutput> excelExporter = new JRXlsxExporter();
    protected final Exporter<ExporterInput, DocxReportConfiguration, DocxExporterConfiguration, OutputStreamExporterOutput> wordExporter = new JRDocxExporter();
    protected final JasperReportsContext context = new SimpleJasperReportsContext();
    protected final Map<String, Object> parameters = new HashMap<>();
    protected final ObjectMapper objectMapper = new ObjectMapper();
    protected ExporterInput exporterInput;
    protected ExporterOutput exporterOutput;

    protected Locale usedLocale;
    protected int hours;
    protected int minutes;

    {
        wordExporter.setConfiguration(new SimpleDocxReportConfiguration());
        wordExporter.setConfiguration(new SimpleDocxReportConfiguration());
    }

    {
        objectMapper.registerModule(new JavaTimeModule());
        context.setProperty("net.sf.jasperreports.default.font.name", Objects.requireNonNull(JasperReportsDocumentGenerator.class.getResource("/fonts/TimesNewRoman.ttf")).getPath());
        context.setProperty("net.sf.jasperreports.default.pdf.font.name", Objects.requireNonNull(JasperReportsDocumentGenerator.class.getResource("/fonts/TimesNewRoman.ttf")).getPath());
        context.setProperty("net.sf.jasperreports.default.pdf.encoding", "CP1251");
        context.setProperty("net.sf.jasperreports.default.pdf.embedded", "true");
        context.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");

        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        objectMapper.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, true);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
    }

    public JasperReportsBaseDocumentGenerator() {
        this.usedLocale = new Locale.Builder().setLanguage("ru").setRegion("RU").build();
        // todo прогрев Jasper. Самый первый запуск провести тут. Чтобы быстрее потом работал
        hours = 0;
        minutes = 0;
    }

    public JasperReportsBaseDocumentGenerator(int hours, int minutes) {
        this.usedLocale = new Locale.Builder().setLanguage("ru").setRegion("RU").build();
        this.hours = hours;
        this.minutes = minutes;
    }

    public JasperReportsBaseDocumentGenerator(int hours) {
        this.hours = hours;
        this.usedLocale = new Locale.Builder().setLanguage("ru").setRegion("RU").build();
        this.minutes = 0;
    }

    public JasperReportsBaseDocumentGenerator(Locale usedLocale) {
        this.usedLocale = usedLocale;
        hours = 0;
        minutes = 0;
    }

    public JasperReportsBaseDocumentGenerator(Locale usedLocale, int hours) {
        this.usedLocale = usedLocale;
        this.hours = hours;
        this.minutes = 0;
    }

    public JasperReportsBaseDocumentGenerator(Locale usedLocale, int hours, int minutes) {
        this.usedLocale = usedLocale;
        this.hours = hours;
        this.minutes = minutes;
    }

    @SuppressWarnings("unchecked")
    private void exportReport(Exporter current) throws JRException {
        current.setExporterInput(exporterInput);
        current.setExporterOutput(exporterOutput);
        current.exportReport();
    }

    protected byte[] generateDocument(Path pathToParentReport, JRDesignBand inclusionFromGhostBuster, List<JRStyle> styles, ByteArrayInputStream inputStream, DocumentFormatEnum target) throws Exception {
        long wholeTime = System.currentTimeMillis();

        parameters.put(JsonQueryExecuterFactory.JSON_INPUT_STREAM, inputStream);
        parameters.put(JsonQueryExecuterFactory.JSON_LOCALE, new Locale("ru", "RU"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        long time = System.currentTimeMillis();

        JasperDesign design = JRXmlLoader.load(pathToParentReport.toFile());

        for (JRStyle style : styles) {
            design.addStyle(style);
        }

        ((JRDesignSection) design.getDetailSection()).addBand(inclusionFromGhostBuster);

        JRXmlWriter.writeReport(design, "C:\\documents_dev\\test.jrxml", "UTF-8");

        JasperReport jasperReport = compileJasperDesign(design);

        log.info("Spent on getting template {} - {} millis", inclusionFromGhostBuster, (System.currentTimeMillis() - time));
        JasperPrint jasperPrint = JasperFillManager.getInstance(context).fill(jasperReport, parameters);

        exporterInput = new SimpleExporterInput(jasperPrint);
        exporterOutput = new SimpleOutputStreamExporterOutput(outputStream);

        switch (target) {
            case PDF:
                jasperPrint.setJasperReportsContext(context);
                exportReport(pdfExporter);
                log.info("Spent on generating file(whole/PDF): {}", (System.currentTimeMillis() - wholeTime));
                return JasperExportManager.getInstance(context).exportToPdf(jasperPrint);
            case XLSX:
                SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
                configuration.setDetectCellType(true);
                excelExporter.setConfiguration(configuration);
                exportReport(excelExporter);
                log.info("Spent on generating file(whole/XLSX): {}", (System.currentTimeMillis() - wholeTime));
                return outputStream.toByteArray();
            case DOCX:
                exportReport(wordExporter);
                log.info("Spent on generating file(whole/DOCX): {}", (System.currentTimeMillis() - wholeTime));
                return outputStream.toByteArray();
            default:
                throw new RuntimeException("Undefined type of target document: " + target);
        }
    }

    protected byte[] generateDocument(Path pathToReport, @NonNull FillerData report, Object inputStream, DocumentFormatEnum target) throws Exception {
        return generateDocument(
                pathToReport,
                report.getData(),
                report.getStyles(),
                new ByteArrayInputStream(objectMapper.writeValueAsString(inputStream).getBytes()),
                target
        );
    }

    public JasperReport compileJasperDesign(JasperDesign jasperDesign) {
        try {
            return JasperCompileManager.compileReport(jasperDesign);
        } catch (Exception e) {
            throw new JRRuntimeException("Could not compile jasper design", e);
        }
    }

}
