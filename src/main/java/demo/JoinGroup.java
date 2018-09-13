package demo;

import com.google.protobuf.InvalidProtocolBufferException;
import org.zeromq.ZMQ;
import proto.Wire;
import rethinkdb.GetData;

/**
 * STF占用设备java Demo
 *
 */
public class JoinGroup
{
    private static Wire.Envelope envelopeGroup(String email, String serial){
        Wire.Envelope.Builder envelope = Wire.Envelope.newBuilder();
        Wire.GroupMessage.Builder groupmessage = Wire.GroupMessage.newBuilder();

        Wire.OwnerMessage.Builder ownermessage = Wire.OwnerMessage.newBuilder();
        ownermessage.setEmail(email);
        ownermessage.setGroup(GetData.getUser(email).get("group").toString());
        ownermessage.setName(GetData.getUser(email).get("name").toString());
        Wire.DeviceRequirement.Builder devicerequirement = Wire.DeviceRequirement.newBuilder();
        devicerequirement.setName("serial");
        devicerequirement.setValue(serial);
        devicerequirement.setType(Wire.RequirementType.EXACT);

        groupmessage.setOwner(ownermessage);
        groupmessage.setTimeout(6000);
        groupmessage.addRequirements(devicerequirement);
        groupmessage.build();

        envelope.setType(Wire.Envelope.MessageType.GroupMessage);

        envelope.setMessage(groupmessage.build().toByteString());
        envelope.setChannel("tx.35a03f9f-a403-4a9a-995e-52420e390dd8");

        return envelope.build();
    }

    public static void main(String args[]) throws InvalidProtocolBufferException {

        String serial = "emulator-5554";
        String email = "test@test.com";
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket subber = context.socket(ZMQ.SUB);
        ZMQ.Socket pusher = context.socket(ZMQ.PUSH);

        subber.connect("tcp://127.0.0.1:7114");
        pusher.connect("tcp://127.0.0.1:7113");

        subber.subscribe("".getBytes());
        pusher.sendMore(GetData.getDevice(serial).get("channel").toString());
        pusher.send(envelopeGroup(email, serial).toByteArray());

        while (!Thread.currentThread().isInterrupted()) {
            String channel = subber.recvStr();
            System.out.println(channel);
            byte[] message = subber.recv();
            //System.out.println(new String(message));
            Wire.Envelope envelope = Wire.Envelope.parseFrom(message);
            switch (envelope.getType()){
                case JoinGroupMessage:
                    Wire.JoinGroupMessage joinGroupMessage = Wire.JoinGroupMessage.parseFrom(envelope.getMessage());
                    System.out.println(joinGroupMessage);
                    break;
                case GroupMessage:
                    Wire.GroupMessage groupMessage = Wire.GroupMessage.parseFrom(envelope.getMessage());
                    System.out.println(envelope);
                    System.out.println(groupMessage);
                    break;
                case LeaveGroupMessage:
                    Wire.LeaveGroupMessage leaveGroupMessage = Wire.LeaveGroupMessage.parseFrom(envelope.getMessage());
                    System.out.println(leaveGroupMessage.getSerial());
                    System.out.println(leaveGroupMessage.getReason());
                    System.out.println(leaveGroupMessage.getOwner().getEmail());
                    break;

                default:
                    //System.out.println("unknow type:" + envelope.getType());
                    break;
            }
        }

    }
}
