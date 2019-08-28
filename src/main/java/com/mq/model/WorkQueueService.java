package com.mq.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

//@Service
public class WorkQueueService{

//	@Autowired
//	private RabbitTemplate rabbitTemplate;
//	
//	@Scheduled(fixedRate=1000)
//	public void sendMessage() {
//		System.out.println("Send message");
//		rabbitTemplate.convertAndSend("myQueue", new Date());
//	}
//	
//	@RabbitListener(queues="myQueue")
//	public void getMessage(Date msg) {
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//		
//		System.out.println("Get Message : " + sdf.format(msg) + " at server 1");
//	}
//	
//	@RabbitListener(queues="myQueue")
//	public void getMessage1(Date msg) {
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//		
//		System.out.println("Get Message : " + sdf.format(msg) + " at server 2");
//		
//	}
	
	private List<Connection> connectionList = new ArrayList<Connection>();
	private List<Channel> channelList = new ArrayList<Channel>();
	
	@Scheduled(fixedRate=1000)
	public void sendMessage() {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		
		Connection connection = null;
		Channel channel = null;
		
		try{
			connection = factory.newConnection();
			channel = connection.createChannel();
			
			channel.queueDeclare("myQueue", false, false, false, null);
			String message = "Hello World!";
			
			
			channel.basicPublish("", "myQueue", null, message.getBytes());
			System.out.println(" [x] Sent '" + message + "'");
			
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
		} finally {
			try {
				if(channel != null)
					channel.close();
				if(connection != null)
					connection.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	// start consumer at system start point
	@EventListener(ContextRefreshedEvent.class)
	public void getMessage() {
		initConsumer(1,5);
		initConsumer(2,0);
	}
	
	// close consumer's connection
	@EventListener(ContextClosedEvent.class)
	public void stopConsumer() {
		try {
			for(Channel channel : channelList)
				channel.close();
			
			for(Connection connection : connectionList)
				connection.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		
	}

	
	// initial consumer
	public void initConsumer(int serverNo,int sleepSecond) {
		ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        
        Connection connection = null;
		Channel channel = null;
		
		try{
			connection = factory.newConnection();
			channel = connection.createChannel();
			
			connectionList.add(connection);
			channelList.add(channel);
			
			channel.queueDeclare("myQueue", false, false, false, null);
	        channel.basicQos(1);
			System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
	        
	        Consumer consumer = new DefaultConsumer(channel) {
	        	@Override
	            public void handleDelivery(String consumerTag,
	                                       Envelope envelope,
	                                       AMQP.BasicProperties properties,
	                                       byte[] body)
	                throws IOException
	            {
	        		
	        		try {
						Thread.sleep(sleepSecond * 1000);
						System.out.println("Recieve from server " + serverNo + " : " + new String(body));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
	        		
	        		getChannel().basicAck(envelope.getDeliveryTag(), false);
	            }
	        };
	        
	        channel.basicConsume("myQueue", false, consumer);
			
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
		} 
		
	}
}
