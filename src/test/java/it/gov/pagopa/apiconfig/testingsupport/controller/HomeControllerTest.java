package it.gov.pagopa.apiconfig.testingsupport.controller;

import it.gov.pagopa.apiconfig.testingsupport.model.AppInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.view.RedirectView;

import static org.junit.jupiter.api.Assertions.*;

class HomeControllerTest {

    private HomeController controller;

    @BeforeEach
    void setUp() {
        controller = new HomeController();
        ReflectionTestUtils.setField(controller, "basePath", "/api");
        ReflectionTestUtils.setField(controller, "name", "test-app");
        ReflectionTestUtils.setField(controller, "version", "1.0.0");
        ReflectionTestUtils.setField(controller, "environment", "test");
    }

    @Test
    void testHomeRedirect() {
        RedirectView redirectView = controller.home();
        assertEquals("/api/swagger-ui.html", redirectView.getUrl());
    }

    @Test
    void testHomeRedirectWithSlash() {
        ReflectionTestUtils.setField(controller, "basePath", "/api/");
        RedirectView redirectView = controller.home();
        assertEquals("/api/swagger-ui.html", redirectView.getUrl());
    }

    @Test
    void testHealthCheck() {
        ResponseEntity<AppInfo> response = controller.healthCheck();
        assertEquals(200, response.getStatusCodeValue());
        AppInfo info = response.getBody();
        assertNotNull(info);
        assertEquals("test-app", info.getName());
        assertEquals("1.0.0", info.getVersion());
        assertEquals("test", info.getEnvironment());
    }
}