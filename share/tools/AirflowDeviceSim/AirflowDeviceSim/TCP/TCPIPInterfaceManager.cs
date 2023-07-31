using System;
using System.Collections.Generic;
using System.Text;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using AirflowDeviceSim.TCP;

namespace AirflowDeviceSim.TCP
{
	public class TCPIPInterfaceManager
	{
		List<TCPClientInterface> Clients;	


		public TCPIPInterfaceManager()
		{
			Clients = new List<TCPClientInterface>();
		}


        public TCPServerInterface AddServer(IPAddress serverAddr, int port, IMessageFactory hdr)
        {
            TCPServerInterface client = new TCPServerInterface(serverAddr, port, hdr);
            Clients.Add(client);
            return client;
        }


		public TCPClientInterface AddClient( IPAddress serverAddr , int port , IMessageFactory hdr)
		{
			TCPClientInterface client = new TCPClientInterface( serverAddr , port , hdr );
			Clients.Add( client );
			return client;
		}

		
		public void StartClients()
		{
			foreach (TCPClientInterface client in Clients)
			{
				client.Start();
			}
		}

		public void StopClients( )
		{
			foreach( TCPClientInterface client in Clients )
			{
				client.Stop();
			}
		}

        public void RemoveClient(TCPClientInterface client)
        {
            Clients.Remove(client);
        }

	}
}
