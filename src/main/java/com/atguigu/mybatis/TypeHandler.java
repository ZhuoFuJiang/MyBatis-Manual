package com.atguigu.mybatis;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface TypeHandler<T> {
    void setParameter(PreparedStatement statement, int i, T value) throws SQLException;

    T getResult(ResultSet resultSet, String columnName) throws SQLException;
}
