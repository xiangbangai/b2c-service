package com.huateng.config.db;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@ConfigurationProperties(prefix = "spring.datasource.db2")
@Data
@Component
public class DB2Config {
    private String ifxIFXHOST;
    private String serverName;
    private String portNumber;
    private String databaseName;
    private String user;
    private String password;
    private String ifxNEWCODESET;
    private String ifxIFX_USE_STRENC;
    private String ifxDBDATE;
    private String ifxDB_LOCALE;
    private String ifxCLIENT_LOCALE;
    private String url;
    private int maxLifeTime;
    private int maxPoolSize;
    private int minPoolSize;
    private int borrowConnectionTimeout;

}
