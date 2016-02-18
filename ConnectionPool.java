package test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Vector;

public class ConnectionPool {
	
	private String jdbcDriver = ""; // 数据库驱动
	private String dbUrl = ""; // 数据 URL
	private String dbUsername = ""; // 数据库用户名
	private String dbPassword = ""; //数据库密码
	
	private String testTable = ""; //测试连接是否可用的测试表名，默认没有测试表
	
	private int initialConnections = 10; //连接池的初始大小
	private int incrementalConnections = 5; //连接池自动增加的大小
	private int maxConnections = 50; //连接池最大的大小
	
	private Vector connections = null; //存放连接池中数据库连接的向量，初始化为null   存放的对象为 pooledConnection 型
	
	/**
	 * 构造函数
	 * @param jdbcDriver
	 * @param dbUrl
	 * @param dbUsername
	 * @param dbPassword
	 */
	public ConnectionPool(String jdbcDriver,String dbUrl,String dbUsername,String dbPassword) {
		this.jdbcDriver = jdbcDriver;
		this.dbUrl = dbUrl;
		this.dbUsername = dbUsername;
		this.dbPassword = dbPassword;
	}

	public String getTestTable() {
		return testTable;
	}

	public void setTestTable(String testTable) {
		this.testTable = testTable;
	}

	public int getInitialConnections() {
		return initialConnections;
	}

	public void setInitialConnections(int initialConnections) {
		this.initialConnections = initialConnections;
	}

	public int getIncrementalConnections() {
		return incrementalConnections;
	}

	public void setIncrementalConnections(int incrementalConnections) {
		this.incrementalConnections = incrementalConnections;
	}

	public int getMaxConnections() {
		return maxConnections;
	}

	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}
	
	
	public synchronized void createPool() throws Exception{
		if(null != connections)
		{
			return;
		}
		
		Driver driver = (Driver) Class.forName(this.jdbcDriver).newInstance();
		DriverManager.registerDriver(driver);
		
		connections = new Vector();
		
		createConnections(this.initialConnections);
		
	}
	
	private void createConnections(int initialConnections) throws SQLException
	{
		for(int x=0;x<this.initialConnections;x++)
		{
			if(this.maxConnections>0 && this.connections.size()>=this.maxConnections)
			{
				break;
			}
		}
		
		try{
			connections.addElement(new PooledConnection(newConnnectin()));
		}catch(SQLException e)
		{
			System.out.println("数据库连接失败"+e.getMessage());
			throw new SQLException();
		}
	}

	private Connection newConnnectin() throws SQLException
	{
		Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
		if(connections.size() ==0)
		{
			DatabaseMetaData metaData = connection.getMetaData();
			int driverMaxConnection = metaData.getMaxConnections();
			if(driverMaxConnection>0 && this.maxConnections>driverMaxConnection)
			{
				this.maxConnections = driverMaxConnection;
			}
		}
		return connection;
	}
	
	
	public synchronized Connection getConnection() throws SQLException
	{
		if(connections == null)
		{
			return null;
		}
		Connection connection = getFreeConnection();
		while(connection == null)
		{
			wait(250);
			connection = getFreeConnection();
		}
		return connection;
	}
	
	private Connection getFreeConnection() throws SQLException
	{
		Connection  connection = findFreeConnection();
		if(connection == null)
		{
			createConnections(incrementalConnections);
			connection = findFreeConnection();
			if(connection == null)
			{
				return null;
			}
		}
		return connection;
	}
	
	private Connection findFreeConnection()
	{
		Connection conn = null;
		PooledConnection pConn=null;
		Enumeration enumerate = connections.elements();
		while(enumerate.hasMoreElements())
		{
			pConn = (PooledConnection) enumerate.nextElement();
			if(!pConn.busy)
			{
				conn = pConn.getConnection();
				pConn.setBusy(true);
				if(!testConnection(conn))
				{
					try {
						conn = newConnnectin();
					} catch (SQLException e) {
						e.printStackTrace();
						return null;
					}
					pConn.setConnection(conn);
				}
				break;
			}
		}
		return conn;
	}
	
	private boolean testConnection(Connection conn)
	{
		try{
			if(testTable.equals(""))
			{
				conn.setAutoCommit(true);
			}else
			{
				Statement stmt = conn.createStatement();
				
				stmt.execute("select count(*) from"+testTable);
			}
		}catch(SQLException e)
		{
			closeConnection(conn);
			return false;
		}
		return true;
	}
	
	private void closeConnection(Connection conn)
	{
		try{
			conn.close();
		}catch(SQLException e)
		{
			System.out.println("关闭数据库连接失败"+e.getMessage());
		}
	}
	
	private void wait(int mSeconds) {
		try
		{
			Thread.sleep(mSeconds);
		}catch(InterruptedException e)
		{
			
		}
	}
	
	public synchronized void closeConnectionPool()
	{
		if(connections == null )
		{
			System.out.println("连接池不存在，无法关闭！");
			return;
		}
		
		PooledConnection pConn = null;
		Enumeration enumerate = connections.elements();
		while(enumerate.hasMoreElements())
		{
			pConn = (PooledConnection) enumerate.nextElement();
			if(pConn.isBusy())
			{
				wait(5000);
			}
			closeConnection(pConn.getConnection());;
			connections.remove(pConn);
		}
		
		connections = null;
	}
	
	public synchronized void refreshConnections()
	{
		
	}
	
	public void returnConnection(Connection connection)
	{
		if(connections == null )
		{
			System.out.println("连接池不存在，无法返回此连接到连接池里！");
			return;
		}
		
		PooledConnection pConn = null;
		Enumeration enumerate = connections.elements();
		while(enumerate.hasMoreElements())
		{
			pConn = (PooledConnection) enumerate.nextElement();
			if(pConn.getConnection() == connection)
			{
				pConn.setBusy(false);
				break;
			}
		}
	}
}

class PooledConnection
{
	Connection connection = null;
	boolean busy = false;
	public PooledConnection(Connection connection) {
		this.connection = connection;
	}
	public Connection getConnection() {
		return connection;
	}
	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	public boolean isBusy() {
		return busy;
	}
	public void setBusy(boolean busy) {
		this.busy = busy;
	}
}
