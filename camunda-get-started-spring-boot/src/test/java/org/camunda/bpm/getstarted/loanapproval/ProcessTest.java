package org.camunda.bpm.getstarted.loanapproval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.init;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.execute;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.job;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.jobQuery;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.taskService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.withVariables;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.runtime.Job;

//import org.camunda.bpm.getstarted.loanapproval.InMemProcessEngineConfiguration;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.getstarted.loanapproval.util.LoggerDelegate;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import io.flowcov.camunda.junit.FlowCovProcessEngineRuleBuilder;


//@RunWith(SpringJUnit4ClassRunner.class)
//@Deployment(resources = { "process_test.bpmn" })
//@ContextConfiguration(classes = {InMemProcessEngineConfiguration.class})
public class ProcessTest {

	//	@Autowired
	//    private ProcessEngine processEngine;
	//
	//    @Rule
	//    @ClassRule
	//    public static ProcessEngineRule rule;
	//
	//    @PostConstruct
	//    public void init() {
	//        if (rule == null) {
	//            rule = FlowCovProcessEngineRuleBuilder
	//                .create(processEngine)
	//                .build();
	//        }
	//    }
	//    
	//    @Test
	//	public void contextLoads() {
	//	}
	//	
	//	private final TaskService taskService = mock(TaskService.class);
	//	private final Task task = mock(Task.class);
	//
	//	@Rule
	//	public final ExpectedException thrown = ExpectedException.none();
	//
	//	@Test
	//	  public void mock_taskQuery() {
	//	    // bind query-mock to service-mock and set result to task.
	//	    final TaskQuery taskQuery = null; //QueryMocks1.mockTaskQuery(taskService).singleResult(task);
	//
	//	    final Task result = taskService.createTaskQuery().active().activityInstanceIdIn("foo").excludeSubtasks().singleResult();
	//
	//	    assertThat(result).isEqualTo(task);
	//
	//	    verify(taskQuery).active();
	//	    verify(taskQuery).activityInstanceIdIn("foo");
	//	    verify(taskQuery).excludeSubtasks();
	//	  }


	@Rule
	@ClassRule
	public static ProcessEngineRule rule = FlowCovProcessEngineRuleBuilder.create().build();

	@Before
	public void setup() {
		init(rule.getProcessEngine());
	}

	@Test
	@Deployment(resources="cheescake-process.bpmn")
	public void testHappyPath() {

		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("endOfVisit", "2020-11-11T12:13:14Z");

		ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("myNYChesscakeProcess");
		assertThat(processInstance).isWaitingAt("NYTripEndedEvent");
		execute(job());
		assertThat(processInstance).isEnded().hasPassed("CheesecakeTestingEndedEndEvent1");

	}

	@Test
	@Deployment(resources="cheescake-process.bpmn")
	public void invitationMessageCheescakeTestingContinues() {
		Mocks.register("logger", new LoggerDelegate());

		ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("myNYChesscakeProcess");
		assertThat(processInstance).isWaitingAt("NYTripEndedEvent");

		runtimeService()
		.createMessageCorrelation("Msg_invite")
		.processInstanceId(processInstance.getId())
		.correlateWithResult();

		assertThat(processInstance).isWaitingAt("CheckInvitationTask");
		complete(task(), withVariables("betterThanCake", false));
		assertThat(processInstance).hasPassed("CheesecakeTestingContinuedEndEvent");
		assertThat(processInstance).isWaitingAt("NYTripEndedEvent");

	}

	@Test
	@Deployment(resources="cheescake-process.bpmn")
	public void invitationMessageCheescakeTestingAborted() {
		Mocks.register("logger", new LoggerDelegate());

		ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("myNYChesscakeProcess");
		assertThat(processInstance).isWaitingAt("NYTripEndedEvent");

		runtimeService()
		.createMessageCorrelation("Msg_invite")
		.processInstanceId(processInstance.getId())
		.correlateWithResult();

		assertThat(processInstance).isWaitingAt("CheckInvitationTask");
		complete(task(), withVariables("betterThanCake", true));
		assertThat(processInstance).hasPassed("AbortCheesecakeTestingEndEvent");
		assertThat(processInstance).hasPassed("AbortCheesecakeTestingStartEvent");
		assertThat(processInstance).isEnded().hasPassed("CheesecakeTestingEndedEndEvent");
	}

