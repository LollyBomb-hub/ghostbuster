package ru.council.GhostBuster.jasper.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignTextElement;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import ru.council.GhostBuster.designer.complex.Cell;
import ru.council.GhostBuster.designer.complex.Row;
import ru.council.GhostBuster.designer.styling.Style;
import ru.council.metan.models.MetaJson;
import ru.council.metan.models.Scalar;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class RowUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule());
    }

    private final ExecutionUtils executionUtils;
    private final StyleUtils styleUtils;
    private final int hours;
    private final int minutes;

    public RowUtils(ExecutionUtils executionUtils, StyleUtils styleUtils) {
        this.executionUtils = executionUtils;
        this.styleUtils = styleUtils;
        this.hours = 0;
        this.minutes = 0;
    }

    public RowUtils(ExecutionUtils executionUtils, StyleUtils styleUtils, int hours, int minutes) {
        this.executionUtils = executionUtils;
        this.styleUtils = styleUtils;
        this.hours = hours;
        this.minutes = minutes;
    }

    public RowUtils(ExecutionUtils executionUtils, StyleUtils styleUtils, int hours) {
        this.executionUtils = executionUtils;
        this.styleUtils = styleUtils;
        this.hours = hours;
        this.minutes = 0;
    }

    public static boolean checkIfApproriate(MetaJson elementByPath, Row r, Long indexInArray, List<Integer> countByLevel) {
        ExpressionParser spelExpressionParser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();

        if (elementByPath.isNode()) {
            // elementByPath is Node
            Map<String, MetaJson> node = elementByPath.getNode();
            Map<String, Object> variables = new HashMap<>();
            variables.put("CountByLevel", countByLevel);
            variables.put("RelativeIndex", indexInArray);
            for (String key : node.keySet()) {
                if (node.get(key) != null) {
                    if (node.get(key).isScalar()) {
                        variables.put(key, node.get(key).getScalar().getValue());
                    }
                } else {
                    variables.put(key, null);
                }
            }
            context.setVariables(Collections.unmodifiableMap(variables));
        } else if (elementByPath.isScalar()) {
            Scalar<?> scalar = elementByPath.getScalar();
            context.setVariable("value", scalar == null ? null : scalar.getValue());
            context.setVariable("RelativeIndex", indexInArray);
            context.setVariable("CountByLevel", countByLevel);
        }

        String condition = r.getCondition();
        Expression expression = spelExpressionParser.parseExpression(condition);
        Boolean value = expression.getValue(context, Boolean.class);
        return value != null && value;
    }

    public List<JRDesignTextElement> convertRow(MetaJson elementByPath, List<Integer> width, Row r, int y, Integer rowHeight) {
        List<JRDesignTextElement> result = new ArrayList<>();

        List<Cell> cells = r.getCells();

        int index = 0;

        for (Cell c : cells) {
            if (c.getStyleRef().equals("")) {
                if (!r.getStyleRef().equals("")) {
                    c.setStyleRef(r.getStyleRef());
                }
            }
            result.add(convertCell(elementByPath, width, c, y, rowHeight, index));
            index += c.getColspan();
        }

        return result;
    }

    public JRDesignTextElement convertCell(MetaJson elementByPath, List<Integer> width, Cell cell, int y, Integer rowHeight, int index) {
        Integer colspan = cell.getColspan();
        Object value = cell.getValue();

        int size = width.subList(index, index + colspan).stream().mapToInt(el -> el).sum();
        int skippedSize = width.subList(0, index).stream().mapToInt(el -> el).sum();

        JRDesignTextField result = new JRDesignTextField();

        if (ExecutionUtils.isVariable((String) value)) {
            String nullReplacement = cell.getNullReplacement();
            if (elementByPath != null) {
                String wrappedVariableExpression = (String) value;
                if (wrappedVariableExpression != null) {

                    log.debug("Variable expression: {}", wrappedVariableExpression);
                    log.debug("Attempt to search in: {}", elementByPath);
                    Object resultOfExecution = executionUtils.executeWrappedVariableExpression(wrappedVariableExpression, elementByPath, Object.class);

                    if (resultOfExecution instanceof Scalar) {
                        resultOfExecution = ((Scalar<?>) resultOfExecution).getValue();
                    } else if (resultOfExecution instanceof MetaJson) {
                        if (((MetaJson) resultOfExecution).isScalar()) {
                            resultOfExecution = ((MetaJson) resultOfExecution).getScalar().getValue();
                        } else if (((MetaJson) resultOfExecution).isArray()) {
                            try {
                                resultOfExecution = objectMapper.writeValueAsString(((MetaJson) resultOfExecution).getArray());
                            } catch (JsonProcessingException e) {
                                resultOfExecution = "Unable to write array as string!";
                            }
                        }
                    }
                    value = resultOfExecution == null ? nullReplacement : resultOfExecution;
                    log.debug("Got: {} of type {} and value {}", resultOfExecution, (resultOfExecution == null ? null : resultOfExecution.getClass()), value);
                }
            }
        }

        result.setStyle(styleUtils.getJasperStyleForContentsByName(cell));

        log.debug("Got style defined pattern: {} / cell ref: {}", result.getStyle().getPattern(), cell.getStyleRef());

        JRDesignExpression expression = new JRDesignExpression();

        if (cell.getPattern() == null) {
            log.debug("No pattern specified!");
            switch (cell.getValueType()) {
                case Integer:
                    log.debug("Setting integer value {}", value);
                    expression.setText("java.lang.Long.parseLong(\"" + value + "\")");
                    break;
                case Float:
                    log.debug("Setting floating point value {}", value);
                    expression.setText("java.lang.Double.parseDouble(\"" + value + "\")");
                    break;
                case Text:
                    try {
                        expression.setText(objectMapper.writeValueAsString(value));
                    } catch (JsonProcessingException e) {
                        expression.setText("\"Could not process value\"");
                    }
                    break;
            }
        } else {
            log.debug("Got cell pattern: {}", cell.getPattern());
            switch (cell.getValueType()) {
                case Date:
                    try {
                        if (value != null) {
                            Instant instant;
                            log.debug("Processing value '{}'", value);
                            if (value instanceof Instant) {
                                instant = (Instant) value;
                            } else {
                                try {
                                    Method toInstant = value.getClass().getMethod("toInstant");
                                    Object invoked = toInstant.invoke(value);
                                    if (invoked instanceof Instant) {
                                        instant = (Instant) invoked;
                                    } else {
                                        throw new ClassCastException("Could not cast to Instant!");
                                    }
                                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassCastException e) {
                                    log.debug("Could not use 'toInstant' method!");
                                    instant = objectMapper.readValue(objectMapper.writeValueAsString(value), Instant.class);
                                }
                            }
                            log.debug("Got value: '{}'", instant);
                            String format = DateTimeFormatter.ofPattern(cell.getPattern(), styleUtils.getUsedLocale()).format(instant.atOffset(ZoneOffset.ofHoursMinutes(hours, minutes)));
                            log.debug("Formatted as '{}': '{}'", format, cell.getPattern());
                            expression.setText(objectMapper.writeValueAsString(format));
                        } else {
                            expression.setText("null");
                        }
                    } catch (JsonProcessingException e) {
                        log.warn("Error reading value as Instant {} from value {}", e.getMessage(), value);
                        expression.setText("\"Could not read value as Instant\"");
                    }
                    break;
                case Integer:
                    log.debug("Setting integer value {}", value);
                    expression.setText("java.lang.Long.parseLong(\"" + value + "\")");
                    break;
                case Float:
                    log.debug("Setting floating point value {}", value);
                    expression.setText("java.lang.Double.parseDouble(\"" + value + "\")");
                    break;
                case Text:
                    try {
                        expression.setText(objectMapper.writeValueAsString(value));
                    } catch (JsonProcessingException e) {
                        expression.setText("\"Could not process value\"");
                    }
                    break;
            }
        }

        result.setExpression(expression);

        Style styling = styleUtils.getContentByNameOrDefault(cell.getStyleRef());

        if (rowHeight == 1) {
            result.getPropertiesMap().setProperty("net.sf.jasperreports.export.xls.auto.fit.row", String.valueOf(true));
        }

        result.getPropertiesMap().setProperty("net.sf.jasperreports.export.xls.wrap.text", String.valueOf(styling.getWrap()));
        result.getPropertiesMap().setProperty("net.sf.jasperreports.print.keep.full.text", String.valueOf(styling.getKeep()));

        result.getPropertiesMap().setProperty("net.sf.jasperreports.export.xls.pattern", result.getStyle().getPattern());

        log.debug("Result pattern: {} / style pattern: {}", result.getPattern(), result.getStyle().getPattern());

        result.setX(skippedSize);
        result.setY(y);
        result.setWidth(size);
        result.setHeight(rowHeight);

        Common.setCommonFields(result);

        return result;
    }

}
