package com.ad1.loggenerator.model.fieldsettingsmodels;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusinessGuid {
    
    // whether to include the field
    private boolean include;
    // the optional values to include
    private String[] values;

}
