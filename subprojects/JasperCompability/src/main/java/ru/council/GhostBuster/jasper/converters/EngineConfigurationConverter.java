package ru.council.GhostBuster.jasper.converters;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignTextElement;
import org.springframework.expression.EvaluationContext;
import ru.council.GhostBuster.Configuration;
import ru.council.GhostBuster.data.SubDataSet;
import ru.council.GhostBuster.designer.complex.Header;
import ru.council.GhostBuster.designer.complex.Row;
import ru.council.GhostBuster.designer.complex.Table;
import ru.council.GhostBuster.designer.positioning.Column;
import ru.council.GhostBuster.designer.positioning.Layout;
import ru.council.GhostBuster.designer.styling.Style;
import ru.council.GhostBuster.jasper.FillerData;
import ru.council.GhostBuster.jasper.utils.Common;
import ru.council.GhostBuster.jasper.utils.ExecutionUtils;
import ru.council.GhostBuster.jasper.utils.RowUtils;
import ru.council.GhostBuster.jasper.utils.StyleUtils;
import ru.council.metan.enums.JsonElementType;
import ru.council.metan.models.MetaJson;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class EngineConfigurationConverter {

    private final Long ROOT_INDEX = null;
    private final Integer ROOT_LEVEL = 0;

    private final Configuration configuration;
    private final ExecutionUtils executionUtils;
    private final StyleUtils styleUtils;
    private final RowUtils rowUtils;
    private final Locale locale;

    public EngineConfigurationConverter(Configuration configuration, EvaluationContext context, Locale locale) {
        this.configuration = configuration;
        this.executionUtils = new ExecutionUtils(context);
        this.styleUtils = new StyleUtils(configuration.getStyles(), locale, executionUtils);
        this.rowUtils = new RowUtils(executionUtils, styleUtils);
        this.locale = locale;
    }

    public EngineConfigurationConverter(Configuration configuration, EvaluationContext context, Locale locale, int hours) {
        this.configuration = configuration;
        this.executionUtils = new ExecutionUtils(context);
        this.styleUtils = new StyleUtils(configuration.getStyles(), locale, executionUtils);
        this.rowUtils = new RowUtils(executionUtils, styleUtils, hours);
        this.locale = locale;
    }

    public EngineConfigurationConverter(Configuration configuration, EvaluationContext context, Locale locale, int hours, int minutes) {
        this.configuration = configuration;
        this.executionUtils = new ExecutionUtils(context);
        this.styleUtils = new StyleUtils(configuration.getStyles(), locale, executionUtils);
        this.rowUtils = new RowUtils(executionUtils, styleUtils, hours, minutes);
        this.locale = locale;
    }

    @NonNull
    public FillerData convertToJasperDesign(@NonNull MetaJson inputData) {
        JRDesignBand result = new JRDesignBand();

        MetaJson elementByPath;

        {
            SubDataSet subDataSet = configuration.getSubDataSet();
            String jsonPath = null;
            if (subDataSet != null) {
                jsonPath = subDataSet.getValue();
            }

            if (jsonPath != null)
                elementByPath = inputData.resolve(jsonPath);
            else {
                elementByPath = inputData;
            }
        }

        return getFillerData(result, elementByPath);
    }

    private FillerData getFillerData(JRDesignBand result, MetaJson elementByPath) {
        Layout layout = configuration.getLayout();

        Integer headerHeight = layout.getHeaderHeight();
        Integer rowHeight = layout.getRowHeight();

        int totalWidth = layout.getTotalWidth();

        List<Column> columns = layout.getColumns();

        List<Integer> widths = getWidths(totalWidth, columns);

        return getFillerData(result, elementByPath, headerHeight, rowHeight, widths);
    }

    private FillerData getFillerData(JRDesignBand result, MetaJson elementByPath, Integer headerHeight, Integer rowHeight, List<Integer> widths) {
        Table table = configuration.getTable();
        {
            // Filling headers
            List<Header> headers = table.getLocaleHeaders(locale);

            if (headers == null) {
                throw new IllegalStateException("Not found headers!");
            }

            addHeadersToResult(result, headerHeight, styleUtils, widths, headers);
        }

        {
            // Filling rows
            List<Row> rows = table.getRows();

            int y = headerHeight;

            ArrayList<Integer> byLevel = new ArrayList<>();

            byLevel.add(null);

            y = processGivenSource(result, elementByPath, rows, widths, y, rowHeight, ROOT_INDEX, byLevel, ROOT_LEVEL);

            result.setHeight(y);
        }

        return new FillerData(result, styleUtils.getUsedStyles());
    }

    private void addHeadersToResult(JRDesignBand result, Integer headerHeight, StyleUtils styleUtils, List<Integer> widths, List<Header> headers) {
        int index = 0;

        for (Header header : headers) {
            result.addElement(convertHeader(styleUtils, widths, header, headerHeight, index));
            index += header.getColspan();
        }
    }

    private void addTextElementToBand(@NonNull JRDesignBand result, @NonNull List<JRDesignTextElement> jrDesignTextElements) {
        for (JRDesignTextElement designElement : jrDesignTextElements) {
            result.addElement(designElement);
        }
    }

    private int processGivenSource(JRDesignBand result, MetaJson elementByPath, List<Row> rows, List<Integer> widths, int y, Integer rowHeight, Long indexInArray, ArrayList<Integer> indexesByLevel, int currentLevel) {
        log.debug("Processing given source {}", elementByPath);
        if (elementByPath != null) {
            if (elementByPath.isNode()) {
                currentLevel += 1;
                if (indexesByLevel.size() <= currentLevel) {
                    indexesByLevel.add(1);
                }
                log.debug("LEVEL INFO {}: {} / {}", indexesByLevel, currentLevel, elementByPath);
                y = addAppropriateRows(result, elementByPath, rows, widths, y, rowHeight, indexInArray, indexesByLevel, currentLevel);
            } else if (elementByPath.isArray()) {
                log.debug("Processing array");
                // calling this method for each element
                List<MetaJson> array = elementByPath.getArray();
                log.debug("LEVEL INFO {}: {}", indexesByLevel, currentLevel);
                for (MetaJson el : array) {
                    log.debug("Processing el: {}", el);
                    y = processGivenSource(result, el, rows, widths, y, rowHeight, (long) array.indexOf(el), indexesByLevel, currentLevel);
                }
            } else if (elementByPath.isScalar()) {
                currentLevel += 1;
                if (indexesByLevel.size() <= currentLevel) {
                    indexesByLevel.add(1);
                }
                log.debug("LEVEL INFO {}: {} / {}", indexesByLevel, currentLevel, elementByPath);
                y = addAppropriateRowsFromScalar(result, elementByPath, rows, widths, y, rowHeight, indexInArray, indexesByLevel, currentLevel);
            }
        }
        return y;
    }

    private int addAppropriateRowsFromScalar(JRDesignBand result, MetaJson elementByPath, List<Row> rows, List<Integer> widths, int y, Integer rowHeight, Long indexInArray, ArrayList<Integer> indexesByLevel, int currentLevel) {
        int countAppropriated = 0;
        for (Row r : rows) {
            boolean isAppropriate = RowUtils.checkIfApproriate(elementByPath, r, indexInArray, indexesByLevel);
            // Put its value for ${_value} specified
            if (isAppropriate) {
                countAppropriated++;
                List<JRDesignTextElement> textElements = rowUtils.convertRow(elementByPath, widths, r, y, rowHeight);
                addTextElementToBand(result, textElements);
                y += rowHeight;
            }
        }
        {
            indexesByLevel.set(currentLevel, indexesByLevel.get(currentLevel) + countAppropriated);
        }
        return y;
    }

    private int addAppropriateRows(JRDesignBand result, MetaJson elementByPath, List<Row> rows, List<Integer> widths, int y, Integer rowHeight, Long indexInArray, ArrayList<Integer> copied, int currentLevel) {
        Map<String, MetaJson> node = elementByPath.getNode();
        MetaJson withoutComplexObjects = new MetaJson(JsonElementType.Node);
        MetaJson complexObjects = new MetaJson(JsonElementType.Node);

        for (String key : node.keySet()) {
            if (node.get(key).isScalar()) {
                withoutComplexObjects.put(key, node.get(key));
            } else {
                complexObjects.put(key, node.get(key));
            }
        }
        int countAppropriated = 0;
        for (Row r : rows) {
            boolean isAppropriate = RowUtils.checkIfApproriate(elementByPath, r, indexInArray, copied);
            if (isAppropriate) {
                log.debug("Condition: {}", r.getCondition());
                log.debug("Without complex objects: {}", withoutComplexObjects);
                log.debug("Complex objects: {}", complexObjects);

                countAppropriated++;

                List<JRDesignTextElement> jrDesignTextElements = rowUtils.convertRow(withoutComplexObjects, widths, r, y, rowHeight);
                addTextElementToBand(result, jrDesignTextElements);
                y += rowHeight;
            }
        }

        {
            copied.set(currentLevel, copied.get(currentLevel) + countAppropriated);
        }

        for (String key : complexObjects.getNode().keySet()) {
            MetaJson innerNode = complexObjects.getNode().get(key);
            y = processGivenSource(result, innerNode, rows, widths, y, rowHeight, null, copied, currentLevel);
        }
        return y;
    }

    @NonNull
    private JRDesignTextElement convertHeader(StyleUtils styleUtils, @NonNull List<Integer> width, @NonNull Header header, Integer headerHeight, int index) {
        Integer colspan = header.getColspan();
        int size = width.subList(index, index + colspan).stream().mapToInt(el -> el).sum();
        int skippedSize = width.subList(0, index).stream().mapToInt(el -> el).sum();
        String headerName = header.getHeader();

        JRDesignStaticText result;

        if (ExecutionUtils.isVariable(headerName)) {
            log.debug("Converting dynamic header: {}", headerName);
            result = new JRDesignStaticText();
            String text = executionUtils.executeWrappedVariableExpression(headerName, String.class);
            result.setText(text);
        } else {
            result = new JRDesignStaticText();
            result.setText(headerName);
        }

        Style styling = styleUtils.getHeaderByNameOrDefault(header.getStyleRef());

        if (headerHeight == 1) {
            result.getPropertiesMap().setProperty("net.sf.jasperreports.export.xls.auto.fit.row", String.valueOf(true));
        }

        result.getPropertiesMap().setProperty("net.sf.jasperreports.export.xls.wrap.text", String.valueOf(styling.getWrap()));
        result.getPropertiesMap().setProperty("net.sf.jasperreports.print.keep.full.text", String.valueOf(styling.getKeep()));

        result.setStyle(styleUtils.getJasperStyleForHeadersByName(header));

        result.setX(skippedSize);
        result.setY(0);
        result.setWidth(size);
        result.setHeight(headerHeight);

        Common.setCommonFields(result);

        return result;
    }

    @NonNull
    private List<Integer> getWidths(int total, @NonNull List<Column> columns) {
        List<Integer> list = Arrays.asList(new Integer[columns.size()]);

        List<Column> fixedSizeColumns = columns.stream().filter(Column::isFixed).collect(Collectors.toList());

        total = processFixedSizeColumns(total, columns, list, fixedSizeColumns);

        List<Column> relativeSizeColumns = columns.stream().filter(Column::isRelative).collect(Collectors.toList());

        int modifiedBy = 0;

        modifiedBy = processRelativeSizeColumns(total, columns, list, relativeSizeColumns, modifiedBy);

        total -= modifiedBy;

        List<Column> noSizeColumns = columns.stream().filter(el -> !el.isPresent()).collect(Collectors.toList());

        processColumnsWithNoSpecifiedSize(total, columns, list, noSizeColumns);

        return list;
    }

    private void processColumnsWithNoSpecifiedSize(int total, List<Column> columns, List<Integer> list, List<Column> noSizeColumns) {
        for (Column c : noSizeColumns) {
            int index = columns.indexOf(c);
            list.set(index, total / noSizeColumns.size());
        }
    }

    private int processRelativeSizeColumns(int total, List<Column> columns, List<Integer> list, List<Column> relativeSizeColumns, int modifiedBy) {
        for (Column c : relativeSizeColumns) {
            int widthPercentage = Integer.parseInt(c.getWidth().substring(0, c.getWidth().indexOf("%")));
            int size = (int) (total * (widthPercentage / 100.));
            modifiedBy += size;
            if (modifiedBy >= total)
                throw new IllegalStateException("Sum of percentage defined columns must be less then 100%");
            list.set(columns.indexOf(c), size);
        }
        return modifiedBy;
    }

    private int processFixedSizeColumns(int total, List<Column> columns, List<Integer> list, List<Column> fixedSizeColumns) {
        for (Column c : fixedSizeColumns) {
            int width = Integer.parseInt(c.getWidth());
            list.set(columns.indexOf(c), width);
            total -= width;
        }
        return total;
    }

}
