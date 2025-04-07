/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package cozinha;

import dao.RestauranteDAO;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author iagov
 */
public class Pedido extends javax.swing.JFrame {
     private RestauranteDAO restauranteDAO;
    private DefaultTableModel modelPedidos;
    
    public Pedido() {
    
       initComponents();
       restauranteDAO = new RestauranteDAO();
       configurarComboMesas();
       configurarTabela();
       atualizarPedidos();
       //iniciarAtualizacaoAutomatica();
       setSize(620, 440);
       setLocationRelativeTo(null);
       
       iniciarServidorSocket();
    }
    
    
   
   private void iniciarServidorSocket() {
    new Thread(() -> {
        try (ServerSocket servidor = new ServerSocket(12345)) {
            System.out.println("🍳 Cozinha escutando pedidos...");

            while (true) {
                System.out.println("🟡 Aguardando conexão...");
                Socket cliente = servidor.accept();
                System.out.println("🟢 Cliente conectado: " + cliente.getInetAddress());

                BufferedReader entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
                String mensagem = entrada.readLine();

                if (!mensagem.isEmpty()) {
                    System.out.println("📥 Pedido recebido via socket: " + mensagem);

                    SwingUtilities.invokeLater(() -> {
                        adicionarNaTabela(mensagem);
                    });
                } else {
                    System.out.println("⚠️ Nenhuma mensagem recebida.");
                }

                entrada.close();
                cliente.close();
            }
        } catch (IOException e) {
            System.out.println("❌ Erro no socket da cozinha:");
            e.printStackTrace();
        }
    }).start();
}






