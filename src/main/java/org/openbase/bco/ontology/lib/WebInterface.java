/**
 * ==================================================================
 *
 * This file is part of org.openbase.bco.ontology.lib.
 *
 * org.openbase.bco.ontology.lib is free software: you can redistribute it and modify
 * it under the terms of the GNU General Public License (Version 3)
 * as published by the Free Software Foundation.
 *
 * org.openbase.bco.ontology.lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with org.openbase.bco.ontology.lib. If not, see <http://www.gnu.org/licenses/>.
 * ==================================================================
 */
package org.openbase.bco.ontology.lib;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by agatting on 12.12.16.
 */
public class WebInterface {
    //CHECKSTYLE.OFF: MultipleStringLiterals
    private static final Logger LOGGER = LoggerFactory.getLogger(Ontology.class);
    private static final String UPDATE_URI = "http://localhost:3030/myAppFuseki/update";
    private static final String DATA_URI = "http://localhost:3030/myAppFuseki/data";
    private static final String UPDATE =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
                    + "INSERT DATA { "
                    + "NS:bla a NS:Observation . "
                    + "} ";

    private static final String QUERY =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
                    + "ASK { "
                    + "NS:o3 a NS:Observation . "
                    + "} ";

    /**
     * WebInterface.
     */
    public WebInterface() {

        // ask query via remote SPARQL
        final Query query = QueryFactory.create(QUERY);
//        HttpAuthenticator authenticator = new SimpleAuthenticator("admin", "admin".toCharArray());
//        QueryEngineHTTP qEngine = QueryExecutionFactory
//                .createServiceRequest("http://localhost:3030/myAppFuseki/query", query, authenticator);
//        System.out.println(qEngine.execAsk());
//
//        try (QueryExecution qe = QueryExecutionFactory.sparqlService("http://localhost:3030/myAppFuseki/sparql",
//                "ASK { ?s a ?type }", authenticator)) {
//            System.out.println(qe.execAsk());
//        }


//            CredentialsProvider credsProvider = new BasicCredentialsProvider();
//            credsProvider.setCredentials(new AuthScope("http://localhost/myAppFuseki/update", 3030),
//              new UsernamePasswordCredentials("admin", "admin"));
//            CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();

//        CredentialsProvider credsProvider = new BasicCredentialsProvider();
//        Credentials unscopedCredentials = new UsernamePasswordCredentials("admin", "admin");
//        credsProvider.setCredentials(AuthScope.ANY, unscopedCredentials);
//        Credentials scopedCredentials = new UsernamePasswordCredentials("admin", "admin");
//        final String host = "http://localhost/myAppFuseki/update";
//        final int port = 3030;
//        AuthScope authscope = new AuthScope(host, port);
//        credsProvider.setCredentials(authscope, scopedCredentials);
//        HttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
//        HttpOp.setDefaultHttpClient(httpclient);

    }

    private OntModel getOntology() {
        // access to fuseki server and download ontology model
        DatasetAccessor datasetAccessor = DatasetAccessorFactory.createHTTP(DATA_URI);
        Model model = datasetAccessor.getModel();
        return ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, model);
    }

    private boolean updateSPARQL(final String updateString) {
        try {
            HttpClient httpclient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(UPDATE_URI);

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("update", updateString));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            HttpResponse httpResponse = httpclient.execute(httpPost);

            return httpResponse.getStatusLine().getStatusCode() == 200;

//            HttpEntity httpEntity = httpResponse.getEntity();
//
//            if (httpEntity != null) {
//                InputStream inputStream = httpEntity.getContent();
//                try {
//                    String output = IOUtils.toString(inputStream, "UTF-8");
//                    System.out.println(output);
//                } finally {
//                    inputStream.close();
//                }
//            }
        } catch (IOException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }

        return false;
    }
    //CHECKSTYLE.ON: MultipleStringLiterals
}
