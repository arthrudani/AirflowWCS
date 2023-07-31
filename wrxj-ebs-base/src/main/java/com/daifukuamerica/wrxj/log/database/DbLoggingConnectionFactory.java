package com.daifukuamerica.wrxj.log.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.util.StringObfuscator;

public class DbLoggingConnectionFactory {
    
    private static interface Singleton {
        final DbLoggingConnectionFactory INSTANCE = new DbLoggingConnectionFactory();
    }
    
    private final DataSource dataSource;

    public DbLoggingConnectionFactory() {
        String vsDBName = Application.getString("database", "SQLServer");
        String vsURL = Application.getString(vsDBName + ".url");
        String vsUserName = Application.getString(vsDBName + ".user");
        String vsPassword = Application.getString(vsDBName + ".password");
        if (vsPassword != null && vsPassword.startsWith("+")) {
          vsPassword = StringObfuscator.decode(vsPassword.substring(1));
        }
        
        ConnectionFactory cf = new DriverManagerConnectionFactory(vsURL, vsUserName, vsPassword);
        PoolableConnectionFactory pcf = new PoolableConnectionFactory(cf, null);
        pcf.setValidationQuery("SELECT 1");
        
        GenericObjectPoolConfig<PoolableConnection> config = new GenericObjectPoolConfig<>();
        config.setTestOnBorrow(true);
        config.setMaxTotal(10);
        GenericObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(pcf, config);
        pcf.setPool(connectionPool);
        
        dataSource = new PoolingDataSource<>(connectionPool);
    }
    
    public static Connection getConnection() throws SQLException {
        return Singleton.INSTANCE.dataSource.getConnection();
    }
}
