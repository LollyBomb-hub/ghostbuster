package ru.council.GhostBuster.jasper.generator;

import lombok.NonNull;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.EvaluationContext;
import ru.council.GhostBuster.Configuration;
import ru.council.GhostBuster.jasper.FillerData;
import ru.council.GhostBuster.jasper.converters.EngineConfigurationConverter;
import ru.council.metan.models.MetaJson;

import java.nio.file.Path;
import java.util.Locale;

public class JasperReportsDocumentGenerator extends JasperReportsBaseDocumentGenerator {

    public JasperReportsDocumentGenerator() {
        super();
    }

    public JasperReportsDocumentGenerator(Locale usedLocale) {
        super(usedLocale);
    }

    public JasperReportsDocumentGenerator(int hours, int minutes) {
        super(hours, minutes);
    }

    public JasperReportsDocumentGenerator(int hours) {
        super(hours);
    }

    public JasperReportsDocumentGenerator(Locale usedLocale, int hours) {
        super(usedLocale, hours);
    }

    public JasperReportsDocumentGenerator(Locale usedLocale, int hours, int minutes) {
        super(usedLocale, hours, minutes);
    }

    public byte[] processGivenConfiguration(@NonNull Path path, @NonNull EvaluationContext context, @NonNull Configuration configuration, MetaJson sourceForJasper, DocumentFormatEnum documentFormatEnum) throws Exception {
        Path template = Path.of(configuration.getTemplate());
        FillerData jasperDesign = getJasperDesign(context, configuration, sourceForJasper);
        if (template.getParent().isAbsolute()) {
            return generateDocument(
                    template,
                    jasperDesign,
                    sourceForJasper,
                    documentFormatEnum
            );
        } else {
            return generateDocument(
                    path.getParent().resolve(template),
                    jasperDesign,
                    sourceForJasper,
                    documentFormatEnum
            );
        }
    }

    public byte[] processGivenConfiguration(@NonNull EvaluationContext context, @NonNull Path path, MetaJson sourceForJasper, DocumentFormatEnum documentFormatEnum) throws Exception {
        return processGivenConfiguration(
                path,
                context,
                Configuration.from(path),
                sourceForJasper,
                documentFormatEnum
        );
    }

    public byte[] processGivenConfiguration(@NonNull EvaluationContext context, @NonNull String path, MetaJson sourceForJasper, DocumentFormatEnum documentFormatEnum) throws Exception {
        return processGivenConfiguration(
                context,
                Path.of(path),
                sourceForJasper,
                documentFormatEnum
        );
    }

    private @NonNull FillerData getJasperDesign(EvaluationContext context, Configuration configuration, MetaJson metaJson) {
        EngineConfigurationConverter configurationConverter = new EngineConfigurationConverter(configuration, context, usedLocale, hours, minutes);
        return configurationConverter.convertToJasperDesign(metaJson);
    }

}

