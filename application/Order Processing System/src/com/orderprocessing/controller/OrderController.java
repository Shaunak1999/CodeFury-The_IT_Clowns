package com.orderprocessing.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.orderprocessing.entity.Customer;
import com.orderprocessing.entity.Invoice;
import com.orderprocessing.entity.Order;
import com.orderprocessing.entity.Product;
import com.orderprocessing.exception.OrderNotFoundException;
import com.orderprocessing.service.OrderService;
import com.orderprocessing.service.OrderServiceImpl;

@WebServlet("/OrderController")
public class OrderController extends HttpServlet{
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher rd = null;
		HttpSession session = request.getSession();
		OrderService orderService = new OrderServiceImpl();
		
		try {
			String operation = request.getAttribute("operation").toString();
		
			if(operation.equals("emporder")) {
				
				System.out.println("In Order Controller; operation = emporder");
				try {
					List<Order> allOrders = orderService.fetchAllOrders();
					
					request.setAttribute("allOrders", allOrders);
					rd = request.getRequestDispatcher("employeeOrderManagement.jsp");
					rd.forward(request, response);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			else if(operation.equals("custorder")) {
				
				System.out.println("In Order Controller; operation = custorder");
				Customer currentCustomer = (Customer) session.getAttribute("user");
				int customerId = currentCustomer.getCustomerId();
				try {
					List<Order> customerOrders = orderService.fetchOrdersByCustomerId(customerId);
					List<Order> customerQuotes = orderService.fetchQuotesByCustomerId(customerId);
					
					request.setAttribute("customerOrders", customerOrders);
					request.setAttribute("customerQuotes", customerQuotes);
					rd = request.getRequestDispatcher("customerordermanagement.jsp");
					rd.forward(request, response);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			else if(operation.equals("custInvoice")) {
				Invoice invoice = (Invoice) request.getAttribute("invoice");
				int id = invoice.getOrderId();
				try {
					Order order = orderService.getOrderById(id);
					Map<Product,Integer> products = orderService.getProducts(id);
					request.setAttribute("order", order);
					request.setAttribute("products", products);
					rd = request.getRequestDispatcher("invoice.jsp");
					rd.forward(request, response);
				} catch (SQLException | OrderNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if(operation.equals("approveOrder")) {
				int id = Integer.parseInt(request.getParameter("orderId"));
				try {
					Order order = orderService.getOrderById(id);
					// Current date
					Date currentDate = new Date();
					// Difference between current date and order date
					long differenceInDays = TimeUnit.MILLISECONDS.toDays(currentDate.getTime() - order.getOrderDate().getTime());
					System.out.println("Difference in days:"+differenceInDays);
					// If difference is more than 30 days approval fails and the status is set to expired.
					if(differenceInDays > 30) {
						orderService.expireOrder(id);
						request.setAttribute("operation", "custorder");
						rd = request.getRequestDispatcher("OrderController");
						rd.forward(request, response);
					}
					else {
						orderService.approveOrder(id);
						Customer currentCustomer = (Customer) session.getAttribute("user");
						request.setAttribute("order", order);
						request.setAttribute("customer", currentCustomer);
						rd = request.getRequestDispatcher("InvoiceController");
						rd.forward(request, response);
					}
				} catch (SQLException | OrderNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch(NullPointerException e) {
			String operation = request.getParameter("operation");
			
			if(operation.equals("addQuote")) {
				int id = Integer.parseInt(request.getParameter("custId"));
				String address = request.getParameter("custAddress");
				float shippingCost = Float.parseFloat(request.getParameter("shipCost"));
				float orderValue = Float.parseFloat(request.getParameter("totalValue"));
				String products = request.getParameter("products");
				String date = request.getParameter("date");
				Date orderDate;
				try {
					orderDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
					int orderId = orderService.addQuote(orderDate, id, address, orderValue, shippingCost);
					orderService.addOrderHasProducts(products,orderId);
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				System.out.println(id);
				System.out.println(address);
				System.out.println(shippingCost);
				System.out.println(orderValue);
				System.out.println(products);
			}
		}
	}
}
