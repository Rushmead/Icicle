/*
 * MIT License
 *
 * Copyright (c) 2022 IceyLeagons and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.iceyleagons.icicle.database;

import net.iceyleagons.icicle.database.filters.Filter;

import java.sql.Connection;
import java.util.Optional;
import java.util.Set;

/**
 * @author TOTHTOMI
 * @version 1.0.0
 * @since Jan. 26, 2022
 */
public interface DatabaseConnector<T> {

    boolean isSql();

    void buildDatabase(Schema<T> schema);

    void connect() throws IllegalStateException; // TODO replace with DBError

    void disconnect() throws IllegalStateException;  // TODO replace with DBError

    void save(T toSave);

    Set<T> findAll();

    Optional<T> findOne(Filter... filters);

    Set<T> findMany(Filter... filters);

    default Connection getConnection() {
        throw new IllegalStateException("Database is not an SQL database!");
    }
}
