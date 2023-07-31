package com.daifukuamerica.wrxj.ipc;

import com.daifukuamerica.wrxj.log.Logger;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

/**
 * <B>Description:</B> Listener for the message service
 */
public class IpcMessageListener implements MessageListener
{
  private MessageService myService;
  private Logger myLogger;
  
  private String selector = null;
  private MessageConsumer msgConsumer = null;
  private String noLocal = null;

  public IpcMessageListener(MessageService ipMessageService, Logger ipLogger)
  {
    myService = ipMessageService;
    myLogger = ipLogger;
  }
  
  void setNoLocal(String isNoLocal)
  {
    noLocal = isNoLocal;
  }
  
  String getNoLocal()
  {
    return noLocal;
  }

  void setMessageSelector(String isSelector)
  {
    selector = isSelector;
  }

  String getMessageSelector()
  {
    return selector;
  }

  void setMessageConsumer(MessageConsumer ipMessageConsumer)
  {
    msgConsumer = ipMessageConsumer;
  }

  MessageConsumer getMessageConsumer()
  {
    return msgConsumer;
  }

  /**
   * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
   */
  @Override
  public void onMessage(Message message)
  {
    try
    {
      myService.onMessage(message, selector);
    }
    catch (Exception e)
    {
      myLogger.logException("Error processing JMS message", e);
    }
  }
}
