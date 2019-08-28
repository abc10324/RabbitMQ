package com.mq.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

@Service
public class RPCService {
	
//	@Autowired
//	private RabbitTemplate rabbitTemplate;
//	
//	private Random random = new Random();
//	
//	@Scheduled(fixedRate=2000)
//	public void sendAndGetMessage() {
//		
//		Integer number = random.nextInt(10);
//				
//		System.out.println("Number is " + number);
//				
//		Object result = rabbitTemplate.convertSendAndReceive("mq.direct","rpc",number);
//	
//		if(result instanceof Integer)
//			result = (Integer) result;
//		
//		System.out.println("Result is " + result);
//	}
//	
//	@RabbitListener(queues="#{autoDeleteQueue1.name}")
//	public Integer getAndSendMessage(Integer number) {
//		return number * number;
//	}
	
	private List<Connection> connectionList = new ArrayList<Connection>();
	private List<Channel> channelList = new ArrayList<Channel>();
	
	private Random random = new Random();
	
	@Scheduled(fixedRate=5000)
	public void sendMessage() {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		
		Connection connection = null;
		Channel channel = null;
		
		try{
			connection = factory.newConnection();
			channel = connection.createChannel();
			
			channel.queueDeclare("rpc", false, false, false, null);
			channel.queuePurge("rpc");
			
			String replyTo = channel.queueDeclare().getQueue();
			final String corrId = UUID.randomUUID().toString();
			
			BasicProperties prop = new BasicProperties()
										.builder()
										.correlationId(corrId)
										.replyTo(replyTo)
										.build();
			
			Integer number = random.nextInt(10);
			
			String message = number.toString();
			
			channel.basicPublish("", "rpc", prop, message.getBytes());
			
			System.out.println(" [x] Sent to Client : '" + message + "'");
			
			final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);
			
			Consumer consumer = new DefaultConsumer(channel) {
	        	@Override
	            public void handleDelivery(String consumerTag,
	                                       Envelope envelope,
	                                       AMQP.BasicProperties properties,
	                                       byte[] body)
	                throws IOException
	            {
	        		
	        		if(properties.getCorrelationId().equals(corrId)) {
	        			response.offer(new String(body));
	        		}
	            }
	        };
	        
	        String consumerTag = channel.basicConsume(replyTo, true, consumer);
	        
	        System.out.println("Recieve from client : " + response.take());
	        System.out.println();
	        
	        channel.basicCancel(consumerTag);
			
		} catch (IOException | TimeoutException | InterruptedException e) {
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
		initClientConsumer();
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

	
	// initial client side consumer
	public void initClientConsumer() {
		ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        
        Connection connection = null;
		Channel channel = null;
		
		try{
			connection = factory.newConnection();
			channel = connection.createChannel();
			
			connectionList.add(connection);
			channelList.add(channel);
			
			channel.queueDeclare("rpc", false, false, false, null);
			
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
	        		
					System.out.println("Recieve from server : " + new String(body));
	        		
					Integer number = Integer.valueOf(new String(body));
					number *= number;
					
					ConnectionFactory factory = new ConnectionFactory();
					factory.setHost("localhost");
					
					Connection connection = null;
					Channel channel = null;
					
					try{
						connection = factory.newConnection();
						channel = connection.createChannel();
						
						BasicProperties prop = new BasicProperties()
													.builder()
													.correlationId(properties.getCorrelationId())
													.build();
						
						String message = number.toString();
						
						channel.basicPublish("", properties.getReplyTo(), prop, message.getBytes());
						
						System.out.println(" Client send to Server : " + message);
						
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
					
					
	        		getChannel().basicAck(envelope.getDeliveryTag(), false);
	            }
	        };
	        
	        channel.basicConsume("rpc", false, consumer);
			
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
		} 
		
	}
	
}
