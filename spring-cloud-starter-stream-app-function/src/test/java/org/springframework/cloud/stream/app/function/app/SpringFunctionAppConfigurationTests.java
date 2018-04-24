/*
 * Copyright 2017 the original author or authors.
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

package org.springframework.cloud.stream.app.function.app;

import java.util.concurrent.TimeUnit;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FunctionAppConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
		"function.location=file:target/it/support/target/function-sample-1.0.0.M1-exec.jar", })
public abstract class SpringFunctionAppConfigurationTests {

	@Autowired
	protected MessageCollector messageCollector;

	@EnableAutoConfiguration
	@TestPropertySource(properties = { "function.bean=myEmitter",
			"function.main=com.example.functions.FunctionApp" })
	public static class SourceTests extends SpringFunctionAppConfigurationTests {

		@Autowired
		private Source source;

		@Test
		public void test() throws Exception {

			Message<?> received = messageCollector.forChannel(source.output()).poll(2,
					TimeUnit.SECONDS);
			assertThat(received.getPayload(), Matchers.is("one"));

		}

	}

	@EnableAutoConfiguration
	@TestPropertySource(properties = { "function.bean=myCounter",
			"function.main=com.example.functions.FunctionApp" })
	public static class ProcessorTests extends SpringFunctionAppConfigurationTests {

		@Autowired
		private Processor processor;

		@Test
		public void test() throws Exception {
			processor.input().send(MessageBuilder.withPayload("hello").build());
			Message<?> received = messageCollector.forChannel(processor.output()).poll(1,
					TimeUnit.SECONDS);
			assertThat(received.getPayload(), Matchers.is("hello".length()));

		}

	}

	@EnableAutoConfiguration
	@TestPropertySource(properties = { "function.bean=myDoubler",
			"function.main=com.example.functions.FunctionApp" })
	public static class SinkTests extends SpringFunctionAppConfigurationTests {

		@Autowired
		private Sink sink;

		@Test
		public void test() throws Exception {
			// Can't assert side effects.
			sink.input().send(MessageBuilder.withPayload(5).build());
		}

	}

}