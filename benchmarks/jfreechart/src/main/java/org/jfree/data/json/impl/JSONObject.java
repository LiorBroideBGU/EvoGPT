

package org.jfree.data.json.impl;


import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class JSONObject extends HashMap implements Map, JSONAware, 
        JSONStreamAware {
    
    private static final long serialVersionUID = -503443796854799292L;

    
    public static void writeJSONString(Map map, Writer out) throws IOException {
        if (map == null) {
            out.write("null");
            return;
        }
        boolean first = true;
        Iterator iter = map.entrySet().iterator();
        out.write('{');
        while (iter.hasNext()) {
            if (first) {
                first = false;
            }
            else {
                out.write(',');
            }
            Entry entry = (Entry) iter.next();
            out.write('\"');
            out.write(JSONValue.escape(String.valueOf(entry.getKey())));
            out.write('\"');
            out.write(':');
            JSONValue.writeJSONString(entry.getValue(), out);
        }
        out.write('}');
    }

    
    @Override
    public void writeJSONString(Writer out) throws IOException {
        writeJSONString(this, out);
    }
    
    
    public static String toJSONString(Map map){
        if (map == null) {
            return "null";
        }
        
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        Iterator iter = map.entrySet().iterator();
        
        sb.append('{');
        while (iter.hasNext()) {
            if (first) {
                first = false;
            }
            else {
                sb.append(',');
            }
            
            Entry entry = (Entry) iter.next();
            toJSONString(String.valueOf(entry.getKey()), entry.getValue(), sb);
        }
        sb.append('}');
        return sb.toString();
    }
    
    
    @Override
    public String toJSONString(){
        return toJSONString(this);
    }
    
    
    private static String toJSONString(String key, Object value, 
            StringBuffer sb) {
        sb.append('\"');
        if (key == null) {
            sb.append("null");
        }
        else {
            JSONValue.escape(key, sb);
        }
        sb.append('\"').append(':');
        
        sb.append(JSONValue.toJSONString(value));
        
        return sb.toString();
    }
    
    
    @Override
    public String toString(){
        return toJSONString();
    }

    
    public static String toString(String key, Object value){
        StringBuffer sb = new StringBuffer();
        toJSONString(key, value, sb);
        return sb.toString();
    }
}

