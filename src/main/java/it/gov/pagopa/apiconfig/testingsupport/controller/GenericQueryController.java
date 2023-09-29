package it.gov.pagopa.apiconfig.testingsupport.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.gov.pagopa.apiconfig.testingsupport.service.GenericQueryService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/generic")
public class GenericQueryController {
  @Autowired
  private GenericQueryService service;

  @PostMapping("/genericQuery")
  @Operation(
      summary = "Executes the query.",
      description = "This endpoint executes the query that is passed after being sanitized and then returns the result.",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Query to be executed.",
          required = true,
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(type = "string", example = "SELECT * FROM TABLE")
      )),
      responses = {
      @ApiResponse(responseCode = "200", description = "Success"),
      @ApiResponse(responseCode = "400", description = "Bad Request"),
      @ApiResponse(responseCode = "403", description = "Forbidden"),
      @ApiResponse(responseCode = "500", description = "Internal Server Error")
  }
  )
  public ResponseEntity<List<Object>> getQueryResponse(@RequestBody String query){
    return ResponseEntity.ok(service.getQueryResponse(query));
  }
}