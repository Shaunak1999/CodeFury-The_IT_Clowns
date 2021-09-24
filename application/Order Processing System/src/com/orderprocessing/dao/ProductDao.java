package com.orderprocessing.dao;

import java.sql.SQLException;
import java.util.List;

import com.orderprocessing.entity.Product;

public interface ProductDao {
	List<Product> fetchAllProducts() throws SQLException;
}
