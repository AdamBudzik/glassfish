/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.s1peqe.connector.mq.simplestress.client;

import jakarta.jms.*;
import javax.naming.*;
import java.util.ArrayList;
import java.util.HashMap;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleMessageClient implements Runnable{
    static int NUM_CLIENTS = 20;
    static int NUM_CYCLES = 30;
    static int TIME_OUT = 20000;
    static long MDB_SLEEP_TIME = 2000;
    static boolean debug = false;

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    int id =0;

    public SimpleMessageClient(int i) {
        this.id = i;
    }

    public static void main(String[] args) {
        /**
	 * Start the threads that will send messages to MDB
	 */
        ArrayList al = new ArrayList();
        try {
            for (int i =0; i < NUM_CLIENTS; i++) {
                Thread client = new Thread(new SimpleMessageClient(i));
		al.add(client);
                client.start();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        Context                 jndiContext = null;
        QueueConnectionFactory  queueConnectionFactory = null;
        QueueConnection         queueConnection = null;
        QueueSession            queueSession = null;
        Queue                   queue = null;
        QueueReceiver           queueReceiver = null;
        TextMessage             message = null;

        try {
            jndiContext = new InitialContext();
            queueConnectionFactory = (QueueConnectionFactory)
                jndiContext.lookup
                ("java:comp/env/jms/QCFactory");
            queue = (Queue) jndiContext.lookup("java:comp/env/jms/clientQueue");

            queueConnection =
                queueConnectionFactory.createQueueConnection();
            queueSession =
                queueConnection.createQueueSession(false,
                    Session.AUTO_ACKNOWLEDGE);
            queueConnection.start();
            queueReceiver = queueSession.createReceiver(queue);

	    HashMap map = new HashMap();

            long startTime = System.currentTimeMillis();
            boolean pass = true;
	    //
	    // Receives all the messages and keep in the data structure
	    //
            for (int i =0; i < NUM_CLIENTS * NUM_CYCLES; i++) {
                TextMessage msg = (TextMessage) queueReceiver.receive(TIME_OUT);
		Integer id = new Integer(msg.getIntProperty("replyid"));
		if (map.containsKey(id)) {
		    pass = false;
		    debug("Duplicate :" + id);
		}
		map.put(id, msg.getText());
            }
            long totalTime = System.currentTimeMillis() - startTime;
	    System.out.println("Received all messages in :" + totalTime + " milliseconds");
	    System.out.println("------------------------------------------------------");

            // 
	    // Now examine the received data
	    //
            for (int i =0; i < NUM_CLIENTS * NUM_CYCLES; i++) {
	        String reply = (String) map.get(new Integer(i));
		if (!reply.equals("REPLIED:CLIENT")) {
		   pass = false;
		}
		System.out.println("Receeived :" + i + ":" + reply);
	    }
            
	    // Try to receive one more message than expected.
            TextMessage msg = (TextMessage) queueReceiver.receive(TIME_OUT);

	    if (msg != null) {
	       pass = false;
	       System.out.println("Received more than expected number of messages :" + msg.getText());
	    }

            if (pass) {
                stat.addStatus("Concurrent message delivery test", stat.PASS);
            } else {
                stat.addStatus("Concurrent message delivery test", stat.FAIL);
	    }
        }catch (Throwable t) {
            t.printStackTrace();
            stat.addStatus("Concurrent message delivery test", stat.FAIL);
        }finally {
            stat.printSummary("Concurrent message delivery test");
	    for (int i=0; i <al.size(); i++) {
	       Thread client = (Thread) al.get(i);
	       try {
	          client.join();
	       } catch (Exception e) {
	          System.out.println(e.getMessage());
	       }
	    }
            System.exit(0);
        }
         

    }

    public void run() {

        Context                 jndiContext = null;
        QueueConnectionFactory  queueConnectionFactory = null;
        QueueConnection         queueConnection = null;
        QueueSession            queueSession = null;
        Queue                   queue = null;
        QueueSender             queueSender = null;
        TextMessage             message = null;

        try {
            jndiContext = new InitialContext();
            queueConnectionFactory = (QueueConnectionFactory)
                jndiContext.lookup
                ("java:comp/env/jms/QCFactory");
            queue = (Queue) jndiContext.lookup("java:comp/env/jms/SampleQueue");

	    int startId = id * NUM_CYCLES;
	    int endId = (id * NUM_CYCLES) + NUM_CYCLES;
	    for (int i= startId;i < endId; i ++) {
                try {
                    queueConnection =
                    queueConnectionFactory.createQueueConnection();
                    queueSession =
                    queueConnection.createQueueSession(false,
                    Session.AUTO_ACKNOWLEDGE);
                    queueConnection.start();
                    queueSender = queueSession.createSender(queue);
                    message = queueSession.createTextMessage();
                    message.setText("CLIENT");
	            message.setIntProperty("id",i);
	            message.setLongProperty("sleeptime",MDB_SLEEP_TIME);
                    queueSender.send(message);
		    debug("Send the message :" + message.getIntProperty("id") + ":" + message.getText());
                } catch (Exception e) {
                    System.out.println("Exception occurred: " + e.toString());
                } finally {
                    if (queueConnection != null) {
                        try {
                            queueConnection.close();
                        } catch (JMSException e) {}
                    } // if
               } // finally
            }
        } catch (Throwable e) {
            System.out.println("Exception occurred: " + e.toString());
        } // finally
    } // main

    static void debug(String msg) {
        if (debug) {
	   System.out.println(msg);
	}
    }
} // class

