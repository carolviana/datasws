package model;

import java.util.Iterator;
import java.util.Map.Entry;

import javax.json.JsonObject;
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
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;

public class ConferenceDAO {

	private static Repository repository = new HTTPRepository("http://localhost:7200/repositories/ArtifactAnalysis");
	private static String uriBase = "https://localhost:8090/dataSWS/conferences/";
	
	public static String getConferences() {
		StringBuilder resultString = new StringBuilder();
						
		try (RepositoryConnection conn = repository.getConnection()) {
							
			String queryString = "PREFIX schema: <http://schema.org/>\n"+
	  						 "select * where {\n"+
							 "?s a schema:Event .\n"+
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
	
	
	public static String getConferenceById(int id) {
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
	

	public static String createConference(JsonObject conference) {
		String response = null;

		Model model = new LinkedHashModel();
		ModelBuilder builder = new ModelBuilder();
		ValueFactory factory = SimpleValueFactory.getInstance();
		
		builder.setNamespace("schema", "http://schema.org/").setNamespace(RDF.NS).setNamespace(FOAF.NS);
		builder.subject(uriBase + conference.getInt("id"))
			   .add(RDF.TYPE, "schema:Event")
			   .add("schema:title", conference.getString("title"))
			   .add("schema:startDate", conference.getString("startDate"))
			   .add("schema:endDate", conference.getString("endDate"))
			   .add("schema:organizer", factory.createIRI(conference.getString("pcchair")));
	
		model = builder.defaultGraph().build();
		
		try (RepositoryConnection conn = repository.getConnection()) {
			conn.add(model);
			response = uriBase + conference.getInt("id");
		} finally {
			repository.shutDown();
		}
		return response;
	}

	
	public static boolean deleteConferenceGraph(String id) {
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


	public static void updateConferenceGraph(String id, JsonObject json) {
		try (RepositoryConnection conn = repository.getConnection()) {
			ValueFactory factory = SimpleValueFactory.getInstance();
			IRI subject = factory.createIRI(uriBase+id);
			for(Iterator<Entry<String, JsonValue>> iterator = json.entrySet().iterator(); iterator.hasNext();) {
				String key = iterator.next().getKey();
				String value = json.getString(key);
				
				switch (key.toString()) {
					case "title":{
						Literal object = factory.createLiteral(value);
						IRI schemaNS = factory.createIRI("http://schema.org/title");
						conn.remove(subject, schemaNS, null);
						conn.add(subject, schemaNS, object);
						break;
					}
					case "startDate":{
						Literal object = factory.createLiteral(value);
						IRI schemaNS = factory.createIRI("http://schema.org/startDate");
						conn.remove(subject, schemaNS, null);
						conn.add(subject, schemaNS, object);
						break;
					}
					case "endDate":{
						Literal object = factory.createLiteral(value);
						IRI schemaNS = factory.createIRI("http://schema.org/endDate");
						conn.remove(subject, schemaNS, null);
						conn.add(subject, schemaNS, object);
						break;
					}
					case "pcchair": {
						IRI object = factory.createIRI(value);
						IRI schemaNS = factory.createIRI("http://schema.org/organizer");
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
