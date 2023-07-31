Rem  replace localhost with hostname from hostconfig

create sequence localhostWrxtoHost
 start with 1
 minvalue 1
 increment by 1
 maxvalue 200000
 cycle; 



 create sequence localhostHosttoWrx
 start with 1
 minvalue 1
 increment by 1
 maxvalue 200000
 cycle; 