package pgkeys.validation;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import pgkeys.validation.parser.listener.Query;

public class MyProcedureTest {
//    static String testDirectory = "src/test/java/com/example/";
//    private Session session;
//    @BeforeEach
//    void setUp() {
//        session = getSession();
//        dropAll(session);
//        initDB1(session);
//    }
//
    @Test
    public void fromStringTest() {
        String schema = """
FOR (c:City)
EXCLUSIVE MANDATORY
  c.id, z.id
WITHIN (c)-[:IS_PART_OF]->(z:Country).
                """;
        MyProcedure p = new MyProcedure();
        List<Query> queries = p.fromString(schema);
        assertEquals(1, queries.size());

        Query q = queries.get(0);
        assertEquals("c", q.mainVar);
        assertEquals("City", q.mainVarLabel);
        assertEquals("EXCLUSIVE MANDATORY", q.restrictor);
        assertEquals("c.id, z.id", q.restrictorClause);
        assertEquals("(c)-[:IS_PART_OF]->(z:Country)", q.whereClause);

    }
//    @AfterEach
//    void tearDown() {
//        session.close();
//    }
//
//    @Test
//    public void testConvertPgToCypher() {
//        MyProcedure p = new MyProcedure();
//
//        Pair<String, String> inOut = inOutFiles(testDirectory + "testConvertPgToCypher");
//        String inStr = inOut.getLeft();
//        String outStr = inOut.getRight();
//
////        try {
////            String result = p.convertPgToCypher(inStr);
////            assertEquals(outStr.trim(), result.trim());
////        } catch (Exception e) {
////            fail("Exception occurred during test execution: " + e.getMessage());
////        }
//    }
//
//    @Test
//    public void testRunQueryOnNeo4jInstance() {
//        String cypherQuery = "MATCH (n) RETURN COUNT(n) AS nodeCount";
//
//        Result result = session.run(cypherQuery);
//        assertTrue(result.hasNext(), "Query did not return any results.");
//        int nodeCount = result.next().get("nodeCount").asInt();
//        assertTrue(nodeCount >= 0, "Node count should be non-negative.");
//}
//
//    @Test
////    public void testIdentifier() {
////        MyProcedure p = new MyProcedure();
////
////        Pair<String, String> inOut = inOutFiles(testDirectory + "testConvertPgToCypher");
////        String inStr = inOut.getLeft();
////
////        try {
////            String result_query = p.convertPgToCypher(inStr);
////            Result db_result = session.run(result_query);
////            List<List<Object>> resultList = new ArrayList<>();
////            while (db_result.hasNext()) {
////                resultList.add(db_result.next().get("result").asList());
////            }
////            List<Object> resultList2 = resultList.stream().map(list -> list.get(0)).toList();
////
////            List<Object> exp = new ArrayList<>();
////            exp.add(Map.of("xname", "New York", "zsize", 5, "zname", "USA"));
////            exp.add(Map.of("xname", "Toronto", "zsize", 3, "zname", "Canada"));
////            exp.add(Map.of("xname", "Berlin", "zsize", 2, "zname", "Germany"));
////
////            assertListMapsEqual(exp, resultList2);
////        } catch (Exception e) {
////            fail("Exception occurred  " + e.getMessage());
////        }
////    }
//
////    void assertListMapsEqual(List<Object> m1, List<Object> m2) {
////        assertEquals(m1.size(), m2.size(), "List sizes differ");
////        for (int i = 0; i < m1.size(); i++) {
////            var o1 = m1.get(i);
////            var o2 = m2.get(i);
////            if (o1 instanceof Map && o2 instanceof Map) {
////                o1 = (Map<String, Object>) o1;
////                o2 = (Map<String, Object>) o2;
////                assertEquals(((Map<String, Object>) o1).keySet(), ((Map<String, Object>) o2).keySet(), "Keys of maps at index " + i + " are not equal");
////                for (String key : ((Map<String, Object>) o1).keySet()) {
////                    assertEquals(String.valueOf(((Map<String, Object>) o1).get(key)), String.valueOf(((Map<String, Object>) o2).get(key)), "Values for key '" + key + "' at index " + i + " are not equal");
////                }
////            } else {
////                fail("Objects at index " + i + " are not maps");
////            }
////        }
////    }
//
//    Pair<String, String> inOutFiles(String directory) {{
//        try {
//            String inputFilePath = directory + "/in.txt";
//            String outputFilePath = directory + "/out.txt";
//
//            String inputContent = new String(Files.readAllBytes(Paths.get(inputFilePath)), StandardCharsets.UTF_8);
//            String outputContent = new String(Files.readAllBytes(Paths.get(outputFilePath)), StandardCharsets.UTF_8);
//
//            return Pair.of(inputContent, outputContent);
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw new RuntimeException("Error reading files from directory: " + directory, e);
//        }
//    }}
//
//    Session getSession() {
//        String uri = "neo4j://localhost:7687";
//        String username = "neo4j";
//        String password = "neo4jneo4j";
//
//        try {
//            Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));
//            Session session = driver.session();
//            return session;
//        } catch (Exception e) {
//            fail("Exception occurred while connecting to Neo4j: " + e.getMessage());
//            return null;
//        }
//    }
//
//    void initDB1(Session session) {
//        String query = """
//            CREATE (c1:City {name: 'New York'})-[:isPartOf]->(:Country {name: 'USA', size: 5})
//            CREATE (c2:City {name: 'Toronto'})-[:isPartOf]->(:Country {name: 'Canada', size: 3})
//            CREATE (c3:City {name: 'Berlin'})-[:isPartOf]->(:Country {name: 'Germany', size: 2});
//        """;
//        session.run(query);
//    }
//    void dropAll(Session session) {
//        String query = """
//            MATCH (n)
//            DETACH DELETE n;
//                """;
//        session.run(query);
//    }
}
