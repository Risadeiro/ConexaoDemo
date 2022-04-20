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
            ArrayList<int[]> portAckMap = new ArrayList<int[]>();    //lista de conexoes e seus respectivos acks

            while(true){
                Gson gson = new Gson(); //instancia Gson
                byte[] recBuffer = new byte[1024];  //buffer de leitura
                DatagramPacket recPkt = new DatagramPacket(recBuffer, recBuffer.length);    //datagrama a ser recebido do cliente

                System.out.println("Esperando uma mensagem");

                serverSocket.receive(recPkt);   //recebe o datagrama do cliente
                String clientMessage = new String(recPkt.getData());    //faz o cast  do datagrama de bytes pra string
                clientMessage = clientMessage.substring(0, clientMessage.lastIndexOf('}')+1);   //remove a parte desenecessaria da string

                Mensagem msg = gson.fromJson(clientMessage, Mensagem.class);    //converte a string no objeto de mensagem
                int port = recPkt.getPort();

                byte[] sendBuff = new byte[1024];   //buffer de mensagem a ser enviada ao cliente

                InetAddress IPAddress = recPkt.getAddress();    //pega o endereço do cliente

                if(portAckMap.size()==0){   //adiciona o primeiro cliente e seu primeiro numero sequencia
                    System.out.println("Mensagem id \""+msg.id+
                            "\" recebida na ordem, entregando para a camada de aplicação");
                    portAckMap.add(new int[]{port, msg.id});

                }else{  //caso ja tenha clients

                    for(int i=0;i<portAckMap.size();i++){   //itera na lista de clients

                        if(port == portAckMap.get(i)[0]){   //pega o client que mandou a msg em questao

                            if(portAckMap.get(i)[1]>=msg.id){ // confere se essa mensagem ja foi recebida
                                System.out.println("Mensagem id "+msg.id+" recebida de forma duplicada");
                                msg.id=portAckMap.get(i)[1];

                            }else if(portAckMap.get(i)[1]==msg.id-1){ //confere se a mensagem que chegou tem ack seguinte a ultima recebida
                                System.out.println("Mensagem id \""+msg.id+
                                        "\" recebida na ordem, entregando para a camada de aplicação");
                                portAckMap.set(i, new int[]{port, msg.id});
                                Thread.sleep(1000);

                            }else{  //mensagem fora de ordem
                                System.out.print("Mensagem id \""+msg.id+
                                        "\" recebida fora de ordem, ainda não recebidos os identificadores [ ");

                                for(int j=portAckMap.get(i)[1] +1; j<msg.id;j++){
                                    System.out.print(j +" ");
                                }

                                System.out.println("]");
                                msg.id=portAckMap.get(i)[1];
                            };
                        }else if(i ==portAckMap.size()){    // novo client adicionado
                            portAckMap.add(new int[]{port, msg.id});
                            System.out.println("Mensagem id \""+msg.id+
                                    "\" recebida na ordem, entregando para a camada de aplicação");
                        }
                    }
                }

                String msgJson = new Gson().toJson(msg);    //converte o datagram pra uma string json
                sendBuff = msgJson.getBytes();   //converte o datagram de string json para array de bytes
                DatagramPacket sendPacket=new DatagramPacket(sendBuff, sendBuff.length, IPAddress, port);   //cria o pacote a ser enviado para o cliente
                serverSocket.send(sendPacket);  //envia o pacote para o cliente
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
