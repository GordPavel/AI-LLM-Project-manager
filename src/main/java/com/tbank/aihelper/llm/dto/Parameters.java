package com.tbank.aihelper.llm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Parameters {
    private String type = "object";

    @JsonProperty("properties")
    private Map<String, Property> properties;

    private List<String> required;

    public Parameters(Map<String, Property> properties, List<String> required) {
        this.properties = properties;
        this.required = required;
    }
}
