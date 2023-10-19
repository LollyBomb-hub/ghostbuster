package ru.council.GhostBuster.jasper.utils;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.design.JRDesignTextElement;
import net.sf.jasperreports.engine.type.ModeEnum;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import ru.council.metan.models.MetaJson;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Common {

    public static void setCommonFields(JRDesignTextElement result) {
        result.setMode(ModeEnum.OPAQUE);
    }

    /**
     * @param json json contents will be set as variables
     * @return SPEL Context
     */
    public static StandardEvaluationContext populateSpelContext(MetaJson json) {
        StandardEvaluationContext context = new StandardEvaluationContext();

        if (json == null) {
            return context;
        } else if (json.isNode()) {
            Map<String, MetaJson> node = json.getNode();
            Map<String, Object> variables = new HashMap<>();
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
        } else if (json.isScalar()) {
            context.setVariable("_value", json.getScalar() == null ? "___" : json.getScalar().getValue());
        }

        return context;
    }
}
