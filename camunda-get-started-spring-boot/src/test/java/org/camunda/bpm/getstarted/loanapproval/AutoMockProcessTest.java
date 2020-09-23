package org.camunda.bpm.getstarted.loanapproval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.extension.mockito.DelegateExpressions.autoMock;
import static org.camunda.bpm.extension.mockito.DelegateExpressions.verifyExecutionListenerMock;
import static org.camunda.bpm.extension.mockito.DelegateExpressions.verifyJavaDelegateMock;
import static org.camunda.bpm.extension.mockito.DelegateExpressions.verifyTaskListenerMock;
import static org.camunda.bpm.getstarted.loanapproval.MostUsefulProcessEngineConfiguration.mostUsefulProcessEngineConfiguration;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class AutoMockProcessTest {

	@Rule
	public final ProcessEngineRule processEngineRule = new ProcessEngineRule(mostUsefulProcessEngineConfiguration().buildProcessEngine());

	private TaskService taskService;

	@Before
	public void setUp() {
		taskService = processEngineRule.getTaskService();
	}

	@Test
	@Deployment(resources = "CaseAssignmentProcess.bpmn")
	public void register_mocks_for_all_listeners_and_delegates() throws Exception {
		autoMock("CaseAssignmentProcess.bpmn");

		assertThat(Mocks.get("loadData")).isNotNull();
		assertThat(Mocks.get("saveData")).isNotNull();

		final ProcessInstance processInstance = processEngineRule.getRuntimeService().startProcessInstanceByKey("CaseAssignmentAutoProcess");

		assertThat(processEngineRule.getTaskService().createTaskQuery().processInstanceId(processInstance.getId()).singleResult()).isNotNull();

		completeTask();

		verifyMocks();
	}

	@Test
	@Deployment(resources = "MockProcess_withoutNS.bpmn")
	public void register_mocks_for_all_listeners_and_delegates_noNS() throws Exception {
		autoMock("MockProcess_withoutNS.bpmn");

		assertThat(Mocks.get("loadData")).isNotNull();
		assertThat(Mocks.get("saveData")).isNotNull();

		final ProcessInstance processInstance = processEngineRule.getRuntimeService().startProcessInstanceByKey("process_mock_dummy");

		assertThat(processEngineRule.getTaskService().createTaskQuery().processInstanceId(processInstance.getId()).singleResult()).isNotNull();

		completeTask();

		verifyMocks();
	}

	private void verifyMocks() {
		verifyTaskListenerMock("verifyData").executed();
		verifyExecutionListenerMock("startProcess").executed();
		verifyJavaDelegateMock("loadData").executed();
		verifyJavaDelegateMock("saveData").executed();
		verifyExecutionListenerMock("beforeLoadData").executed();
	}

	private void completeTask() {
		taskService.complete(taskService.createTaskQuery().singleResult().getId());
	}
}