    private void adicionarNaTabela(String pedido) {
    DefaultTableModel model = (DefaultTableModel) tblPedidos.getModel();

    try {
        // Exemplo: "Mesa 1 - Coca-Cola 350ml x1"
        String[] partes = pedido.split(" - ");
        String mesa = partes[0];  // "Mesa 1"
        String[] produtoEQuantidade = partes[1].split(" x");
        String produto = produtoEQuantidade[0];  // "Coca-Cola 350ml"
        String quantidade = produtoEQuantidade[1];  // "1"

        // Obtem hora atual formatada
        String hora = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());

        model.addRow(new Object[] {
            "-",                  // ID
            produto,              // Produto
            mesa,                 // Mesa
            hora,                 // Hora
            "🧑‍🍳 Preparando",     // Status
            quantidade            // Quantidade
        });

    } catch (Exception e) {
        System.out.println("⚠️ Erro ao interpretar pedido recebido: " + pedido);
        e.printStackTrace();
    }
}


    private void configurarComboMesas() {
    cmbMesa.removeAllItems();
    cmbMesa.addItem("Todas as Mesas"); // Opção padrão
    
    // Adiciona mesas de 1 a 5 (ou conforme seu sistema)
    for (int i = 1; i <= 5; i++) {
        cmbMesa.addItem("Mesa " + i);
    }
    
    // Listener para filtrar quando a mesa for selecionada
    cmbMesa.addActionListener(e -> filtrarPorMesa());
}
    
    
    private void marcarPedidoPronto() {
    int row = tblPedidos.getSelectedRow();
    if (row == -1) {
        JOptionPane.showMessageDialog(this, 
            "Selecione um pedido primeiro!", 
            "Aviso", JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    int idPedido = (int) modelPedidos.getValueAt(row, 0);
    String produto = (String) modelPedidos.getValueAt(row, 1);
    String mesa = (String) modelPedidos.getValueAt(row, 2);
    String statusAtual = ((String) modelPedidos.getValueAt(row, 4)).toLowerCase();

    // Verifica se já está pronto
    if (statusAtual.contains("pronto")) {
        JOptionPane.showMessageDialog(this,
            "Este pedido já está marcado como pronto!",
            "Aviso", JOptionPane.INFORMATION_MESSAGE);
        return;
    }

    // Confirmação
    int confirm = JOptionPane.showConfirmDialog(this,
        "Marcar pedido como PRONTO?\n\n" +
        "Produto: " + produto + "\n" +
        mesa + "\n" +
        "ID: " + idPedido,
        "Confirmar Status",
        JOptionPane.YES_NO_OPTION);
    
    if (confirm == JOptionPane.YES_OPTION) {
        if (restauranteDAO.atualizarStatusPedido(idPedido, "pronto")) {
            JOptionPane.showMessageDialog(this,
                "Pedido #" + idPedido + " marcado como PRONTO!",
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            
            // Atualiza a tabela
            atualizarPedidos();
        } else {
            JOptionPane.showMessageDialog(this,
                "Erro ao atualizar status do pedido!",
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}

private void marcarPedidoEntregue() {
    int row = tblPedidos.getSelectedRow();
    if (row == -1) {
        JOptionPane.showMessageDialog(this, 
            "Selecione um pedido primeiro!", 
            "Aviso", JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    int idPedido = (int) modelPedidos.getValueAt(row, 0);
    String produto = (String) modelPedidos.getValueAt(row, 1);
    String mesa = (String) modelPedidos.getValueAt(row, 2);
    
    try {
        // Confirmação antes de marcar como entregue
        int confirm = JOptionPane.showConfirmDialog(this,
            "Confirmar entrega do pedido?\n" +
            "Produto: " + produto + "\n" +
            mesa,
            "Confirmar Entrega",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (restauranteDAO.atualizarStatusPedido(idPedido, "entregue")) {
                JOptionPane.showMessageDialog(this,
                    "Pedido marcado como entregue!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                
                // Atualizar a tabela
                atualizarPedidos();
                
                // Verificar se a mesa pode ser liberada
                int numeroMesa = Integer.parseInt(mesa.replace("Mesa ", ""));
                if (restauranteDAO.verificarPedidosPendentesMesa(numeroMesa) == 0) {
                    JOptionPane.showMessageDialog(this,
                        "Mesa " + numeroMesa + " liberada!",
                        "Informação", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                throw new Exception("Falha ao atualizar status no banco de dados");
            }
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this,
            "Erro ao marcar como entregue: " + e.getMessage(),
            "Erro", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}
    private void configurarTabela() {
        modelPedidos = new DefaultTableModel(
            new Object [][] {},
            new String [] {"ID", "PRODUTO", "MESA", "HORA", "STATUS", "QUANTIDADE"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Torna a tabela não editável
            }
        };
        tblPedidos.setModel(modelPedidos);
    }
    
    private void iniciarAtualizacaoAutomatica() {
        Timer timer = new Timer(5000, e -> atualizarPedidos()); // Atualiza a cada 5 segundos
        timer.start();
    }

    private void atualizarPedidos() {
    System.out.println("🔄 Atualizando pedidos na tabela...");
    
    String mesaSelecionada = (String) cmbMesa.getSelectedItem();
    int numeroMesa = 0;

    if (!mesaSelecionada.equals("Todas as Mesas")) {
        numeroMesa = Integer.parseInt(mesaSelecionada.replace("Mesa ", ""));
    }

    List<Map<String, Object>> pedidos = restauranteDAO.listarPedidosAtivos();
    System.out.println("📦 Total de pedidos ativos encontrados: " + pedidos.size());
    
    modelPedidos.setRowCount(0);
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    for (Map<String, Object> pedido : pedidos) {
        modelPedidos.addRow(new Object[]{
            pedido.get("id"),
            pedido.get("produto"),
            "Mesa " + pedido.get("mesa"),
            sdf.format(pedido.get("hora")),
            formatarStatus((String) pedido.get("status")),
            pedido.get("quantidade")
        });
    }
}


    private void filtrarPorMesa() {
        atualizarPedidos();
    }

    private String formatarStatus(String status) {
    switch(status.toLowerCase()) {
        case "recebido": return "<html><font color='orange'>⏳ Recebido</font></html>";
        case "preparando": return "<html><font color='#FFCC00'>👨‍🍳 Preparando</font></html>";
        case "pronto": return "<html><font color='green'>✅ Pronto</font></html>";
        case "entregue": return "<html><font color='blue'>🛎️ Entregue</font></html>";
        default: return status;
    }
}
    
    /**
     * Creates new form Pedido
     */
   
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tblPedidos = new javax.swing.JTable();
        btnPreparando = new javax.swing.JButton();
        btnMarcarEntregue = new javax.swing.JButton();
        cmbMesa = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);

        tblPedidos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Código", "Mesa", "Produto", "Tempo"
            }
        ));
        jScrollPane1.setViewportView(tblPedidos);

        getContentPane().add(jScrollPane1);
        jScrollPane1.setBounds(0, 0, 617, 334);

        btnPreparando.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        btnPreparando.setText("Preparando");
        btnPreparando.setActionCommand("Pronto");
        btnPreparando.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPreparandoActionPerformed(evt);
            }
        });
        getContentPane().add(btnPreparando);
        btnPreparando.setBounds(147, 340, 119, 30);

        btnMarcarEntregue.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        btnMarcarEntregue.setText("Entregue");
        btnMarcarEntregue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMarcarEntregueActionPerformed(evt);
            }
        });
        getContentPane().add(btnMarcarEntregue);
        btnMarcarEntregue.setBounds(377, 340, 101, 30);

        getContentPane().add(cmbMesa);
        cmbMesa.setBounds(31, 342, 76, 26);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/Fundo.jpg"))); // NOI18N
        jLabel1.setText("jLabel1");
        getContentPane().add(jLabel1);
        jLabel1.setBounds(0, 0, 700, 560);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnPreparandoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPreparandoActionPerformed
        // TODO add your handling code here:
        int row = tblPedidos.getSelectedRow();
    if (row == -1) {
        JOptionPane.showMessageDialog(this, "Selecione um pedido primeiro!", "Aviso", JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    int idPedido = (int) modelPedidos.getValueAt(row, 0);
    if (restauranteDAO.atualizarStatusPedido(idPedido, "preparando")) {
        JOptionPane.showMessageDialog(this, "Pedido em preparo!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        atualizarPedidos();
    } else {
        JOptionPane.showMessageDialog(this, "Erro ao atualizar pedido!", "Erro", JOptionPane.ERROR_MESSAGE);
    }
    }//GEN-LAST:event_btnPreparandoActionPerformed

    private void btnMarcarEntregueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMarcarEntregueActionPerformed
        // TODO add your handling code here:
        int row = tblPedidos.getSelectedRow();
    if (row == -1) {
        JOptionPane.showMessageDialog(this, "Selecione um pedido primeiro!", "Aviso", JOptionPane.WARNING_MESSAGE);
        return;
    }

    Object idObj = modelPedidos.getValueAt(row, 0); // ← pode ser String ou Integer

    if (idObj instanceof Integer) {
        int idPedido = (int) idObj;
        if (restauranteDAO.atualizarStatusPedido(idPedido, "entregue")) {
            String mesa = (String) modelPedidos.getValueAt(row, 2);
            int numeroMesa = Integer.parseInt(mesa.replace("Mesa ", ""));
            verificarMesaLiberada(numeroMesa);
            JOptionPane.showMessageDialog(this, "Pedido entregue!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            atualizarPedidos();
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar pedido!", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    } else {
        // ID não é válido (veio do socket, não está no banco), remove da tabela apenas
        modelPedidos.removeRow(row);
        JOptionPane.showMessageDialog(this, "SUCESSO", "Informação", JOptionPane.INFORMATION_MESSAGE);
    }
    }//GEN-LAST:event_btnMarcarEntregueActionPerformed
    private void verificarMesaLiberada(int numeroMesa) {
    if (restauranteDAO.verificarPedidosPendentesMesa(numeroMesa) == 0) {
        restauranteDAO.atualizarStatusMesa(numeroMesa, "livre");
    }
}
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Pedido.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Pedido.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Pedido.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Pedido.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Pedido().setVisible(true);
            }
        });
    } 

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnMarcarEntregue;
    private javax.swing.JButton btnPreparando;
    private javax.swing.JComboBox<String> cmbMesa;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblPedidos;
    // End of variables declaration//GEN-END:variables

    
}
