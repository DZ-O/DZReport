package com.dz.eToSQL.generator.domain.DO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 *
 * @TableName report_fields
 */
@TableName(value ="report_fields")
@Data
public class ReportFields implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Integer fieldId;

    /**
     *
     */
    private Integer reportId;

    /**
     *
     */
    private String fieldName;

    /**
     *
     */
    private Object fieldType;

    /**
     *
     */
    private Integer isVisible;

    /**
     *
     */
    private Integer isGrouped;

    /**
     *
     */
    private Object aggregationType;

    /**
     *
     */
    private Date createdAt;

    /**
     *
     */
    private Date updatedAt;

    /**
     *
     */
    private Object customProperties;

    /**
     *
     */
    private Object customFields;

    /**
     *
     */
    private String def1;

    /**
     *
     */
    private String def2;

    /**
     *
     */
    private String def3;

    /**
     *
     */
    private String def4;

    /**
     *
     */
    private String def5;

    /**
     *
     */
    private String def6;

    /**
     *
     */
    private String def7;

    /**
     *
     */
    private String def8;

    /**
     *
     */
    private String def9;

    /**
     *
     */
    private String def10;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
