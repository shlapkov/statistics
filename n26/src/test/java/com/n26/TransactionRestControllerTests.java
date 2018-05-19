/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.n26;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.n26.model.Transaction;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TransactionRestControllerTests {

    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream()
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .orElse(null);

        assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }

    @BeforeClass
    public static void setup() {
        System.setProperty("n26.evictionTimeOut", "3");
    }


    @Test
    public void shouldReturnStatistics() throws Exception {
        Thread.sleep(3000);
        ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
        Transaction transaction = new Transaction(utc.toInstant().toEpochMilli(), 28.0);
        this.mockMvc.perform(post("/transactions" )
                .content(this.toJson(transaction))
                .contentType(contentType)).andExpect(status().is(201));
        this.mockMvc.perform(get("/statistics" ))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.max", is(28.0)))
                .andExpect(jsonPath("$.min", is(28.0)))
                .andExpect(jsonPath("$.count", is(1)));
        Thread.sleep(3000);
        this.mockMvc.perform(get("/statistics" ))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.sum", is(0.0)))
                .andExpect(jsonPath("$.avg", is(0.0)))
                .andExpect(jsonPath("$.max", is(0.0)))
                .andExpect(jsonPath("$.min", is(0.0)))
                .andExpect(jsonPath("$.count", is(0)));
    }

    @Test
    public void shouldReturn201() throws Exception {
        ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
        Transaction transaction = new Transaction(utc.toInstant().toEpochMilli(), 12.3);
        this.mockMvc.perform(post("/transactions" )
                .content(this.toJson(transaction))
        .contentType(contentType)).andExpect(status().is(201));
    }

    @Test
    public void shouldReturn204() throws Exception {
        ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(60);
        Transaction transaction = new Transaction(utc.toInstant().toEpochMilli(), 12.3);
        this.mockMvc.perform(post("/transactions" )
                .content(this.toJson(transaction))
                .contentType(contentType)).andExpect(status().is(204));
    }

    private String toJson(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }

}
