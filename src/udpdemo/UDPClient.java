package udpdemo;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class UDPClient {

    int tamJanela = 7;
    public static void main(String[] args) throws Exception {
        ArrayList<Mensagem> janela = new ArrayList<Mensagem>();    //lista de Mensagens na janela de envio
        Scanner sc = new Scanner(System.in);    //instancia de scanner
        DatagramSocket clientSocket = new DatagramSocket(); //socket do client
        clientSocket.setSoTimeout(10000);   //define o timeout do socket para respostas do server
        int id=new Random().nextInt(10+20)+10, //primeiro id
                numSequencia=0; //numero sequencia
        InetAddress IPAddress  = InetAddress.getByName("127.0.0.1");    //endereço do server
        String informacao;  //retorno do server
        byte[] recBuffer = new byte[1024];  //cria o buffer de leitura de resposta do receiver
        DatagramPacket recPkt = new DatagramPacket(recBuffer,recBuffer.length); //cria o dataframa para resposta do receiver

        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));   //le mensagem do usuario
        while (true) {
            int threadId = 0;
            int opcaoDeEnvio = -1;  //inicializa a variavel do valor de tipo de envio

            String texto = inFromUser.readLine();   //recebe do usuario a mensagem a ser enviada
            Mensagem msg = new Mensagem(numSequencia, texto, id);   //monta o datagrama com numero de sequencia, mensagem e id
            String msgJson = new Gson().toJson(msg);    //converte o datagram pra uma string json
            byte[] sendData = msgJson.getBytes();   //converte o datagram de string json para array de bytes
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876); //cria o formato datagram com os dados anteiores e destino

            do{ //apresenta e escolhe a condição da mensagem a ser enviada
                System.out.println("Digite o número correspondente a opção de envio:" +
                        "\n\t0 - envio corredo" +
                        "\n\t1 - envio lento" +
                        "\n\t2 - envio com perda de pacotes" +
                        "\n\t3 - envio com pacotes fora de ordem" +
                        "\n\t4 - envio com pacotes duplicados");
                opcaoDeEnvio = sc.nextInt();    //recebe do usuario a condição de envio
                switch (opcaoDeEnvio){
                    case 0:
                        System.out.println("mensagem enviada pra servidor: \n\t\""+texto+
                                "\"\ncom id:\n\t" + id);
                        clientSocket.send(sendPacket);  //envia o datagrama para o destino
                        janela.add(msg);    //adiciona a mensagem na jnela
                        id++;   //incrimenta o id para futura mensagem
                        numSequencia++; //incrimenta o numero sequencia para uma futura mensagem

                        clientSocket.receive(recPkt);   //recebe a resposta do receiver
                        informacao = new String(recPkt.getData(),    //transcreve a resposta do receiver
                                recPkt.getOffset(),
                                recPkt.getLength());
                        System.out.println("recebido do servidor: " +informacao);
                        break;
                    case 1:
                        System.out.println("mensagem enviada pra servidor: \n\t\""+texto+
                                "\"\ncomo\n\t[envio lento]\ncom id:\n\t" + id);
                        break;
                    case 2:
                        System.out.println("mensagem enviada pra servidor: \n\t\""+texto+
                                "\"\ncomo\n\t[envio com perda de pacotes]\ncom id:\n\t" + id);

                        //clientSocket.send(sendPacket);  nao envia o datagrama para o destino
                        janela.add(msg);
                        id++;   //altera o id para futura mensagem
                        numSequencia++; //incrimenta o numero sequencia para uma futura mensagem

                        clientSocket.receive(recPkt);   //recebe a resposta do receiver
                        informacao = new String(recPkt.getData(),    //transcreve a resposta do receiver
                                recPkt.getOffset(),
                                recPkt.getLength());
                        System.out.println("recebido do servidor: " +informacao);

                        break;
                    case 3:
                        System.out.println("mensagem enviada pra servidor: \n\t\""+texto+
                                "\"\ncomo\n\t[envio com pacotes fora de ordem]\ncom id:\n\t" + id);
                        break;
                    case 4:
                        System.out.println("mensagem enviada pra servidor: \n\t\""+texto+
                                "\"\ncomo\n\t[envio com pacotes duplicados]\ncom id:\n\t" + id);
                        break;
                    default:
                        System.out.println("Digite um valor valido");
                }
            }while(opcaoDeEnvio<0 || opcaoDeEnvio >4);

            if(opcaoDeEnvio==1){
                ThreadUdp td = new ThreadUdp("timeout?"+threadId);
                td.start();
                Thread.sleep(5000);
                janela.add(msg);    //adiciona a mensagem na janela
                clientSocket.send(sendPacket);  //envia o datagrama para o destino com atraso de 5 segundos
                id++;   //altera o id para futura mensagem
                numSequencia++; //incrimenta o numero sequencia para uma futura mensagem
            }
            if(opcaoDeEnvio==2){
                Thread.sleep(5000);
                //clientSocket.send(sendPacket);  nao envia o datagrama para o destino
                janela.add(msg);
                id++;   //altera o id para futura mensagem
                numSequencia++; //incrimenta o numero sequencia para uma futura mensagem

                clientSocket.receive(recPkt);   //recebe a resposta do receiver
                informacao = new String(recPkt.getData(),    //transcreve a resposta do receiver
                        recPkt.getOffset(),
                        recPkt.getLength());
                System.out.println("recebido do servidor: " +informacao);
            }
            if (opcaoDeEnvio == 3) {

            }
            if(opcaoDeEnvio==4){
                clientSocket.send(sendPacket);  //envia o datagrama para o destino com atraso de 5 segundos
                clientSocket.send(sendPacket);  //envia o datagrama para o destino com atraso de 5 segundos
            }
        }
        //clientSocket.close();
    }
}
