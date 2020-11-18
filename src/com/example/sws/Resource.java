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

@Path("/")
public class Resource {
	
	@GET
	@Path("{type}/")
	@Produces(MediaType.TEXT_PLAIN)
	public String getAll(@PathParam("type") String type){		
		String result;
		switch (type) {
			case "articles":{
				result = ArticleDAO.getArticles();
				break;
			}
			case "users":{
				result = UserDAO.getUsers();
				break;
			}
			default:{
				result = "null";
			}
		}
		return result;
	}
	
	@GET
	@Path("{type}/{id}/")
	//@Produces("application/rdf+xml")
	@Produces(MediaType.TEXT_PLAIN)
	public String getSingle(@PathParam("type") String type, @PathParam("id") int id) {
		String result = "teste";
		switch (type) {
			case "articles":{
				result = ArticleDAO.getArticleById(id);
				break;
			}
			case "users":{
				result = UserDAO.getUserById(id);
				break;
			}
			default:{
				result = "caminho errado";
			}
		}
		return result;
	}
	
	
	@POST
	@Path("{type}/")
	@Consumes("application/json")
	public Response post(@PathParam("type") String type, InputStream data){
		
        JsonReader reader = Json.createReader(data);
        JsonObject jsonObject = reader.readObject();
        reader.close();
                
		try {
			String result;
			switch (type) {
				case "articles":{
					result = ArticleDAO.createArticle(jsonObject);
					break;
				}
				case "users":{
					result = UserDAO.createUser(jsonObject);
					break;
				}
				default:{
					result = "null";
				}
			}
			
			if (!result.equals(null))
				return Response.status(201).build();
			else
				return Response.serverError().build();
			
		}catch (Exception e) {
			e.printStackTrace();
			return Response.status(304).build();
		}
	}
	
	
	@DELETE
	@Path("{type}/{id}/")	
	public Response deletePublication(@PathParam("type") String type, @PathParam("id")String id) {		
		boolean exist;
		try {
			switch (type) {
				case "articles":{
					exist = ArticleDAO.deleteArticleGraph(id);
					break;
				}
				case "users":{
					exist = UserDAO.deleteUserGraph(id);
					break;
				}
				default:{
					exist = false;
				}
			}
			
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
	@Path("{type}/{id}/")
	public Response putPublication(@PathParam("type") String type, @PathParam("id") String id, InputStream data)  {
		JsonReader reader = Json.createReader(data);
        JsonObject dataObject = reader.readObject();
        reader.close();
        
		try {
			switch (type) {
			case "articles":{
				ArticleDAO.updateArticleGraph(id, dataObject);
				break;
			}
			case "users":{
				UserDAO.updateUserGraph(id, dataObject);
				break;
			}
			//default:{
				//exist = false;
			//}
		}
			return Response.status(200).build();
		}	
		catch (Exception e) {
			e.printStackTrace();
			return Response.status(304).build();
		}		
	}
	/*
	//POST http://www.exemplo.com/articles/1234/allocateRevisers
	@POST
	@Path("{id}/allocateRevisers")
	@Consumes("application/json")
	public Response allocateRevisers(@PathParam("id") String id, InputStream revisers){
		
        JsonReader reader = Json.createReader(revisers);
        JsonObject publicationObject = reader.readObject();
        reader.close();
                
        try {			
			PublicationDAO.addReviewers(id , publicationObject);
			return Response.status(200).build();
		}	
		catch (Exception e) {
			e.printStackTrace();
			return Response.status(304).build();
		}
	}
	
	@PUT
	@Path("{id}/removeRevisers")
	@Consumes("application/json")
	public Response removeRevisers(@PathParam("id") String id, InputStream revisers){
		
        JsonReader reader = Json.createReader(revisers);
        JsonObject publicationObject = reader.readObject();
        reader.close();
                
        try {			
			PublicationDAO.removeReviewers(id , publicationObject);
			return Response.status(200).build();
		}	
		catch (Exception e) {
			e.printStackTrace();
			return Response.status(304).build();
		}
	}
*/
}
