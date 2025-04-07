package dao;

import beans.Comida;
import beans.Bebida;
import beans.Sobremesas;
import main.Conexao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;

public class RestauranteDAO {
    private Conexao conexao;
    private Connection conn;

    public RestauranteDAO() {
        this.conexao = new Conexao();
        this.conn = this.conexao.getConexao();
    }

    // Método para buscar comidas
    public List<Comida> buscarComidas() {
    String sql = "SELECT * FROM cardapio WHERE categoria = 'comida' AND disponivel = TRUE";
        List<Comida> comidas = new ArrayList<>();
        
        try {
            PreparedStatement stmt = this.conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Comida comida = new Comida(
                    rs.getInt("id_item"),
                    rs.getString("nome"),
                    rs.getDouble("preco"),
                    rs.getString("categoria"),
                    rs.getInt("quantidade"),
                    rs.getBoolean("disponivel")
                );
                comidas.add(comida);
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Erro ao buscar comidas: " + ex.getMessage());
        }
        
        return comidas;
    }

    // Método para buscar bebidas
    public List<Bebida> buscarBebidas() {
    String sql = "SELECT * FROM cardapio WHERE categoria = 'bebida' AND disponivel = TRUE";
        List<Bebida> bebidas = new ArrayList<>();
        
        try {
            PreparedStatement stmt = this.conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Bebida bebida = new Bebida(
                    rs.getInt("id_item"),
                    rs.getString("nome"),
                    rs.getDouble("preco"),
                    rs.getString("categoria"),
                    rs.getInt("quantidade"),
                    rs.getBoolean("disponivel")
                );
                bebidas.add(bebida);
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Erro ao buscar bebidas: " + ex.getMessage());
        }
        
        return bebidas;
    }

    // Método para buscar sobremesas
    public List<Sobremesas> buscarSobremesas() {
    String sql = "SELECT * FROM cardapio WHERE categoria = 'sobremesa' AND disponivel = TRUE";
        List<Sobremesas> sobremesas = new ArrayList<>();
        
        try {
            PreparedStatement stmt = this.conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Sobremesas sobremesa = new Sobremesas(
                    rs.getInt("id_item"),
                    rs.getString("nome"),
                    rs.getDouble("preco"),
                    rs.getString("categoria"),
                    rs.getInt("quantidade"),
                    rs.getBoolean("disponivel")
                );
                sobremesas.add(sobremesa);
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Erro ao buscar sobremesas: " + ex.getMessage());
        }
        
        return sobremesas;
    }

