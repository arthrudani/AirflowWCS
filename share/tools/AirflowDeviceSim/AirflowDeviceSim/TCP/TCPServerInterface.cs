using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net;
using System.Net.Sockets;

namespace AirflowDeviceSim.TCP
{
    public class TCPServerInterface : TCPClientInterface
    {
        TcpListener TcpServer;

        public TCPServerInterface( IPAddress _ipAddress ,int port , IMessageFactory _factory ) : base( _ipAddress , port, _factory )
        {
        }


        public override void OnStart()
        {
           // base.OnStart();
            TcpServer = new TcpListener(IPAddress.Any , ServerPort);
            TcpServer.Start();

        }

        public override void  OnStop()
        {
            CloseConnection();
            TcpServer.Stop();
        }

        protected override bool ShouldTryConnect()
        {
            return !Connected || TcpServer.Pending();
        }

        protected override bool TryConnect()
        {
            if (TcpServer.Pending())
            {
                TCPConnection = TcpServer.AcceptTcpClient();
            }
            return TCPConnection != null && TCPConnection.Connected;
        }

    }
}
