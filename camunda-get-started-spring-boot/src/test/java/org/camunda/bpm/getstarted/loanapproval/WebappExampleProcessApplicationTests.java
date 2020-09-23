package org.camunda.bpm.getstarted.loanapproval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.extension.mockito.DelegateExpressions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WebappExampleProcessApplicationTests {

	@Test
	public void contextLoads() {
	}

	private static final String BEAN_NAME = "foo";
	private static final String MESSAGE = "message";
	
	private final TaskService taskService = mock(TaskService.class);
	private final Task task = mock(Task.class);

	@Rule
	public final ExpectedException thrown = ExpectedException.none();

	@Test
	public void shouldThrowBpmnError() throws Exception {

		// expect exception
		thrown.expect(BpmnError.class);
		thrown.expectMessage(MESSAGE);

		DelegateExpressions.registerJavaDelegateMock(BEAN_NAME).onExecutionThrowBpmnError("code", MESSAGE);

		final JavaDelegate registeredDelegate = DelegateExpressions.getJavaDelegateMock(BEAN_NAME);

		// test succeeds when exception is thrown
		registeredDelegate.execute(mock(DelegateExecution.class));
	}
	
	@Test
	  public void mock_taskQuery() {
	    // bind query-mock to service-mock and set result to task.
	    final TaskQuery taskQuery = null; //QueryMocks1.mockTaskQuery(taskService).singleResult(task);

	    final Task result = taskService.createTaskQuery().active().activityInstanceIdIn("foo").excludeSubtasks().singleResult();

	    assertThat(result).isEqualTo(task);

	    verify(taskQuery).active();
	    verify(taskQuery).activityInstanceIdIn("foo");
	    verify(taskQuery).excludeSubtasks();
	  }
	
	
}