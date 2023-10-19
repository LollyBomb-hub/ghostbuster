package ru.council.GhostBuster.jasper.utils;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import ru.council.metan.models.MetaJson;
import ru.council.metan.models.Scalar;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
@Slf4j
public class ExecutionUtils {

    private static final Pattern VARIABLE_FIELD_DETECTOR = Pattern.compile("\\$\\{(.+?)}");
    private final EvaluationContext context;

    public static boolean isVariable(String value) {
        if (value == null) {
            value = "";
        }
        log.debug("Testing for variableness of '{}' with pattern: '{}'", value, VARIABLE_FIELD_DETECTOR.pattern());
        Matcher matcher = VARIABLE_FIELD_DETECTOR.matcher(value);

        return matcher.matches();
    }

    public static String unwrapPath(String value) {
        Matcher matcher = VARIABLE_FIELD_DETECTOR.matcher(value);

        if (matcher.matches()) {
            return matcher.group(1);
        }

        return null;
    }

    public <T> T executeVariableExpression(String expression, Class<T> clazz) {
        SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
        Expression spelExpression = spelExpressionParser.parseExpression(expression);
        return spelExpression.getValue(context, clazz);
    }

    public <T> T executeWrappedVariableExpression(String expression, Class<T> clazz) {
        return executeVariableExpression(unwrapPath(expression), clazz);
    }

    public <T> T executeWrappedVariableExpression(@NonNull String expression, @NonNull MetaJson mapOfVariables, @NonNull Class<T> clazz) {
        if (!mapOfVariables.isNode()) {
            throw new IllegalStateException("Attempt to pass non-node type as variables map!");
        }
        expression = unwrapPath(expression);
        context.getRootObject();
        log.debug("Root object: {} of type: {}", context.getRootObject(), context.getRootObject().getClass());
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext(context.getRootObject().getValue());
        evaluationContext.setTypeLocator(context.getTypeLocator());
        if (context.getBeanResolver() != null) {
            evaluationContext.setBeanResolver(context.getBeanResolver());
        }
        evaluationContext.setPropertyAccessors(context.getPropertyAccessors());
        evaluationContext.setMethodResolvers(context.getMethodResolvers());
        evaluationContext.setConstructorResolvers(context.getConstructorResolvers());
        evaluationContext.setOperatorOverloader(context.getOperatorOverloader());
        evaluationContext.setTypeComparator(context.getTypeComparator());
        evaluationContext.setTypeConverter(context.getTypeConverter());

        Map<String, MetaJson> node = mapOfVariables.getNode();
        for (String key: node.keySet()) {
            MetaJson byKey = node.get(key);
            if (byKey.isScalar()) {
                Scalar<?> scalar = byKey.getScalar();
                if (scalar != null) {
                    evaluationContext.setVariable(key, scalar.getValue());
                } else {
                    evaluationContext.setVariable(key, null);
                }
            }
        }
        SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
        Expression spelExpression = spelExpressionParser.parseExpression(expression);
        return spelExpression.getValue(evaluationContext, clazz);
    }
}
