/*
 * Copyright (c) 2008-2020, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.sql;

import com.hazelcast.config.SqlConfig;
import com.hazelcast.internal.util.Preconditions;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Definition of a SQL query.
 * <p>
 * This object is mutable. Properties are read once before the execution is started.
 * Changes to properties do not affect the behavior of already running queries.
 */
public class SqlQuery {
    /** Value for the timeout that is not set. */
    public static final long TIMEOUT_NOT_SET = -1;

    /** Default timeout. */
    public static final long DEFAULT_TIMEOUT = TIMEOUT_NOT_SET;

    /** Default page size. */
    public static final int DEFAULT_CURSOR_BUFFER_SIZE = 4096;

    private String sql;
    private List<Object> parameters;
    private long timeout = DEFAULT_TIMEOUT;
    private int cursorBufferSize = DEFAULT_CURSOR_BUFFER_SIZE;

    public SqlQuery(String sql) {
        setSql(sql);
    }

    /**
     * Copying constructor.
     */
    private SqlQuery(String sql, List<Object> parameters, long timeout, int cursorBufferSize) {
        this.sql = sql;
        this.parameters = parameters;
        this.timeout = timeout;
        this.cursorBufferSize = cursorBufferSize;
    }

    /**
     * Gets the SQL query to be executed.
     *
     * @return SQL query.
     */
    @Nonnull
    public String getSql() {
        return sql;
    }

    /**
     * Sets the SQL query to be executed.
     * <p>
     * The SQL query cannot be null or empty.
     *
     * @param sql SQL query.
     * @return This instance for chaining.
     * @throws NullPointerException If passed SQL query is null
     * @throws IllegalArgumentException If passed SQL query is empty
     */
    @Nonnull
    public SqlQuery setSql(@Nonnull String sql) {
        Preconditions.checkNotNull(sql, "SQL cannot be null");

        if (sql.length() == 0) {
            throw new IllegalArgumentException("SQL cannot be empty");
        }

        this.sql = sql;

        return this;
    }

    /**
     * Gets the query parameters.
     *
     * @return Query parameters.
     */
    @Nonnull
    public List<Object> getParameters() {
        return parameters != null ? parameters : Collections.emptyList();
    }

    /**
     * Sets the query parameters.
     * <p>
     * You may define parameter placeholders in the query with the {@code "?"} character. For every placeholder, a parameter
     * value must be provided.
     * <p>
     * When the method is called, the content of the parameters list is copied. Subsequent changes to the original list don't
     * change the query parameters.
     *
     * @see #addParameter(Object)
     * @see #clearParameters()
     * @param parameters Query parameters.
     * @return This instance for chaining.
     */
    @Nonnull
    public SqlQuery setParameters(List<Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            this.parameters = null;
        } else {
            this.parameters = new ArrayList<>(parameters);
        }

        return this;
    }

    /**
     * Adds a single parameter to the end of the parameters list.
     *
     * @see #setParameters(List)
     * @see #clearParameters()
     * @param parameter Parameter.
     * @return This instance for chaining.
     */
    @Nonnull
    public SqlQuery addParameter(Object parameter) {
        if (parameters == null) {
            parameters = new ArrayList<>(1);
        }

        parameters.add(parameter);

        return this;
    }

    /**
     * Clear query parameters.
     *
     * @see #setParameters(List)
     * @see #addParameter(Object)
     * @return This instance for chaining.
     */
    @Nonnull
    public SqlQuery clearParameters() {
        this.parameters = null;

        return this;
    }

    /**
     * Gets the query timeout in milliseconds.
     *
     * @return Query timeout in milliseconds.
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Sets the query timeout in milliseconds.
     * <p>
     * If the timeout is reached for a running query, it will be cancelled forcefully.
     * <p>
     * Zero value means no timeout. {@value #TIMEOUT_NOT_SET} means that the value from {@link SqlConfig#getQueryTimeout()} will
     * be used. Other negative values are prohibited.
     * <p>
     * Defaults to {@value #TIMEOUT_NOT_SET}.
     *
     * @see SqlConfig#getQueryTimeout()
     * @param timeout Query timeout in milliseconds, {@code 0} for no timeout, {@code -1} to user member's default timeout.
     * @return This instance for chaining.
     */
    @Nonnull
    public SqlQuery setTimeout(long timeout) {
        if (timeout < 0 && timeout != TIMEOUT_NOT_SET) {
            throw new IllegalArgumentException("Timeout should be non-negative or -1: " + timeout);
        }

        this.timeout = timeout;

        return this;
    }

    /**
     * Gets the cursor buffer size (measured in the number of rows).
     *
     * @return Cursor buffer size (measured in the number of rows).
     */
    public int getCursorBufferSize() {
        return cursorBufferSize;
    }

    /**
     * Sets the cursor buffer size (measured in the number of rows).
     * <p>
     * When a query is submitted for execution, a {@link SqlResult} is returned as a result. When rows are ready to be
     * consumed, they are put into an internal buffer of the cursor. This parameter defines the maximum number of rows in
     * that buffer. When the threshold is reached, the backpressure mechanism will slow down the query execution, possibly to a
     * complete halt, to prevent out-of-memory.
     * <p>
     * Only positive values are allowed.
     * <p>
     * The default value is expected to work well for the most workloads. A bigger buffer size may give you a slight performance
     * boost for queries with large result sets at the cost of increased memory consumption.
     * <p>
     * Defaults to {@value #DEFAULT_CURSOR_BUFFER_SIZE}.
     *
     * @see SqlService#query(SqlQuery)
     * @see SqlResult
     * @param cursorBufferSize Cursor buffer size (measured in the number of rows).
     * @return This instance for chaining.
     */
    @Nonnull
    public SqlQuery setCursorBufferSize(int cursorBufferSize) {
        if (cursorBufferSize <= 0) {
            throw new IllegalArgumentException("Cursor buffer size should be positive: " + cursorBufferSize);
        }

        this.cursorBufferSize = cursorBufferSize;

        return this;
    }

    /**
     * Creates a copy of this instance.
     *
     * @return Copy of this instance.
     */
    @Nonnull
    public SqlQuery copy() {
        return new SqlQuery(sql, parameters, timeout, cursorBufferSize);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SqlQuery sqlQuery = (SqlQuery) o;

        return Objects.equals(sql, sqlQuery.sql)
            && Objects.equals(getParameters(), sqlQuery.getParameters())
            && timeout == sqlQuery.timeout
            && cursorBufferSize == sqlQuery.cursorBufferSize;
    }

    @Override
    public int hashCode() {
        int result = sql != null ? sql.hashCode() : 0;

        result = 31 * result + getParameters().hashCode();
        result = 31 * result + (int) (timeout ^ (timeout >>> 32));
        result = 31 * result + cursorBufferSize;

        return result;
    }

    @Override
    public String toString() {
        return "SqlQuery{"
            + "sql=" + sql
            + ", parameters=" + getParameters()
            + ", timeout=" + timeout
            + ", cursorBufferSize=" + cursorBufferSize
            + '}';
    }
}
