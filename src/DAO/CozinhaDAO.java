/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import beans.Bebida;
import beans.Comida;
import beans.Sobremesas;
import main.Conexao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
/**
 *
 * @author iagov
 */
public class CozinhaDAO {
    private Connection conexao;
    
    public CozinhaDAO() {
        Conexao conexaoInstance = new Conexao();
        this.conexao = conexaoInstance.getConexao();
    }

    public boolean cadastrarProduto(String nome, double preco, String categoria, int quantidade) {
    String sql = "INSERT INTO cardapio (nome, preco, categoria, quantidade) VALUES (?, ?, ?, ?)";
    
    try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
        stmt.setString(1, nome);
        stmt.setDouble(2, preco);
        stmt.setString(3, categoria);
        stmt.setInt(4, quantidade);
        
        return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
        System.err.println("Erro ao cadastrar produto: " + e.getMessage());
        return false;
    }
}
    
    // Método para verificar se o produto já existe (opcional)
    public boolean produtoExiste(String nome) {
        String sql = "SELECT COUNT(*) FROM cardapio WHERE nome = ?";
        
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, nome);
            var resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Erro ao verificar produto: " + e.getMessage());
            return false;
        }
    }
     public boolean excluirProduto(int id) {
        String sql = "DELETE FROM cardapio WHERE id_item = ?";
        
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao excluir produto: " + e.getMessage());
            return false;
        }
    }
   public Object buscarProdutoPorId(int id) {
   String sql = "SELECT id_item, nome, preco, categoria, quantidade, disponivel FROM cardapio WHERE id_item = ?";
        
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String categoria = rs.getString("categoria").toLowerCase();
                String nome = rs.getString("nome");
                double preco = rs.getDouble("preco");
                int quantidade = rs.getInt("quantidade");
                boolean disponivel = rs.getBoolean("disponivel");
                int idItem = rs.getInt("id_item");

                // ... (criação do objeto conforme o tipo)
                // Não esqueça de setar a disponibilidade no objeto
            }
            return null;
        } catch (SQLException e) {
            System.err.println("Erro ao buscar produto: " + e.getMessage());
            return null;
        }
    }

     public boolean desabilitarProduto(int id) {
        String sql = "UPDATE cardapio SET disponivel = FALSE WHERE id_item = ?";
        
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao desabilitar produto: " + e.getMessage());
            return false;
        }
    }
    public boolean habilitarProduto(int id) {
        // Verifica primeiro se tem estoque
        String verificaEstoque = "SELECT quantidade FROM cardapio WHERE id_item = ?";
        String atualizaStatus = "UPDATE cardapio SET disponivel = TRUE WHERE id_item = ? AND quantidade > 0";
        
        try {
            // Verifica estoque
            PreparedStatement stmtVerifica = conexao.prepareStatement(verificaEstoque);
            stmtVerifica.setInt(1, id);
            ResultSet rs = stmtVerifica.executeQuery();
            
            if (rs.next()) {
                int quantidade = rs.getInt("quantidade");
                
                if (quantidade <= 0) {
                    return false; // Não pode habilitar
                }
                
                // Se tem estoque, atualiza
                PreparedStatement stmtAtualiza = conexao.prepareStatement(atualizaStatus);
                stmtAtualiza.setInt(1, id);
                return stmtAtualiza.executeUpdate() > 0;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Erro ao habilitar produto: " + e.getMessage());
            return false;
        }
    }
    
    public Map<String, Object> verificarProdutoParaHabilitacao(int id) {
        String sql = "SELECT quantidade, disponivel FROM cardapio WHERE id_item = ?";
        Map<String, Object> resultado = new HashMap<>();
        
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                resultado.put("quantidade", rs.getInt("quantidade"));
                resultado.put("disponivel", rs.getBoolean("disponivel"));
                return resultado;
            }
            return null;
        } catch (SQLException e) {
            System.err.println("Erro ao verificar produto: " + e.getMessage());
            return null;
        }
    }

     public boolean verificarDisponibilidade(int id) {
        String sql = "SELECT disponivel FROM cardapio WHERE id_item = ?";
        
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getBoolean("disponivel");
        } catch (SQLException e) {
            System.err.println("Erro ao verificar disponibilidade: " + e.getMessage());
            return false;
        }
    }
    public boolean produtoExistePorId(int id) {
    String sql = "SELECT COUNT(*) FROM cardapio WHERE id_item = ?";
    
    try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    } catch (SQLException e) {
        System.err.println("Erro ao verificar produto por ID: " + e.getMessage());
        return false;
    }
}
}
