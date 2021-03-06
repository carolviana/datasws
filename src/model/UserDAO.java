package model;

import java.io.StringReader;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;

public class UserDAO {
	private static Repository repository = new HTTPRepository("http://localhost:7200/repositories/ArtifactAnalysis");
	private static String uriBase = "https://localhost:8090/dataSWS/users/";
	
	public static String getUsersUri() {
		return uriBase;
	}
	
	public static void initializeUsersNameRole() {
		String statusString = 
	            "{\n" + 
	            "	  	\"nameRoles\": [\n" + 
	            "	  		{\"label\": \"author\", \"description\": \"Article's author.\"},\n" + 
	            "	  		{\"label\": \"reviewer\", \"description\": \"Article's reviewer.\"},\n" + 
	            "	  		{\"label\": \"pcchair\", \"description\": \"Conference organizer.\"}\n" + 
	            "	  	]\n" + 
	            "	  }";
		JsonReader reader = Json.createReader(new StringReader(statusString));
        JsonObject dataObject = reader.readObject();
        reader.close();
        
        Model model = new LinkedHashModel();
		ModelBuilder builder = new ModelBuilder();
		ValueFactory factory = SimpleValueFactory.getInstance();
		
        JsonArray roles = dataObject.getJsonArray("nameRoles");
        
        try (RepositoryConnection conn = repository.getConnection()) {
        	for (int i = 0; i < roles.size(); i++) {
        		builder.setNamespace("schema", "http://schema.org/").setNamespace(RDFS.NS).setNamespace(RDF.NS);
        		builder.subject(uriBase + "roles/" + roles.getJsonObject(i).getString("label"))
        			   .add(RDF.TYPE, "schema:Property")
					   .add(RDFS.LABEL, factory.createIRI(uriBase + roles.getJsonObject(i).getString("label")))
					   .add("schema:description", roles.getJsonObject(i).getString("description"))
        			   .add("schema:roleName", factory.createIRI(uriBase + roles.getJsonObject(i).getString("label")));
        		
        		model = builder.defaultGraph().build();
        		conn.add(model);
        	}
		} finally {
			repository.shutDown();
		}
	}
	
	public static String getUsersRoles() {
		StringBuilder resultString = new StringBuilder();
		
		try (RepositoryConnection conn = repository.getConnection()) {
							
			String queryString = "PREFIX schema: <http://schema.org/>\n"+
	  						 "select * where {\n"+
							 "?s a schema:Property .\n"+
							 "}"; 
			TupleQuery query = conn.prepareTupleQuery(queryString);
			TupleQueryResult result = query.evaluate();
					
			while(result.hasNext()){
				resultString.append(result.next().toString() + "\n");	 
			}
						
		} finally {
			repository.shutDown();
		}
		return resultString.toString();
	}
	
	public static String getUsers() {
		StringBuilder resultString = new StringBuilder();
						
		try (RepositoryConnection conn = repository.getConnection()) {
							
			String queryString = "PREFIX schema: <http://schema.org/>\n"+
	  						 "select * where {\n"+
							 "?s a schema:Person .\n"+
							 "}"; 
			TupleQuery query = conn.prepareTupleQuery(queryString);
			TupleQueryResult result = query.evaluate();
					
			while(result.hasNext()){
				resultString.append(result.next().toString() + "\n");	 
			}
						
		} finally {
			repository.shutDown();
		}
		return resultString.toString();
	}
	
	public static String getUserById(int id) {
		StringBuilder resultString = new StringBuilder();
				
		try (RepositoryConnection conn = repository.getConnection()) {
			String queryString = "DESCRIBE <" + uriBase + id + "> ?s ?p ?o";
			GraphQueryResult graphResult = conn.prepareGraphQuery(queryString).evaluate();
			Model resultModel = QueryResults.asModel(graphResult);
			for (Statement statement: resultModel) {
				resultString.append(statement.toString() + "\n");
			}
		} finally {
			repository.shutDown();
		}
		return resultString.toString();
	}

	public static String createUser(JsonObject user) {
		String response = null;

		Model model = new LinkedHashModel();
		ModelBuilder builder = new ModelBuilder();
		ValueFactory factory = SimpleValueFactory.getInstance();
		
		builder.setNamespace("schema", "http://schema.org/").setNamespace(RDF.NS).setNamespace(FOAF.NS);
		builder.subject(uriBase + user.getInt("id"))
			   .add(RDF.TYPE, "schema:Person")
			   .add(RDF.TYPE, FOAF.PERSON)
			   .add("schema:name", user.getString("name"))
			   .add(FOAF.NAME, user.getString("name"))
			   .add("schema:email", user.getString("email"))
			   .add(FOAF.MBOX, user.getString("email"))
			   .add("schema:roleName", factory.createIRI(user.getString("roleName")));
	
		model = builder.defaultGraph().build();
		
		try (RepositoryConnection conn = repository.getConnection()) {
			conn.add(model);
			response = uriBase + user.getInt("id");
		} finally {
			repository.shutDown();
		}
		return response;
	}

	public static boolean deleteUserGraph(String id) {
		boolean flag = false;
		try (RepositoryConnection conn = repository.getConnection()) {
			String queryString = "DESCRIBE <" + uriBase + id + "> ?s ?p ?o";
			System.out.println(queryString);
			GraphQueryResult graphResult = conn.prepareGraphQuery(queryString).evaluate();
			Model resultModel = QueryResults.asModel(graphResult);
			for (Statement statement: resultModel) {
				conn.remove(statement);
			}
			flag = true;
		} finally {
			repository.shutDown();
		} 
		return flag;
	}

	public static void updateUserGraph(String id, JsonObject json) {
		
		try (RepositoryConnection conn = repository.getConnection()) {
			ValueFactory factory = SimpleValueFactory.getInstance();
			IRI subject = factory.createIRI(uriBase+id);
			for(Iterator<Entry<String, JsonValue>> iterator = json.entrySet().iterator(); iterator.hasNext();) {
				String key = iterator.next().getKey();
				String value = json.getString(key);
				
				switch (key.toString()) {
					case "name":{
						Literal object = factory.createLiteral(value);
						
						conn.remove(subject, RDFS.LABEL, null);
						conn.add(subject, RDFS.LABEL, object);
						
						IRI schemaNS = factory.createIRI("http://schema.org/name");
						conn.remove(subject, schemaNS, null);
						conn.add(subject, schemaNS, object);
						
						conn.remove(subject, FOAF.NAME, null);
						conn.add(subject, FOAF.NAME, object);
						break;
					}
					case "email":{
						Literal object = factory.createLiteral(value);
						conn.remove(subject, FOAF.MBOX, null);
						conn.add(subject, FOAF.MBOX, object);
						
						IRI schemaNS = factory.createIRI("http://schema.org/email");
						conn.remove(subject, schemaNS, null);
						conn.add(subject, schemaNS, object);
						break;
					}
					case "roleName":{
						IRI object = factory.createIRI(value);
						IRI schemaNS = factory.createIRI("http://schema.org/roleName");
						conn.remove(subject, schemaNS, null);
						conn.add(subject, schemaNS, object);
						break;
					}
				}
			}
		} finally {
			repository.shutDown();
		} 		
	}

}
