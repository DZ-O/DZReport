# 文件转换与 SQL 执行框架

该框架提供了一个灵活的文件转换为 SQL 并执行机制，支持多种文件格式和数据库类型。以下是框架的主要组成部分及其功能。
> 目前已实现的文件格式有：Excel转SQL、CSV转SQL。其他文件请等待后续实现
## 主要组件

### 1. 抽象类

- **`com.dz.eToSQL.sql.domain.bean.convert.abs.FillConvertAbstract`**
  - 该抽象类作为文件转换和 SQL 执行的入口，提供前置处理功能。

### 2. 文件转换器

- **`com.dz.eToSQL.sql.domain.bean.convert.ExcelConvert`**
  - 实现了 Excel 文件的转换功能。

### 3. 数据读取与 SQL 生成

- **`com.dz.eToSQL.sql.domain.factory.FileGeneratorFactory`**
  - 该抽象类提供了数据读取工厂类。

- **`com.dz.eToSQL.sql.domain.bean.generator.abs.FileGeneratorAbstact`**
  - 针对不同类型文件的数据读取和 SQL 生成的抽象类。
  
- **`com.dz.eToSQL.sql.domain.bean.generator.ExcelTableGenerator`**
  - 专门用于 Excel 文件的读取和 SQL 生成。

### 4. 数据库策略接口

- **`com.dz.eToSQL.sql.domain.excelInterface.DatabaseTypeStrategy`**
  - 针对不同数据库实现该接口，使用不同的 SQL 生成策略。

### 5. SQL 执行

- 在 **`FillConvertAbstract`** 中实现了 `executeSql` 方法，支持不同数据库的 JDBC 执行。

## 配置

所有实现类均可在配置文件中注册，支持灵活配置和修改。
> 注意: 当前配置都是以文件类型/数据库类型来实例化对象的

### 示例配置
```yml
# 配置工厂类  
file:  
  converter:  
    factory:  
        xls: com.dz.eToSQL.sql.domain.bean.convert.ExcelConvert  
        xlsx: com.dz.eToSQL.sql.domain.bean.convert.ExcelConvert  
        csv: com.dz.eToSQL.sql.domain.bean.convert.CsvConvert  
        pdf: com.dz.eToSQL.sql.domain.bean.convert.PdfConvert  
        doc: com.dz.eToSQL.sql.domain.bean.convert.DocConvert  
        docx: com.dz.eToSQL.sql.domain.bean.convert.DocConvert  
        json: com.dz.eToSQL.sql.domain.bean.convert.JsonConvert  
        sql: com.dz.eToSQL.sql.domain.bean.convert.SqlConvert  
        txt: com.dz.eToSQL.sql.domain.bean.convert.TxtConvert  
        mysql: com.dz.eToSQL.sql.domain.excelInterface.impl.MySQLStrategy  
        postgresql: com.dz.eToSQL.sql.domain.excelInterface.impl.PostgreSQLStrategy  
        oracle: com.dz.eToSQL.sql.domain.excelInterface.impl.OracleStrategy  
        sqlserver: com.dz.eToSQL.sql.domain.excelInterface.impl.SqlServerStrategy  
        sqlite: com.dz.eToSQL.sql.domain.excelInterface.impl.SQLiteStrategy  
    # 配置数据处理类  
        xlsgenerator: com.dz.eToSQL.sql.domain.bean.generator.ExcelTableGenerator  
        xlsxgenerator: com.dz.eToSQL.sql.domain.bean.generator.ExcelTableGenerator  
# JDBC配置  
database:  
  driver:  
    config:  
      mysql:  
        driverClassName: com.mysql.cj.jdbc.Driver  
        urlTemplate: jdbc:mysql://%s:%d/%s?characterEncoding=utf-8&serverTimezone=Asia/Shanghai  
      postgresql:  
        driverClassName: org.postgresql.Driver  
        urlTemplate: jdbc:postgresql://%s:%d/%s  
      oracle:  
        driverClassName: oracle.jdbc.OracleDriver  
        urlTemplate: jdbc:oracle:thin:@%s:%d/%s  
      sqlserver:  
        driverClassName: com.microsoft.sqlserver.jdbc.SQLServerDriver  
        urlTemplate: jdbc:sqlserver://%s:%d;DatabaseName=%s;encrypt=true;trustServerCertificate=true  
      sqlite:  
        driverClassName: org.sqlite.JDBC  
        urlTemplate: jdbc:sqlite:%s
        ```
```	
