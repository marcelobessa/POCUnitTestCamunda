package org.camunda.bpm.getstarted.loanapproval.delegate;

import org.apache.commons.lang3.RandomUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
public class TaskDelagate1 implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		// TODO Auto-generated method stub
		
		int intBK = RandomUtils.nextInt();		
		
		if(intBK % 2 == 0) {
			execution.setVariable("pathToGo", Boolean.TRUE);
		}else {
			execution.setVariable("pathToGo", Boolean.FALSE);
		}

	}

}
