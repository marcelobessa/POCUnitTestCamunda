package org.camunda.bpm.getstarted.loanapproval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class QueryMocksExample {
	 private final TaskService taskService = mock(TaskService.class);
	  private final Task task = mock(Task.class);

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
