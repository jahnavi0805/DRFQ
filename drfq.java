import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/* 

Packet ID represents the Flow ID number
startTime represents Actual Arrival Time of the packet
vStartTime represents Virtual Start time of the Packet(valid only for memoryless DRFQ)
vFinishTime represents Virtual Finish time of the packet(valid only for memoryless DRFQ)
reqs represents the requirements of different resources
vStartTimes represents virtual start times at different resources(valid only for Dovetailing DRFQ) 
vFinishTimes represents virtual finish times at different resources(valid only for Dovetailing DRFQ) 

*/
class Packet{
  int id;
  int startTime;
  int vStartTime;
  int vFinishTime;
  int[] vStartTimes;
  int[] vFinishTimes;
  ArrayList<Integer> reqs;
  //Constructor
  Packet(int id,int startTime,ArrayList<Integer> reqs){
    this.id=id;
    this.startTime=startTime;
    this.reqs=reqs;
    this.vStartTime=-1;
    this.vFinishTime=-1;
    this.vStartTimes=new int[reqs.size()];
    this.vFinishTimes=new int[reqs.size()];
    Arrays.fill(this.vStartTimes,-1);
    Arrays.fill(this.vFinishTimes,-1);
  }
  int getStartTime(){
    return this.startTime;
  }
  int getVStartTime(){
    return this.vStartTime;
  }
  int getId(){
      return this.id;
  }
}

public class drfq {
  /*
  Calculating Virtual Time for memoryless DRFQ based on the packet Arrival Time
  Assuming that the packet spends the time between vStartTime and vEndTime with the resources
  virtual time is computed as the maximum virtual start time of any packet p that is currently being serviced
  */
  static int calculateVirtualTimeforMemorylessDRFQ(Packet packet,HashMap<Integer,ArrayList<Packet>> packetMap){
    int virtualTime=0;
    int packetArrivaltime=packet.startTime;
    ArrayList<Integer> packetsAtCurrentTime=new ArrayList<>();
    for(Map.Entry<Integer,ArrayList<Packet>> entry:packetMap.entrySet()){
      ArrayList<Packet> packets=entry.getValue();
      for(int i=0;i<packets.size();i++){
        if(packets.get(i).vStartTime==-1)
          break;
        else if(packets.get(i).vStartTime>packetArrivaltime)
          break;
        if(packetArrivaltime>=packets.get(i).vStartTime && packetArrivaltime <= packets.get(i).vFinishTime){
          virtualTime=Math.max(virtualTime,packets.get(i).vStartTime);
        }
      }
    }
    return virtualTime;
  }
  
  //MemorylessDRFQ
  static void memorylessDRFQ(HashMap<Integer,ArrayList<Packet>> packetMap){
    ArrayList<Packet> allPackets = new ArrayList<>();
    for(Map.Entry<Integer,ArrayList<Packet>> entry:packetMap.entrySet()){
      ArrayList<Packet> packets=entry.getValue();
      for(int i=0;i<packets.size();i++){
        Packet packet=packets.get(i);
        if(i==0)
          packet.vStartTime = calculateVirtualTimeforMemorylessDRFQ(packet,packetMap);
        else
          packet.vStartTime = Math.max(calculateVirtualTimeforMemorylessDRFQ(packet,packetMap),packets.get(i-1).vFinishTime);
        packet.vFinishTime=packet.vStartTime+Collections.max(packet.reqs);
        allPackets.add(packet);
      }
    }
    Collections.sort(allPackets, new Comparator<Packet>() {
      @Override
      public int compare(Packet p1, Packet p2) {
          if(p1.getVStartTime() - p2.getVStartTime()!=0)
            return p1.getVStartTime() - p2.getVStartTime();
          else
            return p1.getId()-p2.getId();
      }
    });
    for(int i=0;i<allPackets.size();i++)
      System.out.println("Virual Start Time: "+allPackets.get(i).getVStartTime()+" Virual Finish Time: "+allPackets.get(i).vFinishTime +" PacketID: "+allPackets.get(i).id + " ");
  }

  /*
  Calculating Virtual Time for Dovetailing DRFQ based on the packet Arrival Time
  Assuming that the packet spends the time between vStartTime and vEndTime with the resources
  virtual time is computed as the maximum virtual start time of any packet p that is currently being serviced a resource j
  */

