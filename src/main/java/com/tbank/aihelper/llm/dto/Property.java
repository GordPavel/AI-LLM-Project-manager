package com.tbank.aihelper.llm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Property {
    private String type;
    private String description;

    @JsonProperty("enum")
    private List<String> enumValues;

    public Property(String type, String description) {
        this.type = type;
        this.description = description;
    }
}
