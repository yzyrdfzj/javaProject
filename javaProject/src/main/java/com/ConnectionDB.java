package com;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.Properties;

	/**
	 * 数据库连接类 说明:封装了 无参，有参，存储过程的调用
	 * 本类采取短链接，每次连接操作后都会关闭连接释放资源
	 * @author
	 *
	 */
	public class ConnectionDB {

		/**
		 * 数据库驱动类名称 com.microsoft.sqlserver.jdbc.SQLServerDriver
		 */
		private static String DRIVER;

		/**
		 * 连接字符串 jdbc:sqlserver://localhost:1433; databaseName=Northwind
		 */
		private static String URLSTR;

		/**
		 * 用户名
		 */
		private static String USERNAME;

		/**
		 * 密码
		 */
		private static String USERPASSWORD;

		/**
		 * 创建数据库连接对象
		 */
		private Connection connnection = null;

		/**
		 * 创建PreparedStatement预编译对象 
		 */
		private PreparedStatement preparedStatement = null;

		/**
		 * 创建CallableStatement对象
		 */
		private CallableStatement callableStatement = null;
		//private static Properties p = new Properties();
		/**
		 * 创建结果集对象
		 */

		private ResultSet resultSet = null;

		//这里可通过读取.properties文件来获取数据库连接数据
		static {

//			try {
//				FileInputStream fis;
//					fis = new FileInputStream(							
//							Thread.currentThread().getContextClassLoader().getResource("db.properties").getPath());
//				p.load(fis);
				
				DRIVER = "com.mysql.jdbc.Driver";
				URLSTR = "jdbc:mysql://localhost:3306/world?serverTimezone=GMT%2B8";
				USERNAME = "root";
				USERPASSWORD = "123456";
//				DRIVER = p.getProperty("driver");
//				URLSTR = p.getProperty("url");
//				USERNAME = p.getProperty("user");
//				USERPASSWORD = p.getProperty("pass");
//				Class.forName(DRIVER);
//				
//				//fis.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			} catch (ClassNotFoundException e) {
//				e.printStackTrace();
//			}
		}
		
		public final void setDataBase(String ip, int port, String user, String password, String dbName){
			DRIVER = "com.mysql.jdbc.Driver";
			URLSTR = "jdbc:mysql://"+ ip + ":" + port + "/" + dbName +"?useSSL=false" + "&serverTimezone=GMT%2B8";
			USERNAME = user;
			USERPASSWORD = password;
			try {
				Class.forName(DRIVER);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * 建立数据库连接
		 * @return 数据库连接
		 */
		public Connection getConnection() {
			try {
				// 获取连接 
				connnection = DriverManager.getConnection(URLSTR, USERNAME, USERPASSWORD);
//				connnection.setAutoCommit(false);//设置事务非自动提交
//				1.自动提交事务：每执行一条sql语句，就同步到数据库中。 
//				2.手动提交事务：执行一系列的sql语句后一起同步到数据库中。
			} catch (SQLException e) {
				System.err.println(e.getMessage());
			}
			return connnection;
		}

		/**
		 * insert update delete SQL语句的执行的统一方法
		 * @param sql SQL语句
		 * @param params 参数数组，若没有参数则为null
		 * @return 受影响的行数
		 */
		public int executeUpdate(String sql, Object[] params) {
			// 受影响的行数
			int affectedLine = 0;
			try {
				// 获得连接
				connnection = this.getConnection();
				// 调用SQL 获取预处理对象
				preparedStatement = connnection.prepareStatement(sql);
				// 参数赋值
				if (params != null) {
					for (int i = 0; i < params.length; i++) {
						preparedStatement.setObject(i + 1, params[i]);
					}
				}
				// 执行
				affectedLine = preparedStatement.executeUpdate();

			} catch (SQLException e) {
				System.err.println(e.getMessage());
			} finally {
				// 释放资源
				closeAll();
			}
			return affectedLine;
		}

		/**
		 * SQL 查询将查询结果直接放入ResultSet中
		 * @param sql SQL语句
		 * @param params 参数数组，若没有参数则为null
		 * @return 结果集
		 */
		private ResultSet executeQueryRS(String sql, Object[] params) {
			try {
				// 获得连接
				connnection = this.getConnection();
				// 调用SQL 获取预处理对象
				preparedStatement = connnection.prepareStatement(sql);
				// 参数赋值 即给?的地方赋值如 String sql = "delete from video_info where VideoStartTime = ? order by VideoStartTime limit ?";
				if (params != null) {
					for (int i = 0; i < params.length; i++) {
						preparedStatement.setObject(i + 1, params[i]);
					}
				}
				// 执行
				resultSet = preparedStatement.executeQuery();

			} catch (SQLException e) {
				System.err.println(e.getMessage());
			}
			return resultSet;
		}

		/**
		 * SQL 查询将查询结果：一行一列
		 * @param sql SQL语句
		 * @param params 参数数组，若没有参数则为null
		 * @return 结果集
		 */
		public Object executeQuerySingle(String sql, Object[] params) {
			Object object = null;
			try {
				// 获得连接
				connnection = this.getConnection();
				// 调用SQL 获取预处理对象
				preparedStatement = connnection.prepareStatement(sql);
				// 参数赋值
				if (params != null) {
					for (int i = 0; i < params.length; i++) {
						preparedStatement.setObject(i + 1, params[i]);
					}
				}
				// 执行
				resultSet = preparedStatement.executeQuery();
				if (resultSet.next()) {
					object = resultSet.getObject(1);
				}
			} catch (SQLException e) {
				System.err.println(e.getMessage());
			} finally {
				closeAll();
			}
			return object;
		}

		/**
		 * 获取结果集，并将结果放在List中
		 * 
		 * @param sql SQL语句
		 * @return List 结果集
		 */
		public List<Object> excuteQuery(String sql, Object[] params) {
			// 执行SQL获得结果集
			ResultSet rs = executeQueryRS(sql, params);
			// 创建ResultSetMetaData对象
			ResultSetMetaData rsmd = null;
			// 结果集列数
			int columnCount = 0;
			try {
				rsmd = rs.getMetaData();
				// 获得结果集列数
				columnCount = rsmd.getColumnCount();
			} catch (SQLException e1) {
				System.err.println(e1.getMessage());
			}
			// 创建List
			List<Object> list = new ArrayList<Object>();
			try {
				// 将ResultSet的结果保存到List中
				while (rs.next()) {
					Map<String, Object> map = new HashMap<String, Object>();
					for (int i = 1; i <= columnCount; i++) {
						map.put(rsmd.getColumnLabel(i), rs.getObject(i));
					}
					list.add(map);
				}
			} catch (SQLException e) {
				System.err.println(e.getMessage());
			} finally {
				// 关闭所有资源
				closeAll();
			}

			return list;
		}
		
		/**
		 * 获取结果集，并将结果放在List中
		 * 
		 * @param sql SQL语句
		 * @return List 结果集 输出结果为list<String> 与excuteQuery()相比，List集合中的数据由map类型改为String类型
		 */
		public List<String> excuteQueryListString(String sql, Object[] params) {
			// 执行SQL获得结果集
			ResultSet rs = executeQueryRS(sql, params);
			// 创建ResultSetMetaData对象
			ResultSetMetaData rsmd = null;
			// 结果集列数
			int columnCount = 0;
			try {
				rsmd = rs.getMetaData();
				// 获得结果集列数
				columnCount = rsmd.getColumnCount();
			} catch (SQLException e1) {
				System.err.println(e1.getMessage());
			}
			// 创建List
			List<String> list = new ArrayList<String>();
			try {
				// 将ResultSet的结果保存到List中
				while (rs.next()) {
					String str ="";
					for (int i = 1; i <= columnCount; i++) {
						str = str + rs.getString(i) + ",";
					}
					list.add(str);
				}
			} catch (SQLException e) {
				System.err.println(e.getMessage());
			} finally {
				// 关闭所有资源
				closeAll();
			}

			return list;
		}

		/**
		 * 存储过程带有一个输出参数的方法
		 * 
		 * @param sql 存储过程语句
		 * @param params 参数数组
		 * @param outParamPos 输出参数位置
		 * @param SqlType 输出参数类型
		 * @return 输出参数的值
		 */
		public Object excuteQuery(String sql, Object[] params, int outParamPos, int SqlType) {
			Object object = null;
			connnection = this.getConnection();
			try {
				// 调用存储过程
				callableStatement = connnection.prepareCall(sql);
				// 给参数赋值
				if (params != null) {
					for (int i = 0; i < params.length; i++) {
						callableStatement.setObject(i + 1, params[i]);
					}
				}

				// 注册输出参数
				callableStatement.registerOutParameter(outParamPos, SqlType);
				// 执行
				callableStatement.execute();
				// 得到输出参数
				object = callableStatement.getObject(outParamPos);
			} catch (SQLException e) {
				System.err.println(e.getMessage());
			} finally {
				// 释放资源
				closeAll();
			}

			return object;
		}

		/**
		 * 关闭所有资源
		 */
		private void closeAll() {
			// 关闭结果集对象
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					System.err.println(e.getMessage());
				}
			}

			// 关闭PreparedStatement对象
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					System.err.println(e.getMessage());
				}
			}

			// 关闭CallableStatement 对象
			if (callableStatement != null) {
				try {
					callableStatement.close();
				} catch (SQLException e) {
					System.err.println(e.getMessage());
				}
			}

			// 关闭Connection 对象
			if (connnection != null) {
				try {
					connnection.close();
				} catch (SQLException e) {
					System.err.println(e.getMessage());
				}
			}
		}
}
