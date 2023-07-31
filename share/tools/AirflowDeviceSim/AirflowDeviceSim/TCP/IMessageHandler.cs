using System;
using System.Collections.Generic;
using System.Text;

namespace AirflowDeviceSim.TCP
{
	

	public interface IMessageHandler
	{
		bool HandleMessage( Message msg );
	}
}
