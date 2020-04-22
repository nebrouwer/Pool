package com.amazonaws.samples;

import java.io.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteItemResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

public class server {
	public static BufferedReader in;
	public static PrintWriter out;
	static Socket s;
	static ServerSocket ss;
	static AmazonDynamoDB db;
	public static DynamoDB dynamoDB;
	static Table table;
	public static Table userTable;
	public static Table playerTable;

	public static String players = "";

	public static void main(String[] args) throws Exception { 

		init();

		connect();
	}

	//Initializes server and database tables
	private static void init() throws Exception {

		ss = new ServerSocket(60117);

		db = AmazonDynamoDBClientBuilder.standard()
				.withRegion(Regions.US_WEST_2)
				.build();

		dynamoDB = new DynamoDB(db);

		createTable("Users", "Username");

		createTable("Players", "Name");
		
		userTable = dynamoDB.getTable("Users");
		playerTable = dynamoDB.getTable("Players");
	}

	//Creates client for connection
	private static void connect() throws Exception {
		while(true) {
			s = ss.accept(); // Accept the incoming request from mobile 

			in = new BufferedReader(new InputStreamReader(s.getInputStream(),"utf-8"));
			out = new PrintWriter(new BufferedOutputStream(s.getOutputStream()));

			// Create a new object for handling this request. 
			client c = new client(s, in, out);

			// Create a new Thread with this object. 
			Thread t = new Thread(c); 
			t.start();
		}
	}

	//Method to create database tables
	public static void createTable(String tableName, String category) throws Exception {
		try {

			// Create a table with a primary hash key named 'Category', which holds a string
			CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
					.withKeySchema(new KeySchemaElement().withAttributeName(category).withKeyType(KeyType.HASH))
					.withAttributeDefinitions(new AttributeDefinition().withAttributeName(category).withAttributeType(ScalarAttributeType.S))
					.withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(3L).withWriteCapacityUnits(3L));

			// Create table if it does not exist yet
			TableUtils.createTableIfNotExists(db, createTableRequest);
			// wait for the table to move into ACTIVE state
			TableUtils.waitUntilActive(db, tableName);

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which means your request made it "
					+ "to AWS, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());

		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with AWS, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
	}

	//Method to add new users to "Users" database table for Pool app
	public static void addUser(String id, String pwd) {
		Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
		item.put("Username", new AttributeValue(id));
		item.put("Password", new AttributeValue(pwd));
		PutItemRequest putItemRequest = new PutItemRequest("Users", item);
		PutItemResult putItemResult = db.putItem(putItemRequest);

		System.out.println("Result: " + putItemResult);
	}

	//Method to add players to "Players" database table for Pool app
	public static void addPlayer(String name, String paid) {
		Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
		item.put("Name", new AttributeValue(name));
		item.put("Paid", new AttributeValue(paid));
		PutItemRequest putItemRequest = new PutItemRequest("Players", item);
		PutItemResult putItemResult = db.putItem(putItemRequest);

		System.out.println("Result: " + putItemResult);
	}

	//Method to remove players from "Players" database table for Pool app
	public static void removePlayer(String player) {
		Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
		item.put("Name", new AttributeValue(player));
		DeleteItemRequest deleteItemRequest = new DeleteItemRequest("Players", item);
		DeleteItemResult deleteItemResult = db.deleteItem(deleteItemRequest);

		System.out.println("Result: " + deleteItemResult);
	}

	//Method to get players from "Players" database table for Pool app
	public static void getPlayers() {
		players = "";
		ScanRequest scanRequest = new ScanRequest()
				.withTableName("Players");
		ScanResult result = db.scan(scanRequest);
		for(Map<String, AttributeValue> item : result.getItems()) {
			String name = item.get("Name").toString();
			int i = name.indexOf(" ");
			int j = name.indexOf(",");
			name = name.substring(i + 1, j);
			String paid = item.get("Paid").toString();
			i = paid.indexOf(" ");
			j = paid.indexOf(",");
			paid = paid.substring(i + 1, j);
			players += name + "@" + paid + ",";
		}
	}

	//Method to validate user credentials on login for Pool app
	public static boolean userValidation(String id, String password) throws Exception {

		boolean checkpass = false;

		String tableName = "Users";
		HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
		Condition condition = new Condition() 
				.withComparisonOperator(ComparisonOperator.EQ)
				.withAttributeValueList(new AttributeValue(id)); 

		Condition condition1 = new Condition()
				.withComparisonOperator(ComparisonOperator.EQ)
				.withAttributeValueList(new AttributeValue(password)); //condition for having the correct password

		scanFilter.put("Username", condition);

		//check if there is any item from the table that have those 2 values
		ScanRequest scanRequest = new ScanRequest(tableName).withScanFilter(scanFilter).addScanFilterEntry("Password",condition1);
		ScanResult scanResult = db.scan(scanRequest); //to scan through the table 

		if(scanResult.getCount() == 1) { //if there is the item that satisfies the condition which mean the user input the right password
			checkpass = true; 
		}
		return checkpass;
	}
}
