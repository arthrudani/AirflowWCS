 --- For US 1399---
  Update asrs.CONTROLLERCONFIG set SPROPERTYVALUE = 'com.daifukuoc.wrxj.custom.ebs.scheduler.event.hostcleanuptask.HostCleanupTask',ISCREENCHANGEALLOWED = 2,
  IENABLED = 1 where SCONTROLLER = 'TimedEventScheduler' and SPROPERTYNAME = 'Task.HostCleanup.class';
  Update asrs.CONTROLLERCONFIG set SPROPERTYVALUE = '60',ISCREENCHANGEALLOWED = 2,
  IENABLED = 1 where SCONTROLLER = 'TimedEventScheduler' and SPROPERTYNAME = 'Task.HostCleanup.interval';
  Update asrs.CONTROLLERCONFIG set SPROPERTYVALUE = '5',ISCREENCHANGEALLOWED = 2,
  IENABLED = 1 where SCONTROLLER = 'TimedEventScheduler' and SPROPERTYNAME = 'Task.HostCleanup.DaysToKeep';