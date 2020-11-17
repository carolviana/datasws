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

@Path("/publication")
public class PublicationResource {
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getPublications(){		
		return PublicationDAO.getPublications();
	}
	
	@GET
	@Path("/{id}/")
	//@Produces("application/rdf+xml")
	@Produces(MediaType.TEXT_PLAIN)
	public String getPublication(@PathParam("id") int id) {
		return PublicationDAO.getPublicationById(id);
	}
	
	@POST
	@Consumes("application/json")
	public Response postPublication(InputStream publication){
		
        JsonReader reader = Json.createReader(publication);
        JsonObject publicationObject = reader.readObject();
        reader.close();
                
		try {
			String uriString = PublicationDAO.createPublication(publicationObject);			
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
	public Response deletePublication(@PathParam("id")String id) {		
		boolean exist;
		try {
			exist = PublicationDAO.deletePublicationGraph(id);
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
	public Response putPublication(@PathParam("id") String id, InputStream data)  {
		
		JsonReader reader = Json.createReader(data);
        JsonObject dataObject = reader.readObject();
        reader.close();
        
		try {			
			PublicationDAO.updatePublicationGraph(id , dataObject);
			return Response.status(200).build();
		}	
		catch (Exception e) {
			e.printStackTrace();
			return Response.status(304).build();
		}		
	}
	
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

}
