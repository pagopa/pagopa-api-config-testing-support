package it.gov.pagopa.apiconfig.testingsupport.service;

import it.gov.pagopa.apiconfig.testingsupport.exception.AppError;
import it.gov.pagopa.apiconfig.testingsupport.exception.AppException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import org.hibernate.query.internal.NativeQueryImpl;
import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GenericQueryService {

  @PersistenceContext
  private EntityManager entityManager;
  @Value("${dangerous.sql.keywords}")
  private List<String> dangerousKeywordsList;

  public List getQueryResponse(String query) {
    String sanitizedQuery = sanitizeQuery(query);
    try {
      Query nquery = entityManager.createNativeQuery(sanitizedQuery);
      NativeQueryImpl nativeQuery = (NativeQueryImpl) nquery;
      nativeQuery.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);
      List<java.util.Map<String,Object>> result = nativeQuery.getResultList();
      return result;
    } catch (PersistenceException pExc) {
      throw new AppException(AppError.WRONG_QUERY_GRAMMAR);
    } catch (Exception e) {
      throw new AppException(AppError.INTERNAL_SERVER_ERROR);
    }
  }

  private String sanitizeQuery(String originalQuery) {
    dangerousKeywordsList.forEach(keyword -> {
      if(originalQuery.toLowerCase().contains(keyword.toLowerCase()))
        throw new AppException(AppError.DANGEROUS_QUERY);
    });
    return originalQuery;
  }
}
