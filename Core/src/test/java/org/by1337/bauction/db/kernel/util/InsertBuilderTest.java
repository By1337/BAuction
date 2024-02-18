package org.by1337.bauction.db.kernel.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class InsertBuilderTest  {
    @Test
    public void build_insertStatementWithSingleValue() {
        InsertBuilder insertBuilder = new InsertBuilder("test_table");
        insertBuilder.add("column1", "value1");

        String actualSql = insertBuilder.build();
        String expectedSql = "INSERT INTO test_table (column1) VALUES('value1')";

        assertEquals(expectedSql, actualSql);
    }

    @Test
    public void build_insertStatementWithMultipleValues() {
        InsertBuilder insertBuilder = new InsertBuilder("test_table");
        insertBuilder.add("column1", "value1");
        insertBuilder.add("column2", 42);

        String actualSql = insertBuilder.build();
        String expectedSql = "INSERT INTO test_table (column1,column2) VALUES('value1',42)";

        assertEquals(expectedSql, actualSql);
    }

    @Test
    public void build_insertStatementWithEscapedQuotes() {
        InsertBuilder insertBuilder = new InsertBuilder("test_table");
        insertBuilder.add("column1", "value'1");
        insertBuilder.add("column2", "value\"2");

        String actualSql = insertBuilder.build();
        String expectedSql = "INSERT INTO test_table (column1,column2) VALUES(\"value'1\",'value\"2')";

        assertEquals(expectedSql, actualSql);
    }

    @Test
    public void build_throwsExceptionWhenNoValuesAdded() {
        InsertBuilder insertBuilder = new InsertBuilder("test_table");

        assertThrows(IllegalStateException.class, insertBuilder::build);
    }
}