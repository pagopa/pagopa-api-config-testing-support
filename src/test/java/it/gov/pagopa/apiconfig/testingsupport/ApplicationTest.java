package it.gov.pagopa.apiconfig.testingsupport;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.gov.pagopa.apiconfig.testingsupport.controller.GenericQueryController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
class ApplicationTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void runQuery() throws Exception {
    mockMvc
      .perform(
              post("/genericQuery")
                      .content("select 1;"))
      .andExpect(status().isOk());

    mockMvc
        .perform(
            post("/genericQuery")
                .content("create table test_table (id INT NOT NULL,col1 VARCHAR(50) NOT NULL);"))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            post("/genericQuery")
                .content("insert into test_table(ID, COL1) VALUES(0, 'test insert');"))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            post("/genericQuery")
                .content("select * from test_table;"))
        .andExpect(status().isOk())
        .andExpect(content().string("[{\"ID\":0,\"COL1\":\"test insert\"}]"));

    mockMvc
        .perform(
            post("/genericQuery")
                .content("update test_table set col1 = 'updated value' where id = 0"))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            post("/genericQuery")
                .content("truncate table test_table;"))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(
            post("/genericQuery")
                .content("selct * from test_table;"))
        .andExpect(status().isInternalServerError());

    mockMvc
        .perform(
            post("/genericQuery")
                .content("gibberishfoo from bar;"))
        .andExpect(status().isInternalServerError());

    mockMvc
        .perform(
            post("/genericQuery")
                .content("select * from no_table;"))
        .andExpect(status().isInternalServerError());

    mockMvc
        .perform(
            post("/genericQuery")
                .content("select no_col from test_table;"))
        .andExpect(status().isInternalServerError());
  }
}
