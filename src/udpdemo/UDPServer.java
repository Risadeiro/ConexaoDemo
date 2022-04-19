package udpdemo;

import com.google.gson.Gson;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class UDPServer {
    public static void main(String[] args) {
        try {
            DatagramSocket serverSocket = new DatagramSocket(9876);
            while(true){
                ArrayList<int[]> portAckMap = new ArrayList<int[]>();    //lista de conexoes e seus respectivos acks
                Gson gson = new Gson(); //instancia Gson
                byte[] recBuffer = new byte[1024];  //buffer de leitura
                DatagramPacket recPkt = new DatagramPacket(recBuffer, recBuffer.length);    //datagrama a ser recebido do cliente

                System.out.println("Esperando uma mensagem");

                serverSocket.receive(recPkt);   //recebe o datagrama do cliente
                String clientMessage = new String(recPkt.getData());    //faz o cast  do datagrama de bytes pra string
                clientMessage = clientMessage.substring(0, clientMessage.lastIndexOf('}')+1);   //remove a parte desenecessaria da string

                Mensagem msg = gson.fromJson(clientMessage, Mensagem.class);    //converte a string no objeto de mensagem
                int port = recPkt.getPort();

                System.out.println("Mensagem id:\n\t"+msg.id+
                        "\nrecebida pelo receiver\n\t"+ port);
                byte[] sendBuff = new byte[1024];   //buffer de mensagem a ser enviada ao cliente
                sendBuff = "sou o servidor".getBytes(); //cast para bytes da mensagem que vai para o cliente

                InetAddress IPAddress = recPkt.getAddress();    //pega o endereço do cliente

                for(int i=0;i<portAckMap.size();i++){
                    if(port == portAckMap.get(i)[0]){
                        if(portAckMap.get(i)[1]==msg.numSequencia-1){
                            portAckMap.set(i, new int[]{port, msg.numSequencia});
                        }else{
                            msg.numSequencia=portAckMap.get(i)[1];
                        };
                    }else if(i ==portAckMap.size()-1){
                        portAckMap.add(new int[]{port, msg.numSequencia});
                    }
                }

                DatagramPacket sendPacket=new DatagramPacket(sendBuff, sendBuff.length, IPAddress, port);   //cria o pacote a ser enviado para o cliente
                serverSocket.send(sendPacket);  //envia o pacote para o cliente
                System.out.println("mensagem enviada pelo server");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
