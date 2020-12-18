package icecream.service.resource;
  
import icecream.service.resource.model.Order;
import icecream.service.resource.model.Flavor;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Path("/orders")
public class OrdersResource {
    final static String orderTable = "orders";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Order[] listOrders() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDB dynamoDB = new DynamoDB(client);
        Table table = dynamoDB.getTable(orderTable);

	ScanSpec scanSpec = new ScanSpec().withProjectionExpression("Id, Customer, Flavor");
	List<Order> orders = new ArrayList<Order>();

        try {
            ItemCollection<ScanOutcome> items = table.scan(scanSpec);
            Iterator<Item> iter = items.iterator();
            while (iter.hasNext()) {
                Item item = iter.next();
                Order newOrder = new Order();
            	newOrder.setId(item.getString("Id"));
            	newOrder.setCustomer(item.getString("Customer"));
            	newOrder.setFlavor(item.getString("Flavor"));
            	orders.add(newOrder);
            }
        }
        catch (Exception e) {
            System.err.println("Unable to scan the table:");
            System.err.println(e.getMessage());
        }

        return orders.stream().toArray(Order[]::new);
    }

    @Path("/{orderId}") @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Order getOrderDetails(@PathParam("orderId") String orderId) {
        System.out.println("orderId: " + orderId);
	AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDB dynamoDB = new DynamoDB(client);
        Table table = dynamoDB.getTable(orderTable);

        GetItemSpec spec = new GetItemSpec().withPrimaryKey("Id", orderId);
	Item item = null;
        try {
            System.out.println("Attempting to read the item...");
            item = table.getItem(spec);
            System.out.println("GetItem succeeded: " + item);
        }
        catch (Exception e) {
            System.err.println("Unable to read item: " + orderId);
            System.err.println(e.getMessage());
        }

        Order newOrder = new Order();
        newOrder.setId(orderId);
        newOrder.setCustomer(item.getString("Customer"));
        newOrder.setFlavor(item.getString("Flavor"));
        return newOrder;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createOrder(final Order newOrder) {
        if (newOrder == null) {
	    System.err.println("newOrder is null!");
            return Response.status(400).entity(new Error("No value for order")).build();
	}

        if (newOrder.getCustomer() == null || newOrder.getFlavor() == null) {
	    System.err.println("Invalid customer or flavor");
            return Response.status(400).entity(new Error("Invalid customer or flavor")).build();
        }

        String customer = newOrder.getCustomer();
        String flavor = newOrder.getFlavor();
	try {
	    AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
            DynamoDB dynamoDB = new DynamoDB(client);
            Table table = dynamoDB.getTable("orders");

            System.out.println("Adding a new item...");
            PutItemOutcome outcome = table
                .putItem(new Item().withPrimaryKey("Id", UUID.randomUUID().toString())
		.withString("Customer", customer)
		.withString("Flavor", flavor));

            System.out.println("PutItem succeeded:\n" + outcome.getPutItemResult());
        }
        catch (Exception e) {
            System.err.println("Unable to add item: " + customer + " " + flavor);
            System.err.println(e.getMessage());
            return Response.status(500).build();
        }

        return Response.status(200).build();
    }

    @Path("/{orderId}") @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateOrder(@PathParam("orderId") String orderId, Order order) {
        System.out.println("update orderId: " + orderId);
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDB dynamoDB = new DynamoDB(client);
        Table table = dynamoDB.getTable(orderTable);

	UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey(new PrimaryKey("Id", orderId))
        	.withUpdateExpression("set Flavor = :val1").withValueMap(new ValueMap().withString(":val1", order.getFlavor()))
        	.withReturnValues(ReturnValue.UPDATED_NEW);

        try {
            System.out.println("Updating the item...");
            UpdateItemOutcome outcome = table.updateItem(updateItemSpec);
            System.out.println("UpdateItem succeeded:\n" + outcome.getItem().toJSONPretty());
        }
        catch (Exception e) {
            System.err.println("Unable to update item: " + orderId);
            System.err.println(e.getMessage());
        }

        return Response
        .status(Response.Status.OK)
        .build();
    }

    @Path("/{orderId}") @DELETE
    public Response deleteOrder(@PathParam("orderId") String orderId) {
        System.out.println("delete orderId: " + orderId);
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDB dynamoDB = new DynamoDB(client);
        Table table = dynamoDB.getTable(orderTable);

	DeleteItemSpec deleteItemSpec = new DeleteItemSpec()
            .withPrimaryKey(new PrimaryKey("Id", orderId));

        try {
            System.out.println("Attempting delete...");
            table.deleteItem(deleteItemSpec);
            System.out.println("DeleteItem succeeded");
        }
        catch (Exception e) {
            System.err.println("Unable to delete item: " + orderId);
            System.err.println(e.getMessage());
        }

	return Response
      	.status(Response.Status.OK)
      	.build();
    }
}
