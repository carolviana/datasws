package com.example.sws;

import java.util.Iterator;
import java.util.Map.Entry;

import javax.json.JsonArray;
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

public class ArticleDAO {

	/*
	submitted,
	waitingReviewers,
	inReview,
	reviewed,
	accepted,
	acceptedWaitingLastVersion, 
	withoutLastVersion, 
	rejected,
	withdrawn
	*/
	private static Repository repository = new HTTPRepository("http://localhost:7200/repositories/ArtifactAnalysis");
	private static String uriBase = "https://localhost:8090/dataSWS/articles/";
	
	public static String getArticles() {
		StringBuilder resultString = new StringBuilder();
						
		try (RepositoryConnection conn = repository.getConnection()) {
							
			String queryString = "PREFIX schema: <http://schema.org/>\n"+
	  						 "select * where {\n"+
							 "?s a schema:Article .\n"+
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
	
	public static String getArticleById(int id) {
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

	public static String createArticle(JsonObject article) {
		String response = null;
		Model model = new LinkedHashModel();
		ModelBuilder builder = new ModelBuilder();
		ValueFactory factory = SimpleValueFactory.getInstance();
		
		builder.setNamespace("schema", "http://schema.org/").setNamespace(RDF.NS).setNamespace(FOAF.NS);
		builder.subject(uriBase + article.getInt("id"))
			   .add(RDF.TYPE, "schema:Article")
			   .add("schema:title", article.getString("title"))
			   .add("schema:accountablePerson", factory.createIRI(article.getString("accountablePerson")))
			   .add("schema:creativeWorkStatus", article.getString("creativeWorkStatus"));//TODO: gerar automatico
		
		JsonArray authors = article.getJsonArray("authors");
		for (int i = 0; i < authors.size(); i++) {
			builder.add("schema:author", factory.createIRI(authors.getString(i)));
		}
			   	
		model = builder.defaultGraph().build();
		
		try (RepositoryConnection conn = repository.getConnection()) {
			conn.add(model);
			response = uriBase + article.getInt("id");
		} finally {
			repository.shutDown();
		}
		return response;
	}

	public static boolean deleteArticleGraph(String id) {
		boolean flag = false;
		try (RepositoryConnection conn = repository.getConnection()) {
			String queryString = "DESCRIBE <" + uriBase + id + "> ?s ?p ?o";
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

	public static void updateArticleGraph(String id, JsonObject json) {
		try (RepositoryConnection conn = repository.getConnection()) {
			ValueFactory factory = SimpleValueFactory.getInstance();
			IRI subject = factory.createIRI(uriBase+id);
			for(Iterator<Entry<String, JsonValue>> iterator = json.entrySet().iterator(); iterator.hasNext();) {
				String key = iterator.next().getKey();				
				/*
				String value = json.getString(key);
				Literal object = factory.createLiteral(value);
				IRI schemaNS;
				*/
				switch (key.toString()) {
					case "title":{
						Literal object = factory.createLiteral(json.getString("title"));
						IRI schemaNS = factory.createIRI("http://schema.org/title");
						conn.remove(subject, schemaNS, null);
						conn.add(subject, schemaNS, object);
						break;
					}
					case "accountablePerson":{
						IRI object = factory.createIRI(json.getString("accountablePerson"));
						IRI schemaNS = factory.createIRI("http://schema.org/accountablePerson");
						conn.remove(subject, schemaNS, null);
						conn.add(subject, schemaNS, object);
						break;
					}
					case "authors":{
						IRI schemaNS = factory.createIRI("http://schema.org/author");
						conn.remove(subject, schemaNS, null);
						JsonArray authors = json.getJsonArray("authors");
						for (int i = 0; i <= authors.size(); i++) {
							IRI object = factory.createIRI(authors.getString(i));
							conn.add(subject, schemaNS, object);
						}
						break;
					}
					case "creativeWorkStatus":{//TODO transformar Literal em IRI
						Literal object = factory.createLiteral(json.getString(key));
						IRI schemaNS = factory.createIRI("http://schema.org/creativeWorkStatus");
						conn.remove(subject, schemaNS, null);
						conn.add(subject, schemaNS, object);
						break;
					}
					//TODO CriticReview
					/*case "reviewer":{
						schemaNS = factory.createIRI("http://schema.org/CriticReview");
						IRI reviewer = factory.createIRI(userUri+object);
						conn.add(subject, schemaNS, reviewer);
					}*/
				}
			}

		} finally {
			repository.shutDown();
		} 		
	}

	
	public static void addReviewers(String id, JsonObject json) {		
		try (RepositoryConnection conn = repository.getConnection()) {
			ValueFactory factory = SimpleValueFactory.getInstance();
			IRI subject = factory.createIRI(uriBase+id);
			IRI schemaNS = factory.createIRI("http://schema.org/CriticReview");
			JsonArray values = json.getJsonArray("reviewer");
			for (int i = 0; i < values.size(); i++) {
				IRI reviewer = factory.createIRI(values.getString(i));
				conn.add(subject, schemaNS, reviewer);
			}
		} finally {
			repository.shutDown();
		} 		
	}
	
	public static void removeReviewers(String id, JsonObject json) {		
		try (RepositoryConnection conn = repository.getConnection()) {
			ValueFactory factory = SimpleValueFactory.getInstance();
			IRI subject = factory.createIRI(uriBase+id);
			IRI schemaNS = factory.createIRI("http://schema.org/CriticReview");
			JsonArray values = json.getJsonArray("reviewer");
			for (int i = 0; i < values.size(); i++) {
				IRI reviewer = factory.createIRI(values.getString(i));
				conn.remove(subject, schemaNS, reviewer);
			}
		} finally {
			repository.shutDown();
		} 		
	}
}
