package com.axibase.tsd.api.model.series.query.transformation.evaluate;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
@NoArgsConstructor
public class Evaluate {
    EvaluationMode mode;
    String[] libs;
    String expression;
    String script;
    int order;

    public Evaluate(String expression) {
        this.expression = expression;
    }
}
