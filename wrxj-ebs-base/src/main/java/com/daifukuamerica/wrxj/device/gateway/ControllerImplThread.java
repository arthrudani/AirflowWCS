package com.daifukuamerica.wrxj.device.gateway;

import com.daifukuamerica.wrxj.controller.NamedThread;

public final class ControllerImplThread extends NamedThread
{

  public final SystemGateway mpControllerImpl;
  
  ControllerImplThread(SystemGateway ipControllerImpl)
  {
    super(ipControllerImpl);
    mpControllerImpl = ipControllerImpl;
  }

}