    // Método para inserir um novo pedido
    public boolean inserirPedido(int idMesa, int idItem, int quantidade) {
    // 1. Verificar se o produto está disponível
    if (!verificarDisponibilidade(idItem)) {
        JOptionPane.showMessageDialog(null, "Este produto não está disponível no momento", 
                "Produto Indisponível", JOptionPane.WARNING_MESSAGE);
        return false;
    }
    
    // 2. Verificar ingredientes
    if (!verificarIngredientesSuficientes(idItem, quantidade)) {
        return false;
    }
    
    // 2. Se tudo ok, inserir pedido e debitar estoque
    Connection conn = null;
    try {
        conn = this.conexao.getConexao();
        conn.setAutoCommit(false); // Inicia transação
        
        // Inserir pedido
        String sqlPedido = "INSERT INTO pedidos (id_mesa, id_item, quantidade) VALUES (?, ?, ?)";
        try (PreparedStatement stmtPedido = conn.prepareStatement(sqlPedido)) {
            stmtPedido.setInt(1, idMesa);
            stmtPedido.setInt(2, idItem);
            stmtPedido.setInt(3, quantidade);
            stmtPedido.executeUpdate();
        }
        
        // Atualizar estoque
        String sqlEstoque = "UPDATE ingredientes ing " +
                           "JOIN item_ingredientes ii ON ing.id_ingrediente = ii.id_ingrediente " +
                           "SET ing.quantidade = ing.quantidade - (ii.quantidade_necessaria * ?) " +
                           "WHERE ii.id_item = ?";
        try (PreparedStatement stmtEstoque = conn.prepareStatement(sqlEstoque)) {
            stmtEstoque.setInt(1, quantidade);
            stmtEstoque.setInt(2, idItem);
            stmtEstoque.executeUpdate();
        }
        
        conn.commit();
        return true;
    } catch (SQLException e) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Erro no rollback: " + ex.getMessage());
            }
        }
        System.err.println("Erro ao inserir pedido: " + e.getMessage());
        return false;
    } finally {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                System.err.println("Erro ao fechar conexão: " + e.getMessage());
            }
        }
    }
}
    
    public boolean verificarIngredientesSuficientes(int idItem, int quantidadePedido) {
    String sql = "SELECT i.nome, ii.quantidade_necessaria * ? AS necessario, ing.quantidade AS disponivel " +
                 "FROM item_ingredientes ii " +
                 "JOIN ingredientes ing ON ii.id_ingrediente = ing.id_ingrediente " +
                 "JOIN cardapio i ON ii.id_item = i.id_item " +
                 "WHERE ii.id_item = ? AND ing.quantidade < (ii.quantidade_necessaria * ?)";
    
    try (Connection conn = this.conexao.getConexao();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
         
        stmt.setInt(1, quantidadePedido);
        stmt.setInt(2, idItem);
        stmt.setInt(3, quantidadePedido);
        
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            // Se encontrou algum ingrediente insuficiente
            StringBuilder mensagem = new StringBuilder("Ingredientes insuficientes:\n");
            do {
                mensagem.append(String.format("- %s (Necessário: %.1f %s, Disponível: %.1f %s)\n",
                    rs.getString("nome"),
                    rs.getDouble("necessario"),
                    getUnidadeMedida(rs.getString("nome")),
                    rs.getDouble("disponivel"),
                    getUnidadeMedida(rs.getString("nome"))));
            } while (rs.next());
            
            JOptionPane.showMessageDialog(null, mensagem.toString(), "Estoque Insuficiente", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    } catch (SQLException e) {
        System.err.println("Erro ao verificar ingredientes: " + e.getMessage());
        return false;
    }
}

    private String getUnidadeMedida(String nomeIngrediente) {
        // Implemente conforme suas unidades de medida
        if (nomeIngrediente.contains("bebida")) return "ml";
        if (nomeIngrediente.contains("pão")) return "un";
        return "un"; // padrão
    }
    public int buscarIdPorNome(String nomeItem) throws SQLException {
        String sql = "SELECT id_item FROM cardapio WHERE nome = ?";

        try (Connection conn = this.conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nomeItem);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id_item");
            }
            throw new SQLException("Item não encontrado: " + nomeItem);
        }
    }
    public boolean verificarDisponibilidade(int idItem) {
    String sql = "SELECT disponivel FROM cardapio WHERE id_item = ?";
    
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, idItem);
        ResultSet rs = stmt.executeQuery();
        return rs.next() && rs.getBoolean("disponivel");
    } catch (SQLException e) {
        System.err.println("Erro ao verificar disponibilidade: " + e.getMessage());
        return false;
    }
}
    public int obterUltimoPedidoInserido(int mesa) throws SQLException {
    String sql = "SELECT id_pedido FROM pedidos WHERE id_mesa = ? ORDER BY data_hora DESC LIMIT 1";
    
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, mesa);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("id_pedido");
        }
    }
    return -1;
}

   public List<Map<String, Object>> listarPedidosAtivos() {
    String sql = "SELECT p.id_pedido, c.nome AS produto, p.id_mesa AS mesa, " +
                 "p.data_hora AS hora, p.status, p.quantidade " +
                 "FROM pedidos p JOIN cardapio c ON p.id_item = c.id_item " +
                 "WHERE p.status IN ('pendente', 'preparando') " + // Não mostra 'pronto'
                 "ORDER BY p.data_hora ASC";

    List<Map<String, Object>> pedidos = new ArrayList<>();

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        System.out.println("Executando query: " + sql);
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            Map<String, Object> pedido = new HashMap<>();
            pedido.put("id", rs.getInt("id_pedido"));
            pedido.put("produto", rs.getString("produto"));
            pedido.put("mesa", rs.getInt("mesa"));
            pedido.put("hora", rs.getTimestamp("hora"));
            pedido.put("status", rs.getString("status"));
            pedido.put("quantidade", rs.getInt("quantidade"));
            pedidos.add(pedido);
        }
        System.out.println("Total de pedidos encontrados: " + pedidos.size());
    } catch (SQLException e) {
        System.err.println("Erro em listarPedidosAtivos(): " + e.getMessage());
        e.printStackTrace();
    }
    return pedidos;
}


    // Método para atualizar status do pedido
    public boolean atualizarStatusPedido(int idPedido, String novoStatus) {
    System.out.println("Tentando atualizar pedido " + idPedido + " para status: " + novoStatus);
    
    String sql = "UPDATE pedidos SET status = ? WHERE id_pedido = ?";
    
    try (Connection conn = this.conexao.getConexao();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
         
        stmt.setString(1, novoStatus);
        stmt.setInt(2, idPedido);
        
        int rowsAffected = stmt.executeUpdate();
        System.out.println("Linhas afetadas: " + rowsAffected);
        
        if (rowsAffected > 0 && novoStatus.equalsIgnoreCase("entregue")) {
            // Atualizar status da mesa se necessário
            atualizarStatusMesaAposEntrega(idPedido);
        }
        
        return rowsAffected > 0;
    } catch (SQLException e) {
        System.err.println("Erro ao atualizar status do pedido " + idPedido + " para " + novoStatus + ":");
        e.printStackTrace();
        return false;
    }
}

private void atualizarStatusMesaAposEntrega(int idPedido) {
    String sql = "UPDATE mesas m " +
                "JOIN pedidos p ON m.numero = p.id_mesa " +
                "SET m.status = 'livre' " +
                "WHERE p.id_pedido = ? AND " +
                "(SELECT COUNT(*) FROM pedidos WHERE id_mesa = p.id_mesa AND status != 'entregue') = 0";
    
    try (Connection conn = this.conexao.getConexao();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
         
        stmt.setInt(1, idPedido);
        int updated = stmt.executeUpdate();
        System.out.println("Mesas liberadas: " + updated);
    } catch (SQLException e) {
        System.err.println("Erro ao atualizar status da mesa:");
        e.printStackTrace();
    }
}
    
    public int verificarPedidosPendentesMesa(int numeroMesa) {
    String sql = "SELECT COUNT(*) AS total FROM pedidos " +
                 "WHERE id_mesa = ? AND status != 'entregue'";
    
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, numeroMesa);
        ResultSet rs = stmt.executeQuery();
        return rs.next() ? rs.getInt("total") : 0;
    } catch (SQLException e) {
        System.err.println("Erro ao verificar pedidos pendentes: " + e.getMessage());
        return -1;
    }
}

public boolean atualizarStatusMesa(int numeroMesa, String status) {
    String sql = "UPDATE mesas SET status = ? WHERE numero = ?";
    
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, status);
        stmt.setInt(2, numeroMesa);
        return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
        System.err.println("Erro ao atualizar status da mesa: " + e.getMessage());
        return false;
    }
}
}