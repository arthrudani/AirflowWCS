using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace AirflowDeviceSim.TCP
{
    public class MessageBase
    {


        Dictionary<String, BaseParameter> Fields;

        private int mLength;

        public int Length
        {
            get
            {
                if( mLength > 0 )
                    return mLength;
                else
                    return Fields.Values.Sum(f => f.Length);
            }
            set
            {
                mLength = value;
            }
        }

        public IEnumerable<BaseParameter> Parameters
        {
            get
            {
                return Fields.Values;
            }
        }


        public List<BaseParameter> Params
        {
            get
            {
                List<BaseParameter> list = new List<BaseParameter>();
                foreach (var p in Fields.Values)
                {
                    list.Add(p);
                }
                return list;
            }
        }


        public BaseParameter this[String _name]
        {
            get
            {
                if (Fields.ContainsKey(_name))
                {
                    return Fields[_name];
                }
                return null;
            }
        }


        public MessageBase()
        {
            Fields = new Dictionary<string, BaseParameter>();
        }

        //public T AddField<T>( String _id, bool _readOnly = false ) where T: BaseParameter , new()
        //{
        //    return AddField<T>(_id, _id, _readOnly); 
        //}


        public T AddField<T>( String _name , bool _readOnly = false ) where T: BaseParameter , new()
        {
            T f = new T();
            f.Name = _name;
            f.ReadOnly = _readOnly;
            Fields[f.Name] = f;
            return f;
        }

        public T GetField<T>(String _name) where T : BaseParameter
        {
            if( Fields.ContainsKey( _name ))
            {
                return Fields[_name] as T;
            }
            return null;
        }
                

        public override string ToString()
        {
            String str = String.Empty;
            foreach (BaseParameter p in Fields.Values)
            {
                str += p.ToString();
            }
            return str;
        }

        virtual public void WrapUp()
        { }

        virtual public int Read(byte[] buffer, bool netByteOrder)
        {
            int offset = 0;
            foreach( BaseParameter p in Fields.Values )
            {
                p.Read(buffer, offset, netByteOrder);
                offset += p.Length;
            }
            return offset;
        }
        virtual public int Write(byte[] buffer, bool netByteOrder)
        {
            int offset = 0;
            foreach (BaseParameter p in Fields.Values)
            {
                p.Write(buffer, offset, netByteOrder);
                offset += p.Length;
            }
            return offset;
        }


    }
}
