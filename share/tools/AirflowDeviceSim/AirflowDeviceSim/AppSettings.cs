using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml.Serialization;

namespace AirflowDeviceSim
{
    public class Server
    {
        [XmlAttribute]
        public String Name{ get;set;}
        [XmlAttribute]
        public String HostName;
    }

    public class DeviceMessage
    {
        [XmlAttribute]
        public String Name;

        [XmlAttribute]
        public String Id;

        [XmlAttribute]
        public bool ReceiveOnly;
    }


    public class Device
    {
        [XmlAttribute]
        public String Name{get;set;}
        [XmlAttribute]
        public int TcpPort;
        [XmlAttribute]
        public int DeviceId;
        [XmlAttribute]
        public int LocationId;
        [XmlAttribute]
        public bool NetworkByteOrder;
        [XmlAttribute]
        public bool Configurable;
        [XmlAttribute]
        public bool TcpServer;
        [XmlAttribute]
        public String Type;
    }

    public class ParamState
    {
        [XmlAttribute]
        public String Name;
        public Object Value;
    }


    public class MessageState
    {
        [XmlAttribute]
        public String Name;

        public  List<ParamState> Parameters;

        public MessageState()
        {
            Parameters = new List<ParamState>();
        }
    }

    public class Factory
    {
        [XmlAttribute]
        public String Name { get; set; }
        [XmlAttribute]
        public int FactoryId;
        [XmlAttribute]
        public bool SelectDefault;  
    }


    public class AppSettings
    {
        public int Version;
        public List<Server> Servers;
        public List<Device> Devices;
        public List<Factory> Factories;
        public List<DeviceMessage> DeviceMessages;
        public List<MessageState> MessageState1;
        public List<MessageState> MessageState2;
        public List<MessageState> MessageState3;

        public int SelectedServer;
        public int SelectedDevice;
        public int SelectedFactory;
        public int SelectedMessage1;
        public int SelectedMessage2;
        public int SelectedMessage3;
        public int Message1Interval = 10000;
        public int Message2Interval = 10000;
        public int Message3Interval = 10000;

        public AppSettings()
        {
            Version = 0;
           // MessageFactory = "Airflow_AirNz";
            Servers = new List<Server>();
            Devices = new List<Device>();
            Factories = new List<Factory>();
            DeviceMessages = new List<DeviceMessage>();
            MessageState1 = new List<MessageState>();
            MessageState2 = new List<MessageState>();
            MessageState3 = new List<MessageState>();
        }

        public void Prepopulate()
        {
            //KR:don't need to do this ,,,,

           // Servers.Add(new Server() { Name = "Dev", HostName = "localhost" });
           // Devices.Add(new Device { Name = "AKI Text", Configurable = true, DeviceId = 10, LocationId = 11, TcpPort = 26506 });
            //DeviceMessages.Add(new DeviceMessage() { Name = "Keep Alive", Id = "af_keep_alive" });
        }

        public bool MergeFrom(AppSettings _src)
        {
            bool merged = false;
           /* KR: 
            * if (_src.Version > Version)
            {
                foreach (DeviceMessage msg in _src.DeviceMessages)
                {
                    if( ! DeviceMessages.Any( m => m.Id == msg.Id ))
                    {
                        DeviceMessages.Add(msg);
                        merged = true;
                    }
                }
            }*/
            return merged;
        }

        public void SetServerHostName(String _name, String _hostName)
        {
            Server svr = Servers.FirstOrDefault(s => s.Name == _name);
            if (svr != null)
            {
                svr.HostName = _hostName;
            }
        }

        public void SetDeviceProperties(String _name, int _tcpPort, bool _networkByteOrder, bool _tcpServer)
        {
            Device dev = Devices.FirstOrDefault(d => d.Name == _name);
            if (dev != null)
            {
                dev.TcpPort = _tcpPort;
                dev.NetworkByteOrder = _networkByteOrder;
                dev.TcpServer = _tcpServer;
            }
        }

        public Server GetSelectedEnvironment()
        {
            if (Servers.Count > SelectedServer)
            {
                return Servers[SelectedServer];
            }
            return null;
        }

        public Device GetSelectedDevice()
        {
            if (Devices.Count > SelectedDevice)
            {
                return Devices[SelectedDevice];
            }
            return null;
        }
    }
}