  static int calculateVirtualTimeforDoveTailingDRFQ(Packet packet,HashMap<Integer,ArrayList<Packet>> packetMap,int j){
    int virtualTime=0;
    int packetArrivaltime=packet.startTime;
    ArrayList<Integer> packetsAtCurrentTime=new ArrayList<>();
    for(Map.Entry<Integer,ArrayList<Packet>> entry:packetMap.entrySet()){
      ArrayList<Packet> packets=entry.getValue();
      for(int i=0;i<packets.size();i++){
        if(packets.get(i).vStartTimes[j]==-1)
          break;
        else if(packets.get(i).vStartTimes[j]>packetArrivaltime)
          break;
        if(packetArrivaltime>=packets.get(i).vStartTimes[j] && packetArrivaltime <= packets.get(i).vFinishTimes[j]){
          virtualTime=Math.max(virtualTime,packets.get(i).vStartTimes[j]);
        }
      }
    }
    return virtualTime;
  }
  //DoveTailing DRFQ
  static void DovetailingDRFQ(HashMap<Integer,ArrayList<Packet>> packetMap){
    ArrayList<Packet> allPackets = new ArrayList<>();
    for(Map.Entry<Integer,ArrayList<Packet>> entry:packetMap.entrySet()){
      ArrayList<Packet> packets=entry.getValue();
      for(int i=0;i<packets.size();i++){
        Packet packet=packets.get(i);
        for(int j=0;j<packet.reqs.size();j++){
          if(i==0)
            packet.vStartTimes[j] = calculateVirtualTimeforDoveTailingDRFQ(packet,packetMap,j);
          else
            packet.vStartTimes[j] = Math.max(packets.get(i-1).vFinishTimes[j],calculateVirtualTimeforDoveTailingDRFQ(packet,packetMap,j));
          packet.vFinishTimes[j]=packet.vStartTimes[j]+packet.reqs.get(j);
        }
        allPackets.add(packet);
      }
    }
    Collections.sort(allPackets, new Comparator<Packet>() {
      @Override
      public int compare(Packet p1, Packet p2) {
        int[] p1_vStartTimes = p1.vStartTimes;
        int[] p2_vStartTimes = p2.vStartTimes;
        Arrays.sort(p1_vStartTimes);
        Arrays.sort(p2_vStartTimes);
        int p1_length=p1_vStartTimes.length-1;
        int p2_length=p2_vStartTimes.length-1;
        while(p1_length>=0&&p2_length>=0){
          if(p1_vStartTimes[p1_length]!=p2_vStartTimes[p2_length])
            return p1_vStartTimes[p1_length]-p2_vStartTimes[p2_length];
          p1_length--;
          p2_length--;
        }
        return p1.getId()-p2.getId();
      }
    });
    for(int i=0;i<allPackets.size();i++)
      System.out.println(" PacketID: "+allPackets.get(i).id + " ");
  }

  //Main method
  public static void main(String[] args) {
    String line = "";
    String fileName="";
    String type = "memorylessDRFQ";
    HashMap<Integer,ArrayList<Packet>> hm=new HashMap<>();
    try {
      if(args.length==0)
        throw new Exception("Please specify the csv file which has packet flow id,arrival Time and the packet requirements");
      if(args.length>0)
        fileName=args[0];
      if(args.length>1)
        type=args[1];
      BufferedReader br = new BufferedReader(new FileReader(fileName));
      while ((line = br.readLine()) != null)
      {
        String[] packetCSV= line.split(","); 
        ArrayList<Integer> reqs=new ArrayList<>();
        String[] reqCSV=packetCSV[2].split(";");
        for(int i=0;i<reqCSV.length;i++){
          reqs.add(Integer.parseInt(reqCSV[i]));
        }
        int id=Integer.parseInt(packetCSV[0]);
        int arrivalTime=Integer.parseInt(packetCSV[1]);
        Packet packet=new Packet(id,arrivalTime,reqs);
        if(!hm.containsKey(id))
          hm.put(id,new ArrayList<Packet>(Arrays.asList(packet)));
        else{
          ArrayList<Packet> tmp=hm.get(id);
          tmp.add(packet);
          Collections.sort(tmp, new Comparator<Packet>() {
            @Override
            public int compare(Packet p1, Packet p2) {
              return p1.getStartTime() - p2.getStartTime();
            }
          });
          hm.put(id,tmp);
        }
      }
      if(type.toLowerCase().contains("memoryless"))
        memorylessDRFQ(hm);
      else
        DovetailingDRFQ(hm);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
}
