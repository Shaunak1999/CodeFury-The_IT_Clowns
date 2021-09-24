package com.orderprocessing.dao;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.orderprocessing.entity.Order;
import com.orderprocessing.entity.Product;

public interface OrderDao {
	List<Order> getAllOrdersWithoutProductList() throws SQLException;
	List<Order> getOrdersWithoutProductListByCustomerId(int id) throws SQLException;
	List<Order> getQuotesWithoutProductListByCustomerId(int id) throws SQLException;
	int addQuote(Date order_date, int customer_id, String customer_shipping_address,
			float total_order_value, float shipping_cost) throws SQLException;
}