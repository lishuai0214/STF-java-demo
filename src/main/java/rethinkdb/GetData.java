package rethinkdb;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.gen.exc.ReqlError;
import com.rethinkdb.gen.exc.ReqlQueryLogicError;
import com.rethinkdb.model.MapObject;
import com.rethinkdb.net.Connection;

import java.util.HashMap;

public class GetData {
    static final RethinkDB r = RethinkDB.r;
    static final Connection conn = r.connection().hostname("localhost").port(28015).connect();

    public static HashMap getDevice(String serial){
       return r.db("stf").table("devices").get(serial).run(conn);
    }

    public static HashMap getUser(String email){
        return r.db("stf").table("users").get(email).run(conn);
    }

    public static void main(String args[]) {
        System.out.println(getDevice("emulator-5554"));
    }

}
