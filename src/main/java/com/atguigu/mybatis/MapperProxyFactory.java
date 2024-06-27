package com.atguigu.mybatis;

import com.atguigu.zhuofu.User;
import com.mysql.cj.jdbc.Driver;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

public class MapperProxyFactory {

    private static Map<Class, TypeHandler> typeHandlerMap = new HashMap<>();

    static {
        typeHandlerMap.put(String.class, new StringTypeHandler());
        typeHandlerMap.put(Integer.class, new IntegerTypeHandler());

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public static <T> T getMapper(Class<T> mapper) {
        Object proxyInstance = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{mapper}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // JDBC

                // 创建数据库连接
                Connection connection = getConnection();

                Select annotation = method.getAnnotation(Select.class);
                String sql = annotation.value();

                System.out.println(sql);

                Map<String, Object> paramValueMapper = new HashMap<>();
                Parameter[] parameters = method.getParameters();
                for(int i=0; i < parameters.length; i++) {
                    Parameter parameter = parameters[i];
                    String name = parameter.getAnnotation(Param.class).value();
                    paramValueMapper.put(name, args[i]);
                    System.out.println(parameter.getName());
                    // parameter.getName()方法得到的是arg0这种格式
                    paramValueMapper.put(parameter.getName(), args[i]);
                }

                ParameterMappingTokenHandler tokenHandler = new ParameterMappingTokenHandler();
                GenericTokenParser parser = new GenericTokenParser("#{", "}", tokenHandler);
                String parseSql = parser.parse(sql);
                System.out.println(parseSql);
                List<ParameterMapping> parameterMappings = tokenHandler.getParameterMappings();

                // 构造PreparedStatement
                PreparedStatement preparedStatement = connection.prepareStatement(parseSql);
                for(int i=0; i < parameterMappings.size(); i++) {
                    String property = parameterMappings.get(i).getProperty();

                    Object value = paramValueMapper.get(property);
                    Class<?> type = value.getClass();

                    typeHandlerMap.get(type).setParameter(preparedStatement, i+1, value);

//                    preparedStatement.setString(i+1, (String) paramValueMapper.get(property));

                }

                // 执行PreparedStatement
                preparedStatement.execute();

                // 还要判断是否是泛型
                Class resultType = null;
                Type genericReturnType = method.getGenericReturnType();
                if(genericReturnType instanceof Class) {
                    resultType = (Class) genericReturnType;
                } else {
                    Type[] actualTypeArguments = ((ParameterizedType)genericReturnType).getActualTypeArguments();
                    resultType = (Class)actualTypeArguments[0];

                }
                // 依据当前执行的方法封装结果
                Object result = null;
                List<Object> list = new ArrayList<Object>();
                ResultSet resultSet = preparedStatement.getResultSet();

                ResultSetMetaData metaData = resultSet.getMetaData();
                List<String> columnList = new ArrayList<>();
                for(int i=0; i<metaData.getColumnCount(); i++) {
                    columnList.add(metaData.getColumnName(i+1));
                }

                Map<String, Method> setterMethodMapping = new HashMap<>();
                for(Method declaredMethod : resultType.getDeclaredMethods()) {
                    if(declaredMethod.getName().startsWith("set")) {
                        String propertyName = declaredMethod.getName().substring(3);
                        propertyName = propertyName.substring(0, 1).toLowerCase(Locale.ROOT) + propertyName.substring(1);

                        setterMethodMapping.put(propertyName, declaredMethod);
                    }
                }

                while(resultSet.next()) {
                    Object instance = resultType.newInstance();
                    for(int i=0; i<columnList.size(); i++) {
                        String column = columnList.get(i);
                        Method setterMethod = setterMethodMapping.get(column);

                        Class clazz = setterMethod.getParameterTypes()[0];
                        TypeHandler typeHandler = typeHandlerMap.get(clazz);

                        setterMethod.invoke(instance, typeHandler.getResult(resultSet, column));
                    }

//                    User user = new User();
//                    user.setId(resultSet.getInt("id"));
//                    user.setUsername(resultSet.getString("username"));
//                    user.setPassword(resultSet.getInt("password"));
                    list.add(instance);
                }

                System.out.println(list);

                if(method.getReturnType().equals(List.class)) {
                    result = list;
                } else {
                    result = list.get(0);
                }

                connection.close();
                return result;
            }
        });
        return (T)proxyInstance;
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/mybatis-example?characterEncoding=UTF-8", "root", "19961001@qqASD");
    }
}
