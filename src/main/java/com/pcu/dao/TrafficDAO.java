package com.pcu.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class TrafficDAO {
	
	public static ArrayList<String> getCity() {

		ArrayList<String> city = new ArrayList<>();
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String url = "jdbc:oracle:thin:@localhost:1521:orcl";
		String user = "c##mmoottss";
		String pass = "wltnghks";	
		String sql2 = "select city from ELEVISOR";

		city.clear();

		try{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(url, user, pass);	
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql2);
			while(rs.next()){
				city.add(rs.getString("city"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(rs != null) 	 rs.close();
				if(stmt != null) stmt.close();
				if(conn != null) conn.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		return city;

	}
	
	public static void getData() {
		
		ArrayList<String> column = new ArrayList<>();
		ArrayList<ArrayList> ary = new ArrayList<>();
		ArrayList<String> city = new ArrayList<>();
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		String url = "jdbc:oracle:thin:@localhost:1521:orcl";
		String user = "c##mmoottss";
		String pass = "wltnghks";	
		String sql = "select column_name from user_tab_columns where table_name = upper('ELEVISOR')";
		String sql2 = "select * from ELEVISOR";
		column.clear();
		ary.clear();
		city.clear();
			try{
				Class.forName("oracle.jdbc.driver.OracleDriver");
				conn = DriverManager.getConnection(url, user, pass);	
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);
				while(rs.next()){
					column.add(rs.getString("column_name"));
				}			
				rs2 = stmt.executeQuery(sql2);
				while(rs2.next()){
					ArrayList<Double> sary = new ArrayList<>();
					city.add(rs2.getString("city"));
					sary.add(rs2.getDouble("commute"));
					sary.add(rs2.getDouble("toschool"));
					sary.add(rs2.getDouble("task"));
					sary.add(rs2.getDouble("shopping"));
					sary.add(rs2.getDouble("hobby"));
					sary.add(rs2.getDouble("academy"));
					sary.add(rs2.getDouble("etc"));
					ary.add(sary);
				}
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				try{
					if(rs != null) 	 rs.close();
					if(rs2 != null)  rs2.close();
					if(stmt != null) stmt.close();
					if(conn != null) conn.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
	}
}