	@Test
	@Deployment(resources="cheescake-process.bpmn")
	public void CheescakeRecommendationMsgHappyPath() {
		Mocks.register("logger", new LoggerDelegate());

		ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("myNYChesscakeProcess");
		assertThat(processInstance).isWaitingAt("NYTripEndedEvent");

		runtimeService()
		.createMessageCorrelation("Msg_CheesecakeRecommendation")
		.processInstanceId(processInstance.getId())
		.correlateWithResult();

		assertThat(processInstance).isWaitingAt("TasteCheescakeTask");
		complete(task(), withVariables("approved", true));
		assertThat(processInstance).isWaitingAt("Gateway_1krafy7");

		List<Job> jobList = jobQuery()
				.processInstanceId(processInstance.getId())
				.list();
		assertThat(jobList).hasSize(2);
		Job job = jobList.get(1);
		execute(job);
		assertThat(processInstance).hasPassed("CheesecakeApprovedEndEvent");

	}

	@Test
	@Deployment(resources="cheescake-process.bpmn")
	public void CheescakeRecommendationCheesecakeRejected() {
		Mocks.register("logger", new LoggerDelegate());

		ProcessInstance processInstance = runtimeService()
				.createProcessInstanceByKey("myNYChesscakeProcess")
				.setVariable("approved", false)
				.startAfterActivity("TasteCheescakeTask")
				.execute();
		assertThat(processInstance).hasPassed("SendAComplaintTask").hasPassed("CheescakeRejectedEndEvent");
	}


	@Test
	@Deployment(resources="cheescake-process.bpmn")
	public void CheescakeRecommendationCheesecakeGoneBad() {
		Mocks.register("logger", new LoggerDelegate());

		ProcessInstance processInstance = runtimeService()
				.createProcessInstanceByKey("myNYChesscakeProcess")
				.setVariable("approved", true)
				.startAfterActivity("TasteCheescakeTask")
				.execute();
		assertThat(processInstance).isWaitingAt("Gateway_1krafy7");

		runtimeService().createMessageCorrelation("Msg_foodPoisend").correlateWithResult();

		assertThat(processInstance).hasPassed("BadCheesecakeEvent");
		assertThat(processInstance).hasPassed("DeleteFromListTask");
		assertThat(processInstance).hasPassed("SendAComplaintTask");
		assertThat(processInstance).hasPassed("CheescakeRejectedEndEvent");
	}

	@Test
	@Deployment(resources="cheescake-process.bpmn")
	public void CheescakeRecommendationNoShop() {
		Mocks.register("logger", new LoggerDelegate());

		ProcessInstance processInstance = runtimeService()
				.createProcessInstanceByKey("myNYChesscakeProcess")
				.startBeforeActivity("TasteCheescakeTask")
				.execute();
		assertThat(processInstance).isWaitingAt("TasteCheescakeTask");
		taskService().handleBpmnError((task().getId()), "err_NoShop");
		assertThat(processInstance).hasPassed("CheescakeRejectedEndEvent");
	}

	@Test
	@Deployment(resources="cheescake-process.bpmn")
	public void CheescakeRecommendationMoreThanOneCake() {
		Mocks.register("logger", new LoggerDelegate());

		ProcessInstance processInstance = runtimeService()
				.createProcessInstanceByKey("myNYChesscakeProcess")
				.startBeforeActivity("TasteCheescakeTask")
				.execute();
		assertThat(processInstance).isWaitingAt("TasteCheescakeTask");
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("numberOfCakes", "1");
		taskService().handleEscalation((task().getId()), "es_MoreCheesecake", variables);
		assertThat(processInstance).isWaitingAt("TasteCheescakeTask");

	}


}
