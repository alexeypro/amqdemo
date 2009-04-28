import java.io.PrintStream;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;

import javax.jms.MessageConsumer;
import javax.jms.MessageEOFException;
import javax.jms.MessageListener;

import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;

import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;


//
// took the code from http://weblog.plexobject.com/?p=1368
// it seemed to be the cleanest, than if I'd put atomkeep's here :-)
//
public class ServerDemo implements MessageListener, ExceptionListener {
     Session session;

		 // we will "listen" for the incoming requests here
     public ServerDemo() throws Exception {
				ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER, 
																																										ActiveMQConnection.DEFAULT_PASSWORD,
																																										ActiveMQConnection.DEFAULT_BROKER_URL);																																						
			  Connection connection = connectionFactory.createQueueConnection();
				connection.setExceptionListener(this);		// somebody has to handle exceptions
				connection.start();				
				session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);	
				Queue q = session.createQueue("ScreenscrapBebo");	
				MessageConsumer consumer = session.createConsumer(q);
				consumer.setMessageListener(this);
				System.out.println("[SERVER] Waiting on /queue/" + q.getQueueName());				
		}
		
		// not implemented, but should be :-)
		private void close() throws Exception {
				if (session != null) session.close();
				//if (connection != null) connection.close();
		}
		
		// launch ourselves, "entry point"
		public static void main(String[] args) throws Exception {
				new ServerDemo();
				Thread.currentThread().join();
		}
		
		// wow, we received message, let's work with it here
		public void onMessage(Message message) {
				try {
						System.out.println("[SERVER] Message arrived...");	
						if (message instanceof BytesMessage) {
								BytesMessage bytMsg = (BytesMessage) message;
								StringBuffer msg = new StringBuffer();
								int c;
								try {
										while ((c=bytMsg.readByte()) != -1) {
												msg.append((char) c);
										}
								} catch (javax.jms.MessageEOFException e) {}
								System.out.println("[SERVER] Received: " + msg);
						} else {
								System.out.println("[SERVER] Unknown Message: " + message);
						}
						session.commit();				
								
						// emulate the job here, 
						System.out.print("[SERVER] Doing the job: ");
						for (int i = 0; i <= 7; i++) {
								System.out.print(".");
								Thread.sleep(1000); // 1 secs								
						}
						System.out.println(" Done!");
												
						reply(message);
				} catch (Exception e) {
						e.printStackTrace();
				}
		}
		
		// yeah, yeah, exceptions
		synchronized public void onException(JMSException ex) {
				System.out.println("[SERVER] JMS Exception occured. " + ex);
		}
		
		// we need to reply (but not necessary to do it always! :-)
		public void reply(Message msg) throws Exception {
				ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER,
																																										ActiveMQConnection.DEFAULT_PASSWORD,
																																										ActiveMQConnection.DEFAULT_BROKER_URL);
        QueueConnection replyConnection = connectionFactory.createQueueConnection();
				replyConnection.setExceptionListener(this);
				replyConnection.start();
				QueueSession replySession = (QueueSession) replyConnection.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
				Queue queue = (Queue) msg.getJMSReplyTo();
				QueueSender sender = replySession.createSender(queue);
				BytesMessage responseMessage = replySession.createBytesMessage();
				responseMessage.writeUTF("Response to ... to " + msg.getJMSReplyTo());
				sender.send(responseMessage);
				replySession.commit();
				sender.close();
				replyConnection.close();
				System.out.println("[SERVER] Reply: Sent and closed");								
		}

}