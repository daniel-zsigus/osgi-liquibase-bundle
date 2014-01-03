package org.everit.osgi.liquibase.bundle.tests;

/*
 * Copyright (c) 2011, Everit Kft.
 *
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

import java.sql.Connection;

import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.dev.testrunner.TestDuringDevelopment;
import org.everit.osgi.liquibase.bundle.OSGiResourceAccessor;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;

@Component(name = "LiquibaseTest", immediate = true)
@Service(value = LiquibaseTestComponent.class)
@Properties({ @Property(name = "eosgi.testEngine", value = "junit4"),
        @Property(name = "eosgi.testId", value = "liquibaseTest") })
public class LiquibaseTestComponent {


    @Reference
    private DataSource dataSource;

    private BundleContext bundleContext;

    @Activate
    public void activate(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void bindDataSource(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void dropAll() {
        Database database = null;
        try {
            Connection connection = dataSource.getConnection();
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            database.setDefaultCatalogName("TEST");
            database.setDefaultSchemaName("public");
            Liquibase liquibase = new Liquibase(null, null, database);
            liquibase.dropAll();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (database != null) {
                try {
                    database.close();
                } catch (DatabaseException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    @Test
    @TestDuringDevelopment
    public void testSimple() {
        Bundle testBundle = bundleContext.getBundle();
        ResourceAccessor resourceAccessor = new OSGiResourceAccessor(testBundle);
        Database database = null;
        try {
            Connection connection = dataSource.getConnection();
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            database.setDefaultCatalogName("TEST");
            database.setDefaultSchemaName("public");
            Liquibase liquibase = new Liquibase("/META-INF/liquibase/changelog.xml", resourceAccessor, database);
            liquibase.update(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (database != null) {
                try {
                    database.close();
                } catch (DatabaseException e) {
                    e.printStackTrace();
                }
            }
        }
        dropAll();
    }
}