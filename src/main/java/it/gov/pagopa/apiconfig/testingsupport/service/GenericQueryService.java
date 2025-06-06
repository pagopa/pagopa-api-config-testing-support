package it.gov.pagopa.apiconfig.testingsupport.service;

import it.gov.pagopa.apiconfig.testingsupport.exception.AppError;
import it.gov.pagopa.apiconfig.testingsupport.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.internal.NativeQueryImpl;
import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenericQueryService {

  @PersistenceContext
  EntityManager entityManager;

  @Value("${dangerous.sql.keywords}")
  List<String> dangerousKeywordsList;

  private final JdbcTemplate jdbcTemplate;

  @Transactional
  public List getQueryResponse(String query) {
    String sanitizedQuery = sanitizeQuery(query);
    try {
      return executeQuery(sanitizedQuery);
    } catch (PersistenceException pExc) {
      throw new AppException(AppError.WRONG_QUERY_GRAMMAR);
    } catch (Exception e) {
      throw new AppException(AppError.INTERNAL_SERVER_ERROR);
    }
  }

  @Transactional
  public List<Object> getMassiveQueryResponse(MultipartFile file) {
    if (file.isEmpty()) {
      throw new AppException(AppError.FILE_EMPTY);
    }

    List<String> queries = new ArrayList<>();
    try {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
        StringBuilder queryBuilder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
          line = line.trim();

          // Skip empty lines and SQL comments
          if (line.isEmpty() || line.startsWith("--") || line.startsWith("//") || line.startsWith("/*")) {
            continue;
          }

          queryBuilder.append(line).append(" ");

          if (line.endsWith(";")) {
            String query = queryBuilder.toString().trim();
            queryBuilder.setLength(0); // reset builder

            String sanitizedQuery = sanitizeQuery(query);
            // Remove the trailing semicolon
            queries.add(sanitizedQuery.substring(0, query.length() - 1));
          }
        }
      }
    } catch (IOException e) {
      throw new AppException(AppError.DANGEROUS_QUERY);
    }

    List results = new ArrayList();
    try {
      for (String query : queries) {
        results.add(executeQuery(query));
      }
    } catch (PersistenceException | BadSqlGrammarException pExc) {
      throw new AppException(AppError.WRONG_QUERY_GRAMMAR);
    } catch (Exception e) {
      throw new AppException(AppError.INTERNAL_SERVER_ERROR);
    }

    return results;
  }

  private String sanitizeQuery(String originalQuery) {
    dangerousKeywordsList.forEach(keyword -> {
      if(originalQuery.toLowerCase().contains(keyword.toLowerCase()))
        throw new AppException(AppError.DANGEROUS_QUERY);
    });
    return originalQuery;
  }

  /**
   * return true if query is an update or insert
   * @param query
   * @return
   */
  private boolean modifying(String query){
    String lowerCase = query.toLowerCase();
    return lowerCase.contains("update") || lowerCase.contains("create table") || lowerCase.contains("insert into");
  }

  private List executeQuery(String query) {
    Query nquery = entityManager.createNativeQuery(query);
    NativeQueryImpl nativeQuery = (NativeQueryImpl) nquery;
    nativeQuery.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);
    if(modifying(query)){
      return Collections.singletonList(nativeQuery.executeUpdate());
    }
    else {
      // necessary to maintain the select column order
      return jdbcTemplate.queryForList(query);
    }
  }

}
