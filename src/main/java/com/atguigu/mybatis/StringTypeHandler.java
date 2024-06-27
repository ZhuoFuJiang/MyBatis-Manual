package com.atguigu.mybatis;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class StringTypeHandler implements TypeHandler<String> {

    @Override
    public void setParameter(PreparedStatement statement, int i, String value) throws SQLException {
        statement.setString(i, value);
    }

    @Override
    public String getResult(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getString(columnName);
    }
}
