package it.gov.pagopa.apiconfig.testingsupport.controller;

import it.gov.pagopa.apiconfig.testingsupport.service.GenericQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class GenericQueryControllerTest {

    @Mock
    private GenericQueryService service;

    @InjectMocks
    private GenericQueryController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetQueryResponse() {
        String query = "SELECT * FROM TEST";
        List<Object> expected = Collections.singletonList("result");
        when(service.getQueryResponse(query)).thenReturn(expected);

        ResponseEntity<List<Object>> response = controller.getQueryResponse(query);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expected, response.getBody());
    }

    @Test
    void testGetMassiveQueryResponse() {
        MockMultipartFile file = new MockMultipartFile("file", "test.sql", "text/plain", "SELECT 1;".getBytes());
        List<Object> expected = Collections.singletonList("massiveResult");
        when(service.getMassiveQueryResponse(any())).thenReturn(expected);

        ResponseEntity<List<Object>> response = controller.getMassiveQueryResponse(file);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expected, response.getBody());
    }


}

