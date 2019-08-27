package com.mq.model;

import java.util.Random;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class RPCService {
	
	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	private Random random = new Random();
	
	@Scheduled(fixedRate=2000)
	public void sendAndGetMessage() {
		
		Integer number = random.nextInt(10);
				
		System.out.println("Number is " + number);
				
		Object result = rabbitTemplate.convertSendAndReceive("mq.direct","rpc",number);
	
		if(result instanceof Integer)
			result = (Integer) result;
		
		System.out.println("Result is " + result);
	}
	
	@RabbitListener(queues="#{autoDeleteQueue1.name}")
	public Integer getAndSendMessage(Integer number) {
		return number * number;
	}
	
}
