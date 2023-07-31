package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itempickedup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSLocation;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemstored.AisleEmptyLocationFinderImpl;

/**
 * Unit test case class for AisleEmptyLocationFinderImpl
 * @author MT
 *
 */

@ExtendWith(MockitoExtension.class)
class AisleEmptyLocationFinderImplTest {
	
	private AisleEmptyLocationFinderImpl emptyLocationFinderImpl;
	
	MockedStatic<Factory> mockedFactory;
	
	@Mock
	EBSLocation mpEBSLocation;
	
	@Mock
	LoadData loadData;
	
	private final static String LOAD_ADDRESS = "2000100101";
	private final static String LEVEL = "01";
	
	@BeforeEach
	public void setUp() {
		mockedFactory = Mockito.mockStatic(Factory.class);
		mockedFactory.when(() -> Factory.create(EBSLocation.class)).thenReturn(mpEBSLocation);
		emptyLocationFinderImpl = new AisleEmptyLocationFinderImpl();
	}
	
	@AfterEach
    public void tearDown() {
        mockedFactory.close();
    }
	
	
	@Test
	void testFind_No_Errors() throws DBException {
		doReturn(LOAD_ADDRESS).when(mpEBSLocation).findUnoccupiedLocationOfLevelAndDeviceId("WAREHOUSE", LEVEL, "DEVICE");
		doReturn("WAREHOUSE").when(loadData).getWarehouse();
		doReturn("DEVICE").when(loadData).getDeviceID();
		doReturn(LOAD_ADDRESS).when(loadData).getAddress();
		
		String availableAddress = emptyLocationFinderImpl.find(loadData);
		
		assertEquals(LOAD_ADDRESS, availableAddress);
		
	}

}
