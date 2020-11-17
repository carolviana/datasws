package com.example.sws;

import java.io.InputStream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/user")
public class UserResource {

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getUser(){		
		return UserDAO.getUsers();
	}
	
	@GET
	@Path("/{id}/")
	//@Produces("application/rdf+xml")
	@Produces(MediaType.TEXT_PLAIN)
	public String getUser(@PathParam("id") int id) {
		return UserDAO.getUser(id);
	}
	
	@POST
	@Consumes("application/json")
	public Response postUser(InputStream user){
		
        JsonReader reader = Json.createReader(user);
        JsonObject userObject = reader.readObject();
        reader.close();
                
		try {
			String uriString = UserDAO.createUser(userObject);			
			if (!uriString.equals(null))
				return Response.status(201).build();
			else
				return Response.serverError().build();
		}catch (Exception e) {
			e.printStackTrace();
			return Response.status(304).build();
		}
	}
	
	@DELETE
	@Path("{id}/")	
	public Response deleteUser(@PathParam("id")String id) {		
		boolean exist;
		try {
			exist = UserDAO.deleteUserGraph(id);
			if(exist)
				return Response.status(200).build();
			else
				return Response.status(404).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(304).build();
		}			
	}
	
	@PUT
	@Consumes("application/json")	
	@Path("{id}/")
	public Response putUser(@PathParam("id") String id, InputStream data)  {
		
		JsonReader reader = Json.createReader(data);
        JsonObject dataObject = reader.readObject();
        reader.close();
        
		try {			
			UserDAO.updateUserGraph(id , dataObject);
			return Response.status(200).build();
		}	
		catch (Exception e) {
			e.printStackTrace();
			return Response.status(304).build();
		}		
	}
	
	
}
