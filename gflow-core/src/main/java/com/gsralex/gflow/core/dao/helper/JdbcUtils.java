package com.gsralex.gflow.core.dao.helper;


import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author gsralex
 * @date 2018/2/22
 */
public class JdbcUtils {

    private DataSource dataSource;


    JdbcUtils(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> boolean save(T t) throws SQLException {
        FieldMapper mapper = ModelMapper.getMapper(t.getClass());
        String sql = String.format("insert into `%s`", mapper.getTableName());
        String insertSql = "(";
        String valueSql = " values(";

        FieldValue fieldValue = new FieldValue(t, t.getClass());
        List<Object> objects = new ArrayList<>();
        for (Map.Entry<String, FieldColumn> entry : mapper.getMapper().entrySet()) {
            FieldColumn column = entry.getValue();
            if (!column.isId()) {
                insertSql += String.format("`%s`,", column.getAliasName());
                valueSql += "?,";
                Object value = fieldValue.getValue(entry.getValue().getClass(), entry.getKey());
                objects.add(value);
            }
        }
        insertSql = StringUtils.removeEnd(insertSql, ",");
        insertSql += ")";
        valueSql = StringUtils.removeEnd(valueSql, ",");
        valueSql += ")";
        sql = sql + insertSql + valueSql;
        Object[] objArray = new Object[objects.size()];
        objects.toArray(objArray);
        System.out.println(sql);
        return executeUpdate(sql, objArray) != 0 ? true : false;
    }


    public <T> boolean update(T t) throws SQLException {
        FieldMapper mapper = ModelMapper.getMapper(t.getClass());
        String sql = String.format("update `%s` set ", mapper.getTableName());
        FieldValue fieldValue = new FieldValue(t, t.getClass());
        List<Object> objects = new ArrayList<>();

        for (Map.Entry<String, FieldColumn> entry : mapper.getMapper().entrySet()) {
            FieldColumn column = entry.getValue();
            if (!column.isId()) {
                sql += String.format("`%s`=?,", column.getAliasName());
            }
            Object value = fieldValue.getValue(entry.getValue().getClass(), entry.getKey());
            objects.add(value);
        }
        sql = StringUtils.remove(sql, ",");


        sql += " where 1=1 ";
        for (Map.Entry<String, FieldColumn> entry : mapper.getMapper().entrySet()) {
            FieldColumn column = entry.getValue();
            if (column.isId()) {
                sql += String.format("and %s=? ", column.getAliasName());
            }
            Object value = fieldValue.getValue(entry.getValue().getClass(), entry.getKey());
            objects.add(value);
        }
        Object[] objArray = new Object[objects.size()];
        objects.toArray(objArray);
        return executeUpdate(sql, objArray) != 0 ? true : false;

    }

    public int executeUpdate(String sql, Object[] objects) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = pre(sql, objects);
            return ps.executeUpdate();
        } finally {
            close(ps);
        }
    }

    public <T> List<T> executeQuery(String sql, Object[] objects, Class<T> type) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = pre(sql, objects);
            rs = ps.executeQuery();
            return null;
        } finally {
            closeRs(rs);
            close(ps);
        }
    }

    private <T> List<T> mapperList(ResultSet rs, Class<T> type) throws SQLException, IllegalAccessException, InstantiationException {
        List<T> list = new ArrayList<>();
        FieldMapper fieldMapper = ModelMapper.getMapper(type);
        while (rs.next()) {
            T instance = type.newInstance();
            FieldValue fieldValue = new FieldValue(instance, type);
            list.add(mapper(rs, fieldMapper, fieldValue));
        }
        return list;
    }

    private <T> T mapperEntity(ResultSet rs, Class<T> type) throws SQLException, IllegalAccessException, InstantiationException {
        T instance = type.newInstance();
        FieldMapper fieldMapper = ModelMapper.getMapper(type);
        FieldValue fieldValue = new FieldValue(instance, type);
        if (rs.next()) {
            return mapper(rs, fieldMapper, fieldValue);
        }
        return null;
    }

    private <T> T mapper(ResultSet rs, FieldMapper fieldMapper, FieldValue fieldValue) throws SQLException {
        try {
            for (Map.Entry<String, FieldColumn> item : fieldMapper.getMapper().entrySet()) {
                String name = item.getKey();
                String columnName = item.getValue().getAliasName();
                switch (item.getValue().getTypeName()) {
                    case "java.lang.String": {
                        fieldValue.setValue(String.class, name, rs.getString(columnName));
                        break;
                    }
                    case "int": {
                        fieldValue.setValue(Integer.class, name, rs.getInt(name));
                        break;
                    }
                    case "long": {
                        fieldValue.setValue(Long.class, name, rs.getLong(name));
                        break;
                    }
                    case "double": {
                        fieldValue.setValue(Double.class, name, rs.getDouble(name));
                        break;
                    }
                    case "float": {
                        fieldValue.setValue(Float.class, name, rs.getFloat(name));
                        break;
                    }
                    case "boolean": {
                        fieldValue.setValue(Boolean.class, name, rs.getBoolean(name));
                        break;
                    }
                }
            }
            return (T) fieldValue.getInstance();
        } catch (Throwable e) {
            return null;
        }
    }


    private PreparedStatement pre(String sql, Object[] objects) throws SQLException {
        Connection conn = this.dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        if (objects != null && objects.length != 0) {
            for (int i = 0; i < objects.length; i++) {
                ps.setObject(i + 1, objects[i]);
            }
        }
        return ps;
    }

    private void close(PreparedStatement ps) throws SQLException {
        Connection conn = ps.getConnection();
        if (ps != null) {
            ps.close();
        }
        if (conn != null) {
            conn.close();
        }
    }

    private void closeRs(ResultSet rs) throws SQLException {
        if (rs != null) {
            rs.close();
        }
    }
}
