package com.mq.model;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
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
public class TopicService {

//	@Autowired
//	private RabbitTemplate rabbitTemplate;
//	
//	private String[] destArr = {"nervous.red.rabbit" // topic 2
//							   ,"lazy.brown.dog" 	 // topic 1 2
//							   ,"lazy.gold.cat"  	 // topic 2
//							   ,"normal.brown.human" // topic 1
//							   ,"nervous.blue.fish"};// no topic
//	
//	private Random random = new Random();
//	
//	@Scheduled(fixedRate=2000)
//	public void sendMessage() {
//		
//		String destination = destArr[random.nextInt(destArr.length)];
//		
//		System.out.println("Send to Topic : " + destination);
//		
//		rabbitTemplate.convertAndSend("mq.topic", destination, new Date());
//	}
//	
//	@RabbitListener(queues="#{autoDeleteQueue1.name}")
//	public void getMessage(Date msg) {
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//		
//		System.out.println("Get Message : " + sdf.format(msg) + " at topic 1");
//	}
//	
//	@RabbitListener(queues="#{autoDeleteQueue2.name}")
//	public void getMessage1(Date msg) {
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//		
//		System.out.println("Get Message : " + sdf.format(msg) + " at topic 2");
//		
//	}
	
	
	private List<Connection> connectionList = new ArrayList<Connection>();
	private List<Channel> channelList = new ArrayList<Channel>();
	
	private String[] destArr = {"nervous.red.rabbit" // topic 2
							   ,"lazy.brown.dog" 	 // topic 1 2
							   ,"lazy.gold.cat"  	 // topic 2
							   ,"normal.brown.human" // topic 1
							   ,"nervous.blue.fish"};// no topic
	
	private Random random = new Random();
	
	@Scheduled(fixedRate=2000)
	public void sendMessage() {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		
		Connection connection = null;
		Channel channel = null;
		
		try{
			connection = factory.newConnection();
			channel = connection.createChannel();
			
			channel.exchangeDeclare("mq.topic", "topic");
			
			String destination = destArr[random.nextInt(destArr.length)];
			
			String message = "Hello World!";
			
			channel.basicPublish("mq.topic", destination, null, message.getBytes());
			
			System.out.println(" [x] Sent to topic '" + destination + "' : '" + message + "'");
			
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
		initConsumer(1,0,"*.brown.*");
		initConsumer(2,0,"*.*.rabbit","lazy.#");
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
	public void initConsumer(int serverNo,int sleepSecond,String... routingKeys) {
		ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        
        Connection connection = null;
		Channel channel = null;
		
		try{
			connection = factory.newConnection();
			channel = connection.createChannel();
			
			connectionList.add(connection);
			channelList.add(channel);
			
			String queueName = channel.queueDeclare().getQueue();
			
			for(String routingKey : routingKeys) {
				channel.queueBind(queueName, "mq.topic", routingKey);
			}
				
			
	        
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
	        
	        channel.basicConsume(queueName, false, consumer);
			
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
		} 
		
	}
}
