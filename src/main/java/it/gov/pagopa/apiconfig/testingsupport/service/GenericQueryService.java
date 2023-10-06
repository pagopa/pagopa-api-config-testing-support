package it.gov.pagopa.apiconfig.testingsupport.service;

import it.gov.pagopa.apiconfig.testingsupport.exception.AppError;
import it.gov.pagopa.apiconfig.testingsupport.exception.AppException;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import org.hibernate.query.internal.NativeQueryImpl;
import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GenericQueryService {

  @PersistenceContext
  private EntityManager entityManager;
  @Value("${dangerous.sql.keywords}")
  private List<String> dangerousKeywordsList;

  @Transactional
  public List getQueryResponse(String query) {
    String sanitizedQuery = sanitizeQuery(query);
    try {
      Query nquery = entityManager.createNativeQuery(sanitizedQuery);
      NativeQueryImpl nativeQuery = (NativeQueryImpl) nquery;
      nativeQuery.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);
      if(modifying(query)){
        return Collections.singletonList(nativeQuery.executeUpdate());
      }else{
        return nativeQuery.getResultList();
      }
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

  private boolean modifying(String query){
    String lowerCase = query.toLowerCase();
    return lowerCase.contains("update") || lowerCase.contains("create table") || lowerCase.contains("insert into");
  }
}
