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
 * @TableName data_sources
 */
@TableName(value ="data_sources")
@Data
public class DataSources implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Integer dataSourceId;

    /**
     *
     */
    private Integer userId;

    /**
     *
     */
    private String name;

    /**
     *
     */
    private Object type;

    /**
     *
     */
    private String connectionString;

    /**
     *
     */
    private String description;

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
    private Integer isActive;

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
