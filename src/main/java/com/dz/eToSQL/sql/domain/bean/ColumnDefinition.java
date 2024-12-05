package com.dz.eToSQL.sql.domain.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.usermodel.CellType;

import java.util.Set;

/**
 * @author daizhen
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColumnDefinition {
    private String name;
    private Set<CellType> types;
    private int maxLength;
    private boolean hasDecimals;

}
