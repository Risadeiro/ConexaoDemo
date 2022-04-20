package udpdemo;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class UDPClient {

    public static void main(String[] args) throws Exception {
        int tamJanela = 3;
        int foraDeOrdem = 2;
        ArrayList<Integer> tipoEnvio = new ArrayList<Integer>();    //lista de Mensagens na janela de envio
        ArrayList<Mensagem> janela = new ArrayList<Mensagem>();    //lista de Mensagens na janela de envio
        int serverId=-1;   //ultimo ack recebido do server
        Scanner sc = new Scanner(System.in);    //instancia de scanner
        DatagramSocket clientSocket = new DatagramSocket(); //socket do client
        clientSocket.setSoTimeout(3000);   //define o timeout do socket para respostas do server
        int id=new Random().nextInt(10+20)+10, //primeiro id
                numSequencia=0; //numero sequencia
        InetAddress IPAddress  = InetAddress.getByName("127.0.0.1");    //endereço do server
        String informacao;  //retorno do server
        byte[] recBuffer = new byte[1024];  //cria o buffer de leitura de resposta do receiver
        DatagramPacket recPkt = new DatagramPacket(recBuffer,recBuffer.length); //cria o dataframa para resposta do receiver
        Gson gson = new Gson(); //instancia Gson

        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));   //le mensagem do usuario
        while (true) {
            int breque=1;
            /*while(janela.size()<=3){
                String texto = inFromUser.readLine();   //recebe do usuario a mensagem a ser enviada
                Mensagem msg = new Mensagem(numSequencia, texto, id);   //monta o datagrama com numero de sequencia, mensagem e id
                janela.add(msg);
                id++;   //incrimenta o id para futura mensagem
                numSequencia++; //incrimenta o numero sequencia para uma futura mensagem
                System.out.println("Digite o número correspondente a opção de envio:" +
                        "\n\t0 - envio correto" +   //perfeito
                        "\n\t1 - envio lento" +     //perfeito
                        "\n\t2 - envio com perda de pacotes" +
                        "\n\t3 - envio com pacotes fora de ordem" +
                        "\n\t4 - envio com pacotes duplicados");
                Integer opcaoDeEnvio = sc.nextInt();    //recebe do usuario a condição de envio
                tipoEnvio.add(opcaoDeEnvio);
            }*/
            int threadId = 0;
            int opcaoDeEnvio = -1;  //inicializa a variavel do valor de tipo de envio

            if((janela.size()>0) && (foraDeOrdem!=1)){
                while(!janela.isEmpty()){
                    Thread.sleep(1000);
                    Mensagem msg = janela.get(0);
                    Mensagem serverRes;   //monta o datagrama com numero de sequencia, mensagem e id vindo do server

                    String msgJson = new Gson().toJson(msg);    //converte o datagram pra uma string json
                    byte[] sendData = msgJson.getBytes();   //converte o datagram de string json para array de bytes
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876); //cria o formato datagram com os dados anteiores e destino

                    System.out.println("Mensage \""+msg.mensagem+
                            "\" reenviada com id " + msg.id);
                    clientSocket.send(sendPacket);  //envia o datagrama para o destino
                    clientSocket.receive(recPkt);   //recebe a resposta do receiver
                    informacao = new String(recPkt.getData(),    //transcreve a resposta do receiver
                            recPkt.getOffset(),
                            recPkt.getLength());
                    informacao = informacao.substring(0, informacao.lastIndexOf('}')+1);   //remove a parte desenecessaria da string
                    serverRes = gson.fromJson(informacao, Mensagem.class);    //converte a string no objeto de mensagem
                    serverId=serverRes.id;   //seta o id recebido do server
                    System.out.println("Mensagem id " +serverRes.id+ " recebida pelo receiver");
                    if(serverId == janela.get(0).id){    //remove da janela as msgs com ack vindo do server
                        janela.remove(0);
                    }else{
                        breque =0;
                    }
                }
            }else{
                String texto = inFromUser.readLine();   //recebe do usuario a mensagem a ser enviada
                Mensagem msg = new Mensagem(numSequencia, texto, id);   //monta o datagrama com numero de sequencia, mensagem e id
                Mensagem serverRes;   //monta o datagrama com numero de sequencia, mensagem e id vindo do server
                String msgJson = new Gson().toJson(msg);    //converte o datagram pra uma string json
                byte[] sendData = msgJson.getBytes();   //converte o datagram de string json para array de bytes
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876); //cria o formato datagram com os dados anteiores e destino

                do{ //apresenta e escolhe a condição da mensagem a ser enviada
                    System.out.println("Digite o número correspondente a opção de envio:" +
                            "\n\t0 - envio correto" +   //perfeito
                            "\n\t1 - envio lento" +     //perfeito
                            "\n\t2 - envio com perda de pacotes" +  //funciona
                            "\n\t3 - envio com pacotes fora de ordem" +
                            "\n\t4 - envio com pacotes duplicados");
                    opcaoDeEnvio = sc.nextInt();    //recebe do usuario a condição de envio
                    switch (opcaoDeEnvio){
                        case 0:
                            System.out.println("Mensagem \""+texto+
                                    "\" enviada como [envio correto] com id " + msg.id);
                            clientSocket.send(sendPacket);  //envia o datagrama para o destino
                            janela.add(msg);    //adiciona a mensagem na jnela
                            id++;   //incrimenta o id para futura mensagem
                            numSequencia++; //incrimenta o numero sequencia para uma futura mensagem

                            clientSocket.receive(recPkt);   //recebe a resposta do receiver
                            informacao = new String(recPkt.getData(),    //transcreve a resposta do receiver
                                    recPkt.getOffset(),
                                    recPkt.getLength());
                            informacao = informacao.substring(0, informacao.lastIndexOf('}')+1);   //remove a parte desenecessaria da string
                            serverRes = gson.fromJson(informacao, Mensagem.class);    //converte a string no objeto de mensagem
                            serverId=serverRes.id;   //seta o id recebido do server
                            System.out.println("Mensagem id " +serverRes.id+ " recebida pelo receiver");
                            if(foraDeOrdem==1){
                                Mensagem msgForaDeOrdem = janela.get(janela.size()-1);
                                String msgJsonForaDeOrdem = new Gson().toJson(msgForaDeOrdem);    //converte o datagram pra uma string json
                                byte[] sendDataForaDeOrdem = msgJsonForaDeOrdem.getBytes();   //converte o datagram de string json para array de bytes
                                DatagramPacket sendPacketsendDataForaDeOrdem = new DatagramPacket(sendDataForaDeOrdem, sendDataForaDeOrdem.length, IPAddress, 9876); //cria o formato datagram com os dados anteiores e destino
                                clientSocket.send(sendPacketsendDataForaDeOrdem);  //envia o datagrama para o destino
                            }

                            foraDeOrdem=2;
                            break;
                        case 1:
                            System.out.println("Mensagem \""+texto+
                                    "\" enviada como [envio lento] com id " + msg.id);
                            janela.add(msg);    //adiciona a mensagem na janela
                            ThreadUdp td = new ThreadUdp("timeout?"+threadId);  //inicia a Thread do timeout
                            td.start(); //inicia contagem do timeout
                            Thread.sleep(5000); //adiciona delay de 5 segundos a entrega
                            clientSocket.send(sendPacket);  //envia o datagrama para o destino com atraso de 5 segundos
                            id++;   //altera o id para futura mensagem
                            numSequencia++; //incrimenta o numero sequencia para uma futura mensagem

                            clientSocket.receive(recPkt);   //recebe a resposta do receiver
                            informacao = new String(recPkt.getData(),    //transcreve a resposta do receiver
                                    recPkt.getOffset(),
                                    recPkt.getLength());
                            informacao = informacao.substring(0, informacao.lastIndexOf('}')+1);   //remove a parte desenecessaria da string
                            informacao = informacao.substring(0, informacao.lastIndexOf('}')+1);   //remove a parte desenecessaria da string
                            serverRes = gson.fromJson(informacao, Mensagem.class);    //converte a string no objeto de mensagem
                            serverId=serverRes.id;   //seta o id recebido do server

                            System.out.println("Mensagem id " +serverRes.id+ " recebida pelo receiver");
                            if(foraDeOrdem==1){
                                Mensagem msgForaDeOrdem = janela.get(janela.size()-1);
                                String msgJsonForaDeOrdem = new Gson().toJson(msgForaDeOrdem);    //converte o datagram pra uma string json
                                byte[] sendDataForaDeOrdem = msgJsonForaDeOrdem.getBytes();   //converte o datagram de string json para array de bytes
                                DatagramPacket sendPacketsendDataForaDeOrdem = new DatagramPacket(sendDataForaDeOrdem, sendDataForaDeOrdem.length, IPAddress, 9876); //cria o formato datagram com os dados anteiores e destino
                                clientSocket.send(sendPacketsendDataForaDeOrdem);  //envia o datagrama para o destino
                            }
                            foraDeOrdem=2;
                            break;
                        case 2:
                            System.out.println("Mensagem \""+texto+
                                    "\" enviada como [envio com perda de pacotes] com id " + msg.id);
                            //clientSocket.send(sendPacket);  nao envia o datagrama para o destino
                            janela.add(msg);    //adiciona msg n janela
                            id++;   //altera o id para futura mensagem
                            numSequencia++; //incrimenta o numero sequencia para uma futura mensagem
                            try{
                                clientSocket.receive(recPkt);   //espera a resposta do receiver
                            }catch (SocketTimeoutException ste){

                                System.out.print("Pacotes a serem reenviados [ ");
                                for(int i=serverId+1; i<id;i++){
                                    System.out.print(i +" ");
                                }
                                System.out.println("]");
                            }
                            if(foraDeOrdem==1){
                                Mensagem msgForaDeOrdem = janela.get(janela.size()-1);
                                String msgJsonForaDeOrdem = new Gson().toJson(msgForaDeOrdem);    //converte o datagram pra uma string json
                                byte[] sendDataForaDeOrdem = msgJsonForaDeOrdem.getBytes();   //converte o datagram de string json para array de bytes
                                DatagramPacket sendPacketsendDataForaDeOrdem = new DatagramPacket(sendDataForaDeOrdem, sendDataForaDeOrdem.length, IPAddress, 9876); //cria o formato datagram com os dados anteiores e destino
                                clientSocket.send(sendPacketsendDataForaDeOrdem);  //envia o datagrama para o destino
                            }
                            foraDeOrdem=2;
                            break;
                        case 3:
                            System.out.println("Mensagem \""+texto+
                                    "\" enviada como [envio com pacotes fora de ordem] com id " + msg.id);
                            foraDeOrdem=1;
                            janela.add(msg);    //adiciona msg n janela
                            id++;   //altera o id para futura mensagem
                            numSequencia++; //incrimenta o numero sequencia para uma futura mensagem
                            try{
                                clientSocket.receive(recPkt);   //espera a resposta do receiver
                            }catch (SocketTimeoutException ste){
                                System.out.print("Pacotes a serem reenviados [ ");
                                for(int i=serverId+1; i<id;i++){
                                    System.out.print(i +" ");
                                }
                                System.out.println("]");
                            }
                            break;
                        case 4:
                            System.out.println("Mensagem \""+texto+
                                    "\" enviada como [envio com pacotes duplicados] com id " + msg.id);

                            clientSocket.send(sendPacket);  //envia o datagrama para o destino
                            clientSocket.send(sendPacket);  //envia o datagrama para o destino
                            janela.add(msg);    //adiciona a mensagem na jnela
                            id++;   //incrimenta o id para futura mensagem
                            numSequencia++; //incrimenta o numero sequencia para uma futura mensagem

                            clientSocket.receive(recPkt);   //recebe a resposta do receiver
                            informacao = new String(recPkt.getData(),    //transcreve a resposta do receiver
                                    recPkt.getOffset(),
                                    recPkt.getLength());
                            informacao = informacao.substring(0, informacao.lastIndexOf('}')+1);   //remove a parte desenecessaria da string
                            serverRes = gson.fromJson(informacao, Mensagem.class);    //converte a string no objeto de mensagem
                            serverId=serverRes.id;   //seta o id recebido do server
                            System.out.println("Mensagem id " +serverRes.id+ " recebida pelo receiver");
                            if(foraDeOrdem==1){
                                Mensagem msgForaDeOrdem = janela.get(janela.size()-1);
                                String msgJsonForaDeOrdem = new Gson().toJson(msgForaDeOrdem);    //converte o datagram pra uma string json
                                byte[] sendDataForaDeOrdem = msgJsonForaDeOrdem.getBytes();   //converte o datagram de string json para array de bytes
                                DatagramPacket sendPacketsendDataForaDeOrdem = new DatagramPacket(sendDataForaDeOrdem, sendDataForaDeOrdem.length, IPAddress, 9876); //cria o formato datagram com os dados anteiores e destino
                                clientSocket.send(sendPacketsendDataForaDeOrdem);  //envia o datagrama para o destino
                            }

                            foraDeOrdem=2;
                            break;
                        default:
                            System.out.println("Digite um valor valido");
                    }
                }while(opcaoDeEnvio<0 || opcaoDeEnvio >4);
            }

            while(janela.size()>0 && breque==1){
                if(serverId >= janela.get(0).id){    //remove da janela as msgs com ack vindo do server
                    janela.remove(0);
                }else{
                    breque =0;
                }
            }
        }
    }
}
