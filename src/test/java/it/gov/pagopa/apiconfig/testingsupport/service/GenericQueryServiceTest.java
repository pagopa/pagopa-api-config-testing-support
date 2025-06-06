package it.gov.pagopa.apiconfig.testingsupport.service;

import it.gov.pagopa.apiconfig.testingsupport.exception.AppError;
import it.gov.pagopa.apiconfig.testingsupport.exception.AppException;
import org.hibernate.query.internal.NativeQueryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class GenericQueryServiceTest {

    @InjectMocks
    private GenericQueryService service;

    @Mock
    private EntityManager entityManager;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new GenericQueryService(jdbcTemplate);
        service.dangerousKeywordsList = Arrays.asList(new String[] {"DROP", "DELETE", "TRUNCATE", "ALTER", "CREATE", "INSERT"});
        service.entityManager = entityManager;
    }

    @Test
    void testGetQueryResponse_select() {
        String query = "SELECT * FROM TEST";
        List<Map<String, Object>> expected = List.of(Collections.singletonMap("col", "val"));

        // Mock NativeQueryImpl
        NativeQueryImpl nativeQuery = mock(NativeQueryImpl.class);
        when(entityManager.createNativeQuery(query)).thenReturn(nativeQuery);
        when(nativeQuery.setResultTransformer(any())).thenReturn(nativeQuery);

        when(jdbcTemplate.queryForList(query)).thenReturn(expected);

        List result = service.getQueryResponse(query);
        assertEquals(expected, result);
    }

    @Test
    void testGetQueryResponse_update() {
        String query = "UPDATE TEST SET field='value' WHERE obj_id=111;";
        int updateCount = 2;

        // Mock NativeQueryImpl
        NativeQueryImpl nativeQuery = mock(NativeQueryImpl.class);
        when(entityManager.createNativeQuery(query)).thenReturn(nativeQuery);
        when(nativeQuery.setResultTransformer(any())).thenReturn(nativeQuery);
        when(nativeQuery.executeUpdate()).thenReturn(updateCount);

        List result = service.getQueryResponse(query);
        assertEquals(Collections.singletonList(updateCount), result);
    }

    @Test
    void testGetQueryResponse_dangerousKeyword() {
        String query = "DROP TABLE TEST";
        AppException ex = assertThrows(AppException.class, () -> service.getQueryResponse(query));
        assertEquals(AppError.DANGEROUS_QUERY.httpStatus, ex.getHttpStatus());
        assertEquals(AppError.DANGEROUS_QUERY.title, ex.getTitle());
    }

    @Test
    void testGetQueryResponse_wrongGrammar() {
        String query = "SELECT * FROM";
        when(entityManager.createNativeQuery(query)).thenThrow(new PersistenceException());

        AppException ex = assertThrows(AppException.class, () -> service.getQueryResponse(query));
        assertEquals(AppError.WRONG_QUERY_GRAMMAR.httpStatus, ex.getHttpStatus());
        assertEquals(AppError.WRONG_QUERY_GRAMMAR.title, ex.getTitle());
    }

    @Test
    void testGetQueryResponse_internalServerError() {
        String query = "SELECT * FROM TEST";
        when(entityManager.createNativeQuery(query)).thenThrow(new RuntimeException("Generic error"));

        AppException ex = assertThrows(AppException.class, () -> service.getQueryResponse(query));
        assertEquals(AppError.INTERNAL_SERVER_ERROR.httpStatus, ex.getHttpStatus());
        assertEquals(AppError.INTERNAL_SERVER_ERROR.title, ex.getTitle());
    }

    @Test
    void testGetMassiveQueryResponse_fileEmpty() {
        MockMultipartFile file = new MockMultipartFile("file", new byte[0]);
        AppException ex = assertThrows(AppException.class, () -> service.getMassiveQueryResponse(file));
        assertEquals(AppError.FILE_EMPTY.httpStatus, ex.getHttpStatus());
    }


    @Test
    void testGetMassiveQueryResponse_mixed() throws Exception {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("massive/massive_mixed.sql")) {
            MockMultipartFile file = new MockMultipartFile("file", inputStream);

            String query = "SELECT * FROM TEST";
            List expected = new ArrayList();
            expected.add(Collections.singletonList(0));
            expected.add(Collections.singletonList(0));
            expected.add(Collections.singletonList(0));
            expected.add(Collections.singletonList(0));
            expected.add(Collections.singletonList(0));
            expected.add(new LinkedList<>());

            NativeQueryImpl nativeQuery = mock(NativeQueryImpl.class);
            when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
            when(nativeQuery.setResultTransformer(any())).thenReturn(nativeQuery);
            when(jdbcTemplate.queryForList(query)).thenReturn(expected);

            List result = service.getMassiveQueryResponse(file);
            assertEquals(expected, result);
        }
    }

    @Test
    void testGetMassiveQueryResponse_update() throws Exception {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("massive/massive_update.sql")) {
            MockMultipartFile file = new MockMultipartFile("file", inputStream);

            String query = "UPDATE TEST SET field='value' WHERE obj_id=111";
            List expected = new ArrayList();
            expected.add(Collections.singletonList(0));
            expected.add(Collections.singletonList(0));
            expected.add(Collections.singletonList(0));
            expected.add(Collections.singletonList(0));
            expected.add(Collections.singletonList(0));

            NativeQueryImpl nativeQuery = mock(NativeQueryImpl.class);
            when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
            when(nativeQuery.setResultTransformer(any())).thenReturn(nativeQuery);
            when(jdbcTemplate.queryForList(query)).thenReturn(expected);

            List result = service.getMassiveQueryResponse(file);
            assertEquals(expected, result);
        }
    }

    @Test
    void testGetMassiveQueryResponse_wrongGrammar() throws Exception {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("massive/massive_wrong.sql")) {
            MockMultipartFile file = new MockMultipartFile("file", inputStream);

            String query = "SELECT * FROM";
            NativeQueryImpl nativeQuery = mock(NativeQueryImpl.class);
            when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
            when(jdbcTemplate.queryForList(anyString())).thenThrow(new BadSqlGrammarException(null, null, null));

            AppException ex = assertThrows(AppException.class, () -> service.getMassiveQueryResponse(file));
            assertEquals(AppError.WRONG_QUERY_GRAMMAR.httpStatus, ex.getHttpStatus());
            assertEquals(AppError.WRONG_QUERY_GRAMMAR.getTitle(), ex.getTitle());
        }
    }

    @Test
    void testGetMassiveQueryResponse_dangerousKeyword() throws Exception {
        // File con DROP
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("massive/massive_dangerous.sql")) {
            MockMultipartFile file = new MockMultipartFile("file", inputStream);

            AppException ex = assertThrows(AppException.class, () -> service.getMassiveQueryResponse(file));
            assertEquals(AppError.DANGEROUS_QUERY.httpStatus, ex.getHttpStatus());
        }
    }
}