package com.xero.app;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.amazonaws.services.dynamodbv2.util.TableUtils.TableNeverTransitionedToStateException;

public class Storage 
{	
	static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion("us-east-2").build();
	static DynamoDB dynamoDB = new DynamoDB(client);

	public Storage() 
	{
		super();
	}

	// Get a single item from a table based on User Id as a FK
	public String get(String uId, String tableName, String key)
	{		
		Table table = dynamoDB.getTable(tableName);
		String field = "";
		 
        try {

            Item item = table.getItem("id", uId);
            
    		if(item.get(key) != null) {
    			field = item.get(key).toString();
    		}
        }
        catch (Exception e) {
            System.err.println("GetItem failed.");
            System.err.println(e.getMessage());
        }
        return field;
	}
	
	// Check if a value is Null
	public boolean tokenIsNull(String token) {
		if (token != null && !token.isEmpty()) { 
			return false;
		} else {
			return true;
		}
	}

	// tokens from db
	public void clear(String uId)
	{
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("tempToken","");
		map.put("tempTokenSecret","");
		map.put("sessionHandle","");
		map.put("tokenTimestamp","");	
		save(uId,"users", map);
	}

	// Save the user's ID from AWS Cognito
	public void saveUserId(String uId)
	{
		Table table = dynamoDB.getTable("users");
		 
        try {
            Item item = table.getItem("id", uId);
            
            if (null == item)
    	    {
    			try {
    	            Item newItem = new Item().withPrimaryKey("id", uId);
    	            table.putItem(newItem);	            
    	        }
    	        catch (Exception e) {
    	            System.err.println("Create user failed.");
    	            System.err.println(e.getMessage());
    	        }
    		}
        }
        catch (Exception e) {
            System.err.println("GetItem failed.");
            System.err.println(e.getMessage());
        }
	}
	
	//Saving key/value pairs in table with UserID as FK
	public void save(String uId,String tableN ,HashMap<String,String> map)
	{
		Set<Entry<String, String>> set = map.entrySet();
		Iterator<Entry<String, String>> iterator = set.iterator();
		
		Table table = dynamoDB.getTable(tableN);
        try {

            Item item = new Item().withPrimaryKey("id", uId);
            
            while(iterator.hasNext()) {
    			Map.Entry<?, ?> mentry = iterator.next();
    			String key = (String)mentry.getKey();
    			String value = (String)mentry.getValue();
    			
				if (value.length() > 0) {
    				item.withString(key, value);
    			}
    		}
            
            table.putItem(item);
        }
        catch (Exception e) {
            System.err.println("Create items failed.");
            System.err.println(e.getMessage());
        }	
	}
	
	public void createTable(String tableName)
	{	
		// Create a table with a primary hash key named 'id', which holds a string
        CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
             .withKeySchema(new KeySchemaElement().withAttributeName("id").withKeyType(KeyType.HASH))
             .withAttributeDefinitions(new AttributeDefinition().withAttributeName("id").withAttributeType(ScalarAttributeType.S))
             .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));

        // Create table if it does not exist yet
        TableUtils.createTableIfNotExists(client, createTableRequest);
       
        try {
 			TableUtils.waitUntilActive(client, tableName);
 		} catch (TableNeverTransitionedToStateException e1) {
 			e1.printStackTrace();
 		} catch (InterruptedException e1) {
 			e1.printStackTrace();
 		}  
	}
	
	// update a key/value based on User ID FK
	public void update(String uId,String tableName, String key, String value)
	{
		
		Table table = dynamoDB.getTable(tableName);
        try {

        	 Map<String, String> expressionAttributeNames = new HashMap<String, String>();
             expressionAttributeNames.put("#na", key);

             UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("id", uId)
                 .withUpdateExpression("set #na = :val1").withNameMap(new NameMap().with("#na", key))
                 .withValueMap(new ValueMap().withString(":val1", value)).withReturnValues(ReturnValue.ALL_NEW);

             UpdateItemOutcome outcome = table.updateItem(updateItemSpec);
        	
        }
        catch (Exception e) {
            System.err.println("Failed to add new attribute in " + tableName);
            System.err.println(e.getMessage());
        }
	}
	
	// Get List of Invoices
	public List<Map<String, AttributeValue>> getInvoices(String uId,String tableName)
	{			
        try {
        	Map<String, AttributeValue> expressionAttributeValues = 
        		    new HashMap<String, AttributeValue>();
        		expressionAttributeValues.put(":val", new AttributeValue().withS(uId));
        		
        	ScanRequest scanRequest = new ScanRequest()
        		    .withTableName(tableName)
        		    .withFilterExpression("uId = :val")
        		    .withProjectionExpression("id,amount,duedate,email,cname,inv_num")
        		    .withExpressionAttributeValues(expressionAttributeValues);

        		ScanResult result = client.scan(scanRequest);

        	return result.getItems();
        	
        }
        catch (Exception e) {
            System.err.println("GetItem failed.");
            System.err.println(e.getMessage());
        }
      return null;
	}
	
	// Delete all the objects with User ID as FK
	public void delete(String uId,String tableName)
	{		
		List<Map<String, AttributeValue>> objects = getInvoices(uId, tableName);
		Table table = dynamoDB.getTable(tableName);

		for (Map<String, AttributeValue> item : objects) {
			String invId = item.get("id").getS().toString();
		
	        try {
	            DeleteItemSpec deleteItemSpec = new DeleteItemSpec().withPrimaryKey("id", invId);
	            DeleteItemOutcome outcome = table.deleteItem(deleteItemSpec);
	        }
	        catch (Exception e) {
	            System.err.println("Error deleting item in " + tableName);
	            System.err.println(e.getMessage());
	        }
		}
	}
}
