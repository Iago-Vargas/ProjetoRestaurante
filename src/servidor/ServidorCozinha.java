/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author iagov
 */
public class ServidorCozinha {
    public static void main(String[] args) {
        try (ServerSocket servidor = new ServerSocket(12345)) {
            System.out.println("Cozinha aguardando pedidos...");

            while (true) {
                Socket cliente = servidor.accept();
                new Thread(() -> {
                    try (BufferedReader entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()))) {
                        String pedido;
                        while ((pedido = entrada.readLine()) != null) {
                            System.out.println("📥 Pedido recebido: " + pedido);
                            
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
