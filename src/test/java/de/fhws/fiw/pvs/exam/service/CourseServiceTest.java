package de.fhws.fiw.pvs.exam.service;

import com.owlike.genson.Genson;
import de.fhws.fiw.pvs.exam.database.DAOFactory;
import de.fhws.fiw.pvs.exam.database.dao.CourseDAO;
import de.fhws.fiw.pvs.exam.database.dao.EventDAO;
import okhttp3.*;
import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import de.fhws.fiw.pvs.exam.resources.Course;
import de.fhws.fiw.pvs.exam.resources.Event;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/***
 * By Luca Lanzo
 */


@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class CourseServiceTest {
    private final static MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final CourseDAO courseDatabase = DAOFactory.createCourseDAO();
    private static final EventDAO eventDatabase = DAOFactory.createEventDAO();
    private static String BASE_URL = "";
    private Course testCourse;
    private Event testEvent;
    private Genson builder;
    private OkHttpClient client;
    private String adminCreds;
    private String studentCreds;


    @BeforeAll
    public void setUp() {
        builder = new Genson();
        client = new OkHttpClient();
        adminCreds = "Basic " + Base64.encodeBase64String("admin:admin".getBytes());
        studentCreds = "Basic " + Base64.encodeBase64String("admin:admin".getBytes());

        // Get the BASE_URL from the dispatcher
        Response response = null;
        try {
            Request request = new Request.Builder()
                    .url("http://localhost:8080/api/softskills")
                    .get()
                    .header("Authorization", adminCreds)
                    .build();

            response = client.newCall(request).execute();
        } catch (NullPointerException e) {
            fail("No response body has been sent by the server");
        } catch (IOException e) {
            fail("Call to the Server couldn't be made. Is the server not running?");
        }

        List<String> allLinkHeaders = response.headers("Link");
        String eventLink = "";
        for (String link : allLinkHeaders) {
            if (link.contains("course")) eventLink = link;
        }
        BASE_URL = eventLink.substring(eventLink.indexOf("<") + 1, eventLink.indexOf(">"));
    }


    // POST a course
    @Test
    @Order(1)
    public void createCourseTest() {
        try {
            testCourse = new Course("Testcourse", "A test course for JUnit", 50);
            RequestBody requestBody = RequestBody.create(JSON, builder.serialize(testCourse));

            Request request = new Request.Builder()
                    .url(BASE_URL)
                    .post(requestBody)
                    .header("Authorization", adminCreds)
                    .build();

            Response response = client.newCall(request).execute();

            if (response.code() != 201) {
                fail("Wrong response code.");
            } else {
                assertTrue(Objects.requireNonNull(response.header("Location"))
                        .contains("http://localhost:8080/api/softskills/courses/" + testCourse.getHashId()));
            }
        } catch (NullPointerException e) {
            fail("No location header has been sent by the server.");
        } catch (IOException e) {
            fail("Call to the Server couldn't be made. Is the server not running?");
        }
    }


    // GET all courses as admin
    @Test
    @Order(2)
    public void getAllCoursesAsAdminTest() {
        try {
            Request request = new Request.Builder()
                    .url(BASE_URL)
                    .get()
                    .header("Authorization", adminCreds)
                    .build();

            Response response = client.newCall(request).execute();

            if (response.code() != 200) {
                fail("Wrong response code.");
            } else {
                assertTrue(Objects.requireNonNull(response.body()).string().contains(testCourse.getHashId()));
            }
        } catch (NullPointerException e) {
            fail("No response body has been sent by the server");
        } catch (IOException e) {
            fail("Call to the Server couldn't be made. Is the server not running?");
        }
    }


    // GET all courses as student
    @Test
    @Order(3)
    public void getAllCoursesAsStudentTest() {
        try {
            Request request = new Request.Builder()
                    .url(BASE_URL)
                    .get()
                    .header("Authorization", studentCreds)
                    .build();

            Response response = client.newCall(request).execute();

            if (response.code() != 200) {
                fail("Wrong response code.");
            } else {
                assertTrue(Objects.requireNonNull(response.body()).string().contains(testCourse.getHashId()));
            }
        } catch (NullPointerException e) {
            fail("No response body has been sent by the server");
        } catch (IOException e) {
            fail("Call to the Server couldn't be made. Is the server not running?");
        }
    }


    // GET course by id
    @Test
    @Order(4)
    public void getCourseByIdTest() {
        try {
            Request request = new Request.Builder()
                    .url(BASE_URL + "/" + testCourse.getHashId())
                    .get()
                    .header("Authorization", adminCreds)
                    .build();

            Response response = client.newCall(request).execute();

            if (response.code() != 200) {
                fail("Wrong response code");
            } else {
                assertTrue(Objects.requireNonNull(response.body()).string().contains(testCourse.getHashId()));
            }
        } catch (NullPointerException e) {
            fail("No response body has been sent by the server");
        } catch (IOException e) {
            fail("Call to the Server couldn't be made. Is the server not running?");
        }
    }


    // GET course by name
    @Test
    @Order(5)
    public void getCourseByNameTest() {
        try {
            Request request = new Request.Builder()
                    .url(BASE_URL + "/?courseName=" + testCourse.getCourseName())
                    .get()
                    .header("Authorization", adminCreds)
                    .build();

            Response response = client.newCall(request).execute();

            if (response.code() != 200) {
                fail("Wrong response code");
            } else {
                assertTrue(Objects.requireNonNull(response.body()).string().contains(testCourse.getHashId()));
            }
        } catch (NullPointerException e) {
            fail("No response body has been sent by the server");
        } catch (IOException e) {
            fail("Call to the Server couldn't be made. Is the server not running?");
        }
    }


    // GET all the events from a specific course
    @Test
    @Order(6)
    public void getAllEventsFromSpecificCourseTest() {
        try {
            testEvent = new Event("2020-07-18--18:00:00", "2020-07-18--19:00:00");
            testEvent.setCourseId(testCourse.getHashId());

            eventDatabase.insertInto(testEvent);

            Request request = new Request.Builder()
                    .url(BASE_URL + "/" + testCourse.getHashId() + "/events")
                    .get()
                    .header("Authorization", adminCreds)
                    .build();

            Response response = client.newCall(request).execute();

            if (response.code() != 200) {
                fail("Wrong response code.");
            } else {
                assertTrue(Objects.requireNonNull(response.body()).string().contains(testEvent.getHashId()));
            }
        } catch (NullPointerException e) {
            fail("No response body has been sent by the server");
        } catch (IOException e) {
            fail("Call to the Server couldn't be made. Is the server not running?");
        }
    }

    // GET a specific event from a specific course
    @Test
    @Order(7)
    public void getSpecificEventFromSpecificCourseTest() {
        try {
            Request request = new Request.Builder()
                    .url(BASE_URL + "/" + testCourse.getHashId() + "/events/" + testEvent.getHashId())
                    .get()
                    .header("Authorization", adminCreds)
                    .build();

            Response response = client.newCall(request).execute();

            if (response.code() != 200) {
                fail("Wrong response code.");
            } else {
                assertTrue(Objects.requireNonNull(response.body()).string().contains(testCourse.getHashId()));
            }
        } catch (NullPointerException e) {
            fail("No response body has been sent by the server");
        } catch (IOException e) {
            fail("Call to the Server couldn't be made. Is the server not running?");
        }
    }


    // CourseService: GET all courses cacheControl check
    @Test
    @Order(8)
    public void getAllCoursesCacheControlTest() {
        try {
            Request request = new Request.Builder()
                    .url(BASE_URL)
                    .get()
                    .header("Authorization", studentCreds)
                    .build();

            Response response = client.newCall(request).execute();

            if (response.code() != 200) {
                fail("Wrong response code.");
            } else {
                assertNotNull(response.header("Cache-Control"));
            }
        } catch (NullPointerException e) {
            fail("No response body has been sent by the server");
        } catch (IOException e) {
            fail("Call to the Server couldn't be made. Is the server not running?");
        }
    }


    // CourseService: GET single cacheControl check
    @Test
    @Order(9)
    public void getAllSingleCourseCacheControlTest() {
        try {
            Request request = new Request.Builder()
                    .url(BASE_URL + "/" + testCourse.getHashId())
                    .get()
                    .header("Authorization", studentCreds)
                    .build();

            Response response = client.newCall(request).execute();

            if (response.code() != 200) {
                fail("Wrong response code.");
            } else {
                assertNotNull(response.header("Cache-Control"));
            }
        } catch (NullPointerException e) {
            fail("No response body has been sent by the server");
        } catch (IOException e) {
            fail("Call to the Server couldn't be made. Is the server not running?");
        }
    }


    // CourseService: GET all events cacheControl check
    @Test
    @Order(10)
    public void getAllEventsCacheControlTest() {
        try {
            Request request = new Request.Builder()
                    .url(BASE_URL + "/" + testCourse.getHashId() + "/events")
                    .get()
                    .header("Authorization", studentCreds)
                    .build();

            Response response = client.newCall(request).execute();

            if (response.code() != 200) {
                fail("Wrong response code.");
            } else {
                assertNotNull(response.header("Cache-Control"));
            }
        } catch (NullPointerException e) {
            fail("No response body has been sent by the server");
        } catch (IOException e) {
            fail("Call to the Server couldn't be made. Is the server not running?");
        }
    }

    // CourseService: GET specific event cacheControl check
    @Test
    @Order(11)
    public void getSpecificEventCacheControlTest() {
        try {
            Request request = new Request.Builder()
                    .url(BASE_URL + "/" + testCourse.getHashId() + "/events/" + testEvent.getHashId())
                    .get()
                    .header("Authorization", studentCreds)
                    .build();

            Response response = client.newCall(request).execute();

            if (response.code() != 200) {
                fail("Wrong response code.");
            } else {
                assertNotNull(response.header("Cache-Control"));
            }
        } catch (NullPointerException e) {
            fail("No response body has been sent by the server");
        } catch (IOException e) {
            fail("Call to the Server couldn't be made. Is the server not running?");
        }
    }


    // PUT a course
    @Test
    @Order(12)
    public void updateCourseTest() {
        try {
            testCourse.setCourseName("TestcoursePutTest");
            RequestBody requestBody = RequestBody.create(JSON, builder.serialize(testCourse));

            Request request = new Request.Builder()
                    .url(BASE_URL + "/" + testCourse.getHashId())
                    .header("Authorization", adminCreds)
                    .put(requestBody)
                    .build();

            Response response = client.newCall(request).execute();

            if (response.code() != 204) {
                fail("Wrong response code");
            }

            request = new Request.Builder()
                    .url(BASE_URL + "/" + testCourse.getHashId())
                    .get()
                    .header("Authorization", adminCreds)
                    .build();

            response = client.newCall(request).execute();

            String body = Objects.requireNonNull(response.body()).string();
            assertTrue(body.contains(testCourse.getHashId()) && body.contains("TestcoursePutTest"));
        } catch (NullPointerException e) {
            fail("No response body has been sent by the server");
        } catch (IOException e) {
            fail("Call to the Server couldn't be made. Is the server not running?");
        }
    }


    // DELETE a course
    @Test
    @Order(13)
    public void deleteCourseTest() {
        try {
            Request request = new Request.Builder()
                    .url(BASE_URL + "/" + testCourse.getHashId())
                    .delete()
                    .header("Authorization", adminCreds)
                    .build();

            Response response = client.newCall(request).execute();

            if (response.code() != 204) {
                fail("Wrong response code");
            }

            request = new Request.Builder()
                    .url(BASE_URL + "/" + testCourse.getHashId())
                    .get()
                    .header("Authorization", adminCreds)
                    .build();

            response = client.newCall(request).execute();

            String body = Objects.requireNonNull(response.body()).string();
            assertFalse(body.contains(testCourse.getHashId()) && body.contains("TestcoursePutTest"));
        } catch (NullPointerException e) {
            fail("No response body has been sent by the server");
        } catch (IOException e) {
            fail("Call to the Server couldn't be made. Is the server not running?");
        }
    }


    @AfterAll
    public void tearDown() {
        Course course = courseDatabase.getById(testCourse.getHashId());
        if (course != null) {
            courseDatabase.delete(testCourse.getHashId());
        }
        Event event = eventDatabase.getById(testEvent.getHashId());
        if (event != null) {
            eventDatabase.delete(testEvent.getHashId());
        }
    }
}