using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;

namespace AirflowDeviceSim.TCP
{

    public class MessageQueue
    {
        private Queue<Message> _MessageQ;
        private Mutex _MQLock;

        public MessageQueue()
        {
            _MessageQ = new Queue<Message>();
            _MQLock = new Mutex();
        }

        public void Enqueue(Message msg)
        {
            _MQLock.WaitOne();
            _MessageQ.Enqueue(msg);
            _MQLock.ReleaseMutex();
        }

        public Message Dequeue()
        {
            Message ret = null;
            _MQLock.WaitOne();
            if (_MessageQ.Count > 0)
            {
                ret = _MessageQ.Dequeue();
            }
            _MQLock.ReleaseMutex();
            return ret;
        }
    }

    //public  class MessageDispatch
    //{
    //    //private List<MessageHeaderBase> _SubscribedMessages;
    //    //IMessageProvider Provider;
    //    IMessageHandler Handler;
    //    private MessageQueue _ReceivedQ;

    //    public MessageDispatch( IMessageProvider provider, IMessageHandler handler , List<MessageHeaderBase> messages )
    //    {
    //        _ReceivedQ	 = new MessageQueue();
    //        //Provider = provider;
    //        Handler = handler ;
    //        //_SubscribedMessages =   messages;
    //    }

    //    public bool PeekMessages( )
    //    {
    //        bool bRet = false;
    //        Message msg = null;
    //        while( null != ( msg = _ReceivedQ.Dequeue() ) )
    //        {
    //            if( Handler.HandleMessage( msg ) )
    //            {
    //                bRet = true;
    //            }
    //        }
    //        return bRet;
    //    }

    //    public void Enqueue( Message msg )
    //    {
    //        _ReceivedQ.Enqueue( msg );
    //    }

    //    public bool Dispatch( MessageHeaderBase recvHdr , byte[] hdrData , byte[] msgData )
    //    {	
    //        bool bDone = false;
    //        foreach( MessageHeaderBase hdr in _SubscribedMessages )
    //        {
    //            Message message = null;
    //            if( hdr.EqualsTo( recvHdr ) )
    //            {
    //                message = Provider.GetMessage( hdr );
    //                if( null != message )
    //                {
    //                    int offset = 0;
    //                    message.Header.Read( hdrData );
    //                    //message.WriteLock.WaitOne();
    //                    message.Read( msgData , ref  offset );
    //                    //message.WriteLock.ReleaseMutex();
    //                    message.Finalize();
    //                    _ReceivedQ.Enqueue( message );
    //                    bDone = true;
    //                }
    //            }
    //        }
    //        return bDone;
    //    }
    //}
}
